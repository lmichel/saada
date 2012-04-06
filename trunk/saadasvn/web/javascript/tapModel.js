jQuery.extend({

	TapModel: /**
	 * @param pmodel
	 */
		/**
		 * @param pmodel
		 */
		function(pmodel){
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
		var attributesHandlers = new Array();
		var selectAttributesHandlers = new Array();
		var relations = new Array();
		var editors = new Array();
		var selects = new Array();
		var alphakw ;
		var deltakw ;
		var const_key = 1;
		var table ;
		var storedTreepath = new Array();

		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		}
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(treepath, andsubmit, default_query){
			var jsondata;
			var params;
			var collMode = false;
			storedTreepath = treepath;
			/*
			 * Cases CLASS
			 */
			if( treepath.length == 1 ){
				table = treepath[0];
				params = {query: "ah", name:  table };
			}
			else if( treepath.length == 3 ){
				table = treepath[2];
				params = {query: "ah", name:  table };
			}
			/*
			 * case COLL
			 */
			else if ( treepath.length == 2 ){
				var category = treepath[1];
				table = treepath[0] + "_" + category;
				params = {query: "ah", name:  treepath[0] + '.' +category };
				collMode = true;
			}
			else {
				alert( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: sadaqlModel.js)");
				return;
			}
			showProcessingDialog();
			$.getJSON("getmeta", params, function(jsondata) {
				hideProcessingDialog();
				if( processJsonError(jsondata, "Cannot get meta data") ) {
					return;
				}
				editors = new Array();
				selects = new Array();
				selectattributesHandlers = new Array();
				attributesHandlers = new Array();
				alphakw = "";
				deltakw = "";
				for( i=0 ; i<jsondata.attributes.length ; i++ ) {
					an = jsondata.attributes[i].nameattr;
					/*
					 * Avoid to mix class/collection access because the ADQL join can be very slow
					 */
					if( (collMode && !an.startsWith('_') )
							|| (!collMode && (an.startsWith('_') || an == 'namesaada' || an == 'oidsaada') ) ) {
						attributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
					}
				}
				selectAttributesHandlers = new Array();
				for( i=0 ; i<jsondata.attributes.length ; i++ ) {
					an = jsondata.attributes[i].nameattr;
					/*
					 * Avoid to mix class/collection access because the ADQL join can be very slow
					 */
					if( (collMode && !an.startsWith('_') )
							|| (!collMode && (an.startsWith('_') || an == 'namesaada' || an == 'oidsaada') ) ) {
							selectAttributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
				}
			}
			that.notifyInitDone();		
			if( default_query == null || default_query == "") {
				that.notifyQueryUpdated("SELECT * \n FROM " + table + ";");
			}
			else {
				that.notifyQueryUpdated(default_query);				
			}
			if( andsubmit ) {
				that.submitQuery();
			}
		});
	}

this.processSelectEvent= function(uidraggable){
	var kwname = uidraggable.find(".item").text().split(' ')[0];
	var ah = selectAttributesHandlers[kwname];
	var m = new $.KWConstraintModel(true, { "nameorg" : ah.nameorg
		, "nameattr" : ah.nameattr
		, "type" : "Select"
			, "ucd" : ah.ucd
			, "utype" : ah.utype
			, "unit" : ah.unit
			, "comment" : ah.comment}
	, this);
	var div_key = "kw" +  const_key;
	var v = new $.KWConstraintView(div_key, 'tapselectlist');
	selects[div_key] =  new $.KWConstraintControler(m, v);
	m.notifyInitDone();
	const_key++;
}

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
	var v = new $.KWConstraintView(div_key, 'tapconstraintlist');
	editors[div_key] =  new $.KWConstraintControler(m, v);
	that.updateQuery();
	m.notifyInitDone();
	const_key++;
}


this.processInputCoord= function(coord, radius, mode){
	var frame = 'J2000,ICRS';
	that.notifyCoordDone("coo" +  const_key, 'isInCircle("' + coord + '", ' + radius + ', ' + frame + ')');
	that.updateQuery();
	var alphaname = $('#kwalpha_name').html();
	var deltaname = $('#kwdelta_name').html();
	if( alphaname == "" || deltaname.length == "" ) {
		alert('Give one KW for both alpha and delta');
		return;
	}
	var coords = $('#tapcoordval').val().split(' ');
	if( coords.length != 2 || isNaN(coords[0]) || isNaN(coords[1])) {
		alert('Both coordinates must be given in degrees');
		return;				
	}
	var rs = $('#tapradiusval').val();
	if( isNaN(rs) ){
		alert('Radius/Size must be given in degrees');
		return;								
	}
	var box_summary = coords[0] + "," + coords[1] + "," + rs
	var first = true;
	for( k in editors ) {
		first = false;
		break;
	}
	var m = new $.KWConstraintModel(first, { "nameorg" : alphaname + " " + deltaname
		, "nameattr" : alphaname + " " + deltaname
		, "type" : "ADQLPos"
			, "ucd" : "adql.coor.columns"
				, "utype" : ""
					, "unit" : "deg"
						, "comment" : "Virtual Column"}
	, this, box_summary);

	var div_key = "kw" +  const_key;
	var v = new $.KWConstraintView(div_key, 'tapconstraintlist');
	editors[div_key] =  new $.KWConstraintControler(m, v);
	that.updateQuery();
	m.notifyInitDone();
	const_key++;

}

this.processAlphaEvent= function(uidraggable){
	var kwname = uidraggable.find(".item").text().split(' ')[0];
	var ah = selectAttributesHandlers[kwname];
	var m = new $.KWConstraintModel(true, { "nameorg" : ah.nameorg
		, "nameattr" : ah.nameattr
		, "type" : "Select"
			, "ucd" : ah.ucd
			, "utype" : ah.utype
			, "unit" : ah.unit
			, "comment" : ah.comment}
	, this, '');
	var div_key = "kwalpha";
	var v = new $.KWConstraintView(div_key, 'tapalpha');
	alphakw =  new $.KWConstraintControler(m, v);
	m.notifyInitDone();

}

this.processDeltaEvent= function(uidraggable){
	var kwname = uidraggable.find(".item").text().split(' ')[0];
	var ah = selectAttributesHandlers[kwname];
	var m = new $.KWConstraintModel(true, { "nameorg" : ah.nameorg
		, "nameattr" : ah.nameattr
		, "type" : "Select"
			, "ucd" : ah.ucd
			, "utype" : ah.utype
			, "unit" : ah.unit
			, "comment" : ah.comment}
	, this, '');
	var div_key = "kwdelta";
	var v = new $.KWConstraintView(div_key, 'tapdelta');
	deltakw =  new $.KWConstraintControler(m, v);		
	m.notifyInitDone();

}


this.updateQuery = function() {
	var query = "SELECT ";
	var cq = "";

	cq = "";
	$("#tapselectlist div").each(function() {
		if( cq.length > 0 ) cq += " , ";
		cq +=  '    ' + selects[$(this).attr('id')].getADQL(true) ;
		if( cq.length > 50 ) cq += '\n';
	}); 
	if( cq.length > 0 ) {
		query +=  cq ;
	}
	else {
		query += '*';
	}
	query += "\nFROM " + table;

	cq = "";
	$("#tapconstraintlist div").each(function() {
		cq +=  '    ' + editors[$(this).attr('id')].getADQL(true) ;
		if( cq.length > 50 ) cq += '\n';
	}); 
	if( cq.length > 0 ) {
		query += "\nWHERE \n" + cq + "";
	}

	that.notifyQueryUpdated(query);
}

this.submitQuery = function(){
	showProcessingDialogImmediately();
	var limit = 10000;
	if( $("#qlimit").val().match(/^[0-9]*$/) ) {
		limit = $("#qlimit").val();
	}
	$.post("tap/async"
			, {REQUEST: "doQuery", LANG: 'ADQL', FORMAT: 'json', PHASE: 'RUN', MAXREC: limit,QUERY: ($('#adqltext').val()) }
			, function(jsondata, status) {
				if( processJsonError(jsondata, "Cannot get jobs list") ) {
					return;
				}
				var params = {FORMAT: "json"};
				$.getJSON("tap/async", params, function(jsondata) {
					if( processJsonError(jsondata, "Cannot get jobs list") ) {
						return;
					}
					jv  = new $.JobView();
					var newJob = jsondata.jobs[jsondata.jobs.length - 1];
					jm = new $.JobModel(newJob);
					new $.JobControler(jm, jv);
					that.notifyNewJobs(jv);
					setTimeout("tapView.fireCheckJobCompleted(\"" + newJob.id + "\");", 5000);
				});
			});
}


this.checkJobCompleted = function( jid) {
	hideProcessingDialog();
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get summary of job " + jid) ) {
			return;
		}
		if( jsondata.phase == 'COMPLETED') {
			that.displayResult(jid);
			tapView.fireRefreshJobList();
		}
		else {
			alert("Job " + jid + " not completed: processed asynchronously");
		}			
	});					
}
this.refreshJobList= function() {
	$.getJSON("tap/async", {FORMAT: "json"}, function(jsondata) {
		hideProcessingDialog();
		if( processJsonError(jsondata, "Cannot get jobs list") ) {
			return;
		}
		if( jsondata.jobs != undefined) {
			//for( var i=(jsondata.jobs.length - 1) ; i>= 0 ; i--) {
			for( var i=0 ; i<jsondata.jobs.length ; i++) {
				jv  = new $.JobView();
				jm = new $.JobModel(jsondata.jobs[i]);
				new $.JobControler(jm, jv);
				that.notifyNewJobs(jv);
			}
		}
	});		
}

