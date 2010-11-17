var currentPDSCode;

function validatePostVacant( dataValueMapKey )
{
	var reportingDate = document.getElementById('reportingDate').value;
	var request = new Request();
	request.setResponseTypeXML('element');
	request.setCallbackSuccess( elementReceived );
	request.send( 'getValidatePostVacant.action?dataValue=' + sancPos + '&dataValueMapKey=' + dataValueMapKey + '&reportingDate=' + reportingDate);
}

function elementReceived( recordNo )
{
	var type = recordNo.getAttribute( "type" );
	if (type == 'success' )
	{
		if( confirm ( recordNo.firstChild.nodeValue ) )
		{
			showEmployeePostForm( sancPos, dataValueMapKey );
		}
	}
	else if (type == 'input' )
	{
		alert( recordNo.firstChild.nodeValue );
	}

}
function getEmployeeName( pdsCodeField, pdsCode )
	{
		currentPDSCode = pdsCodeField;
		var request = new Request();
		request.setResponseTypeXML( 'employee' );
		request.setCallbackSuccess( employeeReceived );
		request.send( 'validateEmployeeExist.action?pdsCode=' + pdsCode );
	}

function employeeReceived( employeeElement )
{
	var type = employeeElement.getAttribute( "type" );
	if (type == 'success') 
	{
		if( confirm( employeeElement.firstChild.nodeValue ) )
		{
		}
		else
		{
			var field = document.getElementById( currentPDSCode );
			field.value = "";
			setTimeout(function(){
		        field.focus();field.select();
		    },2);
		}
	}
	else if(type == 'employeeexist')
	{
		if( confirm( employeeElement.firstChild.nodeValue ) )
		{
			alert("Edit Employee");
		}
		else
		{
			var field = document.getElementById( currentPDSCode );
			field.value = "";
			setTimeout(function(){
        	field.focus();field.select();
   			},2);
		}
	}
	else if(type == 'input') 
	{
		if( confirm( employeeElement.firstChild.nodeValue ) )
		{
			var url = 'showAddEmployeeForm.action';
			document.location.href = url;
		}
		else
		{
			var field = document.getElementById( currentPDSCode );
			field.value = "";
			setTimeout(function(){
                field.focus();field.select();
            },2);
		}
	}
	
}

function addLLBNewRow()
{
    var tbl = document.getElementById("tblGrid");
    lastRow = tbl.rows.length;
    curRow = lastRow + 1;
    var newRow = tbl.insertRow(lastRow);
    var oCell = "";
    var i=1;
    oCell = newRow.insertCell(0);
    oCell.innerHTML = '<label id="sr.no">'+lastRow+'</label>';

    for( var element in jsllElementOptions)
    {
        oCell = newRow.insertCell(i);

        tempStr = element + ":"+lastRow ;

        date = "getDate:"+tempStr;
        var options = jsllElementOptions[element];
        var type = jsllElementPtype[element];
        var inputFieldVal = "";
         var butVal = "";
         //alert("jsllElementSize = "+jsllElementSize);
        if( options == null || options.length == 0 )
        {
            if(type=='text')
            {
                    oCell.innerHTML = '<input type="text" name="'+tempStr+'" id = "'+tempStr+'" style="width:10em"/>';
            }
            else if(type=='calender')
            {
                
                    oCell.innerHTML = "<input name='"+tempStr+"' id='"+tempStr+"' type='text' style='width:10em'> <img src='../images/calendar_icon.gif' width='16' height='16' id='"+date+"' style='cursor: pointer;' title='Choose a date' >";
                    inputFieldVal =  tempStr;
                    butVal = date;

                    Calendar.setup({
                        inputField:inputFieldVal,
                        ifFormat:"%Y-%m-%d",
                        button:butVal
                    });
            }
        }
        else
        {
            var tempStr1;

            tempStr1 = '<select name="'+tempStr+'" id="'+tempStr+'" ><option name="SelectOption" value="" selected>--Select--</option>';

            for( var j=0; j<options.length; j++ )
            {
                tempStr1 += '<option value="'+options[j]+'">'+options[j]+'</option>'
            }
            tempStr1 += '</select>';

            oCell.innerHTML = tempStr1;

        }

        i++;
    }
}

function removeLLRecord( delRecordNo )
{
    var result = window.confirm( 'Do you want to save new records and delete this record' );

    if ( result )
    {
        document.getElementById('totalRecords').value = (lastRow-1);
        document.getElementById('delRecordNo').value = delRecordNo;

        document.getElementById('LineListDataEntryForm').submit();
    }
}

function showEmployeePostForm( sancPos, dataValueMapKey ) 
{
	var reportingDate = document.getElementById( "reportingDate" ).value;
	var url = 'showEmployeePostForm.action?reportingDate=' + reportingDate + '&dataValue=' + sancPos + '&dataValueMapKey=' + dataValueMapKey;
	$('#contentDataRecord').dialog('destroy').remove();
    $('<div id="contentDataRecord" style="z-index: 1;">' ).load(url).dialog({
    title: 'Employee Post Detail',
	maximize: true, 
	closable: true,
	modal:true,
	overlay:{background:'#000000', opacity:0.1},
	width: 420,
    height: 380
});
}