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
 * A dummy function handling the Web access log
 * This function must be override in public applications
 */
RecordAction = function(action){

}
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

//Gets an url parameter by name
var getUrlParameter = function getUrlParameter(sParam) {
	var sPageURL = decodeURIComponent(window.location.search.substring(1)),
	sURLVariables = sPageURL.split('&'),
	sParameterName,
	i;

	for (i = 0; i < sURLVariables.length; i++) {
		sParameterName = sURLVariables[i].split('=');

		if (sParameterName[0] === sParam) {
			return sParameterName[1] === undefined ? true : sParameterName[1];
		}
	}
};

function isNumber(val) {
	var exp = new RegExp("^[+-]?[0-9]*[.]?[0-9]*([eE][+-]?[0-9]+)?$","m"); 
	return exp.test(val);
}

var decimaleRegexp = new RegExp("^[+-]?[0-9]*[.][0-9]*([eE][+-]?[0-9]+)?$","m");
var bibcodeRegexp  = new RegExp(/^[12][089]\d{2}[A-Za-z][A-Za-z0-9&][A-Za-z0-9&.]{2}[A-Za-z0-9.][0-9.][0-9.BCRU][0-9.]{2}[A-Za-z0-9.][0-9.]{4}[A-Z:.]$/);

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
 * e.g. 
 * test:
 * 
  alert(
	    'vizls.II/306/sdss8'.quotedTableName() + '\n' +
	    'vizls.II/306/sdss8.OBS'.quotedTableName() + '\n' +
	    'viz2.J/other/KFNT/15.483/jovsat'.quotedTableName() + '\n' +
	    'viz2.J/other/KFNT/15.483/jovsat.OBS'.quotedTableName()+ '\n' +
	    'viz2."J/other/KFNT/15.483/jovsat.OBS"'.quotedTableName()+ '\n' +
	    '"viz2"."J/other/KFNT/15.483/jovsat.OBS"'.quotedTableName()+ '\n' +
	    'J/other/KFNT/15.483/jovsat.OBS'.quotedTableName()+ '\n' +
	    'ivoa.obcore.s_ra'.quotedTableName()

);
 * Return an object {qualifiedName, tableName}
 */

if (!String.prototype.quotedTableName) {
	String.prototype.quotedTableName = function () {
		/*
		 * Node without schema (astrogrid) may have en empty schema name 
		 */
		var thisValue;
		if( this.startsWith(".") ) {
			thisValue = this.substring(1);
		} else {
			thisValue = this;
		}
		/*
		 * already quoted, nothing to do
		 */
		if( thisValue.indexOf("\"") >= 0  ){
			return {qualifiedName: thisValue, tableName: thisValue};
		}
		var results = thisValue.split(".");
		var tbl = new Array();
		/*
		 * One element: take it as as whole
		 */
		if( results.length == 1 ) {
			tbl.push(thisValue);
		} 
		/*
		 * a.b could be either schema.table or just a table name including a dot.
		 */
		else if( results.length == 2 ) {
			/*
			 * If the dot is followed by number, it cannot be a separator since a table name cannot start with number
			 */
			if (results[1].match(/^[0-9].*/)) {
				tbl.push(thisValue);
			}
			/*
			 * Otherwise there is no way to determine the matter, but we can suppose that we are dealing with Vizier
			 * So, if both path elements contain a / we are having to do with a simple table name
			 */
			else if( !results[0].match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
				tbl.push(thisValue);
			} else {
				tbl.push(results[0]);
				tbl.push(results[1]);				
			}
			/*
			 * In this case, we have to know if the first element is a schema or the first part of a table name
			 * We suppose that schemas have regular names 
			 */
		} else if (results.length > 2 ) {
			/*
			 * Gamble on a schema name 
			 */
			if(results[0].match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
				tbl.push(results[0]);
				tbl.push(results[1]);
				var last = results[results.length -1];
				/*
				 * The last one is certainly a field name
				 */
				if( last.match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
					for (var i = 2; i < (results.length -1); i++) {
						tbl[tbl.length - 1] += "." +results[i];
					}	
					tbl.push(last);
				} else {
					for (var i = 2; i < results.length; i++) {
						tbl[tbl.length - 1] += "." +results[i];
					}
				}
			} else {
				tbl.push(thisValue);
			}
		}  
		for (var j = 0; j < tbl.length; j++) {
			if (!tbl[j].match(/^[a-zA-Z0-9][a-zA-Z0-9_]*$/)) {
				tbl[j] = '"' + tbl[j] + '"';
			}
		}		
		return {qualifiedName: tbl.join("."), tableName: tbl[tbl.length - 1]};
	};
}
/**
 * Quotes the element of a data treepath which cannot be understood by SQL
 * e.g. No longer used
 * test:
 * 
   alert(
	    JSON.stringify('vizls.II/306/sdss8'.getTreepath()) + '\n' +
	    JSON.stringify('vizls.II/306/sdss8.OBS'.getTreepath()) + '\n' +
	    JSON.stringify('viz2.J/other/KFNT/15.483/jovsat'.getTreepath()) + '\n' +
	    JSON.stringify('viz2.J/other/KFNT/15.483/jovsat.OBS'.getTreepath())+ '\n' +
	    JSON.stringify('viz2."J/other/KFNT/15.483/jovsat.OBS"'.getTreepath())+ '\n' +
	    JSON.stringify('"viz2"."J/other/KFNT/15.483/jovsat.OBS"'.getTreepath())+ '\n' +
	    JSON.stringify( 'J/other/KFNT/15.483/jovsat.OBS'.getTreepath())+ '\n' +
	    JSON.stringify('ivoa.obcore.s_ra'.getTreepath())
        );
);
 */
if (!String.prototype.getTreepath) {
	String.prototype.getTreepath = function () {
		var retour = {
				schema: ''
					, tableorg: this.valueOf()
					, table: ''};
		var results = this.split(".");
		var tbl = new Array();
		/*
		 * One element: assumed to a table
		 */
		if( results.length == 1 ) {
			retour.table = this.valueOf();			
		}

		/*
		 * a.b could be either schema.table or just a table name including a dot.
		 */
		else if( results.length == 2 ) {
			/*
			 * If the dot is followed by number, it cannot be a separator since a table name cannot start with number
			 */
			if (results[1].match(/^[0-9].*/)) {
				retour.table = this.valueOf();
			}
			/*
			 * Otherwise there is no way to determine the matter, but we can suppose that we are dealing with Vizier
			 * So, if both path elements contain a / we are having to do with a simple table name
			 */
			else if( !results[0].match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
				retour.table = this.valueOf();
			} else {
				retour.schema = results[0];
				retour.table = results[1];
			}
			/*
			 * In this case, we have to know if the first element is a schema or the first part of a table name
			 * We suppose that schemas have regular names 
			 */
		} else if (results.length > 2 ) {
			/*
			 * Gamble on a schema name 
			 */
			if(results[0].match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
				retour.schema = results[0];
				retour.table = results[1];
				var last = results[results.length -1];
				/*
				 * The last one is certainly a field name
				 */
				if( last.match(/^[a-zA-Z_][a-zA-Z0-9_]*$/) ) {
					for (var i = 2; i < (results.length -1); i++) {
						retour.table += "." +results[i];
					}	
				} else {
					for (var i = 2; i < results.length; i++) {
						retour.table += "." +results[i];
					}
				}
			} else {
				retour.table = this.valueOf();
			}
		}
		return retour;
	};
}

/**
 * Basic sky geometry functions
 */
SkyGeometry = function() {
	/**
	 * 
	 */
	var toRadians = function(alpha){
		return alpha*Math.PI/180;
	}
	/**
	 * 
	 */
	var toDegrees = function(alpha){
		return alpha*180/Math.PI;
	}
	/**
	 * 
	 */
	var  distanceDegrees = function(ra0, de0, ra1, de1){
		var  rra0 = toRadians(ra0);
		var  rra1 = toRadians(ra1);
		var  rde0 = toRadians(de0);
		var  rde1 = toRadians(de1);
		return toDegrees(Math.acos((Math.sin(rde0)*Math.sin(rde1)) +
				(Math.cos(rde0)*Math.cos(rde1) * Math.cos(rra0-rra1))));
	}
	/*
	 * exports
	 */
	var pblc = {};
	pblc.toRadians = toRadians;
	pblc.toDegrees = toDegrees;
	pblc.distanceDegrees = distanceDegrees;
	return pblc;

}();

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
var imageTypes = ['gif', 'png', 'jpeg', 'jpg'];

/**
 * @returns {___anonymous_Modalinfo}
 */
Modalinfo = 
	function(){
	var divId = "modaldiv";
	var divSelect = '#' + divId;
	/**
	 * Resources used by 
	 */
	var aladin = null;
	var divAladinContainer = "aladin-lite-container";
	var divAladin = "aladin-lite-catdiv";
	var divInfoAladin = "aladin-lite-catdiv-info";

	var getTitle = function (replacement, title){
		if( title == undefined ) {
			return replacement;
		} else {
			return title;
		}
	};

	var formatMessage = function(message) {
		var retour = "<p>" + message.replace(/\n/g, "<BR>") + "</p>";
		return retour;
	};

	/**
	 * Return the content of the object x as a user readable HTML string
	 */
	var dump = function (x, indent) {
		var indent = indent || '';
		var s = '';
		if (Array.isArray(x)) {
			s += '[';
			for (var i=0; i<x.length; i++) {
				s += dump(x[i], indent);
				//if (i < x.length-1) s += indent +', ';
			}
			s +=indent + ']';
		} else if (x === null) {
			s = 'NULL';
		} else switch(typeof x) {
		case 'undefined':
			s += 'UNDEFINED';
			break;
		case 'object':
			//s += "{ ";
			var first = true;
			for (var p in x) {
				if (!first) {
					if( p != "id" && p != "$" ) s += indent;
					else s += " ";
				} else s += "\n" + indent;
				/*if( p != "id" && p != "$" )*/ s += '<b>'+ p + '</b>: ';
				s += dump(x[p], indent + "  ");
//				s += "\n";
				if( p != "id" && p != "$" ) s += "\n";
//				else s += " " ;
				first = false;
			}
			//s += indent +'}';
			break;
		case 'boolean':
			s += (x) ? 'TRUE' : 'FALSE';
			break;
		case 'number':
			s += x;
			break;
		case 'string':
			if( x.lastIndexOf("http", 0) === 0 ) 
				x = decodeURIComponent(x);
			if( x.match(/\s/))
				s += '"' + x + '"';
			else 
				s += x;
			break;
		case 'function':
			s += '<FUNCTION>';
			break;
		default:
			s += x;
		break;
		}
//		s = s.replace(/{/g,'');
//		s = s.replace(/}/g,'');
		return s;
	};

	/**
	 * Return the content of the object x as a user readable ASCII string
	 */
	var dumpAscii = function (x, indent) {
		var indent = indent || '';
		var s = '';
		if (Array.isArray(x)) {
			s += '[';
			for (var i=0; i<x.length; i++) {
				s += dump(x[i], indent);
				//if (i < x.length-1) s += indent +', ';
			}
			s +=indent + ']';
		} else if (x === null) {
			s = 'NULL';
		} else switch(typeof x) {
		case 'undefined':
			s += 'UNDEFINED';
			break;
		case 'object':
			//s += "{ ";
			var first = true;
			for (var p in x) {
				if (!first) {
					if( p != "id" && p != "$" ) s += indent;
					else s += " ";
				} else s += "\n" + indent;
				/*if( p != "id" && p != "$" )*/ s +=  p + ': ';
				s += dump(x[p], indent + "  ");
//				s += "\n";
				if( p != "id" && p != "$" ) s += "\n";
//				else s += " " ;
				first = false;
			}
			//s += indent +'}';
			break;
		case 'boolean':
			s += (x) ? 'TRUE' : 'FALSE';
			break;
		case 'number':
			s += x;
			break;
		case 'string':
			if( x.lastIndexOf("http", 0) === 0 ) 
				x = decodeURIComponent(x);
			if( x.match(/\s/))
				s += '"' + x + '"';
			else 
				s += x;
			break;
		case 'function':
			s += '<FUNCTION>';
			break;
		default:
			s += x;
		break;
		}
//		s = s.replace(/{/g,'');
//		s = s.replace(/}/g,'');
		return s;
	};

	// Permits to generate id for the various dialogs
	var last_id = 0;

	var nextId = function(){
		last_id++;
		return "modal-"+last_id;
	};

	/**
	 * Set the black shadow for a dialog
	 * @id: id of the dialog
	 */
	var setShadow = function(id){
		var z_modal = $("#"+id).parent(".ui-dialog").zIndex();
		if($("#shadow").length == 0) {
			$('body').append('<div id="shadow" pos="'+id+'"></div>');
			$('#shadow').zIndex(z_modal-1);
		}
		else {
			$('body').append('<div class="shadow" pos="'+id+'"></div>');
			$('div[pos="'+id+'"]').zIndex(z_modal-1);
		}
	};

	/**
	 * Create the dialog
	 * @id: id of the dialog
	 * @resizable: boolean, tell if the dialog can be resizable
	 * @title: string, title of the dialog
	 * @content: string, content of the dialog
	 * @min_size: integer, set a minimal size for the dialog if defined
	 */
	var setModal = function(id, resizable, title, content, min_size){
		if (content == undefined) {
			$('body').append("<div id='"+id+"' title='" + title + "' class='custom-modal'> </div>");
		}
		else {
			$('body').append("<div id='"+id+"' title='" + title + "' class='custom-modal'>" + content + "</div>");
		}

		if (resizable){
			$("#"+id).dialog();
		}
		else {
			if (min_size != undefined) {
				$("#"+id).dialog({
					resizable: false,
					minWidth: min_size,
					position: { my: "center", at: "top", of: window }
				});
			}
			else {
				$("#"+id).dialog({
					resizable: false,
					width: "auto",
					height: "auto"
				});
			}
		}
	};

	// Return the id of the last modal in the page
	var findLastModal = function(){
		var id_last_modal = -1;
		$("div[id^='modal-']").each(function() {
			var tmp = $(this).attr('id').substring(6);
			if (tmp > id_last_modal && isNumber(tmp)){
				id_last_modal = $(this).attr('id').substring(6);
			}
		});
		if (id_last_modal != -1) {
			return "modal-"+id_last_modal;
		} else {
			id_last_modal = undefined;
			return id_last_modal;
		}
	};

	/**
	 * Remove the shadow of a dialog
	 * @id: id of the dialog
	 */
	var removeShadow = function(id){
		$('div[pos="'+id+'"]').remove();
	};

	/**
	 * Remove the dialog
	 * @id: id of the dialog
	 */
	var removeModal = function(id){
		$("#"+id).remove();
	};

	/**
	 * When dialog is closed, remove it and its shadow and buttons
	 * @id: id of the dialog
	 */
	var close = function(id){
		if (id != undefined) {
			removeShadow(id);
			removeModal(id);
			removeBtn(id);
		} else {
			Modalinfo.close(Modalinfo.findLastModal());
		}

	};

	/**
	 * Remove the div of buttons with the class btndialog
	 * @id: id of the dialog
	 */
	var removeBtn = function(id){
		$('div[btndialog="'+id+'"]').remove();
	};

	/**
	 * Permits to call the function close when we click on the shadow of a dialog or click on its cross
	 * @id: id of the dialog
	 */
	var whenClosed = function(id){
		$('div[pos="'+id+'"]').click(function() {
			close(id);
		});
		$("#"+id).prev("div").find("a.ui-dialog-titlebar-close.ui-corner-all").click(function() {
			close(id);
		});
	};

	// Permits to close a dialog when we press escape
	$(document).keydown(function(e) {
		if (e.keyCode == 27) {
			if($("#shadow").length != 0) {
				close(findLastModal());
			}
		}
	});

	/**
	 * Add the div of buttons with the class btndialog
	 * @id: id of the dialog
	 */
	var addBtnDialog = function(id) {
		$("#"+id).append('<div class="btn-dialog" btndialog="'+id+'"></div>');
	};

	/**
	 * Add html before the title, used for add glyphicon
	 * @id: id of the dialog
	 * @icon: html content
	 */
	var addIconTitle = function(id, icon) {
		$("#"+id).prev("div").find("span").prepend(icon);
	};

	/**
	 * Add img before the title with a predefined size
	 * @id: id of the dialog
	 * @img: url of the img
	 * @url: link to follow when img is clicked
	 */
	var addImgIconTitle = function(id, img, url) {
		$("#"+id).prev("div").find("span").prepend(' <a href="'+url+'" target="_blank"><img src="'+img+'" alt="Img" class="img-title"></a>');
	};

	/**
	 * Add img after the title with a predefined class
	 * @id: id of the dialog
	 * @class_img: class to display the img
	 * @url: link to follow when img is clicked
	 */
	var addImgLinkTitle = function(id, class_img, url) {
		$("#"+id).prev("div").find("span").append(' <a href="'+url+'" target="_blank" class="'+class_img+'"></a>');
	};

	/**
	 * Add img before the title with personalized size
	 * @id: id of the dialog
	 * @class_img: class to display the img
	 * @url: link to follow when img is clicked
	 */
	var addLogoTitle = function(id, class_img, url) {
		$("#"+id).prev("div").find("span").prepend('<a href="'+url+'" target="_blank" class="'+class_img+'"></a>');
	};

	/**
	 * Add a button "Ok" with a handler in the buttons div
	 * @id: id of the dialog
	 * @handler: handler of the button
	 */
	var addBtnOk = function(id, handler) {
		if (handler == undefined ) {
			var hdl = function(){
				alert("No attached Handler");
			}
		}
		else {
			var hdl = handler;
		}
		$('div[btndialog="'+id+'"]').append(
				$('<a class="btn btn-sm btn-default">Ok</a>').click(function() {
					close(id);
					hdl();
				})
		);
	};

	/**
	 * Add a button "Cancel" which close the dialog in the buttons div
	 * @id: id of the dialog
	 */
	var addBtnCancel = function(id) {
		$('div[btndialog="'+id+'"]').append(
				$('<a class="btn btn-sm btn-warning">Cancel</a>').click(function() {
					close(id);
				})
		);
	};

	/**
	 * Create an info dialog
	 * @content: string, content of the dialog
	 * @title: string, title of the dialog
	 */
	var info = function(content, title) {
		var id_modal = nextId();
		setModal(id_modal, false, getTitle("Information", title), formatMessage(content));
		//addIconTitle(id_modal, '<span class="glyphicon glyphicon-info-sign"></span>');
		setShadow(id_modal);
		whenClosed(id_modal);
	};

	/**
	 * Create an info object dialog
	 * @content: string, content of the dialog
	 * @title: string, title of the dialog
	 */
	var infoObject = function (object, title) {
		var id_modal = nextId();
		setModal(id_modal, false, getTitle("INFO", title), '<pre>' + dump(object, '  ').replace(/[\n]+/g, "<br />") + '</pre>');
		addIconTitle(id_modal, '<span class="glyphicon glyphicon-info-sign"></span>');
		setShadow(id_modal);
		whenClosed(id_modal);
	};

	/**
	 * Create a confirm dialog with buttons ok and cancel
	 * @content: string, content of the dialog
	 * @title: string, title of the dialog
	 */
	var confirm = function (content, handler, title) {
		var id_modal = nextId();
		setModal(id_modal, false, getTitle("Confirmation", title), formatMessage(content));
		addIconTitle(id_modal, '<span class="glyphicon glyphicon-ok-sign"></span>');
		addBtnDialog(id_modal);
		addBtnOk(id_modal, handler);
		addBtnCancel(id_modal);
		setShadow(id_modal);
		whenClosed(id_modal);
	};

	/**
	 * Create an error dialog
	 * @content: string, content of the dialog
	 * @title: string, title of the dialog
	 */
	var error = function(content, title) {
		var id_modal = nextId();
		Out.infoTrace(content);
		if( jQuery.isPlainObject({}) ) {
			setModal(id_modal, false, getTitle("Error", title), dump(content, '&nbsp;&nbsp;').replace(/\n[\n\s]*/g, "<br />"));
		} else {
			setModal(id_modal, false, getTitle("Error", title), formatMessage(content));
		}
		addIconTitle(id_modal, '<span class="glyphicon glyphicon-remove-sign"></span>');
		setShadow(id_modal);
		whenClosed(id_modal);
	};

	/**
	 * Create an upload dialog
	 * Files can be added with the normal way and with drag & drop 
	 * @title: string, title of the dialog
	 * @url: url of the form
	 * @description: string, description of the form
	 * @handler: action to do if file upload success
	 * @beforehandler: action to do before submit the form
	 * @extraParamers: table, hidden input to add to the form
	 */
	var uploadForm = function (title, url, description, handler, beforeHandler, extraParamers) {
		var id_modal = nextId();
		var htmlForm = '<form id="upload-form" class="form-horizontal" target="_sblank" action="' + url + '" method="post"'
		+  'enctype="multipart/form-data">';
		if( extraParamers != null) {
			for( var i=0 ; i<extraParamers.length ; i++ ) 
				htmlForm += "<input type='hidden'  name='" + extraParamers[i].name + "'  value='" + extraParamers[i].value + "'>";
		}
		htmlForm += '<div class="align-center">'
			+'<input class="stdinput custom-file" id="uploadPanel_filename" type="file" name="file"/>'
			+ '<p class="overflow info-upload"></p>'
			+ '<p id="upload_status"></p>'
			+ '<p class="form-description"></p>'
			+ '</div>'
			+ '<p id="infos"></p>'
			+ '<div class="align-center">'
			+ '<input disabled type="submit" value="Upload" class="custom-submit"/>'
			+ '</div>'
			+ '</form>';
		setModal(id_modal, false, title, htmlForm);
		addIconTitle(id_modal,'<span class="glyphicon glyphicon-file"></span>');
		setShadow(id_modal);
		whenClosed(id_modal);
		// Permits drag & drop
		$("#"+id_modal).find(".custom-file").on("dragover drop", function(e) {
			e.preventDefault();
		}).on("drop", function(e) {
			$("#"+id_modal).find(".custom-file")
			.prop("files", e.originalEvent.dataTransfer.files)
			.closest("form");
			$("input").prop('disabled', false);
		});

		$("#"+id_modal).find(".custom-file").change(function() {            
			$("#"+id_modal).find(".custom-file").fadeTo('slow', 0.3, function(){}).delay(800).fadeTo('slow', 1);
			$("input").prop('disabled', false);
			var filename = this.value.xtractFilename().filename;
			$("#"+id_modal).find(".info-upload").text(filename);     
		});


		if( description != null ) {
			$("#"+id_modal).find(".form-description").html(description);
			//$('#upload-form .form-description').html(description);
		}
		$("#"+id_modal).find('#upload-form').ajaxForm({
			beforeSubmit: function() {
				if(beforeHandler != null ) {
					beforeHandler();
				}
			},
			success: function(e) {
				if( Processing.jsonError(e, "Upload Position List Failure") ) {
					close(id_modal);
					return;
				} else {
					$("#upload_status").html("Uploaded");
					$("#upload_status").css("color","green");
					var retour = {retour: e, path : $("#"+id_modal).find('#uploadPanel_filename').val().xtractFilename()};

					if( handler != null) {
						handler(retour);
					}
					// If no handler, displays infos in the dialog
					else {
						var display_retour = dump(retour).replace(/\n/g, "<br />");
						display_retour = display_retour.replace(/^<br\s*\/?>|<br\s*\/?>$/g,'');
						$("#infos").html(display_retour);	
					}
				}
			}
		});
	};


	/**
	 * Create an Iframe dialog
	 * @id: id of the dialog
	 * @url: url of the content we want to display
	 */
	var setIframePanel = function (id, url, title) {
		if (title != undefined) {
			$('body').append("<div id='"+id+"' title='" + title + "' class='custom-modal'> </div>");
		} else {
			$('body').append("<div id='"+id+"' title='Preview of " + url + "' class='custom-modal'> </div>");
		}
		$("#"+id).dialog({
			resizable: false
		});

		$("#"+id).append('<iframe src="'+url+'" iframeid="'+id+'">Waiting on server response...</iframe>');		

		$("#"+id).dialog( "option", "height", $(window).height());
		$("#"+id).dialog( "option", "width", "80%");	
		$("#"+id).dialog( "option", "position", { my: "center", at: "center", of: window } );	
	};
	/**
	 * Create an  dialog showing a local
	 * @id: id of the dialog
	 * @url: url of the content we want to display
	 */
	var setURLPanel = function (id, url, title) {
		if (title != undefined) {
			$('body').append("<div id='"+id+"' title='" + title + "' class='custom-modal'> </div>");
		} else {
			$('body').append("<div id='"+id+"' title='Preview of " + url + "' class='custom-modal'> </div>");
		}
		$("#"+id).dialog({
			resizable: false
		});

		$("#"+id).append('<div id="'+id+'">Waiting on server response...</iframe>');		

//		$("#"+id).dialog( "option", "height", $(window).height());
		$("#"+id).dialog( "option", "width", "80%");	
		$("#"+id).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		$("#"+id).load(url);
	};

	/**
	 * Used to display an img in the iframe dialog if the iframe content is an img
	 * @id: id of the dialog
	 * @url: url of the img we want to display
	 */
	var setImagePanel = function (id, url, title) {
		if (title != undefined) {
			$('body').append("<div id='"+id+"' title='" + title + "' class='custom-modal img-panel'> </div>");
		} else {
			$('body').append("<div id='"+id+"' title='Preview of " + url + "' class='custom-modal img-panel'> </div>");
		}

		$("#"+id).dialog({
			resizable: false
		});

		$("#"+id).append('<img imgpanelid="'+id+'" src="'+url+'"\>');

		$('img[imgpanelid="'+id+'"]').load(function(){
			setSize(id);
		});

	};

	// Used to adjust the size of the dialog with the image's size
	var setSize = function(id) {
		var h = $("#"+id).prop("scrollHeight");
		var w = $("#"+id).prop("scrollWidth");
		var width = w+30;
		var height = h+60;
		$("#"+id).dialog( "option", "height", height);
		$("#"+id).dialog( "option", "width", width);
		$("#"+id).dialog( "option", "position", { my: "center", at: "center", of: window } );	
	};

	/**
	 * Test if an url comes from the same domain
	 * @url: url we want to test
	 */
	function testSameOrigin(url) {
		var loc = window.location;
		var a = document.createElement('a');

		a.href = url;

		return a.hostname == loc.hostname &&
		a.port == loc.port &&
		a.protocol == loc.protocol;
	};

	/**
	 * You cannot catch errors that occur in an iframe with a different origin. 
	 * Those errors are occurring in a different context which is not your parent page.
	 */

	/**
	 * Create an iframe dialog
	 * @url: string, the url content we want to show
	 * @img: boolean, tell if the content is an img
	 */
	var openIframePanel = function (content, img) {
		var id_modal = nextId();

		if (content.url != undefined) {
			var url = content.url;
			var title = content.title;
		} else {
			var url = content;
			var title = undefined;
		}

		/*
		 * Open an iframe with an adpated size if img is defined
		 */
		if (img != undefined && img == true) {
			setImagePanel(id_modal, url, title);
		}
		else {
			setIframePanel(id_modal, url, title);
		}
		addImgLinkTitle(id_modal, 'floppy', url);
		$("#"+id_modal).prev("div").find("span").find(".img-title").click(function() {
			PageLocation.changeLocation(url);
		});

		setShadow(id_modal);
		whenClosed(id_modal);
	};

	/**
	 * Create an iframe dialog if the url comes from the same domain
	 * Otherwise, open a new page
	 * @url: string, the url content we want to show
	 * @img: boolean, tell if the content is an img
	 */
	var openIframeCrossDomainPanel = function(content, img) {
		var id_modal = nextId();

		if (content.url != undefined) {
			var url = content.url;
			var title = content.title;
		} else {
			var url = content;
			var title = undefined;
		}

		if (testSameOrigin(url)) {
			/*
			 * Open an iframe with an adpated size if img is defined
			 */
			if (img != undefined && img == true) {
				setImagePanel(id_modal, url, title);
			}
			else {
				//setIframePanel(id_modal, url, title);
				setURLPanel(id_modal, url, title)
			}
			addImgLinkTitle(id_modal, 'floppy', url);
			$("#"+id_modal).prev("div").find("span").find(".img-title").click(function() {
				PageLocation.changeLocation(url);
			});
			setShadow(id_modal);
			whenClosed(id_modal);	
		} else {
			PageLocation.changeLocation(url);
		}
	};


	// Create a simbad dialog
	var simbad = function (pos) {
		Processing.show("Waiting on Simbad Response");
		$.getJSON("simbadtooltip", {pos: pos}, function(jsdata) {
			Processing.hide();
			if( Processing.jsonError(jsdata, "Simbad Tooltip Failure") ) {
				return;
			} else {
				var table = "";
				table += '<table cellpadding="0" cellspacing="0" border="0"  id="simbadtable" class="display table"></table>';
				var id_modal = nextId();
				//setModal(id_modal, false, getTitle("Confirmation", title), formatMessage(content));
				setModal(id_modal, false, "Simbad Summary for Position " 
						+ pos 
						+ "<a class=simbad target=blank href=\"http://simbad.u-strasbg.fr/simbad/sim-coo?Radius=1&Coord=" 
						+ encodeURIComponent(pos) + "\"></a>"
						, table, 1000);
				setShadow(id_modal);
				whenClosed(id_modal);

				$("#"+id_modal).css("overflow","hidden");

				var options = {
						"aoColumns" : jsdata.aoColumns,
						"aaData" : jsdata.aaData,
						"bPaginate" : true,
						"sPaginationType": "full_numbers",
						"aaSorting" : [],
						"bSort" : false,
						"bFilter" : true,
						"bAutoWidth" : true,
						"bDestroy" : true
				};

				var img;

				if( jsdata.aaData.length > 0 ) {
					img = '<img src="http://alasky.u-strasbg.fr/cgi/simbad-thumbnails/get-thumbnail.py?name=' 
						+ encodeURIComponent((jsdata.aaData[0])[0]) + '"/>';
				} else {		var divAladin = "aladin-lite-catdiv";
				var divInfoAladin = "aladin-lite-catdiv-info";

				img = '<span class="help">No vignette available</span>';
				}

				var position = [
				                { "name": img,
				                	"pos": "top-left"
				                },
				                { "name": "filter",
				                	"pos": "top-right"
				                },
				                { "name": 'information',
				                	"pos" : "bottom-left"
				                },
				                { "name": "pagination",
				                	"pos" : "bottom-center"
				                },
				                { "name": " ",
				                	"pos" : "bottom-right"
				                }
				                ];

				CustomDataTable.create("simbadtable", options, position);

				// Put the filter just above the table
				$("#"+id_modal).find(".dataTables_filter").css("margin-top","34%");
				$("#"+id_modal).dialog( "option", "position", { my: "center", at: "center", of: window } );
			}
		});
	};

	this.regionEditor = null;

	// Create a region dialog
	region = function (handler, points) {
		var id_modal = nextId();
		$(document.documentElement).append('<div id="'+id_modal+'" class="aladin-lite-div" style="width: 400px; height: 400px"></div>');
		this.regionEditor = new RegionEditor_mVc  (id_modal, handler, points); 
		this.regionEditor.init();
		$('#'+id_modal).dialog({ width: 'auto'
			, dialogClass: 'd-maxsize'
				, title: "Sky Region Editor (beta)" 		  
					, zIndex: zIndexModalinfo
		});
		setShadow(id_modal);
		whenClosed(id_modal);
		/*
		 * For the Aladin command panel to be on the top layer: so it is enable to get all events
		 */
		$(".aladin-box").css("z-index", (9999));
		this.regionEditor.setInitialValue(points);
	}

	// Close the region dialog
	var closeRegion  = function (){
		$('div[pos="'+$('.aladin-lite-div').attr("id")+'"]').remove();
		$('.aladin-lite-div').remove();
	}

	// Used by the stc region dialog to create it
	var commandPanelAsync = function (title, htmlContent, openHandler, closeHandler) {
		var id_modal = nextId();
		$('body').append("<div id='"+id_modal+"' class='aladin-lite-stcdiv'></div>");
		var chdl = ( closeHandler == null )? function(ev, ui)  {}: closeHandler;
		var ohdl = ( openHandler == null )? function(ev, ui)  {}: openHandler;
		$("#"+id_modal).html(htmlContent);
		$("#"+id_modal).dialog({resizable: false
			, width: 'auto'
				, title: title
				, close: chdl
				, open: ohdl
		});
		setShadow(id_modal);
		whenClosed(id_modal);
	};


	// Class for the datapanel
	var divClass        = 'modalinfodiv';
	var divSelect = '.' + divClass;

	// @@@@@@@@@@@@@@@@@@
	var dataURLPanel = function (title, url) {		
		if($(divSelect).length != 0){
			$(divSelect).html('');
			$(divSelect).load(url);


			var chdl =  function(ev, ui)  {$(divSelect).html("");};
			$(divSelect).on( "dialogclose", chdl);
			$('div[pos="'+$(divSelect).attr("id")+'"]').on("click", chdl);

			var ii = $(divSelect).attr("id");
			var last = findLastModal();
			$(document).on("keydown", function(e) { 
				if (e.keyCode == 27) { 
					if (last == ii) {
						chdl();
					}
				} 
			});
		} else {
			// Permits to the dialog to be in foreground
			var new_zindex = 9999;
			if ($(".modalresult").length != 0) {
				new_zindex = $(".modalresult").zIndex() + 10;
			}
			var id_modal = nextId();
			$(document.documentElement).append('<div id="'+id_modal+'" class="'+divClass+'" style="display: none; width: auto; hight: auto;"></div>');

			var chdl = function(ev, ui)  {$("#"+id_modal).html("");};
			$("#"+id_modal).load(url);
			$("#"+id_modal).dialog({ width: 'auto'
				, dialogClass: 'd-maxsize'
					, title: title
					, fluid: true
					, close: chdl
					, resizable: false});


			// Adjust the size of the panel to be responsive
			if ($("#"+id_modal).find("h4").find("#detailhisto").length) {
				if ($(window).width() >= 1000) {
					$("#"+id_modal).dialog( "option", "width", 1000 );
					center();
				} else {
					fluidDialog();
				}
			}

			$("#"+id_modal).zIndex(new_zindex);	
			$('div[pos="'+$(divSelect).attr("id")+'"]').on("click", chdl);

			var ii = $(divSelect).attr("id");
			var last = findLastModal();
			$(document).on("keydown", function(e) { 
				if (e.keyCode == 27) {
					if (last == ii) {
						chdl();
					}
				} 
			});
			setShadow(id_modal);
			whenClosed(id_modal);
		}
	};

	// Create a dialog which can display html and have personalized handler on close
	var dataPanel = function (title, htmlContent, closeHandler, bgcolor) {		
		if($(divSelect).length != 0){
			$(divSelect).html('');
			$(divSelect).html(htmlContent);

			$(divSelect).css("background-color", bgcolor);

			var chdl = ( closeHandler == null )? function(ev, ui)  {$(divSelect).html("");}: closeHandler;
			$(divSelect).on( "dialogclose", chdl);
			$('div[pos="'+$(divSelect).attr("id")+'"]').on("click", chdl);

			var ii = $(divSelect).attr("id");
			var last = findLastModal();
			$(document).on("keydown", function(e) { 
				if (e.keyCode == 27) { 
					if (last == ii) {
						chdl();
					}
				} 
			});
		}
		else {
			// Permits to the dialog to be in foreground
			var new_zindex = 9999;
			if ($(".modalresult").length != 0) {
				new_zindex = $(".modalresult").zIndex() + 10;
			}
			var id_modal = nextId();
			$(document.documentElement).append('<div id="'+id_modal+'" class="'+divClass+'" style="display: none; width: auto; hight: auto;"></div>');

			var chdl = ( closeHandler == null )? function(ev, ui)  {$("#"+id_modal).html("");}: closeHandler;
			if( bgcolor != null ) {
				$("#"+id_modal).css("background-color", bgcolor);
			}
			$("#"+id_modal).html(htmlContent);
			$("#"+id_modal).dialog({ width: 'auto'
				, dialogClass: 'd-maxsize'
					, title: title
					, fluid: true
					, close: chdl
					, resizable: false});


			// Adjust the size of the panel to be responsive
			if ($("#"+id_modal).find("h4").find("#detailhisto").length) {
				if ($(window).width() >= 1000) {
					$("#"+id_modal).dialog( "option", "width", 1000 );
					center();
				}
				else {
					fluidDialog();
				}
			}

			$("#"+id_modal).zIndex(new_zindex);	
			$('div[pos="'+$(divSelect).attr("id")+'"]').on("click", chdl);

			var ii = $(divSelect).attr("id");
			var last = findLastModal();
			$(document).on("keydown", function(e) { 
				if (e.keyCode == 27) {
					if (last == ii) {
						chdl();
					}
				} 
			});
			setShadow(id_modal);
			whenClosed(id_modal);
		}
	};

	var closeDataPanel = function() {
		close($(divSelect).attr("id"));
	};

	/**
	 * These next functions are used to make a panel responsive
	 **/

	// Run function on all dialog opens
	$(document).on("dialogopen", ".ui-dialog", function (event, ui) {
		fluidDialog();
	});

	// Remove window resize namespace
	$(document).on("dialogclose", ".ui-dialog", function (event, ui) {
		$(window).off("resize.responsive");
	});

	// Manage the responsive side of some dialogs
	var fluidDialog = function fluidDialog() {
		var $visible = $(".ui-dialog:visible");
		// each open dialog
		$visible.each(function () {
			var $this = $(this);
			var dialog = $this.find(".ui-dialog-content").data("dialog");
			// if fluid option == true
			if (dialog.options.maxWidth && dialog.options.width) {
				// fix maxWidth bug
				$this.css("max-width", dialog.options.maxWidth);
				//reposition dialog
				dialog.option("position", dialog.options.position);
			}

			if (dialog.options.fluid) {
				// namespace window resize
				$(window).on("resize.responsive", function () {
					var wWidth = $(window).width();
					// check window width against dialog width
					if (wWidth < dialog.options.maxWidth + 50) {
						// keep dialog from filling entire screen
						$this.css("width", "90%");
					}
					//reposition dialog
					dialog.option("position", dialog.options.position);
				});
			}

		});
	}

	var getHtml = function() {
		return $(divSelect).html();
	};

	// Puts the datapanel in the center of the window
	var center = function() {
		var parent = $(divSelect).parent();
		parent.css("position","absolute");
		parent.css("top", Math.max(0, (($(window).height() - parent.outerHeight()) / 2) + 
				$(window).scrollTop()) + "px");
		parent.css("left", Math.max(0, (($(window).width() - parent.outerWidth()) / 2) + 
				$(window).scrollLeft()) + "px");
	};

	var pblc = {};
	pblc.dump = dump;
	pblc.dumpAscii = dumpAscii;
	pblc.nextId = nextId;
	pblc.findLastModal = findLastModal;
	pblc.setShadow = setShadow;
	pblc.whenClosed = whenClosed;
	pblc.setModal = setModal;
	pblc.close = close;
	pblc.info = info;
	pblc.infoObject = infoObject;
	pblc.confirm = confirm;
	pblc.error = error;
	pblc.uploadForm = uploadForm;
	pblc.openIframePanel = openIframePanel;
	pblc.openIframeCrossDomainPanel = openIframeCrossDomainPanel;
	pblc.iframePanel = openIframePanel;
	pblc.simbad = simbad;
	pblc.region = region;
	pblc.closeRegion = closeRegion;
	pblc.dataPanel = dataPanel;
	pblc.closeDataPanel = closeDataPanel;
	pblc.fluidDialog = fluidDialog;
	pblc.getHtml = getHtml;
	pblc.center = center;
	pblc.addIconTitle=addIconTitle
	return pblc;

}();

/**************************************************************************************************
 * Open Aladin inj a Dialog
 * Designed to avoid as much as possible having multiple running instances of Aladin
 * All widget work with the same ID 
 * TODO: connect with the region editor
 * @returns {___anonymous_ModalAladin}
 */
/**
 * @returns {___anonymous_ModalAladin}
 */
/**
 * @returns {___anonymous_ModalAladin}
 */
ModalAladin = function(){
	/**
	 * Resources used by 
	 */
	var aladin = null;
	var divAladinContainer = "aladin-lite-container";
	var divAladin          = "aladin-lite-catdiv";
	var divInfoAladin      = "aladin-lite-catdiv-info";
	var divInfoSider       = "aladin-lite-sider";
	var jqAladinContainer;
	var jqAladin;
	var divInfoAladin;
	var colorMap = new Object();
	colorMap["Vizier"] = {bg: "green", fg : "white"};
	colorMap["Simbad"] = {bg:"blue", fg : "white"};
	colorMap["NED"]    = {bg:"orange", fg : "black"};
	colorMap["target"] = {bg:"red", fg : "white"};
	colorMap["service_0"] = {bg:"#00ffff", fg : "black"};// Aqua 
	colorMap["service_1"] = {bg:"#66ff33", fg : "black"}; // vert pomme
	colorMap["service_2"] = {bg:"#ff9966", fg : "black"}; //salmon
	colorMap["service_3"] = {bg:"yellow", fg : "black"}; 
	var initialTarget;
	var initialFoV;

	/**
	 * Starts Aladin in an hidden panel
	 */
	var init = function (){
		if( aladin == null ){
			/*
			 * Attach the panel components to the body.
			 */
			$('body').append("<div id=" + divAladinContainer + " style='visibility: hidden;'>"
					+ "<div  id=" + divInfoSider + " style='display: block;float: left;width: 200px; height: 100%; padding-right: 5px;'></div>"
					+ "<div style='position: relative; display: block;float:left;width: 400px'>"
					//    + "   <div id='" + divInfoAladin + "'  readonly style='background-color: red; ; height: 100px; width:400px;' >Click to get source info</div>"
					+ "   <div id='" + divAladin + "' style='width: 400px; height: 400px'></div>"
					+ "   <div id='itemList' style='background-color: #f2f2f2; border-radius: 5px; padding: 5px; margin: 5px; display: none; overflow:auto;position: absolute; left: 0px; top: 0px; width: 390px; height: 90%;'></div>"
					+ "</div></div>");
			
			
			/*
			 * Starts Aladin without target or survey to make sure it can immedialtely handle API callbacks
			 */
			aladin = A.aladin('#' + divAladin , {
				showLayersControl : true,
				showGotoControl: true,
				showZoomControl: true,
				showFullscreenControl: false,
				showReticle: true,
				showFrame: true,
				cooFrame : "ICRS"}
			);		
			jqAladinContainer = $("#" + divAladinContainer);
			jqAladin          = $("#" + jqAladin);
			jqInfoAladin      = $("#" + divInfoAladin);
		}
	};
	/**
	 * Hide info panel on the top o AL
	 */
	var hideInfo = function() {
		jqInfoAladin.text("").attr("rows", 1).css("visibility", "hidden");
	}
	/**
	 * Show info panel on the top o AL
	 */
	var showInfo = function() {
		jqInfoAladin.text("Click to get source info").attr("rows", 6).css("visibility", "visible");
	}
	/**
	 * 
	 */
	var hideSidePanel = function(){
		$("#" + divInfoSider).css("visibility", "hidden");
	}
	/**
	 * 
	 */
	var showSidePanel = function(){
		$("#" + divInfoSider).css("visibility", "visible");
	}

	/**
	 * Display the message into the info panel.
	 * Mesage can be either an objct (then dumped) or an atomic value
	 */
	var displayInfo = function(message){
		if( message instanceof Object ) {
			jqInfoAladin.text(Modalinfo.dumpAscii(message, '   '));
		} else {
			jqInfoAladin.text(message);
		}
	}
	/**
	 * Set the black shadow for a dialog
	 * The same is used by modalinfo
	 * @id: id of the dialog
	 */
	var setShadow = function(id){
		var z_modal = $("#"+id).parent(".ui-dialog").zIndex();
		if($("#shadow").length == 0) {
			$('body').append('<div id="shadow" pos="'+id+'"></div>');
			$('#shadow').zIndex(z_modal-1);
		}
		else {
			$('body').append('<div class="shadow" pos="'+id+'"></div>');
			$('div[pos="'+id+'"]').zIndex(z_modal-1);
		}
		jqShadow = $('div[pos="'+divAladinContainer+'"]')

	};

	/**
	 * Make te dialog component no longer visible
	 */
	var closeHandler = function() {
		/*
		 * Class all Aladin popup 
		 */
		$('#' + divAladin + ' .aladin-popup-container').hide();
		/*
		 * Make sure Aladin won't be displayed on the page bottom
		 */
		jqAladinContainer.css("visibility", "hidden")
		jqAladinContainer.dialog('destroy');
		jqShadow.remove();
	}
	/**
	 * Open the dialog windows with Aladin and setup the close prcoedure 
	 */
	var buildAladinDialog = function (title){ 
		ModalAladin.init();	
		aladin.removeLayers();
		var theA=$("#" + divAladinContainer);
		jqAladinContainer.css("visibility", "visible")
		jqAladinContainer.dialog();
		jqAladinContainer.dialog({resizable: false
			, width: 'auto'
				, title: title
				, close: function(event,obj){
					closeHandler();
				}
		});
		setShadow(divAladinContainer);
		jqShadow.click(function() {
			closeHandler();
		});
		jqAladinContainer.prev("div").find("a.ui-dialog-titlebar-close.ui-corner-all").click(function() {
			closeHandler();
		});
	};

	/**
	 * Open a modal panel with AL. The image is overlayed with a catalogue given either as an URL or as an array of points
	 * A handler can, be given to process mousover events
	 * data:
	 * {
	 * label: ....
	 * target:
	 * fov: 
	 * url: ....
	 * (or) points : [{ra, dec, name, description}..]
	 * }
	 */
	var showSourcesInAladinLite = function (data) {	
		buildAladinDialog(data.label);
		showInfo();
		hideSidePanel();
		console.log(aladin.view.imageSurvey);
		if( aladin.view.imageSurvey == null ||  aladin.view.imageSurvey.id == null ) {
			aladin.setImageSurvey('P/XMM/PN/color');
			aladin.gotoObject(data.target);
			aladin.setZoom(data.fov);
		}
		var objClicked;
		if( data.url ) {
			var cat = A.catalogFromURL(data.url, {sourceSize:8, color: 'red', onClick:"showTable"});
			aladin.addCatalog(cat);
			/*
			aladin.on('objectClicked', function(object) {
				var msg;
				if( objClicked ) {
					objClicked.deselect();
				}
				if (object) {
					objClicked = object;
					object.select();
					msg = "Position: " + object.ra + ', ' + object.dec +  Modalinfo.dumpAscii(object.data, '  ');
				} else {
					msg = "Click to get source info";
				}
				$('#' + divInfoAladin).text(msg);
			});
			 */
		} else if( data.points ) {
			var cat = A.catalog({name: 'Source List', sourceSize: 8});
			aladin.addCatalog(cat);
			for( var i=0 ; i<data.points.length ; i++ ){
				var point =  data.points[i];
				cat.addSources([A.marker(point.ra, point.dec, {popupTitle: point.name, popupDesc: point.description})]);
			}
		}
	};


	/** 
	 * Open aladin lite and marks the position given in params
	 * A popup is displayed on the Aladin Screen when th mark is clicked
	 * params:{
	 * ra 
	 * dec 
	 * name 
	 * description }
	 * */
	var showSourceInAladinLite = function (params) {
		showSourcesInAladinLite({target: params.ra + " " + params.dec
			, label: params.name
			, fov: ((params.fov)? patrams.fov: 0.5)
			, points: [{ra: params.ra, dec: params.dec, name: params.name, description: params.description}]});
		hideInfo();
		hideSidePanel();
	}

	/**	
	 * Get a list of points through and AJAX call and display it in Aladin  Lite
	 * data:
	 * {
	 * label: ... panel titke
	 * target: .. central position (can be a name)
	 * fov: .. fov size in deg
	 * url: .. must return an array point points like;  [{ra, dec, name, description}..]
	 * }
	 */
	var showAsyncSourcesInAladinLite = function (data) {				
		Processing.show("Fetching data");
		$.getJSON(data.url, function(jsondata) {
			Processing.hide();
			if( Processing.jsonError(jsondata, data.url) ) {
				return;
			} else {
				showSourcesInAladinLite({
					label: data.label,
					target: data.target,
					fov: data.fov,
					points : jsondata
				})
			}
		});
	}

	var center = function() {
		aladin.setZoom(initialFov);
		aladin.gotoObject(initialTarget);
	}
	/**
	 * type json: {"dec":-12.18401,"name":"2XMM J181905.3-121102","description":"[0002A] NASA\/IPAC Extragalactic Database","ra":274.7721},
	 */
	var aladinExplorer = function(position, services) {
		ModalAladin.init();	
		showSidePanel();
		aladin.removeLayers();
		var theA=$("#" + divAladinContainer);
		var theS=$("#" + divInfoSider);
		theS.html("");
		jqAladinContainer.css("visibility", "visible")
		jqAladinContainer.dialog();
		jqAladinContainer.dialog({
			resizable: false
			, width: 'auto'
				, title: ((position.title)? position.title: 'Aladin Explorer')
				, close: function(event,obj){
					closeHandler();
				}
		});
		aladin.setImageSurvey('http://alasky.u-strasbg.fr/2MASS/J');
		aladin.gotoObject(position.target);
		aladin.setZoom(position.fov);
		initialTarget = position.target;
		initialFov = position.fov;
		var RaDec = aladin.getRaDec();
		var that = this;
		theS.html("");
		/*
		 * Insert target anchor
		 */
		theS.append("<p id='service_target' title='Show&center/hide the target' class='datahelp' style='cursor: pointer;'>Target</p>");
		/*
		 * Insert user services
		 */
		for( var s=0 ; s<services.length; s++){
			var id = "service_" + s;
			theS.append("<p id='" + id + "' title='Show/hide data overlay' class='datahelp' style='cursor: pointer;'>" + services[s].name + "</p>");
			services[s].color = colorMap['service_' + s];
			$("#" + id).data(services[s]);
		}
		/*
		 * Activate user services
		 */
		$("#" + divInfoSider + " p").click(function(){
			var caller = $(this);
			var data = $(this).data(); 
			if( caller.attr("class") == "selecteddatahelp"){
				caller.css("color", "");
				caller.css("background-color", "white");
				caller.attr("class", "datahelp");
				for( var c=0 ; c<aladin.view.catalogs.length ; c++) {
					// a surveiller console.log(c + " " + aladin.view.catalogs[c].name + " "  + data.name)
					if( aladin.view.catalogs[c].name == "Target" || aladin.view.catalogs[c].name.startsWith(data.name))  {
						aladin.view.catalogs.splice(c, 1);
						aladin.view.mustClearCatalog = true;
						aladin.view.requestRedraw(); 
						break;
					}
				}
				return;
			} else {
				var color = (caller.attr("id") == 'service_target')? colorMap['target']: data.color;
				caller.attr("class", "selecteddatahelp");
				caller.css("color", color.fg);
				caller.css("background-color", color.bg);

				if( caller.attr("id") == 'service_target'){
					caller.css("color", color.fg);
					caller.css("background-color", color.bg);
					var cat = A.catalog({name: "Target"});
					aladin.gotoObject(initialTarget);
					//aladin.setZoom(position.fov);
					aladin.addCatalog(cat);
					cat.addSources([A.marker(RaDec[0], RaDec[1], {popupTitle: position.title, /*popupDesc: ''*/})]);
				} else	if( data.type == 'votable') {
					var cat = A.catalogFromURL(data.url, {name: data.name, sourceSize:8, shape: 'plus', color: data.color.bg, onClick:"showTable"});
					aladin.addCatalog(cat);
				} else {
					$.getJSON(data.url, function(jsondata) {
						Processing.hide();
						if( Processing.jsonError(jsondata, data.url) ) {
							return;
						} else {
							var objClicked;
							var cat = A.catalog({name: data.name, sourceSize: 8, color: data.color.bg, shape: 'plus', onClick:"showTable"});
							aladin.addCatalog(cat);
							for( var i=0 ; i<jsondata.length ; i++ ){
								var point =  jsondata[i];
								cat.addSources([A.source(point.ra, point.dec, {ra: Numbers.toSexagesimal(point.ra/15, 7, false), dec:  Numbers.toSexagesimal(point.dec, 7, true), Name: point.name, Description: point.description})]);
							}
						}
					});
				}
			}
		});
		/*
		 * Adding simbad selector
		 */
		var s=services.length;
		id = "service_simbad";
		theS.append("<hr><p id='" + id + "' title='Show/hide Simbad sources' class='datahelp' style='cursor: pointer;' "
				+ "onclick='ModalAladin.displaySimbadCatalog();'>Simbad</p>");
		$("#" + id).data({name: 'Simbad'});
		/*
		 * Adding NED selector
		 */
		s++;
		id = "service_ned";
		theS.append("<p id='" + id + "' title='Show/hide NED sources' class='datahelp' style='cursor: pointer;' "
				+ "onclick='ModalAladin.displayNedCatalog();'>NED</p>");
		$("#" + id).data({name: 'NED'});
		/*
		 * Adding Vizier selector
		 */
		s++;
		id = "service_vizier";
		theS.append("<p id='" + id + "' title='Show/hide selected catalog sources' class='datahelp' style='cursor: pointer;' onclick='ModalAladin.toggleSelectedVizierCatalog();'>Vizier...</p><div id='AladinHipsCatalogs'></div>");
		$("#" + id).data({name: 'VizieR'});
		var he = new HipsExplorer_mVc({
			aladinInstance: aladin,
			parentDivId: "AladinHipsCatalogs", 
			formName   : "AladinHipsCatalogsExplorer", 
			target     : {ra: RaDec[0], dec : RaDec[1], fov: position.fov},
			productType: "catalog",
			handler    : function(jsondata){
				var itemList = $("#itemList");
				if( itemList.css("display") == "none"){
					itemList.css("display", "block");
					itemList.css("z-index", "10000");
				}
				itemList.html("<span class=strong>" + jsondata.length + " matching Catalogues</span>"
						+ '<a href="#" onclick="$(&quot;#itemList&quot;).css(&quot;display&quot;, &quot;none&quot;);" style="top: 18px;" class="ui-dialog-titlebar-close ui-corner-all" role="button"><span class="ui-icon ui-icon-closethick">close</span></a><br>');
				for(var i=0 ; i<jsondata.length ; i++){
					itemList.append("<br><span class=datahelp style='cursor: pointer' "
							+ "onclick='ModalAladin.displaySelectedVizierCatalog(&quot;" + jsondata[i].obs_id + "&quot);'>"  
							+ jsondata[i].obs_title + "</span><br>"
							+ "<span class=blackhelp>"
							+ jsondata[i]. obs_regime + "</span><br>"
							+ "<span class=blackhelp>"
							+ jsondata[i].obs_description + "</span>");
				}
			}
		});
		he.draw();
		/*
		 * Adding Hips survey selector
		 */
		id = "service_aladin";
		theS.append("<hr><p id='" + id + "' title='Change image survey' class='datahelp' style='cursor: pointer;'>Images...</p><div id='AladinHipsImages'></div>");
		var he = new HipsExplorer_mVc({
			aladinInstance: aladin,
			parentDivId: "AladinHipsImages", 
			formName   : "AladinHipsImagesExplorer", 
			target     : {ra: RaDec[0], dec : RaDec[1], fov: position.fov},
			productType: "image",
			handler    : function(jsondata){
				var itemList = $("#itemList");
				if( itemList.css("display") == "none"){
					itemList.css("display", "block");
					itemList.css("z-index", "10000");
				}
				itemList.html("<span class=strong>" + jsondata.length + " matching Hips images</span>\n"
				+ '<a href="#" onclick="$(&quot;#itemList&quot;).css(&quot;display&quot;, &quot;none&quot;);" style="top: 18px;" class="ui-dialog-titlebar-close ui-corner-all" role="button"><span class="ui-icon ui-icon-closethick">close</span></a><br>');
				for(var i=0 ; i<jsondata.length ; i++){
					itemList.append("<br><span class=datahelp style='cursor: pointer' onclick='ModalAladin.displaySelectedHips(&quot;" + jsondata[i].obs_title 
							+ "&quot;, &quot;" + jsondata[i].hips_service_url
							+ "&quot;, &quot;" + jsondata[i].hips_frame
							+ "&quot;, &quot;" + jsondata[i].hips_order
							+ "&quot;, &quot;" + jsondata[i].hips_tile_format
							+ "&quot;)'>"  + jsondata[i].obs_title + "</span><br>"
							+ "<span class=blackhelp>"
							+ jsondata[i].publisher_did + "</span><br>"
							+ "<span class=blackhelp>"
							+ jsondata[i]. obs_regime + "</span><br>"
							+ "<span class=blackhelp>"
							+ jsondata[i].obs_description + "</span>");
				}
			}
		});
		he.draw();
		/*
		 * Popup setup
		 */
		setShadow(divAladinContainer);
		jqShadow.click(function() {
			closeHandler();
		});
		jqAladinContainer.prev("div").find("a.ui-dialog-titlebar-close.ui-corner-all").click(function() {
			closeHandler();
		});
		/*
		 * Display DSS color by default
		 */
		this.displaySelectedHips("DSS2 optical HEALPix survey, color (R=red[~0.6um]/G=average/B=blue[~0.4um])"
				, "http://alasky.u-strasbg.fr/DSS/DSSColor"
				, "equatorial"
				, 9
				, "jpeg");
				
	}
	/**
	 * 
	 */
	var displaySelectedHips = function (obs_title, hips_service_url, hips_frame, moc_order, hips_tile_format) {	
		var cmdNode = $("#service_aladin");
		$("#itemList").css("display", "none");
		cmdNode.html(obs_title);
		var fmt = "";
		console.log(hips_tile_format);
		if(hips_tile_format.indexOf("png") >=0  ){
			fmt = "png";
		} else {
			fmt = "jpg";
		}
		console.log(fmt);
		if( fmt != "")
			aladin.setImageSurvey(aladin.createImageSurvey(obs_title, obs_title, hips_service_url, hips_frame, moc_order, {imgFormat: fmt})  );
		else 
			aladin.setImageSurvey(aladin.createImageSurvey(obs_title, obs_title, hips_service_url, hips_frame, moc_order)  );
	}
	var displaySelectedVizierCatalog = function (obs_id) {	
		if( obs_id == undefined ||  obs_id == "" || obs_id.startsWith("Vizier...")) {
			Modalinfo.info("you must first select a catalogue");
			var cmdNode = $("#service_vizier");
			cmdNode.css("color", "");
			cmdNode.css("background-color", "white");
			return;
		}
		var cmdNode = $("#service_vizier");
		for( var c=0 ; c<aladin.view.catalogs.length ; c++) {
			if( aladin.view.catalogs[c].name.startsWith("VizieR"))  {
				aladin.view.catalogs.splice(c, 1);
				aladin.view.mustClearCatalog = true;
				aladin.view.requestRedraw(); 
				break;
			}
		}
		$("#itemList").css("display", "none");
		cmdNode.attr("class", "selecteddatahelp");
		cmdNode.css("color", colorMap['Vizier'].fg);
		cmdNode.css("background-color", colorMap['Vizier'].bg);
		cmdNode.html(obs_id);
		var fov =  limitFov();
		Processing.show("Fetching Vizier data");
		aladin.addCatalog(A.catalogFromVizieR(obs_id
				, aladin.getRaDec()[0] + " " + aladin.getRaDec()[1]
				, fov
				, {onClick: 'showTable', color: colorMap['Vizier'].bg}
				, function() {Processing.hide(); }
		));
	}
	var toggleSelectedVizierCatalog = function () {	
		var cmdNode = $("#service_vizier");
		$("#itemList").css("display", "none");
		if( cmdNode.attr("class") == "selecteddatahelp" ) {
			cmdNode.css("color", "");
			cmdNode.css("background-color", "white");
			cmdNode.attr("class", "datahelp");
			for( var c=0 ; c<aladin.view.catalogs.length ; c++) {
				if( aladin.view.catalogs[c].name.startsWith("VizieR"))  {
					aladin.view.catalogs.splice(c, 1);
					aladin.view.mustClearCatalog = true;
					aladin.view.requestRedraw(); 
					break;
				}
			}

		} else {
			cmdNode.attr("class", "selecteddatahelp");
			displaySelectedVizierCatalog($("#service_vizier").html());
		}
	}
	var displaySimbadCatalog = function () {	
		var cmdNode = $("#service_simbad");
		if( cmdNode.attr("class") == "selecteddatahelp" ) {
			cmdNode.css("color", "");
			cmdNode.css("background-color", "white");
			cmdNode.attr("class", "datahelp");
			for( var c=0 ; c<aladin.view.catalogs.length ; c++) {
				if( aladin.view.catalogs[c].name.startsWith("Simbad"))  {
					aladin.view.catalogs.splice(c, 1);
					aladin.view.mustClearCatalog = true;
					aladin.view.requestRedraw(); 
					break;
				}
			}
		} else {
			var fov =  limitFov();
			cmdNode.attr("class", "selecteddatahelp");
			cmdNode.css("color", colorMap['Simbad'].fg);
			cmdNode.css("background-color", colorMap['Simbad'].bg);
			Processing.show("Fetching Simbad data");
			aladin.addCatalog(A.catalogFromSimbad(aladin.getRaDec()[0] + " " + aladin.getRaDec()[1]
			, fov
			, {onClick: 'showTable', color: colorMap['Simbad'].bg}
			, function() {Processing.hide(); }
			));
		}
	}
	var displayNedCatalog = function () {	
		var cmdNode = $("#service_ned");
		if( cmdNode.attr("class") == "selecteddatahelp" ) {
			cmdNode.css("color", "");
			cmdNode.css("background-color", "white");
			cmdNode.attr("class", "datahelp");
			for( var c=0 ; c<aladin.view.catalogs.length ; c++) {
				console.log(aladin.view.catalogs[c].name);
				if( aladin.view.catalogs[c].name.startsWith("NED"))  {
					aladin.view.catalogs.splice(c, 1);
					aladin.view.mustClearCatalog = true;
					aladin.view.requestRedraw(); 
					break;
				}
			}
		} else {
			cmdNode.attr("class", "selecteddatahelp");
			cmdNode.css("color", colorMap['NED'].fg);
			cmdNode.css("background-color", colorMap['NED'].bg);
			var fov =  limitFov();
			Processing.show("Fetching Ned data");
			aladin.addCatalog(A.catalogFromNED(aladin.getRaDec()[0] + " " + aladin.getRaDec()[1]
			, fov
			, {onClick: 'showTable', color: colorMap['NED'].bg}
			, function() {Processing.hide(); }
			));
		}
	}
	var limitFov = function () {
		var fov = aladin.getFov()[0];
		if( fov > 0.5 ) {
			Modalinfo.info("Search radius limted to 1/4deg", "Warning")
			fov = 0.25;
		}
		return fov;
	}

	/**
	 * Create a STC Region dialog
	 * data format
	 * stcString: "POLYGON ICRS 213.8925 44.575 213.8925 45.295 214.6125 45.295 214.6125 44.575"
   		size: 0.7199999999999989
   		raCenter: 214.2525
   		decCenter: 44.935
   		points: [[..., ...] .... ]
	 */
	var showSTCRegion = function (stcRegion) {	
		ModalAladin.init();	
		buildAladinDialog("STC Region Viewer");
		aladin.setImageSurvey("P/DSS2/color");
		aladin.gotoObject(stcRegion.raCenter + ' ' + stcRegion.decCenter);
		aladin.setZoom(2*stcRegion.size);
		displayInfo(stcRegion.stcString);
		var overlay = A.graphicOverlay({color: 'red', lineWidth: 2});
		aladin.addOverlay(overlay);
		overlay.addFootprints(A.polygon(stcRegion.points));             
	};

	/**
	 * Public part of the object
	 */ 
	var pblc = {};
	pblc.init = init;
	pblc.showSourcesInAladinLite = showSourcesInAladinLite;
	pblc.showSourceInAladinLite = showSourceInAladinLite;
	pblc.showAsyncSourcesInAladinLite = showAsyncSourcesInAladinLite;
	pblc.showSTCRegion = showSTCRegion;
	pblc.aladinExplorer = aladinExplorer;
	pblc.displaySelectedHips = displaySelectedHips;
	pblc.displaySelectedVizierCatalog = displaySelectedVizierCatalog;
	pblc.toggleSelectedVizierCatalog = toggleSelectedVizierCatalog;
	pblc.displaySimbadCatalog = displaySimbadCatalog;
	pblc.displayNedCatalog = displayNedCatalog;

	return pblc;
}();

ModalResult = function() {
	/**
	 * These next functions are used to build a result panel
	 * The main @param "content" of these function is an object with this structure:
	 * {
	 *    header: {
	 *      histo: {
	 *        prev: handler,
	 *        next: handler
	 *      },
	 *      title: {
	 *        label: "Title"
	 *      },
	 *      icon: {
	 *        classIcon: "class",
	 *        handler: handler
	 *      }
	 *    },
	 *    chapters: [
	 *    {
	 *       id: "Observation",
	 *       label: "Observation - Unique Detection Parameters",
	 *       url: 'url',
	 *       searchable: true,
	 *       params: {
	 *         oid: "1"
	 *       }
	 *    },
	 *    ... as many as you need
	 *    ]
	 * }
	 * url can be replaced by "data" contening aaColumns and aaData for the datatable
	 *  
	 **/

	// The class of the result panel
	var resultClass = "modalresult";
	var resultSelect = '.' + resultClass;

	// Creation of the history array
	// Will be an array of objects with this structure : {place: .., id: .., content: ..}
	var histo = new Array();

	// Current element in the history the user is in
	var current_histo = {};

	// Number of elements, used to define a place for each element in the history 
	var nb = 0;

	/**
	 * Return the content which has to be displayed in the h2 of the panel: the title and the icon if necessary
	 **/
	var getTitle = function(content) {
		var title='';

		if (content.title != undefined) {
			if (content.icon != undefined) {
				title += '<div class="col-xs-11">'+content.title.label+'</div><div class="col-xs-1"><a onclick="'+content.icon.handler+'" class='+content.icon.classIcon+'></a></div>';
			}
			else {
				title += '<div class="col-xs-12">'+content.title.label+'</div>';
			}
		} else {
			if (content.icon != undefined) {
				title += '<div class="col-xs-11">Details</div><div class="col-xs-1"><a onclick="'+content.icon.handler+'" class='+content.icon.classIcon+'></a></div>';
			}
			else {
				title += '<div class="col-xs-12">Details</div>';
			}
		}

		return title;
	};

	/**
	 * Set the pagination in the title of the panel
	 * @id: if of the panel 
	 **/
	var addHistoTitle = function(id) {
		$("#"+id).prev("div").find("span.ui-dialog-title").prepend('<a id="qhistoleft" href="javascript:void(0);" onclick="ModalResult.prevHisto()" class=greyhistoleft></a>'
				+ '<span class="nbpages"></span>'
				+ '<a id="qhistoright" href="javascript:void(0);" onclick="ModalResult.nextHisto()" class=greyhistoright></a>');
	};

	/**
	 * Set the differents chapters of the panel
	 * @selector: jQuery element, element where we want to add chapters
	 * @content: object, contains the chapters
	 **/
	var getChapters = function(selector, content) {
		for (i = 0; i < content.chapters.length; i++) {
			$(selector).append('<p class="chapter" id="'+content.chapters[i].id+'"><img src=\"images/tright.png\">'
					+content.chapters[i].label+'</p>'
					+'<div class="detaildata"></div>');

			var temp = content.chapters[i];

			$("#"+content.chapters[i].id).click({content_click: temp}, function(e){
				openChapterPanel(e.data.content_click);
			});

		}
	};

	/**
	 * Change the directions of chapter arrows
	 **/
	function switchArrow(id) {
		var image = $('#'+id+'').find('img').attr('src');
		if (image == 'images/tdown.png') {
			$('#'+id+'').find('img').attr('src', 'images/tright.png');
		} else if (image == 'images/tright.png') {
			$('#'+id+'').find('img').attr('src', 'images/tdown.png');
		}
	}

	/**
	 * Make the datatable of that panel visible. If there is no datatable, the url is invoked
	 * and the datatable is created 
	 * @param chapter   : Id of H4 banner of the table
	 * @param url       : service providing the JSON data fedding the datatable
	 * @param OID       : saada oid of the considered record
	 * @param searchable: set a search field if true 
	 */
	var openChapterPanel = function(chapter) {
		var div = $('#' + chapter.id).next('.detaildata');
		if( div.length == 0 ){
			Out.info("Can't open chapter " + chapter);
			return;
		}
		if (div.html().length > 0) {
			div.slideToggle(500);
			switchArrow(chapter.id);
		} else if(chapter.url != null ){
			Processing.show("Fetching data");
			$.getJSON(chapter.url, chapter.params , function(data) {
				Processing.hide();
				if( Processing.jsonError(data, chapter.url) ) {
					return;
				} else {
					showDataArray(chapter.id, data, chapter.searchable);
					switchArrow(chapter.id);
					Modalinfo.center();
				}
			});
		} else if (chapter.data != undefined && chapter.data != null) {
			showDataArray(chapter.id, chapter.data, chapter.searchable);	
			switchArrow(chapter.id);
			ModalResult.center();
		}
	};

	/**
	 *  Build a data table from the Josn data
	 *  @param divid     : Id of H4 banner of the table
	 *  @param jsdata    : JSON data readable for the datatable
	 *  @param withFilter: set a search field if true 
	 */
	var showDataArray = function(divid, jsdata, withFilter) {
		if ( jsdata.length != undefined ){
			var div = ($('#' + divid).next('.detaildata'));
			var dom = (withFilter)?'<"top"f>rt' : 'rt';
			for (var i=0; i<jsdata.length; i++){
				var id = "detail" + i + divid + "table";
				div.append("<table id="
						+ id
						+ "  width=100% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  class=\"display\"></table>");

				var options = {
						"aoColumns" : jsdata[i].aoColumns,
						"aaData" : jsdata[i].aaData,
						"sDom" : dom,
						"bPaginate" : false,
						"aaSorting" : [],
						"bSort" : false,
						"bFilter" : withFilter
				};

				var positions = [
				                 { "name": "filter",
				                	 "pos": "top-left"
				                 }];

				CustomDataTable.create(id, options, positions);
				if( jsdata[i].label != undefined ){
					($('#' + divid).next('.detaildata')).append(jsdata[i].label);
				}

			}

			div.slideToggle(0);
		}

		else {
			var id = "detail" + divid + "table";
			var div = $('#' + divid).next('.detaildata');
			var dom = (withFilter)?'<"top"f>rt' : 'rt';
			div.html("<table id="
					+ id
					+ "  width=100% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  class=\"display\"></table>");

			var options = {
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					"sDom" : dom,
					"bPaginate" : false,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : withFilter
			};

			var positions = [
			                 { "name": "filter",
			                	 "pos": "top-left"
			                 }];

			CustomDataTable.create(id, options, positions);
			if( jsdata.label != undefined ){
				($('#' + divid).next('.detaildata')).append(jsdata.label);
			}
			div.slideToggle(0);
		};
	};

	/**
	 * Change the style of filter input
	 * @id: id of the chapter
	 **/
	var changeFilter = function(id) {
		var label_filter = $('input[aria-controls="'+id+'"]').parent("label");
		label_filter.each(function(){
			$(this).prepend('<div class="form-group no-mg-btm">');
			$(this).find(".form-group").append('<div class="input-group">');
			$(this).find(".input-group").append('<div class="input-group-addon input-sm"><span class="glyphicon glyphicon-search"></span></div>');
			$(this).find("input").appendTo($(this).find(".input-group"));		
			$(this).find("input").addClass("form-control filter-result input-sm");
			$(this).find("input").attr("placeholder", "Search");
		});	
	};

	/**
	 * Add the content of a result panel in the history
	 **/
	var addToHisto = function(content, oid) {
		var isIn = false;
		var current;

		if (histo.length == 0) {
			histo.push({place: nb, id: oid, content: content});
			current_histo = {place: nb, id: oid, content: content};
			nb++;
		}
		else if (histo[histo.length - 1].id != oid) {
			histo.push({place: nb, id: oid, content: content});
			current_histo = {place: nb, id: oid, content: content};
			nb++;
		}
	};

	/**
	 * Display the previous content in the history
	 * If no previous content, display the last
	 **/
	var prevHisto = function() {
		if( current_histo.place <= 0 ) {
			current_histo = histo[histo.length - 1];
			resultPanel(current_histo.content, null, "white");
		} else {
			var prev = current_histo.place - 1;
			current_histo = histo[prev];
			resultPanel(current_histo.content, null, "white");
		}
		majHisto();
		return;
	};

	/**
	 * Display the next content in the history
	 * If no next content, display the first
	 **/
	var nextHisto = function() {
		if( current_histo.place >= (histo.length - 1) ) {
			current_histo = histo[0];
			resultPanel(current_histo.content, null, "white");
		}
		else {
			var next = current_histo.place + 1;
			current_histo = histo[next];
			resultPanel(current_histo.content, null, "white");
		}
		majHisto();
		return;
	};

	/**
	 * Display the position in the histo / size of histo
	 */
	var majHisto = function() {
		var true_index = current_histo.place + 1;
		var pages = true_index+"/"+histo.length;
		$("#qhistoleft").next("span").html(pages);
	}


	/**
	 * Build the result panel
	 * @content: object, containing the infos to build a result panel
	 * @closHandler: action to do when result panel is closed 
	 * @bgcolor: background-color of the result panel
	 * @add: boolean, tells if this result panel is open for the first time
	 * @param add is false if the function is called by the history's functions
	 **/
	var resultPanel = function (content, closeHandler, bgcolor, add) {
		// If the result panel already exists, only change its content
		if($(resultSelect).length != 0){
			$(resultSelect).html('');

			if( bgcolor != null ) {
				$("#"+id_modal).css("background-color", bgcolor);
			}

			// Set the handler wanted to be exectued when the panel is closed
			var chdl = ( closeHandler == null )? function(ev, ui)  {$(resultSelect).html("");}: closeHandler;		
			$(resultSelect).on( "dialogclose", function (event, ui) {            
				if (event.originalEvent) {
					chdl();
				}
			});
			$('div[pos="'+$(resultSelect).attr("id")+'"]').on("click", chdl);

			// Set the content of the h2 of the panel
			$(resultSelect).append('<h4><div id="detailhisto" class="row">'+getTitle(content.header)+'</div></h4>');
			getChapters(resultSelect, content);

			if (add) {
				addToHisto(content, content.chapters[0].params.oid);
			};

			if (content.header.histo != undefined) {
				addHistoTitle(id_modal, content.header.histo.prev, content.header.histo.next);
				majHisto();
			}

			content.chapters.forEach(function(chap) {
				changeFilter(chap.id);
			});


			jQuery(".detaildata").each(function(i) {$(this).hide();});
		}
		// If it doesn't exist, building of a new result panel
		else {
			var id_modal = Modalinfo.nextId();
			$(document.documentElement).append('<div id="'+id_modal+'" class="'+resultClass+'" style="display: none; width: auto; hight: auto;"></div>');

			var chdl = ( closeHandler == null )? function(ev, ui)  {$("#"+id_modal).html("");}: closeHandler;
			if( bgcolor != null ) {
				$("#"+id_modal).css("background-color", bgcolor);
			}

			$("#"+id_modal).append('<h4><div id="detailhisto" class="row">'+getTitle(content.header)+'</div></h4>');
			getChapters("#"+id_modal, content);

			$("#"+id_modal).dialog({ width: 'auto'
				, dialogClass: 'd-maxsize'
					, resizable: false
					, closeOnEscape: true
					, close: function (event, ui) {            
						if (event.originalEvent) {
							chdl();
							Modalinfo.close(Modalinfo.findLastModal());
						}
					}
			, width: 'auto' // overcomes width:'auto' and maxWidth bug
				, maxWidth: 1000
				, fluid: true
				, open: function(event, ui){
					// Put the content in the history
					addToHisto(content, content.chapters[0].params.oid);
					Modalinfo.fluidDialog();
				}});

			jQuery(".detaildata").each(function(i) {$(this).hide();});
			if (content.header.histo != undefined) {
				addHistoTitle(id_modal, content.header.histo.prev, content.header.histo.next);
				majHisto();
			}

			content.chapters.forEach(function(chap) {
				changeFilter(chap.id);
			});


			// Adjust the size of the panel to be responsive
			if ($("#"+id_modal).find("h4").find("#detailhisto").length) {
				if ($(window).width() >= 1000) {
					$("#"+id_modal).dialog( "option", "width", 1000 );
					center();
				}
				else {
					Modalinfo.fluidDialog();
				}
			}

			// Set the handler wanted to be executed when the panel is closed
			$('div[pos="'+$(resultSelect).attr("id")+'"]').on("click", chdl);

			Modalinfo.setShadow(id_modal);
			Modalinfo.whenClosed(id_modal);
		}
	};

	var getHtml = function() {
		return $(resultSelect).html();
	};

	/**
	 * Puts the resultpanel in the center of the window
	 **/
	var center = function() {
		var parent = $(resultSelect).parent();
		parent.css("position","absolute");
		parent.css("top", Math.max(0, (($(window).height() - parent.outerHeight()) / 3) + 
				$(window).scrollTop()) + "px");
		parent.css("left", Math.max(0, (($(window).width() - parent.outerWidth()) / 2) + 
				$(window).scrollLeft()) + "px");
	};

	var pblc = {};
	pblc.prevHisto = prevHisto;
	pblc.nextHisto = nextHisto;
	pblc.changeFilter = changeFilter;
	pblc.resultPanel = resultPanel;
	pblc.getHtml = getHtml;
	pblc.center = center;
	pblc.openChapterPanel = openChapterPanel;

	return pblc;
}();

Modalcommand = function() {
	var divId     = 'modalcommanddiv';
	var divSelect = '#' + divId;
	/*
	 * Privates functions
	 */
	var initDiv = function() {
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
		$(divSelect).dialog({resizable: false
			, width: 'auto'
				, title: title 			                      
				, zIndex: (zIndexModalinfo -1)
				, close: chdl});
	};
	/**
	 * Open a modal dialog with an handler called once the html is attached to the DOM
	 */
	var commandPanelAsync = function (title, htmlContent, openHandler, closeHandler) {
		initDiv();
		var chdl = ( closeHandler == null )? function(ev, ui)  {}: closeHandler;
		var ohdl = ( openHandler == null )? function(ev, ui)  {}: openHandler;
		$(divSelect).html(htmlContent);
		$(divSelect).dialog({resizable: false
			, width: 'auto'
				, title: title 			                      
				, zIndex: (zIndexModalinfo -1)
				, close: chdl
				, open: ohdl
		});
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
	pblc.commandPanelAsync = commandPanelAsync;
	pblc.setDivToggling = setDivToggling;
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
	var jsonError = function (jsondata, msg, custom_msg) {
		if( jsondata == undefined || jsondata == null ) {
			Modalinfo.error("JSON ERROR: " + msg + ": no data returned" );
			return true;
		}
		else if( jsondata.errormsg != null) {
			if (custom_msg == undefined) {
				Modalinfo.error("JSON ERROR: " + msg + ": "  + jsondata.errormsg);
			}
			else {
				Modalinfo.error(custom_msg);
			}
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
		/*
		 * String is, duplcated because if it comes from aJSON.stringify, the content of the JSON object may be altered
		 */
		var m = message;
		m = m.replace(/"/g, '');
		Out.info("PROCESSSING " + m);
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
				+ m + "</div>");
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
	var packedMode = false;
	/*
	 * Privates functions
	 */
	var printMsg = function (level, msg, withTrace) {
		if( !packedMode ){
			var e = new Error('dummy');	
			var stk;
			console.log(level + ": " + msg);
			/*
			 * IE ignore the stack property of the object Error
			 */
			if( withTrace && (stk = e.stack) != null ) {
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
		}
	};
	/*
	 * Public functions
	 */
	var setPackedMode = function() {
		debugModeOff();
		traceModeOff();
		packedMode = true;
	};
	var traceOn = function() {
		packedMode = false;
		trace = true;
	};
	var traceOff = function() {
		trace = false;
	};
	var debugModeOn = function() {
		packedMode = false;
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
		debugModeOff();
		traceOff();

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
	pblc.setPackedMode = setPackedMode;
	pblc.setdebugModeFromUrl = setdebugModeFromUrl;
	return pblc;
}();

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
 * Download class 
 * Location had to be renamed PageLocation to avoid a conflict with AladinLite
 */
PageLocation = function () {
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
			$(document.body).append('<iframe id="downloadIframe" src="' + url + '" style="display: hiddden;">Waitng for server response...</iframe>');
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
	pblc.confirmBeforeUnlaod   = confirmBeforeUnlaod; // oupps type
	pblc.confirmBeforeUnload   = confirmBeforeUnlaod;
	return pblc;
}();



console.log('=============== >  basics.js ');

//samp
//----
//Provides capabilities for using the SAMP Web Profile from JavaScript.
//Exported tokens are in the samp.* namespace.
//Inline documentation is somewhat patchy (partly because I don't know
//what javascript documentation is supposed to look like) - it is
//suggested to use it conjunction with the provided examples,
//currently visible at http://astrojs.github.com/sampjs/
//(gh-pages branch of github sources).
WebSamp_Mvc = function(appName, iconUrl, description) {

	/**
	 * keep a reference to ourselves
	 */
	var that = this;
	/**
	 * who is listening to us?
	 */
	var listener = null;
	/**
	 * add a listener to this view
	 */
	var addListener = function(list){
		listener = list;
	};
	/*
	 * Description of the local SAMP client
	 */
	var meta = {
			"samp.name": appName,
			"samp.description": description,
			"samp.icon.url": iconUrl
	};
	var subs=null; //subscription of SAMP events

	//=========================================================================
	// Constants defining well-known location of SAMP Web Profile hub etc.
	var WEBSAMP_PORT = 21012;
	var WEBSAMP_PATH = "/";
	var WEBSAMP_PREFIX = "samp.webhub.";
	var WEBSAMP_CLIENT_PREFIX = "";

	TYPE_STRING = "string";
	TYPE_LIST = "list";
	TYPE_MAP = "map";

	var noHub = true;

	var heir = function(proto) {
		function F() {};
		F.prototype = proto;
		return new F();
	};

	//======================================================================
	// Utility functions for navigating DOM etc.
	var getSampType = function(obj) {
		if (typeof obj === "string") {
			return TYPE_STRING;
		}
		else if (obj instanceof Array) {
			return TYPE_LIST;
		}
		else if (obj instanceof Object && obj !== null) {
			return TYPE_MAP;
		}
		else {
			throw new Error("Not legal SAMP object type: " + obj);
		}
	};
	var getChildElements = function(el, childTagName) {
		var children = el.childNodes;
		var child;
		var childEls = [];
		var i;
		for (i = 0; i < children.length; i++) {
			child = children[i];
			if (child.nodeType === 1) {  // Element
				if (childTagName && (child.tagName !== childTagName)) {
					throw new Error("Child <" + children[i].tagName + ">"
							+ " of <" + el.tagName + ">"
							+ " is not a <" + childTagName + ">");
				}
				childEls.push(child);
			}
		}
		return childEls;
	};
	var getSoleChild = function(el, childTagName) {
		var children = getChildElements(el, childTagName);
		if (children.length === 1 ) {
			return children[0];
		}
		else {
			throw new Error("No sole child of <" + el.tagName + ">");
		}
	};
	var getTextContent = function(el) {
		var txt = "";
		var i;
		var child;
		for (i = 0; i < el.childNodes.length; i++ ) {
			child = el.childNodes[i];
			if (child.nodeType === 1) {           // Element 
				throw new Error("Element found in text content");
			}
			else if (child.nodeType === 3 ||      // Text
					child.nodeType === 4 ) {     // CDATASection
				txt += child.nodeValue;
			}
		}
		return txt;
	};
	var stringify = function(obj) {
		return typeof JSON === "undefined" ? "..." : JSON.stringify(obj);
	};

	//=========================================================================
	// XmlRpc class:
	// Utilities for packing and unpacking XML-RPC messages.
	// See xml-rpc.com.
	var XmlRpc = {};

	// Takes text and turns it into something suitable for use as the content
	// of an XML-RPC string - special characters are escaped.
	XmlRpc.escapeXml = function(s) {
		return s.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;");
	};

	// Asserts that the elements of paramList match the types given by typeList.
	// TypeList must be an array containing only TYPE_STRING, TYPE_LIST
	// and TYPE_MAP objects in some combination.  paramList must be the
	// same length.
	// In case of mismatch an error is thrown.
	XmlRpc.checkParams = function(paramList, typeList) {
		var i;
		for (i = 0; i < typeList.length; i++) {
			if (typeList[i] !== TYPE_STRING &&
					typeList[i] !== TYPE_LIST &&
					typeList[i] !== TYPE_MAP) {
				throw new Error("Unknown type " + typeList[i]
				+ " in check list");
			}
		}
		var npar = paramList.length;
		var actualTypeList = [];
		var ok = true;
		for (i = 0; i < npar; i++) {
			actualTypeList.push(getSampType(paramList[i]));
		}
		ok = ok && (typeList.length === npar);
		for (i = 0; ok && i < npar; i++ ) {
			ok = ok && typeList[i] === actualTypeList[i];
		}
		if (!ok) {
			throw new Error("Param type list mismatch: " 
					+ "[" + typeList + "] != "
					+ "[" + actualTypeList + "]");
		}
	};

	// Turns a SAMP object (structure of strings, lists, maps) into an
	// XML string suitable for use with XML-RPC.
	XmlRpc.valueToXml = function v2x(obj, prefix) {
		prefix = prefix || "";
		var a;
		var i;
		var result;
		var type = getSampType(obj);
		if (type === TYPE_STRING) {
			return prefix
			+ "<value><string>"
			+ XmlRpc.escapeXml(obj)
			+ "</string></value>";
		} else if (type === TYPE_LIST) {
			result = [];
			result.push(prefix + "<value>",
					prefix + "  <array>",
					prefix + "    <data>");
			for (i = 0; i < obj.length; i++) {
				result.push(v2x(obj[i], prefix + "      "));
			}
			result.push(prefix + "    </data>",
					prefix + "  </array>",
					prefix + "</value>");

			return result.join("\n");
		} else if (type === TYPE_MAP) {
			result = [];
			result.push(prefix + "<value>");
			result.push(prefix + "  <struct>");
			for (i in obj) {
				result.push(prefix + "    <member>");
				result.push(prefix + "      <name>"
						+ XmlRpc.escapeXml(i)
						+ "</name>");
				result.push(v2x(obj[i], prefix + "      "));
				result.push(prefix + "    </member>");
			}
			result.push(prefix + "  </struct>");
			result.push(prefix + "</value>");
			return result.join("\n");
		} else {
			throw new Error("bad type");  // shouldn't get here
		}
	};

	// Turns an XML string from and XML-RPC message into a SAMP object
	// (structure of strings, lists, maps).
	XmlRpc.xmlToValue = function x2v(valueEl, allowInt) {
		var childEls = getChildElements(valueEl);
		var i;
		var j;
		var txt;
		var node;
		var childEl;
		var elName;
		if (childEls.length === 0) {
			return getTextContent(valueEl);
		}  else if (childEls.length === 1) {
			childEl = childEls[0];
			elName = childEl.tagName;
			if (elName === "string") {
				return getTextContent(childEl);
			} else if (elName === "array") {
				var valueEls =
					getChildElements(getSoleChild(childEl, "data"), "value");
				var list = [];
				for (i = 0; i < valueEls.length; i++) {
					list.push(x2v(valueEls[i], allowInt));
				}
				return list;
			}
			else if (elName === "struct") {
				var memberEls = getChildElements(childEl, "member");
				var map = {};
				var s_name;
				var s_value;
				var jc;
				for (i = 0; i < memberEls.length; i++) {
					s_name = undefined;
					s_value = undefined;
					for (j = 0; j < memberEls[i].childNodes.length; j++) {
						jc = memberEls[i].childNodes[j];
						if (jc.nodeType == 1) {
							if (jc.tagName === "name") {
								s_name = getTextContent(jc);
							} else if (jc.tagName === "value") {
								s_value = x2v(jc, allowInt);
							}
						}
					}
					if (s_name !== undefined && s_value !== undefined) {
						map[s_name] = s_value;
					} else {
						throw new Error("No <name> and/or <value> "
								+ "in <member>?");
					}
				}
				return map;
			} else if (allowInt && (elName === "int" || elName === "i4")) {
				return getTextContent(childEl);
			} else {
				throw new Error("Non SAMP-friendly value content: "
						+ "<" + elName + ">");
			}
		}
		else {
			throw new Error("Bad XML-RPC <value> content - multiple elements");
		}
	};

	// Turns the content of an XML-RPC <params> element into an array of
	// SAMP objects.
	XmlRpc.decodeParams = function(paramsEl) {
		var paramEls = getChildElements(paramsEl, "param");
		var i;
		var results = [];
		for (i = 0; i < paramEls.length; i++) {
			results.push(XmlRpc.xmlToValue(getSoleChild(paramEls[i], "value")));
		}
		return results;
	};

	// Turns the content of an XML-RPC <fault> element into an XmlRpc.Fault
	// object.
	XmlRpc.decodeFault = function(faultEl) {
		var faultObj = XmlRpc.xmlToValue(getSoleChild(faultEl, "value"), true);
		return new XmlRpc.Fault(faultObj.faultString, faultObj.faultCode);
	};

	// Turns an XML-RPC response element (should be <methodResponse>) into
	// either a SAMP response object or an XmlRpc.Fault object.
	// Note that a fault response does not throw an error, so check for
	// the type of the result if you want to know whether a fault occurred.
	// An error will however be thrown if the supplied XML does not
	// correspond to a legal XML-RPC response.
	XmlRpc.decodeResponse = function(xml) {
		var mrEl = xml.documentElement;
		if (mrEl.tagName !== "methodResponse") {
			throw new Error("Response element is not <methodResponse>");
		}
		var contentEl = getSoleChild(mrEl);
		if (contentEl.tagName === "fault") {
			return XmlRpc.decodeFault(contentEl);
		} else if (contentEl.tagName === "params") {
			return XmlRpc.decodeParams(contentEl)[0];
		} else {
			throw new Error("Bad XML-RPC response - unknown element"
					+ " <" + contentEl.tagName + ">");
		}
	};

	// XmlRpc.Fault class:
	// Represents an XML-RPC Fault response.
	XmlRpc.Fault = function(faultString, faultCode) {
		this.faultString = faultString;
		this.faultCode = faultCode;
	};
	XmlRpc.Fault.prototype.toString = function() {
		return "XML-RPC Fault (" + this.faultCode + "): " + this.faultString;
	};



	//=========================================================================
	// XmlRpcRequest class:
	// Represents an call which can be sent to an XML-RPC server.
	var XmlRpcRequest = function(methodName, params) {
		this.methodName = methodName;
		this.params = params || [];
	}
	XmlRpcRequest.prototype.toString = function() {
		return this.methodName + "(" + stringify(this.params) + ")";
	};
	XmlRpcRequest.prototype.addParam = function(param) {
		this.params.push(param);
		return this;
	};
	XmlRpcRequest.prototype.addParams = function(params) {
		var i;
		for (i = 0; i < params.length; i++) {
			this.params.push(params[i]);
		}
		return this;
	};
	XmlRpcRequest.prototype.checkParams = function(typeList) {
		XmlRpc.checkParams(this.params, typeList);
	};
	XmlRpcRequest.prototype.toXml = function() {
		var lines = [];
		lines.push(
				"<?xml version='1.0'?>",
				"<methodCall>",
				"  <methodName>" + this.methodName + "</methodName>",
		"  <params>");
		for (var i = 0; i < this.params.length; i++) {
			lines.push("    <param>",
					XmlRpc.valueToXml(this.params[i], "      "),
			"    </param>");
		}
		lines.push(
				"  </params>",
		"</methodCall>");		
//		alert(lines.join("\n"));
//		alert(JSON.stringify(meta));

		return lines.join("\n");
	};


	//=========================================================================
	// XmlRpcClient class:
	// Object capable of sending XML-RPC calls to an XML-RPC server.
	// That server will typically reside on the host on which the
	// javascript is running; it is not likely to reside on the host
	// which served the javascript.  That means that sandboxing restrictions
	// will be in effect.  Much of the work done here is therefore to
	// do the client-side work required to potentially escape the sandbox.
	// The endpoint parameter, if supplied, is the URL of the XML-RPC server.
	// If absent, the default SAMP Web Profile server is used.
	var XmlRpcClient = function(endpoint) {
		this.endpoint = endpoint ||
		"http://localhost:" + WEBSAMP_PORT + WEBSAMP_PATH;
	};

	// Creates an XHR facade - an object that presents an interface
	// resembling that of an XMLHttpRequest Level 2.
	// This facade may be based on an actual XMLHttpRequest Level 2 object
	// (on browsers that support it), or it may fake one using other
	// available technology.
	//
	// The created facade in any case presents the following interface:
	//
	//    open(method, url)
	//    send(body)
	//    abort()
	//    setContentType()
	//    responseText
	//    responseXML
	//    onload
	//    onerror(err)  - includes timeout; abort is ignored
	//
	// See the documentation at http://www.w3.org/TR/XMLHttpRequest/
	// for semantics.
	//
	// XMLHttpRequest Level 2 supports Cross-Origin Resource Sharing (CORS)
	// which makes sandbox evasion possible.  Faked XHRL2s returned by
	// this method may use CORS or some other technology to evade the
	// sandbox.  The SAMP hub itself may selectively allow some of these
	// technologies and not others, according to configuration.
	XmlRpcClient.createXHR = function() {

		// Creates an XHR facade based on a genuine XMLHttpRequest Level 2.
		var XhrL2 = function(xhr) {
			this.xhr = xhr;
			xhr.onreadystatechange = (function(l2) {
				return function() {
					if (xhr.readyState !== 4) {
						return;
					} else if (!l2.completed) {
						if (+xhr.status === 200) {
							l2.completed = true;
							l2.responseText = xhr.responseText;
							l2.responseXML = xhr.responseXML;
							if (l2.onload) {
								l2.onload();
							}
						}
					}
				};
			})(this);
			xhr.onerror = (function(l2) {
				return function(event) {
					notifyHubError("XmlRpcClient.create  xhr.onerror No hub?");
					if (!l2.completed) {
						l2.completed = true;
						if (l2.onerror) {
							if (event) {
								event.toString = function() {return "No hub?";};
							} else {
								event = "No hub?";
							}
							l2.onerror(event);

						}
						/*
						 * Added by myself to process a sudden disappearance of the hub (kill -9)
						 */
					} else {
						if (l2.onerror) {
							if (event) {
								event.toString = function() {return "No hub?";};
							}
							else {
								event = "No hub?";
							}
							l2.onerror(event);
						}
					}
				};
			})(this);
			xhr.ontimeout = (function(l2) {
				return function(event) {
					if (!l2.completed) {
						l2.completed = true;
						if (l2.onerror) {
							l2.onerror("timeout");
						}
					}
				};
			})(this);
		};
		XhrL2.prototype.open = function(method, url) {
			this.xhr.open(method, url);
		};
		XhrL2.prototype.send = function(body) {
			this.xhr.send(body);
		};
		XhrL2.prototype.abort = function() {
			this.xhr.abort();
		}
		XhrL2.prototype.setContentType = function(mimeType) {
			if ("setRequestHeader" in this.xhr) {
				this.xhr.setRequestHeader("Content-Type", mimeType);
			}
		}

		// Creates an XHR facade based on an XDomainRequest (IE8+ only).
		var XdrL2 = function(xdr) {
			this.xdr = xdr;
			xdr.onload = (function(l2) {
				return function() {
					var e;
					l2.responseText = xdr.responseText;
					if (xdr.contentType === "text/xml" ||
							xdr.contentType === "application/xml" ||
							/\/x-/.test(xdr.contentType)) {
						try {
							var xdoc = new ActiveXObject("Microsoft.XMLDOM");
							xdoc.loadXML(xdr.responseText);
							l2.responseXML = xdoc;
						}
						catch (e) {
							l2.responseXML = e;
						}
					}
					if (l2.onload) {
						l2.onload();
					}
				};
			})(this);
			xdr.onerror = (function(l2) {
				return function(event) {
					if (l2.onerror) {
						l2.onerror(event);
					}
				};
			})(this);
			xdr.ontimeout = (function(l2) {
				return function(event) {
					if (l2.onerror) {
						l2.onerror(event);
					}
				};
			})(this);
		};
		XdrL2.prototype.open = function(method, url) {
			this.xdr.open(method, url);
		};
		XdrL2.prototype.send = function(body) {
			this.xdr.send(body);
		};
		XdrL2.prototype.abort = function() {
			this.xdr.abort();
		};
		XdrL2.prototype.setContentType = function(mimeType) {
			// can't do it.
		};

		// Creates an XHR Facade based on available XMLHttpRequest-type
		// capabilibities.
		// If an actual XMLHttpRequest Level 2 is available, use that.
		if (typeof XMLHttpRequest !== "undefined") {
			var xhr = new XMLHttpRequest();
			if ("withCredentials" in xhr) {
				return new XhrL2(xhr);
			}
		}

		// Else if an XDomainRequest is available, use that.
		if (typeof XDomainRequest !== "undefined") {
			return new XdrL2(new XDomainRequest());
		}

		// Else fake an XMLHttpRequest using Flash/flXHR, if available
		// and use that.
		if (typeof flensed.flXHR !== "undefined") {
			return new XhrL2(new flensed.flXHR({instancePooling: true}));
		}

		// No luck.
		throw new Error("no cross-origin mechanism available");
	};

	// Executes a request by passing it to the XML-RPC server.
	// On success, the result is passed to the resultHandler.
	// On failure, the errHandler is called with one of two possible
	// arguments: an XmlRpc.Fault object, or an Error object.
	XmlRpcClient.prototype.execute = function(req, resultHandler, errHandler) {
		(function(xClient) {
			var xhr;
			var e;
			try {
				xhr = XmlRpcClient.createXHR();
				xhr.open("POST", xClient.endpoint);
				xhr.setContentType("text/xml");
			}
			catch (e) {
				errHandler(e);
				throw e;
			}
			xhr.onload = function() {
				var xml = xhr.responseXML;
				var result;
				var e;
				if (xml) {
					try {
						result = XmlRpc.decodeResponse(xml);
					}
					catch (e) {
						notifyHubError("XmlRpcClient.prototype.execute wrong XML response");
						if (errHandler) {
							errHandler(e);
						}
						return;
					}
				} else {
					notifyHubError("XmlRpcClient.prototype.execute no XML response");
					if (errHandler) {
						errHandler("no XML response");
					}
					return;
				}
				if (result instanceof XmlRpc.Fault) {
					notifyHubError("XmlRpcClient.prototype.execute  XmlRpc.Fault");
					if (errHandler) {
						errHandler(result);
					}
				} else {
					noHub = false;
					Out.debug("noHub = false XmlRpcClient.prototype.execute");
					if (resultHandler) {
						resultHandler(result);
					}
				}
			};
			xhr.onerror = function(event) {
				if (event) {
					event.toString = function() {return "No hub?";}
				} else {
					event = "No hub";
				}
				if (errHandler) {
					errHandler(event);
				}
			};
			xhr.send(req.toXml());
			return xhr;
		})(this);
	}; 

	//=========================================================================
	// Message class:
	// Aggregates an MType string and a params map.
	var Message = function(mtype, params) {
		this["samp.mtype"] = mtype;
		this["samp.params"] = params;
	};


	//=========================================================================
	// Connection class:
	// this is what clients use to communicate with the hub.
	//
	// All the methods from the Hub Abstract API as described in the
	// SAMP standard are available as methods of a Connection object.
	// The initial private-key argument required by the Web Profile is
	// handled internally by this object - you do not need to supply it
	// when calling one of the methods.
	//
	// All these calls have the same form:
	//
	//    connection.method([method-args], resultHandler, errorHandler)
	//
	// the first argument is an array of the arguments (as per the SAMP
	// abstract hub API), the second argument is a function which is
	// called on successful completion with the result of the SAMP call
	// as its argument, and the third argument is a function which is
	// called on unsuccessful completion with an error object as its
	// argument.  The resultHandler and errorHandler arguments are optional.
	//
	// So for instance if you have a Connection object conn,
	// you can send a notify message to all other clients by doing, e.g.:
	//
	//    conn.notifyAll([new samp.Message(mtype, params)])
	//
	// Connection has other methods as well as the hub API ones
	// as documented below.
	var Connection = function(regInfo) {
		this.regInfo = regInfo;
		this.privateKey = regInfo["samp.private-key"];
		if (! typeof(this.privateKey) === "string") {
			throw new Error("Bad registration object");
		}
		this.xClient = new XmlRpcClient();
	};
	(function() {
		var connMethods = {
				call: [TYPE_STRING, TYPE_STRING, TYPE_MAP],
				callAll: [TYPE_STRING, TYPE_MAP],
				callAndWait: [TYPE_STRING, TYPE_MAP, TYPE_STRING],
				declareMetadata: [TYPE_MAP],
				declareSubscriptions: [TYPE_MAP],
				getMetadata: [TYPE_STRING],
				getRegisteredClients: [],
				getSubscribedClients: [TYPE_STRING],
				getSubscriptions: [TYPE_STRING],
				notify: [TYPE_STRING, TYPE_MAP],
				notifyAll: [TYPE_MAP],
				ping: [],
				reply: [TYPE_STRING, TYPE_MAP]
		};
		var fn;
		var types;
		for (fn in connMethods) {
			(function(fname, types) {
				// errHandler may be passed an XmlRpc.Fault or a thrown Error.
				Connection.prototype[fname] =
					function(sampArgs, resultHandler, errHandler) {
					var closer =
						(function(c) {return function() {c.close()}})(this);
					errHandler = errHandler || closer
					XmlRpc.checkParams(sampArgs, types);
					var request = new XmlRpcRequest(WEBSAMP_PREFIX + fname);
					request.addParam(this.privateKey);
					request.addParams(sampArgs);
					return this.xClient.
					execute(request, resultHandler, errHandler);
				};
			})(fn, connMethods[fn]);
		}
	})();
	Connection.prototype.unregister = function() {
		var e;
		if (this.callbackRequest) {
			try {
				this.callbackRequest.abort();
			} catch (e) {
			}
		}
		var request = new XmlRpcRequest(WEBSAMP_PREFIX + "unregister");
		request.addParam(this.privateKey);
		try {
			this.xClient.execute(request);
		} catch (e) {
			// log unregister failed
		}
		delete this.regInfo;
		delete this.privateKey;
	};

	// Closes this connection.  It unregisters from the hub if still
	// registered, but may harmlessly be called multiple times.
	Connection.prototype.close = function() {
		var e;
		if (this.closed) {
			return;
		}
		this.closed = true;
		try {
			if (this.regInfo) {
				this.unregister();
			}
		} catch (e) {
		}
		if (this.onclose) {
			oc = this.onclose;
			delete this.onclose;
			try {
				oc();
			} catch (e) {
			}
		}
	};

	// Arranges for this connection to receive callbacks.
	//
	// The callableClient argument must be an object implementing the
	// SAMP callable client API, i.e. it must have the following methods:
	//
	//     receiveNotification(string sender-id, map message)
	//     receiveCall(string sender-id, string msg-id, map message)
	//     receiveResponse(string responder-id, string msg-tag, map response)
	// 
	// The successHandler argument will be called with no arguments if the
	// allowCallbacks hub method completes successfully - it is a suitable
	// hook to use for declaring subscriptions.
	//
	// The CallableClient class provides a suitable implementation, see below.
	Connection.prototype.setCallable = function(callableClient,
			successHandler) {
		var e;
		if (this.callbackRequest) {
			try {
				this.callbackRequest.abort();
			} catch (e) {
			} finally {
				delete this.callbackRequest;
			}
		}
		if (!callableClient && !this.regInfo) {
			return;
		}
		var request =
			new XmlRpcRequest(WEBSAMP_PREFIX + "allowReverseCallbacks");
		request.addParam(this.privateKey);
		request.addParam(callableClient ? "1" : "0");
		var closer = (function(c) {return function() {c.close()}})(this);
		if (callableClient) {
			(function(connection) {
				var invokeCallback = function(callback) {
					var methodName = callback["samp.methodName"];
					var methodParams = callback["samp.params"];
					var handlerFunc = undefined;
					if (methodName === WEBSAMP_CLIENT_PREFIX
							+ "receiveNotification") {
						handlerFunc = callableClient.receiveNotification;
					} else if (methodName === WEBSAMP_CLIENT_PREFIX
							+ "receiveCall") {
						handlerFunc = callableClient.receiveCall;
					} else if (methodName === WEBSAMP_CLIENT_PREFIX
							+ "receiveResponse") {
						handlerFunc = callableClient.receiveResponse;
					} else {
						// unknown callback??
					}
					if (handlerFunc) {
						handlerFunc.apply(callableClient, methodParams);
					}
				};
				var startTime;
				var resultHandler = function(result) {
					if (getSampType(result) != TYPE_LIST) {
						errHandler(new Error("pullCallbacks result not List"));
						return;
					}
					var i;
					var e;
					for (i = 0; i < result.length; i++) {
						try {
							invokeCallback(result[i]);
						} catch (e) {
							// log here?
						}
					}
					callWaiter();
				};
				var errHandler = function(error) {
					var elapsed = new Date().getTime() - startTime;
					if (elapsed < 1000) {
						connection.close();
					} else {
						// probably a timeout
						callWaiter();
					}
				};
				var callWaiter = function() {
					if (!connection.regInfo) {
						return;
					}
					var request =
						new XmlRpcRequest(WEBSAMP_PREFIX + "pullCallbacks");
					request.addParam(connection.privateKey);
					request.addParam("600");
					startTime = new Date().getTime();
					connection.callbackRequest =
						connection.xClient.
						execute(request, resultHandler, errHandler);
				};
				var sHandler = function() {
					callWaiter();
					successHandler();
				};
				connection.xClient.execute(request, sHandler, closer);
			})(this);
		}
		else {
			this.xClient.execute(request, successHandler, closer);
		}
	};

	// Takes a public URL and returns a URL that can be used from within
	// this javascript context.  Some translation may be required, since
	// a URL sent by an external application may be cross-domain, in which
	// case browser sandboxing would typically disallow access to it.
	Connection.prototype.translateUrl = function(url) {
		var translator = this.regInfo["samp.url-translator"] || "";
		return translator + url;
	};
	Connection.Action = function(actName, actArgs, resultKey) {
		this.actName = actName;
		this.actArgs = actArgs;
		this.resultKey = resultKey;
	};

	//================================================================================    
	// Suitable implementation for a callable client object which can
	// be supplied to Connection.setCallable().
	// Its callHandler and replyHandler members are string->function maps
	// which can be used to provide handler functions for MTypes and
	// message tags respectively.
	//
	// In more detail:
	// The callHandler member maps a string representing an MType to
	// a function with arguments (senderId, message, isCall).
	// The replyHandler member maps a string representing a message tag to
	// a function with arguments (responderId, msgTag, response).
	var CallableClient = function(connection) {
		this.callHandler = {};
		this.replyHandler = {};
	};
	CallableClient.prototype.init = function(connection) {
	};
	CallableClient.prototype.receiveNotification = function(senderId, message) {
		var mtype = message["samp.mtype"];
		var handled = false;
		var e;
		if (mtype in this.callHandler) {
			try {
				this.callHandler[mtype](senderId, message, false);
			} catch (e) {
			}
			handled = true;
		}
		return handled;
	};
	CallableClient.prototype.receiveCall = function(senderId, msgId, message) {
		var mtype = message["samp.mtype"];
		var handled = false;
		var response;
		var result;
		var e;
		if (mtype in this.callHandler) {
			try {
				result = this.callHandler[mtype](senderId, message, true) || {};
				response = {"samp.status": "samp.ok",
						"samp.result": result};
				handled = true;
			} catch (e) {
				response = {"samp.status": "samp.error",
						"samp.error": {"samp.errortxt": e.toString()}};
			}
		} else {
			response = {"samp.status": "samp.warning",
					"samp.result": {},
					"samp.error": {"samp.errortxt": "no action"}};
		}
		this.connection.reply([msgId, response]);
		return handled;
	};
	CallableClient.prototype.receiveResponse = function(responderId, msgTag,
			response) {
		var handled = false;
		var e;
		if (msgTag in this.replyHandler) {
			try {
				this.replyHandler[msgTag](responderId, msgTag, response);
				handled = true;
			} catch (e) {
			}
		}
		return handled;
	};
	CallableClient.prototype.calculateSubscriptions = function() {
		var subs = {};
		var mt;
		for (mt in this.callHandler) {
			subs[mt] = {};
		}
		return subs;
	};


	//================================================================================
	// ClientTracker is a CallableClient which also provides tracking of
	// registered clients.
	//
	// Its onchange member, if defined, will be called with arguments
	// (client-id, change-type, associated-data) whenever the list or
	// characteristics of registered clients has changed.
	var ClientTracker = function() {
		var tracker = this;
		this.ids = {};
		this.metas = {};
		this.subs = {};
		this.replyHandler = {};
		this.callHandler = {
				"samp.hub.event.shutdown": function(senderId, message) {
					tracker.connection.close();
				},
				"samp.hub.disconnect": function(senderId, message) {
					tracker.connection.close();
				},
				"samp.hub.event.register": function(senderId, message) {
					var id = message["samp.params"]["id"];
					tracker.ids[id] = true;
					tracker.changed(id, "register", null);
				},
				"samp.hub.event.unregister": function(senderId, message) {
					var id = message["samp.params"]["id"];
					delete tracker.ids[id];
					delete tracker.metas[id];
					delete tracker.subs[id];
					tracker.changed(id, "unregister", null);
				},
				"samp.hub.event.metadata": function(senderId, message) {
					var id = message["samp.params"]["id"];
					var meta = message["samp.params"]["metadata"];
					tracker.metas[id] = meta;
					tracker.changed(id, "meta", meta);
				},
				"samp.hub.event.subscriptions": function(senderId, message) {
					var id = message["samp.params"]["id"];
					var subs = message["samp.params"]["subscriptions"];
					tracker.subs[id] = subs;
					tracker.changed(id, "subs", subs);
				}
		};
	};
	ClientTracker.prototype = heir(CallableClient.prototype);
	ClientTracker.prototype.changed = function(id, type, data) {
		if (this.onchange) {
			this.onchange(id, type, data);
		}
	};
	ClientTracker.prototype.init = function(connection) {
		var tracker = this;
		this.connection = connection;
		var retrieveInfo = function(id, type, infoFuncName, infoArray) {
			connection[infoFuncName]([id], function(info) {
				infoArray[id] = info;
				tracker.changed(id, type, info);
			});
		};
		connection.getRegisteredClients([], function(idlist) {
			var i;
			var id;
			tracker.ids = {};
			for (i = 0; i < idlist.length; i++) {
				id = idlist[i];
				tracker.ids[id] = true;
				retrieveInfo(id, "meta", "getMetadata", tracker.metas);
				retrieveInfo(id, "subs", "getSubscriptions", tracker.subs);
			}
			tracker.changed(null, "ids", null);
		});
	};
	ClientTracker.prototype.getName = function(id) {
		var meta = this.metas[id];
		return (meta && meta["samp.name"]) ? meta["samp.name"] : "[" + id + "]";
	};


	//==========================================================================================
	// Connector class:
	// A higher level class which can manage transparent hub
	// registration/unregistration and client tracking.
	//
	// On construction, the name argument is mandatory, and corresponds
	// to the samp.name item submitted at registration time.
	// The other arguments are optional.
	// meta is a metadata map (if absent, no metadata is declared)
	// callableClient is a callable client object for receiving callbacks
	// (if absent, the client is not callable).
	// subs is a subscriptions map (if absent, no subscriptions are declared)
	var Connector = function(name, meta, callableClient, subs) {
		this.name = name;
		this.meta = meta;
		this.callableClient = callableClient;
		this.subs = subs;
		this.regTextNodes = [];
		this.whenRegs = [];
		this.whenUnregs = [];
		this.connection = undefined;
		this.onreg = undefined;
		this.onunreg = undefined;
	};
	var setRegText = function(connector, txt) {
		Out.info(txt);
		var i;
		var nodes = connector.regTextNodes;
		var node;
		for (i = 0; i < nodes.length; i++) {
			node = nodes[i];
			node.innerHTML = "";
			node.appendChild(document.createTextNode(txt));
		}
	};
	Connector.prototype.setConnection = function(conn) {
		var connector = this;
		var e;
		if (this.connection) {
			this.connection.close();
			if (this.onunreg) {
				try {
					this.onunreg();
				} catch (e) {
				}
			}
		}
		this.connection = conn;
		if (conn) {
			conn.onclose = function() {
				connector.connection = null;
				if (connector.onunreg) {
					try {
						connector.onunreg();
					} catch (e) {
					}
				}
				connector.update();
			};
			if (this.meta) {
				conn.declareMetadata([this.meta]);
			}
			if (this.callableClient) {
				if (this.callableClient.init) {
					this.callableClient.init(conn);
				}
				conn.setCallable(this.callableClient, function() {
					conn.declareSubscriptions([connector.subs]);
				});
			}
			if (this.onreg) {
				try {
					this.onreg(conn);
				} catch (e) {
				}
			}
		}
		this.update();
	};
	Connector.prototype.register = function() {
		var connector = this;
		var regErrHandler = function(err) {
			setRegText(connector, "no (" + err.toString() + ")");
			notifyHubError("Connector: no (" + err.toString() + ")");
		};
		var regSuccessHandler = function(conn) {
			connector.setConnection(conn);
			setRegText(connector, conn ? "Yes" : "No");
			Out.debug("noHub = false Connector.prototype.register");
			noHub = false;
		};
		register(this.name, regSuccessHandler, regErrHandler);
	};
	Connector.prototype.unregister = function() {
		if (this.connection) {
			this.connection.unregister([]);
			this.setConnection(null);
		}
	};

	// Returns a document fragment which contains Register/Unregister
	// buttons for use by the user to attempt to connect/disconnect
	// with the hub.  This is useful for models where explicit
	// user registration is encouraged or required, but when using
	// the register-on-demand model such buttons are not necessary.
	Connector.prototype.createRegButtons = function() {
		var connector = this;
		var regButt = document.createElement("button");
		regButt.setAttribute("type", "button");
		regButt.appendChild(document.createTextNode("Register"));
		regButt.onclick = function() {connector.register();};
		this.whenUnregs.push(regButt);
		var unregButt = document.createElement("button");
		unregButt.setAttribute("type", "button");
		unregButt.appendChild(document.createTextNode("Unregister"));
		unregButt.onclick = function() {connector.unregister();};
		this.whenRegs.push(unregButt);
		var regText = document.createElement("span");
		this.regTextNodes.push(regText);
		var node = document.createDocumentFragment();
		node.appendChild(regButt);
		node.appendChild(document.createTextNode(" "));
		node.appendChild(unregButt);
		var label = document.createElement("span");
		label.innerHTML = " <strong>Registered: </strong>";
		node.appendChild(label);
		node.appendChild(regText);
		this.update();
		return node;
	};

	Connector.prototype.update = function() {
		var i;
		var isConnected = !! this.connection;
		var enableds = isConnected ? this.whenRegs : this.whenUnregs;
		var disableds = isConnected ? this.whenUnregs : this.whenRegs;
		for (i = 0; i < enableds.length; i++) {
			enableds[i].removeAttribute("disabled");
		}
		for (i = 0; i < disableds.length; i++) {
			disableds[i].setAttribute("disabled", "disabled");
		}
		setRegText(this, "No");
	};

	// Provides execution of a SAMP operation with register-on-demand.
	// You can use this method to provide lightweight registration/use
	// of web SAMP.  Simply provide a connHandler function which
	// does something with a connection (e.g. sends a message) and
	// Connector.runWithConnection on it.  This will connect if not
	// already connected, and call the connHandler on with the connection.
	// No explicit registration action is then required from the user.
	//
	// If the regErrorHandler argument is supplied, it is a function of
	// one (error) argument called in the case that registration-on-demand
	// fails.
	//
	// This is a more-or-less complete sampjs page:
	//   <script>
	//     var connector = new samp.Connector("pinger", {"samp.name": "Pinger"})
	//     var pingFunc = function(connection) {
	//       connection.notifyAll([new samp.Message("samp.app.ping", {})])
	//     }
	//   </script>
	//   <button onclick="connector.runWithConnection(pingFunc)">Ping</button>
	Connector.prototype.runWithConnection =
		function(connHandler, regErrorHandler) {
		var connector = this;
		var regSuccessHandler = function(conn) {
			connector.setConnection(conn);
			connHandler(conn);
		};
		var regFailureHandler = function(e) {
			connector.setConnection(undefined);
			regErrorHandler(e);
		};
		var pingResultHandler = function(result) {
			connHandler(connector.connection);
		};
		var pingErrorHandler = function(err) {
			register(this.name, regSuccessHandler, regFailureHandler);
		};
		if (this.connection) {
			// Use getRegisteredClients as the most lightweight check
			// I can think of that this connection is still OK.
			// Ping doesn't work because the server replies even if the
			// private-key is incorrect/invalid.  Is that a bug or not?
			this.connection.
			getRegisteredClients([], pingResultHandler, pingErrorHandler);
		} else {
			register(this.name, regSuccessHandler, regFailureHandler);
		}
	};

	// Sets up an interval timer to run at intervals and notify a callback
	// about whether a hub is currently running.
	// Every millis milliseconds, the supplied availHandler function is
	// called with a boolean argument: true if a (web profile) hub is
	// running, false if not.
	// Returns the interval timer (can be passed to clearInterval()).
	Connector.prototype.onHubAvailability = function(availHandler, millis) {
		samp.ping(availHandler);

		// Could use the W3C Page Visibility API to avoid making these
		// checks when the page is not visible.
		return setInterval(function() {samp.ping(availHandler);}, millis);
	};



	//======================================================================================
	// Misc functions
	// Determines whether a given subscriptions map indicates subscription
	// to a given mtype.
	var isSubscribed = function(subs, mtype) {
		var matching = function(pattern, mtype) {
			if (pattern == mtype) {
				return true;
			}
			else if (pattern === "*") {
				return true;
			}
			else {
				var prefix;
				var split = /^(.*)\.\*$/.exec(pat);
				if (split) {
					prefix = split[1];
					if (prefix === mtype.substring(0, prefix.length)) {
						return true;
					}
				}
			}
			return false;
		};
		var pat;
		for (pat in subs) {
			if (matching(pat, mtype)) {
				return true;
			}
		}
		return false;
	}

	// Attempts registration with a SAMP hub.
	// On success the supplied connectionHandler function is called
	// with the connection as an argument, on failure the supplied
	// errorHandler is called with an argument that may be an Error
	// or an XmlRpc.Fault.
	var register = function(appName, connectionHandler, errorHandler) {
		var xClient = new XmlRpcClient();
		var regRequest = new XmlRpcRequest(WEBSAMP_PREFIX + "register");
		var securityInfo = {"samp.name": appName};
		regRequest.addParam(securityInfo);
		regRequest.checkParams([TYPE_MAP]);
		/*
		 * We consider first the the Hub is running. That avoid multiple 
		 * connection attemps while user aggreement popup windos is open
		 */
		noHub = false;

		var resultHandler = function(result) {
			var conn;
			var e;
			try {
				conn = new Connection(result, 1000);
			}
			catch (e) {
				errorHandler(e);
				return;
			}
			connectionHandler(conn);
		};
		xClient.execute(regRequest, resultHandler, errorHandler);
	};

	// Calls the hub ping method once.  It is not necessary to be
	// registered to do this.
	// The supplied pingHandler function is called with a boolean argument:
	// true if a (web profile) hub is running, false if not.
	var ping = function(pingHandler) {
		var xClient = new XmlRpcClient();
		var pingRequest = new XmlRpcRequest(WEBSAMP_PREFIX + "ping");
		var resultHandler = function(result) {
			pingHandler(true);
		};
		var errorHandler = function(error) {
			pingHandler(false);
		};
		xClient.execute(pingRequest, resultHandler, errorHandler);
	};

	// =====================================================================================================================================


	/******** imoprte de essai.html, a nettoyer ***********/

	var tracker = new ClientTracker();
	var callHandler = tracker.callHandler;
	callHandler["samp.app.ping"] = function(senderId, message, isCall) {
		if (isCall) {
			return {text: "ping to you, " + tracker.getName(senderId)};
		}
	};
	tracker.onchange = function(id, type, data) {
		notifyTrackerReply(id, type, data);
	};



	subs = tracker.calculateSubscriptions();
	//subs = {"*": {}};

	logCc = {
			receiveNotification: function(senderId, message) {
				var handled = tracker.receiveNotification(senderId, message);
				logCallback("notification: " + message["samp.mtype"] +
						" from " + tracker.getName(senderId), handled);
			},
			receiveCall: function(senderId, msgId, message) {
				var handled = tracker.receiveCall(senderId, msgId, message);
				logCallback("call: " + message["samp.mtype"] +
						" from " + tracker.getName(senderId), handled);
			},
			receiveResponse: function(responderId, msgTag, response) {
				var handled = tracker.receiveResponse(responderId, msgTag, response);
				logCallback("response: " + msgTag +
						" from " + tracker.getName(responderId), handled);
			},
			init: function(connection) {
				tracker.init(connection);
//				connector.connection.call([id, tag, msg], null, doneFunc);
//				var createTag = (function() {
//				var count = 0;
//				return function() {
//				return "t-" + ++count;
//				};
//				})();

			}
	};

	var connector = new Connector(meta["samp.name"], meta, logCc, subs);
	connector.onunreg = function() {
		document.getElementById("clientList").innerHTML = "";
	};

	function sendMessageToClient(target, message) {
		if( target == null || target.match(/any/i) ) {
			connector.connection.notifyAll([message]);
		} else {
			connector.connection.notify([target, message]);
		}
	}
	// Controler notification
	var notifyTrackerReply = function(id, type, data) {
		listener.controlTrackerReply(id, type, data);
	};
	var notifyHubError = function(message) {
		noHub = true;
		Out.debug("notifyHubError :" + message);
		listener.controlHubError(message);
	};

	/* Exports. */
	var jss = {};
	jss.XmlRpcRequest = XmlRpcRequest;
	jss.XmlRpcClient = XmlRpcClient;
	jss.Message = Message;
	jss.TYPE_STRING = TYPE_STRING;
	jss.TYPE_LIST = TYPE_LIST;
	jss.TYPE_MAP = TYPE_MAP;
	jss.connector = connector;
	jss.isSubscribed = isSubscribed;
	jss.Connector = Connector;
	jss.CallableClient = CallableClient;
	jss.ClientTracker = ClientTracker;

	jss.addListener = addListener;
	jss.registerToHub = function() {
		if( !connector.connection )  {
			if( noHub ) 
				connector.register();
		} else {
			Out.info("Attempt to connect, but already connected");
		}
		return {HubRunning: !noHub};    	
	};
	jss.unregisterToHub    = function() {connector.unregister();
										/*
										 * Givee a delay to the last XHTTP request to complete
										 */
	                                     setTimeout(function() {noHub = true;}, 2000);};
	jss.isConnected        = function() {return ((connector.connection)? true: false);};
	jss.notifyTrackerReply = notifyTrackerReply;
	jss.notifyHubError = notifyHubError;
	jss.sendMessageToClient= sendMessageToClient;

	return jss;
};

console.log('=============== >  WebSamp_m.js ');


WebSamp_mVc = function() {
	var that = this;
	/**
	 * who is listening to us?
	 */
	var listener = null;
	/**
	 * add a listener to this view
	 */
	var addListener = function(list) {
		listener = list;
	};
	/*
	 * used to transform local URLs in full URLs
	 */
	//if (typeof rootUrl != 'undefined') 
	var rootUrl = "http://" + window.location.hostname +  (location.port?":"+location.port:"") + "/" + window.location.pathname.split( '/' )[1] + "/";
	/*
	 * list of connected clients updated by model notifications
	 */
	var sampClients = new Object(); 
	
	var hubs = new Array();
	hubs["topcat"] = {
			webStartUrl : "http://www.star.bris.ac.uk/~mbt/topcat/topcat-full.jnlp",
			iconUrl : "http://www.star.bris.ac.uk/~mbt/topcat/tc3.gif",
			webUrl : "http://www.star.bris.ac.uk/~mbt/topcat/",
			description : "Tool for Operations on Catalogues And Tables"
	};
	hubs["aladin"] = {
			webStartUrl : "http://aladin.u-strasbg.fr/java/nph-aladin.pl?frame=get&id=aladin.jnlp",
			iconUrl : "http://aladin.u-strasbg.fr/aladin_large.gif",
			webUrl : "http://aladin.u-strasbg.fr/",
			description : "The Aladin sky atlas and VO Portal"
	};
	/*
	 * Internal flags
	 */
	var waitForHub = false;
	var modalOpen = false;
	var requete = new Object();
	/*
	 * Initiate the div receiving any information from SAMP model
	 */
	var sampModalId = "sampModalId";
	/*
	 * Connection mode: avoid to open the send messaqe window when the user just want to connect the hud
	 */
	var JUSTCONNECT = 1;
	var SENDDATA    = 2;
	var SLEEPING    = 0;
	var mode        = SLEEPING;
	/*
	 * Avoid flooding the hub with multiple connection attemps
	 */
	var lastAttempt = Date.now();
	var REGISTERDELAY = 2000;
	var NOHUB_REGEXP = new RegExp(/no\s+hub/i);
	/*
	 * Methods handling the content of the modal box
	 */
	var getElement = function(eleSuffix) {
		/*
		 * Make sure we are working on the div attached to this view. Its content
		 * is duplicated in the dialog panel and the selectors are lost, 2 hours waste
		 */
		return $('#' + sampModalId + 'Container #'  + sampModalId + eleSuffix);
	};
	var setLoadIcon = function() {
		Processing.show("Talking with the HUB");
//		$('#' + sampModalId + 'Icon').attr("src",
//		"images/ajax-loader.gif");
	};
	var setIvoaIcon = function() {
		Processing.hide();
//		$('#' + sampModalId + 'Icon')
//		.attr("src", "images/ivoa.jpg");
	};
	var setTitle = function(title) {
		getElement('Title ').text(title);
	};
	var setHelp = function(text) {
		getElement('Help').html(text);
	};
	var setPostHelp = function(text) {
		getElement('PostHelp').html(text);
	};
	var addItem = function(text) {
		getElement('ItemList').append(
				"<li>" + text + "</li>");
	};
	var removeAllItems = function() {
		getElement('ItemList').empty();
	};
	var openModalWithClose = function() {
		if (modalOpen == false) {
			modalOpen = true;			
		}
		if( mode != SLEEPING){
		Modalinfo.dataPanel("Samp Info"
				, getElement('').html()
				, function() {
					WebSamp_mVc.fireCloseModal();
				});}
	};
	/*************************************************************************************************
	 * Information display out (private)
	 */
	var makeUrlAbsolute = function (url){
		if( !url.match(/https?:\/\// ) ) {
			return rootUrl + "/" + requete.param;
		} else {
			return url;
		}
	} ;
	var showConnectionOff = function() {
		$("#sampIndicator").attr("class", "sampOff");
	};
	var showConnectionOn = function() {
		$("#sampIndicator").attr("class", "sampOn");
	};
	var showClientList = function(callback) {
		Out.debug("showClientList");
		setIvoaIcon();
		setTitle("Available SAMP Clients");
		setHelp('Below is the list of SAMP clients accepting data<br>\n'
				+ '- Click on the icon of the client you want to send data.<BR>'
				+ '- Click on the broadcast icon if you want your data to be sent to all clients.');
		setPostHelp('');
		removeAllItems();				
		var callback = (requete.type == "oid" )     ? "WebSamp_mVc.fireSendFileToClient"
				      :(requete.type == "voreport") ? "WebSamp_mVc.fireSendUrlToClient"
					  :(requete.type == "skyat")    ? "WebSamp_mVc.fireSendSkyatToClient"
					  :(requete.type == "script")   ? "fireSendAladinScript"
					  : "";
		var found = false;
		for (ident in sampClients) {
			found = true;
			if (sampClients[ident].meta && sampClients[ident].subs) {
				var meta = sampClients[ident].meta;

				var onclick = (callback == "")? "Modalinfo.info(&quot;No message to send&quot;);"
						: callback + "(\"" +  ident + "\");";
				addItem("<img class=clickableicon align=bottom style='height: 32px; border: 0px;' src='"
						+ meta["samp.icon.url"]
						+ "' onclick='" + onclick + "'>"
						+ "<span class=help> <b>"
						+ meta["samp.name"]
						+ "</b> "
						+ meta["samp.description.text"]
						+ " </span><a style='font-color: blue; font-size: small; font-style: italic;' target=_blank href='"
						+ meta["home.page"] + "'>read more...</a>");
			}
		}
		if (found) {
			addItem("<a class=sampOn "
					+ " onclick='" + callback + "(null);'></a>"
					+ "<span class=help> <b>Broadcast</b> to any client");
			openModalWithClose();
		} else {
			fireCloseModal();
			Modalinfo.info("No SAMP Clients Available: Is your Hub still running?");			
			if (fireIsConnected()) showConnectionOff();

		}
	};
	var processOrShowClientList = function(callback) {
		var cpt=0;
		var loneIdent="";
		for (var ident in sampClients) {
			cpt++;
			loneIdent = ident;
		}
		if( cpt == 1) {
			if(requete.type == "oid" ){
				WebSamp_mVc.fireSendFileToClient(loneIdent);
			} else if(requete.type == "voreport") {
				WebSamp_mVc.fireSendUrlToClient(loneIdent);
			} else if( requete.type == "skyat") {
				WebSamp_mVc.fireSendSkyatToClient(loneIdent);
			} else if( requete.type == "script"){
				WebSamp_mVc.fireSendAladinScript(loneIdent);
			}
			return;
		}
		Out.debug("showClientList");
		setIvoaIcon();
		setTitle("Available SAMP Clients");
		setHelp('Below is the list of SAMP clients accepting data<br>\n'
				+ '- Click on the icon of the client you want to send data.<BR>'
				+ '- Click on the broadcast icon if you want your data to be sent to all clients.');
		setPostHelp('');
		removeAllItems();				
		var callback = (requete.type == "oid" )? "WebSamp_mVc.fireSendFileToClient"
			      :(requete.type == "voreport") ? "WebSamp_mVc.fireSendUrlToClient"
				  :(requete.type == "skyat") ?"WebSamp_mVc.fireSendSkyatToClient"
				  :(requete.type == "script") ?"fireSendAladinScript"
				  : "";
		var found = false;
		for (var ident in sampClients) {
			found = true;
			if (sampClients[ident].meta && sampClients[ident].subs) {
				var meta = sampClients[ident].meta;

				var onclick = (callback == "")? "Modalinfo.info(&quot;No message to send&quot;);"
						: callback + "(\"" +  ident + "\");";
				addItem("<img class=clickableicon align=bottom style='height: 32px; border: 0px;' src='"
						+ meta["samp.icon.url"]
						+ "' onclick='" + onclick + "'>"
						+ "<span class=help> <b>"
						+ meta["samp.name"]
						+ "</b> "
						+ meta["samp.description.text"]
						+ " </span><a style='font-color: blue; font-size: small; font-style: italic;' target=_blank href='"
						+ meta["home.page"] + "'>read more...</a>");
			}
		}
		if (found) {
			addItem("<a class=sampOn "
					+ " onclick='" + callback + "(null);'></a>"
					+ "<span class=help> <b>Broadcast</b> to any client");
			openModalWithClose();
		} else {
			fireCloseModal();
			Modalinfo.info("No SAMP Clients Available: Is your Hub still running?");			
			if (fireIsConnected()) showConnectionOff();

		}
	};

	var showHupLauncher = function() {
		Out.debug("showHupLauncher");
		setIvoaIcon();
		setTitle("No running SAMP hub detected");
		setHelp('You need to start a hub before to export data towards VO clients<br> \n'
				+ 'You can either run it by hand or by clicking on one icon below.<br>'
				+ 'If the applcation doesn\'t start, make sure that Java <a target="_blank" href="http://www.java.com/fr/download/faq/java_webstart.xml">Web Start</a> '
				+ 'is properly installed on your machine.');
		setPostHelp('IMPORTANT: Once the hub is running, click on the <span style="diplay: inline-block;width: 23px"><a href="#" class="dlivoalogo"></a>icon</span>  again to send data\n');
		removeAllItems();
		for ( var h in hubs) {
			var hub = hubs[h];
			addItem("<a href='#' onclick=WebSamp_mVc.fireLaunchHub('"
					+ h
					+ "','"
					+ hub.webStartUrl
					+ "');><img class=clickableicon align=middle style='height: 32px; border: 0ps;' src='"
					+ hub.iconUrl
					+ "'></a>"
					+ "<span class=help> <b>"
					+ h
					+ "</b> "
					+ hub.description
					+ " </span><a style='font-color: blue; font-size: small; font-style: italic;' target=_blank href='"
					+ hub.webUrl + "'>read more...</a>");
		}
		openModalWithClose();
	};

	/**************************************************************
	 * Methods handling messages (private)
	 */
	var fireRegisterToHub = function() {
		waitForHub = !listener.controlRegisterToHub().HubRunning;
	};

	/**************************************************************
	 * Methods invoked by the controller (public)
	 */		
	var fireCloseModal = function() {
		if( modalOpen ) {
			modalOpen = false;
			waitForHub = false;
			mode = SLEEPING;
			Modalinfo.close();
		}
	};
	var fireSendOid = function(oid, mtype, displayName) {
		Out.info("Send OID " + oid);
		mode = SENDDATA;
		requete = {type: "oid", param: oid};			
		if (!fireIsConnected()) {
			fireRegisterToHub();
		} else {
			var cpt=0;
			/*
			 * If more than one client: propose a choice window
			 */
			for (ident in sampClients) {
				cpt++;
				showClientList();
				return ;
			}
			showClientList();
		}
	};
	/*
	 * Closely bind to Saada
	 */
	var fireSendFileToClient = function(target) {
		mode = SENDDATA;
		var oid = requete.param;
		var url = rootUrl + "download?oid=" + oid;
		var urlinfo = "getproductinfo?url=" + encodeURIComponent(url);
		$.getJSON(urlinfo,function(data) {
			var ContentType = data.ContentType;
			var fileName = data.ContentDisposition;
			var results = /.*filename=(.*)$/.exec(data.ContentDisposition);
			if( results == null ) {
				fileName = "3XMMDataFile";
			} else {
				fileName = results[1].replace(/"/g, '');
			}
			var mtype = (ContentType.match(/fits/i) )? "table.load.fits": "table.load.votable";
			var message = new Object();
			message["samp.mtype"] = mtype;
			message["samp.params"] = {'table-id': oid, url:url, name: fileName};
			Processing.showAndHide("File sent to SAMP "+  JSON.stringify(message));
			listener.controlSendFileToClient(target, message); 
			fireCloseModal();
		});
	};
	var fireSendAladinScript = function(script) {		
		if (!fireIsConnected()) {
			fireRegisterToHub();
		} else {
			requete = {type: "script", param: script};			
			mode = SENDDATA;
			var mtype = "script.aladin.send";
			var message = new Object();
			message["samp.mtype"] = mtype;
			message["samp.params"] = {"script": script};
			Processing.showAndHide("Script sent to Aladin "+  JSON.stringify(message));
			listener.controlSendFileToClient(null, message); 
			fireCloseModal();
		}

	};
	var fireSendVoreport = function(reportUrl, mtype, name) {
		mode = SENDDATA;
		requete = {type: "voreport"
			, param: reportUrl
			, mtype: (mtype == null)?null: mtype
			, name: (name == null)?null: name};			
		Out.info("Send report" + JSON.stringify(requete));
		if (!fireIsConnected()) {
			fireRegisterToHub();
		} else {
			var cpt=0;
			/*
			 * If more than one client: propose a choice window
			 */
			for (ident in sampClients) {
				cpt++;
			}
			if( cpt > 1){
				showClientList();
			} else {
				fireSendUrlToClient(null);
			}
		}
	};
	var fireSendUrlToClient = function(target) {
		mode = SENDDATA;
		var reportUrl = makeUrlAbsolute(requete.param);

		var message = new Object();
		var rn ;
		if( requete.name == null ) {
			if( requete.param.match(/.*\.fit.*/i) ) {
				rn = "table.load.fits";
			} else {
				rn = "table.load.votable";
			}
		} else{
			rn = requete.name;
		}
		message["samp.mtype"] = (requete.mtype == null)? "table.load.votable": requete.mtype;
		message["samp.params"] = {
				'table-id': rn
				, url:reportUrl
				, name: rn};
		Processing.showAndHide("URL sent to SAMP " +  JSON.stringify(message).replace(/,/g, ",<br>"));
		listener.controlSendUrlToClient(target, message); 
		fireCloseModal();
	};

	var fireSendSkyat = function(ra, dec) {
		mode = SENDDATA;
		Out.info("Send SkyAt " + ra + " " +  dec );
		requete = {type: "skyat", param: {ra: ra, dec: dec}};			
		if (!fireIsConnected()) {
			fireRegisterToHub();
		} else {
			var cpt=0;
			for (ident in sampClients) {
				cpt++;
			}
			if( cpt > 1){
				showClientList();	
			} else {
				fireSendSkyatToClient(null);
			}
		}
	};	
	var fireSendSkyatToClient = function(target) {
		mode = SENDDATA;
		Out.info("Send SkyAt to Client " + target );
		var message = new Object();
		message["samp.mtype"] = "coord.pointAt.sky";
		message["samp.params"] = {ra: String(requete.param.ra), dec: String(requete.param.dec)};
		Processing.showAndHide("Sky position sent to SAMP");
		listener.controlSkyatToClient(target, message);
		fireCloseModal();
	};
	var fireRegisterToHubAttempt = function() {
		var t = Date.now();
		if( (t - lastAttempt) < REGISTERDELAY ) {
			return
		}
		if (mode != SLEEPING && waitForHub && modalOpen ) {
			Out.debug("attempt " + waitForHub );
			fireRegisterToHub();
			lastAttempt = t;
			setTimeout("WebSamp_mVc.fireRegisterToHubAttempt();", REGISTERDELAY);
		} else {
			waitForHub = false;
			if( mode == JUSTCONNECT) {
				fireCloseModal();				
			}
		}
	};
	var fireUnregister = function() {
		if (fireIsConnected()) {
			Out.debug("unregister");
			listener.controlUnregisterToHub();
			showConnectionOff();
		} else {
			Modalinfo.info("Not registered");
		}
	};
	var fireJustConnect = function() {
		mode = JUSTCONNECT;
		if (!fireIsConnected()) {
			fireRegisterToHub();
//			if (!fireIsConnected()) {
//			showHupLauncher();
//			}

		} else {
			fireUnregister();
		}
	};
	var fireIsConnected = function() {
		var retour = listener.controlIsConnected();
		if (retour) {
			showConnectionOn();
		} else {
			showConnectionOff();
		}
		return retour;
	};
	var fireIsHubRunning = function() {
		listener.controlIsHubRunning();
	};
	var showTrackerReply = function(id, action, data) {
		Out.debug("showTrackerReply " + id + " " +action);
		waitForHub = false;
		showConnectionOn();
		var removed = "";
		var completed = "";
		// Hub is not considered as a SAMP client
		if (!id || id.match(/hub/i)) {
			return;
		}
		if (action == "unregister") {
			if (sampClients[id]) {
				delete sampClients[id];
				removed = id;
			}
		} else {
			if (!sampClients[id]) {
				sampClients[id] = new Object();
			}
			if (action == "meta") {
				sampClients[id].meta = data;
			} 
			// Only accept to deal with clients accepting VOtable or FITS
			else if (action == "subs") {
				if (data["table.load.fits"] && data["table.load.votable"] &&  data["coord.pointAt.sky"] ) {
					sampClients[id].subs = data;
				} else {
					delete sampClients[id];
				}
			}
			if (sampClients[id] && sampClients[id].meta && sampClients[id].subs)
				completed = id;
		}
		/*
		 * Display the client list only if it has changed and if its
		 * content is complete
		 */
		if (mode == SENDDATA && (removed != "" || completed != "") ) {
			showClientList();
		}
	};
	var showHubError = function(message) {
		Out.debug("showHubError " + message + " " + NOHUB_REGEXP.test(message));
		if( NOHUB_REGEXP.test(message) == false) {
			return
		}
		waitForHub = true;
		showConnectionOff();
		setIvoaIcon();
		showHupLauncher();
		setTimeout(WebSamp_mVc.fireRegisterToHubAttempt, REGISTERDELAY);
	};		
	var fireLaunchHub = function(name, url) {
		setLoadIcon();
		PageLocation.changeLocation(url);
		setIvoaIcon();
	};
	var init = function(name, url, description){
		/*
		 * Must be done from $.ready, once  document.body is complete 
		 */
		$(document.body).append(
				"<div id=" + sampModalId + "Container>"
				+ "<div id="
				+ sampModalId
				+ " style='width: 99%; display: none;'>"
				+ '<a id="' // dummy a just to have the image referenced by the css
				+ sampModalId
				+ 'Icon" class="ivoalogo"></a>'
				+ '<span id='
				+ sampModalId
				+ 'Title style="display: inline; font-size: 1.5em;font-weight: bold;"> TITLE</span><BR>'
				+ '<div><ul id=sampClientListItems></ul></div>'
				+ '<span id=' + sampModalId
				+ 'Help class=help></span>'
				+ '<ul id=' + sampModalId
				+ 'ItemList></ul>' + '<span id='
				+ sampModalId
				+ 'PostHelp class=help></span>'
				+ "</div>"
				+ "</div>"
		);
		new WebSamp_mvC(WebSamp_mVc, new WebSamp_Mvc(name, url, description));	
	};

	/* Exports. */
	var jss = {};
	jss.init = init;
	jss.fireCloseModal = fireCloseModal;
	jss.fireSendOid = fireSendOid; //
	jss.fireSendFileToClient = fireSendFileToClient;
	jss.fireSendVoreport = fireSendVoreport;
	jss.fireSendUrlToClient = fireSendUrlToClient;
	jss.fireSendAladinScript = fireSendAladinScript;
	jss.fireSendSkyat = fireSendSkyat; //
	jss.fireSendSkyatToClient = fireSendSkyatToClient;
	jss.fireRegisterToHubAttempt = fireRegisterToHubAttempt;//
	jss.fireUnregister = fireUnregister; //
	jss.fireJustConnect = fireJustConnect;
	jss.fireIsConnected = fireIsConnected; //
	jss.fireIsHubRunning = fireIsHubRunning;//
	jss.showTrackerReply = showTrackerReply; //
	jss.showHubError = showHubError;
	jss.fireLaunchHub = fireLaunchHub;
	jss.addListener = addListener;
	return jss;
}();


console.log('=============== >  WebSamp_v.js ');


WebSamp_mvC = function(view,model){
	/**
	 * listen to the view
	 */

	var vlist = {
			controlIsConnected: function(){
				return model.isConnected();
			},
			controlRegisterToHub: function(){
				return  model.registerToHub();
			},
			controlUnregisterToHub: function(){
				return model.unregisterToHub();
			},
			controlIsHubRunning: function(){
				model.isHubRunning();
			},
			controlSendFileToClient: function(target, message){
				model.sendMessageToClient(target, message);
			},
			controlSendUrlToClient: function(target, message){
				model.sendMessageToClient(target, message);
			},
			controlSkyatToClient: function(target, message){
				model.sendMessageToClient(target, message);
			}
	};

	view.addListener(vlist);

	var mlist = {
			controlTrackerReply: function(id, type, data){
				view.showTrackerReply(id, type, data);
			},
			controlHubError: function(message){
				view.showHubError(message);
			},
			isInit : function(attributesHandlers){
				view.displayTable(attributesHandlers);
			}
	};

	model.addListener(mlist);
};

console.log('=============== >  WebSamp_c.js ');

/**
 * KWConstraint_Mvc: Model of individual KW editor
 * Parameters are received as a Javascript object with the following field:
 * - this.isFirst : NO AND/OR prepended if true
 * - this.attributeHandler: Attribute Handler attached to this MVC
 * - this.editorModel: ConstQEditor_Mvc instance owning this object
 * - defValue: Default this.operand to be applied to the constraint
 */
function KWSimpleConstraint_Mvc(params){

	this.listener = null;
	this.operators = new Array();
	this.range = {type: "not set", values: []};
	this.andors = new Array();

	/*
	 * Parameters decoding
	 */
	this.attributeHandler = params.attributeHandler;
	this.editorModel  = params.editorModel;
	this.defaultValue = params.defValue;
	this.isFirst      = params.isFirst;

	this.operator= '' ;
	this.operand = '';
	this.andor = '';
	this.booleansupported = false;
};

KWSimpleConstraint_Mvc.prototype = {
		addListener: function(list){
			this.listener = list;
		},
		processEnterEvent: function() {
			if( this.editorModel != null ) this.editorModel.updateQuery();
		},
		processRemoveConstRef: function(ahname) {
			if( this.editorModel != null ) this.editorModel.processRemoveConstRef(ahname);
		},
		removeAndOr: function() {
			this.andor = "";
		},
		notifyInitDone: function(){
			this.listener.isInit(this.attributeHandler, this.operators ,this.andors, this.range, this.defaultValue);
		},
		notifyTypoMsg: function(fault, msg) {
			this.fault = fault;
			this.listener.notifyFault(fault);
			if( this.editorModel != null ) this.editorModel.notifyTypoMsg(fault,msg);
		},		
		notifyRunQuery: function() {
			if( this.editorModel != null ) this.editorModel.notifyRunQuery();
		},
		getADQL: function(attQuoted) {
			return this.listener.controlAhName(this.attributeHandler);
		},
		getAttributeHandler: function() {
			return this.attributeHandler;
		},
		processRemoveFirstAndOr : function(key) {			
			if( this.attributeHandle == undefined  ||  this.attributeHandler.type != 'orderby') {
				if( this.editorModel != null ) this.editorModel.processRemoveFirstAndOr(key);
			}
		},
		isString: function(){
			return (this.attributeHandler.type != null)? this.attributeHandler.type.match(/(string)|(text)|(varchar)|char/i): false;
		}

};
/**
 * Simple constraint for TAP: just quote filed names to be compliant with ADQL
 * @param params
 * @returns {TapKWSimpleConstraint_mVc}
 */
function TapKWSimpleConstraint_Mvc(params){
	KWSimpleConstraint_Mvc.call(this, params);
}
TapKWSimpleConstraint_Mvc.prototype  = Object.create(KWSimpleConstraint_Mvc.prototype, {
	getADQL: {
		value : function() {
			return this.listener.controlAhName(this.attributeHandler).quotedTableName().qualifiedName;
		}
	}
});


/**
 * 
 * @param params
 * @returns
 */
function KWConstraint_Mvc(params){
	KWSimpleConstraint_Mvc.call(this, params);
	/*
	 * fault in constraint 
	 */
	this.fault = 0;

	if( this.attributeHandler.nameattr != null 
			&& (this.attributeHandler.nameattr == 'Cardinality' || this.attributeHandler.nameattr.startsWith('Qualifier ') 
					|| this.attributeHandler.nameorg.startsWith('Qualifier '))) {
		this.operators = ["=", "!=", ">", "<", "][", "]=[", "[]", "[=]"];			
		this.andors = [];
	} else if( this.attributeHandler.type == 'Select' ) {
		this.operators = [];
		this.andors = [];
	} else if( this.attributeHandler.type == 'ADQLPos' ) {
		this.operators = ["inCircle", "inBox"];
		this.andors = ["AND", "OR"];

	} else if( this.attributeHandler.type == 'boolean' ) {
		this.operators = ['=', 'IS NOT NULL', 'IS NULL'];
		this.andors = ['AND', 'OR'];
	} else if( !this.isString() ) {
		this.operators = ["=", "!=", ">", "<", "between", 'IS NOT NULL', 'IS NULL'];
		this.andors = ['AND', 'OR'];
	} else {
		this.operators = ["=", "!=", "LIKE", "NOT LIKE", 'IS NOT NULL' , 'IS NULL'];
		this.andors = ['AND', 'OR'];
	}

	if( this.isFirst == true ) {
		this.andors = [];
	}
	if( this.attributeHandler.type == 'orderby' ) {
		this.operators = [];
		this.andors = [];
	}

	if( this.attributeHandler.range != null ) {
		/*
		 * List of values
		 */
		this.range.type = "list";
		if (this.attributeHandler.range.values!=undefined){
			if(this.attributeHandler.range.values.length>0){
				for(var i=0; i<this.attributeHandler.range.values.length; i++){
					var choix = { value: this.attributeHandler.range.values[i] };
					this.range.values[this.range.values.length] = choix;
				}
			} else {
				var choix = { value: this.attributeHandler.range.values };
				this.range.values[this.range.values.length] = choix;
			}
			/*
			 * range of values
			 */
		} else {
			this.range.type = "range";
			if(this.attributeHandler.range.min!=undefined){
				var choix = { value: "min " + this.attributeHandler.range.min};
				this.range.values[this.range.values.length] =  choix;
			} 
			if (this.attributeHandler.range.max!=undefined){
				var choix = { value:  "max " +   this.attributeHandler.range.max};
				this.range.values[this.range.values.length] =choix;
			}
		}
	}

};

KWConstraint_Mvc.prototype  = Object.create(KWSimpleConstraint_Mvc.prototype, {
	processEnterEvent: {
		value : function(ao, op, opd, unit) {
			this.andor = ao;
			if( this.attributeHandler.type == 'orderby') {
				this.editorModel.updateQuery();
				return;
			} else if( this.isString() ) {
				if( !this.checkAndFormatString(op, opd) ) {
					this.editorModel.updateQuery();
					return;
				}
			} else if( this.attributeHandler.type == 'boolean') {
				if( !this.checkAndFormatString(op, opd) ) {
					this.editorModel.updateQuery();
					return;
				}
			} else {
				if( !this.checkAndFormatNum(op, opd) ) {
					this.editorModel.updateQuery();
					return;
				}			
			}
			this.notifyTypoMsg(0, this.operator + ' ' + this.operand);				
			if( this.andors.length == 0 ) {
				this.processRemoveFirstAndOr();
			}
			this.editorModel.updateQuery();
		}
	},
	checkAndFormatNum: {
		value : function(op, opd) {
			/*
			 * Case of select items in ADQL
			 */
			if( op == null || op.length == 0 ) {
				this.operator = "";
				this.operand = "";
				return 1 ;			
			}
			if( op == 'IS NULL' ) {
				this.operator = 'IS NULL';
				this.operand = '';
				return 1;								
			} else if( /^\s*$/.test(opd)  ) {
				if( this.attributeHandler.nameattr == 'Cardinality' || this.attributeHandler.nameattr.startsWith('Qualifier ') 
						|| this.attributeHandler.nameorg.startsWith('Qualifier ')) {
					this.notifyTypoMsg(1, 'Numeric operand required');
					return 0 ;
				} else {
					this.operator = 'IS NOT NULL';
					this.operand = '';
					return 1;
				}
			} else if( op == 'between' ) {
				var words = opd.split(' ') ;
				if( words.length != 3 || !/and/i.test(words[1]) ||
						words[0].length == 00 || words[2].length == 00 ||
						isNaN(words[0]) || isNaN(words[2]) ) {
					this.notifyTypoMsg(1, 'Operator "' + op + '" requires an operand of form "num1 and num2"');
					return 0 ;
				}
				this.operator = op;
				this.operand = words[0] + ' AND ' + words[2];						
				return 1 ;
			} else if( op == 'inCircle' || op == 'inBox')  {
				var area = null;
				if( this.attributeHandler.nameattr.startsWith('POSLIST:')) {
					area = opd.split(',');
					var n = this.attributeHandler.nameattr.replace("POSLIST:", "upload.");
					area = [n + ".pos_ra_csa", n + ".pos_dec_csa", area[area.length-1]];

				} else {
					area = opd.split(',');
					if( area.length != 3 || isNaN(area[0]) || isNaN(area[1]) )  {
						this.notifyTypoMsg(1, 'Search area must be like :alpha,delta,size"');					
						return 0 ;
					}
				}
				if(  isNaN(area[2]) ) {
					this.notifyTypoMsg(1, 'Search area must be like :alpha,delta,size"');					
					return 0 ;
				}
				if( op == 'inCircle') {
					this.operator = "CIRCLE('ICRS', " + area[0]+ ", " +area[1] + ", " + area[2]/60+ ")";
					this.operand = "";
				} else {
					this.operator = "BOX('ICRS', " + area[0]+ ", " +area[1] + ", " + area[2]+  ", " + area[2]/60  +")";
					this.operand = "";					
				}
				return 1 ;
			} else if( isNaN(opd) && this.attributeHandler.type != 'boolean' ) {
				this.notifyTypoMsg(1, 'Operator "' + op + '" requires a single numeric operand');
				return 0 ;
			} else {
				this.operator = op;
				this.operand = opd;
				return 1 ;			
			}
		}
	},
	checkAndFormatString: {
		value : function(op, opd) {
			if( op == 'IS NULL' ) {
				this.operator = 'IS NULL';
				this.operand = '';
				return 1;								
			} else if( /^\s*$/.test(opd)  ) {
				this.operator = 'IS NOT NULL';
				this.operand = '';
				return 1;				
			} else {
				if ( /^\s*'.*'\s*$/.test(opd)  ) {
					this.operand = opd;
				} else {
					this.operand = "'" + opd + "'";
				}
				this.operator = op;
				return 1;			
			}
		}
	},
	processRemoveConstRef: {
		value : function(ahname) {
			this.editorModel.processRemoveConstRef(ahname);
		}
	},
	removeAndOr: {
		value : function() {
			this.andor = "";
		}
	},
	getADQL: {
		value : function(attQuoted) {
			if( this.fault ) {
				return null;
			}
			if(  this.attributeHandler.nameattr.startsWith('Qualifier ')) {
				return 'Qualifier{ ' + this.attributeHandler.nameattr.split(' ')[1] + this.operator + ' ' + this.operand + '}';
			} else if( this.operator.startsWith('CIRCLE') || this.operator.startsWith('BOX'))  {
				//				CONTAINS(POINT('ICRS GEOCENTER', "_s_ra", "_s_dec"), BOX('ICRS GEOCENTER', 'dsa', 'dsad', 'dsa', 'dsad')) = 'true';
				var coordkw = this.attributeHandler.description.split(' ');
				var bcomp = ( this.booleansupported )? "'true'" :  "1";
				return this.andor + " CONTAINS(POINT('ICRS', " + coordkw[0].quotedTableName().qualifiedName + ", " +  coordkw[1].quotedTableName().qualifiedName + "), "
				+ this.operator + ") = " + bcomp;
			} else if( attQuoted ){ 
				return this.andor + ' "' + 	this.listener.controlAhName(this.attributeHandler) + '" ' + this.operator + ' ' + this.operand;
			} else {
				return this.andor + ' ' + this.listener.controlAhName(this.attributeHandler) + ' ' + this.operator + ' ' + this.operand;
			}
		}
	},
	

});

/**
 * Separate behaviour specific to Saada from those specific to TAP (must be continued)
 * @param params
 * @returns {TapKWConstraint_Mvc}
 */
function TapKWConstraint_Mvc(params){
	KWConstraint_Mvc.call(this, params);
}
TapKWConstraint_Mvc.prototype  = Object.create(KWConstraint_Mvc.prototype, {
	getADQL: {
		value : function(attQuoted) {
			if( this.fault ) {
				return null;
			}
			if( this.operator.startsWith('CIRCLE') || this.operator.startsWith('BOX'))  {
				//				CONTAINS(POINT('ICRS GEOCENTER', "_s_ra", "_s_dec"), BOX('ICRS GEOCENTER', 'dsa', 'dsad', 'dsa', 'dsad')) = 'true';
				var coordkw = this.attributeHandler.description.split(' ');
				var bcomp = ( this.booleansupported )? "'true'" :  "1";
				return this.andor + " CONTAINS(POINT('ICRS', " + coordkw[0].quotedTableName().qualifiedName + ", " +  coordkw[1].quotedTableName().qualifiedName + "), "
				+ this.operator + ") = " + bcomp;
			} else if( attQuoted ){ 
				return this.andor + ' "' + 	this.listener.controlAhName(this.attributeHandler).quotedTableName().qualifiedName + '" ' + this.operator + ' ' + this.operand;
			} else {
				return this.andor + ' ' + this.listener.controlAhName(this.attributeHandler).quotedTableName().qualifiedName + ' ' + this.operator + ' ' + this.operand;
			}
		}
	}

});


/**
 * Subclass of KWConstraint_Mvc: manage UCDS based constraint
 * @param params
 */
function UCDConstraint_Mvc(params){
	KWConstraint_Mvc.call(this, params);
	this.operators = ["=", "!=", ">", "<", "][", "]=[", "[]", "[=]"];
	this.unit = "";

};


UCDConstraint_Mvc.prototype = Object.create(KWConstraint_Mvc.prototype, {
	getADQL: {
		value: function(attQuoted) {
			if( this.fault ) {
				return null;
			} else {
				return "[" + this.attributeHandler.ucd + '] ' + this.operator + ' ' + this.operand + ' ' + this.unit;
			}
		}
	},
	processEnterEvent:  {
		value: function(ao, op, opd, unit) {
			this.andor = "AND";
			this.unit = (unit == null || unit == "")?"": ("[" + unit + "]");
			if( opd == "" ) { 
				this.notifyTypoMsg(1, 'Operand is required with UCDs');
				this.editorModel.updateQuery();
				return;
			} else if( op == '][' || op == ']=[' || op == '[]' || op == '[=]') {
				this.operator = op;
				var myArray = opd.match(/^((?:[^\s]+)|(?:'.*'))\s+and\s+((?:[^\s]+)|(?:'.*'))$/);
				if ( myArray != null ) {
					var o1 = myArray[1];
					var o2 = myArray[2];
					o1 = (isNaN(o1) && !/^\s*'.*'\s*$/.test(o1))? "'" + o1 + "'": o1;
					o2 = (isNaN(o2) && !/^\s*'.*'\s*$/.test(o2))? "'" + o2 + "'": o2;
					this.operand = '(' + o1 + ' , ' + o2 + ')';												
				}  else {
					this.notifyTypoMsg(1, 'The operator ' + op + ' requires an operand like "num1 and num2"');
					this.editorModel.updateQuery();
					return;
				}
			} else {
				this.operator = op;
				var o1 = (isNaN(opd) && !/^\s*'.*'\s*$/.test(opd))? "'" + opd + "'": opd;
				this.operand =  o1;												
			}

			this.notifyTypoMsg(0, this.operator + ' ' + this.operand + ' [' + unit + ']');				
			if( this.andors.length == 0 ) {
				this.processRemoveFirstAndOr();
			}
			this.editorModel.updateQuery();
		}
	}
});
/**
 * Subclass of KWConstraint_Mvc: manage position based constraint
 * @param params
 */
function PosConstraint_Mvc(params){
	KWConstraint_Mvc.call(this, params);
	this.operators = [];
};


PosConstraint_Mvc.prototype = Object.create(KWConstraint_Mvc.prototype, {
	processEnterEvent:  {
		value: function(ao, op, opd) {
			this.andor = "AND";
			if( opd == "" ) {
				this.operator = 'IS NOT NULL';
			} else if( op == '][' || op == ']=[' || op == '[]' || op == '[=]') {
				this.operator = op;
				var myArray = opd.match(/^((?:[^\s]+)|(?:'.*'))\s+and\s+((?:[^\s]+)|(?:'.*'))$/);
				if ( myArray != null ) {
					var o1 = myArray[1];
					var o2 = myArray[2];
					o1 = (isNaN(o1) && !/^\s*'.*'\s*$/.test(o1))? "'" + o1 + "'": o1;
					o2 = (isNaN(o2) && !/^\s*'.*'\s*$/.test(o2))? "'" + o2 + "'": o2;
					this.operand = '(' + o1 + ' , ' + o2 + ')';												
				}  else {
					this.notifyTypoMsg(1, 'Operator ' + op + ' requires an operand like "num1 and num2"');
					this.editorModel.updateQuery();
					return;
				}
			} else {
				this.operator = op;
				var o1 = (isNaN(opd) && !/^\s*'.*'\s*$/.test(opd))? "'" + opd + "'": opd;
				this.operand =  o1;												
			}
			var str = this.getADQL();
			str = (str.length > 48)? (str.substring(0, 47) + "..."): str;
			this.notifyTypoMsg(0, str);				
			if( this.andors.length == 0 ) {
				this.processRemoveFirstAndOr();
			}
			this.editorModel.updateQuery();
		}
	},
	getADQL: {
		value: function() {
			if( this.fault ) {
				return null;
			} 
			if( this.attributeHandler.nameattr == "region") {
				return 'isInRegion("' + this.attributeHandler.value + '", 0' 
				+ ', ' + ((this.attributeHandler.frame == "FK5")? "J2000":
					(this.attributeHandler.frame == "FK4")? "J1950": '-')
					+ ', ' + this.attributeHandler.frame + ')';

			} else {
				return 'isInCircle("' + this.attributeHandler.nameorg + '", ' 
				+ this.attributeHandler.radius  
				+ ', ' + ((this.attributeHandler.frame == "FK5")? "J2000":
					(this.attributeHandler.frame == "FK4")? "J1950": '-')
					+ ', ' + this.attributeHandler.frame + ')';
			}
		}
	}

});

/**
 * Subclass of KWConstraint_Mvc: manages catalogue based constraint
 * @param params
 */
function CatalogueConstraint_Mvc(params){
	KWConstraint_Mvc.call(this, params);
	this.qualifier = params.qualifier;
	this.operators = [">", "<", "][", "[]"];
};


CatalogueConstraint_Mvc.prototype = Object.create(KWConstraint_Mvc.prototype, {
	getADQL: {
		value: function(attQuoted) {
			if( this.fault ) {
				return null;
			}
			var retour = "   AssObjClass{" + this.attributeHandler.CLASSNAME + "}";
			if( this.operand != "" ) {
				retour += ", Qualifier{" + this.qualifier +  ' ' + this.operator + ' ' + this.operand + "}";
			}
			return retour;
		}
	},
	processEnterEvent:  {
		value: function(ao, op, opd, unit) {
			/*
			 * No parameter: constraint to be removed
			 */
			if( ao == undefined && op == undefined && opd == undefined) {
				this.editorModel.updateQuery();				
			}
			this.andor = "AND";
			this.unit = (unit == null || unit == "")?"": ("[" + unit + "]");
			if( opd == "" ) { 
				this.notifyTypoMsg(0, 'at any distance');
			} else if( op == ']['  || op == '[]' ) {
				this.operator = op;
				var myArray = opd.match(/^\s*([^\s]+)\s+and\s+([^\s]+)\s*$/);
				if ( myArray != null ) {
					var o1 = myArray[1];
					var o2 = myArray[2];
					if(isNaN(o1) || isNaN(o1) ) {
						this.notifyTypoMsg(1, 'Distance requires a single operands');
						return;
					}
					this.operand = '(' + o1 + ' , ' + o2 + ')';												
					this.notifyTypoMsg(0, this.operator + ' ' + this.operand );				
				}  else {
					this.notifyTypoMsg(1, 'The operator ' + op + ' requires an operand like "num1 and num2"');
					return;
				}
			} else if( isNaN(opd) ) {
				this.notifyTypoMsg(1, 'Distance requires a single numeric operand');
				return ;
			} else {
				this.operator = op;
				this.operand =  opd;												
				this.notifyTypoMsg(0, this.operator + ' ' + this.operand );				
			}
			this.editorModel.updateQuery();
		}
	}
});

/**
 * Subclass of KWConstraint_Mvc: manages catalogue based constraint
 * @param params
 */
function CrossidConstraint_Mvc(params){
	CatalogueConstraint_Mvc.call(this, params);
};


CrossidConstraint_Mvc.prototype = Object.create(CatalogueConstraint_Mvc.prototype, {
	processEnterEvent:  {
		value: function(ao, op, opd, unit) {
			/*
			 * No parameter: constraint to be removed
			 */
			if( ao == undefined && op == undefined && opd == undefined) {
				this.editorModel.updateQuery();				
			}
			this.andor = "AND";
			this.unit = (unit == null || unit == "")?"": ("[" + unit + "]");
			if( opd == "" ) { 
				this.notifyTypoMsg(0, 'with any probality');
			} else if( op == ']['  || op == '[]' ) {
				this.operator = op;
				var myArray = opd.match(/^\s*([^\s]+)\s+and\s+([^\s]+)\s*$/);
				if ( myArray != null ) {
					var o1 = myArray[1];
					var o2 = myArray[2];
					if(isNaN(o1) || isNaN(o1) ) {
						this.notifyTypoMsg(1, 'Distance requires a single operands');
						return;
					} else if( o1 <0 || o1 > 1 || o2 < 0 || o2 > 1 ){
						this.notifyTypoMsg(1, 'Probability must be between 0 and 1');
						return ;
					}
					this.operand = '(' + o1 + ' , ' + o2 + ')';												
					this.notifyTypoMsg(0, this.operator + ' ' + this.operand );				
				}  else {
					this.notifyTypoMsg(1, 'The operator ' + op + ' requires an operand like "num1 and num2"');
					return;
				}
			} else if( isNaN(opd) ) {
				this.notifyTypoMsg(1, 'Probability requires a single numeric operand between 0 and 1');
				return ;
			} else if( opd <0 || opd > 1){
				this.notifyTypoMsg(1, 'Probability must be between 0 and 1');
				return ;
			} else {
				this.operator = op;
				this.operand =  opd;												
				this.notifyTypoMsg(0, this.operator + ' ' + this.operand );				
			}
			this.editorModel.updateQuery();
		}
	}
});



console.log('=============== >  KWConstraint_m.js ');

function KWSimpleConstraint_mVc(params){
	/*
	 * Params decoding
	 */
	this.rootId = params.divId;
	this.constListId = params.constListId;		
	this.treePath = params.treePath; // facultatif
	this.fieldName = "";
	this.defValue = params.defValue;
	this.name = params.name
	/*
	 * who is listening to us?
	 */
	this.listener = null;
	/*
	 * Connect to the controler and to the model
	 */
	new KWConstraint_mvC(new KWSimpleConstraint_Mvc(params), this);	
}

KWSimpleConstraint_mVc.prototype = {
		addListener : function(list){
			this.listener = list;
		},
		
		fireInit : function() {
			this.initForm();
		},

		initForm : function(ah, operators ,andors,range,default_value){
			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id=" + this.rootId + " style='overflow: hidden;'>");
			var baseSelector  = '#' + this.constListId + ' div[id="' + this.rootId + '"]';
			var rootDiv = $(baseSelector);		

			rootDiv.append('&nbsp;<a id="' + this.rootId + '_close" href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>&nbsp;');
			rootDiv.append('<span style="cursor: pointer;" id="' + this.rootId + '_name" style="float: left;">' + this.name + '</span>&nbsp;');

			$("#" + this.rootId + " span").click(function(){
				Modalinfo.info(that.defValue, "Pattern statement");
			})
			if(this.rootId.endsWith("_rafield") || this.rootId.endsWith("_decfield")) {
				$(".kwConstraint#"+this.rootId).css("display","inline");
			}

			$('#' + this.constListId).append("</div>");	

			var closeInput = $('#' + this.constListId + ' a[id="' + this.rootId + '_close"]');

			closeInput.click(function() {
				rootDiv.remove();
				that.fireRemoveFirstAndOr(this.rootId);
				that.fireEnterEvent();
			});
			//this.fireEnterEvent(null, null, null);
		},
		fireGetADQL : function(attQuoted){
			return this.defValue;
		},	
		fireEnterEvent : function(andor, operator, operand, unit){
			var valInput   = $('#' + this.constListId + ' input[id="' + this.rootId + '_val"]');
			this.listener.controlEnterEvent(andor, operator, operand, unit);
			valInput.parents(".constdiv:first").find("span").each(function(){
				$(this).css("font-weight", "normal");
			});		
			valInput.prevAll("span[id$='_name']").css("font-weight", "bold" );
			if ($('#' + this.constListId).closest("fieldset").next("div").find("span:last").css("color") === 'rgb(0, 128, 0)') {
				$('#' + this.constListId).closest("fieldset").next("div").find("span:last").prepend(valInput.prevAll("span[id$='_name']").text()+" ");
			}
		},
		fireRemoveAndOr :  function() {
			$('#' + this.rootId + "_andor" ).remove();
			this.listener.controlRemoveAndOr(this.rootId);
		},
		fireRemoveFirstAndOr : function(){
			this.listener.controlRemoveFirstAndOr(this.rootId);
		},
		getAttributeHandler : function(){
			return this.listener.controlGetAttributeHandler();
		},
		getAhName: function(ah){
			var name = "";
			if( this.treePath != undefined && !ah.nameattr.startsWith("POSLIST") ){
				name =  this.treePath.schema + "." + this.treePath.table + "." + ah.nameorg ;
			} else {
				name =  ah.nameattr ;
			}
			this.fieldName = name;
			return this.fieldName;
		}
};

/**
 * Simple constraint for TAP: just quote filed names to be compliant with ADQL
 * @param params
 * @returns {TapKWSimpleConstraint_mVc}
 */
function TapKWSimpleConstraint_mVc(params){
	KWSimpleConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new TapKWSimpleConstraint_Mvc(params), this);	
}
TapKWSimpleConstraint_mVc.prototype = Object.create(KWSimpleConstraint_mVc.prototype, {});

/**
 * KWConstraint_mVc: View of the KW constraint editor 
 * Parameters are received as a Javascript object with the following field:
 * - divId      : ID of the div containing this constraint. 
 *                Given from outside to avoid conflict with JQuery selectors
 * - constListId: ID of the div containing the list of constraints
 * The following parameters are used by the model:
 *  - isFirst : NO AND/OR prepended if true
 * - attributeHandler: Attribute Handler attached to this MVC
 * - editorModel: ConstQEditor_Mvc instance owning this object
 * - defValue: Default operand to be applied to the constraint

 * @returns {KWConstraint_mVc}
 */
function KWConstraint_mVc(params){
	KWSimpleConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new KWConstraint_Mvc(params), this);	
}

KWConstraint_mVc.prototype = Object.create(KWSimpleConstraint_mVc.prototype, {

	fireInit : {
		value: function() {
		this.listener.controlInit();
		}
	},

	initForm : {
		value: function(ah, operators ,andors,range,default_value){

			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id=" + this.rootId + " style='float: none;'>");
			var baseSelector  = '#' + this.constListId + ' div[id="' + this.rootId + '"]';
			var rootDiv = $(baseSelector);
			rootDiv.append('&nbsp;<a id="' + this.rootId + '_close" href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>&nbsp;');
			/*
			 * AND/OR operators
			 */
			if( andors.length > 0 ) {
				var select='<select id="' + this.rootId + '_andor" style="font-size: small;font-family: courier;\">';
				for( var i=0 ; i<andors.length; i++ ) {
					var op = andors[i];
					select += '<option value=' + op + '>' +op + '</option>';
				}	
				select += '</select>&nbsp;';
				rootDiv.append(select);
			}
			rootDiv.append('<span id="' + this.rootId + '_name">' + this.getAhName(ah) + '</span>&nbsp;');
			/*
			 * Logical operators
			 */
			if( operators.length > 0 ) {
				var select='<select id="' + this.rootId + '_op" style="font-size: small;font-family: courier;">';
				for( i=0 ; i<operators.length; i++ ) {
					var op = operators[i];
					var selected = '';
					if( op == '>' ) {
						op = '&gt;';
						if( ah.nameattr == 'Cardinality' ) {
							selected = 'selected';
						} 
					} else if( op == '<' ) {
						op = '&lt;';
					}
					select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
				}	
				if( range != undefined && range.type == "list" ){
					for( var v=0 ; v<range.values.length ; v++ ) {
						var txt = (ah.type == "String")? "'" + range.values[v].value + "'": range.values[v].value;
						select += '<option value="= ' + txt + '">= ' + txt + '</option>';
					}
				}
				select += '</select>';
				rootDiv.append(select);

				rootDiv.append('<input type=text id="' + this.rootId 
						+ '_val" class="inputvalue form-control input-sm" style="display: inline-block; height: 21px; width:100px; font-size: small;font-family: courier;" value="' 
						+ default_value + '">');
				if( range != undefined && range.values.length>0 ){
					this.loadRange( '#' + this.constListId + ' input[id="' + this.rootId + '_val"]',range);
				}
			}
			$('#' + this.constListId).append("</div>");	

			var opSelected = '#' + this.constListId      + ' select[id="' + this.rootId + '_op"] option:selected';
			var opInput    = $('#' + this.constListId    + ' select[id="' + this.rootId + '_op"]');
			var andorInput = $('#' + this.constListId    + ' select[id="' + this.rootId + '_andor"]');
			var andorInputOpt = $('#' + this.constListId + ' select[id="' + this.rootId + '_andor"] option:selected');
			var valInput   = $('#' + this.constListId    + ' input[id="' + this.rootId + '_val"]');
			var closeInput = $('#' + this.constListId    + ' a[id="' + this.rootId + '_close"]');
			closeInput.click(function() {
				rootDiv.remove();
				that.fireRemoveFirstAndOr(this.rootId);
				if( ah.nameattr.startsWith('Qualifier ') ||  ah.nameorg.startsWith('Qualifier ') || ah.nameattr.startsWith('Cardinality')) {
					that.fireRemoveConstRef(ah.nameattr); 
				}
				that.fireEnterEvent();
				that.fireTypoMsg(false, '');
			});
			opInput.change(function() {
				var v = this.value;
				var regex = /=\s+(.+)/;
				var results = regex.exec(v);
				if( results ) {	
					$('#' + that.constListId + ' select[id="' + that.rootId + '_op"] option[value="="]').prop('selected', true);
					valInput.val(results[1]);
				}
				var ao =  (andorInputOpt.length > 0)? andorInputOpt.text(): "";
				that.fireEnterEvent(ao
						, this.value
						, valInput.val());				
			});
			andorInput.change(function() {
				that.fireEnterEvent(this.value
						, $(opSelected).text()
						, valInput.val());				
			});
			valInput.keyup(function(event) {
				/*
				 * Run the query is CR is typed in a KW editor
				 */
				if (event.which == '13') {
					if( that.isConstraintOK() ) {
						that.fireRunQuery();
					} else {
						Modalinfo.error("Current contraint is not valid: cannot run the query");
					}
				} else {
					that.fireEnterEvent(that.getAndOr()
							, $(opSelected).text()
							, this.value);
				}
			});
			valInput.click(function(event) {
				that.fireEnterEvent(that.getAndOr()
						, $(opSelected).text()
						, this.value);

			});
			valInput.on('input', function(event) {
				that.fireEnterEvent(that.getAndOr()
						, $(opSelected).text()
						, this.value);			
			});
			this.fireEnterEvent(this.getAndOr()
					, $(opSelected).text()
					,valInput.val());
		}
	},
	fireGetADQL : function(attQuoted){
		return this.listener.controlGetADQL(attQuoted);
	},	
	/**
	 * Returns the andor flag if the selector does exist
	 */
	getAndOr:{
		value: function() {
			var ao
			if( (ao = $('#' + this.constListId + ' select[id="' + this.rootId + '_andor"] option:selected')).length > 0 ) {
				return ao.text();
			} else {
				return "";
			}
		}
	},
	isConstraintOK: {
		value: function(){		
			return ($('#' + this.constListId).parent().find("span.typomsg_ok:first").length != 0);
		}
	},
	loadRange : {
		value:  function(id,range) {
			var that = this;
			$(id).autocomplete({
				source: range.values,
				minLength: 0,
				select: function(event, ui) { 				
					var regex = /(?:(?:min)|(?:max))\s+(.+)/;
					var results = regex.exec(ui.item.value);
					if( results ) {
						ui.item.value = results[1];
					} 
					that.fireEnterEvent($('#' + that.constListId + ' select[id="' +  that.rootId + '_andor"] option:selected').text()
							, $('#' + that.constListId + ' select[id="' +  that.rootId + '_op"] option:selected').text()
							, ui.item.value);
				}
			});
		}
	},
	drawFault :  {
		value: function(fault){
			var d = $('#' + this.constListId + ' div[id="' + this.rootId + '"]');
			if( fault ) {
				d.addClass("background-error");
			} else {
				d.removeClass("background-error");
			}
		}
	},
	fireRemoveConstRef :  {
		value: function(ahname){
			this.listener.controlRemoveConstRef(ahname);
		}	
	},
	fireGetADQL :  {
		value: function(attQuoted){
			return this.listener.controlGetADQL(attQuoted);
		}
	},
	fireTypoMsg :  {
		value: function(fault, msg){
			this.listener.controlTypoMsg(fault, msg);
		}
	},
	fireRunQuery:  {
		value:  function() {
			this.listener.controlRunQuery();
		}
	}
});

/**
 * Subclass of KWConstraint_mVc modeling a UCD based constraint
 * @param params
 */
function UCDConstraint_mVc(params){
	KWConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new UCDConstraint_Mvc(params), this);	
};


UCDConstraint_mVc.prototype = Object.create(KWConstraint_mVc.prototype, {
	initForm : { 
		value: function(ah, operators ,andors,range,default_value){
			if( ah == null ) {
				Modalinfo.error("Attempt to init a UCDConstraitn with a attribute handler null");
				return;
			}
			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id='" + this.rootId + "' style='float: none;'>");
			var baseSelector  = '#' + this.constListId + ' div[id="' + this.rootId + '"]';
			var valSelector   = '#' + this.constListId + ' input[id="' + this.rootId + '_val"]';
			var rootDiv = $(baseSelector);
			rootDiv.append('&nbsp;<a id="' + this.rootId + '_close" href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>&nbsp;');
			rootDiv.append('<span id=' + this.rootId + '_name>' + ah.ucd + '</span><br>');
			/*
			 * Logical operators
			 */
			var select='<select id="' + this.rootId + '_op" style="font-size: small;font-family: courier;">';
			for( var i=0 ; i<operators.length; i++ ) {
				var op = operators[i];
				var selected = '';
				if( op == '>' ) {
					op = '&gt;';
				} else if( op == '<' ) {
					op = '&lt;';
				}
				select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
			}	
			if( range != undefined && range.type == "list" ){
				for( var v=0 ; v<range.values.length ; v++ ) {
					var txt = (ah.type == "String")? "'" + range.values[v].value + "'": range.values[v].value;
					select += '<option value="= ' + txt + '">= ' + txt + '</option>';
				}
			}
			select += '</select>';
			rootDiv.append(select);

			rootDiv.append('<input type=text id="' + this.rootId 
					+ '_val" class="inputvalue form-control input-sm" style="width:140px; display: inline-block; height: 21px; font-size: small;font-family: courier;" value="' 
					+ default_value + '">');
			if( range != undefined && range.values.length>0 ){
				this.loadRange(valSelector,range);
			}

			rootDiv.append('<input title="units" style="width:100px; margin:2px; display: inline-block; height: 21px;" type=text id="' + this.rootId  
					+ '_unit" class="inputvalue form-control input-sm"  style="font-size: small; font-family: courier;" value="' 
					+ supportedUnits[0].text + '">');
			var opSelected = '#' + this.constListId + ' select[id="' + this.rootId + '_op"] option:selected';
			var opInput    = $('#' + this.constListId + ' select[id="' + this.rootId + '_op"]');
			var unitInput  = $('#' + this.constListId + ' input[id="' + this.rootId + '_unit"]');
			var closeInput = $('#' + this.constListId + ' a[id="' + this.rootId + '_close"]');
			var valInput   = $(valSelector);

			var tab = new Array();
			for(var i=0; i<supportedUnits.length; i++){
				tab[tab.length]=supportedUnits[i].text;
			};

			unitInput.autocomplete({
				source: tab,
				minLength: 0,
				focus :function(event, ui) {
					that.fireEnterEvent('AND'
							, $(opSelected).text()
							, valInput.val()
							, ui.item.value);
				}

			});

			$('#' + this.constListId).append("</div>");	

			closeInput.click(function() {
				$(baseSelector).remove();
				that.fireRemoveFirstAndOr(this.rootId);
				that.fireEnterEvent();
				that.fireTypoMsg(false, '');
			});

			opInput.change(function() {
				var v = this.value;
				var regex = /=\s+(.+)/;
				var results = regex.exec(v);
				if( results ) {	
					$('#' + that.constListId + ' select[id="' + that.rootId + '_op"] option[value="="]').prop('selected', true);
					valInput.val(results[1]);
				}
				that.fireEnterEvent('AND'
						, this.value
						, valInput.val()
						, unitInput.val());				
			});
			valInput.keyup(function(event) {
				/*
				 * Run the query is CR is typed in a KW editor
				 */
				if (event.which == '13') {
					if( that.isConstraintOK() ) {
						that.fireRunQuery();
					} else {
						Modalinfo.error("Current contraint is not valid: can not run the query");
					}
				} else {
					that.fireEnterEvent('AND'
							, $(opSelected).text()
							, this.value
							, unitInput.val());
				}
			});
			valInput.click(function(event) {
				that.fireEnterEvent('AND'
						, $(opSelected).text()
						, this.value
						, unitInput.val()
				);
			});
			valInput.on('input', function(event) {
				that.fireEnterEvent('AND'
						, $(opSelected).text()
						, this.value
						, unitInput.val()
				);
			});
			unitInput.keyup(function(event) {
				that.fireEnterEvent('AND'
						, $(opSelected).text()
						, valInput.val()
						, this.value);
			});
			this.fireEnterEvent('AND'
					, $(opSelected).text()
					,valInput.val()
					,unitInput.val());
		}
	}
});

/**
 * Subclass of KWConstraint_mVc modeling a UCD based constraint
 * @param params
 */
function PosConstraint_mVc(params){
	KWConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new PosConstraint_Mvc(params), this);	
};

PosConstraint_mVc.prototype = Object.create(KWConstraint_mVc.prototype, {
	initForm : { 
		value: function(ah, operators ,andors,range,default_value){
			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id=" + this.rootId + " style='float: none;'>");
			var rootDiv = $('#' + this.constListId + ' #' + this.rootId);
			var str = this.fireGetADQL();
			str = (str.length > 48)? (str.substring(0, 47) + "..."): str;
			rootDiv.append('&nbsp;<a id=' + this.rootId + '_close href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>');
			rootDiv.append('<span id=' + this.rootId + '_name>' +  str + '</span>');

			$('#' + this.constListId).append("</div>");	

			$('#' + this.constListId + ' #' +  this.rootId + "_close").click(function() {
				that.drop();
			});
			this.fireEnterEvent($('#' + this.constListId + ' #' +  this.rootId + "_andor option:selected").text()
					,$('#' + this.constListId + ' #' +  this.rootId + "_op option:selected").text()
					,$('#' + this.constListId + ' #' +  this.rootId + "_val").val());
		}
	},
	/**
	 * Used to emulate the click on trail from outside
	 */
	drop : {
		value: function(){
			$('#' + this.constListId + ' #' +  this.rootId).remove();
			this.fireRemoveFirstAndOr(this.rootId);
			this.fireEnterEvent();
			this.fireTypoMsg(0, "");	
		}
	}
});

/**
 * Subclass of KWConstraint_mVc modeling a catalogue class based constraint
 * @param params
 */
function CatalogueConstraint_mVc(params){
	KWConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new  CatalogueConstraint_Mvc(params), this);	
};

CatalogueConstraint_mVc.prototype = Object.create(KWConstraint_mVc.prototype, {
	initForm : { 
		value: function(ah, operators ,andors,range,default_value){
			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id=" + this.rootId + " style='float: none;'>");
			var baseSelector  = '#' + this.constListId + ' div[id="' + this.rootId + '"]';
			var rootDiv = $(baseSelector);
			rootDiv.append('&nbsp;<a id="' + this.rootId + '_close" href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>&nbsp;');
			rootDiv.append('<span id="' + this.rootId + '_name">' + ah.ACDS_CATACRO + '</span>&nbsp;<span>at</span>&nbsp;');
			/*
			 * Logical operators
			 */
			if( operators.length > 0 ) {
				var select='<select id="' + this.rootId + '_op" style="font-size: small;font-family: courier;">';
				for( var i=0 ; i<operators.length; i++ ) {
					var op = operators[i];
					var selected = '';
					if( op == '>' ) {
						op = '&gt;';
					} else if( op == '<' ) {
						op = '&lt;';
						selected = 'selected';
					}
					select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
				}	
				select += '</select>';
				rootDiv.append(select);

				rootDiv.append('<input type=text id="' + this.rootId 
						+ '_val" class="inputvalue form-control input-sm" style="width:100px; font-size: small;font-family: courier; display: inline-block; height: 21px;" value="' 
						+ default_value + '"> <span>arcsec</span>');
			}
			$('#' + this.constListId).append("</div>");	

			var opselected = '#' + this.constListId + ' select[id="' + this.rootId + '_op"] option:selected';
			var opInput    = $('#' + this.constListId + ' select[id="' + this.rootId + '_op"]');
			var valInput   = $('#' + this.constListId + ' input[id="' + this.rootId + '_val"]');
			var closeInput = $('#' + this.constListId + ' a[id="' + this.rootId + '_close"]');

			closeInput.click(function() {
				rootDiv.remove();
				that.fireRemoveFirstAndOr(this.rootId);
				that.fireEnterEvent();
				that.fireTypoMsg(false, '');
			});
			opInput.change(function() {
				that.fireEnterEvent('AND'
						, this.value
						, valInput.val());				
			});
			valInput.keyup(function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			valInput.click(function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			valInput.on('input', function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			this.fireEnterEvent('AND'
					, $(opselected).text()
					,valInput.val());
		}
	}
});

/**
 * Subclass of KWConstraint_mVc modeling a constraint based on catalogue class 
 * with a proba of id 
 * @param params
 */
function CrossidConstraint_mVc(params){
	CatalogueConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new  CrossidConstraint_Mvc(params), this);	
};

CrossidConstraint_mVc.prototype = Object.create(CatalogueConstraint_mVc.prototype, {
	initForm : { 
		value: function(ah, operators ,andors,range,default_value){
			var that = this;
			$('#' + this.constListId).append("<div class='kwConstraint' id=" + this.rootId + " style='float: none;'>");
			var baseSelector  = '#' + this.constListId + ' div[id="' + this.rootId + '"]';
			var rootDiv = $(baseSelector);
			rootDiv.append('&nbsp;<a id="' + this.rootId + '_close" href="javascript:void(0);" class=closekw title="Remove this Constraint"></a>&nbsp;');
			rootDiv.append('<span id="' + this.rootId + '_name">' + ah.ACDS_CATACRO + '</span>&nbsp;<span>identfied with a proba</span>&nbsp;');
			/*
			 * Logical operators
			 */
			if( operators.length > 0 ) {
				var select='<select id="' + this.rootId + '_op" style="font-size: small;font-family: courier;">';
				for( var i=0 ; i<operators.length; i++ ) {
					var op = operators[i];
					var selected = '';
					if( op == '>' ) {
						op = '&gt;';
						selected = 'selected';
					} else if( op == '<' ) {
						op = '&lt;';
					}
					select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
				}	
				select += '</select>';
				rootDiv.append(select);

				rootDiv.append('<input type=text id="' + this.rootId 
						+ '_val" class="inputvalue form-control input-sm" style="display: inline-block; height: 21px; width: 80px; font-size: small;font-family: courier;" value="' 
						+ default_value + '"> <span>%</span>');
			}
			$('#' + this.constListId).append("</div>");	

			var opselected = '#' + this.constListId + ' select[id="' + this.rootId + '_op"] option:selected';
			var opInput    = $('#' + this.constListId + ' select[id="' + this.rootId + '_op"]');
			var valInput   = $('#' + this.constListId + ' input[id="' + this.rootId + '_val"]');
			var closeInput = $('#' + this.constListId + ' a[id="' + this.rootId + '_close"]');

			closeInput.click(function() {
				rootDiv.remove();
				that.fireRemoveFirstAndOr(this.rootId);
				that.fireEnterEvent();
				that.fireTypoMsg(false, '');
			});
			opInput.change(function() {
				that.fireEnterEvent('AND'
						, this.value
						, valInput.val());				
			});
			valInput.keyup(function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			valInput.click(function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			valInput.on('input', function(event) {
				that.fireEnterEvent('AND'
						, $(opselected).text()
						, this.value);
			});
			this.fireEnterEvent('AND'
					, $(opselected).text()
					,valInput.val());
		}
	}
});
/**
 * USed by TAP. Just store a trepath in order to used in query covering multiple tables
 * @param params
 * @returns {CatalogueConstraint_mVc}
 */
function TapKWConstraint_mVc(params){
	KWConstraint_mVc.call(this, params);
	new KWConstraint_mvC(new  TapKWConstraint_Mvc(params), this);	
};
TapKWConstraint_mVc.prototype = Object.create(KWConstraint_mVc.prototype, {
});


console.log('=============== >  KWConstraint_v.js ');

KWConstraint_mvC = function(model, view){
	/**
	 * listen to the view
	 */
	var vlist = {
			controlEnterEvent : function(andor, operator, operand, unit){
				model.processEnterEvent(andor, operator, operand, unit);
			},
			controlRemoveConstRef : function(operator, operand){
				model.processRemoveConstRef(operator, operand);
			},
			controlRemoveFirstAndOr: function(key){
				model.processRemoveFirstAndOr(key);
			},
			controlRemoveAndOr: function(key){
				model.removeAndOr(key);
			},
			controlGetADQL: function(attQuoted){
				return model.getADQL(attQuoted);
			}, 
			controlGetAttributeHandler: function(){
				return model.getAttributeHandler();
			}, 
			controlInit: function(){
				return model.notifyInitDone();
			},
			controlTypoMsg: function(fault, msg){
				return model.notifyTypoMsg(fault, msg);
			},
			controlRunQuery: function(){
				return model.notifyRunQuery();
			}
	};
	view.addListener(vlist);

	var mlist = {
			isInit : function(attributehandler, operators ,andors,range, default_value){
				view.initForm(attributehandler, operators ,andors,range,default_value);
			},
			notifyFault: function(fault){
				view.drawFault(fault);
			},
			controlAhName: function(ah){
				return view.getAhName(ah);
			}
	};
	model.addListener(mlist);
};

console.log('=============== >  KWConstraint_c.js ');

/**
 * Form for selecting data by cardinality (0 or not)
 * @param params
 *  - parentDivId: id of the div where the view is drawn
 *  - queryView:  widget managing the global quey: invoked at each change on the view 
 *  - formName   : Name of the current form
 *  - title : Title of the field set
 *  - products : [{relation, label}]: array of object attached to each checkbox
 *         - relation: name of the relationship pointing on the attached product
 *         - label: label to display by the check box
 * @returns
 */
function AttachedData_mVc(params){
	this.queryView   = params.queryView;
	this.parentDivId = params.parentDivId;
	this.title       = params.title;
	this.formName    = params.formName;
	this.products    = params.products;

	this.productListId = this.formName + "_ProductList";
};


/**
 * Methods prototypes
 */
AttachedData_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			var that = this;
			var html = '<fieldset style="display inline-block; width: 100%;" >\n'
				+ '  <legend>' + this.title + '</legend>\n'
				+ '    <div id="' + this.productListId + '" style="display: inline; float: left; width: 50%">\n'
				+ '	   </div>\n'
				+ '	   <div style="display: inline; float: left; margin-left: 30px;"class="spanhelp">\n'
				+ '		one click on the check button for with,<br> two click on the\n'
				+ '		check button for without,<br> three click on the check button\n'
				+ '		for nothing ...\n'
				+ '	   </div>\n'
				+ '</fieldset>\n';
			$('#' + this.parentDivId).html(html);

			this.drawItems();

			$('#' + this.productListId + ' input').click(function(element) {
				var val = $(this).val();
				var id = $(this).attr('id');
				if( val == 0){
					$(this).attr('class','withdata').attr('value',1);
					$(this).next('.tt').text("With");
					//$('.tt#'+id).text("With");
				}
				else if(val == 1){
					$(this).attr('class','withoutdata').attr('value',2);
					$(this).next('.tt').text("Without");

					//$('.tt#'+id).text("Without");
				}
				else if(val == 2){
					$(this).attr('class','anydata').attr('value',0);
					$(this).next('.tt').text("Whatever");
					//$('.tt#'+id).text("Whatever");
				}	
				that.updateQuery();
			});



		},

		drawItems: function(){
			var that = this;
			for( var i=0 ;  i<this.products.length ; i++ ) {
				var html = '<div id=' + this.products[i].relation + ' style="width: 100%;display: block; float: left">\n'
				+ '  <input id=' + this.products[i].relation + ' class="anydata" type="button" value="0" />\n'
				+ '  <span id=' + this.products[i].relation + ' class="tt">Whatever</span>\n'
				+ '  <span class="tt">' + this.products[i].label + '</span>\n'
				+ '</div><br>\n';
				$('#' + this.productListId).append(html);

			}
		},

		updateQuery: function(element){
			var pattern = "";		
			var nl = "";
			$("#" + this.productListId + " input").each(function(){
				var val = $(this).attr("class");
				var produit = $(this).attr("id");
				if( val == "withdata") {
					pattern += nl + '    matchPattern{ '+produit +'}';
					nl = "\n";
				} else if( val == "withoutdata") {
					pattern += nl + '    matchPattern{ '+produit +', Cardinality = 0}';
					nl = "\n";
				}
			});
			if( this.queryView != null )
				this.queryView.fireAddConstraint(this.formName, "relation", pattern);
			else Out.info("Add pattern " + pattern + " no query view");
		},
		fireClearAllConst: function() {
			$('#' + this.productListId + " input").attr('class','anydata').attr('value',0);
			$('#' + this.productListId + ' .tt[id]').text("Whatever");
			this.updateQuery();
		}
};
/**
 * Form for selecting data by match pattern (0 or not)
 * @param params
 *  - parentDivId: id of the div where the view is drawn
 *  - queryView:  widget managing the global quey: invoked at each change on the view 
 *  - formName   : Name of the current form
 *  - title : Title of the field set
 *  - products : [{relation, label}]: array of object attached to each checkbox
 *         - relation: name of the relationship pointing on the attached product
 *         - pattern: Saada matchPattern withoyut cardinality
 *         - label: label to display by the check box
 * @returns
 */

function AttachedPattern_mVc(params){
	AttachedData_mVc.call(this, params);
	this.patterns = {};
	for( var i=0 ; i<params.products.length ; i++){
		/*
		 * Item can run on the same relation, thus relations can not be  used as IDs. We used the label (without spaces) instead
		 */
		var localId = this.products[i].label.replace(/ /g, '_');		
		this.patterns[localId] = { pattern: params.products[i].pattern, relation: params.products[i].relation};
	}
}

AttachedPattern_mVc.prototype = Object.create(AttachedData_mVc.prototype, {

	drawItems: {
		value: function(){
			var that = this;
			for( var localId in this.patterns) {
				var html = '<div id=' + localId + ' style="width: 100%;display: block; float: left">\n'
				+ '  <input id=' + localId + ' class="anydata" type="button" value="0" />\n'
				+ '  <span id=' + localId + ' class="tt">Whatever</span>\n'
				+ '  <span class="tt">' + localId + '</span>\n'
				+ '</div><br>\n';
				$('#' + this.productListId).append(html);
			}
		}
	},

	updateQuery : {
		value: function(element){
			var pattern = "";		
			var nl = ""
				var that = this;
			$("#" + this.productListId + " input").each(function(){
				var val = $(this).attr("class");
				var produit = $(this).attr("id");
				if( val == "withdata") {
					pattern += nl + '    matchPattern{\n      '
					              + that.patterns[produit].relation 
					              + '\n      ' + that.patterns[produit].pattern + '\n      }';
					nl = "\n";
					
				} else if( val == "withoutdata") {
					pattern += nl + '    matchPattern{\n      '
					              + that.patterns[produit].relation 
					              + '\n      , Cardinality = 0,\n      ' 
					              + that.patterns[produit].pattern + '\n      }';
					nl = "\n";
				}
			});
			if( this.queryView != null )
				this.queryView.fireAddConstraint(this.formName, "relation", pattern);
			else Out.info("Add pattern " + pattern + " no query view");
		}
	}
});

/**
 * Form for selecting data by match pattern (0 or not)
 * @param params
 *  - parentDivId: id of the div where the view is drawn
 *  - queryView:  widget managing the global quey: invoked at each change on the view 
 *  - formName   : Name of the current form
 *  - title : Title of the field set
 *  - products : [{relation, label}]: array of object attached to each checkbox
 *         - relation: name of the relationship pointing on the attached product
 *         - patternWith: Saada matchPattern for green button
 *         - patternWithout: Saada matchPattern for red button
 *         - label: label to display by the check box
 * @returns
 */

function AttachedPatterns_mVc(params){
	AttachedPattern_mVc.call(this, params);
	this.patterns = {};
	for( var i=0 ; i<params.products.length ; i++){
		/*
		 * Item can run on the same relation, thus relations can not be  used as IDs. We used the label (without spaces) instead
		 */
		var localId = this.products[i].label.replace(/ /g, '_');		
		this.patterns[localId] = { patternWith: params.products[i].patternWith
				                 , patternWithout: params.products[i].patternWithout
				                 , relation: params.products[i].relation};
	}
}

AttachedPatterns_mVc.prototype = Object.create(AttachedPattern_mVc.prototype, {

	updateQuery : {
		value: function(element){
			var pattern = "";		
			var nl = ""
				var that = this;
			$("#" + this.productListId + " input").each(function(){
				var val = $(this).attr("class");
				var produit = $(this).attr("id");
				if( val == "withdata") {
					pattern += nl + '    matchPattern{\n      '
					              + that.patterns[produit].relation 
					              + '\n      ' + that.patterns[produit].patternWith + '\n      }';
					nl = "\n";
				} else if( val == "withoutdata") {
					pattern += nl + '    matchPattern{\n      '
					              + that.patterns[produit].relation 
					              + '\n      ' + that.patterns[produit].patternWithout + '\n      }';
					nl = "\n";
				} 
			});
			if( this.queryView != null )
				this.queryView.fireAddConstraint(this.formName, "relation", pattern);
			else Out.info("Add pattern " + pattern + " no query view");
		}
	}

});
/**
 * 
 * ****/
function AttachedMatch_mVc(params){
	AttachedPattern_mVc.call(this, params);
	this.relation = params.relation;
	this.pattern = params.pattern;
	this.conditions = {};
	for( var i=0 ; i<params.products.length ; i++){
		/*
		 * Item can run on the same relation, thus relations can not be  used as IDs. We used the label (without spaces) instead
		 */
		var localId = this.products[i].label.replace(/ /g, '_');		
		this.conditions[localId] = params.products[i].condition;
	}
}

AttachedMatch_mVc.prototype = Object.create(AttachedPattern_mVc.prototype, {

	updateQuery : {
		value: function(element){
			var pattern = '    matchPattern{\n      ' + this.relation + '\n      ' + this.pattern + '\n      }';		
			var nl = ""
				var condition = "";
			var that = this;

			$("#" + this.productListId + " input").each(function(){
				var val = $(this).attr("class");
				var produit = $(this).attr("id");
				if( val == "withdata") {
					if( condition != "" ){
						condition += " AND " ;
					}
					condition += that.conditions[produit].yes;
				} else if( val == "withoutdata") {
					if( condition != "" ){
						condition += " AND " ;
					}
					condition += that.conditions[produit].no;
				} 
			});
			if( condition != "") {
				if( this.queryView != null )
					this.queryView.fireAddConstraint(this.formName, "relation", pattern.replace("{}", "{" + condition + "}"));
				else Out.info("Add pattern " + pattern + " no query view");
			} else {
				this.queryView.fireAddConstraint(this.formName, "relation", "");
			}
		}
	}

});
console.log('=============== >  AttachedData_v.js ');

/**
 * 
 * @param params
 *  - parentDivId: id of the div where the view is drawn
 *  - queryView:  widget managing the global quey: invoked at each change on the view 
 *  - formName   : Name of the current form
 *  - title : Title of the field set
 *  - getMeta : Url returning an object formated like [{setname, keywords: []}...]: 
 *         - setName: name of the keyword set (mission, waveband...)
 *         - keywords: set of keywords
 * @returns
 */
function VizierKeywords_mVc(params){
	this.queryView   = params.queryView;
	this.parentDivId = params.parentDivId;
	this.title       = params.title;
	this.formName    = params.formName;
	this.getMetaUrl     = params.getMetaUrl;

	this.productListId = this.formName + "_ProductList";
	this.testData = [
	                 {setName: "set1", keywords: ["set1_kw1", "set1_kw2", "set1_kw3"]},
	                 {setName: "set2", keywords: ["set2_kw1", "set2_kw2", "set2_kw3"]},
	                 {setName: "set3", keywords: ["set3_kw1", "set1_kw2", "set3_kw3"]}
	                 ];
};
/**
 * Methods prototypes
 */
VizierKeywords_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			var that = this;
			if( this.getMetaUrl != null ) {
				$.getJSON(this.getMetaUrl,function(data) {
					that.drawKeywordsSelectors(data);
				});
			} else {
				this.drawKeywordsSelectors(this.testData);
			}
		},
		drawKeywordsSelectors : function(data) {
			var that = this;
			var html = '<div style="display inline-block; width: 100%;" >\n';
				//+ '  <legend>Vizier Keyword Selector</legend>\n';

			for( var set=0 ; set<data.length ; set++ ) {
				var name = data[set].setName;
				var kws  = data[set].keywords;
				html += '<fieldset  style="float: left;">\n'
					  + '  <legend style="margin-bottom: 0px; border-bottom: 0px;">' + name + '&nbsp;<a name="' + name + '" class=closekw  href="javascript:void(0);" title="Unselect all items"></a></legend>\n';
				
				html += '<select name="' + name + '"  style=\"background-color: white;\" multiple="multiple" size="7" width="100%" class="form-control">';
				for ( var i=0; i<kws.length; i++){
					html += '<option title="' + name + '" value="'+kws[i]+'">'+kws[i]+'</option>';
				}
				html += '</select>\n</fieldset>\n';
			}				
			html += 
				'	   <div style="width: 270px;display: inline; float: left; margin-left: 10px;"class="spanhelp">\n'
			  + '		- The keywords proposed here are the same as those used by Vizier to tag catalogues<br>\n'
			  + '		- Constraint on keywords filter X-ray sources correlated with at least one source of a catalogue \n'
			  + '		matching all the selected keywords<br>\n'
			  + '		- Shift click to achieve multiple selections<br>\n'
			  + '       - The constraint on one keyword can be reversed by a ! set before the keyword in the text of the query'
			  + '	   </div>\n';

			html += '</div>';
			$('#' + this.parentDivId).append(html);
			
			$("#" + this.parentDivId + " select").change(function () {
				that.updateQuery();
			});
			$("#" + this.parentDivId + " a.closekw").click(function () {
				$("#" + that.parentDivId + ' option[title="' + this.name + '"]').removeAttr("selected");
				that.updateQuery();
			});


		},		
		updateQuery: function(){
			var selected = new Array();
			$("#" + this.parentDivId + " option:selected").each(function () {
				var kws = selected[this.title];
				if( kws == null ) kws = selected [this.title] = new Array();
				kws.push(this.value);
			});
			var retour = new Array();
			for( k in selected) {
				retour.push('    "' + k + '=' + selected[k].join(",") + '"');
			}
			this.queryView.fireAddConstraint(this.formName, "kwconst", retour.join("\n"));
		},
		fireClearAllConst: function() {
			$("#" + this.parentDivId + ' option').removeAttr("selected");
			this.updateQuery();			
		}
		
};

console.log('=============== >  VizierKeywords_v.js ');

/**
 * 
 * @param parentDivId: ID of the div containing thge filed list. It must exist before.
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors  
 * @param  constContainerId  : Id of the div containing all the list: must existe before             
 * @param  orderByHandler   : Handler reset commans         
 */
OrderBy_mVc = function(parentDivId, formName, constContainerId, orderByHandler){
	var orderByDesId  = parentDivId + "_orderdesc";
	var orderByAscId  = parentDivId + "_orderasc";
	var orderByDrop   = parentDivId + "_orderdrop";
	var orderById     = parentDivId + "_orderby";
	/**
	 *  parentDiv: JQuery DOM node of the container
	 */
	this.draw = function() {
		$('#' + constContainerId).append('<div class=orderby>'
				+ '<input id=' + orderById 
				+ ' class="orderby form-control" style="display: inline-block;" type="text" value="Order By" disabled="disabled">'
				+ '<label> desc <input id=' + orderByDesId + ' type="radio" name="OrderBy" value="desc"> </label>'
				+ '<label>asc <input id=' + orderByAscId + ' type="radio" name="OrderBy" value="asc" checked> </label>'	
				+ '<a href="javascript:void(0);" id=' + orderByDrop + ' class=closekw title="Reset OrderBy"></a>'			
				+ '</div>');
		
		$('#' + orderByDrop).click(function() {
			$('.orderby') .val('Order By');
			$('.orderby') .css('font-style' , 'italic');
			$('.orderby') .css('color' , 'darkgray');
			orderByHandler('OrderBy');
		});
		$('#' + orderByAscId).click(function() {
			orderByHandler($('#' + orderById).val());
		});
		$('#' + orderByDesId).click(function() {
			orderByHandler($('#' + orderById).val());
		});
	};
	this.setOrderBy = function(attname) {
		$('.orderby') .css('font-style' , '');
		$('.orderby') .css('color' , 'black');
		$('.orderby').val(attname);
	};
	this.getOrderBy = function (){
		return $('#' + orderById).val();
	};
	this.isDesc = function() {
		return ($('#' + orderByDesId).attr("checked"))?true: false;
	};
	this.fireClearAllConst= function (){
		$('.orderby').val('');
		$('#' + orderByDesId).attr("checked", "true");
	};	
	
};


console.log('=============== >  OrderBy_v.js ');

/**
 * COne Search form View
 * 
 * @param params:
 *            JS object with the following fields parentDivId: ID of the parent
 *            div formName : Name of the current form frames : Arrays of the
 *            supported frames urls : JS object containing handlers processing
 *            events: sesameURL : name resolver uploadURL : Handle the upload of
 *            position lists
 */
function ConeSearch_mVc(params) {
	this.editor = params.editor;
	this.parentDivId = params.parentDivId;
	this.formName = params.formName;
	this.frames = params.frames, this.sesameURL = params.urls.sesameURL;
	this.uploadURL = params.urls.uploadURL;
	this.cooFieldId = this.formName + "_CScoofield";
	this.radiusFieldId = this.formName + "_CSradiusfield";
	this.frameSelectId = this.formName + "_CSframeselect";
	this.uploadId = this.formName + "_CSupload";
	this.sesameId = this.formName + "_CSsesame";
	this.stackId = this.formName + "_CSstack";
	this.regionId = this.formName + "_CSregion";
};
/**
 * Methods prototypes
 */
ConeSearch_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			if (this.frames == null || this.frames.length == 0) {
				this.frames = [ 'ICRS' ];
			}
			var html = '<fieldset class="fieldiv col-sm-6" style="width:320px;">'
				+ '  <legend> Cone Search Setup </legend>'
				+ '     <form style="background: transparent;" class="form-horizontal">'
				+ '		  <div class="form-group">'
				+ '         <label class="col-sm-4 control-label">Coord/Name</label>'
				+ '         <div class="col-sm-6" style="padding-right: 5px;">'
				+ '           <input type=text id="'
				+ this.cooFieldId
				+ '" class="inputvalue form-control input-sm"/>'
				+ '		    </div>'
				+ '		    <div class="col-sm-2" style="padding-left: 1px;">'
				+ '           <a href="javascript:void(0);" id="'
				+ this.sesameId
				+ '" title="Invoke the CDS name resolver" class="sesame-small"></a>'
				+ '       </div></div>'
				+ '		  <div class="form-group">'
				+ '         <label class="col-sm-4 control-label">Radius(arcmin)</label>'
				+ '         <div class="col-sm-8">'
				+ '           <input type=text id="'
				+ this.radiusFieldId
				+ '" class="inputvalue form-control input-sm" value="1" />'
				+ '       </div></div>'
				+ '		  <div class="form-group" style="margin-bottom: 5px;">'
				+ '         <label class="col-sm-4 control-label">System</label>'
				+ ' 	    <div class="col-sm-8">'
				+ '        	  <select id="'
				+ this.frameSelectId
				+ '" class="form-control input-sm">'
				+ '      	  </select>'
				+ '       </div></div>'
				+ '       <div><span id='
				+ this.regionId
				+ ' class="action activehover" style="margin-right: 10px; float:none;">Draw a Search Region</span>'
				+ '       <span id="'
				+ this.uploadId
				+ '" class="action activehover" style="float:none;">Upload Position List</span></div>'
				+ '       <div style="display: inline; float: right;"><span class=help id="uploadresult"></span></div>'
				+ '       </div>' + '</fieldset>';
			$('#' + this.parentDivId).append(html);
			var s = $('#' + this.frameSelectId);
			for (var i = 0; i < this.frames.length; i++) {
				s.append('<option value=' + this.frames[i] + '>' + this.frames[i]
				+ '</option>');
			}

			this.setUploadForm();
			this.setSesameForm();
		},
		setRegionForm : function(handler) {
			var that = this;
			$('#' + this.regionId).click(function() {
				var dv = null;
				if (that.editor) {
					dv = that.editor.getDefaultValue();
				}
				if( dv == null ) {
					Modalinfo.info("Please, define first a search position before to edit a region", "No Target Defined")
				} else {
					Modalinfo.region(handler, dv
						// , [84.24901652054093, -5.640882748140112,83.34451837951998,
						// -6.103216341255678,83.60897420186223, -4.553808802262613]
					);
				}
			});
		},
		setUploadForm : function() {
			var that = this;
			var handler = (this.uploadURL != null) ? function() {
				Modalinfo.uploadForm("Upload a list of position", that.uploadURL,
						"Upload a CSV position list<br>Error are in arcmin",
						function(returnedPath) {
					var msg;
					if (returnedPath.retour.name != undefined
							&& returnedPath.retour.size != undefined) {
						msg = " File " + returnedPath.retour.name
						+ ' uploaded<br>'
						+ returnedPath.retour.positions
						+ ' positions';
					} else {
						msg = JSON.stringify(returnedPath.retour);
					}
					$('span#uploadresult').html(msg);
					that.editor.firePoslistUpload(returnedPath.path.filename, $('#' + that.radiusFieldId).val());
					Modalinfo.close();
				}, function() {
					$('span#uploadresult').text('');
				});
			} : function() {
				Modalinfo.info("Upload not implemented yet");
			};
			$('#' + this.uploadId).click(handler);

		},
		setSesameForm : function() {
			var that = this;
			var inputfield = $('#' + this.cooFieldId);
			var handler = (this.sesameURL != null) ? function() {
				Processing.show("Waiting on SESAME response");
				$.getJSON("sesame", {
					object : inputfield.val()
				}, function(data) {
					Processing.hide();
					if (Processing.jsonError(data, "Sesame failure", "Name "+inputfield.val()+" cannot be resolved")) {
						return;
					} else {
						inputfield.val(data.alpha + ' ' + data.delta);
						if (that.editor != undefined) {
							that.editor.listener.controlAttributeEvent(that.editor.fieldListView.getSearchParameters(), that.editor.constListId);
							$("#" + that.editor.constListId + " span.help").attr("style","display:none;");
							that.editor.fieldListView.resetPosition();
						}
					}
				});
			} : function() {
				Modalinfo.info("name resolver not implemented yet");
			};
			$('#' + this.sesameId).click(handler);

		},
		hasSearchParameters : function() {
			var coo = $('#' + this.cooFieldId).val();
			var radius = $('#' + this.radiusFieldId).val();
			if (coo.trim() == "" || isNaN(radius)) {
				return false;
			} else {
				return true;
			}
		},
		getSearchParameters : function() {
			var coo = $('#' + this.cooFieldId).val();
			var radius = $('#' + this.radiusFieldId).val();
			var frame = $('#' + this.frameSelectId + '  option:selected').text();
			if (coo.trim() == "") {
				Modalinfo.error("No coordinates given");
				return null;
			} else if (isNaN(radius)) {
				Modalinfo.error("Radius field requires a numerical value");
				return null;
			} else {
				return {
					type : "cone",
					position : coo,
					radius : radius,
					frame : frame
				};
			}
		},
		resetPosition : function() {
			$('#' + this.cooFieldId).val("");
		},
		fireClearAllConst : function() {
			var that = this;
			$('#' + this.cooFieldId).val('');
		}

};

/**
 * Used for merged catalogues 3XMM
 * 
 * @param params
 * @returns {SimplePos_mVc}
 */
function SimplePos_mVc(params) {
	ConeSearch_mVc.call(this, params);
	this.queryView = params.queryView;
};

/**
 * Method overloading
 */
SimplePos_mVc.prototype = Object
.create(
		ConeSearch_mVc.prototype,
		{
			/**
			 * Draw the field container
			 */
			draw : {
				value : function() {
					var that = this;
					if (this.frames == null || this.frames.length == 0) {
						this.frames = [ 'ICRS' ];
					}
					var html = '<div style="background: transparent;"  class="form-inline">'
						+ '		  <div class="form-group">'
						+ '         <label style="margin-left:7px;">Coord/Name</span>'
						+ '         <input type=text id="'
						+ this.cooFieldId
						+ '" class="inputvalue form-control input-sm"/>'
						+ '       	<a href="javascript:void(0);" id="'
						+ this.sesameId
						+ '" title="Invoke the CDS name resolver" class="sesame-small"></a>'
						+ '       </div>'
						+ '		  <div class="form-group">'
						+ '         <label style="margin-right: 7px;">Radius(arcmin)</label>'
						+ '         <input type=text id="'
						+ this.radiusFieldId
						+ '" class="inputvalue form-control input-sm" style="width: 40px;" value="1" />'
						+ '       </div>'
						+ '		  <div class="form-group">'
						+ '         <label style="margin-right: 7px;">System</label>'
						+ ((this.frames != null) ? '      <select class="form-control input-sm" id="'
								+ this.frameSelectId + '" >'
								: '')
								+ '         </select>'
								+ '       </div>'
								+ '       <div class="form-group" style="margin-top: 6px;">'
								+ '			<span id='
								+ this.regionId
								+ ' class="datafield activehover" style="float: none;margin-left: 10px;">Draw a Search Region</span>'
								+ '         <span class="datafield activehover help-block" id="'
								+ this.uploadId
								+ '" style="float: none;margin-left: 10px;">Upload Position List</span>'
								+ '       </div></div>'
								+ '      <div style="overflow: hidden; float:right;">'
								+ '         <span class=help id="uploadresult"></span>'
								+ '      </div>';
					$('#' + this.parentDivId).append(html);
					var s = $('#' + this.frameSelectId);
					for (var i = 0; i < this.frames.length; i++) {
						s.append('<option value=' + this.frames[i]
						+ '>' + this.frames[i] + '</option>');
					}
					this.setUploadForm();
					this.setSesameForm();

					that.setRegionForm(
							function(data){
								if( data.userAction ){
									if( data.region.size.x > 5 || data.region.size.y > 5) {
										Modalinfo.error("The region size can't exceeded 5 deg. \nIts actual size is " + JSON.stringify(data.region.size));
									} else { 
										if( data && data.userAction && data.isReady ) {
											var rq = '';
											if( data.region.format == "array2dim") {
												rq = '';
												for( var i=0 ; i<(data.region.points.length - 1) ; i++ ) {
													if( i > 0 ) rq += " ";
													rq += data.region.points[i][0] + " " + data.region.points[i][1];
												}

											} else if( data.region.format == "array") {
												rq = '';
												for( var i=0 ; i<data.region.points.length  ; i++ ) {
													if( i > 0 ) rq += " ";
													rq += data.region.points[i];
												}

											} else {
												Modalinfo.error(data.region.format + " not supported region format");
											}

											if( rq != '' ) {
												var radius = $('#' + that.radiusFieldId).val();
												var frame = $(
														'#' + that.frameSelectId
														+ '  option:selected').text();
												that.updateQueryRegion(rq, radius, frame);
												Modalinfo.closeRegion();
											}
										}
									}
								}
							});

					$('#' + this.cooFieldId).keyup(function(event) {
						if (event.which == 13) {
							event.preventDefault();
						} else {
							that.readAndUpdate();
						}
					});
					$('#' + this.cooFieldId).click(function(event) {
						that.readAndUpdate();
					});
					$('#' + this.radiusFieldId).keyup(function(event) {
						if (event.which == 13) {
							event.preventDefault();
						} else {
							that.readAndUpdate();
						}
					});
					$('#' + this.radiusFieldId).click(function(event) {
						that.readAndUpdate();
					});
					$('#' + this.frameSelectId).change(function(event) {
						that.readAndUpdate();
					});
				}
			},
			readAndUpdate : {
				value : function() {
					var coo = $('#' + this.cooFieldId).val();
					var radius = $('#' + this.radiusFieldId).val();
					var frame = $(
							'#' + this.frameSelectId
							+ '  option:selected').text();
					this.updateQuery(coo, radius, frame);
				}
			},
			setUploadForm : {
				value : function() {
					var that = this;
					var handler = (this.uploadURL != null) ? function() {
						Modalinfo
						.uploadForm(
								"Upload a list of position",
								that.uploadURL,
								"Upload a CSV position list<br>Error are in arcmin",
								function(returnedPath) {
									var msg;
									if (returnedPath.retour.name != undefined
											&& returnedPath.retour.size != undefined) {
										msg = returnedPath.retour.name
										+ ' uploaded, '
										+ returnedPath.retour.positions
										+ ' positions';
									} else {
										msg = JSON
										.stringify(returnedPath.retour);
									}
									$(
											'#'
											+ that.parentDivId
											+ ' span#uploadresult')
											.html(msg);
									$('#' + that.cooFieldId)
									.val(
											"poslist:"
											+ returnedPath.retour.name);
									$('#' + that.radiusFieldId)
									.val("0");
									that.readAndUpdate();
								}, function() {
									$('span#uploadresult')
									.text('');
								});
					}
					: function() {
						Modalinfo
						.info("Upload not implemented yet");
					};
					$('#' + this.uploadId).click(handler);

				}
			},
			setSesameForm : {
				value : function() {
					var that = this;
					var inputfield = $('#' + this.cooFieldId);
					var handler = (this.sesameURL != null) ? function() {
						Processing.show("Waiting on SESAME response");
						$.getJSON("sesame", {
							object : inputfield.val()
						}, function(data) {
							Processing.hide();
							if (Processing.jsonError(data,
									"Sesame failure")) {
								that.updateQuery('', '', null);
								return;
							} else {
								inputfield.val(data.alpha + ' '
										+ data.delta);
								that.readAndUpdate();
							}
						});
					}
					: function() {
						Modalinfo
						.info("name resolver not implemented yet");
					};

					$('#' + this.sesameId).click(handler);
				}
			},
			updateQuery : {
				value : function(coord, radius, frame) {
					if (this.queryView != null) {
						this.queryView.fireDelConstraint(this.formName,
								"position");
						if (coord != '' && radius != '') {
							this.queryView
							.fireAddConstraint(
									this.formName,
									"position",
									'    isInCircle("'
									+ coord
									+ '", '
									+ radius
									+ ', '
									+ ((frame == "FK5") ? "J2000"
											: (frame == "FK4") ? "J1950"
													: '-')
													+ ', ' + frame
													+ ')');
						}
					} else {
						Out.info("No query view");
					}
				}
			},
			updateQueryRegion : {
				value : function(coord, radius, frame) {
					if (this.queryView != null) {
						this.queryView.fireDelConstraint(this.formName,
								"position");
						if (coord != '' && radius != '') {
							this.queryView
							.fireAddConstraint(
									this.formName,
									"position",
									'    isInRegion("'
									+ coord
									+ '", '
									+ radius
									+ ', '
									+ ((frame == "FK5") ? "J2000"
											: (frame == "FK4") ? "J1950"
													: '-')
													+ ', ' + frame
													+ ')');
						}
					} else {
						Out.info("No query view");
					}
				}
			},
			fireClearAllConst : {
				value : function() {
					var that = this;
					$('#' + this.cooFieldId).val('');
					that.readAndUpdate();
				}
			}
		});

/**
 * Used for Taphandle params.postUploadHandler is invoked after the upload
 * succeed It recieved the an object as parameter {name: filename, size:
 * filesize, positions: num of valid positions}
 * 
 * @param params
 * @returns {SimplePos_mVc}
 */
function TapSimplePos_mVc(params) {
	SimplePos_mVc.call(this, params);
	this.handler = this.editor.fireInputCoordEvent;
	this.postUploadHandler = params.postUploadHandler;
	this.uploadedFile = "";
};

/**
 * Method overloading
 */
TapSimplePos_mVc.prototype = Object
.create(
		SimplePos_mVc.prototype,
		{
			/**
			 * Draw the field container
			 */
			draw : {
				value : function() {
					var that = this;
					if (this.frames == null || this.frames.length == 0) {
						this.frames = [ 'ICRS' ];
					}
					var html = '<div>'
						+ ' <div class="tapPos">'
						+ '       <span class=help style="display: inline-block; width: 7em; margin-right: 0px;">Coord/Name</span>'
						+ '       <input type=text id="'
						+ this.cooFieldId
						+ '" class=inputvalue  size=18 />'
						+ '       <a href="javascript:void(0);" id="'
						+ this.sesameId
						+ '" title="Invoke the CDS name resolver" class="sesame-small"></a>'
						+ '       <span class=help >System</span>'
						+ ((this.frames != null) ? '      <select id="'
								+ this.frameSelectId + '" >'
								: '')
								+ '      </select>'
								+ ' </div>'
								+ ' <div class="tapPos">'
								+ '       <span class=help style="margin-right:3px;">Radius(arcmin)</span>'
								+ '       <input type=text id="'
								+ this.radiusFieldId
								+ '" class=inputvalue style="width: 40px;" value="1" />'
								+ '       <input class=stackconstbutton id="'
								+ this.stackId
								+ '" type="button"/>'
								+ '       <input type=button id="'
								+ this.uploadId
								+ '" value="Upload Position List" disabled title="Not implemented yet (wait for next release)"/>'
								+ '</div>'
								+ '       <div>'
								+ '            <span class=help id="uploadresult"></span>'
								+ '       </div>' + ' </div>';
					$('#' + this.parentDivId).append(html);
					var s = $('#' + this.frameSelectId);
					for (var i = 0; i < this.frames.length; i++) {
						s.append('<option value=' + this.frames[i]
						+ '>' + this.frames[i] + '</option>');
					}
					this.setUploadForm();
					this.setSesameForm();
					$('#' + this.stackId).click(function() {
						that.readAndUpdate();
					});
					$('#' + this.cooFieldId).keyup(function(event) {
						if (event.which == 13) {
							that.readAndUpdate();
						}
					});
					// $('#' + this.cooFieldId
					// ).click(function(eventObject) {
					// that.readAndUpdate();
					// });
					$('#' + this.radiusFieldId).keyup(function(event) {
						if (event.which == 13) {
							that.readAndUpdate();
						}
					});
					// $('#' + this.radiusFieldId
					// ).click(function(eventObject) {
					// that.readAndUpdate();
					// });
					$('#' + this.frameSelectId).change(function(event) {
						that.readAndUpdate();
					});
				}
			},
			readAndUpdate : {
				value : function() {
					this.uploadedFile = "";
					var coo = $('#' + this.cooFieldId).val();
					var radius = $('#' + this.radiusFieldId).val();
					var frame = $(
							'#' + this.frameSelectId
							+ '  option:selected').text();
					if (coo.length == 0) {
						Modalinfo.error("No coordinates given",
						"input error");
					} else if (radius.length == 0) {
						Modalinfo.error("No radius given",
						"input error");
					} else if (!$.isNumeric(radius)) {
						Modalinfo.error("Radius must be numeric",
						"input error");
					} else {
						var rd = coo.split(/\s+/);
						if (coo.startsWith('poslist:')) {
							this.uploadedFile = coo.replace('poslist:',
							'');
							this.editor.fireInputCoordEvent(coo, null,
									radius, frame);
						} else if (rd.length != 2) {
							Modalinfo
							.error(
									"Coordinates must be separataed with a blank",
							"input error");
						} else if (!$.isNumeric(rd[0])
								|| !$.isNumeric(rd[1])) {
							Modalinfo.error("Radius must be numeric",
							"input error");
						} else {
							this.editor.fireInputCoordEvent(rd[0],
									rd[1], radius, frame);
						}
					}
				}
			},
			setUploadForm : {
				value : function() {
					var that = this;
					var handler = (this.uploadURL != null) ? function() {
						var radius = $('#' + that.radiusFieldId).val();
						if (radius == "") {
							Modalinfo.error("Radius must be set");
						} else if (that.editor.isReadyToUpload()) {
							Modalinfo
							.uploadForm(
									"Upload a list of position",
									that.uploadURL,
									"Upload a CSV position list (ra dec or object name)",
									function(returnedPath) {
										var msg;
										if (returnedPath.retour.name != undefined
												&& returnedPath.retour.positions != undefined) {
											msg = returnedPath.retour.name
											+ ' uploaded, '
											+ returnedPath.retour.positions
											+ ' positions';
											$(
													'#'
													+ that.cooFieldId)
													.val(
															"poslist:"
															+ returnedPath.retour.name);
											$(
													'#'
													+ that.parentDivId
													+ ' span#uploadresult')
													.html(msg);
											that
											.readAndUpdate();
											if (that.postUploadHandler != null) {
												that
												.postUploadHandler(returnedPath.retour);
											}
											Modalinfo.close();
										} else {
											Modalinfo
											.error(
													JSON
													.stringify(returnedPath),
													"Upload Failure");
										}
									}, function() {
										$('span#uploadresult')
										.text('');
									}, [ {
										name : 'radius',
										value : radius
									} ]);
						}
					}
					: function() {
						Modalinfo
						.info("Upload not implemented yet");
					};
					$('#' + this.uploadId).click(handler);

				}
			},
			setSesameForm : {
				value : function() {
					var that = this;
					var inputfield = $('#' + this.cooFieldId);
					var handler = (this.sesameURL != null) ? function() {
						Processing.show("Waiting on SESAME response");
						$.getJSON("sesame", {
							object : inputfield.val()
						}, function(data) {
							Processing.hide();
							if (Processing.jsonError(data,
									"Sesame failure")) {
								that.updateQuery('', '', null);
								return;
							} else {
								inputfield.val(data.alpha + ' '
										+ data.delta);
								that.readAndUpdate();
							}
						});
					}
					: function() {
						Modalinfo
						.info("name resolver not implemented yet");
					};

					$('#' + this.sesameId).click(handler);
				}
			},
			updateQuery : {
				value : function(coord, radius, frame) {
					if (this.queryView != null) {
						this.queryView.fireDelConstraint(this.formName,
								"position");
						if (coord != '' && radius != '') {
							this.queryView
							.fireAddConstraint(
									this.formName,
									"position",
									'    isInCircle("'
									+ coord
									+ '", '
									+ radius
									+ ', '
									+ ((frame == "FK5") ? "J2000"
											: (frame == "FK4") ? "J1950"
													: '-')
													+ ', ' + frame
													+ ')');
						}
					} else {
						Out.info("No query view");
					}
				}
			},
			fireClearAllConst : {
				value : function() {
					$('#' + this.cooFieldId).val('');
					that.readAndUpdate();
				}
			}
		});

console.log('=============== >  ConeSearch_v.js ');

/**
 * 
 * @param parentDivId: ID of the div containing thge filed list. It must exist before.
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors  
 * @param  constContainerId  : Id of the div containing all the list: must existe before             
 * @param  removeAllHandler   : Handler processing the click on the remove all button          
 */
ConstList_mVc = function(parentDivId, formName, constContainerId, removeAllHandler){
	/**
	 * keep a reference to ourselves
	 */
	var that = this;

	var constListId   = parentDivId + "_constlist";
	var delConstListId   = parentDivId + "_delconstlist";
	var typoMsgId     = parentDivId + "_typoMsgId";
	/**
	 *  parentDiv: JQuery DOM node of the container
	 */
	this.draw = function(isPos, isADQL) {
//		$("#" + constContainerId).append('<div class=constdiv ><fieldset class="constraintlist" id="' + constListId	+  '">'
//				+ '<legend class=help>List of Active Constraints <a href="javascript:void(0);" id=' + delConstListId + ' class=closekw title="Remove all constraints"></a></legend>'
//				+ '<span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
//				+ '</fieldset>'
//				+ '<span style="font-style: italic;color: lightgray;">QL stmt </span><span style="height: 18;" class=typomsg_ok id=' + typoMsgId + '></span>'
//				+ '</div>');

		if (isPos != undefined && isPos != null) {
			var h = isPos["fieldset"];
			var h2 = isPos["div"];
		} else {
			var h = "185px";
			var h2 = "150px"
		}
		
		if (isADQL != undefined && isADQL) {
			var w = "100%"
		} else {
			var w = "450px"
		}
		
		$("#" + constContainerId).append(			
				  '<div class=constdiv style="width: '+w+'">'
				+ '    <fieldset style=" height:'+h+';">'
				+ '        <legend style="border-bottom: 0px; " class=help>List of Active Constraints <a href="javascript:void(0);" id=' + delConstListId + ' class=closekw style="float: none;" title="Remove all constraints"></a></legend>'
				+ '        <div  style="overflow: auto; height:'+h2+'; background-color: #f2f2f2; width: 100%" id="' + constListId	+  '"><span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span></div>'
				+ '    </fieldset>'
				+ '    <div>'
				+ '      <span class="ql">QL stmt </span><span style="height: 18;" class=typomsg_ok id=' + typoMsgId + '></span>'
				+ '    </div>'
				+ '</div>');
		
		if (isPos) {
			$("#div-"+parentDivId).appendTo($("#"+typoMsgId).parent());
		}
		
		$('#' + delConstListId).click(function() {
			removeAllHandler();
		});	
		
		return constListId;
	};
	
	this.printTypoMsg= function(fault, msg){
		$("#"+ typoMsgId).each(function() {
			if(fault) {
				$(this).attr('class', 'typomsg_ko');
				$(this).text(msg);
			} else {
				$(this).attr('class', 'typomsg_ok');	
				$(this).text(msg);
			}
		});
	};
	this.isTypoGreen= function(){
		return ( $("#"+ typoMsgId).first().attr('class') ==  'typomsg_ok');					
	};
	this.fireClearAllConst= function() {
		$("#" + constListId + " div.kwConstraint").each(function() {
			$(this).remove();
		});
	};
	this.fireClearConst= function(filter) {
		$("#" + constListId + " div.kwConstraint").each(function() {
			if( $(this).attr("id").match(filter) ) $(this).remove();
		});
	};
	this.fireRemoveAllHandler= function() {
		removeAllHandler();
	};
};

console.log('=============== >  ConstList_v.js ');

/**
 * 
 * @param parentDivId: ID of the div containing thge filed list. It must exist before.
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors  
 * @param  handlers       : Object with the handler to be implemented. Possible Fields are                 
 *         stackHandler   : Handler processing the click on the stack button attached to each field            
 *         orderByHandler : Handler processing the click on the Orderby button attached to each field   
 *         raHandler      : Handler processing the click on the RA button   
 *        decHandler      : Handler processing the click on the DEC button   
 */

function BasicFieldList_mVc(parentDivId, formName, handlers){
	/*
	 * Some reference and IDs on useful  DOM elements
	 */
	this.parentDiv = $("#" +parentDivId );
	this.fieldListId   = parentDivId + "_fieldlist";
	this.fieldTableId   = parentDivId + "_fieldtable";
	this.attributesHandlers = new Array();
	this.filterPattern=null;
	this.formName = formName;
	this.dataTreePath = null;	/// instance of DataTreePath		
	/*
	 * Keep handler references
	 */
	this.stackHandler   = handlers.stackHandler;
	this.orderByHandler = handlers.orderByHandler;
	this.raHandler      = handlers.raHandler;
	this.decHandler     = handlers.decHandler;

	this.stackTooltip = "Click to constrain this field";
}

BasicFieldList_mVc.prototype = {
		draw: function() {
			var that = this;
			this.attributesHandlers = new Array();
			this.parentDiv.html('<div class=fielddiv><div class="fieldlist" id="' + this.fieldListId
					+  '"></div>'
				//	+ ' <div class="form-group" style="width:347px; margin-bottom:8px; margin-top:8px;"></div>'
					);

			$("#"+this.fieldListId).closest(".fielddiv").css("padding","3px");

		},

		setStackTooltip: function(stackTooltip) {
			this.stackTooltip = stackTooltip;
		},
		setDataTreePath: function(dataTreePath){
			this.dataTreePath = dataTreePath;
			this.displayFields();
		},
		getAttributeTitle : function(ah) {
			return ah.nameorg 
			+ " - database name; " +  ah.nameattr
			+ " - description: " +  ah.comment
			+ " - UCD: " +  ah.ucd
			+ " - Unit: " +  ah.unit
			+ " - Type: " +  ah.type
			+ " - Range: " +  ((ah.range == null || ah.range.values == null)? 'Not Set':JSON.stringify( ah.range.values).replace(/'/g,"&#39;"))
			;
		},
		/**
		 * Draw one field in the container
		 * Field described by the attribute handler ah
		 */
		displayField:  function(ah){
			var that = this;
			var id = this.formName + "_" + ah.nameattr;
			var title = this.getAttributeTitle(ah);
			var row ="<tr class=attlist id=" + ah.nameattr + ">" 
			+"<td class=attlist><span title='" + title + "'>"+ ah.nameorg+"</span></td>"
			+"<td class='attlist help'>" + ah.type +"</td>"
			+"<td class='attlist help'>" + ((ah.unit != undefined)? ah.unit:"") +"</td>"
			;

			if( this.orderByHandler != null ) {
				row += "<td class='attlist attlistcmd'>"
					+"<input id=order_" + id + " title=\"Click to order the query result by this field\" class=\"orderbybutton\" type=\"button\" ></input>"
					+"</td>";
			}
			if( this.stackHandler != null ) {
				row += "<td class='attlist attlistcmd'>"
					+"<input id=stack_" + id + " title=\"" + this.stackTooltip  + "\"  class=\"stackconstbutton\" type=\"button\"></input>"
					+"</td>";
			}
			if( this.raHandler != null ) {
				row += "<td class='attlist attlistcmd'>"
					+"<input id=tora_" + id + " title=\"Click to use this field as RA coordinate\"  class=\"raconstbutton\" type=\"button\"></input>"
					+"</td>";
			}
			if( this.decHandler != null ) {
				row += "<td class='attlist attlistcmd'>"
					+"<input id=todec_" + id + " title=\"Click to use this field as DEC coordinate\"  class=\"decconstbutton\" type=\"button\"></input>"
					+"</td>";
			}
			row += "</tr>";
			$('#' + this.fieldTableId).append(row);
			var id = this.formName + "_" + ah.nameattr;
			if( this.orderByHandler != null ) {
				$('#' + this.fieldListId + ' input[id="order_' + id + '"]' ).click(function() {that.orderByHandler($(this).closest("tr").attr("id"));});
			}
			if( this.stackHandler != null ){
				$('#' + this.fieldListId + ' input[id="stack_' + id + '"]' ).click(function() {
					that.stackHandler($(this).closest("tr").attr("id"));});
			}
			if( this.raHandler != null ){
				$('#' + this.fieldListId + ' input[id="tora_' + id + '"]' ).click(function() {that.raHandler($(this).closest("tr").attr("id"));});
			}
			if( this.decHandler != null ){
				$('#' + this.fieldListId + ' input[id="todec_' + id + '"]' ).click(function() {that.decHandler($(this).closest("tr").attr("id"));});
			}
			$('#' + this.fieldTableId + " tr#" + ah.nameattr + " span").tooltip( {
				track: true,
				delay: 0,
				showURL: false,
				opacity: 1,
				fixPNG: true,
				showBody: " - ",
				// extraClass: "pretty fancy",
				top: -15,
				left: 5
			});
		},
		/**
		 * Draw all fields in the container
		 * Fields are described by the attribute handler array ahs
		 * Warning ahs is  not a map but an array 
		 */
		displayFields : function(){
			var that = this;
			this.attributesHandlers = new Array();
			if( this.dataTreePath != null ) {
				MetadataSource.getTableAtt(
						this.dataTreePath
						, function() {
							var ahm = MetadataSource.ahMap(that.dataTreePath);
							that.displayAttributeHandlers(ahm);
						});
			}
		},
		/**
		 * Set the filed list with the AH array
		 * @param ahm
		 */
		displayAttributeHandlers: function(ahm) {
			var table  = "<table id=" + this.fieldTableId + " class='table' style='width: 100%; border-spacing: 0px; border-collapse:collapse'></table>";
			$('#' + this.fieldListId).html(table);
			for( var k=0 ; k<ahm.length ; k++) {
				var ah = ahm[k];
				this.attributesHandlers[ah.nameattr] = ah;				
				this.addPresetValues(ah);
				this.displayField(ah);
			}
			
		},
		addPresetValues : function(attributeHandler){
			if( attributeHandler.nameattr == 'dataproduct_type' ) {
				attributeHandler.range = {type: 'list', values: ["'image'", "'spectrum'", "'cube'",
				                                                 "'timeseries'", "'visibility'", "'eventlist'"]};
			} else 	if( attributeHandler.nameattr == 'calib_level' ) {
				attributeHandler.range = {type: 'list', values: [0, 1, 2, 3]};

			} else if( attributeHandler.nameattr == 'access_format' ) {
				attributeHandler.range = {type: 'list', values: ["'text/html'", "'text/xml'","'text/plain'"
				                                                 , "'application/fits'","'application/x-votable+xml'", "'application/pdf'"
				                                                 , "'image/png'", "'image/jpeg'", "'image/gif'", "'image/bmp'"]};
			}
		},
		getAttributeHandler: function(ahname){
			return this.attributesHandlers[ahname];
		}
}
/**
 * class prototype
 */
function FieldList_mVc(parentDivId, formName, handlers){
	BasicFieldList_mVc.call(this, parentDivId, formName, handlers);
	/*
	 * Some reference and IDs on useful  DOM elements
	 */
	this.fieldFilterId = parentDivId + "_fieldfilter";
}


/**
 * Methods prototypes
 */
FieldList_mVc.prototype = Object.create(BasicFieldList_mVc.prototype, {
	/**
	 * Draw the field container
	 */
	draw: {
		value: function(){

			var that = this;
			this.attributesHandlers = new Array();
			this.parentDiv.html('<div class=fielddiv><div class="fieldlist" id="' + this.fieldListId
					+  '"></div>'
					+ ' <div class="form-group" style="width:347px; margin-bottom:8px; margin-top:8px;"><div class="input-group"><div class="input-group-addon input-sm"><span class="glyphicon glyphicon-search"></span></div>'
					+ ' <input id="' + this.fieldFilterId +  '" class="form-control input-sm" type="text" placeholder="Search"/></div></div>');

			$("#"+this.fieldListId).closest(".fielddiv").css("padding","3px");

			$('#' + this.fieldFilterId).keyup(function() {
				that.filterPattern = new RegExp($(this).val(), 'i');
				that.fireFilter();
			});
			$('#' + this.fieldFilterId).one("click",function() {
				$(this).css('color','black');
				$(this).css('font-style','');
				$(this).attr('value','');
			});
		}	
	},

	/**
	 * Filter the displayed fields with the pattern typed by the user
	 */
	fireFilter : {
		value: function(){
			$('#' + this.fieldTableId).html('');
			for( var i in this.attributesHandlers  ) {
				var ah = this.attributesHandlers[i];
				if( (this.filterPattern.test(ah.nameorg)  || 
						this.filterPattern.test(ah.nameattr) || 
						this.filterPattern.test(ah.ucd)      || 
						this.filterPattern.test(ah.comment)) ) {
					this.displayField(ah);
				}
			}
		}
	}
});

/**
 * Sub-class of FieldList_mVc, specialized to display UCDs instead of Fields
 * Same constructor as the superclass
 * Only the draw method is overloaded
 * @param parentDivId
 * @param formName
 * @param handlers
 * @returns {UcdFieldList_mVc}
 */
function UcdFieldList_mVc(parentDivId, formName, handlers){
	FieldList_mVc.call(this, parentDivId, formName, handlers);
};

/**
 * Method overloading
 */
UcdFieldList_mVc.prototype = Object.create(FieldList_mVc.prototype, {
	displayField: {
		value: function(ah){
			var that = this;
			var id = this.formName + "_" + ah.ucd;
			var stackId = "stack_" + id ;
			var title = ah.ucd 
			+ " - description: " +  ah.comment
			;
			var row ="<tr class=attlist id='" + ah.ucd + "'>" 
			+"<td class=attlist><span title='" + title + "'>"+ ah.ucd+"</span></td>"
			;
			if( this.stackHandler != null ) {
				row += "<td class='attlist attlistcmd'>"
					+"<input id=" + stackId + " title=\"Click to constrain fields with this UCD\"  class=\"stackconstbutton\" type=\"button\"></input>"
					+"</td>";
			}
			row += "</tr>";
			$('#' + this.fieldTableId).append(row);
			if( this.stackHandler != null ){
				$('#' + this.fieldTableId  + ' input[id="' + stackId + '"]').click(function() {
					that.stackHandler($(this).closest("tr").attr("id"));}
				);
			}
			$('#' + this.fieldTableId +  ' tr[id="' + ah.ucd + '"] span').tooltip( {
				track: true,
				delay: 0,
				showURL: false,
				opacity: 1,
				fixPNG: true,
				showBody: " - ",
				top: -15,
				left: 5
			});
		}
	},
	/**
	 * Draw all fields in the container
	 * Fields are described by the attribute handler array ahs
	 * Warning ahs is  not a map but an array 
	 */
	displayFields : {
		value : function(){
			var that = this;						
			this.attributesHandlers =new Array();
			MetadataSource.getTableAtt(
					this.dataTreePath
					, function() {
						var ahm = MetadataSource.ahMap(that.dataTreePath);
						var table  = "<table id=" + that.fieldTableId + " style='width: 100%; border-spacing: 0px; border-collapse:collapse' class='table'></table>";
						$('#' + that.fieldListId).html(table);
						for( var k=0 ; k<ahm.length ; k++) {
							var ah = ahm[k];
							that.attributesHandlers[ah.ucd] = ah;				
							that.displayField(ah);
						}
					});
		}
	} 
});

/**
 * Sub-class of FieldList_mVc, specialized to display UCDs instead of Fields
 * Same constructor as the superclass
 * Only the draw method is overloaded
 * @param parentDivId
 * @param formName
 * @param handlers
 * @returns {UcdFieldList_mVc}
 */
function CatalogueList_mVc(parentDivId, formName, handlers){
	FieldList_mVc.call(this, parentDivId, formName, handlers);
};

/**
 * Method overloading
 */
CatalogueList_mVc.prototype = Object.create(FieldList_mVc.prototype, {
	/**
	 * Draw the field container
	 */
	draw :  {
		value: function() {
			var that = this;
			this.attributesHandlers = new Array();
			this.parentDiv.html('<div class=fielddiv>'
					+ '<div class="fieldlist" id="' + this.fieldListId
					+  '"></div>'
					+ ' <div class="form-group" style="width:347px; margin-bottom:8px; margin-top:8px;"><div class="input-group"><div class="input-group-addon input-sm"><span class="glyphicon glyphicon-search"></span></div>'
					+ ' <input id="' + this.fieldFilterId +  '" class="form-control input-sm" type="text" placeholder="Search"/></div></div>');

			$("#"+this.fieldListId).closest(".fielddiv").css("padding","3px");

			$('#' + this.fieldFilterId).keyup(function() {
				that.filterPattern = new RegExp($(this).val(), 'i');
				that.fireFilter();
			});
			$('#' + this.fieldFilterId).one("click",function() {
				$(this).css('color','black');
				$(this).css('font-style','');
				$(this).attr('value','');
			});
		}	
	},
	/**
	 * Draw one field in the container
	 * Field described by the catalogue description
	 */
	displayField: {
		value: function(ah){
			var that = this;
			var id = this.formName + "_" + ah.CLASSNAME;
			var title = ah.ACDS_CATNAME 
			+ " - ACDS_CATACRO " +  ah.ACDS_CATACRO
			+ " - ACDS_CATCDSTB: " +  ah.ACDS_CATCDSTB
			+ " - ACDS_CATCONF: " +  ah.ACDS_CATCONF
			+ " - ACDS_CATINTNB: " +  ah.ACDS_CATINTNB
			+ " - ACDS_CATNAME: " +  ah.ACDS_CATNAME
			+ " - ACDS_CDSCAT: " +  ah.ACDS_CDSCAT
			+ " - VIZIER_KW: " +  ah.VIZIER_KW
			+ " - CLASSNAME: " +  ah.CLASSNAME

			;
			var row ="<tr class=attlist id='" + ah.CLASSNAME + "'>" 
			+"<td class=attlist style='width: 25%; overflow: hidden'><span title='" + title + "'>"+ ah.ACDS_CATACRO+"</span></td>"
			//+"<td class='attlist help'>" + ah.ACDS_CATINTNB +"</td>"
			+"<td class='attlist help'>" + ah.ACDS_CATNAME +"</td>"
			;
			if( this.stackHandler != null ) {
				row += "<td class='attlist attlistcmd' style='width: 16px;'>"
					+"<input id=stack_" + id + " title=\"Click to constrain this field\"  class=\"stackconstbutton\" type=\"button\"></input>"
					+"</td>";
			}
			row += "</tr>"; 
			$('#' + this.fieldTableId).css('table-layout', 'auto');
			$('#' + this.fieldTableId).append(row);
			if( this.stackHandler != null ){
				$('#stack_' + id ).click(function() {that.stackHandler($(this).closest("tr").attr("id"));});
			}
			$('#' + this.fieldTableId +  ' tr[id="' + ah.CLASSNAME + '"] span').tooltip( {

				//$("tr#" + ah.CLASSNAME + " span").tooltip( {
				track: true,
				delay: 0,
				showURL: false,
				opacity: 1,
				fixPNG: true,
				showBody: " - ",
				// extraClass: "pretty fancy",
				top: -15,
				left: 5
			});
		}
	},
	/**
	 * Draw all fields in the container
	 * Fields are described by the attribute handler array ahs
	 * Warning ahs is  not a map but an array 
	 */
	displayFields : {
		value : function(ahs){
			this.attributesHandlers =new Array();
			for( var i=0 ; i<ahs.length ; i++){
				var ah = ahs[i];
				this.attributesHandlers[ah.CLASSNAME] = ah;
			}
			var table  = "<table id=" + this.fieldTableId + " style='width: 100%; border-spacing: 0px; border-collapse:collapse' class='table'></table>";
			$('#' + this.fieldListId).html(table);
			for( var i in this.attributesHandlers  ) {
				this.displayField(this.attributesHandlers[i]);
			}
		}
	},
	/**
	 * returns the AH named ahanme
	 */
	getAttributeHandlerByName: {
		value: function(ahname) {
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.nameaatr == ahname ) {
					return ah;;	
				}
			}
			return nul;
		}
	},
	/**
	 * Filter the displayed fields with the pattern typed by the user
	 */
	fireFilter :  {
		value: function(){
			$('#' + this.fieldTableId).html('');
			for( var i in this.attributesHandlers  ) {
				var ah = this.attributesHandlers[i];
				if( (this.filterPattern.test(ah.ACDS_CATACRO)  || 
						this.filterPattern.test(ah.ACDS_CATCDSTB) || 
						this.filterPattern.test(ah.ACDS_CATCONF)      || 
						this.filterPattern.test(ah.ACDS_CATINTNB) ||
						this.filterPattern.test(ah.ACDS_CATNAME) ||
						this.filterPattern.test(ah.ACDS_CDSCAT) ||
						this.filterPattern.test(ah.VIZIER_KW) ||
						this.filterPattern.test(ah.CLASSNAME) 
				)) {
					this.displayField(ah);
				}
			}
		}
	}
});


function TapColList_mVc(parentDivId, formName, handlers, sessionID){
	FieldList_mVc.call(this, parentDivId, formName, handlers);
	this.tableselectid = parentDivId + "_tableSelect";
	this.joinedTableLoaded = false;
};

/**
 * Column selector for ADQL
 * Method overloading
 */
TapColList_mVc.prototype = Object.create(FieldList_mVc.prototype, {
	draw : {
		value: function() {
			var that = this;
			this.attributesHandlers = new Array();
			this.parentDiv.html('<div class="fielddiv">'
					+ '<div class="fieldlist" id="' + this.fieldListId +  '" style="height: 175px"></div>'
					+ ' <div class="form-group" style="width:347px; margin-bottom:8px; margin-top:8px;"><div class="input-group"><div class="input-group-addon input-sm"><span class="glyphicon glyphicon-search"></span></div>'
					+ ' <input id="' + this.fieldFilterId +  '" class="form-control input-sm" type="text" placeholder="Search"/></div></div>'
					+ '  <div  style="width: 350px; margin: 3px;">'
					+ '		   <div class="form-group form-inline"><label for="tapColEditor_tableSelect">Join with table:</label>'
					+ '        <select id="' + this.tableselectid +  '"  class="table_filter form-control input-sm"  style="width: 250px">'
					+ '        </select></div>'
					+ ' </div>'
					+ '  </div>');		

			$('#' + this.fieldFilterId).keyup(function() {
				that.filterPattern = new RegExp($(this).val(), 'i');
				that.fireFilter();
			});
			$('#' + this.fieldFilterId).one("click",function() {
				$(this).css('color','black');
				$(this).css('font-style','');
				$(this).attr('value','');
			});		
			this.setChangeTableHandler();
		}
	},
	setChangeTableHandler: {
		value: function() {
			var that = this;
			$('#' + this.tableselectid).change(function() {
				var to = this.value;
				var fs = to.split('.');
				var schema, table;
				$('#' + that.fieldFilterId).val(""); 
				if( fs.length == 2 ){
					schema = fs[0];
					table = fs[1];
				} else {
					/*
					 * If no schema in table name, we suppose the new table to belong the same schema
					 */
					schema = that.treePath.schema;
					table = fs[0];
				}

				that.changeTable(new DataTreePath({nodekey: that.dataTreePath.nodekey, schema: schema, table: table}));
			});
		}
	},
	setDataTreePath: {
		value: function(dataTreePath){
			this.dataTreePath = dataTreePath;
			$('#' +  this.fieldListId ).html('');
			$('#' +  this.tableselectid ).html('');
			this.addTableOption(this.dataTreePath );
			this.changeTable(this.dataTreePath);	
		}
	},
	addTableOption: {
		value: function(treePath){
			$('#' + this.tableselectid).append('<option>' + treePath.schema + '.' + treePath.table + '</option>');
			//$('#' + this.tableselectid).append('<option>' + treePath.tableorg + '</option>');
		}
	},
	changeTable : {
		value: function(dataTreePath) {	
			this.dataTreePath = dataTreePath;
			this.displayFields();
		}
	},
	getAttributeTitle: {
		value:  function(ah) {
			return ah.nameorg 
			+ " - node; " +  this.dataTreePath.nodekey
			+ " - schema; " +  this.dataTreePath.schema
			+ " - table; " +  this.dataTreePath.table
			+ " - description: " +  ah.comment
			+ " - UCD: " +  ah.ucd
			+ " - Unit: " +  ah.unit
			+ " - Type: " +  ah.type
			+ " - Range: " +  ((ah.range == null || ah.range.values == null)? 'Not Set':JSON.stringify( ah.range.values).replace(/'/g,"&#39;"))
			;
		}
	},
	displayFields : {
		value : function(){
			var that = this;
			this.attributesHandlers =new Array();
			MetadataSource.getTableAtt(
					this.dataTreePath
					, function() {
						var ahm = MetadataSource.ahMap(that.dataTreePath);
						var table  = "<table id=" + that.fieldTableId + " style='width: 100%; border-spacing: 0px; border-collapse:collapse' class='table'></table>";
						$('#' + that.fieldListId).html(table);
						that.attributesHandlers = new Array();
						for( var k=0 ; k<ahm.length ; k++) {
							var ah = ahm[k];
							that.attributesHandlers[ah.nameattr] = ah;				
							that.displayField(ah);
						}
						if( !that.joinedTableLoaded ) {
							var jt = MetadataSource.joinedTables(that.dataTreePath);
							for( var k=0 ; k<jt.length ; k++) {
								that.addTableOption(jt[k].target_datatreepath);
							}
							/*
							 * The same object is used with different columns sets: no cache for joined tables
							 * uncommented by Pauline
							 */
							that.joinedTableLoaded = true;
						}
						that.lookForAlphaKeyword();
						that.lookForDeltaKeyword();
					});	

		}
	} ,
	lookForAlphaKeyword: {
		value: function(ah) {}
	},
	lookForDeltaKeyword: {
		value: function(ah) {}
	},


});


/**
 * Field list with RA/DEC field selector
 * @param parentDivId
 * @param formName
 * @param handlers
 * @param getTableAttUrl $.getJSON("gettableatt", {jsessionid: sessionID, node: nodekey, table:newTable }
 * @tables {node: nodekey, table:newTable}
 */
function TapFieldList_mVc(parentDivId, formName, handlers, getTableAttUrl, sessionID){
	TapColList_mVc.call(this, parentDivId, formName, handlers, getTableAttUrl, sessionID);
	this.raFieldId = parentDivId + "_rafield";
	this.decFieldId = parentDivId + "_decfield";
	this.alphakw = null;
	this.deltakw = null;
	this.radec = handlers.radec;

	this.getTableAttUrl = getTableAttUrl;
	if(this.radec){
		this.raHandler= function(ah){this.setAlphaKeyword(ah);};
	} else if (handlers.raHandler != undefined) {
		this.raHandler= function(ah){this.setAlphaKeyword(ah);handlers.raHandler(ah);};
	}
	if(this.radec){
		this.decHandler= function(ah){this.setDeltaKeyword(ah);};
	} else if (handlers.decHandler != undefined){
		this.decHandler= function(ah){this.setDeltaKeyword(ah);handlers.decHandler(ah);};
	}
};


/**
 * Method overloading
 */
TapFieldList_mVc.prototype = Object.create(TapColList_mVc.prototype, {
	draw : {
		value: function() {
			var that = this;
			this.attributesHandlers = new Array();
			var radec = "";
			if (this.radec) {
				radec += '<div id="div-'+this.raFieldId.substring(0, this.raFieldId.length-8)+'">'
				+ ' <div>'
				+ '    <span class="help" style="margin-left:5px;">ra&nbsp;&nbsp;</span>'
				+ '    <div id="' + this.raFieldId +  '"  style="display:inline-block;"/>'
				+ ' </div>'
				+ ' <div>'
				+ '    <span class="help"  style="margin-left:5px;">dec&nbsp;</span>'
				+ '    <div id="' + this.decFieldId +  '"  style="display:inline-block;"/>'
				+ ' </div>'
				+ ' </div>';
			}
			this.parentDiv.html('<div class=fielddiv>'
					+ ' <div class="fieldlist" id="' + this.fieldListId +  '" style="height: 175px"></div>'
					+ ' <div class="form-group" style="width:347px; margin-bottom:8px; margin-top: 8px;"><div class="input-group"><div class="input-group-addon input-sm"><span class="glyphicon glyphicon-search"></span></div>'
					+ ' <input id="' + this.fieldFilterId +  '" class="form-control input-sm" type="text" placeholder="Search"/></div></div>'
					+ '  <div  style="width: 350px; margin: 3px;">'
					+ '		   <div class="form-group form-inline"><label for="tapColEditor_tableSelect">Join with table:</label>'
					+ '        <select id="' + this.tableselectid +  '"  class="table_filter form-control input-sm"  style="width: 250px">'
					+ '        </select></div>'
					+ '  </div>'
					+ radec
					+ '  </div>');
			$('#' + this.fieldFilterId).keyup(function() {
				that.filterPattern = new RegExp($(this).val(), 'i');
				that.fireFilter();
			});
			$('#' + this.fieldFilterId).one("click",function() {
				$(this).css('color','black');
				$(this).css('font-style','');
				$(this).attr('value','');
			});		
			this.setChangeTableHandler();
		}
	},
	setTreePath: {
		value: function(dataTreePath){
			this.dataTreePath = dataTreePath;
			$('#' +  this.fieldListId ).html('');
			$('#' +  this.tableselectid ).html('');
			$('#' + this.raFieldId).html('');
			this.alphakw = null;
			$('#' + this.decFieldId).html('');
			this.deltakw = null;
			this.addTableOption(this.dataTreePath );
			this.changeTable(this.dataTreePath);	
		}
	},
	changeTable : {
		value: function(dataTreePath) {	
			this.dataTreePath = dataTreePath;
			this.displayFields();
			/*
			this.lookForAlphaKeyword();
			this.lookForDeltaKeyword();
			 */
		}
	},
	lookForAlphaKeyword: {
		value: function(ah) {
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.ucd == "pos.eq.ra;meta.main" ) {
					this.setAlphaKeyword(ahn);	
					return;
				}
			}
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.ucd  == "pos.eq.ra" || ah.ucd.match( /POS_EQ_RA/i)) {
					this.setAlphaKeyword(ahn);	
					return;
				}
			}
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.nameattr  == "s_ra" || ah.nameattr  == "pos_ra_csa"|| ah.nameattr  == "_sc_ra" 
					|| ah.nameattr.toUpperCase()  == "RA" || ah.nameattr.toUpperCase()  == "RAJ2000") {
					this.setAlphaKeyword(ahn);	
					return;
				}
			}
		}
	},
	setAlphaKeyword: {
		value: function(ahname) {
			$('#' + this.raFieldId).html('');
			this.alphakw  = new TapKWSimpleConstraint_mVc({divId: this.raFieldId
				, constListId: this.raFieldId
				, isFirst: true
				, attributeHandler: this.attributesHandlers[ahname]
			, editorModel: null
			, defValue: ''
				, treePath: jQuery.extend({}, this.dataTreePath)});
			this.alphakw.fireInit();
		}
	}
	,
	getRaKeyword: {
		value: function(ahname) {
			return $('#' + this.raFieldId+ " span" ).text();
		}
	},

	lookForDeltaKeyword: {
		value: function(ah) {
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.ucd == "pos.eq.dec;meta.main" ) {
					this.setDeltaKeyword(ahn);	
					return;
				}
			}
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.ucd  == "pos.eq.dec" || ah.ucd.match( /POS_EQ_DEC/i) ) {
					this.setDeltaKeyword(ahn);	
					return;
				}
			}
			for( var ahn in this.attributesHandlers ) {
				var ah = this.attributesHandlers[ahn];
				if( ah.nameattr  == "s_dec"|| ah.nameattr  == "pos_dec_csa" || ah.nameattr  == "_sc_dec" 
					|| ah.nameattr.toUpperCase()  == "DEC" || ah.nameattr.toUpperCase()  == "DECJ2000" ) {
					this.setDeltaKeyword(ahn);	
					return;
				}
			}
		}
	},
	setDeltaKeyword: {
		value: function(ahname) {
			$('#' + this.decFieldId).html('');
			this.deltakw  = new TapKWSimpleConstraint_mVc({divId: this.decFieldId
				, constListId: this.decFieldId
				, isFirst: true
				, attributeHandler: this.attributesHandlers[ahname]
			, editorModel: null
			, defValue: ''
				, treePath: jQuery.extend({}, this.dataTreePath)});
			this.deltakw.fireInit();
		}
	},
	getDeltaKeyword: {
		value: function(ahname) {
			return $('#' + this.decFieldId + " span" ).text();
		}
	}
});


console.log('=============== >  FieldList_v.js ');

/**
 * Sorter_mVc: Small widget commanding column sorting (ascedent or descendant). The sorter layout includes the 2 arrows and the the filed label.
 * It corresponds to the whole content of the container. 
 * The Sorter is supposed to belongs to a TR container
 * @param container       : JQuery ref of container. It must exist with the DOM  before.
 * @param parentContainer : JQUERY ref of the container containing all coupled sorters (typically all row heads)
 * @param fieldAh         : Attribute handler of the field: namorg is displayed and nameattr is sent to the sort handler
 * @param  handler        : handler called   when  the state of the arrows change. 
 *                          This handler must have be prototyped as function(field, sort)
 *                          where field is the name of the filed given to the constructor
 *                          and sort is the command. It is equals to 
 *                          	- "asc" : for ascendant sort
 *                              - "desc": for descendant sort
 *                              - null  : for no sort
 */

/**
 * class prototype
 */
function Sorter_mVc(container, parentContainer, fieldAh, handler){

	/*
	 * Some reference and IDs on useful  DOM elements
	 */
	this.container = container;
	this.parentContainer = parentContainer;
	this.fieldAh = fieldAh;
	this.handler = (handler == null)? function(fieldName, sortCommand){alert("Sort command: " + fieldName + " " +  sortCommand);}: handler;
}
/**
 * Methods prototypes
 */
Sorter_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			var that = this;
			this.container.html("<div style='display: inline;'>"
					+ "<div class=sorter><a class=sort_asc_disabled href='#'></a><a class=sort_desc_disabled href='#'></a></div>"
					+ "<span class=sorter>" + this.fieldAh.nameorg+ "</span>"
					+ "</div>");
			this.container.find("a").click(function(){
				var clickedAnchor = $(this);
				that.enableDisable(clickedAnchor);
				var nodeClass = clickedAnchor.attr("class");
				if( nodeClass == "sort_asc" ) {
					that.handler(that.fieldAh.nameattr, "asc");
				} else if( nodeClass == "sort_desc" ) {
					that.handler(that.fieldAh.nameattr, "desc");
				} else {
					that.handler(that.fieldAh.nameattr, null);
				}
			});
		},
		enableDisable: function(clickedAnchor) {
			var initClass =  clickedAnchor.attr("class");
			
			this.parentContainer.find('a').each(function() {
				var node = $(this);
				var nodeClass = node.attr("class");
				if( nodeClass == "sort_asc") {
					node.attr("class","sort_asc_disabled" );
				} else if( nodeClass == "sort_desc") {
					node.attr("class","sort_desc_disabled" );
				} 
			});
			clickedAnchor.attr("class",
					(initClass == "sort_asc_disabled")? "sort_asc"
							:(initClass == "sort_asc")? "sort_asc_disabled"
									: (initClass == "sort_desc_disabled")? "sort_desc"
											: "sort_desc_disabled");
					
		},
		activeArrow: function(asc) {
			this.container.find("a").each(function() {
				var node = $(this);
				var nodeClass = node.attr("class");
				if( nodeClass == "sort_asc_disabled" && asc) {
					node.attr("class","sort_asc" );
				} else if( nodeClass == "sort_desc_disabled" && !asc ) {
					node.attr("class","sort_desc" );
				} 
				
				
			});
		}
};



console.log('=============== >  Sorter_v.js ');

NodeFilter = function () {
	var cache = new Object;

	this.applyFilter = function(key, filter) {
		var re = new RegExp(filter, 'i');
		var fl = $("#nodeFilterList");
		var lis = '<ul class=attlist>';
		fl.html('<ul class=attlist>');
		var state = cache[key];
		for(var n=0 ; n<state.length ; n++){
			if( re.test(state[n].name)) {
				var selected = (state[n].selected)? "checked": "";
				lis += "<li class=tableSelected >" 
					+ "<input  type='checkbox' " + selected + " onclick='NodeFilter.synchroniseCache(&quot;" + key + "&quot;);'>"
					+ "<span style='font-color: black;'>" + state[n].name + "</span>"
					+ " <i></i>"
					+ "</li>\n";

			}
		}
		fl.html(lis + '</ul>');

	}

	this.unSelectAll  = function(key){
		$('#nodeFilterList input').removeAttr('checked');
		this.synchroniseCache(key);
	}
	this.selectAll  = function(key){
		$('#nodeFilterList input').attr('checked', true);
		this.synchroniseCache(key);
	}

	this.synchroniseCache  = function(key){
		var state = cache[key];
		var lstate = new Object();
		$('#nodeFilterList li').each(function(){
			var  moi = $(this);
			lstate[moi.children("span").html()] = moi.children("input").is(":checked");
		});
		for(var n=0 ; n<state.length ; n++){
			var ls = lstate[state[n].name];
			if( ls != undefined ){
				state[n].selected = ls
			}
		}
	}

	this.create  = function(key, metadata, handler){
		if(cache[key] == undefined ){
			var state = new Array();
			for(var n=0 ; n<metadata.length ; n++){
				state.push({name: metadata[n].name, selected: true});
			}
			cache[key] = state;
		}

		var table = "<div class='detaildata'>"
			+ "    <div class='detaildata' style='width: 60%; display: inline; overflow: hidden;'>"
			+ "        <span class=help>Give a filter on catalogue name or description:"
			+ "        <br> - The filter is a RegExp case unsensitive."
			+ "        <br> - Type [RETURN] to apply"
			+ "        <br> The number of selected tables returned by the server is limited to 100 in any case."
			+ "        </span>"
			+ "    </div>"
			+ "    <div class='detaildata' style='height: 45px;display: inline;float:right; padding-top:15px; margin-right: 15px;'>"
			+ "        <input id=nodeFilter type=texte width=24 class='form-control input-sm'>"
			+ "    </div>"
			+ "    <div id=nodeFilterList class='detaildata' style='border: 1px black solid; background-color: whitesmoke; width: 100%; height: 380px; overflow: auto; position:relative'>"
			+ "    </div>"
			+ "    <p class=help>"
			+ "    <a href='#' onclick='NodeFilter.selectAll(&quot;" + key + "&quot;);'>select</a>/"
			+ "    <a href='#' onclick='NodeFilter.unSelectAll(&quot;" + key + "&quot;)'>unselect</a> all<br>"
//		+ "     <a href='#' onclick=\"$('#nodeFilterList input').removeAttr('checked');$('#nodeFilterList li').attr('class', 'tableNotSelected');\">unselect</a> all)<br>"
		+ "    <input id=nodeFilterApply type=button value='Accept' style='font-weight: bold;'>"
		+ "    <span class=help>(Type [ESC] to close the window)</span>"
		+ "    </div>"
		+ "</div>";

		Modalinfo.dataPanel('Column Selector for node  <i> + ' + key + ' + </i>', table, null, "white");

		var that = this;
		$("#nodeFilter").keyup(function(event) {
			that.applyFilter(key, $(this).val());
		});
		this.applyFilter(key, "");
		$("#nodeFilterApply").click(function(event) {
			handler(cache[key]);
			Modalinfo.close();
		});
		Modalinfo.center();
	}

	/**
	 * 
	 */
	var pblc = {};
	pblc.create = create;
	pblc.applyFilter = applyFilter;
	pblc.selectAll = selectAll;
	pblc.unSelectAll = unSelectAll;
	pblc.synchroniseCache = synchroniseCache;

	return pblc;
}();
///**
//* keep a reference to ourselves
//*/
//var that = this;
///**
//* who is listening to us?
//*/
//var listeners = new Array();

//var filter = new Array();
///**
//* add a listener to this view
//*/
//this.addListener = function(list) {
//listeners.push(list);
//};

//this.fireGetFilteredNodes  = function(node){
//$.each(listeners, function(i) {
//listeners[i].controlGetFilteredNodes(node);
//});

//if (this.getFilter(node) != null) {
//filter = this.changeFilter(node, $("#nodeFilter").val());
//} else {
//filter.push({node: node, filter: $("#nodeFilter").val()});
//}

//Modalinfo.closeDataPanel();
//};

//this.getFilter = function(node) {
//var f = null;
//filter.forEach(function(element) {
//if (element.node === node) {
//f = element.filter;
//}
//});
//return f;
//}

//this.changeFilter = function(node, new_filter) {
//filter.forEach(function(element) {
//if (element.node === node) {
//element.filter = new_filter;
//}
//});
//return filter;
//}


//this.applyFilter = function(node) {
////Processing.show('Get filtered table list');			
//$.getJSON("getnode", {jsessionid: sessionID, node: node, filter: $("#nodeFilter").val(), selected: ''}, function(jsdata) {
////Processing.hide();
//if( Processing.jsonError(jsdata, "Cannot get the node selection") ) {
//return;
//} else {
//that.fireShowNodeSelection($("#nodeFilterList"), jsdata);
//}
//});
//};
///**
//* Display in the div the list of selected tables returned by the server 
//*/
//this.fireShowNodeSelection = function(listDiv, jsSelection)  {
//listDiv.html('');		
//for( var i=0 ; i<jsSelection.schemas.length ; i++ ) {
//var schema = jsSelection.schemas[i];
//var sn = schema.name;
//if( sn == "TAP_SCHEMA" || sn == 'tap_schema' ) {
//continue;
//}
//listDiv.append("<p class='chapter' style='border: 1px solid #c6c4c4 !important;'><b>Schema</b> " + sn + "</p>");		
//var list = "<ul class=attlist>";
//for( var j=0 ; j<schema.tables.length ; j++ ) {
//var table = schema.tables[j];
//list += "<li class=tableSelected >" 
//+ "<input  type='checkbox' checked onclick='nodeFilterView.fireSelectFilteredNode($(this));'>"
//+ "<span style='font-color: black;'>" + table.name + "</span>"
//+ " <i>" + table.description + "</i>"
//+ "</li>";
//}
//list += "</ul>";
//listDiv.append(list);		
//}
//};

///**
//* Select unselect one filter table
//* 
//*/
//this.fireSelectFilteredNode = function(button) {
//if( button.attr('checked') ) {
//button.parent().attr('class', 'tableSelected');
//} else {
//button.parent().attr('class', 'tableNotSelected');
//}
//};

//}
//});

console.log('=============== >  NodeFilter_v.js ');

/**
 * @returns {QueryTextEditor_Mvc}
 */
function DataLink_Mvc() {
	this.formEntries =  new Array();
	this.listener = null;
};

DataLink_Mvc.prototype = {
		addListener : function(list){
			this.listener = list;
		},
		storeWebService: function(webservice, url) {
			this.formEntries[webservice] = {url: url, loaded: false};
		},
		getUrl: function(webservice) {
			return ( this.formEntries[webservice] == null ) ? null: this.formEntries[webservice].url;
		},
		webServiceLoaded: function(webservice) {
			if( this.formEntries[webservice] != null ) {
				this.formEntries[webservice].loaded = true;
			}
		},
		isWebServiceLoaded: function(webservice) {
			return ( this.formEntries[webservice] == null ) ? false: this.formEntries[webservice].loaded;
		}
};
console.log('=============== >  DataLink_m.js ');

/*
 * Ontology map for links
 * http://www.ivoa.net/rdf/datalink/core/2014-10-30/datalink-core-2014-10-30.html
 */
var linkVocabulary = [];
linkVocabulary ["#this"] = "the primary (as opposed to related) data of the identified resource";
linkVocabulary ["#progenitor"] = "data resources that were used to create this dataset (e.g. input raw data)";
linkVocabulary ["#derivation"] = "data resources that are derived from this dataset (e.g. output data products)";
linkVocabulary ["#auxiliary"] = "auxiliary resources";
linkVocabulary ["#weight"] = "Weight map: resource with array(s) containing weighting values";
linkVocabulary ["#error"] = "Error map: resource with array(s) containing error values";
linkVocabulary ["#noise"] = "Noise map: resource with array(s) containing noise values";
linkVocabulary ["#calibration"] = "Calibration data	resource used to calibrate the primary data";
linkVocabulary ["#bias"] = "Bias calibration data used to subtract the detector offset level";
linkVocabulary ["#dark"] = "Dark calibration data used to subtract the accumulated detector dark current";
linkVocabulary ["#flat"] = "Flat field calibration data	used to calibrate variations in detector sensitivity";
linkVocabulary ["#preview"] = "low fidelity but easily viewed representation of the data";
linkVocabulary ["#preview-image"] = "preview of the data as a 2-dimensional image";
linkVocabulary ["#preview-plot"] = "preview of the data as a plot (e.g. spectrum or light-curve)";
linkVocabulary ["#proc"] = "Processing server-side data processing result";
linkVocabulary ["#cutout"] = "Cutout a subsection of the primary data";

/**
 * @param params { parentDivId: 'query_div',baseurl}
 * @returns
 */
function DataLink_mVc(params) {
	this.parentDiv = $("#" +params.parentDivId );
	this.baseurl = params.baseurl;
	this.forwardurl = params.forwardurl;
	this.textareaid = params.parentDivId + "_text";

	this.listener = null;
}
DataLink_mVc.prototype = {

		addListener : function(list){
			this.listener = list;
		},
		draw : function(){	
			var that = this;
			var url = (this.forwardurl != null )? (this.forwardurl+ "?target=" + encodeURIComponent( this.baseurl))
					: this.baseurl;
			Processing.show("Fetching Datalink Description at " + url);
			$.ajax({
				type: "GET",
				url: url,
				dataType: "xml",      
				error: function (xhr, ajaxOptions, thrownError) {
					Processing.hide();
					Modalinfo.error(url + "\n" + xhr.responseText + "\n" + thrownError);
				},
				success: function(xml) {
					Processing.hide();
					if( $(xml).find('VOTABLE').length != 0 ){
						that.processVOTABLEResponse(xml);
					} else { 
						that.processXMLResponse(xml);
					}
				}});
		},
		processVOTABLEResponse: function(xml){
			var html = "";
			var hasLink = false;
			$(xml).find('TR').each(function(){
				var i=0;
				var uri ="";
				var url ="";
				var semantic ="";
				var productType ="";
				var contentType ="";
				var size ="";

				var type = $(this).find('TD').each(function(){
					hasLink = true;
					switch(i){
					case 0: uri = $(this).text();break;
					case 1: url = $(this).text();break;
					case 2: productType = $(this).text();break;
					case 3: semantic = $(this).text();break;
					case 4: contentType = $(this).text();break;
					case 5: size = $(this).text();break;
					}
					i++;
				});
				html += "<fieldset>";
				html += "  <legend><span>Link <i>" + productType + "</i> <span></legend>";
				html += "    <a class='dlinfo' title='Get info about' href='#' onclick='LinkProcessor.fireGetProductInfo(\"" + this.linkInstance.access_url + "\"); return false;'></a>"
				html += "    <a class=dldownload href='#' onclick='PageLocation.changeLocation(&quot;" + url + "&quot);' title='Download link target'></a>";
				html += "    <span class=help>" +  semantic + "</span><br>";
				html += "    <span class=help><b>Product URI:</b> " +  uri + "</span><br>";
				html += "    <span class=help><b>Content:</b> " +  contentType + " " +  ((size == 0)? "": size +"b") + "</span>";
				html += "</fieldset>";
			});
			if( hasLink){
				Modalcommand.commandPanel("Link Browser", html);
				$("div.datalinkform").toggle();
				Modalcommand.setDivToggling(function() {that.buildWebserviceForm($(this).attr("id"));});
			} else {
				Modalinfo.info("No link available", "Datalink Info");
			}
		},
		processXMLResponse: function(xml){
			var that = this;
			var html = "";
			$(xml).find('endpoint').each(function(){
				var type = $(this).attr('type');
				var datatype = $(this).attr('datatype');
				html += "<fieldset>";
				html += "  <legend><span>Link <i>" + $(this).attr('name') + "</i> (" + datatype + ")<span></legend>";
				if( type == "download" ) {
					html += that.buildDownloadForm($(this), datatype);
				} else if( type == "webservice" ) {							
					html += that.buildWebserviceEntry($(this));
				} else {
					Modalinfo.error("Unknown link type " + type);
				}
				html += "</fieldset>";
			});
			Modalcommand.commandPanel("Link Browser", html);
			$("div.datalinkform").toggle();
			Modalcommand.setDivToggling(function() {that.buildWebserviceForm($(this).attr("id"));});
		},
		buildDownloadForm : function(xml, datatype) {
			var url = (this.forwardurl != null )? (this.forwardurl+ encodeURIComponent( xml.find('url').text()))
					: xml.find('url').text();
			url =  xml.find('url').text();
			var clk = (datatype == "preview")? "Modalinfo.openIframePanel(&quot;" +  url + "&quot);" 
					: "PageLocation.changeLocation(&quot;" +  url + "&quot);";
			var html = "<div>";
			html += "    <a class=dldownload href='#' onclick='" + clk + "' title='Download link target'></a>";
			html += "    <span class=help>" +  xml.find('description').text() + "</span>";
			html += "</div>";
			return html;	
		},
		buildWebserviceEntry : function(xml) {
			var xmlurl =  xml.find('url').text();
			var url = (this.forwardurl != null )? (this.forwardurl+ encodeURIComponent( xmlurl))
					: xmlurl;
			var webservice = xml.attr("name");
			this.fireStoreWebService(webservice, xmlurl);
			var description = xml.find('description').text() ;
			return "<div id=" + webservice + " class=datalinkform >"
			+ "    <ul>"
			+ "      <li>Decription " + description + "</li>"
			+ "      <li>type " +  xml.attr('type')+ "</li>"
			+ "      <li>URL <a href='#' onclick='Modalinfo.openIframePanel(&quot;" +  url + "&quot, &quot;" +  description + "&quot);'>click</a>"+ "</li>"
			+ "    </ul>"
			+ "</div>";
		},
		buildWebserviceForm : function(webservice) {
			if( webservice != null && !this.fireIsWebServiceLoaded(webservice) ) {
				var url = (this.forwardurl != null )? (this.forwardurl+ "?target=" +  encodeURIComponent(this.fireGetUrl(webservice)))
						: this.fireGetUrl(webservice);
				//var url = "forwardxmlresource?target=" +  encodeURIComponent(this.fireGetUrl(webservice));
				var that = this;
				var sliders = new Array();
				var fields = new Array();
				Processing.show("Build form for webservice " + webservice);
				$.ajax({
					type: "GET",
					url: url,
					dataType: "xml",
					error: function (xhr, ajaxOptions, thrownError) {
						Processing.hide();
						Modalinfo.error(url + "\n" + xhr.responseText + "\n" + thrownError);
					},
					success: function(xml) {
						Processing.hide();
						$('#' + webservice).html(''); 
						var baseurl = $(xml).find('baseurl').text();
						var html = "";
						html += "    <a class=dldownload href='#' id='" + webservice + "_submit' title='Submit the query'></a>";
						html += "    <span class=help>" +   $(xml).find('description').first().text() + "</span><br>";
						$('#' + webservice).append(html); 
						$(xml).find('parameter').each(function(){
							var name = $(this).attr('name');
							var id  = webservice + "_" + $(this).attr('name');
							html  = "    <span><b>" +  name + "</b></span>";
							html += "    <span class=help>" +  $(this).find('description').text() + "</span><br>";
							var range = $(this).find('range');
							var type = range.attr('type');
							if( type == "enum" ) {
								html += "&nbsp;&nbsp;<span>Value</span>&nbsp;";
								html += "<select id=" + id + '_input' + ">";
								$(range).find('value').each(function() {	
									var value = $(this);
									var selected = ( value.attr("type") == "default")? "selected": "";
									html += "<option " + selected + ">" + $(this).text() + "</option>";
								});
								html += "</select><br>";
								fields.push(name);
							} else if( type == "range" ) {
								var min=0, max=0, def=0;
								$(range).find('value').each(function() {	
									var value = $(this);
									var type = value.attr("type");
									if( type == "default") {
										def = $(this).text();
									} else if( type == "min") {
										min = $(this).text();
									} else if( type == "max") {
										max = $(this).text();
									}
								});
								var fmin = parseFloat(min);
								var fmax = parseFloat(max);
								var fdef = parseFloat(def);
								if( fmin >= fmax ||fdef < fmin ||fdef > fmax ) {
									Modalinfo.error("Unconsistant range (min=" + min + " max="+ max + " def=" + def + ") no input for this parameter");
								} else {
									html += '&nbsp;&nbsp;<label for="amount">Value</label>'
										+ ' <input type="text" id="' + id + '_input" style="width: 50px; border: 0;font-weight: bold;" value="' + def + '"/>'
										+ '<div style="display: inline-block;width: 300px" id="' + id + '"></div><br>'    ;
									sliders[id] = {id: id, min: fmin, max: fmax, def: fdef};
									fields.push(name );
								}
							} else {
								Modalinfo.error("Unconsistant range type " + type + " no input for this parameter");
							}
							$('#' + webservice).append(html); 
							for( var p in sliders) {
								var slider = sliders[p];
								$( "#" + slider.id ).slider({
									range: "min",
									value: slider.def,
									min: slider.min,
									max: slider.max,
									step: ((slider.max - slider.min)/20),
									slide: function( event, ui ) {
										$( "#" +  $(this).attr("id")+ '_input').val(ui.value );
									}
								});

							}
							that.fireWebServiceLoaded(webservice);
						});
						$( "#" + webservice + "_submit" ).click(function(){
							var query = baseurl;
							for( var f=0 ; f<fields.length ; f++ ) {
								query += "&" + fields[f] + "=" + $('#' + webservice + "_" + fields[f] + '_input').val();
							}
							Modalinfo.openIframePanel(query, webservice);
						});
					}});
			}
		},
		fireGetQuery: function() {
			return $("#" + this.textareaid ).val();
		},		
		/*
		 *	Params: {type, constraints}
		 *	where supported typed are "column" "orderby" "ucd" "position" "relation" "limit"
		 *  Label is used to identify the form  constraints are coming from
		 */
		fireStoreWebService : function(webservice, url) {
			this.listener.controlStoreWebService(webservice, url);
		},
		fireGetUrl: function(webservice) {
			return this.listener.controlGetUrl(webservice);
		},
		fireWebServiceLoaded: function(webservice) {
			this.listener.controlWebServiceLoaded(webservice);
		},
		fireIsWebServiceLoaded: function(webservice) {
			return this.listener.controlIsWebServiceLoaded(webservice);
		}

};


/**
 * Subclass of DataLink_mVc handler
 * @returns {UcdQEditor_mVc}
 */
function CompliantDataLink_mVc(params /* { parentDivId: 'query_div',baseurl, dataobject}*/){
	DataLink_mVc.call(this, params);
	this.dataObject = ('dataobject' in params )? params.dataobject: {};
	this.linkMap = new Array();

	var that = this;
};

/**
 * Method overloading
 */
CompliantDataLink_mVc.prototype = Object.create(DataLink_mVc.prototype, {	
	draw : { 
		value: function() {
			var that = this;
			var url  = (this.forwardurl != null )? (this.forwardurl+ "?target=" + encodeURIComponent( this.baseurl))
					: this.baseurl;
			Processing.show("Fetching Compliant Datalink Description at " + url);
			$.ajax({
				type: "GET",
				url: url,
				dataType: "xml",      
				error: function (xhr, ajaxOptions, thrownError) {
					Processing.hide();
					Modalinfo.error(url + "\n" + xhr.responseText + "\n" + thrownError);
				},
				success: function(xml) {
					Processing.hide();
					if( $(xml).find('VOTABLE').length != 0 ){
						that.processVOTABLEResponse(xml);
					} else { 
						Modalinfo.error(url + "\n doesn't look like a VOTable");
						//that.processXMLResponse(xml);
					}
				}});
		},

	} ,
	processVOTABLEResponse : { 
		value: function(xml) {
			var that = this;
			$(xml).find('RESOURCE[type="results"]').each(function(){
				var fieldNames = new Array();
				$(this).find("FIELD").each(function(){
					var id = $(this).attr("ID");
					if( id != undefined ){
						fieldNames.push(id);
					} else {
						fieldNames.push($(this).attr("name"));
					}
				});
				var type = $(this).find('TR').each(function(index){
					var i=0;
					var obj = new  Object();
					var type = $(this).find('TD').each(function(){
						obj[fieldNames[i]] = $(this).text().trim();
						i++;
					});
					that.linkMap.push(new Link_mVc(obj, xml, index));
				});
			});
			var html = "";
			for( var i=0 ; i<this.linkMap.length ; i++ ){
				html += this.linkMap[i].getHtml();
			}
			Modalcommand.commandPanel("Link Browser", html);
			//$("div.datalinkform").toggle();
			Modalcommand.setDivToggling();

			for( var link in this.linkMap) {
				this.linkMap[link].buildSliders();
				this.linkMap[link].buildRegionEditors(this.dataObject);
			}
		}
	},
	setCutoutRegion : { 
		value: function(params) {
			str = "POLYGON ICRS ";
			var pts = params.region.points
			for( var i=0 ; i<pts.length ; i++) {
				str += pts[i][0] + " " +pts[i][1] + " "; 
			}
			$("textarea[name=cutout]").text(str);
		}
	}

});

function Link_mVc(instance, xml, index) {
	this.linkInstance = instance;
	/**
	 * Array of XML representations of params. 
	 * Each cells contain a reference ref which msut be use with a JQuery selector $(ref)
	 */
	this.params = new Array();
	/**
	 * Link number: requested to get the right data in TABLEDATA
	 */
	this.index = index; 
	this.sliders = new Array();
	this.aladins = new Array();
	this.xml = xml;
	this.setParams();

}
Link_mVc.prototype = {

		setParams : function(){
			var that = this;
			//if( this.linkInstance.semantics != "#this") {
			if( that.linkInstance.service_def != "#") {
				$(this.xml).find('RESOURCE[ID="' + that.linkInstance.service_def + '"]').each(function(){
					$(this).find('PARAM[name="accessURL"]').each(function() {
						that.linkInstance.access_url =  $(this).attr("value");
					});
					$(this).find('GROUP[name="inputParams"]').each(function() {

						$(this).find('PARAM').each(function() {
							var param = {xml: $(this), value : ""};
							var fieldPointer = $(this);
							var x = $(this).find('VALUES');
							/*
							 * IF a field is referenced, its value is taken for the parameter value
							 */
							if( fieldPointer.attr("ref") != null ){
								var ref = fieldPointer.attr("ref");
								param.value = that.getFieldValueByRef(ref);
							}
							/*
							 * If there is no value element attached to the PARAM, will look
							 * if there is a field having the same name as the parameter
							 */
							else if( $(this).find('VALUES').length == 0 ){
								var name = fieldPointer.attr("name");
								param.value = that.getFieldValueByName(name);
							}
							that.params.push(param);
						});
					});
				});
			}
		},
		/**
		 * Get the value of the field identified by ID = fieldRef
		 * in the current column (# this.index)
		 * @param fieldRef
		 * @returns
		 */
		getFieldValueByRef : function(fieldRef){
			var that = this;
			var fieldIndex = -1;
			var retour = null;
			$(this.xml).find('FIELD').each(function(index){
				if( $(this).attr("ID") == fieldRef) {
					fieldIndex = index;
				}
			}); 
			$(this.xml).find('TR').each(function(rowIndex){
				if( rowIndex == that.index) {
					$(this).find('TD').each(function(colIndex){
						if( colIndex == fieldIndex) {
							retour =$(this).text() ;
						}
					}); 
				}
			}); 
			return retour;
		},

		getFieldValueByName : function(fieldRef){
			var that = this;
			var fieldIndex = -1;
			var retour = null;
			$(this.xml).find('FIELD').each(function(index){
				if( $(this).attr("name") == fieldRef) {
					fieldIndex = index;
				}
			}); 
			$(this.xml).find('TR').each(function(rowIndex){
				if( rowIndex == that.index) {
					$(this).find('TD').each(function(colIndex){
						if( colIndex == fieldIndex) {
							retour =$(this).text() ;
						}
					}); 
				}
			}); 
			return retour;
		},

		getSimpleLinkAction: function(){
			if( this.linkInstance.content_type == "application/x-votable+xml;content=datalink") {
				return "<a class='dlinfo' title='Get info about' href='#' onclick='LinkProcessor.fireGetProductInfo(\"" + this.linkInstance.access_url + "\"); return false;'></a>"
				+ "<a class=dldownload href='#' onclick='DataLinkBrowser.startCompliantBrowser(&quot;" +  this.linkInstance.access_url  + "&quot)' title='Download link target'></a>";
			} else if( this.linkInstance.semantics.endsWith("#preview") ){
				return "<a class='dlinfo' title='Get info about' href='#' onclick='LinkProcessor.fireGetProductInfo(\"" + this.linkInstance.access_url + "\"); return false;'></a>"
				+ "<a class=dldownload href='#' onclick='Modalinfo.openIframePanel(&quot;" +  this.linkInstance.access_url  + "&quot)' title='Download link target'></a>";
			} else if( this.linkInstance.semantics.endsWith("#proc") || this.linkInstance.semantics.endsWith("#this")){
				return "<a class='dlinfo' title='Get info about' href='#' onclick='LinkProcessor.fireGetProductInfo(\"" + this.linkInstance.access_url + "\"); return false;'></a>"
				+ "<a class=dldownload href='#' onclick='PageLocation.changeLocation(&quot;" +  this.linkInstance.access_url  + "&quot)' title='Download link target'></a>"
				+ "<a class='dlivoalogo'  href='#' onclick='LinkProcessor.processToSampWithParams(\"" + this.linkInstance.access_url + "\", \"" + this.linkInstance.semantics + "\");' title='broadcast to SAMP'></a>"
				;
			} else {
				return 	"<a class='dlinfo' title='Get info about' href='#' onclick='LinkProcessor.fireGetProductInfo(\"" + this.linkInstance.access_url + "\"); return false;'></a>"
				+  "<a class=dldownload href='#' onclick='Modalinfo.openIframePanel(&quot;" +  this.linkInstance.access_url  + "&quot)' title='Download link target'></a>";	
			}
		},
		getHtml : function(){
			if( this.params.length == 0 ){
				var html = "";
				html += "<fieldset>";
				html += "  <legend><span title='Click on the link name to toggle'>Link <i>"  + this.linkInstance.semantics + "</i></legend><div class=datalinkform>";
				html += this.getSimpleLinkAction();
				html += this.getDescriptionSpan();
				html += "</div></fieldset>";
				return html;
			} else {
				var html = "";
				html += "<fieldset name='" + this.linkInstance.semantics + "'>";
				var that = this;
				html += "  <legend><span title='Click on the link name to toggle'>Link <i>"  + this.linkInstance.semantics + "</i></legend><div class=datalinkform>";
				html += "    <a class=dldownload    href='#' onclick='LinkProcessor.processWithParams(\"" + this.linkInstance.access_url + "\", \"" + this.linkInstance.semantics + "\");' title='Download link target'></a>";
				html += "    <a class='dlivoalogo'  href='#' onclick='LinkProcessor.processToSampWithParams(\"" + this.linkInstance.access_url + "\", \"" + this.linkInstance.semantics + "\");' title='broadcast to SAMP'></a><BR\>";
				html += this.getDescriptionSpan();
				for( var i=0 ; i<this.params.length ; i++) {
					var param = this.params[i];
					var paramName = param.xml.attr("name") ;
					var paramType = param.xml.attr("datatype") ;
					var paramId = paramName + "_" + this.index; // used to buid unique div IDs
					var divId = paramId + "_input";
					var sliderId = paramId + "_input_slider";

					var values;
					var description, de;
					if ( (de = $(param.xml).find('DESCRIPTION')).length != 0 ){
						description = $(de[0]).html() ;
					} else {
						description = "no description provided";
					}

					/*
					 * Constant value
					 */
					if( param.value != null && param.value != ""){
						html += "    <span><b>" + paramName + "</b></span> <span class=help>" + description + "</span><br>"
						+ "<input id='" + divId + "' readonly style='width: 100%;' type='text' value='" 
						+ param.value + "' name='" + param.xml.attr("name") + "'><br>";
						/*
						 * Cutout: open Aladin Lite
						 */
					} else if ("stc:AstroCoordArea" == param.xml.attr("xtype")){
						var divAladin = paramId + "_aladin";
						html += "    <span id='cutoutParam'><b>" + paramName + "</b></span><span class=help> " 
						+ description + "</b></span><br><textarea id='" + divId + "' style='width:100%;' rows='3' name='" 
						+ paramName +"'>POLYGON ICRS</textarea><br><div id='" + divAladin + "' style='width: 400px; height: 400px'></div>";
						this.aladins.push(divAladin);
						/*
						 * Parmas with a defined value range: use either a slider or a choice menu
						 */	
					} else if ( (values = $(param.xml).find('VALUES')).length != 0 ){
						var minField, maxField, option;

						if( (minField = $(values).find("MIN")).length != 0 && (maxField = $(values).find("MAX")).length != 0 ) {
							var range = $(minField).attr("value") + " " + $(maxField).attr("value");
							var min = parseFloat(minField.attr("value"));
							var max = parseFloat(maxField.attr("value"));
							var scaleMin = this.getScaleFactor(min);
							var scaleMax = this.getScaleFactor(max);
							min = scaleMin.normValue;
							max = scaleMax.normValue;
							var def =  (paramName.endsWith("MIN"))? min: (paramName.endsWith("MAX"))?max: (min + max)/2;
							def = (paramType == "int")? Math.round(def) :def ;
							var scaleMention = (scaleMin.factor != 1)? (" (* " + (1/scaleMin.factor) +")"): "" ;
							html += "    <input type='checkbox' name='" + paramName + "' value='takeit' title='uncheck to ignore the parameter' checked><span><b>" + paramName + "</b></span><span class=help>" + description + scaleMention + "</span><br>";
							html += '&nbsp;&nbsp;<label for="amount">Value</label>'
								+ ' <input type="text" name="' + paramName + '" id="' + divId + '" size=10 style="border: 0;font-weight: bold;" value="' + (Math.round(def*1000)/1000) + '"/>'
								+ ' <intput type=hidden value="' + scaleMin.factor + '" id="' + paramId + '_input_factor">'
								+ '<div style="display: inline-block;width: 300px" id="' + sliderId + '"></div><br>'    ;
							this.sliders[sliderId] = {id: divId, type: paramType, min: min, max: max, def: def, scaleFactor: scaleMin.factor};

						} else if( (option = $(values).find("OPTION")).length != 0) {
							html += "    <input type='checkbox' name='" + paramName + "' value='takeit' title='uncheck to ignore the parameter' checked><span><b>" + paramName + "</b></span> <span class=help>" + description + "</span><br>";
							html += ' <select type="text" name="' + paramName + '" id="' + divId + '">';
							option.each(function() {
								var name = $(this).attr("name");
								var value = $(this).attr("value");
								if( name != "" ) {
									name = "(" + name + ")";
								}
								html += "  <option value='" + value + "'>" + value + " " + name + ")</option>";
							});					
							html += ' </select>';


						} else {
							html += "    <input type='checkbox' name='takeit' value='takeit' title='uncheck to ignore the parameter' checked><span class=help>" + paramName + " " + description +"</span><br>FREE<br>";
						}
					} 
				}
				html += "</div></fieldset>";
				return html;
			}

		},
		/*
		 * Active widget must be built after the link HTML has beeb attached to the DOM
		 */
		buildSliders : function(){
			for( sliderId in this.sliders) {
				var slider = this.sliders[sliderId];
				var sliderHandler;
				if( slider.type != "int" ){
					sliderHandler = function( event, ui ) {
						var factor = $( "#" +  $(this).attr("id").replace("_slider", "_factor")).attr("value");
						var value  = Math.round(1000*ui.value)/1000;
						$( "#" +  $(this).attr("id").replace("_slider", "")).val(value );
					}
				} else {
					sliderHandler = function( event, ui ) {
						var factor = $( "#" +  $(this).attr("id").replace("_slider", "_factor")).attr("value");
						var value  = Math.round(ui.value);
						$( "#" +  $(this).attr("id").replace("_slider", "")).val(value );
					}					
				}
				$( "#" + sliderId ).slider({
					range: "min",
					value: slider.def,
					min: slider.min,
					max: slider.max,
					step: ((slider.max - slider.min)/20),
					slide: sliderHandler
				});

			}
		},
		/*
		 * Active widget must be built after the link HTML has beeb attached to the DOM
		 */
		buildRegionEditors : function(fovObject){
			for( var i=0 ; i<this.aladins.length ; i++ ) {
				var div = this.aladins[i];
				var message = "";
				if( div.length != 0){
					var points = [];
					if( !('s_ra' in fovObject) || !('s_dec' in fovObject) ) {
						message = "CUTOUT Link: Cannot access object position: set it by hand";
					} else {
						var ra = fovObject.s_ra;
						var dec = fovObject.s_dec;
						if( ra < 0 || ra > 360 || dec <-90 || dec >90 ) {
							message = "CUTOUT Link: No valid position for this object: set it by hand";
						} else {
							var fov;
							/*
							 * Take 1' as FoV by default
							 */
							if ( !(('s_fov' in fovObject) && (fov = fovObject.s_fov) > 0 && fov < 180 )){
								fov = 1/60;
							}

							size = fov/2;
							var ra_min = ((ra - size) < 0)  ? (360 + (ra - size)): ((ra - size) > 360)? (360 - (ra - size)): (ra - size);
							var dec_min = ((dec - size) < -90)? (-180 - (dec - size)):  ((dec - size) >  90)? ( 180 - (dec - size)):   (dec - size);
							var ra_max = ((ra + size) < 0)  ? (360 + (ra + size)): ((ra + size) > 360)? (360 - (ra + size)): (ra + size);
							var dec_max = ((dec + size) < -90)? (-180 - (dec + size)): ((dec + size) >  90)? ( 180 - (dec + size)):   (dec + size);

							points = [
							          ra_min, dec_min,
							          ra_min, dec_max,
							          ra_max, dec_max,
							          ra_max, dec_min,
							          ];								
						}
					}
					var regionEditor = new RegionEditor_mVc  (div
							, function(params){			
						str = "polygon ICRS ";
						var pts = params.region.points
						for( var i=0 ; i<pts.length ; i++) {
							str += parseFloat(pts[i][0]).toFixed(6) + " " +parseFloat(pts[i][1]).toFixed(6) + " "; 
						}
						$("#" + div.replace("aladin", "input")).text(str);
					}
					, [fovObject.s_ra ,fovObject.s_dec ] 
					); 		
					regionEditor.init();	
					$(".aladin-box").css("z-index", (9999));
					if( message != ""){
						Modalinfo.info(message);								
					}

					if( points.length > 0 ) {
						var initRegion = {type: "array", value: points};
						Processing.show("init Aladin");
						setTimeout(function() {
							regionEditor.setInitialValue( initRegion );
							Processing.hide();
						}
						, 1000);
						/*
						 * We need to draw a polygon in any case; otherwise AL loops for even in the view.redraw function 
						 * from the second time the modal is open
						 */
					} else {
						var initRegion = {type: "array", value: [1,2,1.1, 2, 1.1, 2.1]};
						Processing.show("init Aladin");
						setTimeout(function() {
							regionEditor.setInitialValue( initRegion );
							regionEditor.clean();						
							$("#" + div.replace("aladin", "input")).text("POLYGON ICRS");

							Processing.hide();
						}
						, 1000);

					}
				}
			}
		},
		getScaleFactor : function(value){
			var stringValue = value.toString();
			var tvalue = value;
			var step;
			var factor=1.0;
			if( value > -1 && value < 1 ){
				step = 10.
			} else {
				return ({normValue: tvalue, factor: 1});
			}
			while( (step == 10 && tvalue > -1 && tvalue < 1) || (step == 0.1 && (tvalue > 10 || tvalue < -10)) ){
				tvalue *= step;
				factor *= step;
				stringValue = tvalue.toString();
			}
			return ({normValue: tvalue, factor: factor});
		},
		getDescriptionSpan : function() {
			var html = ""
			var voc = linkVocabulary[this.linkInstance.semantics];
			if( voc != undefined ) {
				html += "    <span class=help>" + voc ;
			}
			if( this.linkInstance.description.length > 0 ) {
				if( html.length > 0 ) {
					html += "<br>";
				} else {
					html += "    <span class=help>";
				}
				html += this.linkInstance.description ;
			}
			if( html.length > 0 ) {
				html += "</span><br>";
			}
			return html
		}
}

/**
 * Singleton class providing the fonction processing the link accesses 
 */
LinkProcessor = function() {
	var processWithParams = function(access_url, semantic) {
		var fieldset = $("fieldset[name='" + semantic + "']");
		var params = new Array();
		fieldset.find("input[type='text'], textarea").each(function(){
			var name = $(this).attr("name");
			var check =  $(this).parent().children('input[type=checkbox][name=' + name + ']');
			if( (name != undefined && name != null  && name != "" )  &&  (check.length == 0 ||  check.prop('checked')) ) {
				var id = $(this).attr("id");
				var factorEle = $("#" + id + "_factor");
				var value;
				if( factorEle.length == 0 ){
					value =  $(this).val();
				} else {
					value =  $(this).val()/ factorEle.attr("value");
				}

				params.push(name + "=" + encodeURIComponent(value) );
			}
		})
		fieldset.find("select").each(function(){
			var id = $(this).attr("id");
			var value =  $(this).val();
			params.push($(this).attr("name") + "=" + encodeURIComponent(value) );
		})
		var retour = access_url + "?" + params.join("&");
		PageLocation.download(retour);
	}; 
	var processToSampWithParams = function(access_url, semantic) {
		var fieldset = $("fieldset[name=" + semantic + "]");
		var params = new Array();
		fieldset.find("input[type='text']").each(function(){
			var id = $(this).attr("id");
			var factorEle = $("#" + id + "_factor");
			var value;
			if( factorEle.length == 0 ){
				value =  $(this).val();
			} else {
				value =  $(this).val()/ factorEle.attr("value");
			}
			params.push($(this).attr("name") + "=" + encodeURIComponent(value) );
		})
		fieldset.find("select").each(function(){
			var id = $(this).attr("id");
			var value =  $(this).val();
			params.push($(this).attr("name") + "=" + encodeURIComponent(value) );
		})
		var retour = access_url + "?" + params.join("&");
		WebSamp_mVc.fireSendVoreport(retour, null, null);
	}; 
	var fileDownload = function(access_url) {
		$.fileDownload($(this).prop(access_url), {
			preparingMessageHtml: "We are preparing your report, please wait...",
			failMessageHtml: "There was a problem generating your report, please try again."
		});
	};
	var fireGetProductInfo = function(url) {
		Processing.show("Waiting on product info");

		$.getJSON("getproductinfo", {jsessionid: "", url: url}, function(jsdata) {
			Processing.hide();
			if( Processing.jsonError(jsdata, "Cannot get product info") ) {
				return;
			} else {
				retour = "url: " + url + "\n";
				$.each(jsdata, function(k, v) {
					retour += k + ": " + v  + "\n";
				});
				Modalinfo.info(retour, "Product Info");
			}
		});
	}	;	

	/*
	 * exports
	 */
	var pblc = {};
	pblc.processWithParams = processWithParams;
	pblc.processToSampWithParams = processToSampWithParams;
	pblc.fileDownload = fileDownload;
	pblc.fireGetProductInfo = fireGetProductInfo;
	return pblc;

}();



console.log('=============== >  DataLink_v.js ');

DataLink_mvC = function(view, model){
	/**
	 * listen to the view
	 */
	var vlist = {
			controlStoreWebService : function(webservice, url){
				return model.storeWebService(webservice, url);
			},
			controlGetUrl : function(webservice){
				return model.getUrl(webservice);
			},
			controlWebServiceLoaded : function(webservice){
				model.webServiceLoaded(webservice);
			},
			controlIsWebServiceLoaded : function(webservice){
				return model.isWebServiceLoaded(webservice);
			}
	};
	view.addListener(vlist);

	var mlist = {
	};
	model.addListener(mlist);
};

console.log('=============== >  DataLink_c.js ');

function Segment(polygoneNodes /*canvas*/)
{
	var alfa;
	var beta;	
	var node = [];
	node = polygoneNodes;
	var nodesegmentos;

	this.IsCursorOn = function(x,y)
	{
		var result;
		
		//crear los segmentos:
		nodesegmentos = NumSegment(node);
		
		//si es un rectangulo
		if(seg = IsRectangle(x,y))
		{
			//calcular la distancia
			result = Distance(seg,x,y);
			return result;
		}			
			
	};
	
	//funcion para saber si se crea el rectangulo
	function IsRectangle(coorx, coory)	
	{	
		
		var x = parseInt(coorx);
		var y = parseInt(coory);
		var nodeXtremity = {};			
		
		var xa,xb,ya,yb;				
		
		for(var i in nodesegmentos)
		{				
			var xmin, xmax;
			var ymin, ymax;
			
			xa = node[nodesegmentos[i].A].cx;
			ya = node[nodesegmentos[i].A].cy;			
			xb = node[nodesegmentos[i].B].cx;
			yb = node[nodesegmentos[i].B].cy;
					
			xmin = (parseInt(xa) > parseInt(xb) )? xb:xa;
			xmax = (parseInt(xa) > parseInt(xb) )? xa:xb;
			
			ymin = (parseInt(ya) > parseInt(yb) )? yb:ya;
			ymax = (parseInt(ya) > parseInt(yb) )? ya:yb;

			if(x >= xmin && x <= xmax)
			{				
				if(y >= ymin && y <= ymax )
				{
					seg = {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};
					if( ( dis = Distance(seg,x,y) ) != -1)
					{
					return {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};	
					}
					
				}
			}
			if(xmax === xmin)
			{
				if(y >= ymin && y <= ymax )
				{
					seg = {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};
					if( ( dis = Distance(seg,x,y) ) != -1)
					{
					return {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};	
					}
				}
			}
			if(ymin === ymax)
			{	
				if(x > xmin && x < xmax)
				{
					seg = {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};
					if( ( dis = Distance(seg,x,y) ) != -1)
					{
					return {xA:xa, yA:ya, xB:xb, yB:yb, segmento:i};	
					}	
				}				
			}
		}	
	}

	//funcion para calcular la distancia del punto M(x,y) de los segmentos: A(xa,ya) y B(xb,yb)
	function Distance(seg,x,y)
	{
		//console.log('puedes calcular distancia');	
		var recta;
		var distancia;
		
		var h,v;
		
		if((v = Vertical(seg, x)) != -1)
		{
			return {flag: "vertical", segmento: seg};
			
		}else if((h = Horizontale(seg, y)) != -1)
		{		
			return {flag: "horizontal", segmento: seg};
		}
		else if(v == -1 && h == -1)
		{
			
			var alfa = CalculerAlfa(seg);
			var beta = Beta(seg);			
			
			recta = Math.abs( ( (alfa * parseInt(x)) + parseInt(y) + beta) );
			distancia = (recta / Math.sqrt(((alfa * alfa)+1)));
			
			//if(distancia <= 3 && distancia >= 0)
			if(distancia <= 2 && distancia >= 0)
			{
				return {flag: "distancia", segmento: seg, alfa: alfa, beta: beta};
			}
		}

		return -1;
		
	}
	
	//function para crear los segmentos a partir de los nodos
	function NumSegment(array)
	{
		var numsegmentos = []; //variable para almacenar el numero de segmentos
		var temp; //variable para contar los nodos "i"
		var segmentoini, segmentofin;
		
		//recorrer los nodos
		for(var i in array)
		{
			if(segmentoini == undefined)
			{
				segmentoini = i;
			}			
			else if(segmentofin == undefined)
			{
				segmentofin = i;
			}				
			
			//almacenar segmentos
			if(segmentoini != undefined && segmentofin != undefined)
			{
				numsegmentos.push
				({
					A: segmentoini,
					B: segmentofin
				});
				
				segmentoini = segmentofin;
				segmentofin = undefined;
			}
			
			if(parseInt(node.length - 1) == i)
			{				
				numsegmentos.push
				({
					A: (node.length -1),
					B: 0
				});
			}
			
		}				
		return numsegmentos;
	}
	
	//dibujar el segmento
	function DrawnLine()
	{
		context.beginPath();
		context.moveTo(125,158);
		context.lineTo(250,158);
	
		
		context.moveTo(250,158);		
		context.lineTo(250,100);
		
		context.moveTo(250,100);		
		context.lineTo(125,158);
		
		context.stroke();
		context.closePath();		
		
		for(var i in node)
		{
			context.beginPath();
			context.arc(node[i].cx,node[i].cy,5, 0, Math.PI * 2,true);
			context.fillStyle = "blue";
		    context.fill();
			context.stroke();
			context.closePath();
		}
	}
	
	//Obtener el valor alfa
	function CalculerAlfa(seg)
	{		
		alfa = -((seg.yB - seg.yA) / (seg.xB - seg.xA));
		
		return alfa;
	}
	
	//Obtener el valor beta
	function Beta(seg)
	{		
		beta = -(alfa *  seg.xA)- seg.yA;
		
		return beta;
	}
	
	//Calcular la distancia de un segmento horizontal
	function Horizontale(seg, y)
	{
		var horizontal;
		var coory = parseInt(y);
		
		horizontal = Math.abs(seg.yA - coory);
		
		if(horizontal <= 1 && horizontal >= 0)		
			return horizontal;
			else
				return -1;	
	}
	
	//Calcular la distancia de un segmento vertical
	function Vertical(seg, x)
	{
		var vertical;
		var coorx = parseInt(x);
		vertical =  Math.abs(seg.xA - coorx);				
		
		if(vertical <= 1 && vertical >= 0)		
		return vertical;
		else
			return -1;		
	}

	//intersertion de segments
	this.Itersection = function(nodeselected,status)
	{
		var numseg = NumSegment(node);
		var lastseg = numseg.length - 2;
		var firstnode = 0;
		var dx,dy;		
		var d=-1;
		
		nodeselected = parseInt(nodeselected);
		
		if(status === false)
		{
			
			if(numseg.length > 3)
			{
				if(nodeselected != 0)
				{
					xa1 = node[numseg[lastseg].A].cx;
					ya1 = node[numseg[lastseg].A].cy;			
					xb2 = node[numseg[lastseg].B].cx;
					yb2 = node[numseg[lastseg].B].cy;
					var nodenumberA =  parseInt(numseg[lastseg].A); 
					var nodenumberB =  parseInt(numseg[lastseg].B);
					
					for(var i in numseg)
					{					
						xa3 = node[numseg[i].A].cx;
						ya3 = node[numseg[i].A].cy;			
						xb4 = node[numseg[i].B].cx;
						yb4 = node[numseg[i].B].cy;						
						
						d = distance(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4);									
						
						if( d != -1)
						{
							//  ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
							dx = ((xa3-xb4)*(xa1*yb2-ya1*xb2)-(xa1-xb2)*(xa3*yb4-ya3*xb4))/d;
							//   ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
							dy = ((ya3-yb4)*(xa1*yb2-ya1*xb2)-(ya1-yb2)*(xa3*yb4-ya3*xb4))/d; 
																																										
							var resultado = ResultadoSegmento(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4 , dx , dy);									
							
							//si es diferente de nulo hay una interseccion
							if(resultado != -1)
							{
								if(i != (numseg.length -1))
								{
									if(xb4 != xa1 && yb4 != ya1)
									{
										//if(xa1 != xa3 && ya1 != ya3)
											return { x1:xa1, y1:ya1 , x2:xb2 , y2:yb2 , seginit:lastseg, segfin:i, nA:nodenumberA, nB:nodenumberB};
									}
										
								}													
							}					
						}
					
					}
				}
				else if(nodeselected === 0)
				{
					xa1 = node[numseg[firstnode].A].cx;
					ya1 = node[numseg[firstnode].A].cy;			
					xb2 = node[numseg[firstnode].B].cx;
					yb2 = node[numseg[firstnode].B].cy;
					var nodenumberA =  parseInt(numseg[firstnode].A); 
					var nodenumberB =  parseInt(numseg[firstnode].B);
					
					//invertir el orden de los segmentos
					numseg.reverse();
					
					for(var i in numseg)
					{												
						if(i != 0)
						{

							xa3 = node[numseg[i].A].cx;
							ya3 = node[numseg[i].A].cy;			
							xb4 = node[numseg[i].B].cx;
							yb4 = node[numseg[i].B].cy;						
							
							d = distance(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4);									
							
							if( d != -1)
							{
								//  ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
								dx = ((xa3-xb4)*(xa1*yb2-ya1*xb2)-(xa1-xb2)*(xa3*yb4-ya3*xb4))/d;
								//   ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
								dy = ((ya3-yb4)*(xa1*yb2-ya1*xb2)-(ya1-yb2)*(xa3*yb4-ya3*xb4))/d; 
																																											
								var resultado = ResultadoSegmento(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4 , dx , dy);									
								
								//si es diferente de -1 hay una interseccion
								if(resultado != -1)
								{
									if(i != (numseg.length -1))
									{
										if(xb2 != xa3 && yb2 != ya3)
										{
											return { x1:xa1, y1:ya1 , x2:xb2 , y2:yb2 , seginit:lastseg, segfin:i, nA:nodenumberA, nB:nodenumberB};
										}
											
									}													
								}					
							}
						}					
					
					}
				}
			}
		}
		else if(status)
		{
			var seg1 ={} , seg2 = {};
			var option;
			var resseg = [];
			
			if(numseg.length > 3)
			{				
				if(nodeselected === 0)
				{
					//segmento 1
					seg1.xA = node.length - 1;
					seg1.xB = nodeselected;
					//segmento 2
					seg2.xA = nodeselected;
					seg2.xB = nodeselected + 1;
				}
				else if(nodeselected === (node.length - 1) )
				{
					//segmento 1
					seg1.xA = nodeselected - 1;
					seg1.xB = nodeselected;
					//segmento 2
					seg2.xA = nodeselected;
					seg2.xB = 0;
				}
				else
				{
					//segmento 1
					seg1.xA = nodeselected - 1;
					seg1.xB = nodeselected;
					//segmento 2
					seg2.xA = nodeselected;
					seg2.xB = nodeselected + 1;
				}														
				
				for(var i in numseg)
				{																			
					if(parseInt(numseg[i].A) === seg1.xA && parseInt(numseg[i].B) == seg1.xB)
					{
						continue;
						//console.log('algo');
						
					}else if(parseInt(numseg[i].A) === seg2.xA && parseInt(numseg[i].B) == seg2.xB)
					{
						continue;
					}
					else
					{	
							//comparar con el segmento 1						
							xa1 = node[seg1.xA].cx;
							ya1 = node[seg1.xA].cy;			
							xb2 = node[seg1.xB].cx;
							yb2 = node[seg1.xB].cy;
							
							xa3 = node[numseg[i].A].cx;
							ya3 = node[numseg[i].A].cy;			
							xb4 = node[numseg[i].B].cx;
							yb4 = node[numseg[i].B].cy;
							
							d = distance(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4);												
							
							
							if(d != -1)
							{
								//((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
								dx = ((xa3-xb4)*(xa1*yb2-ya1*xb2)-(xa1-xb2)*(xa3*yb4-ya3*xb4))/d;
								//((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
								dy = ((ya3-yb4)*(xa1*yb2-ya1*xb2)-(ya1-yb2)*(xa3*yb4-ya3*xb4))/d; 
																																											
								var resultado = ResultadoSegmento(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4 , dx , dy);
								
								//si es diferente de -1 hay una interseccion
								if(resultado != -1)
								{									
										if(xa1 != xb4 && ya1 != yb4)
										{											
											resseg.push
											(
												{
													x1:xa1,												
													y1:ya1 , 
													x2:xb2 , 
													y2:yb2
												}
											);
										}																							
								}				
							}							
							
							//comparar con el segmento 2							
							xa1 = node[seg2.xA].cx;
							ya1 = node[seg2.xA].cy;			
							xb2 = node[seg2.xB].cx;
							yb2 = node[seg2.xB].cy;
							
							d = distance(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4);												
							
							if(d != -1)
							{
								//((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
								dx = ((xa3-xb4)*(xa1*yb2-ya1*xb2)-(xa1-xb2)*(xa3*yb4-ya3*xb4))/d;
								//((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
								dy = ((ya3-yb4)*(xa1*yb2-ya1*xb2)-(ya1-yb2)*(xa3*yb4-ya3*xb4))/d; 
																																											
								var resultado = ResultadoSegmento(xa1,ya1,xb2,yb2,xa3,ya3,xb4,yb4 , dx , dy);
								
								//si es diferente de -1 hay una interseccion
								if(resultado != -1)
								{
										if(xa1 != xb4 && ya1 != yb4)
										{											
											resseg.push
											(
												{
													x1:xa1,												
													y1:ya1 , 
													x2:xb2 , 
													y2:yb2
												}
											);
										}																							
								}				
							}							
						
						if(resseg.length > 1)
						{
							return resseg;
						}						
					}
				}				
			}						
		}
			
			return -1;
	};	
	
	function distance(x1,y1, x2,y2, x3,y3, x4,y4)
	{
		// (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4)
		var d = ((x1-x2)*(y3-y4)) - ((y1-y2)*(x3-x4));
		
		if (d == 0)
		{
		 return -1;
		} 			 
		else
		{
		 return d; 
		} 	
	}
	
	function ResultadoSegmento(x1,y1,x2,y2,x3,y3,x4,y4 , x , y)
	{
		//valida que los segmentos no sean verticales
		if (y < Math.min(y1,y2) || y > Math.max(y1,y2)) return -1;
		if (y < Math.min(y3,y4) || y > Math.max(y3,y4)) return -1;
		
		//valida que los segmentos no sean paralelos
		if (x < Math.min(x1,x2) || x > Math.max(x1,x2)) return -1;
		if (x < Math.min(x3,x4) || x > Math.max(x3,x4)) return -1;
		
		return 2;
	}
};

console.log('=============== >  Segment.js ');

/**
 * Model processing the draw canvas
 * 
 * Author Gerardo Irvin Campos yah
 */
function RegionEditor_Mvc(canvas, canvaso, aladin){

	this.node = [];	
	this.canvas = canvas[0];
	this.canvaso = canvaso[0];
	this.context = this.canvas.getContext('2d');
	this.contexto = this.canvaso.getContext('2d');
	//this.aladin parameters:
	this.aladin = aladin;	
	this.overlay = null;
	this.skyPositions = null;

}

RegionEditor_Mvc.prototype = {

		DrawNode: function (data){
			for(var i in data)
			{
				this.context.beginPath();
				this.context.arc(data[i].cx, data[i].cy, data[i].r, 0, Math.PI * 2,true);     	      
				this.context.fillStyle = "blue";
				this.context.fill();
				this.context.stroke();	 
				this.context.closePath();	  
			} 	     
		},

		//Drawn Line
		DrawnLine: function (startingNode,x,y,result) {
			if(result != null)
			{					
				this.context.beginPath();
				this.context.lineCap="round";

				for(i in this.node)
				{
					if(this.node[result.N] == i)
						this.context.moveTo(this.node[result.N].cx,this.node[result.N].cy);

					this.context.lineTo(this.node[i].cx,this.node[i].cy);				
				}					

				this.context.closePath(); 
				this.context.strokeStyle = 'lime';
				// this.context.lineWidth = 3;
				this.context.stroke();	
			}
			else
			{
				this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);		
				this.context.beginPath();
				this.context.lineCap="round";
				this.context.moveTo(this.node[startingNode].cx,this.node[startingNode].cy);		
				this.context.lineTo(x,y);
				this.context.closePath(); 
				this.context.strokeStyle = 'lime';
				//this.context.lineWidth = 3;
				this.context.stroke();
			}
		},

		//this.Redrawn line and this.node
		Redrawn : function (result)
		{				
			this.CanvasUpdate();
			for(var i in this.node)
			{
				this.context.beginPath();
				this.context.arc(this.node[i].cx, this.node[i].cy, this.node[i].r, 0, Math.PI * 2,true);     	      
				this.context.fillStyle = "red";
				this.context.fill();
				this.context.stroke();	 
				this.context.closePath();	        	    
			} 		

			this.DrawnLine(0,0,0,result);
		},	

		//Clean the this.canvas
		CanvasUpdate : function ()
		{
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
			this.contexto.clearRect(0, 0, this.canvas.width, this.canvas.height);
			this.contexto.drawImage(this.canvas, 0, 0);
		},

		//Convert a Array to Object
		ArrayToObject: function (data)
		{
			NodeTemp = [];
			for(i in data)
			{
				NodeTemp.push
				(
						{
							cx: data[i][0] ,
							cy: data[i][1],
							r:5
						}
				);
			}

			this.node=[];
			this.node = NodeTemp;
		},

		//Fuction pour obtenir le hautor du polygon
		GetHeight: function (array)
		{		
			var Ramax = null, Ramin = null;
			var finaltemp;
			var largeur;

			for(i in array)
			{
				temp = array[i][0];        	

				if(Ramax == null)
				{
					Ramax = temp;
				}
				else if(temp >= Ramax)
				{
					Ramax = temp;
				}

				if(Ramin == null)
				{
					Ramin = temp;
				}
				else if(temp <= Ramin )
				{
					Ramin = temp;
				}
			}

			largeur = (Ramax -Ramin);

			if(largeur > 180)
			{
				largeur = 360 - largeur;
			}

			return { ramax: Ramax, ramin: Ramin , largeur: largeur  };
		},

		//function pour obtenir le numero de segment et construir un segment
		NumeroSegmen : function ()
		{	
			var TotalNodes = this.node.length;		
			var segmentoini, segmentofin;	
			var total = [];

			for(var j=0; j<this.node.length; j++)
			{
				if(segmentoini == undefined)
					segmentoini = j;
				else if(segmentofin == undefined){
					segmentofin = j; 
				}

				if(segmentoini != undefined && segmentofin != undefined)
				{
					total.push
					({
						A: segmentoini,
						B: segmentofin
					});

					segmentoini = segmentofin;
					segmentofin = undefined;
				}
			}

			total.push
			({
				A: (this.node.length  - 1),
				B: 0
			});

			//console.log('total: ' + total.length);
			return total;
		},

		//function pour obtenir le hauteur de un polygone
		GetWidth: function (array)
		{		
			var Decmax = null, Decmin = null;	
			var temp;
			var width;

			for(i in array)
			{
				temp = (array[i][1]);        	

				if(Decmax == null)
				{
					Decmax = temp;
				}
				else if(temp >= Decmax)
				{
					Decmax = temp;
				}

				if(Decmin == null)
				{
					Decmin = temp;
				}
				else if(temp <= Decmin )
				{
					Decmin = temp;
				}
			}

			width = (Decmax - Decmin);

			if(width > 180)
			{
				width = 360 - width;
				//console.log('width 360');
			}

			return { decmax: Decmax, decmin: Decmin , width: width  };
		},

		//function para crear una grafica en el this.canvas
		DrawGrafic: function (canvas1)
		{
			var canvasgraf =  canvas1;
			var ancho = canvasgraf.width;
			var alto = canvasgraf.height;

			var contextGrafic = canvasgraf.getContext('2d');
			var contador = 20;
			var contador2 = 20;

			//console.log("ancho: " + ancho);
			//console.log("alto: " + alto);

			for(var i =0; i < alto ; i++)
			{

				this.contextGrafic.beginPath();

				if(i === 0)
				{
					this.contextGrafic.moveTo( i + 20 , 10);
					this.contextGrafic.lineTo( i + 20, alto);
					this.contextGrafic.fillStyle="black";
					this.contextGrafic.font = "bold 8px sans-serif";
					this.contextGrafic.fillText("0",i + 15 , 20);
				}
				else 
				{
					this.contextGrafic.moveTo( i + contador , 20);
					this.contextGrafic.lineTo( i + contador , alto);
					this.contextGrafic.fillStyle="black";
					this.contextGrafic.font = "bold 8px sans-serif";
					this.contextGrafic.fillText(i,(i+contador)-3 , 20);
				}

				this.contextGrafic.closePath(); 
				this.contextGrafic.strokeStyle = 'yellow';
				//this.context.lineWidth = 3;
				this.contextGrafic.stroke();	

				contador = parseInt( contador + 20);

			}

			for(var i =0; i < ancho ; i++)
			{

				this.contextGrafic.beginPath();
				this.contextGrafic.lineCap="round";

				if(i === 0)
				{
					this.contextGrafic.moveTo( 12 , i + 20 );
					this.contextGrafic.lineTo( ancho , i + 20);	
				}
				else 
				{
					this.contextGrafic.moveTo( 12  , 0 + contador2);
					this.contextGrafic.lineTo( ancho , 0 + contador2);
					this.contextGrafic.font = "bold 8px sans-serif";		     
					this.contextGrafic.fillStyle="black";
					this.contextGrafic.fillText(i, 3, (0+ contador2)+3);
				}

				this.contextGrafic.closePath(); 
				this.contextGrafic.strokeStyle = 'brown';
				//this.context.lineWidth = 3;
				this.contextGrafic.stroke();	
				contador2 = parseInt( contador2 + 20);	    	       
			}  
		},

		isEmpty: function()
		{
			if(this.node.length == 0)
				return true;		
			else
				return false;
		},

		//function que permet de ajouter this.nodes
		addNode: function(x, y,startingNode,polygonestatus)
		{					
			if(polygonestatus)
			{
				var newNode = {};
				var lastnode = {};
				var position = parseInt(startingNode[0].position);

				newNode.cx = startingNode[0].cx;
				newNode.cy = startingNode[0].cy;
				newNode.r = startingNode[0].r;

				if(this.node.length === position)
				{				
					lastnode.cx = this.node[(this.node.length -1)].cx;
					lastnode.cy = this.node[(this.node.length -1)].cy;
					lastnode.r = 5;

					//agregar el nodo
					this.node.splice((this.node.length -1), 1 , lastnode,newNode);				
				}
				else
				{
					lastnode.cx = this.node[startingNode[0].position].cx;
					lastnode.cy = this.node[startingNode[0].position].cy;
					lastnode.r = 5;

					//agregar el nodo
					this.node.splice(startingNode[0].position, 1 ,newNode, lastnode);
				}														
				this.Redrawn(0);
			}
			else
			{
				var flag = typeof(startingNode);
				if(flag != "object")
				{
					if(startingNode == 0 && this.node.length > 1)
					{		
						this.node.unshift
						(
								{
									cx: x,
									cy: y,
									r: 5,	                            
								}
						);
					}
					else
					{
						this.node.push
						(
								{
									cx: x,
									cy: y,
									r: 5            
								}
						);
					}
					this.DrawNode(this.node);
				}	
				else
				{

					if(startingNode != undefined /*&& startingNode.B != undefined*/)
					{					
						var addnode ={};
						var preview ={};					

						preview.cx = startingNode.segmento.xA;
						preview.cy = startingNode.segmento.yA;
						preview.r = 5;

						addnode.cx = x;
						addnode.cy = y;
						addnode.r = 5;

						this.node.splice(startingNode.segmento.segmento, 1 , preview , addnode);
						var renode =  this.node;
						this.Redrawn(0);

					}
				}			          		         
			}

			//console.log('this.node add: ' + this.node.length);        
		},

		//function que permet obtener le numero de this.node
		getNode: function(x,y)
		{
			var dx=0 , dy=0;
			var result = 0;

			for(var i in this.node)
			{	             
				dx = x - this.node[i].cx;
				dy = y - this.node[i].cy;  
				//var result =Math.sqrt(dx * dx + dy * dy);
				var result = dx * dx + dy * dy;

				if(result <= 25)
				{	    
					//console.log('i: ' + i);
					return i;	
				}
			}
			return -1;
		},

		//function pour obtenir les deux this.nodes qui forme un segment
		getSegment: function(clickedNode)
		{		
			var pointA=0 ,pointB=0;

			if(clickedNode == 0)
			{		
				//console.log('nodo 0');
				pointA = (parseInt(clickedNode) +1);
				pointB = (this.node.length -1);
			}
			else if(clickedNode == (this.node.length -1))
			{			
				//console.log('nodo final:' + (this.node.length -1));
				pointA = parseInt((this.node.length -1) -1);
				pointB = 0;			
			}
			else if(clickedNode != 0 && clickedNode != (this.node.length -1))
			{	
				//console.log('otro this.node');
				pointA = (parseInt(clickedNode)+1);
				pointB = (parseInt(clickedNode)-1);			
			}
			return {A :pointA, B:pointB, N:clickedNode};
		},

		//function pour effacer le this.canvas
		canvasUpdate: function()
		{		
			this.contexto.drawImage(this.canvas, 0, 0);
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);		
		},

		//function pour diseigner les lignes
		drawHashline: function(startingNode,x,y)
		{						
			this.DrawnLine(startingNode,x,y);	   	   					
		},	

		//function pour effacer un ligne
		CleanLine: function()
		{	
			//this.contexto.clearRect(0, 0, this.canvas.width, this.canvas.height);
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
		},

		//function pour savoire si un this.node es un extemite
		isExtremity: function(clickedNode)
		{
			if(clickedNode == 0 || clickedNode == (this.node.length -1))
			{		
				return true;								
			}				
			return false;

		},

		//function que permet de fermer un polygon
		closePolygone: function(clickedNode , startingNode)
		{		
			if(clickedNode == startingNode)
			{
				return false;	
			}
			else if(clickedNode == 0 && startingNode == (this.node.length -1))
			{		
				for(var i in this.node)
				{
					this.context.beginPath();
					this.context.arc(this.node[i].cx, this.node[i].cy, this.node[i].r, 0, Math.PI * 2,true);     	      
					this.context.fillStyle = "red";
					this.context.fill();
					this.context.stroke();	 
					this.context.closePath();	  		        
				}  
				return true;
			}
			else if(clickedNode == (this.node.length -1) && startingNode == 0 )
			{			
				for(var i in this.node)
				{
					this.context.beginPath();
					this.context.arc(this.node[i].cx, this.node[i].cy, this.node[i].r, 0, Math.PI * 2,true);     	      
					this.context.fillStyle = "red";
					this.context.fill();
					this.context.stroke();	 
					this.context.closePath();	  		        
				} 
				return true;
			}			
			return false;
		},

		//function pour bouger un this.node et ses deux segments de le poligone
		Drag: function(clickedNode, x,y,result)
		{
			var segmentfirst;
			var segmentlast;
			var flag;
			var resultado = [];								

			//set new values
			this.node[clickedNode].cx = x;
			this.node[clickedNode].cy = y;	

			this.node[result.N].cx = x;
			this.node[result.N].cy = y;					

			this.Redrawn(result);		
		},

		//function pour garder les valeur de alafin lite et les convertir en valeurs de this.canvas("pixel")
		almacenar: function()
		{			
			//console.log('mesage this.almacenar');
			//console.log('this.skyPositions: ' + this.skyPositions);
			if(this.skyPositions != null)
			{
				//console.log('this.skyPositions' + this.skyPositions);
				//console.log('this.node' + this.node);					
				this.node = [];
				this.skyPositions.pop();

				for (var k=0; k<this.skyPositions.length; k++) 
				{
					this.node.push(this.aladin.world2pix
							(
									this.skyPositions[k][0], 
									this.skyPositions[k][1]								
							));								
				}	

				this.ArrayToObject(this.node);

				//console.log('this.node: ' + typeof(this.node));
				/*for(i in this.node)
				{	
					console.log('i : ' + i);
					console.log('this.node x: ' + this.node[i].cx);
					console.log('this.node y: ' + this.node[i].cy);				
				}*/

				this.Redrawn(this.node);	
			}

		},		

		//function pour effacer le poligone de this.aladin lite quand passe a mode edition
		DeleteOverlay :  function()
		{
			if (this.overlay != null) 
			{			 	      
				//console.log('this.skyPositions: ' + this.skyPositions);
				//console.log('A: ' + typeof(A));
				this.overlay.addFootprints(A.polygon(this.skyPositions));
				this.overlay.removeAll();
				this.overlay.overlays = [];
				//console.log('this.overlay' + this.overlay);			           
			}	        	 
		},

		//function pour obtenir les valeurs de le polygon et creer le polygon en adalin lite
		recuperar: function()
		{
			/*
			 * When the position are set from outside, the node remains empty while there is edition action.
			 *  So if the user want to get back the polygoene without editing it, we have to cancel this method
			 */
			if( this.node && this.node.length == 0 && this.skyPositions && this.skyPositions.length > 0 ) {
				return ;
			}
			//console.log('this.node1: ' + this.node.length);

			//console.log('this.node.length: ' + this.node.length);
			this.skyPositions = [];		 
			for (var k=0; k<this.node.length; k++) {
				//this.skyPositions.push(this.aladin.pix2world(this.node[k][0], this.node[k][1]));
				this.skyPositions.push(this.aladin.pix2world(this.node[k].cx, this.node[k].cy));
			};
			//finalthis.node
			if (this.overlay==null) {
				this.overlay = A.graphicOverlay({color: 'red'});
				this.aladin.addOverlay(this.overlay);
			}
			this.overlay.removeAll();	       
			this.overlay.addFootprints(A.polygon(this.skyPositions));
		},

		//function pour obtenir les valeurs de le polygon et creer le polygon en adalin lite
		setPolygon: function(points)
		{
			this.skyPositions = [];		 
			for( var k=0 ; k<points.length ; k++){
				this.skyPositions.push(points[k]);			
			}
			if (this.overlay==null) {
				this.overlay = A.graphicOverlay({color: 'red'});
				this.aladin.addOverlay(this.overlay);
			}
			this.overlay.removeAll();	  
			this.overlay.addFootprints(A.polygon(this.skyPositions));
			this.PolygonCenter();
		},
		setOverlay: function(points)
		{
			if (this.overlay==null) {
				this.overlay = A.graphicOverlay({color: 'red'});
				this.aladin.addOverlay(this.overlay);
			}
			this.overlay.removeAll();	  
		},
		//function pour effacer le poligone de this.canvas
		CleanPoligon: function()
		{
			this.CanvasUpdate();
			this.node = [];
			this.skyPositions= [];
			//console.log('this.node delete: ' + this.node.length);		
		},

		//trouver le polygon en adalin lite si on se trouve en otre part du universe
		PolygonCenter: function()
		{		
			var Height = this.GetHeight(this.skyPositions);		
			var width = this.GetWidth(this.skyPositions);
			if( Height.largeur == 0 || width.largeur == 0 ) {
				return;
			}
			var center = {};
			center.ra = ((Height.ramax +  Height.ramin)/2);
			center.dec =  ((width.decmax + width.decmin)/2);
			this.aladin.gotoPosition(center.ra, center.dec);
			this.aladin.setZoom( (width.width + Height.largeur) );
		},

		//effacer un this.node de le polygone si se trouve sûr autre this.node
		RemoveNode: function(nodevalue,status)
		{
			var index = this.node[nodevalue];

			if(this.node.length >= 4)
			{			
				this.node.splice(nodevalue,1);
				if(status)
				{
					this.DrawNode(this.node);
				}else
				{
					this.Redrawn(0);
				}

			}
		},

		//function pour obtenir le this.node initial et final du polygon
		GetXYNode: function(x,y)
		{
			var nodes={};        

			for(var i in this.node)
			{	         
				//console.log('this.nodenum:  ' + i);
				//console.log('cx: ' + this.node[i].cx);
				//console.log('cy: ' + this.node[i].cy);
				dx = x - this.node[i].cx;
				dy = y - this.node[i].cy;  
				//var result =Math.sqrt(dx * dx + dy * dy);
				var result = dx * dx + dy * dy;

				if(result <= 25)
				{	                	
					if(nodes.a == undefined)
					{
						nodes.a = i;
					}
					else 
					{
						nodes.b = i;
					}            		            		
				}                      
			}

			return nodes;
		},

		//metodo que debuelve el numero de nodos del poligono
		GetNodelength: function()
		{
			return this.node;
		},

		//crear la grafica
		createGrafic: function(parametre)
		{
			this.DrawGrafic(parametre);
		},

		//indicar cuando serrar poligono
		cuadradoIndicador: function(x,y)
		{	
			this.context.beginPath();
			this.context.fillRect(x,y,10,10);     	      
			this.context.fillStyle = "red";
			this.context.fill();
			this.context.stroke();	 
			this.context.closePath();
		},

		stokeNode: function(nodeposition)
		{
			if(nodeposition != undefined) 
				var stocknode = [];
				stocknode.push
				({
					position: nodeposition,
					cx:this.node[nodeposition].cx,
					cy:this.node[nodeposition].cy,
					r:5
				});

				return stocknode;
			
		},
		getSkyPositions: function() {
			return this.skyPositions;
		}
}
console.log('=============== >  RegionEditor_m.js ');

/**
 * Manager of the view of the region editor
 * 
 * Author Gerardo Irvin Campos yah
 */ 
function RegionEditor_mVc(parentDivId, handler, points){
	this.parentDivId = parentDivId;
	this.drawCanvas = null; // canvas where the polygon is drawn
	this.drawContext = null;
	this.lineCanvas = null; // canvas where the moving lines are drawn
	this.lineContext = null;
	this.controller = null;
	this.points = points; // Initial values
	this.clientHandler = (handler == null) ? function(){Modalinfo.info("No client handler registered");}: handler;
} 

RegionEditor_mVc.prototype = {
		init: function (data){
			// création instance d'Aladin lite
			$('#' + this.parentDivId).append('<div id="' + this.parentDivId + '_aladin" style="width: 390px; height: 320px;"></div><div id="' + this.parentDivId + '_button"></div>');
				this.aladin = $.aladin('#' + this.parentDivId + '_aladin'
					, {showControl: true, 
				      //fov: 0.55, 
				     // target: "orion", 
				      cooFrame: "ICRS", 
				      survey: "P/DSS2/color", 
				      showFullscreenControl: false, 
				      showFrame: false, 
				      showGotoControl: false});
			//this.aladin.setImageSurvey("P/XMM/PN/color");
			this.aladin.setImageSurvey("P/DSS2/color");
			
			this.parentDiv = this.aladin.getParentDiv();
			$('#' + this.parentDivId).css("position", "relative");
			// création du canvas pour éditeur régions
			/*
			 * Be cautious: the canvas context must be taken before the canvas is appended to the parent div, otherwise the geometry is wrong. 
			 */
			this.lineCanvas = $("<canvas id='RegionCanvasTemp' class='editor-canvas'></canvas>");
			this.lineCanvas[0].width = this.parentDiv.width();
			this.lineCanvas[0].height = this.parentDiv.height();
			this.lineContext = this.lineCanvas[0].getContext('2d');	        
			this.parentDiv.append(this.lineCanvas);
			this.lineCanvas.css('z-index', '100');
			this.lineCanvas.css('position', 'absolute');
			this.lineCanvas.hide(); 

			/*
			 * Canvas pour les traces temporaires
			 */
			this.drawCanvas = $("<canvas id='RegionCanvas' class='editor-canvas'></canvas>");
			this.drawCanvas[0].width = this.parentDiv.width();
			this.drawCanvas[0].height = this.parentDiv.height();
			this.drawContext = this.drawCanvas[0].getContext('2d');
			this.parentDiv.append(this.drawCanvas);
			this.drawCanvas.css('z-index', '100');
			this.drawCanvas.css('position', 'absolute');
			this.drawCanvas.hide(); 


			this.controller = new RegionEditor_mvC({ "points": this.points, "handler": this.clientHandler, "canvas": this.drawCanvas, "canvaso": this.lineCanvas, "aladin": this.aladin});
			/*
			 * The controller function is wrapped in a function in order to make it working in the context of the controller object
			 * and not of he HTML widget
			 */
			var that = this;
			this.drawCanvas[0].addEventListener('mousedown', function(event) {that.controller.mouseDown(event);}, false);
			this.drawCanvas[0].addEventListener('mousemove',  function(event) {that.controller.mouseMove(event);}, false);
			this.drawCanvas[0].addEventListener('mouseup', function(event) { that.controller.mouseUp(event);}, false);

			/*----crear botones con jquery----*/
			var divButtons = $("<div id='RegionButtons' style=' width:"+ this.parentDiv.width() +'px' +" ';' '><div/>").appendTo("#" + this.parentDivId + "_button");        
			divButtons.css('background', 'gray');//'height:' "+ 200 +'px' +"';'
			divButtons.css('height', '70px');

			this.browseBtn = $("<input type='button' id='edit' value='Browse' />");
			divButtons.append(this.browseBtn);
			this.browseBtn.css('margin-top','10px');
			this.browseBtn.css('margin-left','5px');
			this.browseBtn.attr('disabled', 'disabled');
			this.browseBtn.click(function() {        	 
				that.controller.recuperar();   
				that.setBrowseMode();
			});

			this.editBtn = $("<input type='button' id='edit' value='Edit' />");
			divButtons.append(this.editBtn);
			this.editBtn.css('margin-top','10px');
			this.editBtn.css('margin-left','5px');
			var that = this;
			this.editBtn.click(function() {        	 
				that.setEditMode();
				that.controller.DeleteOverlay()
				that.lineContext.clearRect(0, 0, that.lineCanvas[0].width, that.lineCanvas[0].height);            
				that.drawContext.clearRect(0, 0, that.drawCanvas[0].width, that.drawCanvas[0].height);
				that.controller.almacenar();	       
			});


			this.centerBtn = $("<input type='button' id='edit' value='Center' />");
			divButtons.append(this.centerBtn);
			this.centerBtn.css('margin-top','10px');
			this.centerBtn.css('margin-left','5px');
			this.centerBtn.click(function() {        	 
				that.controller.PolygonCenter();
			});

			this.effacerBtn = $("<input type='button' id='edit' value='clear' />");
			divButtons.append(this.effacerBtn);
			this.effacerBtn.css('margin-top','10px');
			this.effacerBtn.css('margin-left','5px');
			this.effacerBtn.click(function() {        	 
				that.controller.CleanPoligon();
			});
			this.setBrowseMode();

			var buttonSet = $("<input type='button' id='edit' value='Accept' />");
			divButtons.append(buttonSet);
			buttonSet.css('margin-top','10px');
			buttonSet.css('margin-left','5px');
			buttonSet.css('font-weight',' bold');
			buttonSet.click(function() {
				that.controller.recuperar();  
				that.setBrowseMode();
				that.controller.invokeHandler(true);
			});
			this.posField = $("<br><input type='text' id='position' size=24 />");
			divButtons.append(this.posField);
			this.posField.css('margin-top','10px');
			this.posField.css('margin-left','5px');
			this.posField.keyup(function(e){
				if(e.keyCode == 13){
					var pos = $(this).val().replace(/:/g , " ");
					that.posField.val(pos);
					that.aladin.gotoObject(pos);
					$(this).val(pos);
					that.aladin.gotoObject(pos);
				}
			});

		},
		/**
		 * Operate the drawing removal from outside 
		 */
		clean: function() {
			this.controller.CleanPoligon();				
			this.setEditMode();
			this.controller.DeleteOverlay()
			this.lineContext.clearRect(0, 0, this.lineCanvas[0].width, this.lineCanvas[0].height);            
			this.drawContext.clearRect(0, 0, this.drawCanvas[0].width, this.drawCanvas[0].height);
			this.controller.almacenar();	       
			this.controller.recuperar();   
			this.setBrowseMode();

		},
		/**
		 * Initalize the darw with the default parameter. If points contains a region, it is drawn, 
		 * if it just contain a position, AladinLite is centered on that position
		 * @param points  object denoting the initial value of the polygone : {type: ... value:} type is format of the 
		 * value (saadaql or array) and value is the data string wich will be parsed
		 */
		setInitialValue: function (points){
			/*
			 * Set the region passed by the client if it exists
			 */
			this.points = points;
			this.controller.CleanPoligon();
			if( this.points ){
				var pts = [];
				/*
				 * Extract region or position from SaadaQL statement
				 */
				if( this.points.type == "saadaql") {
					var s = /"(.*)"/.exec(this.points.value);
					if( s.length != 2 ) {
						Modalinfo.error(this.points.value + " does not look like a SaadaQL statment");
						return;
					} else {
						if( this.points.value.startsWith("isInRegion")) {
							var ss = s[1].split(/[\s,;]/);
							for( var i=0 ; i<ss.length ; i++ ) {
								pts.push(parseFloat(ss[i]));
							}
						} else {
							var pos = s[1].replace(/:/g , " ");
							this.posField.val(pos);
							this.aladin.setZoom(0.55);
							this.aladin.gotoObject(pos);
						}
					}
				} else if (this.points.type == "array") {
					pts = this.points.value;
				} else {
					Modalinfo.error("Polygone format " + points.type + " not understood");
					return;
				}

				this.setBrowseMode();
				this.controller.DeleteOverlay()
				this.controller.setPoligon(pts);
			}
			/*
			 * Fix for the errors when we open a new region editor
			 */
			var that = this;
	           setTimeout(function() {
                   that.aladin.increaseZoom();
                   that.aladin.decreaseZoom();
                   }, 500);

		},
		setBrowseMode: function() {
			this.editBtn.removeAttr('disabled');
			this.browseBtn.attr('disabled', 'disabled');   
			this.effacerBtn.attr('disabled', 'disabled');                      
			this.lineCanvas.hide();
			this.drawCanvas.hide();
		},
		setEditMode: function() {
			this.browseBtn.removeAttr('disabled');
			this.editBtn.attr('disabled', 'disabled');   
			this.effacerBtn.removeAttr('disabled');                
			this.lineCanvas.show();
			this.drawCanvas.show();
		},

}


console.log('=============== >  RegionEditor_v.js ');

/**
 * Controller handling the user actions in connection with the model 
 * 
 *  params = {canvas,canvaso, aladin}
 * 
 * Author Gerardo Irvin Campos yah
 */
function /**
 * @author michel
 *
 */
RegionEditor_mvC(params){
	//this.poligone =  new poligoneCreate(params.canvas, params.canvaso, params.aladin);
	this.poligone =  new RegionEditor_Mvc(params.canvas, params.canvaso, params.aladin);

	this.canvas = params.canvas; 	
	this.clientHandler = params.handler;
	this.startingNode= -1; 
	this.buttondown = false; 
	this.closed = false;	
	this.movestart = false;
	this.startdrag = false;
	this.drag = null;
	this.result = -1;
	this.stokeNode;
	var that = this;
}

RegionEditor_mvC.prototype = {
		getStatus: function() {
			 return "startingNode=" 
			        +this.startingNode + " buttondown=" 
			  		+ this.buttondown+ " closed=" 
			  		+ this.closed+ " movestart=" 
			  		+ this.movestart + " startdrag=" 
			  		+ this.startdrag + " drag=" 
			  		+ this.drag  + " result=" 
			  		+ this.result + " stokeNode=" 
			  		+ this.stokeNode
			  		;
		},
		/**
		 * TODO to be implemented
		 */
		checkPolygon : function(points) {
			return true;
		},
		/**
		 * 
		 */
		mouseDown : function(event) {
			var clickedNode = -1;
			var clickedSegment = -1;
			var x = parseInt(event.pageX) - parseInt( this.canvas.offset().left).toFixed(1);
			var y = parseInt(event.pageY) - parseInt( this.canvas.offset().top).toFixed(1);
					
			//pregunta si el pologono esta vacio
			if( this.poligone.isEmpty()) 
			{
				this.poligone.addNode(x,y);			 
			}
			//obtener segmento
			
			//comenzar el this.drag del nodo		
			else if(this.closed == true && (clickedNode = this.poligone.getNode(x,y)) != -1)
			{
				//console.log('start this.drag');
				//console.log('clickedNode: ' + clickedNode);
				this.result = this.poligone.getSegment(clickedNode);
				this.stokeNode = this.poligone.stokeNode(clickedNode);
				this.startdrag = true;		
				this.drag = clickedNode;
				this.startingNode = clickedNode;		
				this.canvas.css('cursor','move');
			}
			//pregunta si el espacio presionado es un nodo 
			else if((clickedNode = this.poligone.getNode(x,y)) != -1 )
			{
				//pregunta si es una extremidad
				if(this.poligone.isExtremity(clickedNode) /*poligono abierto*/) 
				{			
					//pregunta estas abierto
					if(this.closed == true)
					{
						this.startingNode = -1;
						this.buttondown = false;	
					}
					else
					{
						this.startingNode = clickedNode;
						this.buttondown = true;					
						this.closed = false;
					}
				}							
			} 		
			
			//saber si estoy sobre un segmento
			if(this.closed && clickedNode == -1)
			{						
				var node = this.poligone.GetNodelength();	
						
				var Segmentos = new Segment(node);	
				var option = Segmentos.IsCursorOn(x,y);
				
				if(option != undefined)
				{
					if(option.flag == "vertical")
					{
						//console.log("option: " + option.flag);
						this.poligone.addNode(x, y, option);
					}
					else if(option.flag == "horizontal")
					{
						//console.log("option: " + option.flag);
						this.poligone.addNode(x, y, option);
					}
					else if(option.flag == "distancia")
					{
						//console.log("option: " + option.flag);
						this.poligone.addNode(x, y, option);
					}
				}						
			
			}
			
		},
		/**
		 * 
		 */
		mouseMove : function(event) {
			var x = parseInt(event.pageX) - parseInt( this.canvas.offset().left).toFixed(1);
			var y = parseInt(event.pageY) - parseInt( this.canvas.offset().top).toFixed(1);
			//console.log("mouse move " + this.getStatus());
			//pregunta si el nodo fue presionado y si es un nodo
			if(this.buttondown == true  && this.startingNode != -1 )
			{
				//console.log ('this.drag');
				//console.log ('this.startingNode' + this.startingNode);
				this.movestart = true;
				this.poligone.drawHashline(this.startingNode,x,y,this.result);		
			}		
			else if(this.startdrag)
			{
				this.poligone.Drag(this.drag, x, y , this.result);
				
				//console.log('this.startdrag move');		
			}
			
//			var h2x = document.getElementById("idcoor");
//			h2x.innerHTML = 'X coords: '+x+', Y coords: '+y;
		},
		
		mouseUp: function(event) {
			var clickedNode = -1;
			var x = parseInt(event.pageX) - parseInt( this.canvas.offset().left).toFixed(1);
			var y = parseInt(event.pageY) - parseInt( this.canvas.offset().top).toFixed(1);		
		//pregunta nodo es presionado y es si es un nodo
			if(this.buttondown == true && (clickedNode = this.poligone.getNode(x,y)) != -1 )
			{		
				//pregunta si es un extremo
				if( this.poligone.isExtremity(clickedNode) == false) 
				{				
					this.poligone.CleanLine();				
					this.buttondown = false;
				}	
				
				//console.log('clickedNode: ' + clickedNode + ' this.startingNode: ' +  this.startingNode);
				if(this.poligone.closePolygone(clickedNode , this.startingNode) == true)
				{
					//console.log('this.closed polygon');					
					this.buttondown = false;	
					this.closed = true;
					this.invokeHandler(false);
				
					//console.log('clickedNode: ' + clickedNode + ' this.startingNode: ' +  this.startingNode);							
				}
			} 
			
			if(this.closed == true && (finalnode = this.poligone.GetXYNode(x, y) ) != null)			
			{
				if(finalnode.a != undefined && finalnode.b != undefined)
				{
					//console.log('finalnode a: ' + finalnode.a + ' finalnode b: ' + finalnode.b);
					
					if(this.startingNode ==  finalnode.a)
						this.poligone.RemoveNode(finalnode.a,false);
					else if(this.startingNode ==  finalnode.b)
						this.poligone.RemoveNode(finalnode.b,false);
				}			
			}
					
			if(this.buttondown == true && this.movestart == true)
			{		
				if( clickedNode == this.startingNode && (clickedNode = this.poligone.getNode(x,y) != -1) )
				//if((clickedNode = this.poligone.getNode(x,y)) != -1)
				{											
					this.buttondown = false;		
					this.movestart = false;	
					this.poligone.CleanLine();							
				}				
				else
				{						
						this.poligone.addNode(x,y,this.startingNode);
						this.buttondown = false;		
						this.movestart = false;	
						
						var nodos = this.poligone.GetNodelength();					
						var Segmentos = new Segment(nodos);	
						var temp;
						
						var inter = Segmentos.Itersection(this.startingNode,false);
						
						if(inter != -1 && inter != undefined)
						{			
							//poligono abierto = true
							if(this.startingNode != 0)
								this.poligone.RemoveNode(inter.nB,true);
							else
								this.poligone.RemoveNode(inter.nA,true);
							
							this.poligone.CleanLine();
						}												
				}			
				
			}
			else if(this.buttondown == true && this.movestart == false)
			{			
				this.buttondown = false;		
				this.movestart = false;	
			}
			
			if(this.startdrag == true)
			{
				//console.log('this.startdrag fin');
				this.startdrag = false;
				this.canvas.css('cursor','default');
				
				//stoke le numero de noeud appuyer
				//this.startingNode;			
				
				var nodos = this.poligone.GetNodelength();					
				var Segmentos = new Segment(nodos);	
				var inter = Segmentos.Itersection(this.startingNode,true);			
				if(inter != -1 && inter != undefined)
				{						
					this.poligone.RemoveNode(this.startingNode, false);
					this.poligone.addNode(x, y, this.stokeNode,true);
					//console.log(inter.length);
				}
			}
			this.poligone.canvasUpdate();
		},
		
		almacenar : function()
		{
			this.poligone.almacenar();
		},
		
		recuperar : function()
		{
			this.poligone.recuperar();
		},
		
		DeleteOverlay : function() {
			this.poligone.DeleteOverlay();
		},
		
		CleanPoligon : function(){
			this.poligone.CleanPoligon();
			this.closed = false;
		},
		
		PolygonCenter : function(){
			this.poligone.PolygonCenter();
		},
	
		CreateGrafic : function(canvas){
			this.poligone.createGrafic(this.canvas);
		},
		
		show : function() {
			console.log(this.poligone.getSkyPositions());
			alert(this.poligone.getSkyPositions());
		},
		/**
		 * Set the polygone with points. Points is a simple array. It must have at 
		 * least 6 values (3pts) and an even number of points
		 * @param points  [a,b,c,.....]
		 * @returns {Boolean} true if the polygone is OK
		 */
		setPoligon : function(points) {
			if( !points || (points.length%2) != 0 || points.length < 6 ) {
				return false;
			} else {
				var x = [];
				for(var i=0 ; i<(points.length/2) ; i++){
					x[x.length] = [points[2*i], points[(2*i)+1]];
				}
				this.poligone.setPolygon(x);
				this.closed = true;
				this.invokeHandler(false);
				return true;
			}
		},
		/**
		 * Call the client handler when the polygine is close or when the user click on accept
		 * The data passed to the user handler look like that:
		    {isReady: true,             // true if the polygone is closed
		    userAction: userAction,     // handler called after the user have clicked on Accept
		    region : {
		        format: "array2dim",    // The only one suported yet [[x, y]....]
		        points: this.poligone.skyPositions  // array with structire matching the format
		        size: {x: , y:} // regiosn size in deg
		        }
		 */
		invokeHandler : function(userAction){
			if( this.closed || ( this.poligone.node == undefined || this.poligone.node.length == 0) ){
				/*
				 * Compute the region size in degrees
				 */
				for( var p=0 ; p<this.poligone.skyPositions.length ; p++ ) {
					for( var q=(p+1) ; q<this.poligone.skyPositions.length ; q++ ) {
						var x = Math.abs(this.poligone.skyPositions[p][0] - this.poligone.skyPositions[q][0]);
						if( x > 180 )x = 360 - x; 
						var y = Math.abs(this.poligone.skyPositions[p][1] - this.poligone.skyPositions[q][1]);
						if( y > 180 )y = 360 - y; 
					}
				}
				this.clientHandler({isReady: true
					, userAction: userAction
					, region : {format: "array2dim"
						       , points: this.poligone.skyPositions
							   , size: {x: x, y: y}}});
			} else {
				Modalinfo.error("Polygon not closed");
			}
		}
}

console.log('=============== >  RegionEditor_c.js ');

/**
 * ConstQEditor_Mvc: Model of the constraint query editor
 * 
 * @param chemin : URL returning metadata (format given bellow)
 * @returns {ConstQEditor_Mvc}
 */
function ConstQEditor_Mvc(){
	this.listener = {};
	this.const_key = 1;
	this.attributeHandlers = new Array();
	this.attributeHandlersSearch = new Array();
	this.editors = new Object();
	this.constEditorRootId = 'kwconsteditor_';
}

ConstQEditor_Mvc.prototype = {
		addListener : function(listener){
			this.listener = listener;
		},
		loadFields : function(dataTreePath /* instance of DataTreePath */) {
			var that = this;
			this.attributesHandlers = {};
			if( dataTreePath ) {
				MetadataSource.getTableAtt(
						dataTreePath
						, function() {
							var ahm = MetadataSource.ahMap(dataTreePath);
							that.attributeHandlersSearch = new Array();
							for( var k=0 ; k<ahm.length ; k++) {
								var ah = ahm[k];
								that.attributeHandlersSearch.push(ah);		
								that.attributeHandlers[ah.nameattr] = ah;				
							}
						});
			}
		},
		addPresetValues : function(){
			// Nothing to do here but for TAP
		},		
		clearAllConst : function(){
			this.const_key = 1;
			this.editors = new Object();
			this.notifyTypoMsg(false, '');
			this.updateQuery();
		},
		clearConst : function(filter){
			this.notifyTypoMsg(false, '');
			for(  var e in this.editors) {
				if( e.match(filter) ) {
					delete this.editors[e];
					this.const_key --;
				}
			}
			this.updateQuery();
		},

		edit : function(){
			return this.editors;
		},
		processAttributeEvent : function(ahname, constListId){
			this.processAttributeHandlerEvent(this.attributeHandlers[ahname], constListId);
		},
		processAttributeHandlerEvent : function(ah, constListId){
			var first = true;
			for( k in this.editors ) {
				first = false;
				break;
			}
			var divKey = this.constEditorRootId + ah.nameattr + this.const_key;
			Out.debug("mv constraint " + ah.nameattr + " to #" + constListId);
			var v = new KWConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		},
		processRemoveFirstAndOr : function(key) {
			delete this.editors[key];
			for( var k in this.editors ) {
				this.editors[k].fireRemoveAndOr();
				break;
			}
		},
		notifyNextListener : function(attr) {
			var that = this;
			this.listener.nextListener(attr);
		},
		updateQuery : function() {
			var that = this;
			var retour= "    ";
			for( var e in this.editors) {
				var q =  this.editors[e].fireGetADQL();
				if( retour.length > 96 ) retour += "\n    ";
				if( q != null ) {
					retour += " " + $.trim(this.editors[e].fireGetADQL());
				}
			}
			this.listener.controlUpdateQuery(retour);
		},
		getNumberOfEditor: function() {
			var retour = 0;
			for( var e in this.editors) {
				retour ++;
			}
			return retour;
		},
		notifyRunQuery : function() {
			var that = this;
			this.listener.controlRunQuery();
		},
		notifyTypoMsg : function(fault, msg){
			var that = this;
			this.listener.controlTypoMsg(fault, msg) ;
		},
		notifyFieldsStored: function(){
			var that = this;
			this.listener.controlFieldsStored(that.attributeHandlersSearch) ;
		},
		getAttributeHandlers: function() {
			return this.attributeHandlers;
		},
		// used by regions
		getDefaultValue: function() {
			return null;
		}

};

/**
 * Sub-class of ConstQEditor_Mvc, specialized to manage UCDs instead of Fields
 * Same constructor as the superclass
 * Only the UcdQEditor_Mvc method is overloaded
 * @param chemin
 */
function UcdQEditor_Mvc(){
	ConstQEditor_Mvc.call(this);
};
/**
 * Method overloading
 */
UcdQEditor_Mvc.prototype = Object.create(ConstQEditor_Mvc.prototype, {
	processAttributeEvent: {
		value : function(ahname, constListId){
			var ah = this.attributeHandlers[ahname];
			if( ah == null ){
				Out.info("No AH referenced by " + ahname);
			} else {
				var first = true;
				for( k in this.editors ) {
					first = false;
					break;
				}
				Out.debug("mv UCD constraint " + ahname + " to #" + constListId );
				var divKey = this.constEditorRootId + ahname + this.const_key;
				var v = new UCDConstraint_mVc({divId: divKey
					, constListId: constListId
					, isFirst: first
					, attributeHandler: ah
					, editorModel: this
					, defValue: ''});

				this.editors[divKey] = v;
				v.fireInit();
				this.const_key++;
			}
		}
	},
	loadFields: {
		value : function(dataTreePath /* instance of DataTreePath */) {
			var that = this;
			this.attributesHandlers = {};
			if( dataTreePath ) {
				MetadataSource.getTableAtt(
						dataTreePath
						, function() {
							var ahm = MetadataSource.ahMap(dataTreePath);
							that.attributeHandlersSearch = new Array();
							for( var k=0 ; k<ahm.length ; k++) {
								var ah = ahm[k];
								that.attributeHandlersSearch.push(ah);		
								that.attributeHandlers[ah.ucd] = ah;				
							}
						});
			}
		}
	},

	updateQuery : {
		value : function() {
			var that = this;
			var retour= "";
			var and = "";
			for( var e in this.editors) {
				var q = this.editors[e].fireGetADQL();
				if( q != null ) {
					retour += and + q;
					if( and == "" ) and = "\n        AND " ;
				}
			}
			this.listener.controlUpdateQuery(retour);
		}
	}
});

/**
 * Sub-class of ConstQEditor_Mvc, specialized to manage UCDs instead of Fields
 * Same constructor as the superclass
 * Only the UcdQEditor_Mvc method is overloaded
 * @param chemin
 */
function UcdPatternEditor_Mvc(relationName){
	UcdQEditor_Mvc.call(this);
	this.relationName = relationName;
};
/**
 * Method overloading
 */
UcdPatternEditor_Mvc.prototype = Object.create(UcdQEditor_Mvc.prototype, {
	updateQuery : {
		value : function() {
			var that = this;
			var retour= "";
			var and = "";
			for( var e in this.editors) {
				var q = this.editors[e].fireGetADQL();
				if( q != null ) {
					retour += and + q;
					if( and == "" ) and = "\n        AND " ;
				}
			}
			if( retour != "" ) {
				retour = "  matchPattern {" + this.relationName + ",\n"
				+ "    AssUCD{" + retour + "}\n"
				+ "  }";
			}
			this.listener.controlUpdateQuery(retour);
		} 
	}
});

/**
 * Sub-class of ConstQEditor_Mvc, specialized to manage Position search instead of Fields
 * Same constructor as the superclass
 * Only the UcdQEditor_Mvc method is overloaded
 * @param chemin
 */
function PosQEditor_Mvc(){
	ConstQEditor_Mvc.call(this, null);
};

/**
 * Method overloading
 */
PosQEditor_Mvc.prototype = Object.create(ConstQEditor_Mvc.prototype, {
	processAttributeEvent: {
		value : function(coneParams, constListId){
			if(coneParams == null){
				return;
			}
			/*
			 * create a pseudo ah for each searched position
			 */
			var ah = {
					nameorg: coneParams.position,
					nameattr: (coneParams.type == "region")? "region" : coneParams.position.replace(/[^0-9a-zA-Z_]+/g, '_'),
							ucd: '',
							unit: '',
							comment: 'cone search',
							radius:  coneParams.radius,
							frame:  coneParams.frame,
							value: coneParams.position
			};
			var divKey = this.constEditorRootId + ah.nameattr ;
			/*
			 * Only one constraint for a given position
			 */					

			/*
			 * If the same project is already targeted, this one is repalced with the new cone 
			 */
			if( this.editors[divKey] != null ) {
				this.editors[divKey].drop();
			}
			/*
			 * Now we can add the cone in "toute serenite"
			 */
			var first = true;
			for( k in this.editors ) {
				first = false;
				break;
			}
			var v = new PosConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''});
			this.editors[divKey] = v;
			v.fireInit();
		}
	},
	storeFields : {
		value :function(data) {
			/*
			 * Stockage des AHs dans le modele
			 */
			this.attributeHandlers = new Array();
			this.attributeHandlersSearch = new Array();
			for(var i=0 ; i<data.attributes.length ; i++ ) {
				this.attributeHandlers[data.attributes[i].ucd] = data.attributes[i];
				this.attributeHandlersSearch[this.attributeHandlersSearch.length]= data.attributes[i];
			}
			this.notifyFieldsStored();
		}
	},		
	updateQuery : {
		value : function() {
			var that = this;
			var retour= "";
			for( var e in this.editors) {
				var q = this.editors[e].fireGetADQL();
				if( q != null ) {
					if( retour != "" ) retour += ",\n" ;
					retour += "    " + this.editors[e].fireGetADQL();
				}
			}
			this.listener.controlUpdateQuery(retour);
		}
	},
	getDefaultValue : {
		value :  function() {				
			for( var e in this.editors) {
				//	if( e.match(".region.*") ){
				return {type: "saadaql", value: this.editors[e].fireGetADQL()};
				//	}
			}

			return null;
		}
	}

});

/**
 * Sub-class of ConstQEditor_Mvc, specialized catalogue counterpart
 * Same constructor as the superclass
 * @param chemin
 */
function CatalogueQEditor_Mvc(params /*{parentDivId, formName, getMetaUrl, queryView, relationName, distanceQualifer, help}*/){
	ConstQEditor_Mvc.call(this, params);
	this.relationName = params.relationName;
	this.qualifier = params.distanceQualifer;
	this.getMetaUrl = params.getMetaUrl;
};

CatalogueQEditor_Mvc.prototype = Object.create(ConstQEditor_Mvc.prototype, {

	loadFields : {
		value: function(){
			var that = this;
			if( this.getMetaUrl != null ) {
				$.getJSON(this.getMetaUrl,function(data) {
					that.storeFields(data);
				});
			} else {
				this.storeFields(
						{attributes: [
						              {ACDS_CATACRO: "ACDS_CATACRO", ACDS_CATCDSTB: "ACDS_CATCDSTB", ACDS_CATCONF: "ACDS_CATCONF", ACDS_CATINTNB: "ACDS_CATINTNB", ACDS_CATINTNB: "ACDS_CATINTNB", ACDS_CATNAME: "ACDS_CATNAME", ACDS_CDSCAT: "ACDS_CDSCAT", VIZIER_KW: "VIZIER_KW", CLASSNAME: "CLASSNAME"}
						              ,{ACDS_CATACRO: "ACDS_CATACRO", ACDS_CATCDSTB: "ACDS_CATCDSTB", ACDS_CATCONF: "ACDS_CATCONF", ACDS_CATINTNB: "ACDS_CATINTNB", ACDS_CATINTNB: "ACDS_CATINTNB", ACDS_CATNAME: "ACDS_CATNAME", ACDS_CDSCAT: "ACDS_CDSCAT", VIZIER_KW: "VIZIER_KW", CLASSNAME: "CLASSNAME2"}
						              ]
						});
			}
		}
	},
	processAttributeEvent: {
		value : function(ahname, constListId){
			var ah = this.attributeHandlers[ahname];
			if( ah == null ) {
				Modalinfo.error("Internal error: No constraint referenced by the key '" + ahname + "' (see console)");	
				var ks = new Array();
				$.each(this.attributeHandlers, function(key, value) {
					ks.push(key);	

				});
				return;
			}
			var first = true;
			for( k in this.editors ) {
				first = false;
				break;
			}
			Out.debug("mv catalogue constraint " + ahname + " to #" + constListId );
			var divKey = this.constEditorRootId + ahname ;

			if( this.editors[divKey] != null ) {
				Modalinfo.error("Constraint on " + ahname + " already active: cannot be duplicated");
				return;
			}
			var v = new CatalogueConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''
					, qualifier: this.qualifier});

			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		}
	},
	storeFields : {
		value :  function(data) {
			this.attributeHandlers = new Array();
			this.attributeHandlersSearch = new Array();
			for(var i=0 ; i<data.attributes.length ; i++ ) {
				this.attributeHandlers[data.attributes[i].CLASSNAME] = data.attributes[i];
				this.attributeHandlersSearch[this.attributeHandlersSearch.length]= data.attributes[i];
			}
			this.notifyFieldsStored();
		}
	},
	updateQuery : {
		value : function() {
			var that = this;
			var sq = "";
			for( var e in this.editors) {
				var q = this.editors[e].fireGetADQL();
				if( q != null && q != "" ) {
					if( sq != "" ) sq += "\n";
					sq += "    matchPattern { " + this.relationName + "," + q+ "}";
				}
			}
			this.listener.controlUpdateQuery(sq);
		} 
	}
});

/**
 * Sub-class of ConstQEditor_Mvc, specialized in catalogue counterpart with proba of identification
 * Same constructor as the superclass
 * @param chemin
 */
function CrossidQEditor_Mvc(params){
	CatalogueQEditor_Mvc.call(this, params);
};

CrossidQEditor_Mvc.prototype = Object.create(CatalogueQEditor_Mvc.prototype, {

	processAttributeEvent: {
		value : function(ahname, constListId){
			var ah = this.attributeHandlers[ahname];
			if( ah == null ) {
				Modalinfo.error("Internal error: No constraint referenced by the key '" + ahname + "' (see console)");	
				var ks = new Array();
				$.each(this.attributeHandlers, function(key, value) {
					ks.push(key);
				});
				return;
			}
			var first = true;
			for( k in this.editors ) {
				first = false;
				break;
			}
			Out.debug("mv catalogue constraint " + ahname + " to #" + constListId );
			var divKey = this.constEditorRootId + ahname ;

			if( this.editors[divKey] != null ) {
				Modalinfo.error("Constraint on " + ahname + " already active: canot be duplicated");
				return;
			}
			var v = new CrossidConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''
					, qualifier: this.qualifier});

			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		}
	}
});

/**
 * Sub-class of ConstQEditor_Mvc, specialized in catalogue counterpart with proba of identification
 * Same constructor as the superclass
 * @param chemin
 */
function tapColSelector_Mvc(){
	ConstQEditor_Mvc.call(this);
	this.joinKeys = null;
	this.dataTreePath = null; // instance of DataTreePath
	this.joinedTableLoaded = false;
	this.orderBy = "";
};

tapColSelector_Mvc.prototype = Object.create(ConstQEditor_Mvc.prototype, {
	loadFields : {
		value: function(treePath, handler) {
			var that = this;
			if(treePath) {
				this.dataTreePath = jQuery.extend({}, treePath);			
				MetadataSource.getTableAtt(
						this.dataTreePath
						, function() {
							that.notifyAddTableOption(that.dataTreePath);	
							var ahm = MetadataSource.ahMap(that.dataTreePath);
							that.attributeHandlers = new Array();
							that.attributeHandlersSearch = new Array();
							for(var k=0 ; k<ahm.length ; k++ ) {
								var ah = ahm[k];
								that.attributeHandlers[ah.nameattr] = ah;
								that.attributeHandlersSearch[that.attributeHandlersSearch.length]=ah;
							}
							if( !that.joinedTableLoaded ) {
								that.joinKeys = new Array();
								var jt = MetadataSource.joinedTables(that.dataTreePath);
								for( var k=0 ; k<jt.length ; k++) {
									that.joinKeys.push(jt[k]);
								}
								// The same component is used for all dataset: meta data must be refreshed at any time
								//that.joinedTableLoaded = true;
							}
							if( handler ) handler();
						});
			} else {
				this.attributeHandlers = new Array();
				this.attributeHandlersSearch = new Array();
				this.joinKeys = new Array();
				this.dataTreePath = null;
			}
		}
	},
	notifyAddTableOption: {
		value: function(treePath){
			var that = this;
			this.listener.controlAddTableOption(treePath);
		}
	},
	processAttributeHandlerEvent : {
		value: function(ah, constListId){
			var that = this;
			var first = true;
			var currentTreePath = null;
			for( k in this.editors ) {
				first = false;
				break;
			}
			currentTreePath  = this.listener.controlCurrentTreePath();
			var divKey = this.constEditorRootId + ah.nameattr + this.const_key;
			Out.debug("mv constraint " + ah.nameattr + " to #" + this.constListId);
			var v = new TapKWSimpleConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''
					, treePath: jQuery.extend({}, currentTreePath)});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		}
	},
	processOrderBy: {
		value: function(nameattr) {
			this.orderBy = ( nameattr != 'OrderBy' )? nameattr: "";		
			this.updateQuery();
		}
	},
	updateQuery : {
		value: function() {
			var that = this;
			// queried table path
			var st = this.dataTreePath.schema + "." + this.dataTreePath.table;
			var joins = new Array();
			var q = new Array();
			/*
			 * Merge all constraints
			 */
			for( var e in this.editors) {
				var ed = this.editors[e];
				// Path of the table targeted by he current constraint
				var tt = ed.treePath.schema + "." + ed.treePath.table ;
				q.push( ed.fireGetADQL());
				// if constraint not applied to the queried table: join
				if( tt != st ) {
					/*
					 * Key of the join descriptor
					 */
					for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
						var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
						if( lt == tt) {
							joins[tt] = this.joinKeys[i];
						}
					} 
				}
			}
			if( this.orderBy != "" ) {
				var fs = this.orderBy.split('.');
				var tt = fs[0] + "." + fs[1] ;
				if( tt != st ) {
					for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
						/*
						 * Key of the join descriptor
						 */
						var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
						if( lt == tt) {
							joins[tt] = this.joinKeys[i];
						}
					} 
				}
			}
			if( q.length == 0 ) {
				q = ["*"];
			}
			this.listener.controlUpdateQuery(q, joins);
		}
	}
});

function tapQEditor_Mvc(){
	tapColSelector_Mvc.call(this);
};
/**
 * 
 */
tapQEditor_Mvc.prototype = Object.create(tapColSelector_Mvc.prototype, {
	processAttributeHandlerEvent : {
		value: function(ah, constListId){
			var that = this;
			var first = true;
			var currentTreePath = null;
			for( k in this.editors ) {
				first = false;
				break;
			}
			currentTreePath  = this.listener.controlCurrentTreePath();

			var divKey = this.constEditorRootId + ah.nameattr + this.const_key;
			Out.debug("mv constraint " + ah.nameattr + " to #" + this.constListId);
			var v = new TapKWConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''
					, treePath: jQuery.extend({}, currentTreePath)});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		}
	},
	processInputCoord: {
		value: function(ra, dec, radius, frame, rakw, deckw, constListId) {
			var that = this;
			var first = true;
			var currentTreePath = null;
			for( k in this.editors ) {
				first = false;
				break;
			}
			currentTreePath  = this.listener.controlCurrentTreePath();
			var defValue = '';
			var nameAh = '';
			if( ra.startsWith('poslist:')) {
				defValue = 'list params,'+ radius;
				nameAh = 'POSLIST:' + ra.replace('poslist:','');;
			} else {
				defValue = ra + ',' + dec + ','+ radius;
				nameAh = 'POSITION';				
			}
			var divKey = this.constEditorRootId + "ADQLPos" + this.const_key;
			var v = new TapKWConstraint_mVc({divId: divKey
				, constListId:  constListId
				, isFirst: first
				, editorModel : this
				, attributeHandler: {nameattr: nameAh
					, nameorg: nameAh
					, "type" : "ADQLPos"
						, "ucd" : "adql.coor.columns"
							, "utype" : ""
								, "unit" : "deg"
									, "description" :  rakw + " " + deckw}
			, defValue: defValue
			, treePath: jQuery.extend({}, currentTreePath)});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;			
		}
	},
	updateQuery : {
		value: function() {
			var that = this;
			var retour= "    ";
			// queried table path
			var st = this.dataTreePath.schema + "." + this.dataTreePath.table;
			var joins = new Array();
			/*
			 * Merge all constraints
			 */
			for( var e in this.editors) {
				var ed = this.editors[e];
				var q =  ed.fireGetADQL();
				if( retour.length > 96 ) retour += "\n    ";
				if( q != null ) {
					retour += this.editors[e].fireGetADQL() + ' ';
					// Path of the table targeted by he current constraint
					var tt = ed.treePath.schema + "." + ed.treePath.table;
					// if constraint not applied to the queried table: join
					if( tt != st ) {
						for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
							var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
							if( lt == tt) {
								joins[tt] = this.joinKeys[i];
							}
						}
						// if the targetted table is an uploaded file: crosmatch 	
					} else if( ed.fieldName.startsWith("POSLIST") ) {
						joins[tt] = {target_table: "upload." + ed.fieldName.replace('POSLIST:',''), target_column: "", source_column: ""};
					}
				}
			}
			if( this.orderBy != "" ) {
				var fs = this.orderBy.split('.');
				var tt = fs[0] + "." + fs[1] ;
				if( tt != st ) {
					for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
						var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
						if( lt == tt) {
							joins[tt] = this.joinKeys[i];
						}
					} 
				}
			}
			this.listener.controlUpdateQuery(retour, joins);
		}
	}
});

function tapPosQEditor_Mvc(){
	tapColSelector_Mvc.call(this);
};
/**
 * 
 */
tapPosQEditor_Mvc.prototype = Object.create(tapColSelector_Mvc.prototype, {
	processAttributeHandlerEvent : {
		value: function(ah, constListId){
			var that = this;
			var first = true;
			var currentTreePath = null;
			for( k in this.editors ) {
				first = false;
				break;
			}
			currentTreePath  = this.listener.controlCurrentTreePath();

			var divKey = this.constEditorRootId + ah.nameattr + this.const_key;
			Out.debug("mv constraint " + ah.nameattr + " to #" + this.constListId);
			var v = new TapKWConstraint_mVc({divId: divKey
				, constListId: constListId
				, isFirst: first
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''
					, treePath: jQuery.extend({}, currentTreePath)});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		}
	},
	processInputCoord: {
		value: function(ra, dec, radius, frame, rakw, deckw, constListId) {
			var that = this;
			var first = true;
			var currentTreePath = null;
			for( k in this.editors ) {
				first = false;
				break;
			}
			currentTreePath  = this.listener.controlCurrentTreePath();
			var defValue = '';
			var nameAh = '';
			if( ra.startsWith('poslist:')) {
				defValue = 'list params,'+ radius;
				nameAh = 'POSLIST:' + ra.replace('poslist:','');;
			} else {
				defValue = ra + ',' + dec + ','+ radius;
				nameAh = 'POSITION';				
			}
			var divKey = this.constEditorRootId + "ADQLPos" + this.const_key;
			var v = new TapKWConstraint_mVc({divId: divKey
				, constListId:  constListId
				, isFirst: first
				, editorModel : this
				, attributeHandler: {nameattr: nameAh
					, nameorg: nameAh
					, "type" : "ADQLPos"
						, "ucd" : "adql.coor.columns"
							, "utype" : ""
								, "unit" : "deg"
									, "description" :  rakw + " " + deckw}
			, defValue: defValue
			, treePath: jQuery.extend({}, currentTreePath)});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;			
		}
	},
	updateQuery : {
		value: function() {
			var that = this;
			var retour= "    ";
			// queried table path
			var st = this.dataTreePath.schema + "." + this.dataTreePath.table;
			var joins = new Array();
			/*
			 * Merge all constraints
			 */
			for( var e in this.editors) {
				var ed = this.editors[e];
				var q =  ed.fireGetADQL();
				if( retour.length > 96 ) retour += "\n    ";
				if( q != null ) {
					retour += this.editors[e].fireGetADQL() + ' ';
					// Path of the table targeted by he current constraint
					var tt = ed.treePath.schema + "." + ed.treePath.table;
					// if constraint not applied to the queried table: join
					if( tt != st ) {
						for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
							var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
							if( lt == tt) {
								joins[tt] = this.joinKeys[i];
							}
						}
						// if the targetted table is an uploaded file: crosmatch 	
					} else if( ed.fieldName.startsWith("POSLIST") ) {
						joins[tt] = {target_table: "upload." + ed.fieldName.replace('POSLIST:',''), target_column: "", source_column: ""};
					}
				}
			}
			if( this.orderBy != "" ) {
				var fs = this.orderBy.split('.');
				var tt = fs[0] + "." + fs[1] ;
				if( tt != st ) {
					for(var i=0 ;  i<this.joinKeys.length ; i++ ) {
						var lt = this.joinKeys[i].target_datatreepath.schema + "." + this.joinKeys[i].target_datatreepath.table;
						if( lt == tt) {
							joins[tt] = this.joinKeys[i];
						}
					} 
				}
			}
			this.listener.controlUpdateQuery(retour, joins);
		}
	}
});

console.log('=============== >  ConstQEditor_m.js ');

/**
 * ConstQEditor_mVc: View of the constraint query editor
 * 
 * @param parentDivId : ID of the div containing the query editor
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors 
 * @param queryview   : JS object supposed to take the constraint locally edited.
 *                      It supposed to have a public method names fireConstQuery(const)
 *                      where const is an object like {columnConst:"SQL stmt", orderConst:"att name"}
 *                     
 * @returns {ConstQEditor_mVc}
 */
function ConstQEditor_mVc(params /*{parentDivId,formName,queryView}*/){
	var that = this;
	this.fieldsLoaded = false;
	this.dataTreePath = null; // instance of DataTreePath
	/**
	 * who is listening to us?
	 */
	this.listener = {};
	this.queryView = params.queryView;
	/**
	 * DOM references
	 */
	this.parentDiv = $("#" + params.parentDivId );
	this.constContainerId   = params.parentDivId + "_constcont";
	this.constListId   = '';
	this.formName = params.formName;
	this.orderById     = params.parentDivId + "_orderby";
	this.fieldListView = new FieldList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	, orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	}
	);
	this.constListView = new ConstList_mVc(params.parentDivId
			, this.formName
			, this.constContainerId
			, function(ahName){ that.fireClearAllConst();}
	);
	this.orderByView = new 	OrderBy_mVc(params.parentDivId
			, this.formName
			, this.constContainerId
			, function(ahName){ that.fireOrderBy(ahName);}
	);
};

ConstQEditor_mVc.prototype = {
		/**
		 * Instanciate components
		 */
		/**
		 * add a listener to this view
		 */
		addListener : function(listener){
			this.listener = listener;
		},
		draw : function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="width: 450px;float: left;background: transparent; display: inline;"></div>');
			var isPos = {"fieldset":"130px", "div":"102px"};
			this.constListId = this.constListView.draw(isPos);
			this.orderByView.draw();
		},	
		fireSetTreepath: function(dataTreePath){
			this.fieldListView.setStackTooltip("Click to select this field");
			var that = this;
			this.dataTreePath = dataTreePath;	
			this.fieldListView.setDataTreePath(this.dataTreePath);
			this.listener.controlLoadFields(that.dataTreePath);
		},
		fireOrderBy : function(nameattr){
			if( nameattr != 'OrderBy' ) {
				this.orderByView.setOrderBy(nameattr);
				var stmt = nameattr ;
				if( this.orderByView.isDesc()) stmt += " desc";
				if( this.queryView != null ) {				
					this.queryView.fireAddConstraint(this.formName, "orderby", [stmt]);
				} else {
					Out.info("No Query View OB:" + stmt);
				}
			} else {
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}
		},
		fireAttributeEvent : function(ahname){
			var that = this;
			this.listener.controlAttributeEvent(ahname, that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		},
		fireClearAllConst : function() {
			this.constListView.fireClearAllConst();
			this.orderByView.fireClearAllConst();
			this.fireOrderBy('OrderBy');
			var that = this;
			this.listener.controlClearAllConst();	
		},
		fireClearConst : function(filter) {
			this.constListView.fireClearConst(filter);
			var that = this;
			this.listener.controlClearConst(filter);	
		},
		fireGetNumberOfEditor : function() {
			var that = this;
			var retour = 0;
			retour =  this.listener.controlGetNumberOfEditor();	
			return retour;
		},
		printTypoMsg : function(fault, msg){
			this.constListView.printTypoMsg(fault, msg);
		},
		isTypoGreen : function(){
			return constListView.printTypoMsg(fault, msg);
		},
		updateQuery : function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "column", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		},
		runQuery : function() {	
			if( this.queryView != null ) {
				this.queryView.runQuery();
			} else {
				Out.info("No Query View");
			}
		}, 
		getAttributeHandlers: function(){
			var that = this;
			var retour = 0;
			retour =  this.listener.controlGetAttributeHandlers();		
			return retour;
		},
		/*
		 * The purpose of the default value depends on the context
		 * Just used for regions right now
		 */
		getDefaultValue: function(){
			var that = this;
			var retour = 0;
			retour =  this.listener.controlGetDefaultValue();	
			return retour;
		},
		fieldsStored: function(ahs){
			this.fieldListView.displayFields(ahs);
		}
};

/**
 * Subclass of ConstQEditor_mVc handling the edition of UCD bases constraints
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function UcdQEditor_mVc(params /*{parentDivId,formName,queryView, help}*/){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.fieldListView = new UcdFieldList_mVc(params.parentDivId
			, this.formName
			, { stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	, orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	}
	);
};

/**
 * Method overloading
 */
UcdQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="background: transparent; width:450px;float: left;"></div>');
			var isPos = {"fieldset":"130px", "div":"102px"};
			this.constListId = this.constListView.draw(isPos);
			if( this.help != undefined)
				$('#' + this.constContainerId).append('<div style="width: 100%;"><span class=spanhelp>' + this.help + '</span></div>');
		}
	},		
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "ucd", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});

/**
 * Subclass of UcdQEditor_mVc handling the edition of UCD bases constraints in matchPattern
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function UcdPatternEditor_mVc(params /*{parentDivId, formName, queryView, help}*/){
	UcdQEditor_mVc.call(this, params);
};
/**
 * Method overloading
 */
UcdPatternEditor_mVc.prototype = Object.create(UcdQEditor_mVc.prototype, {	
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "relation", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});

/**
 * Subclass of ConstQEditor_mVc handling the edition of position based constraints
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function PosQEditor_mVc(params /*{parentDivId, formName, queryView, frames, urls}*/){
	ConstQEditor_mVc.call(this, params);
	params.editor = this;
	this.fieldListView = new ConeSearch_mVc(params);
	console.log("1");
};
/**
 * Method overloading
 */
PosQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			var that = this;
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="float: left;background: transparent; display: inline;"></div>');
			$("#"+this.constContainerId).append('<input  title="Click to add a search cone" class="bigarrowbutton" type="button">');
			var isPos = {"fieldset":"105px","div":"80px"};
			this.constListId = this.constListView.draw(isPos);
			this.parentDiv.find("input.bigarrowbutton").click(function() {that.fireAttributeEvent();});
			this.fieldListView.setRegionForm(
					function(data){
						if( data.userAction ){
							if( data.region.size.x > 5 || data.region.size.y > 5) {
									Modalinfo.error("The region size can't exceeded 5 deg. \nIts actual size is " + JSON.stringify(data.region.size));
							} else { 
								that.fireRegionEvent(data);				
								Modalinfo.closeRegion();
							}
						}
					});
		}
	},
	displayFields: { 
		value: function() {}
	},
	setTestForm : function() {
		var inputfield = $('#' + this.cooFieldId);
		var handler = (this.sesameURL != null) ? function() {
			Processing.show("Waiting on SESAME response");
			$.getJSON("sesame", {
				object : inputfield.val()
			}, function(data) {
				Processing.hide();
				if (Processing.jsonError(data, "Sesame failure")) {
					return;
				} else {
					inputfield.val(data.alpha + ' ' + data.delta);
				}
			});
		} : function() {
			Modalinfo.info("name resolver not implemented yet");
		};
		$("#poscolumns_CSsesame").parents("#posConstEditor").find("#posConstEditor_constcont").find("input:first").click(handler);
		$('#' + this.sesameId).click(handler);
		
	},
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "position", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	fireClearAllConst : { 
		value:  function() {
			this.constListView.fireClearAllConst();
			var that = this;
			this.listener.controlClearAllConst();	
			this.fieldListView.fireClearAllConst();
		}
	},

	fireRegionEvent: { 
		value: function(data) {		
			var that = this;
			if( data && data.userAction && data.isReady ) {
				var rq = '';
				if( data.region.format == "array2dim") {
					rq = '';
					for( var i=0 ; i<(data.region.points.length - 1) ; i++ ) {
						if( i > 0 ) rq += " ";
						rq += data.region.points[i][0] + " " + data.region.points[i][1];
					}

				} else if( data.region.format == "array") {
					rq = '';
					for( var i=0 ; i<data.region.points.length  ; i++ ) {
						if( i > 0 ) rq += " ";
						rq += data.region.points[i];
					}

				} else {
					Modalinfo.error(data.region.format + " not supported region format");
					return;
				}
				/*
				 * In region mode: only one constraint
				 */
				this.fireClearAllConst();
				console.log(rq);
				if( rq != '' ) {
					this.listener.controlAttributeEvent({type: "region", frame: "ICRS", position: rq, radius: 0}, that.constListId);
					$("#" + this.constListId + " span.help").attr("style","display:none;");
				}
				this.fieldListView.resetPosition();
			}
		}
	},
	fireAttributeEvent: { 
		value: function() {			
			var that = this;
//			this.listener.controlClearConst(".*region.*");	
//			this.listener.controlAttributeEvent(that.fieldListView.getSearchParameters(), that.constListId);
//			$("#" + this.constListId + " span.help").attr("style","display:none;");
//			this.fieldListView.resetPosition();
			var cooField = this.parentDiv.find("input[id$='CScoofield']");
			
			var handler = function(listener, constList, fieldListView) {
				Processing.show("Waiting on SESAME response");
				$.getJSON("sesame", {
					object : cooField.val()
				}, function(data) {
					Processing.hide();
					if (Processing.jsonError(data, "Sesame failure", "Name "+cooField.val()+" cannot be resolved")) {
						return;
					} else {
						cooField.val(data.alpha + ' ' + data.delta);
						listener.controlClearConst(".*region.*");	
						listener.controlAttributeEvent(that.fieldListView.getSearchParameters(), that.constListId);
						$("#" + constList + " span.help").attr("style","display:none;");
						fieldListView.resetPosition();
					}
				});
			}
			
			this.parentDiv.find("input.bigarrowbutton").click(handler(this.listener, this.constListId, this.fieldListView));

		}
	},
	fireAttributeAutoEvent: { 
		value: function() {			
			var that = this;
			/*
			 * More than One editor mean that he users have stacked multiple position constraints
			 */
			if( this.fireGetNumberOfEditor() <= 1 && this.fieldListView.hasSearchParameters() ) {
				//this.fireClearAllConst();
				$("#" + this.constListId + " span.help").attr("style","display:none;");
				this.listener.controlAttributeEvent(that.fieldListView.getSearchParameters(), that.constListId);
			}
		}
	},
	firePoslistUpload: { 
		value: function(filename, radius) {			
			var that = this;
			this.constListView.fireRemoveAllHandler();
			this.listener.controlAttributeEvent({position: "poslist:" + filename, radius: radius, frame: 'ICRS'}, that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	}
});

/**
 * Subclass of CatalogueQEditor_mVc handling the edition of catalogue based constraints in matchPattern
 */
function CatalogueQEditor_mVc(params /*{parentDivId, formName, getMetaUrl, queryView, relationName, distanceQualifer, help}*/){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.fieldListView = new CatalogueList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	}
	);
};
/**
 * Method overloading
 */
CatalogueQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			var that = this;
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="width:450px;float: left;background: transparent; display: inline;"></div>');
			var isPos = {"fieldset":"131px", "div":"102px"};
			this.constListId = this.constListView.draw(isPos);
			$('#' + this.constContainerId).append('<div style="width: 100%;"><span class=spanhelp>' + this.help + '</span></div>');
			if( !this.fieldsLoaded ) {
				this.listener.controlLoadFields();
				this.fieldsLoaded = true;
			}
		}
	},		
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "relation", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});


/**
 * Subclass of tapQEditor_mVc handling the edition of tap queries
 */
function tapColSelector_mVc(params){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.constPosContainer   = params.parentDivId + "_constposcont";
	this.fieldListView = new TapColList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	, orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	}	        
	, params.sessionID
	);
	this.fieldListView.setStackTooltip("Click to select this field");
};
/**
 * Method overloading
 */
tapColSelector_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div >'
					//	+ '<div id=' + this.constPosContainer + '></div>'
					+ '<div id=' + this.constContainerId + '></div>'
					+ '</div>');
			var isADQL = true;
			this.constListId = this.constListView.draw(null, isADQL);
			this.orderByView.draw();
		}
	},
	// handler is called after the form is uptdated: query submission e.g.
	fireSetTreepath : { 
		value: function(treePath, handler){
			var that = this;
			if( this.dataTreePath != null ){
				this.fireClearAllConst();
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}

			this.dataTreePath = treePath;	
			// table name can include the schema
			//this.dataTreePath.table = this.dataTreePath.table.split('.').pop();
			this.queryView.fireSetTreePath(this.dataTreePath);
			this.queryView.fireAddConstraint(this.formName, "select", ["*"]);
			this.orderByView.fireClearAllConst();
			this.listener.controlLoadFields(that.dataTreePath, handler);
			this.fieldListView.setDataTreePath(this.dataTreePath);
		}
	},
	addTableOption: { 
		value: function(treePath) {
			this.fieldListView.addTableOption(treePath);
		}
	},
	fireAttributeEvent :  { 
		value: function(ahname){
			var that = this;
			this.listener.controlAttributeHandlerEvent(that.fieldListView.getAttributeHandler(ahname), that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},		
	fireOrderBy :  { 
		value : function(nameattr){
			var tpn ="";
			if( nameattr != 'OrderBy' ) {
				tpn = this.fieldListView.dataTreePath.schema + "." + this.fieldListView.dataTreePath.table + "." + nameattr;
				var qtpn = tpn.quotedTableName().tableName;
				this.orderByView.setOrderBy(tpn);
				var stmt = nameattr ;
				if( this.orderByView.isDesc()) qtpn += " desc";
				if( this.queryView != null ) {				
					this.queryView.fireAddConstraint(this.formName, "orderby" , [qtpn]);
				} else {
					Out.info("No Query View OB:" + stmt);
				}
			} else {
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}
			var that = this;
			this.listener.controlOrderBy(tpn);

		}
	},
	updateQuery  :  { 
		value:function(consts, joins) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "select", consts, joins);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	getCurrentTreePath: {
		value: function(){
			return this.fieldListView.dataTreePath;
		}
	}

});


/**
 * @param parentDivId
 * @param formName
 * @param queryview
 * @param getTableAttUrl
 * @param sesameUrl
 * @param upload: {url,  postHandler: called on success}
 * @param sessionID
 * @param help
 * @returns {tapQEditor_mVc}
 */
function tapQEditor_mVc(params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/){
	tapColSelector_mVc.call(this, params);
	var that = this;
	this.fieldListView = new TapFieldList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	, radec: false}	        
	, params.sessionID
	);
	this.fieldListView.setStackTooltip("Click to constrain this field");
	this.posEditor = new TapSimplePos_mVc({editor: this
		, parentDivId: this.constPosContainer
		, formName: this.formName
		, frames: null
		, urls: {sesameURL: params.sesameUrl, uploadURL:params.upload.url}
	, postUploadHandler: params.upload.postHandler
	});
};
/**
 * Method overloading
 */
tapQEditor_mVc.prototype = Object.create(tapColSelector_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div >'
					+ '<div id=' + this.constPosContainer + '></div>'
					+ '<div id=' + this.constContainerId + '></div>'
					+ '</div>');

//			$("#" +  this.constContainerId).append('<div class=constdiv><fieldset class="constraintlist">'
//			+ '<legend>Position</legend>'
//			+ '<span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
//			+ '</fieldset>'
//			+ '</div>');
			//this.posEditor.draw();
			var isADQL = true;
			this.constListId = this.constListView.draw(null, isADQL);
			//this.orderByView.draw();
		}
	},
	displayFields: { 
		value: function(attributesHandlers){
			this.fieldListView.displayFields(attributesHandlers);
			this.fieldListView.lookForAlphaKeyword();
			this.fieldListView.lookForDeltaKeyword();
			return;
		}
	},
	fireAttributeEvent :  { 
		value: function(ahname){
			var that = this;
			this.listener.controlAttributeHandlerEvent(that.fieldListView.getAttributeHandler(ahname), that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},		
	fireInputCoordEvent :  { 
		value : function(ra, dec, radius, frame){
			var that = this;
			var rakw = this.fieldListView.getRaKeyword();
			if( rakw.length == '' ) {
				Modalinfo.error("RA field not set");
				return;
			}
			var deckw = this.fieldListView.getDeltaKeyword();
			if( deckw.length == '' ) {
				Modalinfo.error("DEC field not set");
				return;
			}
			this.listener.controlInputCoord(ra, dec, radius, frame
						, rakw, deckw, that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},
	updateQuery  :  { 
		value:function(consts, joins) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "column", consts, joins);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	isReadyToUpload :  { 
		value:function(consts, joins) {	
			if( this.fieldListView.getRaKeyword().length == '' || this.fieldListView.getDeltaKeyword() == '' ) {
				Modalinfo.error("Both RA and DEC fields must be set");
				return false;
			}
			return true;
		}
	},
	getUploadedFile: {
		value: function(){
			return this.posEditor.uploadedFile;
		}
	}
});

/**
 * @param parentDivId
 * @param formName
 * @param queryview
 * @param getTableAttUrl
 * @param sesameUrl
 * @param upload: {url,  postHandler: called on success}
 * @param sessionID
 * @param help
 * @returns {tapPosQEditor_mVc}
 */
function tapPosQEditor_mVc(params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/){
	tapColSelector_mVc.call(this, params);
	var that = this;
	this.fieldListView = new TapFieldList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: null
	, orderByHandler: null
	, raHandler: null
	, decHandler: null
	, radec: true}	        
	, params.sessionID
	);
	this.fieldListView.setStackTooltip("Click to constrain this field");
	this.posEditor = new TapSimplePos_mVc({editor: this
		, parentDivId: this.constPosContainer
		, formName: this.formName
		, frames: null
		, urls: {sesameURL: params.sesameUrl, uploadURL:params.upload.url}
	, postUploadHandler: params.upload.postHandler
	});
};
/**
 * Method overloading
 */
tapPosQEditor_mVc.prototype = Object.create(tapColSelector_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div>'
					+ '<div id=' + this.constPosContainer + '></div>'
					+ '<div id=' + this.constContainerId + '></div>'
					+ '</div>');

//			$("#" +  this.constContainerId).append('<div class=constdiv><fieldset class="constraintlist">'
//			+ '<legend>Position</legend>'
//			+ '<span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
//			+ '</fieldset>'
//			+ '</div>');
			this.posEditor.draw();
			
			var isPos = {"fieldset":"inherit", "div":"102px"};
			var isADQL = true;
			this.constListId = this.constListView.draw(isPos, isADQL);
			//this.orderByView.draw();
		}
	},
	displayFields: { 
		value: function(attributesHandlers){
			this.fieldListView.displayFields(attributesHandlers);
			this.fieldListView.lookForAlphaKeyword();
			this.fieldListView.lookForDeltaKeyword();
			return;
		}
	},
	fireAttributeEvent :  { 
		value: function(ahname){
			var that = this;
			this.listener.controlAttributeHandlerEvent(that.fieldListView.getAttributeHandler(ahname), that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},		
	fireInputCoordEvent :  { 
		value : function(ra, dec, radius, frame){
			var that = this;
			var rakw = this.fieldListView.getRaKeyword();
			if( rakw.length == '' ) {
				Modalinfo.error("RA field not set");
				return;
			}
			var deckw = this.fieldListView.getDeltaKeyword();
			if( deckw.length == '' ) {
				Modalinfo.error("DEC field not set");
				return;
			}
			this.listener.controlInputCoord(ra, dec, radius, frame
						, rakw, deckw, that.constListId);
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},
	updateQuery  :  { 
		value:function(consts, joins) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "column", consts, joins);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	isReadyToUpload :  { 
		value:function(consts, joins) {	
			if( this.fieldListView.getRaKeyword().length == '' || this.fieldListView.getDeltaKeyword() == '' ) {
				Modalinfo.error("Both RA and DEC fields must be set");
				return false;
			}
			return true;
		}
	},
	getUploadedFile: {
		value: function(){
			return this.posEditor.uploadedFile;
		}
	}
});


console.log('=============== >  ConstQEditor_v.js ');

ConstQEditor_mvC = function(view,model){
	/**
	 * listen to the view
	 */

	var vlist = {
			controlLoadFields: function(treePath, handler){
				model.loadFields(treePath, handler);
			},
			controlAttributeEvent: function(ahname, constListId){
				model.processAttributeEvent(ahname, constListId);
			},
			controlAttributeHandlerEvent: function(ah, constListId){
				model.processAttributeHandlerEvent(ah, constListId);
			},
			controlClearAllConst : function() {
				model.clearAllConst();
			},
			controlClearConst : function(filter) {
				model.clearConst(filter);
			},
			controlGetNumberOfEditor : function() {
				return model.getNumberOfEditor();
			},
			controlInputCoord: function(ra, dec, radius, frame, rakw, deckw, constListId) {
				model.processInputCoord(ra, dec, radius, frame, rakw, deckw, constListId);
			},
			controlOrderBy: function(nameattr) {
				model.processOrderBy(nameattr);
			},
			controlGetAttributeHandlers: function() {
				return model.getAttributeHandlers();
			},
			controlGetDefaultValue: function() {
				return model.getDefaultValue();
			}
	};

	view.addListener(vlist);

	var mlist = {
			nextListener: function(){
				view.nextListener();
			}, 
			controlTypoMsg : function(fault, msg) {
				view.printTypoMsg(fault, msg);
			},
			controlTypoGreen : function() {
				return view.isTypoGreen();
			},
			controlUpdateQuery : function(consts, joins) {
				view.updateQuery(consts, joins);
			},
			controlRunQuery : function(consts) {
				view.runQuery(consts);
			},
			controlAddTableOption: function(treePath) {
				view.addTableOption(treePath);
			},
			controlCurrentTreePath: function() {
				return view.getCurrentTreePath();
			}, 
			controlFieldsStored: function(attributeHandlers) {
				view.fieldsStored(attributeHandlers);
			}
	};

	model.addListener(mlist);
};

console.log('=============== >  ConstQEditor_c.js ');

/**
 * @returns {QueryTextEditor_Mvc}
 */
function QueryTextEditor_Mvc() {
	this.colConst =  new Array();
	this.obConst  = new Array();
	this.ucdConst = new Array();
	this.posConst = new Array();
	this.relConst = new Array();
	this.kwConst  = new Array();
	this.fitConst = new Array();
	this.limConst = "";
	this.query = "";
	this.treePath = null;
	this.listener = null;
};

QueryTextEditor_Mvc.prototype = {
		addListener : function(list){
			this.listener = list;
		},
		getQuery: function() {
			return this.query;
		},
		reset: function(){
			this.colConst =  new Array();
			this.obConst  = new Array();
			this.ucdConst = new Array();
			this.posConst = new Array();
			this.relConst = new Array();
			this.kwConst  = new Array();
			this.fitConst = new Array();
			this.limConst = "";
			this.query = "";
			this.treePath = null;			
		},
		processAddConstraint: function(label, type, constraints) {
			Out.debug("add constraint of type " + type + " from form " + label);
			if( type == "column") {
				this.delConst(label, this.colConst);
				this.addConstraintToArray(label, constraints, this.colConst) ;
			} else if( type == "orderby" && constraints.length > 0) {
				this.obConst = constraints[0];				
			} else if( type == "ucd") {
				this.delConst(label, this.ucdConst);
				this.addConstraintToArray(label, constraints,this. ucdConst) ;
			} else if( type == "position") {
				this.delConst(label,this.posConst);
				this.addConstraintToArray(label, constraints, this.posConst) ;
			} else if( type == "relation") {
				this.delConst(label, this.relConst);
				this.addConstraintToArray(label, constraints, this.relConst) ;
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
				this.addConstraintToArray(label, constraints, this.kwConst) ;
			} else if( type == "fitconst") {
				this.delConst(label, this.fitConst);
				this.addConstraintToArray(label, constraints, this.fitConst) ;
			} else if( type == "limit" && constraints.length > 0) {
				this.limConst = constraints[0];				
			} else {
				Modalinfo.error("QueryTextEditor do not know what to do with a constraint typed as " +type);
			}		
			this.buildQuery();
			this.notifyDisplayQuery();
		},
		addConstraintToArray: function(label, constraints, constArray) {
			if(constraints != null &&  $.trim(constraints) != "" ) {
				constArray.push({label:label, constraints: constraints});
			}
		},
		processDelConstraint: function(label, type) {
			Out.debug("Del constraint of type " + type + " from form " + label);
			if( type == "column") {
				this.delConst(label, this.colConst);
			} else if( type == "orderby") {
				this.obConst = "";				
			} else if( type == "ucd") {
				this.delConst(label, this.ucdConst);
			} else if( type == "position") {
				this.delConst(label, this.posConst);
			} else if( type == "relation") {
				this.delConst(label, this.relConst);
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
			} else if( type == "fitconst") {
				this.delConst(label, this.fitConst);
			} else if( type == "limit") {
				this.limConst = "";												
			} else {
				Modalinfo.error("QueryTextEditor do not know what to do with a constraint typed as " + type);
			}	
			this.buildQuery();
			this.notifyDisplayQuery();
		},
		delConst: function(label, constArray) {
			for( var i=0 ; i<constArray.length ; i++ ) {
				if( constArray[i].label == label){
					constArray.splice(i,1);
					return;
				}
			}
		},
		buildQuery: function() {
			this.query =  
			  this.buildWhereConstraint("\nWherePosition"         , ","  , this.posConst)
			+ this.buildWhereConstraint("\nWhereAttributeSaada"   , "AND", this.colConst)
			+ this.buildWhereConstraint("\nWhereUCD"              , "AND", this.ucdConst)
			+ this.buildWhereConstraint("\nWhereRelation"         , " "  , this.relConst)
			+ this.buildWhereConstraint("\nHavingCounterpartsWith", ","  , this.kwConst)
			+ this.buildWhereConstraint("\nWhereModel"            , " "  , this.fitConst)
			if( this.obConst != "" ){
				this.query += "\nOrder By " + this.obConst + "\n";
			}
			if( this.limConst != "" && !isNaN(this.limConst) ){
				this.query += "\nLimit " + this.limConst + "\n";
			}
		},
		buildWhereConstraint : function(stmt, logical, constArray){
			var constrt ="";		
			var openPar = '';
			var closePar = '';
			if( logical == 'AND' ) {
				openPar = '(';
				closePar = ')';
			}
			for( var i=0 ; i<constArray.length ; i++ ){
				if( i > 0 ) {
					constrt += (logical.trim() == "") ?"\n":  ("\n    " + logical + "\n") ;
				}
				var tc = $.trim(constArray[i].constraints);
				constrt += (constArray.length > 1 )? ("    " +  openPar + tc + closePar):  constArray[i].constraints;
			}
			return (constrt == "")? "" : stmt + " {\n" + constrt + "\n}";
		},
		notifyDisplayQuery:  function() {
			this.listener.controlDisplayQuery(this.query);
		},
		setTreePath: function(treePath){
			this.treePath = $.extend({},treePath);
		}
};

/**
 * Subclass of QueryTextEditor_Mvc handling adql queries
 */
function ADQLTextEditor_Mvc(){
	QueryTextEditor_Mvc.call(this);
	this.selectConst = new Array();
	this.joinedTables = new JoinKeyMap();
	this.flatJoined = new Array();
	/*
	 * 
	 * {tablename -> {joinKey [select][orderby][constrainst]}
	 * joinKey {source_table, source_column, target_table, target_column}
	 */
	this.tableConst = new Array();

};
/**
 * Method overloading
 */
ADQLTextEditor_Mvc.prototype = Object.create(QueryTextEditor_Mvc.prototype, {	
	processAddConstraint: {
		value: function(label, type, constraints, tableJoin) {
			Out.debug("add constraint of type " + type + " from form " + label);
			if( type == "select") {
				this.selectConst = [{label:label, constraints: constraints}];
			} else if( type == "column") {
				this.delConst(label, this.colConst);
				this.addConstraintToArray(label, constraints, this.colConst) ;
			} else if( type == "orderby" && constraints.length > 0) {
				this.obConst = [{label:label, constraints: constraints[0]}];
			} else if( type == "position") {
				this.delConst(label,this.posConst);
				this.addConstraintToArray(label, constraints, this.posConst) ;
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
				this.addConstraintToArray(label, constraints, this.kwConst) ;
			} else if( type == "fitconst") {
				this.delConst(label, this.fitConst);
				this.addConstraintToArray(label, constraints, this.fitConst) ;
			}else if( type == "limit" && constraints.length > 0) {
				this.limConst = {label:label, constraints: constraints[0]};;				
			} else {
				Modalinfo.error("QueryTextEditor does not know what to do with a constraint typed as " +type);
				return;
			}	
			this.joinedTables.removeFromOrigin(label);
			this.flatJoined = new Array();
			if( tableJoin != null ) {
				for( var jt in tableJoin ) {
					/* 
					 * key of tableJoin array
					 *     [target_datatreepath.schema].[target_datatreepath.table];
					 * joinKey format:  
					 *     {"target_datatreepath":{"nodekey":"node","schema":"schema","table":"autre1","tableorg":"autre1","jobid":"","key":"node.schema.autre1"},
					 *      "target_column":"1autre1","source_column":"1schema.table"}
					 */
					var joinKey = tableJoin[jt];
					if( joinKey.target_column == "" ) {
						this.flatJoined.push(joinKey.target_table);
					} else {
						this.joinedTables.addJoinKey(jt, joinKey, label);
					}
				}
			}
			this.buildQuery();
			this.notifyDisplayQuery();
		}
	},

	getJoin : { 
		value: function() {
			var retour = "";
			var joinKeys = this.joinedTables.getJoinKeys();
			var arrayLength = joinKeys.length;
			for (var i = 0; i < arrayLength; i++) {
				var joinKey = joinKeys[i];
				var tt = (joinKey.target_datatreepath.schema + "." + joinKey.target_datatreepath.table).quotedTableName().qualifiedName;
				retour += "JOIN " + tt + " ON " 
				+ this.getCurrentTableName().quotedTableName().qualifiedName + "." +  joinKey.source_column.quotedTableName().qualifiedName
				+ " = " 
				+  tt + "." + joinKey.target_column.quotedTableName().qualifiedName  + "\n";		
			}
			return retour;
		}
	},

	buildQuery :{
		value: function() {
			var tableName = this.getCurrentTableName().quotedTableName().qualifiedName;
			var li = this.limConst.constraints;
			var topLimit = ( li != undefined) ? ' TOP ' + li + ' ': '';
			this.query = "";
			this.query += this.buildWhereConstraint("SELECT " + topLimit , ",", this.selectConst);
			this.query += "FROM " + tableName ;
			if( this.flatJoined != null ){
				for( var i in this.flatJoined) {
					this.query += ", " + this.flatJoined[i];
				}
			}
			this.query += "\n";
			this.query += this.getJoin();			
			this.query += this.buildWhereConstraint("WHERE"   , "AND", this.colConst);
			this.query += this.buildWhereConstraint("ORDER BY", ","  , this.obConst);
		}
	},

	buildWhereConstraint : {
		value: function(stmt, logical, constArray){
			var constrt ="";		
			var openPar = '';
			var closePar = '';
			if( logical == 'AND' ) {
				openPar = '(';
				closePar = ')';
			}
			for( var i=0 ; i<constArray.length ; i++ ){
				if( i > 0 ) {
					constrt += (logical.trim() == "") ?"\n":  ("\n    " + logical + "\n") ;
				}
				var tc = $.trim(constArray[i].constraints) ;
				constrt += (constArray.length > 1 )? "    " +  openPar + tc + closePar:  constArray[i].constraints;
			}
			return (constrt == "")? "" : stmt + " " + constrt + "\n";
		}
	},

	getCurrentTableName: {
		value: function(){
			return this.treePath.schema + "." + this.treePath.table;
		}
	}
});
/**
 * Object merging the keyJoin coming from different forms.
 * keyJoins remain unique but with a reference of all forms using them.
 * When a key is no longer used by a form and just by it, it is removed from the map.
 * If it is used by multiple keywords, the form label is removed from the origins array, 
 * but the keyJoin remains in place within the map.
 */
function JoinKeyMap() {
	/**
	 * Map key: 
	 *     [target_datatreepath.schema].[target_datatreepath.table];
	 * Map value
	 *     { keyJoin: {"target_datatreepath":{"nodekey":"node","schema":"schema","table":"autre1","tableorg":"autre1","jobid":"","key":"node.schema.autre1"},
	 *                 "target_column":"1autre1","source_column":"1schema.table"}
	 *       origins: []
	 * Origins contains the labels of the forms using the keyJoin
	 */
	this.keyMap =  new Array();
};
/**
 * Methods
 */
JoinKeyMap.prototype = {
		/**
		 * Add a joinKey to the map if it does not exist. 
		 * If the joinKey already exists, the origins array is updated
		 * @param key      map key: [target_datatreepath.schema].[target_datatreepath.table]
		 * @param joinKey  map value: see above for the format
		 * @param origin   form label
		 */
		addJoinKey : function(key, joinKey, origin){
			var jk = this.keyMap[key];
			if( jk == null ){
				this.keyMap[key] = {joinKey: joinKey, origins: [origin]};
			} else {
				console.log(this.keyMap[key]);
				var ors = jk.origins;
				var index = ors.indexOf(origin);
				if (index == -1) {
					jk.origins.push(origin);
				}
			}
		},
		/**
		 * remove the joinKey attached to origin it is only used by the form referenced by origin.
		 * If it is used by multiple keywords, the lebel origin is removed from the origins array, 		 
		 * @param origin form label
		 */
		removeFromOrigin : function(origin){
			for( var key in this.keyMap ) {
				var entry = this.keyMap[key];
				var jk = entry.joinKey;
				var ors = entry.origins;
				if( ors.length == 1 && ors[0] == origin){
					delete this.keyMap[key];
				} else {
					var index = ors.indexOf(origin);
					if (index > -1) {
					    ors.splice(index, 1);
					}
				}
			}
		},
		/**
		 * Used to iterate on the joinKeys
		 * @returns {Array} All joinKeys in an array
		 */
		getJoinKeys: function() {
			var retour = new Array();
			for( var k in this.keyMap ) {
				retour.push(this.keyMap[k].joinKey);
			}
			return retour;
		}
};

console.log('=============== >  QueryTextEditor_m.js ');


/**
 * @param params { parentDivId: 'query_div',defaultQuery}
 * @returns
 */
function QueryTextEditor_mVc(params) {
	this.parentDiv = $("#" +params.parentDivId );
	this.textareaid = params.parentDivId + "_text";
	this.defaultQuery = (params.defaultQuery.endsWith("\n"))? params.defaultQuery: params.defaultQuery + "";
	
	this.listener = null;
}
QueryTextEditor_mVc.prototype = {

		addListener : function(list){
			this.listener = list;
		},
		draw : function(){
			this.parentDiv.html('<textarea id="' + this.textareaid + '" class="querytext" id="Catalogue"></textarea><p class="help">Widgets do not reflect the query anymore after you modified it directly</p>');
			this.displayQuery("");
		},
		fireGetQuery: function() {
			return $("#" + this.textareaid ).val();
		},		
		/*
		 *	Params: {type, constraints}
		 *	where supported typed are "column" "orderby" "ucd" "position" "relation" "limit"
		 *  Label is used to identify the form  constraints are coming from
		 */
		fireAddConstraint : function(label, type, constraints, joins) {
			this.listener.controlAddConstraint(label, type, constraints, joins);
		},
		fireDelConstraint : function(label, type) {
			this.listener.controlDelConstraint(label, type);
		},
		fireSetTreePath : function(treePath) {
			this.listener.controlSetTreePath(treePath);
		},
		reset: function(defaultQuery){
			this.defaultQuery = defaultQuery;
			this.displayQuery("");
			this.listener.controlReset();
		},
		displayQuery : function(query) {
			$("#" + this.textareaid ).val(this.defaultQuery + query);
		},
		getQuery : function(query) {
			return $("#" + this.textareaid ).val();
		},
		toggle: function() {
			this.parentDiv.toggle();
		}
};

console.log('=============== >  QueryTextEditor_v.js ');

QueryTextEditor_mvC = function(view, model){
	/**
	 * listen to the view
	 */
	var vlist = {
			controlGetQuery : function(){
				return model.getQuery();
			},
			controlAddConstraint : function(label, type, constraints, joins){
				model.processAddConstraint(label, type, constraints, joins);
			},
			controlDelConstraint : function(label, type){
				model.processDelConstraint(label, type);
			},
			controlSetTreePath: function(treePath){
				model.setTreePath(treePath);
			},
			controlReset: function(defaultQuery){
				model.reset(defaultQuery);
			}
	};
	view.addListener(vlist);

	var mlist = {
			controlDisplayQuery : function(query){
				view.displayQuery(query);
			}
	};
	model.addListener(mlist);
};

console.log('=============== >  QueryTextEditor_c.js ');

/**
 * Pattern_mVc: Editor for constraint on SaadaQL relationships
 * 
 * @param parentDivId : ID of the div containing the query editor
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoid conflict by JQuery selectors 
 * @param queryview   : JS object supposed to take the constraint locally edited.
 *                      It supposed to have a public method names fireConstQuery(const)
 *                      where const is an object like {columnConst:"SQL stmt", orderConst:"att name"}
 *                     
 * @returns {ConstQEditor_mVc}
 */
function Pattern_mVc(params /*{parentDivId,formName,queryView}*/){
	var that = this;
	this.fieldsLoaded = false;
	this.dataTreePath = null; // instance of DataTreePath
	this.queryView = params.queryView;
	this.relations = {};
	/**
	 * DOM references
	 */
	this.parentDiv = $("#" + params.parentDivId );
	this.constContainerId   = params.parentDivId + "_constcont";
	this.patternContainerId   = params.parentDivId + "_pattern";
	this.constListId   = '';
	this.formName = params.formName;
	this.patternEditor;

	this.constListView = new ConstList_mVc(params.parentDivId
			, this.formName
			, this.constContainerId
			, function(ahName){ that.fireClearAllConst();}
	);
	this.editors =[];
	this.const_key = 0;
};

Pattern_mVc.prototype = {
		/**
		 * Instanciate components
		 */
		/**
		 * add a listener to this view
		 */
		addListener : function(listener){
			this.listener = listener;
		},
		draw : function() {
			this.parentDiv.append(
					'<div class="fielddiv" style="padding: 3px;">'
					+ '   <span class=help>Select the relationship</span><br>'
					+ '   <select id="relationSelector"  class="table_filter form-control input-sm"  style="width: 250px"></select>'
					+ '   <span class=help id=relationDescriptor></span>'
					+ '</div>'
			);

			this.parentDiv.append('<div id=' + this.constContainerId + ' style="width: 450px;float: left;background: transparent; display: inline;"></div>');
			var isPos = {"fieldset":"130px", "div":"102px"};
			this.constListId = this.constListView.draw(isPos);
			var that = this;
			$("#relationSelector").change(function(){
				if( !this.value.startsWith('--') ) {			
					var desc = "";
					var relation = that.relations[this.value];
					if( relation.description != undefined ) desc =  relation.description;
					desc += "Relationship pointing on " + relation.ending_collection + "." + relation.ending_category ;
					$("#relationDescriptor").html('<br>' 
							+ desc 
							+ '<br><a title="Click to open the pattern editor window">Edit Pattern</a>');
					$("#relationDescriptor a").click(function(){
						that.patternEditor = new PatternEditor_mVc({
							parentDivId: this.patternContainerId
							,patternView: that
							,relation: relation
						});
					}) 
				} else {
					$("#relationDescriptor").html('');
				}
			});
		},	
		fireSetRelations: function(relations){
			this.relations = {};
			$("#relationSelector").append('<option>---------</option>');
			$("#relationDescriptor").html('');
			for( var r=0 ; r<relations.length ; r++){ 
				var rr = relations[r]; 
				this.relations[rr.name] = rr;
				$("#relationSelector").append('<option>' + rr.name + '</option>');
			}
		},
		fireSetTreepath: function(dataTreePath){
			var rel = MetadataSource.getRelations(dataTreePath);
			this.fireSetRelations(rel);
		},
		fireClearAllConst : function() {
			this.constListView.fireClearAllConst();
			this.queryView.fireDelConstraint(this.formName, "pattern");
		},

		takePattern: function(relation, patternText){
			$("#" + this.constContainerId + " span.help").attr("style","display:none;");

			var divKey = this.constContainerId + "_" + relation.name + "_" + this.const_key;
			var v = new KWSimpleConstraint_mVc({divId: divKey
				, constListId: this.constListId
				, isFirst: true
				, editorModel: this
				, name: "Pattern on relation " + relation.name
				, defValue: patternText});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
			this.updateQuery();
		},
		processRemoveFirstAndOr : function(key) {	
			console.log(key);
			delete this.editors[key];
			this.const_key--;
			this.updateQuery();
		},
		updateQuery : function() {	
			var consts = [];
			for ( var k in this.editors){
				consts.push(this.editors[k].fireGetADQL());
			}
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "relation", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
};

/**
 * See the ConstList API
 * {
 * relation: JSON description of the relation given by the server
 * patternView: Calling pattern editor
 * parentDivId= ID of the div containing the parent. Just used to build inner div ids.
 * }
 */
function PatternEditor_mVc(params /*{parentDivId,patternView}*/){
	params.parentDivId = 'azerty';
	this.relation = params.relation;
	this.parentDivId = 'azerty';
	this.patternView = params.patternView;
	this.constDivId = params.parentDivId + "_const";
	this.patternDivId = this.constDivId + "_pattern";
	this.qualifierDivId = params.parentDivId + "_qualif";
	this.attDivId = params.parentDivId + "_att";
	this.constListId = '';
	this.fieldListView;
	this.qualifierListView;
	this.endpointKey;
	this.classes = {};
	this.editors =[];
	this.qualifiers = {};

	this.draw();
	this.setRelation();

}

PatternEditor_mVc.prototype = {
		draw : function() {
			Modalinfo.dataPanel('Pattern Editor for the Relation ' + this.relation.name, '<div class="patternContainer"  id="' + this.parentDivId + '"></div>');
			this.parentDiv = $("#" + this.parentDivId );
			this.parentDiv.append(
					  '<div class="patternContainerLeft">'
					+ '   <div id="' + this.qualifierDivId + '"style="width: 100%; height: 50%;">'
					+ '   </div>'
					+ '   <div id="' + this.attDivId + '" style="width: 100%; height: 50%;">'
					+ '   </div>'
					+ '</div>'
					+ '<div id="' + this.constDivId + '" class="patternContainerRight">'
					+ '</div>'
			);
			var that = this;
			this.fieldListView = new FieldList_mVc(this.attDivId
					, "this.formName"
					, {stackHandler: function(ahname){
						that.fireFieldEvent(ahname)}});
			this.fieldListView.draw();

			this.qualifierListView = new BasicFieldList_mVc(this.qualifierDivId
					, "this.formName"
					, {stackHandler: function(ahname){that.fireQualifierEvent(ahname, that.constListId)}});
			this.qualifierListView.draw();
			this.qualifierListView.displayField({nameatt: "qualifier", nameorg: "qualifier"});
			$('#' + this.qualifierDivId).append(
					'   <br><span  class=help>Select a data class</span>'
					+  '   <select id="patternClassSelector"  class="table_filter form-control input-sm"  style="width: 250px"></select>'
			);
			this.constListView = new ConstList_mVc(this.constDivId
					, "this.formName"
					, this.constDivId
					, function(ahName){ that.fireClearAllConst();}
			);
			this.constListId = this.constListView.draw();


			$("#" + this.constDivId).append("<textarea rows=7  style='margin-left: 5px; width: 100%;' id='" + this.patternDivId + "' ></textarea>");
			$("#" + this.constDivId).append("<br><br><div style='float: right;'>"
					+ "<input type='button' value='Cancel' onclick='Modalinfo.close();' ></input>&nbsp;"
					+ "<input id='acceptPattern' type='button' value='Add to Query' style='font-weight: bold;'></input></div>");
			$("#acceptPattern").click(function(){
				var x =  $("#" + that.patternDivId);
				var y = x.val();
				var z = x.text();
				that.patternView.takePattern(that.relation, y);
				Modalinfo.close();
			});
		},

		setRelation: function(){
			var ahm = [{"nameattr": "Cardinality", "nameorg": "Cardinality", "type": "integer"}];
			this.qualifiers["Cardinality"]  = ahm[0];
			for( var q=0 ; q<this.relation.qualifiers.length ; q++){
				var ah = {"nameattr": this.relation.qualifiers[q], "nameorg": "Qualifier " +  this.relation.qualifiers[q], "type": "double"};
				ahm.push(ah)
				this.qualifiers[ah.nameattr]  = ah;
			} 
			this.endpointKey = this.relation.ending_collection + "." + this.relation.ending_category;
			this.fireSetClasses();
			this.qualifierListView.displayAttributeHandlers(ahm);
			this.fieldListView.displayAttributeHandlers(MetadataSource.getTableAtt({nodekey: this.endpointKey}))		;
			this.updateQuery();
		},
		fireSetClasses: function(){
			this.classes = MetadataSource.getClasses({nodekey: this.endpointKey});
			$("#patternClassSelector").append('<option>Any Class</option>');
			for( var r=0 ; r<this.classes.length ; r++){ 
				$("#patternClassSelector").append('<option>' + this.classes[r] + '</option>');
			}
			var that = this;
			$("#patternClassSelector").change(function(){
				that.removeClassFields();
				if( !this.value.startsWith('Any') ) {
//					var x = MetadataSource.getTableAtt({nodekey: this.value});
//					that.fieldListView.displayAttributeHandlers(x);		
					
					MetadataSource.getTableAtt({nodekey: this.value}, function(cache){
						that.fieldListView.displayAttributeHandlers(cache.hamap);
					})
				} else {
					//that.fieldListView.displayAttributeHandlers(MetadataSource.getTableAtt({nodekey: that.endpointKey}))	;
					MetadataSource.getTableAtt({nodekey: that.endpointKey}, function(cache){
						that.fieldListView.displayAttributeHandlers(cache.hamap);
					})
				}
				that.updateQuery()
			});
		},	
		/**
		 * Add a constraint on a qualifier
		 */
		fireQualifierEvent: function(ahname){
			$("#" + this.constListId + " span.help").attr("style","display:none;");
			ah = this.qualifiers[ahname];

			var divKey = this.constDivId + ah.nameattr;
			if( this.editors[divKey] != undefined )
				return
				var v = new KWConstraint_mVc({divId: divKey
					, constListId: this.constListId
					, isFirst: true
					, attributeHandler: ah
					, editorModel: this
					, defValue: ''});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		},
		fireFieldEvent: function(ahname){
			$("#" + this.constListId + " span.help").attr("style","display:none;");
			ah = this.fieldListView.getAttributeHandler(ahname);

			var divKey = this.constDivId + ah.nameattr;
			var v = new KWConstraint_mVc({divId: divKey
				, constListId: this.constListId
				, isFirst: true
				, attributeHandler: ah
				, editorModel: this
				, defValue: ''});
			this.editors[divKey] = v;
			v.fireInit();
			this.const_key++;
		},
		/**
		 * remove all constraints applied to a classs field
		 */
		removeClassFields: function(){
			for( var k in this.editors ){
				var kedit = this.editors[k]
				var ah = kedit.getAttributeHandler();
				if( ah.nameattr.startsWith('_')){
					this.constListView.fireClearConst(".*" + ah.nameattr);
					delete this.editors[k];
				}
			}
			this.updateQuery();
		},

		notifyTypoMsg : function(fault, msg){
			this.constListView.printTypoMsg(fault, msg);	
		},
		processRemoveConstRef: function(ahname) {
			for( var k in this.editors ){
				var kedit = this.editors[k]
				var ah = kedit.getAttributeHandler();
				if( ah.nameattr == ahname || ah.nameorg == ahname ){
					this.constListView.fireClearConst(".*" + ah.nameattr);
					delete this.editors[k];
					break;
				}
			}

			delete this.editors[key];
			this.updateQuery();
		},
		/**
		 * Just to keep compliant with the ConsQEditor API
		 * @param key
		 */
		processRemoveFirstAndOr : function(key) {
			this.updateQuery();
		},
		updateQuery : function() {
			var query = "  matchPattern {\n"
				+ "    " + this.relation.name;

			for( var k in this.editors ){
				var kedit = this.editors[k]
				var ah = kedit.getAttributeHandler();
				var q;
				if( ah.nameattr == "Cardinality" && (q = kedit.fireGetADQL()) != null ){
					query += ",\n    " + q;
				}
			}
			for( var k in this.editors ){
				var kedit = this.editors[k]
				var ah = kedit.getAttributeHandler();
				var q;
				if( ah.nameorg.startsWith("Qualifier ")  && (q = kedit.fireGetADQL()) != null ){
					query += ",\n     Qualifier{" + q + "}";
				}
			}
			var qa = "";
			var classe = null
			for( var k in this.editors ){
				var kedit = this.editors[k]
				var ah = kedit.getAttributeHandler();
				var q;
				if( !ah.nameorg.startsWith("Qualifier ")  &&  ah.nameattr != "Cardinality" && (q = kedit.fireGetADQL()) != null ){
					if( ah.nameattr.startsWith("_") ){
						classe = $("#patternClassSelector option:selected").text();
					}
					qa += " " + q;
				}
			}
			if( qa != "" ){
				if( classe == null ){
					query += ",\n     AssObjAtt{" + qa + "}";		
				} else {
					query += ",\n     AssObjClass{" + classe + "}";		
					query += ",\n     AssObjAttClass{" + qa + "}";		
				}
			}
			query += "\n  }";
			$('#' + this.patternDivId).text(query);
		}
}





console.log('=============== >  Pattern_v.js ');


/***********************************************************************************************
 * Javascript classes common to all Web application supported by LM
 * These classes  refer to some basic web resource
 * 
 * Content:
 * - Modalinfo.simbad : open a Simbad view in a modal dialog
 * - Modalinfo.uploadForm : open a Simbad dialog box handling file upload 
 * and all Query editor components
 * - DataLinkBrowser
 * Required external resources 
 * - basics.js
 * - jquery-ui
 * - jquery.alerts
 * - jquery.datatables
 * - jquery.simplemodal
 * - jquery.prints
 * - jquery.tooltip
 * - jquery.forms
 * 
 * Laurent Michel 20/12/2012
 */

var supportedUnits =  [
                       {id: 'none', text: "none"}

                       , {id: 'Power_erg/s', text: "erg/s"}
                       , {id: 'Power_W', text: "W"}

                       , {id: 'Flux_erg/s/cm2', text: "erg/s/cm2"}
                       , {id: 'Flux_Jy', text: "Jy"}
                       , {id: 'Flux_mJy', text: "mJy"}
                       , {id: 'Flux_mJy', text: "mJy"}

                       , {id: 'Angle_deg', text: "deg"}
                       , {id: 'Angle_arcmin', text: "arcmin"}
                       , {id: 'Angle_arcsec', text: "arcsec"}
                       , {id: 'Angle_h:m:s', text: "h:m:s"}

                       , {id: 'Velocity_m/s', text: "m/s"}
                       , {id: 'Velocity_km/s', text: "km/s"}
                       , {id: 'Velocity_km/h', text: "km/h"}
                       , {id: 'Velocity_mas/yr', text: "mas/yr"}

                       , {id: 'Length_kpc', text: "kpc"}
                       , {id: 'Length_pc', text: "pc"}
                       , {id: 'Length_AU', text: "AU"}
                       , {id: 'Length_km', text: "km"}
                       , {id: 'Length_m', text: "m"}
                       , {id: 'Length_cm', text: "cm"}
                       , {id: 'Length_mm', text: "mm"}
                       , {id: 'Length_um', text: "um"}
                       , {id: 'Length_nm', text: "nm"}
                       , {id: 'Length_Angstroem', text: "Angstroem"}

                       , {id: 'Energy_erg', text: "erg"}
                       , {id: 'Energy_eV', text: "eV"}
                       , {id: 'Energy_keV', text: "keV"}
                       , {id: 'Energy_MeV', text: "MeV"}
                       , {id: 'Energy_GeV', text: "GeV"}
                       , {id: 'Energy_TeV', text: "TeV"}
                       , {id: 'Energy_J', text: "J"}
                       , {id: 'Energy_ryd', text: "ryd"}

                       , {id: 'Frequency_Hz', text: "Hz"}
                       , {id: 'Frequency_KHz', text: "KHz"}
                       , {id: 'Frequency_MHz', text: "MHz"}
                       , {id: 'Frequency_GHz', text: "GHz"}
                       , {id: 'Frequency_THz', text: "THz"}

                       , {id: 'Time_y', text: "y"}
                       , {id: 'Time_d', text: "d"}
                       , {id: 'Time_h', text: "h"}
                       , {id: 'Time_mn', text: "mn"}
                       , {id: 'Time_sec', text: "sec"}
                       , {id: 'Time_msec', text: "msec"}
                       , {id: 'Time_nsec', text: "nsec"}
                       ];

function STCRegion(stcString) {
	this.stcString = stcString;
	this.size = 0.0;
	this.raCenter = 0.0;
	this.decCenter = 0.0;			
	this.points = [];

	this.init();
}
STCRegion.prototype = {
		init: function(){
			var elements  = this.stcString.split(" ");
			var coords = [];
			/*
			 * Extract coordinates from STC string
			 */
			for( var i=0 ; i<elements.length ; i++){
				if( isNumber(elements[i])) {
					coords.push(parseFloat(elements[i]));
				}
			}
			if((coords.length %2) ){
				Modalinfo.error("STC Region " + this.stcString + " is not valid");
			} else {
				/*
				 * Get the coords extrema
				 */
				var raMin = 360, raMax = 0;
				var decMin = 90, decMax = -90;
				for( var i=0 ; i<(coords.length/2) ; i++){
					var ra = coords[2*i];
					var dec = coords[(2*i) + 1];
					if( ra > raMax ) raMax = ra;
					if( ra < raMin ) raMin = ra;
					if( dec > decMax ) decMax = dec;
					if( dec < decMin ) decMin = dec;
				}
				/*
				 * Get size and center
				 */
				var width = Math.abs(raMin - raMax);
				var height = Math.abs(decMin - decMax);
				this.size = (width > height) ? width: height;
				this.raCenter = raMin + width/2;
				if( this.raCenter > 360 ) this.raCenter -= 360;
				this.decCenter = decMin + height/2;
				if( this.decCenter > 90 ) this.decCenter -= 90;
				/*
				 * Build the point array for Aladin Lite
				 */
				for( var i=0 ; i<(coords.length/2) ; i++){
					this.points.push([coords[2*i], coords[(2*i) + 1]]);

				}
				this.points.push([coords[0], coords[1]]);
			}

		},
		getAladinScript : function(list){
			return this.stcString + ";sync; " 
			+ this.raCenter.toFixed(6) + " " + this.decCenter.toFixed(6) + ";sync;" 
			+ " zoom " + 2*this.size + " deg;";
		}
}
/**
 * Object modeling a dataTreePath
 * 
 * Code de test sur JSFILLDE
	var dtp = new DataTreePath({signature:"a.b.c"});
	alert(JSON.stringify(dtp));
	dtp = new DataTreePath({nodekey:"node", schema: "schema", table: "table"});
	alert(JSON.stringify(dtp));
	dtp = new DataTreePath({nodekey:"node", schema: "schema", tableorg: "tableorg"});
	alert(JSON.stringify(dtp));
	dtp = new DataTreePath({nodekey:"node", schema: "schema", table: "table", tableorg: "tableorg"});
	alert(JSON.stringify(dtp));
	dtp = new DataTreePath({nodekey:"node", schema: "schema", table: "table", tableorg: "tableorg", jobid: "jobid"});
	alert(JSON.stringify(dtp));
 *
 * Used to make sure that dataTreePath given as parameters as more or less consistent 
 * @param params
 * @returns {DataTreePath}
 */
function DataTreePath(params){
	this.nodekey = "notset";
	this.schema = "notset";
	this.table = "notset";
	this.tableorg = "notset";
	this.tableorg = "notset";
	this.jobid = "";
	this.key = "notset";
	/*
	 * received a signature like nodekey.schema.table
	 */
	if( 'signature' in params ){
		var fields = params.signature.split(".");
		this.nodekey = fields[0];
		if( fields.length > 1 ) {
			this.schema = fields[1];
			if( fields.length > 2 ) {
				this.table = fields[2];
				this.tableorg = fields[2];
				if( fields.length > 3 ) {
					this.jobid = fields[3];
				}
			}
		}
		/*
		 * received fields in a JSON object
		 */	
	} else {
		if( 'jobid' in params ){
			this.jobid = params.jobid;
		}
		if( 'nodekey' in params ){
			this.nodekey = params.nodekey;
		}
		if( 'schema' in params ){
			this.schema = params.schema;
		}
		if( 'table' in params ){
			this.table = params.table;
		}
		if( 'tableorg' in params ){
			this.tableorg = params.tableorg;
		}
		if(  this.table == "notset" && this.tableorg != "notset")
			this.table = this.tableorg;
		else if(  this.table != "notset" && this.tableorg == "notset")
			this.tableorg = this.table;
	}
	this.key = this.nodekey + "." + this.schema + "." + this.tableorg + ((this.jobid)? ("." + this.jobid): "");
};

MetadataSource = function() {
	var getMetaTableUrl = null;
	var getJoinedTablesUrl = null;
	var getUserGoodieUrl = null;
	var isAjaxing = false;
	/*
	 * buffer = {  dataTreePath
	 *   hamap <== map of ah
	 *   targets:[{target_datatreepath, source columns, target column}...]
	 *
	 * cache = map<DataTreePath.key, buffer>
	 */
	var cache = new Object;
	/**
	 * private methods
	 */
	/*
	 * setAttMap: Normalize AHs and store them in the buffer
	 */
	var buildAttMap = function (data){
		var hamap  = new Array();
		if( data.attributes ) {
			for( var i=0 ; i<data.attributes.length ; i++ ){
				var ah = data.attributes[i];
				/*
				 * Make sure that all naming fields are populated
				 * if possible:
				 */
				if( !ah.nameattr && ah.column_name  ) {
					ah.nameattr = ah.column_name;
				} else if( ah.nameattr && !ah.column_name ) {
					ah.column_name = ah.nameattr ;
				}
				if( !ah.nameattr  && ah.nameorg ){
					ah.nameattr = ah.nameorg ;
					ah.column_name = ah.nameorg ;
				} else if (!ah.nameorg ) {
					ah.nameorg = ah.nameattr ;
				}
				hamap.push(ah);
			}
		} 
		return hamap;
	};
	/**
	 * public methods
	 */
	/**
	 * init(): params : {getMetaTable, getJoinedTables, getUserGoodie}
	 */
	var init = function (params) {
		if( 'getMetaTable' in params ){
			getMetaTableUrl = params.getMetaTable;
		}
		if( 'getJoinedTables' in params ){
			getJoinedTablesUrl = params. getJoinedTables;
		}
		if( 'getUserGoodie' in params ){
			getUserGoodieUrl = params.getUserGoodie;
		}
	};
	/**
	 * dataTreePath: instance of the classe DataTreePath
	 * handler(data) called when requested datra are retreived. 
	 * These data are passed as parameter {ahmap
	 */
	var getTableAtt =  function(dataTreePath /* instance of DataTreePath */, handler){
		/*
		 * Query a new node
		 */
		//key = JSON.stringify(dataTreePath) 
		key = dataTreePath.nodekey;
		if(  cache[key] == undefined ) {
			var buffer = {};
			buffer.dataTreePath = dataTreePath;		
			buffer.hamap        = new Array();
			buffer.relations    = new Array(); // utilisees par Saada
			buffer.classes      = new Array(); // utilisees par Saada
			buffer.targets      = new Array();
			if( getMetaTableUrl != null ) {
				isAjaxing = true;
				Out.info("Connect new node  " + key);

				var params = {jsessionid: this.sessionID, nodekey: buffer.dataTreePath.nodekey, schema:buffer.dataTreePath.schema, table:buffer.dataTreePath.tableorg};
				Processing.show("Get column description of table " +JSON.stringify(dataTreePath) );
				$.ajax({
					url: getMetaTableUrl,
					dataType: 'json',
					async: false,
					data: params,
					success:  function(data)  {
						Processing.hide();
						if( !Processing.jsonError(data)) {
							buffer.hamap = buildAttMap(data);
							if( data.relations!= undefined ){
								buffer.relations = data.relations;
							} else{
								buffer.relations = [];
							}
							if( data.classes!= undefined ){
								buffer.classes = data.classes;
							} else{
								buffer.classes = [];
							}
							isAjaxing = false;
							cache[key] = buffer;
							if( handler != null ) {
								handler(cache[key]);
							}
						}
					}
				});
				if( getJoinedTablesUrl != null) {
					Processing.show("Waiting on join keys " + getJoinedTablesUrl);
					$.ajax({
						url: getJoinedTablesUrl,
						dataType: 'json',
						async: false,
						data: params,
						success:  function(jdata)  {
							Processing.hide();
							if( jdata && !jdata.errormsg )  {
								var dt = jdata.targets;			
								for( var i=0 ; i<dt.length ; i++ ) {
									var t = dt[i];
									var st = t.target_table.split('.');
									var schema, table;
									if( st.length > 1) {
										schema =  st[0]; table = st[1];
									} else {										
										schema =  dataTreePath.schema; table = st[0];
									}
									var tdtp = new DataTreePath({nodekey: dataTreePath.nodekey, schema: schema, table: table});
									buffer.targets.push({target_datatreepath: tdtp, target_column: t.target_column, source_column: t.source_column});
								}
							} else {
								Out.info((jdata == null)? 'Empty data' : jdata.errormsg);
							}
							isAjaxing = false;
							cache[key] = buffer;
							if( handler != null ) handler();
						}
					});
				}
			}
//				//cache[key] = buffer;
//				//}
//			} else {
//				Out.info("No getMetaTableUrl provided" );
//			}
//			//if( handler != null ) handler();
//			if( handler!= null ) handler(cache[key]); else Out.info("No handler");
			return cache[key];

			/*
			 * Meta data are in the cache
			 */
		} else  {
			Out.info("get stored node  " + key + " length = " + cache[key].hamap.length);
			if( handler!= null ) handler(cache[key]); else Out.info("No handler");
			return cache[key].hamap;
		}
	};
	var getRelations =  function(dataTreePath /* instance of DataTreePath */){
		if( cache[dataTreePath.nodekey] == undefined )
			this.getTableAtt(dataTreePath);
		return cache[dataTreePath.nodekey].relations;
	};
	var getClasses =  function(dataTreePath /* instance of DataTreePath */){
		if( cache[dataTreePath.nodekey] == undefined )
			this.getTableAtt(dataTreePath);
		return cache[dataTreePath.nodekey].classes;
	};
	var getTableAttASync =  function(dataTreePath /* instance of DataTreePath */, handler){
		/*
		 * Query a new node
		 */
		if( isAjaxing ) {
			setTimeout(function(){MetadataSource.getTableAtt(dataTreePath, handler);}, 100);
			return;
		} 
		//console.log("Looking for " + JSON.stringify(dataTreePath) + " " + isAjaxing);
		if( !cache || !cache[dataTreePath.key] ) {
			buffer = {};
			buffer.dataTreePath = dataTreePath;		
			buffer.hamap        = new Array();
			buffer.targets      = new Array();
			if( getMetaTableUrl != null ) {
				isAjaxing = true;
				Out.infoTrace("Connect new node  " + dataTreePath.nodekey);

				var params = {jsessionid: this.sessionID, nodekey: buffer.dataTreePath.nodekey, schema:buffer.dataTreePath.schema, table:buffer.dataTreePath.tableorg};
				//console.log("Looking for " + JSON.stringify(dataTreePath) + " " + isAjaxing);
				Processing.show("Get column description of table " +JSON.stringify(dataTreePath) );
				$.getJSON(getMetaTableUrl
						, params
						, function(data)  {
					Processing.hide();
					if( !Processing.jsonError(data)) {
						buffer.hamap = buildAttMap(data);
						if( getJoinedTablesUrl) {
							Processing.show("Waiting on join keys " + getJoinedTablesUrl);
							$.getJSON(getJoinedTablesUrl
									, params
									, function(jdata) {
								Processing.hide();
								if( jdata && !jdata.errormsg )  {
									var dt = jdata.targets;			

									for( var i=0 ; i<dt.length ; i++ ) {
										var t = dt[i];
										var st = t.target_table.split('.');
										var schema, table;
										if( st.length > 1) {
											schema =  st[0]; table = st[1];
										} else {										
											schema =  dataTreePath.schema; table = st[0];
										}
										var tdtp = new DataTreePath({nodekey: dataTreePath.nodekey, schema: schema, table: table});
										buffer.targets.push({target_datatreepath: tdtp, target_column: t.target_column, source_column: t.source_column});
									}
								} else {
									Out.info((jdata == null)? 'Empty data' : jdata.errormsg);
								}
								isAjaxing = false;
								cache[dataTreePath.nodekey] = buffer;
								if( handler != null ) handler();
							});
						} else {
							isAjaxing = false;
							cache[dataTreePath.nodekey] = buffer;
							if( handler != null ) {
								if( handler != null ) handler();
							}
						}
					}
				});
			} else {
				Out.info("No getMetaTableUrl provided" );
			}
			/*
			 * Meta data are in the cache
			 */
		} else if( cache[dataTreePath.nodekey] ) {
			Out.info("get stored node  " + dataTreePath.nodekey + " length = " + cache[dataTreePath.nodekey].hamap.length);
			if( handler!= null ) handler(); else Out.info("No handler");
			return cache[dataTreePath.nodekey].hamap;
		}
	};
	var test =  function(){
		MetadataSource.init({getMetaTable: "gettableatt", getJoinedTables: "gettablejoin", getUserGoodie: null});
		var dataTreePath = new DataTreePath({nodekey:'node', schema: 'schema', table: 'table', tableorg: 'schema.table'});
		MetadataSource.getTableAtt(
				dataTreePath
				, function() {
					console.log("ahMap "        + JSON.stringify(MetadataSource.ahMap(dataTreePath)));
					console.log("joinedTables " + JSON.stringify(MetadataSource.joinedTables(dataTreePath)));	
					//	alert("ahMap "        + JSON.stringify(MetadataSource.ahMap(dataTreePath)));
				});
		MetadataSource.getTableAtt(
				dataTreePath
				, function() {
					console.log("ahMap "        + JSON.stringify(MetadataSource.ahMap(dataTreePath)));
					console.log("joinedTables " + JSON.stringify(MetadataSource.joinedTables(dataTreePath)));	
					//	alert("ahMap "        + JSON.stringify(MetadataSource.ahMap(dataTreePath)));
				});
	};

	var getJoinedTables =  function(){};
	var getUserGoodie =  function(){};
	var setJobColumns =  function(){};

	/*
	 * exports
	 */
	var pblc = {};
	pblc.ahMap        = function(dataTreePath){return(!cache)?null: cache[dataTreePath.nodekey].hamap;};
	pblc.relations        = function(dataTreePath){return(!cache)?null: cache[dataTreePath.nodekey].relation;};
	pblc.joinedTables = function(dataTreePath){return(!cache)?null: cache[dataTreePath.nodekey].targets;};
	pblc.init = init;
	pblc.getTableAtt = getTableAtt;
	pblc.getRelations = getRelations;
	pblc.getClasses = getClasses;
	pblc.getJoinedTables = getJoinedTables;
	pblc.getUserGoodie = getUserGoodie;
	pblc.setJobColumns = setJobColumns;
	pblc.test = test;
	return pblc;	
}();

/**		
 * Opens thje region editor
 * The data passed to the  handler look like that:
		    {isReady: true,             // true if the polygone is closed
		    userAction: userAction,     // handler called after the user have clicked on Accept
		    region : {
		        format: "array2dim",    // The only one suported yet [[x, y]....]
		        points: this.poligone.skyPositions  // array with structire matching the format
		        size: {x: , y:} // regiosn size in deg
		        }
 * @param handler handler to be called when the polygone is complete ao accepted
 * @param points object denoted the initial value of the polygone {type: ... value:} type is format of the 
 * value (saadaql or array) and value is the data string wich will be parsed
 */
Modalinfo.regionEditor = null;


/**
 * Used to debug the isseu with multiple aladin instance running at the same time
 */
Modalinfo.regionForAMDebugging = function (handler, points) {		
	$(document.documentElement).append('<div id="aladin-lite-div" style="width: 400px; height: 400px"></div>');
	this.regionEditor = new RegionEditor_mVc  ("aladin-lite-div", handler, points); 
	this.regionEditor.init();
	$('#aladin-lite-div').dialog({ modal: true
		, width: 'auto'
			, dialogClass: 'd-maxsize'
				, title: "title" 		  
					, zIndex: zIndexModalinfo
	});	
	$(".aladin-box").css("z-index", (zIndexModalinfo + 2));

	this.regionEditor.setInitialValue(points);
}




/**
 * Modalinfo extension extension handling a file upload
 * All parameters are required (even null)
 * - title: dialog box title
 * - url: Upload url: This servlet is supposed to return the JSON message which will be displayed
 * - description: HTML text describing the service
 * - handler is called in case of success with the object {path, retour} as parameter
 *   where retour is the object returned by the server: {name, size} usually and path 
 *   results from uploaded filepath parsing {path, filename}
 * - beforeHandler is called before the download starts
 * - extraParamers: [{name, value}]: List of hidden parameters to be set with in addition to the file upload
 */
/*Modalinfo.uploadForm = function (title, url, description, handler, beforeHandler, extraParamers) {
	var htmlForm = '<form id="uploadPanel" target="_sblank" action="' + url + '" method="post"'
	+  'enctype="multipart/form-data">';
	if( extraParamers != null) {
		for( var i=0 ; i<extraParamers.length ; i++ ) 
			htmlForm += "<input type='hidden'  name='" + extraParamers[i].name + "'  value='" + extraParamers[i].value + "'>";
	}
	htmlForm += ' <input class=stdinput  id="uploadPanel_filename" type="file" name="file" /><br>'
		+ ' <p class=help></p><br>'
		+ '    <input  type="submit" value="Upload" />'
		+ '</form>';	
	Modalinfo.dataPanel(title, htmlForm, null);

	if( description != null ) {
		$('#uploadPanel p').html(description);
	}
	$('form#uploadPanel').ajaxForm({
		beforeSubmit: function() {
			if(beforeHandler != null ) {
				beforeHandler();
			}
		},
		success: function(e) {
			if( Processing.jsonError(e, "Upload Position List Failure") ) {
				Modalinfo.close();
				return;
			} else {
				Out.debug("Upload success: " + JSON.stringify(e));
				if( handler != null) {
					var retour = {retour: e, path : $('#uploadPanel_filename').val().xtractFilename()};
					handler(retour);
				}
			}
		}
	});
};*/

/**
 * Singleton object wrapping the creation of query editor forms
 */
QueryConstraintEditor = function() {
	var nativeConstraintEditor = function (params /*{parentDivId, formName, getMetaUrl, queryView*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new ConstQEditor_mVc(params /*{parentDivId,formName,queryView}*/);
		new ConstQEditor_mvC(view, new ConstQEditor_Mvc());
		view.draw();
		return view;
	};
	var ucdConstraintEditor = function (params /*{parentDivId, formName, queryView, help}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new UcdQEditor_mVc(params /*{parentDivId,formName,queryView, help}*/);
		var mod = new UcdQEditor_Mvc();
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	var ucdPatternEditor = function (params /*{parentDivId, formName, queryView, relationName, help}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new UcdPatternEditor_mVc(params /*{parentDivId, formName, queryView, help}*/);
		var mod = new UcdPatternEditor_Mvc(params.relationName);
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	//FIT
	var FitPatternEditor = function (params /*{parentDivId, formName, queryView, relationName, help}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new  FitPatternEditor_mVc(params /*{parentDivId, formName, queryView, help}*/);
		var mod = new FitPatternEditor_Mvc(params.relationName,params.className);
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	var FitAttachedPatternsEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new FitAttachedPatterns_mVc(params);
		view.draw();
		return view;
	};
	var FitBestModelAttachedPatternEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new FitBestModelAttachedPattern_mVc(params);
		view.draw();
		return view;
	};
	var FitBetterModelAttachedPatternEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new FitBetterModelAttachedPattern_mVc(params);
		view.draw();
		return view;
	};
	var FitOrderModelAttachedPatternEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new FitOrderModelAttachedPattern_mVc(params);
		view.draw();
		return view;
	};
	//
	var matchPatternEditor = function (params /*{{parentDivId,formName,queryView:}*/) {
		var view  = new Pattern_mVc(params);
		view.draw();
		return view;
	};
	var posConstraintEditor = function (params /*{parentDivId, formName, queryView, frames, urls}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new PosQEditor_mVc(params);
		var mod = new PosQEditor_Mvc();
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	var simplePosConstraintEditor = function (params /*{parentDivId, formName, queryView, frames, urls}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new SimplePos_mVc(params);
		view.draw();
		return view;
	};
	var catalogueConstraintEditor = function (params /*{parentDivId, formName, getMetaUrl, queryView, relationName, distanceQualifer, help}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new CatalogueQEditor_mVc(params);
		var mod = new CatalogueQEditor_Mvc(params);
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	var crossidConstraintEditor = function (params /*{parentDivId, formName, getMetaUrl, queryView, relationName, probaQualifier, help}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new CatalogueQEditor_mVc(params);
		var mod = new CrossidQEditor_Mvc(params);
		new ConstQEditor_mvC(view, mod);
		view.draw();
		return view;
	};
	var attachedDataEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new AttachedData_mVc(params);
		view.draw();
		return view;
	};
	var attachedPatternEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new AttachedPattern_mVc(params);
		view.draw();
		return view;
	};
	var attachedPatternsEditor = function(params /*{parentDivId, formName, queryView, title, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new AttachedPatterns_mVc(params);
		view.draw();
		return view;
	};
	var attachedMatchEditor = function(params /*{parentDivId, formName, queryView, title, relation, pattern, products}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new AttachedMatch_mVc(params);
		view.draw();
		return view;
	};
	var vizierKeywordsEditor = function(params /*{parentDivId, formName, queryView, title, getMetaUrl*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view =  new VizierKeywords_mVc(params);
		view.draw();
		return view;
	};

	var tapColumnSelector = function (params /*{parentDivId, formName, queryView, currentNode}*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" +params. parentDivId + " not found");
			return ;
		}
		var view  = new tapColSelector_mVc(params);
		new ConstQEditor_mvC(view, new tapColSelector_Mvc());
		view.draw();
		return view;
	};

	var tapConstraintEditor = function (params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new tapQEditor_mVc(params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/);
		new ConstQEditor_mvC(view, new tapQEditor_Mvc());
		view.draw();
		return view;
	};

	var tapPosSelector = function (params) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new tapPosQEditor_mVc(params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/);
		new ConstQEditor_mvC(view, new tapPosQEditor_Mvc());
		view.draw();
		return view;
	}

	var queryTextEditor= function (params) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new QueryTextEditor_mVc(params);
		var mod = new QueryTextEditor_Mvc();
		new QueryTextEditor_mvC(view, mod);
		view.draw();
		return view;
		;
	};
	var adqlTextEditor= function (params) {
		var parentdiv = $('#' + params.parentDivId);
		if( !parentdiv.length) {
			Modalinfo.error("Div #" + params.parentDivId + " not found");
			return ;
		}
		var view  = new QueryTextEditor_mVc(params);
		var mod = new ADQLTextEditor_Mvc();
		new QueryTextEditor_mvC(view, mod);
		view.draw();
		return view;
		;
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.nativeConstraintEditor = nativeConstraintEditor;
	pblc.ucdConstraintEditor = ucdConstraintEditor;
	pblc.ucdPatternEditor = ucdPatternEditor;
	pblc.FitPatternEditor = FitPatternEditor;
	pblc.FitAttachedPatternsEditor = FitAttachedPatternsEditor;
	pblc.FitBestModelAttachedPatternEditor = FitBestModelAttachedPatternEditor;
	pblc.FitBetterModelAttachedPatternEditor = FitBetterModelAttachedPatternEditor;
	pblc.FitOrderModelAttachedPatternEditor = FitOrderModelAttachedPatternEditor;
	pblc.matchPatternEditor = matchPatternEditor;
	pblc.posConstraintEditor = posConstraintEditor;
	pblc.simplePosConstraintEditor = simplePosConstraintEditor;
	pblc.attachedDataEditor = attachedDataEditor;
	pblc.attachedPatternEditor = attachedPatternEditor;
	pblc.attachedPatternsEditor = attachedPatternsEditor;
	pblc.attachedMatchEditor = attachedMatchEditor;
	pblc.vizierKeywordsEditor = vizierKeywordsEditor;
	pblc.catalogueConstraintEditor = catalogueConstraintEditor;
	pblc.crossidConstraintEditor = crossidConstraintEditor;
	pblc.tapConstraintEditor = tapConstraintEditor;
	pblc.tapColumnSelector = tapColumnSelector;
	pblc.tapPosSelector = tapPosSelector;
	pblc.queryTextEditor = queryTextEditor;
	pblc.adqlTextEditor = adqlTextEditor;
	return pblc;
}();

/**
 * This object open a dialog box handling the datalink returned by 
 * baseurl. Forwardurl is a proxy url which can be setup to wor aroubf XDomain issues.
 *   in this case, the datalink description is searched at forwardurl + encodeURI(baseurl)
 */
DataLinkBrowser = function() {
	var startBrowser = function (baseurl, forwardurl) {
		var view  = new DataLink_mVc({baseurl: baseurl,forwardurl:forwardurl });
		var mod = new DataLink_Mvc();
		new DataLink_mvC(view, mod);
		view.draw();
		return view;
		;
	};
	/*
	 * The dataObject should contain some value of the data row from which the Datalink popup has ben called
	 * This might help to get the parameter ranges and the obs_did
	 * if must have the form {key: value...}
	 * positional parameter must be complaient with Obscore (s_ra/dev/fov)
	 */
	var startCompliantBrowser = function (baseurl, forwardurl, dataObject) {
		var view  = new CompliantDataLink_mVc({baseurl: baseurl,forwardurl:forwardurl, dataobject: dataObject});
		var mod = new DataLink_Mvc();
		new DataLink_mvC(view, mod);
		view.draw();
		return view;
		;
	};
	/*
	 * exports
	 */
	var pblc = {};
	pblc.startBrowser = startBrowser;
	pblc.startCompliantBrowser = startCompliantBrowser;
	return pblc;
}();


CustomDataTable = function () {
	// Used to add custom content
	var custom = 0;
	var custom_array = [];

	/**
	 * Create a dataTable
	 * @param options are the parameters of the dataTable like:
	 * options = {
	 				"aoColumns" : columns,
	 				"aaData" : rows,
	 				"bPaginate" : true,
	 				"bSort" : true,
	 				"bFilter" : true
	 			  };
	 * @param position tells what components to add with the table and their position
	 * 6 positions available: top-left, top-center, top-right, bottom-left, bottom-center, bottom-right
	 * 5 basic components available: filter, length, pagination, information, processing
	 * var position = [
 			{ "name": "pagination",
 			  "pos": "top-left"
 			},
 			{ "name": "length",
 	 			  "pos": "top-center"
 	 		},
 			{ "name": "<p>DataTable</p>",
 			  "pos" : "top-center"
 			},
 			{ "name": 'filter',
 	 			  "pos" : "bottom-center"
 	 		},
 			{ "name": "information",
 	 	 		  "pos" : "bottom-right"
 	 	 	}
 	 	];
	 **/
	var create = function(id, options, position) {
		// Remove filter label and next previous label
		if (options["sPaginationType"] != undefined) {
			if (options["sPaginationType"] === "full_numbers") {
				options = addSimpleParameter(options, "oLanguage", {"sSearch": ""});
			}
		}
		else {
			options = addSimpleParameter(options, "oLanguage", {"sSearch": "", "oPaginate": { "sNext": "", "sPrevious": "" }});
		}

		// Positioning the elements
		if (position != undefined) {
			options = addSimpleParameter(options, "sDom", changePosition(position));
		}				
		var table = $('#' + id).dataTable(options);

		// Adding the custom content
		if (position != undefined) {
			custom_array.forEach(function(element) {
				$("div.custom"+element.pos).html(element.content);
			});
			ModalResult.changeFilter(id);
		}		
		$('#' + id + "_wrapper").css("overflow","inherit");
		return table;
	};

	/**
	 * Add a parameter to the @options of the dataTable
	 * @options: object, options of the dataTable
	 * @parameter: name of the parameter
	 * @value: value of the parameter
	 **/
	var addSimpleParameter = function(options, parameter, value) {
		options[parameter] = value;		
		return options;
	}

	/**
	 * Create the dom according to the components and positions asked
	 * @position: object, indicate the position of the different elements
	 **/
	var changePosition = function(position) {
		var dom = '';	
		var top_left = [];
		var top_center = [];
		var top_right = [];
		var bot_left = [];
		var bot_center = [];
		var bot_right = [];

		position.forEach(function(element) {
			switch (element.pos) {
			case "top-left":
				top_left.push(getDomName(element.name));
				break;
			case "top-center":
				top_center.push(getDomName(element.name));
				break;
			case "top-right":
				top_right.push(getDomName(element.name));
				break;
			case "bottom-left":
				bot_left.push(getDomName(element.name));
				break;
			case "bottom-center":
				bot_center.push(getDomName(element.name));
				break;
			case "bottom-right":
				bot_right.push(getDomName(element.name));
				break;
			}
		});	

		// Search the number of position asked for which row to know the size of the div columns
		var nb_top = 0;
		if (top_left.length > 0) {
			nb_top++;
		}
		if (top_center.length > 0) {
			nb_top++;
		}
		if (top_right.length > 0) {
			nb_top++;
		}
		nb_top = Math.floor(12/nb_top);

		var nb_bot = 0;
		if (bot_left.length > 0) {
			nb_bot++;
		}
		if (bot_center.length > 0) {
			nb_bot++;
		}
		if (bot_right.length > 0) {
			nb_bot++;
		}
		nb_bot = Math.floor(12/nb_bot);

		if (nb_top > 0) {
			dom += '<"row"'
		}

		if (top_left.length > 0) {
			dom += '<"txt-left col-xs-'+nb_top+'"';
			top_left.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}
		if (top_center.length > 0) {
			dom += '<"txt-center col-xs-'+nb_top+'"';
			top_center.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}
		if (top_right.length > 0) {
			dom += '<"txt-right col-xs-'+nb_top+'"';
			top_right.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}

		if (nb_top > 0) {
			dom += ">";
		}

		dom += '<"custom-dt" rt>'

			if (nb_bot > 0) {
				dom += '<"row"';
			}	

		if (bot_left.length > 0) {
			dom += '<"txt-left col-xs-'+nb_bot+'"';
			bot_left.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}
		if (bot_center.length > 0) {
			dom += '<"txt-center col-xs-'+nb_bot+'"';
			bot_center.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}
		if (bot_right.length > 0) {
			dom += '<"txt-right col-xs-'+nb_bot+'"';
			bot_right.forEach(function(element) {
				dom += '<"side-div"'+element+'>';
			});
			dom += ">";
		}

		if (nb_bot > 0) {
			dom += ">";
		}

		return dom;
	}

	/**
	 * Return the real dom name of the basic components and create div for the custom ones
	 * @name: explicit name of a basic component or name of a custom one
	 **/
	var getDomName = function(name) {
		var dom_name;

		switch (name) {
		case "filter":
			dom_name = "f";
			break;
		case "pagination":
			dom_name = "p";
			break;
		case "information":
			dom_name = "i";
			break;
		case "length":
			dom_name = "l";
			break;
		case "processing":
			dom_name = "r";
			break;
		default:
			// If it's not a basic component, create a div with a unique class
			dom_name = '<"custom'+custom+'">';
		// Push the element in an array in order to add it later thanks to its unique class
		custom_array.push({"content": name, "pos": custom});
		custom++;
		break;
		}

		return dom_name;
	}

	var pblc = {};
	pblc.create = create;

	return pblc;
}();
//Receive data and do the plot
Modalinfo.plot=function(url){
	//Ajax call
	$.get(url,function(data){
		//Set a modal and customize it
		var id_modal = Modalinfo.nextId();
		Modalinfo.setModal(id_modal, false,"Detection Plots");
		Modalinfo.addIconTitle(id_modal, '<span class="fi-graph-trend"></span>');
		$("#"+id_modal).dialog( "option", "height", $(window).height());
		$("#"+id_modal).dialog( "option", "width", "80%");	
		$("#"+id_modal).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		Modalinfo.setShadow(id_modal);
		Modalinfo.whenClosed(id_modal);
		//Create PlotSuccess object
		plotsuccess=new PlotSuccess();  
		//Parse the data
		var dataparse=JSON.parse(data.replace(/NaN/g, '"NaN"'));
		//Delete NaN object from the array data 
		for(var i=0;i<dataparse.data.length;i++){
			for(var j=0;j<dataparse.data[i].data.length;j++){
				for(var k=0;k<dataparse.data[i].data[j].data.length;k++){
					for(var n=0;n<dataparse.data[i].data[j].data[k].data.length;n++){
						var index=dataparse.data[i].data[j].data[k].data[n].indexOf("NaN");
						if (index > -1) {
							dataparse.data[i].data[j].data[k].data.splice(n, 1);
							n=n-1
						} 
					}
					if(dataparse.data[i].data[j].data[k].data.length==0){
						dataparse.data[i].data[j].data.splice(k, 1)
						k=k-1
					}
				}   
			}   
		}
		//Sort the date into ascending order using triasc function
		function triasc(d1,d2) {
			return d1[0] - d2[0];}
		for(var i=0;i<2;i++){
			for(var j=0;j<dataparse.data[i].data.length;j++){
				for(var k=0;k<dataparse.data[i].data[j].data.length;k++){
					dataparse.data[i].data[j].data[k].data.sort(triasc);
				}   
			}   
		}
		//Call the function of plot
		plotsuccess.plotAll(id_modal,dataparse);
		//Animate the modal by three onglets 
		$('#ongletsgraphs').tabs();
	}).fail(function() {
		Modalinfo.error("JSON ERROR", "Error");
	});
};

console.log('=============== >  domain.js ');

function HipsExplorer_mVc(params){
	this.aladinInstance = params.aladinInstance;
	this.parentDivId = params.parentDivId;
	this.title       = params.title;
	this.formName    = params.formName;
	this.handler     = params.handler;
	this.maskId      = this.formName + "_mask";
	this.productType = params.productType;
	this.target      = params.target
	this.baseUrl ="http://alasky.unistra.fr/MocServer/query?RA=" 
		+ this.target.ra + "&DEC=" + this.target.dec + "&SR=" + this.target.fov + "&fmt=json&get=record&logic=or";
	this.imageIdPattern = new RegExp(/.*\/C\/.*/);
	this.imageTilePattern = new RegExp(/.*((jpeg)|(png)).*/);

};


/**
 * Methods prototypes
 */
HipsExplorer_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			var that = this;
			var html = '<input type="text" id="' + this.maskId + '" size=10>\n';
			$('#' + this.parentDivId).html(html);

			$('#' + this.maskId).keyup(function(e) {
				if( $(this).val().length >= 2 || e.which == 13) {
					that.searchHisp($(this).val());
				}
			});

		},

		searchHisp: function(mask){
			var that = this;
			console.log(mask);
			if( this.aladinInstance != undefined ) {
				var radec = this.aladinInstance.getRaDec();
				this.baseUrl ="http://alasky.unistra.fr/MocServer/query?RA=" 
					+ radec[0] + "&DEC=" + radec[1] + "&SR=" + this.aladinInstance.getFov()[0] + "&fmt=json&get=record&casesensitive=false";
			}
			var url = this.baseUrl;

			if( mask != "" ){
				url += "&publisher_id,publisher_did,obs_id,obs_title,obs_regime=*"  + mask + "*";
			}
			$.getJSON(url, function(jsondata) {
				Processing.hide();
				if( Processing.jsonError(jsondata,url) ) {
					return;
				} else {
					if( that.productType != undefined ){
						for(var i = jsondata.length - 1; i >= 0; i--) {
							if(jsondata[i].dataproduct_type != that.productType ) {
								jsondata.splice(i, 1);
							}
						}
						/*
						 * Only keep survey supported buy AladinLite
						 */
						if( that.productType == "image" ){
							for(var i = jsondata.length - 1; i >= 0; i--) {
								var keepIt = 0;
								//if( that.imageIdPattern.test(jsondata[i]) ) {
									if(  $.isArray(jsondata[i].hips_tile_format)) {
										for( var j=0 ; j<jsondata[i].hips_tile_format.length ; j++){
											if( that.imageTilePattern.test(jsondata[i].hips_tile_format[j]) ){
												keepIt = 1;
												break;
											}
										}
									} else if(  that.imageTilePattern.test(jsondata[i].hips_tile_format) ){
										keepIt = 1;
									}
								//}
								if( keepIt == 0 ){
									jsondata.splice(i, 1);
								}
							}
						}
					}
					that.handler(jsondata);
				}
			});
		},
		setMaskValue: function(string){
			$('#' + this.maskId).val(string);
		}

}
console.log('=============== >  HipsExplorer_v.js ');


/*
 * Unit array used to setup UCD based queries
 */
var unitMap = new Array();
unitMap['Energy']    = ['erg', 'eV', 'keV', 'MeV', 'GeV', 'TeV', 'J', 'ryd'];
unitMap['Frequency'] = ['Hz', 'KHz', 'MHz', 'GHz', 'THz'];
unitMap['Time']      = ['y', 'd', 'h', 'mn', 'sec', 'msec', 'nsec'];
unitMap['Length']    = ['kpc', 'pc', 'AU', 'km', 'm', 'cm', 'mm', 'um', 'nm', 'Angstroem'];
unitMap['Velocity']  = ['m/s', 'km/s', 'km/h', 'mas/yr'];
unitMap['Angle']     = ['deg', 'arcmin', 'arcsec'];
unitMap['Flux']      = ['erg/s/cm2', 'Jy', 'mJy', 'count/s'];
unitMap['Power']     = ['erg/s', 'W'];

var units =  [
              {id: 'none', text: "none"}

              , {id: 'Power_erg/s', text: "erg/s"}
              , {id: 'Power_W', text: "W"}

              , {id: 'Flux_erg/s/cm2', text: "erg/s/cm2"}
              , {id: 'Flux_Jy', text: "Jy"}
              , {id: 'Flux_mJy', text: "mJy"}
              , {id: 'Flux_mJy', text: "mJy"}

              , {id: 'Angle_deg', text: "deg"}
              , {id: 'Angle_arcmin', text: "arcmin"}
              , {id: 'Angle_arcsec', text: "arcsec"}
              , {id: 'Angle_h:m:s', text: "h:m:s"}

              , {id: 'Velocity_m/s', text: "m/s"}
              , {id: 'Velocity_km/s', text: "km/s"}
              , {id: 'Velocity_km/h', text: "km/h"}
              , {id: 'Velocity_mas/yr', text: "mas/yr"}

              , {id: 'Length_kpc', text: "kpc"}
              , {id: 'Length_pc', text: "pc"}
              , {id: 'Length_AU', text: "AU"}
              , {id: 'Length_km', text: "km"}
              , {id: 'Length_m', text: "m"}
              , {id: 'Length_cm', text: "cm"}
              , {id: 'Length_mm', text: "mm"}
              , {id: 'Length_um', text: "um"}
              , {id: 'Length_nm', text: "nm"}
              , {id: 'Length_Angstroem', text: "Angstroem"}

              , {id: 'Energy_erg', text: "erg"}
              , {id: 'Energy_eV', text: "eV"}
              , {id: 'Energy_keV', text: "keV"}
              , {id: 'Energy_MeV', text: "MeV"}
              , {id: 'Energy_GeV', text: "GeV"}
              , {id: 'Energy_TeV', text: "TeV"}
              , {id: 'Energy_J', text: "J"}
              , {id: 'Energy_ryd', text: "ryd"}

              , {id: 'Frequency_Hz', text: "Hz"}
              , {id: 'Frequency_KHz', text: "KHz"}
              , {id: 'Frequency_MHz', text: "MHz"}
              , {id: 'Frequency_GHz', text: "GHz"}
              , {id: 'Frequency_THz', text: "THz"}

              , {id: 'Time_y', text: "y"}
              , {id: 'Time_d', text: "d"}
              , {id: 'Time_h', text: "h"}
              , {id: 'Time_mn', text: "mn"}
              , {id: 'Time_sec', text: "sec"}
              , {id: 'Time_msec', text: "msec"}
              , {id: 'Time_nsec', text: "nsec"}
              ];

DataTreePath.prototype.getClassname = function(){
	return (this.table.match(/.*\..*/))? "*": this.table;
}
/**
 * @param treepath array of treepath elements
 */
function setGlobalTreePath(treepath) {
	var title;
	var table;
	var collection;
	if( treepath.length == 3 ){
		collection = treepath[0];
		var category = treepath[1];
		var classe = treepath[2];
		table = classe;
		title = collection + '&gt;' + category + '&gt;' + table;
	}
	else if ( treepath.length == 2 ){
		collection = treepath[0];
		category = treepath[1];
		classe = '*'; 
		params = {query: "ah", name:  collection + '.' +category };
		table = collection + '.' +category;
		title = collection + '&gt;' + category;
	}
	globalTreePath = new DataTreePath({nodekey:table, schema: collection, table: table, tableorg: table});
	globalTreePath.category = category;
	globalTreePath.title = title;
	$('#titlepath').html(title);
}

function getTreePathAsKey() {
	return globalTreePath.nodekey;
}

function switchArrow(id) {
	var image = $('#'+id+'').find('img').attr('src');
	if (image == 'images/tdown.png') {
		$('#'+id+'').find('img').attr('src', 'images/tright.png');
	} else if (image == 'images/tright.png') {
		$('#'+id+'').find('img').attr('src', 'images/tdown.png');
	}
}


console.log('=============== >  utils.js ');

DataTree = function () {
	var cache = new Object;

	var init = function() {

		Processing.show("Loading Data Tree");
		$.getJSON("getmeta", {query: "datatree" }, function(jsdata) {
			Processing.hide();
			if( Processing.jsonError(jsdata, "Cannot make data tree") ) {
				return;
			}
			dataTree = $("div#treedisp").jstree();
			/*
			 * Loop on collections
			 */
			var data = jsdata.data;
			for( var n=0 ; n<data.length ; n++){
				var node = data[n];
				var treepath = node.attr.id.split('.');
				var children =node.children;
				
				if( children.length == 0 ) continue;

				$("div#treedisp").jstree("create_node"
						, $("div#treedisp")
						, "last"
						, {"data" : {"icon": "images/Database2.png", "attr":{"id": node.attr.id, "title": ""}, "title" : node.attr.id},
							"state": "close"}
						,false
						,true);  
				$("a#" + node.attr.id).before("<img title='Click to get the description of the collection' class=infoanchor id='" + node.attr.id + "' src='images/metadata.png'></img>");
				$("a#" + node.attr.id + " ins").remove();
				$("img#" + node.attr.id).click(function(){resultPaneView.fireShowMetaCollection($(this).attr("id"));});
				/*
				 * Loop on categories
				 */
				var cparent = $("#" + node.attr.id);
				for( var c=0 ; c<children.length ; c++){
					var child = children[c];
					var ctreepath = child.attr.id.split('.');
					var id = child.attr.id.replace(/\./g, '_DoT_');
					var title;
					var icon;
					if(child.data == "IMAGE" ) {
						icon = "images/nodeImage.png";
						title =child.data;
					} else if(child.data == "MISC" ) {
						icon = "images/nodeMisc.png";
						title =child.data;
					} else if(child.data == "SPECTRUM" ) {
						icon = "images/nodeSpectrum.png";
						title =child.data;
					} else if(child.data == "FLATFILE" ) {
						icon = "images/nodeFlat.png";
						title =child.data;
					} else if(child.data == "TABLE" ) {
						icon = "images/SQLTable2.png";
						title = "TABLE Headers";
					} else if(child.data == "ENTRY" ) {
						icon = "images/SQLTable2.png";
						title ="TABLE Entries";
					}
					$("div#treedisp").jstree("create_node"
							, cparent
							, "last"
							, {"data" : {"icon":icon, "attr":{"id": id, "title": "Click to display the content"}, "title" : title},
								"state": "close",
								"attr" :{"id": id}}
							, null
							, false);   
					$("a#" + id).click(function(){							
						var tp  = $(this).attr("id").split('_DoT_');
						//resultPaneView.fireSetTreePath(tp);	
						resultPaneView.fireTreeNodeEvent(tp);
						//setTitlePath(tp);
					});
					/*
					 * Loop on classes
					 */
					var classes =child.children;
					var parent = $("#" + id);

					for( var d=0 ; d<classes.length ; d++){
						var classe = classes[d];
						var ctreepath = classe.attr.id.split('.');
						var cid = classe.attr.id.replace(/\./g, '_DoT_')
						$("div#treedisp").jstree("create_node"
								, parent
								, false
								, {"data" : {"icon":"images/blank.png", "attr":{"id": cid, "title": "Click to display the content of this data class"}, "title" : classe.data},
									"state": "closed",
									"attr" :{"id":cid}}
								,false
								,true);   
						$("a#" + cid).before("<img 'Click to get the description' class=infoanchor id='" + cid + "' src='images/metadata.png'></img>");
						$("a#" + cid).click(function(){	
							var tp  = $(this).attr("id").split('_DoT_');
							resultPaneView.fireSetTreePath(tp);	
							resultPaneView.fireTreeNodeEvent(tp);
							//setTitlePath(tp);
							});
						$("a#" + cid + " ins").remove();
						$("img#" + cid).click(function(){resultPaneView.fireShowMetaNode($(this).attr("id").split('_DoT_'));	});
					}

				}

			}
			layoutPane.sizePane("west", $("#treedisp").width()) ;
			layoutPane.sizePane("south", '10%') ;

			return

			dataTree = $("div#treedisp").jstree({
				"json_data"   : data , 
				"plugins"     : [ "themes", "json_data", "dnd", "crrm", "ui"],
				"dnd"         : {"drop_target" : "#resultpane,#saadaqltab,#saptab,#taptab,#showquerymeta",

					"drop_finish" : function (data) {
						var parent = data.r;
						var treepath = data.o.attr("id").split('.');
						if( treepath.length < 2 ) {
							Modalinfo.info("Query can only be applied on one data category or one data class");
						}
						else {
							while(parent.length != 0  ) {
								resultPaneView.fireSetTreePath(treepath);	
								if(parent.attr('id') == "resultpane" ) {
									setTitlePath(treepath);
									resultPaneView.fireTreeNodeEvent(treepath);	
									return;
								}
								else if(parent.attr('id') == "showquerymeta" ) {
									setTitlePath(treepath);
									resultPaneView.fireShowMetaNode(treepath);	
									return;
								}

//								else if(parent.attr('id') == "displayfilter" ) {
//								setTitlePath(treepath);
//								resultPaneView.fireTreeNodeEvent(treepath);	
//								filterManagerView.fireShowFilterManager(treepath);	
//								return;
//								}

								else if( parent.attr('id') == "saadaqltab" || parent.attr('id') == "saptab" || parent.attr('id') == "taptab") {
									saadaqlView.fireTreeNodeEvent(treepath);	
									sapView.fireTreeNodeEvent(treepath);	
									return;
								}
								parent = parent.parent();
							}
						}
					}
				},
				// Node sorting by DnD blocked
				"crrm" : {"move" : {"check_move" : function (m) {return false; }}
				}
				
			}); // end of jstree
//			dataTree.bind("select_node.jstree", function (e, data) {
//			Modalinfo.info(data);
//			});
			dataTree.bind("dblclick.jstree", function (e, data) {
				var node = $(e.target).closest("li");
				var id = node[0].id; //id of the selected node					
				var treepath = id.split('.');
				if( treepath.length < 2 ) {
					Modalinfo.info("Query can only be applied on one data category or one data class");
				}
				else {
					Processing.show("Open node " + getTreePathAsKey());
					resultPaneView.fireSetTreePath(treepath);	
					setTitlePath(treepath);
					resultPaneView.fireTreeNodeEvent(treepath);	
					Processing.hide();
				}
			});
		}); // end of ajax
	}
	
	this.init2 = function() {

		Processing.show("Loading Data Tree");
		$.getJSON("getmeta", {query: "datatree" }, function(data) {
			Processing.hide();
			if( Processing.jsonError(data, "Cannot make data tree") ) {
				return;
			}
			console.log(JSON.stringify(data));
			dataTree = $("div#treedisp").jstree({
				"json_data"   : data , 
				"plugins"     : [ "themes", "json_data", "dnd", "crrm", "ui"],
				"dnd"         : {"drop_target" : "#resultpane,#saadaqltab,#saptab,#taptab,#showquerymeta",

					"drop_finish" : function (data) {
						var parent = data.r;
						var treepath = data.o.attr("id").split('.');
						if( treepath.length < 2 ) {
							Modalinfo.info("Query can only be applied on one data category or one data class");
						}
						else {
							while(parent.length != 0  ) {
								resultPaneView.fireSetTreePath(treepath);	
								if(parent.attr('id') == "resultpane" ) {
									setTitlePath(treepath);
									resultPaneView.fireTreeNodeEvent(treepath);	
									return;
								}
								else if(parent.attr('id') == "showquerymeta" ) {
									setTitlePath(treepath);
									resultPaneView.fireShowMetaNode(treepath);	
									return;
								}

//								else if(parent.attr('id') == "displayfilter" ) {
//								setTitlePath(treepath);
//								resultPaneView.fireTreeNodeEvent(treepath);	
//								filterManagerView.fireShowFilterManager(treepath);	
//								return;
//								}

								else if( parent.attr('id') == "saadaqltab" || parent.attr('id') == "saptab" || parent.attr('id') == "taptab") {
									saadaqlView.fireTreeNodeEvent(treepath);	
									sapView.fireTreeNodeEvent(treepath);	
									return;
								}
								parent = parent.parent();
							}
						}
					}
				},
				// Node sorting by DnD blocked
				"crrm" : {"move" : {"check_move" : function (m) {return false; }}
				}
			}); // end of jstree
//			dataTree.bind("select_node.jstree", function (e, data) {
//			Modalinfo.info(data);
//			});
			dataTree.bind("dblclick.jstree", function (e, data) {
				var node = $(e.target).closest("li");
				var id = node[0].id; //id of the selected node					
				var treepath = id.split('.');
				if( treepath.length < 2 ) {
					Modalinfo.info("Query can only be applied on one data category or one data class");
				}
				else {
					Processing.show("Open node " + getTreePathAsKey());
					resultPaneView.fireSetTreePath(treepath);	
					setTitlePath(treepath);
					resultPaneView.fireTreeNodeEvent(treepath);	
					Processing.hide();
				}
			});
		}); // end of ajax
	}

	/**
	 * 
	 */
	var pblc = {};
	pblc.init2 = init2;
	pblc.init = init;
	return pblc;
}();

console.log('=============== >  dataTree.js ');

jQuery.extend({

	ResultPaneModel: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		var dataJSONObject;
		/**
		 * List of OIDS displyed by the detail modal box
		 */
		var histo = new Array();
		var histo_ptr = 0;
		/**
		 * Query displayed data are coming from
		 */
		var current_query = "";
		var treePath = new Array();

		var zipJob;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		}
		/*
		 * Event processing
		 */
//		this.setTreePath = function(treepath){
//			treePath = treepath;
//		}
//		this.processSaadaQLTreeNodeEvent = function(treepath){
//			var query;
//			if( treepath.length == 3 ){
//				query = "Select " + treepath[1] + ' From ' + treepath[2] + ' In ' + treepath[0];
//			}
//			else if ( treepath.length == 2 ){
//				collection = treepath[0];
//				query = "Select " + treepath[1] + ' From * In ' + treepath[0];
//			}
//			else {
//				Modalinfo.info( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: processTreeNodeEvent.js)");
//				return;
//			}
//			if( $("#qlimit").val().match(/^[0-9]+$/) ) {
//				query += '\nLimit ' + $("#qlimit").val();
//			}
//
//			that.processSaadaQLQueryEvent(query);
//		}

		this.processSaadaQLQueryEvent = function(query){
			current_query = query;
			if( !query.startsWith('Select') ) {
				Modalinfo.info( "\"" + query + "\" SaadaQL query does not look very good !");
			}
			else {
				$('#data_processing').attr("visiblity", "visible");
				Processing.show("Run query");
				$.ajax({
					url: "runquery",
					dataType: 'json',
					timeout: 100000,
					data : { query : query },
					type: 'GET',
					error: function(XMLHttpRequest, textStatus, errorThrown){
						Processing.hide();
						$('#data_processing').attr("visiblity", "hidden");
						that.notifyJobFailed(textStatus);
					},
					success: function(json){			
						Processing.hide();
//						Modalinfo.info(treePath)
						dataJSONObject = json;
						$('#data_processing').attr("visiblity", "hidden");
						$('#showquerymeta').unbind('click');
						$('#showquerymeta').click(function(){resultPaneView.fireShowMeta();});
						that.notifyTableInitDone(dataJSONObject, query);
//						setTitlePath(treePath);
					}
				});
			}
		};

		this.processShowRecord= function(oid, panelToOpen){
			var jsdata ="";
			Processing.show("Get Object description");
			$.getJSON("getobject", {oid: oid }, function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "") ) {
					return;
				}

				else {
					jsdata = data;
					histo[histo.length] = oid;
					histo_ptr = histo.length - 1;
					var limit = 'MaxRight';
					if( histo.length == 1  ) limit = 'NoHisto';
					that.notifyDetailLoaded(oid, jsdata, limit, panelToOpen);
				}
			});
		};

		this.processShowMeta= function(){
			var jsdata ="";
			Processing.show("Fetching meta data");
			var tp;
			if( treePath.length == 3 ) {
				tp = treePath[2];
			}
			else {
				tp = treePath[0] + "." + treePath[1];
			}
			$.getJSON("getmeta", {query: "aharray", name:tp }, function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "get metadata") ) {
					return;
				}
				else {
					histo[histo.length] = "meta: " + tp;
					histo_ptr = histo.length - 1;
					var limit = 'MaxRight';
					if( histo.length == 1  ) limit = 'NoHisto';
					that.notifyMetaLoaded(data, limit);
				}
			});
		}

		this.processShowMetaNode= function(treepath){
			var jsdata ="";
			Processing.show("Fetching meta data");
			var tp;
			if( treepath.length == 3 ) {
				tp = treepath[2];
			}
			else {
				tp = treepath[0] + "." + treepath[1];
			}
			$.getJSON("getmeta", {query: "aharray", name:tp }, function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "get attribute handlers") ) {
					return;
				}
				else {
					histo[histo.length] = "meta: " + tp;
					histo_ptr = histo.length - 1;
					var limit = 'MaxRight';
					if( histo.length == 1  ) limit = 'NoHisto';
					that.notifyMetaLoaded(data, limit);
				}
			});
		}

		this.processShowSources= function(oid){
			Processing.show("Get Object detail");
			$.getJSON("getobject", {target: "sources", oid: oid }, function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "get catalogue sources") ) {
					return;
				}
				else {
					jsdata = data;
					saadaqlView.fireTreeNodeEvent(jsdata.treepath.split('.'), false, jsdata.query);	
					sapView.fireTreeNodeEvent(jsdata.treepath.split('.'));	
					setTitlePath(jsdata.treepath.split('.'));
					that.notifyTableInitDone(jsdata);	
					/*
					 * should be done in the async callback intiated by saadaqlView.fireTreeNodeEvent
					 */
					setTimeout("saadaqlView.fireOIDTableEvent(\"" + oid + "\"); ", 2000);
					current_query = jsdata.query;
				}
			});
		};

		this.processShowSimbad= function(coord){
			window.open("simbad?coord=" + escape(coord), "Simbad");
		};

		this.processPreviousRecord= function(){
			var jsdata ="";
			if( histo_ptr <= 0 ) {
				Modalinfo.info("end of the historic reached");
				return;
			}
			histo_ptr --;

			var oid = histo[histo_ptr];
			Processing.show("Fetching meta data");
			if( oid.match(/^meta:\s*/)) {
				$.getJSON("getmeta", {query: "aharray", name:oid.split(' ')[1] }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "get attribute handlers") ) {
						return;
					}
					else {
						var limit = '';
						if( histo_ptr == 0 ) limit = 'MaxLeft';
						that.notifyMetaLoaded(data, limit);
					}
				});
			}
			else {
				$.getJSON("getobject", {oid: oid }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "get object") ) {
						return;
					}
					else {
						jsdata = data;
						var limit = '';
						if( histo_ptr == 0 ) limit = 'MaxLeft';
						that.notifyDetailLoaded(oid, jsdata, limit);
					}
				});
			}
		}
		this.processNextRecord= function(){
			var jsdata ="";
			if( histo_ptr >= (histo.length - 1) ) {
				Modalinfo.info("end of the historic reached");
				return;
			}
			histo_ptr ++;
			var oid = histo[histo_ptr];
			Processing.show("Fetching meta data");
			if( oid.match(/^meta:\s*/)) {
				$.getJSON("getmeta", {query: "aharray", name:oid.split(' ')[1] }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "get attribute handlers") ) {
						return;
					}
					else {
						var limit = '';
						if( histo_ptr == (histo.length - 1) ) limit = 'MaxRight';
						that.notifyMetaLoaded(data, limit);
					}
				});
			}
			else {
				$.getJSON("getobject", {oid: oid }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "get object") ) {
						return;
					}
					else {
						jsdata = data;
						var limit = '';
						if( histo_ptr == (histo.length - 1) ) limit = 'MaxRight';
						that.notifyDetailLoaded(oid, jsdata, limit);
					}
				});
			}
		}

		this.processShowCounterparts= function(poid, relationname){

			var jsdata ="";
			var param = {oid: poid, relation: relationname};
			Processing.show("Get object detail");
			$.getJSON("getobject", param , function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "get object") ) {
					return;
				}
				else {
					jsdata = data;
					that.notifyCounterpartsLoaded(jsdata);
				}
			});
		};
		
		this.sampVOTable = function() {
			/*
			 * The mode (TAP/SaadaQL is detected by anaylsing the title
			 */
			var titlepath = $('#titlepath').html().split('&gt;');
			if( titlepath.length == 3 && titlepath[1] == 'Job' ) {
			}
			else {
				var url = "getqueryreport?query=" + escape(current_query) + "&model=samp&format=votable";
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};
		this.downloadVOTable = function() {
			/*
			 * The mode (TAP/SaadaQL is detected by anaylsing the title
			 */
			var titlepath = $('#titlepath').html().split('&gt;');
			if( titlepath.length == 3 && titlepath[1] == 'Job' ) {
			}
			else {
				var url = "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=votable";
				Modalinfo.iframePanel(url, 'DL VOTable');
			}
		};
		this.downloadVOTableURL = function() {
			return "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=votable";
		}
		this.downloadFITS = function() {
			var url = "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=fits";
			Modalinfo.iframePanel(url, 'DL FITS');
		};
		this.downloadZip = function() {
			if( that.zipJob != null ) {
				that.zipJob.kill();
			}
			var limit = 10000;
			if( $("#qlimit").val().match(/^[0-9]*$/) ) {
				limit = $("#qlimit").val();
			}
			Out.info("zipasync");

			$.ajax({
				type: 'POST',
				url: "datapack/zipper",
				data: {PHASE: 'RUN', FORMAT: 'json',RELATIONS: "any-relations", LIMIT: limit,QUERY: ($('#saadaqltext').val()) },
				success: function(xmljob, status) {
					$(".zip").css("background-image", "url(http://jacds.u-strasbg.fr/saadasvn/images/connecting.gif)");					
					setTimeout("resultPaneView.fireCheckZipCompleted(\"cocu\");", 1000);
					that.zipJob = new $.ZipjobModel(xmljob);
				},
				dataType: "xml",
				error: function(xmljob, textStatus, errorThrown) {
					Modalinfo.info("Error: " + textStatus);
				}
			});
		}
		this.checkZipCompleted = function(jobid) {
			if( that.zipJob == null ) {
				Modalinfo.info("Error: Job seems to get lost ??");			
				$(".zip").css("background-image", "url(images/zip_32.png)");								
			}
			else {
				$.get("datapack/zipper/" + that.zipJob.jobId
						, "FORMAT=xml"
						, function(data) {
					that.zipJob.init(data);
					var phase = that.zipJob.phase;
					if( phase == 'COMPLETED') {
						Out.info(that.zipJob.results);
						var url = that.zipJob.results[0];
						if( confirm("Do you want to downlad the ZIP ball") ) {
							location = url;
						}
						else {
							that.cancelZip();
						}
						$(".zip").css("background-image", "url(images/zip_32.png)");					
					}
					else if( phase == 'EXECUTING') {
						Out.info("executing");
						setTimeout("resultPaneView.fireCheckZipCompleted(\"cocu\");", 1000);
					}
					else {
						Modalinfo.info("Zip job is in an unexpected status: " + phase + ": canceled");
						that.zipJob.kill();
						$(".zip").css("background-image", "url(images/zip_32.png)");					
					}
				}) ;
			}
		}
		this.cancelZip = function() {							
			if( that.zipJob != null ) {
				that.zipJob.kill();
			}
			$(".zip").css("background-image", "url(images/zip_32.png)");					
		}
		this.sampBroadcast = function() {
			/*
			 * The mode (TAP/SaadaQL is detected by analysing the title
			 */
			var titlepath = $('#titlepath').html().split('&gt;');
			if( titlepath.length == 3 && titlepath[1] == 'Job' ) {
			}
			else {

				if (current_query.match(/\s*Select\s+IMAGE\s*.*/)) {
					sampView.fireSendSIAQuery(current_query);
				}
				else if (current_query.match(/\s*Select\s+SPECTRUM\s*.*/)) {
					sampView.fireSendSSAQuery(current_query);
				}
				else  if (current_query.match(/\s*Select\s+ENTRY\s*.*/)) {
					sampView.fireSendCSQuery(current_query);
				}
				else {
					Modalinfo.info("Samp messages are not  implemented for this data category.")
				}
			}

//			var url = "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=fits";
//			window.open(url, 'DL VOTable');
		}

		/*
		 * Listener notifications
		 */
		this.notifyJobInProgress = function(){
			$.each(listeners, function(i){
				listeners[i].jobInProgress();
			});
		}
		this.notifyJobDone = function(dataJSONObject){
			$.each(listeners, function(i){
				listeners[i].jobIsDone(dataJSONObject);
			});
		}
		this.notifyTableInitDone = function(dataJSONObject, query){
			$.each(listeners, function(i){
				listeners[i].tableIsInit(dataJSONObject, query);
			});
		}
		this.notifyJobFailed = function(textStatus){
			$.each(listeners, function(i){
				listeners[i].jobFailed(textStatus);
			});
		}
		this.notifyDetailLoaded= function(oid, jsdata, limit, panelToOpen){
			$.each(listeners, function(i){
				listeners[i].detailIsLoaded(oid, jsdata, limit, panelToOpen);
			});
		}
		this.notifyMetaLoaded= function(jsdata, limit){
			$.each(listeners, function(i){
				listeners[i].metaIsLoaded(jsdata, limit);
			});
		}
		this.notifyCounterpartsLoaded= function(jsdata){
			$.each(listeners, function(i){
				listeners[i].counterpartsAreLoaded(jsdata);
			});
		}
	}
});

console.log('=============== >  resultPaneModel.js ');

jQuery
.extend({

	ResultPaneView : function() {
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		var fixedHeader = null;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list) {
			listeners.push(list);
		};
		this.fireTreeNodeEvent = function(treepath) {
			setGlobalTreePath(treepath);
			var mode = $("input[@name=qlang]:checked").val();
			var runSaadaQL = false;
			if (mode == 'saadaql') {
				runSaadaQL = true;
			} else if (mode == 'sap') {
				$("input[value=saadaql]").attr('checked', true);
				$('#taptab').hide();
				$('#saptab').hide();
				$('#saadaqltab').show('slow');
				runSaadaQL = true;
			} else if (mode == 'tap') {
				runTAP = true;
			}
			saadaqlView.fireTreeNodeEvent(runSaadaQL, true);
			sapView.fireTreeNodeEvent();
		};

		this.fireSubmitQueryEvent = function() {
			$("#resultpane").html();
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				that.fireSaadaQLQueryEvent(queryView.getQuery());
			} else if (mode == 'sap') {
				sapView.fireSubmitQueryEvent();
			} else {
				Modalinfo.info('Unknown query mode:' + mode);
			}
		};
		this.fireSetTreePath = function() {
			$.each(listeners, function(i) {
				listeners[i].controlSetTreePath();
			});
		};
		this.fireHisto = function(direction) {
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				saadaqlView.fireHisto(direction);
			}
		};
		this.fireStoreHisto = function(query) {
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				saadaqlView.fireStoreHisto(query);
			}
		};

		this.fireSaadaQLQueryEvent = function(query) {
			$("#resultpane").html();
			$.each(listeners, function(i) {
				listeners[i].controlSaadaQLQueryEvent(query);
			});
		};
		this.fireGetProductInfo = function(url) {
			Processing.show("Waiting on product info");

			$.getJSON("getproductinfo", {url: url}, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "Cannot get product info") ) {
					return;
				}
				else {
					retour = "<span><b> Doanload URL</b>: " + url + "</span><ul>";
					$.each(jsdata, function(k, v) {
						retour += "<li><b>" + k + "</b>: " + v  + "</li>";
					});
					retour += "</ul>";
					Modalinfo.info(retour, "Product Info");
				}
			});
		};		
		this.fireGetRelationInfo = function(relation) {
			Processing.show("Waiting on product info");

			$.getJSON("getmeta", {query: "relation", name: relation}, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "Cannot get relation info") ) {
					return;
				}
				else {
					Modalinfo.infoObject(jsdata, "Relation Info");
				}
			});
		};	
		this.fireSampVOTable = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlSampVOTable();
			});
		};	
		this.fireDownloadVOTable = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadVOTable();
			});
		};		
		
		this.getDownloadVOTableURL = function() {
			var retour;
			$.each(listeners, function(i) {
				retour = listeners[i].controlDownloadVOTableURL();
				return;
			});
			return retour;
		}

		this.fireDownloadFITS = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadFITS();
			});
		};
		this.fireDownloadZip = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}			
			else if( $("input[@name=qlang]:checked").val() == 'saadaql') {
				if( $(".zip").css("background-image").match(".*connecting.*") ) {
					if( confirm("Do you want to cancel the Zipper job?") ) {
						$.each(listeners, function(i) {
							listeners[i].controlCancelZip();
						});
						return;
					}
				}					
				else {
					$.each(listeners, function(i) {
						listeners[i].controlDownloadZip();
					});
				}
			}
			else {
				Modalinfo.info("Data zipper facility only available in SaadaQL mode");

			}
		};
		this.fireCheckZipCompleted= function(jobid) {
			$.each(listeners, function(i) {
				listeners[i].controlCheckZipCompleted(jobid);
			});
		};
		this.fireSampBroadcast = function(query) {
			$.each(listeners, function(i) {
				listeners[i].controlSampBroadcast();
			});
		};
		this.fireShowRecord = function(oid, panelToOpen) {
			$.each(listeners, function(i) {
				listeners[i].controlShowRecord(oid, panelToOpen);
			});
		};
		this.fireShowMeta = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowMeta();
			});
		};
		this.fireShowMetaCollection = function(treepath) {
			$.getJSON("getmeta", {query: "collection", name:treepath }, function(data) {
				Processing.hide();
				if( Processing.jsonError(data, "get collection description") ) {
					return;
				} else {
					Modalinfo.infoObject(data, "Collection Description")
				}
				});
		};
		this.fireShowMetaNode = function(treepath) {
			$.each(listeners, function(i) {
				listeners[i].controlShowMetaNode(treepath);
			});
		};
		this.fireShowSources = function(oid) {
			$('#saadaqllang').attr('checked', 'checked');
			$('#taptab').hide();
			$('#saptab').hide();
			$('#saadaqltab').show('slow');
			$("#qhistocount").css("visibility", "visible");
			saadaqlView.fireDisplayHisto();

			$.each(listeners, function(i) {
				listeners[i].controlShowSources(oid);
			});
		};
		this.fireShowSimbad = function(coord) {
			$.each(listeners, function(i) {
				listeners[i].controlShowSimbad(coord);
			});
		};
		this.fireShowPreviousRecord = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowPreviousRecord();
			});
		};
		this.fireShowNextRecord = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowNextRecord();
			});
		};

		this.fireShowCounterparts = function(oid, relation) {
			var div = $('#' + relation).next('.detaildata');
			if (div.html().length > 0) {
				div.slideToggle(500);
			} else {
				$.each(listeners,
						function(i) {
					listeners[i].controlShowCounterparts(oid,
							relation);
				});
			}
			// $('#detaildiv').animate({scrollTop:
			// $('#detaildiv').height()}, 800);
		};
		this.fireShowVignette = function(oid, title) {
//			Modalinfo.dataPanel('Preview of ' + title,
//			"<img class=vignette src='getvignette?oid=" + oid
//			+ "'>");
			Modalinfo.dataPanel('Vignette of ' + title
					, "<img class=vignette src='getvignette?oid=" + oid + "'>"
					, 'getvignette?oid=' + oid);
		};
		this.fireShowPreview = function(preview_url, title) {
			Modalinfo.openIframePanel(preview_url);
		};

		this.fireExpendForm= function() {
			var height = $(window).height() ;
			var label = $('#formexpender').attr("value");
			if( label == "Refine Query" ) {
				$('#formexpender').attr("value", "Hide Query");				
				$('#formexpender').attr("title", "Expend query form");
				height='90%';
			}
			else {
				$('#formexpender').attr("value", "Refine Query");				
				$('#formexpender').attr("title", "Hide query form");
				height='10%';
			}
			layoutPane.sizePane("south", height);
			//	$("div#accesspane").trigger("resize",[ height]);		
		};

		this.fireOpenDescription = function() {
			Modalinfo.iframePanel("help/description.html");
		};
		this.showProgressStatus = function() {
			Modalinfo.info("Job in progress");
		};
		this.showFailure = function(textStatus) {
			Modalinfo.info("view: " + textStatus);
		};
		this.showDetail = function(oid, jsdata, limit, panelToOpen) {
			var numPanelToOpen = 0;
			if( Processing.jsonError(jsdata, "") ) {
				return;
			}

			var content = {
					header: {
						histo: {
							prev: "resultPaneView.fireShowPreviousRecord()",
							next: "resultPaneView.fireShowNextRecord()"
						},
						title: {
							label: jsdata.title 
						},
						icon: {
							classIcon: "printer",
							handler: "print('simplemodal-container')"
						}
					},
					chapters: []
			}

			content.chapters.push({
				id: "ClassLevel",
				label: "Data Read in the Input file - (class level data)",
				data:  {
					"aoColumns" : jsdata.classlevel.aoColumns,
					"aaData" : jsdata.classlevel.aaData
				},
				params: {
					oid: oid
				},
				searchable: true,
			});

			content.chapters.push({
				id: "CollLevel",
				label: "Data Set by Saada - (collection level data)",
				data:  {
					"aoColumns" : jsdata.collectionlevel.aoColumns,
					"aaData" : jsdata.collectionlevel.aaData
				},
				params: {
					oid: oid
				},
				searchable: true,
			});
			var chapterToOpen;
			for (var i = 0; i < jsdata.relations.length; i++) {
				var relation= jsdata.relations[i];
				var chapter = {
						id: "Relation" + relation,
						label: "Linked Data (relation "+ relation + ") <a id=" + relation + " title='Get relation info' class='dl_info' onclick='event.stopPropagation(); resultPaneView.fireGetRelationInfo(&quot;" + relation + "&quot;);'></A>",
						//url:  "getobject?relation=" + relation,
						url:  "getobject",
						params: {
							oid: oid,
							relation: relation
						},
						searchable: true,
				};
				content.chapters.push(chapter);

				if( relation == panelToOpen) {
					chapterToOpen = chapter;
				} 
			}
			ModalResult.resultPanel(content, null, "white", true);
			if(panelToOpen == "ClassLevel"){
				chapterToOpen = content.chapters[0];
			} else if(panelToOpen == "CollLevel"){
				chapterToOpen = content.chapters[1];
			}
			if( chapterToOpen != null ){
				ModalResult.openChapterPanel(chapterToOpen); 
			}

			return;

			Modalinfo.dataPanel("Source Detail" , table, null, "white");
			/*
			 * Click on relation relation title bars: 
			 * on the bar: open/close the data panel
			 * on tne Info button: display relation info
			 */			
			for (var i = 0; i < jsdata.relations.length; i++) {
				var relation= jsdata.relations[i];
				$('#' + relation).click(function(e) {
					var relation = $(e.target).attr('id');
					if( $(e.target).is('a') ) {
						resultPaneView.fireGetRelationInfo(relation);
					}
					else {
						resultPaneView.fireShowCounterparts(oid, relation ); 
						switchArrow(relation);
					}
				});
			}

			$('#detailtable').dataTable({
				"aoColumns" : jsdata.classlevel.aoColumns,
				"aaData" : jsdata.classlevel.aaData,
				"sDom" : '<f"top">rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});

			$('#detailmappedtable').dataTable({
				"aoColumns" : jsdata.collectionlevel.aoColumns,
				"aaData" : jsdata.collectionlevel.aaData,
				"sDom" : '<"top"f>rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});			
			if( numPanelToOpen > 1 ) {
				jQuery(".detaildata").each(function(i) {
					$(this).hide();
				});
				for (var i = 0; i < jsdata.relations.length; i++) {
					var relation= jsdata.relations[i];
					if( relation == panelToOpen) {
						numPanelToOpen = i+2;
						resultPaneView.fireShowCounterparts(oid, relation);
						switchArrow(relation);
					}
				}
			}
			else {
				jQuery(".detaildata").each(function(i) {
					if (i != numPanelToOpen ) {
						$(this).hide();
					}
				});
			}
		};

		this.showCounterparts = function(jsdata) {
			var id = "reltable" + jsdata.relation;
			var div = $('#' + jsdata.relation).next('.detaildata');
			div.html("<table id="
					+ id
					+ "  width=600px cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  class=\"display\"></table>");
			if( jsdata.aaData.length > 10 ) {
				$('#' + id).dataTable({
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					//	"sDom" : 'rt',
					"sDom": '<"top"i>rt<"bottom"flp><"clear">',
					//	"bPaginate" : true,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : true
				});
			} else {
				$('#' + id).dataTable({
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					"bPaginate" : false,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : true
				});

			}

			$('#' + jsdata.relation).next('.detaildata').slideToggle(
					500);

		};

		this.showMeta = function(jsdata, limit) {
			if( Processing.jsonError(jsdata, "FATAL ERROR: Cannot show object detail: ") ) {
				return;
			}

			var table = '';
			var histo = '';

			if (limit != 'NoHisto') {
				if (limit != 'MaxLeft') {
					histo += '<a id="qhistoleft" href="javascript:void(0);" onclick="resultPaneView.fireShowPreviousRecord();" class=histoleft></a>';
				} else {
					histo += '<a id="qhistoleft" class="histoleft shaded" onclick="return false;"></a>';
				}
				if (limit != 'MaxRight') {
					histo += '<a id="qhistoright"  href="javascript:void(0);" onclick="resultPaneView.fireShowNextRecord();" class=historight></a>';
				} else {
					histo += '<a id="qhistoright" class="historight shaded" onclick="return false;"></a>';
				}
			} else {
				histo += '<a id="qhistoleft" class="histoleft shaded" onclick="return false;"></a>';
				histo += '<a id="qhistoleft" class="historight shaded" onclick="return false;"></a>';
			}
			histo += "<div style='display: inline; float: right'>" + Printer.getPrintButton("simplemodal-container") + "</div>";

			var title;
			if (jsdata.classLevel != null) {
				title = "Meta data of class <i>"
					+ jsdata.classLevel.name
					+ "</i> of collection <i>"
					+ jsdata.collectionLevel.name + "."
					+ jsdata.collectionLevel.category + "</i>";
			} else {
				title = "Meta data of collection <i>"
					+ jsdata.collectionLevel.name + "."
					+ jsdata.collectionLevel.category + "</i>";
			}

			var content = {
					header: {
						histo: {
							prev: "resultPaneView.fireShowPreviousRecord()",
							next: "resultPaneView.fireShowNextRecord()"
						},
						title: {
							label: title 
						},
						icon: {
							classIcon: "printer",
							handler: "print('simplemodal-container')"
						}
					},
					chapters: []
			};

			if (jsdata.classLevel != null) 
				content.chapters.push({
					id: "ClassLevel",
					label: "Class Level Attributes - (Read in input files)",
					data:  {
						"aoColumns" : jsdata.classLevel.attributes.aoColumns,
						"aaData" : jsdata.classLevel.attributes.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});
			if (jsdata.collectionLevel != null) 
				content.chapters.push({
					id: "CollLevel",
					label: "Collection Level Attributes - (Set by Saada)",
					data:  {
						"aoColumns" : jsdata.collectionLevel.attributes.aoColumns,
						"aaData" : jsdata.collectionLevel.attributes.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});

			if (jsdata.collectionLevel.startingRelations.aaData.length > 0) {
				content.chapters.push({
					id: "StartRel",
					label: "Relationships Starting From this dData Collection",
					data:  {
						"aoColumns" : jsdata.collectionLevel.startingRelations.aoColumns,
						"aaData" : jsdata.collectionLevel.startingRelations.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});
			}

			if (jsdata.collectionLevel.endingRelations.aaData.length > 0) {
				content.chapters.push({
					id: "EndRem",
					label: "Relationships Ending To this Data Collection",
					data:  {
						"aoColumns" : jsdata.collectionLevel.endingRelations.aoColumns,
						"aaData" : jsdata.collectionLevel.endingRelations.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});

			}
			ModalResult.resultPanel(content, null, "white", true);
		};
		this.displayResult = function(dataJSONObject) {
		};
		this.initTable = function(dataJSONObject, query) {
			if( Processing.jsonError(dataJSONObject, "") ) {
				return;
			} else {
				/*
				 * Get table columns
				 */
				var ahs = dataJSONObject["attributes"];
				var table = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"datatable\" class=\"display\">";
				var headCells = "";
				for (var i = 0; i < ahs.length; i++) {
					headCells += "<th nowrap style='width: auto;'>" + ahs[i].name + "</th>";
				}

				table +=  "<thead><tr>" + headCells + "</tr></thead>";
				table +=  "<tfoot><tr>" + headCells + "</tr></tfoot>";				
				table +=
					"<tbody>"
					+ "<tr><td colspan="
					+ i
					+ " class=\"dataTables_empty\">Loading data from server</td></tr>"
					+ "</tbody>";

				table += "</table>";
				$("#resultpane").html(table);

				var orderParams = saadaqlView.fireOrderByParameters();
				$('#datatable th').each(function() {
					var att = $(this).text();
					if( !att.startsWith('Rel ')  && att != 'Gallery'  && att != 'DL Link'  && att != 'Plot') {
						var ah;
						if( att == 'Access' ) {
							ah = {nameorg: 'Access', nameattr: 'oidsaada'};
						} else if( att == 'Detail' ) {
							ah = {nameorg: 'Detail', nameattr: 'oidsaada'};
						} else if( att == 'Name' ) {
							ah = {nameorg: 'Name', nameattr: 'namesaada'};
						} else if( att == 'Position' ) {
							ah = {nameorg: 'Position', nameattr: 'pos_ra_csa'};
						} else if( att.startsWith('Error ') ) {
							ah = {nameorg: att, nameattr: 'error_maj_csa'};
						} else {
							ah = {nameorg: att, nameattr: att};
						}
						var s = new Sorter_mVc($(this), $(this).parent(), ah, saadaqlView.fireSortColumnEvent);
						s.draw();
						if( att == 'Access' && orderParams.nameattr == 'oidsaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Detail'  && orderParams.nameattr == 'oidsaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Name' && orderParams.nameattr == 'namesaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Position' && orderParams.nameattr == 'pos_ra_csa') {
							s.activeArrow(orderParams.asc);
						} else if( att.startsWith('Error ') && orderParams.nameattr == 'error_maj_csa') {
							s.activeArrow(orderParams.asc);
						} else if( att == orderParams.nameattr) {
							s.activeArrow(orderParams.asc);
						}

					}
				});
				/*
				 * Connect the table with the DB
				 */
				var options = {
						"aLengthMenu": [5, 10, 25, 50, 100],
						"pageLength": 15,
						"bServerSide" : true,
						"bProcessing" : true,
						"aaSorting" : [],
						"pagingType" : "simple",
						"bSort" : false,
						"bFilter" : false,
						"sAjaxSource" : "nextpage",
							"sServerMethod": "POST"
				};
				var positions = [
				                 { "name": "pagination",
				                	 "pos": "top-left"
				                 },
				                 { "name": "length",
				                	 "pos": "top-center"
				                 },
				                 { "name": "information",
				                	 "pos": "top-right"
				                 },
				                 { "name": "pagination",
				                	 "pos": "bottom-left"
				                 },
				                 { "name": "length",
				                	 "pos": "bottom-center"
				                 },
				                 { "name": "information",
				                	 "pos": "bottom-right"
				                 },
				                 { "name" : '<a id="ColumnSelector" class="dl_column" title="Column selector"></a>',
				                	 "pos": "top-center"
				                 },
				                 { "name" : '<a href=' + resultPaneView.getDownloadVOTableURL() + ' title="Download the current selection in a VOTable" class="dl_download" download></a>',
				                	 "pos": "top-center"
				                 },
				                 { "name" : '<a class="dl_cart" title="Add the current selection to the cart" onclick="cartView.fireAddJobResult($(this), \'' + escape(query) + '\');"></a>',
				                	 "pos": "top-center"
				                 }
				                 ];
				if( globalTreePath.category == "ENTRY" || globalTreePath.category == "IMAGE"|| globalTreePath.category == "SPECTRUM"){
					positions.push({"name": '<a title="Send the entry selection to SAMP client" class="dl_samp" onclick="resultPaneView.fireSampVOTable();"></a>',
						"pos" : "top-center"})
				}
				positions.push({ "name" : Printer.getSmallPrintButton("resultpane") ,
               	 "pos": "top-center"
                });				              	


				var datatable = CustomDataTable.create("datatable", options, positions);			
				$('#datatable_wrapper').css("overflow", "inherit");
				var columnSelector = function(states){
					for( var n=0 ; n<states.length ; n++){
						var column = datatable.api().column( n);
						column.visible( states[n].selected);						
					}
				}
				$('#ColumnSelector').click(function() {
					NodeFilter.create(globalTreePath.nodekey, ahs, columnSelector);
				});
				return;
			}
		};
		this.updateFixedHeader = function() {
			alert(this.fixedHeader);
			if( this.fixedHeader != null ) {
				this.fixedHeader.fnUpdate();
			}
		};
		this.sortColumns = function(nameattr, direction) {
			alert(nameattr + " " +  direction);
			saadaqlView.fireOrderByEvent(nameattr);
			fireSubmitQueryEvent();

		};
		/*
		 * Returns the arrow commanding the historic on the modal box
		 */
		this.histoCommands = function(limit) {
			var histo = '';

			if (limit != 'NoHisto') {
				if (limit != 'MaxLeft') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowPreviousRecord();" class=histoleft></a>';
				}
				if (limit != 'MaxRight') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowNextRecord();" class=historight></a>';
				}
			}
			return histo;
		};

		this.updateQueryHistoCommands = function(length, ptr) {
			var result = '';
			$("#qhistocount").html((ptr + 1) + "/" + length);
			if (length <= 1) {
				result += '<a id="qhistoleft" title="Previous query" class="shaded histoleft  shaded" onclick="return false;"></a>';
				result += '<a id="qhistoright" title="Previous query" class="shaded historight" onclick="return false;"></a>';
			} else {
				if (ptr > 0) {
					result += '<a id="qhistoleft" title="Previous query" class=histoleft onclick="resultPaneView.fireHisto(\'previous\');"></a>';
				} else {
					result += '<a id="qhistoleft" title="Previous query" class="histoleft  shaded" onclick="return false;"></a>';
				}
				if (ptr < (length - 1)) {
					result += '<a id="qhistoright" title="Next query" class=historight onclick="resultPaneView.fireHisto(\'next\');"></a>';
				} else {
					result += '<a id="qhistoright" title="Previous query" class="historight  shaded" onclick="return false;"></a>';
				}
			}
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};

		this.overPosition = function(pos) {
//			simbadToBeOpen = true;
//			setTimeout("if( simbadToBeOpen == true ) openSimbadModalinfo.(\"" + pos + "\");", 1000);
			Modalinfo.simbad(pos );
		};
		this.outPosition = function() {
			simbadToBeOpen = false;
		};
	}
});
console.log('=============== >  resultPaneView.js ');

jQuery.extend({

	ResultPaneControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlSaadaQLQueryEvent : function(query){
					model.processSaadaQLQueryEvent(query);
					saadaqlView.fireStoreHisto(query);
				},
				controlShowRecord: function(oid, panelToOpen){
					model.processShowRecord(oid, panelToOpen);
				},
				controlShowMeta: function(){
					model.processShowMeta();
				},
				controlShowMetaNode: function(treepath){
					model.processShowMetaNode(treepath);
				},
				controlShowSources: function(oid){
					model.processShowSources(oid);
				},
				controlShowSimbad: function(coord){
					model.processShowSimbad(coord);
				},
				controlShowPreviousRecord: function(){
					model.processPreviousRecord();
				},
				controlShowNextRecord: function(oid){
					model.processNextRecord();
				},
				controlShowCounterparts: function(oid, relation){
					model.processShowCounterparts(oid, relation);				
				},
				controlSampVOTable: function(){
					model.sampVOTable();				
				},
				controlDownloadVOTable: function(){
					model.downloadVOTable();				
				},
				controlDownloadVOTableURL: function(){
					return model.downloadVOTableURL();				
				},
				controlDownloadFITS: function(){
					model.downloadFITS();				
				},
				controlDownloadZip: function(){
					model.downloadZip();				
				},
				controlCheckZipCompleted: function(jobid){
					model.checkZipCompleted(jobid);				
				},
				controlCancelZip: function(){
					model.cancelZip();				
				},
				controlSampBroadcast: function(){
					model.sampBroadcast();				
				},
				controlSetTreePath : function(treepath){
					//model.setTreePath(treepath);
				}

		}
		view.addListener(vlist);

		var mlist = {
				jobInProgress : function(){
					view.showProgressStatus();
				},
				jobFailed : function(textStatus){
					view.showFailure(textStatus);
				},
				jobIsDone : function(dataJSONObject){
					view.displayResult(dataJSONObject);
				},
				tableIsInit : function(dataJSONObject, query){
					view.initTable(dataJSONObject, query);
				},
				detailIsLoaded: function(oid, dataJSONObject, limit, panelToOpen){
					view.showDetail(oid, dataJSONObject, limit, panelToOpen);
				},
				metaIsLoaded: function(dataJSONObject, limit){
					view.showMeta(dataJSONObject, limit);
				},
				counterpartsAreLoaded: function(dataJSONObject){
					view.showCounterparts(dataJSONObject);
				}
		}

		model.addListener(mlist);
	}
});

console.log('=============== >  resultPaneControler.js ');

jQuery.extend({

	KWConstraintModel: function(first, ah, model, def_value){
		var that = this;

		var listeners = new Array();
		var operators = new Array();
		var andors = new Array();

		this.addListener = function(list){
			listeners.push(list);
		};

		var attributehandler = ah;
		var saadaqlmodel = model;
		var default_value = def_value;
		var operator= '' ;
		var operand = '';
		var andor = '';

		if( ah.nameattr == 'Cardinality' || ah.nameattr.startsWith('Qualifier ')) {
			operators = ["=", "!=", ">", "<", "][", "]=[", "[]", "[=]"];			
			andors = [];
		}
		else if( ah.type == 'Select' ) {
			operators = [];
			andors = [];
		}
		else if( ah.type == 'ADQLPos' ) {
			operators = ["inCircle", "inBox"];
			andors = ["OR", "AND"];

		}
		else if( ah.type != 'String' ) {
			operators = ["=", "!=", ">", "<", "BETWEEN", 'IS NULL'];
			andors = ['AND', 'OR'];
		}
		else {
			operators = ["=", "!=", "LIKE", "NOT LIKE", 'IS NULL', "BETWEEN"];
			andors = ['AND', 'OR'];
		}

		if( first == true ) {
			andors = [];
		}
		if( attributehandler.type == 'orderby' ) {
			operators = [];
			andors = [];
		}

		this.processEnterEvent = function(ao, op, opd) {
			andor = ao;
			if( attributehandler.type == 'orderby') {
				saadaqlmodel.updateQuery();
				return;
			}
			else if( attributehandler.type == 'String') {
				if( !that.checkAndFormatString(op, opd) ) {
					return;
				}
			}
			else {
				if( !that.checkAndFormatNum(op, opd) ) {
					return;
				}			
			}
			that.notifyTypomsg(0, operator + ' ' + operand);				
			if( andors .lengthlength == 0 ) {
				that.removeAndOr();
			}
			saadaqlmodel.updateQuery();
		};

		this.checkAndFormatNum = function(op, opd) {
			/*
			 * Case of select items in ADQL
			 */
			if( op == null || op.length == 0 ) {
				operator = "";
				operand = "";
				return 1 ;			
			}
			if( op == 'IS NULL' ) {
				operator = 'IS NULL';
				operand = '';
				return 1;								
			}
			else if( /^\s*$/.test(opd)  ) {
				if( ah.nameattr == 'Cardinality' || ah.nameattr.startsWith('Qualifier ')) {
					that.notifyTypomsg(1, 'Numerical operand requested');
					return 0 ;
				}
				else {
					operator = 'IS NOT NULL';
					operand = '';
					return 1;
				}
			}
			else if( op == 'BETWEEN' || op == '][' || op == ']=[' || op == '[]' || op == '[=]') {
				var words = opd.split(' ') ;
				if( words.length != 3 || !/and/i.test(words[1]) ||
						words[0].length == 0 || words[2].length == 0 ||
						isNaN(words[0]) || isNaN(words[2]) ) {
					that.notifyTypomsg(1, 'Operand must have the form "num1 and num2" with operator "' + op + '"');
					return 0 ;
				}
				if( op == 'BETWEEN' ) {
					operator = op;
					operand = words[0] + ' AND ' + words[2];						
				}
				else {
					operator = op;
					operand = '(' + words[0] + ' , ' + words[2] + ')';												
				}
				return 1 ;
			}
			else if( op == 'inCircle' || op == 'inBox')  {
				var area = opd.split(',');
				if( area.length != 3 || isNaN(area[0]) || isNaN(area[1]) || isNaN(area[2]) ) {
					that.notifyTypomsg(1, 'Search area must be like :alpha,delta,size"');					
					return 0 ;
				}
				if( op == 'inCircle') {
					operator = "CIRCLE('ICRS GEOCENTER', '" + area[0]+ "', '" +area[1] + "', " + area[2]+ ")";
					operand = "";
				}
				else {
					operator = "BOX('ICRS GEOCENTER', '" + area[0]+ "', '" +area[1] + "', " + area[2]+  ", " + area[2]  +")";
					operand = "";					
				}
				return 1 ;

			}
			else if( isNaN(opd) ) {
				that.notifyTypomsg(1, 'Single numeric operand required with operator "' + op + '"');				
				return 0 ;
			}
			else {
				operator = op;
				operand = opd;
				return 1 ;			
			}
		};

		this.checkAndFormatString = function(op, opd) {
			if( op == 'IS NULL' ) {
				operator = 'IS NULL';
				operand = '';
				return 1;								
			}
			else if( /^\s*$/.test(opd)  ) {
				operator = 'IS NOT NULL';
				operand = '';
				return 1;				
			}
			else if( op == 'BETWEEN' || op == '][' || op == ']=[' || op == '[]' || op == '[=]') {
				var words = opd.split(' ') ;
				if( words.length != 3 || !/and/i.test(words[1]) ||
						words[0].length == 0 || words[2].length == 0  ) {
					words = opd.match(/('.*')\s+(and)\s+('.*')/i);
					if( words == null  ||  words.length != 4 ) {
						that.notifyTypomsg(1, 'Operand must have the form "val1 AND val2" with operator "' + op + '"');
						return 0 ;
					} else {
						words[0] = words[1];
						words[1] = words[2];
						words[2] = words[3];
					}
				} 
				if( op == 'BETWEEN' ) {
					operator = op;
					if ( ! /^\s*'.*'\s*$/.test(words[0])  ) {
						words[0] = "'" +  words[0] + "'";
					}
					if ( ! /^\s*'.*'\s*$/.test(words[2])  ) {
						words[2] = "'" +  words[2] + "'";
					}

					operand = words[0] + ' AND ' + words[2];						
				}
				else {
					operator = op;
					operand = '(' + words[0] + ' , ' + words[2] + ')';												
				}
				return 1 ;
			}

			else {
				if ( /^\s*'.*'\s*$/.test(opd)  ) {
					operand = opd;
				}
				else {
					operand = "'" + opd + "'";
				}
				operator = op;
				return 1;			
			}
		};

		this.processRemoveConstRef = function(ahname) {
			saadaqlmodel.processRemoveConstRef(ahname);
		};

		this.processRemoveFirstAndOr = function(key) {			
			if( attributehandler.type != 'orderby') {
				saadaqlmodel.processRemoveFirstAndOr(key);
			}
		};

		this.removeAndOr = function() {
			andor = "";
		};

		this.getADQL = function(attQuoted) {		
			if(  ah.nameattr.startsWith('Qualifier ')) {
				return 'Qualifier{ ' + ah.nameattr.split(' ')[1] + operator + ' ' + operand + '}';
			}
			else if( operator.startsWith('CIRCLE') || operator.startsWith('BOX'))  {
				//				CONTAINS(POINT('ICRS GEOCENTER', "_s_ra", "_s_dec"), BOX('ICRS GEOCENTER', 'dsa', 'dsad', 'dsa', 'dsad')) = 'true';
				var coordkw = attributehandler.nameattr.split(' ');
				var bcomp = ( booleansupported )? "'true'" :  "1";
				return andor + " CONTAINS(POINT('ICRS GEOCENTER', \"" + coordkw[0] + "\", \"" +  coordkw[1] + "\"), "
				+ operator + ") = " + bcomp;
			}
			else if( attQuoted ){
				return andor + ' "' + attributehandler.nameattr + '" ' + operator + ' ' + operand;
			}
			else {
				return andor + ' ' + attributehandler.nameattr + ' ' + operator + ' ' + operand;
			}
		};
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributehandler, operators, andors, default_value);
			});
		};
		this.notifyTypomsg = function(fault, msg) {
			$.each(listeners, function(i){
				listeners[i].printTypomsg(fault,msg);
			});			
		};

	}
});
console.log('=============== >  kwconstraintModel.js ');

jQuery.extend({

	KWConstraintView: function(key, constlistid){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		var id_root = key;
		var constlist_id = constlistid;

		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};


		this.initForm= function(ah, operators, andors, default_value){
			$('#' + constlist_id).append("<div id=" + id_root + " style='float: none;'>");
			$('#' + id_root).html('');
			/*
			 * AND/OR operators
			 */
			if( andors.length > 0 ) {
				var select='<select id=' + id_root + "_andor style=\"font-size: small;font-family: courier;\">";
				for( var i=0 ; i<andors.length; i++ ) {
					var op = andors[i];
					select += '<option value=' + op + '>' +op + '</option>';
				}	
				select += '</select>';
				$('#' + id_root).append(select);
			}
			$('#' + id_root).append('<span id=' + id_root + '_name>' + ah.nameattr + '</span>');
			/*
			 * Logical operators
			 */
			if( operators.length > 0 ) {
				var select='<select id=' + id_root + "_op style=\"font-size: small;font-family: courier;\">";
				for( i=0 ; i<operators.length; i++ ) {
					var op = operators[i];
					var selected = '';
					if( op == '>' ) {
						op = '&gt;';
						if( ah.nameattr == 'Cardinality' ) {
							selected = 'selected';
						} 
					}
					else if( op == '<' ) {
						op = '&lt;';
					}
					select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
				}	

				select += '</select>';
				$('#' + id_root).append(select);
			}

			if( operators.length > 0 ) {
				$('#' + id_root).append('<input type=text id=' + id_root 
						+ "_val class=inputvalue style=\"font-size: small;font-family: courier;\" value='" 
						+ default_value + "'>");
			}
			$('#' + id_root).append('&nbsp;<a id=' + id_root + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' + constlist_id).append("</div>");		

			$('#' +  id_root + "_close").click(function() {
				$('#' +  id_root).remove();
				that.fireRemoveFirstAndOr(id_root);
				if( ah.nameattr.startsWith('Qualifier') || ah.nameattr.startsWith('Cardinality')) {
					that.fireRemoveConstRef(ah.nameattr);
				}
				that.fireEnterEvent();
			});

			$('#' +  id_root + "_op").change(function() {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, this.value
						, $('#' +  id_root + "_val").val());				
			});
			$('#' +  id_root + "_andor").change(function() {
				that.fireEnterEvent(this.value
						, $('#' +  id_root + "_op option:selected").text()
						, $('#' +  id_root + "_val").val());				
			});
			$('#' +  id_root + "_val").keyup(function(event) {
				/*
				 * Run the query is CR is typed in a KW editor
				 */
				if (event.which == '13') {
					if(  $('span.typomsg').css('color') == 'green') {
						resultPaneView.fireSubmitQueryEvent();
					}
					else {
						Modalinfo.info("Current contraint is not valid: can not run the query");
					}
				}
				else {
					that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
							, $('#' +  id_root + "_op option:selected").text()
							, this.value);
				}
			});
			$('#' +  id_root + "_val").click(function(event) {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, this.value);

			});


			that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
					,$('#' +  id_root + "_op option:selected").text()
					,$('#' +  id_root + "_val").val());
		};

		this.printTypomsg= function(fault, msg){
			$(".typomsg").each(function() {
				if(fault) {
					$(this).css('color', 'red');
				}
				else {
					$(this).css('color', 'green');					
				}
				$(this).text(msg);
			});
		};

		this.removeAndOr = function() {
			$('#' + id_root + "_andor" ).remove();
		};

		this.fireRemoveFirstAndOr = function(id_root){
			$.each(listeners, function(i){
				listeners[i].controlRemoveFirstAndOr(id_root);
			});
		};
		this.fireEnterEvent = function(andor, operator, operand){
			$.each(listeners, function(i){
				listeners[i].controlEnterEvent(andor, operator, operand);
			});
		};

		this.fireRemoveConstRef = function(ahname){
			$.each(listeners, function(i){
				listeners[i].controlRemoveConstRef(ahname);
			});
		};
	}
});
console.log('=============== >  kwconstraintView.js ');

jQuery.extend({

	KWConstraintControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
			controlEnterEvent : function(andor, operator, operand){
				model.processEnterEvent(andor, operator, operand);
			},
			controlRemoveConstRef : function(operator, operand){
				model.processRemoveConstRef(operator, operand);
			},
			controlRemoveFirstAndOr: function(key){
				model.processRemoveFirstAndOr(key);
			}
		}
		view.addListener(vlist);

		var mlist = {
			isInit : function(attributehandler, operators, andors, default_value){
				view.initForm(attributehandler, operators, andors, default_value);
			},
			printTypomsg: function(fault, msg){
				view.printTypomsg(fault,  msg);
			}
		}
		model.addListener(mlist);
		
		this.controlRemoveAndOr= function() {
			model.removeAndOr();
			view.removeAndOr();
		}
		this.getADQL = function(attQuoted) {
			return model.getADQL(attQuoted);
		}
	}
});

console.log('=============== >  kwconstraintControler.js ');

jQuery.extend({

	SaadaQLModel: function(pmodel){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners ;
		/*
		 * What we have to store and play with
		 */
		var attributesHandlers = new Array();
		var queriableUCDs = new Array();
		var relations = new Array();
		var editors = new Array();
		var orberby = '';
		var ucdeditors = new Array();
		var const_key = 1;
		var patternModel = pmodel;			
		var collection = '';
		var classe = '';
		var category = '';
		var histoQuery = new Array();
		var histoQueryPtr = -1;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(andsubmit, newTreeNode){
			nativeConstraintEditor.fireSetTreepath(globalTreePath);
			patternConstraintEditor.fireSetTreepath(globalTreePath);
			var md = MetadataSource.relations;
			var disabled = new Array();
			var selected = 0;
			if( globalTreePath.category == 'TABLE' ||  globalTreePath.category == 'MISC'||  globalTreePath.category == 'FLATFILE') {
				disabled[disabled.length] = 0;
				selected = 1;
			}
			/*
			 * If the event has been initiated from the data tree, the history pointer is
			 * set the end of the query list
			 */
			if(  newTreeNode ) {
				histoQueryPtr = (histoQuery.length - 1);
			}
			disabled[disabled.length] = 2;					

			$("#saadaqltab").tabs({
				disabled: disabled,
				selected: selected
			});
			queryView.reset("Select " + globalTreePath.category + " From " + globalTreePath.getClassname() + " In " + globalTreePath.schema);
			if( andsubmit == true ) {
				resultPaneView.fireSubmitQueryEvent();
			}
			if( md.relations != null ) {
				for( i=0 ; i<md.relations.length ; i++ ) {
					relations[md.relations[i].name] = md.relations[i];
				}
			}

			return;
///////////////////////////////
			Processing.show("Fetching meta data");
			$.getJSON("getmeta", params, function(jsondata) {
				Processing.hide();
				if( Processing.jsonError(jsondata, "Can not get data tree node description") ) {
					Processing.hide();
					return;
				}
				editors = new Array();
				ucdeditors = new Array();
				attributesHandlers = new Array();
				for( var i=0 ; i<jsondata.attributes.length ; i++ ) {
					attributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
				}
				/*
				 * Queriables UCDs are stored in a map (ucd as key, array of units as value)
				 */
				var with_ucd = false;
				queriableUCDs = new Array();
				for(var  i=0 ; i<jsondata.queriableucds.length ; i++ ) {
					var ah = jsondata.queriableucds[i];
					var ucd = ah.ucd;
					if( queriableUCDs[ucd] == null || queriableUCDs[ucd] == undefined) {
						queriableUCDs[ucd] = new Array();
						if( ah.unit != "" ) {
							(queriableUCDs[ucd])[0] = ah.unit;
						}
					} else {
						if( ah.unit != "" ) {
							var found = false;
							for( var u=0 ; u<queriableUCDs[ucd].length ; u++) {
								if( (queriableUCDs[ucd])[u] == ah.unit ) {
									found = true;
									break;
								}
							}
							if( !found ) {
								(queriableUCDs[ucd])[queriableUCDs[ucd].length] = ah.unit;
							}						
						}
					}
					with_ucd = true;									
				}
				relations = new Array();
				if( jsondata.relations != null ) {
					for( i=0 ; i<jsondata.relations.length ; i++ ) {
						relations[jsondata.relations[i].name] = jsondata.relations[i];
					}
				}
				var disabled = new Array();
				var selected = 0;
				if( category == 'TABLE' ||  category == 'MISC'||  category == 'FLATFILE') {
					disabled[disabled.length] = 0;
					selected = 1;
				}
				if( jsondata.relations == null || jsondata.relations.length == 0 ) {
					disabled[disabled.length] = 3;
				}
				if( with_ucd == false ) {
					disabled[disabled.length] = 2;					
				}
				$("#saadaqltab").tabs({
					disabled: disabled,
					selected: selected
				});

				that.notifyInitDone();	
				if( defaultquery == undefined ) {
					/*
					 * Check in UCDS if there a column sorted by default
					 */
					for( kw in attributesHandlers ) {
						ah = attributesHandlers[kw];
						if( ah.ucd.match(/sort.desc/) ){
							that.setOrderBy(kw);
							$('#orderby_des').prop('checked',true);
						} else if( ah.ucd.match(/sort.asc/) ){
							that.setOrderBy(kw);
							$('#orderby_asc').prop('checked',true);
						}
					}
					/*
					 * Set a desc sort by default on oidsaada in order to display first the latest data
					 */
					if( $("#orderby span").length == 0 ) {
						that.setOrderBy("oidsaada");
						$('#orderby_des').prop('checked',true);
					}
//					$("#orderby span").each(function() {
//						if( $(this).text() == '' ) {
//							$(this).text('oidsaada');
//						}
//					});
					query = that.updateQuery();
				} else {
					that.notifyQueryUpdated(defaultquery);
					$("#saadaqltab").tabs({
						selected: 4
					});				

				}
				if( andsubmit == true ) {
					resultPaneView.fireSubmitQueryEvent();
				}
			});

		};

		this.processAttributeEvent= function(uidraggable){
			var kwname = uidraggable.find(".item").text().split(' ')[0];
			var ah = attributesHandlers[kwname];
			var first = true;
			for( k in editors ) {
				first = false;
				break;
			}
			var m = new $.KWConstraintModel(first, ah, this, '');
			var div_key = "kw" +  const_key;
			var v = new $.KWConstraintView(div_key, 'ConstraintsList');
			editors[div_key] =  new $.KWConstraintControler(m, v);
			m.notifyInitDone();
			const_key++;
		};

		this.processOrderByEvent= function(uidraggable){
			var kwname = uidraggable.find(".item").text().split(' ')[0];
			this.setOrderBy(kwname);
		};

		this.setOrderBy= function(kwname){
			var ah = attributesHandlers[kwname];
			if( kwname != null || ah == null) {
				var ah = attributesHandlers[kwname];
				var m = new $.KWConstraintModel(true, { 
					"nameattr" : ah.nameattr 
					, "nameorg" : ah.nameorg
					, "type" : "orderby"
						, "ucd" : ah.ucd
						, "utype" : ah.utype
						, "unit" : ah.unit
						, "comment" : ah.description}
				, this);

				var div_key = "ob" +  const_key;
				var v = new $.KWConstraintView(div_key, 'orderby');
				orderby =  new $.KWConstraintControler(m, v);
				m.notifyInitDone();

			} else {
				$("#orderby").each(function() {
					$(this).html('');
					orderby = null;
				});

			}
		};
		this.sortColumn= function(nameattr, sens) {
			if( sens != null ) {
				this.setOrderBy(nameattr);		
				if( sens == 'asc' ) {
					$('#orderby_asc').prop('checked',true);
				} else {			
					$('#orderby_des').prop('checked',true);
				}
			} else {
				this.setOrderBy(null);		
			}
			this.updateQuery();
			resultPaneView.fireSubmitQueryEvent();
		};
		this.getOrderByParameters = function() {
			var nameattr ='';;
			$("#orderby span").each(function() {
				nameattr = $(this).text();
			});
			return {nameattr: nameattr, asc: ($('#orderby_asc').prop('checked') == true) };
		};
		this.processOIDTableEvent= function(oidtable){
			var ah = attributesHandlers["oidtable"];
			if( ah != undefined) {
				var first = true;
				for( k in editors ) {
					first = false;
					break;
				}
				var m = new $.KWConstraintModel(first, ah, this, '');
				var div_key = "kw" +  const_key;
				var v = new $.KWConstraintView(div_key, 'ConstraintsList');
				editors[div_key] =  new $.KWConstraintControler(m, v);
				m.notifyInitDone();
				m.processEnterEvent("", "=", oidtable);
				$("#" + div_key + "_val").val(oidtable);
				const_key++;
			}
		};

		this.processUCDEvent= function(uidraggable){
			var ucd = uidraggable.find(".item").text().split(' ')[0];
			var first = true;
			for( k in ucdeditors ) {
				first = false;
				break;
			}
			var m = new $.UCDConstraintModel(first, ucd, queriableUCDs[ucd], this, '');
			var div_key = "ucd" +  const_key;
			var v = new $.UCDConstraintView(div_key, 'UCDConstraintsList');
			ucdeditors[div_key] =  new $.UCDConstraintControler(m, v);
			m.notifyInitDone();
			const_key++;
		};

		this.processInputCoord= function(coord, radius){
			var frame = 'J2000,ICRS';

			that.notifyCoordDone("coo" +  const_key, 'isInCircle("' + coord + '", ' + radius + ', ' + frame + ')');
			that.updateQuery();
			const_key++;
		};

		this.processSelectRelation= function(relation) {
			patternModel.initRelation(relations[relation]);
		};

		this.updateQuery = function() {
			/*
			 * Query can not be updated while category/class/collection are not set
			 */
			if( typeof category == 'undefined' ) {
				return ;
			}
			var query = "Select " + category + " From " + classe + " In " + collection ;
			var cq = "";
			$("#CoordList span").each(function() {
				if( cq.length > 0 ) cq += ",\n";
				cq +=  '    ' + $(this).text();
			}); 
			if( cq.length > 0 ) {
				query += "\nWherePosition { \n" + cq + "}";
			}

			cq = "";
			$("#ConstraintsList div").each(function() {
				cq +=  '    ' + editors[$(this).attr('id')].getADQL(false) ;
				if( cq.length > 50 ) cq += '\n';
			}); 
			if( cq.length > 0 ) {
				query += "\nWhereAttributeSaada { \n" + cq + "}";
			}

			cq = "";
			$("#UCDConstraintsList div").each(function() {
				cq +=  '    ' + ucdeditors[$(this).attr('id')].getADQL(false) ;
				if( cq.length > 50 ) cq += '\n';
			}); 
			if( cq.length > 0 ) {
				query += "\nWhereUCD { \n" + cq + "}";
			}

			cq = "";
			$("#patternlist input").each(function() {
				if( cq.length > 0 ) cq += "\n";
				cq += unescape($(this).val());
			});
			if( cq.length > 0 ) {
				query += "\nWhereRelation { \n" + cq + "\n    }";
			}

			$("#orderby span").each(function() {
				query += "\nOrder By " + $(this).text();
				if( $("input[name=sens]:checked").attr("value") == 'des' ) {
					query += " desc";
				}
			});

			if( $("#qlimit").val().match(/^[0-9]+$/) ) {
				query += '\nLimit ' + $("#qlimit").val();
			}

			that.notifyQueryUpdated(query);
		};

		this.processRemoveFirstAndOr = function(key) {
			delete editors[key];
			for( var k in editors ) {
				editors[k].controlRemoveAndOr();
				break;
			}
			delete ucdeditors[key];
			for( var k in ucdeditors ) {
				ucdeditors[k].controlRemoveAndOr();
				break;
			}
		};

		this.processStoreHisto = function(query) {
			console.log( histoQueryPtr  + " "+  histoQuery.length)
			/*
			 * Do not store if the query has not change
			 */
			if( histoQuery.length > 0 && histoQuery[histoQuery.length-1].query == query ) {
				return;
			}
			/*
			 * Do not not store if a previous query is submitted again
			 */
			if( histoQueryPtr > -1 && histoQueryPtr != (histoQuery.length - 1) ) {
				this.setTitle();
				return;
			}
			histoQueryPtr = histoQuery.length;
			histoQuery[histoQuery.length] = {query: query , treepath: jQuery.extend({}, globalTreePath) };

			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.displayHisto = function() {
			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.processHisto = function(direction) {
			if( direction == 'next') {
				if( histoQueryPtr >= (histoQuery.length - 1)) {
					return;
				}
				histoQueryPtr++;
			} else {
				if( histoQueryPtr < 1) {
					return;
				}
				histoQueryPtr--;
			}
			
			this.processTreeNodeEvent(false, false);
			queryView.reset(histoQuery[histoQueryPtr].query);
			$("#saadaqltab").tabs({
				selected: 4
			});
			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.setTitle = function(){
			globalTreePath = jQuery.extend({}, histoQuery[histoQueryPtr].treepath);
			$('#titlepath').html(globalTreePath.title);

		};
		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			listener.isInit(attributesHandlers, relations, queriableUCDs);
		};
		this.notifyCoordDone = function(key, constr){
			listener.coordDone(key, constr);
		};
		this.notifyQueryUpdated= function(query) {
			listener.queryUpdated(query);
		};

	}
});

console.log('=============== >  saadaqlModel.js ');

jQuery.extend({

	SaadaQLView: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listener ;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};

		this.fireTreeNodeEvent = function(andsubmit, newTreeNode){
			listener.controlTreeNodeEvent(andsubmit, newTreeNode);
		};
		this.fireAttributeEvent = function(uidraggable){
			listener.controlAttributeEvent(uidraggable);
		};
		this.fireOrderByEvent = function(uidraggable){
			listener.controlOrderByEvent(uidraggable);
		};
		this.fireSortColumnEvent = function(nameattr, sens){
			listener.controlSortColumnEvent(nameattr, sens);
		};
		this.fireOrderByParameters= function() {
			var retour = null;
			retour = listener.controlOrderByParameters();
			return retour;
		};

		this.fireOIDTableEvent = function(oidtable){
			listener.controlOIDTableEvent(oidtable);
		};
		this.fireUCDEvent = function(uidraggable){
			listener.controlUCDEvent(uidraggable);
		};
		this.fireInputCoordEvent = function(){
			if( $("#coordval").val() == '' || $("#radiusval").val() == '' ) {
				Modalinfo.info("Both position and radius must be given");
				return;
			}
			listener.controlInputCoord($("#coordval").val(), $("#radiusval").val());
		};
		this.fireUpdateQueryEvent = function(){
			listener.controlUpdateQueryEvent();
		};
		this.fireSelectRelationEvent = function(relation){
			listener.controlSelectRelationEvent(relation);
		};
		this.fireHisto = function(direction){
			$("#saadaqltab").tabs({
				selected: 4
			});				
			listener.controlHisto(direction);
		};
		this.fireStoreHisto = function(query){
			listener.controlStoreHisto(query);
		};
		this.fireDisplayHisto = function(){
			listener.controlDisplayHisto();
		};
		this.fireTitleEvent = function(){
			listener.controlTitleEvent();
		};
		this.showProgressStatus = function(){
			Modalinfo.info("Job in progress");
		};
		this.showFailure = function(textStatus){
			Modalinfo.info("view: " + textStatus);
		}	;	
		this.displayResult= function(dataJSONObject){
		};
		this.initForm= function(attributesHandlers, relations, queriableUCDs){
			/*
			 * Reset form
			 */
			$('#CoordList').html('');
			$('#ConstraintsList').html('');
			$('#orderby').html('');
			$('#UCDConstraintsList').html('');
			$('#relationselect').html('');
			$('#patterncardqual').html('');
			$('#cpclassselect').html('');
			$('#patternatt').html('');
			$('#patternconst').html('');
			$('#patternlist').html('');
			/*
			 * Get table columns
			 */
			var table  = "<ul class=attlist>";
			for( i in attributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ attributesHandlers[i].nameattr 
					+ " (" + attributesHandlers[i].type 
					+ ") " + attributesHandlers[i].unit 
					+ "</span></li>";
			}
			table += "</ul>";
			$("#meta").html(table);
			$(function() {
				$("#meta" ).sortable({
					revert: "true"
				});
				$( "div#meta li" ).draggable({ 
					connectToSortable: ".SortableConstraintsList",
					helper: "clone", 
					revert: "invalid"
				});
			});
			/*
			 * Get UCDs list
			 */
			var table  = "<ul class=attlist>";
			for( i in queriableUCDs  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ i;
				if( queriableUCDs[i].length > 0 ) {
					table += "</span><select>";
					table += "<option>-- Stored units --</option>";						
					for( var u=0 ; u<queriableUCDs[i].length ; u++) {
						table += "<option>" + (queriableUCDs[i])[u] + "</option>";						
					}
					table += "</select>";
				}
				else {
					table += " (no stored units)</span>";
				}
				table += "</li>";
			}
			table += "</ul>";
			$("#ucdmeta").html(table);
			$(function() {
				$("#ucdmeta" ).sortable({
					revert: "true"
				});
				$( "div#ucdmeta li" ).draggable({ 
					connectToSortable: "#UCDConstraintsList",
					helper: "clone", 
					revert: "invalid"
				});
			});
			/*
			 * Populate the relation selector
			 */
			var options = "<option>-- Select a relation --</option>";
			for( var i in relations  ) {
				options += "<option>" + relations[i].name + "</option>";
			}
			$("#relationselect").html(options);
		};

		this.coordDone= function(key, constr){
			$('#CoordList').append("<div id=" + key + "></div>");

			$('#' + key).html('<span id=' + key + '_name>' + constr + '</span>');
			$('#' + key).append('<a id=' + key + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' +  key + "_close").click(function() {
				$('#' +  key).remove();
				that.fireUpdateQueryEvent();
			});
		};

		this.queryUpdated= function(query){
			$('#saadaqltext').val(query);
		};
	}
});
console.log('=============== >  saadaqlView.js ');

jQuery.extend({

	SaadaQLControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlTreeNodeEvent : function(andsubmit, newTreeNode){
					model.processTreeNodeEvent(andsubmit, newTreeNode);
				},
				controlAttributeEvent: function(uidraggable){
					model.processAttributeEvent(uidraggable);
				},
				controlOrderByEvent: function(uidraggable){
					model.processOrderByEvent(uidraggable);
				},
				controlSortColumnEvent: function(nameattr, sens){
					model.sortColumn(nameattr, sens);
				},
				controlOrderByParameters: function() {
					return model.getOrderByParameters();
				},
				controlOIDTableEvent: function(oidtable){
					model.processOIDTableEvent(oidtable);
				},
				controlUCDEvent: function(uidraggable){
					model.processUCDEvent(uidraggable);
				},
				controlInputCoord: function(coord, radius){
					model.processInputCoord(coord, radius);
				},
				controlUpdateQueryEvent: function(){
					model.updateQuery();
				},
				controlSelectRelationEvent: function(relation){
					model.processSelectRelation(relation);
				},
				controlHisto: function(direction){
					model.processHisto(direction);
				},
				controlStoreHisto: function(query){
					model.processStoreHisto(query);
				},
				controlDisplayHisto: function() {
					model.displayHisto();
				}, 
				controlTitleEvent: function() {
					model.setTitle();
				}, 
		}
		view.addListener(vlist);

		var mlist = {
				isInit : function(attributesHandlers, relations, queriableUCDs){
					view.initForm(attributesHandlers, relations, queriableUCDs);
				},
				coordDone : function(key, constr){
					view.coordDone(key, constr);
				},
				queryUpdated : function(query){
					view.queryUpdated(query);
				}
		}

		model.addListener(mlist);
	}
});

console.log('=============== >  saadaqlControler.js ');

jQuery.extend({

	CartView: function(jid){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;

		/**
		 * datatables references
		 */
		var folderTables = new Array();
		/**
		 * who is listening to us?
		 */
		var listener;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};

		this.fireAddJobResult = function(element, query) {
			var elementClass = element.attr('class');
			if( elementClass == 'dl_cart' ) {
				Processing.show("Selection " + getTreePathAsKey() + " added to the cart");
				element.attr('class', 'dl_cart_added');
				Out.info("add " + getTreePathAsKey() + " <> " + query);
				listener.controlAddJobResult(getTreePathAsKey(), query);
			} else {
				Processing.show("Selection " + getTreePathAsKey() + " remove from the cart");
				element.attr('class', 'dl_cart');
				listener.controlRemoveJobResult(getTreePathAsKey(), query);
			}
			this.fireCheckArchiveCompleted();
			setTimeout('Processing.hide();', 1000);
		};
		this.fireRemoveJobResult = function(nodekey, jobid) {
			Out.info("remove " + nodekey() + " <> " + jobid);
			listener.controlRemoveJobResult(nodekey, jobid);
			this.fireCheckArchiveCompleted();
		};
		this.fireAddUrl = function(element, name, oid) {
			var elementClass = element.attr('class');
			if( elementClass == 'dl_cart' || elementClass == 'dl_securecart') {
				element.attr('class', elementClass + '_added');
				Out.info("add " + name + " <> " + oid);
				listener.controlAddUrl(name, oid);
			} else {
				element.attr('class', elementClass.replace('_added', ''));
				listener.controlRemoveUrl(getTreePathAsKey(), oid);
			}
			this.fireCheckArchiveCompleted();
		};
		this.fireRemoveUrl = function(nodekey, url) {
			Out.info("removeURL " + nodekey() + " <> " + jobid);
			listener.controlRemoveUrl(nodekey, url);
			this.fireCheckArchiveCompleted();
		};
		this.fireOpenCart = function() {			
			listener.controlOpenCart();
		};
		this.fireCleanCart = function(tokens) {
			listener.controleCleanCart(tokens);
			this.fireCheckArchiveCompleted();
		};
		this.fireStartArchiveBuilding = function() {
			listener.controlStartArchiveBuilding();
		};
		this.fireKillArchiveBuilding = function() {
			listener.controlKillArchiveBuilding();
		};
		this.fireArchiveDownload = function() {			
			listener.controlArchiveDownload();
		};
		this.fireGetJobPhase = function() {
			var retour='';
			retour = listener.controlGetJobPhase();
			return retour;
		};
		this.fireChangeName = function(nodekey, dataType, rowNum, newName){
			listener.controlChangeName(nodekey, dataType, rowNum, newName);
			this.fireCheckArchiveCompleted();
		};
		this.fireSetRelations= function(nodekey, dataType, uri, checked) {
			listener.controlSetRelations(nodekey, dataType, uri, checked);
			this.fireCheckArchiveCompleted();
		};
		this.fireDelegateCartDownload= function() {
			listener.controlDelegateCartDownload();
		};
		
		this.resetJobControl= function() {
			Out.info("resetJobControl");
			listener.controlResetZipjob();
			$('.cart').css("border", "0px");
			$('#detaildiv_download').attr("disabled", true);
			$('#detaildiv_submit').removeAttr("disabled");
			var jobspan = $('#cartjob_phase');
			jobspan.attr('class', 'nojob');
			jobspan.text('nojob');
			};

		this.fireCheckArchiveCompleted = function() {
			var phase = that.fireGetJobPhase();
			var queriespan = $('#cartjob_phase');
			queriespan.attr('class', phase.toLowerCase());
			queriespan.text(phase);
			if( phase == 'nojob') {
				$('.cart').css("border", "0px");
				$('#detaildiv_submit').removeAttr("disabled");
				$('#detaildiv_download').attr("disabled", true);
			}
			else if( phase == 'EXECUTING') {
				$('.cart').css("border", "2px solid orange");
				setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
			}
			else if( phase == 'COMPLETED') {
				$('.cart').css("border", "2px solid green");
				$('#detaildiv_submit').attr("disabled", true);
				$('#detaildiv_download').removeAttr("disabled");
			}
			else {
				$('.cart').css("border", "2px solid red");
			}
		};    

		this.initForm = function(cartData) {
			$('#detaildiv').remove();
			if ($('#detaildiv').length == 0) {
				$(document.documentElement).append(
				"<div id=detaildiv style='width: 99%; display: none;'></div>");
			}
			var empty = true;
			for( var nodekey in cartData) {
				empty = false;
				break;
			}			
			if( empty ) {
				Modalinfo.info("Empty Shopping Cart");
				return;
			}

			var table = '';
			//var phase = that.fireGetJobPhase();

			//table += '<h2><img src="images/groscaddy.png"> Shopping Cart</h2>';
			table += '<div id=table_div></div>';
			table += "<p id=\"cartjob\" class='chapter'> <img src=\"images/tdown.png\">Processing status</p>";
			//table += '<span>Current Job Status</span> <span id=cartjob_phase class="' + phase.toLowerCase() + '">' + phase + '</span><BR>';
			table += '<br><span>Current Job Status</span> <span id=cartjob_phase class=""></span><BR>';
			table += "<span>Manage Content</span> <input type=button id=detaildiv_clean value='Remove Unselected Items'>";			
			table += "<input type=button id=detaildiv_cleanall value='Remove All Items'><br>";			
			table += "<span>Manage Job</span> <input type=button id=detaildiv_submit value='Start Processing'>";	
			table += "<input type=button id=detaildiv_abort value='Abort'>";			
			if( typeof PeerCartClient != 'undefined' && PeerCartClient != '' ) {
				table += "<input title='Delegate cart control to " + PeerCartClient + "' type=button value='Delegate' onclick='cartView.fireDelegateCartDownload();'>" ;
			}
			table += "<br><span>Get the Result</span> <input type=button id=detaildiv_download value='Download Cart' disabled='disabled'>";			

			//Modalpanel.open(table);
			Modalinfo.dataPanel('<a class="cart-title" href="#"></a> Shopping Cart' , table, null, "white");
			that.setTableDiv(cartData);

			$('#detaildiv_clean').click( function() {
				var tokenArray =new Array();
				for( var i=0 ; i<folderTables.length ; i++) {
					tokenArray[tokenArray.length]  = $('input',folderTables[i].fnGetNodes()).serialize();
				}
				that.fireCleanCart(tokenArray);
				return false;
			} );
			$('#detaildiv_cleanall').click( function() {
				that.fireCleanCart("");
				Modalinfo.close($(this).parent().attr("id"));
				//$(".dl_cart_added").attr("class","dl_cart");
				return false;
			} );
			$('#detaildiv_submit').click( function() {
				aborted = false;
				that.fireStartArchiveBuilding();
				return false;
			} );
			$('#detaildiv_abort').click( function() {
				if (aborted == false) {
					aborted = true;
					that.fireKillArchiveBuilding();
					that.fireCheckArchiveCompleted();
				}
				return false;
			} );

			$('#detaildiv_download').click( function() {
				that.fireArchiveDownload();
				$('.zip').css("border", "0px");
				return false;
			} );
			this.fireCheckArchiveCompleted();
		};

		this.setTableDiv= function(cartData) {
			folderTables = new Array();
			var table = '';
			var empty = true;
			for( var nodekey in cartData) {
				empty = false;
				break;
			}			
			if( empty ) {
				Modalinfo.info("Empty Shopping Cart");
				$.modal.close();
				return;
			}
			for( var nodekey in cartData) {
				var tableId = "folder_" + nodekey.replace(/\./g, '_DoT_');;
				table += "<p id=\"mappedmeta\" class='chapter'> <img src=\"images/tdown.png\">Node  " + nodekey + " </p>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"" + tableId +"\" class=\"display\"></table>";
				table += "</div>";
			}
			$('#table_div').html(table);
			for( var nodekey in cartData) {
				var tableId = "folder_" + nodekey.replace(/\./g, '_DoT_');;
				var folder = cartData[nodekey];
				var aaData = new Array();
				var cb = "<INPUT TYPE=CHECKBOX name=\"" + nodekey + " url\" class=\"include_relation\" />";
				for( var i=0 ; i<folder.queries.length ; i++) {
					aaData[aaData.length] = ["<INPUT TYPE=CHECKBOX checked name=\"" + nodekey + " query_result " + i + "\" value=" + i +" />"
					                         , "QUERY_RESULT"
					                         , cb
					                         , "<span>" + folder.queries[i].name + "</span>", folder.queries[i].uri];
				}
				for( var i=0 ; i<folder.files.length ; i++) {
					aaData[aaData.length] = ["<INPUT TYPE=CHECKBOX checked name=\"" + nodekey + " single_file " + i + "\" value=" + i +" />"
					                         ,  "SINGLE_FILE"
					                         , cb
					                         , "<span>" + folder.files[i].name + "</span>", folder.files[i].uri];
				}
				
				
				var options = {
						"aoColumns" : [{sTitle: "Keep/Discard"}, {sTitle: "Data Source"},{sTitle: "Include Linked Data"},{sTitle: "Resource Name"},{sTitle: "Resource URI"}],
						"aaData" : aaData,
						"bPaginate" : false,
						"bInfo" : false,
						"aaSorting" : [],
						"bSort" : false,
						"bFilter" : false,
						"bAutoWidth" : true,
						"bDestroy": true
				}
				
				folderTables[folderTables.length] = CustomDataTable.create(tableId, options);
				/* Apply the jEditable handlers to the table */
				$('span', folderTables[folderTables.length-1].fnGetNodes()).editable( 
						function(data) {
							var retour = data.replace(/[^\w\.]/g, "_");
							return  (retour != '')? retour: 'NoName';
						},
						{        
							"callback": function( sValue, settings ) {
								var oTable = folderTables[settings["numTable"]];
								var node = $(this).parent().get(0);
								var aPos = oTable.fnGetPosition( node );
								var nodekey = $(node).parents("table").attr("id").replace("folder_", "").replace(/_DoT_/g, '.');
								cartView.fireChangeName(nodekey, oTable.fnGetData( aPos[0] )[1], aPos[0], sValue);
							},
							"height": "1.33em", 
							"width": "16em",
							"numTable": folderTables.length-1 // added by myself
						}
				);
				$('#' + tableId + ' input.include_relation').click(
						function(e){
							cartView.fireSetRelations($(this).parents("table").attr("id").replace("folder_", "").replace(/_DoT_/g, '.')
									, $(this).parents("tr").find('td:nth-child(2)').text()
									, $(this).parents("tr").find('td:nth-child(5)').text()
									, $(this).is(':checked'));
							
						});
			}
			Modalinfo.center();
		}
	}
});
console.log('=============== >  cartView.js ');

jQuery.extend({

	CartControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlAddJobResult : function(nodekey, jobid){
					model.addJobResult(nodekey, jobid);
				},
				controlRemoveJobResult : function(nodekey, jobid){
					model.removeJobResult(nodekey, jobid);
				},
				controlAddUrl : function(name, oid){
					model.addUrl(name, oid);
				},
				controlRemoveUrl : function(nodekey, url){
					model.removeUrl(nodekey, url);
				},
				controlOpenCart : function(){
					model.notifyCartOpen();
				},
				controleCleanCart: function(tokens){
					model.cleanCart(tokens);
				},
				controlStartArchiveBuilding: function() {
					model.startArchiveBuilding();
				},
				controlKillArchiveBuilding: function() {
					model.killArchiveBuilding();
				},
				controlGetJobPhase: function() {
					return model.getJobPhase();
				},
				controlArchiveDownload: function() {
					return model.archiveDownload();
				},
				controlChangeName: function(nodekey, dataType, rowNum, newName) {
					model.changeName(nodekey, dataType, rowNum, newName);				
				},
				controlSetRelations: function(nodekey, dataType, uri, checked) {
					model.setRelations(nodekey, dataType, uri, checked);				
				},
				controlDelegateCartDownload: function() {
					model.delegateCartDownload();				
				},
				controlResetZipjob: function() {
					model.resetZipjob();				
				}
		};
		view.addListener(vlist);

		var mlist = {
				isCartCleaned : function(cartData){
					view.setTableDiv(cartData);
				},
				isInit : function(cartData){
					view.initForm(cartData);
				}
		};
		model.addListener(mlist);

	}
});

console.log('=============== >  cartControler.js ');

jQuery.extend({

	CartModel: function(nodekey, description){

		var listeners;
		var that = this;

		var cartData = {};
		var zipJob = '';;
		var queryNum=0;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};
		this.addJobResult = function(nodekey, jobid) {
			var entry;
			var name = 'query_' + queryNum;
			var query = unescape(jobid);
			queryNum++;
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {queries: new Array(), files: new Array()};
				cartData[nodekey].queries[0] = {name: name, uri: query, relations: []};
			}
			else {
				var queries = entry.queries;
				for( var i=0 ; i<queries.length ; i++ ) {
					if( queries[i].uri == query ) {
						Modalinfo.info("Query Result already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].queries[i] = {name: name, uri: query, relations: []};			
			}
			this.killArchiveBuilding();
		};
		this.removeJobResult = function(nodekey, jobid) {
			var entry;
			var sjobid = unescape(jobid);
			if( (entry = cartData[nodekey]) == undefined ) {
				Modalinfo.info("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var queries = entry.queries;
				for( var i=0 ; i<queries.length ; i++ ) {
					Out.info(queries[i].uri + " " + sjobid );
					if( queries[i].uri == sjobid ) {
						queries.splice(i,1);
						if( queries.length == 0 && entry.files.length == 0 ) {
							delete cartData[nodekey];
						}
						this.killArchiveBuilding();
						return;
					}
				}
				Modalinfo.info("Job " + nodekey + "." + jobid+ " not found in from the cart", "input Error");
			}			
			that.killArchiveBuilding();
		};
		this.addUrl = function(name, oid) {
			var entry;
			var nodekey = getTreePathAsKey();
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {queries: new Array(), files: new Array()};
				cartData[nodekey].files[0] = {name: name, uri: oid, relations: []};
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					if( files[i].uri == oid ) {
						Modalinfo.info("This url of node " + nodekey  + " is already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].files[i] = {name: name, uri: oid, relations: []};			
			}	
			that.killArchiveBuilding();

		};
		this.removeUrl = function(nodekey, url) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				Modalinfo.info("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					if( files[i].uri == url ) {
						files.splice(i,1);
						if( files.length == 0 && entry.queries.length == 0 ) {
							delete cartData[nodekey];
						}		
						this.killArchiveBuilding();
						return;
					}
				}
				Modalinfo.info("URL not found in from the cart", "input Error");
			}						
			this.killArchiveBuilding();
		};

		this.cleanCart = function(tokenArray) {
			var old_cartData = cartData;
			cartData = {};
			for( var t=0 ; t<tokenArray.length ; t++ ) {
				var tokens = tokenArray[t];

				var tkList = tokens.split("&");
				for( var i=0 ; i<tkList.length ; i++ ){
					var row  = tkList[i].split('=');
					var num  = row[1];
					var key  = row[0].split('+');
					var node = key[0];
					if( key[1] == 'query_result' ) {
						that.addJobResult((old_cartData[node]).queries[num].name
								, (old_cartData[node]).queries[num].uri);
					}
					else if( key[1] == 'single_file' ) {
						that.addUrl((old_cartData[node]).files[num].name
								, (old_cartData[node]).files[num].uri);
					}
				}
			}
			this.killArchiveBuilding();
			this.notifyCartCleaned();
		};

		this.changeName= function(nodekey, dataType, rowNum, newName) {
			if( dataType.toLowerCase() == "query_result" ) {
				cartData[nodekey].queries[rowNum].name = newName;
			}
			else {
				cartData[nodekey].files[rowNum].name = newName;
			}
			this.killArchiveBuilding();
			this.notifyCartCleaned();			
		};

		this.setRelations = function(nodekey, dataType, uri, checked) {
			var relValue = (checked == true )? ["any-relations"] : [];
			var files;
			if( dataType.toLowerCase() == "single_file" ) {
				files = cartData[nodekey].files;
			}
			else {
				files = cartData[nodekey].queries;
			}

			for( var i=0 ; i<files.length ; i++ ) {
				if( files[i].uri == uri ) {
					files[i].relations = relValue;
					Out.info("Relations set to "  + checked + " in " + dataType  + "/" + dataType + "/" +  uri);
					this.killArchiveBuilding();
					return;
				}
			}
		};

		this.notifyCartCleaned = function() {
			listener.isCartCleaned(cartData);
		};
		this.notifyCartOpen = function() {
			listener.isInit(cartData);
		};

		this.startArchiveBuilding = function() {
			$.ajax({
				type: 'POST',
				url: "cart/zipper",
				data: {PHASE: 'RUN', FORMAT: 'json',CART: JSON.stringify(cartData) },
				success: function(xmljob, status) {
					that.zipJob = new $.ZipjobModel(xmljob);
					setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
				},
				dataType: "xml",
				error: function(xmljob, textStatus, errorThrown) {
					Modalinfo.info("Error: " + textStatus);
				}
			});
		};

		this.delegateCartDownload= function() {
//			if ($('#detaildiv').length == 0) {
//				$(document.documentElement)
//				.append(
//				"<div id=detaildiv style='height: 99%; width: 99%; display: none;'><div style='color: black;' id=description name=pouet></div></div>");
//			}
//			$('#detaildiv').css('height', '99%');
//			$('#detaildiv').html('<iframe id="iframeid" name="iframename" style="height: 99%; width: 99%"></iframe>');
//			$('#detaildiv').modal();

			if ($('#delegateFormId').length == 0) {
				//var form = [ '<form id="delegateFormId" target="iframename" method="POST" action="', PeerCartClient, '">' ];
				var form = [ '<form id="delegateFormId" target="_sblank" method="POST" action="', PeerCartClient, '">' ];
				form.push('<input type="hidden" name="saadadburl" value="'
						, escape($(location).attr('href').replace('/#', '') + '/cart/zipper')
						, '" />');
				form.push('<input id="delegateCartContent" type="hidden" name="cartcontent" value=""/>');
				form.push('</form>');
				jQuery(form.join('')).appendTo('body');
			}
			$('#delegateCartContent').attr('value', escape(JSON.stringify(cartData)));			
			$('#delegateFormId').submit();
		};

		this.killArchiveBuilding = function() {
			if( that.zipJob == null ) {
				return "nojob";
			}
			else {
				that.zipJob.kill();
				return that.zipJob.phase;
			}
		};

		this.getJobPhase= function() {
			if( that.zipJob == null ) {
				return "nojob";
			}
			else {
				that.zipJob.refresh();
				return that.zipJob.phase;
			}
		};

		this.archiveDownload = function() {
			if( that.zipJob == null ) {
				Modalinfo.info("There is no active ZIP builder");
			}
			else {
				that.zipJob.download();
			}
		};		
		
		this.resetZipjob = function() {
			Out.info("Reset Zipjob");
			zipJob = null ;
		};

	}
});

console.log('=============== >  cartModel.js ');

jQuery.extend({

	SapView: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		this.fireTreeNodeEvent = function(treepath){
			$.each(listeners, function(i){
				listeners[i].controlTreeNodeEvent(treepath);
			});
		};
		this.fireSubmitQueryEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPQueryEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPQueryEvent();
				});	
			}
			// CS
			else if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSQueryEvent();
				});				
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		this.fireSubmitSampEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPSampEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPSampEvent();
				});	
			}
			// CS
			else if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSSampEvent();
				});				
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};

		this.fireDisplayHisto = function(){
			var result = '';
			result += '<a id="qhistoleft" title="Previous query" class="shaded histoleft  shaded" onclick="return false;"></a>';
			result += '<a id="qhistoright" title="Previous query" class="shaded historight" onclick="return false;"></a>';
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};
	}
});
console.log('=============== >  sapView.js ');

jQuery.extend({

	SapControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlTreeNodeEvent : function(treepath){
					model.processTreeNodeEvent(treepath);
				},
				controlSIAPQueryEvent: function(treepath){
					model.processSIAPQueryEvent(treepath);
				},
				controlSIAPSampEvent: function(treepath){
					model.processSIAPSampEvent(treepath);
				},
				controlSSAPQueryEvent: function(treepath){
					model.processSSAPQueryEvent(treepath);
				},
				controlSSAPSampEvent: function(treepath){
					model.processSSAPSampEvent(treepath);
				},
				controlCSQueryEvent: function(treepath){
					model.processCSQueryEvent(treepath);
				},
				controlCSSampEvent: function(treepath){
					model.processCSSampEvent(treepath);
				},
		};
		view.addListener(vlist);

		var mlist = {
				isInit : function(attributesHandlers, relations){
					view.initForm(attributesHandlers, relations);
				},
				coordDone : function(key, constr){
					view.coordDone(key, constr);
				},
				queryUpdated : function(query){
					view.queryUpdated(query);
				}
		};
		model.addListener(mlist);
	}
});

console.log('=============== >  sapControler.js ');

jQuery.extend({

	SapModel: function(pmodel){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/*
		 * What we have to store and play with
		 */		
		var collection = null;
		var category  = null;

		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			$("#siapscope").html("Service scope: Not Set");
			if( category == 'IMAGE' ) {
				$("#saptab").tabs({
					disabled: [2],
					selected: 1,
				});
				$("#siapscope").html("Service scope: IMAGES of " + collection);

			}
			else if( category == 'SPECTRUM' ) {
				$("#ssapscope").html("Service scope: SPECTRA of " + collection);
				$("#saptab").tabs({
					disabled: [1],
					selected: 2
				});
			}
			else if( category == 'ENTRY' ) {
				$("#csscope").html("Service scope: Table ENTRIES of " + collection);
				$("#saptab").tabs({
					disabled: [1,2],
					selected: 0
				});
			}
			else {
				$("#saptab").tabs({
					disabled: [0, 1,2],
				});
				$("#siapscope").html("Service scope: Not Set");
				$("#ssapscope").html("Service scope: Not Set");
				$("#cspscope").html("Service scope: Not Set");
			}
		};

		/*********************
		 * SIAP Event processing
		 */
		this.buildSIAPUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;
			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#siapcoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#siapradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}
			var url = base_url + 'siaservice?collection=[' + collection + ']&';
			var inter = $('#siapintersect option:selected').val();
			if( inter != 'COVERS') {
				url += 'INTERSECT=' + inter + '&';
			}
			if( $("input[value=siapcutout]").attr('checked') ) {				
				url += 'MODE=CUTOUT&';
			}
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) ;
			return url;
		};
		this.processSIAPQueryEvent= function(){
			var url = this.buildSIAPUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SIAP Result');
			}
		};
		this.processSIAPSampEvent= function(){
			var url = this.buildSIAPUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};
		/*********************
		 * SSAP Event processing
		 */
		this.buildSSAPUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#ssapcoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#ssapradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}

			var band1 = jQuery.trim($('#ssapbandmin').val());
			var band2 = jQuery.trim($('#ssapbandmax').val());
			var unit  = $('#ssapunit option:selected').val();
			var band = '';
			if( band1 != '' && band2 != '' ) {
				band = '&band=' + band1 + '/' + band2 + '[' + unit + ']';
			}
			var url = base_url + 'ssaservice?collection=[' + collection + ']&';
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) + band;
			return url;
		};
		this.processSSAPQueryEvent= function(){
			var url = this.buildSSAPUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SSA Result');
			}
		};
		this.processSSAPSampEvent= function(){
			var url = this.buildSSAPUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};

		/*********************
		 * CS Event processing
		 */
		this.buildCSUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#cscoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#csradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}
			var url = base_url + 'conesearch?collection=[' + collection + ']&';

			url += 'SR=' + escape(rad) + '&RA=' + escape(pos) ;
			return url;
		};
		this.processCSQueryEvent= function(){
			var url = this.buildCSUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SCS Result');
			}
		};
		this.processCSSampEvent= function(){
			var url = this.buildCSUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};

		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributesHandlers, relations);
			});
		};
	}
});

console.log('=============== >  sapModel.js ');

jQuery.extend({

	ZipjobModel:function(xmlSummary){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/*
		 * Job description params
		 */
		var xmlRoot='';
		var jobId='';
		var phase='';
		var params='';
		var results='';

		this.init = function(xmlSummary) {
			// Out.info((new XMLSerializer()).serializeToString(xmlSummary));
			/*
			 * The pair Chrome 15 and after and Jquery 1.7 do not support NS in XML
			 * parsing. We must feed the find() function selector including both NS an no NS filed names
			 */
			var xmlRoot = $(xmlSummary).find('uws\\:job, job');
			this.jobId = xmlRoot.find('uws\\:jobId, jobId').text();
			this.phase = xmlRoot.find('uws\\:phase, phase').text();
			this.params = new Array();
			xmlRoot.find("uws\\:parameters, parameters").find("uws\\:parameter, parameter").each(function() {
				that.params[$(this).attr("id")] = $(this).text();
			});	
			that.results = new Array();
			xmlRoot.find("uws\\:results, results").find("uws\\:result, result").each(function() {
				that.results[that.results.length] = $(this).attr("xlink:href");
			});
		};

		that.init(xmlSummary);

		this.kill = function() {
			if( that.jobId != '' ) {
				var url = "cart/zipper/" + that.jobId;
				that.jobId = '';
				that.results = [];
				that.phase = 'nojob';
				$.ajax({
					type: 'DELETE',
					dataType: "xml",
					url: url,
					success: function(xmljob, status) {
						Out.info("Job " +  url + " killed");
					},
					error: function(xhr, ajaxOptions, thrownError) {
						Modalinfo.info("Zipjob kill failed: Error " +  xhr.status + "\n" + xhr  + "\n" +ajaxOptions + "\n" + thrownError);
					}
				});
			}
		};
		this.refresh = function() {
			if( that.jobId != '' ) {
				$.ajax({
					dataType: "xml",
					type: 'GET',
					url: "cart/zipper/" + that.jobId,
					success: function(xmljob, status) {
						Out.info("refresh cart job success");
						that.init(xmljob);
					},
					error: function(xhr, ajaxOptions, thrownError) {
						that.jobId = '';
						that.results = [];
						Modalinfo.info("Zipjob refresh failed: Error " + xhr.status + "\n" + xhr  + "\n" +ajaxOptions + "\n" + thrownError);
					}
				});
			}
//			$.get("datapack/zipper/" + that.jobId
//			, function(data) {that.init(data);}
//			, "xml") ;
		};
		this.download = function() {
			if( that.results.length >= 1 ) {
				var url = that.results[0];
				PageLocation.changeLocation(url,"Download ZIPBALL");
			} else {
				Modalinfo.info("No ZIP archive available");
			}
		};
	}
});
console.log('=============== >  zipjobModel.js ');

/**
 * This file function is invoked atfer the DOM tree is built.
 * It creates all MVC and bind some DOM node with relevant event handlers
 * 
 * @author michel
 * @version $Id$
 */
var resultPaneView;
var saadaqlView ;
var sapView ;
var webSampView ;
var filterManagerView;
var cartView;

var base_url = '';
var booleansupported = false;
/*
 * JQuery object managing splitter panels
 */
var layoutPane;

/*
 * instance of DataTreePath: {nodekey:table, schema: collection, table: table, tableorg: table, category}
 */
var globalTreePath ;

var queryView;
var nativeConstraintEditor;
//var patternConstraintEditor;
var posConstraintEditor;

$().ready(function() {
	Out.setdebugModeFromUrl();	
	base_url = "http://" + window.location.hostname +  (location.port?":"+location.port:"") + window.location.pathname;
	/*
	 * layout plugin, requires JQuery 1.7 or higher
	 * Split the bottom div in 3 splitters divs.
	 */		
	layoutPane = $('#accesspane').layout();
	/*
	 * Connect the URL passed as parameter
	 */
	var defaultUrl  =  (RegExp('url=' + '(.+?)(&|$)').exec(location.search)||[,null])[1];
	if( defaultUrl != null ) {
		resultPaneView.fireNewNodeEvent(unescape(defaultUrl));
	}

	var resultPaneModel      = new $.ResultPaneModel();
	resultPaneView           = new $.ResultPaneView();
	new $.ResultPaneControler(resultPaneModel, resultPaneView);

	var saadaqlModel      = new $.SaadaQLModel(null);
	saadaqlView           = new $.SaadaQLView();
	new $.SaadaQLControler(saadaqlModel, saadaqlView);

	WebSamp_mVc.init("Saada"
			, (window.location.href + "/images/saadatransp-text-small.gif").replace(/\/#/g, '')
			, "Saada Database");


	var sapModel       = new $.SapModel();
	sapView            = new $.SapView();
	new $.SapControler(sapModel, sapView);

	var cartModel       = new $.CartModel();
	cartView            = new $.CartView();
	new $.CartControler(cartModel, cartView);

	/*********************************************************************************************
	 * Query form setup
	 * Create tabs to switch between SAADAQL and TAP forms
	 */
	//$("#saadaqltab").tabs();
	$("#saadaqltab").tabs({
		selected: 4,
		disabled: [0,1,2,3]
	});
	//$("#saptab").tabs();
	$("#saptab").tabs({
		unselect : true,
		//selected: 0,
		disabled: [0, 1,2]
	});
	$("#saptab").hide();
	/*
	 * Activate submit buttons
	 */
	$('#submitdirect').click(function() {
		saadaqlView.fireInputCoordEvent();
		resultPaneView.fireSaadaQLQueryEvent($('#saadaqltext').val());
	});
	$('#submitquery').click(function() {
		resultPaneView.fireSubmitQueryEvent();
	});
	$('.sapcapability').click(function() {
		sapView.fireSubmitCapabilityEvent();
	});
	$('.sapregistry').click(function() {
		sapView.fireSubmitRegistryEvent();
	});
	$('.sapglu').click(function() {
		sapView.fireSubmitGluEvent();
	});
	$("#qlimit").keyup(function(event) {
		if( $("#qlimit").val() == '' || $("#qlimit").val().match(/^[0-9]+$/) ) {
			saadaqlView.fireUpdateQueryEvent();
		}
		else {
			Modalinfo.info('The result limit must be a positive integer value' );
			$("#qlimit").val(100);
			return false;
		}

	});

	/*
	 * Coordinates input
	 */
	$("#coordform input").keypress(function(event) {
		if (event.which == '13') {
			saadaqlView.fireInputCoordEvent();
		}
	});
	/*
	 * pattern selectors
	 */
	$("#relationselect").change(function() {
		$("#relationselect option:selected").each(function () {
			console.log($(this).text());
			var text = $(this).text();
			if( !text.startsWith("--") ) {
				saadaqlView.fireSelectRelationEvent(text);
			}
			return;
		});
	});
	$("#cpclassselect").change(function() {
		$("#cpclassselect option:selected").each(function () {
			var text = $(this).text();
			if( !text.startsWith("--") ) {
				patternView.fireSelectClassEvent(text);
				return;
			}
		});
	});


	$("#acceptpattern").click(function() {
		patternView.fireAcceptPattern();
	});
	/*
	 * Order by
	 */
	$("input[name=sens]").click(function(){
		saadaqlView.fireUpdateQueryEvent();		
	});
	/*
	 * Query language selector
	 */
	$(".langswitch").click(function() {
		var mode = $(this).val();
		if( mode == 'saadaql') {
			$('#saptab').hide();
			$('#saadaqltab').show('slow');
			$("#qhistocount").css("visibility", "visible");
			saadaqlView.fireDisplayHisto();

		}
		else if( mode == 'sap') {
			$('#saadaqltab').hide();
			$('#saptab').show('slow');
			$("#qhistocount").css("visibility", "hidden");
			sapView.fireDisplayHisto();
		}
		else {
			$('#saadaqltab').hide();
			$('#saptab').hide();
			$("#qhistocount").css("visibility", "hidden");

		}
	});   
	/*
	 * Name resolver buton activation
	 */
	$(".sesame").click(function() {
		var inputfield = $(this).parents('div').find(".coordinputvalue");
		Processing.show("Asking Sesame name resolver");
		$.getJSON("sesame", {object: inputfield.val() }, function(data) {
			Processing.hide();
			if( Processing.jsonError(data, "Sesame failure") ) {
				return;
			}
			else {
				inputfield.val(data.alpha + ' ' + data.delta);
			}
		});
	});
	/*
	 * Get the base URL of the site. 
	 * Importatnt to avoid cross access issues
	 */
	Processing.show("Get site description");
	$.getJSON("sitedesc", function(data) {
		Processing.hide();
		base_url = data.rooturl;
		if( !base_url.match("/$") ) {
			base_url = base_url +"/";
		}
		$('#pagetitle').html("<span class=pagetitle>" + data.dbname + "</span>&nbsp<span class=pagetitlepath id=titlepath></span>");
		$('title').html(data.dbname);
		if( data.booleansupported == "true") {
			booleansupported = true;
		} 
	});
	
	$.getJSON("getversion", function(data) {
		$('#saadaLogo').attr("title", data.dbname + " database build with Saada " + data.version + " on " + data.dbms);
	});
	
	/*
	 * This callback can be changed changed at everytime: do not use the "onclick" HTML  
	 * attribute which is not overriden by JQuery "click" callback
	 */
	$('#showquerymeta').click(function(){Modalinfo.info("No meta data available yet");});

	$("[name=qlang]").filter("[value=\"saadaql\"]").attr("checked","checked");
	
	MetadataSource.init({getMetaTable: "getmeta"})
	queryView = QueryConstraintEditor.queryTextEditor({ parentDivId: 'texttab', defaultQuery: ''});	   		    
	posConstraintEditor = QueryConstraintEditor.posConstraintEditor({ parentDivId: 'postab', formName:'simpleposcolumns',  queryView: queryView
			, frames: ['ICRS', 'GALACTIC', 'FK5'], urls: {sesameURL: "sesame", uploadURL: "uploadposlist"}} );
	
	nativeConstraintEditor = QueryConstraintEditor.nativeConstraintEditor({parentDivId: 'kwtab', getMetaUrl: "getmeta", queryView: queryView});
	
	patternConstraintEditor = QueryConstraintEditor.matchPatternEditor({parentDivId: 'patterntab',formName: 'matchPattern',queryView: queryView});
	
		//qce.fireSetTreepath(new DataTreePath({nodekey:'node', schema: 'schema', table: 'table', tableorg: 'schema.table'}));
	
	DataTree.init();

	//PageLocation.confirmBeforeUnlaod();		
	Out.setdebugModeFromUrl();
});

console.log('=============== >  ready.js ');