this.processJobAction= function(jid) {
	var val = $('#' + jid + "_actions").val(); 
	$('#' + jid + "_actions").val('Actions'); 
	if( val == 'Show Query') {	
		that.showQuery(jid);				
	}
	else if( val == 'Summary') {	
		that.showSummary(jid);				
	}
	else if( val == 'Display Result') {			
		that.displayResult(jid);
	}			
	else if( val == 'Edit Query' ) {
		that.editQuery(jid);
	}
	/*
	 * Down by the Vot button in the banner as for any download actions
	 */
	else if( val == 'Download Result') {					
		alert("Not implemented");	
	}
}
this.showQuery = function(jid) {
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get summary of job") ) {
			return;
		}
		var report  = "";
		report = jsondata.parameters.query.replace(/\\n/g,'\n            ')+ "\n";
		alert(report);
	});					
}
this.showSummary = function(jid) {
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get summary of job " + jid) ) {
			return;
		}
		var report  = "";
		report += "jobId            : " + jid + "\n";
		report += "owner            : " + jsondata.owner+ "\n";
		report += "phase            : " + jsondata.phase+ "\n";
		report += "startTime        : " + jsondata.startTime+ "\n";
		report += "endTime          : " + jsondata.endTime+ "\n";
		report += "executionDuration: " + jsondata.executionDuration+ "\n";
		report += "destruction      : " + jsondata.destruction+ "\n";
		report += "parameters " + "\n";
		report += "    query  : " + jsondata.parameters.query.replace(/\\n/g,'\n            ')+ "\n";
		report += "    request: " + jsondata.parameters.request+ "\n";
		report += "    format : " + jsondata.parameters.format+ "\n";
		report += "    lang   : " + jsondata.parameters.lang+ "\n";
		report += "    maxrec : " + jsondata.parameters.maxrec+ "\n";
		for( var i=0 ; i<jsondata.results.length ; i++ ) {
			report += "results #" + (i+1) + "\n";
			report += "    id  : " + jsondata.results[i].id+ "\n";
			report += "    type: " + jsondata.results[i].type+ "\n";
			report += "    href: " + jsondata.results[i].href+ "\n";
		}
		alert(report);
	});					
}
this.displayResult = function(jid) {
	showProcessingDialog();
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get result of job " + jid) ) {
			hideProcessingDialog();
			return;
		}
		for( var rep=0 ; rep<jsondata.results.length ; rep ++) {
			if( jsondata.results[rep].href.endsWith(".json") ) {
				$.getJSON(jsondata.results[rep].href, function(jsdata) {
					hideProcessingDialog();
					if( processJsonError(jsdata, "Cannot get data of job " + jid + " Possibly a cross domain issue: check the presence of the domain name in the url)") ) {
						return;
					}
					$('#showquerymeta').unbind('click');
					$('#showquerymeta').click(function(){alert("Not meta data available for ADQL queries (TAP)")});
					resultPaneView.showTapResult(jid, jsdata);
				});	
				hideProcessingDialog();
				return;
			}
		}
		alert("FATAL ERROR; Can only process TAP response in JSON format not found among " + jsondata.results.length);	
	});					
}
this.editQuery= function(jid) {
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get summary of job " + jid) ) {
			return;
		}
		/*
		 * Setup the TAP form with the JOB query
		 * including the treepath the job refers to
		 */
		var query = jsondata.parameters.query;
		var m = query.match(/FROM\s+(.*)[\s;]/);
		var pos = m[1].lastIndexOf('_');
		if( pos != -1 ) {
			treepath = [ m[1].substring(0, pos), m[1].substring(pos+1)];
		}
		else {
			treepath = m[1]
		}
		that.processTreeNodeEvent(treepath, false, jsondata.parameters.query);
	});					
}

