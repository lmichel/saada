jQuery.extend({

	SaadaQLModel: function(pmodel){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
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
			listeners.push(list);
		};
		/*
		 * Event processing
		 */
		this.processTreeNodeEvent = function(treepath, andsubmit, defaultquery){
			var params;
			if( treepath.length == 3 ){
				collection = treepath[0];
				category = treepath[1];
				classe = treepath[2];
				params = {query: "ah", name:  classe };
			}
			else if ( treepath.length == 2 ){
				collection = treepath[0];
				category = treepath[1];
				classe = '*'; 
				params = {query: "ah", name:  collection + '.' +category };
			}
			else {
				loggedAlert( treepath.length + " Query can only be applied on one data category or one data class (should never happen here: saadaqlModel.js");
				return;
			}
			showProcessingDialog("Fetching meta data");
			$.getJSON("getmeta", params, function(jsondata) {
				hideProcessingDialog();
				if( processJsonError(jsondata, "Can not get data tree node description") ) {
					hideProcessingDialog();
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
					}
					else {
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
				var query = "Select " + category + " From " + classe + " In " + collection ;
				if( $("#qlimit").val().match(/^[0-9]+$/) ) {
					query += '\nLimit ' + $("#qlimit").val();
				}
				if( defaultquery == undefined ) {
					that.notifyQueryUpdated(query);
				}
				else {
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
			}
			); 
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
			if( histoQuery.length > 0 && histoQuery[histoQuery.length-1].query == query ) {
				return;
			}
			histoQueryPtr = histoQuery.length;
			if( classe != '*') {
				histoQuery[histoQuery.length] = {query: query , treepath:[collection,category,classe]};
			}
			else {
				histoQuery[histoQuery.length] = {query: query , treepath:[collection,category]};
			}

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
				that.processTreeNodeEvent(histoQuery[histoQueryPtr].treepath, false, histoQuery[histoQueryPtr].query);
			}
			else {
				if( histoQueryPtr < 1) {
					return;
				}
				histoQueryPtr--;
				that.processTreeNodeEvent(histoQuery[histoQueryPtr].treepath, false,  histoQuery[histoQueryPtr].query);
			}
			resultPaneView.updateQueryHistoCommands(histoQuery.length, histoQueryPtr);
		};

		/*
		 * Listener notifications
		 */
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributesHandlers, relations, queriableUCDs);
			});
		};
		this.notifyCoordDone = function(key, constr){
			$.each(listeners, function(i){
				listeners[i].coordDone(key, constr);
			});
		};
		this.notifyQueryUpdated= function(query) {
			$.each(listeners, function(i){
				listeners[i].queryUpdated(query);
			});
		};

	}
});
