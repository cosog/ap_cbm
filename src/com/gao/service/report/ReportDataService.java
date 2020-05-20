package com.gao.service.report;

import java.util.List;
import org.springframework.stereotype.Service;

import com.gao.service.base.BaseService;
import com.gao.utils.Config;
import com.gao.utils.ConfigFile;
import com.gao.utils.Page;
import com.gao.utils.StringManagerUtils;

@Service("reportDataService")
public class ReportDataService<T> extends BaseService<T> {

	public String showCBMWellDailyReportData(Page pager, String orgId,String wellName,String startDate,String endDate,String wellType)throws Exception {
		StringBuffer result_json = new StringBuffer();
		String sql="select t.id, t.wellName,to_char(t.calculateDate,'yyyy-mm-dd') as calculateDate,"
				+ " t.commTime,t.commRange, t.commTimeEfficiency,"
				+ " t.runTime,t.runRange, t.runTimeEfficiency,"
				+ " t.gasCumulativeFlow,t.gasTodayProd,t.gasMonthProd,t.gasYearProd,"
				+ " t.liquidCumulativeFlow,t.liquidFlowmeterProd,t.liquidMonthProd,t.liquidYearProd,"
				+ " t.spm,t.fluidLevel,t.casingPressure*0.001,t.totalWattEnergy,t.todayWattEnergy,"
				+ " remark"
				+ " from viw_cbm_total_day t "
				+ " where t.org_id in ("+orgId+") "
				+ " and t.calculateDate =  to_date('"+endDate+"','yyyy-mm-dd')";
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and  t.wellName='"+wellName+"'";
			sql+=" order by t.calculateDate ";
		}else{
			sql+=" order by t.sortNum,t.wellName";
		}
		int totals=this.getTotalCountRows(sql);
		List<?> list = this.findCallSql(sql);
		String columns= "["
				+ "{ \"header\":\"序号\",\"dataIndex\":\"id\",width:50},"
				+ "{ \"header\":\"井名\",\"dataIndex\":\"wellName\"},"
				+ "{ \"header\":\"日期\",\"dataIndex\":\"calculateDate\",width:100},"
				
				+ "{ \"header\":\"通信时间(h)\",\"dataIndex\":\"commTime\"},"
				+ "{ \"header\":\"在线区间\",\"dataIndex\":\"commRange\"},"
				+ "{ \"header\":\"在线时率(%)\",\"dataIndex\":\"commTimeEfficiency\"},"
				
				+ "{ \"header\":\"运行时间(h)\",\"dataIndex\":\"runTime\"},"
				+ "{ \"header\":\"运行区间\",\"dataIndex\":\"runRange\"},"
				+ "{ \"header\":\"运行时率(%)\",\"dataIndex\":\"runTimeEfficiency\"},"
				
				+ "{ \"header\":\"累计产气量(m^3)\",\"dataIndex\":\"gasCumulativeFlow\"},"
				+ "{ \"header\":\"日产气量(m^3/d)\",\"dataIndex\":\"gasTodayProd\"},"
				+ "{ \"header\":\"月累计产气量(m^3)\",\"dataIndex\":\"gasMonthProd\"},"
				+ "{ \"header\":\"年累计产气量(m^3)\",\"dataIndex\":\"gasYearProd\"},"

				+ "{ \"header\":\"累计产水量(m^3)\",\"dataIndex\":\"liquidCumulativeFlow\"},"
				+ "{ \"header\":\"日产水量(m^3/d)\",\"dataIndex\":\"liquidFlowmeterProd\"},"
				+ "{ \"header\":\"月累计产水量(m^3/d)\",\"dataIndex\":\"liquidMonthProd\"},"
				+ "{ \"header\":\"年累计产水量(m^3/d)\",\"dataIndex\":\"liquidYearProd\"},"
				
				+ "{ \"header\":\"冲次(1/min)\",\"dataIndex\":\"spm\"},"
				+ "{ \"header\":\"动液面(m)\",\"dataIndex\":\"fluidLevel\"},"
				+ "{ \"header\":\"套压(kPa)\",\"dataIndex\":\"casingPressure\"},"

				+ "{ \"header\":\"累计耗电量(kW·h)\",\"dataIndex\":\"totalWattEnergy\"},"
				+ "{ \"header\":\"日用电量(kW·h)\",\"dataIndex\":\"todayWattEnergy\"},"
				
				+ "{ \"header\":\"备注\",\"dataIndex\":\"remark\"}"
				+ "]";
		
