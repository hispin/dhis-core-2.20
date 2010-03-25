﻿Ext.BLANK_IMAGE_URL = '../resources/ext/resources/images/default/s.gif';

var MAP;
var BASECOORDINATE;
var MAPSOURCE;
var MAPDATA;
var URL;
var MAPVIEW;
var PARAMETER;
var BOUNDS = 0;
var ACTIVEPANEL;
var MASK;
var LABELS;
var COLORINTERPOLATION;
var EXPORTVALUES;

function getUrlParam(strParamName) {
    var output = '';
    var strHref = window.location.href;
    if ( strHref.indexOf('?') > -1 ) {
        var strQueryString = strHref.substr(strHref.indexOf('?')).toLowerCase();
        var aQueryString = strQueryString.split('&');
        for ( var iParam = 0; iParam < aQueryString.length; iParam++ ) {
            if (aQueryString[iParam].indexOf(strParamName.toLowerCase() + '=') > -1 ) {
                var aParam = aQueryString[iParam].split('=');
                output = aParam[1];
                break;
            }
        }
    }
    return unescape(output);
}

function validateInput(name) {
    return (name.length <= 25);
}

function getMultiSelectHeight() {
    var h = screen.height;
    
    if (h <= 800) {
        return 220;
    }
    else if (h <= 1050) {
        return 310;
    }
    else if (h <= 1200) {
        return 470;
    }
    else {
        return 900;
    }
}

function toggleFeatureLabels(classify) {
	var layer = MAP.getLayersByName('Thematic map')[0];
	
	function activateLabels() {
		layer.styleMap = new OpenLayers.StyleMap({
			'default': new OpenLayers.Style(
				OpenLayers.Util.applyDefaults(
					{'fillOpacity': 1, 'strokeColor': '#222222', 'strokeWidth': 1, 'label': '${' + MAPDATA.nameColumn + '}', 'fontFamily': 'arial, lucida sans unicode', 'fontWeight': 'bold', 'fontSize': 14 },
					OpenLayers.Feature.Vector.style['default']
				)
			),
			'select': new OpenLayers.Style(
				{'strokeColor': '#000000', 'strokeWidth': 2, 'cursor': 'pointer'}
			)
		});
		layer.refresh();
		LABELS = true;
	}
	
	function deactivateLabels() {
		layer.styleMap = new OpenLayers.StyleMap({
			'default': new OpenLayers.Style(
				OpenLayers.Util.applyDefaults(
					{'fillOpacity': 1, 'strokeColor': '#222222', 'strokeWidth': 1 },
					OpenLayers.Feature.Vector.style['default']
				)
			),
			'select': new OpenLayers.Style(
				{'strokeColor': '#000000', 'strokeWidth': 2, 'cursor': 'pointer'}
			)
		});
		layer.refresh();
		LABELS = false;
	}
	
	if (classify) {
		if (LABELS) {
			deactivateLabels();
		}
		else {
			activateLabels();
		}
		
		if (ACTIVEPANEL == thematicMap) {
			choropleth.classify(false, true);
		}
		else if (ACTIVEPANEL == organisationUnitAssignment) {
			mapping.classify(false, true);
		}
	}
	else {
		if (LABELS) {
			activateLabels();
		}
	}
}
			
