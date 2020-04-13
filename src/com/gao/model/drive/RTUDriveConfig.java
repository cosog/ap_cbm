package com.gao.model.drive;


public class RTUDriveConfig {
	private String DriverName;
    
    private String DriverCode;

    private int Port;

    private int Protocol;

    private CMBWellDataConfig CMBWellDataConfig;
    
    private GroupValveDataConfig GroupValveDataConfig;
    
    private BoosterPumpDataConfig BoosterPumpDataConfig;

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
    public void setCMBWellDataConfig(CMBWellDataConfig CMBWellDataConfig){
        this.CMBWellDataConfig = CMBWellDataConfig;
    }
    public CMBWellDataConfig getCMBWellDataConfig(){
        return this.CMBWellDataConfig;
    }
	public String getDriverCode() {
		return DriverCode;
	}
	public void setDriverCode(String driverCode) {
		DriverCode = driverCode;
	}
	
	public GroupValveDataConfig getGroupValveDataConfig() {
		return GroupValveDataConfig;
	}
	public void setGroupValveDataConfig(GroupValveDataConfig groupValveDataConfig) {
		GroupValveDataConfig = groupValveDataConfig;
	}

	public BoosterPumpDataConfig getBoosterPumpDataConfig() {
		return BoosterPumpDataConfig;
	}
	public void setBoosterPumpDataConfig(BoosterPumpDataConfig boosterPumpDataConfig) {
		BoosterPumpDataConfig = boosterPumpDataConfig;
	}



	public static class Item
	{
	    private int Address;

	    private int DataType;

	    private int Length;

	    private float ZoomX;
	    
	    private float ZoomY;

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
		public float getZoomX() {
			return ZoomX;
		}
		public void setZoomX(float zoomX) {
			ZoomX = zoomX;
		}
		public float getZoomY() {
			return ZoomY;
		}
		public void setZoomY(float zoomY) {
			ZoomY = zoomY;
		}
	    
	}
	
	public static class CMBWellDataConfig
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

	public static class GroupValveDataConfig
	{
		private Item CumulativeFlow1;
		private Item FlowmeterBackupPoint1;
		private Item InstantaneousFlow1;
		private Item FlowmeterTemperature1;
		private Item FlowmeterPress1;
		
		private Item CumulativeFlow2;
		private Item FlowmeterBackupPoint2;
		private Item InstantaneousFlow2;
		private Item FlowmeterTemperature2;
		private Item FlowmeterPress2;
		
		private Item CumulativeFlow3;
		private Item FlowmeterBackupPoint3;
		private Item InstantaneousFlow3;
		private Item FlowmeterTemperature3;
		private Item FlowmeterPress3;
		
		private Item CumulativeFlow4;
		private Item FlowmeterBackupPoint4;
		private Item InstantaneousFlow4;
		private Item FlowmeterTemperature4;
		private Item FlowmeterPress4;
		
