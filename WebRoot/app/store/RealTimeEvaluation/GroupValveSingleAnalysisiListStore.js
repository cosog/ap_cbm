Ext.define('AP.store.RealTimeEvaluation.GroupValveSingleAnalysisiListStore', {
    extend: 'Ext.data.Store',
    fields: ['wellName','acquisitionTime',
    	'commStatusName','commTime','commTimeEfficiency','commRange', 'commAlarmLevel','commtimeefficiencyLevel',
    	'cumulativeFlow1', 'flowmeterBackupPoint1','instantaneousFlow1', 'flowmeterTemperature1', 'flowmeterPress1',
    	'cumulativeFlow2', 'flowmeterBackupPoint2','instantaneousFlow2', 'flowmeterTemperature2', 'flowmeterPress2',
    	'cumulativeFlow3', 'flowmeterBackupPoint3','instantaneousFlow3', 'flowmeterTemperature3', 'flowmeterPress3',
    	'cumulativeFlow4', 'flowmeterBackupPoint4','instantaneousFlow4', 'flowmeterTemperature4', 'flowmeterPress4'],
    autoLoad: true,
    pageSize: 50,
    proxy: {
    	type: 'ajax',
        url: context + '/realTimeEvaluationController/getRealtimeAnalysisGroupValveList',
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
            var column = createGroupValveRealtimeTableColumn(arrColumns);
            Ext.getCmp("GroupValveRealtimeTableColumnStr_Id").setValue(column);
            Ext.getCmp("AlarmShowStyle_Id").setValue(JSON.stringify(get_rawData.AlarmShowStyle));
            var newColumns = Ext.JSON.decode(column);
            var bbar = new Ext.PagingToolbar({
            	store: store,
            	displayInfo: true,
            	displayMsg: '当前 {0}~{1}条  共 {2} 条'
	        });
            var gridPanel = Ext.getCmp("GroupValveAnalysisSingleDetails_Id");
            if (!isNotVal(gridPanel)) {
            	gridPanel= Ext.create('Ext.grid.Panel', {
                    id: 'GroupValveAnalysisSingleDetails_Id',
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
                        		Ext.create("AP.store.RealTimeEvaluation.GroupValveAnalysisTableStore");
                            }
                        },
                        itemdblclick: function (view,record,item,index,e,eOpts) {
                        	var groupValveName=Ext.getCmp('GroupValveRealtimeAnalysisGroupValveCom_Id').getValue();
                    		if(groupValveName==null||groupValveName==""){
                    			Ext.getCmp("GroupValveRealtimeAnalysisGroupValveListPanel_Id").setTitle("单井历史");
                    			Ext.getCmp("GroupValveRealtimeAnalysisStartDate_Id").show();
                            	Ext.getCmp("GroupValveRealtimeAnalysisEndDate_Id").show();
                            	Ext.getCmp("GroupValveRealtimeAnalysisHisBtn_Id").hide();
                                Ext.getCmp("GroupValveRealtimeAnalysisAllBtn_Id").show();
                                
                                var statPanelId=getGroupValveSingleStatType().piePanelId;
                            	Ext.getCmp(statPanelId).collapse();
                            	
                    			Ext.getCmp('GroupValveRealtimeAnalysisGroupValveCom_Id').setValue(record.data.wellName);
                            	Ext.getCmp('GroupValveRealtimeAnalysisGroupValveCom_Id').setRawValue(record.data.wellName);
                            	Ext.getCmp('GroupValveAnalysisSingleDetails_Id').getStore().loadPage(1);
                            	
                    		}
                        }
                    }
                });
            	var panelId=getGroupValveSingleStatType().panelId;
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
            	        	Ext.create("AP.store.RealTimeEvaluation.GroupValveRTAnalysisStatStore");
            	         }
            	)
            	
            }else{
            	gridPanel.reconfigure(newColumns);
            }
            var startDate=Ext.getCmp('GroupValveRealtimeAnalysisStartDate_Id').rawValue;
            if(startDate==''||null==startDate){
            	Ext.getCmp("GroupValveRealtimeAnalysisStartDate_Id").setValue(get_rawData.start_date==undefined?get_rawData.startDate:get_rawData.start_date);
            }
            if(get_rawData.totalCount>0){
            	var SingleAnalysisGridPanel = Ext.getCmp("GroupValveAnalysisSingleDetails_Id");
                if (isNotVal(SingleAnalysisGridPanel)) {
                	SingleAnalysisGridPanel.getSelectionModel().deselectAll(true);
                	SingleAnalysisGridPanel.getSelectionModel().select(0, true);
                }
            }else{
            	Ext.getCmp("GroupValveRTAnalysisTableCalDataPanel_Id").removeAll();
            	Ext.getCmp("GroupValveRTAnalysisTableAcqDataPanel_Id").removeAll();
            	Ext.getCmp("GroupValveRTAnalysisControlDataPanel_Id").removeAll();
            }
            
            
        },
        beforeload: function (store, options) {
            var orgId = Ext.getCmp('leftOrg_Id').getValue();
            var wellName = Ext.getCmp('GroupValveRealtimeAnalysisGroupValveCom_Id').getValue();
            var startDate=Ext.getCmp('GroupValveRealtimeAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('GroupValveRealtimeAnalysisEndDate_Id').rawValue;
            var statValue = Ext.getCmp('GroupValveSingleDetailsSelectedStatValue_Id').getValue();
            var type=getGroupValveSingleStatType().type;
            var new_params = {
                orgId: orgId,
                name: wellName,
                startDate:startDate,
                endDate:endDate,
                statValue:statValue,
                type:type,
                unitType:2
            };
            Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
        	onStoreSizeChange(v, o, "DiagnosisPumpingUnit_SingleDinagnosisAnalysisTotalCount_Id");
        }
    }
});