/*******************************************************************************
 * 煤层气井实时评价视图
 *
 *
 */
Ext.define("AP.view.RealTimeEvaluation.WellRealTimeEvaluationView", {
    extend: 'Ext.panel.Panel',
    alias: 'widget.wellRealTimeEvaluationView', // 定义别名
    layout: 'fit',
    border: false,
    initComponent: function () {
        var me = this;
        var wellCombStore = new Ext.data.JsonStore({
            pageSize: defaultWellComboxSize,
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
            autoLoad: true,
            listeners: {
                beforeload: function (store, options) {
                    var org_Id = Ext.getCmp('leftOrg_Id').getValue();
                    var wellName = Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
                    var new_params = {
                    	wellName: wellName,
                        orgId: org_Id,
                        wellType: 1
                    };
                    Ext.apply(store.proxy.extraParams, new_params);
                }
            }
        });
        var wellComb = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: cosog.string.wellName,
            id: "CBMWellRealtimeAnalysisWellCom_Id",
            store: wellCombStore,
            labelWidth: 35,
            width: 125,
            queryMode: 'remote',
            typeAhead: true,
            autoSelect: false,
            editable: true,
            triggerAction: 'all',
            displayField: "boxval",
            emptyText: cosog.string.all,
            blankText: cosog.string.all,
            valueField: "boxkey",
            pageSize: comboxPagingStatus,
            minChars: 0,
            multiSelect: false,
            listeners: {
                expand: function (sm, selections) {},
                select: function (combo, record, index) {
                	var statPanelId=getCBMWellSingleStatType().piePanelId;;
                    if(combo.value==""){
                    	Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("统计数据");
                    	Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").hide();
                  	  	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").hide();
                        Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").show();
                  	  	Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").hide();
                    	Ext.getCmp(statPanelId).expand(true);
                    }else{
                    	Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("历史数据");
            			Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").show();
                    	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").show();
                    	Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").hide();
                        Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").show();
                    	Ext.getCmp(statPanelId).collapse();
                    }
                    Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                }
            }
        });
        Ext.apply(me, {
            items: [{
                layout: "border",
                border: false,
                items: [
                    {
                        region: 'center',
                        layout: 'fit',
                        id: 'CBMWellRealtimeAnalysisWellListPanel_Id',
                        title: '统计数据',
                        border: false,
                        tbar: [wellComb, '-',{
                            id: 'CBMWellSingleDetailsSelectedStatValue_Id',//选择的统计项的值
                            xtype: 'textfield',
                            value: '',
                            hidden: true
                        }, {
                            id: 'CBMWellRealtimeTableColumnStr_Id',//选择查看曲线的数据项代码
                            xtype: 'textfield',
                            value: '',
                            hidden: true
                        },{
                            id: 'CBMWellAnalysisCurveItem_Id',//选择查看曲线的数据项名称
                            xtype: 'textfield',
                            value: '',
                            hidden: true
                        }, {
                            id: 'CBMWellAnalysisCurveItemCode_Id',//选择查看曲线的数据项代码
                            xtype: 'textfield',
                            value: '',
                            hidden: true
                        }, {
                            xtype: 'datefield',
                            anchor: '100%',
                            hidden: true,
                            fieldLabel: '',
                            labelWidth: 0,
                            width: 90,
                            format: 'Y-m-d ',
                            id: 'CBMWellRealtimeAnalysisStartDate_Id',
                            value: '',
                            listeners: {
                                select: function (combo, record, index) {
                                	Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                                }
                            }
                        }, {
                            xtype: 'datefield',
                            anchor: '100%',
                            hidden: true,
                            fieldLabel: '至',
                            labelWidth: 15,
                            width: 105,
                            format: 'Y-m-d ',
                            id: 'CBMWellRealtimeAnalysisEndDate_Id',
                            value: new Date(),
                            listeners: {
                                select: function (combo, record, index) {
                                	Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                                }
                            }
                        }, {
                            xtype: 'button',
                            text: cosog.string.exportExcel,
                            pressed: true,
                            handler: function (v, o) {
                            	exportCBMWellRTAnalisiDataExcel();
                            }
                        }, '->', {
                            xtype: 'button',
                            text: '查看历史',
                            tooltip: '点击按钮或者双击表格，查看单井历史数据',
                            id: 'CBMWellRealtimeAnalysisHisBtn_Id',
                            pressed: true,
                            hidden: false,
                            handler: function (v, o) {
                            	var statPanelId=getCBMWellSingleStatType().piePanelId;
                            	Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("历史数据");
                    			Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").show();
                            	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").show();
                            	Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").hide();
                                Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").show();
                            	Ext.getCmp(statPanelId).collapse();
                                
                                var wellName  = Ext.getCmp("CBMWellAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
                                Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setValue(wellName);
                                Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setRawValue(wellName);
                                Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                            }
                      }, {
                            xtype: 'button',
                            text: '返回统计',
                            id: 'CBMWellRealtimeAnalysisAllBtn_Id',
                            pressed: true,
                            hidden: true,
                            handler: function (v, o) {
                            	var statPanelId=getCBMWellSingleStatType().piePanelId;
                            	Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("统计数据");
                            	Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").hide();
                          	  	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").hide();
                                Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").show();
                          	  	Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").hide();
                            	Ext.getCmp(statPanelId).expand(true);
                                
                                Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setValue('');
                                Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setRawValue('');
                                Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                            }
                      }, {
                            id: 'CBMWellRealtimeAnalysisCount_Id',
                            xtype: 'component',
                            hidden: true,
                            tpl: cosog.string.totalCount + ': {count}',
                            style: 'margin-right:15px'
                    }],
                        items: {
                            xtype: 'tabpanel',
                            id: 'CBMWellSingleDetailsStatTabpanel_Id',
                            activeTab: 0,
                            border: true,
                            header: false,
                            collapsible: true, // 是否折叠
                            split: true, // 竖折叠条
                            tabPosition: 'top',
                            items: [{
                                    xtype: 'tabpanel',
                                    tabPosition: 'right',
                                    title: '时率',
                                    iconCls: 'select',
                                    id: 'CBMWellSingleRunTimeEffStatTabpanel_Id',
                                    tabRotation: 1,
                                    items: [{
                                        title: '运行状态',
                                        border: false,
                                        iconCls: 'dtgreen',
                                        layout: 'border',
                                        id: 'CBMWellSingleRunStatusStatPanel_Id',
                                        items: [{
                                            region: 'center',
                                            id: 'CBMWellSingleRunStatusDataListPanel_Id',
                                            header: false,
                                            layout: 'fit'
                                        }, {
                                            region: 'south',
                                            id: 'CBMWellSingleRunStatusStatGraphPanel_Id',
                                            height: '50%',
                                            border: true,
                                            header: false,
                                            collapsible: true, // 是否折叠
                                            split: true, // 竖折叠条
                                            html: '<div id="CBMWellSingleRunStatusStatGraphPieDiv_Id" style="width:100%;height:100%;"></div>',
                                            listeners: {
                                                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                                                	if ($("#CBMWellSingleRunStatusStatGraphPieDiv_Id").highcharts() != undefined) {
                                                        $("#CBMWellSingleRunStatusStatGraphPieDiv_Id").highcharts().setSize(adjWidth, adjHeight, true);
                                                    }else{
                                                    	Ext.create('Ext.tip.ToolTip', {
                                                            target: 'CBMWellSingleRunStatusStatGraphPieDiv_Id',
                                                            html: '点击饼图不同区域或标签，查看相应统计数据'
                                                        });
                                                    }
                                                }
                                            }
                                        }]
                                    }, {
                                        title: '运行时率',
                                        border: false,
                                        layout: 'border',
                                        id: 'CBMWellSingleRunTimeEffStatPanel_Id',
                                        items: [{
                                            region: 'center',
                                            id: 'CBMWellSingleRunTimeEffDataListPanel_Id',
                                            header: false,
                                            layout: 'fit'
                                        }, {
                                            region: 'south',
                                            id: 'CBMWellSingleRunTimeEffStatGraphPanel_Id',
                                            height: '50%',
                                            border: true,
                                            header: false,
                                            collapsible: true, // 是否折叠
                                            split: true, // 竖折叠条
                                            html: '<div id="CBMWellSingleRunTimeEffStatGraphPieDiv_Id" style="width:100%;height:100%;"></div>',
                                            listeners: {
                                                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                                                	if ($("#CBMWellSingleRunTimeEffStatGraphPieDiv_Id").highcharts() != undefined) {
                                                        $("#CBMWellSingleRunTimeEffStatGraphPieDiv_Id").highcharts().setSize(adjWidth, adjHeight, true);
                                                    }else{
                                                    	Ext.create('Ext.tip.ToolTip', {
                                                            target: 'CBMWellSingleRunTimeEffStatGraphPieDiv_Id',
                                                            html: '点击饼图不同区域或标签，查看相应统计数据'
                                                        });
                                                    }
                                                }
                                            }
                                        }]
                                    }],
                                    listeners: {
                                        tabchange: function (tabPanel, newCard, oldCard, obj) {
                                            newCard.setIconCls("dtgreen");
                                            oldCard.setIconCls("");
                                            loadCBMWellSingleStatData();
                                        }
                                    }
                                },{
                                    xtype: 'tabpanel',
                                    tabPosition: 'right',
                                    title: '通信',
                                    id: 'CBMWellSingleCommEffStatTabpanel_Id',
                                    tabRotation: 1,
                                    items: [{
                                        title: '通信状态',
                                        border: false,
                                        iconCls: 'dtgreen',
                                        layout: 'border',
                                        id: 'CBMWellSingleCommStatusStatPanel_Id',
                                        items: [{
                                            region: 'center',
                                            id: 'CBMWellSingleCommStatusDataListPanel_Id',
                                            header: false,
                                            layout: 'fit'
                                        }, {
                                            region: 'south',
                                            id: 'CBMWellSingleCommStatusStatGraphPanel_Id',
                                            height: '50%',
                                            border: true,
                                            header: false,
                                            collapsible: true, // 是否折叠
                                            split: true, // 竖折叠条
                                            html: '<div id="CBMWellSingleCommStatusStatGraphPieDiv_Id" style="width:100%;height:100%;"></div>',
                                            listeners: {
                                                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                                                	if ($("#CBMWellSingleCommStatusStatGraphPieDiv_Id").highcharts() != undefined) {
                                                        $("#CBMWellSingleCommStatusStatGraphPieDiv_Id").highcharts().setSize(adjWidth, adjHeight, true);
                                                    }else{
                                                    	Ext.create('Ext.tip.ToolTip', {
                                                            target: 'CBMWellSingleCommStatusStatGraphPieDiv_Id',
                                                            html: '点击饼图不同区域或标签，查看相应统计数据'
                                                        });
                                                    }
                                                }
                                            }
                                        }]
                                    }, {
                                        title: '在线时率',
                                        border: false,
                                        layout: 'border',
                                        id: 'CBMWellSingleCommEffStatPanel_Id',
                                        items: [{
                                            region: 'center',
                                            id: 'CBMWellSingleCommEffDataListPanel_Id',
                                            header: false,
                                            layout: 'fit'
                                        }, {
                                            region: 'south',
                                            id: 'CBMWellSingleCommEffStatGraphPanel_Id',
                                            height: '50%',
                                            border: true,
                                            header: false,
                                            collapsible: true, // 是否折叠
                                            split: true, // 竖折叠条
                                            html: '<div id="CBMWellSingleCommEffStatGraphPieDiv_Id" style="width:100%;height:100%;"></div>',
                                            listeners: {
                                                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                                                	if ($("#CBMWellSingleCommEffStatGraphPieDiv_Id").highcharts() != undefined) {
                                                        $("#CBMWellSingleCommEffStatGraphPieDiv_Id").highcharts().setSize(adjWidth, adjHeight, true);
                                                    }else{
                                                    	Ext.create('Ext.tip.ToolTip', {
                                                            target: 'CBMWellSingleCommEffStatGraphPieDiv_Id',
                                                            html: '点击饼图不同区域或标签，查看相应统计数据'
                                                        });
                                                    }
                                                }
                                            }
                                        }]
                                    }],
                                    listeners: {
                                        tabchange: function (tabPanel, newCard, oldCard, obj) {
                                            newCard.setIconCls("dtgreen");
                                            oldCard.setIconCls("");
                                            loadCBMWellSingleStatData();
                                        }
                                    }
                                }
                            ],
                            listeners: {
                                tabchange: function (tabPanel, newCard, oldCard, obj) {
                                    oldCard.setIconCls("");
                                    newCard.setIconCls("select");
                                    loadCBMWellSingleStatData();
                                }
                            }
                        },
                        listeners: {
                            afterrender: function (comp, eOpts) {
                                Ext.getCmp("CBMWellSingleRunTimeEffStatTabpanel_Id").getTabBar().insert(0, {
                                    xtype: 'tbfill'
                                });
                                Ext.getCmp("CBMWellSingleCommEffStatTabpanel_Id").getTabBar().insert(0, {
                                    xtype: 'tbfill'
                                });
                            }
                        }
                },
                    {
                        region: 'east',
                        id: 'CBMWellRealtimeAnalysisDataPanel_Id',
                        width: '65%',
                        title: '单井数据',
                        collapsible: true, // 是否折叠
                        split: true, // 竖折叠条
                        border: false,
                        layout: {
                            type: 'hbox',
                            pack: 'start',
                            align: 'stretch'
                        },
                        items: [
                            {
                                border: false,
                                flex: 2,
                                height: 900,
                                margin: '0 0 0 0',
                                padding: 0,
                                autoScroll: true,
                                scrollable: true,
                                hidden:true,
                                layout: {
                                    type: 'hbox',
                                    pack: 'start',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        border: false,
                                        margin: '0 0 0 0',
                                        flex: 1,
                                        height: 900,
                                        autoScroll: true,
                                        scrollable: true,
                                        layout: {
                                            type: 'vbox',
                                            pack: 'start',
                                            align: 'stretch'
                                        },
                                        items: [{
                                            border: false,
                                            margin: '0 0 1 0',
                                            height: 1000,
                                            layout: 'fit',
                                            id: 'CBMWellRTCurveDataPanel_Id',
                                            html: '<div id="CBMWellRTCurveDataChartDiv_Id" style="width:100%;height:100%;"></div>',
                                            listeners: {
                                                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                                                	if ($("#CBMWellRTCurveDataChartDiv_Id").highcharts() != undefined) {
                                                        $("#CBMWellRTCurveDataChartDiv_Id").highcharts().setSize(adjWidth, adjHeight, true);
                                                    }
                                                }
                                            }
                                     }]
                                 }
                                ]
                            },
                            {
                                border: false,
                                flex: 1,
                                height: 900,
                                margin: '0 0 0 0',
                                padding: 0,
                                autoScroll: true,
                                scrollable: true,
                                collapsible: true,
                                header: false,
                                collapseDirection: 'right',
                                split: true,
                                layout: {
                                    type: 'hbox',
                                    pack: 'start',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'tabpanel',
                                        id: 'CBMWellRealtimeAnalysisAndAcqDataTabpanel_Id',
                                        flex: 1,
                                        activeTab: 1,
                                        header: false,
                                        collapsible: true,
                                        split: true,
                                        collapseDirection: 'right',
                                        border: true,
                                        tabPosition: 'top',
                                        items: [
                                            {
                                                title: '分析',
                                                id: 'CBMWellRTAnalysisTableCalDataPanel_Id',
                                                border: false,
                                                layout: 'fit',
                                                autoScroll: true,
                                                hidden:true,
                                                scrollable: true
                                            }, {
                                                title: '采集',
                                                id: 'CBMWellRTAnalysisTableAcqDataPanel_Id',
                                                border: false,
                                                layout: 'fit',
                                                autoScroll: true,
                                                scrollable: true
                                            },{
                                				title:'控制',
                                				border: false,
                                                layout: 'border',
                                                hideMode:'offsets',
                                                id:'CBMWellRTAnalysisTableControlDataPanel_Id',
                                                items: [{
                                                	region: 'center',
                                                    height: '60%',
                                                    id:'CBMWellRTAnalysisControlDataPanel_Id',
                                    				border: false,
                                    				autoScroll:true,
                                                    scrollable: true,
                                                    layout: 'fit',
                                                    listeners: {
                                                    	resize: function (abstractcomponent, adjWidth, adjHeight, options) {}
                                                    }
                                                }]
                                			}],
                                			listeners: {
                                            	tabchange: function (tabPanel, newCard, oldCard,obj) {}
                                            }
                                    }
                                ]
                            }
                        ],
                        listeners: {
                            beforeCollapse: function (panel, eOpts) {
                                $("#CBMWellRTCurveDataChartDiv_Id").hide();
                            },
                            expand: function (panel, eOpts) {
                                $("#CBMWellRTCurveDataChartDiv_Id").show();
                            }
                        }
                        }]
                }]
        });
        me.callParent(arguments);
    }
});

