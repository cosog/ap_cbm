Ext.define('AP.store.DailyEvaluation.GroupValveDailyCurveStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.groupValveDailyCurveStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/dailyEvaluationController/getGroupValveDailyDataCurve',
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
        	initGroupValveDailyCurveChartFn(get_rawData,"GroupValveDailyCurveDataChartDiv_Id");
        	initGroupValveDailyCurveChartFn2(get_rawData,"GroupValveDailyCurveDataChartDiv2_Id");
        },
        beforeload: function (store, options) {
        	var wellName=Ext.getCmp('GroupValveDailyAnalysisWellCom_Id').getValue();
        	var selectedWellName  = Ext.getCmp("GroupValveAnalysisDaily_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var startDate=Ext.getCmp('GroupValveDailyAnalysisStartDate_Id').rawValue;
            var endDate=Ext.getCmp('GroupValveDailyAnalysisEndDate_Id').rawValue;
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