package com.gao.controller.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gao.controller.base.BaseController;
import com.gao.model.User;
import com.gao.service.base.CommonDataService;
import com.gao.service.report.ReportDataService;
import com.gao.utils.Config;
import com.gao.utils.ConfigFile;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <p>描述：采出井日报表Action</p>
 * 
 * @author gao 2014-06-04
 * 
 */
@Controller
@RequestMapping("/reportDataController")
@Scope("prototype")
public class ReportDataController extends BaseController {
	private static Log log = LogFactory.getLog(ReportDataController.class);
	private static final long serialVersionUID = 1L;
	private String wellName = "";
	private String calculateDate;
	private int limit = 10;
	private String orgId;
	private int page = 1;
	@Autowired
	private CommonDataService commonDataService;
	@Autowired
	private ReportDataService reportDataService;


	/** <p>描述：采出井日报表json数据方法</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/showCBMWellDailyReportData")
	public String showCBMWellDailyReportData() throws Exception {
		log.debug("reportOutputWell enter==");
		orgId = ParamUtils.getParameter(request, "orgId");
		wellName = ParamUtils.getParameter(request, "wellName");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String wellType = ParamUtils.getParameter(request, "wellType");
		this.pager = new Page("pagerForm", request);
		String tableName="tbl_cbm_total_day";
		if (!StringUtils.isNotBlank(orgId)) {
			HttpSession session=request.getSession();
			User user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if (!StringUtils.isNotBlank(endDate)) {
			String sql = " select * from (select  to_char(t.calculateDate,'yyyy-mm-dd') from "+tableName+" t order by calculateDate desc) where rownum=1 ";
			List<?> list = this.commonDataService.findCallSql(sql);
			if (list.size() > 0 && list.get(0)!=null ) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=endDate;
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = "";
		this.pager = new Page("pagerForm", request);
		pager.setJssj(calculateDate);
		json = reportDataService.showCBMWellDailyReportData(pager, orgId, wellName, startDate,endDate,wellType);
		
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** <p>描述：导出煤层气井报表</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/exportCBMWellDailyReportExcelData")
	public String exportCBMWellDailyReportExcelData() throws Exception {
		log.debug("reportOutputWell enter==");
		Vector<String> v = new Vector<String>();
		orgId = ParamUtils.getParameter(request, "orgId");
		wellName = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "wellName"),"utf-8");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String wellType = ParamUtils.getParameter(request, "wellType");
		this.pager = new Page("pagerForm", request);
		String tableName="tbl_cbm_total_day";
		if (!StringUtils.isNotBlank(orgId)) {
			HttpSession session=request.getSession();
			User user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if (!StringUtils.isNotBlank(endDate)) {
			String sql = " select * from (select  to_char(t.calculateDate,'yyyy-mm-dd') from "+tableName+" t order by calculateDate desc) where rownum=1 ";
			List<?> list = this.commonDataService.findCallSql(sql);
			if (list.size() > 0 && list.get(0)!=null ) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=endDate;
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = "";
		this.pager = new Page("pagerForm", request);
		pager.setJssj(calculateDate);
		json = reportDataService.exportCBMWellDailyReportExcelData(pager, orgId, wellName, startDate,endDate,wellType);
		
		JSONObject jsonObject = JSONObject.fromObject("{\"data\":"+json+"}");//解析数据
		JSONArray jsonArray = jsonObject.getJSONArray("data");
		try {
	        /** 
	         * web端生成保存打开excel弹出框 
	         */  
	        response.setContentType("application/x-msdownload;charset=gbk");      
	        String fileName = "煤层气排采井数据表-"+endDate+".xls";
	        response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
	        OutputStream os = response.getOutputStream();     
	        //打开文件    
	        WritableWorkbook book= Workbook.createWorkbook(os);     
	        WritableSheet sheetOne=book.createSheet("煤层气排采井数据表",0);    
	        
	        WritableFont wf_title = new WritableFont(WritableFont.ARIAL, 20,WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.BLACK); // 定义格式 字体 下划线 斜体 粗体 颜色    
	        WritableFont wf_head = new WritableFont(WritableFont.ARIAL, 12,WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.BLACK); // 定义格式 字体 下划线 斜体 粗体 颜色    
	        WritableFont wf_table = new WritableFont(WritableFont.ARIAL, 11,WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.BLACK); // 定义格式 字体 下划线 斜体 粗体 颜色    
	   
