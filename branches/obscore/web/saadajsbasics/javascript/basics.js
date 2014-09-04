/***********************************************************************************************
 * Javascript classes common to all Web application supported by LM
 * These classes do not refer to any application-specific resource
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
 * - jquery.simplemodal
 * - jquery.prints
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
 * return the last node of file pathname
 */
if(!String.prototype.xtractFilename){
	String.prototype.xtractFilename = function(){
		var re = new RegExp(/(\w:)?([\/\\].*)*([\/\\](.*))/);
		var m = this.match(re);
		if( m == null ) {
			return {path: "", filename: this};
		}
		else {
			return {path: m[1], filename: m[m.length-1]};
		}
	} ;
}

/**
 * Quotes the element of a data treepath which cannot be understood by SQL
 * e.g. vizls.II/306/sdss8 => vizls."II/306/sdss8"
 */
if(!String.prototype.quotedTableName){
	String.prototype.quotedTableName = function() {		
		var results = this.split(".");
		for( var i=0 ; i<results.length ; i++) {
			if ( !results[i].match(/^[a-zA-Z0-9][a-zA-Z0-9_]*$/) ){
				results[i] = '"' + results[i] +'"';
			}
		}
		return results.join(".");
//		switch(results.length)  {}
//		case 1: return result;
//		case 2
//	
//		var regex = /([^.]*)\.(.*)/;
//		var results = regex.exec(this);
//		var table, schema;
//		if(!results){
//			table = tableName;
//			schema = "";
//		} else if( results.length == 2 ) {
//			table = results[1]; 
//			schema = "";
//		} else  {
//			table =  results[2];  
//			schema = results[1] + ".";
//		}
//		if( table.match(/^[a-zA-Z0-9][a-zA-Z0-9_]*$/ ) ){
//			return schema + table;
//		} else {
//			return schema + '"' + table +'"';
//		}
	}
}

/*****************************************************
 * Some global variables
 */

var rootUrl = "http://" + window.location.hostname +  (location.port?":"+location.port:"") + window.location.pathname;

/*
 * make sure that Modalinfo are on top of modals
 * and that Processing widows are on top of Modalinfo
 */
var zIndexModalinfo = 3000;
var zIndexProcessing = 4000;
/*
 * Types supported by the previewer based on iframes
 */
var previewTypes = ['text', 'ascii', 'html', 'gif', 'png', 'jpeg', 'jpg', 'pdf'];


/*****************************************************************************************************
 * Object managing printer features
 */
