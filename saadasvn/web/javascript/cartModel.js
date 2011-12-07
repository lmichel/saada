jQuery.extend({

	CartModel: function(nodekey, description){

		var listeners = new Array();
		var that = this;

		var cartData = {};
		var zipJob = '';;
		var queryNum=0;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		this.addJobResult = function(nodekey, jobid) {
			var entry;
			var name = 'query_' + queryNum;
			var query = unescape(jobid);
			queryNum++;
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {queries: new Array(), files: new Array()};
				cartData[nodekey].queries[0] = {name: name, uri: query, relations: []};
			}
			else {
				var queries = entry.queries;
				for( var i=0 ; i<queries.length ; i++ ) {
					if( queries[i].uri == query ) {
						logged_alert("Query Result already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].queries[i] = {name: name, uri: query, relations: []};			
			}
		};
		this.removeJobResult = function(nodekey, jobid) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				logged_alert("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var queries = entry.queries;
				for( var i=0 ; i<queries.length ; i++ ) {
					if( queries[i].uri == jobid ) {
						queries.splice(i,1);
						if( queries.length == 0 && entry.files.length == 0 ) {
							delete cartData[nodekey];
						}
						return;
					}
				}
				logged_alert("Job " + nodekey + "." + jobid+ " not found in from the cart", "input Error");
			}			
		};
		this.addUrl = function(name, oid) {
			var entry;
			var nodekey = getTreePathAsKey();
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {queries: new Array(), files: new Array()};
				cartData[nodekey].files[0] = {name: name, uri: oid, relations: []};
				logMsg("add URL1 " + nodekey + " (" + name + " " + oid + ")");
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					if( files[i].uri == oid ) {
						logged_alert("This url of node " + nodekey  + " is already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].files[i] = {name: name, uri: oid, relations: []};			
				logMsg("add URL2 " + nodekey + " (" + name + " " + oid + ")");
			}			
		};
		this.removeUrl = function(nodekey, url) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				logged_alert("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					logger.debug("To remove " + files[i].uri + " in " + url);
					if( files[i].uri == url ) {
						files.splice(i,1);
						if( files.length == 0 && entry.queries.length == 0 ) {
							logger.debug("remove " + nodekey + " in " + url);

							delete cartData[nodekey];
						}		

						return;
					}
				}
				logged_alert("URL not found in from the cart", "input Error");
			}						
		};

		this.cleanCart = function(tokenArray) {
			var old_cartData = cartData;
			cartData = {};
			for( var t=0 ; t<tokenArray.length ; t++ ) {
				var tokens = tokenArray[t];

				var tkList = tokens.split("&");
				for( var i=0 ; i<tkList.length ; i++ ){
					var row  = tkList[i].split('=');
					var num  = row[1];
					var key  = row[0].split('+');
					var node = key[0];
					logMsg("@@@@ " + tokens+ '|' + row + '|' +  num + '|' +   key  + '|' +  node);
					if( key[1] == 'query_result' ) {
						that.addJobResult((old_cartData[node]).queries[num].name
								, (old_cartData[node]).queries[num].uri);
					}
					else if( key[1] == 'single_file' ) {
						logMsg("@@@@ " + (old_cartData[node]).files[num].uri);
						that.addUrl((old_cartData[node]).files[num].name
								, (old_cartData[node]).files[num].uri);
					}
				}
			}
			that.notifyCartCleaned();
		};

		this.changeName= function(nodekey, dataType, rowNum, newName) {
			logMsg("@@@@@@ changeName" + nodekey + " " +  dataType+ " " + rowNum+ " " + newName);
			if( dataType.toLowerCase() == "query_result" ) {
				cartData[nodekey].queries[rowNum].name = newName;
			}
			else {
				cartData[nodekey].files[rowNum].name = newName;
			}
			that.notifyCartCleaned();			
		};

		this.setRelations = function(nodekey, dataType, uri, checked) {
			var relValue = (checked == true )? ["any-relations"] : [];
			var files;
			if( dataType.toLowerCase() == "single_file" ) {
				files = cartData[nodekey].files;
			}
			else {
				files = cartData[nodekey].queries;
			}

			for( var i=0 ; i<files.length ; i++ ) {
				if( files[i].uri == uri ) {
					files[i].relations = relValue;
					logMsg("Relations set to "  + checked + " in " + dataType  + "/" + dataType + "/" +  uri);
					return;
				}
			}
		};

		this.notifyCartCleaned = function() {
			$.each(listeners, function(i){
				listeners[i].isCartCleaned(cartData);
			});			
		};
		this.notifyCartOpen = function() {
			$.each(listeners, function(i){
				listeners[i].isInit(cartData);
			});			
		};

		this.startArchiveBuilding = function() {
			$.ajax({
				type: 'POST',
				url: "cart/zipper",
				data: {PHASE: 'RUN', FORMAT: 'json',CART: JSON.stringify(cartData) },
				success: function(xmljob, status) {
					that.zipJob = new $.ZipjobModel(xmljob);
					setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
				},
				dataType: "xml",
				error: function(xmljob, textStatus, errorThrown) {
					alert("Error: " + textStatus);
				}
			});
		};
		
		this.delegateCartDownload= function() {
			logMsg(PeerCartClient);
			var form = [ '<form target=_blank method="POST" action="', PeerCartClient, '">' ];

			form.push('<input type="hidden" name="saadadburl" value="', window.location.replace('/#', ''),'/cart/zipper" />');
			form.push('<input type="hidden" name="cartcontent" value="',escape(JSON.stringify(cartData)), '"/>');
			form.push('</form>');

			jQuery(form.join('')).appendTo('body')[0].submit();			
		};

		this.killArchiveBuilding = function() {
			if( that.zipJob == null ) {
				return "nojob";
			}
			else {
				that.zipJob.kill();
				return that.zipJob.phase;
			}
		};

		this.getJobPhase= function() {
			if( that.zipJob == null ) {
				return "nojob";
			}
			else {
				that.zipJob.refresh();
				return that.zipJob.phase;
			}
		};

		this.archiveDownload = function() {
			if( that.zipJob == null ) {
				logged_alert("There is no active ZIP builder");
			}
			else {
				that.zipJob.download();
			}
		};
	}
});
