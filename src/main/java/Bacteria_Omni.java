import BacteriaOmni_Tools.Find_focused_slices;
import BacteriaOmni_Tools.Tools;
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


/**
 * Detect bacteria with OmniPose
 * @author Orion-CIRB
 */
public class Bacteria_Omni implements PlugIn {
    
    Tools tools = new Tools();
    private String imageDir = "";
    public String outDirResults = "";
    public BufferedWriter results;
   
    
    public void run(String arg) {
        try {
            if (!tools.checkInstalledModules()) {
                return;
            } 
            
            imageDir = IJ.getDirectory("Choose directory containing image files...");
            if (imageDir == null) {
                return;
            }   
            // Find images with extension
            String file_ext = tools.findImageType(new File(imageDir));
            ArrayList<String> imageFiles = new ArrayList();
            tools.findImages(imageDir, file_ext, imageFiles);
            if (imageFiles.isEmpty()) {
                IJ.showMessage("Error", "No images found with " + file_ext + " extension");
                return;
            }
            
            // Create output folder
            outDirResults = imageDir + File.separator + "Results" + File.separator;
            File outDir = new File(outDirResults);
            if (!Files.exists(Paths.get(outDirResults))) {
                outDir.mkdir();
            }
            // Write header in results file
            String header = "Parent folder\tImage name\tImage area (µm2)\tFocused slice\tNb bacteria\tBacteria total area (µm2)"
                    + "\tMean bacterium area (µm2)\tBacterium area std\tMean bacterium lenght (µm)\tBacterium lenght std"
                    + "\tMean bacterium circularity\tBacterium circularity std\tMean bacterium aspect ratio\tBacterium aspect ratio std"
                    + "\tMean bacterium roundness\tBacterium roundness std\n";
            FileWriter fwResults = new FileWriter(outDirResults + "results.xls", false);
            results = new BufferedWriter(fwResults);
            results.write(header);
            results.flush();
            
            // Create OME-XML metadata store of the latest schema version
            ServiceFactory factory;
            factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();
            ImageProcessorReader reader = new ImageProcessorReader();
            reader.setMetadataStore(meta);
            reader.setId(imageFiles.get(0));
            
            // Dialog box
            tools.dialog();
            if (tools.canceled) {
                IJ.showMessage("Error", "Plugin canceled");
                return;
            }
            
            // Image calibration
            tools.setImageCalib();
            
            for (String f : imageFiles) {
                reader.setId(f);
                String rootName = FilenameUtils.getBaseName(f);
                String parentFolder = f.replace(imageDir, "").replace(FilenameUtils.getName(f), "");
                tools.print("--- ANALYZING IMAGE " + parentFolder + rootName + " ------");
                
                ImporterOptions options = new ImporterOptions();
                options.setId(f);
                options.setQuiet(true);
                options.setColorMode(ImporterOptions.COLOR_MODE_GRAYSCALE);
                 
                // Open stack
                ImagePlus stack = BF.openImagePlus(options)[0];
                
                // Invert stack
                IJ.run(stack, "Invert", "stack");
                
                // Get focused slice
                tools.print("- Finding focused slice -");
                Find_focused_slices focus = new Find_focused_slices();
                focus.setParams(100, 0, false, false);
                ImagePlus img = focus.run(stack);
                String focusedSlice = img.getProperty("Label").toString().replace("Z_", "");
                System.out.println("Focused slice in stack: " + focusedSlice);
                
                // Detect bacteria with Omnipose
                tools.print("- Detecting bacteria -");
                Objects3DIntPopulation bactPop = tools.omniposeDetection(img);
                System.out.println(bactPop.getNbObjects() + " bacteria found");
                
                // Save results
                tools.print("- Saving results -");
                tools.saveResults(bactPop, img, focusedSlice, rootName, parentFolder, results);
                
                // Save images
                tools.drawResults(img, bactPop, rootName, parentFolder, outDirResults);
                tools.flush_close(img);
            }
        
            tools.print("--- All done! ---");
            
        }   catch (IOException | FormatException | DependencyException | ServiceException ex) {
            Logger.getLogger(Bacteria_Omni.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
}    
