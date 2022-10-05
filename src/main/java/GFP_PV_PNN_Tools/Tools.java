package GFP_PV_PNN_Tools;


import Cellpose.CellposeSegmentImgPlusAdvanced;
import Cellpose.CellposeTaskSettings;
import GFP_PV_PNNStardistOrion.StarDist2D;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import ij.process.ImageProcessor;
import io.scif.DependencyException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.ImageIcon;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.plugins.util.ImageProcessorReader;
import mcib3d.geom2.BoundingBox;
import mcib3d.geom2.Object3DComputation;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.Objects3DIntPopulation;
import mcib3d.geom2.Objects3DIntPopulationComputation;
import mcib3d.geom2.measurements.Measure2Colocalisation;
import mcib3d.geom2.measurements.MeasureIntensity;
import mcib3d.geom2.measurements.MeasureVolume;
import mcib3d.geom2.measurementsPopulation.MeasurePopulationColocalisation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import org.apache.commons.io.FilenameUtils;

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
    public double minCellVol = 300;
    public double maxCellVol = 5000;
    public double minFociVol = 0.02;
    public double maxFociVol = 0.5;
    
    private Object syncObject = new Object();
    private final double stardistPercentileBottom = 0.2;
    private final double stardistPercentileTop = 99.8;
    private final double stardistFociProbThresh = 0.05;
    private final double stardistFociOverlayThresh = 0.25;
    private File modelsPath = new File(IJ.getDirectory("imagej")+File.separator+"models");
    private String stardistOutput = "Label Image"; 
    private String stardistFociModel = "pmls2.zip";
    
     // Cellpose
    public int cellPoseDiameter = 50;
    private boolean useGpu = true;
    private String[] cellposeModels = {"cyto","nuclei","tissuenet","livecell", "cyto2", "general","CP", "CPx", "TN1", "TN2", "TN3", "LC1",
        "LC2", "LC3", "LC4"};
    public String cellposeModel = "";
    private String cellPoseEnvDirPath = "/home/phm/.conda/envs/cellpose";
    
    public String[] channelNames = {"PV", "PNN", "GFP", "DAPI"};
    public Calibration cal;
    public double pixVol= 0;
    private final ImageIcon icon = new ImageIcon(this.getClass().getResource("/Orion_icon.png"));

    private CLIJ2 clij2 = CLIJ2.getInstance();
    
    
     /**
     * check  installed modules
     * @return 
     */
    public boolean checkInstalledModules() {
        // check install
        ClassLoader loader = IJ.getClassLoader();
        try {
            loader.loadClass("net.haesleinhuepf.clij2.CLIJ2");
        } catch (ClassNotFoundException e) {
            IJ.log("CLIJ not installed, please install from update site");
            return false;
        }
        try {
            loader.loadClass("mcib3d.geom.Object3D");
        } catch (ClassNotFoundException e) {
            IJ.log("3D ImageJ Suite not installed, please install from update site");
            return false;
        }
        return true;
    }
    
     /*
    Find starDist models in Fiji models folder
    */
    public String[] findStardistModels() {
        FilenameFilter filter = (dir, name) -> name.endsWith(".zip");
        File[] modelList = modelsPath.listFiles(filter);
        String[] models = new String[modelList.length];
        for (int i = 0; i < modelList.length; i++) {
            models[i] = modelList[i].getName();
        }
        Arrays.sort(models);
        return(models);
    }   
    
    /* Median filter 
     * Using CLIJ2
     * @param ClearCLBuffer
     * @param sizeXY
     * @param sizeZ
     */ 
    public ClearCLBuffer median_filter(ClearCLBuffer  imgCL, double sizeXY, double sizeZ) {
        ClearCLBuffer imgCLMed = clij2.create(imgCL);
        clij2.mean3DBox(imgCL, imgCLMed, sizeXY, sizeXY, sizeZ);
        clij2.release(imgCL);
        return(imgCLMed);
    }

   
    /**
    Add cells parameters
    */
    public void pvCellsParameters (Object3DInt cell, Objects3DIntPopulation pop, ArrayList<Cells_PV> pvCells, ImagePlus img, String cellType, String fociType) {
        double vol = 0;
        double Int = 0;
        for (Object3DInt obj : pop.getObjects3DInt()) {
            vol += new MeasureVolume(obj).getVolumeUnit();
            Int = new MeasureIntensity(obj, ImageHandler.wrap(img)).getValueMeasurement(MeasureIntensity.INTENSITY_SUM);
        }
        int cellIndex = (int)(cell.getLabel()-1);
        double cellInt = new MeasureIntensity(cell, ImageHandler.wrap(img)).getValueMeasurement(MeasureIntensity.INTENSITY_SUM);
        switch (cellType) {
            case "PV" :
                if (fociType.equals("GFP")) {
                    pvCells.get(cellIndex).setPvCellGFPInt(cellInt);
                    pvCells.get(cellIndex).setPvGFPFociVol(vol);
                    pvCells.get(cellIndex).setPvGFPFociInt(Int);
                    pvCells.get(cellIndex).setPvNbGFPFoci(pop.getNbObjects());
                }
                else {
                    pvCells.get(cellIndex).setPvCellDapiInt(cellInt);
                    pvCells.get(cellIndex).setPvDapiFociVol(vol);
                    pvCells.get(cellIndex).setPvDapiFociInt(Int);
                    pvCells.get(cellIndex).setPvNbDapiFoci(pop.getNbObjects());
                }
                break;
            case "PNN" :
                if (fociType.equals("GFP")) {
                    pvCells.get(cellIndex).setPnnCellGFPInt(cellInt);
                    pvCells.get(cellIndex).setPnnGFPFociVol(vol);
                    pvCells.get(cellIndex).setPnnGFPFociInt(Int);
                    pvCells.get(cellIndex).setPnnNbGFPFoci(pop.getNbObjects());
                }
                else {
                    pvCells.get(cellIndex).setPnnCellDapiInt(cellInt);
                    pvCells.get(cellIndex).setPnnDapiFociVol(vol);
                    pvCells.get(cellIndex).setPnnDapiFociInt(Int);
                    pvCells.get(cellIndex).setPnnNbDapiFoci(pop.getNbObjects());
                }
                break;
        }    
    }
    
    /**
    Fill cells parameters
    * Initiate with pv first
    */
    public void pvCellsParameters (Objects3DIntPopulation cellsPop, ArrayList<Cells_PV> pvCells, ImagePlus img, String cellType) {
        for (Object3DInt obj : cellsPop.getObjects3DInt()) {
            float objLabel = obj.getLabel();
            double cellVol = new MeasureVolume(obj).getVolumeUnit();
            double cellInt = new MeasureIntensity(obj, ImageHandler.wrap(img)).getValueMeasurement(MeasureIntensity.INTENSITY_SUM);
            Cells_PV cell = null;
            switch (cellType) {
                case "PV" :
                    // check if PV cell label is in arrayList
                    if (pvCells.size() < objLabel) {
                        cell = new Cells_PV(objLabel, false, cellVol, cellInt, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                        pvCells.add(cell);
                    }
                    else {
                        pvCells.get((int)objLabel-1).setPnnCellLabel(obj.getLabel());
                        pvCells.get((int)objLabel-1).setPnnCellVol(cellVol);
                        pvCells.get((int)objLabel-1).setPnnCellInt(cellInt);   
                    }
                    break;
                case "PNN" :
                    // check if PNN cell label is in arrayList
                    if (pvCells.size() < objLabel) {
                        cell = new Cells_PV(0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, obj.getLabel(), cellVol, cellInt, 0, 0, 0, 0, 0, 0, 0, 0);
                        pvCells.add(cell);
                    }
                    else {
                        pvCells.get((int)objLabel-1).setPnnCellLabel(obj.getLabel());
                        pvCells.get((int)objLabel-1).setPnnCellVol(cellVol);
                        pvCells.get((int)objLabel-1).setPnnCellInt(cellInt);                        
                    }
                    break;
            }
            
        }
    }
    
    
    
    public String[] dialog(String[] chs) {
        String[] models = findStardistModels();
        if (IJ.isWindows())
            cellPoseEnvDirPath = System.getProperty("user.home")+"\\miniconda3\\envs\\CellPose";
        GenericDialogPlus gd = new GenericDialogPlus("Parameters");
        gd.setInsets​(0, 100, 0);
        gd.addImage(icon);
        gd.addMessage("Channels", Font.getFont("Monospace"), Color.blue);
        int index = 0;
        for (String chNames : channelNames) {
            gd.addChoice(chNames+" : ", chs, chs[index]);
            index++;
        } 
        int pmlModel = Arrays.asList(models).indexOf("pmls2.zip");
        gd.addMessage("--- Stardist model ---", Font.getFont(Font.MONOSPACED), Color.blue);
        if (models.length > 0 && pmlModel >= 0) {
            gd.addChoice("StarDist dots model :",models, stardistFociModel);
        }
        else {
            gd.addMessage("No StarDist model found in Fiji !!", Font.getFont("Monospace"), Color.red);
            gd.addFileField("StarDist dots model :", stardistFociModel);
        }
        gd.addMessage("Cells detection", Font.getFont("Monospace"), Color.blue);
        gd.addDirectoryField("Cellpose environment path : ", cellPoseEnvDirPath);
        gd.addChoice("Cellpose model : ", cellposeModels, cellposeModels[4]);
        gd.addNumericField("min cell volume : ", minCellVol);
        gd.addNumericField("max cell volume : ", maxCellVol);
        gd.addMessage("Foci size filter", Font.getFont("Monospace"), Color.blue);
        gd.addNumericField("min foci GFP : ", minFociVol);
        gd.addNumericField("max foci GFP : ", maxFociVol);
        gd.addMessage("Image calibration", Font.getFont("Monospace"), Color.blue);
        gd.addNumericField("Pixel size : ", cal.pixelWidth);
        gd.showDialog();
        if (gd.wasCanceled())
            canceled = true;
        String[] chChoices = new String[channelNames.length];
        for (int n = 0; n < chChoices.length; n++) 
            chChoices[n] = gd.getNextChoice();
        if (models.length > 0  && pmlModel >= 0) {
            stardistFociModel = modelsPath+File.separator+gd.getNextChoice();
        }
        else {
            stardistFociModel = gd.getNextString();
        }
        if (stardistFociModel.isEmpty()) {
            IJ.error("No model specify !!");
            return(null);
        }
        cellPoseEnvDirPath = gd.getNextString();
        cellposeModel = gd.getNextChoice();
        minCellVol = gd.getNextNumber();
        maxCellVol = gd.getNextNumber();
        minFociVol = gd.getNextNumber();
        maxFociVol = gd.getNextNumber();
        cal.pixelWidth = cal.pixelHeight = gd.getNextNumber();
        pixVol = cal.pixelWidth*cal.pixelWidth*cal.pixelDepth;
        return(chChoices);
    }
     
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
    For each each find dots
    * return dots pop cell population
     */
        public Objects3DIntPopulation stardistFociInCellsPop(ImagePlus img, Objects3DIntPopulation cellPop, String cellType, 
                String fociType, ArrayList<Cells_PV> cells) throws IOException{
            Objects3DIntPopulation allFociPop = new Objects3DIntPopulation();
            float fociIndex = 0;
            for (Object3DInt cell: cellPop.getObjects3DInt()) {
                BoundingBox box = cell.getBoundingBox();
                int ZStartCell = box.zmin +1;
                int ZStopCell = box.zmax + 1;
                Roi roiBox = new Roi(box.xmin, box.ymin, box.xmax-box.xmin + 1 , box.ymax - box.ymin + 1);
                img.setRoi(roiBox);
                img.updateAndDraw();
                // Crop image
                ImagePlus imgCell = new Duplicator().run(img, ZStartCell, ZStopCell);
                imgCell.deleteRoi();
                imgCell.updateAndDraw();
                ClearCLBuffer imgCL = clij2.push(imgCell);
                ClearCLBuffer imgCLM = clij2.create(imgCL);
                imgCLM = median_filter(imgCL, 2, 2);
                clij2.release(imgCL);
                ImagePlus imgM = clij2.pull(imgCLM);
                clij2.release(imgCLM);
            
                // Go StarDist
                File starDistModelFile = new File(stardistFociModel);
                StarDist2D star = new StarDist2D(syncObject, starDistModelFile);
                star.loadInput(imgM);
                star.setParams(stardistPercentileBottom, stardistPercentileTop, stardistFociProbThresh, stardistFociOverlayThresh, stardistOutput);
                star.run();
                
                // label in 3D
                ImagePlus imgLabels = star.associateLabels();
                imgLabels.setCalibration(cal);
                ImageInt label3D = ImageInt.wrap(imgLabels);
                Objects3DIntPopulation fociPop = new Objects3DIntPopulationComputation(new Objects3DIntPopulation(label3D)).
                        getFilterSize(minFociVol/pixVol, maxFociVol/pixVol);
                fociPop.resetLabels();
                fociPop.setVoxelSizeXY(cal.pixelWidth);
                fociPop.setVoxelSizeZ(cal.pixelDepth);
                // find foci in cell
                Object3DInt cellT = new Object3DComputation(cell).getObject3DCopy();
                cellT.translate(-box.xmin, -box.ymin, -ZStartCell + 1);
                Objects3DIntPopulation fociColocPop = findColocCell(cellT, fociPop);
                System.out.println(fociColocPop.getNbObjects()+" foci "+fociType+" found in "+cellType+" cell "+cell.getLabel());
                // add foci info in cell
                pvCellsParameters(cell, fociColocPop, cells, img, cellType, fociType);
                // reset foci in global image
                int tx = box.xmin;
                int ty = box.ymin;
                int tz = ZStartCell;
                for (Object3DInt foci: fociColocPop.getObjects3DInt()) {
                    foci.translate(tx, ty, tz-1);
                    fociIndex++;
                    foci.setLabel(fociIndex);
                    allFociPop.addObject(foci);
                }
                flush_close(imgCell);
                flush_close(imgLabels);
                flush_close(imgM);
            }
            return(allFociPop);
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
 * Find cells with cellpose
 * return cell cytoplasm
 * @param img
 * @return 
 */
    public Objects3DIntPopulation cellPoseCellsPop(ImagePlus imgCell){
        ImagePlus imgIn = null;
        // resize to be in a friendly scale
        int width = imgCell.getWidth();
        int height = imgCell.getHeight();
        float factor = 0.25f;
        boolean resized = false;
        if (imgCell.getWidth() > 1024) {
            imgIn = imgCell.resize((int)(width*factor), (int)(height*factor), 1, "none");
            resized = true;
        }
        else
            imgIn = new Duplicator().run(imgCell);
        imgIn.setCalibration(cal);
        CellposeTaskSettings settings = new CellposeTaskSettings(cellposeModel, 1, cellPoseDiameter, cellPoseEnvDirPath);
        settings.setCellProbTh(0);
        settings.setStitchThreshold(0.25); 
        settings.setFlowTh(0.6);
        settings.useGpu(true);
        CellposeSegmentImgPlusAdvanced cellpose = new CellposeSegmentImgPlusAdvanced(settings, imgIn);
        ImagePlus cellpose_img = cellpose.run(); 
        flush_close(imgIn);
        ImagePlus cells_img = (resized) ? cellpose_img.resize(width, height, 1, "none") : cellpose_img;
        flush_close(cellpose_img);
        cells_img.setCalibration(cal);
        Objects3DIntPopulation pop = new Objects3DIntPopulation(ImageHandler.wrap(cells_img));
        Objects3DIntPopulation cellPop = popFilterOneZ(pop);
        cellPop.setVoxelSizeXY(cal.pixelWidth);
        cellPop.setVoxelSizeZ(cal.pixelDepth);
        Objects3DIntPopulation cellFilterPop = new Objects3DIntPopulationComputation(cellPop).getFilterSize(minCellVol/pixVol, maxCellVol/pixVol);
        cellFilterPop.resetLabels();
        flush_close(cells_img);
        return(cellFilterPop);
    }
    
    
    /**
     * Remove object with only one plan
     */
    private Objects3DIntPopulation popFilterOneZ(Objects3DIntPopulation pop) {
        Objects3DIntPopulation popN = new Objects3DIntPopulation();
        for (Object3DInt obj : pop.getObjects3DInt()) {
            if (obj.getObject3DPlanes().size() > 1)
                popN.addObject(obj);
        }
        popN.resetLabels();
        return(popN);
    }
    
    /**
     * Find objects population coloc in objet 
     */
    public Objects3DIntPopulation findColocCell(Object3DInt cellObj, Objects3DIntPopulation pop1) {
        Objects3DIntPopulation objs = new Objects3DIntPopulation();
        for (Object3DInt obj1 : pop1.getObjects3DInt()) {
            Measure2Colocalisation coloc = new Measure2Colocalisation(obj1, cellObj);
            if (coloc.getValue(Measure2Colocalisation.COLOC_VOLUME) > 0.5*obj1.size())
                objs.addObject(obj1);
        }
        objs.setVoxelSizeXY(cal.pixelWidth);
        objs.setVoxelSizeZ(cal.pixelDepth);
        return(objs);
    }
    
    /**
     * Find coloc cells
     * pop1 PV cells
     * pop2 PNN cells
     */
    public void findColocCells (Objects3DIntPopulation pop1, Objects3DIntPopulation pop2, ArrayList<Cells_PV> pvCells) {
        IJ.showStatus("Finding colocalized cells population ...");
        if (pop1.getNbObjects() > 0 && pop2.getNbObjects() > 0) {
            MeasurePopulationColocalisation coloc = new MeasurePopulationColocalisation(pop1, pop2);
            for (Object3DInt obj1: pop1.getObjects3DInt()) {
                for (Object3DInt obj2 : pop2.getObjects3DInt()) {
                    double colocVal = coloc.getValueObjectsPair(obj1, obj2);
                    if ( colocVal > 0.2*obj1.size()) {
                        pvCells.get((int)obj1.getLabel()-1).setPvCellIsPNN(true);
                        break;
                    }
                }
            }
        }
    }
    
    
     /**
     * Label object
     * @param popObj
     * @param img 
     */
    public void labelsObject (Object3DInt obj, ImageHandler imh) {
        int fontSize = Math.round(8f/(float)imh.getCalibration().pixelWidth);
        Font tagFont = new Font("SansSerif", Font.PLAIN, fontSize);
        float label = obj.getLabel();
        BoundingBox box = obj.getBoundingBox();
        int z = box.zmin;
        int x = box.xmin - 2;
        int y = box.ymin - 2;
        imh.getImagePlus().setSlice(z+1);
        ImageProcessor ip = imh.getImagePlus().getProcessor();
        ip.setFont(tagFont);
        ip.setColor(255);
        ip.drawString(Integer.toString((int)label), x, y);
        imh.getImagePlus().updateAndDraw();
    }
   
    /**
     * Save dots Population in image
     * @param pop1 PV cells green channel
     * @param pop2 pv foci in GFP cyan channel
     * @param pop3 pv foci in DAPI blue channel
     * @param pop4 PNN cells red channel
     * @param pop5 pnn foci in GFP cyan channel
     * @param pop6 pnn foci in DAPI blue channel
     * @param img 
     * @param outDir 
     */
    public void saveImgObjects(Objects3DIntPopulation pop1, Objects3DIntPopulation pop2, Objects3DIntPopulation pop3, Objects3DIntPopulation pop4, 
        Objects3DIntPopulation pop5, Objects3DIntPopulation pop6, String imageName, ImagePlus img, String outDir) {
        //create image objects population
        //PV cells green
        ImageHandler imgObj1 = ImageHandler.wrap(img).createSameDimensions();
        
        ImageHandler imgObj2 = imgObj1.createSameDimensions();
        if (pop1.getNbObjects() > 0)
            pop1.drawInImage(imgObj2);
        
        //PNN cells red
        if (pop4.getNbObjects() > 0)
            pop4.drawInImage(imgObj1);

        
        // Foci pv GFP cyan            
        ImageHandler imgObj4 = imgObj1.createSameDimensions();
        if (pop2.getNbObjects() > 0)
            for (Object3DInt obj : pop2.getObjects3DInt())
                obj.drawObject(imgObj4, 255);        
        
        // Foci pv DAPI blue
        ImageHandler imgObj3 = imgObj1.createSameDimensions();
        if (pop3.getNbObjects() > 0)
            for (Object3DInt obj : pop3.getObjects3DInt())
                obj.drawObject(imgObj3, 255);        
        
         // Foci pnn GFP cyan
        if (pop5.getNbObjects() > 0)
            for (Object3DInt obj : pop5.getObjects3DInt())
                obj.drawObject(imgObj4, 255);        
        
        // Foci pnn DAPI blue
        if (pop6.getNbObjects() > 0)
            for (Object3DInt obj : pop6.getObjects3DInt())
                obj.drawObject(imgObj3, 255);        
        
   
        // save image for objects population
        ImagePlus[] imgColors = {imgObj1.getImagePlus(), imgObj2.getImagePlus(), imgObj3.getImagePlus(), null, imgObj4.getImagePlus()};
        ImagePlus imgObjects = new RGBStackMerge().mergeHyperstacks(imgColors, false);
        imgObjects.setCalibration(img.getCalibration());
        FileSaver ImgObjectsFile = new FileSaver(imgObjects);
        ImgObjectsFile.saveAsTiff(outDir + imageName + "_Objects.tif"); 
        imgObj1.closeImagePlus();
        imgObj2.closeImagePlus();
        imgObj3.closeImagePlus();
        imgObj4.closeImagePlus();
        flush_close(imgObjects);
    }
    
    
}
