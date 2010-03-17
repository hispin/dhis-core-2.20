
function saveDocument()
{
    var url = "validateDocument.action?name=" + getFieldValue( "name" ) + "&id=" + getFieldValue( "id" );
	
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( saveDocumentReceived );
    request.send( url );
}

function saveDocumentReceived( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    
    if ( type == "input" )
    {
        setMessage( message );
        
        return false;
    }
    else if ( type == "success" )
    {
        document.getElementById( "documentForm" ).submit();
    }
}

function removeDocument( id )
{
	removeItem( id, "", i18n_confirm_remove_report, "removeDocument.action" );
}

function addDocumentToDashboard( id )
{
    var dialog = window.confirm( i18n_confirm_add_to_dashboard );
    
    if ( dialog )
    {
        var request = new Request(); 
        request.send( "addDocumentToDashboard.action?id=" + id );
    }
}

function toggleExternal()
{
    var external = getListValue( "external" );
    
    if ( external == "true" )
    {
        document.getElementById( "fileDiv" ).style.display = "none";
        document.getElementById( "urlDiv" ).style.display = "block";
    }
    else
    {
        document.getElementById( "fileDiv" ).style.display = "block";
        document.getElementById( "urlDiv" ).style.display = "none";
    }
}