function createCBMWellRealtimeTableColumn(columnInfo) {
    var myArr = columnInfo;

    var myColumns = "[";
    for (var i = 0; i < myArr.length; i++) {
        var attr = myArr[i];
        var width_ = "";
        var lock_ = "";
        var hidden_ = "";
        if (attr.hidden == true) {
            hidden_ = ",hidden:true";
        }
        if (isNotVal(attr.lock)) {
            //lock_ = ",locked:" + attr.lock;
        }
        if (isNotVal(attr.width)) {
            width_ = ",width:" + attr.width;
        }
        myColumns += "{text:'" + attr.header + "',lockable:true,align:'center' ";
        if (attr.dataIndex.toUpperCase() == 'workingConditionName'.toUpperCase()) {
            myColumns += ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceColor(value,o,p,e);}";
        } else if (attr.dataIndex.toUpperCase()=='workingConditionName_Elec'.toUpperCase()||attr.dataIndex.toUpperCase()=='workingConditionName_E'.toUpperCase()) {
            myColumns += ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceElecWorkingConditionColor(value,o,p,e);}";
        } else if (attr.dataIndex.toUpperCase()=='commStatusName'.toUpperCase()) {
            myColumns += ",width:" + attr.width + ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceCommStatusColor(value,o,p,e);}";
        } else if (attr.dataIndex.toUpperCase()=='runStatusName'.toUpperCase()) {
            myColumns += ",width:" + attr.width + ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceRunStatusColor(value,o,p,e);}";
        } else if (attr.dataIndex.toUpperCase()=='iDegreeBalanceName'.toUpperCase()) {
            myColumns += ",width:" + attr.width + ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceBalanceStatusColor(value,o,p,e);}";
        } else if (attr.dataIndex.toUpperCase()=='wattDegreeBalanceName'.toUpperCase()) {
            myColumns += ",width:" + attr.width + ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return advicePowerBalanceStatusColor(value,o,p,e);}";
        } else if (attr.dataIndex == 'id') {
            myColumns += ",width:" + attr.width + ",xtype: 'rownumberer',sortable : false,locked:true";
        } else if (attr.dataIndex.toUpperCase()=='wellName'.toUpperCase()) {
            myColumns += width_ + ",sortable : false,locked:true,dataIndex:'" + attr.dataIndex + "',renderer:function(value){return \"<span data-qtip=\"+(value==undefined?\"\":value)+\">\"+(value==undefined?\"\":value)+\"</span>\";}";
        } else if (attr.dataIndex.toUpperCase() == 'acquisitionTime'.toUpperCase()) {
            myColumns += width_ + ",sortable : false,locked:false,dataIndex:'" + attr.dataIndex + "',renderer:function(value,o,p,e){return adviceTimeFormat(value,o,p,e);}";
        } else {
            myColumns += hidden_ + lock_ + width_ + ",sortable : false,dataIndex:'" + attr.dataIndex + "',renderer:function(value){return \"<span data-qtip=\"+(value==undefined?\"\":value)+\">\"+(value==undefined?\"\":value)+\"</span>\";}";
            //        	myColumns += hidden_ + lock_ + width_ + ",sortable : false,dataIndex:'" + attr.dataIndex + "'";
        }
        myColumns += "}";
        if (i < myArr.length - 1) {
            myColumns += ",";
        }
    }
    myColumns += "]";
    return myColumns;
};

