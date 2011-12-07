jQuery.extend({

	CartView: function(jid){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;

		/**
		 * datatables references
		 */
		var folderTables = new Array();
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

		this.fireAddJobResult = function(query) {
			logged_alert("Query result  added to the cart");
			$.each(listeners, function(i){
				listeners[i].controlAddJobResult(getTreePathAsKey(), query);
			});
		};
		this.fireRemoveJobResult = function(nodekey, jobid) {
			$.each(listeners, function(i){
				listeners[i].controlRemoveJobResult(nodekey, jobid);
			});
		};
		this.fireAddUrl = function(name, oid) {
			logged_alert("File  " + name + " added to the cart");
			$.each(listeners, function(i){
				listeners[i].controlAddUrl(name, oid);
			});
		};
		this.fireRemoveUrl = function(nodekey, url) {
			$.each(listeners, function(i){
				listeners[i].controlRemoveUrl(nodekey, url);
			});
		};
		this.fireOpenCart = function() {			
			$.each(listeners, function(i){
				listeners[i].controlOpenCart();
			});
		};
		this.fireCleanCart = function(tokens) {
			$.each(listeners, function(i){
				listeners[i].controleCleanCart(tokens);
			});
		};
		this.fireStartArchiveBuilding = function() {
			$.each(listeners, function(i){
				listeners[i].controlStartArchiveBuilding();
			});
		};
		this.fireKillArchiveBuilding = function() {
			$.each(listeners, function(i){
				listeners[i].controlKillArchiveBuilding();
			});
		};
		this.fireArchiveDownload = function() {			
			$.each(listeners, function(i){
				listeners[i].controlArchiveDownload();
			});
		};
		this.fireGetJobPhase = function() {
			var retour='';
			$.each(listeners, function(i){
				retour = listeners[i].controlGetJobPhase();
			});
			return retour;
		};
		this.fireChangeName = function(nodekey, dataType, rowNum, newName){
			$.each(listeners, function(i){
				listeners[i].controlChangeName(nodekey, dataType, rowNum, newName);
			});			
		};
		this.fireSetRelations= function(nodekey, dataType, uri, checked) {
			$.each(listeners, function(i){
				listeners[i].controlSetRelations(nodekey, dataType, uri, checked);
			});			
		};
		this.fireDelegateCartDownload= function() {
			$.each(listeners, function(i){
				listeners[i].controlDelegateCartDownload();
			});			
		};
		this.fireCheckArchiveCompleted = function() {
			var phase = that.fireGetJobPhase();
			var queriespan = $('#cartjob_phase');
			queriespan.attr('class', phase.toLowerCase());
			queriespan.text(phase);
			if( phase == 'nojob') {
				$('.cart').css("border", "0px");
			}
			else if( phase == 'EXECUTING') {
				$('.cart').css("border", "2px solid orange");
				setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
			}
			else if( phase == 'COMPLETED') {
				$('.cart').css("border", "2px solid green");
			}
			else {
				$('.cart').css("border", "2px solid red");
			}
		};    


		this.initForm = function(cartData) {
			$('#detaildiv').remove();
			if ($('#detaildiv').length == 0) {
				$(document.documentElement).append(
				"<div id=detaildiv style='width: 99%; display: none;'></div>");
			}
			var empty = true;
			for( var nodekey in cartData) {
				empty = false;
				break;
			}			
			if( empty ) {
				logged_alert("Empty Shopping Cart");
				return;
			}

			var table = '';
			var phase = that.fireGetJobPhase();

			table += '<h2><img src="images/groscaddy.png"> Shopping Cart</h2>';
			table += '<div id=table_div></div>';
			table += "<h4 id=\"cartjob\" class='detailhead'> <img src=\"images/tdown.png\">Processing status</h4>";
			table += '<br><span>Current Job Status</span> <span id=cartjob_phase class="' + phase.toLowerCase() + '">' + phase + '</span><BR>';
			table += "<span>Manage Content</span> <input type=button id=detaildiv_clean value='Remove Unselected Items'>";			
			table += "<input type=button id=detaildiv_cleanall value='Remove All Items'><br>";			
			table += "<span>Manage Job</span> <input type=button id=detaildiv_submit value='Start Processing'>";	
			table += "<input type=button id=detaildiv_abort value='Abort'>";			
			if( typeof PeerCartClient != 'undefined' && PeerCartClient != '' ) {
				table += "<input title='Delegate cart control to " + PeerCartClient + "' type=button value='Delegate' onclick='cartView.fireDelegateCartDownload();'>" ;
			}
			table += "<br><span>Get the Result</span> <input type=button id=detaildiv_download value='Download Cart'>";			


			$('#detaildiv').html(table);

			var modalbox = $('#detaildiv').modal();
			that.setTableDiv(cartData);

			$('#detaildiv_clean').click( function() {
				var tokenArray =new Array();
				for( var i=0 ; i<folderTables.length ; i++) {
					tokenArray[tokenArray.length]  = $('input',folderTables[i].fnGetNodes()).serialize();
				}
				that.fireCleanCart(tokenArray);
				return false;
			} );
			$('#detaildiv_cleanall').click( function() {
				that.fireCleanCart("");
				modalbox.close();
				return false;
			} );
			$('#detaildiv_submit').click( function() {
				that.fireStartArchiveBuilding();
				return false;
			} );
			$('#detaildiv_abort').click( function() {
				that.fireKillArchiveBuilding();
				return false;
			} );

			$('#detaildiv_download').click( function() {
				that.fireArchiveDownload();
				$('.zip').css("border", "0px");
				return false;
			} );
		};

		this.setTableDiv= function(cartData) {
			folderTables = new Array();
			var table = '';
			var empty = true;
			for( var nodekey in cartData) {
				empty = false;
				break;
			}			
			if( empty ) {
				logged_alert("Empty Shopping Cart");
				$.modal.close();
				return;
			}
			var tableId = "folder_" + nodekey;
			for( var nodekey in cartData) {
				table += "<h4 id=\"mappedmeta\" class='detailhead'> <img src=\"images/tdown.png\">Node  " + nodekey + " </h4>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"" + tableId +"\" class=\"display\"></table>";
				table += "</div>";
			}
			$('#table_div').html(table);
			for( var nodekey in cartData) {
				var folder = cartData[nodekey];
				var aaData = new Array();
				var cb = "<INPUT TYPE=CHECKBOX name=\"" + nodekey + " url\" class=\"include_relation\" />";
				for( var i=0 ; i<folder.queries.length ; i++) {
					aaData[aaData.length] = ["<INPUT TYPE=CHECKBOX checked name=\"" + nodekey + " query_result " + i + "\" value=" + i +" />"
					                         , "QUERY_RESULT"
					                         , cb
					                         , "<span>" + folder.queries[i].name + "</span>", folder.queries[i].uri];
				}
				for( var i=0 ; i<folder.files.length ; i++) {
					aaData[aaData.length] = ["<INPUT TYPE=CHECKBOX checked name=\"" + nodekey + " single_file " + i + "\" value=" + i +" />"
					                         ,  "SINGLE_FILE"
					                         , cb
					                         , "<span>" + folder.files[i].name + "</span>", folder.files[i].uri];
				}
				folderTables[folderTables.length] = $('#' + tableId).dataTable(
						{
							"aoColumns" : [{sTitle: "Keep/Discard"}, {sTitle: "Data Source"},{sTitle: "Include Linked Data"},{sTitle: "Resource Name"},{sTitle: "Resource URI"}],
							"aaData" : aaData,
							"bPaginate" : false,
							"bInfo" : false,
							"aaSorting" : [],
							"bSort" : false,
							"bFilter" : false,
							"bAutoWidth" : true
						});
				/* Apply the jEditable handlers to the table */
				$('span', folderTables[folderTables.length-1].fnGetNodes()).editable( 
						function(data) {
							return data.replace(/[^\w]/g, "_");
						},
						{        
							"callback": function( sValue, settings ) {
								var oTable = folderTables[settings["numTable"]];
								var node = $(this).parent().get(0);
								var aPos = oTable.fnGetPosition( node );
								var nodekey = $(node).parents("table").attr("id").replace("folder_", "");
								cartView.fireChangeName(nodekey, oTable.fnGetData( aPos[0] )[1], aPos[0], sValue);
							},
							"height": "1.33em", 
							"width": "16em",
							"numTable": folderTables.length-1 // added by myself
						}
				);
				$('#' + tableId).find('.include_relation').click(
						function(e){
							cartView.fireSetRelations($(this).parents("table").attr("id").replace("folder_", "")
									, $(this).parents("tr").find('td:nth-child(2)').text()
									, $(this).parents("tr").find('td:nth-child(5)').text()
									, $(this).attr('checked'));
						});
			}	
		}
	}
});