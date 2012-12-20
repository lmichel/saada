
/***********************************************************************************************
 * Javascript classes common to all Web application supported by LN
 * These classe do not refer to any application-specific resource
 * 
 * Content:
 * - String utilities (not a class but extensions of the String class )
 * - Printer   : printout any div
 * - Modalinfo : open any kind of dialog boxes
 * - Processing: Progress bar open while Ajax calls
 * - Out       : Print message in the console with or without stack trace
 * - Location  : to be called to process data downloading
 * 
 * Required external resources 
 * - jquery-ui
 * - jquery.alerts
 * - jquery.datatables
 * 
 * Laurent Michel 20/12/2012
 */

/**
 * Adding some useful String methods
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
			charac = this.charCodeAt(i);
			hash = 31*hash+charac;
			hash = hash & hash; 
		}
		return hash;
	};
};
if(!String.prototype.trim){
	String.prototype.trim = function(){
	} ;
};

/**
 * make sure that Modalinfo are on top of modals
 * and that Processing widows are on top of Modalinfo
 */
var zIndexModalinfo = 3000;
var zIndexProcessing = 4000;

/**
 * Types supported by the previewer based on iframes
 */
var previewTypes = ['text', 'html', 'votable', 'gif', 'png', 'jpeg', 'pdf', 'xml'];
/**
 * Object managing printer features
 */
