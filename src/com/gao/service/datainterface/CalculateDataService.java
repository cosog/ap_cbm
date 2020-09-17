package com.gao.service.datainterface;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gao.model.calculate.CommResponseData;
import com.gao.model.calculate.TimeEffResponseData;
import com.gao.service.base.BaseService;
import com.gao.utils.Config;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@SuppressWarnings("deprecation")
@Service("calculateDataService")
public class CalculateDataService<T> extends BaseService<T> {
	
	public void CBMDailyCalculation(String tatalDate,String wellId){
		StringBuffer dataSbf=null;
		List<String> requestDataList=new ArrayList<String>();
		String timeEffTotalUrl=Config.getInstance().configFile.getAgileCalculate().getRun()[0];
		String commTotalUrl=Config.getInstance().configFile.getAgileCalculate().getCommunication()[0];
		String statusSql="select well.wellname,to_char(t.acquisitiontime,'yyyy-mm-dd hh24:mi:ss') as acquisitiontime,"
				+ "t.commstatus,t.commtimeefficiency,t.commtime,t.commrange,"
				+ "t.runstatus,t.runtimeefficiency,t.runtime,t.runrange "
				+ " from tbl_cbm_discrete_hist t,tbl_wellinformation well "
				+ " where t.wellid=well.id and t.acquisitiontime=( select max(t2.acquisitiontime) from tbl_cbm_discrete_hist t2 where t2.wellid=t.wellid and t2.acquisitiontime between to_date('"+tatalDate+"','yyyy-mm-dd')-1 and to_date('"+tatalDate+"','yyyy-mm-dd'))";
		if(StringManagerUtils.isNotNull(wellId)){
			statusSql+=" and t.wellid in("+wellId+")";
		}
		statusSql+=" order by well.sortnum";

		List<?> statusList = findCallSql(statusSql);
		for(int j=0;j<statusList.size();j++){
			try{
				TimeEffResponseData timeEffResponseData=null;
				CommResponseData commResponseData=null;
				Object[] statusObj=(Object[]) statusList.get(j);

				boolean commStatus=false;
				boolean runStatus=false;
				if(statusObj[2]!=null&&StringManagerUtils.stringToInteger(statusObj[2]+"")==1){
					commStatus=true;
				}
				if(statusObj[6]!=null&&StringManagerUtils.stringToInteger(statusObj[6]+"")==1){
					runStatus=true;
				}
				String commTotalRequestData="{"
						+ "\"AKString\":\"\","
						+ "\"WellName\":\""+statusObj[0]+"\","
						+ "\"Last\":{"
						+ "\"AcqTime\": \""+statusObj[1]+"\","
						+ "\"CommStatus\": "+commStatus+","
						+ "\"CommEfficiency\": {"
						+ "\"Efficiency\": "+statusObj[3]+","
						+ "\"Time\": "+statusObj[4]+","
						+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(statusObj[5]+"")+""
						+ "}"
						+ "},"
						+ "\"Current\": {"
						+ "\"AcqTime\":\""+tatalDate+" 01:00:00\","
						+ "\"CommStatus\":true"
						+ "}"
						+ "}";
				String runTotalRequestData="{"
						+ "\"AKString\":\"\","
						+ "\"WellName\":\""+statusObj[0]+"\","
						+ "\"Last\":{"
						+ "\"AcqTime\": \""+statusObj[1]+"\","
						+ "\"RunStatus\": "+runStatus+","
						+ "\"RunEfficiency\": {"
						+ "\"Efficiency\": "+statusObj[7]+","
						+ "\"Time\": "+statusObj[8]+","
						+ "\"Range\": "+StringManagerUtils.getWellRuningRangeJson(statusObj[9]+"")+""
						+ "}"
						+ "},"
						+ "\"Current\": {"
						+ "\"AcqTime\":\""+tatalDate+" 01:00:00\","
						+ "\"RunStatus\":true"
						+ "}"
						+ "}";
				Gson gson = new Gson();
				java.lang.reflect.Type type=null;
				String commTotalResponse=StringManagerUtils.sendPostMethod(commTotalUrl, commTotalRequestData,"utf-8");
				type = new TypeToken<CommResponseData>() {}.getType();
				commResponseData = gson.fromJson(commTotalResponse, type);
				String runTotalResponse=StringManagerUtils.sendPostMethod(timeEffTotalUrl, runTotalRequestData,"utf-8");
				type = new TypeToken<TimeEffResponseData>() {}.getType();
				timeEffResponseData = gson.fromJson(runTotalResponse, type);
				if(timeEffResponseData!=null&&timeEffResponseData.getResultStatus()==1
						&&commResponseData!=null&&commResponseData.getResultStatus()==1){
					saveCBMDailyCalculationData(timeEffResponseData,commResponseData);
				}
			}catch(ParseException|SQLException e){
				e.printStackTrace();
				continue;
			}
			
		}
	}
	public boolean saveCBMDailyCalculationData(TimeEffResponseData timeEffResponseData,CommResponseData commResponseData) throws SQLException, ParseException{
		return this.getBaseDao().saveCBMDailyCalculationData(timeEffResponseData,commResponseData);
	}
	
	
}