package com.gao.tast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gao.model.AcquisitionUnitData;
import com.gao.model.AlarmShowStyle;
import com.gao.model.drive.RTUDriveConfig;
import com.gao.model.gridmodel.WellHandsontableChangedData;
import com.gao.tast.EquipmentDriverServerTast.AcquisitionData;
import com.gao.thread.calculate.DriveServerThread;
import com.gao.utils.AcquisitionUnitMap;
import com.gao.utils.DataModelMap;
import com.gao.utils.EquipmentDriveMap;
import com.gao.utils.OracleJdbcUtis;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("BeeTechDriverServerTast")  
public class EquipmentDriverServerTast {
	public static Connection conn = null;   
	public static PreparedStatement pstmt = null;  
	public static Statement stmt = null;  
	public static ResultSet rs = null; 
	public static List<UnitData> units=null;
	public static List<ClientUnit> clientUnitList=null;
	public static ServerSocket beeTechServerSocket;
	public static ServerSocket sunMoonServerSocket;
	public static boolean exit=false;
	//单例模式
	private static EquipmentDriverServerTast instance=new EquipmentDriverServerTast();
	
	
	public static EquipmentDriverServerTast getInstance(){
		return instance;
	}
	
	@Scheduled(fixedRate = 1000*60*60*24*365*100)
	public void driveServerTast() throws SQLException, ParseException,InterruptedException, IOException{
//		Gson gson = new Gson();
//		StringManagerUtils stringManagerUtils=new StringManagerUtils();
//		String url=Config.getInstance().configFile.getServer().getAccessPath()+"/graphicalUploadController/saveRTUAcquisitionData";
//		String path=stringManagerUtils.getFilePath("test.json","data/");
//		String json=stringManagerUtils.readFile(path,"utf-8");
//		StringManagerUtils.sendPostMethod(url, json,"utf-8");
		
		initDriverConfig();//初始化驱动配置
		boolean reg=false;
		do{
			reg=initAcquisitionUnit();//初始化采集单元信息
			if(!reg){
				Thread.sleep(5*1000);
			}
		}while(!reg);
		
		do{
			reg=init();//初始化井信息
			if(!reg){
				Thread.sleep(5*1000);
			}
		}while(!reg);
		
		Map<String, Object> equipmentDriveMap = EquipmentDriveMap.getMapObject();
		for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
			RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
			if(driveConfig.getPort()>0){
				try {
					ServerSocket serverSocket = new ServerSocket(driveConfig.getPort());
					DriveServerThread driveServerThread=new DriveServerThread(serverSocket,driveConfig);
					driveServerThread.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean init(){
		Map<String, Object> equipmentDriveMap = EquipmentDriveMap.getMapObject();
		Map<String, Object> acquisitionUnitMap = AcquisitionUnitMap.getMapObject();
		String sql="select t.wellName,t.unitType,t.driverAddr,t.driverId,t.acqcycle_discrete,t.savecycle_discrete,"
				+ " t.drivercode,"
				+ " to_char(t3.acquisitiontime,'yyyy-mm-dd hh24:mi:ss') as disAcquisitiontime,"
				+ " t3.commstatus,t3.commtime,t3.commtimeefficiency,t3.commrange,"
				+ " t3.runstatus,t3.runtime,t3.runtimeefficiency,t3.runrange"
				+ " from tbl_wellinformation t "
				+ " left outer join  tbl_cbm_discrete_latest  t3 on t3.wellId=t.id"
				+ " order by t.sortNum";
		String AcquisitionTime=StringManagerUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
		String resetCommStatus="update tbl_cbm_discrete_latest t set t.commstatus=0  ";
		if(clientUnitList!=null){
			for(int i=0;i<clientUnitList.size();i++){
				if(clientUnitList.get(i).thread!=null){
					clientUnitList.get(i).thread.interrupt();
				}
				if(clientUnitList.get(i).socket!=null){
					try {
						clientUnitList.get(i).socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				}
			}
			for(int i=0;i<clientUnitList.size();i++){
				clientUnitList.remove(i);
			}
		}
		units=new ArrayList<UnitData>();
		clientUnitList=new ArrayList<ClientUnit>();
		conn=OracleJdbcUtis.getConnection();
		if(conn==null){
			return false;
		}
		
		try {
			stmt=conn.createStatement();
			int result=stmt.executeUpdate(resetCommStatus);
			pstmt = conn.prepareStatement(sql); 
			rs=pstmt.executeQuery();
			while(rs.next()){
				UnitData unit=new UnitData();
				ClientUnit clientUnit=new ClientUnit();
				unit.wellName=rs.getString(1);
				unit.type=Integer.parseInt(rs.getString(2)==null?"1":rs.getString(2));
				unit.driverAddr=rs.getString(3)==null?"":rs.getString(3);
				unit.dirverId=rs.getString(4)==null?"":rs.getString(4);
				unit.UnitId=Integer.parseInt(rs.getString(4)==null?"0":rs.getString(4));
				unit.commStatus=0;
				unit.acquisitionData=new AcquisitionData();
				unit.acquisitionData.setRunStatus(0);
				unit.acqCycle_Discrete=60*1000*rs.getInt(5);
				unit.saveCycle_Discrete=60*1000*rs.getInt(6);//离散数据保存间隔,单位毫秒
				for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
					RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
					if(driveConfig.getDriverCode().equalsIgnoreCase(rs.getString(7))){
						unit.setRtuDriveConfig(driveConfig);
						unit.setDirverName(driveConfig.getDriverName());
						break;
					}
				}
				unit.lastDisAcquisitionTime=rs.getString(8);
				unit.lastCommStatus=rs.getInt(9);
				unit.lastCommTime=rs.getFloat(10);
				unit.lastCommTimeEfficiency=rs.getFloat(11);
				unit.lastCommRange=rs.getString(12);
				unit.lastRunStatus=rs.getInt(13);
				unit.lastRunTime=rs.getFloat(14);
				unit.lastRunTimeEfficiency=rs.getFloat(15);
				unit.lastRunRange=rs.getString(16);
				
				units.add(unit);
				clientUnitList.add(clientUnit);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
			return false;
		}
		
		System.out.println("共配置井数:"+units.size()+",分配线程数量:"+clientUnitList.size());
		OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
		exit=true;
		try {
			if(beeTechServerSocket!=null){
				beeTechServerSocket.close();
				beeTechServerSocket=null;
			}
			if(sunMoonServerSocket!=null){
				sunMoonServerSocket.close();
				sunMoonServerSocket=null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		exit=true;
		return true;
	}
	
	public static int updateWellConfig(WellHandsontableChangedData wellHandsontableChangedData) throws SQLException, IOException{
		String wellList="";
		Map<String, Object> equipmentDriveMap = EquipmentDriveMap.getMapObject();
		Map<String, Object> acquisitionUnitMap = AcquisitionUnitMap.getMapObject();
		if(wellHandsontableChangedData!=null){
			if(wellHandsontableChangedData.getUpdatelist()!=null){
				for(int i=0;i<wellHandsontableChangedData.getUpdatelist().size();i++){
					wellList+="'"+wellHandsontableChangedData.getUpdatelist().get(i).getWellName()+"',";
				}
			}
			
			if(wellHandsontableChangedData.getInsertlist()!=null){
				for(int i=0;i<wellHandsontableChangedData.getInsertlist().size();i++){
					wellList+="'"+wellHandsontableChangedData.getInsertlist().get(i).getWellName()+"',";
				}
			}
		}
		if(wellList.endsWith(",")){
			wellList=wellList.substring(0, wellList.length()-1);
		}
		
		String sql="select t.wellName,t.unitType,t.driveraddr,t.driverid,t.acqcycle_discrete,t.savecycle_discrete,"
				+ " t.drivercode "
				+ " from tbl_wellinformation t "
				+ " where 1=1  ";
		if(StringManagerUtils.isNotNull(wellList)){
			sql+=" and t.wellName in("+wellList+")";
		}
		sql+=" order by t.sortNum, t.wellName";
		conn=OracleJdbcUtis.getConnection();
		if(conn==null){
			return 0;
		}
		
		pstmt = conn.prepareStatement(sql); 
		rs=pstmt.executeQuery();
		while(units!=null&&rs.next()){
			boolean isAdd=true;
			for(int i=0;i<units.size();i++){
				if(units.get(i).wellName.equals(rs.getString(1))){//遍历目前内存中的井列表
					if(!units.get(i).driverAddr.equals(rs.getString(2)==null?"":rs.getString(2))
							||!units.get(i).dirverId.equals(rs.getString(3)==null?"":rs.getString(3))){//驱动信息发生改变
						System.out.println("配置发生变化");
						for(int j=0;j<clientUnitList.size();j++){//遍历已连接的客户端
							boolean isExit=false;
							if(clientUnitList.get(j).socket!=null){//如果已连接
								for(int k=0;k<clientUnitList.get(j).unitDataList.size();k++){
									if(units.get(i).driverAddr.equals(clientUnitList.get(j).unitDataList.get(k).driverAddr)
											||(rs.getString(3)==null?"":rs.getString(3)).equals(clientUnitList.get(j).unitDataList.get(k).driverAddr)){//查询原有设备地址和新地址的连接，如存在断开资源，释放资源
										if(clientUnitList.get(j).thread!=null){
											clientUnitList.get(j).thread.interrupt();
										}
										isExit=true;
									}
								}
							}
							if(isExit){
								break;
							}
						}
					}
					units.get(i).type=Integer.parseInt(rs.getString(2)==null?"1":rs.getString(2));
					units.get(i).driverAddr=rs.getString(3)==null?"":rs.getString(3);
					units.get(i).dirverId=rs.getString(4)==null?"":rs.getString(4);
					units.get(i).UnitId=Integer.parseInt(rs.getString(4)==null?"01":rs.getString(4));
					units.get(i).commStatus=0;
					units.get(i).acqCycle_Discrete=60*1000*rs.getInt(5);
					units.get(i).saveCycle_Discrete=60*1000*rs.getInt(6);//离散数据保存间隔,单位毫秒
					for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
						RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
						if(driveConfig.getDriverCode().equals(rs.getString(7))){
							units.get(i).setRtuDriveConfig(driveConfig);
							units.get(i).setDirverName(driveConfig.getDriverName());
							break;
						}
					}
					isAdd=false;
					break;
				}
			}
			if(isAdd){//如果新加井
				UnitData unit=new UnitData();
				ClientUnit clientUnit=new ClientUnit();
				unit.wellName=rs.getString(1);
				unit.type=Integer.parseInt(rs.getString(2)==null?"1":rs.getString(2));
				unit.driverAddr=rs.getString(3);
				unit.dirverId=rs.getString(4);
				unit.UnitId=Integer.parseInt(unit.dirverId);
				unit.commStatus=0;
				unit.acquisitionData=new AcquisitionData();
				unit.acquisitionData.setRunStatus(0);
				unit.acqCycle_Discrete=1000*rs.getInt(5);
				unit.saveCycle_Discrete=1000*rs.getInt(6);//离散数据保存间隔,单位毫秒
				unit.type=1;
				for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
					RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
					if(driveConfig.getDriverCode().equals(rs.getString(7))){
						unit.setRtuDriveConfig(driveConfig);
						unit.setDirverName(driveConfig.getDriverName());
						break;
					}
				}
				units.add(unit);
				clientUnitList.add(clientUnit);
				
				for(int j=0;j<clientUnitList.size();j++){
					boolean isExit=false;
					if(clientUnitList.get(j).socket!=null){
						for(int k=0;k<clientUnitList.get(j).unitDataList.size();k++){
							if(unit.driverAddr.equals(clientUnitList.get(j).unitDataList.get(k).driverAddr)){
								if(clientUnitList.get(j).thread!=null){
									clientUnitList.get(j).thread.interrupt();
								}
								isExit=true;
								break;
							}
						}
					}
					if(isExit){
						break;
					}
				}
			}
		}
		OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
		return 1;
	}
	
	public static int updateWellName(String data) throws SQLException, IOException{
		JSONObject jsonObject = JSONObject.fromObject("{\"data\":"+data+"}");//解析数据
		JSONArray jsonArray = jsonObject.getJSONArray("data");
		for(int i=0;i<jsonArray.size();i++){
			JSONObject everydata = JSONObject.fromObject(jsonArray.getString(i));
			String oldWellName=everydata.getString("oldWellName");
			String newWellName=everydata.getString("newWellName");
			for(int j=0;units!=null&&j<units.size();j++){
				if(oldWellName.equals(units.get(j).wellName)){
					units.get(j).setWellName(newWellName);
				}
			}
		}
		return 1;
	}
	
	@SuppressWarnings("static-access")
	public static void initDriverConfig(){
		Map<String, Object> equipmentDriveMap = EquipmentDriveMap.getMapObject();
		StringManagerUtils stringManagerUtils=new StringManagerUtils();
		Gson gson = new Gson();
		//添加山西中联煤层气驱动配置
		String path=stringManagerUtils.getFilePath("ShanXiCBMDriverConfig.json","data/");
		String DriverConfigData=stringManagerUtils.readFile(path,"utf-8");
		java.lang.reflect.Type type = new TypeToken<RTUDriveConfig>() {}.getType();
		RTUDriveConfig RTUDrive=gson.fromJson(DriverConfigData, type);
		if(RTUDrive.getPort()>0){
			equipmentDriveMap.put(RTUDrive.getDriverCode(), RTUDrive);
		}
	}
	
	@SuppressWarnings("static-access")
	public static boolean  initAcquisitionUnit(){
		Map<String, Object> acquisitionUnitMap = AcquisitionUnitMap.getMapObject();
		String sql="select t.unit_code,t.unit_name from tbl_acq_group_conf t order by id";
		conn=OracleJdbcUtis.getConnection();
		if(conn==null){
			return false;
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			ResultSet itemRs = null; 
			while(rs.next()){
				AcquisitionUnitData acquisitionUnitData=null;
				if(acquisitionUnitMap.containsKey(rs.getString(1))){
					acquisitionUnitData=(AcquisitionUnitData)acquisitionUnitMap.get(rs.getString(1));
					acquisitionUnitData.init();
				}else{
					acquisitionUnitData=new AcquisitionUnitData();
				}
				acquisitionUnitData.setAcquisitionUnitCode(rs.getString(1));
				acquisitionUnitData.setAcquisitionUnitName(rs.getString(2));
				String itemsSql="select t2.itemcode,t2.itemname "
						+ " from tbl_acq_item2group_conf t,tbl_acq_item_conf t2,tbl_acq_group_conf t3 "
						+ " where t.itemid=t2.id and  t.unitid=t3.id and t3.unit_code= '"+acquisitionUnitData.getAcquisitionUnitCode()+"'  "
						+ " and t2.id not in(select t4.parentid from tbl_acq_item_conf t4 )  order by t2.id";
				pstmt = conn.prepareStatement(itemsSql); 
				itemRs=pstmt.executeQuery();
				while(itemRs.next()){
					if("RTUStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setRTUStatus(1);
					
					else if("runStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setRunStatus(1);
					else if("SPM".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSPM(1);
					else if("AI1".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setAI1(1);
					else if("AI2".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setAI2(1);
					else if("AI3".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setAI3(1);
					else if("AI4".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setAI4(1);
					
					//气体质量流量计
					else if("gasFlowmeter".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setGasFlowmeter(1);
					else if("gasFlowmeterCommStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setGasFlowmeterCommStatus(1);
					else if("gasInstantaneousFlow".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setGasInstantaneousFlow(1);
					else if("gasCumulativeFlow".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setGasCumulativeFlow(1);
					else if("gasFlowmeterPress".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setGasFlowmeterPress(1);
					
					//量水仪
					else if("liquidFlowmeter".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setLiquidFlowmeter(1);
					else if("liquidFlowmeterCommStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setLiquidFlowmeterCommStatus(1);
					else if("liquidInstantaneousFlow".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setLiquidInstantaneousFlow(1);
					else if("liquidCumulativeFlow".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setLiquidCumulativeFlow(1);
					else if("liquidFlowmeterProd".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setLiquidFlowmeterProd(1);
					
					//液面仪
					else if("fluidLevelIndicator".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevelIndicator(1);
					else if("fluidLevelIndicatorCommStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevelIndicatorCommStatus(1);
					else if("fluidLevelAcquisitionTime".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevelAcquisitionTime(1);
					else if("fluidLevelIndicatorSoundVelocity".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevelIndicatorSoundVelocity(1);
					else if("fluidLevel".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevel(1);
					else if("fluidLevelIndicatorPress".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFluidLevelIndicatorPress(1);
					
					//变频器
					else if("frequencyChanger".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChanger(1);
					else if("frequencyChangerCommStatus".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerCommStatus(1);
					else if("frequencyChangerStatus2".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerStatus2(1);
					else if("runFrequency".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setRunFrequency(1);
					else if("frequencyChangerBusbarVoltage".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerBusbarVoltage(1);
					else if("frequencyChangerOutputVoltage".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerOutputVoltage(1);
					else if("frequencyChangerOutputCurrent".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerOutputCurrent(1);
					else if("setFrequencyFeedback".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSetFrequencyFeedback(1);
					else if("frequencyChangerFaultCode".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerFaultCode(1);
					else if("frequencyChangerPosition".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerPosition(1);
					else if("frequencyChangerManufacturerCode".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyChangerManufacturerCode(1);
					
					//控制项
					else if("frequencyOrRPMControlSign".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencyOrRPMControlSign(1);
					else if("frequencySetValue".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setFrequencySetValue(1);
					else if("SPMSetValue".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSPMSetValue(1);
					else if("SPMBy10Hz".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSPMBy10Hz(1);
					else if("SPMBy50Hz".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSPMBy50Hz(1);
					else if("RTUAddr".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setRTUAddr(1);
					else if("RTUProgramVersion".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setRTUProgramVersion(1);
					else if("setWellName".equalsIgnoreCase(itemRs.getString(1)))
						acquisitionUnitData.setSetWellName(1);
				}
				if(!acquisitionUnitMap.containsKey(rs.getString(1))){
					acquisitionUnitMap.put(acquisitionUnitData.getAcquisitionUnitCode(), acquisitionUnitData);
				}
			}
			if(itemRs!=null){
				itemRs.close();
			}
		} catch (SQLException e) {
			OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		
		OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
		return true;
	}
	
	public static void initAlarmStyle() throws IOException, SQLException{
		Map<String, Object> dataModelMap = DataModelMap.getMapObject();
		AlarmShowStyle alarmShowStyle=(AlarmShowStyle) dataModelMap.get("AlarmShowStyle");
		if(alarmShowStyle==null){
			alarmShowStyle=new AlarmShowStyle();
		}
		String sql="select v1.itemvalue,v1.itemname,v2.itemname,v3.itemname from "
				+ " (select * from tbl_code t where t.itemcode='BJYS' ) v1,"
				+ " (select * from tbl_code t where t.itemcode='BJQJYS' ) v2,"
				+ " (select * from tbl_code t where t.itemcode='BJYSTMD' ) v3 "
				+ " where v1.itemvalue=v2.itemvalue and v1.itemvalue=v3.itemvalue order by v1.itemvalue ";
		conn=OracleJdbcUtis.getConnection();
		if(conn==null){
			return ;
		}
		pstmt = conn.prepareStatement(sql); 
		rs=pstmt.executeQuery();
		while(rs.next()){
			if(rs.getInt(1)==0){
				alarmShowStyle.getNormal().setValue(rs.getInt(1));
				alarmShowStyle.getNormal().setBackgroundColor(rs.getString(2));
				alarmShowStyle.getNormal().setColor(rs.getString(3));
				alarmShowStyle.getNormal().setOpacity(rs.getString(4));
			}else if(rs.getInt(1)==100){
				alarmShowStyle.getFirstLevel().setValue(rs.getInt(1));
				alarmShowStyle.getFirstLevel().setBackgroundColor(rs.getString(2));
				alarmShowStyle.getFirstLevel().setColor(rs.getString(3));
				alarmShowStyle.getFirstLevel().setOpacity(rs.getString(4));
			}else if(rs.getInt(1)==200){
				alarmShowStyle.getSecondLevel().setValue(rs.getInt(1));
				alarmShowStyle.getSecondLevel().setBackgroundColor(rs.getString(2));
				alarmShowStyle.getSecondLevel().setColor(rs.getString(3));
				alarmShowStyle.getSecondLevel().setOpacity(rs.getString(4));
			}else if(rs.getInt(1)==300){
				alarmShowStyle.getThirdLevel().setValue(rs.getInt(1));
				alarmShowStyle.getThirdLevel().setBackgroundColor(rs.getString(2));
				alarmShowStyle.getThirdLevel().setColor(rs.getString(3));
				alarmShowStyle.getThirdLevel().setOpacity(rs.getString(4));
			}	
		}
		if(!dataModelMap.containsKey("AlarmShowStyle")){
			dataModelMap.put("AlarmShowStyle", alarmShowStyle);
		}
		OracleJdbcUtis.closeDBConnection(conn, stmt, pstmt, rs);
	}
	
	public static class AcquisitionData{
		public  String AcquisitionTime="";
		public  String ReadTime="";//读取数据时间
		public  String SaveTime="";//离散数据保存时间
		
		public int RTUStatus;
		public String RTUSystemTime;
		public int runStatus;
		public float SPM;
		public float AI1;
		public float AI2;
		public float AI3;
		public float AI4;
		
		public int gasFlowmeterCommStatus;
		public float gasInstantaneousFlow;
		public float gasCumulativeFlow;
		public float gasFlowmeterPress;
		
		public int liquidFlowmeterCommStatus;
		public float liquidInstantaneousFlow;
		public float liquidCumulativeFlow;
		public float liquidFlowmeterProd;

		public int fluidLevelIndicatorCommStatus;
		public String fluidLevelAcquisitionTime;
		public float fluidLevelIndicatorSoundVelocity;
		public float fluidLevel;
		public float fluidLevelIndicatorPress;
		
		public int frequencyChangerCommStatus;
		public int frequencyChangerStatus;
		public int frequencyChangerStatus2;
		public float runFrequency;
		public float frequencyChangerBusbarVoltage;
		public float frequencyChangerOutputVoltage;
		public float frequencyChangerOutputCurrent;
		public float setFrequencyFeedback;
		public int frequencyChangerFaultCode;
		public int frequencyChangerPosition;
		public int frequencyChangerManufacturerCode;
		
		public int frequencyOrRPMControlSign;
		public float frequencySetValue;
		public float SPMSetValue;
		public float SPMBy10Hz;
		public float SPMBy50Hz;
		public int RTUAddr;
		public int RTUProgramVersion;
		public int setWellName;

	    
	    public String RunRange;//运行区间字符串
	    public float RunTime;//运行时间
	    public float RunEfficiency;//运行时率
	    
	    public String CommRange;//通信区间字符串
	    public float CommTime;//在线时间
	    public float CommEfficiency;//在线时率
		public String getAcquisitionTime() {
			return AcquisitionTime;
		}
		public void setAcquisitionTime(String acquisitionTime) {
			AcquisitionTime = acquisitionTime;
		}
		
		public String getRTUSystemTime() {
			return RTUSystemTime;
		}
		public void setRTUSystemTime(String rTUSystemTime) {
			RTUSystemTime = rTUSystemTime;
		}
		public String getReadTime() {
			return ReadTime;
		}
		public void setReadTime(String readTime) {
			ReadTime = readTime;
		}
		public String getSaveTime() {
			return SaveTime;
		}
		public void setSaveTime(String saveTime) {
			SaveTime = saveTime;
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
		public float getSPM() {
			return SPM;
		}
		public void setSPM(float sPM) {
			SPM = sPM;
		}
		public float getAI1() {
			return AI1;
		}
		public void setAI1(float aI1) {
			AI1 = aI1;
		}
		public float getAI2() {
			return AI2;
		}
		public void setAI2(float aI2) {
			AI2 = aI2;
		}
		public float getAI3() {
			return AI3;
		}
		public void setAI3(float aI3) {
			AI3 = aI3;
		}
		public float getAI4() {
			return AI4;
		}
		public void setAI4(float aI4) {
			AI4 = aI4;
		}
		public int getGasFlowmeterCommStatus() {
			return gasFlowmeterCommStatus;
		}
		public void setGasFlowmeterCommStatus(int gasFlowmeterCommStatus) {
			this.gasFlowmeterCommStatus = gasFlowmeterCommStatus;
		}
		public float getGasInstantaneousFlow() {
			return gasInstantaneousFlow;
		}
		public void setGasInstantaneousFlow(float gasInstantaneousFlow) {
			this.gasInstantaneousFlow = gasInstantaneousFlow;
		}
		public float getGasCumulativeFlow() {
			return gasCumulativeFlow;
		}
		public void setGasCumulativeFlow(float gasCumulativeFlow) {
			this.gasCumulativeFlow = gasCumulativeFlow;
		}
		public float getGasFlowmeterPress() {
			return gasFlowmeterPress;
		}
		public void setGasFlowmeterPress(float gasFlowmeterPress) {
			this.gasFlowmeterPress = gasFlowmeterPress;
		}
		public int getLiquidFlowmeterCommStatus() {
			return liquidFlowmeterCommStatus;
		}
		public void setLiquidFlowmeterCommStatus(int liquidFlowmeterCommStatus) {
			this.liquidFlowmeterCommStatus = liquidFlowmeterCommStatus;
		}
		public float getLiquidInstantaneousFlow() {
			return liquidInstantaneousFlow;
		}
		public void setLiquidInstantaneousFlow(float liquidInstantaneousFlow) {
			this.liquidInstantaneousFlow = liquidInstantaneousFlow;
		}
		public float getLiquidCumulativeFlow() {
			return liquidCumulativeFlow;
		}
		public void setLiquidCumulativeFlow(float liquidCumulativeFlow) {
			this.liquidCumulativeFlow = liquidCumulativeFlow;
		}
		public float getLiquidFlowmeterProd() {
			return liquidFlowmeterProd;
		}
		public void setLiquidFlowmeterProd(float liquidFlowmeterProd) {
			this.liquidFlowmeterProd = liquidFlowmeterProd;
		}
		public int getFluidLevelIndicatorCommStatus() {
			return fluidLevelIndicatorCommStatus;
		}
		public void setFluidLevelIndicatorCommStatus(int fluidLevelIndicatorCommStatus) {
			this.fluidLevelIndicatorCommStatus = fluidLevelIndicatorCommStatus;
		}
		public String getFluidLevelAcquisitionTime() {
			return fluidLevelAcquisitionTime;
		}
		public void setFluidLevelAcquisitionTime(String fluidLevelAcquisitionTime) {
			this.fluidLevelAcquisitionTime = fluidLevelAcquisitionTime;
		}
		public float getFluidLevelIndicatorSoundVelocity() {
			return fluidLevelIndicatorSoundVelocity;
		}
		public void setFluidLevelIndicatorSoundVelocity(float fluidLevelIndicatorSoundVelocity) {
			this.fluidLevelIndicatorSoundVelocity = fluidLevelIndicatorSoundVelocity;
		}
		public float getFluidLevel() {
			return fluidLevel;
		}
		public void setFluidLevel(float fluidLevel) {
			this.fluidLevel = fluidLevel;
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
		public float getRunFrequency() {
			return runFrequency;
		}
		public void setRunFrequency(float runFrequency) {
			this.runFrequency = runFrequency;
		}
		public float getFrequencyChangerBusbarVoltage() {
			return frequencyChangerBusbarVoltage;
		}
		public void setFrequencyChangerBusbarVoltage(float frequencyChangerBusbarVoltage) {
			this.frequencyChangerBusbarVoltage = frequencyChangerBusbarVoltage;
		}
		public float getFrequencyChangerOutputVoltage() {
			return frequencyChangerOutputVoltage;
		}
		public void setFrequencyChangerOutputVoltage(float frequencyChangerOutputVoltage) {
			this.frequencyChangerOutputVoltage = frequencyChangerOutputVoltage;
		}
		public float getFrequencyChangerOutputCurrent() {
			return frequencyChangerOutputCurrent;
		}
		public void setFrequencyChangerOutputCurrent(float frequencyChangerOutputCurrent) {
			this.frequencyChangerOutputCurrent = frequencyChangerOutputCurrent;
		}
		public float getSetFrequencyFeedback() {
			return setFrequencyFeedback;
		}
		public void setSetFrequencyFeedback(float setFrequencyFeedback) {
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
		public int getFrequencyOrRPMControlSign() {
			return frequencyOrRPMControlSign;
		}
		public void setFrequencyOrRPMControlSign(int frequencyOrRPMControlSign) {
			this.frequencyOrRPMControlSign = frequencyOrRPMControlSign;
		}
		public float getFrequencySetValue() {
			return frequencySetValue;
		}
		public void setFrequencySetValue(float frequencySetValue) {
			this.frequencySetValue = frequencySetValue;
		}
		public float getSPMSetValue() {
			return SPMSetValue;
		}
		public void setSPMSetValue(float sPMSetValue) {
			SPMSetValue = sPMSetValue;
		}
		public float getSPMBy10Hz() {
			return SPMBy10Hz;
		}
		public void setSPMBy10Hz(float sPMBy10Hz) {
			SPMBy10Hz = sPMBy10Hz;
		}
		public float getSPMBy50Hz() {
			return SPMBy50Hz;
		}
		public void setSPMBy50Hz(float sPMBy50Hz) {
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
		public String getRunRange() {
			return RunRange;
		}
		public void setRunRange(String runRange) {
			RunRange = runRange;
		}
		public float getRunTime() {
			return RunTime;
		}
		public void setRunTime(float runTime) {
			RunTime = runTime;
		}
		public float getRunEfficiency() {
			return RunEfficiency;
		}
		public void setRunEfficiency(float runEfficiency) {
			RunEfficiency = runEfficiency;
		}
		public String getCommRange() {
			return CommRange;
		}
		public void setCommRange(String commRange) {
			CommRange = commRange;
		}
		public float getCommTime() {
			return CommTime;
		}
		public void setCommTime(float commTime) {
			CommTime = commTime;
		}
		public float getCommEfficiency() {
			return CommEfficiency;
		}
		public void setCommEfficiency(float commEfficiency) {
			CommEfficiency = commEfficiency;
		}
		public float getFluidLevelIndicatorPress() {
			return fluidLevelIndicatorPress;
		}
		public void setFluidLevelIndicatorPress(float fluidLevelIndicatorPress) {
			this.fluidLevelIndicatorPress = fluidLevelIndicatorPress;
		}
	    
	}
	
	public static class UnitData{
		public  String wellName;
		
		//煤层气井控制标志
		public int wellStartupControl=0;
		public int wellStopControl=0;
		public int frequencyOrRPMControlSignControl=0;
		public float frequencySetValueControl=0;
		public float SPMSetValueControl=0;
		public float SPMBy10HzControl=0;
		public float SPMBy50HzControl=0;
		public int RTUAddrControl=0;
		public int RTUProgramVersionControl=0;
		public int setWellNameControl=0;
		//阀组控制标志
		public int groupValveDeviceIdControl=0;
		public int groupValveBaudRateControl=0;
		public int groupValveInstrumentCombinationMode1Control=0;
		public int groupValveInstrumentCombinationMode2Control=0;
		public int groupValveInstrumentCombinationMode3Control=0;
		public int groupValveInstrumentCombinationMode4Control=0;
		
		//增压泵控制标志
		
		
		public  String driverAddr;
		public  String dirverId;
		public  int UnitId;
		public  String dirverName;
		public int commStatus;
		public  String lastDisAcquisitionTime;
		public int lastCommStatus;
		public float lastCommTime;
		public float lastCommTimeEfficiency;
		public String lastCommRange;
		public int lastRunStatus;
		public float lastRunTime;
		public float lastRunTimeEfficiency;
		public String lastRunRange;
		
		public  int acqCycle_Discrete=1000*60*2;//离散数据以及心跳读取周期,单位毫秒
		public  int saveCycle_Discrete=1000*60*5;//离散数据保存间隔,单位毫秒
		
		public int type=1;//单元类型 1-井 2-阀组 2-增压撬
		
		public int recvPackageCount=0;
		public int recvPackageSize=0;
		public int sendPackageCount=0;
		public int sendPackageSize=0;
		public String currentDate=StringManagerUtils.getCurrentTime();//判断是否跨天
		public AcquisitionData acquisitionData=null;
		public RTUDriveConfig rtuDriveConfig;
		public AcquisitionUnitData acquisitionUnitData;

		public String getWellName() {
			return wellName;
		}

		public void setWellName(String wellName) {
			this.wellName = wellName;
		}

		public int getWellStartupControl() {
			return wellStartupControl;
		}

		public void setWellStartupControl(int wellStartupControl) {
			this.wellStartupControl = wellStartupControl;
		}

		public int getWellStopControl() {
			return wellStopControl;
		}

		public void setWellStopControl(int wellStopControl) {
			this.wellStopControl = wellStopControl;
		}

		public int getFrequencyOrRPMControlSignControl() {
			return frequencyOrRPMControlSignControl;
		}

		public void setFrequencyOrRPMControlSignControl(int frequencyOrRPMControlSignControl) {
			this.frequencyOrRPMControlSignControl = frequencyOrRPMControlSignControl;
		}

		public float getFrequencySetValueControl() {
			return frequencySetValueControl;
		}

		public void setFrequencySetValueControl(float frequencySetValueControl) {
			this.frequencySetValueControl = frequencySetValueControl;
		}

		public float getSPMSetValueControl() {
			return SPMSetValueControl;
		}

		public void setSPMSetValueControl(float sPMSetValueControl) {
			SPMSetValueControl = sPMSetValueControl;
		}

		public float getSPMBy10HzControl() {
			return SPMBy10HzControl;
		}

		public void setSPMBy10HzControl(float sPMBy10HzControl) {
			SPMBy10HzControl = sPMBy10HzControl;
		}

		public float getSPMBy50HzControl() {
			return SPMBy50HzControl;
		}

		public void setSPMBy50HzControl(float sPMBy50HzControl) {
			SPMBy50HzControl = sPMBy50HzControl;
		}

		public int getRTUAddrControl() {
			return RTUAddrControl;
		}

		public void setRTUAddrControl(int rTUAddrControl) {
			RTUAddrControl = rTUAddrControl;
		}

		public int getRTUProgramVersionControl() {
			return RTUProgramVersionControl;
		}

		public void setRTUProgramVersionControl(int rTUProgramVersionControl) {
			RTUProgramVersionControl = rTUProgramVersionControl;
		}

		public int getSetWellNameControl() {
			return setWellNameControl;
		}

		public void setSetWellNameControl(int setWellNameControl) {
			this.setWellNameControl = setWellNameControl;
		}

		public String getDriverAddr() {
			return driverAddr;
		}

		public void setDriverAddr(String driverAddr) {
			this.driverAddr = driverAddr;
		}

		public String getDirverId() {
			return dirverId;
		}

		public void setDirverId(String dirverId) {
			this.dirverId = dirverId;
		}

		public String getDirverName() {
			return dirverName;
		}

		public void setDirverName(String dirverName) {
			this.dirverName = dirverName;
		}

		public int getCommStatus() {
			return commStatus;
		}

		public void setCommStatus(int commStatus) {
			this.commStatus = commStatus;
		}

		public String getLastDisAcquisitionTime() {
			return lastDisAcquisitionTime;
		}

		public void setLastDisAcquisitionTime(String lastDisAcquisitionTime) {
			this.lastDisAcquisitionTime = lastDisAcquisitionTime;
		}

		public int getLastCommStatus() {
			return lastCommStatus;
		}

		public void setLastCommStatus(int lastCommStatus) {
			this.lastCommStatus = lastCommStatus;
		}

		public float getLastCommTime() {
			return lastCommTime;
		}

		public void setLastCommTime(float lastCommTime) {
			this.lastCommTime = lastCommTime;
		}

		public float getLastCommTimeEfficiency() {
			return lastCommTimeEfficiency;
		}

		public void setLastCommTimeEfficiency(float lastCommTimeEfficiency) {
			this.lastCommTimeEfficiency = lastCommTimeEfficiency;
		}

		public String getLastCommRange() {
			return lastCommRange;
		}

		public void setLastCommRange(String lastCommRange) {
			this.lastCommRange = lastCommRange;
		}

		public int getLastRunStatus() {
			return lastRunStatus;
		}

		public void setLastRunStatus(int lastRunStatus) {
			this.lastRunStatus = lastRunStatus;
		}

		public float getLastRunTime() {
			return lastRunTime;
		}

		public void setLastRunTime(float lastRunTime) {
			this.lastRunTime = lastRunTime;
		}

		public float getLastRunTimeEfficiency() {
			return lastRunTimeEfficiency;
		}

		public void setLastRunTimeEfficiency(float lastRunTimeEfficiency) {
			this.lastRunTimeEfficiency = lastRunTimeEfficiency;
		}

		public String getLastRunRange() {
			return lastRunRange;
		}

		public void setLastRunRange(String lastRunRange) {
			this.lastRunRange = lastRunRange;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getUnitId() {
			return UnitId;
		}

		public void setUnitId(int unitId) {
			UnitId = unitId;
		}

		public int getAcqCycle_Discrete() {
			return acqCycle_Discrete;
		}

		public void setAcqCycle_Discrete(int acqCycle_Discrete) {
			this.acqCycle_Discrete = acqCycle_Discrete;
		}

		public int getSaveCycle_Discrete() {
			return saveCycle_Discrete;
		}

		public void setSaveCycle_Discrete(int saveCycle_Discrete) {
			this.saveCycle_Discrete = saveCycle_Discrete;
		}

		public int getRecvPackageCount() {
			return recvPackageCount;
		}

		public void setRecvPackageCount(int recvPackageCount) {
			this.recvPackageCount = recvPackageCount;
		}

		public int getRecvPackageSize() {
			return recvPackageSize;
		}

		public void setRecvPackageSize(int recvPackageSize) {
			this.recvPackageSize = recvPackageSize;
		}

		public int getSendPackageCount() {
			return sendPackageCount;
		}

		public void setSendPackageCount(int sendPackageCount) {
			this.sendPackageCount = sendPackageCount;
		}

		public int getSendPackageSize() {
			return sendPackageSize;
		}

		public void setSendPackageSize(int sendPackageSize) {
			this.sendPackageSize = sendPackageSize;
		}

		public String getCurrentDate() {
			return currentDate;
		}

		public void setCurrentDate(String currentDate) {
			this.currentDate = currentDate;
		}

		public AcquisitionData getAcquisitionData() {
			return acquisitionData;
		}

		public void setAcquisitionData(AcquisitionData acquisitionData) {
			this.acquisitionData = acquisitionData;
		}

		public RTUDriveConfig getRtuDriveConfig() {
			return rtuDriveConfig;
		}

		public void setRtuDriveConfig(RTUDriveConfig rtuDriveConfig) {
			this.rtuDriveConfig = rtuDriveConfig;
		}

		public AcquisitionUnitData getAcquisitionUnitData() {
			return acquisitionUnitData;
		}

		public void setAcquisitionUnitData(AcquisitionUnitData acquisitionUnitData) {
			this.acquisitionUnitData = acquisitionUnitData;
		}

		public int getGroupValveDeviceIdControl() {
			return groupValveDeviceIdControl;
		}

		public void setGroupValveDeviceIdControl(int groupValveDeviceIdControl) {
			this.groupValveDeviceIdControl = groupValveDeviceIdControl;
		}

		public int getGroupValveBaudRateControl() {
			return groupValveBaudRateControl;
		}

		public void setGroupValveBaudRateControl(int groupValveBaudRateControl) {
			this.groupValveBaudRateControl = groupValveBaudRateControl;
		}

		public int getGroupValveInstrumentCombinationMode1Control() {
			return groupValveInstrumentCombinationMode1Control;
		}

		public void setGroupValveInstrumentCombinationMode1Control(int groupValveInstrumentCombinationMode1Control) {
			this.groupValveInstrumentCombinationMode1Control = groupValveInstrumentCombinationMode1Control;
		}

		public int getGroupValveInstrumentCombinationMode2Control() {
			return groupValveInstrumentCombinationMode2Control;
		}

		public void setGroupValveInstrumentCombinationMode2Control(int groupValveInstrumentCombinationMode2Control) {
			this.groupValveInstrumentCombinationMode2Control = groupValveInstrumentCombinationMode2Control;
		}

		public int getGroupValveInstrumentCombinationMode3Control() {
			return groupValveInstrumentCombinationMode3Control;
		}

		public void setGroupValveInstrumentCombinationMode3Control(int groupValveInstrumentCombinationMode3Control) {
			this.groupValveInstrumentCombinationMode3Control = groupValveInstrumentCombinationMode3Control;
		}

		public int getGroupValveInstrumentCombinationMode4Control() {
			return groupValveInstrumentCombinationMode4Control;
		}

		public void setGroupValveInstrumentCombinationMode4Control(int groupValveInstrumentCombinationMode4Control) {
			this.groupValveInstrumentCombinationMode4Control = groupValveInstrumentCombinationMode4Control;
		}
		
	}
	
	public static class ClientUnit{
		public  List<UnitData> unitDataList=new ArrayList<UnitData>();
		public  Socket socket=null;
		public  int sign=0;//连接标志
		public  Thread thread=null;
		public  String revData="";
		public List<UnitData> getUnitDataList() {
			return unitDataList;
		}
		public void setUnitDataList(List<UnitData> unitDataList) {
			this.unitDataList = unitDataList;
		}
		public Socket getSocket() {
			return socket;
		}
		public void setSocket(Socket socket) {
			this.socket = socket;
		}
		public int getSign() {
			return sign;
		}
		public void setSign(int sign) {
			this.sign = sign;
		}
		public Thread getThread() {
			return thread;
		}
		public void setThread(Thread thread) {
			this.thread = thread;
		}
		public String getRevData() {
			return revData;
		}
		public void setRevData(String revData) {
			this.revData = revData;
		}
		
	}
}
