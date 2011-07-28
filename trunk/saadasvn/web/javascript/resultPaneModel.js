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
				alert( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: processTreeNodeEvent.js)");
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
				alert( "\"" + query + "\" SaadaQL query does not look very good !");
			}
			else {
				$('#data_processing').attr("visiblity", "visible");
				showProcessingDialog();
				$.ajax({
					url: "runquery",
					dataType: 'json',
					timeout: 100000,
					data : { query : query },
					type: 'GET',
					error: function(XMLHttpRequest, textStatus, errorThrown){
						hideProcessingDialog();
						$('#data_processing').attr("visiblity", "hidden");
						that.notifyJobFailed(textStatus);
					},
					success: function(json){			
						hideProcessingDialog();
//						alert(treePath)
						dataJSONObject = json;
						$('#data_processing').attr("visiblity", "hidden");
						$('#showquerymeta').unbind('click');
						$('#showquerymeta').click(function(){resultPaneView.fireShowMeta();});
						that.notifyTableInitDone(dataJSONObject, query);
						setTitlePath(treePath);
					}
				});
			}
		}

		this.processShowRecord= function(oid){
			var jsdata ="";
			showProcessingDialog();
			$.getJSON("getobject", {oid: oid }, function(data) {
				hideProcessingDialog();
				if( processJsonError(data, "") ) {
					return;
				}

				else {
					jsdata = data;
					histo[histo.length] = oid;
					histo_ptr = histo.length - 1;
					var limit = 'MaxRight';
					if( histo.length == 1  ) limit = 'NoHisto';
					that.notifyDetailLoaded(oid, jsdata, limit);
				}
			});
		}

		this.processShowMeta= function(){
			var jsdata ="";
			showProcessingDialog();
			var tp;
			if( treePath.length == 3 ) {
				tp = treePath[2];
			}
			else {
				tp = treePath[0] + "." + treePath[1];
			}
			$.getJSON("getmeta", {query: "aharray", name:tp }, function(data) {
				hideProcessingDialog();
				if( processJsonError(data, "get metadata") ) {
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
			showProcessingDialog();
			var tp;
			if( treepath.length == 3 ) {
				tp = treepath[2];
			}
			else {
				tp = treepath[0] + "." + treepath[1];
			}
			$.getJSON("getmeta", {query: "aharray", name:tp }, function(data) {
				hideProcessingDialog();
				if( processJsonError(data, "get attribute handlers") ) {
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
			showProcessingDialog();
			$.getJSON("getobject", {target: "sources", oid: oid }, function(data) {
				hideProcessingDialog();
				if( processJsonError(data, "get catalogue sources") ) {
					return;
				}
				else {
					jsdata = data;
					saadaqlView.fireTreeNodeEvent(jsdata.treepath.split('.'), false, jsdata.query);	
					sapView.fireTreeNodeEvent(jsdata.treepath.split('.'));	
					tapView.fireTreeNodeEvent(jsdata.treepath.split('.'));	
					setTitlePath(jsdata.treepath.split('.'));
					that.notifyTableInitDone(jsdata);	
					/*
					 * should be done in the async callback intiated by saadaqlView.fireTreeNodeEvent
					 */
					setTimeout("saadaqlView.fireOIDTableEvent(\"" + oid + "\"); ", 2000);
					current_query = jsdata.query;
				}
			});
		}

		this.processShowSimbad= function(coord){
			window.open("simbad?coord=" + escape(coord), "Simbad");
		}

		this.processPreviousRecord= function(){
			var jsdata ="";
			if( histo_ptr <= 0 ) {
				alert("end of the historic reached");
				return;
			}
			histo_ptr --;

			var oid = histo[histo_ptr];
			showProcessingDialog();
			if( oid.match(/^meta:\s*/)) {
				$.getJSON("getmeta", {query: "aharray", name:oid.split(' ')[1] }, function(data) {
					hideProcessingDialog();
					if( processJsonError(data, "get attribute handlers") ) {
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
					hideProcessingDialog();
					if( processJsonError(data, "get object") ) {
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
				alert("end of the historic reached");
				return;
			}
			histo_ptr ++;
			var oid = histo[histo_ptr];
			showProcessingDialog();
			if( oid.match(/^meta:\s*/)) {
				$.getJSON("getmeta", {query: "aharray", name:oid.split(' ')[1] }, function(data) {
					hideProcessingDialog();
					if( processJsonError(data, "get attribute handlers") ) {
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
					hideProcessingDialog();
					if( processJsonError(data, "get object") ) {
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
			showProcessingDialog();
			$.getJSON("getobject", param , function(data) {
				hideProcessingDialog();
				if( processJsonError(data, "get object") ) {
					return;
				}
				else {
					jsdata = data;
					that.notifyCounterpartsLoaded(jsdata);
				}
			});
		}
		this.downloadVOTable = function() {
			/*
			 * The mode (TAP/SaadaQL is detected by anaylsing the title
			 */
			var titlepath = $('#titlepath').html().split('&gt;');
			if( titlepath.length == 3 && titlepath[1] == 'Job' ) {
				tapView.fireDownloadVotable(titlepath[2]);
			}
			else {
				var url = "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=votable";
				window.open(url, 'DL VOTable');
			}
		}
		this.downloadFITS = function() {
			var url = "getqueryreport?query=" + escape(current_query) + "&protocol=auto&format=fits";
			window.open(url, 'DL FITS');
		}
		this.downloadZip = function() {
			if( that.zipJob != null ) {
				that.zipJob.kill();
			}
			var limit = 10000;
			if( $("#qlimit").val().match(/^[0-9]*$/) ) {
				limit = $("#qlimit").val();
			}
			logMsg("zipasync");

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
					alert("Error: " + textStatus);
				},
				dataType: "xml"
			});
		}
		this.checkZipCompleted = function(jobid) {
			if( that.zipJob == null ) {
				alert("Error: Job seems to get lost ??");			
				$(".zip").css("background-image", "url(images/zip_32.png)");								
			}
			else {
				$.get("datapack/zipper/" + that.zipJob.jobId
						, "FORMAT=xml"
						, function(data) {
					that.zipJob.init(data);
					var phase = that.zipJob.phase;
					if( phase == 'COMPLETED') {
						logMsg(that.zipJob.results);
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
						logMsg("executing");
						setTimeout("resultPaneView.fireCheckZipCompleted(\"cocu\");", 1000);
					}
					else {
						alert("Zip job is in an unexpected status: " + phase + ": canceled");
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
				tapView.fireSampBroadcast(titlepath[2]);
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
					alert("Samp messages are not  implemented for this data category.")
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
		this.notifyDetailLoaded= function(oid, jsdata, limit){
			$.each(listeners, function(i){
				listeners[i].detailIsLoaded(oid, jsdata, limit);
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
