DataTree = function () {
	var cache = new Object;

	var init = function() {

		Processing.show("Loading Data Tree");
		$.getJSON("getmeta", {query: "datatree" }, function(jsdata) {
			Processing.hide();
			if( Processing.jsonError(jsdata, "Cannot make data tree") ) {
				return;
			}
			dataTree = $("div#treedisp").jstree();
			/*
			 * Loop on collections
			 */
			var data = jsdata.data;
			for( var n=0 ; n<data.length ; n++){
				var node = data[n];
				var treepath = node.attr.id.split('.');
				var children =node.children;
				
				if( children.length == 0 ) continue;

				$("div#treedisp").jstree("create_node"
						, $("div#treedisp")
						, "last"
						, {"data" : {"icon": "images/Database2.png", "attr":{"id": node.attr.id, "title": ""}, "title" : node.attr.id},
							"state": "close"}
						,false
						,true);  
				$("a#" + node.attr.id).before("<img title='Click to get the description of the collection' class=infoanchor id='" + node.attr.id + "' src='images/metadata.png'></img>");
				$("a#" + node.attr.id + " ins").remove();
				$("img#" + node.attr.id).click(function(){resultPaneView.fireShowMetaCollection($(this).attr("id"));});
				/*
				 * Loop on categories
				 */
				var cparent = $("#" + node.attr.id);
				for( var c=0 ; c<children.length ; c++){
					var child = children[c];
					var ctreepath = child.attr.id.split('.');
					var id = child.attr.id.replace(/\./g, '_DoT_');
					var title;
					var icon;
					if(child.data == "IMAGE" ) {
						icon = "images/nodeImage.png";
						title =child.data;
					} else if(child.data == "MISC" ) {
						icon = "images/nodeMisc.png";
						title =child.data;
					} else if(child.data == "SPECTRUM" ) {
						icon = "images/nodeSpectrum.png";
						title =child.data;
					} else if(child.data == "FLATFILE" ) {
						icon = "images/nodeFlat.png";
						title =child.data;
					} else if(child.data == "TABLE" ) {
						icon = "images/SQLTable2.png";
						title = "TABLE Headers";
					} else if(child.data == "ENTRY" ) {
						icon = "images/SQLTable2.png";
						title ="TABLE Entries";
					}
					$("div#treedisp").jstree("create_node"
							, cparent
							, "last"
							, {"data" : {"icon":icon, "attr":{"id": id, "title": "Click to display the content"}, "title" : title},
								"state": "close",
								"attr" :{"id": id}}
							, null
							, false);   
					$("a#" + id).click(function(){							
						var tp  = $(this).attr("id").split('_DoT_');
						//resultPaneView.fireSetTreePath(tp);	
						resultPaneView.fireTreeNodeEvent(tp);
						//setTitlePath(tp);
					});
					/*
					 * Loop on classes
					 */
					var classes =child.children;
					var parent = $("#" + id);

					for( var d=0 ; d<classes.length ; d++){
						var classe = classes[d];
						var ctreepath = classe.attr.id.split('.');
						var cid = classe.attr.id.replace(/\./g, '_DoT_')
						$("div#treedisp").jstree("create_node"
								, parent
								, false
								, {"data" : {"icon":"images/blank.png", "attr":{"id": cid, "title": "Click to display the content of this data class"}, "title" : classe.data},
									"state": "closed",
									"attr" :{"id":cid}}
								,false
								,true);   
						$("a#" + cid).before("<img 'Click to get the description' class=infoanchor id='" + cid + "' src='images/metadata.png'></img>");
						$("a#" + cid).click(function(){	
							var tp  = $(this).attr("id").split('_DoT_');
							resultPaneView.fireSetTreePath(tp);	
							resultPaneView.fireTreeNodeEvent(tp);
							//setTitlePath(tp);
							});
						$("a#" + cid + " ins").remove();
						$("img#" + cid).click(function(){resultPaneView.fireShowMetaNode($(this).attr("id").split('_DoT_'));	});
					}

				}

			}
			layoutPane.sizePane("west", $("#treedisp").width()) ;
			layoutPane.sizePane("south", '10%') ;

			return

			dataTree = $("div#treedisp").jstree({
				"json_data"   : data , 
				"plugins"     : [ "themes", "json_data", "dnd", "crrm", "ui"],
				"dnd"         : {"drop_target" : "#resultpane,#saadaqltab,#saptab,#taptab,#showquerymeta",

					"drop_finish" : function (data) {
						var parent = data.r;
						var treepath = data.o.attr("id").split('.');
						if( treepath.length < 2 ) {
							Modalinfo.info("Query can only be applied on one data category or one data class");
						}
						else {
							while(parent.length != 0  ) {
								resultPaneView.fireSetTreePath(treepath);	
								if(parent.attr('id') == "resultpane" ) {
									setTitlePath(treepath);
									resultPaneView.fireTreeNodeEvent(treepath);	
									return;
								}
								else if(parent.attr('id') == "showquerymeta" ) {
									setTitlePath(treepath);
									resultPaneView.fireShowMetaNode(treepath);	
									return;
								}

//								else if(parent.attr('id') == "displayfilter" ) {
//								setTitlePath(treepath);
//								resultPaneView.fireTreeNodeEvent(treepath);	
//								filterManagerView.fireShowFilterManager(treepath);	
//								return;
//								}

								else if( parent.attr('id') == "saadaqltab" || parent.attr('id') == "saptab" || parent.attr('id') == "taptab") {
									saadaqlView.fireTreeNodeEvent(treepath);	
									sapView.fireTreeNodeEvent(treepath);	
									return;
								}
								parent = parent.parent();
							}
						}
					}
				},
				// Node sorting by DnD blocked
				"crrm" : {"move" : {"check_move" : function (m) {return false; }}
				}
				
			}); // end of jstree
//			dataTree.bind("select_node.jstree", function (e, data) {
//			Modalinfo.info(data);
//			});
			dataTree.bind("dblclick.jstree", function (e, data) {
				var node = $(e.target).closest("li");
				var id = node[0].id; //id of the selected node					
				var treepath = id.split('.');
				if( treepath.length < 2 ) {
					Modalinfo.info("Query can only be applied on one data category or one data class");
				}
				else {
					Processing.show("Open node " + getTreePathAsKey());
					resultPaneView.fireSetTreePath(treepath);	
					setTitlePath(treepath);
					resultPaneView.fireTreeNodeEvent(treepath);	
					Processing.hide();
				}
			});
		}); // end of ajax
	}
	
	this.init2 = function() {

		Processing.show("Loading Data Tree");
		$.getJSON("getmeta", {query: "datatree" }, function(data) {
			Processing.hide();
			if( Processing.jsonError(data, "Cannot make data tree") ) {
				return;
			}
			console.log(JSON.stringify(data));
			dataTree = $("div#treedisp").jstree({
				"json_data"   : data , 
				"plugins"     : [ "themes", "json_data", "dnd", "crrm", "ui"],
				"dnd"         : {"drop_target" : "#resultpane,#saadaqltab,#saptab,#taptab,#showquerymeta",

					"drop_finish" : function (data) {
						var parent = data.r;
						var treepath = data.o.attr("id").split('.');
						if( treepath.length < 2 ) {
							Modalinfo.info("Query can only be applied on one data category or one data class");
						}
						else {
							while(parent.length != 0  ) {
								resultPaneView.fireSetTreePath(treepath);	
								if(parent.attr('id') == "resultpane" ) {
									setTitlePath(treepath);
									resultPaneView.fireTreeNodeEvent(treepath);	
									return;
								}
								else if(parent.attr('id') == "showquerymeta" ) {
									setTitlePath(treepath);
									resultPaneView.fireShowMetaNode(treepath);	
									return;
								}

//								else if(parent.attr('id') == "displayfilter" ) {
//								setTitlePath(treepath);
//								resultPaneView.fireTreeNodeEvent(treepath);	
//								filterManagerView.fireShowFilterManager(treepath);	
//								return;
//								}

								else if( parent.attr('id') == "saadaqltab" || parent.attr('id') == "saptab" || parent.attr('id') == "taptab") {
									saadaqlView.fireTreeNodeEvent(treepath);	
									sapView.fireTreeNodeEvent(treepath);	
									return;
								}
								parent = parent.parent();
							}
						}
					}
				},
				// Node sorting by DnD blocked
				"crrm" : {"move" : {"check_move" : function (m) {return false; }}
				}
			}); // end of jstree
//			dataTree.bind("select_node.jstree", function (e, data) {
//			Modalinfo.info(data);
//			});
			dataTree.bind("dblclick.jstree", function (e, data) {
				var node = $(e.target).closest("li");
				var id = node[0].id; //id of the selected node					
				var treepath = id.split('.');
				if( treepath.length < 2 ) {
					Modalinfo.info("Query can only be applied on one data category or one data class");
				}
				else {
					Processing.show("Open node " + getTreePathAsKey());
					resultPaneView.fireSetTreePath(treepath);	
					setTitlePath(treepath);
					resultPaneView.fireTreeNodeEvent(treepath);	
					Processing.hide();
				}
			});
		}); // end of ajax
	}

	/**
	 * 
	 */
	var pblc = {};
	pblc.init2 = init2;
	pblc.init = init;
	return pblc;
}();
