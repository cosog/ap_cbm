//<!-- 报表——煤层气井 -->
var cbmWellDailyReportHelper=null
Ext.define("AP.view.reportOut.CBMWellDailyReportPanel", {
    extend: 'Ext.panel.Panel',
    alias: 'widget.CBMWellDailyReportPanel',
    layout: 'fit',
    id: 'CBMWellDailyReportPanel_view',
    border: false,
    initComponent: function () {
        var me = this;
//        var ReportPumpUnitDayStore = Ext.create("AP.store.reportOut.ReportPumpUnitDayStore2");
        /** 
         * 定义降序的groupingStore 
         */
        var wellListStore = new Ext.data.JsonStore({
        	pageSize:defaultWellComboxSize,
        	fields: [{
                name: "boxkey",
                type: "string"
            }, {
                name: "boxval",
                type: "string"
            }],
            proxy: {
            	url: context + '/wellInformationManagerController/loadWellComboxList',
                type: "ajax",
                actionMethods: {
                    read: 'POST'
                },
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'totals'
                }
            },
            autoLoad: false,
            listeners: {
                beforeload: function (store, options) {
                	var org_Id = Ext.getCmp('leftOrg_Id').getValue();
                    var wellName = Ext.getCmp('CBMWellDailyReportWellCom_Id').getValue();
                    var new_params = {
                    	wellName: wellName,
                        orgId: org_Id,
                        wellType: 1
                    };
                    Ext.apply(store.proxy.extraParams, new_params);
                }
            }
        });
        var wellListCombo = Ext.create(
            'Ext.form.field.ComboBox', {
                fieldLabel: cosog.string.wellName,
                id: 'CBMWellDailyReportWellCom_Id',
                store: wellListStore,
                labelWidth: 35,
                width: 145,
                queryMode: 'remote',
                emptyText: cosog.string.all,
                blankText: cosog.string.all,
                typeAhead: true,
                autoSelect: false,
                allowBlank: true,
                triggerAction: 'all',
                editable: true,
                displayField: "boxval",
                valueField: "boxkey",
                pageSize:comboxPagingStatus,
                minChars:0,
                listeners: {
                    expand: function (sm, selections) {
//                        wellListCombo.clearValue();
                        wellListCombo.getStore().loadPage(1); // 加载井下拉框的store
                    },
                    select: function (combo, record, index) {
                        try {
                        	CreateCBMWellDailyReportTable();
                        } catch (ex) {
                            Ext.Msg.alert(cosog.string.tips, cosog.string.fail);
                        }
                    }
                }
            });
        Ext.apply(me, {
            tbar: [wellListCombo, {
                xtype: 'datefield',
                anchor: '100%',
                fieldLabel: '',
                labelWidth: 0,
                width: 90,
                hidden:true,
                format: 'Y-m-d ',
                id: 'CBMWellDailyReportStartDate_Id',
                value: new Date(),
                listeners: {
                    select: function (combo, record, index) {
                    	CreateCBMWellDailyReportTable();
                    }
                }
            }, {
                xtype: 'datefield',
                anchor: '100%',
                fieldLabel: '日期',
                labelWidth: 30,
                width: 130,
                format: 'Y-m-d ',
                id: 'CBMWellDailyReportEndDate_Id',
                value: new Date(),
                listeners: {
                    select: function (combo, record, index) {
                    	CreateCBMWellDailyReportTable();
                    }
                }
            }, {
                xtype: 'button',
                text: cosog.string.search,
                pressed: true,
                hidden:true,
                iconCls: 'search',
                handler: function (v, o) {
                	CreateCBMWellDailyReportTable();
                }
            }, {
                xtype: 'button',
                text: cosog.string.exportExcel,
                pressed: true,
                handler: function (v, o) {
                	var leftOrg_Id = obtainParams('leftOrg_Id');
                	var wellName = Ext.getCmp('CBMWellDailyReportWellCom_Id').getValue();
                	var startDate=Ext.getCmp('CBMWellDailyReportStartDate_Id').rawValue;
                    var endDate=Ext.getCmp('CBMWellDailyReportEndDate_Id').rawValue;
                	var url=context + '/reportDataController/exportCBMWellDailyReportExcelData?wellType=200&wellName='+URLencode(URLencode(wellName))+'&startDate='+startDate+'&endDate='+endDate+'&orgId='+leftOrg_Id;
                	document.location.href = url;
                }
            }, '->', {
                id: 'CBMWellDailyReportTotalCount_Id',
                xtype: 'component',
                tpl: cosog.string.totalCount + ': {count}',
                style: 'margin-right:15px'
            }],
            html:'<div class="CBMWellDailyReportContainer" style="width:100%;height:100%;"><div class="con" id="CBMWellDailyReportDiv_id"></div></div>',
            listeners: {
                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                	CreateCBMWellDailyReportTable();
                }
            }
        });
        me.callParent(arguments);
    }
});