function getCBMWellSingleStatType() {
	var type=1;
	panelId="CBMWellSingleRunStatusDataListPanel_Id";
	piePanelId="CBMWellSingleRunStatusStatGraphPanel_Id";
	pieDivId="CBMWellSingleRunStatusStatGraphPieDiv_Id";
	pieChartTitle="运行状态";
	exportExcelTitle='煤层气井实时评价-运行状态';
	var activeTabId= Ext.getCmp(Ext.getCmp("CBMWellSingleDetailsStatTabpanel_Id").getActiveTab().id).getActiveTab().id;
	if(activeTabId=="CBMWellSingleRunStatusStatPanel_Id"){//运行状态
		type=1;
		panelId="CBMWellSingleRunStatusDataListPanel_Id";
		piePanelId="CBMWellSingleRunStatusStatGraphPanel_Id";
		pieDivId="CBMWellSingleRunStatusStatGraphPieDiv_Id";
		pieChartTitle="运行状态";
		exportExcelTitle='煤层气井实时评价-运行状态';
	}else if(activeTabId=="CBMWellSingleRunTimeEffStatPanel_Id"){//运行时率
		type=2;
		panelId="CBMWellSingleRunTimeEffDataListPanel_Id";
		piePanelId="CBMWellSingleRunTimeEffStatGraphPanel_Id";
		pieDivId="CBMWellSingleRunTimeEffStatGraphPieDiv_Id";
		pieChartTitle="运行时率";
		exportExcelTitle='煤层气井实时评价-运行时率';
	}else if(activeTabId=="CBMWellSingleCommStatusStatPanel_Id"){//通信状态
		type=3;
		panelId="CBMWellSingleCommStatusDataListPanel_Id";
		piePanelId="CBMWellSingleCommStatusStatGraphPanel_Id";
		pieDivId="CBMWellSingleCommStatusStatGraphPieDiv_Id";
		pieChartTitle="通信状态";
		exportExcelTitle='煤层气井实时评价-通信状态';
	}else if(activeTabId=="CBMWellSingleCommEffStatPanel_Id"){//在线时率
		type=4;
		panelId="CBMWellSingleCommEffDataListPanel_Id";
		piePanelId="CBMWellSingleCommEffStatGraphPanel_Id";
		pieDivId="CBMWellSingleCommEffStatGraphPieDiv_Id";
		pieChartTitle="在线时率";
		exportExcelTitle='煤层气井实时评价-在线时率';
	}
	var result=Ext.JSON.decode("{\"type\":"+type+",\"panelId\":\""+panelId+"\",\"piePanelId\":\""+piePanelId+"\",\"pieDivId\":\""+pieDivId+"\",\"pieChartTitle\":\""+pieChartTitle+"\",\"exportExcelTitle\":\""+exportExcelTitle+"\"}");
	return result;
}

