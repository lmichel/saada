jQuery.extend({

	FilterManagerControler: function(model, view){
		
		var vlist = {
				controlShowFilterManager: function(treepath){
					model.processShowFilterManager(treepath);
				},
				controlInitExisting: function(){
					model.processInitExisting();
				},
				controlSpeFieldEvent: function(uidraggable){
					model.processSpeFieldEvent(uidraggable);
				},
				controlNativeEvent: function(uidraggable){
					model.processNativeEvent(uidraggable);
				},
				controlRelationsEvent: function(uidraggable){
					model.processRelationsEvent(uidraggable);
				},
				controlSpeFieldRemoval: function(sfname){
					model.processSpeFieldRemove(sfname);
				},
				controlNativeRemoval: function(nname){
					model.processNativeRemove(nname);
				},
				controlRelationRemoval: function(rname){
					model.processRelationRemove(rname);
				},
				controlSaveFilter: function(){
					model.processSaveFilter();
				},
				controlSaveUserFilter: function(){
					model.processSaveUserFilter();
				},
				controlApplyToAllColl: function(){
					model.processApplyToAllColl();
				},
				controlFilterPreview: function(){
					model.processShowFilterPreview();
				},
				controlResetFilter: function(){
					model.processResetFilter();
				},
				controlResetAll: function(){
					model.processResetAll();
				}
		}

		view.addListener(vlist);
		

		var mlist = {
					
				filterManagerIsReady: function (jsdata, speFields, attributesHandlers, relations) {
					view.showFilterManager(jsdata, speFields, attributesHandlers, relations);
				},
					
				notifySpeFieldsUpdate: function (index, sfname) {
					view.updateSpeFields(index, sfname);
				},
					
				notifyNativesUpdate: function (nname, nnameorg) {
					view.updateNatives(nname, nnameorg);
				},
					
				notifyRelationsUpdate: function (index, rname) {
					view.updateRelations(index, rname);
				},
				
				notifyPreviewIsReady: function (filter, collection, category) {
					view.showFilterPreview(filter, collection, category);
				}
		}

		model.addListener(mlist);
	}
});