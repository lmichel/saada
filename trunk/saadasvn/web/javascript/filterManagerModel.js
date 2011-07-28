jQuery.extend({

	FilterManagerModel : function() {
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		var dataJSONObject;
		/**
		 * Query displayed data are coming from
		 */
		var current_query = "";
		var treePath = new Array();

		/*
		 * What we have to store and play with
		 */
		var collection;
		var category;
		
		var path = $(location).attr('href');
		if (path.endsWith("#")) path = path.substr(0, path.length - 1);

		var speFields = new Array();
		var attributesHandlers = new Array();
		var queriableUCDs = new Array();
		var relations = new Array();

		var droppedspefields = new Array();
		var droppednatives = new Array();
		var droppedrelations = new Array();

		var spefieldkey = 1;
		var nativekey = 1;
		var relationkey = 1;
		
		var currentfilter;
		var universal = false;
		/**
		 * add a listener to this view
		 */
		
		String.prototype.startsWith = function(str) {
			return (this.match("^"+str)==str);
		}
		
		String.prototype.endsWith = function(str) {
			return (this.match(str+"$")==str);
		}
			
		this.addListener = function(list) {
			listeners.push(list);
		}

		this.setTreePath = function(treepath) {
			treePath = treepath;
		}

		this.initSpeFieldsList = function(category) {
			switch (category) {
			case 'ENTRY':
				speFields = [ 'Detail', 'Position', 'Position with error', 'Table Header', 'Error (arcsec)', 'Aladin', 'TopCat', 'Simbad', 'VizieR', 'Name' ];
				break;
			case 'SPECTRUM':
				speFields = [ 'Detail', 'DL Link', 'Position', 'Position with error', 'Aladin', 'Visu', 'TopCat', 'Simbad', 'VizieR', 'Name' ];
				break;
			case 'IMAGE':
				speFields = [ 'Detail', 'DL Link', 'Position', 'Position with error', 'Size (deg)', 'Aladin', 'TopCat', 'Simbad', 'VizieR', 'Name', 'Plot', 'Size (deg)' ];
				break;
			case 'FLATFILE':
				speFields = [ 'Preview', 'Detail', 'DL Link', 'Name' ];
				break;
			case 'TABLE':
				speFields = [ 'DL Link', 'Header', 'Table', 'Entries' ];
				break;
			case 'MISC':
				speFields = [ 'Detail', 'DL Link', 'Name' ];
				break;
			}
		}
		
		this.initNativesList = function(jsondata) {
			
			attributesHandlers = new Array();
			
			for (i = 0; i < jsondata.attributes.length; i++) {
				if (!(jsondata.attributes[i].nameattr).startsWith("_")) attributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
			}
		}
		
		this.initQueriablesUCDList = function(jsondata) {
			
			var with_ucd = false;
			
			queriableUCDs = new Array();
			
			for (i = 0; i < jsondata.queriableucds.length; i++) {
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
			
		}
		
		this.initRelationsList = function(jsondata) {

			relations = new Array();
			if (jsondata.relations != null) {
				for (i = 0; i < jsondata.relations.length; i++) {
					relations[jsondata.relations[i].name] = jsondata.relations[i];
				}
			}
		}

		this.processShowFilterManager = function(treepath) {
			attributesHandlers = new Array();
			queriableUCDs = new Array();
			relations = new Array();
			
			var jsondata;
			var params;
			
			if (treepath.length == 3) {
				collection = treepath[0];
				category = treepath[1];
				classe = treepath[2];
				params = {query: "ah", name:  classe };
			} else if (treepath.length == 2) {
				collection = treepath[0];
				category = treepath[1];
				classe = '*'; 
				params = {query: "ah", name:  collection + '.' +category };
			} else {
				alert(treepath.length + " Query can only be applied on one data category or one data class (should never happen here: filterManagerModel.js");
				return;
			}

			that.initSpeFieldsList(treepath[1]);
			
			showProcessingDialog();
			
			$.getJSON("getmeta?", params, function(jsondata) {
				hideProcessingDialog();
				if (processJsonError(jsondata, "Can not get data tree node description")) {
					hideProcessingDialog();
					return;
				}
				
				that.initNativesList(jsondata);
				that.initRelationsList(jsondata);
				that.initQueriablesUCDList(jsondata);
				
				var param = "cat=" + category + "&coll=" + collection;
				
				var data = null;
				$.getJSON("getfilter?", param, function(jsondata) {
					data = jsondata;
				});
				
				if (data == null) {
					data = "{\"collection\": [\"" + collection + "\"],\"category\": \"" + category + "\",\"relationship\": {\"show\": [\"\"],\"query\": [\"Any-Relation\"]}, \"ucd.show\": " +
					"\"false\",\"ucd.query\": \"false\",\"specialField\": [\"\"],\"collections\": " +
					"{\"show\": [\"\"],\"query\": [\"\"]}}";
				}

				universal = $('#unifilter').attr('checked'); 
				that.filterIsReady(data, speFields, attributesHandlers, relations);
			});
		}
		
		this.processInitExisting = function() {

			var param = "cat=" + category + "&coll=" + collection;
			var sfname, rname, nname;

			droppedspefields = new Array();
			droppednatives = new Array();
			droppedrelations = new Array();
			
			$.getJSON("getfilter?", param, function(jsondata) {
				
				if (jsondata != null) {
					for (i = 0; i < jsondata.specialField.length; i ++) {
						sfname = jsondata.specialField[i];
						var index = jQuery.inArray(sfname, droppedspefields);
						if (index == -1) {
							droppedspefields.push(sfname);
							that.SpeFieldsUpdated(i, sfname);
						}
					}
					
					if (jsondata.collections.show.length > 0) {
						for (i = 0; i < jsondata.collections.show.length; i ++) {
							nname = jsondata.collections.show[i];

							if (droppednatives[nname] == null) {
								droppednatives[nname] = attributesHandlers[nname];
								that.NativesUpdated(nname, droppednatives[nname].nameorg);
							}
						}
					}
					
					for (i = 0; i < jsondata.relationship.show.length; i ++) {
						rname = jsondata.relationship.show[i];
						if (rname != 'Any-Relation') {
							var index = jQuery.inArray(rname, droppedrelations);
							if (index == -1) {
								droppedrelations.push(rname);
								that.RelationsUpdated(i, rname);
							}
						} else {
							var ind = 0;
							for (j in relations) {
								var index = jQuery.inArray(j, droppedrelations);
								if (index == -1) {
									droppedrelations.push(j);
									that.RelationsUpdated(ind, j);
								}
								ind ++;
							}
						}
					}
				}
			});
			
		}

		this.processSpeFieldEvent = function(uidraggable) {
			var sfname = uidraggable.find(".item").text();
			
			var index = jQuery.inArray(sfname, droppedspefields);
			if (index == -1) {
				droppedspefields.push(sfname);
				that.SpeFieldsUpdated(droppedspefields.length, sfname);
			}
		}

		this.processNativeEvent = function(uidraggable) {
			var nname = uidraggable.find(".hidden").text();
			
			if (droppednatives[nname] == null) {
				droppednatives[nname] = attributesHandlers[nname];
				that.NativesUpdated(nname, droppednatives[nname].nameorg);
			}
		}

		this.processRelationsEvent = function(uidraggable) {
			var rname = uidraggable.find(".item").text();

			var index = jQuery.inArray(rname, droppedrelations);
			if (index == -1) {
				droppedrelations.push(rname);
				that.RelationsUpdated(droppedrelations.length, rname);
			}
		}
		
		this.processSpeFieldRemove = function(sfname) {
			droppedspefields = jQuery.grep(droppedspefields, function(value) {
				return value != sfname;
			});
		}
		
		this.processNativeRemove = function(nname) {
			delete droppednatives[nname];
		}
		
		this.processRelationRemove = function(rname) {
			droppedrelations = jQuery.grep(droppedrelations, function(value) {
				return value != rname;
			});
		}
		
		this.processSaveFilter = function() {
			if (universal) {
				var newfilter = "{\n\t \"collection\": [\"Any-Collection\"],";
			} else {
				var newfilter = "{\n\t \"collection\": [\"" + collection + "\"],";
			}
			newfilter += "\"category\": \"" + category + "\",";
			newfilter += "\"relationship\": {";
			newfilter += "\"show\": [";
			if (universal) {
				newfilter += "\"Any-Relation\"";
			} else {
				var iterator = 0;
				$.each(droppedrelations, function(i){
					iterator ++;
					newfilter += "\"" + this + "\"";
					if (iterator < droppedrelations.length) {
						newfilter += ", ";
					}
				});
			}
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
			
			var length = 0;
			for (i in droppednatives) {
				length++; 
			}
			
			var iterator = 0;
			for (i in droppednatives) { 
				iterator ++; 
				newfilter += "\"" + i + "\"";
				if (iterator < length) {
					newfilter += ", ";
				}
			}
			
			newfilter += "],";
			newfilter += "\"query\": [\"\"]}}";
			var param = escape(newfilter);

			var filename = collection + "_" + category;
			var tmppath = path + "setfilter?filter=" + param + "&name=" + filename;
			if (universal) {
				var answer = confirm('This filter will be applied to all the collections. If you created any other filter for the category ' + category +' it will be overwritten. \n Are you sure you want to save ?');
				if (answer) {
					$.post(tmppath);
					alert('Filter saved.');
				}
			} else {
				$.post(tmppath);
				alert('Filter saved.');
			}
		}
		
		this.processShowFilterPreview = function() {
			if (universal) {
				var newfilter = "{\n\t \"collection\": [\"Any-Collection\"],";
			} else {
				var newfilter = "{\n\t \"collection\": [\"" + collection + "\"],";
			}
			newfilter += "\n\t\"category\": \"" + category + "\",";
			newfilter += "\n\t\"relationship\": {";
			newfilter += "\n\t\t\"show\": [";
			if (universal) {
				newfilter += "\"Any-Relation\"";
			} else {
				var iterator = 0;
				$.each(droppedrelations, function(i){
					iterator ++;
					newfilter += "\"" + this + "\"";
					if (iterator < droppedrelations.length) {
						newfilter += ", ";
					}
				});
			}
			newfilter += "],";
			newfilter += "\n\t\t\"query\": [\"Any-Relation\"]";
			newfilter += "\n\t},";
			newfilter += "\n\t\"ucd.show\": \"false\",";
			newfilter += "\n\t\"ucd.query\": \"false\",";
			newfilter += "\n\t\"specialField\": [";
			iterator = 0;
			$.each(droppedspefields, function(i){
				iterator ++;
				newfilter += "\"" + this + "\"";
				if (iterator < droppedspefields.length) {
					newfilter += ", ";
				}
			});
			newfilter += "],";
			newfilter += "\n\t\"collections\": {";
			newfilter += "\n\t\t\"show\": [";
			
			var length = 0;
			for (i in droppednatives) {
				length++; 
			}
			
			var iterator = 0;
			for (i in droppednatives) { 
				iterator ++; 
				newfilter += "\"" + i + "\"";
				if (iterator < length) {
					newfilter += ", ";
				}
			}
			
			newfilter += "],";
			newfilter += "\n\t\t\"query\": [\"\"]\n\t}\n}";
			that.PreviewIsReady(newfilter, collection, category);
		}
		
		this.processApplyToAllColl = function () {
			universal = !universal;
		}
		
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
		}
		
		this.processResetAll = function () {
			var question = 'Delete all the custom filters?';
			var answer = confirm(question);
			if (answer) {
				var tmppath = path + "resetfilter?coll=all&cat=all";
				$.post(tmppath);
				alert('Filter reset complete.');
			}
		}
		
		this.filterIsReady = function(jsdata, speFields, attributesHandlers, relations) {
			$.each(listeners, function(i) {
				listeners[i].filterManagerIsReady(jsdata, speFields, attributesHandlers, relations);
			});
		}
		
		this.SpeFieldsUpdated = function(index, sfname) {
			$.each(listeners, function(i){
				listeners[i].notifySpeFieldsUpdate(index, sfname);
			});
		}
		
		this.NativesUpdated = function(nname, nnameorg) {
			$.each(listeners, function(i){
				listeners[i].notifyNativesUpdate(nname, nnameorg);
			});
		}
		
		this.RelationsUpdated = function(index, rname) {
			$.each(listeners, function(i){
				listeners[i].notifyRelationsUpdate(index, rname);
			});
		}
		
		this.PreviewIsReady = function(filter, collection, category) {
			$.each(listeners, function(i){
				listeners[i].notifyPreviewIsReady(filter, collection, category);
			});
		}
	}
});