package com.gao.model.drive;


public class RTUDriveConfig {
	private String DriverName;
    
    private String DriverCode;

    private int Port;

    private int Protocol;

    private DataConfig DataConfig;

    public void setDriverName(String DriverName){
        this.DriverName = DriverName;
    }
    public String getDriverName(){
        return this.DriverName;
    }
    public void setPort(int Port){
        this.Port = Port;
    }
    public int getPort(){
        return this.Port;
    }
    public void setProtocol(int Protocol){
        this.Protocol = Protocol;
    }
    public int getProtocol(){
        return this.Protocol;
    }
    public void setDataConfig(DataConfig DataConfig){
        this.DataConfig = DataConfig;
    }
    public DataConfig getDataConfig(){
        return this.DataConfig;
    }
	public String getDriverCode() {
		return DriverCode;
	}
	public void setDriverCode(String driverCode) {
		DriverCode = driverCode;
	}
	
	public static class Item
	{
	    private int Address;

	    private int DataType;

	    private int Length;

	    private float Zoom;

	    public void setAddress(int Address){
	        this.Address = Address;
	    }
	    public int getAddress(){
	        return this.Address;
	    }
	    public void setDataType(int DataType){
	        this.DataType = DataType;
	    }
	    public int getDataType(){
	        return this.DataType;
	    }
	    public void setLength(int Length){
	        this.Length = Length;
	    }
	    public int getLength(){
	        return this.Length;
	    }
	    public void setZoom(float Zoom){
	        this.Zoom = Zoom;
	    }
	    public float getZoom(){
	        return this.Zoom;
	    }
	}
	
	public static class DataConfig
	{
	    private Item RTUStatus;
	    private Item RTUSystemTime;
	    private Item RunStatus;
	    
	    private Item SPM;
	    private Item AI1;
	    private Item AI2;
	    private Item AI3;
	    private Item AI4;
	    
	    private Item gasFlowmeterCommStatus;
	    private Item gasInstantaneousFlow;
	    private Item gasCumulativeFlow;
	    private Item gasFlowmeterPress;
	    
	    private Item liquidFlowmeterCommStatus;
	    private Item liquidInstantaneousFlow;
	    private Item liquidCumulativeFlow;
	    private Item liquidFlowmeterProd;
	    
	    private Item fluidLevelIndicatorCommStatus;
	    private Item fluidLevelAcquisitionTime;
	    private Item fluidLevelIndicatorSoundVelocity;
	    private Item fluidLevel;
	    private Item fluidLevelIndicatorPress;
	    
	    private Item frequencyChangerCommStatus;
	    private Item frequencyChangerStatus;
	    private Item frequencyChangerStatus2;
	    private Item runFrequency;
	    private Item frequencyChangerBusbarVoltage;
	    private Item frequencyChangerOutputVoltage;
	    private Item frequencyChangerOutputCurrent;
	    private Item setFrequencyFeedback;
	    private Item frequencyChangerFaultCode;
	    private Item frequencyChangerPosition;
	    private Item frequencyChangerManufacturerCode;
	    
