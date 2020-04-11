package com.gao.model;

public class AcquisitionUnitData {

	private String AcquisitionUnitName;

    private String AcquisitionUnitCode;
    
    private int RTUStatus=0;
    private int runStatus=0;
    private int SPM=0;
    private int AI1=0;
    private int AI2=0;
    private int AI3=0;
    private int AI4=0;
    private int gasFlowmeter=0;
    private int gasFlowmeterCommStatus=0;
    private int gasInstantaneousFlow=0;
    private int gasCumulativeFlow=0;
    private int gasFlowmeterPress=0;
    private int liquidFlowmeter=0;
    private int liquidFlowmeterCommStatus=0;
    private int liquidInstantaneousFlow=0;
    private int liquidCumulativeFlow=0;
    private int liquidFlowmeterProd=0;
    private int fluidLevelIndicator=0;
    private int fluidLevelIndicatorCommStatus=0;
    private int fluidLevelAcquisitionTime=0;
    private int fluidLevelIndicatorSoundVelocity=0;
    private int fluidLevel=0;
    private int fluidLevelIndicatorPress=0;
    private int frequencyChanger=0;
    private int frequencyChangerCommStatus=0;
    private int frequencyChangerStatus=0;
    private int frequencyChangerStatus2=0;
    private int runFrequency=0;
    private int frequencyChangerBusbarVoltage=0;
    private int frequencyChangerOutputVoltage=0;
    private int frequencyChangerOutputCurrent=0;
    private int setFrequencyFeedback=0;
    private int frequencyChangerFaultCode=0;
    private int frequencyChangerPosition=0;
    private int frequencyChangerManufacturerCode=0;

    
    private int wellStartup=0;
    private int wellStop=0;
    private int frequencyOrRPMControlSign=0;
    private int frequencySetValue=0;
    private int SPMSetValue=0;
    private int SPMBy10Hz=0;
    private int SPMBy50Hz=0;
    private int RTUAddr=0;
    private int RTUProgramVersion=0;
    private int setWellName=0;

    
    public void init(){
    	this.RTUStatus=0;
        this.runStatus=0;
        this.SPM=0;
        this.AI1=0;
        this.AI2=0;
        this.AI3=0;
        this.AI4=0;
        this.gasFlowmeter=0;
        this.gasFlowmeterCommStatus=0;
        this.gasInstantaneousFlow=0;
        this.gasCumulativeFlow=0;
        this.gasFlowmeterPress=0;
        
        this.liquidFlowmeter=0;
        this.liquidFlowmeterCommStatus=0;
        this.liquidInstantaneousFlow=0;
        this.liquidCumulativeFlow=0;
        this.liquidFlowmeterProd=0;
        
        this.fluidLevelIndicator=0;
        this.fluidLevelIndicatorCommStatus=0;
        this.fluidLevelAcquisitionTime=0;
        this.fluidLevelIndicatorSoundVelocity=0;
        this.fluidLevel=0;
        this.fluidLevelIndicatorPress=0;
        
        this.frequencyChanger=0;
        this.frequencyChangerCommStatus=0;
        this.frequencyChangerStatus=0;
        this.frequencyChangerStatus2=0;
        this.runFrequency=0;
        this.frequencyChangerBusbarVoltage=0;
        this.frequencyChangerOutputVoltage=0;
        this.frequencyChangerOutputCurrent=0;
        this.setFrequencyFeedback=0;
        this.frequencyChangerFaultCode=0;
        this.frequencyChangerPosition=0;
        this.frequencyChangerManufacturerCode=0;

        this.wellStartup=0;
        this.wellStop=0;
        this.frequencyOrRPMControlSign=0;
        this.frequencySetValue=0;
        this.SPMSetValue=0;
        this.SPMBy10Hz=0;
        this.SPMBy50Hz=0;
        this.RTUAddr=0;
        this.RTUProgramVersion=0;
        this.setWellName=0;
    }


	public String getAcquisitionUnitName() {
		return AcquisitionUnitName;
	}


	public void setAcquisitionUnitName(String acquisitionUnitName) {
		AcquisitionUnitName = acquisitionUnitName;
	}


	public String getAcquisitionUnitCode() {
		return AcquisitionUnitCode;
	}


	public void setAcquisitionUnitCode(String acquisitionUnitCode) {
		AcquisitionUnitCode = acquisitionUnitCode;
	}


	public int getRTUStatus() {
		return RTUStatus;
	}


	public void setRTUStatus(int rTUStatus) {
		RTUStatus = rTUStatus;
	}


	public int getRunStatus() {
		return runStatus;
	}


	public void setRunStatus(int runStatus) {
		this.runStatus = runStatus;
	}


	public int getSPM() {
		return SPM;
	}