function initCBMWellRTStatPieChat(store) {
	var divid=getCBMWellSingleStatType().pieDivId;
	var title=getCBMWellSingleStatType().pieChartTitle;
	var get_rawData = store.proxy.reader.rawData;
	var datalist=get_rawData.List;
	
	var pieDataStr="[";
	for(var i=0;i<datalist.length;i++){
		pieDataStr+="['"+datalist[i].item+"',"+datalist[i].count+"]";
		if(i<datalist.length-1){
			pieDataStr+=",";
		}
	}
	pieDataStr+="]";
	var pieData = Ext.JSON.decode(pieDataStr);
	ShowCBMWellRTStatPieChat(title,divid, "井数占", pieData);
}


function ShowCBMWellRTStatPieChat(title,divid, name, data) {
	$('#'+divid).highcharts({
		chart : {
			plotBackgroundColor : null,
			plotBorderWidth : null,
			plotShadow : false
		},
		credits : {
			enabled : false
		},
		title : {
			text : title
		},
		colors : ['#058DC7', '#50B432', '#ED561B', '#24CBE5', '#64E572',
				'#FF9655', '#FFF263', '#6AF9C4', '#DDDF00'],
		tooltip : {
			pointFormat : '井数: <b>{point.y}</b> 占: <b>{point.percentage:.1f}%</b>'
		},
		legend : {
			align : 'center',
			verticalAlign : 'bottom',
			layout : 'horizontal' //vertical 竖直 horizontal-水平
		},
		plotOptions : {
			pie : {
				allowPointSelect : true,
				cursor : 'pointer',
				dataLabels : {
					enabled : true,
					color : '#000000',
					connectorColor : '#000000',
					format : '<b>{point.name}</b>: {point.y}口'
				},
				events: {
					click: function(e) {
						if(!e.point.selected){//如果没被选中
							Ext.getCmp('CBMWellSingleDetailsSelectedStatValue_Id').setValue(e.point.name);
						}else{
							Ext.getCmp('CBMWellSingleDetailsSelectedStatValue_Id').setValue("");
						}
						Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setValue("");
	            		Ext.getCmp("CBMWellRealtimeAnalysisWellCom_Id").setRawValue("");
	            		var gridPanel = Ext.getCmp("CBMWellAnalysisSingleDetails_Id");
	                    if (isNotVal(gridPanel)) {
	                    	gridPanel.getSelectionModel().clearSelections();
	                    	gridPanel.getStore().loadPage(1);
	                    }
					}
				},
				showInLegend : true
			}
		},
		exporting:{    
            enabled:true,    
            filename:'class-booking-chart',    
            url:context + '/exportHighcharsPicController/export'
       },
		series : [{
					type : 'pie',
					name : name,
					data : data
				}]
	});
}


