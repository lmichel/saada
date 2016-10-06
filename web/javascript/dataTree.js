DataTree = function () {
	var cache = new Object;
	var jsDataTree;
	var loadedNodes = {};
	var init = function() {

		Processing.show("Building the Data Tree");
		$.getJSON("getmeta", {query: "datatree" }, function(jsdata) {
			var classNodeIds = [];
			if( Processing.jsonError(jsdata, "Cannot make data tree") ) {
				return;
			}
			jsDataTree = jsdata
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
						var id  = $(this).attr("id");
						var tp = id.split('_DoT_');
						if( loadedNodes[id] != null ) {
							/*
							 * The timeout is to give time to the progress popup to be displayed
							 * Otherwise, the jsDataTree keeps all browser resources and the interface seems to be crashed
							 */
							Processing.show("Inserting " + loadedNodes[id].children.length +  " class nodes. Can make you interface like frozen.");
							setTimeout(function(){
								loadClasse(id, loadedNodes[id].children);
							    loadedNodes[id] = null;
							    Processing.hide();
							    resultPaneView.fireTreeNodeEvent(tp);
							    }, 500);
						} else {
							resultPaneView.fireTreeNodeEvent(tp);
						}
					});				
					/*
					 * If there are more than 10 classes, they will be displayed once the user opens the node.
					 * That avoid the data tree to take too long to be built
					 */
					if( child.children.length > 10 ) {
						loadedNodes[id] = child;
					} else if( child.children.length > 0 ) {
						loadClasse(id, child.children);
					}
				}

			}
			layoutPane.sizePane("west", $("#treedisp").width()) ;
			layoutPane.sizePane("south", '10%') ;
			Processing.hide();

			return

		}); // end of ajax
	}
	
	loadClasse = function(parentId, jsonClassDescription){
		var parent = $("#" + parentId);
		var classNodeIds = [];
		for( var d=0 ; d<jsonClassDescription.length ; d++){
			var classe = jsonClassDescription[d];
			var ctreepath = classe.attr.id.split('.');
			var cid = classe.attr.id.replace(/\./g, '_DoT_');
			$("div#treedisp").jstree("create_node"
					, parent
					, false
					, {"data" : {"icon":"images/blank.png", "attr":{"id": cid, "title": "Click to display the content of this data class"}, "title" : classe.data},
						"state": "closed",
						"attr" :{"id":cid}}
					,false
					,true);   
			classNodeIds.push(cid);
		}
		/*
		 * Inserting the meta node after the tree is complete is quite more faster the doing it while the tre building
		 */
		for(var n=0 ; n<classNodeIds.length ; n++) {
			var node = $("a#" + classNodeIds[n]);
			node.before("<img 'Click to get the description' class=infoanchor id='" + classNodeIds[n] + "' src='images/metadata.png'></img>");
			
			node.click(function(){	
				var tp  = $(this).attr("id").split('_DoT_');
				resultPaneView.fireSetTreePath(tp);	
				resultPaneView.fireTreeNodeEvent(tp);
				//setTitlePath(tp);
				});
			$("a#" + classNodeIds[n] + " ins").remove();
			$("img#" + classNodeIds[n]).click(function(){resultPaneView.fireShowMetaNode($(this).attr("id").split('_DoT_'));	});
		}
	}
	/**
	 * 
	 */
	var pblc = {};
	pblc.init = init;
	return pblc;
}();
