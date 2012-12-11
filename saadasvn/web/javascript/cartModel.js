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
						loggedAlert("Query Result already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].queries[i] = {name: name, uri: query, relations: []};			
			}
		};
		this.removeJobResult = function(nodekey, jobid) {
			var entry;
			var sjobid = unescape(jobid);
			if( (entry = cartData[nodekey]) == undefined ) {
				loggedAlert("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var queries = entry.queries;
				for( var i=0 ; i<queries.length ; i++ ) {
					logMsg(queries[i].uri + " " + sjobid );
					if( queries[i].uri == sjobid ) {
						queries.splice(i,1);
						if( queries.length == 0 && entry.files.length == 0 ) {
							delete cartData[nodekey];
						}
						return;
					}
				}
				loggedAlert("Job " + nodekey + "." + jobid+ " not found in from the cart", "input Error");
			}			
		};
		this.addUrl = function(name, oid) {
			var entry;
			var nodekey = getTreePathAsKey();
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {queries: new Array(), files: new Array()};
				cartData[nodekey].files[0] = {name: name, uri: oid, relations: []};
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					if( files[i].uri == oid ) {
						loggedAlert("This url of node " + nodekey  + " is already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].files[i] = {name: name, uri: oid, relations: []};			
			}			
		};
		this.removeUrl = function(nodekey, url) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				loggedAlert("There is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var files = entry.files;
				for( var i=0 ; i<files.length ; i++ ) {
					if( files[i].uri == url ) {
						files.splice(i,1);
						if( files.length == 0 && entry.queries.length == 0 ) {

							delete cartData[nodekey];
						}		
						return;
					}
				}
				loggedAlert("URL not found in from the cart", "input Error");
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
					if( key[1] == 'query_result' ) {
						that.addJobResult((old_cartData[node]).queries[num].name
								, (old_cartData[node]).queries[num].uri);
					}
					else if( key[1] == 'single_file' ) {
						that.addUrl((old_cartData[node]).files[num].name
								, (old_cartData[node]).files[num].uri);
					}
				}
			}
			that.notifyCartCleaned();
		};

		this.changeName= function(nodekey, dataType, rowNum, newName) {
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
//			if ($('#detaildiv').length == 0) {
//				$(document.documentElement)
//				.append(
//				"<div id=detaildiv style='height: 99%; width: 99%; display: none;'><div style='color: black;' id=description name=pouet></div></div>");
//			}
//			$('#detaildiv').css('height', '99%');
//			$('#detaildiv').html('<iframe id="iframeid" name="iframename" style="height: 99%; width: 99%"></iframe>');
//			$('#detaildiv').modal();

			if ($('#delegateFormId').length == 0) {
				//var form = [ '<form id="delegateFormId" target="iframename" method="POST" action="', PeerCartClient, '">' ];
				var form = [ '<form id="delegateFormId" target="_sblank" method="POST" action="', PeerCartClient, '">' ];
				form.push('<input type="hidden" name="saadadburl" value="'
						, escape($(location).attr('href').replace('/#', '') + '/cart/zipper')
						, '" />');
				form.push('<input id="delegateCartContent" type="hidden" name="cartcontent" value=""/>');
				form.push('</form>');
				jQuery(form.join('')).appendTo('body');
			}
			$('#delegateCartContent').attr('value', escape(JSON.stringify(cartData)));			
			$('#delegateFormId').submit();
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
				loggedAlert("There is no active ZIP builder");
			}
			else {
				that.zipJob.download();
			}
		};		
		
		this.resetZipjob = function() {
			logMsg("Reset Zipjob");
			zipJob = null ;
		};

	}
});
