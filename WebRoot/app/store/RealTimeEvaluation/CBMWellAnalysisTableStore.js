Ext.define('AP.store.RealTimeEvaluation.CBMWellAnalysisTableStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.diagnosisAnalysisTableStore',
    autoLoad: true,
    pageSize: 10000,
    proxy: {
        type: 'ajax',
        url: context + '/realTimeEvaluationController/getCBMWellAnalysisAndAcqAndControlData',
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
    		acqSataStr+="{\"item\":\"运行状态\",\"itemCode\":\"runStatus\",\"value\":\""+get_rawData.runStatusName+"\",\"alarmLevel\":"+get_rawData.runAlarmLevel+",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"气体质量流量计通讯状态\",\"itemCode\":\"gasFlowmeterCommStatus\",\"value\":\""+get_rawData.gasFlowmeterCommName+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"瞬时流量(m^3/h)\",\"itemCode\":\"gasInstantaneousFlow\",\"value\":\""+get_rawData.gasInstantaneousFlow+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"累积流量(m^3)\",\"itemCode\":\"gasCumulativeFlow\",\"value\":\""+get_rawData.gasCumulativeFlow+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"当前压力(Kpa)\",\"itemCode\":\"gasFlowmeterPress\",\"value\":\""+get_rawData.gasFlowmeterPress+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"量水仪通讯状态\",\"itemCode\":\"liquidFlowmeterCommStatus\",\"value\":\""+get_rawData.liquidFlowmeterCommName+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"瞬时流量(m^3/h)\",\"itemCode\":\"liquidInstantaneousflow\",\"value\":\""+get_rawData.liquidInstantaneousflow+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"累积流量(m^3)\",\"itemCode\":\"liquidCumulativeflow\",\"value\":\""+get_rawData.liquidCumulativeflow+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"日产量(m^3/d)\",\"itemCode\":\"liquidFlowmeterProd\",\"value\":\""+get_rawData.liquidFlowmeterProd+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"液面仪通讯状态\",\"itemCode\":\"fluidLevelIndicatorCommStatus\",\"value\":\""+get_rawData.fluidLevelIndicatorCommName+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"音速(m/s)\",\"itemCode\":\"soundVelocity\",\"value\":\""+get_rawData.soundVelocity+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"液面深度(m)\",\"itemCode\":\"fluidLevel\",\"value\":\""+get_rawData.fluidLevel+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"液面仪套压(Kpa)\",\"itemCode\":\"fluidLevelIndicatorPress\",\"value\":\""+get_rawData.fluidLevelIndicatorPress+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"变频器通讯状态\",\"itemCode\":\"vfdCommStatus\",\"value\":\""+get_rawData.vfdCommName+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"变频器状态字\",\"itemCode\":\"vfdStatus\",\"value\":\""+get_rawData.vfdStatusName+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"变频器状态字2\",\"itemCode\":\"vfdStatus2\",\"value\":\""+get_rawData.vfdStatus2Name+"\",\"curve\":\"\"},";
    		
    		acqSataStr+="{\"item\":\"运行频率(Hz)\",\"itemCode\":\"runFrequency\",\"value\":\""+get_rawData.runFrequency+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"冲次(1/min)\",\"itemCode\":\"SPM\",\"value\":\""+get_rawData.SPM+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"母线电压(V)\",\"itemCode\":\"vfdBusbarVoltage\",\"value\":\""+get_rawData.vfdBusbarVoltage+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"输出电压(V)\",\"itemCode\":\"vfdOutputVoltage\",\"value\":\""+get_rawData.vfdOutputVoltage+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"输出电流(A)\",\"itemCode\":\"vfdOutputCurrent\",\"value\":\""+get_rawData.vfdOutputCurrent+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"设定频率反馈(Hz)\",\"itemCode\":\"setFrequencyFeedback\",\"value\":\""+get_rawData.setFrequencyFeedback+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"故障代码\",\"itemCode\":\"vfdFaultCode\",\"value\":\""+get_rawData.vfdFaultCode+"\",\"curve\":\"\"},";
    		acqSataStr+="{\"item\":\"本地旋钮位置\",\"itemCode\":\"vfdPosition\",\"value\":\""+get_rawData.vfdPositionName+"\",\"curve\":\"\"}";
    		
    		if(get_rawData.vfdManufacturerCode==0){//如果没有变频器，显示AI1
    			acqSataStr+=",{\"item\":\"电流AI1(A)\",\"itemCode\":\"AI1\",\"value\":\""+get_rawData.AI1+"\",\"curve\":\"\"}";
    		}
    		
    		acqSataStr+="]}";
    		
    		var controlSataStr="{\"items\":[";
    		controlSataStr+="{\"item\":\"即时采集\",\"itemcode\":\"ImmediatelyAcquisition\",\"value\":\"\",\"commStatus\":"+get_rawData.commStatus+",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"启/停抽\",\"itemcode\":\"startOrStopWell\",\"value\":\""+get_rawData.runStatusName+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":0},";
    		controlSataStr+="{\"item\":\"频率/冲次控制方式\",\"itemcode\":\"frequencyOrSPMcontrolSign\",\"value\":\""+get_rawData.frequencyOrSPMcontrol+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"频率设定值(Hz)\",\"itemcode\":\"frequencySetValue\",\"value\":\""+get_rawData.frequencySetValue+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"冲次设定值(1/min)\",\"itemcode\":\"SPMSetValue\",\"value\":\""+get_rawData.SPMSetValue+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"10HZ对应冲次值(1/min)\",\"itemcode\":\"SPMBy10hz\",\"value\":\""+get_rawData.SPMBy10hz+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"50HZ对应冲次值(1/min)\",\"itemcode\":\"SPMBy50hz\",\"value\":\""+get_rawData.SPMBy50hz+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		
    		controlSataStr+="{\"item\":\"变频器厂家\",\"itemcode\":\"vfdManufacturerCode\",\"value\":\""+get_rawData.vfdManufacturerName+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":2},";
    		controlSataStr+="{\"item\":\"RTU地址\",\"itemcode\":\"rtuAddr\",\"value\":\""+get_rawData.rtuAddr+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1},";
    		controlSataStr+="{\"item\":\"程序版本号\",\"itemcode\":\"rtuProgramVersion\",\"value\":\""+get_rawData.rtuProgramVersion+"\",\"commStatus\":\""+get_rawData.commStatus+"\",\"operation\":true,\"isControl\":"+isControl+",\"showType\":1}";
    		
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
    		
    		var GridPanel=Ext.getCmp("CBMWellAnalysisDataGridPanel_Id");
    		if(!isNotVal(GridPanel)){
    			GridPanel=Ext.create('Ext.grid.Panel', {
    				id:'CBMWellAnalysisDataGridPanel_Id',
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
    			        { header: '趋势曲线', dataIndex: 'curve',align:'center',flex:1,renderer :function(value,e,o){return iconCBMAnalysisCurve(value,e,o)} }
    			    ]
    			});
    			Ext.getCmp("CBMWellRTAnalysisTableCalDataPanel_Id").add(GridPanel);
    		}else{
    			GridPanel.reconfigure(store);
    		}
    		
    		var acqGridPanel=Ext.getCmp("CBMWellAcqDataGridPanel_Id");
    		if(!isNotVal(acqGridPanel)){
    			acqGridPanel=Ext.create('Ext.grid.Panel', {
    				id:'CBMWellAcqDataGridPanel_Id',
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
    			        		if(p.data.itemCode=="commStatus"||p.data.itemCode=="runStatus"){
    			        			if(p.data.itemCode=="runStatus"&&value=="离线"){
    			        				return '';
    			        			}
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
    			        		}else if(p.data.itemCode=="gasFlowmeterCommStatus"||p.data.itemCode=="liquidFlowmeterCommStatus"||p.data.itemCode=="fluidLevelIndicatorCommStatus"||p.data.itemCode=="vfdCommStatus"){
    			        			if(value!="正常"&&value!=""){
    			        				BackgroundColor='#'+AlarmShowStyle.ThirdLevel.BackgroundColor;
    			        		 		Color='#'+AlarmShowStyle.ThirdLevel.Color;
    			        		 		Opacity=AlarmShowStyle.ThirdLevel.Opacity
    			        			}
    			        		}else if(p.data.itemCode=="vfdStatus"){
    			        			if(value!="运行"&value!=""){
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
    			        { header: '趋势曲线', dataIndex: 'curve',align:'center',flex:1,renderer :function(value,e,o){return iconCBMAnalysisCurve(value,e,o)} }
    			    ]
    			});
    			Ext.getCmp("CBMWellRTAnalysisTableAcqDataPanel_Id").add(acqGridPanel);
    		}else{
    			acqGridPanel.reconfigure(acqStore);
    		}
    		
    		var controlGridPanel=Ext.getCmp("CBMWellControlDataGridPanel_Id");
    		if(!isNotVal(controlGridPanel)){
    			controlGridPanel=Ext.create('Ext.grid.Panel', {
    				id:'CBMWellControlDataGridPanel_Id',
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
//    			        		return iconCBMAnalysisCurve(value,e,o)
    			        		var id = e.record.id;
    			        		var item=o.data.item;
    			        		var commStatus = o.data.commStatus;
    			        		var isControl=o.data.isControl
    			        		var text="";
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
    			        		if(o.data.itemcode==="startOrStopWell"){
    			        			if(o.data.value=="运行"){
    			        				text="停抽";
    			        			}else if(o.data.value=="停抽" ||o.data.value=="停止"){
    			        				text="启抽";
    			        				hand=false;
    			        			}else{
    			        				text="不可用";
    			        			}
    			        		}
    			        		else if(o.data.itemcode==="ImmediatelyAcquisition"){
    			        			text="即时采集";
    			        		}
    			        		else{
    			        			text="设置";
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
    		                            	var operaName="";
    		                            	if(text=="停抽"||text=="启抽"){
    		                            		operaName="是否执行"+text+"操作";
    		                            	}else{
    		                            		operaName="是否执行"+text+item.split("(")[0]+"操作";
    		                            	}
    		                            	 Ext.MessageBox.msgButtons['yes'].text = "<img   style=\"border:0;position:absolute;right:50px;top:1px;\"  src=\'" + context + "/images/zh_CN/accept.png'/>&nbsp;&nbsp;&nbsp;确定";
    		                                 Ext.MessageBox.msgButtons['no'].text = "<img   style=\"border:0;position:absolute;right:50px;top:1px;\"  src=\'" + context + "/images/zh_CN/cancel.png'/>&nbsp;&nbsp;&nbsp;取消";
    		                                 Ext.Msg.confirm("操作确认", operaName, function (btn) {
    		                                     if (btn == "yes") {
    		                                         var win_Obj = Ext.getCmp("WellControlCheckPassWindow_Id")
    		                                         if (win_Obj != undefined) {
    		                                             win_Obj.destroy();
    		                                         }
    		                                         var WellControlCheckPassWindow = Ext.create("AP.view.RealTimeEvaluation.WellControlCheckPassWindow", {
    		                                             title: '控制'
    		                                         });
    		                                         
    		                                     	 var wellName  = Ext.getCmp("CBMWellAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
    		                                     	 Ext.getCmp("CBMWellControlWellName_Id").setValue(wellName);
    		                                         Ext.getCmp("CBMWellControlType_Id").setValue(o.data.itemcode);
    		                                         Ext.getCmp("CBMWellControlShowType_Id").setValue(o.data.showType);
    		                                         if(o.data.itemcode=="startOrStopWell"){
    		                                        	 if(o.data.value=="运行"){
    		                                        		 Ext.getCmp("CBMWellControlValue_Id").setValue(2);
    		                                        	 }else if(o.data.value=="停抽" ||o.data.value=="停止"){
    		                                        		 Ext.getCmp("CBMWellControlValue_Id").setValue(1);
    		             			        			 }
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").hide();
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").hide();
    		                                         }else if(o.data.itemcode=="ImmediatelyAcquisition"){//即时采集
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").setValue(1);
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").hide();
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").hide();
    		                                         }else if(o.data.itemcode=="frequencyOrSPMcontrolSign"||o.data.itemcode=="vfdManufacturerCode"){
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").hide();
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").setFieldLabel(o.data.item);
    		                                        	 var data=[];
    		                                        	 if(o.data.itemcode=="frequencyOrSPMcontrolSign"){
    		                                        		 data=[['0', '冲次控制'], ['1', '频率控制']];
    		                                        	 }else if(o.data.itemcode=="vfdManufacturerCode"){
    		                                        		 data=[['0', '无变频器或变频器无通讯功能'], ['1', '英威腾'], ['2', '科陆新能'], ['3', '步科'], ['4', '汇川'], ['5', '信宇'], ['6', '科陆新能旧'], ['7', '日业电气'], ['8', '普传科技']];
    		                                        	 }
    		                                        	 var controlTypeStore = new Ext.data.SimpleStore({
    		                                             	autoLoad : false,
    		                                                 fields: ['boxkey', 'boxval'],
    		                                                 data: data
    		                                             });
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").setStore(controlTypeStore);
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").setRawValue(o.data.value);
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").show();
    		                                         }else{
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").show();
    		                                        	 Ext.getCmp("CBMWellControlTypeCombo_Id").hide();
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").setFieldLabel(o.data.item);
    		                                        	 Ext.getCmp("CBMWellControlValue_Id").setValue(o.data.value);
    		                                         }
    		                                         
    		                                         WellControlCheckPassWindow.show();
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
    			Ext.getCmp("CBMWellRTAnalysisControlDataPanel_Id").add(controlGridPanel);
    		}else{
    			controlGridPanel.reconfigure(controlStore);
    		}
    		
        	
//    		Ext.getCmp("FSDiagramAnalysisSingleDetailsRightRunRangeTextArea_Id").setValue(get_rawData.runRange);
    		
        },
        beforeload: function (store, options) {
        	var id  = Ext.getCmp("CBMWellAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.id;
        	var wellName=Ext.getCmp('CBMWellRealtimeAnalysisWellCom_Id').getValue();
        	var selectedWellName  = Ext.getCmp("CBMWellAnalysisSingleDetails_Id").getSelectionModel().getSelection()[0].data.wellName;
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