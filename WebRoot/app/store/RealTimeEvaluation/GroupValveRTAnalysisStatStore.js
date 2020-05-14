Ext.define('AP.store.RealTimeEvaluation.GroupValveRTAnalysisStatStore', {
	extend : 'Ext.data.Store',
	autoLoad : true,
	pageSize : defaultPageSize,
	proxy : {
		type : 'ajax',
		url : context + '/realTimeEvaluationController/groupValveStatisticsData',
		actionMethods : {
			read : 'POST'
		},
		params : {
			start : 0,
			limit : defaultPageSize
		},
		reader : {
			type : 'json',
			rootProperty : 'list',
			totalProperty : 'totals',
            keepRawData: true
		}
	},
	listeners : {
		load:function(store,record,f,op,o){
			initGroupValveRTStatPieChat(store);
			
			var gridPanel = Ext.getCmp("GroupValveAnalysisSingleDetails_Id");
            if (isNotVal(gridPanel)) {
            	gridPanel.getStore().load();
            }else{
            	Ext.create('AP.store.RealTimeEvaluation.GroupValveSingleAnalysisiListStore');
            }
		},
		beforeload : function(store, options) {
			var type=getGroupValveSingleStatType().type;
			var orgId = Ext.getCmp('leftOrg_Id').getValue();
			var new_params = {
				orgId:orgId,
				type:type
			};
			Ext.apply(store.proxy.extraParams, new_params);
		}
	}
});
