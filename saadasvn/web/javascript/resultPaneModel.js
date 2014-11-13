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
		this.setTreePath = function(treepath){
			treePath = treepath;
		}
		this.processSaadaQLTreeNodeEvent = function(treepath){
			var query;
			if( treepath.length == 3 ){
				query = "Select " + treepath[1] + ' From ' + treepath[2] + ' In ' + treepath[0];
			}
			else if ( treepath.length == 2 ){
				collection = treepath[0];
				query = "Select " + treepath[1] + ' From * In ' + treepath[0];
			}
			else {
				Modalinfo.info( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: processTreeNodeEvent.js)");
				return;
			}
			if( $("#qlimit").val().match(/^[0-9]+$/) ) {
				query += '\nLimit ' + $("#qlimit").val();
			}

			that.processSaadaQLQueryEvent(query);
		}

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
						setTitlePath(treePath);
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
