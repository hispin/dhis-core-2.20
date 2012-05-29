
$( document ).ready( function() {
	$( '.commentArea' ).autogrow();
} );

function showUserInfo( id )
{
	$( "#userInfo" ).load( "../dhis-web-commons-ajax-html/getUser.action?id=" + id );
	$( "#userInfo" ).dialog( {
		modal : true,
		width : 350,
		height : 350,
		title : "User"
	} );
}

function postComment( uid )
{	
	var text = $( "#commentArea" + uid ).val();
	
	var url = "../api/interpretations/" + uid + "/comment";
	
	var created = getCurrentDate();
	
	$.ajax( url, {
		type: "POST",
		contentType: "text/html",
		data: text,
		success: function() {			
			var template = 
				"<div><div class=\"interpretationName\"><span class=\"bold pointer\" " +
				"onclick=\"showUserInfo( \'${userId}\' )\">${userName}<\/span>&nbsp; " +
				"<span class=\"grey\">${created}<\/span><\/div><\/div>" +
				"<div class=\"interpretationText\">${text}<\/div>";
			
			$.tmpl( template, { "userId": currentUser.id, "userName": currentUser.name, created: created, text: text } ).appendTo( "#comments" + uid );
		}		
	} );
}