function loadCBMWellSingleStatData() {
	var selectWellName=Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
	var statPanelId=getCBMWellSingleStatType().piePanelId;
	if(selectWellName==null||selectWellName==""){
		Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("统计数据");
    	Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").hide();
  	  	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").hide();
        Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").show();
  	  	Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").hide();
    	Ext.getCmp(statPanelId).expand(true);
    }else{
    	Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("历史数据");
		Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").show();
    	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").show();
    	Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").hide();
        Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").show();
    	Ext.getCmp(statPanelId).collapse();
    }
	Ext.getCmp("CBMWellSingleDetailsSelectedStatValue_Id").setValue("");
	
	var gridPanel=Ext.getCmp("CBMWellAnalysisSingleDetails_Id");
	if(isNotVal(gridPanel)){
		gridPanel.destroy();
	}
	Ext.create("AP.store.RealTimeEvaluation.CBMWellRTAnalysisStatStore");
}

function exportCBMWellRTAnalisiDataExcel() {
	var gridId = "CBMWellAnalysisSingleDetails_Id";
    var url = context + '/diagnosisAnalysisOnlyController/exportProductionWellRTAnalysisDataExcel';
    var fileName = getCBMWellSingleStatType().exportExcelTitle;
    var title =  getCBMWellSingleStatType().exportExcelTitle;
    
    var orgId = Ext.getCmp('leftOrg_Id').getValue();
    var wellName = Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
    var startDate=Ext.getCmp('CBMWellRealtimeAnalysisStartDate_Id').rawValue;
    var endDate=Ext.getCmp('CBMWellRealtimeAnalysisEndDate_Id').rawValue;
    var statValue = Ext.getCmp('CBMWellSingleDetailsSelectedStatValue_Id').getValue();
    var type=getCBMWellSingleStatType().type;
    var wellType=400;
    
    var fields = "";
    var heads = "";
    var lockedheads = "";
    var unlockedheads = "";
    var lockedfields = "";
    var unlockedfields = "";
    var columns_ = Ext.JSON.decode(Ext.getCmp("CBMWellRealtimeTableColumnStr_Id").getValue());
    
    Ext.Array.each(columns_, function (name, index, countriesItSelf) {
        var column = columns_[index];
        if (index > 0 && !column.hidden) {
        	if(column.locked){
        		lockedfields += column.dataIndex + ",";
        		lockedheads += column.text + ",";
        	}else{
        		unlockedfields += column.dataIndex + ",";
        		unlockedheads += column.text + ",";
        	}
            
        }
    });
    if (isNotVal(lockedfields)) {
    	lockedfields = lockedfields.substring(0, lockedfields.length - 1);
    	lockedheads = lockedheads.substring(0, lockedheads.length - 1);
    }
    if (isNotVal(unlockedfields)) {
    	unlockedfields = unlockedfields.substring(0, unlockedfields.length - 1);
    	unlockedheads = unlockedheads.substring(0, unlockedheads.length - 1);
    }
    fields = "id," + lockedfields+","+unlockedfields;
    heads = "序号," + lockedheads+","+unlockedheads;
    var param = "&fields=" + fields + "&heads=" + URLencode(URLencode(heads)) 
    + "&orgId=" + orgId 
    + "&wellName=" + URLencode(URLencode(wellName))
    + "&statValue=" + URLencode(URLencode(statValue)) 
    + "&fileName=" + URLencode(URLencode(fileName)) 
    + "&title=" + URLencode(URLencode(title))
    + "&type=" + type 
    + "&wellType=" + wellType 
    + "&startDate=" + startDate 
    + "&endDate=" + endDate ;
    openExcelWindow(url + '?flag=true' + param);
};

