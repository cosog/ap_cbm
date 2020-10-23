Ext.define('AP.store.DailyEvaluation.GroupValveDailyAnalysisiListStore', {
    extend: 'Ext.data.Store',
    fields: ['wellName','acqTime',
    	'commStatusName','commTime','commTimeEfficiency','commRange', 'commAlarmLevel','commtimeefficiencyLevel',
    	'runStatusName', 'runTime','runTimeEfficiency', 'runRange', 'runAlarmLevel','runtimeefficiencyLevel',
    	'rtuStatus', 'SPM','AI1', 'AI2', 'AI3', 'AI4', 
    	'gasFlowmeterCommStatus','gasFlowmeterCommName', 'gasInstantaneousFlow', 'gasCumulativeFlow','gasTodayProd','gasFlowmeterPress',
        'liquidFlowmeterCommStatus', 'liquidFlowmeterCommName', 'liquidInstantaneousflow', 'liquidCumulativeflow','liquidFlowmeterProd',
        'fluidLevelIndicatorCommStatus','fluidLevelIndicatorCommName', 'fluidLevelAcquisitionTime', 'soundVelocity', 'fluidLevel', 'fluidLevelIndicatorPress',
        'vfdCommStatus', 'vfdCommName','vfdStatus', 'vfdStatusName', 'vfdStatus2', 'vfdStatus2Name', 
        'runFrequency', 'vfdBusbarVoltage','vfdOutputVoltage', 'vfdOutputCurrent', 'setFrequencyFeedback', 'vfdFaultCode','vfdPosition', 'vfdPositionName','vfdManufacturerCode','vfdManufacturerName',
        'frequencyOrSPMcontrolSign', 'frequencyOrSPMcontrol',
        'frequencySetValue', 'SPMSetValue', 'SPMBy10hz', 'SPMBy50hz',
        'rtuAddr','rtuProgramVersion','setWellname'],
    autoLoad: true,
    pageSize: 50,
    proxy: {
    	type: 'ajax',
        url: context + '/dailyEvaluationController/getGroupValveRealtimeAnalysisWellList',
        actionMethods: {
            read: 'POST'
        },
        reader: {
            type: 'json',
            rootProperty: 'totalRoot',
            totalProperty: 'totalCount',
            keepRawData: true
        }
    },
    listeners: {
        load: function (store, sEops) {
        	var get_rawData = store.proxy.reader.rawData;
            var arrColumns = get_rawData.columns;
            var column = createGroupValveDailyTableColumn(arrColumns);
            Ext.getCmp("GroupValveDailyTableColumnStr_Id").setValue(column);
            Ext.getCmp("AlarmShowStyle_Id").setValue(JSON.stringify(get_rawData.AlarmShowStyle));
            var newColumns = Ext.JSON.decode(column);
            var bbar = new Ext.PagingToolbar({
            	store: store,
            	displayInfo: true,
            	displayMsg: '当前 {0}~{1}条  共 {2} 条'
	        });
            var gridPanel = Ext.getCmp("GroupValveAnalysisDaily_Id");
            if (!isNotVal(gridPanel)) {
            	gridPanel= Ext.create('Ext.grid.Panel', {
                    id: 'GroupValveAnalysisDaily_Id',
                    border: false,
                    forceFit: false,
                    bbar: bbar,
                    store:store,
                    viewConfig: {
                        emptyText: "<div class='con_div_' id='div_dataactiveid'><" + cosog.string.nodata + "></div>"
                    },
                    columnLines: true,
                    columns:newColumns,
                    listeners: {
                        selectionchange: function (view, selected, o) {
                            if (selected.length > 0) {
                        		//请求单井详情数据
                        		Ext.create("AP.store.DailyEvaluation.GroupValveDailyCurveStore");
                        		Ext.create("AP.store.DailyEvaluation.GroupValveDailyAnalysisTableStore");
                            }
                            
                        },
                        itemdblclick: function (view,record,item,index,e,eOpts) {
                        	var wellName=Ext.getCmp('GroupValveDailyAnalysisWellCom_Id').getValue();
                    		if(wellName==null||wellName==""){
                    			Ext.getCmp("GroupValveDailyAnalysisWellListPanel_Id").setTitle("阀组历史");
                    			Ext.getCmp("GroupValveDailyAnalysisDate_Id").hide();
                            	Ext.getCmp("GroupValveDailyAnalysisStartDate_Id").show();
                            	Ext.getCmp("GroupValveDailyAnalysisEndDate_Id").show();
                            	Ext.getCmp("GroupValveDailyAnalysisHisBtn_Id").hide();
                                Ext.getCmp("GroupValveDailyAnalysisAllBtn_Id").show();
                                
                                var statPanelId=getGroupValveDailyStatType().piePanelId;
                            	Ext.getCmp(statPanelId).collapse();
                            	
                    			Ext.getCmp('GroupValveDailyAnalysisWellCom_Id').setValue(record.data.wellName);
                            	Ext.getCmp('GroupValveDailyAnalysisWellCom_Id').setRawValue(record.data.wellName);
                            	Ext.getCmp('GroupValveAnalysisDaily_Id').getStore().loadPage(1);
                            	
                    		}
                        }
                    }
                });
            	var panelId=getGroupValveDailyStatType().panelId;
            	Ext.getCmp(panelId).add(gridPanel);
            	
            	var length = gridPanel.dockedItems.keys.length;
                var refreshStr= "";
                for (var i = 0; i < length; i++) {
                   if (gridPanel.dockedItems.keys[i].indexOf("pagingtoolbar") !== -1) {
                      refreshStr= gridPanel.dockedItems.keys[i];
                   }
                }
            	gridPanel.dockedItems.get(refreshStr).child('#refresh').setHandler(   
            	        function() {
            	        	Ext.create("AP.store.DailyEvaluation.GroupValveDailyAnalysisStatStore");
            	         }
            	)
            	
            }else{
            	gridPanel.reconfigure(newColumns);
            }
            var startDate=Ext.getCmp('GroupValveDailyAnalysisStartDate_Id').rawValue;
            if(startDate==''||null==startDate){
            	Ext.getCmp("GroupValveDailyAnalysisStartDate_Id").setValue(get_rawData.start_date==undefined?get_rawData.startDate:get_rawData.start_date);
            }
            if(get_rawData.totalCount>0){
            	gridPanel.getSelectionModel().deselectAll(true);
            	gridPanel.getSelectionModel().select(0, true);
            }else{
            	Ext.getCmp("GroupValveDailyAnalysisTableCalDataPanel_Id").removeAll();
            	Ext.getCmp("GroupValveDailyAnalysisTableAcqDataPanel_Id").removeAll();
            	if($("#GroupValveDailyCurveDataChartDiv_Id")!=null){
            		$("#GroupValveDailyCurveDataChartDiv_Id").html('');
            	}
            	if($("#GroupValveDailyCurveDataChartDiv2_Id")!=null){
            		$("#GroupValveDailyCurveDataChartDiv2_Id").html('');
            	}
            }
            
            
        },
        beforeload: function (store, options) {
            var orgId = Ext.getCmp('leftOrg_Id').getValue();
            var wellName = Ext.getCmp('GroupValveDailyAnalysisWellCom_Id').getValue();
            var totalDate=Ext.getCmp('GroupValveDailyAnalysisDate_Id').rawValue;
            var startDate=Ext.getCmp('GroupValveDailyAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('GroupValveDailyAnalysisEndDate_Id').rawValue;
            var statValue = Ext.getCmp('GroupValveDailySelectedStatValue_Id').getValue();
            var type=getGroupValveDailyStatType().type;
            var new_params = {
                orgId: orgId,
                wellName: wellName,
                totalDate:totalDate,
                startDate:startDate,
                endDate:endDate,
                statValue:statValue,
                type:type,
                unitType:1
            };
            Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
        	onStoreSizeChange(v, o, "GroupValveDailyAnalysisCount_Id");
        }
    }
});