	    private Item wellStartup;
	    private Item wellStop;
	    private Item frequencyOrRPMControlSign;
	    private Item frequencySetValue;
	    private Item SPMSetValue;
	    private Item SPMBy10Hz;
	    private Item SPMBy50Hz;
	    private Item RTUAddr;
	    private Item RTUProgramVersion;
	    private Item setWellName;
		public Item getRTUStatus() {
			return RTUStatus;
		}
		public void setRTUStatus(Item rTUStatus) {
			RTUStatus = rTUStatus;
		}
		public Item getRTUSystemTime() {
			return RTUSystemTime;
		}
		public void setRTUSystemTime(Item rTUSystemTime) {
			RTUSystemTime = rTUSystemTime;
		}
		public Item getRunStatus() {
			return RunStatus;
		}
		public void setRunStatus(Item runStatus) {
			RunStatus = runStatus;
		}
		public Item getSPM() {
			return SPM;
		}
		public void setSPM(Item sPM) {
			SPM = sPM;
		}
		public Item getAI1() {
			return AI1;
		}
		public void setAI1(Item aI1) {
			AI1 = aI1;
		}
		public Item getAI2() {
			return AI2;
		}
		public void setAI2(Item aI2) {
			AI2 = aI2;
		}
		public Item getAI3() {
			return AI3;
		}
		public void setAI3(Item aI3) {
			AI3 = aI3;
		}
		public Item getAI4() {
			return AI4;
		}
		public void setAI4(Item aI4) {
			AI4 = aI4;
		}
		public Item getGasFlowmeterCommStatus() {
			return gasFlowmeterCommStatus;
		}
		public void setGasFlowmeterCommStatus(Item gasFlowmeterCommStatus) {
			this.gasFlowmeterCommStatus = gasFlowmeterCommStatus;
		}
		public Item getGasInstantaneousFlow() {
			return gasInstantaneousFlow;
		}
		public void setGasInstantaneousFlow(Item gasInstantaneousFlow) {
			this.gasInstantaneousFlow = gasInstantaneousFlow;
		}
		public Item getGasCumulativeFlow() {
			return gasCumulativeFlow;
		}
		public void setGasCumulativeFlow(Item gasCumulativeFlow) {
			this.gasCumulativeFlow = gasCumulativeFlow;
		}
		public Item getGasFlowmeterPress() {
			return gasFlowmeterPress;
		}
		public void setGasFlowmeterPress(Item gasFlowmeterPress) {
			this.gasFlowmeterPress = gasFlowmeterPress;
		}
		public Item getLiquidFlowmeterCommStatus() {
			return liquidFlowmeterCommStatus;
		}
		public void setLiquidFlowmeterCommStatus(Item liquidFlowmeterCommStatus) {
			this.liquidFlowmeterCommStatus = liquidFlowmeterCommStatus;
		}
		public Item getLiquidInstantaneousFlow() {
			return liquidInstantaneousFlow;
		}
		public void setLiquidInstantaneousFlow(Item liquidInstantaneousFlow) {
			this.liquidInstantaneousFlow = liquidInstantaneousFlow;
		}
		public Item getLiquidCumulativeFlow() {
			return liquidCumulativeFlow;
		}
		public void setLiquidCumulativeFlow(Item liquidCumulativeFlow) {
			this.liquidCumulativeFlow = liquidCumulativeFlow;
		}
		public Item getLiquidFlowmeterProd() {
			return liquidFlowmeterProd;
		}
		public void setLiquidFlowmeterProd(Item liquidFlowmeterProd) {
			this.liquidFlowmeterProd = liquidFlowmeterProd;
		}
		public Item getFluidLevelIndicatorCommStatus() {
			return fluidLevelIndicatorCommStatus;
		}
		public void setFluidLevelIndicatorCommStatus(Item fluidLevelIndicatorCommStatus) {
			this.fluidLevelIndicatorCommStatus = fluidLevelIndicatorCommStatus;
		}
		public Item getFluidLevelAcquisitionTime() {
			return fluidLevelAcquisitionTime;
		}
		public void setFluidLevelAcquisitionTime(Item fluidLevelAcquisitionTime) {
			this.fluidLevelAcquisitionTime = fluidLevelAcquisitionTime;
		}
		public Item getFluidLevelIndicatorSoundVelocity() {
			return fluidLevelIndicatorSoundVelocity;
		}
		public void setFluidLevelIndicatorSoundVelocity(Item fluidLevelIndicatorSoundVelocity) {
			this.fluidLevelIndicatorSoundVelocity = fluidLevelIndicatorSoundVelocity;
		}
		public Item getFluidLevel() {
			return fluidLevel;
		}
		public void setFluidLevel(Item fluidLevel) {
			this.fluidLevel = fluidLevel;
		}
		public Item getFluidLevelIndicatorPress() {
			return fluidLevelIndicatorPress;
		}
		public void setFluidLevelIndicatorPress(Item fluidLevelIndicatorPress) {
			this.fluidLevelIndicatorPress = fluidLevelIndicatorPress;
		}
		public Item getFrequencyChangerCommStatus() {
			return frequencyChangerCommStatus;
		}
		public void setFrequencyChangerCommStatus(Item frequencyChangerCommStatus) {
			this.frequencyChangerCommStatus = frequencyChangerCommStatus;
		}
		public Item getFrequencyChangerStatus() {
			return frequencyChangerStatus;
		}
		public void setFrequencyChangerStatus(Item frequencyChangerStatus) {
			this.frequencyChangerStatus = frequencyChangerStatus;
		}
		public Item getFrequencyChangerStatus2() {
			return frequencyChangerStatus2;
		}
		public void setFrequencyChangerStatus2(Item frequencyChangerStatus2) {
			this.frequencyChangerStatus2 = frequencyChangerStatus2;
		}
		public Item getRunFrequency() {
			return runFrequency;
		}
		public void setRunFrequency(Item runFrequency) {
			this.runFrequency = runFrequency;
		}
		public Item getFrequencyChangerBusbarVoltage() {
			return frequencyChangerBusbarVoltage;
		}
		public void setFrequencyChangerBusbarVoltage(Item frequencyChangerBusbarVoltage) {
			this.frequencyChangerBusbarVoltage = frequencyChangerBusbarVoltage;
		}
		public Item getFrequencyChangerOutputVoltage() {
			return frequencyChangerOutputVoltage;
		}
		public void setFrequencyChangerOutputVoltage(Item frequencyChangerOutputVoltage) {
			this.frequencyChangerOutputVoltage = frequencyChangerOutputVoltage;
		}
		public Item getFrequencyChangerOutputCurrent() {
			return frequencyChangerOutputCurrent;
		}
		public void setFrequencyChangerOutputCurrent(Item frequencyChangerOutputCurrent) {
			this.frequencyChangerOutputCurrent = frequencyChangerOutputCurrent;
		}
		public Item getSetFrequencyFeedback() {
			return setFrequencyFeedback;
		}
		public void setSetFrequencyFeedback(Item setFrequencyFeedback) {
			this.setFrequencyFeedback = setFrequencyFeedback;
		}
		public Item getFrequencyChangerFaultCode() {
			return frequencyChangerFaultCode;
		}
		public void setFrequencyChangerFaultCode(Item frequencyChangerFaultCode) {
			this.frequencyChangerFaultCode = frequencyChangerFaultCode;
		}
		public Item getFrequencyChangerPosition() {
			return frequencyChangerPosition;
		}
		public void setFrequencyChangerPosition(Item frequencyChangerPosition) {
			this.frequencyChangerPosition = frequencyChangerPosition;
		}
		public Item getFrequencyChangerManufacturerCode() {
			return frequencyChangerManufacturerCode;
		}
		public void setFrequencyChangerManufacturerCode(Item frequencyChangerManufacturerCode) {
			this.frequencyChangerManufacturerCode = frequencyChangerManufacturerCode;
		}
		public Item getWellStartup() {
			return wellStartup;
		}
		public void setWellStartup(Item wellStartup) {
			this.wellStartup = wellStartup;
		}
		public Item getWellStop() {
			return wellStop;
		}
		public void setWellStop(Item wellStop) {
			this.wellStop = wellStop;
		}
		public Item getFrequencyOrRPMControlSign() {
			return frequencyOrRPMControlSign;
		}
		public void setFrequencyOrRPMControlSign(Item frequencyOrRPMControlSign) {
			this.frequencyOrRPMControlSign = frequencyOrRPMControlSign;
		}
		public Item getFrequencySetValue() {
			return frequencySetValue;
		}
		public void setFrequencySetValue(Item frequencySetValue) {
			this.frequencySetValue = frequencySetValue;
		}
		public Item getSPMSetValue() {
			return SPMSetValue;
		}
		public void setSPMSetValue(Item sPMSetValue) {
			SPMSetValue = sPMSetValue;
		}
		public Item getSPMBy10Hz() {
			return SPMBy10Hz;
		}
		public void setSPMBy10Hz(Item sPMBy10Hz) {
			SPMBy10Hz = sPMBy10Hz;
		}
		public Item getSPMBy50Hz() {
			return SPMBy50Hz;
		}
		public void setSPMBy50Hz(Item sPMBy50Hz) {
			SPMBy50Hz = sPMBy50Hz;
		}
		public Item getRTUAddr() {
			return RTUAddr;
		}
		public void setRTUAddr(Item rTUAddr) {
			RTUAddr = rTUAddr;
		}
		public Item getRTUProgramVersion() {
			return RTUProgramVersion;
		}
		public void setRTUProgramVersion(Item rTUProgramVersion) {
			RTUProgramVersion = rTUProgramVersion;
		}
		public Item getSetWellName() {
			return setWellName;
		}
		public void setSetWellName(Item setWellName) {
			this.setWellName = setWellName;
		}
	    
	}

}
