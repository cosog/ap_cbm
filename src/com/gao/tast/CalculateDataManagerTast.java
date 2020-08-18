package com.gao.tast;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gao.utils.Config;
import com.gao.utils.StringManagerUtils;

@Component("calculateDataManagerTast")  
public class CalculateDataManagerTast {
	private static Connection conn = null;   
    private static PreparedStatement pstmt = null;  
    private static ResultSet rs = null;  
	
	
	/**
	 * 汇总计算
	 * */
	@Scheduled(cron = "0 0 1/24 * * ?")
	public void totalCalculationTast() throws SQLException, UnsupportedEncodingException, ParseException{
		String url=Config.getInstance().configFile.getServer().getAccessPath()+"/calculateDataController/CBMDailyCalculation";
		@SuppressWarnings("unused")
		String result="";
		result=StringManagerUtils.sendPostMethod(url, "","utf-8");
	}
}
