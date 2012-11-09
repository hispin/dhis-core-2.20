
function organisationUnitSelected( orgUnits, orgUnitNames )
{
	hideById('dataEntryInfor');
	hideById('listDiv');
	showById('mainLinkLbl');
	setFieldValue("filter", false);
	jQuery.getJSON( "anonymousPrograms.action",{}, 
		function( json )
		{   
			jQuery('#searchingAttributeIdTD [id=searchObjectId] option').remove();
			jQuery('#advancedSearchTB [id=searchObjectId] option').remove();
			clearListById('displayInReports');
			clearListById('programId');
			
			jQuery( '#programId').append( '<option value="" psid="" reportDateDes="' + i18n_report_date + '">[' + i18n_please_select + ']</option>' );
			for ( i in json.programs ) {
				jQuery( '#programId').append( '<option value="' + json.programs[i].id +'" psid="' + json.programs[i].programStageId + '" reportDateDes="' + json.programs[i].reportDateDescription + '">' + json.programs[i].name + '</option>' );
			}
			disableCriteriaDiv();
			showById('selectDiv');
		});
		
	setFieldValue( 'orgunitId', orgUnits[0] );
	setFieldValue( 'orgunitName', orgUnitNames[0] );
	hideById('listDiv');
	hideById('dataEntryInfor');
}

selection.setListenerFunction( organisationUnitSelected );

function disableCriteriaDiv()
{
	disable('listBtn');
	disable('addBtn');
	disable('advancedBtn');
	disable('removeBtn');
	jQuery('#criteriaDiv :input').each( function( idx, item ){
		disable(this.id);
	});
}

function enableCriteriaDiv()
{
	enable('listBtn');
	enable('addBtn');
	enable('advancedBtn');
	enable('removeBtn');
	jQuery('#criteriaDiv :input').each( function( idx, item ){
		enable(this.id);
	});
}

function getDataElements()
{
	hideById('dataEntryInfor');
	hideById('listDiv');
	jQuery('#searchingAttributeIdTD [id=searchObjectId] option').remove();
	jQuery('#advancedSearchTB [id=searchObjectId] option').remove();
	programStageId = jQuery('#programId option:selected').attr('psid');
	setFieldValue('programStageId', programStageId );
	setInnerHTML('reportDateDescriptionField', jQuery('#programId option:selected').attr('reportDateDes'));
	setInnerHTML('reportDateDescriptionField2', jQuery('#programId option:selected').attr('reportDateDes'));
	
	if( programStageId == '')
	{
		removeAllAttributeOption();
		disableCriteriaDiv();
		enable('orgunitName');
		enable('programId');
		hideById('listDiv');
		setFieldValue('searchText');
		return;
	}
	
	jQuery.getJSON( "getProgramStageDataElementList.action",
		{
			programStageId: getFieldValue('programStageId')
		}, 
		function( json ) 
		{   
			jQuery('.stage-object-selected').attr('psid', jQuery('#programId option:selected').attr("psid"));
	
			clearListById('searchObjectId');
			clearListById('displayInReports');
			
			jQuery( '#searchObjectId').append( '<option value="" >[' + i18n_please_select + ']</option>' );
			for ( i in json.programStageDataElements ) {
				jQuery( '[id=searchObjectId]').append( '<option value="' + json.programStageDataElements[i].id + '" type="' + json.programStageDataElements[i].type +'">' + json.programStageDataElements[i].name + '</option>' );
				
				if( json.programStageDataElements[i].displayInReports=='true' ){
					jQuery( '#displayInReports').append( '<option value="' + json.programStageDataElements[i].id + '"></option>');
				}
			}
			
			enableCriteriaDiv();
			
			validateSearchEvents( true );
			setFieldValue('isShowEventList', true);
		});
}

function dataElementOnChange( this_ )
{
	var container = jQuery(this_).parent().parent().attr('id');
	var element = jQuery('#' + container + ' [id=searchText]');
	var valueType = jQuery('#' + container+ ' [id=searchObjectId] option:selected').attr('type');
	
	if( valueType == 'date' ){
		element.replaceWith( getDateField( container ) );
		datePickerValid( 'searchText_' + container );
		return;
	}
	else
	{
		$( '#searchText_' + container ).datepicker("destroy");
		$('#' + container + ' [id=dateOperator]').replaceWith("");
		
		if( valueType == 'bool' ){
			element.replaceWith( getTrueFalseBox() );
		}
		else if ( valueType=='optionset' ){
			element.replaceWith( searchTextBox );
			autocompletedFilterField( container + " [id=searchText]" , jQuery(this_).val() );
		}
		else{
			element.replaceWith( searchTextBox );
		}
	}
}