Ext.onReady( function() {
	Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
    
    Ext.override(Ext.layout.FormLayout, {
        renderItem : function(c, position, target) {
            if (c && !c.rendered && c.isFormField && c.inputType != 'hidden') {
                var args = [
                    c.id, c.fieldLabel,
                    c.labelStyle||this.labelStyle||'',
                    this.elementStyle||'',
                    typeof c.labelSeparator == 'undefined' ? this.labelSeparator : c.labelSeparator,
                    (c.itemCls||this.container.itemCls||'') + (c.hideLabel ? ' x-hide-label' : ''),
                    c.clearCls || 'x-form-clear-left' 
                ];
                
                if (typeof position == 'number') {
                    position = target.dom.childNodes[position] || null;
                }
                
                if (position) {
                    c.formItem = this.fieldTpl.insertBefore(position, args, true);
                }
                else {
                    c.formItem = this.fieldTpl.append(target, args, true);
                }
                c.actionMode = 'formItem';
                c.render('x-form-el-'+c.id);
                c.container = c.formItem;
                c.actionMode = 'container';
            }
            else {
                Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
            }
        }
    });

    Ext.override(Ext.form.TriggerField, {
        actionMode: 'wrap',
        onShow: Ext.form.TriggerField.superclass.onShow,
        onHide: Ext.form.TriggerField.superclass.onHide
    });
    
    Ext.override(Ext.form.Checkbox, {
        onRender: function(ct, position) {
            Ext.form.Checkbox.superclass.onRender.call(this, ct, position);
            if(this.inputValue !== undefined) {
                this.el.dom.value = this.inputValue;
            }
            /*this.el.addClass('x-hidden');*/
            this.innerWrap = this.el.wrap({
                /*tabIndex: this.tabIndex,*/
                cls: this.baseCls+'-wrap-inner'
            });
            
            this.wrap = this.innerWrap.wrap({cls: this.baseCls+'-wrap'});
            
            this.imageEl = this.innerWrap.createChild({
                tag: 'img',
                src: Ext.BLANK_IMAGE_URL,
                cls: this.baseCls
            });
            
            if(this.boxLabel){
                this.labelEl = this.innerWrap.createChild({
                    tag: 'label',
                    htmlFor: this.el.id,
                    cls: 'x-form-cb-label',
                    html: this.boxLabel
                });
            }
            /*this.imageEl = this.innerWrap.createChild({
                tag: 'img',
                src: Ext.BLANK_IMAGE_URL,
                cls: this.baseCls
            }, this.el);*/
            if(this.checked) {
                this.setValue(true);
            }
            else {
                this.checked = this.el.dom.checked;
            }
            this.originalValue = this.checked;
        },
        afterRender: function() {
            Ext.form.Checkbox.superclass.afterRender.call(this);
            /*this.wrap[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);*/
            this.imageEl[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
        },
        initCheckEvents: function() {
            /*this.innerWrap.removeAllListeners();*/
            this.innerWrap.addClassOnOver(this.overCls);
            this.innerWrap.addClassOnClick(this.mouseDownCls);
            this.innerWrap.on('click', this.onClick, this);
            /*this.innerWrap.on('keyup', this.onKeyUp, this);*/
        },
        onFocus: function(e) {
            Ext.form.Checkbox.superclass.onFocus.call(this, e);
            /*this.el.addClass(this.focusCls);*/
            this.innerWrap.addClass(this.focusCls);
        },
        onBlur: function(e) {
            Ext.form.Checkbox.superclass.onBlur.call(this, e);
            /*this.el.removeClass(this.focusCls);*/
            this.innerWrap.removeClass(this.focusCls);
        },
        onClick: function(e) {
            if (e.getTarget().htmlFor != this.el.dom.id) {
                if (e.getTarget() !== this.el.dom) {
                    this.el.focus();
                }
                if (!this.disabled && !this.readOnly) {
                    this.toggleValue();
                }
            }
            /*e.stopEvent();*/
        },
        onEnable: Ext.form.Checkbox.superclass.onEnable,
        onDisable: Ext.form.Checkbox.superclass.onDisable,
        onKeyUp: undefined,
        setValue: function(v) {
            var checked = this.checked;
            this.checked = (v === true || v === 'true' || v == '1' || String(v).toLowerCase() == 'on');
            if (this.rendered) {
                this.el.dom.checked = this.checked;
                this.el.dom.defaultChecked = this.checked;
                /*this.wrap[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);*/
                this.imageEl[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
            }
            if (checked != this.checked) {
                this.fireEvent("check", this, this.checked);
                if(this.handler){
                    this.handler.call(this.scope || this, this, this.checked);
                }
            }
        },
        getResizeEl: function() {
            /*if(!this.resizeEl){
                this.resizeEl = Ext.isSafari ? this.wrap : (this.wrap.up('.x-form-element', 5) || this.wrap);
            }
            return this.resizeEl;*/
            return this.wrap;
        }
    });
    
    Ext.override(Ext.form.Radio, {
        checkedCls: 'x-form-radio-checked'
    });
    
//    document.body.oncontextmenu = function() { return false; };
	
	Ext.QuickTips.init();
    
    MAP = new OpenLayers.Map({
		controls: [
			new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.ArgParser(),
			new OpenLayers.Control.Attribution()
		]
	});
	
	MASK = new Ext.LoadMask(Ext.getBody(), {msg: 'Loading...', msgCls: 'x-mask-loading2'});
	
    if (getUrlParam('view')) {
        PARAMETER = getUrlParam('view');
    }
	
	var mapViewParam = PARAMETER ? PARAMETER : 0;
	
	Ext.Ajax.request({
		url: path + 'getBaseCoordinate' + type,
		method: 'GET',
		success: function(r) {
			var bc = Ext.util.JSON.decode( r.responseText ).baseCoordinate;
			BASECOORDINATE = {longitude:bc[0].longitude, latitude:bc[0].latitude};
			
			Ext.Ajax.request({
				url: path + 'getMapView' + type,
				method: 'GET',
				params: { id: mapViewParam },
				success: function(r) {
					var mst = Ext.util.JSON.decode(r.responseText).mapView[0].mapSourceType;
					
					Ext.Ajax.request({
						url: path + 'getMapSourceTypeUserSetting' + type,
						method: 'GET',
						success: function(r) {
							var ms = Ext.util.JSON.decode(r.responseText).mapSource;
							MAPSOURCE = PARAMETER ? mst : ms;
							
							Ext.Ajax.request({
								url: path + 'setMapSourceTypeUserSetting' + type,
								method: 'POST',
								params: { mapSourceType: MAPSOURCE },
								success: function() {
			
    /* VIEW PANEL */
	var viewStore = new Ext.data.JsonStore({
        url: path + 'getAllMapViews' + type,
        root: 'mapViews',
        fields: ['id', 'name'],
        id: 'id',
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
	
    var viewNameTextField = new Ext.form.TextField({
        id: 'viewname_tf',
        emptyText: '',
        width: combo_width,
		hideLabel: true
    });
    
    var viewComboBox = new Ext.form.ComboBox({
        id: 'view_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: viewStore
    });
    
    var view2ComboBox = new Ext.form.ComboBox({
        id: 'view2_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: viewStore
    });
    
    var newViewPanel = new Ext.form.FormPanel({
        id: 'newview_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [
            { html: '<div class="window-info">Saving current thematic map selection.</div>' },
            { html: '<div class="window-field-label-first">Display name</div>' },
			viewNameTextField,
			{
				xtype: 'button',
                id: 'newview_b',
				isFormField: true,
				hideLabel: true,
				cls: 'window-button',
				text: 'Save',
				handler: function() {
					var vn = Ext.getCmp('viewname_tf').getValue();
					var ig = Ext.getCmp('indicatorgroup_cb').getValue();
					var ii = Ext.getCmp('indicator_cb').getValue();
					var pt = Ext.getCmp('periodtype_cb').getValue();
					var p = Ext.getCmp('period_cb').getValue();
					var ms = Ext.getCmp('map_cb').getValue();
					var c = Ext.getCmp('numClasses').getValue();
					var ca = Ext.getCmp('colorA_cf').getValue();
					var cb = Ext.getCmp('colorB_cf').getValue();
					var lon = MAP.getCenter().lon;
					var lat = MAP.getCenter().lat;
					var zoom = parseInt(MAP.getZoom());
					
					if (!vn) {
						Ext.messageRed.msg('New map view', 'Map view form is not complete.');
						return;
					}
					
					if (!ig || !ii || !pt || !p || !ms || !c ) {
						Ext.messageRed.msg('New map view', 'Thematic map form is not complete.');
						return;
					}
					
					if (validateInput(vn) == false) {
						Ext.messageRed.msg('New map view', 'Map view name cannot be longer than 25 characters.');
						return;
					}
					
					Ext.Ajax.request({
						url: path + 'getAllMapViews' + type,
						method: 'GET',
						success: function(r) {
							var mapViews = Ext.util.JSON.decode( r.responseText ).mapViews;
							
							for (var i = 0; i < mapViews.length; i++) {
								if (mapViews[i].name == vn) {
									Ext.messageRed.msg('New map view', 'There is already a map view called <span class="x-msg-hl">' + vn + '</span>.');
									return;
								}
							}
					
							Ext.Ajax.request({
								url: path + 'addOrUpdateMapView' + type,
								method: 'POST',
								params: { name: vn, indicatorGroupId: ig, indicatorId: ii, periodTypeId: pt, periodId: p, mapSource: ms, method: 2, classes: c, colorLow: ca, colorHigh: cb, longitude: lon, latitude: lat, zoom: zoom },

								success: function(r) {
									Ext.messageBlack.msg('New map view', 'The view <span class="x-msg-hl">' + vn + '</span> was registered.');
									Ext.getCmp('view_cb').getStore().reload();
									Ext.getCmp('mapview_cb').getStore().reload();
									Ext.getCmp('viewname_tf').reset();
								},
								failure: function() {
									alert( 'Error: addOrUpdateMapView' );
								}
							});
						},
						failure: function() {
									alert( 'Error: getAllMapViews' );
						}
					});
				}
			}
        ]
    });
    
    var deleteViewPanel = new Ext.form.FormPanel({   
        id: 'deleteview_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">View</div>' },
			viewComboBox,
			{
				xtype: 'button',
                id: 'deleteview_b',
				isFormField: true,
				hideLabel: true,
				text: 'Delete',
				cls: 'window-button',
				handler: function() {
					var v = Ext.getCmp('view_cb').getValue();
					var name = Ext.getCmp('view_cb').getStore().getById(v).get('name');
					
					if (!v) {
						Ext.messageRed.msg('Delete map view', 'Please select a map view.');
						return;
					}
					
					Ext.Ajax.request({
						url: path + 'deleteMapView' + type,
						method: 'POST',
						params: { id: v },

						success: function(r) {
							Ext.messageBlack.msg('Delete map view', 'The map view <span class="x-msg-hl">' + name + '</span> was deleted.');
							Ext.getCmp('view_cb').getStore().reload();
							Ext.getCmp('view_cb').reset();
							Ext.getCmp('mapview_cb').getStore().reload();
						},
						failure: function() {
							alert( 'Status', 'Error while saving data' );
						}
					});
				}
			}
        ]
    });
    
    var dashboardViewPanel = new Ext.form.FormPanel({   
        id: 'dashboardview_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">View</div>' },
			view2ComboBox,
			{
				xtype: 'button',
                id: 'dashboardview_b',
				isFormField: true,
				hideLabel: true,
				text: 'Add to DHIS dashboard',
				cls: 'window-button',
				handler: function() {
					var v2 = Ext.getCmp('view2_cb').getValue();
					var nv = Ext.getCmp('view2_cb').getRawValue();
					
					if (!v2) {
						Ext.messageRed.msg('Dashboard map view', 'Please select a map view.');
						return;
					}
					
					Ext.Ajax.request({
						url: path + 'addMapViewToDashboard' + type,
						method: 'POST',
						params: { id: v2 },

						success: function(r) {
							Ext.messageBlack.msg('Dashboard map view', 'The view <span class="x-msg-hl">' + nv + '</span> was added to dashboard.');
							
							Ext.getCmp('view_cb').getStore().reload();
							Ext.getCmp('view_cb').reset();
							Ext.getCmp('mapview_cb').getStore().reload();
						},
						failure: function() {
							alert( 'Status', 'Error while saving data' );
						}
					});
				}
			}
        ]
    });
    
	var viewWindow = new Ext.Window({
        id: 'view_w',
        title: '<span id="window-favorites-title">Favorites</span>',
		layout: 'fit',
        closeAction: 'hide',
		width: 234,
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
				layoutOnTabChange: true,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px; border:0px'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        if (tab.id == 'view0') { 
                            viewWindow.setHeight(188);
                        }
                        else if (tab.id == 'view1') {
                            viewWindow.setHeight(150);
                        }
                        else if (tab.id == 'view2') {
                            viewWindow.setHeight(150);
                        }
                    }
                },
                items:
                [
                    {
                        title: '<span class="panel-tab-title">New</span>',
                        id: 'view0',
                        items:
                        [
							newViewPanel
                        ]
                    },
                    {
                        title: '<span class="panel-tab-title">Delete</span>',
                        id: 'view1',
                        items:
                        [
                            deleteViewPanel
                        ]
                    },
                    {
                        title: '<span class="panel-tab-title">Add to dashboard</span>',
                        id: 'view2',
                        items:
                        [
                            dashboardViewPanel
                        ]
                    }
                ]
            }
        ],
		listeners: {
			'hide': {
				fn: function() {
					mapping.relation = false;
				}
			}
		}
    });
	
	/* EXPORT MAP PANEL */
	var exportImagePanel = new Ext.form.FormPanel({
        id: 'export_image_p',        
        items:
        [
			{
				xtype: 'textfield',
				id: 'export_image_title',
				fieldLabel: 'Image title',
				labelSeparator: labelseparator,
				editable: true,
				valueField: 'id',
				displayField: 'text',
				isFormField: true,
				width: combo_width_fieldset,
				minListWidth: combo_list_width_fieldset,
				mode: 'local',
				triggerAction: 'all',
				value: 'Image Title'						
			},
			{
				xtype: 'numberfield',
				id: 'export_image_width',
				fieldLabel: 'Image width',
				labelSeparator: labelseparator,
				editable: true,
				valueField: 'id',
				displayField: 'text',
				isFormField: true,
				width: combo_width_fieldset,
				minListWidth: combo_list_width_fieldset,
				mode: 'local',
				triggerAction: 'all',
				value: 1000
						
			},			
			{
				xtype: 'numberfield',
				id: 'export_image_height',
				fieldLabel: 'Image height',
				labelSeparator: labelseparator,
				editable: true,
				valueField: 'id',
				displayField: 'text',
				isFormField: true,
				width: combo_width_fieldset,
				minListWidth: combo_list_width_fieldset,
				mode: 'local',
				triggerAction: 'all',
				value: 1000						
			},
			{
				xtype: 'checkbox',
				id: 'export_image_include_legend',
				fieldLabel: 'Include legend',
				labelSeparator: '',				
				isFormField: true		
			},
			{
				xtype: 'button',
                id: 'export_image_button',
				isFormField: true,
				labelSeparator: labelseparator,
				hideLabel: false,
				cls: 'window-button',
				text: 'Export Image',
				handler: function() {					
					MASK.msg = 'Exporting image...';
					MASK.show();
					var svg = document.getElementById('OpenLayers.Layer.Vector_17').innerHTML;
					var title = Ext.getCmp('export_image_title').getValue();
					var w = Ext.getCmp('export_image_width').getValue();
					var h = Ext.getCmp('export_image_height').getValue();
					var includeLegend = Ext.getCmp('export_image_include_legend').getValue();
					var period = Ext.getCmp('period_cb').getValue();
					var indicator = Ext.getCmp('indicator_cb').getValue();
					Ext.Ajax.request({
						url: path + 'exportImage' + type,
						method: 'POST',
						params: { 
								title: title,
								svg: svg,
								width: w,
								height: h,
								includeLegend: includeLegend,
								period: period,
								indicator: indicator
						},
						success: function( responseObject ) {
							MASK.hide();
							var file =  Ext.util.JSON.decode(responseObject.responseText).file;
							window.open(path + "download" + type + "?path=" + file + "&outputFormat=application/image" );
						}
					});						
						
				}
			}	
		]
	});
	
	var exportExcelPanel = new Ext.form.FormPanel({
        id: 'export_excel_p',        
        items:
        [
			{
				xtype: 'textfield',
				id: 'export_excel_title',
				fieldLabel: 'Map title',
				labelSeparator: labelseparator,
				editable: true,
				valueField: 'id',
				displayField: 'text',
				isFormField: true,
				width: combo_width_fieldset,
				minListWidth: combo_list_width_fieldset,
				mode: 'local',
				triggerAction: 'all',
				value: 'Map Title'						
			},	
			{
				xtype: 'checkbox',
				id: 'export_excel_include_legend',
				fieldLabel: 'Include legend',				
				isFormField: true							
			},	
			{
				xtype: 'checkbox',
				id: 'export_excel_include_value',
				fieldLabel: 'Include values',
				labelSeparator: '',				
				isFormField: true								
			},
			{
				xtype: 'button',
                id: 'export_excel_button',
				isFormField: true,
				labelSeparator: labelseparator,
				hideLabel: false,
				cls: 'window-button',
				text: 'Export Excel',
				handler: function() {					
					MASK.msg = 'Exporting excel...';
					MASK.show();
					var title = Ext.getCmp('export_excel_title').getValue();
					var svg = document.getElementById('OpenLayers.Layer.Vector_17').innerHTML;					
					var includeLegend = Ext.getCmp('export_image_include_legend').getValue();
					var includeValues = Ext.getCmp('export_excel_include_value').getValue();
					var period = Ext.getCmp('period_cb').getValue();
					var indicator = Ext.getCmp('indicator_cb').getValue();					
					Ext.Ajax.request({
						url: path + 'exportExcel' + type,
						method: 'POST',
						params: { 	
								title: title,
								width:500,
								height:500,
								svg: svg,							
								includeLegend: includeLegend,
								includeValues: includeValues,
								period: period,
								indicator: indicator,
								datavalues: EXPORTVALUES								
						},
						success: function( responseObject ) {
							MASK.hide();
							var file =  Ext.util.JSON.decode(responseObject.responseText).file;
							window.open(path + "download" + type + "?path=" + file + "&outputFormat=application/ms-excel" );
						}
					});						
						
				}
			}	
		]
	});
	
	/* EXPORT MAP WINDOW */
	var exportMapWindow = new Ext.Window({
        id: 'view_export_map_w',
        title: '<span id="window-export-map-title">Export Map</span>',
		layout: 'fit',
        closeAction: 'hide',
		width: 250,
        items:
        [            
		   {
                xtype: 'tabpanel',
                activeTab: 0,
				layoutOnTabChange: true,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px; border:0px'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        if (tab.id == 'export-image') { 
                            exportMapWindow.setHeight(220);
                        }
                        else if (tab.id == 'export-excel') {
                            exportMapWindow.setHeight(200);
                        }                        
                    }
                },
                items:
                [
                    {
                        title: '<span class="panel-tab-title">Export Image</span>',
                        id: 'export-image',
                        items:
                        [
							exportImagePanel							
                        ]
                    },
					{
                        title: '<span class="panel-tab-title">Export Excel</span>',
                        id: 'export-excel',
                        items:
                        [
							exportExcelPanel							
                        ]
                    }
                ]
            }
        ],
		listeners: {
			'hide': {
				fn: function() {
					mapping.relation = false;
				}
			}
		}
    });
    
    /* AUTOMATIC MAP LEGEND SET PANEL */
    var automaticMapLegendSetNameTextField = new Ext.form.TextField({
        id: 'automaticmaplegendsetname_tf',
		isFormField: true,
		hideLabel: true,
        emptyText: emptytext,
        width: combo_width
    });
    
    var automaticMapLegendSetMethodComboBox = new Ext.form.ComboBox({
        id: 'automaticmaplegendsetmethod_cb',
		isFormField: true,
		hideLabel: true,
        editable: false,
        valueField: 'value',
        displayField: 'text',
        mode: 'local',
        emptyText: emptytext,
        triggerAction: 'all',
        width: combo_width,
        minListWidth: combo_list_width,
        store: new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data: [[2, 'Distributed values'], [1, 'Equal intervals']]
        })
    });
    
    var automaticMapLegendSetClassesComboBox = new Ext.form.ComboBox({
        id: 'automaticmaplegendsetclasses_cb',
		isFormField: true,
		hideLabel: true,
        editable: false,
        valueField: 'value',
        displayField: 'value',
        mode: 'local',
        emptyText: emptytext,
        triggerAction: 'all',
		value: 5,
        width: combo_number_width,
        minListWidth: combo_number_list_width,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[1], [2], [3], [4], [5], [6], [7], [8]]
        })
    });
    
    var automaticMapLegendSetLowColorColorPalette = new Ext.ux.ColorField({
        id: 'automaticmaplegendsetlowcolor_cp',
		isFormField: true,
		hideLabel: true,
        allowBlank: false,
        width: combo_width,
        minListWidth: combo_list_width,
        value: "#FFFF00"
    });
    
    var automaticMapLegendSetHighColorColorPalette = new Ext.ux.ColorField({
        id: 'automaticmaplegendsethighcolor_cp',
		isFormField: true,
		hideLabel: true,
        allowBlank: false,
        width: combo_width,
        minListWidth: combo_list_width,
        value: "#FF0000"
    });
        
    var automaticMapLegendSetStore = new Ext.data.JsonStore({
        url: path + 'getMapLegendSetsByType' + type,
		baseParams: { type: map_legend_type_automatic },
        root: 'mapLegendSets',
		id: 'id',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
	
	var automaticMapLegendSetComboBox = new Ext.form.ComboBox({
        id: 'automaticmaplegendset_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: automaticMapLegendSetStore,
		listeners:{
			'select': {
				fn: function() {
					var lsid = Ext.getCmp('automaticmaplegendset_cb').getValue();
					
					Ext.Ajax.request({
						url: path + 'getMapLegendSetIndicators' + type,
						method: 'POST',
						params: { id:lsid },
						success: function(r) {
							var indicators = Ext.util.JSON.decode(r.responseText).mapLegendSet[0].indicators;
							var indicatorString = '';
							
							for (var i = 0; i < indicators.length; i++) {
								indicatorString += indicators[i];
								if (i < indicators.length-1) {
									indicatorString += ',';
								}
							}
							
							Ext.getCmp('automaticmaplegendsetindicator_ms').setValue(indicatorString);							
						},
						failure: function() {
							alert( 'Status', 'Error while saving data' );
						}
					});
				}
			}
		}					
    });

    var automaticMapLegendSetIndicatorStore = new Ext.data.JsonStore({
        url: path + 'getAllIndicators' + type,
        root: 'indicators',
        fields: ['id', 'name', 'shortName'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var automaticMapLegendSetIndicatorMultiSelect = new Ext.ux.Multiselect({
        id: 'automaticmaplegendsetindicator_ms',
		isFormField: true,
		hideLabel: true,
        dataFields: ['id', 'name', 'shortName'], 
        valueField: 'id',
        displayField: 'shortName',
        width: multiselect_width,
        height: getMultiSelectHeight(),
        store: automaticMapLegendSetIndicatorStore
    });
	    
    var automaticMapLegendSet2ComboBox = new Ext.form.ComboBox({
        id: 'automaticmaplegendset2_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: automaticMapLegendSetStore
    });
	
	var newAutomaticMapLegendSetPanel = new Ext.form.FormPanel({   
        id: 'newautomaticmaplegendset_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">Display name</div>' },
            automaticMapLegendSetNameTextField,
/*            { html: '<p style="padding-bottom:4px; color:' + MENU_TEXTCOLOR + ';">&nbsp;Method</p>' }, legendSetMethodComboBox, { html: '<br>' },*/
            { html: '<div class="window-field-label">Classes</div>' },
            automaticMapLegendSetClassesComboBox,
            { html: '<div class="window-field-label">Lowest value color</div>' },
            automaticMapLegendSetLowColorColorPalette,
            { html: '<div class="window-field-label">Highest value color</div>' },
            automaticMapLegendSetHighColorColorPalette,
            {
                xtype: 'button',
                id: 'newautomaticmaplegendset_b',
				isFormField: true,
				hideLabel: true,
                text: 'Save',
				cls: 'window-button',
                handler: function() {
                    var ln = Ext.getCmp('automaticmaplegendsetname_tf').getValue();
        /*            var lm = Ext.getCmp('automaticmaplegendsetmethod_cb').getValue();*/
                    var lc = Ext.getCmp('automaticmaplegendsetclasses_cb').getValue();            
                    var llc = Ext.getCmp('automaticmaplegendsetlowcolor_cp').getValue();
                    var lhc = Ext.getCmp('automaticmaplegendsethighcolor_cp').getValue();
                    
                    if (!ln || !lc) {
                        Ext.messageRed.msg('New legend set', 'Form is not complete.');
                        return;
                    }
                    
                    if (validateInput(ln) == false) {
                        Ext.messageRed.msg('New legend set', 'Legend set name cannot be longer than 25 characters.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'getAllMapLegendSets' + type,
                        method: 'GET',
						success: function(r) {
                            var mapLegendSets = Ext.util.JSON.decode(r.responseText).mapLegendSets;
                            for (var i = 0; i < mapLegendSets.length; i++) {
                                if (ln == mapLegendSets[i].name) {
                                    Ext.messageRed.msg('New legend set', 'A legend set called <span class="x-msg-hl">' + ln + '</span> already exists.');
                                    return;
                                }
                            }
                            
                            Ext.Ajax.request({
                                url: path + 'addOrUpdateMapLegendSet' + type,
                                method: 'POST',
                                params: { name: ln, type: map_legend_type_automatic, method: 2, classes: lc, colorLow: llc, colorHigh: lhc },
                                success: function(r) {
                                    Ext.messageBlack.msg('New legend set', 'The legend set <span class="x-msg-hl">' + ln + '</span> was registered.');
                                    Ext.getCmp('automaticmaplegendset_cb').getStore().reload();
                                    Ext.getCmp('automaticmaplegendsetname_tf').reset();
                                    Ext.getCmp('automaticmaplegendsetclasses_cb').reset();
                                    Ext.getCmp('automaticmaplegendsetlowcolor_cp').reset();
                                    Ext.getCmp('automaticmaplegendsethighcolor_cp').reset();
                                },
                                failure: function() {
                                    alert( 'Status', 'Error while saving data' );
                                }
                            });
                        },
                        failure: function() {
                            alert( 'Error: getAllMapLegendSets' );
                        }
                    });
                }
            }
        ]	
    });
	
	var assignAutomaticMapLegendSetPanel = new Ext.form.FormPanel({   
        id: 'assignautomaticmaplegendset_p',
		bodyStyle: 'border:0px',
        items:
        [   
            { html: '<div class="window-field-label-first">Legend set</div>' },
            automaticMapLegendSetComboBox,
            { html: '<div class="window-field-label">Indicators</div>' },
			automaticMapLegendSetIndicatorMultiSelect,
            {
                xtype: 'button',
                id: 'assignautomaticmaplegendset_b',
                text: 'Assign to indicators',
				cls: 'window-button',
                handler: function() {
                    var ls = Ext.getCmp('automaticmaplegendset_cb').getValue();
                    var lsrw = Ext.getCmp('automaticmaplegendset_cb').getRawValue();
                    var lims = Ext.getCmp('automaticmaplegendsetindicator_ms').getValue();
                    
                    if (!ls) {
                        Ext.messageRed.msg('Assign to indicators', 'Please select a legend set.');
                        return;
                    }
                    
                    if (!lims) {
                        Ext.messageRed.msg('Link legend set to indicator', 'Please select at least one indicator.');
                        return;
                    }
                    
                    var array = new Array();
                    array = lims.split(',');
                    var params = '?indicators=' + array[0];
                    
                    if (array.length > 1) {
                        for (var i = 1; i < array.length; i++) {
                            array[i] = '&indicators=' + array[i];
                            params += array[i];
                        }
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'assignIndicatorsToMapLegendSet.action' + params,
                        method: 'POST',
                        params: { id: ls },

                        success: function(r) {
                            Ext.messageBlack.msg('Assign to indicators', 'The legend set <span class="x-msg-hl">' + lsrw + '</span> was updated.');
                            Ext.getCmp('automaticmaplegendset_cb').getStore().reload();
                        },
                        failure: function() {
                            alert( 'Error: assignIndicatorsToMapLegendSet' );
                        }
                    });
                }
            }
        ]
    });
    
    var deleteAutomaticMapLegendSetPanel = new Ext.form.FormPanel({
        id: 'deleteautomaticmaplegendset_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">Legend set</p>' },
            automaticMapLegendSet2ComboBox,
            {
                xtype: 'button',
                id: 'deleteautomaticmaplegendset_b',
                text: 'Delete',
				cls: 'window-button',
                handler: function() {
                    var ls = Ext.getCmp('automaticmaplegendset2_cb').getValue();
                    var lsrw = Ext.getCmp('automaticmaplegendset2_cb').getRawValue();
                    
                    if (!ls) {
                        Ext.messageRed.msg('Delete legend set', 'Please select a legend set.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'deleteMapLegendSet' + type,
                        method: 'GET',
                        params: { id: ls },
                        success: function(r) {
                            Ext.messageBlack.msg('Delete legend set', 'The legend set <span class="x-msg-hl">' + lsrw + '</span> was deleted.');
                            Ext.getCmp('automaticmaplegendset_cb').getStore().reload();
                            Ext.getCmp('automaticmaplegendset_cb').reset();
							Ext.getCmp('automaticmaplegendset2_cb').reset();
                            Ext.getCmp('automaticmaplegendsetindicator_ms').reset();
                        },
                        failure: function() {
                            alert( 'Status', 'Error while saving data' );
                        }
                    });
                }
            }
        ]
    });
    
    var automaticMapLegendSetWindow = new Ext.Window({
        id: 'automaticmaplegendset_w',
        title: '<span id="window-legendset-title">Automatic legend sets</span>',
		layout: 'fit',
        closeAction: 'hide',
		width: 245,
        items:
        [
			{
				xtype: 'tabpanel',
				activeTab: 0,
				layoutOnTabChange: true,
				deferredRender: false,
				plain: true,
				defaults: {layout: 'fit', bodyStyle: 'padding:8px; border:0px'},
				listeners: {
					tabchange: function(panel, tab)
					{
						var w = Ext.getCmp('automaticmaplegendset_w');
						
						if (tab.id == 'automaticmaplegendset0') { 
							w.setHeight(306);
						}
						else if (tab.id == 'automaticmaplegendset1') {
							w.setHeight(getMultiSelectHeight() + 180);
						}
						else if (tab.id == 'automaticmaplegendset2') {
							w.setHeight(149);
						}
					}
				},
				items:
				[
					{
						title: '<span class="panel-tab-title">New</span>',
						id: 'automaticmaplegendset0',
						items:
						[
							newAutomaticMapLegendSetPanel
						]
					},
					{
						title: '<span class="panel-tab-title">Assign to indicators</span>',
						id: 'automaticmaplegendset1',
						items:
						[
							assignAutomaticMapLegendSetPanel
						]
					},
					{
						title: '<span class="panel-tab-title">Delete</span>',
						id: 'automaticmaplegendset2',
						items:
						[
							deleteAutomaticMapLegendSetPanel
						]
					}
				]
			}
        ]
    });
	
	/* PREDEFINED MAP LEGEND SET PANEL */
	var predefinedMapLegendStore = new Ext.data.JsonStore({
        url: path + 'getAllMapLegends' + type,
        root: 'mapLegends',
		id: 'id',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
	
	var predefinedMapLegendSetStore = new Ext.data.JsonStore({
        url: path + 'getMapLegendSetsByType' + type,
		baseParams: { type: map_legend_type_predefined },
        root: 'mapLegendSets',
		id: 'id',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
	
	var predefinedMapLegendNameTextField = new Ext.form.TextField({
		id: 'predefinedmaplegendname_tf',
		isFormField: true,
		hideLabel: true,
		emptyText: emptytext,
		width: combo_width
	});
	
	var predefinedMapLegendStartValueTextField = new Ext.form.TextField({
		id: 'predefinedmaplegendstartvalue_tf',
		isFormField: true,
		hideLabel: true,
		emptyText: emptytext,
		width: combo_number_width,
		minListWidth: combo_number_list_width
	});
	
	var predefinedMapLegendEndValueTextField = new Ext.form.TextField({
		id: 'predefinedmaplegendendvalue_tf',
		isFormField: true,
		hideLabel: true,
		emptyText: emptytext,
		width: combo_number_width,
		minListWidth: combo_number_list_width
	});
	
    var predefinedMapLegendColorColorPalette = new Ext.ux.ColorField({
		id: 'predefinedmaplegendcolor_cp',
		isFormField: true,
		hideLabel: true,
		allowBlank: false,
		width: combo_width,
		minListWidth: combo_list_width,
		value: "#FFFF00"
	});
	
	var predefinedMapLegendComboBox = new Ext.form.ComboBox({
        id: 'predefinedmaplegend_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: predefinedMapLegendStore
    });
	
	var predefinedMapLegendSetNameTextField = new Ext.form.TextField({
		id: 'predefinedmaplegendsetname_tf',
		isFormField: true,
		hideLabel: true,
		emptyText: emptytext,
		width: combo_width
	});
	
	var predefinedNewMapLegendMultiSelect = new Ext.ux.Multiselect({
        id: 'predefinednewmaplegend_ms',
		isFormField: true,
		hideLabel: true,
        dataFields: ['id', 'name', 'startValue', 'endValue', 'color'], 
        valueField: 'id',
        displayField: 'name',
        width: multiselect_width,
        height: getMultiSelectHeight(),
        store: predefinedMapLegendStore
    });
	
	var predefinedMapLegendSetComboBox = new Ext.form.ComboBox({
        id: 'predefinedmaplegendset_cb',
		isFormField: true,
		hideLabel: true,
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: predefinedMapLegendSetStore
    });
	
	var newPredefinedMapLegendPanel = new Ext.form.FormPanel({   
        id: 'newpredefinedmaplegend_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">Display name</div>' },
            predefinedMapLegendNameTextField,
            { html: '<div class="window-field-label">Start value</div>' },
            predefinedMapLegendStartValueTextField,
            { html: '<div class="window-field-label">End value</div>' },
            predefinedMapLegendEndValueTextField,
            { html: '<div class="window-field-label">Color</div>' },
            predefinedMapLegendColorColorPalette,
            {
                xtype: 'button',
                id: 'newpredefinedmaplegend_b',
				isFormField: true,
				hideLabel: true,
                text: 'Save',
				cls: 'window-button',
                handler: function() {
                    var mln = Ext.getCmp('predefinedmaplegendname_tf').getValue();
                    var mlsv = Ext.getCmp('predefinedmaplegendstartvalue_tf').getValue();            
                    var mlev = Ext.getCmp('predefinedmaplegendendvalue_tf').getValue();
                    var mlc = Ext.getCmp('predefinedmaplegendcolor_cp').getValue();
                    
                    if (!mln || !mlsv || !mlev || !mlc) {
                        Ext.messageRed.msg('New legend', 'Form is not complete.');
                        return;
                    }
                    
                    if (validateInput(mln) == false) {
                        Ext.messageRed.msg('New legend set', 'Legend name cannot be longer than 25 characters.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'getAllMapLegends' + type,
                        method: 'GET',
						success: function(r) {
                            var mapLegends = Ext.util.JSON.decode(r.responseText).mapLegends;
                            for (var i = 0; i < mapLegends.length; i++) {
                                if (mln == mapLegends[i].name) {
                                    Ext.messageRed.msg('New legend', 'A legend called <span class="x-msg-hl">' + ln + '</span> already exists.');
                                    return;
                                }
                            }
                            
                            Ext.Ajax.request({
                                url: path + 'addOrUpdateMapLegend' + type,
                                method: 'POST',
                                params: { name: mln, startValue: mlsv, endValue: mlev, color: mlc },
                                success: function(r) {
                                    Ext.messageBlack.msg('New legend', 'The legend <span class="x-msg-hl">' + mln + '</span> was registered.');
                                    Ext.getCmp('predefinedmaplegend_cb').getStore().reload();
                                    Ext.getCmp('predefinedmaplegendname_tf').reset();
                                    Ext.getCmp('predefinedmaplegendstartvalue_tf').reset();
                                    Ext.getCmp('predefinedmaplegendendvalue_tf').reset();
                                    Ext.getCmp('predefinedmaplegendcolor_cp').reset();
                                },
                                failure: function() {
                                    alert( 'Error: addOrUpdateMapLegend' );
                                }
                            });
                        },
                        failure: function() {
                            alert( 'Error: getAllMapLegends' );
                        }
                    });
                }
            }
        ]	
    });
	
	var deletePredefinedMapLegendPanel = new Ext.form.FormPanel({
        id: 'deletepredefinedmaplegend_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">Legend</p>' },
            predefinedMapLegendComboBox,
            {
                xtype: 'button',
                id: 'deletepredefinedmaplegend_b',
                text: 'Delete',
				cls: 'window-button',
                handler: function() {
                    var mlv = Ext.getCmp('predefinedmaplegend_cb').getValue();
                    var mlrv = Ext.getCmp('predefinedmaplegend_cb').getRawValue();
                    
                    if (!mlv) {
                        Ext.messageRed.msg('Delete legend', 'Please select a legend.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'deleteMapLegend' + type,
                        method: 'POST',
                        params: { id: mlv },
                        success: function(r) {
                            Ext.messageBlack.msg('Delete legend', 'The legend <span class="x-msg-hl">' + mlrv + '</span> was deleted.');
                            Ext.getCmp('predefinedmaplegend_cb').getStore().reload();
                            Ext.getCmp('predefinedmaplegend_cb').reset();
                        },
                        failure: function() {
                            alert( 'Error: deleteMapLegend' );
                        }
                    });
                }
            }
        ]
    });
	
	var newPredefinedMapLegendSetPanel = new Ext.form.FormPanel({   
        id: 'newpredefinedmaplegendset_p',
		bodyStyle: 'border:0px',
        items:
        [   
            { html: '<div class="window-field-label-first">Display name</div>' },
            predefinedMapLegendSetNameTextField,
            { html: '<div class="window-field-label">Legends</div>' },
			predefinedNewMapLegendMultiSelect,
            {
                xtype: 'button',
                id: 'newpredefinedmaplegendset_b',
                text: 'Save',
				cls: 'window-button',
                handler: function() {
                    var mlsv = Ext.getCmp('predefinedmaplegendsetname_tf').getValue();
                    var mlms = Ext.getCmp('predefinednewmaplegend_ms').getValue();
                    
                    if (!mlsv) {
                        Ext.messageRed.msg('New legend set', 'Please select a legend set.');
                        return;
                    }
                    
                    if (!mlms) {
                        Ext.messageRed.msg('New legend set', 'Please select at least one legend.');
                        return;
                    }
                    
                    var array = new Array();
                    array = mlms.split(',');
                    var params = '?mapLegends=' + array[0];
                    
                    if (array.length > 1) {
                        for (var i = 1; i < array.length; i++) {
                            array[i] = '&mapLegends=' + array[i];
                            params += array[i];
                        }
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'addOrUpdateMapLegendSet.action' + params,
                        method: 'POST',
                        params: { name: mlsv, type: map_legend_type_predefined },
                        success: function(r) {
                            Ext.messageBlack.msg('New legend set', 'The legend set <span class="x-msg-hl">' + mlsv + '</span> was registered.');
                            Ext.getCmp('predefinedmaplegendset_cb').getStore().reload();
							Ext.getCmp('maplegendset_cb').getStore().reload();
							Ext.getCmp('predefinedmaplegendsetname_tf').reset();
							Ext.getCmp('predefinednewmaplegend_ms').reset();							
                        },
                        failure: function() {
                            alert( 'Error: addOrUpdateMapLegendSet' );
                        }
                    });
                }
            }
        ]
    });
	
	var deletePredefinedMapLegendSetPanel = new Ext.form.FormPanel({
        id: 'deletepredefinedmaplegendset_p',
		bodyStyle: 'border:0px solid #fff',
        items:
        [   
            { html: '<div class="window-field-label-first">Legend set</p>' },
            predefinedMapLegendSetComboBox,
            {
                xtype: 'button',
                id: 'deletepredefinedmaplegendset_b',
                text: 'Delete',
				cls: 'window-button',
                handler: function() {
                    var mlsv = Ext.getCmp('predefinedmaplegendset_cb').getValue();
                    var mlsrv = Ext.getCmp('predefinedmaplegendset_cb').getRawValue();
                    
                    if (!mlsv) {
                        Ext.messageRed.msg('Delete legend set', 'Please select a legend set.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'deleteMapLegendSet' + type,
                        method: 'POST',
                        params: { id: mlsv },
                        success: function(r) {
                            Ext.messageBlack.msg('Delete legend set', 'The legend set <span class="x-msg-hl">' + mlsrv + '</span> was deleted.');
                            Ext.getCmp('predefinedmaplegendset_cb').getStore().reload();
                            Ext.getCmp('predefinedmaplegendset_cb').reset();
                        },
                        failure: function() {
                            alert( 'Error: deleteMapLegendSet' );
                        }
                    });
                }
            }
        ]
    });
	
	var predefinedMapLegendSetWindow = new Ext.Window({
        id: 'predefinedmaplegendset_w',
        title: '<span id="window-legendset-title">Predefined legend sets</span>',
		layout: 'fit',
        closeAction: 'hide',
		width: 245,
        items:
        [
			{
				xtype: 'tabpanel',
				activeTab: 0,
				layoutOnTabChange: true,
				deferredRender: false,
				plain: true,
				defaults: {layout: 'fit', bodyStyle: 'padding:8px; border:0px'},
				listeners: {
					tabchange: function(panel, tab)
					{
						var w = Ext.getCmp('predefinedmaplegendset_w');
						
						if (tab.id == 'predefinedmaplegendset0') { 
							w.setHeight(306);
						}
						else if (tab.id == 'predefinedmaplegendset1') {
							w.setHeight(149);
						}
						else if (tab.id == 'predefinedmaplegendset2') {
							w.setHeight(getMultiSelectHeight() + 180);
						}
						else if (tab.id == 'predefinedmaplegendset3') {
							w.setHeight(149);
						}
					}
				},
				items:
				[
					{
						title: '<span class="panel-tab-title">New legend</span>',
						id: 'predefinedmaplegendset0',
						items: [
							newPredefinedMapLegendPanel
						]
					},
					{
						title: '<span class="panel-tab-title">Delete</span>',
						id: 'predefinedmaplegendset1',
						items: [
							deletePredefinedMapLegendPanel
						]
					},
					{
						title: '<span class="panel-tab-title">New legend set</span>',
						id: 'predefinedmaplegendset2',
						items: [
							newPredefinedMapLegendSetPanel
						]
					},
					{
						title: '<span class="panel-tab-title">Delete</span>',
						id: 'predefinedmaplegendset3',
						items: [
							deletePredefinedMapLegendSetPanel
						]
					}
				]
			}
        ]
    });
    

	
    /* HELP PANEL */
	function getHelpText(topic, tab) {
		Ext.Ajax.request({
			url: '../../dhis-web-commons-about/getHelpContent.action',
			method: 'POST',
			params: { id: topic },
			success: function(r) {
				Ext.getCmp(tab).body.update('<div id="help">' + r.responseText + '</div>');
			},
			failure: function() {
				alert('Error: getHelpText');
				return;
			}
		});
	}
    
	var helpWindow = new Ext.Window({
        id: 'help_w',
        title: '<span id="window-help-title">Help</span>',
		layout: 'fit',
        closeAction: 'hide',
		width: 607,
		height: 430, 
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
				layoutOnTabChange: true,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        if (tab.id == 'help0') {
							getHelpText(thematicMap, tab.id);
                        }
                        else if (tab.id == 'help1') {
							getHelpText(mapRegistration, tab.id);
                        }
                        else if (tab.id == 'help2') {
                            getHelpText(organisationUnitAssignment, tab.id);
                        }
						if (tab.id == 'help3') { 
                            getHelpText(overlayRegistration, tab.id);
                        }
                        else if (tab.id == 'help4') {
                            getHelpText(administration, tab.id);
                        }
                        else if (tab.id == 'help5') {
                            getHelpText(favorites, tab.id);
                        }
						else if (tab.id == 'help6') {
                            getHelpText(legendSets, tab.id);
                        }
						else if (tab.id == 'help7') {
                            getHelpText(pdfprint, tab.id);
                        }
                    }
                },
                items:
                [
                    {
                        title: '<span class="panel-tab-title">Thematic map</span>',
                        id: 'help0'
                    },
                    {
                        title: '<span class="panel-tab-title">Maps</span>',
                        id: 'help1'
                    },
                    {
                        title: '<span class="panel-tab-title">Assignment</span>',
                        id: 'help2'
                    },
                    {
                        title: '<span class="panel-tab-title">Overlays</span>',
                        id: 'help3'
                    },
                    {
                        title: '<span class="panel-tab-title">Admin</span>',
                        id: 'help4'
                    },
                    {
                        title: '<span class="panel-tab-title">Favorites</span>',
                        id: 'help5'
                    },
                    {
                        title: '<span class="panel-tab-title">Legend sets</span>',
                        id: 'help6'
                    },
                    {
                        title: '<span class="panel-tab-title">PDF print</span>',
                        id: 'help7'
                    }
                ]
            }
        ],
		listeners: {
			'hide': {
				fn: function() {
					mapping.relation = false;
				}
			}
		}
    });

    /* REGISTER MAPS PANEL */
    var organisationUnitLevelStore = new Ext.data.JsonStore({
        url: path + 'getOrganisationUnitLevels' + type,
		id: 'id',
        baseParams: { format: 'json' },
        root: 'organisationUnitLevels',
        fields: ['id', 'level', 'name'],
        autoLoad: true
    });

    var organisationUnitStore = new Ext.data.JsonStore({
        url: path + 'getOrganisationUnitsAtLevel' + type,
        baseParams: { level: 1, format: 'json' },
        root: 'organisationUnits',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: false
    });
    
    var existingMapsStore = new Ext.data.JsonStore({
        url: path + 'getAllMaps' + type,
        baseParams: { format: 'jsonmin' },
        root: 'maps',
        fields: ['id', 'name', 'mapLayerPath', 'organisationUnitLevel'],
        autoLoad: true
    });
	
	var wmsMapStore = new GeoExt.data.WMSCapabilitiesStore({
		url: path_geoserver + ows
	});
	
	if (MAPSOURCE == map_source_type_shapefile) {
		wmsMapStore.load();
	}
	
	var geojsonStore = new Ext.data.JsonStore({
        url: path + 'getGeoJsonFiles' + type,
        root: 'files',
        fields: ['name'],
        autoLoad: true
    });
	
	var nameColumnStore = new Ext.data.SimpleStore({
		fields: ['name'],
		data: []
	});
	
	var baseCoordinateStore = new Ext.data.JsonStore({
        url: path + 'getBaseCoordinate' + type,
        root: 'baseCoordinate',
        fields: ['longitude','latitude'],
        autoLoad: true
    });
	
    var organisationUnitComboBox = new Ext.form.ComboBox({
        id: 'organisationunit_cb',
        fieldLabel: 'Organisation unit',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        emptyText: emptytext,
		hideLabel: true,
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: organisationUnitStore
    });
    
    var organisationUnitLevelComboBox = new Ext.form.ComboBox({
        id: 'organisationunitlevel_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        emptyText: emptytext,
		hideLabel: true,
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: organisationUnitLevelStore
    });

    var newNameTextField = new Ext.form.TextField({
        id: 'newname_tf',
        emptyText: emptytext,
		hideLabel: true,
        width: combo_width
    });
    
    var editNameTextField = new Ext.form.TextField({
        id: 'editname_tf',
        emptyText: emptytext,
		hideLabel: true,
        width: combo_width
    });
    
	var mapLayerPathComboBox = new Ext.form.ComboBox({
        id: 'maplayerpath_cb',
		typeAhead: true,
        editable: false,
        valueField: 'name',
        displayField: 'name',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
        minListWidth: combo_list_width,
        triggerAction: 'all',
        mode: 'remote',
        store: geojsonStore,
		listeners: {
			'select': {
				fn: function() {
					var n = Ext.getCmp('maplayerpath_cb').getValue();
					
					Ext.Ajax.request({
						url: path + 'getGeoJson' + type,
						method: 'POST',
						params: {name: n},
						success: function(r) {
							var file = Ext.util.JSON.decode(r.responseText);
							var keys = [];
							var data = [];
							
							function getKeys(object) {
								for (var key in object) {
									if (object.hasOwnProperty(key)) {
										keys.push(key);
									}
								}
								return keys;
							}

							var nameList = getKeys(file.features[0].properties);
							for (var i = 0; i < nameList.length; i++) {
								data.push(new Array(nameList[i]));
							}
							
							Ext.getCmp('newnamecolumn_cb').getStore().loadData(data, false);
						},
						failure: function() {}
					});
				},
				scope: this
			}
		}
    });
    
	var wmsGrid = new Ext.grid.GridPanel({
		id: 'wms_g',
		sm: new Ext.grid.RowSelectionModel({
			singleSelect: true
		}),
        columns: [
            {header: 'Title', dataIndex: 'title', sortable: true, width: 180},
            {header: 'Name', dataIndex: 'name', sortable: true, width: 180},
            {header: 'Queryable', dataIndex: 'queryable', sortable: true, width: 100},
            {header: 'Description', id: 'description_c', dataIndex: 'abstract'}
        ],
        autoExpandColumn: 'description_c',
        width: 700,
        height: screen.height * 0.6,
		store: wmsMapStore,
        listeners: {
            'rowdblclick': mapPreview
        }
    });
    
    function mapPreview(grid, index) {
        var record = grid.getStore().getAt(index);
        var layer = record.get('layer').clone();
        
        var wmsPreviewWindow = new Ext.Window({
            title: '<span class="panel-title">Preview: ' + record.get("title") + '</span>',
            width: screen.width * 0.5,
            height: screen.height * 0.3,
            layout: 'fit',
            items: [{
                xtype: 'gx_mappanel',
                layers: [layer],
                extent: record.get('llbbox')
            }]
        });
        wmsPreviewWindow.show();
    }
	
	var wmsWindow = new Ext.Window({
		id: 'wms_w',
		title: '<span class="panel-title">Geoserver shapefiles</span>',
		closeAction: 'hide',
		width: wmsGrid.width,
		height: screen.height * 0.4,
		items: [wmsGrid],
		bbar: new Ext.Toolbar({
			id: 'wmswindow_sb',
			items:
			[
				/* {
					 xtype: 'button',
					 id: 'previewwms_b',
					 text: 'Preview',
					 handler: function() {
						
					 }
				 },*/
				{
					xtype: 'button',
					id: 'selectwms_b',
					text: 'Select',
					cls: 'aa_med',
					handler: function() {
						var name = Ext.getCmp('wms_g').getSelectionModel().getSelected().get('name');
						mapLayerPathWMSTextField.setValue(name);
						wmsWindow.hide();
						newNameColumnComboBox.focus();						
					}
				}
			]
		})		
	});
	
	var mapLayerPathWMSTextField = new Ext.form.TextField({
		id: 'maplayerpathwms_tf',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
		listeners: {
			'focus': {
				fn: function() {
					var x = Ext.getCmp('center').x + 15;
					var y = Ext.getCmp('center').y + 41;    
					wmsWindow.show();
					wmsWindow.setPosition(x,y);
				}
			}
		}
	});
	
    var typeComboBox = new Ext.form.ComboBox({
        id: 'type_cb',
        editable: false,
        displayField: 'name',
        valueField: 'name',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
        minListWidth: combo_list_width,
        triggerAction: 'all',
        mode: 'local',
        value: 'Polygon',
        store: new Ext.data.SimpleStore({
            fields: ['name'],
            data: [['Polygon']]
        })
    });

	var newNameColumnComboBox = new Ext.form.ComboBox({
        id: 'newnamecolumn_cb',
        editable: false,
        displayField: 'name',
        valueField: 'name',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
        minListWidth: combo_list_width,
        triggerAction: 'all',
        mode: 'local',
        store: nameColumnStore,
		listeners: {
			'focus': {
				fn: function() {
					var mlp = Ext.getCmp('maplayerpathwms_tf').getValue();
					
					if (mlp) {					
						Ext.Ajax.request({
							url: path_geoserver + wfs + mlp + output,
							method: 'POST',
							success: function(r) {
								var file = Ext.util.JSON.decode(r.responseText);
								var keys = [];
								var data = [];
								
								function getKeys(object) {
									for (var key in object) {
										if (object.hasOwnProperty(key)) {
											keys.push(key);
										}
									}
									return keys;
								}

								var nameList = getKeys(file.features[0].properties);
								for (var i = 0; i < nameList.length; i++) {
									data.push(new Array(nameList[i]));
								}
								
								Ext.getCmp('newnamecolumn_cb').getStore().loadData(data, false);
							},
							failure: function() {}
						});
					}
				}
			}
		}				
	});
    
	var editNameColumnComboBox = new Ext.form.ComboBox({
        id: 'editnamecolumn_cb',
        editable: false,
        displayField: 'name',
        valueField: 'name',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
        minListWidth: combo_list_width,
        triggerAction: 'all',
        mode: 'local',
        store: nameColumnStore
	});
	
    var newLongitudeComboBox = new Ext.form.ComboBox({
        id: 'newlongitude_cb',
		valueField: 'longitude',
		displayField: 'longitude',
		editable: true,
        emptyText: emptytext,
		hideLabel: true,
        width: combo_number_width,
		minListWidth: combo_number_list_width,
		triggerAction: 'all',
		value: BASECOORDINATE.longitude,
		mode: 'remote',
		store: baseCoordinateStore
    });
    
    var editLongitudeComboBox = new Ext.form.ComboBox({
        id: 'editlongitude_cb',
		valueField: 'longitude',
		displayField: 'longitude',
		editable: true,
        emptyText: emptytext,
		hideLabel: true,
        width: combo_number_width,
		minListWidth: combo_number_list_width,
		triggerAction: 'all',
		mode: 'remote',
		store: baseCoordinateStore

    });
	
    var newLatitudeComboBox = new Ext.form.ComboBox({
        id: 'newlatitude_cb',
		valueField: 'latitude',
		displayField: 'latitude',
		editable: true,
        emptyText: emptytext,
		hideLabel: true,
        width: combo_number_width,
		minListWidth: combo_number_list_width,
		triggerAction: 'all',
		value: BASECOORDINATE.latitude,
		mode: 'remote',
		store: baseCoordinateStore
    });
    
    var editLatitudeComboBox = new Ext.form.ComboBox({
        id: 'editlatitude_cb',
		valueField: 'latitude',
		displayField: 'latitude',
		editable: true,
        emptyText: emptytext,
		hideLabel: true,
        width: combo_number_width,
		minListWidth: combo_number_list_width,
		triggerAction: 'all',
		mode: 'remote',
		store: baseCoordinateStore
    });
    
    var newZoomComboBox = new Ext.form.ComboBox({
        id: 'newzoom_cb',
        editable: true,
        displayField: 'text',
        valueField: 'value',
		hideLabel: true,
        width: combo_number_width,
        minListWidth: combo_number_list_width,
        triggerAction: 'all',
        mode: 'local',
        value: 7,
        store: new Ext.data.SimpleStore({
            fields: ['value','text'],
            data: [[5, '5 (out)'], [6,'6'], [7,'7'], [8,'8'], [9,'9 (in)']]
        })
    });
    
    var editZoomComboBox = new Ext.form.ComboBox({
        id: 'editzoom_cb',
        editable: false,
        emptyText: '',
        displayField: 'value',
        valueField: 'value',
		hideLabel: true,
        width: combo_number_width,
        minListWidth: combo_number_width + 17,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
            fields: ['value','text'],
            data: [[5, '5 (out)'], [6,'6'], [7,'7'], [8,'8'], [9,'9 (in)']]
        })
    });
    
    var newMapButton = new Ext.Button({
        id: 'newmap_b',
        text: 'Register new map',
		cls: 'aa_med',
        handler: function()
        {
            /*var nm = Ext.getCmp('newmap_cb').getValue();
            var oui = Ext.getCmp('organisationunit_cb').getValue();*/
    
            Ext.Ajax.request({
                url: path + 'getOrganisationUnitsAtLevel' + type,
                method: 'POST',
                params: { level: 1, format: 'json' },

                success: function(r) {
                    var oui = Ext.util.JSON.decode( r.responseText ).organisationUnits[0].id;
                    var ouli = Ext.getCmp('organisationunitlevel_cb').getValue();
                    var nn = Ext.getCmp('newname_tf').getValue();
                    var t = Ext.getCmp('type_cb').getValue();
					var mlp = Ext.getCmp('maplayerpath_cb').getValue();
					var mlpwms = Ext.getCmp('maplayerpathwms_tf').getValue();					
                    var nc = Ext.getCmp('newnamecolumn_cb').getValue();
                    var lon = Ext.getCmp('newlongitude_cb').getRawValue();
                    var lat = Ext.getCmp('newlatitude_cb').getRawValue();
                    var zoom = Ext.getCmp('newzoom_cb').getValue();
                     
                    if (!nn || !oui || !ouli || !nc || !lon || !lat) {
						Ext.messageRed.msg('New map', 'Form is not complete.');
						return;
					}
					else if (!mlp && !mlpwms) {
						Ext.messageRed.msg('New map', 'Form is not complete.');
						return;
                    }
                    
                    if (validateInput(nn) == false) {
                        Ext.messageRed.msg('New map', '<span class="x-msg-hl">' + 'Map name' + '</span> cannot have more than 25 characters.');
                        return;
                    }
                    
                    if (!Ext.num(parseFloat(lon), false)) {
                        Ext.messageRed.msg('New map', '<span class="x-msg-hl">' + 'Longitude' + '</span> must be a number.');
                        return;
                    }
                    else {
                        if (lon < -180 || lon > 180) {
                            Ext.messageRed.msg('New map', '<span class="x-msg-hl">' + 'Longitude' + '</span> must be between -180 and 180.');
                            return;
                        }
                    }
                    
                    if (!Ext.num(parseFloat(lat), false)) {
                        Ext.messageRed.msg('New map', '<span class="x-msg-hl">' + 'Latitude' + '</span> must be a number.');
                        return;
                    }
                    else {
                        if (lat < -90 || lat > 90) {
                            Ext.messageRed.msg('New map', '<span class="x-msg-hl">' + 'Latitude' + '</span> must be between -90 and 90.');
                            return;
                        }
                    }

                    Ext.Ajax.request({
                        url: path + 'getAllMaps' + type,
                        method: 'GET',
                        success: function(r) {
                            var maps = Ext.util.JSON.decode(r.responseText).maps;
                            for (var i = 0; i < maps.length; i++) {
                                if (maps[i].name == nn) {
                                    Ext.messageRed.msg('New map', 'There is already a map called <span class="x-msg-hl">' + nn + '</span>.');
                                    return;
                                }
                                else if (maps[i].mapLayerPath == mlp) {
                                    Ext.messageRed.msg('New map', 'The source file <span class="x-msg-hl">' + mlp + '</span> is already registered.');
                                    return;
                                }
                            }
							
							var source = mlp ? mlp : mlpwms;
							
                            Ext.Ajax.request({
                                url: path + 'addOrUpdateMap' + type,
                                method: 'POST',
                                params: { name: nn, mapLayerPath: source, type: t, sourceType: MAPSOURCE, organisationUnitId: oui, organisationUnitLevelId: ouli, nameColumn: nc, longitude: lon, latitude: lat, zoom: zoom},
                                success: function(r) {
                                    Ext.messageBlack.msg('New map', 'The map <span class="x-msg-hl">' + nn + '</span> (<span class="x-msg-hl">' + source + '</span>) was registered.');
                                    
                                    Ext.getCmp('map_cb').getStore().reload();
                                    Ext.getCmp('maps_cb').getStore().reload();
                                    Ext.getCmp('editmap_cb').getStore().reload();
                                    Ext.getCmp('deletemap_cb').getStore().reload();
                                    
                                    Ext.getCmp('organisationunitlevel_cb').reset();
                                    Ext.getCmp('newname_tf').reset();
                                    Ext.getCmp('maplayerpath_cb').reset();
                                    Ext.getCmp('newnamecolumn_cb').reset();
                                    Ext.getCmp('newlongitude_cb').reset();
                                    Ext.getCmp('newlatitude_cb').reset();
                                    Ext.getCmp('newzoom_cb').reset();                                    
                                },
                                failure: function() {
                                    alert( 'Error: addOrUpdateMap' );
                                }
                            });
                        },
                        failure: function() {
                            alert( 'Error: getAllMaps' );
                        }
                    });
                },
                failure: function() {
                    alert( 'Error: getOrganisationUnitsAtLevel' );
                }
            });
        }
    });
    
    var editMapButton = new Ext.Button({
        id: 'editmap_b',
        text: 'Save changes',
		cls: 'aa_med',
        handler: function() {
            var en = Ext.getCmp('editname_tf').getValue();
            var em = Ext.getCmp('editmap_cb').getValue();
            var nc = Ext.getCmp('editnamecolumn_cb').getValue();
            var lon = Ext.getCmp('editlongitude_cb').getRawValue();
            var lat = Ext.getCmp('editlatitude_cb').getRawValue();
            var zoom = Ext.getCmp('editzoom_cb').getValue();
			var t = Ext.getCmp('type_cb').getValue();
			
            if (!en || !em || !nc || !lon || !lat) {
                Ext.messageRed.msg('New map', 'Form is not complete.');
                return;
            }
            
            if (validateInput(en) == false) {
                Ext.messageRed.msg('New map', 'Map name cannot be longer than 25 characters.');
                return;
            }
           
            Ext.Ajax.request({
                url: path + 'addOrUpdateMap' + type,
                method: 'GET',
                params: { name: en, mapLayerPath: em, nameColumn: nc, longitude: lon, latitude: lat, zoom: zoom },

                success: function(r) {
                    Ext.messageBlack.msg('Edit map', 'The map <span class="x-msg-hl">' + en + '</span> (<span class="x-msg-hl">' + em + '</span>) was updated.');
                    
                    Ext.getCmp('map_cb').getStore().reload();
                    Ext.getCmp('maps_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').reset();
                    Ext.getCmp('deletemap_cb').getStore().reload();
                    Ext.getCmp('deletemap_cb').reset();
                    
                    Ext.getCmp('editmap_cb').reset();
                    Ext.getCmp('editname_tf').reset();
                    Ext.getCmp('editnamecolumn_cb').reset();
                    Ext.getCmp('editlongitude_cb').reset();
                    Ext.getCmp('editlatitude_cb').reset();
                    Ext.getCmp('editzoom_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var deleteMapButton = new Ext.Button({
        id: 'deletemap_b',
        text: 'Delete map',
		cls: 'aa_med',
        handler: function() {
            var mlp = Ext.getCmp('deletemap_cb').getValue();
            var mn = Ext.getCmp('deletemap_cb').getRawValue();
            
            if (!mlp) {
                Ext.messageRed.msg('Delete map', 'Please select a map.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMap' + type,
                method: 'GET',
                params: { mapLayerPath: mlp },

                success: function(r) {
                    Ext.messageBlack.msg('Edit map', 'The map <span class="x-msg-hl">' + mn + '</span> (<span class="x-msg-hl">' + mlp + '</span>) was deleted.');
                    
                    
                    
                    Ext.getCmp('map_cb').getStore().reload();
					
					if (Ext.getCmp('map_cb').getValue() == mlp) {
						Ext.getCmp('map_cb').reset();
					}
					
                    Ext.getCmp('maps_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').reset();
                    Ext.getCmp('deletemap_cb').getStore().reload();
                    Ext.getCmp('deletemap_cb').reset();
                    Ext.getCmp('mapview_cb').getStore().reload();
                    Ext.getCmp('mapview_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var newMapComboBox = new Ext.form.ComboBox({
        id: 'newmap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'level',
        displayField: 'name',
        emptyText: emptytext,
		hideLabel: true,
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: organisationUnitLevelStore,
        listeners: {
            'select': {
                fn: function() {
                    var level = Ext.getCmp('newmap_cb').getValue();
                    organisationUnitStore.baseParams = { level: level, format: 'json' };
                    organisationUnit();
                },
                scope: this
            }
        }
    });
    
    var editMapComboBox = new Ext.form.ComboBox({
        id: 'editmap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'mapLayerPath',
        displayField: 'name',
        emptyText: emptytext,
		hideLabel: true,
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: existingMapsStore,
        listeners: {
            'select': {
                fn: function() {
                    var mlp = Ext.getCmp('editmap_cb').getValue();
                    
                    Ext.Ajax.request({
                        url: path + 'getMapByMapLayerPath' + type,
                        method: 'GET',
                        params: { mapLayerPath: mlp, format: 'json' },

                        success: function(r) {
                            var map = Ext.util.JSON.decode( r.responseText ).map[0];
                            
                            Ext.getCmp('editname_tf').setValue(map.name);
                            Ext.getCmp('editnamecolumn_cb').setValue(map.nameColumn);
                            Ext.getCmp('editlongitude_cb').setValue(map.longitude);
                            Ext.getCmp('editlatitude_cb').setValue(map.latitude);
                            Ext.getCmp('editzoom_cb').setValue(map.zoom);
                        },
                        failure: function() {
                            alert( 'Error while retrieving data: getAssignOrganisationUnitData' );
                        } 
                    });
					
					if (MAPSOURCE == map_source_type_geojson) {
						Ext.Ajax.request({
							url: path + 'getGeoJson' + type,
							method: 'POST',
							params: {name: mlp},
							success: function(r) {
								var file = Ext.util.JSON.decode(r.responseText);
								var keys = [];
								var data = [];
								
								function getKeys(object) {
									for (var key in object) {
										if (object.hasOwnProperty(key)) {
											keys.push(key);
										}
									}
									return keys;
								}

								var nameList = getKeys(file.features[0].properties);
								for (var i = 0; i < nameList.length; i++) {
									data.push(new Array(nameList[i]));
								}
								
								Ext.getCmp('editnamecolumn_cb').getStore().loadData(data, false);
							},
							failure: function() {}
						});
					}
					else if (MAPSOURCE == map_source_type_shapefile) {
						Ext.Ajax.request({
							url: path_geoserver + wfs + mlp + output,
							method: 'POST',
							success: function(r) {
								var file = Ext.util.JSON.decode(r.responseText);
								var keys = [];
								var data = [];
								
								function getKeys(object) {
									for (var key in object) {
										if (object.hasOwnProperty(key)) {
											keys.push(key);
										}
									}
									return keys;
								}

								var nameList = getKeys(file.features[0].properties);
								for (var i = 0; i < nameList.length; i++) {
									data.push(new Array(nameList[i]));
								}
								
								Ext.getCmp('editnamecolumn_cb').getStore().loadData(data, false);
							},
							failure: function() {}
						});
					}
                },
                scope: this
            }
        }
    });
    
    var deleteMapComboBox = new Ext.form.ComboBox({
        xtype: 'combo',
        id: 'deletemap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'mapLayerPath',
        displayField: 'name',
        emptyText: emptytext,
		hideLabel: true,
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: existingMapsStore
    });
    
    var newMapPanel = new Ext.form.FormPanel({   
        id: 'newmap_p',
        items:
        [   
            /*{ html: '<div class="panel-fieldlabel">Map type</div>' }, typeComboBox,
            { html: '<div class="panel-fieldlabel">Organisation unit level</div>' }, newMapComboBox,
            { html: '<div class="panel-fieldlabel">Organisation unit</div>' }, multi,*/
            { html: '<div class="panel-fieldlabel-first">Display name</div>' }, newNameTextField,
            { html: '<div class="panel-fieldlabel">Organisation unit level</div>' }, organisationUnitLevelComboBox,
			{ html: '<div class="panel-fieldlabel">Map source file</div>' }, mapLayerPathComboBox, mapLayerPathWMSTextField,
            { html: '<div class="panel-fieldlabel">Name column</div>' }, newNameColumnComboBox,
            { html: '<div class="panel-fieldlabel">Longitude (x)</div>' }, newLongitudeComboBox,
            { html: '<div class="panel-fieldlabel">Latitude (y)</div>' }, newLatitudeComboBox,
            { html: '<div class="panel-fieldlabel">Zoom</div>' }, newZoomComboBox
        ]
    });
    
    var editMapPanel = new Ext.form.FormPanel({
        id: 'editmap_p',
        items: [
            { html: '<div class="panel-fieldlabel-first">Map</div>' }, editMapComboBox,
            { html: '<div class="panel-fieldlabel">Display name</div>' }, editNameTextField,
            { html: '<div class="panel-fieldlabel">Name column</div>' }, editNameColumnComboBox,
            { html: '<div class="panel-fieldlabel">Longitude</div>' }, editLongitudeComboBox,
            { html: '<div class="panel-fieldlabel">Latitude</div>' }, editLatitudeComboBox,
            { html: '<div class="panel-fieldlabel">Zoom</div>' }, editZoomComboBox
        ]
    });
    
    var deleteMapPanel = new Ext.form.FormPanel({
        id: 'deletemap_p',
        items: [
            { html: '<div class="panel-fieldlabel-first">Map</div>' }, deleteMapComboBox
        ]
    });

    shapefilePanel = new Ext.Panel({
        id: 'shapefile_p',
        title: '<span class="panel-title">Register maps</span>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab) {
                        var nm_b = Ext.getCmp('newmap_b');
                        var em_b = Ext.getCmp('editmap_b');
                        var dm_b = Ext.getCmp('deletemap_b');
                        
                        if (tab.id == 'map0')
                        { 
                            nm_b.setVisible(true);
                            em_b.setVisible(false);
                            dm_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'map1')
                        {
                            nm_b.setVisible(false);
                            em_b.setVisible(true);
                            dm_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'map2')
                        {
                            nm_b.setVisible(false);
                            em_b.setVisible(false);
                            dm_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title: '<span class="panel-tab-title">New</span>',
                        id: 'map0',
                        items:
                        [
                            newMapPanel
                        ]
                    },
                    {
                        title: '<span class="panel-tab-title">Edit</span>',
                        id: 'map1',
                        items:
                        [
                            editMapPanel
                        ]
                    },
                    {
                        title: '<span class="panel-tab-title">Delete</span>',
                        id: 'map2',
                        items:
                        [
                            deleteMapPanel
                        ]
                    }
                ]
            },
            { html: '<br>' },
            
            newMapButton,
            
            editMapButton,
            
            deleteMapButton
        ],
		listeners: {
			expand: {
				fn: function() {
					if (MAPSOURCE == map_source_type_shapefile) {
						mapLayerPathComboBox.hide();
						mapLayerPathWMSTextField.show();						
					}
					else {
						mapLayerPathComboBox.show();
						mapLayerPathWMSTextField.hide();						
					}
					
					ACTIVEPANEL = mapRegistration;
				}
			},
			collapse: {
				fn: function() {
					ACTIVEPANEL = false;
				}
			}
		}
    });
    
    /* OVERLAY PANEL */
	var wmsOverlayStore = new GeoExt.data.WMSCapabilitiesStore({
		url: path_geoserver + ows
	});
	
	if (MAPSOURCE == map_source_type_shapefile) {
		wmsOverlayStore.load();
	}
	
    var mapLayerNameTextField = new Ext.form.TextField({
        id: 'maplayername_tf',
        emptyText: emptytext,
		hideLabel: true,
        width: combo_width
    });
	
	var mapLayerMapSourceFileComboBox = new Ext.form.ComboBox({
        id: 'maplayermapsourcefile_cb',
        editable: false,
        displayField: 'name',
        valueField: 'name',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
        minListWidth: combo_list_width,
        triggerAction: 'all',
        mode: 'remote',
        store: geojsonStore
    });
	
	var wmsOverlayGrid = new Ext.grid.GridPanel({
		id: 'wmsoverlay_g',
		sm: new Ext.grid.RowSelectionModel({
			singleSelect:true
		}),
        columns: [
            {header: 'Title', dataIndex: 'title', sortable: true, width: 180},
            {header: 'Name', dataIndex: 'name', sortable: true, width: 180},
            {header: 'Queryable', dataIndex: 'queryable', sortable: true, width: 100},
            {header: 'Description', id: 'description', dataIndex: 'abstract'}
        ],
        autoExpandColumn: 'description',
        width: 700,
        height: screen.height * 0.6,
        store: wmsOverlayStore,
        listeners: {
            'rowdblclick': mapOverlayPreview
        }
    });
    
    function mapOverlayPreview(grid, index) {
        var record = grid.getStore().getAt(index);
        var layer = record.get('layer').clone();
        
        var wmsOverlayPreviewWindow = new Ext.Window({
            title: '<span class="panel-title">Preview: ' + record.get("title") + '</span>',
            width: screen.width * 0.4,
            height: screen.height * 0.4,
            layout: 'fit',
            items: [{
                xtype: 'gx_mappanel',
                layers: [layer],
                extent: record.get('llbbox')
            }]
        });
        wmsOverlayPreviewWindow.show();
    }
	
	var wmsOverlayWindow = new Ext.Window({
		id: 'wmsoverlay_w',
		title: '<span class="panel-title">Geoserver shapefiles</span>',
		closeAction: 'hide',
		width: wmsOverlayGrid.width,
		height: screen.height * 0.4,
		items: [wmsOverlayGrid],
		bbar: new Ext.StatusBar({
			id: 'wmsoverlaywindow_sb',
			items:
			[
/*			
				{
					xtype: 'button',
					id: 'previewwmsoverlay_b',
					text: 'Preview',
					handler: function() {}
				},
*/				
				{
					xtype: 'button',
					id: 'selectwmsoverlay_b',
					text: 'Select',
					cls: 'aa_med',
					handler: function() {
						var name = Ext.getCmp('wmsoverlay_g').getSelectionModel().getSelected().get('name');
						mapLayerPathWMSOverlayTextField.setValue(name);
						wmsOverlayWindow.hide();
						newMapLayerButton.focus();						
					}
				}
			]
		})
	});
	
	var mapLayerPathWMSOverlayTextField = new Ext.form.TextField({
		id: 'maplayerpathwmsoverlay_tf',
		emptyText: emptytext,
		hideLabel: true,
        width: combo_width,
		listeners: {
			'focus': {
				fn: function() {
					var x = Ext.getCmp('center').x + 15;
					var y = Ext.getCmp('center').y + 41;    
					wmsOverlayWindow.show();
					wmsOverlayWindow.setPosition(x,y);
				}
			}
		}
	});
    
    var mapLayerFillColorColorField = new Ext.ux.ColorField({
        id: 'maplayerfillcolor_cf',
		hideLabel: true,
        allowBlank: false,
        width: combo_width,
        value: '#FF0000'
    });
    
    var mapLayerFillOpacityComboBox = new Ext.form.ComboBox({
        id: 'maplayerfillopacity_cb',
		hideLabel: true,
        editable: true,
        valueField: 'value',
        displayField: 'value',
        mode: 'local',
        triggerAction: 'all',
        width: combo_number_width,
        minListWidth: combo_number_list_width,
        value: 0.5,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[0.0], [0.1], [0.2], [0.3], [0.4], [0.5], [0.6], [0.7], [0.8], [0.9], [1.0]]
        })
    });
    
    var mapLayerStrokeColorColorField = new Ext.ux.ColorField({
        id: 'maplayerstrokecolor_cf',
		hideLabel: true,
        allowBlank: false,
        width: combo_width,
        value: '#222222'
    });
    
    var mapLayerStrokeWidthComboBox = new Ext.form.ComboBox({
        id: 'maplayerstrokewidth_cb',
		hideLabel: true,
        editable: true,
        valueField: 'value',
        displayField: 'value',
        mode: 'local',
        triggerAction: 'all',
        width: combo_number_width,
        minListWidth: combo_number_list_width,
        value: 2,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[0], [1], [2], [3], [4]]
        })
    });
    
    var mapLayerStore = new Ext.data.JsonStore({
        url: path + 'getAllMapLayers' + type,
        root: 'mapLayers',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var mapLayerComboBox = new Ext.form.ComboBox({
        id: 'maplayer_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: emptytext,
		hideLabel: true,
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_list_width,
        store: mapLayerStore
    });
    
    var newMapLayerButton = new Ext.Button({
        id: 'newmaplayer_b',
        text: 'Register new overlay',
		cls: 'aa_med',
        handler: function() {
            var mln = Ext.getCmp('maplayername_tf').getRawValue();
            var mlfc = Ext.getCmp('maplayerfillcolor_cf').getValue();
            var mlfo = Ext.getCmp('maplayerfillopacity_cb').getRawValue();
            var mlsc = Ext.getCmp('maplayerstrokecolor_cf').getValue();
            var mlsw = Ext.getCmp('maplayerstrokewidth_cb').getRawValue();
			
			var mlmsf = Ext.getCmp('maplayermapsourcefile_cb').getValue();
			var mlwmso = Ext.getCmp('maplayerpathwmsoverlay_tf').getValue();			
            
            if (!mln) {
                Ext.messageRed.msg('New overlay', 'Overlay form is not complete.');
                return;
            }
			else if (!mlmsf && !mlwmso) {
				Ext.messageRed.msg('New overlay', 'Overlay form is not complete.');
                return;
			}
            
            if (validateInput(mln) == false) {
                Ext.messageRed.msg('New overlay', 'Overlay name cannot be longer than 25 characters.');
                return;
            }
			
			Ext.Ajax.request({
                url: path + 'getAllMapLayers' + type,
                method: 'GET',
                success: function(r) {
					var mapLayers = Ext.util.JSON.decode(r.responseText).mapLayers;
					
					for (i in mapLayers) {
						if (mapLayers[i].name == mln) {
							Ext.messageRed.msg('New overlay', 'The name <span class="x-msg-hl">' + mln + '</span> is already in use.');
							return;
						}
					}
			
					var ms = MAPSOURCE == map_source_type_geojson ? mlmsf : mlwmso;
					
					Ext.Ajax.request({
						url: path + 'addOrUpdateMapLayer' + type,
						method: 'POST',
						params: { name: mln, type: 'overlay', mapSource: ms, fillColor: mlfc, fillOpacity: mlfo, strokeColor: mlsc, strokeWidth: mlsw },
						success: function(r) {
							Ext.messageBlack.msg('New overlay', 'The overlay <span class="x-msg-hl">' + mln + '</span> was registered.');
							Ext.getCmp('maplayer_cb').getStore().reload();
					
							var mapurl = MAPSOURCE == map_source_type_geojson ? path + 'getGeoJson.action?name=' + mlmsf : path_geoserver + wfs + mlwmso + output;
							
							MAP.addLayer(
								new OpenLayers.Layer.Vector(mln, {
									'visibility': false,
									'styleMap': new OpenLayers.StyleMap({
										'default': new OpenLayers.Style(
											OpenLayers.Util.applyDefaults(
												{'fillColor': mlfc, 'fillOpacity': mlfo, 'strokeColor': mlsc, 'strokeWidth': mlsw},
												OpenLayers.Feature.Vector.style['default']
											)
										)
									}),
									'strategies': [new OpenLayers.Strategy.Fixed()],
									'protocol': new OpenLayers.Protocol.HTTP({
										'url': mapurl,
										'format': new OpenLayers.Format.GeoJSON()
									})
								})
							);
							
							Ext.getCmp('maplayername_tf').reset();
							Ext.getCmp('maplayermapsourcefile_cb').reset();
							Ext.getCmp('maplayerpathwmsoverlay_tf').reset();
						},
						failure: function() {}
					});
				},
				failure: function() {}
			});
        }
    });
    
    var deleteMapLayerButton = new Ext.Button({
        id: 'deletemaplayer_b',
        text: 'Delete overlay',
		cls: 'aa_med',
        handler: function() {
            var ml = Ext.getCmp('maplayer_cb').getValue();
            var mln = Ext.getCmp('maplayer_cb').getRawValue();
            
            if (!ml) {
                Ext.messageRed.msg('Delete overlay', 'Please select an overlay.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMapLayer' + type,
                method: 'POST',
                params: { id: ml },

                success: function(r) {
                    Ext.messageBlack.msg('Delete overlay', 'The overlay <span class="x-msg-hl">' + mln + '</span> was deleted.');
                    Ext.getCmp('maplayer_cb').getStore().reload();
                    Ext.getCmp('maplayer_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
            
            MAP.getLayersByName(mln)[0].destroy();
        }
    });
	
    var newMapLayerPanel = new Ext.form.FormPanel({
        id: 'newmaplayer_p',
        items:
        [
            { html: '<div class="panel-fieldlabel-first">Display name</div>' }, mapLayerNameTextField,
            { html: '<div class="panel-fieldlabel">Map source file</div>' }, mapLayerMapSourceFileComboBox, mapLayerPathWMSOverlayTextField,
            { html: '<div class="panel-fieldlabel">Fill color</div>' }, mapLayerFillColorColorField,
            { html: '<div class="panel-fieldlabel">Fill opacity</div>' }, mapLayerFillOpacityComboBox,
            { html: '<div class="panel-fieldlabel">Stroke color</div>' }, mapLayerStrokeColorColorField,
            { html: '<div class="panel-fieldlabel">Stroke width</div>' }, mapLayerStrokeWidthComboBox,
        ]
    });
    
    var deleteMapLayerPanel = new Ext.form.FormPanel({
        id: 'deletemaplayer_p',
        items:
        [
            { html: '<div class="panel-fieldlabel-first">Overlay</div>' }, mapLayerComboBox
        ]
    });
    
    var mapLayerPanel = new Ext.Panel({
        id: 'maplayer_p',
        title: '<span class="panel-title">Register overlays</span>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        var nml_b = Ext.getCmp('newmaplayer_b');
                        var dml_b = Ext.getCmp('deletemaplayer_b');
                        
                        if (tab.id == 'maplayer0') { 
                            nml_b.setVisible(true);
                            dml_b.setVisible(false);
                        }
                        else if (tab.id == 'maplayer1') {
                            nml_b.setVisible(false);
                            dml_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title: '<span class="panel-tab-title">New</span>',
                        id: 'maplayer0',
                        items:
                        [
                            newMapLayerPanel
                        ]
                    },
                    {
                        title: '<span class="panel-tab-title">Delete</span>',
                        id: 'maplayer1',
                        items:
                        [
                            deleteMapLayerPanel
                        ]
                    }
                ]
            },
            
            { html: '<br>' },
            
            newMapLayerButton,
            
            deleteMapLayerButton
        ],
		listeners: {
			expand: {
				fn: function() {
					if (MAPSOURCE == map_source_type_geojson) {
						mapLayerMapSourceFileComboBox.show();
						mapLayerPathWMSOverlayTextField.hide();
					}
					else if (MAPSOURCE == map_source_type_shapefile) {
						mapLayerMapSourceFileComboBox.hide();
						mapLayerPathWMSOverlayTextField.show();
					}
					
					ACTIVEPANEL = overlayRegistration;
				}
			},
			collapse: {
				fn: function() {
					ACTIVEPANEL = false;
				}
			}
		}
    });
	
    /* ADMIN PANEL */
    var adminPanel = new Ext.form.FormPanel({
        id: 'admin_p',
        title: '<span class="panel-title">Administrator</span>',
        items:
        [
			{ html: '<p style="height:5px;">' },
			{
				xtype:'fieldset',
				columnWidth: 0.5,
				title: '&nbsp;<span class="panel-tab-title">Map source</span>&nbsp;',
				collapsible: true,
				animCollapse: true,
				autoHeight:true,
				items:
				[
					{
						xtype: 'combo',
						id: 'mapsource_cb',
						fieldLabel: 'Map source',
						labelSeparator: labelseparator,
						editable: false,
						valueField: 'id',
						displayField: 'text',
						isFormField: true,
						width: combo_width_fieldset,
						minListWidth: combo_list_width_fieldset,
						mode: 'local',
						triggerAction: 'all',
						value: MAPSOURCE,
						store: new Ext.data.SimpleStore({
							fields: ['id', 'text'],
							data: [[map_source_type_geojson, 'GeoJSON files'], [map_source_type_shapefile, 'Shapefiles'], [map_source_type_database, 'DHIS database']]
						}),
						listeners: {
							'select': {
								fn: function() {
									var msv = Ext.getCmp('mapsource_cb').getValue();
									var msrw = Ext.getCmp('mapsource_cb').getRawValue();
									
									if (MAPSOURCE == msv) {
										Ext.messageRed.msg('Map source', '<span class="x-msg-hl">' + msrw + '</span> is already selected.');
									}
									else {
										Ext.Ajax.request({
											url: path + 'setMapSourceTypeUserSetting' + type,
											method: 'POST',
											params: { mapSourceType: msv },
											success: function(r) {
												MAPSOURCE = msv;
												
												Ext.getCmp('map_cb').getStore().reload();
												Ext.getCmp('maps_cb').getStore().reload();
												Ext.getCmp('mapview_cb').getStore().reload();
												Ext.getCmp('view_cb').getStore().reload();
												Ext.getCmp('editmap_cb').getStore().reload();
												Ext.getCmp('maplayer_cb').getStore().reload();

												Ext.getCmp('map_cb').reset();
												Ext.getCmp('mapview_cb').reset();
												
												if (MAPSOURCE == map_source_type_geojson) {
													Ext.getCmp('register_chb').enable();
													
													if (Ext.getCmp('register_chb').checked) {
														mapping.show();
														shapefilePanel.show();
														mapLayerPanel.show();
													}
												}
												else if (MAPSOURCE == map_source_type_shapefile) {
													Ext.getCmp('register_chb').enable();
													
													if (Ext.getCmp('register_chb').checked) {
														mapping.show();
														shapefilePanel.show();
														mapLayerPanel.show();
													}
												}
												else if (MAPSOURCE == map_source_type_database) {
													Ext.getCmp('register_chb').disable();
													
													mapping.hide();
													shapefilePanel.hide();
													mapLayerPanel.hide();
												}
												
												if (MAP.layers.length > 2) {
													for (var i = MAP.layers.length - 1; i >= 2; i--) {
														MAP.removeLayer(MAP.layers[i]);
													}
												}
												addOverlaysToMap();
												
												Ext.messageBlack.msg('Map source', '<span class="x-msg-hl">' + msrw + '</span> is saved as map source.');
											},
											failure: function() {
												alert( 'Status', 'Error while saving data' );
											}
										});
									}
								}
							}
						}
					},
					{
						xtype: 'checkbox',
						id: 'register_chb',
						fieldLabel: 'Admin panels',
						labelSeparator: labelseparator,
						isFormField: true,
						listeners: {
							'check': {
								fn: function(checkbox,checked) {
									if (checked) {
										mapping.show();
										shapefilePanel.show();
										mapLayerPanel.show();
										Ext.getCmp('west').doLayout();
									}
									else {
										mapping.hide();
										shapefilePanel.hide();
										mapLayerPanel.hide();
										Ext.getCmp('west').doLayout();
									}
								},
								scope: this
							}
						}
					}
				]
			},
			{
				xtype:'fieldset',
				columnWidth: 0.5,
				title: '&nbsp;<span class="panel-tab-title">Base coordinate</span>&nbsp;',
				collapsible: true,
				animCollapse: true,
				autoHeight:true,
				items:
				[
					{
						xtype: 'combo',
						id: 'baselongitude_cb',
						fieldLabel: 'Longitude (x)',
						valueField: 'longitude',
						displayField: 'longitude',
						editable: true,
						isFormField: true,
						emptyText: emptytext,
						width: combo_number_width,
						minListWidth: combo_number_list_width,
						triggerAction: 'all',
						value: BASECOORDINATE.longitude,
						mode: 'remote',
						store: baseCoordinateStore
					},	
					{
						xtype: 'combo',
						id: 'baselatitude_cb',
						fieldLabel: 'Latitude (y)',
						valueField: 'latitude',
						displayField: 'latitude',
						editable: true,
						isFormField: true,
						emptyText: emptytext,
						width: combo_number_width,
						minListWidth: combo_number_list_width,
						triggerAction: 'all',
						value: BASECOORDINATE.latitude,
						mode: 'remote',
						store: baseCoordinateStore
					},
					{ html: '<p style="height:5px;">' },
					{
						xtype: 'button',
						isFormField: true,
						fieldLabel: '',
						labelSeparator: '',
						text: 'Save coordinate',
						cls: 'aa_med',
						handler: function() {
							var blo = Ext.getCmp('baselongitude_cb').getRawValue();
							var bla = Ext.getCmp('baselatitude_cb').getRawValue();
							
							Ext.Ajax.request({
								url: path + 'setBaseCoordinate' + type,
								method: 'POST',
								params: {longitude:blo, latitude:bla},
								
								success: function() {
									BASECOORDINATE = {longitude:blo, latitude:bla};
									Ext.messageBlack.msg('Base coordinate','Longitude <span class="x-msg-hl">' + blo + '</span> and latitude <span class="x-msg-hl">' + bla + '</span> was saved as base coordinate');
									Ext.getCmp('newlongitude_cb').getStore().reload();
									Ext.getCmp('newlongitude_cb').setValue(blo);
									Ext.getCmp('newlatitude_cb').setValue(bla);
									Ext.getCmp('baselongitude_cb').getStore().reload();
									Ext.getCmp('baselongitude_cb').setValue(blo);
									Ext.getCmp('baselatitude_cb').setValue(bla);
								},
								failure: function() {
									alert('Error: setBaseCoordinate');
								}
							});
						}
					}
				]
			}
        ],
        listeners: {
            expand: {
                fn: function() {
                    if (MAPSOURCE == map_source_type_geojson) {
                        Ext.getCmp('register_chb').enable();
                    }
                    else if (MAPSOURCE == map_source_type_database) {
                        Ext.getCmp('register_chb').disable();
                    }
					
					ACTIVEPANEL = administration;
                }
            },
			collapse: {
				fn: function() {
					ACTIVEPANEL = false;
				}
			}
        }
    });
	
	/* LAYERS */
	var vmap0 = new OpenLayers.Layer.WMS(
        'World',
        'http://labs.metacarta.com/wms/vmap0', 
        {layers: 'basic'}
    );
                                   
    var choroplethLayer = new OpenLayers.Layer.Vector('Thematic map', {
        'visibility': false,
        'displayInLayerSwitcher': false,
        'styleMap': new OpenLayers.StyleMap({
            'default': new OpenLayers.Style(
                OpenLayers.Util.applyDefaults(
                    {'fillOpacity': 1, 'strokeColor': '#222222', 'strokeWidth': 1 },
                    OpenLayers.Feature.Vector.style['default']
                )
            ),
            'select': new OpenLayers.Style(
                {'strokeColor': '#000000', 'strokeWidth': 2, 'cursor': 'pointer'}
            )
        })
    });
    
    MAP.addLayers([ vmap0, choroplethLayer ]);
    
	MAP.layers[0].setVisibility(false);
    
	function addOverlaysToMap() {
		Ext.Ajax.request({
			url: path + 'getAllMapLayers' + type,
			method: 'GET',
			success: function(r) {
				var mapLayers = Ext.util.JSON.decode(r.responseText).mapLayers;
				
				for (var i = 0; i < mapLayers.length; i++) {
					var mapurl = MAPSOURCE == map_source_type_geojson ? path + 'getGeoJson.action?name=' + mapLayers[i].mapSource : path_geoserver + wfs + mapLayers[i].mapSource + output;
					var fillColor = mapLayers[i].fillColor;
					var fillOpacity = parseFloat(mapLayers[i].fillOpacity);
					var strokeColor = mapLayers[i].strokeColor;
					var strokeWidth = parseFloat(mapLayers[i].strokeWidth);
					
					var treeLayer = new OpenLayers.Layer.Vector(mapLayers[i].name, {
						'visibility': false,
						'styleMap': new OpenLayers.StyleMap({
							'default': new OpenLayers.Style(
								OpenLayers.Util.applyDefaults(
									{'fillColor': fillColor, 'fillOpacity': fillOpacity, 'strokeColor': strokeColor, 'strokeWidth': strokeWidth},
									OpenLayers.Feature.Vector.style['default']
								)
							)
						}),
						'strategies': [new OpenLayers.Strategy.Fixed()],
						'protocol': new OpenLayers.Protocol.HTTP({
							'url': mapurl,
							'format': new OpenLayers.Format.GeoJSON()
						})
					});
					
					treeLayer.events.register('loadstart', null, function() {
						MASK.msg = 'Loading...';
						MASK.show();
					});
					
					treeLayer.events.register('loadend', null, function() {
						MASK.hide();
					});
						
					MAP.addLayer(treeLayer);
				}
			},
			failure: function() {
				alert('Error: getAllMapLayers');
			}
		});
	}
	
	addOverlaysToMap();
	
	var layerTreeConfig = [{
        nodeType: 'gx_baselayercontainer',
        singleClickExpand: true,
        expanded: true,
        text: 'Backgrounds',
		iconCls: 'icon-background'
    }, {
        nodeType: 'gx_overlaylayercontainer',
        singleClickExpand: true
    }, {
        nodeType: 'gx_layer',
        layer: 'Thematic map'
    }];       
    
    var layerTree = new Ext.tree.TreePanel({
        title: '<span class="panel-title">Map layers</span>',
        enableDD: true,
        bodyStyle: 'padding-bottom:5px;',
        rootVisible: false,
        root: {
            nodeType: 'async',
            children: layerTreeConfig
        }
    });
	
    /* WIDGETS */
    choropleth = new mapfish.widgets.geostat.Choropleth({
        id: 'choropleth',
        map: MAP,
        layer: choroplethLayer,
		title: '<span class="panel-title">Thematic map</span>',
        url: 'init',
        featureSelection: false,
        legendDiv: 'choroplethLegend',
        defaults: {width: 130},
        listeners: {
            expand: {
                fn: function() {
                    choroplethLayer.setVisibility(false);
                    choropleth.classify(false, true);
                    ACTIVEPANEL = thematicMap;
                }
            }
        }
    });
    
    mapping = new mapfish.widgets.geostat.Mapping({
        id: 'mapping',
        map: MAP,
        layer: choroplethLayer,
        title: '<span class="panel-title">Assign organisation units to map</span>',
        url: 'init',
        featureSelection: false,
        legendDiv: 'choroplethLegend',
        defaults: {width: 130},
        listeners: {
            expand: {
                fn: function() {
                    choroplethLayer.setVisibility(false);
                    mapping.classify(false, true);
                    ACTIVEPANEL = organisationUnitAssignment;
                }
            }
        }
    });
	
	/* TOOLBAR */  
	var mapLabel = new Ext.form.Label({
		text: 'Map',
		style: 'font:bold 11px arial; color:#333;'
	});
	
	var zoomInButton = new Ext.Button({
		iconCls: 'icon-zoomin',
		tooltip: 'Zoom in',
		handler:function() {
			MAP.zoomIn();
		},
		scope: this
	});
	
	var zoomOutButton = new Ext.Button({
		iconCls: 'icon-zoomout',
		tooltip: 'Zoom out',
		handler:function() {
			MAP.zoomOut();
		},
		scope: this
	});
	
	var zoomMaxExtentButton = new Ext.Button({
		iconCls: 'icon-zoommin',
		tooltip: 'Zoom to visible extent',
		handler: function() {
			MAP.zoomToMaxExtent();
		},
		scope: this
	});
		
	var labelsButton = new Ext.Button({
		iconCls: 'icon-labels',
		tooltip: 'Show/hide feature labels',
		handler: function() {
			toggleFeatureLabels(true);				
		}
	});
	
	var favoritesButton = new Ext.Button({
		cls: 'x-btn-text-icon',
		ctCls: 'aa_med',
		icon: '../../images/favorite_star2.png',
		text: 'Favorites',
		tooltip: 'Favorite map views',
		handler: function() {
			var x = Ext.getCmp('center').x + 15;
			var y = Ext.getCmp('center').y + 41;    
			viewWindow.setPosition(x,y);

			if (viewWindow.visible) {
				viewWindow.hide();
			}
			else {
				viewWindow.show();
			}
		}
	});
	
	function showExportMap() {		
		if (ACTIVEPANEL == thematicMap
			&& Ext.getCmp('period_cb').getValue()!='' 
			&& Ext.getCmp('indicator_cb').getValue()!=''
			&& Ext.getCmp('map_cb').getValue()!='') {
		
			var x = Ext.getCmp('center').x + 15;
			var y = Ext.getCmp('center').y + 41;   
			
			exportMapWindow.setPosition(x,y);

			if (exportMapWindow.visible) {
				exportMapWindow.hide();
			}
			else {
				exportMapWindow.show();
			}
		}
		else {
			Ext.messageRed.msg('Please render the map fist!','Form does not completed');
		}
	}
	
	var exportMapButton = new Ext.Button({
		iconCls: 'icon-export-map',
		tooltip: 'Export map to image/excel',
		handler: function() {
			showExportMap();				
		}
	});
	
	function showPdf() {
		var active = ACTIVEPANEL;
		var printMultiPagePanel = Ext.getCmp('printMultiPage_p');
		if (printMultiPagePanel.hidden) {
            printMultiPagePanel.show();
			printMultiPagePanel.expand();
        }
        else {
			printMultiPagePanel.collapse();
			printMultiPagePanel.hide();
			if (active == thematicMap) {
				choropleth.expand();
			}
			else if (active == organisationUnitAssignment) {
				mapping.expand();
			}
        }
	}
	
	var pdfButton = new Ext.Button({
		iconCls: 'icon-pdf',
		tooltip: 'Show/hide PDF printing panel',
		handler: function() {
			showPdf();				
		}
	});

    var automaticMapLegendSetButton = new Ext.Button({
		cls: 'x-btn-text-icon',
		ctCls: 'aa_med',
		icon: '../../images/color_swatch.png',
		text: 'Automatic LS',
		tooltip: 'Assign legend sets to indicators',
		handler: function() {
			var x = Ext.getCmp('center').x + 15;
			var y = Ext.getCmp('center').y + 41;    
			automaticMapLegendSetWindow.setPosition(x,y);
		
			if (automaticMapLegendSetWindow.visible) {
				automaticMapLegendSetWindow.hide();
			}
			else {
				automaticMapLegendSetWindow.show();
			}
		}
	});
	
	var predefinedMapLegendSetButton = new Ext.Button({
		cls: 'x-btn-text-icon',
		ctCls: 'aa_med',
		icon: '../../images/color_swatch.png',
		text: 'Predefined LS',
		tooltip: 'Assign legend sets to indicators',
		handler: function() {
			var x = Ext.getCmp('center').x + 15;
			var y = Ext.getCmp('center').y + 41;    
			predefinedMapLegendSetWindow.setPosition(x,y);
		
			if (predefinedMapLegendSetWindow.visible) {
				predefinedMapLegendSetWindow.hide();
			}
			else {
				predefinedMapLegendSetWindow.show();
			}
		}
	});
	
	var helpButton = new Ext.Button({
		cls: 'x-btn-text-icon',
		ctCls: 'aa_med',
		icon: '../../images/help.png',
		text: 'Help',
		tooltip: 'Get help for the active panel',
		handler: function() {
			var c = Ext.getCmp('center').x;
			var e = Ext.getCmp('east').x;
			helpWindow.setPagePosition(c+((e-c)/2)-280, Ext.getCmp('east').y + 100);
			helpWindow.show();
		}
	});
	
	var exitButton = new Ext.Button({
		text: 'Exit GIS',
		cls: 'x-btn-text-icon',
		ctCls: 'aa_med',
		icon: '../../images/exit.png',
		tooltip: 'Return to DHIS 2 Dashboard',
		handler: function() {
			window.location.href = '../../dhis-web-portal/redirect.action'
		}
	});
	
	var mapToolbar = new Ext.Toolbar({
		id: 'map_tb',
		items: [
			' ',' ',' ',
			mapLabel,
			' ',' ',' ',' ',' ',
			zoomInButton,
			zoomOutButton,
            ' ',
			zoomMaxExtentButton,
			labelsButton,
			'-',
			pdfButton,
			'-',
			exportMapButton,
			'-',
			favoritesButton,
			'-',
            automaticMapLegendSetButton,
			'-',
			predefinedMapLegendSetButton,
			'-',
			helpButton,
			'->',
			exitButton
		]
	});
    
	/* VIEWPORT */
    viewport = new Ext.Viewport({
        id: 'viewport',
        layout: 'border',
        margins: '0 0 5 0',
        items:
        [
            new Ext.BoxComponent(
            {
                region: 'north',
                id: 'north',
                el: 'north',
                height: north_height
            }),
            {
                region: 'east',
                id: 'east',
                collapsible: true,
                width: 200,
                margins: '0 5 0 5',
                defaults: {
                    border: true,
                    frame: true
                },
                layout: 'anchor',
                items:
                [
                    layerTree,
                    {
                        title: '<span class="panel-title">Overview map</span>',
                        html:'<div id="overviewmap" style="height:97px; padding-top:0px;"></div>'
                    },
                    {
                        title: '<span class="panel-title">Cursor position</span>',
                        height: 65,
                        contentEl: 'position',
                        anchor: '100%',
                        bodyStyle: 'padding-left: 4px;'
                    },
					{
						xtype: 'panel',
						title: '<span class="panel-title">Feature data</span>',
						height: 65,
						anchor: '100%',
						bodyStyle: 'padding-left: 4px;',
						items:
						[
							new Ext.form.Label({
								id: 'featureinfo_l',
								text: 'No feature selected.',
								style: 'color:#666'
							})
						]
					},
                    {
                        title: '<span class="panel-title">Map legend</span>',
                        minHeight: 65,
                        autoHeight: true,
                        contentEl: 'legend',
                        anchor: '100%',
						bodyStyle: 'padding-left: 4px;'
                    }
                ]
            },
            {
                region: 'west',
                id: 'west',
                split: true,
				title: '',
                collapsible: true,
				collapseMode: 'mini',
                width: west_width,
                minSize: 175,
                maxSize: 500,
                margins: '0 0 0 5',
                layout: 'accordion',
                defaults: {
                    border: true,
                    frame: true
                },
                items: [
                    choropleth,
                    shapefilePanel,
                    mapping,
                    mapLayerPanel,
					adminPanel,
					{
						xtype: 'print-multi',
						id: 'printMultiPage_p',
						title: '<span class="panel-title">Print multi page PDF</span>',
						formConfig: {
							labelWidth: 65,
							bodyStyle: 'padding: 7px;',
							defaults: {
								width: 140,
								listWidth: 140
							}
						},
						columns: [
							{
								header: 'Map title',
								width: 80,
								dataIndex: 'mapTitle',
								editor: new Ext.form.TextField()
							},
							{
								header: 'Comment',
								dataIndex: 'comment',
								editor: new Ext.form.TextField()
							}
						],
						border: false,
						map: MAP,
						configUrl: printConfigUrl,
						overrides: layerOverrides
					}
                ]
            },
            {
                xtype: 'gx_mappanel',
                region: 'center',
                id: 'center',
                height: 1000,
                width: 800,
                map: MAP,
                title: '',
                zoom: 3,
				tbar: mapToolbar
            }
        ]
    });
	
    shapefilePanel.hide();
	mapping.hide();
    mapLayerPanel.hide();
	Ext.getCmp('printMultiPage_p').hide();
	ACTIVEPANEL = thematicMap;
    
	/* MAP CONTROLS */
	var selectFeatureChoropleth = new OpenLayers.Control.newSelectFeature(
        choroplethLayer, {
            onClickSelect: onClickSelectChoropleth,
            onClickUnselect: onClickUnselectChoropleth,
            onHoverSelect: onHoverSelectChoropleth,
            onHoverUnselect: onHoverUnselectChoropleth
        }
    );
    
    MAP.addControl(selectFeatureChoropleth);
    selectFeatureChoropleth.activate();

	MAP.addControl(new OpenLayers.Control.MousePosition({
        displayClass: 'void', 
        div: $('mouseposition'), 
        prefix: '<span color="#666">x: &nbsp;</span>',
        separator: '<br/><span color="#666">y: &nbsp;</span>'
    }));

    MAP.addControl(new OpenLayers.Control.OverviewMap({
        div: $('overviewmap'),
        size: new OpenLayers.Size(188, 97),
        minRectSize: 0
    }));
    
    MAP.addControl(new OpenLayers.Control.ZoomBox());
	
	MAP.setCenter(new OpenLayers.LonLat(BASECOORDINATE.longitude, BASECOORDINATE.latitude), 6);
    
	MAP.events.on({
        changelayer: function(e) {
            if (e.property == 'visibility' && e.layer != choroplethLayer && e.layer != vmap0) {
                if (e.layer.visibility) {
                    selectFeatureChoropleth.deactivate();
                }
                else {
                    selectFeatureChoropleth.activate();
                }
            }
        }
    });
	
    Ext.get('loading').fadeOut({remove: true});
	
	}});
	}});
	}});
	}});
});