WellHistoryDataCurveChartFn = function (get_rawData, itemName, itemCode, divId) {
    var tickInterval = 1;
    var data = get_rawData.totalRoot;
    tickInterval = Math.floor(data.length / 10) + 1;
    var upline = 0,
        downline = 0;
    var uplineName = '',
        downlineName = '';
    var limitlinewidth = 0;
    if (itemCode == 'currenta' || itemCode == 'currentb' || itemCode == 'currentc' || itemCode == 'voltagea' || itemCode == 'voltageb' || itemCode == 'voltagec') {
        upline = parseFloat(get_rawData.uplimit);
        downline = parseFloat(get_rawData.downlimit);
        uplineName = '上限:' + upline;
        downlineName = '下限:' + downline;
        limitlinewidth = 3;
    } else {
        upline = 0;
        downline = 0;
        uplineName = '';
        downlineName = '';
        limitlinewidth = 0;
    }

    var catagories = "[";
    var title = get_rawData.wellName + "井" + itemName.split("(")[0] + "曲线";
    for (var i = 0; i < data.length; i++) {
        catagories += "\"" + data[i].acquisitionTime + "\"";
        if (i < data.length - 1) {
            catagories += ",";
        }
    }
    catagories += "]";
    var legendName = [itemName];
    var series = "[";
    for (var i = 0; i < legendName.length; i++) {
        series += "{\"name\":\"" + legendName[i] + "\",";
        series += "\"data\":[";
        for (var j = 0; j < data.length; j++) {
            var year = parseInt(data[j].acquisitionTime.split(" ")[0].split("-")[0]);
            var month = parseInt(data[j].acquisitionTime.split(" ")[0].split("-")[1]);
            var day = parseInt(data[j].acquisitionTime.split(" ")[0].split("-")[2]);
            var hour = parseInt(data[j].acquisitionTime.split(" ")[1].split(":")[0]);
            var minute = parseInt(data[j].acquisitionTime.split(" ")[1].split(":")[1]);
            var second = parseInt(data[j].acquisitionTime.split(" ")[1].split(":")[2]);
//            series += "[" + Date.UTC(year, month - 1, day, hour, minute, second) + "," + data[j].value + "]";
            series += "[" + Date.parse(data[j].acquisitionTime.replace(/-/g, '/')) + "," + data[j].value + "]";
            if (j != data.length - 1) {
                series += ",";
            }
        }
        series += "]}";
        if (i != legendName.length - 1) {
            series += ",";
        }
    }
    series += "]";
    var cat = Ext.JSON.decode(catagories);
    var ser = Ext.JSON.decode(series);
    var color = ['#800000', // 红
       '#008C00', // 绿
       '#000000', // 黑
       '#0000FF', // 蓝
       '#F4BD82', // 黄
       '#FF00FF' // 紫
     ];

    initWellHistoryDataCurveChartFn(cat, ser, tickInterval, divId, title, "[" + get_rawData.startDate + "~" + get_rawData.endDate + "]", "时间", itemName, color, upline, downline, uplineName, downlineName, limitlinewidth);

    return false;
};

