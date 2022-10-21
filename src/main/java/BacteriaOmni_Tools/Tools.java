package BacteriaOmni_Tools;


import BacteriaOmni_Tools.Cellpose.CellposeTaskSettings;
import BacteriaOmni_Tools.Cellpose.CellposeSegmentImgPlusAdvanced;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.plugins.util.ImageProcessorReader;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.Objects3DIntPopulation;
import mcib3d.geom2.Objects3DIntPopulationComputation;
import mcib3d.geom2.VoxelInt;
import mcib3d.geom2.measurements.MeasureFeret;
import mcib3d.geom2.measurements.MeasureIntensity;
import mcib3d.geom2.measurements.MeasureSurface;
import mcib3d.geom2.measurements.MeasureVolume;
import mcib3d.image3d.ImageHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author phm
 */
public class Tools {

    public boolean canceled = false;
    public double minBactVol = 10;
    public double maxBactVol = 300;
    
     // Omnipose
    public int omniPoseDiameter = 0;
    private boolean useGpu = true;
    private String omniPoseEnvDirPath = "/home/phm/.conda/envs/omnipose";
    private String omniPoseModelsPath = "/home/phm/.cellpose/models/";
    public String omniPoseModel = omniPoseModelsPath+"bact_phase_omnitorch_0";
    public Calibration cal;
    public double pixVol= 1;
    private final ImageIcon icon = new ImageIcon(this.getClass().getResource("/Orion_icon.png"));

     
    // Flush and close images
    public void flush_close(ImagePlus img) {
        img.flush();
        img.close();
    }

    
/**
     * Find images in folder
     * @param imagesFolder
     * @param imageExt
     * @return 
     */
    public ArrayList<String> findImages(String imagesFolder, String imageExt) {
        File inDir = new File(imagesFolder);
        String[] files = inDir.list();
        if (files == null) {
            System.out.println("No Image found in "+imagesFolder);
            return null;
        }
        ArrayList<String> images = new ArrayList();
        for (String f : files) {
            // Find images with extension
            String fileExt = FilenameUtils.getExtension(f);
            if (fileExt.equals(imageExt))
                images.add(imagesFolder + File.separator + f);
        }
        Collections.sort(images);
        return(images);
    }
    