/*SELECT FEATURES*/

var popup;

function onHoverSelectChoropleth(feature) {
    if (MAPDATA != null) {
        if (ACTIVEPANEL == thematicMap) {
			Ext.getCmp('featureinfo_l').setText('<div style="color:black">' + feature.attributes[MAPDATA.nameColumn] + '</div><div style="color:#555">' + feature.attributes.value + '</div>', false);
        }
        else if (ACTIVEPANEL == organisationUnitAssignment) {
			Ext.getCmp('featureinfo_l').setText('<span style="color:black">' + feature.attributes[MAPDATA.nameColumn] + '</span>', false);
        }
    }
}

function onHoverUnselectChoropleth(feature) {
    Ext.getCmp('featureinfo_l').setText('<span style="color:#666">No feature selected.</span>', false);
}

function onClickSelectChoropleth(feature) {
	var east_panel = Ext.getCmp('east');
	var x = east_panel.x - 210;
	var y = east_panel.y + 41;
	
    if (ACTIVEPANEL == organisationUnitAssignment) {
		if (popup) {
			popup.destroy();
		}
		
		var feature_popup = new Ext.Window({
			title: '<span class="panel-title">Assign organisation unit</span>',
			width: 180,
			height: 60,
			layout: 'fit',
			plain: true,
			html: '<div class="window-orgunit-text">' + feature.attributes[MAPDATA.nameColumn] + '</div>',
			x: x,
			y: y,
			listeners: {
				'close': {
					fn: function() {
						mapping.relation = false;
					}
				}
			}
		});
		
		popup = feature_popup;		
		feature_popup.show();
		mapping.relation = feature.attributes[MAPDATA.nameColumn];
    }
	else {
		MAP.setCenter(feature.geometry.getBounds().getCenterLonLat(), MAP.getZoom()+1);
	}
}