function initWellHistoryDataCurveChartFn(catagories, series, tickInterval, divId, title, subtitle, xtitle, ytitle, color, upline, downline, uplineName, downlineName, limitlinewidth) {
    var max = null;
    var min = null;
    if (upline != 0) {
        max = upline + 10;
    }
    if (downline != 0) {
        min = downline - 10;
    }
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    mychart = new Highcharts.Chart({
        chart: {
            renderTo: divId,
            type: 'spline',
            shadow: true,
            borderWidth: 0,
            zoomType: 'xy'
        },
        credits: {
            enabled: false
        },
        title: {
            text: title
        },
        subtitle: {
            text: subtitle
        },
        colors: color,
        xAxis: {
            type: 'datetime',
            title: {
                text: xtitle
            },
            labels: {
                formatter: function () {
                    return Highcharts.dateFormat("%Y-%m-%d", this.value);
                },
                rotation: 0, //倾斜度，防止数量过多显示不全  
                step: 2
            }
        },
        yAxis: [{
            lineWidth: 1,
            title: {
                text: ytitle,
                style: {
                    color: '#000000',
                    fontWeight: 'bold'
                }
            },
            max: max,
            min: min,
            labels: {
                formatter: function () {
                    return Highcharts.numberFormat(this.value, 2);
                }
            },
            plotLines: [{ //一条延伸到整个绘图区的线，标志着轴中一个特定值。
                color: 'red',
                dashStyle: 'shortdash', //Dash,Dot,Solid,shortdash,默认Solid
                label: {
                    text: uplineName,
                    align: 'right',
                    x: -10
                },
                width: limitlinewidth,
                zIndex:10,
                value: upline //y轴显示位置
                   }, {
                color: 'green',
                dashStyle: 'shortdash',
                label: {
                    text: downlineName,
                    align: 'right',
                    x: -10
                },
                width: limitlinewidth,
                zIndex:10,
                value: downline //y轴显示位置
                   }]
      }],
        tooltip: {
            crosshairs: true, //十字准线
            style: {
                color: '#333333',
                fontSize: '12px',
                padding: '8px'
            },
            dateTimeLabelFormats: {
                millisecond: '%Y-%m-%d %H:%M:%S.%L',
                second: '%Y-%m-%d %H:%M:%S',
                minute: '%Y-%m-%d %H:%M',
                hour: '%Y-%m-%d %H',
                day: '%Y-%m-%d',
                week: '%m-%d',
                month: '%Y-%m',
                year: '%Y'
            }
        },
        exporting: {
            enabled: true,
            filename: 'class-booking-chart',
            url: context + '/exportHighcharsPicController/export'
        },
        plotOptions: {
            spline: {
                lineWidth: 1,
                fillOpacity: 0.3,
                marker: {
                    enabled: true,
                    radius: 3, //曲线点半径，默认是4
                    //                            symbol: 'triangle' ,//曲线点类型："circle", "square", "diamond", "triangle","triangle-down"，默认是"circle"
                    states: {
                        hover: {
                            enabled: true,
                            radius: 6
                        }
                    }
                },
                shadow: true
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            enabled: false,
            borderWidth: 0
        },
        series: series
    });
};