	public void setSPM(int sPM) {
		SPM = sPM;
	}


	public int getAI1() {
		return AI1;
	}


	public void setAI1(int aI1) {
		AI1 = aI1;
	}


	public int getAI2() {
		return AI2;
	}


	public void setAI2(int aI2) {
		AI2 = aI2;
	}


	public int getAI3() {
		return AI3;
	}


	public void setAI3(int aI3) {
		AI3 = aI3;
	}


	public int getAI4() {
		return AI4;
	}


	public void setAI4(int aI4) {
		AI4 = aI4;
	}


	public int getGasFlowmeter() {
		return gasFlowmeter;
	}


	public void setGasFlowmeter(int gasFlowmeter) {
		this.gasFlowmeter = gasFlowmeter;
	}


	public int getGasFlowmeterCommStatus() {
		return gasFlowmeterCommStatus;
	}


	public void setGasFlowmeterCommStatus(int gasFlowmeterCommStatus) {
		this.gasFlowmeterCommStatus = gasFlowmeterCommStatus;
	}


	public int getGasInstantaneousFlow() {
		return gasInstantaneousFlow;
	}


	public void setGasInstantaneousFlow(int gasInstantaneousFlow) {
		this.gasInstantaneousFlow = gasInstantaneousFlow;
	}


	public int getGasCumulativeFlow() {
		return gasCumulativeFlow;
	}


	public void setGasCumulativeFlow(int gasCumulativeFlow) {
		this.gasCumulativeFlow = gasCumulativeFlow;
	}


	public int getGasFlowmeterPress() {
		return gasFlowmeterPress;
	}


	public void setGasFlowmeterPress(int gasFlowmeterPress) {
		this.gasFlowmeterPress = gasFlowmeterPress;
	}


	public int getLiquidFlowmeter() {
		return liquidFlowmeter;
	}


	public void setLiquidFlowmeter(int liquidFlowmeter) {
		this.liquidFlowmeter = liquidFlowmeter;
	}


	public int getLiquidFlowmeterCommStatus() {
		return liquidFlowmeterCommStatus;
	}


	public void setLiquidFlowmeterCommStatus(int liquidFlowmeterCommStatus) {
		this.liquidFlowmeterCommStatus = liquidFlowmeterCommStatus;
	}


	public int getLiquidInstantaneousFlow() {
		return liquidInstantaneousFlow;
	}


	public void setLiquidInstantaneousFlow(int liquidInstantaneousFlow) {
		this.liquidInstantaneousFlow = liquidInstantaneousFlow;
	}


	public int getLiquidCumulativeFlow() {
		return liquidCumulativeFlow;
	}


	public void setLiquidCumulativeFlow(int liquidCumulativeFlow) {
		this.liquidCumulativeFlow = liquidCumulativeFlow;
	}


	public int getLiquidFlowmeterProd() {
		return liquidFlowmeterProd;
	}


	public void setLiquidFlowmeterProd(int liquidFlowmeterProd) {
		this.liquidFlowmeterProd = liquidFlowmeterProd;
	}


	public int getFluidLevelIndicator() {
		return fluidLevelIndicator;
	}


	public void setFluidLevelIndicator(int fluidLevelIndicator) {
		this.fluidLevelIndicator = fluidLevelIndicator;
	}


	public int getFluidLevelIndicatorCommStatus() {
		return fluidLevelIndicatorCommStatus;
	}


	public void setFluidLevelIndicatorCommStatus(int fluidLevelIndicatorCommStatus) {
		this.fluidLevelIndicatorCommStatus = fluidLevelIndicatorCommStatus;
	}


	public int getFluidLevelAcquisitionTime() {
		return fluidLevelAcquisitionTime;
	}


	public void setFluidLevelAcquisitionTime(int fluidLevelAcquisitionTime) {
		this.fluidLevelAcquisitionTime = fluidLevelAcquisitionTime;
	}


	public int getFluidLevelIndicatorSoundVelocity() {
		return fluidLevelIndicatorSoundVelocity;
	}


	public void setFluidLevelIndicatorSoundVelocity(int fluidLevelIndicatorSoundVelocity) {
		this.fluidLevelIndicatorSoundVelocity = fluidLevelIndicatorSoundVelocity;
	}


	public int getFluidLevel() {
		return fluidLevel;
	}


	public void setFluidLevel(int fluidLevel) {
		this.fluidLevel = fluidLevel;
	}


	public int getFrequencyChanger() {
		return frequencyChanger;
	}


	public void setFrequencyChanger(int frequencyChanger) {
		this.frequencyChanger = frequencyChanger;
	}


	public int getFrequencyChangerCommStatus() {
		return frequencyChangerCommStatus;
	}


	public void setFrequencyChangerCommStatus(int frequencyChangerCommStatus) {
		this.frequencyChangerCommStatus = frequencyChangerCommStatus;
	}