Printer = function() {
	/*
	 * Public functions
	 */
	var getPrintButton = function(divToPrint) {
		var retour =  "<a href='#' onclick='printer.printDiv(\"" + divToPrint + "\");' class='printer'></a>";
		return retour;
	};
	var insertPrintButton = function(divToPrint, divHost) {
		$("#" + divHost).append(printer.getPrintButton(divToPrint));
	};
	var printDiv = function(divId) {
		var ele = $('#' + divId);
		if( !ele ) {
			Modalinfo.error("PRINT: the element " + divId +" doesn't exist");
		} else {
			Out.infoMsg(ele);
			ele.print();
		}		
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.getPrintButton  = getPrintButton;
	pblc.insertPrintButton = insertPrintButton;
	pblc.printDiv = printDiv;
	return pblc;
}();

/**
 * Object opening any types of dialog boxes
 */
Modalinfo = function() {
	/*
	 * Privates functions
	 */
	var initDiv = function() {
		if( $('#diagdiv').length == 0){		
			$(document.documentElement).append("<div id=diagdiv style='display: none; width: auto; hight: auto;'></div>");
		}		
	}; 
	var formatMessage = function(message) {
		var retour = "<span class=alert>" + message.replace(/\n/g, "<BR>") + "</span>";
		return retour;
	};
	var buildStandardPopup = function(logoClass, title, formatedMessage) {
		initDiv();
		$('#diagdiv').html("<div class=" + logoClass 
				+ "></div><div style='display: inline;'><span class=help>" 
				+ formatedMessage+ "</span></div>");
		$('#diagdiv').dialog({  maxWidth: '50%'
			, title: title
			, resizable: false
			,  modal: true
			, zIndex: zIndexModalinfo});

	};
	var getTitle = function (replacement, title, content){
		if( content == undefined ) {
			return replacement;
		} else {
			return title;
		}
	};
	/*
	 * Public functions
	 */
	var dataPanel = function (title, htmlContent, closeHandler) {
		initDiv();
		var chdl = ( closeHandler == null )? function(ev, ui)  {}: closeHandler;
		$('#diagdiv').html(htmlContent);
		$('#diagdiv').dialog({ modal: true
			, resizable: false
			, width: 'auto'
				, title: title 			                      
				, zIndex: zIndexModalinfo
				, close: chdl});
	};
	var iframePanel = function (url) {
		initDiv();
		/*
		 * try to get information about the data returned by the URL
		 */
		$.ajax({
			type: 'GET',
			async: false,
			url:url,
			complete: function (XMLHttpRequest, textStatus) {
				var fileName = url;
				var type = "undef";
				var headers = XMLHttpRequest.getAllResponseHeaders();
				var ls = headers.split(/\n/g);
				/*
				 * Extract both filename and content type from the http header
				 */
				for( var i=0 ; i<ls.length ; i++) {
					var l = ls[i];
					if( l.startsWith("Content-Disposition") ) {
						var res = /filename=(.*)/.exec(l);
						if( res ) fileName = res[1].replace(/\"/g, '');
					} else if( l.startsWith("Content-Type") ) {
						var res = /Content-Type:\s*(.*)/.exec(l);
						if( res ) type = res[1].replace(/\"/g, '');
					}
				}
				/*
				 * Open an iframe if the content type can be displayed.
				 * Let the browser do in any others cases
				 */
				for( var t=0 ; t<previewTypes.length ; t++ ) {
					if( type.match(previewTypes[t]) ) {
						var title =  "<span>Preview of " 
							+ fileName 
							+ " <a class=dldownload href='#' onclick='Location.changeLocation(&quot;" 
							+ url + "&quot;, &quot;" + fileName 
							+ "&quot;);' title='Open in a new tab or download' style='display: inline-block;'></a></span>";
						$('#diagdiv').html("<iframe src=" + url + " style='width: 98%; height: 98%;'></iframe>");
						$('#diagdiv').dialog({ modal: true
							, resizable: true
							, width: ($(window).width()*0.9)
							, height: ($(window).height()*0.9)
							, title: title	                      
							, zIndex: zIndexModalinfo});
						return;
					} 
				}
				Location.changeLocation(url, fileName);
			}
		});
	};			

	var info = function (title, content) {
		buildStandardPopup("infologo", getTitle("INFO", title, content), formatMessage(getTitle(title,content)));
	};
	var confirm = function(title, content) {
		buildStandardPopup("confirmlogo",getTitle("CONFIRMATION", title, content), formatMessage(getTitle(title,content)));
	};
	var error = function (title, content) {
		buildStandardPopup("warninglogo", getTitle("ERROR", title, content), formatMessage(getTitle(title,content)));
	};
	var simbad = function (pos) {
		Processing.show("Waiting on Simbad Response");
		$.getJSON("simbadtooltip", {pos: pos}, function(jsdata) {
			Processing.hide();
			if( Processing.jsonError(jsdata, "Simbad Tooltip Failure") ) {
				return;
			} else {
				var table = "";
				if( jsdata.aaData.length > 0 ) {
					table += ("<img src='http://alasky.u-strasbg.fr/cgi/simbad-thumbnails/get-thumbnail.py?name=" 
							+ encodeURIComponent((jsdata.aaData[0])[0]) + "'/>");
				} else {
					table +="<span class='help'>No vignette available</span>";
				}
				table += "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"simbadtable\" class=\"display\"></table>";
				Modalinfo.dataPanel("Simbad Summary for Position " 
						+ pos 
						+ "<a class=simbad target=blank href=\"http://simbad.u-strasbg.fr/simbad/sim-coo?Radius=1&Coord=" 
						+ encodeURIComponent(pos) + "\"></a>"
						, table);
				$('#simbadtable').dataTable({
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					"sDom": '<"#simbadtable_banner"pif>t',
					"bPaginate" : true,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : true,
					"bAutoWidth" : true,
					"bDestroy" : true
				});
				$("#simbadtable_banner").css("font-size","small");
				$("#simbadtable_info").css("width","30%");
			}
		});
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.dataPanel = dataPanel;
	pblc.iframePanel = iframePanel;
	pblc.info    = info;
	pblc.confirm = confirm;
	pblc.error   = error;
	pblc.simbad  = simbad;
	return pblc;
}();

/**
 * Object showing AJAX callback progress
 */
Processing  = function() {
	/*
	 * public functions
	 */
	var openTime = -1;	
	var jsonError = function (jsondata, msg) {
		if( jsondata == undefined || jsondata == null ) {
			Modalinfo.error("JSON ERROR: " + msg + ": no data returned" );
			return true;
		}
		else if( jsondata.errormsg != null) {
			Modalinfo.error("JSON ERROR: " + msg + ": "  + jsondata.errormsg);
			return true;
		}	
		return false;
	};
	var showAndHide = function(message){
		show(message);
		setTimeout('$("#saadaworking").css("display", "none");$("#saadaworkingContent").css("display", "none");', 500);		
	};
	var show = function (message) {
		Out.info("PROCESSSING " + message);
		stillToBeOpen = true;
		if( $('#saadaworking').length == 0){	
			$(document.body).append(
					'<div id="saadaworking" style="margin: auto;padding: 5px; display: none;z-index: ' + zIndexProcessing 
					+ ';opacity: 0.5;top: 0; right: 0; bottom: 0; left: 0;background-color: black;position: fixed;"></div>'
					+ '<div id="saadaworkingContent" style="position:absolute; top:50%;height: 45px; margin-top:-22px;'
					+ ' width: 300px;  margin-left: -150px; left: 50%; background-color: white; opacity: 1;z-index: ' 
					+ (zIndexProcessing+1) + ';'
					+ ' border:5px solid #DDD; border-radius: 5px"></div>');
		}
		$('#saadaworkingContent').html("<img style='padding: 5px; vertical-align:middle' src=images/ajax-loader.gif></img><span class=help>" 
				+ message + "</span>");
		$('#saadaworking').css("display", "inline");
		$('#saadaworkingContent').css("display", "inline");
		openTime = new Date().getTime() ;
	};
	var hide = function () {
		Out.debug("close processing");
		var seconds = new Date().getTime() ;
		/*
		 * Make sure the progress windows remains open at least 700ms: avoids blinking
		 */
		if( (seconds - openTime) < 700 ) {
			setTimeout('$("#saadaworking").css("display", "none");$("#saadaworkingContent").css("display", "none");', 700);
		} else    {
			$("#saadaworking").css("display", "none");$("#saadaworkingContent").css("display", "none");
		}
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.show   = show;
	pblc.hide   = hide;
	pblc.jsonError   = jsonError;
	pblc.showAndHide = showAndHide;
	return pblc;
}();

/**
 * Console functions:
 * *Msg(): print out a message with the caller stacl position
 * *Trace(): print out a message with the caller stack trace
 */
Out = function() {
	var debugMode = false;
	var trace = false;
	/*
	 * Privates functions
	 */
	var printMsg = function (level, msg, withTrace) {
		var e = new Error('dummy');	
		var ls = e.stack.split("\n");
		console.log(level + ": " + msg);
		for( var i=3 ; i<ls.length ; i++ ) {
			console.log(ls[i]);
			if( ! withTrace) break;
		}
	};
	/*
	 * Public functions
	 */
	var traceOn = function() {
		trace = true;
	};
	var traceOff = function() {
		trace = false;
	};
	var debugModeOn = function() {
		debugMode = true;
	};
	var debugModeOff = function() {
		debugMode = false;
	};
	var debugMsg = function (msg) {
		if( debugMode ) printMsg("DEBUG", msg, false);
	};
	var debugTrace = function (msg) {
		if( debugMode ) printMsg("DEBUG", msg, true);
	};
	var debug = function (msg) {
		if( debugMode ) printMsg("DEBUG", msg, trace);
	};
	var infoMsg = function infoMsg(msg) {
		printMsg(" INFO", msg, false);
	};
	var infoTrace  =function (msg) {
		printMsg(" INFO", msg, true);
	};
	var info  =function (msg) {
		printMsg(" INFO", msg, trace);
	};
	/*
	 * Exports
	 */
	var pblc = {};
	pblc.debugMsg     = debugMsg;
	pblc.debugTrace   = debugTrace;
	pblc.infoMsg      = infoMsg;
	pblc.infoTrace    = infoTrace;
	pblc.info         = info;
	pblc.debugModeOn  = debugModeOn;
	pblc.debugModeOff = debugModeOff;
	pblc.debug        = debug;
	pblc.traceOn      = traceOn;
	pblc.traceOff     = traceOff;
	return pblc;
}();

/**
 * Download class
 */
Location = function () {
	var authOK = false;
	var that = this;
	/*
	/*
	 * Public functions
	 */
	var changeLocation = function (url, title){
		Out.info("changeLocation to " + url);
		authOK = true;
		var t = ( title )? title: '_blank';
		window.open (url, t);
	};
	var confirmBeforeUnlaod = function() {
		Out.info("Prompt user before to leave");
		window.onbeforeunload = function() {
			if( !that.authOK) {
				if( webSampView.fireIsConnected() ) {
					webSampView.fireUnregister();
				}
				return  'WARNING: Reloading or leaving this page will lost the current session';
			}
			else {
				that.authOK = false;
			}
		};
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.changeLocation   = changeLocation;
	pblc.confirmBeforeUnlaod   = confirmBeforeUnlaod;
	return pblc;
}();