Printer = function() {
	/*
	 * Public functions
	 */
	var getPrintButton = function(divToPrint) {
		var retour =  "<a href='#' onclick='Printer.printDiv(\"" + divToPrint + "\");' class='printer'></a>";
		return retour;
	};
	var getSmallPrintButton = function(divToPrint) {
		var retour =  "<a href='#' onclick='Printer.printDiv(\"" + divToPrint + "\");' class='dlprinter'></a>";
		return retour;
	};
	var insertPrintButton = function(divToPrint, divHost) {
		$("#" + divHost).append(printer.getPrintButton(divToPrint));
	};
	var printDiv = function(divSelect) {
		var ele = $('#' + divSelect);
		if( !ele ) {
			Modalinfo.error("PRINT: the element " + divSelect +" doesn't exist");
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
	pblc.getSmallPrintButton  = getSmallPrintButton;
	pblc.insertPrintButton = insertPrintButton;
	pblc.printDiv = printDiv;
	return pblc;
}();

/*****************************************************************************************************
 * Object opening a modal window centered on the screen and not draggable.
 * The content of the box must often be set after the box is open in order to have it accessible in the DOM.
 * In this case the real size must be computed again after the content is totally set.
 * -open: open the modal box
 * -resize: resize it according its real content.
 */
Modalpanel = function() {
	var divId = 'modalpanediv';
	var divSelect = '#' + divId;
	var height = 0;
	var width = 0;
	/*
	 * Private methods
	 */
	var initDiv = function() {
		if( $(divSelect).length != 0){	 
			$(divSelect).remove();
		}		
		$(document.documentElement).append("<div id=" + divId + " style='overflow: auto;display: none; width: auto;height: auto;'></div>");
	}; 
	/*
	 * Public methods
	 */
	var open = function(innerDivHtml, modalParams) {
		Out.debug("Open Modal Box");
		initDiv();
		$(divSelect).html(innerDivHtml);
		/*
		 * Build the modal
		 */
		$(divSelect).modal(modalParams);	
		resize();
	};
	var resize = function(){
		var heighthMax = $(window).height()*0.9;
		var widthMax = $(window).width()*0.9;
		/*
		 * Take the size of the content
		 */
		height =  $(divSelect).height();
		width = $(divSelect).width();
		/*
		 * Limit the size of the container to the maximum size allowed
		 */
		if(height > heighthMax) {
			$(divSelect).css("height", heighthMax);
			height = heighthMax;
		}
		if(width > widthMax) {
			$(divSelect).css("width", widthMax);
			width = widthMax;
		}			
		Out.debug("Resize Modal Box to " +  (8+ width) + "px x " + (8+ height) + "px");
		/*
		 * Resize it to fits the size constraints
		 * Add 8px for the borders: avoids double scroll-bars
		 */
		var smc = $("#simplemodal-container");
		smc.css('height', (8+ height)); 
		smc.css('width', (8+ width)); 
		$(window).trigger('resize.simplemodal'); 
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.open = open;
	pblc.resize = resize;
	return pblc;

}();

/******************************************************************
 * This objet opens a modal panel just beneath the standard modal panel 
 * That way, it remains open when the Modalinfo panel closes
 */
Modalcommand = function() {
	var divId     = 'modalcommanddiv';
	var divSelect = '#' + divId;
	/*
	 * Privates functions
	 */
	var initDiv = function() {
		/*
		 * The div must be re-built at any time because 
		 * FF an Opera keep the wider size used before
		 */
		if( $(divSelect).length != 0){		
			$(divSelect).remove();
		}		
	$(document.documentElement).append("<div id=" + divId + " style='display: none; width: auto; hight: auto;'></div>");
	}; 
	/*
	 * Public functions
	 */
	var commandPanel = function (title, htmlContent, closeHandler) {
		initDiv();
		var chdl = ( closeHandler == null )? function(ev, ui)  {}: closeHandler;
		$(divSelect).html(htmlContent);
		$(divSelect).dialog({ modal: true
			    , resizable: false
			    , width: 'auto'
				, title: title 			                      
				, zIndex: (zIndexModalinfo -1)
				, close: chdl});
	};
	var setDivToggling = function(handler) {
		$(divSelect + " fieldset legend").click(function() {
		    $(this).parent().find("div").first().toggle(handler);		  
		});
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.commandPanel = commandPanel;
	pblc.setDivToggling = setDivToggling;
	return pblc;
}();

/*****************************************************************************************************
 * Object opening any types of dialog boxes
 */
Modalinfo = function() {
	var divId        = 'modalinfodiv';
	var divIdCnt =  divId + '_contnt';
	var divSelect = '#' + divId;
	/*
	 * Iframes are pushed on another div because they can not have interaction 
	 * with the current modal panel
	 */
	var iframeId     = 'iframeInfodiv';
	var iframeSelect = '#' + iframeId;
	/*
	 * Popup panel can be open from  dialog
	 */
	var popupId     = 'popupInfodiv';
	var popupSelect = '#' + popupId;
	/*
	 * Privates functions
	 */
	var initDiv = function() {
		/*
		 * The div must be re-built at any time because 
		 * FF an Opera keep the wider size used before
		 */
		if( $(divSelect).length != 0){		
			$(divSelect).remove();
		}		
	$(document.documentElement).append("<div id=" + divId + " style='display: none; width: auto; hight: auto;'></div>");
	}; 
	var initIframeDiv = function() {
		/*
		 * The div must be re-built at any time because 
		 * FF an Opera keep the wider size used before
		 */
		if( $(iframeSelect).length != 0){		
			$(iframeSelect).remove();
		}		
	$(document.documentElement).append("<div id=" + iframeId + " style='display: none; width: auto; hight: auto;'></div>");
	}; 
	var initPopupDiv = function() {
		/*
		 * The div must be re-built at any time because 
		 * FF an Opera keep the wider size used before
		 */
		if( $(popupSelect).length != 0){		
			$(popupSelect).remove();
		}		
	$(document.documentElement).append("<div id=" + popupId + " style='display: none; width: auto; hight: auto;'></div>");
	}; 
	var formatMessage = function(message) {
		var retour = "<span class=alert>" + message.replace(/\n/g, "<BR>") + "</span>";
		return retour;
	};
	var buildStandardPopup = function(logoClass, title, formatedMessage) {
		initPopupDiv();
		$(popupSelect).html("<div class=" + logoClass 
				+ "></div><div style='display: inline;'><span class=help>" 
				+ formatedMessage+ "</span></div>");
		$(popupSelect).dialog({  maxWidth: '50%'
			, title: title
			, resizable: false
			, modal: true
			, zIndex: zIndexModalinfo});

	};
	var getTitle = function (replacement, title, content){
		if( title == undefined ) {
			return replacement;
		} else {
			return title;
		}
	};

	/*
	 * Public functions
	 */
	var dataPanel = function (title, htmlContent, closeHandler, bgcolor) {
		initDiv();
		var chdl = ( closeHandler == null )? function(ev, ui)  {$(divSelect).html("");}: closeHandler;
		if( bgcolor != null ) {
			$(divSelect).css("background-color", bgcolor);
		}
		$(divSelect).html(htmlContent);
		$(divSelect).dialog({ modal: true
			   // , resizable: false
			    , width: 'auto'
				//, maxHeight: '1000px'    
				, dialogClass: 'd-maxsize'

				, title: title 		  
				, zIndex: zIndexModalinfo
				, close: chdl});
	};
	/**
	 * htmlCOntent is used to fill the div whereas url is called when the view s expended toanother tab
	 */
	var urlDataPanel = function (title, htmlContent, url, closeHandler, bgcolor) {
		initDiv();
		var chdl = ( closeHandler == null )? function(ev, ui)  {$(divSelect).html("");}: closeHandler;
		if( bgcolor != null ) {
			$(divSelect).css("background-color", bgcolor);
		}
		var stitle =  "<span>Preview of " 
			+ title 
			+ " <a class=dldownload href='#' onclick='Location.changeLocation(&quot;" 
			+ url + "&quot;, &quot;" + title 
			+ "&quot;);' title='Open in a new tab or download' style='display: inline-block;'></a></span>";
		$(divSelect).html(htmlContent);
		$(divSelect).dialog({ modal: true
			   // , resizable: false
			    , width: 'auto'
				//, maxHeight: '1000px'    
				, dialogClass: 'd-maxsize'

				, title: stitle 		  
				, zIndex: zIndexModalinfo
				, close: chdl});
	};
	
	var openIframePanel = function (url) {
		initIframeDiv();
		var fileName = url;
		var title =  "<span>Preview of " 
			+ fileName 
			+ " <a class=dldownload href='#' onclick='Location.changeLocation(&quot;" 
			+ url + "&quot;, &quot;" + fileName 
			+ "&quot;);' title='Open in a new tab or download' style='display: inline-block;'></a></span>";
		$(iframeSelect).html("<iframe style='width: 98%; height: 98%;'></iframe>");
		$(iframeSelect).dialog({ modal: true
			, resizable: true
			, width: ($(window).width()*0.9)
			, height: ($(window).height()*0.9)
			, title: title	                      
			, zIndex: zIndexModalinfo
			, close: function(){$(iframeSelect).html("");} // avoid the src to be called again
			});
		$(iframeSelect + " iframe").attr("src", url);
	};
	
	var iframePanel = function (url) {
		var that=this;
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
						that.openIframePanel(url);
						return;
					} 
				}
				Out.info("Mime type " + type + " not recognized; open a tab");
				Location.changeLocation(url, fileName);
			}
		});
	};			

	var info = function (content, title) {
		Out.info("Info Popup " + content);
		buildStandardPopup("infologo", getTitle("INFO", title), formatMessage(content));
	};
	var confirm = function(content, handler, title) {
		Out.info("Confirm Popup " + content);
		var hdl = (handler == undefined )? function(){alert("No attached Handler");}: handler;
		buildStandardPopup("confirmlogo",getTitle("CONFIRMATION", title), formatMessage(getTitle(content)));
		$(popupSelect).dialog( "option"
				, "buttons"
				, [ { text: "Cancel", click: function() { $( this ).dialog( "close" );} }
				, { text: "Ok"    , click: function() { $( this ).dialog( "close" ); hdl();} }]);
	};
	var error = function (content, title) {
		Out.info("Error Popup " + content);
		buildStandardPopup("warninglogo", getTitle("ERROR", title), formatMessage(content));
	};
	var getHtml = function() {
		return $(divSelect).html();
	};
	var center = function() {
		
		var parent = $(divSelect).parent();
		parent.css("position","absolute");
		parent.css("top", Math.max(0, (($(window).height() - parent.outerHeight()) / 2) + 
	                                                $(window).scrollTop()) + "px");
		parent.css("left", Math.max(0, (($(window).width() - parent.outerWidth()) / 2) + 
	                                                $(window).scrollLeft()) + "px");
	};
	var close = function() {
		$(divSelect).dialog("close");
	};

	var resize = function(){
		var heighthMax = $(window).height()*0.9;
		var widthMax = $(window).width()*0.9;
		var ctnt = $("#" + divIdCnt);
		/*
		 * Take the size of the content
		 */
		height =  ctnt.height();
		width = ctnt.width();
		/*
		 * Limit the size of the container to the maximum size allowed
		 */
		if(height > heighthMax) {
			ctnt.css("height", heighthMax);
			height = heighthMax;
		}
		if(width > widthMax) {
			ctnt.css("width", widthMax);
			width = widthMax;
		}			
		Out.debug("Resize Modal Box to " +  (8+ width) + "px x " + (8+ height) + "px");
		/*
		 * Resize it to fits the size constraints
		 * Add 8px for the borders: avoids double scroll-bars
		 */
		var smc = $("#modalinfodiv");
		smc.css('height', (8+ height)); 
		smc.css('width', (8+ width)); 
		$(window).trigger('resize.simplemodal'); 
	};

	/*
	 * exports
	 */
	var pblc = {};
	pblc.dataPanel = dataPanel;
	pblc.urlDataPanel = urlDataPanel;
	pblc.iframePanel = iframePanel;
	pblc.openIframePanel = openIframePanel;
	pblc.info    = info;
	pblc.confirm = confirm;
	pblc.error   = error;
	pblc.getHtml   = getHtml;
	pblc.center   = center;
	pblc.close   = close;
	pblc.resize   = resize;
	return pblc;
}();