function onClickUnselectChoropleth(feature) {}

/*MAP DATA*/
function loadMapData(redirect, position) {

    Ext.Ajax.request({
        url: path + 'getMapByMapLayerPath' + type,
        method: 'POST',
        params: { mapLayerPath: URL, format: 'json' },
        success: function(r) {
		
            MAPDATA = Ext.util.JSON.decode(r.responseText).map[0];	
            
            if (MAPSOURCE == map_source_type_database) {
                MAPDATA.name = Ext.getCmp('map_cb').getRawValue();
                MAPDATA.organisationUnit = 'Country';
                MAPDATA.organisationUnitLevel = Ext.getCmp('map_cb').getValue();
                MAPDATA.nameColumn = 'name';
                MAPDATA.longitude = BASECOORDINATE.longitude;
                MAPDATA.latitude = BASECOORDINATE.latitude;
                MAPDATA.zoom = 7;
            }
            else if (MAPSOURCE == map_source_type_geojson || MAPSOURCE == map_source_type_shapefile) {
                MAPDATA.organisationUnitLevel = parseFloat(MAPDATA.organisationUnitLevel);
                MAPDATA.longitude = parseFloat(MAPDATA.longitude);
                MAPDATA.latitude = parseFloat(MAPDATA.latitude);
                MAPDATA.zoom = parseFloat(MAPDATA.zoom);
            }
			
			if (!position) {
				if (MAPDATA.zoom != MAP.getZoom()) {
					MAP.zoomTo(MAPDATA.zoom);
				}
				MAP.setCenter(new OpenLayers.LonLat(MAPDATA.longitude, MAPDATA.latitude));
			}
			
			if (MAPVIEW) {
				if (MAPVIEW.longitude && MAPVIEW.latitude && MAPVIEW.zoom) {
					MAP.setCenter(new OpenLayers.LonLat(MAPVIEW.longitude, MAPVIEW.latitude), MAPVIEW.zoom);
				}
				else {
					MAP.setCenter(new OpenLayers.LonLat(MAPDATA.longitude, MAPDATA.latitude), MAPDATA.zoom);
				}
				MAPVIEW = false;
			}
			
			toggleFeatureLabels(false);

            if (redirect == thematicMap) {
                getChoroplethData(); }
            else if (redirect == organisationUnitAssignment) {
                getAssignOrganisationUnitData(); }
            else if (redirect == 'auto-assignment') {
                getAutoAssignOrganisationUnitData(position); }
        },
        failure: function() {
            alert( 'Error while retrieving map data: loadMapData' );
        } 
    });
}

