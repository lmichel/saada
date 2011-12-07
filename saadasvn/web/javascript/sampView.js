jQuery.extend({

	SampView: function(){
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

		this.fireSampInit = function(){
			$.each(listeners, function(i){
				listeners[i].controlSampInit();
			});
		};

		this.fireSendImage= function(oid){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				WebSampConnector.sendMsg('image.load.fits',oid,'Img' + oid,base_url + 'download?oid=' + oid,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		};
		this.fireSendSpectra= function(oid){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				WebSampConnector.sendMsg('table.load.fits',oid,'Spec' + oid,base_url + 'download?oid=' + oid,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		};
		this.fireSendSIAQuery= function(query){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				var url = base_url + 'getqueryreport?';
				url += 'query=' + escape(query);
				url += '&datamodel=SIA&protocol=sia&format=votable';
				WebSampConnector.sendMsg('table.load.votable','From Query Result' ,'Images', url,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		};
		this.fireSendSSAQuery= function(query){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				var url = base_url + 'getqueryreport?';
				url += 'query=' + escape(query);
				url += '&datamodel=SSA&protocol=ssa&format=votable';
				WebSampConnector.sendMsg('table.load.votable','From Query Result' ,'Spectra', url,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		};
		this.fireSendTapDownload= function(url){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				WebSampConnector.sendMsg('table.load.votable','From TAP Result' ,'VOTable', url,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		}	;
		this.fireSendTapDownload= function(ra, dec){
			WebSampConnector.pointAtSky(ra, dec);
		};
		this.fireSendCSQuery= function(query){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				var url = base_url + 'getqueryreport?';
				url += 'query=' + escape(query);
				url += '&datamodel=CS&protocol=cs&format=votable';
				WebSampConnector.sendMsg('table.load.votable','From Query Result' ,'Table entries', url,'');
			}
			else {
				alert('No active SAMP connnection');
			}
		}	;	
		this.firePointatSky= function(pos){
			if( that.isSampConnect() ) {
				showSampMessageSent();
				//WebSampConnector.sendAladinScript('get Aladin(DSS2) 198.69107026467 +9.085315305339 15arcmin; sync; get VizieR(USNO2); sync; set USNO2 shape=triangle');
				$.getJSON("sesame", {object: pos }, function(data) {
					hideProcessingDialog();
					if( processJsonError(data, "Sesame failure") ) {
						return;
					}
					else {
						showSampMessageSent();
						logMsg([data.alpha, data.delta]);
						WebSampConnector.pointAtSky(data.alpha, data.delta);
					}
				});
			}
			else {
				//alert('No active SAMP connnection');
			}
		}	;	
		this.isSampConnect = function() {
			try {
				var connected = WebSampConnector.isConnected();
			} catch(e) {
				return false;
			}
			return  connected ;
		};

	}
});