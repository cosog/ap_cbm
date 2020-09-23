Ext.define('AP.store.DailyEvaluation.GroupValveDailyAnalysisStatStore', {
	extend : 'Ext.data.Store',
	autoLoad : true,
	pageSize : defaultPageSize,
	proxy : {
		type : 'ajax',
		url : context + '/dailyEvaluationController/groupValveDailyStatisticsData',
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
			initGroupValveDailyStatPieChat(store);
			
			var gridPanel = Ext.getCmp("GroupValveAnalysisDaily_Id");
            if (isNotVal(gridPanel)) {
            	gridPanel.getStore().load();
            }else{
            	Ext.create('AP.store.DailyEvaluation.GroupValveDailyAnalysisiListStore');
            }
		},
		beforeload : function(store, options) {
			var type=getGroupValveDailyStatType().type;
			var orgId = Ext.getCmp('leftOrg_Id').getValue();
			var totalDate=Ext.getCmp('GroupValveDailyAnalysisDate_Id').rawValue;
			var new_params = {
				orgId:orgId,
				type:type,
				totalDate:totalDate
			};
			Ext.apply(store.proxy.extraParams, new_params);
		}
	}
});
