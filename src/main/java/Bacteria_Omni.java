/*
 * Find bacteria with OmniPose
 */

import BacteriaOmni_Tools.Tools;
import ij.*;
import ij.io.FileSaver;
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
import mcib3d.image3d.ImageHandler;
import org.apache.commons.io.FilenameUtils;



public class Bacteria_Omni implements PlugIn {
    
    Tools tools = new Tools();
    
    private String imageDir = "";
    public String outDirResults = "";
    private boolean canceled = false;
    public BufferedWriter bacts_results_analyze;
   
    
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
            String file_ext = tools.findImageType(new File(imageDir));
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
            // Find calibration
            reader.setId(imageFiles.get(0));
            //tools.cal = tools.findImageCalib(meta);
            
            // Write header
            
            String header = "Image Name\t#Bacteria\tMean Area\tStd mean area\tMean lenght\tStd mean length\n";
            FileWriter fwBacts = new FileWriter(outDirResults + "Bacteria_Results.xls", false);
            bacts_results_analyze = new BufferedWriter(fwBacts);
            bacts_results_analyze.write(header);
            bacts_results_analyze.flush();
            
            for (String f : imageFiles) {
                reader.setId(f);
                String rootName = FilenameUtils.getBaseName(f);
                ImporterOptions options = new ImporterOptions();
                options.setId(f);
                options.setSplitChannels(true);
                options.setQuiet(true);
                options.setColorMode(ImporterOptions.COLOR_MODE_GRAYSCALE);
                 
                // open image
                System.out.println("--- Opening image  ...");
                ImagePlus img = BF.openImagePlus(options)[0];
                
                // Find bateria with omnipose
                Objects3DIntPopulation bactPop = tools.omniPoseBactsPop(img);
                int bacts = bactPop.getNbObjects();
                System.out.println(bacts +" bacteria found");
                
                // write parameters
                tools.write_parameters(bactPop, rootName, bacts_results_analyze);
                
                // save images
                tools.saveObjects(img, bactPop, outDirResults+"Bacteria_objects.tif");
                tools.flush_close(img);
            }
        
        IJ.showStatus("Process done");
    }   catch (IOException | FormatException | DependencyException | ServiceException ex) {
            Logger.getLogger(Bacteria_Omni.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
}    
