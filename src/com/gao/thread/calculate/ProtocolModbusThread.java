package com.gao.thread.calculate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.gao.model.calculate.CommResponseData;
import com.gao.model.calculate.ElectricCalculateResponseData;
import com.gao.model.calculate.EnergyCalculateResponseData;
import com.gao.model.calculate.TimeEffResponseData;
import com.gao.model.drive.RTUDriveConfig;
import com.gao.model.drive.RTUDriveConfig.Item;
import com.gao.tast.EquipmentDriverServerTast;
import com.gao.tast.EquipmentDriverServerTast.AcquisitionData;
import com.gao.tast.EquipmentDriverServerTast.ClientUnit;
import com.gao.tast.EquipmentDriverServerTast.UnitData;
import com.gao.utils.Config;
import com.gao.utils.OracleJdbcUtis;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProtocolModbusThread extends Thread{

	private int threadId;
	private ClientUnit clientUnit;
	private String url=Config.getInstance().configFile.getServer().getAccessPath()+"/graphicalUploadController/saveRTUAcquisitionData";
	private String tiemEffUrl=Config.getInstance().configFile.getAgileCalculate().getRun()[0];
	private String commUrl=Config.getInstance().configFile.getAgileCalculate().getCommunication()[0];
	private String energyUrl=Config.getInstance().configFile.getAgileCalculate().getEnergy()[0];
	private RTUDriveConfig driveConfig;
	private boolean isExit=false;
	public ProtocolModbusThread(int threadId, ClientUnit clientUnit,RTUDriveConfig driveConfig) {
		super();
		this.threadId = threadId;
		this.clientUnit = clientUnit;
		this.driveConfig = driveConfig;
	}
	@SuppressWarnings({"unused", "static-access" })
	public void run(){
		clientUnit.setSign(1);
        int rc=0;
        InputStream is=null;
        OutputStream os=null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer recvBuff=new StringBuffer();
        EquipmentDriverServerTast beeTechDriverServerTast=EquipmentDriverServerTast.getInstance();
        int readTimeout=1000*5;//socket read超时时间
        Gson gson = new Gson();
        while(!isExit){
        	//获取输入流，并读取客户端信息
            try {
    			byte[] recByte=new byte[256];
    			byte[] readByte=new byte[12];
    			is=clientUnit.socket.getInputStream();
    			os=clientUnit.socket.getOutputStream();
    			boolean wellReaded=false;
    			if(clientUnit.unitDataList.size()==0){//未注册过，读取注册包进行注册
    				rc=this.readSocketConnReg(clientUnit.socket, readTimeout*10, recByte,is);
    				if(rc==-1){//断开连接
        				System.out.println("第一次读取心跳失败，断开连接,释放资源");
        				this.releaseResource(is,os);
        				break;
        			}
    				String revMacStr="";
    				if("shanxiCBM".equalsIgnoreCase(driveConfig.getDriverCode())){
    					if((recByte[0]&0xFF)==0xAA&&(recByte[1]&0xFF)==0x01){
    						byte[] macByte=new byte[13];
        					for(int i=0;i<13&&i<recByte.length-3;i++){
        						macByte[i]=recByte[i+2];
        					}
        					revMacStr=new String(macByte);
    					}
    				}else {
    					if((recByte[0]&0xFF)==0xAA&&(recByte[1]&0xFF)==0x01){
    						byte[] macByte=new byte[13];
        					for(int i=0;i<13&&i<recByte.length-3;i++){
        						macByte[i]=recByte[i+2];
        					}
        					revMacStr=new String(macByte);
    					}
    				}
    				
    				if(StringManagerUtils.isNotNull(revMacStr)){//接收到注册包
    					boolean isRun=false;
						for(int j=0;j<EquipmentDriverServerTast.clientUnitList.size();j++){//遍历已连接的客户端
							if(EquipmentDriverServerTast.clientUnitList.get(j).socket!=null){//如果已连接
								for(int k=0;k<EquipmentDriverServerTast.clientUnitList.get(j).unitDataList.size();k++){
									if(revMacStr.equals(EquipmentDriverServerTast.clientUnitList.get(j).unitDataList.get(k).driverAddr)){//查询原有设备地址和新地址的连接，如存在断开资源，释放资源
										if(EquipmentDriverServerTast.clientUnitList.get(j).thread!=null){
											EquipmentDriverServerTast.clientUnitList.get(j).thread.interrupt();
											isRun=true;
											break;
										}
									}
								}
							}
							if(isRun){
								break;
							}
						}
    					
    					
    					for(int i=0;i<EquipmentDriverServerTast.units.size();i++){
    						if(revMacStr.equalsIgnoreCase(beeTechDriverServerTast.units.get(i).driverAddr)){
    							System.out.println(beeTechDriverServerTast.units.get(i).wellName+"上线");
    							clientUnit.unitDataList.add(beeTechDriverServerTast.units.get(i));
    							clientUnit.unitDataList.get(clientUnit.unitDataList.size()-1).setCommStatus(1);
    							clientUnit.unitDataList.get(clientUnit.unitDataList.size()-1).recvPackageCount+=1;
    							clientUnit.unitDataList.get(clientUnit.unitDataList.size()-1).recvPackageSize+=(64+16);
    							if(!StringManagerUtils.getCurrentTime().equals(clientUnit.unitDataList.get(clientUnit.unitDataList.size()-1).currentDate)){//如果跨天保存数据
    		    	    			saveCommLog(clientUnit.unitDataList.get(clientUnit.unitDataList.size()-1));
    							}
    						}
    					}
    					if(clientUnit.unitDataList.size()==0){//未找到匹配的井
    						System.out.println("线程"+this.threadId+"未找到匹配的井，断开连接,释放资源:"+StringManagerUtils.bytesToHexString(recByte,recByte.length)+":"+revMacStr);
            				this.releaseResource(is,os);
            				wellReaded=false;
            				break;
    					}else{
    						String AcquisitionTime=StringManagerUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
							String updateDiscreteComm="update tbl_cbm_discrete_latest t set t.commstatus=1,t.acquisitiontime=to_date('"+AcquisitionTime+"','yyyy-mm-dd hh24:mi:ss')  "
									+ " where t.wellId in (select well.id from tbl_wellinformation well where well.driveraddr='"+revMacStr+"') ";
							Connection conn=OracleJdbcUtis.getConnection();
							Statement stmt=null;
							try {
								stmt = conn.createStatement();
								int result=stmt.executeUpdate(updateDiscreteComm);
    							conn.close();
    							stmt.close();
							} catch (SQLException e) {
								try {
									conn.close();
									if(stmt!=null){
										stmt.close();
									}
								} catch (SQLException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
    					}
        			}
    				continue;
    			}else{
    				//循环读取数据 
    				String AcquisitionTime=StringManagerUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
    				for(int i=0;i<clientUnit.unitDataList.size();i++){
    					if(clientUnit.unitDataList.get(i).type==1){//抽油机
    						//先查看是否有待发控制项
    						//离散数据即时采集控制
        					if(clientUnit.unitDataList.get(i).ImmediatelyAcquisitionControl>0){
        						clientUnit.unitDataList.get(i).setImmediatelyAcquisitionControl(0);
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
    						//启井控制
        					if(clientUnit.unitDataList.get(i).wellStartupControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getWellStartup()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,5, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getWellStartup().getAddress(), clientUnit.unitDataList.get(i).wellStartupControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setWellStartupControl(0);
    							
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"停井指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取启井控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//停井控制
        					if(clientUnit.unitDataList.get(i).wellStopControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getWellStop()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,5, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getWellStop().getAddress(), clientUnit.unitDataList.get(i).wellStopControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setWellStopControl(0);
    							
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"停井指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取启停控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//频率控制
        					if(clientUnit.unitDataList.get(i).frequencySetValueControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencySetValue()!=null){
        						wellReaded=true;
        						//现将频率/冲次控制方式寄存器写入值：1
        						readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyOrRPMControlSign().getAddress(),1,driveConfig.getProtocol());
        						rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
        						if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"控制频率/冲次控制方式指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
        						rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
        						if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取控制频率/冲次控制方式返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
        						//写频率
    							readByte=this.getWriteFloatData(clientUnit.unitDataList.get(i).UnitId, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencySetValue().getAddress(), clientUnit.unitDataList.get(i).frequencySetValueControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setFrequencySetValueControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"变频频率控制指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取变频频率控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
    	    				}
        					
        					
        					//冲次控制
        					if(clientUnit.unitDataList.get(i).SPMSetValueControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMSetValue()!=null){
        						wellReaded=true;
        						//现将频率/冲次控制方式寄存器写入值：0
        						readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyOrRPMControlSign().getAddress(),0,driveConfig.getProtocol());
        						rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
        						if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"控制频率/冲次控制方式指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
        						rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
        						if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取控制频率/冲次控制方式返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
        						//写冲次
    							readByte=this.getWriteFloatData(clientUnit.unitDataList.get(i).UnitId, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMSetValue().getAddress(), clientUnit.unitDataList.get(i).SPMSetValueControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setSPMSetValueControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"冲次控制指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取冲次控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
    	    				}
        					
        					//10HZ对应冲次值设置
        					if(clientUnit.unitDataList.get(i).SPMBy10HzControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy10Hz()!=null){
        						wellReaded=true;
    							readByte=this.getWriteFloatData(clientUnit.unitDataList.get(i).UnitId, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy10Hz().getAddress(), clientUnit.unitDataList.get(i).SPMBy10HzControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setSPMBy10HzControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"10HZ对应冲次控制指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取10HZ对应冲次控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
    	    				}
        					
        					//50HZ对应冲次值设置
        					if(clientUnit.unitDataList.get(i).SPMBy50HzControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy50Hz()!=null){
        						wellReaded=true;
    							readByte=this.getWriteFloatData(clientUnit.unitDataList.get(i).UnitId, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy50Hz().getAddress(), clientUnit.unitDataList.get(i).SPMBy50HzControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setSPMBy50HzControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"50HZ对应冲次控制指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取50HZ对应冲次控制返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
    	    				}
        					
        					
        					//变频器厂家设置
        					if(clientUnit.unitDataList.get(i).vfdManufacture!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerManufacturerCode()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerManufacturerCode().getAddress(), clientUnit.unitDataList.get(i).vfdManufacture,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setRTUAddrControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"变频器厂家设置指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取变频器厂家设置返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					
        					
        					//RTU地址设置
        					if(clientUnit.unitDataList.get(i).RTUAddrControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUAddr()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUAddr().getAddress(), clientUnit.unitDataList.get(i).RTUAddrControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setRTUAddrControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"RTU地址设置指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取RTU地址设置返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//RTU程序版本号设置
        					if(clientUnit.unitDataList.get(i).RTUProgramVersionControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUProgramVersion()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUProgramVersion().getAddress(), clientUnit.unitDataList.get(i).RTUProgramVersionControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setRTUProgramVersionControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"RTU程序版本号设置指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取RTU程序版本号设置返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//RTU井号设置
        					if(clientUnit.unitDataList.get(i).setWellNameControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSetWellName()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSetWellName().getAddress(), clientUnit.unitDataList.get(i).setWellNameControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setSetWellNameControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"RTU井号设置指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取RTU井号设置返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//读取数据
        					long readTime=0;
    						if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime())){
    							readTime=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime()).getTime();
    						}
    						//当前采集时间与上次读取时间差值大于离散数据采集周期时，读取离散数据
        					if(format.parse(AcquisitionTime).getTime()-readTime>=clientUnit.unitDataList.get(i).getAcqCycle_Discrete()){
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime(AcquisitionTime);
        						int RTUStatus=0;
        						String RTUSystemTime="";
        						int runStatus=0;
        						float SPM=0;
        						float AI1=0;
        						float AI2=0;
        						float AI3=0;
        						float AI4=0;
        						
        						int gasFlowmeterCommStatus=0;
        						float gasInstantaneousFlow=0;
        						float gasCumulativeFlow=0;
        						float gasFlowmeterPress=0;
        						
        						int liquidFlowmeterCommStatus=0;
        						float liquidInstantaneousFlow=0;
        						float liquidCumulativeFlow=0;
        						float liquidFlowmeterProd=0;

        						int fluidLevelIndicatorCommStatus=0;
        						String fluidLevelAcquisitionTime="";
        						float fluidLevelIndicatorSoundVelocity=0;
        						float fluidLevel=0;
        						float fluidLevelIndicatorPress=0;
        						
        						int frequencyChangerCommStatus=0;
        						int frequencyChangerStatus=0;
        						int frequencyChangerStatus2=0;
        						float runFrequency=0;
        						float frequencyChangerBusbarVoltage=0;
        						float frequencyChangerOutputVoltage=0;
        						float frequencyChangerOutputCurrent=0;
        						float setFrequencyFeedback=0;
        						int frequencyChangerFaultCode=0;
        						int frequencyChangerPosition=0;
        						int frequencyChangerManufacturerCode=0;
        						
        						int frequencyOrRPMControlSign=0;
        						float frequencySetValue=0;
        						float SPMSetValue=0;
        						float SPMBy10Hz=0;
        						float SPMBy50Hz=0;
        						int RTUAddr=0;
        						int RTUProgramVersion=0;
        						int setWellName=0;
        						
        						clientUnit.unitDataList.get(i).getAcquisitionData().setAcquisitionTime(AcquisitionTime);
        						//一次性将100个寄存器数据读回
        						rc=sendAndReadData(is,os,readTimeout,clientUnit.unitDataList.get(i).UnitId,03,40001,100,recByte,clientUnit.unitDataList.get(i),driveConfig.getProtocol());
    							if(rc==-1||rc==-2){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取数据发送或接收失败,rc="+rc);
    								this.releaseResource(is,os);
                    				wellReaded=false;
                    				break;
    							}else if(rc==-3){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取数据异常,rc="+rc);
    								break;
    							}else{
    								//频率/冲次控制方式
    								frequencyOrRPMControlSign=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyOrRPMControlSign().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//频率设定值
    								frequencySetValue=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencySetValue().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//冲次设定值
    								SPMSetValue=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMSetValue().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//10HZ对应冲次值
    								SPMBy10Hz=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy10Hz().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//50HZ对应冲次值
    								SPMBy50Hz=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPMBy50Hz().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//气体质量流量计通信状态
    								gasFlowmeterCommStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getGasFlowmeterCommStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//气体质量流量计瞬时流量
    								gasInstantaneousFlow=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getGasInstantaneousFlow().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//气体质量流量计累积流量
    								gasCumulativeFlow=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getGasCumulativeFlow().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//气体质量流量计当前压力
    								gasFlowmeterPress=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getGasFlowmeterPress().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//量水仪通讯状态
    								liquidFlowmeterCommStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getLiquidFlowmeterCommStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//量水仪瞬时流量
    								liquidInstantaneousFlow=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getLiquidInstantaneousFlow().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//量水仪累积流量
    								liquidCumulativeFlow=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getLiquidCumulativeFlow().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//量水仪日产量
    								liquidFlowmeterProd=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getLiquidFlowmeterProd().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//液面仪通讯状态
    								fluidLevelIndicatorCommStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelIndicatorCommStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//液面仪-测试时间
    								fluidLevelAcquisitionTime=(getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())+2000)+"-"
    										+getUnsignedShort(recByte,
    	    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000)*2, 
    	    										driveConfig.getProtocol())+"-"
    	    								+getUnsignedShort(recByte,
    	    	    								(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000+1)*2, 
    	    	    								driveConfig.getProtocol())+" "
    	    	    						+getUnsignedShort(recByte,
    	    	    	    						(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000+2)*2, 
    	    	    	    						driveConfig.getProtocol())+":"
    	    	    	    				+getUnsignedShort(recByte,
    	    	    	    						(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000+3)*2, 
    	    	    	    	    				driveConfig.getProtocol())+":"
    	    	    	    	    		+getUnsignedShort(recByte,
    	    	    	    	    				(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelAcquisitionTime().getAddress()%10000+4)*2, 
    	    	    	    	    	    		driveConfig.getProtocol());
    								if(!StringManagerUtils.isValidDate(fluidLevelAcquisitionTime, "yyyy-MM-dd HH:mm:ss")){
    									fluidLevelAcquisitionTime="";
    								}
    								//液面仪音速
    								fluidLevelIndicatorSoundVelocity=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelIndicatorSoundVelocity().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelIndicatorSoundVelocity().getZoomX();
    								//液面仪-计算液面深度
    								fluidLevel=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevel().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevel().getZoomX();
    								//液面仪-套压
    								fluidLevelIndicatorPress=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelIndicatorPress().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFluidLevelIndicatorPress().getZoomX();
    								
    								//变频器通讯状态
    								frequencyChangerCommStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerCommStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//变频器状态字
    								frequencyChangerStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//变频器状态字2
    								frequencyChangerStatus2=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerStatus2().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//变频器-运行频率
    								runFrequency=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRunFrequency().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRunFrequency().getZoomX();
    								//变频器-母线电压
    								frequencyChangerBusbarVoltage=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerBusbarVoltage().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerBusbarVoltage().getZoomX();
    								//变频器-输出电压
    								frequencyChangerOutputVoltage=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerOutputVoltage().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerOutputVoltage().getZoomX();
    								//变频器-输出电流
    								frequencyChangerOutputCurrent=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerOutputCurrent().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerOutputCurrent().getZoomX();
    								//变频器-设定频率反馈
    								setFrequencyFeedback=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSetFrequencyFeedback().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSetFrequencyFeedback().getZoomX();
    								//变频器故障代码
    								frequencyChangerFaultCode=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerFaultCode().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//变频器本地旋钮位置
    								frequencyChangerPosition=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerPosition().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//变频器厂家代码
    								frequencyChangerManufacturerCode=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getFrequencyChangerManufacturerCode().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//计算冲次值
    								SPM=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSPM().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//AI1换算后实际值
    								AI1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getAI1().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//AI2换算后实际值
    								AI2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getAI2().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//AI3换算后实际值
    								AI3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getAI3().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//AI4换算后实际值
    								AI4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getAI4().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//抽油机启停状态
    								runStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRunStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//RTU状态
    								RTUStatus=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUStatus().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//RTU系统时间
    								RTUSystemTime=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol())+"-"
    										+getUnsignedShort(recByte,
    	    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000)*2, 
    	    										driveConfig.getProtocol())+"-"
    	    								+getUnsignedShort(recByte,
    	    	    								(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000+1)*2, 
    	    	    								driveConfig.getProtocol())+" "
    	    	    						+getUnsignedShort(recByte,
    	    	    	    						(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000+2)*2, 
    	    	    	    						driveConfig.getProtocol())+":"
    	    	    	    				+getUnsignedShort(recByte,
    	    	    	    						(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000+3)*2, 
    	    	    	    	    				driveConfig.getProtocol())+":"
    	    	    	    	    		+getUnsignedShort(recByte,
    	    	    	    	    				(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUSystemTime().getAddress()%10000+4)*2, 
    	    	    	    	    	    		driveConfig.getProtocol());
    								if(!StringManagerUtils.isValidDate(RTUSystemTime, "yyyy-MM-dd HH:mm:ss")){
    									RTUSystemTime="";
    								}
    								//RTU地址
    								RTUAddr=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUAddr().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//RTU程序版本号
    								RTUProgramVersion=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getRTUProgramVersion().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								//RTU设置的井号
    								setWellName=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getCMBWellDataConfig().getSetWellName().getAddress()%10000-1)*2, 
    										driveConfig.getProtocol());
    								
    								//进行通信计算
    	        					String commRequest="{"
    										+ "\"AKString\":\"\","
    										+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)&&StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastCommRange)){
    	        						commRequest+= "\"Last\":{"
    	    									+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
    	    									+ "\"CommStatus\": "+(clientUnit.unitDataList.get(i).lastCommStatus==1?true:false)+","
    	    									+ "\"CommEfficiency\": {"
    	    									+ "\"Efficiency\": "+clientUnit.unitDataList.get(i).lastCommTimeEfficiency+","
    	    									+ "\"Time\": "+clientUnit.unitDataList.get(i).lastCommTime+","
    	    									+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(clientUnit.unitDataList.get(i).lastCommRange+"")+""
    	    									+ "}"
    	    									+ "},";
    	        					}	
    	        					commRequest+= "\"Current\": {"
    										+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
    										+ "\"CommStatus\":true"
    										+ "}"
    										+ "}";
    	        					String commResponse=StringManagerUtils.sendPostMethod(commUrl, commRequest,"utf-8");
    	        					java.lang.reflect.Type type = new TypeToken<CommResponseData>() {}.getType();
    	        					CommResponseData commResponseData=gson.fromJson(commResponse, type);
    	        					if(commResponseData!=null&&commResponseData.getResultStatus()==1){
    	        						if(commResponseData.getCurrent().getCommEfficiency().getRangeString().indexOf("-;")>=0){
    	        							System.out.println("通信返回数据出现：-;");
    	        							System.out.println("通信请求数据："+commRequest);
    	        							System.out.println("通信返回数据："+commResponse);
    	        							commResponseData.getCurrent().getCommEfficiency().setRangeString(commResponseData.getCurrent().getCommEfficiency().getRangeString().replaceAll("-;", ""));
    	        						}
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommTime(commResponseData.getCurrent().getCommEfficiency().getTime());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommEfficiency(commResponseData.getCurrent().getCommEfficiency().getEfficiency());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommRange(commResponseData.getCurrent().getCommEfficiency().getRangeString());
    	        						
//    	        						clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        						clientUnit.unitDataList.get(i).lastCommStatus=commResponseData.getCurrent().getCommStatus()?1:0;
    	        						clientUnit.unitDataList.get(i).lastCommTime=commResponseData.getCurrent().getCommEfficiency().getTime();
    	        						clientUnit.unitDataList.get(i).lastCommTimeEfficiency=commResponseData.getCurrent().getCommEfficiency().getEfficiency();
    	        						clientUnit.unitDataList.get(i).lastCommRange=commResponseData.getCurrent().getCommEfficiency().getRangeString();
    	        					}else{
    	        						System.out.println("comm error");
    	        						System.out.println("通信请求数据："+commRequest);
    	    							System.out.println("通信返回数据："+commResponse);
    	        					}
    	        					
    	        					//进行时率计算
    	        					TimeEffResponseData timeEffResponseData=null;
    	        					String tiemEffRequest="{"
    										+ "\"AKString\":\"\","
    										+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)&&StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastRunRange)){
    	        						tiemEffRequest+= "\"Last\":{"
    	    									+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
    	    									+ "\"RunStatus\": "+(clientUnit.unitDataList.get(i).lastRunStatus==1?true:false)+","
    	    									+ "\"RunEfficiency\": {"
    	    									+ "\"Efficiency\": "+clientUnit.unitDataList.get(i).lastRunTimeEfficiency+","
    	    									+ "\"Time\": "+clientUnit.unitDataList.get(i).lastRunTime+","
    	    									+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(clientUnit.unitDataList.get(i).lastRunRange+"")+""
    	    									+ "}"
    	    									+ "},";
    	        					}	
    	        					tiemEffRequest+= "\"Current\": {"
    										+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
    										+ "\"RunStatus\":"+(runStatus==1?true:false)+""
    										+ "}"
    										+ "}";
    	        					String timeEffResponse=StringManagerUtils.sendPostMethod(tiemEffUrl, tiemEffRequest,"utf-8");
	            					type = new TypeToken<TimeEffResponseData>() {}.getType();
	            					timeEffResponseData=gson.fromJson(timeEffResponse, type);
    	        					if(timeEffResponseData!=null&&timeEffResponseData.getResultStatus()==1){
    	        						if(timeEffResponseData.getCurrent().getRunEfficiency().getRangeString().indexOf("-;")>=0){
    	        							System.out.println("时率返回数据出现：-;");
    	        							System.out.println("时率请求数据："+tiemEffRequest);
    	        							System.out.println("时率返回数据："+timeEffResponse);
    	        							timeEffResponseData.getCurrent().getRunEfficiency().setRangeString(timeEffResponseData.getCurrent().getRunEfficiency().getRangeString().replaceAll("-;", ""));
    	        						}
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setRunTime(timeEffResponseData.getCurrent().getRunEfficiency().getTime());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setRunEfficiency(timeEffResponseData.getCurrent().getRunEfficiency().getEfficiency());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setRunRange(timeEffResponseData.getCurrent().getRunEfficiency().getRangeString());
    	        						clientUnit.unitDataList.get(i).lastRunStatus=timeEffResponseData.getCurrent().getRunStatus()?1:0;
    	        						clientUnit.unitDataList.get(i).lastRunTime=timeEffResponseData.getCurrent().getRunEfficiency().getTime();
    	        						clientUnit.unitDataList.get(i).lastRunTimeEfficiency=timeEffResponseData.getCurrent().getRunEfficiency().getEfficiency();
    	        						clientUnit.unitDataList.get(i).lastRunRange=timeEffResponseData.getCurrent().getRunEfficiency().getRangeString();
    	        					}else{
    	        						System.out.println("run error");
    	        						System.out.println("时率请求数据："+tiemEffRequest);
    	    							System.out.println("时率返回数据："+timeEffResponse);
    	        					}
    	        					
    	        					//进行电量计算
    	        					EnergyCalculateResponseData energyCalculateResponseData=null;
    	        					String energyRequest="{"
    										+ "\"AKString\":\"\","
    										+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)){
    	        						energyRequest+= "\"Last\":{"
    	    									+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
    	    									+ "\"Total\":{"
    	    									+ "\"Watt\":"+clientUnit.unitDataList.get(i).lastGasCumulativeflow
    	    									+ "},\"Today\":{"
    	    									+ "\"Watt\":"+clientUnit.unitDataList.get(i).lastGasTodayProd
    	    									+ "}"
    	    									+ "},";
    	        					}	
    	        					energyRequest+= "\"Current\": {"
    										+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
    										+ "\"Total\":{"
    										+ "\"Watt\":"+gasCumulativeFlow
    										+ "}"
    										+ "}"
    										+ "}";
    	        					String energyResponse=StringManagerUtils.sendPostMethod(energyUrl, energyRequest,"utf-8");
    	        					type = new TypeToken<EnergyCalculateResponseData>() {}.getType();
    	        					energyCalculateResponseData=gson.fromJson(energyResponse, type);
    	        					if(energyCalculateResponseData!=null&&energyCalculateResponseData.getResultStatus()==1){
//    	        						clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        						clientUnit.unitDataList.get(i).lastGasCumulativeflow=energyCalculateResponseData.getCurrent().getTotal().getWatt();
    	        						clientUnit.unitDataList.get(i).lastGasTodayProd=energyCalculateResponseData.getCurrent().getToday().getWatt();
    	        					}else{
    	        						System.out.println("energy error");
    	        						System.out.println("请求数据："+energyRequest);
    	    							System.out.println("返回数据："+energyResponse);
    	        					}
    	        					clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        					//判断是否保存数据
    	        					long hisDataInterval=0;
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime())){
    	    							hisDataInterval=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime()).getTime();
    	    						}
    	    						if(commResponseData!=null&&commResponseData.getResultStatus()==1
    	    								&&timeEffResponseData!=null&&timeEffResponseData.getResultStatus()==1
    	    								&&energyCalculateResponseData!=null&&energyCalculateResponseData.getResultStatus()==1
    	    								&&
    	        							(runStatus!=clientUnit.unitDataList.get(i).acquisitionData.runStatus//运行状态发生改变
    	        							||format.parse(AcquisitionTime).getTime()-hisDataInterval>=clientUnit.unitDataList.get(i).getSaveCycle_Discrete()//比上次保存时间大于5分钟
    	        							)
    	        						){
    	        						//入库
        	        					Connection conn=OracleJdbcUtis.getConnection();
                						Statement stmt=null;
                						String saveDiscreteData="insert into tbl_cbm_discrete_hist("
                								+ "wellid,acquisitiontime,"
                								+ "commstatus,commtimeefficiency,commtime,commrange,"
                								+ "runstatus,runtimeefficiency,runtime,runrange,"
                								+ "rtustatus,spm,ai1,ai2,ai3,ai4,"
                								+ "gasflowmetercommstatus,gasinstantaneousflow,gascumulativeflow,gasflowmeterpress,gastodayprod,"
                								+ "liquidflowmetercommstatus,liquidinstantaneousflow,liquidcumulativeflow,liquidflowmeterprod,"
                								+ "fluidlevelindicatorcommstatus,fluidlevelacquisitiontime,soundvelocity,fluidlevel,fluidlevelindicatorpress,"
                								+ "vfdcommstatus,vfdstatus,vfdstatus2,runfrequency,"
                								+ "vfdbusbarvoltage,vfdoutputvoltage,vfdoutputcurrent,setfrequencyfeedback,"
                								+ "vfdfaultcode,vfdposition,vfdmanufacturercode,"
                								+ "frequencyorspmcontrolsign,frequencysetvalue,spmsetvalue,spmby10hz,spmby50hz,"
                								+ "rtuaddr,rtuprogramversion,setwellname)"
                								+ "select id,to_date('"+AcquisitionTime+"','yyyy-mm-dd hh24:mi:ss'),"
                								+ "1,"+commResponseData.getCurrent().getCommEfficiency().getEfficiency()+","+commResponseData.getCurrent().getCommEfficiency().getTime()+",'"+commResponseData.getCurrent().getCommEfficiency().getRangeString()+"',"
                								+ runStatus+","+timeEffResponseData.getCurrent().getRunEfficiency().getEfficiency()+","+timeEffResponseData.getCurrent().getRunEfficiency().getTime()+",'"+timeEffResponseData.getCurrent().getRunEfficiency().getRangeString()+"',"
                								+ RTUStatus+","+SPM+","+AI1+","+AI2+","+AI3+","+AI4+","
                								+ gasFlowmeterCommStatus+","+gasInstantaneousFlow+","+gasCumulativeFlow+","+gasFlowmeterPress+","+energyCalculateResponseData.getCurrent().getToday().getWatt()+","
                								+ liquidFlowmeterCommStatus+","+liquidInstantaneousFlow+","+liquidCumulativeFlow+","+liquidFlowmeterProd+","
                								+ fluidLevelIndicatorCommStatus+",to_date('"+fluidLevelAcquisitionTime+"','yyyy-mm-dd hh24:mi:ss'),"+fluidLevelIndicatorSoundVelocity+","+fluidLevel+","+fluidLevelIndicatorPress+","
                								+ frequencyChangerCommStatus+","+frequencyChangerStatus+","+frequencyChangerStatus2+","+runFrequency+","
                								+ frequencyChangerBusbarVoltage+","+frequencyChangerOutputVoltage+","+frequencyChangerOutputCurrent+","+setFrequencyFeedback+","
                								+ frequencyChangerFaultCode+","+frequencyChangerPosition+","+frequencyChangerManufacturerCode+","
                								+ frequencyOrRPMControlSign+","+frequencySetValue+","+SPMSetValue+","+SPMBy10Hz+","+SPMBy50Hz+","
                								+ RTUAddr+","+RTUProgramVersion+","+setWellName
                								+ " from tbl_wellinformation t where t.wellname='"+clientUnit.unitDataList.get(i).wellName+"'";
        	        					
                						try {
            								stmt = conn.createStatement();
            								int result=stmt.executeUpdate(saveDiscreteData);
            								conn.close();
            								stmt.close();
            								clientUnit.unitDataList.get(i).getAcquisitionData().setRunStatus(runStatus);
            								clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime(AcquisitionTime);
            							} catch (SQLException e) {
            								e.printStackTrace();
            								try {
            									conn.close();
            									if(stmt!=null){
            										stmt.close();
            									}
            								} catch (SQLException e1) {
            									e1.printStackTrace();
            								}
            							}
    	    						}
    							}
        					}
    					}else if(clientUnit.unitDataList.get(i).type==2){//阀组
    						//先查看是否有待发控制项
    						//离散数据即时采集控制
        					if(clientUnit.unitDataList.get(i).ImmediatelyAcquisitionControl>0){
        						clientUnit.unitDataList.get(i).setImmediatelyAcquisitionControl(0);
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
    						//设置设备地址
        					if(clientUnit.unitDataList.get(i).groupValveDeviceIdControl!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getDeviceId()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getDeviceId().getAddress(), clientUnit.unitDataList.get(i).groupValveDeviceIdControl,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setGroupValveDeviceIdControl(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置设备地址指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置设备地址返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					//设置A1B1口波特率
        					if(clientUnit.unitDataList.get(i).groupValveBaudRate1Control!=-99&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate()!=null){
        						wellReaded=true;
        						//先将目前的两个波特率读取回来
        						int BaudRate;
        						int BaudRate2;
        						rc=sendAndReadData(is,os,readTimeout,clientUnit.unitDataList.get(i).UnitId,03,
        								clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress(),
        								clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getLength(),
        								recByte,clientUnit.unitDataList.get(i),driveConfig.getProtocol());
    							if(rc==-1||rc==-2){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组波特率发送或接收失败,rc="+rc);
    								this.releaseResource(is,os);
                    				wellReaded=false;
                    				break;
    							}else if(rc==-3){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组波特率数据异常,rc="+rc);
    								break;
    							}else{
    								int index=0;
    								if(clientUnit.unitDataList.get(i).getRtuDriveConfig().getProtocol()==1){
    									index=10;
    								}else{
    									index=4;
    								}
    								//通讯波特率
    								BaudRate=(short) (0x0000 | (0x01 & recByte[index]));  
    								BaudRate2=(short) (0x0000 | (0x02 & recByte[index])>>1);  
    								
    							}
        						
        						int controlValue=BaudRate2*2+clientUnit.unitDataList.get(i).groupValveBaudRate1Control;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress(),controlValue,driveConfig.getProtocol());
    							
    							clientUnit.unitDataList.get(i).setGroupValveBaudRate1Control(-99);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置波特率指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置波特率返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//设置A2B2口波特率
        					if(clientUnit.unitDataList.get(i).groupValveBaudRate2Control!=-99&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate()!=null){
        						wellReaded=true;
        						//先将目前的两个波特率读取回来
        						int BaudRate;
        						int BaudRate2;
        						rc=sendAndReadData(is,os,readTimeout,clientUnit.unitDataList.get(i).UnitId,03,
        								clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress(),
        								clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getLength(),
        								recByte,clientUnit.unitDataList.get(i),driveConfig.getProtocol());
    							if(rc==-1||rc==-2){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组波特率发送或接收失败,rc="+rc);
    								this.releaseResource(is,os);
                    				wellReaded=false;
                    				break;
    							}else if(rc==-3){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组波特率数据异常,rc="+rc);
    								break;
    							}else{
    								int index=0;
    								if(clientUnit.unitDataList.get(i).getRtuDriveConfig().getProtocol()==1){
    									index=10;
    								}else{
    									index=4;
    								}
    								//通讯波特率
    								BaudRate=(short) (0x0000 | (0x01 & recByte[index]));  
    								BaudRate2=(short) (0x0000 | (0x02 & recByte[index])>>1);  
    								
    							}
        						
        						int controlValue=clientUnit.unitDataList.get(i).groupValveBaudRate2Control*2+BaudRate;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress(),controlValue,driveConfig.getProtocol());
    							
    							clientUnit.unitDataList.get(i).setGroupValveBaudRate2Control(-99);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置波特率指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置波特率返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					
        					
        					//设置仪表组合方式-1#从站
        					if(clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode1Control!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode1()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode1().getAddress(), clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode1Control,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setGroupValveInstrumentCombinationMode1Control(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置仪表组合方式-1#从站指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置仪表组合方式-1#从站返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					//设置仪表组合方式-2#从站
        					if(clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode2Control!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode2()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode2().getAddress(), clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode2Control,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setGroupValveInstrumentCombinationMode2Control(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置仪表组合方式-2#从站指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置仪表组合方式-2#从站返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					//设置仪表组合方式-3#从站
        					if(clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode3Control!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode3()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode3().getAddress(), clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode3Control,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setGroupValveInstrumentCombinationMode3Control(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置仪表组合方式-3#从站指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置仪表组合方式-3#从站返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					//设置仪表组合方式-4#从站
        					if(clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode4Control!=0&&clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode4()!=null){
        						wellReaded=true;
    							readByte=this.getWriteSingleRegisterByteData(clientUnit.unitDataList.get(i).UnitId,6, clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode4().getAddress(), clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode4Control,driveConfig.getProtocol());
    							clientUnit.unitDataList.get(i).setGroupValveInstrumentCombinationMode4Control(0);
    							rc=this.writeSocketData(clientUnit.socket, readByte,os,clientUnit.unitDataList.get(i));
    							if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",阀组:"+clientUnit.unitDataList.get(i).getWellName()+"设置仪表组合方式-4#从站指令发送失败:"+StringManagerUtils.bytesToHexString(readByte,readByte.length));
    	        					this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    							rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,clientUnit.unitDataList.get(i));
    	    					if(rc==-1){//断开连接
    	    						System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取设置仪表组合方式-4#从站返回数据读取失败，断开连接,释放资源");
    	            				this.releaseResource(is,os);
    	            				wellReaded=false;
    	            				break;
    	            			}
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
        					
        					//读取数据
        					long readTime=0;
    						if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime())){
    							readTime=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime()).getTime();
    						}
    						//当前采集时间与上次读取时间差值大于离散数据采集周期时，读取离散数据
        					if(format.parse(AcquisitionTime).getTime()-readTime>=clientUnit.unitDataList.get(i).getAcqCycle_Discrete()){
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime(AcquisitionTime);
        						
        						float SPM=0;
        						float AI1=0;
        						float AI2=0;
        						float AI3=0;
        						float AI4=0;
        						
        						float CumulativeFlow1;
        						float FlowmeterBackupPoint1;
        						float InstantaneousFlow1;
        						float FlowmeterTemperature1;
        						float FlowmeterPress1;
        						
        						float CumulativeFlow2;
        						float FlowmeterBackupPoint2;
        						float InstantaneousFlow2;
        						float FlowmeterTemperature2;
        						float FlowmeterPress2;
        						
        						float CumulativeFlow3;
        						float FlowmeterBackupPoint3;
        						float InstantaneousFlow3;
        						float FlowmeterTemperature3;
        						float FlowmeterPress3;
        						
        						float CumulativeFlow4;
        						float FlowmeterBackupPoint4;
        						float InstantaneousFlow4;
        						float FlowmeterTemperature4;
        						float FlowmeterPress4;
        						
        						int DeviceId;
        						int BaudRate;
        						int BaudRate2;
        						int InstrumentCombinationMode1;
        						int InstrumentCombinationMode2;
        						int InstrumentCombinationMode3;
        						int InstrumentCombinationMode4;
        						
        						clientUnit.unitDataList.get(i).getAcquisitionData().setAcquisitionTime(AcquisitionTime);
        						//一次性将60个寄存器数据读回
        						rc=sendAndReadData(is,os,readTimeout,clientUnit.unitDataList.get(i).UnitId,03,40101,60,recByte,clientUnit.unitDataList.get(i),driveConfig.getProtocol());
        						if(rc==-1||rc==-2){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组数据发送或接收失败,rc="+rc);
    								this.releaseResource(is,os);
                    				wellReaded=false;
                    				break;
    							}else if(rc==-3){
    								System.out.println("线程"+this.threadId+",井:"+clientUnit.unitDataList.get(i).getWellName()+"读取阀组数据异常,rc="+rc);
    								break;
    							}else{
    								//1#流量计总量
									CumulativeFlow1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getCumulativeFlow1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计备用点位
									FlowmeterBackupPoint1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterBackupPoint1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计标况瞬时流量
									InstantaneousFlow1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstantaneousFlow1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计温度
									FlowmeterTemperature1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterTemperature1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计压力
									FlowmeterPress1=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterPress1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									
									//2#流量计总量
									CumulativeFlow2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getCumulativeFlow2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计备用点位
									FlowmeterBackupPoint2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterBackupPoint2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计标况瞬时流量
									InstantaneousFlow2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstantaneousFlow2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计温度
									FlowmeterTemperature2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterTemperature2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//1#流量计压力
									FlowmeterPress2=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterPress2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									
									//3#流量计总量
									CumulativeFlow3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getCumulativeFlow3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//3#流量计备用点位
									FlowmeterBackupPoint3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterBackupPoint3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//3#流量计标况瞬时流量
									InstantaneousFlow3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstantaneousFlow3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//3#流量计温度
									FlowmeterTemperature3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterTemperature3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//3#流量计压力
									FlowmeterPress3=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterPress3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									
									//4#流量计总量
									CumulativeFlow4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getCumulativeFlow4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//4#流量计备用点位
									FlowmeterBackupPoint4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterBackupPoint4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//4#流量计标况瞬时流量
									InstantaneousFlow4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstantaneousFlow4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//4#流量计温度
									FlowmeterTemperature4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterTemperature4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//4#流量计压力
									FlowmeterPress4=getFloat(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getFlowmeterPress4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									
									//通讯地址
									DeviceId=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getDeviceId().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//通讯波特率
									BaudRate=(short) (0x0000 | (0x01 & recByte[(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress()-40101)*2+1]));  
									BaudRate2=(short) (0x0000 | (0x02 & recByte[(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getBaudRate().getAddress()-40101)*2+1])>>1);  
									
									if(BaudRate==0){
										BaudRate=9600;
									}else if(BaudRate==1){
										BaudRate=19200;
									}
									
									if(BaudRate2==0){
										BaudRate2=9600;
									}else if(BaudRate2==1){
										BaudRate2=19200;
									}
									
									//仪表组合方式-1#从站
									InstrumentCombinationMode1=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode1().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//仪表组合方式-2#从站
									InstrumentCombinationMode2=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode2().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//仪表组合方式-3#从站
									InstrumentCombinationMode3=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode3().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									//仪表组合方式-4#从站
									InstrumentCombinationMode4=getUnsignedShort(recByte,
    										(clientUnit.unitDataList.get(i).getRtuDriveConfig().getGroupValveDataConfig().getInstrumentCombinationMode4().getAddress()-40101)*2, 
    										driveConfig.getProtocol());
									
									//进行通信计算
    	        					String commRequest="{"
    										+ "\"AKString\":\"\","
    										+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)&&StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastCommRange)){
    	        						commRequest+= "\"Last\":{"
    	    									+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
    	    									+ "\"CommStatus\": "+(clientUnit.unitDataList.get(i).lastCommStatus==1?true:false)+","
    	    									+ "\"CommEfficiency\": {"
    	    									+ "\"Efficiency\": "+clientUnit.unitDataList.get(i).lastCommTimeEfficiency+","
    	    									+ "\"Time\": "+clientUnit.unitDataList.get(i).lastCommTime+","
    	    									+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(clientUnit.unitDataList.get(i).lastCommRange+"")+""
    	    									+ "}"
    	    									+ "},";
    	        					}	
    	        					commRequest+= "\"Current\": {"
    										+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
    										+ "\"CommStatus\":true"
    										+ "}"
    										+ "}";
    	        					String commResponse=StringManagerUtils.sendPostMethod(commUrl, commRequest,"utf-8");
    	        					java.lang.reflect.Type type = new TypeToken<CommResponseData>() {}.getType();
    	        					CommResponseData commResponseData=gson.fromJson(commResponse, type);
    	        					if(commResponseData!=null&&commResponseData.getResultStatus()==1){
    	        						if(commResponseData.getCurrent().getCommEfficiency().getRangeString().indexOf("-;")>=0){
    	        							System.out.println("通信返回数据出现：-;");
    	        							System.out.println("通信请求数据："+commRequest);
    	        							System.out.println("通信返回数据："+commResponse);
    	        							commResponseData.getCurrent().getCommEfficiency().setRangeString(commResponseData.getCurrent().getCommEfficiency().getRangeString().replaceAll("-;", ""));
    	        						}
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommTime(commResponseData.getCurrent().getCommEfficiency().getTime());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommEfficiency(commResponseData.getCurrent().getCommEfficiency().getEfficiency());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommRange(commResponseData.getCurrent().getCommEfficiency().getRangeString());
    	        						
//    	        						clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        						clientUnit.unitDataList.get(i).lastCommStatus=commResponseData.getCurrent().getCommStatus()?1:0;
    	        						clientUnit.unitDataList.get(i).lastCommTime=commResponseData.getCurrent().getCommEfficiency().getTime();
    	        						clientUnit.unitDataList.get(i).lastCommTimeEfficiency=commResponseData.getCurrent().getCommEfficiency().getEfficiency();
    	        						clientUnit.unitDataList.get(i).lastCommRange=commResponseData.getCurrent().getCommEfficiency().getRangeString();
    	        					}else{
    	        						System.out.println("comm error");
    	        						System.out.println("通信请求数据："+commRequest);
    	    							System.out.println("通信返回数据："+commResponse);
    	        					}
    	        					clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        					//判断是否保存数据
    	        					long hisDataInterval=0;
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime())){
    	    							hisDataInterval=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime()).getTime();
    	    						}
    	    						if(commResponseData!=null&&commResponseData.getResultStatus()==1
    	    							&&format.parse(AcquisitionTime).getTime()-hisDataInterval>=clientUnit.unitDataList.get(i).getSaveCycle_Discrete()//比上次保存时间大于5分钟
    	        						){
    	        						//入库
        	        					Connection conn=OracleJdbcUtis.getConnection();
                						Statement stmt=null;
                						String saveGroupValveDiscreteData="insert into tbl_GroupValve_discrete_hist("
                								+ "wellid,acquisitiontime,"
                								+ "commstatus,commtimeefficiency,commtime,commrange,"
                								+ "CumulativeFlow1,FlowmeterBackupPoint1,InstantaneousFlow1,FlowmeterTemperature1,FlowmeterPress1,"
                								+ "CumulativeFlow2,FlowmeterBackupPoint2,InstantaneousFlow2,FlowmeterTemperature2,FlowmeterPress2,"
                								+ "CumulativeFlow3,FlowmeterBackupPoint3,InstantaneousFlow3,FlowmeterTemperature3,FlowmeterPress3,"
                								+ "CumulativeFlow4,FlowmeterBackupPoint4,InstantaneousFlow4,FlowmeterTemperature4,FlowmeterPress4,"
                								+ "DeviceId,BaudRate,BaudRate2,"
                								+ "InstrumentCombinationMode1,InstrumentCombinationMode2,InstrumentCombinationMode3,InstrumentCombinationMode4) "
                								+ " select id,to_date('"+AcquisitionTime+"','yyyy-mm-dd hh24:mi:ss'),"
                								+ "1,"+commResponseData.getCurrent().getCommEfficiency().getEfficiency()+","+commResponseData.getCurrent().getCommEfficiency().getTime()+",'"+commResponseData.getCurrent().getCommEfficiency().getRangeString()+"',"
                								+ CumulativeFlow1+","+FlowmeterBackupPoint1+","+InstantaneousFlow1+","+FlowmeterTemperature1+","+FlowmeterPress1+","
                								+ CumulativeFlow2+","+FlowmeterBackupPoint2+","+InstantaneousFlow2+","+FlowmeterTemperature2+","+FlowmeterPress2+","
                								+ CumulativeFlow3+","+FlowmeterBackupPoint3+","+InstantaneousFlow3+","+FlowmeterTemperature3+","+FlowmeterPress3+","
                								+ CumulativeFlow4+","+FlowmeterBackupPoint4+","+InstantaneousFlow4+","+FlowmeterTemperature4+","+FlowmeterPress4+","
                								+ DeviceId+","+BaudRate+","+BaudRate2+","
                								+ InstrumentCombinationMode1+","+InstrumentCombinationMode2+","+InstrumentCombinationMode3+","+InstrumentCombinationMode4
                								+ " from tbl_wellinformation t where t.wellname='"+clientUnit.unitDataList.get(i).wellName+"' and rownum=1";
                						try {
            								stmt = conn.createStatement();
            								int result=stmt.executeUpdate(saveGroupValveDiscreteData);
            								conn.close();
            								stmt.close();
            								clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime(AcquisitionTime);
            							} catch (SQLException e) {
            								e.printStackTrace();
            								try {
            									conn.close();
            									if(stmt!=null){
            										stmt.close();
            									}
            								} catch (SQLException e1) {
            									e1.printStackTrace();
            								}
            							}
    	    						}
    	        					
    							}
        					}
    					}else if(clientUnit.unitDataList.get(i).type==3){//55kW增压泵
    						//离散数据即时采集控制
        					if(clientUnit.unitDataList.get(i).ImmediatelyAcquisitionControl>0){
        						clientUnit.unitDataList.get(i).setImmediatelyAcquisitionControl(0);
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime("");//控制指令发出后，将离散数据上一次读取时间清空，执行离散数据读取
    	    					clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime("");//控制指令发出后，将离散数据上一次保存时间清空，执行离散数据保存
        					}
    						//读取数据
        					long readTime=0;
    						if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime())){
    							readTime=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getReadTime()).getTime();
    						}
    						//当前采集时间与上次读取时间差值大于离散数据采集周期时，读取离散数据
        					if(format.parse(AcquisitionTime).getTime()-readTime>=clientUnit.unitDataList.get(i).getAcqCycle_Discrete()){
        						clientUnit.unitDataList.get(i).getAcquisitionData().setReadTime(AcquisitionTime);
        						float InletGasPressure;
        						float OutletGasPressure;
        						float SupplyOilPressure;
        						float InletGasPressureDifference;
        						float OilBranchCorePressureDifference;
        						float OilFilterPressureDifference;
        						float OilBranchTankLiquidLevel;
        						float FlammableGasConcentration;
        						float SupplyGasPressure;
        						float SupplyGasTemperature;
        						float MainMotorFrequency;
        						float MainMotorCurrent;
        						float InletGasTemperature;
        						float OutletGasTemperature;
        						float OilBranchTankTemperature;
        						float SupplyOilTemperature;
        						float LubricatingOilServiceTime;
        						float GreaseServiceTime;
        						float OilFilterServiceTime;
        						float OilBranchCoreServiceTime;
        						float InletGasFilterServiceTime;
        						float MechanicalSealServiceTime;
        						float HostTotalRunTime;
        						float HostCurrentRunTime;
        						int Status1;
        						int Status2;
        						int Status3;
        						int Status4;
        						int Status5;
        						int Status6;
        						int Status7;
        						int Status8;
        						//一次性将100个寄存器数据读回
        						rc=sendAndReadData(is,os,readTimeout,clientUnit.unitDataList.get(i).UnitId,03,40001,100,recByte,clientUnit.unitDataList.get(i),driveConfig.getProtocol());
        						if(rc==-1||rc==-2){
    								System.out.println("线程"+this.threadId+"增压泵:"+clientUnit.unitDataList.get(i).getWellName()+"读取数据发送或接收失败,rc="+rc);
    								this.releaseResource(is,os);
                    				wellReaded=false;
                    				break;
    							}else if(rc==-3){
    								System.out.println("线程"+this.threadId+",增压泵:"+clientUnit.unitDataList.get(i).getWellName()+"读取数据异常,rc="+rc);
    								break;
    							}else{
    								//进气压力
    								InletGasPressure=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressure().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressure().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressure().getZoomY();
    								//排气压力
    								OutletGasPressure=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasPressure().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasPressure().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasPressure().getZoomY();
    								//供油压力
    								SupplyOilPressure=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilPressure().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilPressure().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilPressure().getZoomY();
    								//进气压差
    								InletGasPressureDifference=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressureDifference().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressureDifference().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasPressureDifference().getZoomY();
    								//油分芯压差
    								OilBranchCorePressureDifference=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCorePressureDifference().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCorePressureDifference().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCorePressureDifference().getZoomY();
    								//油过滤器压差
    								OilFilterPressureDifference=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterPressureDifference().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterPressureDifference().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterPressureDifference().getZoomY();
    								//油分罐液位
    								OilBranchTankLiquidLevel=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankLiquidLevel().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankLiquidLevel().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankLiquidLevel().getZoomY();
    								//可燃性气体浓度
    								FlammableGasConcentration=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getFlammableGasConcentration().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getFlammableGasConcentration().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getFlammableGasConcentration().getZoomY();
    								//供气压力
    								SupplyGasPressure=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasPressure().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasPressure().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasPressure().getZoomY();
    								//供气温度
    								SupplyGasTemperature=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasTemperature().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasTemperature().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyGasTemperature().getZoomY();
    								//主电机频率
    								MainMotorFrequency=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorFrequency().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorFrequency().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorFrequency().getZoomY();
    								//主机电流
    								MainMotorCurrent=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorCurrent().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorCurrent().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMainMotorCurrent().getZoomY();
    								//进气温度
    								InletGasTemperature=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasTemperature().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasTemperature().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasTemperature().getZoomY();
    								//排气温度
    								OutletGasTemperature=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasTemperature().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasTemperature().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOutletGasTemperature().getZoomY();
    								//油分罐温度
    								OilBranchTankTemperature=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankTemperature().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankTemperature().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchTankTemperature().getZoomY();
    								//供油温度
    								SupplyOilTemperature=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilTemperature().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilTemperature().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getSupplyOilTemperature().getZoomY();
    								//润滑油使用时间
    								LubricatingOilServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getLubricatingOilServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getLubricatingOilServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getLubricatingOilServiceTime().getZoomY();
    								//润滑脂使用时间
    								GreaseServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getGreaseServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getGreaseServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getGreaseServiceTime().getZoomY();
    								//油滤使用时间
    								OilFilterServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilFilterServiceTime().getZoomY();
    								//油分芯使用时间
    								OilBranchCoreServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCoreServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCoreServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getOilBranchCoreServiceTime().getZoomY();
    								//进气过滤使用时间
    								InletGasFilterServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasFilterServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasFilterServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getInletGasFilterServiceTime().getZoomY();
    								//机械密封使用时间
    								MechanicalSealServiceTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMechanicalSealServiceTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMechanicalSealServiceTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getMechanicalSealServiceTime().getZoomY();
    								//主机累计运行时间
    								HostTotalRunTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostTotalRunTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostTotalRunTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostTotalRunTime().getZoomY();
    								//主机本次运行时间
    								HostCurrentRunTime=getUnsignedShort(recByte,(clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostCurrentRunTime().getAddress()-40001)*2,driveConfig.getProtocol())
    										*clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostCurrentRunTime().getZoomX()
    										+clientUnit.unitDataList.get(i).getRtuDriveConfig().getBoosterPumpDataConfig().getHostCurrentRunTime().getZoomY();
    								
    								//进行通信计算
    	        					String commRequest="{"
    										+ "\"AKString\":\"\","
    										+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)&&StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastCommRange)){
    	        						commRequest+= "\"Last\":{"
    	    									+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
    	    									+ "\"CommStatus\": "+(clientUnit.unitDataList.get(i).lastCommStatus==1?true:false)+","
    	    									+ "\"CommEfficiency\": {"
    	    									+ "\"Efficiency\": "+clientUnit.unitDataList.get(i).lastCommTimeEfficiency+","
    	    									+ "\"Time\": "+clientUnit.unitDataList.get(i).lastCommTime+","
    	    									+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(clientUnit.unitDataList.get(i).lastCommRange+"")+""
    	    									+ "}"
    	    									+ "},";
    	        					}	
    	        					commRequest+= "\"Current\": {"
    										+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
    										+ "\"CommStatus\":true"
    										+ "}"
    										+ "}";
    	        					String commResponse=StringManagerUtils.sendPostMethod(commUrl, commRequest,"utf-8");
    	        					java.lang.reflect.Type type = new TypeToken<CommResponseData>() {}.getType();
    	        					CommResponseData commResponseData=gson.fromJson(commResponse, type);
    	        					if(commResponseData!=null&&commResponseData.getResultStatus()==1){
    	        						if(commResponseData.getCurrent().getCommEfficiency().getRangeString().indexOf("-;")>=0){
    	        							System.out.println("通信返回数据出现：-;");
    	        							System.out.println("通信请求数据："+commRequest);
    	        							System.out.println("通信返回数据："+commResponse);
    	        							commResponseData.getCurrent().getCommEfficiency().setRangeString(commResponseData.getCurrent().getCommEfficiency().getRangeString().replaceAll("-;", ""));
    	        						}
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommTime(commResponseData.getCurrent().getCommEfficiency().getTime());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommEfficiency(commResponseData.getCurrent().getCommEfficiency().getEfficiency());
    	        						clientUnit.unitDataList.get(i).getAcquisitionData().setCommRange(commResponseData.getCurrent().getCommEfficiency().getRangeString());
    	        						
//    	        						clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        						clientUnit.unitDataList.get(i).lastCommStatus=commResponseData.getCurrent().getCommStatus()?1:0;
    	        						clientUnit.unitDataList.get(i).lastCommTime=commResponseData.getCurrent().getCommEfficiency().getTime();
    	        						clientUnit.unitDataList.get(i).lastCommTimeEfficiency=commResponseData.getCurrent().getCommEfficiency().getEfficiency();
    	        						clientUnit.unitDataList.get(i).lastCommRange=commResponseData.getCurrent().getCommEfficiency().getRangeString();
    	        					}else{
    	        						System.out.println("comm error");
    	        						System.out.println("通信请求数据："+commRequest);
    	    							System.out.println("通信返回数据："+commResponse);
    	        					}
    	        					clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
    	        					//判断是否保存数据
    	        					long hisDataInterval=0;
    	        					if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime())){
    	    							hisDataInterval=format.parse(clientUnit.unitDataList.get(i).getAcquisitionData().getSaveTime()).getTime();
    	    						}
    	    						if(commResponseData!=null&&commResponseData.getResultStatus()==1
    	    							&&format.parse(AcquisitionTime).getTime()-hisDataInterval>=clientUnit.unitDataList.get(i).getSaveCycle_Discrete()//比上次保存时间大于5分钟
    	        						){
    	        						//入库
        	        					Connection conn=OracleJdbcUtis.getConnection();
                						Statement stmt=null;
                						String saveBoosterPumpDiscreteData="insert into tbl_bp_discrete_hist("
                								+ "wellid,acquisitiontime,"
                								+ "commstatus,commtimeefficiency,commtime,commrange,"
                								+ "InletGasPressure,OutletGasPressure,SupplyOilPressure,InletGasPressureDifference,OilBranchCorePressDifference,"
                								+ "OilFilterPressureDifference,OilBranchTankLiquidLevel,FlammableGasConcentration,SupplyGasPressure,SupplyGasTemperature,"
                								+ "MainMotorFrequency,MainMotorCurrent,InletGasTemperature,OutletGasTemperature,OilBranchTankTemperature,"
                								+ "SupplyOilTemperature,LubricatingOilServiceTime,GreaseServiceTime,OilFilterServiceTime,OilBranchCoreServiceTime,"
                								+ "InletGasFilterServiceTime,MechanicalSealServiceTime,HostTotalRunTime,HostCurrentRunTime) "
                								+ "select id,to_date('"+AcquisitionTime+"','yyyy-mm-dd hh24:mi:ss'),"
                								+ "1,"+commResponseData.getCurrent().getCommEfficiency().getEfficiency()+","+commResponseData.getCurrent().getCommEfficiency().getTime()+",'"+commResponseData.getCurrent().getCommEfficiency().getRangeString()+"',"
                								+ InletGasPressure+","+OutletGasPressure+","+SupplyOilPressure+","+InletGasPressureDifference+","+OilBranchCorePressureDifference+","
                								+ OilFilterPressureDifference+","+OilBranchTankLiquidLevel+","+FlammableGasConcentration+","+SupplyGasPressure+","+SupplyGasTemperature+","
                								+ MainMotorFrequency+","+MainMotorCurrent+","+InletGasTemperature+","+OutletGasTemperature+","+OilBranchTankTemperature+","
                								+ SupplyOilTemperature+","+LubricatingOilServiceTime+","+GreaseServiceTime+","+OilFilterServiceTime+","+OilBranchCoreServiceTime+","
                								+ InletGasFilterServiceTime+","+MechanicalSealServiceTime+","+HostTotalRunTime+","+HostCurrentRunTime
                								+ " from tbl_wellinformation t where t.wellname='"+clientUnit.unitDataList.get(i).wellName+"'";
                						try {
            								stmt = conn.createStatement();
            								int result=stmt.executeUpdate(saveBoosterPumpDiscreteData);
            								conn.close();
            								stmt.close();
            								clientUnit.unitDataList.get(i).getAcquisitionData().setSaveTime(AcquisitionTime);
            							} catch (SQLException e) {
            								e.printStackTrace();
            								try {
            									conn.close();
            									if(stmt!=null){
            										stmt.close();
            									}
            								} catch (SQLException e1) {
            									e1.printStackTrace();
            								}
            							}
    	    						}
    							}
        					}
    					}
    				}
    			}
    			if(this.interrupted()){
            		throw new InterruptedException();
            	}else{
            		Thread.sleep(1000);
            	}
    		} catch (Exception e) {
    			e.printStackTrace();
				this.releaseResource(is,os);
				break;
    		} 
            
        }
	}
	
	@SuppressWarnings("static-access")
	public  void releaseResource(InputStream is,OutputStream os){
		
		try {
			isExit=true;
			Connection conn=OracleJdbcUtis.getConnection();
			Statement stmt=null;
			stmt = conn.createStatement();
			Gson gson = new Gson();
			String AcquisitionTime=StringManagerUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
			
			for(int i=0;i<this.clientUnit.unitDataList.size();i++){
				clientUnit.unitDataList.get(i).acquisitionData=new  AcquisitionData();
				clientUnit.unitDataList.get(i).commStatus=0;
				
				clientUnit.unitDataList.get(i).wellStartupControl=0;
				clientUnit.unitDataList.get(i).wellStopControl=0;
				clientUnit.unitDataList.get(i).frequencyOrRPMControlSignControl=0;
				
				clientUnit.unitDataList.get(i).frequencySetValueControl=0;
				clientUnit.unitDataList.get(i).SPMSetValueControl=0;
				clientUnit.unitDataList.get(i).SPMBy10HzControl=0;
				clientUnit.unitDataList.get(i).SPMBy50HzControl=0;
				clientUnit.unitDataList.get(i).RTUAddrControl=0;
				clientUnit.unitDataList.get(i).RTUProgramVersionControl=0;
				clientUnit.unitDataList.get(i).setWellNameControl=0;
				
				//阀组控制标志
				clientUnit.unitDataList.get(i).groupValveDeviceIdControl=0;
				clientUnit.unitDataList.get(i).groupValveBaudRate1Control=-99;
				clientUnit.unitDataList.get(i).groupValveBaudRate2Control=-99;
				clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode1Control=0;
				clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode2Control=0;
				clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode3Control=0;
				clientUnit.unitDataList.get(i).groupValveInstrumentCombinationMode4Control=0;
				
				clientUnit.unitDataList.get(i).acquisitionData.runStatus=0;
				//进行通信计算
				String commRequest="{"
						+ "\"AKString\":\"\","
						+ "\"WellName\":\""+clientUnit.unitDataList.get(i).getWellName()+"\",";
				if(StringManagerUtils.isNotNull(clientUnit.unitDataList.get(i).lastDisAcquisitionTime)){
					commRequest+= "\"Last\":{"
							+ "\"AcquisitionTime\": \""+clientUnit.unitDataList.get(i).lastDisAcquisitionTime+"\","
							+ "\"CommStatus\": "+(clientUnit.unitDataList.get(i).lastCommStatus==1?true:false)+","
							+ "\"CommEfficiency\": {"
							+ "\"Efficiency\": "+clientUnit.unitDataList.get(i).lastCommTimeEfficiency+","
							+ "\"Time\": "+clientUnit.unitDataList.get(i).lastCommTime+","
							+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(clientUnit.unitDataList.get(i).lastCommRange+"")+""
							+ "}"
							+ "},";
				}	
				commRequest+= "\"Current\": {"
						+ "\"AcquisitionTime\":\""+AcquisitionTime+"\","
						+ "\"CommStatus\":false"
						+ "}"
						+ "}";
				String commResponse=StringManagerUtils.sendPostMethod(commUrl, commRequest,"utf-8");
				java.lang.reflect.Type type = new TypeToken<CommResponseData>() {}.getType();
				CommResponseData commResponseData=gson.fromJson(commResponse, type);
				
				String tableName="";
				if(clientUnit.unitDataList.get(i).type==1){//抽油机
					tableName="tbl_cbm_discrete_latest";
				}else if(clientUnit.unitDataList.get(i).type==2){//阀组
					tableName="tbl_GroupValve_discrete_latest";
				}else if(clientUnit.unitDataList.get(i).type==3){//55kW增压泵
					tableName="tbl_bp_discrete_hist";
				}
				String updateCommStatus="update "+tableName+" t set t.commStatus=0,t.acquisitionTime=to_date('"+AcquisitionTime+"','yyyy-mm-dd hh24:mi:ss') ";
				if(commResponseData!=null&&commResponseData.getResultStatus()==1){
					updateCommStatus+=" ,t.commTimeEfficiency= "+commResponseData.getCurrent().getCommEfficiency().getEfficiency()
							+ " ,t.commTime= "+commResponseData.getCurrent().getCommEfficiency().getTime()
							+ " ,t.commRange= '"+commResponseData.getCurrent().getCommEfficiency().getRangeString()+"'";
					
					clientUnit.unitDataList.get(i).lastDisAcquisitionTime=AcquisitionTime;
					clientUnit.unitDataList.get(i).lastCommStatus=commResponseData.getCurrent().getCommStatus()?1:0;
					clientUnit.unitDataList.get(i).lastCommTime=commResponseData.getCurrent().getCommEfficiency().getTime();
					clientUnit.unitDataList.get(i).lastCommTimeEfficiency=commResponseData.getCurrent().getCommEfficiency().getEfficiency();
					clientUnit.unitDataList.get(i).lastCommRange=commResponseData.getCurrent().getCommEfficiency().getRangeString();
				}
				updateCommStatus+=" where t.wellId= (select t2.id from tbl_wellinformation t2 where t2.wellName='"+clientUnit.unitDataList.get(i).wellName+"') ";
				int result=stmt.executeUpdate(updateCommStatus);
			}
			conn.close();
			if(stmt!=null){
				stmt.close();
			}
			if(is!=null)
				is.close();
			if(os!=null)
				os.close();
			if(clientUnit.socket!=null)
				clientUnit.socket.close();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} finally{
			EquipmentDriverServerTast beeTechDriverServerTast=EquipmentDriverServerTast.getInstance();
			if(beeTechDriverServerTast.clientUnitList.size()>threadId){
				beeTechDriverServerTast.clientUnitList.remove(threadId);
				clientUnit=null;
				clientUnit=new ClientUnit();
				beeTechDriverServerTast.clientUnitList.add(threadId,clientUnit);
			}
		}
		
	}
	
	public byte[] getSendByteData(int id,int gnm,int startAddr,int length,int protocol){
		byte startAddrArr[]=StringManagerUtils.getByteArray((short)(startAddr-40001));
		byte lengthArr[]=StringManagerUtils.getByteArray((short)length);
		byte[] readByte=null;
		if(protocol==1){//modubus-tcp
			readByte=new byte[12];
			readByte[0]=startAddrArr[0];
			readByte[1]=startAddrArr[1];
			readByte[2]=0x00;
			readByte[3]=0x00;
			readByte[4]=0x00;
			readByte[5]=0x06;
			readByte[6]=(byte)id;
			readByte[7]=(byte)gnm;
			readByte[8]=startAddrArr[0];
			readByte[9]=startAddrArr[1];
			readByte[10]=lengthArr[0];
			readByte[11]=lengthArr[1];
		}else if(protocol==2){//modubus-rtu
			byte[] dataByte=new byte[6];
			dataByte[0]=(byte)id;
			dataByte[1]=(byte)gnm;
			dataByte[2]=startAddrArr[0];
			dataByte[3]=startAddrArr[1];
			dataByte[4]=lengthArr[0];
			dataByte[5]=lengthArr[1];
			byte[] CRC16=StringManagerUtils.getCRC16(dataByte);
			readByte=StringManagerUtils.linlByteArray(dataByte,CRC16);
		}
		return readByte;
		
		
	}
	
	public byte[] getWriteSingleRegisterByteData(int id,int gnm,int startAddr,int data,int protocol){
		byte startAddrArr[]=null;
		byte dataArr[]=null;
		if (gnm==6){//写单个保持寄存器
			startAddrArr=StringManagerUtils.getByteArray((short)(startAddr-40001));
			dataArr=StringManagerUtils.getByteArray((short)data);
		}else if(gnm==5){//写单个线圈
			startAddrArr=StringManagerUtils.getByteArray((short)(startAddr-00001));
			if(data==1){//线圈置位
				dataArr=new byte[]{(byte) 0xFF,0x00};
			}else{
				dataArr=new byte[]{0x00,0x00};
			}
		}
		
		byte[] readByte=null;
		if(protocol==1){
			readByte=new byte[12];
			readByte[0]=startAddrArr[0];
			readByte[1]=startAddrArr[1];
			readByte[2]=0x00;
			readByte[3]=0x00;
			readByte[4]=0x00;
			readByte[5]=0x06;
			readByte[6]=(byte)id;
			readByte[7]=(byte)gnm;
			readByte[8]=startAddrArr[0];
			readByte[9]=startAddrArr[1];
			readByte[10]=dataArr[0];
			readByte[11]=dataArr[1];
		}else if(protocol==2){
			byte[] dataByte=new byte[6];
			dataByte[0]=(byte)id;
			dataByte[1]=(byte)gnm;
			dataByte[2]=startAddrArr[0];
			dataByte[3]=startAddrArr[1];
			dataByte[4]=dataArr[0];
			dataByte[5]=dataArr[1];
			byte[] CRC16=StringManagerUtils.getCRC16(dataByte);
			readByte=StringManagerUtils.linlByteArray(dataByte,CRC16);
		}
		return readByte;
	}
	
	public byte[] getWriteFloatData(int id,int startAddr,float data,int protocol){
		byte startAddrArr[]=StringManagerUtils.getByteArray((short)(startAddr-40001));
		byte dataArr[]=StringManagerUtils.getByteArray(data);
		byte[] readByte=null;
		if(protocol==1){
			readByte=new byte[17];
			readByte[0]=startAddrArr[0];
			readByte[1]=startAddrArr[1];
			readByte[2]=0x00;
			readByte[3]=0x00;
			readByte[4]=0x00;
			readByte[5]=0x0B;
			readByte[6]=(byte)id;
			readByte[7]=0x10;
			readByte[8]=startAddrArr[0];
			readByte[9]=startAddrArr[1];
			readByte[10]=0x00;
			readByte[11]=0x02;
			readByte[12]=0x04;
			readByte[13]=dataArr[0];
			readByte[14]=dataArr[1];
			readByte[15]=dataArr[2];
			readByte[16]=dataArr[3];
		}else if(protocol==2){
			byte[] dataByte=new byte[11];
			dataByte[0]=(byte)id;
			dataByte[1]=0x10;
			dataByte[2]=startAddrArr[0];
			dataByte[3]=startAddrArr[1];
			dataByte[4]=0x00;
			dataByte[5]=0x02;
			dataByte[6]=0x04;
			dataByte[7]=dataArr[0];
			dataByte[8]=dataArr[1];
			dataByte[9]=dataArr[2];
			dataByte[10]=dataArr[3];
			byte[] CRC16=StringManagerUtils.getCRC16(dataByte);
			readByte=StringManagerUtils.linlByteArray(dataByte,CRC16);
		}
		return readByte;
	}
	
	//socket 读取数据
    public int readSocketData(Socket socket,int timeout,byte[] recByte,InputStream is,UnitData unit){
    	int rc=0;
    	int i=0;
    	do{
    		try {
    			socket.setSoTimeout(timeout);
    	    	rc=is.read(recByte);
    	    	while(rc!=-1&&(recByte[0]&0xFF)==0xAA&&(recByte[1]&0xFF)==0x01){
    	    		unit.recvPackageCount+=1;
    	    		unit.recvPackageSize+=(64+rc);
    	    		if(!StringManagerUtils.getCurrentTime().equals(unit.currentDate)){//如果跨天保存数据
    	    			saveCommLog(unit);
					}
    				rc=is.read(recByte);
    			}
    	    	unit.recvPackageCount+=1;
	    		unit.recvPackageSize+=(64+rc);
	    		if(!StringManagerUtils.getCurrentTime().equals(unit.currentDate)){//如果跨天保存数据
	    			saveCommLog(unit);
				}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			rc=-1;
    		}
    		i++;
    	}while(rc<=0&&i<2);
    	
    	return rc;
    }
    
    //读取心跳
    public int readSocketConnReg(Socket socket,int timeout,byte[] recByte,InputStream is){
    	int rc=0;
    	try {
			socket.setSoTimeout(timeout);
	    	rc=is.read(recByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
    	return rc;
    }
    
    public int sendAndReadData(InputStream is,OutputStream os,int readTimeout,int id,int gnm,int startAddr,int lengthOrData,byte[] recByte,UnitData unit,int protocol){
		byte[] readByte=new byte[12];
		readByte=this.getSendByteData(id, gnm, startAddr, lengthOrData,protocol);
		int rc=this.writeSocketData(clientUnit.socket, readByte,os,unit);
		if(rc==-1){//断开连接
			return -1;//发送数据失败
		}
		rc=this.readSocketData(clientUnit.socket, readTimeout, recByte,is,unit);
		if(rc==-1){//断开连接
			return -2;//读取数据失败
		}
		if(recByte[2]==0x83){//读取异常
			return -3;//读取异常
		}
		return rc;
    }
    
    //socket写数据
    public int writeSocketData(Socket socket,byte[] readByte,OutputStream os,UnitData unit){
    	int rc=0;
    	int i=0;
    	do{
    		try {
        		os.write(readByte);
    			os.flush();
    			rc=1;
    			unit.sendPackageCount+=1;
    			unit.sendPackageSize+=readByte.length;
    			if(!StringManagerUtils.getCurrentTime().equals(unit.currentDate)){//如果跨天保存数据
	    			saveCommLog(unit);
				}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			rc=-1;
    		}
    		i++;
    	}while(rc!=1&&i<5);
    	
    	return rc;
    }
    
    public short getShort(byte[] arr,int index, int protocol) {  
    	short result=0;
    	if(protocol==1){
    		result=StringManagerUtils.getShort(arr, 9+index);
    	}else if(protocol==2){
    		result=StringManagerUtils.getShort(arr, 3+index);
    	}
        return result;
    } 
    
    public int getUnsignedShort(byte[] arr,int index, int protocol) {  
    	int result=0;
    	if(protocol==1){
    		result=StringManagerUtils.getUnsignedShort(arr, 9+index);
    	}else if(protocol==2){
    		result=StringManagerUtils.getUnsignedShort(arr, 3+index);
    	}
        return result;
    }
    
    public float getFloat(byte[] arr,int index, int protocol) {  
    	float result=0;
    	if(protocol==1){
    		result=StringManagerUtils.getFloat(arr, 9+index);
    	}else if(protocol==2){
    		result=StringManagerUtils.getFloat(arr, 3+index);
    	}
        return result;
    }  
    
    public String BCD2TimeString(byte[] arr, int protocol) {
        String result="";
        if(protocol==1){
    		result=StringManagerUtils.BCD2TimeString(arr, 9);
    	}else if(protocol==2){
    		result=StringManagerUtils.BCD2TimeString(arr, 3);
    	}
        return result;
    }
    
    public boolean saveCommLog(UnitData unit){
//    	Connection conn=OracleJdbcUtis.getConnection();
//		CallableStatement cs=null;
//		try {
//			cs = conn.prepareCall("{call PRO_SAVECOMMLOG(?,?,?,?,?,?)}");
//			cs.setString(1, unit.wellName);
//			cs.setString(2, unit.currentDate);
//			cs.setInt(3, unit.recvPackageCount);
//			cs.setInt(4, unit.recvPackageSize);
//			cs.setInt(5, unit.sendPackageCount);
//			cs.setInt(6, unit.sendPackageSize);
//			cs.executeUpdate();
//		} catch (SQLException e) {
//			try {
//				conn.close();
//				if(cs!=null){
//					cs.close();
//				}
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//			}
//			e.printStackTrace();
//			return false;
//		}finally{
//			unit.setRecvPackageCount(0);
//			unit.setRecvPackageSize(0);
//			unit.setSendPackageCount(0);
//			unit.setSendPackageSize(0);
//			unit.setCurrentDate(StringManagerUtils.getCurrentTime());
//			try {
//				if(cs!=null){
//					cs.close();
//				}
//				conn.close();
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//			}
//		}
    	return true;
    }


	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public ClientUnit getClientUnit() {
		return clientUnit;
	}

	public void setClientUnit(ClientUnit clientUnit) {
		this.clientUnit = clientUnit;
	}


	public RTUDriveConfig getDriveConfig() {
		return driveConfig;
	}


	public void setDriveConfig(RTUDriveConfig driveConfig) {
		this.driveConfig = driveConfig;
	}
	public boolean isExit() {
		return isExit;
	}
	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}
	
}