function autocompletedFilterField( idField, searchObjectId )
{
	var input = jQuery( "#" +  idField );
	input.autocomplete({
		delay: 0,
		minLength: 0,
		source: function( request, response ){
			$.ajax({
				url: "getOptions.action?id=" + searchObjectId + "&query=" + input.val(),
				dataType: "json",
				success: function(data) {
					response($.map(data.options, function(item) {
						return {
							label: item.o,
							id: item.o
						};
					}));
				}
			});
		},
		minLength: 2,
		select: function( event, ui ) {
			input.val(ui.item.value);
			input.autocomplete( "close" );
		}
	})
	.addClass( "ui-widget" );
}

function removeAllAttributeOption()
{
	jQuery( '#advancedSearchTB tbody tr' ).each( function( i, item )
    {
		if(i>0){
			jQuery( item ).remove();
		}
	})
}

function validateSearchEvents( listAll )
{	
	var flag = true;
	if( !listAll )
	{
		if( getFieldValue('startDate')=="" || getFieldValue('endDate')=="" ){
			showWarningMessage( i18n_specify_a_date );
			flag = false;
		}
		
		if(flag && getFieldValue('filter') == "true" )
		{
			jQuery( '#advancedSearchTB tr' ).each( function( i, row ){
				jQuery( this ).find(':input').each( function( idx, item ){
					var input = jQuery( item );
					if( input.attr('type') != 'button' && idx==0 && input.val()=='' ){
						showWarningMessage( i18n_specify_data_element );
						flag = false;
					}
				})
			});
		}
	}
	
	if(flag){
		searchEvents( listAll );
	}
}

function searchEvents( listAll )
{
	hideById('dataEntryInfor');
	hideById('listDiv');
	setFieldValue('isShowEventList', listAll );
	
	var params = '';
	jQuery( '#displayInReports option' ).each( function( i, item ){
		var input = jQuery( item );
		params += '&searchingValues=de_' + input.val() + '_false_';
	});
	
	if(listAll){	
		params += '&startDate=';
		params += '&endDate=';
	}
	else{
		var value = '';
		var searchingValue = '';
		params += '&startDate=' + getFieldValue('startDate');
		params += '&endDate=' + getFieldValue('endDate');
		jQuery( '#advancedSearchTB tr' ).each( function(){
			jQuery( this ).find(':input').each( function( idx, item ){
				var input = jQuery( item );
				if( input.attr('type') != 'button' ){
					if( idx==0 && input.val()!=''){
						searchingValue = 'de_' + input.val() + '_false_';
					}
					else if( input.val()!='' ){
						value += jQuery.trim(input.val()).toLowerCase();
					}
				}
			});
			
			if( value !=''){
				searchingValue += getValueFormula(value);
			}
			params += '&searchingValues=' + searchingValue;
			searchingValue = '';
			value = '';
		})
	}
	
	params += '&facilityLB=selected';
	params += '&level=0';
	params += '&orgunitIds=' + getFieldValue('orgunitId');
	params += '&programStageId=' + jQuery('#programId option:selected').attr('psid');
	params += '&orderByOrgunitAsc=false';
	
	contentDiv = 'listDiv';
	showLoader();
	
	$.ajax({
		type: "POST",
		url: 'searchProgramStageInstances.action',
		data: params,
		success: function( html ){
			hideById('dataEntryInfor');
			setInnerHTML( 'listDiv', html );
			
			var searchInfor = (listAll) ? i18n_list_all_events : i18n_search_events_by_dataelements;
			setInnerHTML( 'searchInforTD', searchInfor);
	
			if(getFieldValue('filter')=='true')
			{
				showById('minimized-advanced-search');
				hideById('advanced-search');
			}
	
			showById('listDiv');
			hideById('loaderDiv');
		}
    });
}

function updateEvents()
{
	validateSearchEvents( false );
}