function getExportDataValueJSON( mapvalues ){
	var json = '{';
	json += '"datavalues":';
	json += '[';	
	for (var i = 0; i < mapvalues.length; i++) {		
		json += '{';
		json += '"organisation": "' + mapvalues[i].orgUnitId + '",';
		json += '"value": "' + mapvalues[i].value + '" ';
		if(i < mapvalues.length-1){
			json += '},';
		}else{
			json += '}';
		}
	}
	json += ']';
	json += '}';
	
	return json;
	
}

/*CHOROPLETH*/
function getChoroplethData() {
	MASK.msg = 'Creating choropleth...';
	MASK.show();
	
    var indicatorId = Ext.getCmp('indicator_cb').getValue();
    var periodId = Ext.getCmp('period_cb').getValue();
    var mapLayerPath = MAPDATA.mapLayerPath;
	var url = MAPSOURCE == map_source_type_geojson || MAPSOURCE == map_source_type_shapefile ? 'getMapValuesByMap' : 'getMapValuesByLevel';
	var params = MAPSOURCE == map_source_type_geojson || MAPSOURCE == map_source_type_shapefile ? { indicatorId: indicatorId, periodId: periodId, mapLayerPath: mapLayerPath } : { indicatorId: indicatorId, periodId: periodId, level: URL };

    Ext.Ajax.request({
        url: path + url + type,
        method: 'POST',
        params: params,

        success: function(r) {
			var layers = MAP.getLayersByName('Thematic map');
			var features = layers[0].features;
			var mapvalues = Ext.util.JSON.decode(r.responseText).mapvalues;
			
			EXPORTVALUES = getExportDataValueJSON( mapvalues );	
			
			var mv = new Array();
			var nameColumn = MAPDATA.nameColumn;
			var options = {};
			
			if (mapvalues.length == 0) {
				Ext.messageRed.msg('Thematic map', 'The selected indicator, period and level returned no data.');
				MASK.hide();
				return;
			}

			for (var i = 0; i < mapvalues.length; i++) {
				mv[mapvalues[i].featureId] = mapvalues[i].featureId ? mapvalues[i].value : '';
			}

			if (MAPSOURCE == map_source_type_geojson || MAPSOURCE == map_source_type_shapefile) {
				for (var j = 0; j < features.length; j++) {
					features[j].attributes.value = mv[features[j].attributes[nameColumn]] ? mv[features[j].attributes[nameColumn]] : 0;
				}
			}
			else if (MAPSOURCE == map_source_type_database) {
				for (var i = 0; i < mapvalues.length; i++) {
					for (var j = 0; j < features.length; j++) {
						if (mapvalues[i].orgUnitName == features[j].attributes.name) {
							features[j].attributes.value = parseFloat(mapvalues[i].value);
							break;
						}
					}
				}
			}

			choropleth.indicator = 'value';
			choropleth.indicatorText = 'Indicator';
			options.indicator = choropleth.indicator;
			
			options.method = Ext.getCmp('method').getValue();
			options.numClasses = Ext.getCmp('numClasses').getValue();
			options.colors = choropleth.getColors();
			
			choropleth.coreComp.updateOptions(options);
			choropleth.coreComp.applyClassification();
			choropleth.classificationApplied = true;
			
			MASK.hide();		
        },
        failure: function() {
            alert( 'Error: getMapValues' );
        } 
    });
}

