
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
	 */
	var getTableAtt =  function(dataTreePath /* instance of DataTreePath */, handler){
		/*
		 * Query a new node
		 */
		if( isAjaxing ) {
			setTimeout(function(){MetadataSource.getTableAtt(dataTreePath, handler);}, 100);
			return;
		} 
		console.log("Looking for " + JSON.stringify(dataTreePath) + " " + isAjaxing);
		if( !cache || !cache[dataTreePath.key] ) {
			buffer = {};
			buffer.dataTreePath = dataTreePath;		
			buffer.hamap        = new Array();
			buffer.targets      = new Array();
			if( getMetaTableUrl != null ) {
				isAjaxing = true;
				Out.info("Connect new node  " + dataTreePath.nodekey);

				var params = {jsessionid: this.sessionID, nodekey: buffer.dataTreePath.nodekey, schema:buffer.dataTreePath.schema, table:buffer.dataTreePath.tableorg};
				Processing.show("Get column description of table " + buffer.key + " (constqeditor)");
				$.getJSON(getMetaTableUrl
						, params
						, function(data)  {
					Processing.hide();
					if( !Processing.jsonError(data)) {
						buffer.hamap = buildAttMap(data);
						if(getJoinedTablesUrl) {
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
	pblc.joinedTables = function(dataTreePath){return(!cache)?null: cache[dataTreePath.nodekey].targets;};
	pblc.init = init;
	pblc.getTableAtt = getTableAtt;
	pblc.getJoinedTables = getJoinedTables;
	pblc.getUserGoodie = getUserGoodie;
	pblc.setJobColumns = setJobColumns;
	pblc.test = test;
	return pblc;	
}();

/**
 * Modalinfo extension opening a popup with a Simbad summary for the given solution
 */
Modalinfo.simbad = function (pos) {
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
Modalinfo.uploadForm = function (title, url, description, handler, beforeHandler, extraParamers) {
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
};

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
	pblc.posConstraintEditor = posConstraintEditor;
	pblc.simplePosConstraintEditor = simplePosConstraintEditor;
	pblc.attachedDataEditor = attachedDataEditor;
	pblc.vizierKeywordsEditor = vizierKeywordsEditor;
	pblc.catalogueConstraintEditor = catalogueConstraintEditor;
	pblc.crossidConstraintEditor = crossidConstraintEditor;
	pblc.tapConstraintEditor = tapConstraintEditor;
	pblc.tapColumnSelector = tapColumnSelector;
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
	 * exports
	 */
	var pblc = {};
	pblc.startBrowser = startBrowser;
	return pblc;
}();
