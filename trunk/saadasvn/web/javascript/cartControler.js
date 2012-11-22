jQuery.extend({

	CartControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
				controlAddJobResult : function(nodekey, jobid){
					model.addJobResult(nodekey, jobid);
				},
				controlRemoveJobResult : function(nodekey, jobid){
					model.removeJobResult(nodekey, jobid);
				},
				controlAddUrl : function(name, oid){
					model.addUrl(name, oid);
				},
				controlRemoveUrl : function(nodekey, url){
					model.removeUrl(nodekey, url);
				},
				controlOpenCart : function(){
					model.notifyCartOpen();
				},
				controleCleanCart: function(tokens){
					model.cleanCart(tokens);
				},
				controlStartArchiveBuilding: function() {
					model.startArchiveBuilding();
				},
				controlKillArchiveBuilding: function() {
					model.killArchiveBuilding();
				},
				controlGetJobPhase: function() {
					return model.getJobPhase();
				},
				controlArchiveDownload: function() {
					return model.archiveDownload();
				},
				controlChangeName: function(nodekey, dataType, rowNum, newName) {
					model.changeName(nodekey, dataType, rowNum, newName);				
				},
				controlSetRelations: function(nodekey, dataType, uri, checked) {
					model.setRelations(nodekey, dataType, uri, checked);				
				},
				controlDelegateCartDownload: function() {
					model.delegateCartDownload();				
				},
				controlResetZipjob: function() {
					model.resetZipjob();				
				}
		};
		view.addListener(vlist);

		var mlist = {
				isCartCleaned : function(cartData){
					view.setTableDiv(cartData);
				},
				isInit : function(cartData){
					view.initForm(cartData);
				}
		};
		model.addListener(mlist);

	}
});
