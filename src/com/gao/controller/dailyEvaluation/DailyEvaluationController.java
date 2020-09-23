package com.gao.controller.dailyEvaluation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gao.controller.base.BaseController;
import com.gao.model.User;
import com.gao.service.base.CommonDataService;
import com.gao.service.dailyEvaluation.DailyEvaluationService;
import com.gao.tast.EquipmentDriverServerTast;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;
import com.gao.utils.UnixPwdCrypt;

@Controller
@RequestMapping("/dailyEvaluationController")
@Scope("prototype")
public class DailyEvaluationController<T> extends BaseController{
	private static final long serialVersionUID = 1L;
	@Autowired
	private CommonDataService service;
	@Autowired
	private DailyEvaluationService dailyEvaluationService;
	private int page;
	private int limit;
	private int totals;
	private String wellName;
	private String orgId;
	private int id;
	
	/**
	 * <p>
	 * 描述：工况统计饼图、柱状图json数据
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/statisticsData")
	public String statisticsData() throws Exception {
		orgId=ParamUtils.getParameter(request, "orgId");
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			User user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		String type = ParamUtils.getParameter(request, "type");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String json = "";
		
		/******
		 * 饼图及柱状图需要的data信息
		 * ***/
		json = dailyEvaluationService.statisticsData(orgId,type,wellType,totalDate);
//		json="{ \"success\":true,\"List\":[{\"item\":\"离线\",\"count\":1},{\"item\":\"在线\",\"count\":1}]}";
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	/**
	 * <p>
	 * 描述：阀组全天评价统计饼图json数据
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/groupValveDailyStatisticsData")
	public String groupValveDailyStatisticsData() throws Exception {
		orgId=ParamUtils.getParameter(request, "orgId");
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			User user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		String type = ParamUtils.getParameter(request, "type");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String json = "";
		
		/******
		 * 饼图及柱状图需要的data信息
		 * ***/
		json = dailyEvaluationService.groupValveDailyStatisticsData(orgId,type,wellType,totalDate);
//		json="{ \"success\":true,\"List\":[{\"item\":\"离线\",\"count\":1},{\"item\":\"在线\",\"count\":1}]}";
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**
	 * <P>
	 * 描述:煤层气井全天评价数据表
	 * </p>
	 * 
	 */
	@RequestMapping("/getRealtimeAnalysisWellList")
	public String getRealtimeAnalysisWellList() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String type = ParamUtils.getParameter(request, "type");
		String unitType = ParamUtils.getParameter(request, "unitType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		String tableName="tbl_cbm_total_day";
		if("1".equals(unitType)){
			tableName="tbl_cbm_total_day";
		}else if("2".equals(unitType)){
			tableName="tbl_groupvalve_total_day";
		}else if("3".equals(unitType)){
			tableName="tbl_bp_total_day";
		}
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.calculatedate),'yyyy-mm-dd') from "+tableName+" t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = dailyEvaluationService.getRealtimeAnalysisWellList(orgId, wellName, pager,type,unitType,totalDate,startDate,endDate,statValue);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/exportCBMWellDailyAnalisiDataExcel")
	public String exportCBMWellDailyAnalisiDataExcel() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String type = ParamUtils.getParameter(request, "type");
		String unitType = ParamUtils.getParameter(request, "unitType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		
		String heads = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "heads"),"utf-8");
		String fields = ParamUtils.getParameter(request, "fields");
		String fileName = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "fileName"),"utf-8");
		String title = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "title"),"utf-8");
		String tableName="tbl_cbm_total_day";
		if("1".equals(unitType)){
			tableName="tbl_cbm_total_day";
		}else if("2".equals(unitType)){
			tableName="tbl_groupvalve_total_day";
		}else if("3".equals(unitType)){
			tableName="tbl_bp_total_day";
		}
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.calculatedate),'yyyy-mm-dd') from "+tableName+" t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = dailyEvaluationService.exportCBMWellDailyAnalisiDataExcel(orgId, wellName, pager,type,unitType,totalDate,startDate,endDate,statValue);
		this.service.exportGridPanelData(response,fileName,title, heads, fields,json);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * <P>
	 * 描述:阀组全天评价数据表
	 * </p>
	 * 
	 */
	@RequestMapping("/getGroupValveRealtimeAnalysisWellList")
	public String getGroupValveRealtimeAnalysisWellList() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String type = ParamUtils.getParameter(request, "type");
		String unitType = ParamUtils.getParameter(request, "unitType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		String tableName="tbl_cbm_total_day";
		if("1".equals(unitType)){
			tableName="tbl_cbm_total_day";
		}else if("2".equals(unitType)){
			tableName="tbl_groupvalve_total_day";
		}else if("3".equals(unitType)){
			tableName="tbl_bp_total_day";
		}
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.calculatedate),'yyyy-mm-dd') from "+tableName+" t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = dailyEvaluationService.getGroupValveRealtimeAnalysisWellList(orgId, wellName, pager,type,unitType,totalDate,startDate,endDate,statValue);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/exportGroupValveDailyAnalisiDataExcel")
	public String exportGroupValveDailyAnalisiDataExcel() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String type = ParamUtils.getParameter(request, "type");
		String unitType = ParamUtils.getParameter(request, "unitType");
		String totalDate = ParamUtils.getParameter(request, "totalDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		
		String heads = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "heads"),"utf-8");
		String fields = ParamUtils.getParameter(request, "fields");
		String fileName = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "fileName"),"utf-8");
		String title = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "title"),"utf-8");
		String tableName="tbl_cbm_total_day";
		if("1".equals(unitType)){
			tableName="tbl_cbm_total_day";
		}else if("2".equals(unitType)){
			tableName="tbl_groupvalve_total_day";
		}else if("3".equals(unitType)){
			tableName="tbl_bp_total_day";
		}
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.calculatedate),'yyyy-mm-dd') from "+tableName+" t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = dailyEvaluationService.exportGroupValveDailyAnalisiDataExcel(orgId, wellName, pager,type,unitType,totalDate,startDate,endDate,statValue);
		this.service.exportGridPanelData(response,fileName,title, heads, fields,json);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/getCBMWellDailyDataCurve")
	public String getCBMWellDailyDataCurve() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		this.pager = new Page("pagerForm", request);
		String json =dailyEvaluationService.getCBMWellDailyDataCurve(wellName,selectedWellName,startDate,endDate);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/getGroupValveDailyDataCurve")
	public String getGroupValveDailyDataCurve() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		this.pager = new Page("pagerForm", request);
		String json =dailyEvaluationService.getGroupValveDailyDataCurve(wellName,selectedWellName,startDate,endDate);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/getCBMWellDailyAnalysisAndAcqAndControlData")
	public String getCBMWellDailyAnalysisAndAcqAndControlData() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		String recordId = ParamUtils.getParameter(request, "id");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		this.pager = new Page("pagerForm", request);
		
		String json =dailyEvaluationService.getCBMWellDailyAnalysisAndAcqAndControlData(recordId,wellName,selectedWellName,user.getUserNo());
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/getGroupValveDailyAnalysisAndAcqAndControlData")
	public String getGroupValveDailyAnalysisAndAcqAndControlData() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		String recordId = ParamUtils.getParameter(request, "id");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		this.pager = new Page("pagerForm", request);
		
		String json =dailyEvaluationService.getGroupValveDailyAnalysisAndAcqAndControlData(recordId,wellName,selectedWellName,user.getUserNo());
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping("/getCBMWellDailyHistoryDataCurveData")
	public String getCBMWellDailyHistoryDataCurveData() throws Exception {
		String json = "";
		wellName = ParamUtils.getParameter(request, "wellName");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String itemName = ParamUtils.getParameter(request, "itemName");
		String itemCode = ParamUtils.getParameter(request, "itemCode");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String tableName="viw_cbm_total_day";
		this.pager = new Page("pagerForm", request);
		if(!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from "+tableName+" t  where wellName= '"+wellName+"' ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		if("1".equals(wellType)){//井
			json =  dailyEvaluationService.getCBMWellDailyHistoryDataCurveData(wellName, startDate,endDate,itemName,itemCode);
		}
		
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
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
	
	@RequestMapping("/getGroupValveDailyHistoryDataCurveData")
	public String getGroupValveDailyHistoryDataCurveData() throws Exception {
		String json = "";
		wellName = ParamUtils.getParameter(request, "wellName");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String itemName = ParamUtils.getParameter(request, "itemName");
		String itemCode = ParamUtils.getParameter(request, "itemCode");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String tableName="viw_groupvalve_total_day";
		this.pager = new Page("pagerForm", request);
		if(!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from "+tableName+" t  where wellName= '"+wellName+"' ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		if("1".equals(wellType)){//井
			json =  dailyEvaluationService.getGroupValveDailyHistoryDataCurveData(wellName, startDate,endDate,itemName,itemCode);
		}
		
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
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
	
	@RequestMapping("/wellControlOperation")
	public String WellControlOperation() throws Exception {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		String wellName = request.getParameter("wellName");
		String password = request.getParameter("password");
		String controlType = request.getParameter("controlType");
		String controlValue = request.getParameter("controlValue");
		String jsonLogin = "";
		String clientIP=StringManagerUtils.getIpAddr(request);
		User userInfo = (User) request.getSession().getAttribute("userLogin");
		// 用户不存在
		if (null != userInfo) {
			String getUpwd = userInfo.getUserPwd();
			String getOld = UnixPwdCrypt.crypt("dogVSgod", password);
			if (getOld.equals(getUpwd)&&StringManagerUtils.isNumber(controlValue)) {
				for(int i=0;EquipmentDriverServerTast.units!=null&&i<EquipmentDriverServerTast.units.size();i++){
					if(wellName.equals(EquipmentDriverServerTast.units.get(i).getWellName())){
						
						if("ImmediatelyAcquisition".equalsIgnoreCase(controlType)){//即时采集
							EquipmentDriverServerTast.units.get(i).setImmediatelyAcquisitionControl(StringManagerUtils.stringToInteger(controlValue));
						}
						//煤层气井控制
						else if("startOrStopWell".equalsIgnoreCase(controlType)){//启停井控制
							if(StringManagerUtils.stringToInteger(controlValue)==1){//启抽
								EquipmentDriverServerTast.units.get(i).setWellStopControl(0);
								EquipmentDriverServerTast.units.get(i).setWellStartupControl(1);
							}else if(StringManagerUtils.stringToInteger(controlValue)==2){//停抽
								EquipmentDriverServerTast.units.get(i).setWellStartupControl(0);
								EquipmentDriverServerTast.units.get(i).setWellStopControl(1);
							}
							
							
						}else if("frequencyOrSPMcontrolSign".equalsIgnoreCase(controlType)){//频率/冲次控制方式
							EquipmentDriverServerTast.units.get(i).setFrequencyOrRPMControlSignControl(StringManagerUtils.stringToInteger(controlValue));
						}else if("frequencySetValue".equalsIgnoreCase(controlType)){//频率设定
							EquipmentDriverServerTast.units.get(i).setFrequencySetValueControl(StringManagerUtils.stringToFloat(controlValue));
						}
						else if("SPMSetValue".equalsIgnoreCase(controlType)){//冲次设定
							EquipmentDriverServerTast.units.get(i).setSPMSetValueControl(StringManagerUtils.stringToFloat(controlValue));
						}
						else if("SPMBy10hz".equalsIgnoreCase(controlType)){//10HZ对应冲次值
							EquipmentDriverServerTast.units.get(i).setSPMBy10HzControl(StringManagerUtils.stringToFloat(controlValue));
						}
						else if("SPMBy50hz".equalsIgnoreCase(controlType)){//50HZ对应冲次值
							EquipmentDriverServerTast.units.get(i).setSPMBy50HzControl(StringManagerUtils.stringToFloat(controlValue));
						}
						else if("vfdManufacturerCode".equalsIgnoreCase(controlType)){//设置变频器厂家
							EquipmentDriverServerTast.units.get(i).setVfdManufacture(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("rtuAddr".equalsIgnoreCase(controlType)){//设置RTU地址
							EquipmentDriverServerTast.units.get(i).setRTUAddrControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("rtuProgramVersion".equalsIgnoreCase(controlType)){//设置程序版本号
							EquipmentDriverServerTast.units.get(i).setRTUProgramVersionControl(StringManagerUtils.stringToInteger(controlValue));
						}
						//阀组控制项
						else if("deviceId".equalsIgnoreCase(controlType)){//设置设备地址
							EquipmentDriverServerTast.units.get(i).setGroupValveDeviceIdControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("baudrate".equalsIgnoreCase(controlType)){//设置A1B1口波特率
							EquipmentDriverServerTast.units.get(i).setGroupValveBaudRate1Control(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("baudrate2".equalsIgnoreCase(controlType)){//设置A2B2口波特率
							EquipmentDriverServerTast.units.get(i).setGroupValveBaudRate2Control(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("instrumentCombinationMode1".equalsIgnoreCase(controlType)){//设置仪表组合方式-1#从站
							EquipmentDriverServerTast.units.get(i).setGroupValveInstrumentCombinationMode1Control(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("instrumentCombinationMode2".equalsIgnoreCase(controlType)){//设置仪表组合方式-2#从站
							EquipmentDriverServerTast.units.get(i).setGroupValveInstrumentCombinationMode2Control(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("instrumentCombinationMode3".equalsIgnoreCase(controlType)){//设置仪表组合方式-2#从站
							EquipmentDriverServerTast.units.get(i).setGroupValveInstrumentCombinationMode3Control(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("instrumentCombinationMode4".equalsIgnoreCase(controlType)){//设置仪表组合方式-4#从站
							EquipmentDriverServerTast.units.get(i).setGroupValveInstrumentCombinationMode4Control(StringManagerUtils.stringToInteger(controlValue));
						}
						
						
						
						break;
					}
				}
				jsonLogin = "{success:true,flag:true,error:true,msg:'<font color=blue>命令发送成功。</font>'}";
			}
			else if(getOld.equals(getUpwd) && !StringManagerUtils.isNumber(controlValue)){
				jsonLogin = "{success:true,flag:true,error:false,msg:'<font color=red>数据有误，请检查输入数据！</font>'}";
			}
			else {
				jsonLogin = "{success:true,flag:true,error:false,msg:'<font color=red>您输入的密码有误！</font>'}";
			}

		} else {
			jsonLogin = "{success:true,flag:false}";
		}
		// 处理乱码。
		response.setCharacterEncoding("utf-8");
		// 输出json数据。
		out.print(jsonLogin);
		return null;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getTotals() {
		return totals;
	}
	public void setTotals(int totals) {
		this.totals = totals;
	}
	public String getWellName() {
		return wellName;
	}
	public void setWellName(String wellName) {
		this.wellName = wellName;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
