jQuery
.extend({

	WebSampView : function() {
		/**
		 * keep a reference to ourselves in JSON callback
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listener = null;
		/**
		 * add a listener to this view
		 */
		var addListener = function(list) {
			listener = list;
		};
		/*
		 * used to transform local URLs in full URLs
		 */
		var rootUrl = "http://" + window.location.hostname +  (location.port?":"+location.port:"") + window.location.pathname;
		/*
		 * list of connected clients updated by model notifications
		 */
		var sampClients = new Object(); 
		/*
		 * List of hubs which are both Hubs and SAMP clients
		 */
		var hubs = new Array();
		hubs["topcat"] = {
				webStartUrl : "http://www.star.bris.ac.uk/~mbt/topcat/topcat-full.jnlp",
				iconUrl : "http://www.star.bris.ac.uk/~mbt/topcat/tc3.gif",
				webUrl : "http://www.star.bris.ac.uk/~mbt/topcat/",
				description : "Tool for OPerations on Catalogues And Tables"
		};
		hubs["aladin"] = {
				webStartUrl : "http://aladin.u-strasbg.fr/java/nph-aladin.pl?frame=get&id=aladin.jnlp",
				iconUrl : "http://aladin.u-strasbg.fr/aladin_large.gif",
				webUrl : "http://aladin.u-strasbg.fr/",
				description : "The Aladin sky atlas and VO Portal"
		};
		/*
		 * Internal flags
		 */
		var waitForHub = false;
		var modalOpen = false;
		var requete = new Object();
		/*
		 * Initiate the div receiving any information from SAMP model
		 */
		var sampModalId = "sampModalId";
		$(document.body)
		.append(
				"<div id=" + sampModalId + "Container>"
				+ "<div id="
				+ sampModalId
				+ " style='width: 99%; display: none;'>"
				+ '<a id="' // dummy a just to have the image referenced by the css
				+ sampModalId
				+ 'Icon" class="ivoalogo"></a>'
				+ '<span id='
				+ sampModalId
				+ 'Title style="display: inline; font-size: 1.5em;font-weight: bold;"> TITLE</span><BR>'
				+ '<div><ul id=sampClientListItems></ul></div>'
				+ '<span id=' + sampModalId
				+ 'Help class=help></span>'
				+ '<ul id=' + sampModalId
				+ 'ItemList></ul>' + '<span id='
				+ sampModalId
				+ 'PostHelp class=help></span>'
				+ "</div>"
				+ "</div>"
		);
		/*
		 * Methods handling the content of the modal box
		 */
		var getElement = function(eleSuffix) {
			/*
			 * Make sure we are working on the div attached to this view. Its content
			 * is duplicated in the dialog panel and the selectors are lost, 2 hours waste
			 */
			return $('#' + sampModalId + 'Container #'  + sampModalId + eleSuffix);
		};
		var setLoadIcon = function() {
			Processing.show("Talking with the HUB");
//			$('#' + sampModalId + 'Icon').attr("src",
//			"images/ajax-loader.gif");
		};
		var setIvoaIcon = function() {
			Processing.hide();
//			$('#' + sampModalId + 'Icon')
//			.attr("src", "images/ivoa.jpg");
		};
		var setTitle = function(title) {
			getElement('Title ').text(title);
		};
		var setHelp = function(text) {
			getElement('Help').html(text);
		};
		var setPostHelp = function(text) {
			getElement('PostHelp').html(text);
		};
		var addItem = function(text) {
			getElement('ItemList').append(
					"<li>" + text + "</li>");
		};
		var removeAllItems = function() {
			getElement('ItemList').empty();
		};
		var openModalWithClose = function() {
			if (modalOpen == false) {
				modalOpen = true;			
			}
			Modalinfo.dataPanel("Samp Info", getElement('').html(), function() { webSampView.fireCloseModal();});
		};
		/*************************************************************************************************
		 * Information display out (private)
		 */
		var makeUrlAbsolute = function (url){
			if( !url.match(/https?:\/\// ) ) {
				return rootUrl + "/" + requete.param;
			} else {
				return url;
			}
		} ;
		var showConnectionOff = function() {
			$("#sampIndicator").attr("class", "sampOff");
		};
		var showConnectionOn = function() {
			$("#sampIndicator").attr("class", "sampOn");
		};
		var showClientList = function(callback) {
			setIvoaIcon();
			setTitle("Available SAMP Clients");
			setHelp('Below is the list of SAMP clients accepting data<br>\n'
					+ '- Click on the icon of the client you want to send data.<BR>'
					+ '- Click on the broadcast icon if you want your data to be sent to all clients.');
			setPostHelp('');
			removeAllItems();				
			
			var callback = (requete.type == "oid" )? "webSampView.fireSendFileToClient"
					:(requete.type == "voreport") ? "webSampView.fireSendUrlToClient"
					: "webSampView.fireSendSkyatToClient";
			var found = false;
			for (ident in sampClients) {
				found = true;
				if (sampClients[ident].meta && sampClients[ident].subs) {
					var meta = sampClients[ident].meta;
					addItem("<img class=clickableicon align=bottom style='height: 32px; border: 0px;' src='"
							+ meta["samp.icon.url"]
							+ "' onclick='" + callback + "(\"" +  ident + "\", \"" + requete.param + "\");'>"
							+ "<span class=help> <b>"
							+ meta["samp.name"]
							+ "</b> "
							+ meta["samp.description.text"]
							+ " </span><a style='font-color: blue; font-size: small; font-style: italic;' target=_blank href='"
							+ meta["home.page"] + "'>read more...</a>");
					}
			}
			if (found) {
				addItem("<img class=clickableicon align=bottom style='height: 32px; border: 0px;' src='saadajsbasics/icons/sampOn.png'"
						+ " onclick='" + callback + "(null, \"" + requete.param + "\");'>"
						+ "<span class=help> <b>Broadcast</b> to any client");
				openModalWithClose();
			} else {
				fireCloseModal();
				Modalinfo.info("No SAMP Clients Available: Is your Hub still running?");			
				if (fireIsConnected()) showConnectionOff();

			}
		};


		/**************************************************************
		 * Methods handling messages (private)
		 */
		var fireRegisterToHub = function() {
			waitForHub = listener.controlRegisterToHub().HubRunning;
		};

		/**************************************************************
		 * Methods invoked by the controller (public)
		 */		
		var fireCloseModal = function() {
			modalOpen = false;
			waitForHub = false;
			$( this ).dialog( "close" );
		};
		var fireJustConnect = function() {
			if (!fireIsConnected()) {
				fireRegisterToHub();
			} else {
				fireUnregister();
			}
		};
		var fireSendOid = function(oid) {
			Out.info("Send OID " + oid);
			requete = {type: "oid", param: oid};			
			if (!fireIsConnected()) {
				fireRegisterToHub();
			} else {
				var cpt=0;
				/*
				 * If more than one client: propose a choice window
				 */
				for (ident in sampClients) {
					cpt++;
					showClientList();
					return ;
				}
				showClientList();
			}
		};
		/*
		 * Closely bind to Saada
		 */
		var fireSendFileToClient = function(target) {
			var oid = requete.param;
			var url = rootUrl + "download?oid=" + oid;
			var urlinfo = "getproductinfo?url=" + encodeURIComponent(url);
			$.getJSON(urlinfo,function(data) {
				var ContentType = data.ContentType;
				var fileName = data.ContentDisposition;
				var results = /.*filename=(.*)$/.exec(data.ContentDisposition);
				if( results == null ) {
					fileName = "3XMMDataFile";
				} else {
					fileName = results[1].replace(/"/g, '');
				}
				var mtype = (ContentType.match(/fits/i) )? "table.load.fits": "table.load.votable";
				var message = new Object();
				message["samp.mtype"] = mtype;
				message["samp.params"] = {'table-id': oid, url:url, name: fileName};
				Processing.showAndHide("File sent to SAMP");
				listener.controlSendFileToClient(target, message); 
				webSampView.fireCloseModal();

			});
		};
		var fireSendVoreport = function(reportUrl) {
			Out.info("Send report");
			requete = {type: "voreport", param: reportUrl};			
			if (!fireIsConnected()) {
				fireRegisterToHub();
			} else {
				var cpt=0;
				/*
				 * If more than one client: propose a choice window
				 */
				for (ident in sampClients) {
					cpt++;
					showClientList();
					return ;
				}
				fireSendUrlToClient(null);
			}
		};
		var fireSendUrlToClient = function(target) {
			var reportUrl = makeUrlAbsolute(requete.param);
			Out.info("Send Send URL " + reportUrl + " to " + target);
			var message = new Object();
			message["samp.mtype"] = "table.load.votable";
			message["samp.params"] = {'table-id': "voreport", url:reportUrl, name: "VOReport"};
			Processing.showAndHide("URL sent to SAMP");
			listener.controlSendUrlToClient(target, message); 
			fireCloseModal();
		};

		var fireSendSkyat = function(ra, dec) {
			Out.info("Send Sky At");
			requete = {type: "skyat", param: {ra: ra, dec: dec}};			
			if (!fireIsConnected()) {
				fireRegisterToHub();
			} else {
				var cpt=0;
				for (ident in sampClients) {
					cpt++;
						showClientList();
						return ;
				}
					fireSendSkyatToClient(null);
			}
		};	
		var fireSendSkyatToClient = function(target) {
			var message = new Object();
			message["samp.mtype"] = "coord.pointAt.sky";
			message["samp.params"] = {ra: String(requete.param.ra), dec: String(requete.param.dec)};
			Processing.showAndHide("Sky position sent to SAMP");
			listener.controlSkyatToClient(target, message);
			fireCloseModal();
		};
		var fireRegisterToHubAttempt = function() {
			if (waitForHub) {
				Out.debug("attempt " + waitForHub );
				waitForHub = false;
				fireRegisterToHub();
				//setTimeout("webSampView.fireRegisterToHubAttempt();", 5000);
			} else {
				waitForHub = false;
			}
		};
		var fireUnregister = function() {
			if (fireIsConnected()) {
				Out.debug("unregister");
				listener.controlUnregisterToHub();
				showConnectionOff();
			} else {
				Modalinfo.info("Not registered");
			}
		};
		var fireIsConnected = function() {
			var retour = listener.controlIsConnected();
			if (retour) {
				showConnectionOn();
			} else {
				showConnectionOff();
			}
			return retour;
		};
		var fireIsHubRunning = function() {
			listener.controlIsHubRunning();
		};
		var showTrackerReply = function(id, action, data) {
			waitForHub = false;
			showConnectionOn();
			var removed = "";
			var completed = "";
			// Hub is not considered as a SAMP client
			if (!id || id.match(/hub/i)) {
				return;
			}
			if (action == "unregister") {
				if (sampClients[id]) {
					delete sampClients[id];
					removed = id;
				}
			} else {
				if (!sampClients[id]) {
					sampClients[id] = new Object();
				}
				if (action == "meta") {
					sampClients[id].meta = data;
				} 
				// Only accept to deal with clients accepting VOtable or FITS
				else if (action == "subs") {
					if (data["table.load.fits"] && data["table.load.votable"] &&  data["coord.pointAt.sky"] ) {
						sampClients[id].subs = data;
					} else {
						delete sampClients[id];
					}
				}
				if (sampClients[id] && sampClients[id].meta && sampClients[id].subs)
					completed = id;
			}
			/*
			 * Display the client list only if it has changed and if its
			 * content is complete
			 */
			if (removed != "" || completed != "") {
				showClientList();
			}
		};
		var showHubError = function(message) {
			Out.debug("showHubError " + message);
			if( !message.match(/no\s+hub/i) ) {
				return
			}
			waitForHub = true;
			showConnectionOff();
			setIvoaIcon();
			setTitle("No running SAMP hub detected");
			setHelp('You need to start a hub before to export data towards VO clients<br> \n'
					+ 'You can either run it by hand or by clicking on one icon below.<br>'
					+ 'If the applcation doesn\'t start, make sure that Java <a target="_blank" href="http://www.java.com/fr/download/faq/java_webstart.xml">Web Start</a> '
					+ 'is properly installed on your machine.');
			setPostHelp('IMPORTANT: Once the hub is running, click on the <span style="diplay: inline-block;width: 23px"><a href="#" class="dlivoalogo"></a>icon</span>  again to send data\n');
			removeAllItems();
			for ( var h in hubs) {
				var hub = hubs[h];
				addItem("<a href='#' onclick=webSampView.fireLaunchHub('"
						+ h
						+ "','"
						+ hub.webStartUrl
						+ "');><img class=clickableicon align=middle style='height: 32px; border: 0ps;' src='"
						+ hub.iconUrl
						+ "'></a>"
						+ "<span class=help> <b>"
						+ h
						+ "</b> "
						+ hub.description
						+ " </span><a style='font-color: blue; font-size: small; font-style: italic;' target=_blank href='"
						+ hub.webUrl + "'>read more...</a>");
			}
			openModalWithClose();
			setTimeout("webSampView.fireRegisterToHubAttempt();", 3000);

		};		
		var fireLaunchHub = function(name, url) {
			setLoadIcon();
			Location.changeLocation(url);
			setIvoaIcon();
			//setTimeout("webSampView.fireRegisterToHubAttempt();", 1000);
		};

		/* Exports. */
		var jss = {};
		jss.fireCloseModal = fireCloseModal;
		jss.fireSendOid = fireSendOid; //
		jss.fireSendFileToClient = fireSendFileToClient;
		jss.fireSendVoreport = fireSendVoreport;
		jss.fireSendUrlToClient = fireSendUrlToClient;
		jss.fireSendSkyat = fireSendSkyat; //
		jss.fireSendSkyatToClient = fireSendSkyatToClient;
		jss.fireRegisterToHubAttempt = fireRegisterToHubAttempt;//
		jss.fireUnregister = fireUnregister; //
		jss.fireJustConnect = fireJustConnect;
		jss.fireIsConnected = fireIsConnected; //
		jss.fireIsHubRunning = fireIsHubRunning;//
		jss.showTrackerReply = showTrackerReply; //
		jss.showHubError = showHubError;
		jss.fireLaunchHub = fireLaunchHub;
		jss.addListener = addListener;
		return jss;
	}
});