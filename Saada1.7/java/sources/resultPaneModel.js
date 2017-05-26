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
		var histo_ptr = -1;
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
		};
		/*
		 * Event processing
		 */
		this.processShowRecord= function(oid, panelToOpen){
			/*
			 * Look for the page in the cache
			 */
			for( var i=(histo.length-1) ; i>=0; i-- ) {
				Out.info(i + " " + histo[i]);
				if( histo[i].oid == oid ) {
					Out.info("----------------------------------found " + oid + " in cache");
					//Out.info(histo[i].html);
//					$('#detaildiv').html(histo[i].html);
//					$('#detaildiv').modal({onClose: function() {resultPaneView.fireStoreHistory(oid);$.modal.close();}});
					Modalinfo.dataPanel("Histo", histo[i].html, function() {resultPaneView.fireStoreHistory(oid);});
					histo_ptr = i;
					that.notifyHistoRank();
					Out.info("----------------------------------");
					return;
				}
			}
			/*
			 * Build a new page is nothing found in the cache
			 */
			Processing.show();
			$.getJSON("getobject", {oid: oid }, function(jsdata) {
				Processing.hide("Get object detail");
				if( Processing.jsonError(jsdata, "") ) {
					return;
				} else {
					that.notifyDetailLoaded(oid, jsdata, 'MaxRight', panelToOpen);
					Out.info("----------------------------------Load " + oid );
					histo_ptr = histo.length;
//					histo[histo.length] = {oid: oid, html: $('#detaildiv').html()};
					histo[histo.length] = {oid: oid, html: Modalinfo.getHtml()};					
					that.notifyHistoRank();
				}
			});
		};

		/**
		 * Display the detail of a catalogue source in the modal dialog
		 * @param oid : Saada OID of the record to display
		 * @param panelToOpen : not used here
		 */
		this.processShowCatalogueSource= function(oid, panelToOpen){
			/*
			 * Look for the page in the cache
			 */
			for( var i=(histo.length-1) ; i>=0; i-- ) {
				Out.info(i + " " + histo[i]);
				if( histo[i].oid == oid ) {
					Out.info("----------------------------------found " + oid + " in cache " + histo[i].html.length);
					//Out.info(histo[i].html);
//					$('#detaildiv').html(histo[i].html);
//					$('#detaildiv').modal({onClose: function() {resultPaneView.fireStoreHistory(oid);$.modal.close();}});
					Modalinfo.dataPanel('Histo', histo[i].html, function() {resultPaneView.fireStoreHistory(oid);});
					histo_ptr = i;
					that.notifyHistoRank();
					Out.info("----------------------------------");
					return;
				}
			}
			/*
			 * Build a new page is nothing found in the cache
			 */
			Processing.show("Fetching catalogue source data");
			$.getJSON("getgallery", {oid: oid }, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "") ) {
					return;
				} else {
					that.notifyCatalogueDetailLoaded(oid, jsdata, 'MaxRight', panelToOpen);
					Out.info("----------------------------------Load " + oid );
					histo_ptr = histo.length;
//					histo[histo.length] = {oid: oid, html: $('#detaildiv').html()};
					histo[histo.length] = {oid: oid, html: Modalinfo.getHtml()};
					that.notifyHistoRank();
				}
			});
		};

		/**
		 * Display the detail of a ACDS source in the modal dialog
		 * @param oid : Saada OID of the record to display
		 * @param panelToOpen : not used here
		 */
		this.processShowAcdsSource= function(oid, panelToOpen){
			Processing.show("Fetching archial sources");
			$.getJSON("getacdssrcdetail", {oid: oid }, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "") ) {
					return;
				} else {
					that.notifyAcdsDetailLoaded(oid, jsdata, 'MaxRight', panelToOpen);
					Out.info("----------------------------------Load " + oid );
					histo_ptr = histo.length;
//					histo[histo.length] = {oid: oid, html: $('#detaildiv').html()};
					histo[histo.length] = {oid: oid, html: Modalinfo.getHtml()};
					that.notifyHistoRank();
				}
			});
		};

		this.processStoreHistory = function(oid) {
			Out.info("------------ store " + histo_ptr + "-----------------------"); 			
			//Out.info($('#detaildiv').html()); 			
			//histo[histo_ptr] = {oid: oid, html: $('#detaildiv').html()};
			histo[histo.length] = {oid: oid, html: Modalinfo.getHtml()};
			Out.info("------------ stored " + histo[histo_ptr].oid + "----------------------"); 						
		};
		this.processPreviousRecord= function(){
			if( histo_ptr <= 0 ) {
				Modalinfo.info("Start of the historic reached");
				return;
			}
			/*
			 * Store the current page layout
			 */
			Out.info("------------ previous " + histo_ptr + " " + histo.length + "-----------------------"); 			
			that.processStoreHistory(histo[histo_ptr].oid);
			histo_ptr --;
			Out.info("------------ restore " + histo_ptr + " " + histo[histo_ptr].oid + "-----------------------"); 	
			//Out.info(histo[histo_ptr].html);
			//$('#detaildiv').html(histo[histo_ptr].html);
			Modalinfo.dataPanel('Histo', histo[histo_ptr].html);
			that.notifyHistoRank();
			return;
		};
		this.processNextRecord= function(){
			if( histo_ptr >= (histo.length - 1) ) {
				Modalinfo.info("End of the historic reached");
				return;
			}
			/*
			 * Store the current page layout
			 */
			that.processStoreHistory(histo[histo_ptr].oid);
			histo_ptr ++;
			//$('#detaildiv').html(histo[histo_ptr].html);
			Modalinfo.dataPanel('Histo', histo[histo_ptr].html);
			that.notifyHistoRank();
			return;

		};		
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
		this.processSaadaQLQueryEvent = function(query,idFormulaire,select){
			current_query = query;
			if( !query.startsWith('Select') ) {
				Modalinfo.info( "\"" + query + "\" SaadaQL query does not look very good !");
			} else if (select=='Catalogue') {
				Processing.show("Process query on 3XMM catalogue");
				$.ajax({
					url: "runquerycat",
					dataType: 'json',
					timeout: 100000,
					data : { query : query },
					type: 'GET',
					error: function(XMLHttpRequest, textStatus, errorThrown){
						Processing.hide();
						Modalinfo.info(textStatus, "Query Error");
					},
					success: function(json){					
						Processing.hide();
						if( Processing.jsonError(json, "") ) {
							return;
						} else {
							dataJSONObject = json;
							//$('#data_processing').attr("visiblity", "hidden");
							$('#'+idFormulaire).dialog('close');
							that.notifyTableInitDone(dataJSONObject, query);
						}
					}
				});
			} else{
				Processing.show("Process query on merged catalogue");
				$.ajax({
					url: "runquerymerg",
					dataType: 'json',
					timeout: 100000,
					data : { query : query },
					type: 'GET',
					error: function(XMLHttpRequest, textStatus, errorThrown){
						Processing.hide();
						Modalinfo.info(textStatus, "Query Error");
					},
					success: function(json){			
						Processing.hide();
						if( Processing.jsonError(json, "") ) {
							return;
						} else {
							dataJSONObject = json;
							//$('#data_processing').attr("visiblity", "hidden");
							$('#'+idFormulaire).dialog('close');
							that.notifyTableInitDone(dataJSONObject, query);
						}
					}
				});
			}
		};
		this.notifyTableInitDone = function(dataJSONObject, query){
			$.each(listeners, function(i){
				listeners[i].tableIsInit(dataJSONObject, query);
			});
		};
		this.notifyDetailLoaded= function(oid, jsdata, limit, panelToOpen){
			$.each(listeners, function(i){
				listeners[i].detailIsLoaded(oid, jsdata, limit, panelToOpen);
			});
		};
		this.notifyCatalogueDetailLoaded= function(oid, jsdata, limit, panelToOpen){
			$.each(listeners, function(i){
				listeners[i].catalogueDetailIsLoaded(oid, jsdata, limit, panelToOpen);
			});
		};
		this. notifyAcdsDetailLoaded= function(oid, jsdata, limit, panelToOpen){
			$.each(listeners, function(i){
				listeners[i].acdsDetailIsLoaded(oid, jsdata, limit, panelToOpen);
			});
		};
		this.notifyCounterpartsLoaded= function(jsdata){
			$.each(listeners, function(i){
				listeners[i].counterpartsAreLoaded(jsdata);
			});
		};					
		this.notifyHistoRank= function(){
			$.each(listeners, function(i){
				listeners[i].histoRank(histo.length, histo_ptr);
			});
		};					


	}
});
