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
				controlCollNativeEvent: function(uidraggable){
					model.processCollNativeEvent(uidraggable);
				},
				controlClassNativeEvent: function(uidraggable){
					model.processClassNativeEvent(uidraggable);
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
				controlAllNativeRemoval: function(){
					model.processAllNativeRemove();
				},
				controlAllClassNativeRemoval: function(nnam){
					model.processAllClassNativeRemove();
				},
				controlRelationRemoval: function(rname){
					model.processRelationRemove(rname);
				},
				controlAllRelationRemoval: function(){
					model.processAllRelationRemove();
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
				controlAnyCollAtt: function(any){
					model.processAnyCollAtt(any);
				},
				controlAnyClassAtt: function(any){
					model.processAnyClassAtt(any);
				},
				controlAnyRelation: function(any){
					model.processAnyRelation(any);
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
		};

		view.addListener(vlist);
		

		var mlist = {
					
				filterManagerIsReady: function (jsdata, speFields, collAttributesHandlers, classAttributeHandlers, relations) {
					view.showFilterManager(jsdata, speFields, collAttributesHandlers, classAttributeHandlers, relations);
				},
					
				notifySpeFieldsUpdate: function (index, sfname) {
					view.updateSpeFields(index, sfname);
				},
					
				notifyCollNativesUpdate: function (nname, nnameorg) {
					view.updateCollNatives(nname, nnameorg);
				},
					
				notifyClassNativesUpdate: function (nname, nnameorg) {
					view.updateClassNatives(nname, nnameorg);
				},
				notifyRelationsUpdate: function (index, rname) {
					view.updateRelations(index, rname);
				},
				
				notifyPreviewIsReady: function (filter, collection, category) {
					view.showFilterPreview(filter, collection, category);
				}
		};

		model.addListener(mlist);
	}
});