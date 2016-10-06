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
