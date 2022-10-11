/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GFP_PV_PNN_Tools;

/**
 *
 * @author phm
 */
public class Cells_PV {
    private double pvLabel;
    private double pvVol;
    private double pvSumInt;
    private double pvMeanInt;
    private double pvGFPSumInt;
    private double pvGFPMeanInt;
    private double pvDapiSumInt;
    private double pvDapiMeanInt;
    private boolean pvCell_PNN;
    private int pvCell_PNNLabel;
    private int pvNbGFPFoci;
    private double pvGFPFociVol;
    private double pvGFPFociSumInt;
    private double pvGFPFociMeanInt;
    private int pvNbDapiFoci;
    private double pvDapiFociVol;
    private double pvDapiFociSumInt;
    private double pvDapiFociMeanInt;
    private double pnnLabel;
    private double pnnVol;
    private double pnnSumInt;
    private double pnnMeanInt;
    private double pnnGFPSumInt;
    private double pnnGFPMeanInt;
    private double pnnDapiSumInt;
    private double pnnDapiMeanInt;
    private int pnnNbGFPFoci;
    private double pnnGFPFociVol;
    private double pnnGFPFociSumInt;
    private double pnnGFPFociMeanInt;
    private int pnnNbDapiFoci;
    private double pnnDapiFociVol;
    private double pnnDapiFociSumInt;
    private double pnnDapiFociMeanInt;  
    
   
	
	public Cells_PV(double pvLabel, boolean pvCell_PNN, int pvCell_PNNLabel, double pvVol, double pvInt, double pvGFPInt, int pvNbGFPFoci, double pvGFPFociVol, double pvGFPFociInt,
                double pvDapiInt, int pvNbDapiFoci, double pvDapiFociVol, double pvDapiFociInt, double pnnLabel, double pnnVol, double pnnInt, double pnnGFPInt,
                int pnnNbGFPFoci, double pnnGFPFociVol, double pnnGFPFociInt, double pnnDapiInt, int pnnNbDapiFoci, double pnnDapiFociVol, double pnnDapiFociInt) {
            this.pvLabel = pvLabel;
            this.pvCell_PNN = pvCell_PNN;
            this.pvCell_PNNLabel = pvCell_PNNLabel;
            this.pvVol = pvVol;
            this.pvInt = pvInt;
            this.pvGFPInt = pvGFPInt;
            this.pvNbGFPFoci = pvNbGFPFoci;
            this.pvGFPFociVol = pvGFPFociVol;
            this.pvGFPFociInt = pvGFPFociInt;
            this.pvDapiInt = pvDapiInt;
            this.pvNbDapiFoci = pvNbDapiFoci;
            this.pvDapiFociVol = pvDapiFociVol;
            this.pvDapiFociInt = pvDapiFociInt;
            this.pnnLabel = pnnLabel;
            this.pnnVol = pnnVol;
            this.pnnInt= pnnInt;
            this.pnnGFPInt = pnnGFPInt;
            this.pnnNbGFPFoci = pnnNbGFPFoci;
            this.pnnGFPFociVol = pnnGFPFociVol;
            this.pnnGFPFociInt = pnnGFPFociInt;
            this.pnnDapiInt = pnnDapiInt;
            this.pnnNbDapiFoci = pnnNbDapiFoci;
            this.pnnDapiFociVol = pnnDapiFociVol;
            this.pnnDapiFociInt = pnnDapiFociInt;
	}
        
        public void setPvCellLabel(double pvLabel) {
            this.pvLabel = pvLabel;
	}
         
        public void setPvCellIsPNN(boolean pvCell_PNN) {
            this.pvCell_PNN = pvCell_PNN;
	}
        
        public void setPvCellVol(double pvVol) {
            this.pvVol = pvVol;
	}
        
        public void setPvCellPNNLabel(int label) {
            this.pvCell_PNNLabel = label;
	}
        
        public void setPvCellInt(double pvInt) {
            this.pvInt = pvInt;
	}
        
        public void setPvCellGFPInt(double pvGFPInt) {
            this.pvGFPInt = pvGFPInt;
	}
        
        public void setPvNbGFPFoci(int pvNbGFPFoci) {
            this.pvNbGFPFoci = pvNbGFPFoci;
	}
        
        public void setPvGFPFociVol(double pvGFPFociVol) {
            this.pvGFPFociVol = pvGFPFociVol;
	}
        
