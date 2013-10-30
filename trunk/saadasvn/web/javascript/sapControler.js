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
				controlSIAPCapabilityEvent: function(treepath){
					model.processSIAPCapabilityEvent(treepath);
				},
				controlSIAPRegistryEvent: function(treepath){
					model.processSIAPRegistryEvent(treepath);
				},
				controlSIAPGluEvent: function(treepath){
					model.processSIAPGluEvent(treepath);
				},

				controlSSAPQueryEvent: function(treepath){
					model.processSSAPQueryEvent(treepath);
				},
				controlSSAPSampEvent: function(treepath){
					model.processSSAPSampEvent(treepath);
				},
				controlSSAPCapabilityEvent: function(treepath){
					model.processSSAPCapabilityEvent(treepath);
				},
				controlSSAPRegistryEvent: function(treepath){
					model.processSSAPRegistryEvent(treepath);
				},
				controlSSAPGluEvent: function(treepath){
					model.processSSAPGluEvent(treepath);
				},

				controlCSQueryEvent: function(treepath){
					model.processCSQueryEvent(treepath);
				},
				controlCSSampEvent: function(treepath){
					model.processCSSampEvent(treepath);
				},
				controlCSCapabilityEvent: function(treepath){
					model.processCSCapabilityEvent(treepath);
				},
				controlCSRegistryEvent: function(treepath){
					model.processCSRegistryEvent(treepath);
				},
				controlCSGluEvent: function(treepath){
					model.processCSGluEvent(treepath);
				}


		}
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
		}

		model.addListener(mlist);
	}
});
