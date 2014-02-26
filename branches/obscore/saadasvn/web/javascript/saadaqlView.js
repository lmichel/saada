jQuery.extend({

	SaadaQLView: function(){
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
		this.addListener = function(list){
			listeners.push(list);
		};

		this.fireTreeNodeEvent = function(treepath, andsubmit, defaultQuery){
			$.each(listeners, function(i){
				listeners[i].controlTreeNodeEvent(treepath, andsubmit, defaultQuery);
			});
		};
		this.fireAttributeEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlAttributeEvent(uidraggable);
			});
		};
		this.fireOrderByEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlOrderByEvent(uidraggable);
			});
		};
		this.fireSortColumnEvent = function(nameattr, sens){
			$.each(listeners, function(i){
				listeners[i].controlSortColumnEvent(nameattr, sens);
			});
		};
		this.fireOrderByParameters= function() {
			var retour = null;
			$.each(listeners, function(i){
				retour = listeners[i].controlOrderByParameters();
			});
			return retour;
		};

		this.fireOIDTableEvent = function(oidtable){
			$.each(listeners, function(i){
				listeners[i].controlOIDTableEvent(oidtable);
			});
		};
		this.fireUCDEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlUCDEvent(uidraggable);
			});
		};
		this.fireInputCoordEvent = function(){
			if( $("#coordval").val() == '' || $("#radiusval").val() == '' ) {
				Modalinfo.info("Both position and radius must be given");
				return;
			}
			$.each(listeners, function(i){
				listeners[i].controlInputCoord($("#coordval").val(), $("#radiusval").val());
			});
		};
		this.fireUpdateQueryEvent = function(){
			$.each(listeners, function(i){
				listeners[i].controlUpdateQueryEvent();
			});
		};
		this.fireSelectRelationEvent = function(relation){
			$.each(listeners, function(i){
				listeners[i].controlSelectRelationEvent(relation);
			});
		};
		this.fireHisto = function(direction){
			$("#saadaqltab").tabs({
				selected: 4
			});				
			$.each(listeners, function(i){
				listeners[i].controlHisto(direction);
			});
		};
		this.fireStoreHisto = function(query){
			$.each(listeners, function(i){
				listeners[i].controlStoreHisto(query);
			});
		};
		this.fireDisplayHisto = function(){
			$.each(listeners, function(i){
				listeners[i].controlDisplayHisto();
			});
		};
		this.showProgressStatus = function(){
			Modalinfo.info("Job in progress");
		};
		this.showFailure = function(textStatus){
			Modalinfo.info("view: " + textStatus);
		}	;	
		this.displayResult= function(dataJSONObject){
		};
		this.initForm= function(attributesHandlers, relations, queriableUCDs){
			/*
			 * Reset form
			 */
			$('#CoordList').html('');
			$('#ConstraintsList').html('');
			$('#orderby').html('');
			$('#UCDConstraintsList').html('');
			$('#relationselect').html('');
			$('#patterncardqual').html('');
			$('#cpclassselect').html('');
			$('#patternatt').html('');
			$('#patternconst').html('');
			$('#patternlist').html('');
			/*
			 * Get table columns
			 */
			var table  = "<ul class=attlist>";
			for( i in attributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ attributesHandlers[i].nameattr 
					+ " (" + attributesHandlers[i].type 
					+ ") " + attributesHandlers[i].unit 
					+ "</span></li>";
			}
			table += "</ul>";
			$("#meta").html(table);
			$(function() {
				$("#meta" ).sortable({
					revert: "true"
				});
				$( "div#meta li" ).draggable({ 
					connectToSortable: ".SortableConstraintsList",
					helper: "clone", 
					revert: "invalid"
				});
			});
			/*
			 * Get UCDs list
			 */
			var table  = "<ul class=attlist>";
			for( i in queriableUCDs  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ i;
				if( queriableUCDs[i].length > 0 ) {
					table += "</span><select>";
					table += "<option>-- Stored units --</option>";						
					for( var u=0 ; u<queriableUCDs[i].length ; u++) {
						table += "<option>" + (queriableUCDs[i])[u] + "</option>";						
					}
					table += "</select>";
				}
				else {
					table += " (no stored units)</span>";
				}
				table += "</li>";
			}
			table += "</ul>";
			$("#ucdmeta").html(table);
			$(function() {
				$("#ucdmeta" ).sortable({
					revert: "true"
				});
				$( "div#ucdmeta li" ).draggable({ 
					connectToSortable: "#UCDConstraintsList",
					helper: "clone", 
					revert: "invalid"
				});
			});
			/*
			 * Populate the relation selector
			 */
			var options = "<option>-- Select a relation --</option>";
			for( var i in relations  ) {
				options += "<option>" + relations[i].name + "</option>";
			}
			$("#relationselect").html(options);
		};

		this.coordDone= function(key, constr){
			$('#CoordList').append("<div id=" + key + "></div>");

			$('#' + key).html('<span id=' + key + '_name>' + constr + '</span>');
			$('#' + key).append('<a id=' + key + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' +  key + "_close").click(function() {
				$('#' +  key).remove();
				that.fireUpdateQueryEvent();
			});
		};

		this.queryUpdated= function(query){
			$('#saadaqltext').val(query);
		};
	}
});