/*MAPPING*/
function getAssignOrganisationUnitData() {
	MASK.msg = 'Creating map...';
	MASK.show();
	
    var mlp = MAPDATA.mapLayerPath;
    
    Ext.Ajax.request({
        url: path + 'getAvailableMapOrganisationUnitRelations' + type,
        method: 'GET',
        params: { mapLayerPath: mlp, format: 'json' },

        success: function(r) {
			var layers = MAP.getLayersByName('Thematic map');
			features = layers[0]['features'];
			var relations = Ext.util.JSON.decode(r.responseText).mapOrganisationUnitRelations;
			var nameColumn = MAPDATA.nameColumn;
			var noCls = 1;
			var noAssigned = 0;
			var options = {};
			
			for (var i = 0; i < features.length; i++) {
				features[i].attributes['value'] = 0;
			
				for (var j=0; j < relations.length; j++) {
					if (relations[j].featureId == features[i].attributes[nameColumn]) {
						features[i].attributes['value'] = 1;
						noAssigned++;
						if (noCls < 2) {
							noCls = 2;
						}
						break;
					}
				}
			}
	
			var color = noCls > 1 && noAssigned == features.length ? assigned_row_color : unassigned_row_color;
			noCls = noCls > 1 && noAssigned == features.length ? 1 : noCls;
			
			mapping.indicator = 'value';
			mapping.indicatorText = 'Indicator';
			options.indicator = mapping.indicator;
			
			options.method = 1;
			options.numClasses = noCls;
			
			var colorA = new mapfish.ColorRgb();
			colorA.setFromHex(color);
			var colorB = new mapfish.ColorRgb();
			colorB.setFromHex(assigned_row_color);
			options.colors = [colorA, colorB];
			
			mapping.coreComp.updateOptions(options);
			mapping.coreComp.applyClassification();
			mapping.classificationApplied = true;
			
			MASK.hide();
        },
        failure: function() {
            alert( 'Error while retrieving data: getAssignOrganisationUnitData' );
        } 
    });
}

