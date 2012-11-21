/*
 * Some utilities
 */
if(!String.prototype.startsWith){
	String.prototype.startsWith = function (str) {
		return !this.indexOf(str);
	};
};
if(!String.prototype.endsWith){
	String.prototype.endsWith = function(suffix) {
		return (this.indexOf(suffix, this.length - suffix.length) !== -1);
	};
};

if(!String.prototype.hashCode){
	String.prototype.hashCode = function(){
		var hash = 0;
		if (this.length == 0) return code;
		for (var i= 0; i < this.length; i++) {
			var char = this.charCodeAt(i);
			hash = 31*hash+char;
			hash = hash & hash; 
		}
		return hash;
	};
};
if(!String.prototype.trim){
	String.prototype.trim = function(){
	} ;
};
var DEBUG = true;
function logMsg(message) {
	if( DEBUG && (typeof console != 'undefined') ) {
		console.log(message);
	}
}

function logged_alert(message, title) {
	logMsg("ALERT " + message);
	jAlert(message, title);
}

function setTitlePath(treepath) {
	globalTreePath = treepath;

	$('#titlepath').html('<i>');
	for( var i=0 ; i<treepath.length  ; i++ ) {
		if( i > 0 ) $('#titlepath').append('&gt;');
		$('#titlepath').append(treepath[i]);
	}
}

var globalTreePath = new Array();
function setTitlePath(treepath) {
	globalTreePath = treepath;

	$('#titlepath').html('<i>');
	for( var i=0 ; i<treepath.length  ; i++ ) {
		if( i > 0 ) $('#titlepath').append('&gt;');
		$('#titlepath').append(treepath[i]);
	}
}

function getTreePathAsKey() {
	var retour = '';
	for( var i=0 ; i<globalTreePath.length  ; i++ ) {
		if( i > 0 )retour += '_';
		retour += globalTreePath[i];
	}
	return retour;
}

/*
 * Using a Jquery bind() here has a strange behaviour...
 * http://stackoverflow.com/questions/4458630/unable-to-unbind-the-window-beforeunload-event-in-jquery
 */
window.onbeforeunload = function() {
	if( !authOK) {
		return  'WARNING: Reloading or leaving this page will lost the current session';
	}
	else {
		authOK = false;
	}
};
var authOK = false;
function changeLocation(url){
	logMsg("changeLocation " + url);
	authOK = true;
	window.open (url, "_blank");
}

var stillToBeOpen = false;
var simbadToBeOpen = false;

function showProcessingDialog() {
	stillToBeOpen = true;
	if( $('#saadaworking').length == 0){		
		$('#resultpane').append('<div id="saadaworking" class="dataTables_processing" style="visibility: hidden; "></div>');
	}
	$('#saadaworking').html("Waiting on Saada reply");
	/*
	 * It is better to immediately show the process dialog in order to give a feed back to the user
	 * It we dopn't, user could click several time on submit a get lost with what happens
	 *
	 * setTimeout("if( stillToBeOpen == true ) $('#saadaworking').css('visibility', 'visible');", 500);
	 */
	$('#saadaworking').css('visibility', 'visible');
}
function showProcessingDialogImmediately() {
	stillToBeOpen = true;
	if( $('#saadaworking').length == 0){		
		$('#resultpane').append('<div id="saadaworking" class="dataTables_processing" style="visibility: hidden; "></div>');
	}
	$('#saadaworking').html("Waiting on Saada reply");
	$('#saadaworking').css('visibility', 'visible');
}

function hideProcessingDialog() {
	stillToBeOpen = false;
	if( $('#saadaworking').length != 0){
		$('#saadaworking').css('visibility', 'hidden');	
	}
}

function showSampMessageSent() {
	stillToBeOpen = true;
	if( $('#saadaworking').length == 0){		
		$('#resultpane').append('<div id="saadaworking" class="dataTables_processing" style="visibility: hidden; "></div>');
	}
	$('#saadaworking').html("SAMP message sent");
	$('#saadaworking').css('visibility', 'visible');
	setTimeout(" $('#saadaworking').css('visibility', 'hidden');", 2000);

}

//function showQuerySent() {
//stillToBeOpen = true;
//if( $('#saadaworking').length == 0){		
//$('#resultpane').append('<div id="saadaworking" class="dataTables_processing" style="visibility: hidden; "></div>');
//}
//$('#saadaworking').html("Query submitted");
//$('#saadaworking').css('visibility', 'visible');
//setTimeout(" $('#saadaworking').css('visibility', 'hidden');", 2000);
//}


function openDialog(title, content) {
	if( $('#diagdiv').length == 0){		
		$(document.documentElement).append("<div id=diagdiv style='width: 99%; display: none; width: auto; hight: auto;'></div>");
	}
	$('#diagdiv').html(content);
	$('#diagdiv').dialog({  width: 'auto', title: title});
}

function openSimbadDialog(pos) {
	if( $('#diagdiv').length == 0){		
		$(document.documentElement).append("<div id=diagdiv style='display: none; width: auto; hight: auto;'></div>");
	}
	var table = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"simbadtable\" class=\"display\"></table>";
	$('#diagdiv').html(table);

	$.getJSON("simbadtooltip", {pos: pos}, function(jsdata) {
		hideProcessingDialog();
		if( processJsonError(jsdata, "Simbad Tooltip Failure") ) {
			return;
		}
		else {
			$('#simbadtable').dataTable({
				"aoColumns" : jsdata.aoColumns,
				"aaData" : jsdata.aaData,
				"sDom" : 'rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true,
				"bAutoWidth" : true,
				"bDestroy" : true
			});
			if( jsdata.aaData.length > 0 ) {
				logMsg((jsdata.aaData[0])[0]);
				logMsg(encodeURIComponent((jsdata.aaData[0])[0]));
				$('#simbadtable').append("<img src='http://alasky.u-strasbg.fr/cgi/simbad-thumbnails/get-thumbnail.py?name=" 
						+ encodeURIComponent((jsdata.aaData[0])[0]) + "'/>");
			}

			var simbadpage = "<a class=simbad target=blank href=\"http://simbad.u-strasbg.fr/simbad/sim-coo?Radius=1&Coord=" + encodeURIComponent(pos) + "\"></a>";
			$('#diagdiv').dialog({  width: 'auto', title: "Simbad Summary for Position " + pos + simbadpage });
			sampView.firePointatSky(pos);
		}
	});
}

function openModal(title, content) {
	if( $('#detaildiv').length == 0){		
		$(document.documentElement).append("<div id=detaildiv style='width: 99%; display: none;'></div>");
	}
	$('#detaildiv').html(content);
	$('#detaildiv').modal();
}

function switchArrow(id) {
	var image = $('#'+id+'').find('img').attr('src');
	if (image == 'images/tdown.png') {
		$('#'+id+'').find('img').attr('src', 'images/tright.png');
	} else if (image == 'images/tright.png') {
		$('#'+id+'').find('img').attr('src', 'images/tdown.png');
	}
}

function processJsonError(jsondata, msg) {
	if( jsondata == undefined || jsondata == null ) {
		alert("JSON ERROR: " + msg + ": no data returned" );
		return true;
	}
	else if( jsondata.errormsg != null) {
		alert("JSON ERROR: " + msg + ": "  + jsondata.errormsg);
		return true;
	}	
	return false;

}