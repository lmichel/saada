jQuery.extend({

	FilterManagerModel : /**
	 * 
	 */
		/**
		 * 
		 */
		function() {
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
		var collection = '';
		var category = '';
		var classe = '';

		//var path = $(location).attr('href');
		var path = '';
		if (path.endsWith("#")) path = path.substr(0, path.length - 1);

		var speFields = new Array();
		var collAttributesHandlers = new Array();
		var classAttributesHandlers = new Array();
		var queriableUCDs = new Array();
		var relations = new Array();

		var droppedspefields = new Array();
		var droppednatives = new Array();
		var droppedclassnatives = new Array();
		var droppedrelations = new Array();


		var universal = false;
		var anyCollAtt = false;
		var anyClassAtt = false;
		var anyRelation = false;
		this.addListener = function(list) {
			listeners.push(list);
		};


		this.initSpeFieldsList = function(category) {
			switch (category) {
			case 'ENTRY':
				speFields = [ 'Access', 'Detail', 'Position', 'Position with error', 'Table Header', 'Error (arcsec)', 'Aladin', 'TopCat', 'Simbad', 'VizieR', 'Name', 'Gallery' ];
				break;
			case 'SPECTRUM':
				speFields = [ 'Access', 'Detail', 'DL Link', 'Position', 'Position with error', 'Aladin', 'Visu', 'TopCat', 'Simbad', 'VizieR', 'Name' , 'Gallery'];
				break;
			case 'IMAGE':
				speFields = [ 'Access', 'Detail', 'DL Link', 'Position', 'Position with error', 'Size (deg)', 'Aladin', 'TopCat', 'Simbad', 'VizieR', 'Name', 'Plot', 'Size (deg)' , 'Gallery'];
				break;
			case 'FLATFILE':
				speFields = [ 'Access', 'Preview', 'Detail', 'DL Link', 'Name' , 'Gallery'];
				break;
			case 'TABLE':
				speFields = [ 'Access', 'DL Link', 'Header', 'Table', 'Entries' , 'Gallery'];
				break;
			case 'MISC':
				speFields = ['Access',  'Detail', 'DL Link', 'Name' , 'Gallery'];
				break;
			}
		};

		this.initNativesList = function(jsondata) {

			collAttributesHandlers = new Array();
			classAttributesHandlers = new Array();

			for ( var i = 0; i < jsondata.attributes.length; i++) {
				if (!(jsondata.attributes[i].nameattr).startsWith("_")) {
					collAttributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
				} else {
					classAttributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];					
				}
			}
		};

		this.initQueriablesUCDList = function(jsondata) {

			var with_ucd = false;

			queriableUCDs = new Array();

			for (var i = 0; i < jsondata.queriableucds.length; i++) {
				var ah = jsondata.queriableucds[i];
				var ucd = ah.ucd;
				if (queriableUCDs[ucd] == null || queriableUCDs[ucd] == undefined) {
					queriableUCDs[ucd] = new Array();
					if (ah.unit != "") {
						(queriableUCDs[ucd])[0] = ah.unit;
					}
				} else {
					if (ah.unit != "") {
						var found = false;
						for ( var u = 0; u < queriableUCDs[ucd].length; u++) {
							if ((queriableUCDs[ucd])[u] == ah.unit) {
								found = true;
								break;
							}
						}
						if (!found) {
							(queriableUCDs[ucd])[queriableUCDs[ucd].length] = ah.unit;
						}
					}
				}
				with_ucd = true;
			}

		};

		this.initRelationsList = function(jsondata) {

			relations = new Array();
			if (jsondata.relations != null) {
				for (var i = 0; i < jsondata.relations.length; i++) {
					relations[jsondata.relations[i].name] = jsondata.relations[i];
				}
			}
		};

		this.processShowFilterManager = function() {
			collAttributesHandlers = new Array();
			classAttributesHandlers = new Array();
			queriableUCDs = new Array();
			relations = new Array();
			var params;

			if (globalTreePath.length == 3) {
				collection = globalTreePath[0];
				category = globalTreePath[1];
				classe = globalTreePath[2];
				params = {query: "ah", name:  classe };
			} else if (globalTreePath.length == 2) {
				collection = globalTreePath[0];
				category = globalTreePath[1];
				classe = '*'; 
				params = {query: "ah", name:  collection + '.' +category };
			} else {
				alert(globalTreePath.length + " Query can only be applied on one data category or one data class (should never happen here: filterManagerModel.js");
				return;
			}

			that.initSpeFieldsList(globalTreePath[1]);

			showProcessingDialog();

			$.getJSON("getmeta", params, function(jsonmeta) {
				hideProcessingDialog();
				if (processJsonError(jsonmeta, "Can not get data tree node description")) {
					hideProcessingDialog();
					return;
				}

				that.initNativesList(jsonmeta);
				that.initRelationsList(jsonmeta);
				that.initQueriablesUCDList(jsonmeta);

				var param = "cat=" + category + "&coll=" + collection+ "&saadaclass=" + classe;

				var data = null;
				$.getJSON("getfilter", param, function(jsondata) {
					if( processJsonError(jsondata, "Can not save filter") ) {
						return;
					}
					else {
						data = jsondata;
					}

					if (data == null) {
						data = "{\"saadaclass\": \"" + classe 
						+ "\", \"collection\": [\"" + collection + "\"],\"category\": \"" 
						+ category + "\",\"relationship\": {\"show\": [\"\"],\"query\": [\"Any-Relation\"]}, \"ucd.show\": " +
						"\"false\",\"ucd.query\": \"false\",\"specialField\": [\"\"],\"collections\": " +
						"{\"show\": [\"\"],\"query\": [\"\"]}}";
					}
					universal = $('#unifilter').attr('checked'); 
					that.filterIsReady(data);
				});

			});
		};

		this.processInitExisting = function() {

			var param = "cat=" + category + "&coll=" + collection + "&saadaclass=" + classe;
			var sfname, rname, nname;

			droppedspefields = new Array();
			droppednatives = new Array();
			droppedclassnatives = new Array();
			droppedrelations = new Array();

			$.getJSON("getfilter", param, function(jsondata) {

				if (jsondata != null) {
					for (var i = 0; i < jsondata.specialField.length; i ++) {
						sfname = jsondata.specialField[i];
						var index = jQuery.inArray(sfname, droppedspefields);
						if (index == -1) {
							droppedspefields.push(sfname);
							that.SpeFieldsUpdated(i, sfname);
						}
					}

					if (jsondata.collections.show.length > 0) {
						var anyClass = false;
						var anyColl = false;
						for (var i = 0; i < jsondata.collections.show.length; i ++) {
							nname = jsondata.collections.show[i];
							if( nname == 'Any-Class-Att') {
								anyClass = true;
								that.ClassNativesUpdated(nname, null);									
							} else  if( nname == 'Any-Coll-Att') {
								anyColl = true;
								that.CollNativesUpdated(nname, null);									
							} 
						}
						if( !anyClass || !anyColl ) {
							for (var i = 0; i < jsondata.collections.show.length; i ++) {
								nname = jsondata.collections.show[i];

								if (droppednatives[nname] == null) {
									if( !anyClass && nname.startsWith("_") ) {
										droppedclassnatives[nname] = classAttributesHandlers[nname];
										that.ClassNativesUpdated(nname, droppedclassnatives[nname].nameorg);									
									} else if( !anyColl ) {
										droppednatives[nname] = collAttributesHandlers[nname];
										that.CollNativesUpdated(nname, droppednatives[nname].nameorg);
									}
								}
							}
						}
					}

					if (jsondata.relationship.show.length > 0) {
						var anyRel = false;
						for (var i = 0; i < jsondata.relationship.show.length; i ++) {
							var rname = jsondata.relationship.show[i];
							if( rname == 'Any-Relation') { 
								anyRel = true;
								that.RelationsUpdated(-1, rname);	
								break;
							} 
						}
						if( !anyRel ) {
							for (i = 0; i < jsondata.relationship.show.length; i ++) {
								var rname = jsondata.relationship.show[i];
								if (droppedrelations[rname] == null) {
									droppedrelations[rname] = push(rname);
									that.RelationsUpdated(i, rname);
								}
							}
						}
					}
				}
			});

		};

		this.processSpeFieldEvent = function(uidraggable) {
			var sfname = uidraggable.find(".item").text();

			var index = jQuery.inArray(sfname, droppedspefields);
			if (index == -1) {
				droppedspefields.push(sfname);
				that.SpeFieldsUpdated(droppedspefields.length, sfname);
			}
		};

		this.processCollNativeEvent = function(uidraggable) {
			var nname = uidraggable.find(".hidden").text();

			if (droppednatives[nname] == null) {
				droppednatives[nname] = collAttributesHandlers[nname];
				that.CollNativesUpdated(nname, droppednatives[nname].nameorg);
			}
		};

		this.processClassNativeEvent = function(uidraggable) {
			var nname = uidraggable.find(".hidden").text();

			if (droppedclassnatives[nname] == null) {
				droppedclassnatives[nname] = classAttributesHandlers[nname];
				that.ClassNativesUpdated(nname, droppedclassnatives[nname].nameorg);
			}
		};
		this.processRelationsEvent = function(uidraggable) {
			var rname = uidraggable.find(".item").text();

			var index = jQuery.inArray(rname, droppedrelations);
			if (index == -1) {
				droppedrelations.push(rname);
				that.RelationsUpdated(droppedrelations.length, rname);
			}
		};

		this.processSpeFieldRemove = function(sfname) {
			droppedspefields = jQuery.grep(droppedspefields, function(value) {
				return value != sfname;
			});
		};

		this.processNativeRemove = function(nname) {
			delete droppednatives[nname];
		};

		this.processAllNativeRemove = function() {
			droppednatives = new Array();
		};
		this.processAllClassNativeRemove = function() {
			droppedclassnatives = new Array();
		};

		this.processRelationRemove = function(rname) {
			droppedrelations = jQuery.grep(droppedrelations, function(value) {
				return value != rname;
			});
		};
		this.processAllRelationRemove = function() {
			droppedrelations = new Array();
		};
		/**
		 * Build a JSON filter from instance parameters
		 */
		this.getNewJSONFilter = function() {
			var newfilter = '';
			if (universal) {
				newfilter = "{\n\t \"saadaclass\": \"Any-Class\", \"collection\": [\"Any-Collection\"],";
			} else {
				newfilter = "{\n\t \"saadaclass\": \"" + classe + "\", \"collection\": [\"" + collection + "\"],";
			}
			newfilter += "\"category\": \"" + category + "\",";
			newfilter += "\"relationship\": {";
			newfilter += "\"show\": [";
			if ( anyRelation ) {
				droppedrelations[droppedrelations.length] ="\"Any-Relation\"";
			} else if( !universal ) {
				for (i in droppedrelations) {
					droppedrelations[dropped.length] = "\"" + i+ "\"";
				}
			}
			newfilter += droppedrelations.join(',');
			newfilter += "],";
			newfilter += "\"query\": [\"Any-Relation\"]";
			newfilter += "},";
			newfilter += "\"ucd.show\": \"false\",";
			newfilter += "\"ucd.query\": \"false\",";
			newfilter += "\"specialField\": [";
			iterator = 0;
			$.each(droppedspefields, function(i){
				iterator ++;
				newfilter += "\"" + this + "\"";
				if (iterator < droppedspefields.length) {
					newfilter += ", ";
				}
			});
			newfilter += "],";
			newfilter += "\"collections\": {";
			newfilter += "\"show\": [";

			var dropped = new Array();
			if( anyCollAtt ) {
				dropped[dropped.length] ="\"Any-Coll-Att\"";
			}else {
				for (i in droppednatives) {
					dropped[dropped.length] = "\"" + i+ "\"";
				}
			}

			if( anyCollAtt ) {
				dropped[dropped.length] ="\"Any-Class-Att\"";
			}else if( !universal ) {
				for (i in droppedclassnatives) {
					dropped[dropped.length] = "\"" + i+ "\"";
				}
			}

			newfilter += dropped.join(',');
			newfilter += "],";
			newfilter += "\"query\": [\"\"]}}";
			return newfilter;
		};

		this.processSaveFilter = function() {
			var param = escape(this.getNewJSONFilter());
			var filename = collection + "_" + category;
			var tmppath = path + "setfilter?filter=" + param + "&name=" + filename;
			if (universal) {
				filename = "Any-Collection." + category;;
				droppedclassnatives = new Array();
				droppedrelations = new Array();

			} 
			$.post(tmppath, function(jsondata){
				$.ajax({
					type: "POST",
					url: "setfilter",
					data: { filter: that.getNewJSONFilter(), name: "filename" },
					error: function(jqXHR, textStatus, errorThrown) {
						alert('Cannot save filter: ' + jqXHR.status + "\n" + textStatus+ "\n" + errorThrown);

					} ,
					success: function (jsondata, status)  {
						if (processJsonError(jsondata, "Cannot save filter")) {
							return;
						}
						alert("Filter " + name + " saved.");
					}
				});

			});
		};

		this.processShowFilterPreview = function() {
			var newfilter = this.getNewJSONFilter();
			that.PreviewIsReady(newfilter, collection, category);
		};

		this.processApplyToAllColl = function () {
			universal = !universal;
		};
		this.processAnyCollAtt= function (any) {
			anyCollAtt = any;
		};
		this.processAnyClassAtt= function (any) {
			anyClassAtt = any;
		};
		this.processAnyRelation= function (any) {
			anyRelation = any;
		};
		this.processResetFilter = function () {
			if (!universal) {
				var question = 'Delete the custom filter for '+collection+', '+category+'?';
				var answer = confirm(question);
				if (answer) {
					var tmppath = path + "resetfilter?coll=" + collection + "&cat=" + category;
					$.post(tmppath);
					alert('Filter reset complete.');
				}
			} else {
				var question = 'Delete the custom filters for the category '+category+'?';
				var answer = confirm(question);
				if (answer) {
					var tmppath = path + "resetfilter?coll=all&cat=" + category;
					$.post(tmppath);
					alert('Filter reset complete.');
				}
			}
		};

		this.processResetAll = function () {
			var question = 'Delete all the custom filters?';
			var answer = confirm(question);
			if (answer) {
				var tmppath = path + "resetfilter?coll=all&cat=all";
				$.post(tmppath);
				alert('Filter reset complete.');
			}
		};

		this.filterIsReady = function(jsdata) {
			$.each(listeners, function(i) {
				listeners[i].filterManagerIsReady(jsdata, speFields, collAttributesHandlers, classAttributesHandlers, relations);
			});
		};

		this.SpeFieldsUpdated = function(index, sfname) {
			$.each(listeners, function(i){
				listeners[i].notifySpeFieldsUpdate(index, sfname);
			});
		};
		this.CollNativesUpdated = function(nname, nnameorg) {
			$.each(listeners, function(i){
				listeners[i].notifyCollNativesUpdate(nname, nnameorg);
			});
		};
		this.ClassNativesUpdated = function(nname, nnameorg) {
			$.each(listeners, function(i){
				listeners[i].notifyClassNativesUpdate(nname, nnameorg);
			});
		};

		this.RelationsUpdated = function(index, rname) {
			$.each(listeners, function(i){
				listeners[i].notifyRelationsUpdate(index, rname);
			});
		};

		this.PreviewIsReady = function(filter, collection, category) {
			$.each(listeners, function(i){
				listeners[i].notifyPreviewIsReady(filter, collection, category);
			});
		};
	}
});