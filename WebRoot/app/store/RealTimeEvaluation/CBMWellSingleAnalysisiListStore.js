Ext.define('AP.store.RealTimeEvaluation.CBMWellSingleAnalysisiListStore', {
    extend: 'Ext.data.Store',
    fields: ['wellName','acquisitionTime',
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
        url: context + '/realTimeEvaluationController/getRealtimeAnalysisWellList',
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
            var column = createCBMWellRealtimeTableColumn(arrColumns);
            Ext.getCmp("CBMWellRealtimeTableColumnStr_Id").setValue(column);
            Ext.getCmp("AlarmShowStyle_Id").setValue(JSON.stringify(get_rawData.AlarmShowStyle));
            var newColumns = Ext.JSON.decode(column);
            var bbar = new Ext.PagingToolbar({
            	store: store,
            	displayInfo: true,
            	displayMsg: '当前 {0}~{1}条  共 {2} 条'
	        });
            var gridPanel = Ext.getCmp("CBMWellAnalysisSingleDetails_Id");
            if (!isNotVal(gridPanel)) {
            	gridPanel= Ext.create('Ext.grid.Panel', {
                    id: 'CBMWellAnalysisSingleDetails_Id',
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
                        		//请求图形数据
//                        		Ext.create("AP.store.diagnosis.SinglePumpCardStore");
                        		Ext.create("AP.store.RealTimeEvaluation.CBMWellAnalysisTableStore");
                            }
                            
                        },
                        itemdblclick: function (view,record,item,index,e,eOpts) {
                        	var wellName=Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
                    		if(wellName==null||wellName==""){
                    			Ext.getCmp("CBMWellRealtimeAnalysisWellListPanel_Id").setTitle("历史数据");
                    			Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").show();
                            	Ext.getCmp("CBMWellRealtimeAnalysisEndDate_Id").show();
                            	Ext.getCmp("CBMWellRealtimeAnalysisHisBtn_Id").hide();
                                Ext.getCmp("CBMWellRealtimeAnalysisAllBtn_Id").show();
                                
                                var statPanelId=getCBMWellSingleStatType().piePanelId;
                            	Ext.getCmp(statPanelId).collapse();
                            	
                    			Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').setValue(record.data.wellName);
                            	Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').setRawValue(record.data.wellName);
                            	Ext.getCmp('CBMWellAnalysisSingleDetails_Id').getStore().loadPage(1);
                            	
                    		}
                        }
                    }
                });
            	var panelId=getCBMWellSingleStatType().panelId;
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
            	        	Ext.create("AP.store.RealTimeEvaluation.CBMWellRTAnalysisStatStore");
            	         }
            	)
            	
            }else{
            	gridPanel.reconfigure(newColumns);
            }
            var startDate=Ext.getCmp('CBMWellRealtimeAnalysisStartDate_Id').rawValue;
            if(startDate==''||null==startDate){
            	Ext.getCmp("CBMWellRealtimeAnalysisStartDate_Id").setValue(get_rawData.start_date==undefined?get_rawData.startDate:get_rawData.start_date);
            }
            if(get_rawData.totalCount>0){
            	var SingleAnalysisGridPanel = Ext.getCmp("CBMWellAnalysisSingleDetails_Id");
                if (isNotVal(SingleAnalysisGridPanel)) {
                	SingleAnalysisGridPanel.getSelectionModel().deselectAll(true);
                	SingleAnalysisGridPanel.getSelectionModel().select(0, true);
                }
            }else{
            	Ext.getCmp("CBMWellRTAnalysisTableCalDataPanel_Id").removeAll();
            	Ext.getCmp("CBMWellRTAnalysisTableAcqDataPanel_Id").removeAll();
            	Ext.getCmp("CBMWellRTAnalysisControlDataPanel_Id").removeAll();
            }
            
            
        },
        beforeload: function (store, options) {
            var orgId = Ext.getCmp('leftOrg_Id').getValue();
            var wellName = Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
            var startDate=Ext.getCmp('CBMWellRealtimeAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('CBMWellRealtimeAnalysisEndDate_Id').rawValue;
            var statValue = Ext.getCmp('CBMWellSingleDetailsSelectedStatValue_Id').getValue();
            var type=getCBMWellSingleStatType().type;
            var new_params = {
                orgId: orgId,
                wellName: wellName,
                startDate:startDate,
                endDate:endDate,
                statValue:statValue,
                type:type,
                unitType:1
            };
            Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
        	onStoreSizeChange(v, o, "DiagnosisPumpingUnit_SingleDinagnosisAnalysisTotalCount_Id");
        }
    }
});