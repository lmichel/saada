jQuery.extend({

	JobModel: function(description){
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		var that = this;

		var id = description.id;
		var href = description.href;
		var phase = description.phase;
		var operator = ["Refresh", "get JSon result", "Show Query", "Summary"];			
		var actions = new Array();
		actions['COMPLETED'] = ["Actions", "Display Result", "Show Query", "Edit Query", "Summary"];
		actions['EXECUTING'] = ["Actions", "Kill", "Show Query", "Summary"];
		actions['PENDED']    = ["Actions", "Start", "Show Query", "Edit Query", "Summary"];
		actions['ERROR']     = ["Actions", "Show Query", "Edit Query", "Summary"];
		this.addListener = function(list){
			listeners.push(list);
		}
		
		this.initForm = function() {
			that.notifyIsInit(id, phase)
		}
		this.notifyIsInit = function(id, phase) {
			$.each(listeners, function(i){
				listeners[i].isInit(id, phase, actions[phase]);
			});
		}
	}
});