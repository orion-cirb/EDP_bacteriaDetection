package BacteriaOmni_Tools;

import BacteriaOmni_Tools.Cellpose.CellposeTaskSettings;
import BacteriaOmni_Tools.Cellpose.CellposeSegmentImgPlusAdvanced;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import fiji.util.gui.GenericDialogPlus;
import ij.plugin.RGBStackMerge;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.ImageIcon;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.Objects3DIntPopulation;
import mcib3d.geom2.Objects3DIntPopulationComputation;
import mcib3d.geom2.VoxelInt;
import mcib3d.geom2.measurements.MeasureFeret;
import mcib3d.geom2.measurements.MeasureVolume;
import mcib3d.image3d.ImageHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


/**
 * @author Orion-CIRB
 */
public class Tools {
    private final ImageIcon icon = new ImageIcon(this.getClass().getResource("/Orion_icon.png"));
    public boolean canceled = false;
    public Calibration cal;
    public double pixelWidth = 0.202;
    //public double pixelDepth = 0.360;
    
     // Omnipose
    private String omniposeEnvDirPath = "/opt/miniconda3/envs/omnipose";
    private String omniposeModelsPath = "/home/heloise/.cellpose/models/";
    public String omniposeModel = "bact_phase_omnitorch_0";
    public int omniposeDiameter = 0;
    public int omniposeMaskThreshold = 0;
    public double omniposeFlowThreshold = 0;
    private boolean useGpu = true;
    