		result_json.append("{ \"success\":true,\"wellName\":\""+wellName+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"columns\":"+columns+",");
		result_json.append("\"totalCount\":"+totals+",");
		result_json.append("\"totalRoot\":[");
		for(int i=0;i<list.size();i++){
			Object[] obj=(Object[]) list.get(i);
			result_json.append("{\"id\":"+obj[0]+",");
			result_json.append("\"wellName\":\""+obj[1]+"\",");
			result_json.append("\"calculateDate\":\""+obj[2]+"\",");
			result_json.append("\"commTime\":\""+obj[3]+"\",");
			result_json.append("\"commRange\":\""+obj[4]+"\",");
			result_json.append("\"commTimeEfficiency\":\""+obj[5]+"\",");
			result_json.append("\"runTime\":\""+obj[6]+"\",");
			result_json.append("\"runRange\":\""+obj[7]+"\",");
			result_json.append("\"runTimeEfficiency\":\""+obj[8]+"\",");
			
			result_json.append("\"gasCumulativeFlow\":\""+obj[9]+"\",");
			result_json.append("\"gasTodayProd\":\""+obj[10]+"\",");
			result_json.append("\"gasMonthProd\":\""+obj[11]+"\",");
			result_json.append("\"gasYearProd\":\""+obj[12]+"\",");
			
			result_json.append("\"liquidCumulativeFlow\":\""+obj[13]+"\",");
			result_json.append("\"liquidFlowmeterProd\":\""+obj[14]+"\",");
			result_json.append("\"liquidMonthProd\":\""+obj[15]+"\",");
			result_json.append("\"liquidYearProd\":\""+obj[16]+"\",");
			
			result_json.append("\"spm\":\""+obj[17]+"\",");
			result_json.append("\"fluidLevel\":\""+obj[18]+"\",");
			result_json.append("\"casingPressure\":\""+obj[19]+"\",");
			result_json.append("\"totalWattEnergy\":\""+obj[20]+"\",");
			result_json.append("\"todayWattEnergy\":\""+obj[21]+"\",");
			result_json.append("\"remark\":\""+obj[22]+"\"},");
		}
		if(result_json.toString().endsWith(",")){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]}");
		return result_json.toString().replaceAll("null", "");
	}
	
	public String exportCBMWellDailyReportExcelData(Page pager, String orgId,String wellName,String startDate,String endDate,String wellType)throws Exception {
		StringBuffer result_json = new StringBuffer();
		String sql="select t.id, t.wellName,to_char(t.calculateDate,'yyyy-mm-dd') as calculateDate,"
				+ " t.commTime,t.commRange, t.commTimeEfficiency,"
				+ " t.runTime,t.runRange, t.runTimeEfficiency,"
				+ " t.gasCumulativeFlow,t.gasTodayProd,t.gasMonthProd,t.gasYearProd,"
				+ " t.liquidCumulativeFlow,t.liquidFlowmeterProd,t.liquidMonthProd,t.liquidYearProd,"
				+ " t.spm,t.fluidLevel,t.casingPressure*0.001,t.totalWattEnergy,t.todayWattEnergy,"
				+ " remark"
				+ " from viw_cbm_total_day t "
				+ " where t.org_id in ("+orgId+") "
				+ " and t.calculateDate =  to_date('"+endDate+"','yyyy-mm-dd')";
		if(StringManagerUtils.isNotNull(wellName)){
			sql+=" and  t.wellName='"+wellName+"'";
			sql+=" order by t.calculateDate ";
		}else{
			sql+=" order by t.sortNum,t.wellName";
		}
		int totals=this.getTotalCountRows(sql);
		List<?> list = this.findCallSql(sql);
		
		result_json.append("[");
		for(int i=0;i<list.size();i++){
			Object[] obj=(Object[]) list.get(i);
			result_json.append("{\"id\":"+obj[0]+",");
			result_json.append("\"wellName\":\""+obj[1]+"\",");
			result_json.append("\"calculateDate\":\""+obj[2]+"\",");
			result_json.append("\"commTime\":\""+obj[3]+"\",");
			result_json.append("\"commRange\":\""+obj[4]+"\",");
			result_json.append("\"commTimeEfficiency\":\""+obj[5]+"\",");
			result_json.append("\"runTime\":\""+obj[6]+"\",");
			result_json.append("\"runRange\":\""+obj[7]+"\",");
			result_json.append("\"runTimeEfficiency\":\""+obj[8]+"\",");
			
			result_json.append("\"gasCumulativeFlow\":\""+obj[9]+"\",");
			result_json.append("\"gasTodayProd\":\""+obj[10]+"\",");
			result_json.append("\"gasMonthProd\":\""+obj[11]+"\",");
			result_json.append("\"gasYearProd\":\""+obj[12]+"\",");
			
			result_json.append("\"liquidCumulativeFlow\":\""+obj[13]+"\",");
			result_json.append("\"liquidFlowmeterProd\":\""+obj[14]+"\",");
			result_json.append("\"liquidMonthProd\":\""+obj[15]+"\",");
			result_json.append("\"liquidYearProd\":\""+obj[16]+"\",");
			
			result_json.append("\"spm\":\""+obj[17]+"\",");
			result_json.append("\"fluidLevel\":\""+obj[18]+"\",");
			result_json.append("\"casingPressure\":\""+obj[19]+"\",");
			result_json.append("\"totalWattEnergy\":\""+obj[20]+"\",");
			result_json.append("\"todayWattEnergy\":\""+obj[21]+"\",");
			result_json.append("\"remark\":\""+obj[22]+"\"},");
		}
		if(result_json.toString().endsWith(",")){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]");
		return result_json.toString().replaceAll("null", "");
	}
}
