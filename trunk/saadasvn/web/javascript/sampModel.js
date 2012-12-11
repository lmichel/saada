jQuery.extend({

	SampModel: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;					
		var attempt = 0;

		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		}


		this.sampInit = function(list){

			$("#sampconnector").click(function() {				
				WebSampConnector.configure({
					jAppletId: 'WebSampConnectorApplet',
					jAppletCodeBase: './applets/', 
					jAppletVersion: '1.5'
				});

				$(this).css("background", "url(images/connecting.gif)center left no-repeat");
				var connected = false;
				try {
					connected = WebSampConnector.isConnected();
				}catch(err) {
				}
				if( connected == false ) {
					WebSampConnector.connect();
					that.attempt = 0;
					setTimeout(that.checkConnection, 1000);
				}
				else {
					WebSampConnector.disconnect();
					$("#sampconnector").css("background", "url(images/disconnected.png)center left no-repeat");					
					$(".ivoa").css('visibility', 'hidden');
				}
			});
		}

		this.checkConnection = function() {
			try {
				if( WebSampConnector.isConnected() == false ) {	
					if( attempt > 7 ) {		
						$("#sampconnector").css("background", "url(images/disconnected.png)center left no-repeat");
						$(".ivoa").css('visibility', 'hidden');
						loggedAlert('Connection failed: Make sure you have a SAMP hub running<br>'
								+ 'If not, you can start one by clicking on one icon below<br>'
								+ '<a href="javascript:void(0);" onclick="changeLocation(\'http://aladin.u-strasbg.fr/java/nph-aladin.pl?frame=get&id=AladinBeta.jnlp\');"><img valign=center height=24 src="http://aladin.u-strasbg.fr/aladin_large.gif"></a>'
						 		+ '<BR><a href="javascript:void(0);" onclick="changeLocation(\'http://www.star.bris.ac.uk/~mbt/topcat/topcat-full.jnlp\');"><img valign=center  height=24 src="http://www.star.bris.ac.uk/~mbt/topcat/tc3.gif"></a>'
						 		+ '<br>Once the application is running, close this dialog and try again to connect<br>'
						 		);
					}   //http://aladin.u-strasbg.fr/aladin_large.gif 
						//http://www.star.bris.ac.uk/~mbt/topcat/tc3.gif
					//http://vo.imcce.fr/webservices/share/logo/logo_VOParis.png
					else {
						attempt++;
						setTimeout(that.checkConnection, 1000);
					}
				}
				else {
					$("#sampconnector").css("background", "url(images/connected.png)center left no-repeat");	
					$(".ivoa").css('visibility', 'visible');
				}
			} catch(err) {
				$("#sampconnector").css("background", "url(images/disconnected.png)center left no-repeat");
				$(".ivoa").css('visibility', 'hidden');
				loggedAlert(err + '\nConnection failed: Make sure the applet WebSampConnector is authorized to run');				
			}

		}

	}	
});