    // Bacteria
    public double minBactSurface = 0.5;
    public double maxBactSurface = 10;
    

    
    /**
     * Display a message in the ImageJ console and status bar
     */
    public void print(String log) {
        System.out.println(log);
        IJ.showStatus(log);
    }
    
    
    /**
     * Check that needed modules are installed
     */
    public boolean checkInstalledModules() {
        ClassLoader loader = IJ.getClassLoader();
        try {
            loader.loadClass("mcib3d.geom.Object3D");
        } catch (ClassNotFoundException e) {
            IJ.showMessage("Error", "3D ImageJ Suite not installed, please install from update site");
            return false;
        }
        return true;
    }
    
    
    /**
     * Flush and close an image
     */
    public void flush_close(ImagePlus img) {
        img.flush();
        img.close();
    }
    
    
    /**
     * Find images extension
     */
    public String findImageType(File imagesFolder) {
        String ext = "";
        File[] files = imagesFolder.listFiles();
        for (File file: files) {
            if(file.isFile()) {
                String fileExt = FilenameUtils.getExtension(file.getName());
                switch (fileExt) {
                   case "nd" :
                       ext = fileExt;
                       break;
                    case "czi" :
                       ext = fileExt;
                       break;
                    case "lif"  :
                        ext = fileExt;
                        break;
                    case "ics2" :
                        ext = fileExt;
                        break;
                    case "tif" :
                        ext = fileExt;
                        break;
                    case "tiff" :
                        ext = fileExt;
                        break;
                }
            } else if (file.isDirectory() && !file.getName().equals("Results")) {
                ext = findImageType(file);
                if (! ext.equals(""))
                    break;
            }
        }
        return(ext);
    }
     
    
    /**
     * Find images in folder
     */
    public void findImages(String imagesFolder, String imageExt, ArrayList<String> imageFiles) {
        File inDir = new File(imagesFolder);
        File[] files = inDir.listFiles();
        
        for (File file: files) {
            if(file.isFile()) {
                String fileExt = FilenameUtils.getExtension(file.getName());
                if (fileExt.equals(imageExt) && !file.getName().startsWith("."))
                    imageFiles.add(file.getAbsolutePath());
            } else if (file.isDirectory() && !file.getName().equals("Results")) {
                findImages(file.getAbsolutePath(), imageExt, imageFiles);
            }
        }
        Collections.sort(imageFiles);
    }
    
    
    /**
     * Generate dialog box
     */
    public void dialog() {     
        GenericDialogPlus gd = new GenericDialogPlus("Parameters");
        gd.setInsets​(0, 80, 0);
        gd.addImage(icon);
        
        gd.addMessage("Bacteria detection", Font.getFont("Monospace"), Color.blue);
        if (IJ.isWindows()) {
            omniposeEnvDirPath = System.getProperty("user.home")+"\\Miniconda3\\envs\\omnipose";
            omniposeModelsPath = System.getProperty("user.home")+"\\.cellpose\\models\\";
        }
        gd.addDirectoryField("Omnipose environment directory: ", omniposeEnvDirPath);
        gd.addDirectoryField("Omnipose models path: ", omniposeModelsPath); 
        gd.addNumericField("Min bacterium surface (µm2): ", minBactSurface);
        gd.addNumericField("Max bacterium surface (µm2): ", maxBactSurface);
        
        gd.addMessage("Image calibration", Font.getFont("Monospace"), Color.blue);
        gd.addNumericField("XY calibration (µm):", pixelWidth);
        gd.showDialog();
        
        omniposeEnvDirPath = gd.getNextString();
        omniposeModelsPath = gd.getNextString();
        minBactSurface = (float) gd.getNextNumber();
        maxBactSurface = (float) gd.getNextNumber();
        pixelWidth = gd.getNextNumber();
        
        if(gd.wasCanceled())
           canceled = true;
    }
    
    
    /**
     * Set image calibration
     */
    public void setImageCalib() {
        cal = new Calibration();
        cal.pixelWidth = pixelWidth;
        cal.pixelHeight = pixelWidth;
        cal.setUnit("microns");
        System.out.println("XY calibration = " + cal.pixelWidth);
    }
    
   
    /**
    * Detect bacteria with Omnipose
    */
    public Objects3DIntPopulation omniposeDetection(ImagePlus imgBact){
        
        // Resize to be in a Omnipose-friendly scale
        ImagePlus imgIn = null;
        boolean resize = false;
        if (imgBact.getWidth() < 500) {
            float factor = 2f;
            imgIn = imgBact.resize((int)(imgBact.getWidth()*factor), (int)(imgBact.getHeight()*factor), 1, "bicubic");
            resize = true;
        } else {
            imgIn = new Duplicator().run(imgBact);
        }
        imgIn.setCalibration(cal);
        
        // Set Omnipose settings
        CellposeTaskSettings settings = new CellposeTaskSettings(omniposeModelsPath+omniposeModel, 1, omniposeDiameter, omniposeEnvDirPath);
        settings.setVersion("0.7");
        settings.setCluster(true);
        settings.setOmni(true);
        settings.useMxNet(false);
        settings.setCellProbTh(omniposeMaskThreshold);
        settings.setFlowTh(omniposeFlowThreshold);
        settings.useGpu(useGpu);
        
        // Run Omnipose
        CellposeSegmentImgPlusAdvanced cellpose = new CellposeSegmentImgPlusAdvanced(settings, imgIn);
        ImagePlus imgOut = cellpose.run();
        
        ImageProcessor imgOutProc = (resize) ? imgOut.getProcessor().resize(imgBact.getWidth(), imgBact.getHeight(), false) : imgOut.getProcessor();
        imgOut = new ImagePlus("", imgOutProc);
        imgOut.setCalibration(cal);
        
        Objects3DIntPopulation pop = new Objects3DIntPopulation(ImageHandler.wrap(imgOut));
        Objects3DIntPopulation excludeBorders = new Objects3DIntPopulationComputation(pop).getExcludeBorders(ImageHandler.wrap(imgOut), false);
        Objects3DIntPopulation cellFilterPop = new Objects3DIntPopulationComputation(excludeBorders).getFilterSize(minBactSurface/(pixelWidth*pixelWidth), maxBactSurface/(pixelWidth*pixelWidth));
        cellFilterPop.resetLabels();
        
        // Close images
        flush_close(imgIn);
        flush_close(imgOut);
        
        return(cellFilterPop);
    }
    
  
    /**
     * Compute bacteria parameters and save them in file
     */
    public void saveResults(Objects3DIntPopulation pop, double imageArea, String focusedSlice, String imgName, String parentFolder, BufferedWriter file) throws IOException {
        DescriptiveStatistics areas = new DescriptiveStatistics();
        DescriptiveStatistics lengths = new DescriptiveStatistics();
        for (Object3DInt obj : pop.getObjects3DInt()) {
            areas.addValue(new MeasureVolume(obj).getVolumeUnit());
            VoxelInt feret1Unit = new MeasureFeret(obj).getFeret1Unit();
            VoxelInt feret2Unit = new MeasureFeret(obj).getFeret2Unit();
            lengths.addValue(feret1Unit.distance(feret2Unit));
        }
        double totalArea = areas.getSum();
        double meanArea = areas.getMean();
        double stdArea = areas.getStandardDeviation();
        double meanLength = lengths.getMean();
        double stdlength = lengths.getStandardDeviation();
        file.write(parentFolder+"\t"+imgName+"\t"+imageArea+"\t"+focusedSlice+"\t"+pop.getNbObjects()+"\t"+totalArea+"\t"+meanArea+"\t"+stdArea+"\t"+meanLength+"\t"+stdlength+"\n");
        file.flush();
    }
    
    
    // Save objects image
    public void drawResults(ImagePlus img, Objects3DIntPopulation pop, String imgName, String parentFolder, String outDir) {
        ImageHandler imgOut1 = ImageHandler.wrap(img).createSameDimensions();
        pop.drawInImage(imgOut1);
        IJ.run(imgOut1.getImagePlus(), "glasbey on dark", "");
        imgOut1.getImagePlus().setCalibration(cal);
        FileSaver ImgObjectsFile = new FileSaver(imgOut1.getImagePlus());
        ImgObjectsFile.saveAsTiff(outDir+parentFolder.replace(File.separator, "_")+imgName+"_detections.tif");
        
        IJ.run(img, "Invert", "");
        ImagePlus[] imgColors2 = {imgOut1.getImagePlus(), null, null, img};
        ImagePlus imgOut2 = new RGBStackMerge().mergeHyperstacks(imgColors2, true);
        imgOut2.setCalibration(cal);
        FileSaver ImgObjectsFile2 = new FileSaver(imgOut2);
        ImgObjectsFile2.saveAsTiff(outDir+parentFolder.replace(File.separator, "_")+imgName+"_overlay.tif");

        flush_close(imgOut1.getImagePlus());
        flush_close(imgOut2);
    }
    
}
