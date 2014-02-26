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
