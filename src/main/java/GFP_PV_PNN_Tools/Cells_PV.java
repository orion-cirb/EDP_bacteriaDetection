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
    private double pvBgMeanInt;
    private double pvMeanInt;
    private double GFPBgMeanInt;
    private double pvGFPMeanInt;
    private double DapiBgMeanInt;
    private double pvDapiMeanInt;
    private int pvCell_PNN;
    private int pvCell_PNNLabel;
    private int pvNbGFPFoci;
    private double pvGFPFociVol;
    private double pvGFPFociMeanInt;
    private int pvNbDapiFoci;
    private double pvDapiFociVol;
    private double pvDapiFociMeanInt;
    private double pnnLabel;
    private double pnnVol;
    private double pnnBgMeanInt;
    private double pnnMeanInt;
    private double pnnGFPMeanInt;
    private double pnnDapiMeanInt;
    private int pnnNbGFPFoci;
    private double pnnGFPFociVol;
    private double pnnGFPFociMeanInt;
    private int pnnNbDapiFoci;
    private double pnnDapiFociVol;
    private double pnnDapiFociMeanInt;  
    
   
	
	public Cells_PV(double pvLabel, int pvCell_PNN, int pvCell_PNNLabel, double pvVol, double pvBgMeanInt, double pvMeanInt, double GFPBgMeanInt,
                double pvGFPMeanInt, int pvNbGFPFoci, double pvGFPFociVol, double pvGFPFociMeanInt, double DapiBgMeanInt, double pvDapiMeanInt, 
                int pvNbDapiFoci, double pvDapiFociVol,double pvDapiFociMeanInt, double pnnLabel, double pnnVol, double pnnBgMeanInt,
                double pnnMeanInt, double pnnGFPMeanInt, int pnnNbGFPFoci, double pnnGFPFociVol, double pnnGFPFociMeanInt, double pnnDapiMeanInt, int pnnNbDapiFoci, 
                double pnnDapiFociVol, double pnnDapiFociMeanInt) {
            this.pvLabel = pvLabel;
            this.pvCell_PNN = pvCell_PNN;
            this.pvCell_PNNLabel = pvCell_PNNLabel;
            this.pvVol = pvVol;
            this.pvMeanInt = pvMeanInt;
            this.pvBgMeanInt = pvBgMeanInt;
            this.pvGFPMeanInt = pvGFPMeanInt;
            this.GFPBgMeanInt = GFPBgMeanInt;
            this.pvNbGFPFoci = pvNbGFPFoci;
            this.pvGFPFociVol = pvGFPFociVol;
            this.pvGFPFociMeanInt = pvGFPFociMeanInt;
            this.pvDapiMeanInt = pvDapiMeanInt;
            this.DapiBgMeanInt = DapiBgMeanInt;
            this.pvNbDapiFoci = pvNbDapiFoci;
            this.pvDapiFociVol = pvDapiFociVol;
            this.pvDapiFociMeanInt = pvDapiFociMeanInt;
            this.pnnLabel = pnnLabel;
            this.pnnVol = pnnVol;
            this.pnnBgMeanInt = pnnBgMeanInt;
            this.pnnMeanInt= pnnMeanInt;
            this.pnnGFPMeanInt = pnnGFPMeanInt;
            this.pnnNbGFPFoci = pnnNbGFPFoci;
            this.pnnGFPFociVol = pnnGFPFociVol;
            this.pnnGFPFociMeanInt = pnnGFPFociMeanInt;
            this.pnnDapiMeanInt = pnnDapiMeanInt;
            this.pnnNbDapiFoci = pnnNbDapiFoci;
            this.pnnDapiFociVol = pnnDapiFociVol;
            this.pnnDapiFociMeanInt = pnnDapiFociMeanInt;
	}
        
        public void setPvCellLabel(double pvLabel) {
            this.pvLabel = pvLabel;
	}
         
        public void setPvCellIsPNN(int pvCell_PNN) {
            this.pvCell_PNN = pvCell_PNN;
	}
        
        public void setPvCellVol(double pvVol) {
            this.pvVol = pvVol;
	}
        
        public void setPvCellPNNLabel(int label) {
            this.pvCell_PNNLabel = label;
        }
        
        public void setPvBgMeanInt(double pvBgMeanInt) {
            this.pvBgMeanInt = pvBgMeanInt;
	}
        
        public void setPvCellMeanInt(double pvMeanInt) {
            this.pvMeanInt = pvMeanInt;
	}
        
        public void setGFPBgMeanInt(double GFPBgMeanInt) {
            this.GFPBgMeanInt = GFPBgMeanInt;
	}
        
        public void setPvCellGFPMeanInt(double pvGFPMeanInt) {
            this.pvGFPMeanInt = pvGFPMeanInt;
	}
        
        public void setPvNbGFPFoci(int pvNbGFPFoci) {
            this.pvNbGFPFoci = pvNbGFPFoci;
	}
        
        public void setPvGFPFociVol(double pvGFPFociVol) {
            this.pvGFPFociVol = pvGFPFociVol;
	}
        
        public void setPvGFPFociMeanInt(double pvGFPFociMeanInt) {
            this.pvGFPFociMeanInt = pvGFPFociMeanInt;
	}
        
        public void setDapiBgMeanInt(double DapiBgMeanInt) {
            this.DapiBgMeanInt = DapiBgMeanInt;
	}
        
        public void setPvCellDapiMeanInt(double pvDapiMeanInt) {
            this.pvDapiMeanInt = pvDapiMeanInt;
	}
        
        public void setPvNbDapiFoci(int pvNbDapiFoci) {
            this.pvNbDapiFoci = pvNbDapiFoci;
	}
        
        public void setPvDapiFociVol(double pvDapiFociVol) {
            this.pvDapiFociVol = pvDapiFociVol;
	}
        
        public void setPvDapiFociMeanInt(double pvDapiFociMeanInt) {
            this.pvDapiFociMeanInt = pvDapiFociMeanInt;
	}
        
        public void setPnnCellLabel(double pnnLabel) {
            this.pnnLabel = pnnLabel;
	}
        
        public void setPnnCellVol(double pnnVol) {
            this.pnnVol = pnnVol;
	}
        
        public void setPnnBgMeanInt(double pnnBgMeanInt) {
            this.pnnBgMeanInt = pnnBgMeanInt;
	}
        
        public void setPnnCellMeanInt(double pnnMeanInt) {
            this.pnnMeanInt = pnnMeanInt;
	}
        
        public void setPnnCellGFPMeanInt(double pnnGFPMeanInt) {
            this.pnnGFPMeanInt = pnnGFPMeanInt;
	}
        
        public void setPnnNbGFPFoci(int pnnNbGFPFoci) {
            this.pnnNbGFPFoci = pnnNbGFPFoci;
	}
        
        public void setPnnGFPFociVol(double pnnGFPFociVol) {
            this.pnnGFPFociVol = pnnGFPFociVol;
	}
        
        public void setPnnGFPFociMeanInt(double pnnGFPFociMeanInt) {
            this.pnnGFPFociMeanInt = pnnGFPFociMeanInt;
	}
        
        public void setPnnCellDapiMeanInt(double pnnDapiMeanInt) {
            this.pnnDapiMeanInt = pnnDapiMeanInt;
	}
        
        public void setPnnNbDapiFoci(int pnnNbDapiFoci) {
            this.pnnNbDapiFoci = pnnNbDapiFoci;
	}
        
        public void setPnnDapiFociVol(double pnnDapiFociVol) {
            this.pnnDapiFociVol = pnnDapiFociVol;
	}
        
        public void setPnnDapiFociMeanInt(double pnnDapiFociMeanInt) {
            this.pnnDapiFociMeanInt = pnnDapiFociMeanInt;
	}
        
        public double getPvCellLabel() {
            return(pvLabel);
	}
        
        public int getPvIsPNN() {
            return(pvCell_PNN);
	}
        
        public int getPvCellPNNLabel() {
            return(pvCell_PNNLabel);
	}
        
        public double getPvCellVol() {
            return(pvVol);
	}
        
        public double getPvBgMeanInt() {
            return(pvBgMeanInt);
	}
        
        public double getPvCellMeanInt() {
            return(pvMeanInt);
	}
        
        public double getGFPBgMeanInt() {
            return(GFPBgMeanInt);
	}
        
        public double getPvCellGFPMeanInt() {
            return(pvGFPMeanInt);
	}
        
        public int getPvNbGFPFoci() {
            return(pvNbGFPFoci);
	}
        
        public double getPvGFPFociVol() {
            return(pvGFPFociVol);
	}
        
        public double getPvGFPFociMeanInt() {
            return(pvGFPFociMeanInt);
	}
        
        public double getDapiBgMeanInt() {
            return(DapiBgMeanInt);
	}
        
        public double getPvCellDapiMeanInt() {
            return(pvDapiMeanInt);
	}
        
        public int getPvNbDapiFoci() {
            return(pvNbDapiFoci);
	}
        
        public double getPvDapiFociVol() {
            return(pvDapiFociVol);
	}
        
        public double getPvFociDapiMeanInt() {
            return(pvDapiFociMeanInt);
	}
        
        public double getPnnCellLabel() {
            return(pnnLabel);
	}
        
        public double getPnnCellVol() {
            return(pnnVol);
	}
        
        public double getPnnBgMeanInt() {
            return(pnnBgMeanInt);
	}
        
        public double getPnnCellMeanInt() {
            return(pnnMeanInt);
	}
        
        public double getPnnCellGFPMeanInt() {
            return(pnnGFPMeanInt);
	}
        
        public int getPnnNbGFPFoci() {
            return(pnnNbGFPFoci);
	}
        
        public double getPnnGFPFociVol() {
            return(pnnGFPFociVol);
	}
        
        public double getPnnGFPFociMeanInt() {
            return(pnnGFPFociMeanInt);
	}
        
        public double getPnnCellDapiInt() {
            return(pnnDapiMeanInt);
	}
        
        public int getPnnNbDapiFoci() {
            return(pnnNbDapiFoci);
	}
        
        public double getPnnDapiFociVol() {
            return(pnnDapiFociVol);
	}
        
        public double getPnnDapiFociMeanInt() {
            return(pnnDapiFociMeanInt);
	}
        
}