     /**
     * Find image type
     */
    public String findImageType(File imagesFolder) {
        String ext = "";
        String[] files = imagesFolder.list();
        for (String name : files) {
            String fileExt = FilenameUtils.getExtension(name);
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
                case "isc2" :
                    ext = fileExt;
                    break;
                case "tif" :
                    ext = fileExt;
                    break;
            }
        }
        return(ext);
    }
    
     /**
     * Find image calibration
     * @param meta
     * @return 
     */
    public Calibration findImageCalib(IMetadata meta) {
        cal = new Calibration();  
        // read image calibration
        cal.pixelWidth = meta.getPixelsPhysicalSizeX(0).value().doubleValue();
        cal.pixelHeight = cal.pixelWidth;
        if (meta.getPixelsPhysicalSizeZ(0) != null)
            cal.pixelDepth = meta.getPixelsPhysicalSizeZ(0).value().doubleValue();
        else
            cal.pixelDepth = 1;
        cal.setUnit("microns");
        return(cal);
    }
    
    
     /**
     * Find channels name
     * @param imageName
     * @return 
     * @throws loci.common.services.DependencyException
     * @throws loci.common.services.ServiceException
     * @throws loci.formats.FormatException
     * @throws java.io.IOException
     */
    public String[] findChannels (String imageName, IMetadata meta, ImageProcessorReader reader) throws DependencyException, ServiceException, FormatException, IOException {
        int chs = reader.getSizeC();
        String[] channels = new String[chs];
        String imageExt =  FilenameUtils.getExtension(imageName);
        switch (imageExt) {
            case "nd" :
                for (int n = 0; n < chs; n++) 
                {
                    if (meta.getChannelID(0, n) == null)
                        channels[n] = Integer.toString(n);
                    else 
                        channels[n] = meta.getChannelName(0, n).toString();
                }
                break;
            case "lif" :
                for (int n = 0; n < chs; n++) 
                    if (meta.getChannelID(0, n) == null || meta.getChannelName(0, n) == null)
                        channels[n] = Integer.toString(n);
                    else 
                        channels[n] = meta.getChannelName(0, n).toString();
                break;
            case "czi" :
                for (int n = 0; n < chs; n++) 
                    if (meta.getChannelID(0, n) == null)
                        channels[n] = Integer.toString(n);
                    else 
                        channels[n] = meta.getChannelFluor(0, n).toString();
                break;
            case "ics" :
                for (int n = 0; n < chs; n++) 
                    if (meta.getChannelID(0, n) == null)
                        channels[n] = Integer.toString(n);
                    else 
                        channels[n] = meta.getChannelExcitationWavelength(0, n).value().toString();
                break;    
            default :
                for (int n = 0; n < chs; n++)
                    channels[n] = Integer.toString(n);
        }
        return(channels);         
    }
    
        
     /**
     * Find volume of objects  
     * @param dotsPop
     * @return vol
     */
    
    public double findPopVolume (Objects3DIntPopulation dotsPop) {
        IJ.showStatus("Findind object's volume");
        List<Double[]> results = dotsPop.getMeasurementsList(new MeasureVolume().getNamesMeasurement());
        double sum = results.stream().map(arr -> arr[1]).reduce(0.0, Double::sum);
        return(sum);
    }
    
    /**
     * Find intensity of objects  
     * @param dotsPop
     * @return intensity
     */
    
    public double findPopIntensity (Objects3DIntPopulation dotsPop, ImagePlus img) {
        IJ.showStatus("Findind object's intensity");
        ImageHandler imh = ImageHandler.wrap(img);
        double sumInt = 0;
        for(Object3DInt obj : dotsPop.getObjects3DInt()) {
            MeasureIntensity intMes = new MeasureIntensity(obj, imh);
            sumInt +=  intMes.getValueMeasurement(MeasureIntensity.INTENSITY_SUM);
        }
        return(sumInt);
    }
    
   
 /**
 * Find Bacteria with omnipose
 * @param img
 * @return 
 */
    public Objects3DIntPopulation omniPoseBactsPop(ImagePlus imgBact){
        ImagePlus imgIn = null;
        // resize to be in a friendly scale
        int width = imgBact.getWidth();
        int height = imgBact.getHeight();
        float factor = 0.5f;
        boolean resized = false;
        if (imgBact.getWidth() > 1024) {
            imgIn = imgBact.resize((int)(width*factor), (int)(height*factor), 1, "none");
            resized = true;
        }
        else
            imgIn = new Duplicator().run(imgBact);
        imgIn.setCalibration(cal);
        CellposeTaskSettings settings = new CellposeTaskSettings(omniPoseModel, 1, omniPoseDiameter, omniPoseEnvDirPath);
        settings.setStitchThreshold(0.25); 
        settings.useGpu(true);
        settings.setCluster(true);
        settings.setOmni(true);
        settings.setVerbose(true);
        settings.useMxNet(false);
        settings.setInvert(false);
        settings.setVersion("0.7");
        CellposeSegmentImgPlusAdvanced cellpose = new CellposeSegmentImgPlusAdvanced(settings, imgIn);
        ImagePlus cellpose_img = cellpose.run();
        ImagePlus cells_img = (resized) ? cellpose_img.resize(width, height, 1, "none") : cellpose_img;
        cells_img.setCalibration(cal);
        Objects3DIntPopulation pop = new Objects3DIntPopulation(ImageHandler.wrap(cells_img));
        Objects3DIntPopulation excludeBorders = new Objects3DIntPopulationComputation(pop).getExcludeBorders(ImageHandler.wrap(cells_img), false);
        Objects3DIntPopulation cellFilterPop = new Objects3DIntPopulationComputation(excludeBorders).getFilterSize(minBactVol/pixVol, maxBactVol/pixVol);
        cellFilterPop.resetLabels();
        flush_close(cells_img);
        flush_close(imgIn);
        flush_close(cellpose_img);
        return(cellFilterPop);
    }
    
    // Save objects image
    public void saveObjects (ImagePlus img, Objects3DIntPopulation pop, String imageName) {
        ImageHandler imgObjects = ImageHandler.wrap(img).createSameDimensions();
        pop.drawInImage(imgObjects);
        IJ.run(imgObjects.getImagePlus(),"glasbey on dark","");
        FileSaver ImgObjectsFile = new FileSaver(imgObjects.getImagePlus());
        ImgObjectsFile.saveAsTiff(imageName);
        flush_close(imgObjects.getImagePlus());
    }
    
    // wrtite parameters
    public void write_parameters(Objects3DIntPopulation bactPop, String imageName, BufferedWriter fwBacts) throws IOException {
        DescriptiveStatistics area = new DescriptiveStatistics();
        DescriptiveStatistics length = new DescriptiveStatistics();
        for (Object3DInt obj : bactPop.getObjects3DInt()) {
            area.addValue(new MeasureSurface(obj).getValueMeasurement(MeasureSurface.SURFACE_UNIT));
            VoxelInt feret1Unit = new MeasureFeret(obj).getFeret1Unit();
            VoxelInt feret2Unit = new MeasureFeret(obj).getFeret2Unit();
            length.addValue(feret1Unit.distance(feret2Unit));
        }
        double meanArea = area.getMean();
        double stdArea = area.getStandardDeviation();
        double meanLength = length.getMean();
        double stdlength = length.getStandardDeviation();
        fwBacts.write(imageName+"\t"+bactPop.getNbObjects()+"\t"+meanArea+"\t"+stdArea+"\t"+meanLength+"\t"+stdlength+"\n");
        fwBacts.flush();
    }
    
}
