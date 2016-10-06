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
		var listener;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};

		this.fireAddJobResult = function(element, query) {
			var elementClass = element.attr('class');
			if( elementClass == 'dl_cart' ) {
				Processing.show("Selection " + getTreePathAsKey() + " added to the cart");
				element.attr('class', 'dl_cart_added');
				Out.info("add " + getTreePathAsKey() + " <> " + query);
				listener.controlAddJobResult(getTreePathAsKey(), query);
			} else {
				Processing.show("Selection " + getTreePathAsKey() + " remove from the cart");
				element.attr('class', 'dl_cart');
				listener.controlRemoveJobResult(getTreePathAsKey(), query);
			}
			this.fireCheckArchiveCompleted();
			setTimeout('Processing.hide();', 1000);
		};
		this.fireRemoveJobResult = function(nodekey, jobid) {
			Out.info("remove " + nodekey() + " <> " + jobid);
			listener.controlRemoveJobResult(nodekey, jobid);
			this.fireCheckArchiveCompleted();
		};
		this.fireAddUrl = function(element, name, oid) {
			var elementClass = element.attr('class');
			if( elementClass == 'dl_cart' || elementClass == 'dl_securecart') {
				element.attr('class', elementClass + '_added');
				Out.info("add " + name + " <> " + oid);
				listener.controlAddUrl(name, oid);
			} else {
				element.attr('class', elementClass.replace('_added', ''));
				listener.controlRemoveUrl(getTreePathAsKey(), oid);
			}
			this.fireCheckArchiveCompleted();
		};
		this.fireRemoveUrl = function(nodekey, url) {
			Out.info("removeURL " + nodekey() + " <> " + jobid);
			listener.controlRemoveUrl(nodekey, url);
			this.fireCheckArchiveCompleted();
		};
		this.fireOpenCart = function() {			
			listener.controlOpenCart();
		};
		this.fireCleanCart = function(tokens) {
			listener.controleCleanCart(tokens);
			this.fireCheckArchiveCompleted();
		};
		this.fireStartArchiveBuilding = function() {
			listener.controlStartArchiveBuilding();
		};
		this.fireKillArchiveBuilding = function() {
			listener.controlKillArchiveBuilding();
		};
		this.fireArchiveDownload = function() {			
			listener.controlArchiveDownload();
		};
		this.fireGetJobPhase = function() {
			var retour='';
			retour = listener.controlGetJobPhase();
			return retour;
		};
		this.fireChangeName = function(nodekey, dataType, rowNum, newName){
			listener.controlChangeName(nodekey, dataType, rowNum, newName);
			this.fireCheckArchiveCompleted();
		};
		this.fireSetRelations= function(nodekey, dataType, uri, checked) {
			listener.controlSetRelations(nodekey, dataType, uri, checked);
			this.fireCheckArchiveCompleted();
		};
		this.fireDelegateCartDownload= function() {
			listener.controlDelegateCartDownload();
		};
		
		this.resetJobControl= function() {
			Out.info("resetJobControl");
			listener.controlResetZipjob();
			$('.cart').css("border", "0px");
			$('#detaildiv_download').attr("disabled", true);
			$('#detaildiv_submit').removeAttr("disabled");
			var jobspan = $('#cartjob_phase');
			jobspan.attr('class', 'nojob');
			jobspan.text('nojob');
			};

		this.fireCheckArchiveCompleted = function() {
			var phase = that.fireGetJobPhase();
			var queriespan = $('#cartjob_phase');
			queriespan.attr('class', phase.toLowerCase());
			queriespan.text(phase);
			if( phase == 'nojob') {
				$('.cart').css("border", "0px");
				$('#detaildiv_submit').removeAttr("disabled");
				$('#detaildiv_download').attr("disabled", true);
			}
			else if( phase == 'EXECUTING') {
				$('.cart').css("border", "2px solid orange");
				setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
			}
			else if( phase == 'COMPLETED') {
				$('.cart').css("border", "2px solid green");
				$('#detaildiv_submit').attr("disabled", true);
				$('#detaildiv_download').removeAttr("disabled");
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
				Modalinfo.info("Empty Shopping Cart");
				return;
			}

			var table = '';
			//var phase = that.fireGetJobPhase();

			//table += '<h2><img src="images/groscaddy.png"> Shopping Cart</h2>';
			table += '<div id=table_div></div>';
			table += "<p id=\"cartjob\" class='chapter'> <img src=\"images/tdown.png\">Processing status</p>";
			//table += '<span>Current Job Status</span> <span id=cartjob_phase class="' + phase.toLowerCase() + '">' + phase + '</span><BR>';
			table += '<br><span>Current Job Status</span> <span id=cartjob_phase class=""></span><BR>';
			table += "<span>Manage Content</span> <input type=button id=detaildiv_clean value='Remove Unselected Items'>";			
			table += "<input type=button id=detaildiv_cleanall value='Remove All Items'><br>";			
			table += "<span>Manage Job</span> <input type=button id=detaildiv_submit value='Start Processing'>";	
			table += "<input type=button id=detaildiv_abort value='Abort'>";			
			if( typeof PeerCartClient != 'undefined' && PeerCartClient != '' ) {
				table += "<input title='Delegate cart control to " + PeerCartClient + "' type=button value='Delegate' onclick='cartView.fireDelegateCartDownload();'>" ;
			}
			table += "<br><span>Get the Result</span> <input type=button id=detaildiv_download value='Download Cart' disabled='disabled'>";			

			//Modalpanel.open(table);
			Modalinfo.dataPanel('<a class="cart-title" href="#"></a> Shopping Cart' , table, null, "white");
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
				Modalinfo.close($(this).parent().attr("id"));
				//$(".dl_cart_added").attr("class","dl_cart");
				return false;
			} );
			$('#detaildiv_submit').click( function() {
				aborted = false;
				that.fireStartArchiveBuilding();
				return false;
			} );
			$('#detaildiv_abort').click( function() {
				if (aborted == false) {
					aborted = true;
					that.fireKillArchiveBuilding();
					that.fireCheckArchiveCompleted();
				}
				return false;
			} );

			$('#detaildiv_download').click( function() {
				that.fireArchiveDownload();
				$('.zip').css("border", "0px");
				return false;
			} );
			this.fireCheckArchiveCompleted();
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
				Modalinfo.info("Empty Shopping Cart");
				$.modal.close();
				return;
			}
			for( var nodekey in cartData) {
				var tableId = "folder_" + nodekey.replace(/\./g, '_DoT_');;
				table += "<p id=\"mappedmeta\" class='chapter'> <img src=\"images/tdown.png\">Node  " + nodekey + " </p>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"" + tableId +"\" class=\"display\"></table>";
				table += "</div>";
			}
			$('#table_div').html(table);
			for( var nodekey in cartData) {
				var tableId = "folder_" + nodekey.replace(/\./g, '_DoT_');;
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
				
				
				var options = {
						"aoColumns" : [{sTitle: "Keep/Discard"}, {sTitle: "Data Source"},{sTitle: "Include Linked Data"},{sTitle: "Resource Name"},{sTitle: "Resource URI"}],
						"aaData" : aaData,
						"bPaginate" : false,
						"bInfo" : false,
						"aaSorting" : [],
						"bSort" : false,
						"bFilter" : false,
						"bAutoWidth" : true,
						"bDestroy": true
				}
				
				folderTables[folderTables.length] = CustomDataTable.create(tableId, options);
				/* Apply the jEditable handlers to the table */
				$('span', folderTables[folderTables.length-1].fnGetNodes()).editable( 
						function(data) {
							var retour = data.replace(/[^\w\.]/g, "_");
							return  (retour != '')? retour: 'NoName';
						},
						{        
							"callback": function( sValue, settings ) {
								var oTable = folderTables[settings["numTable"]];
								var node = $(this).parent().get(0);
								var aPos = oTable.fnGetPosition( node );
								var nodekey = $(node).parents("table").attr("id").replace("folder_", "").replace(/_DoT_/g, '.');
								cartView.fireChangeName(nodekey, oTable.fnGetData( aPos[0] )[1], aPos[0], sValue);
							},
							"height": "1.33em", 
							"width": "16em",
							"numTable": folderTables.length-1 // added by myself
						}
				);
				$('#' + tableId + ' input.include_relation').click(
						function(e){
							cartView.fireSetRelations($(this).parents("table").attr("id").replace("folder_", "").replace(/_DoT_/g, '.')
									, $(this).parents("tr").find('td:nth-child(2)').text()
									, $(this).parents("tr").find('td:nth-child(5)').text()
									, $(this).is(':checked'));
							
						});
			}
			Modalinfo.center();
		}
	}
});