		private Item DeviceId;
		private Item BaudRate;
		private Item InstrumentCombinationMode1;
		private Item InstrumentCombinationMode2;
		private Item InstrumentCombinationMode3;
		private Item InstrumentCombinationMode4;
		public Item getCumulativeFlow1() {
			return CumulativeFlow1;
		}
		public void setCumulativeFlow1(Item cumulativeFlow1) {
			CumulativeFlow1 = cumulativeFlow1;
		}
		public Item getFlowmeterBackupPoint1() {
			return FlowmeterBackupPoint1;
		}
		public void setFlowmeterBackupPoint1(Item flowmeterBackupPoint1) {
			FlowmeterBackupPoint1 = flowmeterBackupPoint1;
		}
		public Item getInstantaneousFlow1() {
			return InstantaneousFlow1;
		}
		public void setInstantaneousFlow1(Item instantaneousFlow1) {
			InstantaneousFlow1 = instantaneousFlow1;
		}
		public Item getFlowmeterTemperature1() {
			return FlowmeterTemperature1;
		}
		public void setFlowmeterTemperature1(Item flowmeterTemperature1) {
			FlowmeterTemperature1 = flowmeterTemperature1;
		}
		public Item getFlowmeterPress1() {
			return FlowmeterPress1;
		}
		public void setFlowmeterPress1(Item flowmeterPress1) {
			FlowmeterPress1 = flowmeterPress1;
		}
		public Item getCumulativeFlow2() {
			return CumulativeFlow2;
		}
		public void setCumulativeFlow2(Item cumulativeFlow2) {
			CumulativeFlow2 = cumulativeFlow2;
		}
		public Item getFlowmeterBackupPoint2() {
			return FlowmeterBackupPoint2;
		}
		public void setFlowmeterBackupPoint2(Item flowmeterBackupPoint2) {
			FlowmeterBackupPoint2 = flowmeterBackupPoint2;
		}
		public Item getInstantaneousFlow2() {
			return InstantaneousFlow2;
		}
		public void setInstantaneousFlow2(Item instantaneousFlow2) {
			InstantaneousFlow2 = instantaneousFlow2;
		}
		public Item getFlowmeterTemperature2() {
			return FlowmeterTemperature2;
		}
		public void setFlowmeterTemperature2(Item flowmeterTemperature2) {
			FlowmeterTemperature2 = flowmeterTemperature2;
		}
		public Item getFlowmeterPress2() {
			return FlowmeterPress2;
		}
		public void setFlowmeterPress2(Item flowmeterPress2) {
			FlowmeterPress2 = flowmeterPress2;
		}
		public Item getCumulativeFlow3() {
			return CumulativeFlow3;
		}
		public void setCumulativeFlow3(Item cumulativeFlow3) {
			CumulativeFlow3 = cumulativeFlow3;
		}
		public Item getFlowmeterBackupPoint3() {
			return FlowmeterBackupPoint3;
		}
		public void setFlowmeterBackupPoint3(Item flowmeterBackupPoint3) {
			FlowmeterBackupPoint3 = flowmeterBackupPoint3;
		}
		public Item getInstantaneousFlow3() {
			return InstantaneousFlow3;
		}
		public void setInstantaneousFlow3(Item instantaneousFlow3) {
			InstantaneousFlow3 = instantaneousFlow3;
		}
		public Item getFlowmeterTemperature3() {
			return FlowmeterTemperature3;
		}
		public void setFlowmeterTemperature3(Item flowmeterTemperature3) {
			FlowmeterTemperature3 = flowmeterTemperature3;
		}
		public Item getFlowmeterPress3() {
			return FlowmeterPress3;
		}
		public void setFlowmeterPress3(Item flowmeterPress3) {
			FlowmeterPress3 = flowmeterPress3;
		}
		public Item getCumulativeFlow4() {
			return CumulativeFlow4;
		}
		public void setCumulativeFlow4(Item cumulativeFlow4) {
			CumulativeFlow4 = cumulativeFlow4;
		}
		public Item getFlowmeterBackupPoint4() {
			return FlowmeterBackupPoint4;
		}
		public void setFlowmeterBackupPoint4(Item flowmeterBackupPoint4) {
			FlowmeterBackupPoint4 = flowmeterBackupPoint4;
		}
		public Item getInstantaneousFlow4() {
			return InstantaneousFlow4;
		}
		public void setInstantaneousFlow4(Item instantaneousFlow4) {
			InstantaneousFlow4 = instantaneousFlow4;
		}
		public Item getFlowmeterTemperature4() {
			return FlowmeterTemperature4;
		}
		public void setFlowmeterTemperature4(Item flowmeterTemperature4) {
			FlowmeterTemperature4 = flowmeterTemperature4;
		}
		public Item getFlowmeterPress4() {
			return FlowmeterPress4;
		}
		public void setFlowmeterPress4(Item flowmeterPress4) {
			FlowmeterPress4 = flowmeterPress4;
		}
		public Item getDeviceId() {
			return DeviceId;
		}
		public void setDeviceId(Item deviceId) {
			DeviceId = deviceId;
		}
		public Item getBaudRate() {
			return BaudRate;
		}
		public void setBaudRate(Item baudRate) {
			BaudRate = baudRate;
		}
		public Item getInstrumentCombinationMode1() {
			return InstrumentCombinationMode1;
		}
		public void setInstrumentCombinationMode1(Item instrumentCombinationMode1) {
			InstrumentCombinationMode1 = instrumentCombinationMode1;
		}
		public Item getInstrumentCombinationMode2() {
			return InstrumentCombinationMode2;
		}
		public void setInstrumentCombinationMode2(Item instrumentCombinationMode2) {
			InstrumentCombinationMode2 = instrumentCombinationMode2;
		}
		public Item getInstrumentCombinationMode3() {
			return InstrumentCombinationMode3;
		}
		public void setInstrumentCombinationMode3(Item instrumentCombinationMode3) {
			InstrumentCombinationMode3 = instrumentCombinationMode3;
		}
		public Item getInstrumentCombinationMode4() {
			return InstrumentCombinationMode4;
		}
		public void setInstrumentCombinationMode4(Item instrumentCombinationMode4) {
			InstrumentCombinationMode4 = instrumentCombinationMode4;
		}
	}
	
