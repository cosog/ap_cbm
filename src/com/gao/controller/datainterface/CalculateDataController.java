package com.gao.controller.datainterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gao.controller.base.BaseController;
import com.gao.service.base.CommonDataService;
import com.gao.service.datainterface.CalculateDataService;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;

@Controller
@RequestMapping("/calculateDataController")
@Scope("prototype")
public class CalculateDataController extends BaseController{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	CalculateDataService<?> calculateDataService;
	@Autowired
	private CommonDataService commonDataService;
	
		
	
	@RequestMapping("/CBMDailyCalculation")
	public String CBMDailyCalculation() throws ParseException, SQLException{
		String tatalDate=ParamUtils.getParameter(request, "date");
		String wellId=ParamUtils.getParameter(request, "wellId");
		if(StringManagerUtils.isNotNull(tatalDate)){
			tatalDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(tatalDate));
		}else{
			tatalDate=StringManagerUtils.getCurrentTime();
		}
		calculateDataService.CBMDailyCalculation(tatalDate,wellId);
		
		
		System.out.println("汇总完成");
		
		String json ="";
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
