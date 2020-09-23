package com.gao.service.dailyEvaluation;

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

@Component("dailyEvaluationService")
public class DailyEvaluationService<T> extends BaseService<T> {
	private BaseDao dao;
	@Autowired
	private CommonDataService service;
	@Autowired
	private DataitemsInfoService dataitemsInfoService;
	
	public String statisticsData(String orgId,String type,String wellType,String totalDate){
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
		sql="select "+statType+",count(1) from viw_cbm_total_day t where  t.org_id in("+orgId+") and calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
		
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
	
	public String groupValveDailyStatisticsData(String orgId,String type,String wellType,String totalDate){
		StringBuffer result_json = new StringBuffer();
		String sql="";
		String statType="commStatusName";
		if("1".equalsIgnoreCase(type)){
			statType="commStatusName";
		}else if("2".equalsIgnoreCase(type)){
			statType="commtimeefficiencyLevel";
		}
		sql="select "+statType+",count(1) from viw_groupvalve_total_day t where  t.org_id in("+orgId+") and calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
		
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
	
	public String getRealtimeAnalysisWellList(String orgId, String wellName, Page pager,String type,String unitType,String totalDate,String startDate,String endDate,String statValue)
			throws Exception {
		DataDictionary ddic = null;
		String columns= "";
		String sql="";
		String finalSql="";
		String sqlAll="";
		String ddicName="CBMWellDailyRunStatus";
		String tableName="viw_cbm_total_day";
		String typeColumnName="runStatusName";
		
		if("1".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyRunStatus";
			typeColumnName="runStatusName";
		}else if("2".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyRunEff";
			typeColumnName="runtimeefficiencyLevel";
		}else if("3".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommStatus";
			typeColumnName="commStatusName";
		}else if("4".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommEff";
			typeColumnName="commtimeefficiencyLevel";
		}
		
		ddic  = dataitemsInfoService.findTableSqlWhereByListFaceId(ddicName);
		
		columns = ddic.getTableHeader();
		
		sql=ddic.getSql()+",commStatus,commAlarmLevel,runStatus,runAlarmLevel";
		
		
		sql+= " from "+tableName+" t where t.org_id in("+orgId+")";
		
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and to_date(to_char(t.calculateDate,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " and  t.wellName='"+wellName+"' "
				+ " order by t.calculateDate desc";
		}else{
			sql+=" and t.calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
			if(StringManagerUtils.isNotNull(statValue)){
				sql+=" and "+typeColumnName+"='"+statValue+"'";
			}
			sql+=" order by t.sortnum, t.wellName";
		}
		
		sqlAll=sql;
		
		int maxvalue=pager.getLimit()+pager.getStart();
		finalSql="select * from   ( select a.*,rownum as rn from ("+sqlAll+" ) a where  rownum <="+maxvalue+") b where rn >"+pager.getStart();
		String getResult = this.findCustomPageBySqlEntity(sqlAll,finalSql, columns, 20 + "", pager);
		return getResult;
	}
	
	public String exportCBMWellDailyAnalisiDataExcel(String orgId, String wellName, Page pager,String type,String unitType,String totalDate,String startDate,String endDate,String statValue)
			throws Exception {
		DataDictionary ddic = null;
		String columns= "";
		String sql="";
		String finalSql="";
		String sqlAll="";
		String ddicName="CBMWellDailyRunStatus";
		String tableName="viw_cbm_total_day";
		String typeColumnName="runStatusName";
		
		if("1".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyRunStatus";
			typeColumnName="runStatusName";
		}else if("2".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyRunEff";
			typeColumnName="runtimeefficiencyLevel";
		}else if("3".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommStatus";
			typeColumnName="commStatusName";
		}else if("4".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommEff";
			typeColumnName="commtimeefficiencyLevel";
		}
		
		ddic  = dataitemsInfoService.findTableSqlWhereByListFaceId(ddicName);
		
		columns = ddic.getTableHeader();
		
		sql=ddic.getSql()+",commStatus,commAlarmLevel,runStatus,runAlarmLevel";
		
		
		sql+= " from "+tableName+" t where t.org_id in("+orgId+")";
		
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and to_date(to_char(t.calculateDate,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " and  t.wellName='"+wellName+"' "
				+ " order by t.calculateDate desc";
		}else{
			sql+=" and t.calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
			if(StringManagerUtils.isNotNull(statValue)){
				sql+=" and "+typeColumnName+"='"+statValue+"'";
			}
			sql+=" order by t.sortnum, t.wellName";
		}
		
		sqlAll=sql;
		
		String getResult = this.findExportDataBySqlEntity(sqlAll,sqlAll, columns, 20 + "", pager);
		return getResult;
	}
	
	public String getGroupValveRealtimeAnalysisWellList(String orgId, String wellName, Page pager,String type,String unitType,String totalDate,String startDate,String endDate,String statValue)
			throws Exception {
		DataDictionary ddic = null;
		String columns= "";
		String sql="";
		String finalSql="";
		String sqlAll="";
		String ddicName="CBMWellDailyCommStatus";
		String tableName="viw_groupvalve_total_day";
		String typeColumnName="commStatusName";
		
		if("1".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommStatus";
			typeColumnName="commStatusName";
		}else if("2".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommEff";
			typeColumnName="commtimeefficiencyLevel";
		}
		
		ddic  = dataitemsInfoService.findTableSqlWhereByListFaceId(ddicName);
		
		columns = ddic.getTableHeader();
		
		sql=ddic.getSql()+",commStatus,commAlarmLevel";
		
		
		sql+= " from "+tableName+" t where t.org_id in("+orgId+")";
		
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and to_date(to_char(t.calculateDate,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " and  t.wellName='"+wellName+"' "
				+ " order by t.calculateDate desc";
		}else{
			sql+=" and t.calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
			if(StringManagerUtils.isNotNull(statValue)){
				sql+=" and "+typeColumnName+"='"+statValue+"'";
			}
			sql+=" order by t.sortnum, t.wellName";
		}
		
		sqlAll=sql;
		
		int maxvalue=pager.getLimit()+pager.getStart();
		finalSql="select * from   ( select a.*,rownum as rn from ("+sqlAll+" ) a where  rownum <="+maxvalue+") b where rn >"+pager.getStart();
		String getResult = this.findCustomPageBySqlEntity(sqlAll,finalSql, columns, 20 + "", pager);
		return getResult;
	}
	
	
	public String exportGroupValveDailyAnalisiDataExcel(String orgId, String wellName, Page pager,String type,String unitType,String totalDate,String startDate,String endDate,String statValue)
			throws Exception {
		DataDictionary ddic = null;
		String columns= "";
		String sql="";
		String finalSql="";
		String sqlAll="";
		String ddicName="CBMWellDailyCommStatus";
		String tableName="viw_groupvalve_total_day";
		String typeColumnName="commStatusName";
		
		if("1".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommStatus";
			typeColumnName="commStatusName";
		}else if("2".equalsIgnoreCase(type)){
			ddicName="CBMWellDailyCommEff";
			typeColumnName="commtimeefficiencyLevel";
		}
		
		ddic  = dataitemsInfoService.findTableSqlWhereByListFaceId(ddicName);
		
		columns = ddic.getTableHeader();
		
		sql=ddic.getSql()+",commStatus,commAlarmLevel";
		
		
		sql+= " from "+tableName+" t where t.org_id in("+orgId+")";
		
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and to_date(to_char(t.calculateDate,'yyyy-mm-dd'),'yyyy-mm-dd') between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " and  t.wellName='"+wellName+"' "
				+ " order by t.calculateDate desc";
		}else{
			sql+=" and t.calculateDate=to_date('"+totalDate+"','yyyy-mm-dd') ";
			if(StringManagerUtils.isNotNull(statValue)){
				sql+=" and "+typeColumnName+"='"+statValue+"'";
			}
			sql+=" order by t.sortnum, t.wellName";
		}
		
		sqlAll=sql;
		
		String getResult = this.findExportDataBySqlEntity(sqlAll,sqlAll, columns, 20 + "", pager);
		return getResult;
	}
	
	
	public String getCBMWellDailyAnalysisAndAcqAndControlData(String recordId,String wellName,String selectedWellName,int userId)throws Exception {
		StringBuffer result_json = new StringBuffer();
		
		
		String sql="select t.commTime,t.commTimeEfficiency,t.runStatus,t.runTimeEfficiency,"
				+ " t.gasTodayProd,t.liquidFlowMeterProd "
				+ " from viw_cbm_total_day t"
				+ " where t.id="+recordId;
		List<?> list = this.findCallSql(sql);
		result_json.append("{ \"success\":true");
		if(list.size()>0){
			Object[] obj=(Object[]) list.get(0);
			result_json.append(",\"commTime\":\""+obj[0]+"\",");
			result_json.append("\"commTimeEfficiency\":\""+obj[1]+"\",");
			
			result_json.append("\"runStatus\":\""+obj[2]+"\",");
			result_json.append("\"runTimeEfficiency\":\""+obj[3]+"\",");
			
			result_json.append("\"gasTodayProd\":\""+obj[4]+"\",");
			result_json.append("\"liquidFlowMeterProd\":\""+obj[5]+"\"");
		}
		result_json.append("}");
		return result_json.toString().replaceAll("null", "");
	}
	
	public String getGroupValveDailyAnalysisAndAcqAndControlData(String recordId,String wellName,String selectedWellName,int userId)throws Exception {
		StringBuffer result_json = new StringBuffer();
		
		
		String sql="select t.commTime,t.commTimeEfficiency,"
				+ " t.dailyFlow1,t.cumulativeFlow1,"
				+ " t.dailyFlow2,t.cumulativeFlow2,"
				+ " t.dailyFlow3,t.cumulativeFlow3,"
				+ " t.dailyFlow4,t.cumulativeFlow4"
				+ " from viw_groupvalve_total_day t"
				+ " where t.id="+recordId;
		List<?> list = this.findCallSql(sql);
		result_json.append("{ \"success\":true");
		if(list.size()>0){
			Object[] obj=(Object[]) list.get(0);
			result_json.append(",\"commTime\":\""+obj[0]+"\",");
			result_json.append("\"commTimeEfficiency\":\""+obj[1]+"\",");
			
			result_json.append("\"dailyFlow1\":\""+obj[2]+"\",");
			result_json.append("\"cumulativeFlow1\":\""+obj[3]+"\",");
			
			result_json.append("\"dailyFlow2\":\""+obj[4]+"\",");
			result_json.append("\"cumulativeFlow2\":\""+obj[5]+"\",");
			
			result_json.append("\"dailyFlow3\":\""+obj[6]+"\",");
			result_json.append("\"cumulativeFlow3\":\""+obj[7]+"\",");
			
			result_json.append("\"dailyFlow4\":\""+obj[8]+"\",");
			result_json.append("\"cumulativeFlow4\":\""+obj[9]+"\"");
		}
		result_json.append("}");
		return result_json.toString().replaceAll("null", "");
	}
	
//	public String getCBMWellDataCurve(String wellName,String selectedWellName,String startDate,String endDate) throws SQLException, IOException {
//		StringBuffer dynSbf = new StringBuffer();
//		
//		String sql="select "
//				+ " to_char(t.acquisitionTime,'yyyy-mm-dd hh24:mi:ss'),"
//				+ " runTimeEfficiency,"
//				+ " t.gasCumulativeFlow,t.liquidCumulativeflow,t.gasFlowmeterPress,"
//				+ " t.totalWattEnergy"
//				+ " from viw_cbm_discrete_hist t "
//				+ " where t.wellName='"+selectedWellName+"'";
//		if(StringManagerUtils.isNotNull(wellName)){
//			sql+=" and t.acquisitionTime between to_date('"+startDate+"','yyyy-mm-dd')  and to_date('"+endDate+"','yyyy-mm-dd')+1 ";
//		}else{
//			sql+=" and t.acquisitiontime >to_date(to_char(sysdate,'yyyy-mm-dd'),'yyyy-mm-dd') ";
//		}
//		sql+= " order by t.acquisitionTime";
//		List<?> list=this.findCallSql(sql);
//		
//		dynSbf.append("{\"success\":true,\"totalCount\":" + list.size() + ",\"wellName\":\""+selectedWellName+"\",\"totalRoot\":[");
//		if (list.size() > 0) {
//			for (int i = 0; i < list.size(); i++) {
//				Object[] obj = (Object[]) list.get(i);
//				dynSbf.append("{ \"acquisitionTime\":\"" + obj[0] + "\",");
//				dynSbf.append("\"runTimeEfficiency\":"+obj[1]+",");
//				dynSbf.append("\"gasCumulativeFlow\":"+obj[2]+",");
//				dynSbf.append("\"liquidCumulativeflow\":"+obj[3]+",");
//				dynSbf.append("\"gasFlowmeterPress\":"+obj[4]+",");
//				dynSbf.append("\"totalWattEnergy\":"+obj[5]+"},");
//				
//			}
//		}
//		if(dynSbf.toString().endsWith(",")){
//			dynSbf.deleteCharAt(dynSbf.length() - 1);
//		}
//		dynSbf.append("]");
//		dynSbf.append("}");
//		return dynSbf.toString();
//	}
	
	public String getCBMWellDailyDataCurve(String wellName,String selectedWellName,String startDate,String endDate) throws SQLException, IOException {
		StringBuffer dynSbf = new StringBuffer();
		
		String sql="select "
				+ " to_char(t.calculateDate,'yyyy-mm-dd'),"
				+ " t.gastodayprod,t.liquidflowmeterprod"
				+ " from viw_cbm_total_day t "
				+ " where t.wellName='"+selectedWellName+"'";
		sql+=" and t.calculateDate between to_date('"+startDate+"','yyyy-mm-dd')  and to_date('"+endDate+"','yyyy-mm-dd') ";
		sql+= " order by t.calculateDate";
		List<?> list=this.findCallSql(sql);
		
		dynSbf.append("{\"success\":true,\"totalCount\":" + list.size() + ",\"wellName\":\""+selectedWellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"totalRoot\":[");
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				dynSbf.append("{ \"calculateDate\":\"" + obj[0] + "\",");
				dynSbf.append("\"gastodayProd\":\""+obj[1]+"\",");
				dynSbf.append("\"liquidflowMeterProd\":\""+obj[2]+"\"},");
				
			}
		}
		if(dynSbf.toString().endsWith(",")){
			dynSbf.deleteCharAt(dynSbf.length() - 1);
		}
		dynSbf.append("]");
		dynSbf.append("}");
		return dynSbf.toString().replaceAll("null", "");
	}
	
	public String getGroupValveDailyDataCurve(String wellName,String selectedWellName,String startDate,String endDate) throws SQLException, IOException {
		StringBuffer dynSbf = new StringBuffer();
		
		String sql="select "
				+ " to_char(t.calculateDate,'yyyy-mm-dd'),"
				+ " t.dailyFlow1,t.dailyFlow2,t.dailyFlow3,t.dailyFlow4"
				+ " from viw_groupvalve_total_day t "
				+ " where t.wellName='"+selectedWellName+"'";
		sql+=" and t.calculateDate between to_date('"+startDate+"','yyyy-mm-dd')  and to_date('"+endDate+"','yyyy-mm-dd') ";
		sql+= " order by t.calculateDate";
		List<?> list=this.findCallSql(sql);
		
		dynSbf.append("{\"success\":true,\"totalCount\":" + list.size() + ",\"wellName\":\""+selectedWellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"totalRoot\":[");
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				dynSbf.append("{ \"calculateDate\":\"" + obj[0] + "\",");
				dynSbf.append("\"dailyFlow1\":\""+obj[1]+"\",");
				dynSbf.append("\"dailyFlow2\":\""+obj[2]+"\",");
				dynSbf.append("\"dailyFlow3\":\""+obj[3]+"\",");
				dynSbf.append("\"dailyFlow4\":\""+obj[4]+"\"},");
				
			}
		}
		if(dynSbf.toString().endsWith(",")){
			dynSbf.deleteCharAt(dynSbf.length() - 1);
		}
		dynSbf.append("]");
		dynSbf.append("}");
		return dynSbf.toString().replaceAll("null", "");
	}
	
	
	public String getCBMWellDailyHistoryDataCurveData(String wellName,String startDate,String endDate,String itemName,String itemCode) throws SQLException, IOException {
		StringBuffer dynSbf = new StringBuffer();
		String uplimit="";
		String downlimit="";
		String sql="select to_char(t.calculateDate,'yyyy-mm-dd'),t."+itemCode+" from viw_cbm_total_day t "
				+ " where t.wellName='"+wellName+"' "
				+ " and t.calculateDate between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " order by t.calculateDate";
		
		
		int totals = getTotalCountRows(sql);//获取总记录数
		List<?> list=this.findCallSql(sql);
		
		dynSbf.append("{\"success\":true,\"totalCount\":" + totals + ",\"wellName\":\""+wellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"totalRoot\":[");
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				dynSbf.append("{ \"calculateDate\":\"" + obj[0] + "\",");
				dynSbf.append("\"value\":\""+obj[1]+"\"},");
				if(obj.length==4&&i==list.size()-1){
					uplimit=obj[2]+"";
					downlimit=obj[3]+"";
				}
			}
			dynSbf.deleteCharAt(dynSbf.length() - 1);
		}
		dynSbf.append("],\"uplimit\":\""+uplimit+"\",\"downlimit\":\""+downlimit+"\"}");
		return dynSbf.toString().replaceAll("null", "");
	}
	
	public String getGroupValveDailyHistoryDataCurveData(String wellName,String startDate,String endDate,String itemName,String itemCode) throws SQLException, IOException {
		StringBuffer dynSbf = new StringBuffer();
		String uplimit="";
		String downlimit="";
		String sql="select to_char(t.calculateDate,'yyyy-mm-dd'),t."+itemCode+" from viw_groupvalve_total_day t "
				+ " where t.wellName='"+wellName+"' "
				+ " and t.calculateDate between to_date('"+startDate+"','yyyy-mm-dd') and to_date('"+endDate+"','yyyy-mm-dd') "
				+ " order by t.calculateDate";
		
		
		int totals = getTotalCountRows(sql);//获取总记录数
		List<?> list=this.findCallSql(sql);
		
		dynSbf.append("{\"success\":true,\"totalCount\":" + totals + ",\"wellName\":\""+wellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"totalRoot\":[");
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				dynSbf.append("{ \"calculateDate\":\"" + obj[0] + "\",");
				dynSbf.append("\"value\":\""+obj[1]+"\"},");
				if(obj.length==4&&i==list.size()-1){
					uplimit=obj[2]+"";
					downlimit=obj[3]+"";
				}
			}
			dynSbf.deleteCharAt(dynSbf.length() - 1);
		}
		dynSbf.append("],\"uplimit\":\""+uplimit+"\",\"downlimit\":\""+downlimit+"\"}");
		return dynSbf.toString().replaceAll("null", "");
	}
}
