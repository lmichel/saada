jQuery.extend({

	TapController: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlTreeNodeEvent : function(treepath, andsubmit, default_query){
					model.processTreeNodeEvent(treepath, andsubmit, default_query);
				},
				controlAttributeEvent: function(uidraggable){
					model.processAttributeEvent(uidraggable);
				},
				controlSelectEvent: function(uidraggable){
					model.processSelectEvent(uidraggable);
				},
				controlInputCoord: function(coord, radius, mode){
					model.processInputCoord(coord, radius, mode);
				},
				controlAlphaEvent: function(uidraggable){
					model.processAlphaEvent(uidraggable);
				},
				controlDeltaEvent: function(uidraggable){
					model.processDeltaEvent(uidraggable);
				},
				controlUpdateQueryEvent: function(){
					model.updateQuery();
				},
				controlSubmitQueryEvent: function(){
					model.submitQuery();
				},
				controlRefreshJobList: function(){
					model.refreshJobList();
				},
				controlJobAction: function(jid){
					model.processJobAction(jid);
				},
				controlDownloadVotable: function(jid){
					model.downloadVotable(jid);
				},
				controlCheckJobCompleted: function(jid){
					model.checkJobCompleted(jid);
				},
				controlSampBroadcast: function(jid){
					model.sampBroadcast(jid);
				}
		}
		view.addListener(vlist);

		var mlist = {
				isInit : function(attributesHandlers, selectAttributesHandlers){
					view.initForm(attributesHandlers, selectAttributesHandlers);
				},
				coordDone : function(key, constr){
					view.coordDone(key, constr);
				},
				queryUpdated : function(query){
					view.queryUpdated(query);
				},
				newJob: function(jobview){
					view.jobView(jobview);
				}
		}

		model.addListener(mlist);
	}
});