	public static class BoosterPumpDataConfig
	{
		private Item InletGasPressure;
		private Item OutletGasPressure;
		private Item SupplyOilPressure;
		private Item InletGasPressureDifference;
		private Item OilBranchCorePressureDifference;
		private Item OilFilterPressureDifference;
		private Item OilBranchTankLiquidLevel;
		private Item FlammableGasConcentration;
		private Item SupplyGasPressure;
		private Item SupplyGasTemperature;
		private Item MainMotorFrequency;
		private Item MainMotorCurrent;
		private Item InletGasTemperature;
		private Item OutletGasTemperature;
		private Item OilBranchTankTemperature;
		private Item SupplyOilTemperature;
		private Item LubricatingOilServiceTime;
		private Item GreaseServiceTime;
		private Item OilFilterServiceTime;
		private Item OilBranchCoreServiceTime;
		private Item InletGasFilterServiceTime;
		private Item MechanicalSealServiceTime;
		private Item HostTotalRunTime;
		private Item HostCurrentRunTime;
		private Item Status1;
		private Item Status2;
		private Item Status3;
		private Item Status4;
		private Item Status5;
		private Item Status6;
		private Item Status7;
		private Item Status8;
		public Item getInletGasPressure() {
			return InletGasPressure;
		}
		public void setInletGasPressure(Item inletGasPressure) {
			InletGasPressure = inletGasPressure;
		}
		public Item getOutletGasPressure() {
			return OutletGasPressure;
		}
		public void setOutletGasPressure(Item outletGasPressure) {
			OutletGasPressure = outletGasPressure;
		}
		public Item getSupplyOilPressure() {
			return SupplyOilPressure;
		}
		public void setSupplyOilPressure(Item supplyOilPressure) {
			SupplyOilPressure = supplyOilPressure;
		}
		public Item getInletGasPressureDifference() {
			return InletGasPressureDifference;
		}
		public void setInletGasPressureDifference(Item inletGasPressureDifference) {
			InletGasPressureDifference = inletGasPressureDifference;
		}
		public Item getOilBranchCorePressureDifference() {
			return OilBranchCorePressureDifference;
		}
		public void setOilBranchCorePressureDifference(Item oilBranchCorePressureDifference) {
			OilBranchCorePressureDifference = oilBranchCorePressureDifference;
		}
		public Item getOilFilterPressureDifference() {
			return OilFilterPressureDifference;
		}
		public void setOilFilterPressureDifference(Item oilFilterPressureDifference) {
			OilFilterPressureDifference = oilFilterPressureDifference;
		}
		public Item getOilBranchTankLiquidLevel() {
			return OilBranchTankLiquidLevel;
		}
		public void setOilBranchTankLiquidLevel(Item oilBranchTankLiquidLevel) {
			OilBranchTankLiquidLevel = oilBranchTankLiquidLevel;
		}
		public Item getFlammableGasConcentration() {
			return FlammableGasConcentration;
		}
		public void setFlammableGasConcentration(Item flammableGasConcentration) {
			FlammableGasConcentration = flammableGasConcentration;
		}
		public Item getSupplyGasPressure() {
			return SupplyGasPressure;
		}
		public void setSupplyGasPressure(Item supplyGasPressure) {
			SupplyGasPressure = supplyGasPressure;
		}
		public Item getSupplyGasTemperature() {
			return SupplyGasTemperature;
		}
		public void setSupplyGasTemperature(Item supplyGasTemperature) {
			SupplyGasTemperature = supplyGasTemperature;
		}
		public Item getMainMotorFrequency() {
			return MainMotorFrequency;
		}
		public void setMainMotorFrequency(Item mainMotorFrequency) {
			MainMotorFrequency = mainMotorFrequency;
		}
		public Item getMainMotorCurrent() {
			return MainMotorCurrent;
		}
		public void setMainMotorCurrent(Item mainMotorCurrent) {
			MainMotorCurrent = mainMotorCurrent;
		}
		public Item getInletGasTemperature() {
			return InletGasTemperature;
		}
		public void setInletGasTemperature(Item inletGasTemperature) {
			InletGasTemperature = inletGasTemperature;
		}
		public Item getOutletGasTemperature() {
			return OutletGasTemperature;
		}
		public void setOutletGasTemperature(Item outletGasTemperature) {
			OutletGasTemperature = outletGasTemperature;
		}
		public Item getOilBranchTankTemperature() {
			return OilBranchTankTemperature;
		}
		public void setOilBranchTankTemperature(Item oilBranchTankTemperature) {
			OilBranchTankTemperature = oilBranchTankTemperature;
		}
		public Item getSupplyOilTemperature() {
			return SupplyOilTemperature;
		}
		public void setSupplyOilTemperature(Item supplyOilTemperature) {
			SupplyOilTemperature = supplyOilTemperature;
		}
		public Item getLubricatingOilServiceTime() {
			return LubricatingOilServiceTime;
		}
		public void setLubricatingOilServiceTime(Item lubricatingOilServiceTime) {
			LubricatingOilServiceTime = lubricatingOilServiceTime;
		}
		public Item getGreaseServiceTime() {
			return GreaseServiceTime;
		}
		public void setGreaseServiceTime(Item greaseServiceTime) {
			GreaseServiceTime = greaseServiceTime;
		}
		public Item getOilFilterServiceTime() {
			return OilFilterServiceTime;
		}
		public void setOilFilterServiceTime(Item oilFilterServiceTime) {
			OilFilterServiceTime = oilFilterServiceTime;
		}
		public Item getOilBranchCoreServiceTime() {
			return OilBranchCoreServiceTime;
		}
		public void setOilBranchCoreServiceTime(Item oilBranchCoreServiceTime) {
			OilBranchCoreServiceTime = oilBranchCoreServiceTime;
		}
		public Item getInletGasFilterServiceTime() {
			return InletGasFilterServiceTime;
		}
		public void setInletGasFilterServiceTime(Item inletGasFilterServiceTime) {
			InletGasFilterServiceTime = inletGasFilterServiceTime;
		}
		public Item getMechanicalSealServiceTime() {
			return MechanicalSealServiceTime;
		}
		public void setMechanicalSealServiceTime(Item mechanicalSealServiceTime) {
			MechanicalSealServiceTime = mechanicalSealServiceTime;
		}
		public Item getHostTotalRunTime() {
			return HostTotalRunTime;
		}
		public void setHostTotalRunTime(Item hostTotalRunTime) {
			HostTotalRunTime = hostTotalRunTime;
		}
		public Item getHostCurrentRunTime() {
			return HostCurrentRunTime;
		}
		public void setHostCurrentRunTime(Item hostCurrentRunTime) {
			HostCurrentRunTime = hostCurrentRunTime;
		}
		public Item getStatus1() {
			return Status1;
		}
		public void setStatus1(Item status1) {
			Status1 = status1;
		}
		public Item getStatus2() {
			return Status2;
		}
		public void setStatus2(Item status2) {
			Status2 = status2;
		}
		public Item getStatus3() {
			return Status3;
		}
		public void setStatus3(Item status3) {
			Status3 = status3;
		}
		public Item getStatus4() {
			return Status4;
		}
		public void setStatus4(Item status4) {
			Status4 = status4;
		}
		public Item getStatus5() {
			return Status5;
		}
		public void setStatus5(Item status5) {
			Status5 = status5;
		}
		public Item getStatus6() {
			return Status6;
		}
		public void setStatus6(Item status6) {
			Status6 = status6;
		}
		public Item getStatus7() {
			return Status7;
		}
		public void setStatus7(Item status7) {
			Status7 = status7;
		}
		public Item getStatus8() {
			return Status8;
		}
		public void setStatus8(Item status8) {
			Status8 = status8;
		}
		
		
	}
}
