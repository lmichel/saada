jQuery.extend({

	SaadaQLModel: function(pmodel){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners ;
		/*
		 * What we have to store and play with
		 */
		var attributesHandlers = new Array();
		var queriableUCDs = new Array();
		var relations = new Array();
		var editors = new Array();
		var orberby = '';
		var ucdeditors = new Array();
		var const_key = 1;
		var patternModel = pmodel;			
		var collection = '';
		var classe = '';
		var category = '';
		var histoQuery = new Array();
		var histoQueryPtr = -1;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listener = list;
		};
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(andsubmit, newTreeNode, limit){
			nativeConstraintEditor.fireSetTreepath(globalTreePath);
			patternConstraintEditor.fireSetTreepath(globalTreePath);
			var md = MetadataSource.relations;
			var disabled = new Array();
			var selected = 0;
			if( globalTreePath.category == 'TABLE' ||  globalTreePath.category == 'MISC'||  globalTreePath.category == 'FLATFILE') {
				disabled[disabled.length] = 0;
				selected = 1;
			}
			/*
			 * If the event has been initiated from the data tree, the history pointer is
			 * set the end of the query list
			 */
			if(  newTreeNode ) {
				histoQueryPtr = (histoQuery.length - 1);
			}
			disabled[disabled.length] = 2;					

			$("#saadaqltab").tabs({
				disabled: disabled,
				selected: selected
			});
			queryView.reset("Select " + globalTreePath.category + " From " + globalTreePath.getClassname() + " In " + globalTreePath.schema);
			if(limit != null) {
				queryView.fireAddConstraint("Merged", "limit", [limit]);
			}

			if( andsubmit == true ) {
				resultPaneView.fireSubmitQueryEvent();
			}
			if( md.relations != null ) {
				for( i=0 ; i<md.relations.length ; i++ ) {
					relations[md.relations[i].name] = md.relations[i];
				}
			}

			return;
///////////////////////////////
			Processing.show("Fetching meta data");
			$.getJSON("getmeta", params, function(jsondata) {
				Processing.hide();
				if( Processing.jsonError(jsondata, "Can not get data tree node description") ) {
					Processing.hide();
					return;
				}
				editors = new Array();
				ucdeditors = new Array();
				attributesHandlers = new Array();
				for( var i=0 ; i<jsondata.attributes.length ; i++ ) {
					attributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
				}
				/*
				 * Queriables UCDs are stored in a map (ucd as key, array of units as value)
				 */
				var with_ucd = false;
				queriableUCDs = new Array();
				for(var  i=0 ; i<jsondata.queriableucds.length ; i++ ) {
					var ah = jsondata.queriableucds[i];
					var ucd = ah.ucd;
					if( queriableUCDs[ucd] == null || queriableUCDs[ucd] == undefined) {
						queriableUCDs[ucd] = new Array();
						if( ah.unit != "" ) {
							(queriableUCDs[ucd])[0] = ah.unit;
						}
					} else {
						if( ah.unit != "" ) {
							var found = false;
							for( var u=0 ; u<queriableUCDs[ucd].length ; u++) {
								if( (queriableUCDs[ucd])[u] == ah.unit ) {
									found = true;
									break;
								}
							}
							if( !found ) {
								(queriableUCDs[ucd])[queriableUCDs[ucd].length] = ah.unit;
							}						
						}
					}
					with_ucd = true;									
				}
				relations = new Array();
				if( jsondata.relations != null ) {
					for( i=0 ; i<jsondata.relations.length ; i++ ) {
						relations[jsondata.relations[i].name] = jsondata.relations[i];
					}
				}
				var disabled = new Array();
				var selected = 0;
				if( category == 'TABLE' ||  category == 'MISC'||  category == 'FLATFILE') {
					disabled[disabled.length] = 0;
					selected = 1;
				}
				if( jsondata.relations == null || jsondata.relations.length == 0 ) {
					disabled[disabled.length] = 3;
				}
				if( with_ucd == false ) {
					disabled[disabled.length] = 2;					
				}
				$("#saadaqltab").tabs({
					disabled: disabled,
					selected: selected
				});

				that.notifyInitDone();	
				if( defaultquery == undefined ) {
					/*
					 * Check in UCDS if there a column sorted by default
					 */
					for( kw in attributesHandlers ) {
						ah = attributesHandlers[kw];
						if( ah.ucd.match(/sort.desc/) ){
							that.setOrderBy(kw);
							$('#orderby_des').prop('checked',true);
						} else if( ah.ucd.match(/sort.asc/) ){
							that.setOrderBy(kw);
							$('#orderby_asc').prop('checked',true);
						}
					}
					/*
					 * Set a desc sort by default on oidsaada in order to display first the latest data
					 */
					if( $("#orderby span").length == 0 ) {
						that.setOrderBy("oidsaada");
						$('#orderby_des').prop('checked',true);
					}
//					$("#orderby span").each(function() {
//						if( $(this).text() == '' ) {
//							$(this).text('oidsaada');
//						}
//					});
					query = that.updateQuery();
				} else {
					that.notifyQueryUpdated(defaultquery);
					$("#saadaqltab").tabs({
						selected: 4
					});				

				}
				if( andsubmit == true ) {
					resultPaneView.fireSubmitQueryEvent();
				}
			});

		};

		this.processAttributeEvent= function(uidraggable){
			var kwname = uidraggable.find(".item").text().split(' ')[0];
			var ah = attributesHandlers[kwname];
			var first = true;
			for( k in editors ) {
				first = false;
				break;
			}
			var m = new $.KWConstraintModel(first, ah, this, '');
			var div_key = "kw" +  const_key;
			var v = new $.KWConstraintView(div_key, 'ConstraintsList');
			editors[div_key] =  new $.KWConstraintControler(m, v);
			m.notifyInitDone();
			const_key++;
		};

		this.processOrderByEvent= function(uidraggable){
			var kwname = uidraggable.find(".item").text().split(' ')[0];
			this.setOrderBy(kwname);
		};

		this.setOrderBy= function(kwname){
			var ah = attributesHandlers[kwname];
			if( kwname != null || ah == null) {
				var ah = attributesHandlers[kwname];
				var m = new $.KWConstraintModel(true, { 
					"nameattr" : ah.nameattr 
					, "nameorg" : ah.nameorg
					, "type" : "orderby"
						, "ucd" : ah.ucd
						, "utype" : ah.utype
						, "unit" : ah.unit
						, "comment" : ah.description}
				, this);

				var div_key = "ob" +  const_key;
				var v = new $.KWConstraintView(div_key, 'orderby');
				orderby =  new $.KWConstraintControler(m, v);
				m.notifyInitDone();

			} else {
				$("#orderby").each(function() {
					$(this).html('');
					orderby = null;
				});

			}
		};
		this.sortColumn= function(nameattr, sens) {
			if( sens != null ) {
				this.setOrderBy(nameattr);		
				if( sens == 'asc' ) {
					$('#orderby_asc').prop('checked',true);
				} else {			
					$('#orderby_des').prop('checked',true);
				}
			} else {
				this.setOrderBy(null);		
			}
			this.updateQuery();
			resultPaneView.fireSubmitQueryEvent();
		};
		this.getOrderByParameters = function() {
			var nameattr ='';;
			$("#orderby span").each(function() {
				nameattr = $(this).text();
			});
			return {nameattr: nameattr, asc: ($('#orderby_asc').prop('checked') == true) };
		};
		this.processOIDTableEvent= function(oidtable){
			var ah = attributesHandlers["oidtable"];
			if( ah != undefined) {
				var first = true;
				for( k in editors ) {
					first = false;
					break;
				}
				var m = new $.KWConstraintModel(first, ah, this, '');
				var div_key = "kw" +  const_key;
				var v = new $.KWConstraintView(div_key, 'ConstraintsList');
				editors[div_key] =  new $.KWConstraintControler(m, v);
				m.notifyInitDone();
				m.processEnterEvent("", "=", oidtable);
				$("#" + div_key + "_val").val(oidtable);
				const_key++;
			}
		};

		this.processUCDEvent= function(uidraggable){
			var ucd = uidraggable.find(".item").text().split(' ')[0];
			var first = true;
			for( k in ucdeditors ) {
				first = false;
				break;
			}
			var m = new $.UCDConstraintModel(first, ucd, queriableUCDs[ucd], this, '');
			var div_key = "ucd" +  const_key;
			var v = new $.UCDConstraintView(div_key, 'UCDConstraintsList');
			ucdeditors[div_key] =  new $.UCDConstraintControler(m, v);
			m.notifyInitDone();
			const_key++;
		};

		this.processInputCoord= function(coord, radius){
			var frame = 'J2000,ICRS';

			that.notifyCoordDone("coo" +  const_key, 'isInCircle("' + coord + '", ' + radius + ', ' + frame + ')');
			that.updateQuery();
			const_key++;
		};

		this.processSelectRelation= function(relation) {
			patternModel.initRelation(relations[relation]);
		};

		this.updateQuery = function() {
			/*
			 * Query can not be updated while category/class/collection are not set
			 */
			if( typeof category == 'undefined' ) {
				return ;
			}
			var query = "Select " + category + " From " + classe + " In " + collection ;
			var cq = "";
			$("#CoordList span").each(function() {
				if( cq.length > 0 ) cq += ",\n";
				cq +=  '    ' + $(this).text();
			}); 
			if( cq.length > 0 ) {
				query += "\nWherePosition { \n" + cq + "}";
			}

			cq = "";
			$("#ConstraintsList div").each(function() {
				cq +=  '    ' + editors[$(this).attr('id')].getADQL(false) ;
				if( cq.length > 50 ) cq += '\n';
			}); 
			if( cq.length > 0 ) {
				query += "\nWhereAttributeSaada { \n" + cq + "}";
			}

			cq = "";
			$("#UCDConstraintsList div").each(function() {
				cq +=  '    ' + ucdeditors[$(this).attr('id')].getADQL(false) ;
				if( cq.length > 50 ) cq += '\n';
			}); 
			if( cq.length > 0 ) {
				query += "\nWhereUCD { \n" + cq + "}";
			}

			cq = "";
			$("#patternlist input").each(function() {
				if( cq.length > 0 ) cq += "\n";
				cq += unescape($(this).val());
			});
			if( cq.length > 0 ) {
				query += "\nWhereRelation { \n" + cq + "\n    }";
			}

			$("#orderby span").each(function() {
				query += "\nOrder By " + $(this).text();
				if( $("input[name=sens]:checked").attr("value") == 'des' ) {
					query += " desc";
				}
			});

			if( $("#qlimit").val().match(/^[0-9]+$/) ) {
				query += '\nLimit ' + $("#qlimit").val();
			}

			that.notifyQueryUpdated(query);
		};

		this.processRemoveFirstAndOr = function(key) {
			delete editors[key];
			for( var k in editors ) {
				editors[k].controlRemoveAndOr();
				break;
			}
			delete ucdeditors[key];
			for( var k in ucdeditors ) {
				ucdeditors[k].controlRemoveAndOr();
				break;
			}
		};

		this.processStoreHisto = function(query) {
			console.log( histoQueryPtr  + " "+  histoQuery.length)
			/*
			 * Do not store if the query has not change
			 */
			if( histoQuery.length > 0 && histoQuery[histoQuery.length-1].query == query ) {
				return;
			}
			/*
			 * Do not not store if a previous query is submitted again
			 */
			if( histoQueryPtr > -1 && histoQueryPtr != (histoQuery.length - 1) ) {
				this.setTitle();
				return;
			}
			histoQueryPtr = histoQuery.length;
			histoQuery[histoQuery.length] = {query: query , treepath: jQuery.extend({}, globalTreePath) };

			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.displayHisto = function() {
			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.processHisto = function(direction) {
			if( direction == 'next') {
				if( histoQueryPtr >= (histoQuery.length - 1)) {
					return;
				}
				histoQueryPtr++;
			} else {
				if( histoQueryPtr < 1) {
					return;
				}
				histoQueryPtr--;
			}
			
			this.processTreeNodeEvent(false, false);
			queryView.reset(histoQuery[histoQueryPtr].query);
			$("#saadaqltab").tabs({
				selected: 4
			});
			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		this.setTitle = function(){
			globalTreePath = jQuery.extend({}, histoQuery[histoQueryPtr].treepath);
			$('#titlepath').html(globalTreePath.title);

		};
		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			listener.isInit(attributesHandlers, relations, queriableUCDs);
		};
		this.notifyCoordDone = function(key, constr){
			listener.coordDone(key, constr);
		};
		this.notifyQueryUpdated= function(query) {
			listener.queryUpdated(query);
		};

	}
});
