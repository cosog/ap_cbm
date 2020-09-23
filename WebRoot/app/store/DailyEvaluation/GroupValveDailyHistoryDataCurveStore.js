Ext.define('AP.store.DailyEvaluation.GroupValveDailyHistoryDataCurveStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.groupValveDailyHistoryDataCurveStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/dailyEvaluationController/getGroupValveDailyHistoryDataCurveData',
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
        load: function (store, record, f, op, o) {
            //获得列表数
            var get_rawData = store.proxy.reader.rawData;
            var itemCode=Ext.getCmp('GroupValveDailyAnalysisCurveItemCode_Id').rawValue;
            var itemName=Ext.getCmp('GroupValveDailyAnalysisCurveItem_Id').rawValue;
            divId="GroupValveDailyHistoryCurveDiv_Id";
            var startDate=Ext.getCmp("GroupValveDailyHistoryCurve_from_date_Id").getValue();
			if(startDate==""||startDate==null){
				Ext.getCmp("GroupValveDailyHistoryCurve_from_date_Id").setValue(get_rawData.startDate);
			}
			GroupValveDailyHistoryDataCurveChartFn(get_rawData,itemName,itemCode,divId);
        },
        beforeload: function (store, options) {
        	var wellName  = Ext.getCmp("GroupValveAnalysisDaily_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var StartDate = Ext.getCmp('GroupValveDailyHistoryCurve_from_date_Id').rawValue;
        	var EndDate = Ext.getCmp('GroupValveDailyHistoryCurve_end_date_Id').rawValue;
        	var itemName=Ext.getCmp('GroupValveDailyAnalysisCurveItem_Id').rawValue;
        	var itemCode=Ext.getCmp('GroupValveDailyAnalysisCurveItemCode_Id').rawValue;
        	var new_params = {
        			wellName: wellName,
                    startDate:StartDate,
                    endDate:EndDate,
                    itemName:itemName,
                    itemCode:itemCode,
                    wellType:1
                };
           Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
            //onLabelSizeChange(v, o, "statictisTotalsId");
        }
    }
});