	public int getFrequencyChangerStatus() {
		return frequencyChangerStatus;
	}


	public void setFrequencyChangerStatus(int frequencyChangerStatus) {
		this.frequencyChangerStatus = frequencyChangerStatus;
	}


	public int getFrequencyChangerStatus2() {
		return frequencyChangerStatus2;
	}


	public void setFrequencyChangerStatus2(int frequencyChangerStatus2) {
		this.frequencyChangerStatus2 = frequencyChangerStatus2;
	}


	public int getRunFrequency() {
		return runFrequency;
	}


	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}


	public int getFrequencyChangerBusbarVoltage() {
		return frequencyChangerBusbarVoltage;
	}


	public void setFrequencyChangerBusbarVoltage(int frequencyChangerBusbarVoltage) {
		this.frequencyChangerBusbarVoltage = frequencyChangerBusbarVoltage;
	}


	public int getFrequencyChangerOutputVoltage() {
		return frequencyChangerOutputVoltage;
	}


	public void setFrequencyChangerOutputVoltage(int frequencyChangerOutputVoltage) {
		this.frequencyChangerOutputVoltage = frequencyChangerOutputVoltage;
	}


	public int getFrequencyChangerOutputCurrent() {
		return frequencyChangerOutputCurrent;
	}


	public void setFrequencyChangerOutputCurrent(int frequencyChangerOutputCurrent) {
		this.frequencyChangerOutputCurrent = frequencyChangerOutputCurrent;
	}


	public int getSetFrequencyFeedback() {
		return setFrequencyFeedback;
	}


	public void setSetFrequencyFeedback(int setFrequencyFeedback) {
		this.setFrequencyFeedback = setFrequencyFeedback;
	}


	public int getFrequencyChangerFaultCode() {
		return frequencyChangerFaultCode;
	}


	public void setFrequencyChangerFaultCode(int frequencyChangerFaultCode) {
		this.frequencyChangerFaultCode = frequencyChangerFaultCode;
	}


	public int getFrequencyChangerPosition() {
		return frequencyChangerPosition;
	}


	public void setFrequencyChangerPosition(int frequencyChangerPosition) {
		this.frequencyChangerPosition = frequencyChangerPosition;
	}


	public int getFrequencyChangerManufacturerCode() {
		return frequencyChangerManufacturerCode;
	}


	public void setFrequencyChangerManufacturerCode(int frequencyChangerManufacturerCode) {
		this.frequencyChangerManufacturerCode = frequencyChangerManufacturerCode;
	}


	public int getWellStartup() {
		return wellStartup;
	}


	public void setWellStartup(int wellStartup) {
		this.wellStartup = wellStartup;
	}


	public int getWellStop() {
		return wellStop;
	}


	public void setWellStop(int wellStop) {
		this.wellStop = wellStop;
	}


	public int getFrequencyOrRPMControlSign() {
		return frequencyOrRPMControlSign;
	}


	public void setFrequencyOrRPMControlSign(int frequencyOrRPMControlSign) {
		this.frequencyOrRPMControlSign = frequencyOrRPMControlSign;
	}


	public int getFrequencySetValue() {
		return frequencySetValue;
	}


	public void setFrequencySetValue(int frequencySetValue) {
		this.frequencySetValue = frequencySetValue;
	}


	public int getSPMSetValue() {
		return SPMSetValue;
	}


	public void setSPMSetValue(int sPMSetValue) {
		SPMSetValue = sPMSetValue;
	}


	public int getSPMBy10Hz() {
		return SPMBy10Hz;
	}


	public void setSPMBy10Hz(int sPMBy10Hz) {
		SPMBy10Hz = sPMBy10Hz;
	}


	public int getSPMBy50Hz() {
		return SPMBy50Hz;
	}


	public void setSPMBy50Hz(int sPMBy50Hz) {
		SPMBy50Hz = sPMBy50Hz;
	}


	public int getRTUAddr() {
		return RTUAddr;
	}


	public void setRTUAddr(int rTUAddr) {
		RTUAddr = rTUAddr;
	}


	public int getRTUProgramVersion() {
		return RTUProgramVersion;
	}


	public void setRTUProgramVersion(int rTUProgramVersion) {
		RTUProgramVersion = rTUProgramVersion;
	}


	public int getSetWellName() {
		return setWellName;
	}


	public void setSetWellName(int setWellName) {
		this.setWellName = setWellName;
	}


	public int getFluidLevelIndicatorPress() {
		return fluidLevelIndicatorPress;
	}


	public void setFluidLevelIndicatorPress(int fluidLevelIndicatorPress) {
		this.fluidLevelIndicatorPress = fluidLevelIndicatorPress;
	}
}
