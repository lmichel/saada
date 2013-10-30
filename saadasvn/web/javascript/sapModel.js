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
		var collection ;
		var category ;

		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		}
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(treepath){
			var jsondata;
			var params;
			if( treepath.length == 3 ){
				collection = treepath[0];
				category = treepath[1];
			}
			else if ( treepath.length == 2 ){
				collection = treepath[0];
				category = treepath[1];
			}
			else {
				Modalinfo.info( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: sadaqlModel.js");
				return;
			}
			$("#siapscope").html("Service scope: Not Set");
			if( category == 'IMAGE' ) {
				$("#saptab").tabs({
					disabled: [1,2],
					selected: 0
				});
				$("#siapscope").html("Service scope: IMAGES of " + collection);

			}
			else if( category == 'SPECTRUM' ) {
				$("#ssapscope").html("Service scope: SPECTRA of " + collection);
				$("#saptab").tabs({
					disabled: [0,2],
					selected: 1
				});
			}
			else if( category == 'ENTRY' ) {
				$("#csscope").html("Service scope: Table ENTRIES of " + collection);
				$("#saptab").tabs({
					disabled: [0,1],
					selected: 2
				});
			}
			else {
				$("#saptab").tabs({
					disabled: [0,1,2]
				});
				$("#siapscope").html("Service scope: Not Set");
				$("#ssapscope").html("Service scope: Not Set");
				$("#cspscope").html("Service scope: Not Set");
			}
		}
		
		/*********************
		 * SIAP Event processing
		 */
		this.buildSIAPUrl= function(){
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
			if( $("input[value=siapcutout]").attr('checked') == true ) {				
				url += 'MODE=CUTOUT&';
			}
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) ;
			alert(url);
			return url;
		};
		this.processSIAPQueryEvent= function(){
			window.open(this.buildSIAPUrl(), 'SIAP Result');
		};
		this.processSIAPSampEvent= function(){
			WebSamp_mVc.fireSendVoreport(this.buildSIAPUrl(), null, null);
		};

		this.processSIAPCapabilityEvent= function(){
			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#siapcoordval').val());
			var rad = jQuery.trim($('#siapradiusdval').val());

			var url = base_url + 'siaservice?collection=[' + collection + ']&';
			var inter = $('#siapintersect option:selected').val();
			if( inter != 'COVERS') {
				url += 'INTERSECT=' + inter + '&';
			}
			if( $("input[value=siapcutout]").attr('checked') == true ) {				
				url += 'MODE=CUTOUT&';
			}
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) + '&format=METADATA';
			window.open(url, 'SIAP Capability');
		}
		
		
		this.processSIAPRegistryEvent= function(){
			var url = 'getregistry?type=registry&get=SIA&coll=[' + collection + ']&';
			window.open(url, 'SIAP Registry');
		}
		this.processSIAPGluEvent= function(){
			var url = 'getregistry?type=glu&get=SIA&coll=[' + collection + ']&';
			window.open(url, 'SIAP Glu');
		}

		/*********************
		 * SSAP Event processing
		 */
		this.buildSSAPUrl= function(){
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
			window.open(this.buildSSAPUrl, 'SSA Result');
		};
		this.processSSAPSampEvent= function(){
			WebSamp_mVc.fireSendVoreport(this.buildSSAPUrl(), null, null);
		};

		this.processSSAPCapabilityEvent= function(){
			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#ssapcoordval').val());
			var rad = jQuery.trim($('#ssapradiusdval').val());

			var url = base_url + 'ssaservice?collection=[' + collection + ']&';
			var inter = $('#ssapintersect option:selected').val();
			if( inter != 'COVERS') {
				url += 'INTERSECT=' + inter + '&';
			}
			if( $("input[value=ssapcutout]").attr('checked') == true ) {				
				url += 'MODE=CUTOUT&';
			}
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) + '&format=METADATA';
			window.open(url, 'SSA Capability');
		}
		
		
		this.processSSAPRegistryEvent= function(){
			var url = 'getregistry?type=registry&get=SIA&coll=[' + collection + ']&';
			window.open(url, 'SSA Registry');
		}
		this.processSSAPGluEvent= function(){
			var url = 'getregistry?type=glu&get=SIA&coll=[' + collection + ']&';
			window.open(url, 'SSA Glu');
		}


		/*********************
		 * CS Event processing
		 */
		this.buildCSUrl= function(){
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
			window.open(this.buildCSUrl, 'SSA Result');
		};
		this.processCSSampEvent= function(){
			WebSamp_mVc.fireSendVoreport(this.buildCSUrl(), null, null);
		};

		this.processCSCapabilityEvent= function(){
			if( !collection ) {
				Modalinfo.info("No data collection has been selected");
				return;				
			}
			var pos = jQuery.trim($('#cspcoordval').val());
			var rad = jQuery.trim($('#cspradiusdval').val());

			var url = base_url + 'conesearch?collection=[' + collection + ']&';
			url += 'size=' + escape(rad) + '&pos=' + escape(pos) + '&format=METADATA';
			window.open(url, 'CS Capability');
		}
		
		
		this.processCSRegistryEvent= function(){
			var url = 'getregistry?type=registry&get=CS&coll=[' + collection + ']&';
			window.open(url, 'CS Registry');
		}
		this.processCSGluEvent= function(){
			var url = 'getregistry?type=glu&get=CS&coll=[' + collection + ']&';
			window.open(url, 'CS Glu');
		}

		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributesHandlers, relations);
			});
		}
	}
});