	        WritableCellFormat wcf_title = new WritableCellFormat(wf_title); // 单元格定义    
	        wcf_title.setBackground(jxl.format.Colour.WHITE); // 设置单元格的背景颜色    
	        wcf_title.setAlignment(jxl.format.Alignment.CENTRE); // 设置对齐方式    
	        wcf_title.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 设置垂直对齐方式   
	        wcf_title.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN,jxl.format.Colour.BLACK); //设置边框    
	        
	        WritableCellFormat wcf_head = new WritableCellFormat(wf_head);     
	        wcf_head.setBackground(jxl.format.Colour.GRAY_25);    
	        wcf_head.setAlignment(jxl.format.Alignment.CENTRE);     
	        wcf_head.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 设置垂直对齐方式   
	        wcf_head.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN,jxl.format.Colour.BLACK); 
	        wcf_head.setWrap(true);
	   
	        WritableCellFormat wcf_table = new WritableCellFormat(wf_table);    
	        wcf_table.setBackground(jxl.format.Colour.WHITE);     
	        wcf_table.setAlignment(jxl.format.Alignment.CENTRE);     
	        wcf_table.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 设置垂直对齐方式   
	        wcf_table.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN,jxl.format.Colour.BLACK);     
	               
	        sheetOne.setColumnView(0, 15); // 设置列的宽度    
	        
	        sheetOne.setColumnView(1, 15); // 设置列的宽度   
	        
	        sheetOne.setColumnView(2, 15); // 设置列的宽度    
	        sheetOne.setColumnView(3, 15); // 设置列的宽度    
	        sheetOne.setColumnView(4, 20); // 设置列的宽度 
	        
	        sheetOne.setColumnView(5, 15); // 设置列的宽度 
	        sheetOne.setColumnView(6, 15); // 设置列的宽度    
	        sheetOne.setColumnView(7, 15); // 设置列的宽度    
	        sheetOne.setColumnView(8, 15); // 设置列的宽度   
	        sheetOne.setColumnView(9, 15); // 设置列的宽度    
	        sheetOne.setColumnView(10, 15); // 设置列的宽度  
	        sheetOne.setColumnView(11, 15); // 设置列的宽度   
	        
	        sheetOne.setColumnView(12, 15); // 设置列的宽度  
	        sheetOne.setColumnView(13, 15); // 设置列的宽度
	        sheetOne.setColumnView(14, 15); // 设置列的宽度
	        
	        sheetOne.setColumnView(15, 15); // 设置列的宽度
	        sheetOne.setColumnView(16, 15); // 设置列的宽度  
	        sheetOne.setColumnView(17, 15); // 设置列的宽度
	        
	        sheetOne.setColumnView(18, 15); // 设置列的宽度
	        sheetOne.setColumnView(19, 15); // 设置列的宽度
	        sheetOne.setColumnView(20, 15); // 设置列的宽度
	   
	        //在Label对象的构造子中指名单元格位置是第一列第一行(0,0)     
	        //以及单元格内容为test
	        
	        Label title=new Label(0,0,"煤层气排采井数据表",wcf_title);
	        
	        Label header1_1=new Label(0,1,"",wcf_head);
	        Label header1_2=new Label(1,1,"本日生产损耗(m3/d)：",wcf_head);
	        Label header1_3=new Label(8,1,"",wcf_head);
	        Label header1_4=new Label(16,1,"制表人：",wcf_head);
	        Label header1_5=new Label(17,1,"",wcf_head);
	        Label header1_6=new Label(18,1,"",wcf_head);
	        Label header1_7=new Label(19,1,"审核人：",wcf_head);
	        Label header1_8=new Label(20,1,"",wcf_head);
	        
	        
	        Label header2_1=new Label(0,2,"时间",wcf_head);
	        Label header2_2=new Label(1,2,"井号",wcf_head);
	        Label header2_3=new Label(2,2,"运行时间(h)",wcf_head);
	        Label header2_4=new Label(3,2,"运行时率(小数)",wcf_head);
	        Label header2_5=new Label(4,2,"运行区间",wcf_head);
	        Label header2_6=new Label(5,2,"泵径(mm)",wcf_head);
	        Label header2_7=new Label(6,2,"冲程(m)",wcf_head);
	        Label header2_8=new Label(7,2,"冲次(n/min)",wcf_head);
	        Label header2_9=new Label(8,2,"理论排量(m3/d)",wcf_head);
	        Label header2_10=new Label(9,2,"泵效(%)",wcf_head);
	        Label header2_11=new Label(10,2,"动液面(m)",wcf_head);
	        Label header2_12=new Label(11,2,"套压(Mpa)",wcf_head);
	        Label header2_13=new Label(12,2,"3#煤层顶板(m)",wcf_head);
	        Label header2_14=new Label(13,2,"15#煤层顶板(m)",wcf_head);
	        Label header2_15=new Label(14,2,"井底流压(Mpa)",wcf_head);
	        Label header2_16=new Label(15,2,"产水量(m3/d)",wcf_head);
	        Label header2_17=new Label(16,2,"月累计产水量(m3)",wcf_head);
	        Label header2_18=new Label(17,2,"年累计产水量(m3)",wcf_head);
	        Label header2_19=new Label(18,2,"产气量(m3/d)",wcf_head);
	        Label header2_20=new Label(19,2,"月累计产气量(m3)",wcf_head);
	        Label header2_21=new Label(20,2,"年累计产气量(m3)",wcf_head);
	        
	       
	     //或者WritableCell cell =  new jxl.write.Number(column, row, value, wcf)    
           //将定义好的单元格添加到工作表中     
           sheetOne.addCell(title);   
           
           sheetOne.addCell(header1_1);     
           sheetOne.addCell(header1_2);
           sheetOne.addCell(header1_3);     
           sheetOne.addCell(header1_4);  
           sheetOne.addCell(header1_5);     
           sheetOne.addCell(header1_6);
           sheetOne.addCell(header1_7);     
           sheetOne.addCell(header1_8);  
           
           sheetOne.addCell(header2_1);     
           sheetOne.addCell(header2_2);     
           sheetOne.addCell(header2_3); 
           sheetOne.addCell(header2_4);
           sheetOne.addCell(header2_5);     
           sheetOne.addCell(header2_6);   
           sheetOne.addCell(header2_7);  
           sheetOne.addCell(header2_8);
           sheetOne.addCell(header2_9);     
           sheetOne.addCell(header2_10);
           sheetOne.addCell(header2_11);     
           sheetOne.addCell(header2_12);     
           sheetOne.addCell(header2_13); 
           sheetOne.addCell(header2_14);
           sheetOne.addCell(header2_15);     
           sheetOne.addCell(header2_16);   
           sheetOne.addCell(header2_17);  
           sheetOne.addCell(header2_18);
           sheetOne.addCell(header2_19);     
           sheetOne.addCell(header2_20);
           sheetOne.addCell(header2_21);
           
         //合： 第1列第1行  到 第13列第1行     
           sheetOne.mergeCells(0, 0, 20, 0);   
           
           sheetOne.mergeCells(1, 1, 7, 1);     
           sheetOne.mergeCells(8, 1, 15, 1);  
           
           /*动态数据   */ 
           float sumGasTodayProd=0;
           float sumGasMonthProd=0;
           float sumGasYearProd=0;
           
           float sumLiquidFlowmeterProd=0;
           float sumLiquidMonthProd=0;
           float sumLiquidYearProd=0;
           
           for(int i=0;i<=jsonArray.size();i++){
        	   Label content1=null;
        	   Label content2=null;
        	   Label content3=null;
        	   Label content4=null;
        	   Label content5=null;
        	   Label content6=null;
        	   Label content7=null;
        	   Label content8=null;
        	   Label content9=null;
        	   Label content10=null;
        	   Label content11=null;
        	   Label content12=null;
        	   Label content13=null;
        	   Label content14=null;
        	   Label content15=null;
        	   Label content16=null;
        	   Label content17=null;
        	   Label content18=null;
        	   Label content19=null;
        	   Label content20=null;
        	   Label content21=null;
        	   if(i<jsonArray.size()){
        		   JSONObject everydata = JSONObject.fromObject(jsonArray.getString(i));
        		   sumGasTodayProd+=StringManagerUtils.stringToFloat(everydata.getString("gasTodayProd"));
        		   sumGasMonthProd+=StringManagerUtils.stringToFloat(everydata.getString("gasMonthProd"));
        		   sumGasYearProd+=StringManagerUtils.stringToFloat(everydata.getString("gasYearProd"));
        		   sumLiquidFlowmeterProd+=StringManagerUtils.stringToFloat(everydata.getString("liquidFlowmeterProd"));
        		   sumLiquidMonthProd+=StringManagerUtils.stringToFloat(everydata.getString("liquidMonthProd"));
        		   sumLiquidYearProd+=StringManagerUtils.stringToFloat(everydata.getString("liquidYearProd"));
            	   
            	   
            	   content1=new Label(0,i+3,everydata.getString("calculateDate"),wcf_table);
            	   content2=new Label(1,i+3,everydata.getString("wellName"),wcf_table);
            	   
            	   content3=new Label(2,i+3,everydata.getString("runTime"),wcf_table);
            	   content4=new Label(3,i+3,everydata.getString("runTimeEfficiency"),wcf_table);
            	   content5=new Label(4,i+3,everydata.getString("runRange"),wcf_table);
            	   
            	   content6=new Label(5,i+3,"",wcf_table);
            	   content7=new Label(6,i+3,"",wcf_table);
            	   content8=new Label(7,i+3,everydata.getString("spm"),wcf_table);
            	   content9=new Label(8,i+3,"",wcf_table);
            	   content10=new Label(9,i+3,"",wcf_table);
            	   content11=new Label(10,i+3,everydata.getString("fluidLevel"),wcf_table);
            	   content12=new Label(11,i+3,everydata.getString("casingPressure"),wcf_table);
            	   
            	   content13=new Label(12,i+3,"",wcf_table);
            	   content14=new Label(13,i+3,"",wcf_table);
            	   content15=new Label(14,i+3,"",wcf_table);
            	   
            	   content16=new Label(15,i+3,everydata.getString("liquidFlowmeterProd"),wcf_table);
            	   content17=new Label(16,i+3,everydata.getString("liquidMonthProd"),wcf_table);
            	   content18=new Label(17,i+3,everydata.getString("liquidYearProd"),wcf_table);
            	   
            	   content19=new Label(18,i+3,everydata.getString("gasTodayProd"),wcf_table);
            	   content20=new Label(19,i+3,everydata.getString("gasMonthProd"),wcf_table);
            	   content21=new Label(20,i+3,everydata.getString("gasYearProd"),wcf_table);
            	   
        	   }else if(i==jsonArray.size()){
        		   sumGasTodayProd=StringManagerUtils.stringToFloat(sumGasTodayProd+"",2);
        		   sumGasMonthProd=StringManagerUtils.stringToFloat(sumGasMonthProd+"",2);
        		   sumGasYearProd=StringManagerUtils.stringToFloat(sumGasYearProd+"",2);
        		   sumLiquidFlowmeterProd=StringManagerUtils.stringToFloat(sumLiquidFlowmeterProd+"",2);
        		   sumLiquidMonthProd=StringManagerUtils.stringToFloat(sumLiquidMonthProd+"",2);
        		   sumLiquidYearProd=StringManagerUtils.stringToFloat(sumLiquidYearProd+"",2);
        		   
            	   content1=new Label(0,i+3,"合计",wcf_table);
            	   content2=new Label(1,i+3,"",wcf_table);
            	   content3=new Label(2,i+3,"",wcf_table);
            	   content4=new Label(3,i+3,"",wcf_table);
            	   content5=new Label(4,i+3,"",wcf_table);
            	   content6=new Label(5,i+3,"",wcf_table);
            	   content7=new Label(6,i+3,"",wcf_table);
            	   content8=new Label(7,i+3,"",wcf_table);
            	   content9=new Label(8,i+3,"",wcf_table);
            	   content10=new Label(9,i+3,"",wcf_table);
            	   content11=new Label(10,i+3,"",wcf_table);
            	   content12=new Label(11,i+3,"",wcf_table);
            	   content13=new Label(12,i+3,"",wcf_table);
            	   content14=new Label(13,i+3,"",wcf_table);
            	   content15=new Label(14,i+3,"",wcf_table);
            	   
            	   content16=new Label(15,i+3,sumLiquidFlowmeterProd+"",wcf_table);
            	   content17=new Label(16,i+3,sumLiquidMonthProd+"",wcf_table);
            	   content18=new Label(17,i+3,sumLiquidYearProd+"",wcf_table);
            	   content19=new Label(18,i+3,sumGasTodayProd+"",wcf_table);
            	   content20=new Label(19,i+3,sumGasMonthProd+"",wcf_table);
            	   content21=new Label(20,i+3,sumGasYearProd+"",wcf_table);
            	   
        	   }
        	   sheetOne.addCell(content1);
        	   sheetOne.addCell(content2);
        	   sheetOne.addCell(content3);
        	   sheetOne.addCell(content4);
        	   sheetOne.addCell(content5);
        	   sheetOne.addCell(content6);
        	   sheetOne.addCell(content7);
        	   sheetOne.addCell(content8);
        	   sheetOne.addCell(content9);
        	   sheetOne.addCell(content10);
        	   sheetOne.addCell(content11);
        	   sheetOne.addCell(content12);
        	   sheetOne.addCell(content13);
        	   sheetOne.addCell(content14);
        	   sheetOne.addCell(content15);
        	   sheetOne.addCell(content16);
        	   sheetOne.addCell(content17);
        	   sheetOne.addCell(content18);
        	   sheetOne.addCell(content19);
        	   sheetOne.addCell(content20);
        	   sheetOne.addCell(content21);
           }
           sheetOne.mergeCells(0, jsonArray.size()+3, 14, jsonArray.size()+3);  
           //写入数据并关闭文件     
           book.write();     
           book.close();
	    } catch (Exception e) {  
	        // TODO: handle exception  
	        e.printStackTrace();  
	    }
		return null;
	}
	


	public static String formatStringDate(Calendar Month) {
		Month = Calendar.getInstance();
		String time = null;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		time = sd.format(Month.getTime());
		return time;
	}


	
	public String getWellName() {
		return wellName;
	}

	public void setEellName(String wellName) {
		this.wellName = wellName;
	}

	public String getCalculateDate() {
		return calculateDate;
	}

	public void setCalculateDate(String calculateDate) {
		this.calculateDate = calculateDate;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

}
