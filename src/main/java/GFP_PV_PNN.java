/*
 * Find PNN cells (PNN) and PV coloc
 * compute nuclear foci in PNN/PV cells
 * Author Philippe Mailly
 */

import GFP_PV_PNN_Tools.Cells_PV;
import GFP_PV_PNN_Tools.Tools;
import ij.*;
import ij.plugin.PlugIn;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.in.ImporterOptions;
import mcib3d.geom2.Objects3DIntPopulation;
import org.apache.commons.io.FilenameUtils;
import org.scijava.util.ArrayUtils;



public class GFP_PV_PNN implements PlugIn {
    
    Tools tools = new Tools();
    
    private String imageDir = "";
    public String outDirResults = "";
    private boolean canceled = false;
    public String file_ext = "czi";
    public BufferedWriter global_results_analyze;
    public BufferedWriter cells_results_analyze;
   
    
    /**
     * 
     * @param arg
     */
    @Override
    public void run(String arg) {
        try {
            imageDir = IJ.getDirectory("Choose directory containing image files...");
            if (imageDir == null) {
                return;
            }   
            // Find images with extension
            file_ext = tools.findImageType(new File(imageDir));
            ArrayList<String> imageFiles = tools.findImages(imageDir, file_ext);
            if (imageFiles == null) {
                IJ.showMessage("Error", "No images found with "+file_ext+" extension");
                return;
            }
            // create output folder
            outDirResults = imageDir + File.separator+ "Results"+ File.separator;
            File outDir = new File(outDirResults);
            if (!Files.exists(Paths.get(outDirResults))) {
                outDir.mkdir();
            }
            
            // create OME-XML metadata store of the latest schema version
            ServiceFactory factory;
            factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();
            ImageProcessorReader reader = new ImageProcessorReader();
            reader.setMetadataStore(meta);
            // Find channel names , calibration
            reader.setId(imageFiles.get(0));
            tools.cal = tools.findImageCalib(meta);
            String[] chsName = tools.findChannels(imageFiles.get(0), meta, reader);
            
            
            // Channels dialog
            
            String[] channels = tools.dialog(chsName);
            if ( channels == null || tools.canceled) {
                IJ.showStatus("Plugin cancelled");
                return;
            }
            // Write header
            
            String header = "Image Name\t#PV Cell\tPV Vol\tPV Int\tPV Int in GFP\t#Foci GFP PV cell\tFoci GFP Vol PV cell\tFoci GFP Int PV cell"
                + "\tPV Int in DAPI\t#Foci DAPI PV cell\tFoci DAPI Vol PV cell\tFoci DAPI Int PV cell\tPV is PNN\t#PNN Cell\tPNN Vol\tPNN Int\tPNN Int in GFP"
                + "\t#Foci GFP PNN cell\tFoci GFP Vol PNN cell\tFoci GFP Int PNN cell\tPNN Int in DAPI\t#Foci DAPI PNN cell\tFoci DAPI Vol PNN cell"
                + "\tFoci DAPI Int PNN cell\n";
            FileWriter fwCells = new FileWriter(outDirResults + "GFP_PV_PNN-Cells_Results.xls", false);
            cells_results_analyze = new BufferedWriter(fwCells);
            cells_results_analyze.write(header);
            cells_results_analyze.flush();
            
            for (String f : imageFiles) {
                reader.setId(f);
                String rootName = FilenameUtils.getBaseName(f);
                // Find xml points file
                ImporterOptions options = new ImporterOptions();
                options.setId(f);
                options.setSplitChannels(true);
                options.setQuiet(true);
                options.setColorMode(ImporterOptions.COLOR_MODE_GRAYSCALE);
                 
                ArrayList<Cells_PV> pvCellsList = new ArrayList();
                // open PV Channel
                System.out.println("--- Opening PV channel  ...");
                int indexCh = ArrayUtils.indexOf(chsName,channels[0]);
                ImagePlus imgPV = BF.openImagePlus(options)[indexCh];
                
                // Find PV cells with cellpose
                Objects3DIntPopulation pvPop = tools.cellPoseCellsPop(imgPV);
                int pvCells = pvPop.getNbObjects();
                System.out.println(pvCells +" PV cells found");
                
                // Add parameters
                tools.pvCellsParameters (pvPop, pvCellsList, imgPV, "PV");
                tools.flush_close(imgPV);
                
                // open PNN Channel
                System.out.println("--- Opening PNN channel  ...");
                indexCh = ArrayUtils.indexOf(chsName,channels[1]);
                ImagePlus imgPNN = BF.openImagePlus(options)[indexCh];
                
                // Find PNN cells with cellpose
                Objects3DIntPopulation pnnPop = tools.cellPoseCellsPop(imgPNN);
                int pnnCells = pnnPop.getNbObjects();
                System.out.println(pnnCells +" PNN cells found");
                
                // Add parameters
                tools.pvCellsParameters (pnnPop, pvCellsList, imgPNN, "PNN");
                
                // Find PV coloc with PNN cells
                tools.findColocCells(pvPop, pnnPop, pvCellsList);
                tools.flush_close(imgPNN);
               
                // Finding foci GFP in PV cells
                System.out.println("--- Opening GFP channel  ...");
                indexCh = ArrayUtils.indexOf(chsName,channels[2]);
                ImagePlus imgGfpFoci = BF.openImagePlus(options)[indexCh];
                
                // pv GFP Foci
                Objects3DIntPopulation pvFociGfpPop = tools.stardistFociInCellsPop(imgGfpFoci, pvPop, "PV", "GFP", pvCellsList);
                                                
                // pnn GFP Foci
                Objects3DIntPopulation pnnFociGfpPop = tools.stardistFociInCellsPop(imgGfpFoci, pnnPop, "PNN", "GFP", pvCellsList);
                tools.flush_close(imgGfpFoci);
                
                // Open DAPI channel
                System.out.println("--- Opening DAPI channel  ...");
                indexCh = ArrayUtils.indexOf(chsName,channels[3]);
                ImagePlus imgDapiFoci = BF.openImagePlus(options)[indexCh];
               
                // pv DAPI Foci
                Objects3DIntPopulation pvFociDapiPop = tools.stardistFociInCellsPop(imgDapiFoci, pvPop, "PV", "DAPI", pvCellsList);
                
                // pnn DAPI Foci
                Objects3DIntPopulation pnnFociDapiPop = tools.stardistFociInCellsPop(imgDapiFoci, pnnPop, "PNN", "DAPI", pvCellsList);
                
                
                // Save image objects
                tools.saveImgObjects(pvPop, pvFociGfpPop, pvFociDapiPop, pnnPop,  pnnFociGfpPop, pnnFociDapiPop, rootName, imgDapiFoci, outDirResults);
                tools.flush_close(imgDapiFoci);

                // write cells data
                int index = 0;
                for (Cells_PV pvCell : pvCellsList) {
                    cells_results_analyze.write(rootName+"\t"+pvCell.getPvCellLabel()+"\t"+pvCell.getPvCellVol()+"\t"+pvCell.getPvCellInt()+"\t"+
                        pvCell.getPvCellGFPInt()+"\t"+pvCell.getPvNbGFPFoci()+"\t"+pvCell.getPvGFPFociVol()+"\t"+pvCell.getPvGFPFociInt()+"\t"+
                        pvCell.getPvCellDapiInt()+"\t"+pvCell.getPvNbDapiFoci()+"\t"+pvCell.getPvDapiFociVol()+"\t"+pvCell.getPvFociDapiInt()+"\t"+
                        pvCell.getPvIsPNN()+"\t"+pvCell.getPnnCellLabel()+"\t"+pvCell.getPnnCellVol()+"\t"+pvCell.getPnnCellInt()+"\t"+
                        pvCell.getPnnCellGFPInt()+"\t"+pvCell.getPnnNbGFPFoci()+"\t"+pvCell.getPnnGFPFociVol()+"\t"+pvCell.getPnnGFPFociInt()+"\t"+
                        pvCell.getPnnCellDapiInt()+"\t"+pvCell.getPnnNbDapiFoci()+"\t"+pvCell.getPnnDapiFociVol()+"\t"+pvCell.getPnnDapiFociInt()+"\n");
                    cells_results_analyze.flush();
                }
                
            }

        } catch (IOException | DependencyException | ServiceException | FormatException | io.scif.DependencyException  ex) {
            Logger.getLogger(GFP_PV_PNN.class.getName()).log(Level.SEVERE, null, ex);
        }
        IJ.showStatus("Process done");
    }    
}    
