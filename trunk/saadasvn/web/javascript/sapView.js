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
			if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPQueryEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPQueryEvent();
				});	
			}
			// CS
			else if( selected_prot ==  2 ) {
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
			if( selected_prot ==  0 ) {
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
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSSampEvent();
				});				
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		this.fireSubmitCapabilityEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPCapabilityEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPCapabilityEvent();
				});
			}
			// CS
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSCapabilityEvent();
				});
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		this.fireSubmitRegistryEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPRegistryEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPRegistryEvent();
				});
			}
			// CS
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSRegistryEvent();
				});
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		this.fireSubmitGluEvent = function(){
			var selected_prot = $('#saptab').tabs('option', 'selected'); 
			// SIAP
			if( selected_prot ==  0 ) {
				$.each(listeners, function(i){
					listeners[i].controlSIAPGluEvent();
				});
			}
			// SSAP
			else if( selected_prot ==  1 ) {
				$.each(listeners, function(i){
					listeners[i].controlSSAPGluEvent();
				});
			}
			// CS
			else if( selected_prot ==  2 ) {
				$.each(listeners, function(i){
					listeners[i].controlCSGluEvent();
				});
			}
			else {
				Modalinfo.info('#tab protocol out of range ' + selected_prot);
			}
		};
		
		this.fireDisplayHisto = function(){
			var result = '';
			result += '<img src="images/histoleft-grey.png">';
			result += '<img src="images/historight-grey.png">';	
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};
	}
});