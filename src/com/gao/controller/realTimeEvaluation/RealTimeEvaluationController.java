package com.gao.controller.realTimeEvaluation;

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
import com.gao.service.realTimeEvaluation.RealTimeEvaluationService;
import com.gao.tast.EquipmentDriverServerTast;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;
import com.gao.utils.UnixPwdCrypt;

@Controller
@RequestMapping("/realTimeEvaluationController")
@Scope("prototype")
public class RealTimeEvaluationController<T> extends BaseController{
	private static final long serialVersionUID = 1L;
	@Autowired
	private CommonDataService service;
	@Autowired
	private RealTimeEvaluationService realTimeEvaluationService;
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
	 * @author zhao 2016-06-23
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
		String json = "";
		
		/******
		 * 饼图及柱状图需要的data信息
		 * ***/
		json = realTimeEvaluationService.statisticsData(orgId,type,wellType);
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
	 * 描述:煤层气井井实时评价数据表
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
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		String tableName="tbl_cbm_discrete_hist";
		if("1".equals(unitType)){
			tableName="tbl_cbm_discrete_hist";
		}else if("2".equals(unitType)){
			tableName="tbl_groupvalve_discrete_hist";
		}else if("3".equals(unitType)){
			tableName="tbl_bp_discrete_hist";
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
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from "+tableName+" t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
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
		
		String json = realTimeEvaluationService.getRealtimeAnalysisWellList(orgId, wellName, pager,type,unitType,startDate,endDate,statValue);
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
	
	@RequestMapping("/getCBMWellAnalysisAndAcqAndControlData")
	public String getCBMWellAnalysisAndAcqAndControlData() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		String recordId = ParamUtils.getParameter(request, "id");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		this.pager = new Page("pagerForm", request);
		
		String json =realTimeEvaluationService.getCBMWellAnalysisAndAcqAndControlData(recordId,wellName,selectedWellName,user.getUserNo());
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
	@RequestMapping("/getWellHistoryDataCurveData")
	public String getWellHistoryDataCurveData() throws Exception {
		String json = "";
		wellName = ParamUtils.getParameter(request, "wellName");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String itemName = ParamUtils.getParameter(request, "itemName");
		String itemCode = ParamUtils.getParameter(request, "itemCode");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String tableName="viw_rpc_diagram_hist";
		if("400".equals(wellType)){//螺杆泵
			tableName="viw_pcp_rpm_hist";
		}
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
			json =  realTimeEvaluationService.getWellHistoryDataCurveData(wellName, startDate,endDate,itemName,itemCode);
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
						if("startOrStopWell".equalsIgnoreCase(controlType)){//启停井控制
							EquipmentDriverServerTast.units.get(i).setWellStopControl(StringManagerUtils.stringToInteger(controlValue));
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
