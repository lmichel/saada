QueryTextEditor_mvC = function(view, model){
	/**
	 * listen to the view
	 */
	var vlist = {
			controlGetQuery : function(){
				return model.getQuery();
			},
			controlAddConstraint : function(label, type, constraints, joins){
				model.processAddConstraint(label, type, constraints, joins);
			},
			controlDelConstraint : function(label, type){
				model.processDelConstraint(label, type);
			},
			controlSetTreePath: function(treePath){
				model.setTreePath(treePath);
			}
	};
	view.addListener(vlist);

	var mlist = {
			controlDisplayQuery : function(query){
				view.displayQuery(query);
			}
	};
	model.addListener(mlist);
};
