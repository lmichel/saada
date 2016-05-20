jQuery.extend({

	SaadaQLControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlTreeNodeEvent : function(andsubmit, newTreeNode){
					model.processTreeNodeEvent(andsubmit, newTreeNode);
				},
				controlAttributeEvent: function(uidraggable){
					model.processAttributeEvent(uidraggable);
				},
				controlOrderByEvent: function(uidraggable){
					model.processOrderByEvent(uidraggable);
				},
				controlSortColumnEvent: function(nameattr, sens){
					model.sortColumn(nameattr, sens);
				},
				controlOrderByParameters: function() {
					return model.getOrderByParameters();
				},
				controlOIDTableEvent: function(oidtable){
					model.processOIDTableEvent(oidtable);
				},
				controlUCDEvent: function(uidraggable){
					model.processUCDEvent(uidraggable);
				},
				controlInputCoord: function(coord, radius){
					model.processInputCoord(coord, radius);
				},
				controlUpdateQueryEvent: function(){
					model.updateQuery();
				},
				controlSelectRelationEvent: function(relation){
					model.processSelectRelation(relation);
				},
				controlHisto: function(direction){
					model.processHisto(direction);
				},
				controlStoreHisto: function(query){
					model.processStoreHisto(query);
				},
				controlDisplayHisto: function() {
					model.displayHisto();
				}, 
				controlTitleEvent: function() {
					model.setTitle();
				}, 
		}
		view.addListener(vlist);

		var mlist = {
				isInit : function(attributesHandlers, relations, queriableUCDs){
					view.initForm(attributesHandlers, relations, queriableUCDs);
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