        public void setPvGFPFociInt(double pvGFPFociInt) {
            this.pvGFPFociInt = pvGFPFociInt;
	}
        
        public void setPvCellDapiInt(double pvDapiInt) {
            this.pvDapiInt = pvDapiInt;
	}
        
        public void setPvNbDapiFoci(int pvNbDapiFoci) {
            this.pvNbDapiFoci = pvNbDapiFoci;
	}
        
        public void setPvDapiFociVol(double pvDapiFociVol) {
            this.pvDapiFociVol = pvDapiFociVol;
	}
        
        public void setPvDapiFociInt(double pvDapiFociInt) {
            this.pvDapiFociInt = pvDapiFociInt;
	}
        
        public void setPnnCellLabel(double pnnLabel) {
            this.pnnLabel = pnnLabel;
	}
        
        public void setPnnCellVol(double pnnVol) {
            this.pnnVol = pnnVol;
	}
        
        public void setPnnCellInt(double pnnInt) {
            this.pnnInt = pnnInt;
	}
        
        public void setPnnCellGFPInt(double pnnGFPInt) {
            this.pnnGFPInt = pnnGFPInt;
	}
        
        public void setPnnNbGFPFoci(int pnnNbGFPFoci) {
            this.pnnNbGFPFoci = pnnNbGFPFoci;
	}
        
        public void setPnnGFPFociVol(double pnnGFPFociVol) {
            this.pnnGFPFociVol = pnnGFPFociVol;
	}
        
        public void setPnnGFPFociInt(double pnnGFPFociInt) {
            this.pnnGFPFociInt = pnnGFPFociInt;
	}
        
        public void setPnnCellDapiInt(double pnnDapiInt) {
            this.pnnDapiInt = pnnDapiInt;
	}
        
        public void setPnnNbDapiFoci(int pnnNbDapiFoci) {
            this.pnnNbDapiFoci = pnnNbDapiFoci;
	}
        
        public void setPnnDapiFociVol(double pnnDapiFociVol) {
            this.pnnDapiFociVol = pnnDapiFociVol;
	}
        
        public void setPnnDapiFociInt(double pnnDapiFociInt) {
            this.pnnDapiFociInt = pnnDapiFociInt;
	}
        
        public double getPvCellLabel() {
            return(pvLabel);
	}
        
        public boolean getPvIsPNN() {
            return(pvCell_PNN);
	}
        
        public int getPvCellPNNLabel() {
            return(pvCell_PNNLabel);
	}
        
        public double getPvCellVol() {
            return(pvVol);
	}
        
        public double getPvCellInt() {
            return(pvInt);
	}
        
        public double getPvCellGFPInt() {
            return(pvGFPInt);
	}
        
        public int getPvNbGFPFoci() {
            return(pvNbGFPFoci);
	}
        
        public double getPvGFPFociVol() {
            return(pvGFPFociVol);
	}
        
        public double getPvGFPFociInt() {
            return(pvGFPFociInt);
	}
        
        public double getPvCellDapiInt() {
            return(pvDapiInt);
	}
        
        public int getPvNbDapiFoci() {
            return(pvNbDapiFoci);
	}
        
        public double getPvDapiFociVol() {
            return(pvDapiFociVol);
	}
        
        public double getPvFociDapiInt() {
            return(pvDapiFociInt);
	}
        
        public double getPnnCellLabel() {
            return(pnnLabel);
	}
        
        public double getPnnCellVol() {
            return(pnnVol);
	}
        
        public double getPnnCellInt() {
            return(pnnInt);
	}
        
        public double getPnnCellGFPInt() {
            return(pnnGFPInt);
	}
        
        public int getPnnNbGFPFoci() {
            return(pnnNbGFPFoci);
	}
        
        public double getPnnGFPFociVol() {
            return(pnnGFPFociVol);
	}
        
        public double getPnnGFPFociInt() {
            return(pnnGFPFociInt);
	}
        
        public double getPnnCellDapiInt() {
            return(pnnDapiInt);
	}
        
        public int getPnnNbDapiFoci() {
            return(pnnNbDapiFoci);
	}
        
        public double getPnnDapiFociVol() {
            return(pnnDapiFociVol);
	}
        
        public double getPnnDapiFociInt() {
            return(pnnDapiFociInt);
	}
        
}