function getValueFormula( value )
{
	if( value.indexOf('"') != value.lastIndexOf('"') )
	{
		value = value.replace(/"/g,"'");
	}
	// if key is [xyz] && [=xyz]
	if( value.indexOf("'")==-1 ){
		var flag = value.match(/[>|>=|<|<=|=|!=]+[ ]*/);
	
		if( flag == null )
		{
			value = "='"+ value + "'";
		}
		else
		{
			value = value.replace( flag, flag + "'");
			value +=  "'";
		}
	}
	// if key is ['xyz'] && [='xyz']
	// if( value.indexOf("'") != value.lastIndexOf("'") )
	else
	{
		var flag = value.match(/[>|>=|<|<=|=|!=]+[ ]*/);
	
		if( flag == null )
		{
			value = "="+ value;
		}
	}
	
	return value;
}

function removeEvent( programStageId )
{	
	removeItem( programStageId, '', i18n_comfirm_delete_event, 'removeCurrentEncounter.action' );	
}

function showUpdateEvent( programStageInstanceId )
{
	hideById('selectDiv');
    hideById('searchDiv');
    hideById('listDiv');
	setFieldValue('programStageInstanceId', programStageInstanceId);
	setInnerHTML('dataEntryFormDiv','');
    showLoader();

	$( '#dataEntryFormDiv' ).load( "dataentryform.action", 
		{ 
			programStageInstanceId: programStageInstanceId
		},function()
		{
			jQuery('#inputCriteriaDiv').remove();
			hideById('mainLinkLbl');
			showById('actionDiv');
			var programName = jQuery('#programId option:selected').text();
			var programStageId = jQuery('#programId option:selected').attr('psid');
			jQuery('.stage-object-selected').attr('psid',programStageId);
			setInnerHTML('programName', programName );
			if( getFieldValue('completed')=='true' ){
				disable("completeBtn");
				enable("uncompleteBtn");
			}
			else{
				enable("completeBtn");
				disable("uncompleteBtn");
			}
			hideById('loaderDiv');
			showById('dataEntryInfor');
			showById('entryFormContainer');
		});
}

function backEventList()
{
	hideById('dataEntryInfor');
	showById('mainLinkLbl');
	showById('selectDiv');
	showById('searchDiv');
	showById('listDiv');
	searchEvents( getFieldValue('isShowEventList') );
}

function showAddEventForm()
{
	setInnerHTML('dataEntryFormDiv','');
	setFieldValue('executionDate','');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('listDiv');
	hideById('mainLinkLbl');
	hideById('actionDiv');
	showById('dataEntryInfor');
	setFieldValue('programStageInstanceId','0');
	byId('executionDate').style.backgroundColor = "#ffffff";
	setInnerHTML('programName', jQuery('#programId option:selected').text());
}

function addNewEvent()
{
	var programStageInstanceId = getFieldValue('programStageInstanceId');
	var programId = jQuery('#programId option:selected').val();
	var executionDate = getFieldValue('executionDate');
	jQuery("#executionDate").css('background-color', SAVING_COLOR);
	jQuery.postJSON( "saveExecutionDate.action",
		{
			programStageInstanceId:programStageInstanceId,
			programId:programId,
			executionDate:executionDate
		}, 
		function( json ) 
		{    
			if(json.response=='success')
			{
				jQuery("#executionDate").css('background-color', SUCCESS_COLOR);
				setFieldValue('programStageInstanceId',json.message);
				if(programStageInstanceId != json.message){
					showUpdateEvent( json.message );
				}
			}
			else
			{
				jQuery("#executionDate").css('background-color', ERROR_COLOR);
				showWarningMessage( json.message );
			}
		});
}

function completedAndAddNewEvent()
{
	doComplete( true );
}

function removeEmptyEvents()
{	
	var result = window.confirm( i18n_confirm_remove_empty_events );
    
    if ( result )
    {
		jQuery.getJSON( "removeEmptyEvents.action",
			{
				programStageId: jQuery('#selectDiv [id=programId] option:selected').attr('psid')
			}, 
			function( json ) 
			{   
				if(json.response=='success')
				{
					showSuccessMessage( i18n_remove_empty_events_success );
					validateSearchEvents( true )
				}
			});
	}
}

function removeCurrentEvent()
{	
    var result = window.confirm( i18n_comfirm_delete_event );
    if ( result )
    {
    	$.postJSON(
    	    "removeCurrentEncounter.action",
    	    {
    	        "id": getFieldValue('programStageInstanceId')   
    	    },
    	    function( json )
    	    { 
    	    	if ( json.response == "success" )
    	    	{
					backEventList();
				}
				else if ( json.response == "error" )
    	    	{ 
					showWarningMessage( json.message );
    	    	}
			});
	}
}

function showFilterForm()
{
	jQuery('#advanced-search').toggle();
	hideById('minimized-advanced-search');
	disable('advancedBtn');
	setFieldValue('filter', true);
}

function removeAllOption()
{
	enable('advancedBtn');
	setFieldValue('filter', false);
	jQuery('#advancedBtn').val(i18n_add_filter);
	jQuery('#advancedBtn').attr("isShown", false);
	jQuery( '#advancedSearchTB tr' ).each( function( i, row ){
		if(i==0){
			jQuery( this ).find(':input').each( function( idx, item ){
				var input = jQuery( item );
				if( input.attr('type') != 'button'){
					input.val('');
				}
			});
		}
		else{
			jQuery(this).remove();
		}
	});
	hideById('advanced-search');
	hideById('minimized-advanced-search');
	searchEvents( false );
}
