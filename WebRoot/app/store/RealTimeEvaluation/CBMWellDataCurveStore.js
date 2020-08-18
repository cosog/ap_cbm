Ext.define('AP.store.RealTimeEvaluation.CBMWellDataCurveStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.CBMWellDataCurveStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/realTimeEvaluationController/getCBMWellDataCurve',
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
        	initCBMWellRTCurveChartFn(get_rawData,"CBMWellRTCurveDataChartDiv_Id");
        	initCBMWellRTCurveChartFn2(get_rawData,"CBMWellRTCurveDataChartDiv2_Id");
        },
        beforeload: function (store, options) {
        	var wellName=Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
        	var selectedWellName  = Ext.getCmp("CBMWellAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var startDate=Ext.getCmp('CBMWellRealtimeAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('CBMWellRealtimeAnalysisEndDate_Id').rawValue;
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