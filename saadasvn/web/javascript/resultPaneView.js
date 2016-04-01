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
		var fixedHeader = null;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list) {
			listeners.push(list);
		};
		this.fireTreeNodeEvent = function(treepath) {
			var mode = $("input[@name=qlang]:checked").val();
			var runSaadaQL = false;
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
		};

		this.fireSubmitQueryEvent = function() {
			$("#resultpane").html();
			var mode = $("input[@name=qlang]:checked").val();
			if (mode == 'saadaql') {
				that.fireSaadaQLQueryEvent($('#saadaqltext').val());
			} else if (mode == 'sap') {
				sapView.fireSubmitQueryEvent();
			} else {
				Modalinfo.info('Unknown query mode:' + mode);
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
			Processing.show("Waiting on product info");

			$.getJSON("getproductinfo", {url: url}, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "Cannot get product info") ) {
					return;
				}
				else {
					retour = "<span><b> Doanload URL</b>: " + url + "</span><ul>";
					$.each(jsdata, function(k, v) {
						retour += "<li><b>" + k + "</b>: " + v  + "</li>";
					});
					retour += "</ul>";
					Modalinfo.info(retour, "Product Info");
				}
			});
		};		
		this.fireGetRelationInfo = function(relation) {
			Processing.show("Waiting on product info");

			$.getJSON("getmeta", {query: "relation", name: relation}, function(jsdata) {
				Processing.hide();
				if( Processing.jsonError(jsdata, "Cannot get relation info") ) {
					return;
				}
				else {
					retour = "relation: " + relation + "\n";
					$.each(jsdata, function(k, v) {
						retour += k + ": " + v  + "\n";
					});
					Modalinfo.info(retour, "Relation Info");
				}
			});
		};	
		this.fireSampVOTable = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlSampVOTable();
			});
		};	
		this.fireDownloadVOTable = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadVOTable();
			});
		};
		this.fireDownloadFITS = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
				return;
			}
			$.each(listeners, function(i) {
				listeners[i].controlDownloadFITS();
			});
		};
		this.fireDownloadZip = function(query) {
			if($("#datatable") == undefined ||  $("#datatable").html() == null ) {
				Modalinfo.info("No data selection");
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
				Modalinfo.info("Data zipper facility only available in SaadaQL mode");

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
//			Modalinfo.dataPanel('Preview of ' + title,
//			"<img class=vignette src='getvignette?oid=" + oid
//			+ "'>");
			Modalinfo.dataPanel('Vignette of ' + title
					, "<img class=vignette src='getvignette?oid=" + oid + "'>"
					, 'getvignette?oid=' + oid);
		};
		this.fireShowPreview = function(preview_url, title) {
			Modalinfo.openIframePanel(preview_url);
		};

		this.fireExpendForm= function() {
			var height = $(window).height() ;
			var icon = $('#formexpender').css("background-image");
			if( icon.match("screen_up") != null ) {
				$('#formexpender').css("background-image", "url(images/screen_down.png)");
				$('#formexpender').attr("title", "Expend query form");
				height='10%';
			}
			else {
				$('#formexpender').css("background-image", "url(images/screen_up.png)");
				$('#formexpender').attr("title", "Minimize query form");
				height='90%';
			}
			layoutPane.sizePane("south", height);
			//	$("div#accesspane").trigger("resize",[ height]);		
		};

		this.fireOpenDescription = function() {
			Modalinfo.iframePanel("help/description.html");
		};
		this.showProgressStatus = function() {
			Modalinfo.info("Job in progress");
		};
		this.showFailure = function(textStatus) {
			Modalinfo.info("view: " + textStatus);
		};
		this.showDetail = function(oid, jsdata, limit, panelToOpen) {
			var numPanelToOpen = 0;
			if( Processing.jsonError(jsdata, "") ) {
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
			histo += "<div style='display: inline; float: right'>" + Printer.getPrintButton("simplemodal-container") + "</div>";
			//table += '<h2> ' + histo + ' DETAIL <span>' + jsdata.title
			table += '<h3> ' + histo  + jsdata.title + '<span>'
			+ '</span></h3>';

			if (jsdata.links.length > 0) {
				table += "<div style='overflow: hidden;border-width: 0;'>";
				for (var i = 0; i < jsdata.links.length; i++) {
					table += '<span>' + jsdata.links[i] + '</span><br>';
				}
				table += "</div>";
			}
			table += "<h4 id=\"native\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'native\');;\"> <img src=\"images/tdown.png\"> Native Data </h4>";
			table += "<div class='detaildata'>";
			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"detailtable\" class=\"display\"></table>";
			table += "</div>";

			table += "<h4 id=\"mapped\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'mapped\');\"> <img src=\"images/tright.png\"> Mapped Data </h4>";
			table += "<div class='detaildata'>";
			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"detailmappedtable\" class=\"display\"></table>";
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

			
			 var content = {
						header: {
						    histo: {
						      prev: "resultPaneView.fireShowPreviousRecord()",
						      next: "resultPaneView.fireShowNextRecord()"
						    },
						    title: {
						      label: jsdata.title 
						    },
						    icon: {
						      classIcon: "printer",
						      handler: "print('simplemodal-container')"
						    }
						  },
						  chapters: []
			 }
			 
			content.chapters.push({
					id: "ClassLevel",
					label: "Class Level Data - (Read in input files)",
					data:  {
						"aoColumns" : jsdata.classlevel.aoColumns,
						"aaData" : jsdata.classlevel.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});

			content.chapters.push({
					id: "CollLevel",
					label: "Collection Level Data - (Set by Saada)",
					data:  {
						"aoColumns" : jsdata.collectionlevel.aoColumns,
						"aaData" : jsdata.collectionlevel.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});
			
			for (var i = 0; i < jsdata.relations.length; i++) {
				var relation= jsdata.relations[i];
				console.log(JSON.stringify(relation))
				content.chapters.push({
					id: "Relation" + relation,
					label: "Linked Data (relation "+ relation + ")",
					//url:  "getobject?relation=" + relation,
					url:  "getobject",
					params: {
						oid: oid,
						relation: relation
					},
					searchable: true,
				});
				
//				var relation= jsdata.relations[i];
//				if( relation == panelToOpen) {
//					numPanelToOpen = i+2;
//				}
//				table += "<h4 id=" + relation + " class='detailhead'> <img id=" + relation + " src=\"images/tright.png\"> Relation " + relation 
//				+ "&nbsp;<a id=" + relation + " title='Get info the relation' class=dl_info href='javascript:void(0)'></A></h4>";
//				table += "<div class='detaildata'></div>";
			}
			 ModalResult.resultPanel(content, null, "white", true);
			
			return;

			Modalinfo.dataPanel("Source Detail" , table, null, "white");
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
				"sDom" : '<f"top">rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});

			$('#detailmappedtable').dataTable({
				"aoColumns" : jsdata.collectionlevel.aoColumns,
				"aaData" : jsdata.collectionlevel.aaData,
				"sDom" : '<"top"f>rt',
				"bPaginate" : false,
				"aaSorting" : [],
				"bSort" : false,
				"bFilter" : true
			});			
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
			div.html("<table id="
					+ id
					+ "  width=600px cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  class=\"display\"></table>");
			if( jsdata.aaData.length > 10 ) {
				$('#' + id).dataTable({
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					//	"sDom" : 'rt',
					"sDom": '<"top"i>rt<"bottom"flp><"clear">',
					//	"bPaginate" : true,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : true
				});
			} else {
				$('#' + id).dataTable({
					"aoColumns" : jsdata.aoColumns,
					"aaData" : jsdata.aaData,
					"bPaginate" : false,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : true
				});

			}

			$('#' + jsdata.relation).next('.detaildata').slideToggle(
					500);

		};

		this.showMeta = function(jsdata, limit) {
			if( Processing.jsonError(jsdata, "FATAL ERROR: Cannot show object detail: ") ) {
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
			histo += "<div style='display: inline; float: right'>" + Printer.getPrintButton("simplemodal-container") + "</div>";

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
//			table += '<h2> ' + histo + ' DETAIL <span>' + title
//			+ '</span></h2>';
//			table += "<h4 id=\"mappedmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'mappedmeta\');\"> <img src=\"images/tdown.png\"> Description of Mapped Keywords </h4>";
//			table += "<div class='detaildata'>";
//			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"detailtable\" class=\"display\"></table>";
//			table += "</div>";

//			if (jsdata.classLevel != null) {
//			table += "<h4 id=\"nativemeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'nativemeta\');\"> <img src=\"images/tright.png\">  Description of  Native Data </h4>";
//			table += "<div class='detaildata'>";
//			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"detailmappedtable\" class=\"display\"></table>";
//			table += "</div>";
//			}

//			if (jsdata.collectionLevel.startingRelations.aaData.length > 0) {
//			table += "<h4 id=\"startingmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'startingmeta\');\"> <img src=\"images/tright.png\"> Relationships starting from it </h4>";
//			table += "<div class='detaildata'>";
//			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"startingmetatable\" class=\"display\"></table>";
//			table += "</div>";
//			}

//			if (jsdata.collectionLevel.endingRelations.aaData.length > 0) {
//			table += "<h4 id=\"endingmeta\" class='detailhead' onclick=\"$(this).next('.detaildata').slideToggle(500); switchArrow(\'endingmeta\');\"> <img src=\"images/tright.png\"> Relationships ending at it </h4>";
//			table += "<div class='detaildata'>";
//			table += "<table width=99% cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"endingmetatable\" class=\"display\"></table>";
//			table += "</div>";
//			}

//			if ($('#detaildiv').length == 0) {
//			$(document.documentElement)
//			.append(
//			"<div id=detaildiv style='width: 99%; display: none;'></div>");
//			}
			//Modalpanel.open(table);
//			$('#detailtable').dataTable(
//			{
//			"aoColumns" : jsdata.collectionLevel.attributes.aoColumns,
//			"aaData" : jsdata.collectionLevel.attributes.aaData,
//			"sDom" : '<"top"f>rt',
//			"bPaginate" : false,
//			"aaSorting" : [],
//			"bSort" : false,
//			"bFilter" : true,
//			"bAutoWidth" : true
//			});

			var content = {
					header: {
						histo: {
							prev: "resultPaneView.fireShowPreviousRecord()",
							next: "resultPaneView.fireShowNextRecord()"
						},
						title: {
							label: title 
						},
						icon: {
							classIcon: "printer",
							handler: "print('simplemodal-container')"
						}
					},
					chapters: []
			};

			if (jsdata.classLevel != null) 
				content.chapters.push({
					id: "ClassLevel",
					label: "Class Level Attributes - (Read in input files)",
					data:  {
						"aoColumns" : jsdata.classLevel.attributes.aoColumns,
						"aaData" : jsdata.classLevel.attributes.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});
			if (jsdata.collectionLevel != null) 
				content.chapters.push({
					id: "CollLevel",
					label: "Collection Level Attributes - (Set by Saada)",
					data:  {
						"aoColumns" : jsdata.collectionLevel.attributes.aoColumns,
						"aaData" : jsdata.collectionLevel.attributes.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});

			if (jsdata.collectionLevel.startingRelations.aaData.length > 0) {
				content.chapters.push({
					id: "StartRel",
					label: "Relationships Starting From this dData Collection",
					data:  {
						"aoColumns" : jsdata.collectionLevel.startingRelations.aoColumns,
						"aaData" : jsdata.collectionLevel.startingRelations.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});
			}

			if (jsdata.collectionLevel.endingRelations.aaData.length > 0) {
				content.chapters.push({
					id: "EndRem",
					label: "Relationships Ending To this Data Collection",
					data:  {
						"aoColumns" : jsdata.collectionLevel.endingRelations.aoColumns,
						"aaData" : jsdata.collectionLevel.endingRelations.aaData
					},
					params: {
						oid: "oid"
					},
					searchable: true,
				});

			}
			ModalResult.resultPanel(content, null, "white", true);
		};
		this.displayResult = function(dataJSONObject) {
		};
		this.initTable = function(dataJSONObject, query) {
			if( Processing.jsonError(dataJSONObject, "") ) {
				return;
			} else {
				/*
				 * Get table columns
				 */
				var ahs = dataJSONObject["attributes"];
				var table = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\"  id=\"datatable\" class=\"display\">";
				var headCells = "";
				for (var i = 0; i < ahs.length; i++) {
					headCells += "<th nowrap style='width: auto;'>" + ahs[i].name + "</th>";
				}

				table +=  "<thead><tr>" + headCells + "</tr></thead>";
				table +=  "<tfoot><tr>" + headCells + "</tr></tfoot>";				
				table +=
					"<tbody>"
					+ "<tr><td colspan="
					+ i
					+ " class=\"dataTables_empty\">Loading data from server</td></tr>"
					+ "</tbody>";

				table += "</table>";
				$("#resultpane").html(table);

				var orderParams = saadaqlView.fireOrderByParameters();
				$('#datatable th').each(function() {
					var att = $(this).text();
					if( !att.startsWith('Rel ')  && att != 'Gallery'  && att != 'DL Link'  && att != 'Plot') {
						var ah;
						if( att == 'Access' ) {
							ah = {nameorg: 'Access', nameattr: 'oidsaada'};
						} else if( att == 'Detail' ) {
							ah = {nameorg: 'Detail', nameattr: 'oidsaada'};
						} else if( att == 'Name' ) {
							ah = {nameorg: 'Name', nameattr: 'namesaada'};
						} else if( att == 'Position' ) {
							ah = {nameorg: 'Position', nameattr: 'pos_ra_csa'};
						} else if( att.startsWith('Error ') ) {
							ah = {nameorg: att, nameattr: 'error_maj_csa'};
						} else {
							ah = {nameorg: att, nameattr: att};
						}
						var s = new Sorter_mVc($(this), $(this).parent(), ah, saadaqlView.fireSortColumnEvent);
						s.draw();
						if( att == 'Access' && orderParams.nameattr == 'oidsaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Detail'  && orderParams.nameattr == 'oidsaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Name' && orderParams.nameattr == 'namesaada') {
							s.activeArrow(orderParams.asc);
						} else if( att == 'Position' && orderParams.nameattr == 'pos_ra_csa') {
							s.activeArrow(orderParams.asc);
						} else if( att.startsWith('Error ') && orderParams.nameattr == 'error_maj_csa') {
							s.activeArrow(orderParams.asc);
						} else if( att == orderParams.nameattr) {
							s.activeArrow(orderParams.asc);
						}

					}
				});
				/*
				 * Connect the table with the DB
				 */
				var options = {
						"aLengthMenu": [5, 10, 25, 50, 100],
						"bServerSide" : true,
						"bProcessing" : true,
						"aaSorting" : [],
						"pagingType" : "simple",
						"bSort" : false,
						"bFilter" : false,
						//"sDom": '<"top"pfli>rt<"bottom"pfli<"clear">>',
						//	"sDom": '<"top"pfli>rt<"bottom"pfli<"clear">>',
						"sAjaxSource" : "nextpage"
//							"fnDrawCallback": function() {
//							var width = $(this).width();
//							console.log($(this).width());
//							var wa = new Array();
//							$(this).find('thead').remove();
//							$(this).find('tfoot').find('th').each(function(){wa.push($(this).width() + 2); console.log($(this).width());});
//							$('#pouet').html("<table cellpadding=0; cellspacing=0;  class='display dataTable' style='border: 1px solid black; width: " + width + "px;'><thead>" + $("#resultpane tfoot").html() + "</thead></table>");
//							$('#pouet th').each(function(index){$(this).width(wa[index]); $(this).css('border', '1px solid black');});
//							}
//							"fnInitComplete": function(oSettings, json) {
//							that.fixedHeader.fnUpdate();
//							} 
							// , "bPaginate": false
				};
				var positions = [
				                 { "name": "pagination",
				                	 "pos": "top-left"
				                 },
				                 { "name": "length",
				                	 "pos": "top-center"
				                 },
				                 { "name": "information",
				                	 "pos": "top-right"
				                 },
				                 { "name": "pagination",
				                	 "pos": "bottom-left"
				                 },
				                 { "name": "length",
				                	 "pos": "bottom-center"
				                 },
				                 { "name": "information",
				                	 "pos": "bottom-right"
				                 },
				                 { "name" : '<a title="Download the current selection in a VOTable" class="dl_download" onclick="resultPaneView.fireDownloadVOTable();"></a>',
				                	 "pos": "top-center"
				                 },
				                 { "name" : '<a class="dl_cart" title="Add the current selection to the cart" onclick="cartView.fireAddJobResult($(this), \'' + escape(query) + '\');"></a>',
				                	 "pos": "top-center"
				                 },
				                 { "name" : '<a id="ColumnSelector" class="dl_cart" title="Column selector"></a>',
				                	 "pos": "top-center"
				                 },
				                 { "name" : Printer.getSmallPrintButton("resultpane") ,
				                	 "pos": "top-center"
				                 }				              	
				                 ];
				if( globalTreePath[1] == "ENTRY" || globalTreePath[1] == "IMAGE"|| globalTreePath[1] == "SPECTRUM"){
					positions.push({"name": '<a title="Send the entry selection to SAMP client" class="dl_samp" onclick="resultPaneView.fireSampVOTable();"></a>',
						"pos" : "top-center"})
				}

				var datatable = CustomDataTable.create("datatable", options, positions);			
				$('#datatable_wrapper').css("overflow", "inherit");
				var columnSelector = function(states){
					for( var n=0 ; n<states.length ; n++){
						var column = datatable.api().column( n);
						column.visible( states[n].selected);						
					}
				}
				$('#ColumnSelector').click(function() {
					NodeFilter.create(globalTreePath[0] + globalTreePath[1] + globalTreePath[2], ahs, columnSelector);
				});
				return;
/////////////////////////////////////////////////////////////////////////////////////////
				// p: change page
				//	$('.fixedHeader').remove();
				var iconBar = '&nbsp;<a title="Download the current selection in a VOTable" class="dl_download" onclick="resultPaneView.fireDownloadVOTable();"></a>'	
					+ '<a class="dl_cart" title="Add the current selection to the cart" onclick="cartView.fireAddJobResult($(this), \'' + escape(query) + '\');"></a>'
					+ Printer.getSmallPrintButton("resultpane")
					;
				if( globalTreePath[1] == "ENTRY" || globalTreePath[1] == "IMAGE"|| globalTreePath[1] == "SPECTRUM"){
					iconBar += '<a title="Send the entry selection to SAMP client" class="dl_samp" onclick="resultPaneView.fireSampVOTable();"></a>';
				}

				var  oTable = $('#datatable').dataTable({
					"aLengthMenu": [5, 10, 25, 50, 100],
					"bServerSide" : true,
					"bProcessing" : true,
					"aaSorting" : [],
					"bSort" : false,
					"bFilter" : false,
					//"sDom": '<"top"pfli>rt<"bottom"pfli<"clear">>',
					"sDom": '<"top"pfli>rt<"bottom"pfli<"clear">>',
					"sAjaxSource" : "nextpage"
//						"fnDrawCallback": function() {
//						var width = $(this).width();
//						console.log($(this).width());
//						var wa = new Array();
//						$(this).find('thead').remove();
//						$(this).find('tfoot').find('th').each(function(){wa.push($(this).width() + 2); console.log($(this).width());});
//						$('#pouet').html("<table cellpadding=0; cellspacing=0;  class='display dataTable' style='border: 1px solid black; width: " + width + "px;'><thead>" + $("#resultpane tfoot").html() + "</thead></table>");
//						$('#pouet th').each(function(index){$(this).width(wa[index]); $(this).css('border', '1px solid black');});
//						}
//						"fnInitComplete": function(oSettings, json) {
//						that.fixedHeader.fnUpdate();
//						} 
						// , "bPaginate": false
				});
				//	this.fixedHeader = new FixedHeader( oTable );


			}
			$('#resultpane div.dataTables_length').append(iconBar);
			//$('div .bottom').appendTo('#pouet1');
			that.fireStoreHisto(query);
			//this.fixedHeader.fnUpdate();
			/*
			 * Images are loaded asynchronously and they can change the column width.*
			 * There is no way to trigger this kind of event to update FixHeader.
			 * Les do it a couple of seconds later
			 */
			//setTimeout(function (){that.fixedHeader.fnUpdate()}, 2000);

		};
		this.updateFixedHeader = function() {
			alert(this.fixedHeader);
			if( this.fixedHeader != null ) {
				this.fixedHeader.fnUpdate();
			}
		};
		this.sortColumns = function(nameattr, direction) {
			alert(nameattr + " " +  direction);
			saadaqlView.fireOrderByEvent(nameattr);
			fireSubmitQueryEvent();

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
//			setTimeout("if( simbadToBeOpen == true ) openSimbadModalinfo.(\"" + pos + "\");", 1000);
			Modalinfo.simbad(pos );
		};
		this.outPosition = function() {
			simbadToBeOpen = false;
		};
	}
});