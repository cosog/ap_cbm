Ext.define('AP.store.RealTimeEvaluation.GroupValveHistoryDataCurveStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.groupValveHistoryDataCurveStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/realTimeEvaluationController/getGroupValveHistoryDataCurveData',
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
            var itemCode=Ext.getCmp('GroupValveAnalysisCurveItemCode_Id').rawValue;
            var itemName=Ext.getCmp('GroupValveAnalysisCurveItem_Id').rawValue;
            divId="SingleGroupValveHistoryCurveDiv_Id";
            var startDate=Ext.getCmp("GroupValveHistoryCurve_from_date_Id").getValue();
			if(startDate==""||startDate==null){
				Ext.getCmp("GroupValveHistoryCurve_from_date_Id").setValue(get_rawData.startDate);
			}
			GroupValveHistoryDataCurveChartFn(get_rawData,itemName,itemCode,divId);
        },
        beforeload: function (store, options) {
        	var wellName  = Ext.getCmp("GroupValveAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var StartDate = Ext.getCmp('GroupValveHistoryCurve_from_date_Id').rawValue;
        	var EndDate = Ext.getCmp('GroupValveHistoryCurve_end_date_Id').rawValue;
        	var itemName=Ext.getCmp('GroupValveAnalysisCurveItem_Id').rawValue;
        	var itemCode=Ext.getCmp('GroupValveAnalysisCurveItemCode_Id').rawValue;
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