/*****************************************************************************************************
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
		Out.debug("PROCESSSING (show and hide) " + message);
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
					+ '<div id="saadaworkingContent" style="position:absolute; top:50%;margin-top:-22px;'
					+ ' width: 300px;  margin-left: -150px; left: 50%; background-color: white; opacity: 1;z-index: ' 
					+ (zIndexProcessing+1) + ';'
					+ ' border:5px solid #DDD; border-radius: 5px"></div>');
		}
		$('#saadaworkingContent').html("<div class=progresslogo>" 
				+ "</div><div id=saadaworkingContentText class=help style='margin-top: 8px; display: inline; width: 240px; float:left; padding: 5px;font-size: small;'>" 
				+ message + "</div>");
		$('#saadaworking').css("display", "inline");
		$('#saadaworkingContent').css("display", "inline");
		openTime = new Date().getTime() ;
	};
	var hide = function () {
		Out.debug("close processing");
		var msg = $("#saadaworkingContentText").text();
		var seconds = new Date().getTime() ;
		/*
		 * Make sure the progress windows remains open at least 700ms: avoids blinking
		 */
		if( (seconds - openTime) < 700 ) {
			setTimeout('Processing.closeIfNoChange("' + msg + '" )', 700);
		} else    {
			$("#saadaworking").css("display", "none");$("#saadaworkingContent").css("display", "none");
		}
	};
	var closeIfNoChange = function(lastMsg){
		var currentMsg = $("#saadaworkingContentText").text();
		if( currentMsg == lastMsg) {
			$('#saadaworking').css("display", "none");
			$('#saadaworkingContent').css("display", "none");	
		} else {
			Out.debug("The content of the progress dialog has changed: not closing it");
		}
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.show   = show;
	pblc.hide   = hide;
	pblc.closeIfNoChange   = closeIfNoChange;
	pblc.jsonError   = jsonError;
	pblc.showAndHide = showAndHide;
	return pblc;
}();