/*AUTO MAPPING*/
function getAutoAssignOrganisationUnitData(position) {
	MASK.msg = 'Loading data...';
	MASK.show();

    var level = MAPDATA.organisationUnitLevel;

    Ext.Ajax.request({
        url: path + 'getOrganisationUnitsAtLevel' + type,
        method: 'POST',
        params: { level: level, format: 'json' },

        success: function(r) {
		    var layers = MAP.getLayersByName('Thematic map');
			var features = layers[0]['features'];
			var organisationUnits = Ext.util.JSON.decode(r.responseText).organisationUnits;
			var nameColumn = MAPDATA.nameColumn;
			var mlp = MAPDATA.mapLayerPath;
			var count_match = 0;
			var relations = '';
			var featureName, orgunitName;
			
			for ( var i = 0; i < features.length; i++ ) {
				features[i].attributes.compareName = features[i].attributes[nameColumn].split(' ').join('').toLowerCase();
			}
	
			for ( var i = 0; i < organisationUnits.length; i++ ) {
				organisationUnits[i].compareName = organisationUnits[i].name.split(' ').join('').toLowerCase();
			}
			
			for ( var i=0; i < organisationUnits.length; i++ ) {
				for ( var j=0; j < features.length; j++ ) {
					if (features[j].attributes.compareName == organisationUnits[i].compareName) {
						count_match++;
						relations += organisationUnits[i].id + '::' + features[j].attributes[nameColumn] + ';;';
						break;
					}
				}
			}
			
			MASK.msg = count_match == 0 ? 'No organisation units assigned...' : 'Assigning ' + count_match + ' organisation units...';
			MASK.show();

			Ext.Ajax.request({
				url: path + 'addOrUpdateMapOrganisationUnitRelations' + type,
				method: 'POST',
				params: { mapLayerPath: mlp, relations: relations },

				success: function(r) {
					MASK.msg = 'Applying organisation units relations...';
					MASK.show();
					
					Ext.messageBlack.msg('Assign organisation units', '<span class="x-msg-hl">' + count_match + '</span> organisation units assigned.<br><br>Database: <span class="x-msg-hl">' + organisationUnits.length + '</span><br>Shapefile: <span class="x-msg-hl">' + features.length + '</span>');
					
					Ext.getCmp('grid_gp').getStore().reload();
					loadMapData(organisationUnitAssignment, position);
				},
				failure: function() {
					alert( 'Error: addOrUpdateMapOrganisationUnitRelations' );
				} 
			});
        },
        failure: function() {
            alert( 'Status', 'Error while retrieving data' );
        } 
    });
}
