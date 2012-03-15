jQuery
.extend({

	ResultPaneView : function() {
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
		this.addListener = function(list) {
			listeners.push(list);
		};
		this.fireTreeNodeEvent = function(treepath) {
			var mode = $("input[@name=qlang]:checked").val();
			var runSaadaQL = false;
			var runTAP = false;
			if (mode == 'saadaql') {
				runSaadaQL = true;
			} else if (mode == 'sap') {
				$("input[value=saadaql]").attr('checked', true);
				$('#taptab').hide();
				$('#saptab').hide();
				$('#saadaqltab').show('slow');
				runSaadaQL = true;
			} else if (mode == 'tap') {
				runTAP = true;
			}
			saadaqlView.fireTreeNodeEvent(treepath, runSaadaQL);
			sapView.fireTreeNodeEvent(treepath);
			tapView.fireTreeNodeEvent(treepath, runTAP);
		};

		this.fireSubmitQueryEvent = function() {
			$("#resultpane").html();
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				that.fireSaadaQLQueryEvent($('#saadaqltext').val());
			} else if (mode == 'sap') {
				sapView.fireSubmitQueryEvent();
			} else if (mode == 'tap') {
				tapView.fireSubmitQueryEvent();
			} else {
				alert('Unknown query mode:' + mode);
			}
		};
		this.fireSetTreePath = function(treepath) {
			$.each(listeners, function(i) {
				listeners[i].controlSetTreePath(treepath);
			});
		};
		this.fireHisto = function(direction) {
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				saadaqlView.fireHisto(direction);
			}
		};
		this.fireStoreHisto = function(query) {
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				saadaqlView.fireStoreHisto(query);
			}
		};

		this.fireSaadaQLQueryEvent = function(query) {
			$("#resultpane").html();

			$.each(listeners, function(i) {
				listeners[i].controlSaadaQLQueryEvent(query);
			});
		};
		this.fireGetProductInfo = function(url) {
			showProcessingDialog("Waiting on product info");

			$.getJSON("getproductinfo", {url: url}, function(jsdata) {
				hideProcessingDialog();
				if( processJsonError(jsdata, "Cannot get product info") ) {
					return;
				}
				else {
					retour = "url: " + url + "\n";
					$.each(jsdata, function(k, v) {
						retour += k + ": " + v  + "\n";
					});
					logged_alert(retour, "Product Info");
				}
			});
		};		
		this.fireGetRelationInfo = function(relation) {
			showProcessingDialog("Waiting on product info");

			$.getJSON("getmeta", {query: "relation", name: relation}, function(jsdata) {
				hideProcessingDialog();
				if( processJsonError(jsdata, "Cannot get relation info") ) {
					return;
				}
				else {
					retour = "relation: " + relation + "\n";
					$.each(jsdata, function(k, v) {
						retour += k + ": " + v  + "\n";
					});
					logged_alert(retour, "Relation Info");
				}
			});
		};		
		this.fireDownloadVOTable = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				alert("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadVOTable();
			});
		};
		this.fireDownloadFITS = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				alert("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadFITS();
			});
		};
		this.fireDownloadZip = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				alert("No data selection");
				return;
			}			
			else if( $("input[@name=qlang]:checked").val() == 'saadaql') {
				if( $(".zip").css("background-image").match(".*connecting.*") ) {
					if( confirm("Do you want to cancel the Zipper job?") ) {
						$.each(listeners, function(i) {
							listeners[i].controlCancelZip();
						});
						return;
					}
				}					
				else {
					$.each(listeners, function(i) {
						listeners[i].controlDownloadZip();
					});
				}
			}
			else {
				alert("Data zipper facility only available in SaadaQL mode");

			}
		};
		this.fireCheckZipCompleted= function(jobid) {
			$.each(listeners, function(i) {
				listeners[i].controlCheckZipCompleted(jobid);
			});
		};
		this.fireSampBroadcast = function(query) {
			$.each(listeners, function(i) {
				listeners[i].controlSampBroadcast();
			});
		};
		this.fireShowRecord = function(oid, panelToOpen) {
			$.each(listeners, function(i) {
				listeners[i].controlShowRecord(oid, panelToOpen);
			});
		};
		this.fireShowMeta = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowMeta();
			});
		};
		this.fireShowMetaNode = function(treepath) {
			$.each(listeners, function(i) {
				listeners[i].controlShowMetaNode(treepath);
			});
		};
		this.fireShowSources = function(oid) {
			$('#saadaqllang').attr('checked', 'checked');
			$('#taptab').hide();
			$('#saptab').hide();
			$('#saadaqltab').show('slow');
			$("#qhistocount").css("visibility", "visible");
			saadaqlView.fireDisplayHisto();

			$.each(listeners, function(i) {
				listeners[i].controlShowSources(oid);
			});
		};
		this.fireShowSimbad = function(coord) {
			$.each(listeners, function(i) {
				listeners[i].controlShowSimbad(coord);
			});
		};
		this.fireShowPreviousRecord = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowPreviousRecord();
			});
		};
		this.fireShowNextRecord = function() {
			$.each(listeners, function(i) {
				listeners[i].controlShowNextRecord();
			});
		};

		this.fireShowCounterparts = function(oid, relation) {
			var div = $('#' + relation).next('.detaildata');
			if (div.html().length > 0) {
				div.slideToggle(500);
			} else {
				$.each(listeners,
						function(i) {
					listeners[i].controlShowCounterparts(oid,
							relation);
				});
			}
			// $('#detaildiv').animate({scrollTop:
			// $('#detaildiv').height()}, 800);
		};
		this.fireShowVignette = function(oid, title) {
			openDialog('Preview of ' + title,
					"<img class=vignette src='getvignette?oid=" + oid
					+ "'>");
		};
		this.fireShowPreview = function(preview_url, title) {
			openDialog('Preview of ' + title,
					"<img class=vignette src='" + preview_url + "'>");
		};

		this.fireExpendForm= function() {
			var icon = $('#formexpender').css("background-image");
			var height = 0;
			if( icon.match("screen_up") != null ) {
				$('#formexpender').css("background-image", "url(images/screen_down.png)");
				$('#formexpender').attr("title", "Expend query form");
				height = $(window).height() - 70 - 50;
				if( height < 100) {
					height = 100;
				}
			}
			else {
				$('#formexpender').css("background-image", "url(images/screen_up.png)");
				$('#formexpender').attr("title", "Minimize query form");
				height = 200;
				if( height < 100) {
					height = 100;
				}
			}
			$("div#accesspane").trigger("resize",[ height]);		
		};

		this.fireOpenDescription = function() {
			if ($('#detaildiv').length == 0) {
				$(document.documentElement)
				.append(
				"<div id=detaildiv style='width: 99%; display: none;'><div style='color: black;' id=description></div></div>");
			} else {
				$('#detaildiv').html("<div style='color: black;' id=description></div>");
			}
			$('#description').load("help/description.html");
			$('#detaildiv').modal();
		};
		this.showProgressStatus = function() {
			alert("Job in progress");
		};
		this.showFailure = function(textStatus) {
			alert("view: " + textStatus);
		};
		this.showDetail = function(oid, jsdata, limit, panelToOpen) {
			var numPanelToOpen = 0;
			if (jsdata.errormsg != null) {
				alert("FATAL ERROR: Cannot show object detail: "
						+ jsdata.errormsg);
				return;
			}

			var table = '';
			var histo = '';

			if (limit != 'NoHisto') {
				if (limit != 'MaxLeft') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowPreviousRecord();" class=histoleft></a>';
				} else {
					histo += '<a id="qhistoleft"><img src="images/histoleft-grey.png"></a>';
				}
				if (limit != 'MaxRight') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowNextRecord();" class=historight></a>';
				} else {
					histo += '<a id="qhistoright"><img src="images/historight-grey.png"></a>';
				}
			} else {
				histo += '<a id="qhistoleft"><img src="images/histoleft-grey.png"></a>';
				histo += '<a id="qhistoright"><img src="images/historight-grey.png"></a>';
			}

			table += '<h2> ' + histo + ' DETAIL <span>' + jsdata.title
			+ '</span></h2>';
			if (jsdata.links.length > 0) {
				table += "<div style='overflow: hidden;border-width: 0;'>";
				for (var i = 0; i < jsdata.links.length; i++) {
					table += '<span>' + jsdata.links[i] + '</span><br>';
				}
				table += "</div>";
			}
			table += "<h4 id=\"native\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'native\');\"> <img src=\"images/tdown.png\"> Native Data </h4>";
			table += "<div class='detaildata'>";
			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"detailtable\" class=\"display\"></table>";
			table += "</div>";

			table += "<h4 id=\"mapped\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'mapped\');\"> <img src=\"images/tright.png\"> Mapped Data </h4>";
			table += "<div class='detaildata'>";
			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"detailmappedtable\" class=\"display\"></table>";
			table += "</div>";

			/*
			 * relation panels
			 */
			for (var i = 0; i < jsdata.relations.length; i++) {
				var relation= jsdata.relations[i];
				if( relation == panelToOpen) {
					numPanelToOpen = i+2;
				}
				table += "<h4 id=" + relation + " class='detailhead'> <img id=" + relation + " src=\"images/tright.png\"> Relation " + relation 
				+ "&nbsp;<a id=" + relation + " title='Get info the relation' class=dl_info href='javascript:void(0)'></A></h4>";
				table += "<div class='detaildata'></div>";
			}

			if ($('#detaildiv').length == 0) {
				$(document.documentElement)
				.append(
				"<div id=detaildiv style='width: 99%; display: none;'></div>");
			}
			$('#detaildiv').html(table);

			/*
			 * Click on relation relation title bars: 
			 * on the bar: open/close the data panel
			 * on tne Info button: display relation info
			 */			
			for (var i = 0; i < jsdata.relations.length; i++) {
				var relation= jsdata.relations[i];
				$('#' + relation).click(function(e) {
					var relation = $(e.target).attr('id');
					if( $(e.target).is('a') ) {
						resultPaneView.fireGetRelationInfo(relation);
					}
					else {
						resultPaneView.fireShowCounterparts(oid, relation ); 
						switchArrow(relation);
					}
				});
			}

			$('#detailtable').dataTable({
				"aoColumns" : jsdata.classlevel.aoColumns,
				"aaData" : jsdata.classlevel.aaData,
				"sDom" : '<"top"f>rt<"bottom">',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});

			$('#detailmappedtable').dataTable({
				"aoColumns" : jsdata.collectionlevel.aoColumns,
				"aaData" : jsdata.collectionlevel.aaData,
				"sDom" : '<"top"f>rt<"bottom">',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});
			$('#detaildiv').modal();
			if( numPanelToOpen > 1 ) {
				jQuery(".detaildata").each(function(i) {
					$(this).hide();
				});
				for (var i = 0; i < jsdata.relations.length; i++) {
					var relation= jsdata.relations[i];
					if( relation == panelToOpen) {
						numPanelToOpen = i+2;
						resultPaneView.fireShowCounterparts(oid, relation);
						switchArrow(relation);
					}
				}
			}
			else {
				jQuery(".detaildata").each(function(i) {
					if (i != numPanelToOpen ) {
						$(this).hide();
					}
				});
			}
		};

		this.showCounterparts = function(jsdata) {
			var id = "reltable" + jsdata.relation;
			var div = $('#' + jsdata.relation).next('.detaildata');
			div
			.html("<table id="
					+ id
					+ "  width=600px cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  class=\"display\"></table>");
			$('#' + id).dataTable({
				"aoColumns" : jsdata.aoColumns,
				"aaData" : jsdata.aaData,
				"sDom" : 'rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});
			$('#' + jsdata.relation).next('.detaildata').slideToggle(
					500);

		};

		this.showMeta = function(jsdata, limit) {
			if (jsdata.errormsg != null) {
				alert("FATAL ERROR: Cannot show object detail: "
						+ jsdata.errormsg);
				return;
			}

			var table = '';
			var histo = '';

			if (limit != 'NoHisto') {
				if (limit != 'MaxLeft') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowPreviousRecord();" class=histoleft></a>';
				} else {
					histo += '<a id="qhistoleft"><img src="images/histoleft-grey.png"></a>';
				}
				if (limit != 'MaxRight') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowNextRecord();" class=historight></a>';
				} else {
					histo += '<a id="qhistoright"><img src="images/historight-grey.png"></a>';
				}
			} else {
				histo += '<a id="qhistoleft"><img src="images/histoleft-grey.png"></a>';
				histo += '<a id="qhistoright"><img src="images/historight-grey.png"></a>';
			}

			var title;
			if (jsdata.classLevel != null) {
				title = "Meta data of class <i>"
					+ jsdata.classLevel.name
					+ "</i> of collection <i>"
					+ jsdata.collectionLevel.name + "."
					+ jsdata.collectionLevel.category + "</i>";
			} else {
				title = "Meta data of collection <i>"
					+ jsdata.collectionLevel.name + "."
					+ jsdata.collectionLevel.category + "</i>";
			}
			table += '<h2> ' + histo + ' DETAIL <span>' + title
			+ '</span></h2>';
			table += "<h4 id=\"mappedmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'mappedmeta\');\"> <img src=\"images/tdown.png\"> Mapped Data </h4>";
//			table += "<p id=nativemeta class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500);\"><span id=spear>></span> Mapped Data </p>";
			table += "<div class='detaildata'>";
			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"detailtable\" class=\"display\"></table>";
			table += "</div>";

			if (jsdata.classLevel != null) {
				table += "<h4 id=\"nativemeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'nativemeta\');\"> <img src=\"images/tright.png\"> Native Data </h4>";
//				table += "<p id=mappedmeta class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500);\"><span id=spear>></span> Native Data </p>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"detailmappedtable\" class=\"display\"></table>";
				table += "</div>";
			}

			if (jsdata.collectionLevel.startingRelations.aaData.length > 0) {
				table += "<h4 id=\"startingmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'startingmeta\');\"> <img src=\"images/tright.png\"> Relationships starting from it </h4>";
//				table += "<p id=startingmeta class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500);\"><span id=spear>></span> Relationships starting from it </p>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"startingmetatable\" class=\"display\"></table>";
				table += "</div>";
			}

			if (jsdata.collectionLevel.endingRelations.aaData.length > 0) {
				table += "<h4 id=\"endingmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'endingmeta\');\"> <img src=\"images/tright.png\"> Relationships ending at it </h4>";
//				table += "<p id=endingmeta class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500);\"><span id=spear>></span> Relationships ending at it </p>";
				table += "<div class='detaildata'>";
				table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"endingmetatable\" class=\"display\"></table>";
				table += "</div>";
			}

			if ($('#detaildiv').length == 0) {
				$(document.documentElement)
				.append(
				"<div id=detaildiv style='width: 99%; display: none;'></div>");
			}
			$('#detaildiv').html(table);

			$('#detailtable')
			.dataTable(
					{
						"aoColumns" : jsdata.collectionLevel.attributes.aoColumns,
						"aaData" : jsdata.collectionLevel.attributes.aaData,
						"sDom" : '<"top"f>rt<"bottom">',
						"bPaginate" : false,
						"aaSorting" : [],
						"bSort" : false,
						"bFilter" : true,
						"bAutoWidth" : true
					});
			if (jsdata.classLevel != null) {
				$('#detailmappedtable')
				.dataTable(
						{
							"aoColumns" : jsdata.classLevel.attributes.aoColumns,
							"aaData" : jsdata.classLevel.attributes.aaData,
							"sDom" : '<"top"f>rt<"bottom">',
							"bPaginate" : false,
							"aaSorting" : [],
							"bSort" : false,
							"bFilter" : true,
							"bAutoWidth" : true
						});
			}
			if (jsdata.collectionLevel.startingRelations.aaData.length > 0) {
				$('#startingmetatable')
				.dataTable(
						{
							"aoColumns" : jsdata.collectionLevel.startingRelations.aoColumns,
							"aaData" : jsdata.collectionLevel.startingRelations.aaData,
							"sDom" : '<"top"f>rt<"bottom">',
							"bPaginate" : false,
							"aaSorting" : [],
							"bSort" : false,
							"bFilter" : true,
							"bAutoWidth" : true
						});
			}
			if (jsdata.collectionLevel.endingRelations.aaData.length > 0) {
				$('#endingmetatable')
				.dataTable(
						{
							"aoColumns" : jsdata.collectionLevel.endingRelations.aoColumns,
							"aaData" : jsdata.collectionLevel.endingRelations.aaData,
							"sDom" : '<"top"f>rt<"bottom">',
							"bPaginate" : false,
							"aaSorting" : [],
							"bSort" : false,
							"bFilter" : true,
							"bAutoWidth" : true
						});
			}

			$('#detaildiv').modal();

			jQuery(".detaildata").each(function(i) {
				if (i > 0) {
					$(this).hide();
				}
			});
		};
		this.showTapResult = function(jid, jsdata) {
			setTitlePath([ 'TAP', 'Job', jid ]);
			var table = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"datatable\" class=\"display\"></table>";
			$("#resultpane").html(table);

			$('#datatable').dataTable({
				"aoColumns" : jsdata.aoColumns,
				"aaData" : jsdata.aaData,
				"sDom" : '<"top">lrt<"bottom">ip',
				"bPaginate" : true,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});

		};
		this.displayResult = function(dataJSONObject) {
		};
		this.initTable = function(dataJSONObject, query) {
			if( processJsonError(dataJSONObject, "") ) {
				return;
			}
			else {
				/*
				 * Get table columns
				 */
				var ahs = dataJSONObject["attributes"];
				var table = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"  id=\"datatable\" class=\"display\">"
					+ "<thead>" + "<tr>";
				for (var i = 0; i < ahs.length; i++) {
					table += "<th>" + ahs[i].name + "</th>";
				}
				/*
				 * Build empty table
				 */
				table += "</tr>"
					+ "</thead>"
					+ "<tbody>"
					+ "<tr><td colspan="
					+ i
					+ " class=\"dataTables_empty\">Loading data from server</td></tr>"
					+ "</tbody>" + "</table>";
				$("#resultpane").html(table);
				/*
				 * Connect the table with the DB
				 */
				$('#datatable').dataTable({
			        "aLengthMenu": [5, 10, 25, 50, 100],
					"bServerSide" : true,
					"bProcessing" : true,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : false,
			//		"sDom": '<"top"iflp<"clear">>rt<"bottom"iflp<"clear">>',
					"sAjaxSource" : "nextpage"
				});
			}
			$('div#datatable_length').append('&nbsp;<a title="Download the current selection in a VOTable" class="dl_download" onclick="resultPaneView.fireDownloadVOTable();"></a> ');		
			$('div#datatable_length').append('<a class="dl_cart" title="Add the current selection to the cart" onclick="cartView.fireAddJobResult($(this), \'' + escape(query) + '\');">');
			that.fireStoreHisto(query);
		};
		/*
		 * Returns the arrow commanding the historic on the modal box
		 */
		this.histoCommands = function(limit) {
			var histo = '';

			if (limit != 'NoHisto') {
				if (limit != 'MaxLeft') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowPreviousRecord();" class=histoleft></a>';
				}
				if (limit != 'MaxRight') {
					histo += '<a href="javascript:void(0);" onclick="resultPaneView.fireShowNextRecord();" class=historight></a>';
				}
			}
			return histo;
		};

		this.updateQueryHistoCommands = function(length, ptr) {
			var result = '';
			$("#qhistocount").html((ptr + 1) + "/" + length);
			if (length <= 1) {
				result += '<img src="images/histoleft-grey.png">';
				result += '<img src="images/historight-grey.png">';
			} else {
				if (ptr > 0) {
					result += '<a id="qhistoleft" title="Previous query" class=histoleft onclick="resultPaneView.fireHisto(\'previous\');"></a>';
				} else {
					result += '<a id="qhistoleft"><img src="images/histoleft-grey.png"></a>';
				}
				if (ptr < (length - 1)) {
					result += '<a id="qhistoright" title="Next query" class=historight onclick="resultPaneView.fireHisto(\'next\');"></a>';
				} else {
					result += '<img src="images/historight-grey.png">';
				}
			}
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};

		this.overPosition = function(pos) {
//			simbadToBeOpen = true;
//			setTimeout("if( simbadToBeOpen == true ) openSimbadDialog(\"" + pos + "\");", 1000);
			openSimbadDialog(pos );
		};
		this.outPosition = function() {
			simbadToBeOpen = false;
		};
	}
});