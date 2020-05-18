Ext.define('AP.store.RealTimeEvaluation.GroupValveAnalysisTableStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.groupValveAnalysisTableStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/realTimeEvaluationController/getGroupValveAnalysisAndAcqAndControlData',
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
        	var get_rawData = store.proxy.reader.rawData;
        	var isControl=get_rawData.isControl;
    		var dataStr="{\"items\":[";
    		dataStr+="]}";
    		
    		var acqSataStr="{\"items\":[";
    		acqSataStr+="{\"item\":\"采集时间:"+get_rawData.acquisitionTime+"\",\"itemCode\":\"acquisitionTime\",\"value\":\"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"通信状态\",\"itemCode\":\"commStatus\",\"value\":\""+get_rawData.commStatusName+"\",\"alarmLevel\":"+get_rawData.commAlarmLevel+",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"1#流量计总量(m^3)\",\"itemCode\":\"cumulativeFlow1\",\"value\":\""+get_rawData.cumulativeFlow1+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"1#流量计备用点位\",\"itemCode\":\"flowmeterBackupPoint1\",\"value\":\""+get_rawData.flowmeterBackupPoint1+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"1#流量计标况瞬时流量(m^3/h)\",\"itemCode\":\"instantaneousFlow1\",\"value\":\""+get_rawData.instantaneousFlow1+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"1#流量计温度(℃)\",\"itemCode\":\"flowmeterTemperature1\",\"value\":\""+get_rawData.flowmeterTemperature1+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"1#流量计压力(Kpa)\",\"itemCode\":\"flowmeterPress1\",\"value\":\""+get_rawData.flowmeterPress1+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"2#流量计总量(m^3)\",\"itemCode\":\"cumulativeFlow2\",\"value\":\""+get_rawData.cumulativeFlow2+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"2#流量计备用点位\",\"itemCode\":\"flowmeterBackupPoint2\",\"value\":\""+get_rawData.flowmeterBackupPoint2+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"2#流量计标况瞬时流量(m^3/h)\",\"itemCode\":\"instantaneousFlow2\",\"value\":\""+get_rawData.instantaneousFlow2+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"2#流量计温度(℃)\",\"itemCode\":\"flowmeterTemperature2\",\"value\":\""+get_rawData.flowmeterTemperature2+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"2#流量计压力(Kpa)\",\"itemCode\":\"flowmeterPress2\",\"value\":\""+get_rawData.flowmeterPress2+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"3#流量计总量(m^3)\",\"itemCode\":\"cumulativeFlow3\",\"value\":\""+get_rawData.cumulativeFlow3+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"3#流量计备用点位\",\"itemCode\":\"flowmeterBackupPoint3\",\"value\":\""+get_rawData.flowmeterBackupPoint3+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"3#流量计标况瞬时流量(m^3/h)\",\"itemCode\":\"instantaneousFlow3\",\"value\":\""+get_rawData.instantaneousFlow3+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"3#流量计温度(℃)\",\"itemCode\":\"flowmeterTemperature3\",\"value\":\""+get_rawData.flowmeterTemperature3+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"3#流量计压力(Kpa)\",\"itemCode\":\"flowmeterPress3\",\"value\":\""+get_rawData.flowmeterPress3+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"4#流量计总量(m^3)\",\"itemCode\":\"cumulativeFlow4\",\"value\":\""+get_rawData.cumulativeFlow4+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"4#流量计备用点位\",\"itemCode\":\"flowmeterBackupPoint4\",\"value\":\""+get_rawData.flowmeterBackupPoint4+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"4#流量计标况瞬时流量(m^3/h)\",\"itemCode\":\"instantaneousFlow4\",\"value\":\""+get_rawData.instantaneousFlow4+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"4#流量计温度(℃)\",\"itemCode\":\"flowmeterTemperature4\",\"value\":\""+get_rawData.flowmeterTemperature4+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"4#流量计压力(Kpa)\",\"itemCode\":\"flowmeterPress4\",\"value\":\""+get_rawData.flowmeterPress4+"\",\"curve\":\"\"}";
    		
    		acqSataStr+="]}";
    		
    		var controlSataStr="{\"items\":[";
    		controlSataStr+="{\"item\":\"设备地址\",\"itemcode\":\"deviceId\",\"value\":\""+get_rawData.deviceId+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"A1B1口波特率\",\"itemcode\":\"baudrate\",\"value\":\""+get_rawData.baudrate+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"A2B2口波特率\",\"itemcode\":\"baudrate2\",\"value\":\""+get_rawData.baudrate2+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"仪表组合方式-1#从站\",\"itemcode\":\"instrumentCombinationMode1\",\"value\":\""+get_rawData.instrumentCombinationModeName1+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"仪表组合方式-2#从站\",\"itemcode\":\"instrumentCombinationMode2\",\"value\":\""+get_rawData.instrumentCombinationModeName2+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"仪表组合方式-3#从站\",\"itemcode\":\"instrumentCombinationMode3\",\"value\":\""+get_rawData.instrumentCombinationModeName3+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"仪表组合方式-4#从站\",\"itemcode\":\"instrumentCombinationMode4\",\"value\":\""+get_rawData.instrumentCombinationModeName4+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2}";
    		controlSataStr+="]}";
    		
    		var storeData=Ext.JSON.decode(dataStr);
    		var acqStoreData=Ext.JSON.decode(acqSataStr);
    		var controlStoreData=Ext.JSON.decode(controlSataStr);
    		
    		
    		var store=Ext.create('Ext.data.Store', {
			    fields:['item', 'itemCode','value', 'curve'],
			    data:storeData,
			    proxy: {
			        type: 'memory',
			        reader: {
			            type: 'json',
			            root: 'items'
			        }
			    }
			});
    		var acqStore=Ext.create('Ext.data.Store', {
			    fields:['item', 'itemCode','value', 'curve'],
			    data:acqStoreData,
			    proxy: {
			        type: 'memory',
			        reader: {
			            type: 'json',
			            root: 'items'
			        }
			    }
			});
    		
    		var controlStore=Ext.create('Ext.data.Store', {
			    fields:['item','value','operation'],
			    data:controlStoreData,
			    proxy: {
			        type: 'memory',
			        reader: {
			            type: 'json',
			            root: 'items'
			        }
			    }
			});
    		
    		var GridPanel=Ext.getCmp("GroupValveAnalysisDataGridPanel_Id");
    		if(!isNotVal(GridPanel)){
    			GridPanel=Ext.create('Ext.grid.Panel', {
    				id:'GroupValveAnalysisDataGridPanel_Id',
    				border: false,
    				columnLines: true,
    				forceFit: false,
    				store: store,
    			    columns: [
    			        { 
    			        	header: '名称',  
    			        	dataIndex: 'item',
    			        	align:'left',
    			        	flex:3,
    			        	renderer:function(value){
    			        		return "<span data-qtip=\""+(value==undefined?"":value)+"\">"+(value==undefined?"":value)+"</span>";
    			        	}
    			        },
    			        { 
    			        	header: '值', 
    			        	dataIndex: 'value',
    			        	align:'center',
    			        	flex:1,
    			        	renderer:function(value){
    			        		return "<span data-qtip=\""+(value==undefined?"":value)+"\">"+(value==undefined?"":value)+"</span>";
    			        	}
    			        },
    			        { header: '趋势曲线', dataIndex: 'curve',align:'center',flex:1,renderer :function(value,e,o){return iconGroupValveAnalysisCurve(value,e,o)} }
    			    ]
    			});
    			Ext.getCmp("GroupValveRTAnalysisTableCalDataPanel_Id").add(GridPanel);
    		}else{
    			GridPanel.reconfigure(store);
    		}
    		
    		var acqGridPanel=Ext.getCmp("GroupValveAcqDataGridPanel_Id");
    		if(!isNotVal(acqGridPanel)){
    			acqGridPanel=Ext.create('Ext.grid.Panel', {
    				id:'GroupValveAcqDataGridPanel_Id',
    				border: false,
    				columnLines: true,
    				forceFit: false,
    				store: acqStore,
    			    columns: [
    			    	{ 
    			        	header: '名称',  
    			        	dataIndex: 'item',
    			        	align:'left',
    			        	flex:3,
    			        	renderer:function(value,o,p,e){
    			        		return "<span data-qtip=\""+(value==undefined?"":value)+"\">"+(value==undefined?"":value)+"</span>";
    			        	}
    			        },
    			        { 
    			        	header: '值', 
    			        	dataIndex: 'value',
    			        	align:'center',
    			        	flex:1,
    			        	renderer:function(value,o,p,e){
    			        		var AlarmShowStyle=Ext.JSON.decode(Ext.getCmp("AlarmShowStyle_Id").getValue());
    			        		var BackgroundColor='#FFFFFF';
    			        	 	var Color='#000000';
    			        	 	var Opacity=1;
    			        	 	var alarmLevel=p.data.alarmLevel;
    			        	 	if(value==undefined||value=="undefined"){
    			        	 		value="";
    			        		}
    			        	 	var tipval=value;
    			        		if(p.data.itemCode=="commStatus"){
    			        			if (alarmLevel == 0) {
    			        		 		BackgroundColor='#'+AlarmShowStyle.Normal.BackgroundColor;
    			        		 		Color='#'+AlarmShowStyle.Normal.Color;
    			        		 		Opacity=AlarmShowStyle.Normal.Opacity
    			        			}else if (alarmLevel == 100) {
    			        				BackgroundColor='#'+AlarmShowStyle.FirstLevel.BackgroundColor;
    			        		 		Color='#'+AlarmShowStyle.FirstLevel.Color;
    			        		 		Opacity=AlarmShowStyle.FirstLevel.Opacity
    			        			}else if (alarmLevel == 200) {
    			        				BackgroundColor='#'+AlarmShowStyle.SecondLevel.BackgroundColor;
    			        		 		Color='#'+AlarmShowStyle.SecondLevel.Color;
    			        		 		Opacity=AlarmShowStyle.SecondLevel.Opacity
    			        			}else if (alarmLevel == 300) {
    			        				BackgroundColor='#'+AlarmShowStyle.ThirdLevel.BackgroundColor;
    			        		 		Color='#'+AlarmShowStyle.ThirdLevel.Color;
    			        		 		Opacity=AlarmShowStyle.ThirdLevel.Opacity
    			        			}
    			        		}
    			        		var rgba=color16ToRgba(BackgroundColor,Opacity);
    			        	 	o.style='background-color:'+rgba+';color:'+Color+';';
    			        	 	return '<span data-qtip="'+tipval+'" data-dismissDelay=10000>'+value+'</span>';
    			        	}
    			        },
    			        { header: '趋势曲线', dataIndex: 'curve',align:'center',flex:1,renderer :function(value,e,o){return iconGroupValveAnalysisCurve(value,e,o)} }
    			    ]
    			});
    			Ext.getCmp("GroupValveRTAnalysisTableAcqDataPanel_Id").add(acqGridPanel);
    		}else{
    			acqGridPanel.reconfigure(acqStore);
    		}
    		
    		var controlGridPanel=Ext.getCmp("GroupValveControlDataGridPanel_Id");
    		if(!isNotVal(controlGridPanel)){
    			controlGridPanel=Ext.create('Ext.grid.Panel', {
    				id:'GroupValveControlDataGridPanel_Id',
    				requires: [
                       	'Ext.grid.selection.SpreadsheetModel',
                       	'Ext.grid.plugin.Clipboard'
                       	],
                    xtype:'spreadsheet-checked',
                    plugins: [
                        'clipboard',
                        'selectionreplicator',
                        new Ext.grid.plugin.CellEditing({
                      	  clicksToEdit:2
                        })
                    ],
    				border: false,
    				columnLines: true,
    				forceFit: false,
    				store: controlStore,
    			    columns: [
    			        { header: '操作项',  dataIndex: 'item',align:'left',flex:3},
    			        { header: '状态/值', dataIndex: 'value',align:'center',flex:1},
    			        { 	header: '操作', 
    			        	dataIndex: 'operation',
    			        	align:'center',
    			        	flex:1,
    			        	renderer :function(value,e,o){
//    			        		return iconGroupValveAnalysisCurve(value,e,o)
    			        		var id = e.record.id;
    			        		var item=o.data.item;
    			        		var commStatus = o.data.commStatus;
    			        		var isControl=o.data.isControl
    			        		var text="设置";
    			        		var hand=false;
    			        		var hidden=false;
    			        		if(commStatus==1&&isControl==1){
    			        			hand=false;
    			        		}else{
    			        			hand=true;
    			        		}
    			        		if(!o.data.operation){
    			        			hidden=true;
    			        		}
    		                    Ext.defer(function () {
    		                        Ext.widget('button', {
    		                            renderTo: id,
    		                            height: 18,
    		                            width: 50,
    		                            text: text,
    		                            disabled:hand,
    		                            hidden:hidden,
    		                            handler: function () {
    		                            	var operaName="是否执行"+text+item.split("(")[0]+"操作";
    		                            	 Ext.MessageBox.msgButtons['yes'].text = "<img   style=\"border:0;position:absolute;right:50px;top:1px;\"  src=\'" + context + "/images/zh_CN/accept.png'/>&nbsp;&nbsp;&nbsp;确定";
    		                                 Ext.MessageBox.msgButtons['no'].text = "<img   style=\"border:0;position:absolute;right:50px;top:1px;\"  src=\'" + context + "/images/zh_CN/cancel.png'/>&nbsp;&nbsp;&nbsp;取消";
    		                                 Ext.Msg.confirm("操作确认", operaName, function (btn) {
    		                                     if (btn == "yes") {
    		                                         var win_Obj = Ext.getCmp("GroupValveControlCheckPassWindow_Id")
    		                                         if (win_Obj != undefined) {
    		                                             win_Obj.destroy();
    		                                         }
    		                                         var GroupValveControlCheckPassWindow = Ext.create("AP.view.RealTimeEvaluation.GroupValveControlCheckPassWindow", {
    		                                             title: '控制'
    		                                         });
    		                                         
    		                                     	 var wellName  = Ext.getCmp("GroupValveAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
    		                                     	 Ext.getCmp("GroupValveControlGroupValveName_Id").setValue(wellName);
    		                                         Ext.getCmp("GroupValveControlType_Id").setValue(o.data.itemcode);
    		                                         Ext.getCmp("GroupValveControlShowType_Id").setValue(o.data.showType);
    		                                         if(o.data.itemcode=="baudrate"||o.data.itemcode=="baudrate2"
    		                                        	||o.data.itemcode=="instrumentCombinationMode1"
    		                                        	||o.data.itemcode=="instrumentCombinationMode2"
    		                                        	||o.data.itemcode=="instrumentCombinationMode3"
    		                                        	||o.data.itemcode=="instrumentCombinationMode4"){
    		                                        	 Ext.getCmp("GroupValveControlValue_Id").hide();
    		                                        	 Ext.getCmp("GroupValveControlTypeCombo_Id").setFieldLabel(o.data.item);
    		                                        	 var data=[];
    		                                        	 if(o.data.itemcode=="baudrate"||o.data.itemcode=="baudrate2"){
    		                                        		 data=[['0', '9600'], ['1', '19200']];
    		                                        	 }else if(o.data.itemcode=="instrumentCombinationMode1"
    		                                        		 ||o.data.itemcode=="instrumentCombinationMode2"
    		    		                                     ||o.data.itemcode=="instrumentCombinationMode3"
    		    		                                     ||o.data.itemcode=="instrumentCombinationMode4"){
    		                                        		 data=[['0', '仪表通讯关闭'], ['1', '天信仪表'], ['2', '创盛仪表'], ['3', '天信超声波']];
    		                                        	 }
    		                                        	 var controlTypeStore = new Ext.data.SimpleStore({
    		                                             	autoLoad : false,
    		                                                 fields: ['boxkey', 'boxval'],
    		                                                 data: data
    		                                             });
    		                                        	 Ext.getCmp("GroupValveControlTypeCombo_Id").setStore(controlTypeStore);
    		                                        	 Ext.getCmp("GroupValveControlTypeCombo_Id").setRawValue(o.data.value);
    		                                        	 Ext.getCmp("GroupValveControlTypeCombo_Id").show();
    		                                         }else{
    		                                        	 Ext.getCmp("GroupValveControlValue_Id").show();
    		                                        	 Ext.getCmp("GroupValveControlTypeCombo_Id").hide();
    		                                        	 Ext.getCmp("GroupValveControlValue_Id").setFieldLabel(o.data.item);
    		                                        	 Ext.getCmp("GroupValveControlValue_Id").setValue(o.data.value);
    		                                         }
    		                                         
    		                                         GroupValveControlCheckPassWindow.show();
    		                                     }
    		                                 });
    		                            }
    		                        });
    		                    }, 50);
    		                    return Ext.String.format('<div id="{0}"></div>', id);
    			        	} 
    			        }
    			    ]
    			});
    			Ext.getCmp("GroupValveRTAnalysisControlDataPanel_Id").add(controlGridPanel);
    		}else{
    			controlGridPanel.reconfigure(controlStore);
    		}
    		
        	
//    		Ext.getCmp("FSDiagramAnalysisSingleDetailsRightRunRangeTextArea_Id").setValue(get_rawData.runRange);
    		
        },
        beforeload: function (store, options) {
        	var id  = Ext.getCmp("GroupValveAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.id;
        	var wellName=Ext.getCmp('GroupValveRealtimeAnalysisGroupValveCom_Id').getValue();
        	var selectedWellName  = Ext.getCmp("GroupValveAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
        	var new_params = {
        			id: id,
        			wellName:wellName,
        			selectedWellName:selectedWellName
                };
           Ext.apply(store.proxy.extraParams, new_params);
        },
        datachanged: function (v, o) {
            //onLabelSizeChange(v, o, "statictisTotalsId");
        }
    }
});