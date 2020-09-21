Ext.define('AP.store.DailyEvaluation.CBMWellDailyCurveStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.CBMWellDailyCurveStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/dailyEvaluationController/getCBMWellDailyDataCurve',
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
        	var get_rawData=store.proxy.reader.rawData;
        	initCBMWellDailyCurveChartFn(get_rawData,"CBMWellDailyCurveDataChartDiv_Id");
        	initCBMWellDailyCurveChartFn2(get_rawData,"CBMWellDailyCurveDataChartDiv2_Id");
        },
        beforeload: function (store, options) {
        	var wellName=Ext.getCmp('CBMWellDailyAnalysisWellCom_Id').getValue();
        	var selectedWellName  = Ext.getCmp("CBMWellAnalysisDaily_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var startDate=Ext.getCmp('CBMWellDailyAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('CBMWellDailyAnalysisEndDate_Id').rawValue;
        	var new_params = {
        			wellName:wellName,
        			selectedWellName:selectedWellName,
        			startDate:startDate,
                    endDate:endDate
                };
           Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
            //onLabelSizeChange(v, o, "statictisTotalsId");
        }
    }
});