this.downloadVotable= function(jid) {
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get result of job " + jid) ) {
			return;
		}
		for( var rep=0 ; rep<jsondata.results.length ; rep ++) {
			var href = jsondata.results[rep].href;
			if( href.endsWith(".xml") ) {
				window.open(href, 'DL VOTable');
				return;
			}
		}
		alert("FATAL ERROR: TAP response in VOTable format not found among " + jsondata.results.length);	
	});								
}

this.sampBroadcast= function(jid) {
	$.getJSON("tap/async/" + jid , function(jsondata) {
		if( processJsonError(jsondata, "Cannot get summary of job " + jid) ) {
			return;
		}
		for( var i=0 ; i<jsondata.results.length ; i++ ) {
			var url = jsondata.results[i].href;
			if( url.endsWith("xml")) {
				sampView.fireSendTapDownload(url);;
				return;
			}
		}
		alert("No result file looking like a VOTable, sorry.")
	});					

}

this.processRemoveFirstAndOr = function(key) {
	delete editors[key];
	for( var k in editors ) {
		editors[k].controlRemoveAndOr();
		break;
	}
}


/*
 * Listener notifications
 */
this.notifyInitDone = function(){
	$.each(listeners, function(i){
		listeners[i].isInit(attributesHandlers, selectAttributesHandlers);
	});
}
this.notifyCoordDone = function(key, constr){
	$.each(listeners, function(i){
		listeners[i].coordDone(key, constr);
	});
}
this.notifyQueryUpdated= function(query) {
	$.each(listeners, function(i){
		listeners[i].queryUpdated(query);
	});
}
this.notifyNewJobs= function(jobview) {
	$.each(listeners, function(i){
		listeners[i].newJob(jobview);
	});
}

}
});
