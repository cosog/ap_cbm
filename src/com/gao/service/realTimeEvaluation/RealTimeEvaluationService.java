package com.gao.service.realTimeEvaluation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gao.dao.BaseDao;
import com.gao.model.data.DataDictionary;
import com.gao.service.base.BaseService;
import com.gao.service.base.CommonDataService;
import com.gao.service.data.DataitemsInfoService;
import com.gao.utils.Page;
import com.gao.utils.StringManagerUtils;

@Component("realTimeEvaluationService")
public class RealTimeEvaluationService<T> extends BaseService<T> {
	private BaseDao dao;
	@Autowired
	private CommonDataService service;
	@Autowired
	private DataitemsInfoService dataitemsInfoService;
	
	public String statisticsData(String orgId,String type,String wellType){
		StringBuffer result_json = new StringBuffer();
		String sql="";
		String statType="runStatusName";
		if("1".equalsIgnoreCase(type)){
			statType="runStatusName";
		}else if("2".equalsIgnoreCase(type)){
			statType="runtimeEfficiencyLevel";
		}else if("3".equalsIgnoreCase(type)){
			statType="commStatusName";
		}else if("4".equalsIgnoreCase(type)){
			statType="commtimeefficiencyLevel";
		}
		sql="select "+statType+",count(1) from viw_cbm_discrete_latest t where  t.org_id in("+orgId+")";
		
		sql+=" group by "+statType;
		
		List<?> list = this.findCallSql(sql);
		result_json.append("{ \"success\":true,");
		result_json.append("\"List\":[");
		for(int i=0;i<list.size();i++){
			Object[] obj=(Object[]) list.get(i);
			if(StringManagerUtils.isNotNull(obj[0]+"")){
//				result_json.append("{\"id\":"+obj[0]+",");
				result_json.append("{\"item\":\""+obj[0]+"\",");
				result_json.append("\"count\":"+obj[1]+"},");
			}
		}
		if(result_json.toString().endsWith(",")){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]}");
		return result_json.toString();
	}
	
	public String getRealtimeAnalysisWellList(String orgId, String wellName, Page pager,String type,String unitType,String startDate,String endDate,String statValue)
			throws Exception {
		DataDictionary ddic = null;
		String columns= "";
		String sql="";
		String sqlHis="";
		String finalSql="";
		String sqlAll="";
		String ddicName="";
		String tableName_latest="viw_cbm_discrete_latest";
		String tableName_hist="viw_cbm_discrete_hist";
		String typeColumnName="runStatusName";
		
		if("1".equalsIgnoreCase(type)){
			if("1".equals(unitType)){//煤层气井
				ddicName="CBMWellRealtimeRunStatus";
			}
			typeColumnName="runStatusName";
		}else if("2".equalsIgnoreCase(type)){
			if("1".equals(unitType)){//煤层气井
				ddicName="CBMWellRealtimeRunStatus";
			}
			typeColumnName="runtimeEfficiencyLevel";
		}else if("3".equalsIgnoreCase(type)){
			if("1".equals(unitType)){//煤层气井
				ddicName="CBMWellRealtimeRunStatus";
			}
			typeColumnName="commStatusName";
		}else if("4".equalsIgnoreCase(type)){
			if("1".equals(unitType)){//煤层气井
				ddicName="CBMWellRealtimeRunStatus";
			}
			typeColumnName="commtimeefficiencyLevel";
		}
		
		ddic  = dataitemsInfoService.findTableSqlWhereByListFaceId(ddicName);
		
		columns = ddic.getTableHeader();
		
		if("1".equals(unitType)){//螺杆泵井
			tableName_latest="viw_cbm_discrete_latest";
			tableName_hist="viw_cbm_discrete_hist";
			sql=ddic.getSql()+",commStatus,runStatus,commAlarmLevel,runAlarmLevel ";
			sqlHis=ddic.getSql()+",commStatus,runStatus,commAlarmLevel,runAlarmLevel ";
		}
		
		
		sql+= " from "+tableName_latest+" t where t.org_id in("+orgId+")";
		sqlHis+= " from "+tableName_hist+" t where t.org_id in("+orgId+")";
		
		
		if(StringManagerUtils.isNotNull(statValue)){
			sql+=" and "+typeColumnName+"='"+statValue+"' ";
		}
		sql+=" order by t.sortNum, t.wellName";
		sqlHis+=" and to_date(to_char(t.acquisitionTime,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ "and  t.wellName = '" + wellName.trim() + "' order by t.acquisitionTime desc";
		
		if(StringManagerUtils.isNotNull(wellName.trim())){
			sqlAll=sqlHis;
		}else{
			sqlAll=sql;
		}
		
		int maxvalue=pager.getLimit()+pager.getStart();
		finalSql="select * from   ( select a.*,rownum as rn from ("+sqlAll+" ) a where  rownum <="+maxvalue+") b where rn >"+pager.getStart();
		String getResult = this.findCustomPageBySqlEntity(sqlAll,finalSql, columns, 20 + "", pager);
		return getResult;
	}
	
	public String getCBMWellAnalysisAndAcqAndControlData(String recordId,String wellName,String selectedWellName,int userId)throws Exception {
		StringBuffer result_json = new StringBuffer();
		String tableName="viw_cbm_discrete_latest";
		if(StringManagerUtils.isNotNull(wellName)){
			tableName="viw_cbm_discrete_hist";
		}
		String isControlSql="select t2.role_flag from tbl_user t,tbl_role t2 where t.user_type=t2.role_id and t.user_no="+userId;
		
		String sql="select to_char(acquisitionTime,'yyyy-mm-dd hh24:mi:ss'),"
				+ " commStatus,commStatusName,runStatus,runStatusName,"
				+ " gasFlowmeterCommStatus,gasFlowmeterCommName,gasInstantaneousFlow,gasCumulativeFlow,gasTodayProd,gasFlowmeterPress,"
				+ " liquidFlowmeterCommStatus,liquidFlowmeterCommName,liquidInstantaneousflow,liquidCumulativeflow,liquidFlowmeterProd,"
				+ " fluidLevelIndicatorCommStatus,fluidLevelIndicatorCommName,to_char(fluidLevelAcquisitionTime,'yyyy-mm-dd hh24:mi:ss'),soundVelocity,fluidLevel,fluidLevelIndicatorPress,"
				+ " vfdCommStatus,vfdCommName,vfdStatus,vfdStatusName,vfdStatus2,vfdStatus2Name,"
				+ " runFrequency,SPM,vfdBusbarVoltage,vfdOutputVoltage,vfdOutputCurrent,setFrequencyFeedback,"
				+ " vfdFaultCode,vfdPosition,vfdPositionName,vfdManufacturerCode,vfdManufacturerName,"
				+ " frequencyOrSPMcontrolSign, frequencyOrSPMcontrol,"
				+ " frequencySetValue, SPMSetValue, SPMBy10hz, SPMBy50hz,"
				+ " rtuAddr,rtuProgramVersion,setWellname"
				+ " from "+tableName+" t where id="+recordId;
		List<?> isControlList = this.findCallSql(isControlSql);
		List<?> list = this.findCallSql(sql);
		String isControl=isControlList.size()>0?isControlList.get(0).toString():"0";
		result_json.append("{ \"success\":true,\"isControl\":"+isControl);
		if(list.size()>0){
			Object[] obj=(Object[]) list.get(0);
			result_json.append(",\"acquisitionTime\":\""+obj[0]+"\",");
			result_json.append("\"commStatus\":\""+obj[1]+"\",");
			result_json.append("\"commStatusName\":\""+obj[2]+"\",");
			result_json.append("\"runStatus\":\""+obj[3]+"\",");
			result_json.append("\"runStatusName\":\""+obj[4]+"\",");
			
			result_json.append("\"gasFlowmeterCommStatus\":\""+obj[5]+"\",");
			result_json.append("\"gasFlowmeterCommName\":\""+obj[6]+"\",");
			result_json.append("\"gasInstantaneousFlow\":\""+obj[7]+"\",");
			result_json.append("\"gasCumulativeFlow\":\""+obj[8]+"\",");
			result_json.append("\"gasTodayProd\":\""+obj[9]+"\",");
			result_json.append("\"gasFlowmeterPress\":\""+obj[10]+"\",");
			
			result_json.append("\"liquidFlowmeterCommStatus\":\""+obj[11]+"\",");
			result_json.append("\"liquidFlowmeterCommName\":\""+obj[12]+"\",");
			result_json.append("\"liquidInstantaneousflow\":\""+obj[13]+"\",");
			result_json.append("\"liquidCumulativeflow\":\""+obj[14]+"\",");
			result_json.append("\"liquidFlowmeterProd\":\""+obj[15]+"\",");
			
			result_json.append("\"fluidLevelIndicatorCommStatus\":\""+obj[16]+"\",");
			result_json.append("\"fluidLevelIndicatorCommName\":\""+obj[17]+"\",");
			result_json.append("\"fluidLevelAcquisitionTime\":\""+obj[18]+"\",");
			result_json.append("\"soundVelocity\":\""+obj[19]+"\",");
			result_json.append("\"fluidLevel\":\""+obj[20]+"\",");
			result_json.append("\"fluidLevelIndicatorPress\":\""+obj[21]+"\",");
			
			result_json.append("\"vfdCommStatus\":\""+obj[22]+"\",");
			result_json.append("\"vfdCommName\":\""+obj[23]+"\",");
			result_json.append("\"vfdStatus\":\""+obj[24]+"\",");
			result_json.append("\"vfdStatusName\":\""+obj[25]+"\",");
			result_json.append("\"vfdStatus2\":\""+obj[26]+"\",");
			result_json.append("\"vfdStatus2Name\":\""+obj[27]+"\",");
			
			result_json.append("\"runFrequency\":\""+obj[28]+"\",");
			result_json.append("\"SPM\":\""+obj[29]+"\",");
			result_json.append("\"vfdBusbarVoltage\":\""+obj[30]+"\",");
			result_json.append("\"vfdOutputVoltage\":\""+obj[31]+"\",");
			result_json.append("\"vfdOutputCurrent\":\""+obj[32]+"\",");
			result_json.append("\"setFrequencyFeedback\":\""+obj[33]+"\",");
			
			result_json.append("\"vfdFaultCode\":\""+obj[34]+"\",");
			result_json.append("\"vfdPosition\":\""+obj[35]+"\",");
			result_json.append("\"vfdPositionName\":\""+obj[36]+"\",");
			result_json.append("\"vfdManufacturerCode\":\""+obj[37]+"\",");
			result_json.append("\"vfdManufacturerName\":\""+obj[38]+"\",");
			
			result_json.append("\"frequencyOrSPMcontrolSign\":\""+obj[39]+"\",");
			result_json.append("\"frequencyOrSPMcontrol\":\""+obj[40]+"\",");
			
			result_json.append("\"frequencySetValue\":\""+obj[41]+"\",");
			result_json.append("\"SPMSetValue\":\""+obj[42]+"\",");
			result_json.append("\"SPMBy10hz\":\""+obj[43]+"\",");
			result_json.append("\"SPMBy50hz\":\""+obj[44]+"\",");
			
			result_json.append("\"rtuAddr\":\""+obj[45]+"\",");
			result_json.append("\"rtuProgramVersion\":\""+obj[46]+"\",");
			result_json.append("\"setWellname\":\""+obj[47]+"\"");
		}
		result_json.append("}");
		return result_json.toString().replaceAll("null", "");
	}
	
	public String getWellHistoryDataCurveData(String wellName,String startDate,String endDate,String itemName,String itemCode) throws SQLException, IOException {
		StringBuffer dynSbf = new StringBuffer();
		String uplimit="";
		String downlimit="";
		String sql="select to_char(t.acquisitionTime,'yyyy-mm-dd hh24:mi:ss'),t."+itemCode+" from viw_cbm_discrete_hist t "
				+ " where t.wellName='"+wellName+"' and to_date(to_char(t.acquisitionTime,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') order by t.acquisitionTime";
		
		
		int totals = getTotalCountRows(sql);//获取总记录数
		List<?> list=this.findCallSql(sql);
		
		dynSbf.append("{\"success\":true,\"totalCount\":" + totals + ",\"wellName\":\""+wellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"totalRoot\":[");
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				dynSbf.append("{ \"acquisitionTime\":\"" + obj[0] + "\",");
				dynSbf.append("\"value\":\""+obj[1]+"\"},");
				if(obj.length==4&&i==list.size()-1){
					uplimit=obj[2]+"";
					downlimit=obj[3]+"";
				}
			}
			dynSbf.deleteCharAt(dynSbf.length() - 1);
		}
		dynSbf.append("],\"uplimit\":\""+uplimit+"\",\"downlimit\":\""+downlimit+"\"}");
		return dynSbf.toString();
	}
}
