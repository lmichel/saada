jQuery.extend({

	SapModel: function(pmodel){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/*
		 * What we have to store and play with
		 */		
		var collection = null;
		var category  = null;

		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			$("#siapscope").html("Service scope: Not Set");
			if( category == 'IMAGE' ) {
				$("#saptab").tabs({
					disabled: [2],
					selected: 1,
				});
				$("#siapscope").html("Service scope: IMAGES of " + collection);

			}
			else if( category == 'SPECTRUM' ) {
				$("#ssapscope").html("Service scope: SPECTRA of " + collection);
				$("#saptab").tabs({
					disabled: [1],
					selected: 2
				});
			}
			else if( category == 'ENTRY' ) {
				$("#csscope").html("Service scope: Table ENTRIES of " + collection);
				$("#saptab").tabs({
					disabled: [1,2],
					selected: 0
				});
			}
			else {
				$("#saptab").tabs({
					disabled: [0, 1,2],
				});
				$("#siapscope").html("Service scope: Not Set");
				$("#ssapscope").html("Service scope: Not Set");
				$("#cspscope").html("Service scope: Not Set");
			}
		};

		/*********************
		 * SIAP Event processing
		 */
		this.buildSIAPUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;
			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#siapcoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#siapradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}
			var url = base_url + 'siaservice?collection=[' + collection + ']&';
			var inter = $('#siapintersect option:selected').val();
			if( inter != 'COVERS') {
				url += 'INTERSECT=' + inter + '&';
			}
			if( $("input[value=siapcutout]").attr('checked') ) {				
				url += 'MODE=CUTOUT&';
			}
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) ;
			return url;
		};
		this.processSIAPQueryEvent= function(){
			var url = this.buildSIAPUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SIAP Result');
			}
		};
		this.processSIAPSampEvent= function(){
			var url = this.buildSIAPUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};
		/*********************
		 * SSAP Event processing
		 */
		this.buildSSAPUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#ssapcoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#ssapradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}

			var band1 = jQuery.trim($('#ssapbandmin').val());
			var band2 = jQuery.trim($('#ssapbandmax').val());
			var unit  = $('#ssapunit option:selected').val();
			var band = '';
			if( band1 != '' && band2 != '' ) {
				band = '&band=' + band1 + '/' + band2 + '[' + unit + ']';
			}
			var url = base_url + 'ssaservice?collection=[' + collection + ']&';
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) + band;
			return url;
		};
		this.processSSAPQueryEvent= function(){
			var url = this.buildSSAPUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SSA Result');
			}
		};
		this.processSSAPSampEvent= function(){
			var url = this.buildSSAPUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};

		/*********************
		 * CS Event processing
		 */
		this.buildCSUrl= function(){
			var category = globalTreePath.category;
			var collection = globalTreePath.schema;

			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#cscoordval').val());
			if( pos == '' ) {
				Modalinfo.info("No position given");
				return;
			}
			var rad = jQuery.trim($('#csradiusdval').val());
			if( rad == '' ) {
				Modalinfo.info("No size given");
				return;
			}
			if( isNaN(rad) ) {
				Modalinfo.info("Size must be  numeric");
				return;
			}
			if( rad < 0 || rad > 1) {
				Modalinfo.info("Size must be  between 0 and 1");
				return;
			}
			var url = base_url + 'conesearch?collection=[' + collection + ']&';

			url += 'SR=' + escape(rad) + '&RA=' + escape(pos) ;
			return url;
		};
		this.processCSQueryEvent= function(){
			var url = this.buildCSUrl();
			if( url){
				PageLocation.changeLocation(url);
				//Modalinfo.openIframePanel(url, 'SCS Result');
			}
		};
		this.processCSSampEvent= function(){
			var url = this.buildCSUrl();
			if( url){
				WebSamp_mVc.fireSendVoreport(url, null, null);
			}
		};

		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributesHandlers, relations);
			});
		};
	}
});
