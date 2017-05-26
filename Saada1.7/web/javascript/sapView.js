jQuery.extend({

	SapView: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		this.fireTreeNodeEvent = function(treepath){
			$.each(listeners, function(i){
				listeners[i].controlTreeNodeEvent(treepath);
			});
		};
		this.fireSubmitQueryEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPQueryEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPQueryEvent();
				});	
			}
			// CS
			else if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSQueryEvent();
				});				
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		this.fireSubmitSampEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPSampEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPSampEvent();
				});	
			}
			// CS
			else if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSSampEvent();
				});				
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};

		this.fireDisplayHisto = function(){
			var result = '';
			result += '<a id="qhistoleft" title="Previous query" class="shaded histoleft  shaded" onclick="return false;"></a>';
			result += '<a id="qhistoright" title="Previous query" class="shaded historight" onclick="return false;"></a>';
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};
	}
});