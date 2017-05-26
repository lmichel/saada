jQuery.extend({

	PatternControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlEnterEvent : function(operator, operand){
					model.processEnterEvent(operator, operand);
				},
				controlAttributeEvent: function(uidraggable){
					model.processAttributeEvent(uidraggable);
				},
				controlRemoveConstRef: function(ahname){
					model.processRemoveConstRef(ahname);
				},
				controlSelectClassEvent: function(classe){
					model.processSelectClassEvent(classe);
				},
				getADQL: function() {
					return model.updateQuery();
				},
				getRelationName: function() {
					return model.getRelationName();
				},
				removeAllConstRef: function() {
					return model.processRemoveAllConstRef();
				}

		}
		view.addListener(vlist);

		var mlist = {
				isInit : function(relation, collAttributesHandlers, classAttributesHandlers, jsoncpclasses){
					view.initForm(relation, collAttributesHandlers, classAttributesHandlers, jsoncpclasses);
				},
				isFormReset: function(relation, collAttributesHandlers, classAttributesHandlers, jsoncpclasses){
					view.formReset();
				},
				isClassSelected: function(collAttributesHandlers, classAttributesHandlers){
					view.classSelected(collAttributesHandlers, classAttributesHandlers);
				}
		}
		model.addListener(mlist);

	}
});