function CreateCBMWellDailyReportTable(){
	var orgId = Ext.getCmp('leftOrg_Id').getValue();
    var wellName = Ext.getCmp('CBMWellDailyReportWellCom_Id').getValue();
    var startDate=Ext.getCmp('CBMWellDailyReportStartDate_Id').rawValue;
    var endDate=Ext.getCmp('CBMWellDailyReportEndDate_Id').rawValue;
	Ext.Ajax.request({
		method:'POST',
		url:context + '/reportDataController/showCBMWellDailyReportData',
		success:function(response) {
			var result =  Ext.JSON.decode(response.responseText);
			cbmWellDailyReportHelper = CBMWellDailyReportHelper.createNew("CBMWellDailyReportDiv_id","CBMWellDailyReportContainer");
			cbmWellDailyReportHelper.getData(result);
			cbmWellDailyReportHelper.createTable();
			Ext.getCmp("CBMWellDailyReportTotalCount_Id").update({count: result.totalCount});
		},
		failure:function(){
			Ext.MessageBox.alert("错误","与后台联系的时候出了问题");
		},
		params: {
			orgId: orgId,
			wellName: wellName,
			startDate:startDate,
            endDate:endDate,
            wellType:1
        }
	});
};


var CBMWellDailyReportHelper = {
	    createNew: function (divid, containerid) {
	        var cbmWellDailyReportHelper = {};
	        cbmWellDailyReportHelper.get_data = {};
	        cbmWellDailyReportHelper.hot = '';
	        cbmWellDailyReportHelper.container = document.getElementById(divid);
	        cbmWellDailyReportHelper.last_index = 0;
	        cbmWellDailyReportHelper.calculation_type_computer = [];
	        cbmWellDailyReportHelper.calculation_type_not_computer = [];
	        cbmWellDailyReportHelper.editable = 0;
	        cbmWellDailyReportHelper.sum = 0;
	        cbmWellDailyReportHelper.editRecords = [];
	        
	        cbmWellDailyReportHelper.my_data = [
	    ['煤层气排采井数据表', '', '', '', '', '', '', '', '', '',  '', '', '', '', '', '', '', '', '', '', ''],
	    ['','本日生产损耗(m3/d)：', '','', '', '', '', '','', '', '',  '', '', '', '', '', '制表人：', '', '', '审核人：', ''],
	    ['时间', '井号','运行时间(h)','运行时率(小数)','运行区间', '泵径(mm)','冲程(m)','冲次(n/min)', '理论排量(m3/d)','泵效(%)', '动液面(m)','套压(Mpa)','3#煤层顶板(m)','15#煤层顶板(m)','井底流压(Mpa)','产水量(m3/d)','月累计产水量(m3)', '年累计产水量(m3)', '产气量(m3/d)', '月累计产气量(m3)','年累计产气量(m3)'],
	    ['合计', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '']
	  ];
	        cbmWellDailyReportHelper.updateArray = function () {
	            for (var i = 0; i < cbmWellDailyReportHelper.sum; i++) {
	                cbmWellDailyReportHelper.my_data.splice(i + 3, 0, ['', '', '', '', '', '', '', '', '', '',  '', '', '', '', '', '', '', '', '', '', '']);
	            }
	        }
	        cbmWellDailyReportHelper.clearArray = function () {
	            cbmWellDailyReportHelper.hot.loadData(cbmWellDailyReportHelper.table_header);

	        }

	        cbmWellDailyReportHelper.addBoldBg = function (instance, td, row, col, prop, value, cellProperties) {
	            Handsontable.renderers.TextRenderer.apply(this, arguments);
	            td.style.backgroundColor = 'rgb(242, 242, 242)';
	            if (row <= 2&&row>=1) {
	                td.style.fontWeight = 'bold';
					td.style.fontSize = '13px';
					td.style.color = 'rgb(0, 0, 51)';
					td.style.fontFamily = 'SimSun';//SimHei-黑体 SimSun-宋体
	            }
	        }
			
			cbmWellDailyReportHelper.addSizeBg = function (instance, td, row, col, prop, value, cellProperties) {
	             Handsontable.renderers.TextRenderer.apply(this, arguments);
	            if (row < 1) {
	                td.style.fontWeight = 'bold';
			        td.style.fontSize = '25px';
			        td.style.fontFamily = 'SimSun';
			        td.style.height = '50px';   
			    }      
	        }
			
			cbmWellDailyReportHelper.addColBg = function (instance, td, row, col, prop, value, cellProperties) {
	             Handsontable.renderers.TextRenderer.apply(this, arguments);
	             td.style.backgroundColor = 'rgb(242, 242, 242)';
		         if(row < 3){
	                 td.style.fontWeight = 'bold';
			         td.style.fontSize = '5px';
			         td.style.fontFamily = 'SimHei';
	            }      
	        }
			

	        cbmWellDailyReportHelper.addBgBlue = function (instance, td, row, col, prop, value, cellProperties) {
	            Handsontable.renderers.TextRenderer.apply(this, arguments);
	            td.style.backgroundColor = 'rgb(183, 222, 232)';
	        }

	        cbmWellDailyReportHelper.addBgGreen = function (instance, td, row, col, prop, value, cellProperties) {
	            Handsontable.renderers.TextRenderer.apply(this, arguments);
	            td.style.backgroundColor = 'rgb(216, 228, 188)';
	        }
	        
	        cbmWellDailyReportHelper.hiddenColumn = function (instance, td, row, col, prop, value, cellProperties) {
	            Handsontable.renderers.TextRenderer.apply(this, arguments);
	            td.style.display = 'none';
	        }

	        // 实现标题居中
	        cbmWellDailyReportHelper.titleCenter = function () {
	            $(containerid).width($($('.wtHider')[0]).width());
	        }

	        cbmWellDailyReportHelper.createTable = function () {
	            cbmWellDailyReportHelper.container.innerHTML = "";
	            cbmWellDailyReportHelper.hot = new Handsontable(cbmWellDailyReportHelper.container, {
	                data: cbmWellDailyReportHelper.my_data,
	                fixedRowsTop:3, //固定顶部多少行不能垂直滚动
	                fixedRowsBottom: 1,//固定底部多少行不能垂直滚动
//	                fixedColumnsLeft:1, //固定左侧多少列不能水平滚动
	                rowHeaders: false,
	                colHeaders: false,
					rowHeights: [50],
					colWidths:[75,90, 80,80,120, 50,50,60, 80,50,70,60, 80,90,70, 60,100,100, 60,100,100],
					stretchH: 'all',
	                mergeCells: [
	                    {
	                        "row": 0,
	                        "col": 0,
	                        "rowspan": 1,
	                        "colspan": 21
	                    },{
	                        "row": 1,
	                        "col": 1,
	                        "rowspan": 1,
	                        "colspan": 7
	                    },{
	                        "row": 1,
	                        "col": 8,
	                        "rowspan": 1,
	                        "colspan": 8
	                    },{
	                        "row": cbmWellDailyReportHelper.last_index,
	                        "col": 0,
	                        "rowspan": 1,
	                        "colspan": 15
	                    }],
	                cells: function (row, col, prop) {
	                    var cellProperties = {};
	                    var visualRowIndex = this.instance.toVisualRow(row);
	                    var visualColIndex = this.instance.toVisualColumn(col);

	                    cellProperties.readOnly = true;
	                    // 表头
	                    if (visualRowIndex <= 2 && visualRowIndex >= 1) {
	                        cellProperties.renderer = cbmWellDailyReportHelper.addBoldBg;
	                    }
	                    
	                    // 合计
//	                    if (visualRowIndex ==cbmWellDailyReportHelper.last_index) {
//	                        cellProperties.renderer = cbmWellDailyReportHelper.addBoldBg;
//	                    }
						
						if (visualRowIndex < 1 ) {
	                       cellProperties.renderer = cbmWellDailyReportHelper.addSizeBg;
	                    }
						
						if (visualColIndex === 26&&visualRowIndex>2&&visualRowIndex<cbmWellDailyReportHelper.last_index) {
							cellProperties.readOnly = false;
		                }
						
						
	                    return cellProperties;
	                },
	                columnSummary: [
	                    {
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 15,
	                        type: 'sum',
	                        forceNumeric: true
	                    },
	                    {
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 16,
	                        type: 'sum',
	                        forceNumeric: true
	                    },
	                    {
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 17,
	                        type: 'sum',
	                        forceNumeric: true
	                    },
						{
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 18,
	                        type: 'sum',
	                        forceNumeric: true
						},
						{
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 19,
	                        type: 'sum',
	                        forceNumeric: true
						},{
	                        destinationRow: cbmWellDailyReportHelper.last_index,
	                        destinationColumn: 20,
	                        type: 'sum',
	                        forceNumeric: true
						}
	                ],
	                afterChange:function(changes, source){}
	            });
	        }



	        cbmWellDailyReportHelper.getData = function (data) {
	            cbmWellDailyReportHelper.get_data = data;
	            cbmWellDailyReportHelper.editable = +data.Editable;
	            var _daily = data.totalRoot;
	            cbmWellDailyReportHelper.sum = _daily.length;
	            cbmWellDailyReportHelper.updateArray();
	            _daily.forEach(function (_day, index) {

	                cbmWellDailyReportHelper.my_data[index + 3][0] = _day.calculateDate;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][1] = _day.wellName;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][2] = _day.runTime;
	                cbmWellDailyReportHelper.my_data[index + 3][3] = _day.runTimeEfficiency;
	                var runRange=_day.runRange;
	                if(runRange.length>12){
	                	runRange=runRange.substring(0, 11)+"...";
	                }
	                cbmWellDailyReportHelper.my_data[index + 3][4] = runRange;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][7] = _day.spm;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][10] = _day.fluidLevel;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][11] = _day.casingPressure;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][15] = _day.liquidFlowmeterProd;
	                cbmWellDailyReportHelper.my_data[index + 3][16] = _day.liquidMonthProd;
	                cbmWellDailyReportHelper.my_data[index + 3][17] = _day.liquidYearProd;
	                
	                cbmWellDailyReportHelper.my_data[index + 3][18] = _day.gasTodayProd;
	                cbmWellDailyReportHelper.my_data[index + 3][19] = _day.gasMonthProd;
	                cbmWellDailyReportHelper.my_data[index + 3][20] = _day.gasYearProd;
	            })

	            var _total = data.totalCount;
	            cbmWellDailyReportHelper.last_index = _daily.length + 3;
	        }

	        var init = function () {
	        }

	        init();
	        return cbmWellDailyReportHelper;
	    }
	};