/*****************************************************************************************************
 * Console functions:
 * -Msg(): print out a message with the caller stacl position
 * -Trace(): print out a message with the caller stack trace
 */
Out = function() {
	var debugMode = false;
	var trace = false;
	/*
	 * Privates functions
	 */
	var printMsg = function (level, msg, withTrace) {
		var e = new Error('dummy');	
		var stk;
        console.log(level + ": " + msg);
		/*
		 * IE ignore the stack property of the object Error
		 */
		if( (stk = e.stack) != null ) {
			var ls = stk.split("\n");
			/*
			 * Always display the 4th lines of the stack
			 * The 3rd is the current line : not relevant
			 * The 4th refers to the caller
			 */
			for( var i=3 ; i<ls.length ; i++ ) {
				//if( i == 3) continue;
				console.log(ls[i]);
				if( i > 3 && ! withTrace) break;
			}
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
	var setdebugModeFromUrl = function() {
		/*
		 * Set the debug mode from the debug parameters
		 */
		var debug  =  (RegExp('debug=' + '(.+?)(&|$)').exec(location.search)||[,null])[1];
		Out.debugModeOff;
		Out.traceModeOff;

		if( debug != null ) {
			if( debug == "on" ) {
				Out.info("Set debug on and trace off");
				Out.debugModeOn();
				Out.traceOff();
			} else if( debug == "withtrace" ) {
				Out.info("Set debug on and trace on");
				Out.debugModeOn();
				Out.traceOn();
			} else if( debug == "traceonly" ) {
				Out.info("Set debug off and trace on");
				Out.debugModeOff();
				Out.traceOn();
			} else {
				Modalinfo.info("debug parameter must be either on, withtrace or traceonly. It is ignored for this session.");
			}
		}
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
	pblc.setdebugModeFromUrl = setdebugModeFromUrl;
	return pblc;
}();

Processing.show("Load scripts");

/*****************************************************************************************************
 * Download class
 */
Location = function () {
	var that = this;
	var downloadIframe = null;
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
	var download = function (url){
		authOK = true;
		if( !url.startsWith("http")) {
			url = window.location.protocol + "//" + window.location.hostname +  (location.port?":"+location.port:"") + window.location.pathname + "/" + url; 
		}
		Out.info("Download " + url);
		if( downloadIframe == null ) {
			$(document.body).append('<iframe id="downloadIframe" src="' + url + '" style="display: hiddden;"></iframe>');
			this.downloadIframe =  $("#downloadIframe");
		} else {
			this.downloadIframe.attr("src", url);
		}
	};
	var confirmBeforeUnlaod = function() {
		Out.info("Prompt user before to leave");
		window.onbeforeunload = function() {
			if( !that.authOK) {
				if( WebSamp_mVc.fireIsConnected() ) {
					WebSamp_mVc.fireUnregister();
				}
				return  'WARNING: Reloading or leaving this page will lost the current session';
			} else {
				that.authOK = false;
			}
		};
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.download   = download;
	pblc.changeLocation   = changeLocation;
	pblc.confirmBeforeUnlaod   = confirmBeforeUnlaod;
	return pblc;
}();


