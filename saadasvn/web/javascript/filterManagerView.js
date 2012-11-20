jQuery.extend({

	FilterManagerView : function() {
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

		this.fireShowFilterManager = function(treepath) {
			$.each(listeners, function(i) {
				listeners[i].controlShowFilterManager(treepath);
			});
		};

		this.fireInitExistingFields = function () {
			$.each(listeners, function(i){
				listeners[i].controlInitExisting();
			});
		};

		this.fireSpeFieldEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlSpeFieldEvent(uidraggable);
			});
		};

		this.fireCollNativeEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlCollNativeEvent(uidraggable);
			});
		};
		this.fireClassNativeEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlClassNativeEvent(uidraggable);
			});
		};

		this.fireRelationEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlRelationsEvent(uidraggable);
			});
		};

		this.fireRemoveSpeField = function(sfname) {
			$.each(listeners, function(i){
				listeners[i].controlSpeFieldRemoval(sfname);
			});
		};

		this.fireRemoveNative = function(nname) {
			$.each(listeners, function(i){
				listeners[i].controlNativeRemoval(nname);
			});
		};
		this.fireRemoveAllNative = function() {
			$.each(listeners, function(i){
				listeners[i].controlAllNativeRemoval();
			});
		};
		this.fireRemoveAllClassNative = function() {
			$.each(listeners, function(i){
				listeners[i].controlAllClassNativeRemoval();
			});
		};

		this.fireRemoveRelation = function(rname) {
			$.each(listeners, function(i){
				listeners[i].controlRelationRemoval(rname);
			});
		};

		this.fireRemoveAllRelation = function() {
			$.each(listeners, function(i){
				listeners[i].controlAllRelationRemoval();
			});
		};

		this.fireSaveFilter = function() {
			$.each(listeners, function(i){
				listeners[i].controlSaveFilter();
			});
		};

		this.fireApplyToAllColl = function() {
			$.each(listeners, function(i){
				listeners[i].controlApplyToAllColl();
			});
		};
		
		this.fireAnyCollAtt = function(any) {
			$.each(listeners, function(i){
				listeners[i].controlAnyCollAtt(any);
			});
		};
		this.fireAnyClassAtt = function(any) {
			$.each(listeners, function(i){
				listeners[i].controlAnyClassAtt(any);
			});
		};
		this.fireAnyRelation = function(any) {
			$.each(listeners, function(i){
				listeners[i].controlAnyRelation(any);
			});
		};

		this.fireFilterPreview = function() {
			$.each(listeners, function(i){
				listeners[i].controlFilterPreview();
			});
		};

		this.fireResetFilter = function() {
			$.each(listeners, function(i){
				listeners[i].controlResetFilter();
			});
		};

		this.fireResetAll = function() {
			$.each(listeners, function(i){
				listeners[i].controlResetAll();
			});
		};

		this.showFilterManager = function(jsdata, speFields, collAttributesHandlers, classAttributeHandlers, relations) {
			var content ="";

			if ((jsdata.category != null) && (jsdata.collection != null)) {
				content = "<h2>Editing filter of the category " + jsdata.category ;
				if (jsdata.collection != "Any-Collection") {
					content += " from the collection " + jsdata.collection + "</h2>";
				} else {
					content += " from any collection</h2>";
				}
			}

			content += "<input id=\"fallcollatt\" type=checkbox><span class=help>Display all collection level attributes<br>";
			content += "<input id=\"fallclassatt\" type=checkbox><span class=help>Display all class level attributes when it is allowed by the scope of the query<br>";
			content += "<input id=\"fallrelation\" type=checkbox><span class=help>Display all relations";

//			content += "<h4>&nbsp;&bull;&nbsp;&nbsp;Special Fields selector</h4>";
			content += "<h4 id=\"spefieldstitle\" class='detailhead' onclick=\"$(this).next('.selector').slideToggle(500); switchArrow('spefieldstitle');\"> <img src=\"images/tdown.png\"> Special Fields Selector</h4>";

			content += "<div class='selector'>";
			content += "<div id=\"fspefieldsselector\">";
			content += "<div id=\"fspefieldsdrag\"></div>";
			content += "<div id=\"fspefieldsdrop\" class=\"ui-droppable\"></div>";
			content += "</div></div>";
//			content += "<h4>&nbsp;&bull;&nbsp;&nbsp;Natives Attributes selector</h4>";

			content += "<h4 id=\"nativestitle\" class=\"detailhead\" onclick=\"$(this).next('.selector').slideToggle(500); switchArrow('nativestitle');\"> <img src=\"images/tright.png\"> Natives Attributes Selector</h4>";			
			content += "<div class='selector'>";
			content += "<div id=\"fnativeselector\">";
			content += "<div id=\"fnativedrag\"></div>";
			content += "<div id=\"fnativedrop\" class=\"ui-droppable\"></div>";
			content += "</div></div>";

			if( globalTreePath.length == 3) {
				content += "<h4 id=\"classnativestitle\" class=\"detailhead\" onclick=\"$(this).next('.selector').slideToggle(500); switchArrow('classnativestitle');\"> <img src=\"images/tright.png\"> Class Attributes Selector</h4>";			
				content += "<div class='selector'>";
				content += "<div id=\"classfnativeselector\">";
				content += "<div id=\"classfnativedrag\"></div>";
				content += "<div id=\"classfnativedrop\" class=\"ui-droppable\"></div>";
				content += "</div></div>";
			}

//			content += "<h4>&nbsp;&bull;&nbsp;&nbsp;Relationships selector</h4>";
			content += "<h4 id=\"relationstitle\" class=\"detailhead\" onclick=\"$(this).next('.selector').slideToggle(500); switchArrow('relationstitle');\"> <img src=\"images/tright.png\"> Relations Selector</h4>";			
			content += "<div class='selector'>";
			content += "<div id=\"frelselector\">";
			content += "<div id=\"frelationsdrag\"></div>";
			content += "<div id=\"frelationsdrop\" class=\"ui-droppable\"></div>";
			content += "</div></div>";

			content += "<div id=bonusoptions><input type=checkbox id=unifilter OnClick=\"filterManagerView.fireApplyToAllColl();\" title=\"Apply this Filter to this Category regardless of the Collection\" value=\"Apply this filter to any collection\"> <span>Apply to Any Collection</span><br/>";
			content += "<input type=button OnClick=\"filterManagerView.fireFilterPreview();\" title=\"See the JSON file\" value=\"Preview\">&nbsp;&nbsp;";
//			content += "<input type=button OnClick=\"filterManagerView.fireSaveFilter();\" title=\"Save this display filter for the current collection/category\" value=\"Save Filter\">&nbsp;&nbsp;";
			content += "<input type=button OnClick=\"filterManagerView.fireSaveFilter(); resultPaneView.fireSubmitQueryEvent(); $.modal.close();\" title=\"Save and apply this filter to the current query\" value=\"Save & Apply\"><br/>";
			content += "<input type=button OnClick=\"filterManagerView.fireResetFilter(); resultPaneView.fireSubmitQueryEvent(); $.modal.close();\" title=\"Delete the custom filter existing for this collection/category\" value=\"Restore Default Filter\">&nbsp;&nbsp;";
			content += "<input type=button OnClick=\"filterManagerView.fireResetAll(); resultPaneView.fireSubmitQueryEvent(); $.modal.close();\" title=\"Delete all custom filters\" value=\"Restore all Default Filters\"></div>";

			if ($('#filterdiv').length == 0) {
				$(document.documentElement).append("<div id=filterdiv style='width: 99%; display: none;'></div>");
			}

			$('#filterdiv').html(content);

			$('#filterdiv').modal();

			jQuery(".selector").each(function(i) {
				if (i > 0) {
					$(this).hide();
				}
			});
			that.initFieldsList(speFields,  collAttributesHandlers, classAttributeHandlers, relations);
			that.fireInitExistingFields();
		};

		this.initFieldsList = function(speFields, collAttributesHandlers, classAttributeHandlers, relations){

			$('#fspefieldsdrag').html('');
			/* 
			 * Get table columns
			 */
			var table  = "<ul class=attlist>";
			for( i in speFields  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" + speFields[i] + "</span></li>";
			}

			table += "</ul>";
			$("#fspefieldsdrag").html(table);
			$(function() {
				$("#fspefieldsdrop").droppable({
					drop: function(event, ui){
						filterManagerView.fireSpeFieldEvent(ui.draggable);		
					}
				});
				$("#fspefieldsdrag" ).sortable({
					revert: "true"
				});
				$( "div#fspefieldsdrag li" ).draggable({ 
					connectToSortable: "#fspefieldsdrop",
					helper: "clone", 
					revert: "invalid"
				});
			});

			$('#fnativedrag').html('');
			/* 
			 * Get table columns
			 */
			var table  = "<ul class=attlist>";
			for( i in collAttributesHandlers ) {
				table += "<li class=\"ui-state-default\"><span class=name>" 
					+ collAttributesHandlers[i].nameorg 
					+ " (" + collAttributesHandlers[i].type 
					+ ")</span><span class=hidden>" + collAttributesHandlers[i].nameattr + "</span></li>";
			}
			table += "</ul>";
			$("#fnativedrag").html(table);
			$(function() {
				$("#fnativedrop").droppable({
					accept: function() { return true; },
					drop: function(event, ui){
						filterManagerView.fireCollNativeEvent(ui.draggable);		
						$('#fallcollatt').attr('checked', 'false');
					}
				});
				$("#fnativedrag" ).sortable({
					revert: "true"
				});
				$( "div#fnativedrag li" ).draggable({ 
					connectToSortable: "#fnativedrop",
					helper: "clone", 
					revert: "invalid"
				});
			});

			if( globalTreePath.length == 3) {

				$('#classfnativedrag').html('');
				/* 
				 * Get table columns
				 */
				var table  = "<ul class=attlist>";
				for( i in classAttributeHandlers ) {
					table += "<li class=\"ui-state-default\"><span class=name>" 
						+ classAttributeHandlers[i].nameorg 
						+ " (" + classAttributeHandlers[i].type 
						+ ")</span><span class=hidden>" + classAttributeHandlers[i].nameattr + "</span></li>";
				}
				table += "</ul>";
				$("#classfnativedrag").html(table);
				$(function() {
					$("#classfnativedrop").droppable({
						accept: function() { return true; },
						drop: function(event, ui){
							filterManagerView.fireClassNativeEvent(ui.draggable);		
							$('#fallclassatt').attr('checked', false);
						}
					});
					$("#classfnativedrag" ).sortable({
						revert: "true"
					});
					$( "div#classfnativedrag li" ).draggable({ 
						connectToSortable: "#classfnativedrop",
						helper: "clone", 
						revert: "invalid"
					});
				});

			}

			$('#frelationsdrag').html('');
			/* 
			 * Get table columns
			 */
			var table  = "<ul class=attlist>";
			for( i in relations ) {
				table += "<li class=\"ui-state-default\"><span class=item>" + relations[i].name + "</span></li>";
			}

			table += "</ul>";
			$("#frelationsdrag").html(table);
			$(function() {
				$("#frelationsdrop").droppable({
					drop: function(event, ui){
						that.fireRelationEvent(ui.draggable);		
						$('#fallrelation').attr('checked', false);
					}
				});
				$("#frelationsdrag" ).sortable({
					revert: "true"
				});
				$( "div#frelationsdrag li" ).draggable({ 
					connectToSortable: "#frelationsdrop",
					helper: "clone", 
					revert: "invalid"
				});
			});
			$('#fallclassatt').change(function(){
				var check = $(this).attr('checked');
				that.fireAnyClassAtt(check);
				if( check ) {
					that.fireRemoveAllClassNative();
					$('#classfnativedrop').html('');
				}
			});
			$('#fallcollatt').change(function(){
				var check = $(this).attr('checked');
				that.fireAnyCollAtt(check);
				if( check ) {
					that.fireRemoveAllNative();
					$('#fnativedrop').html('');
					$('#fnativedrag li').each(function() {that.fireCollNativeEvent($(this));	});
				}
			});
			$('#fallrelation').change(function(){
				var check = $(this).attr('checked');
				that.fireAnyRelation(check);
				if( check ) {
					that.fireRemoveAllRelation();
					$('#fnativedrop').html('');
					$('#frelationsdrop li').each(function() {that.fireRelationEvent($(this));	});
				}
			});
		};

		this.showFilterPreview = function (filter, collection, category) {
			if (collection == 'Any-Collection') alert('Preview of the filter set for the category ' + category + '\nfrom any collection\n\n' + filter);
			else alert('Preview of the filter set for the category ' + category + '\nfrom the collection ' + collection + '\n\n' + filter);
		}
		;
		this.updateSpeFields = function (index, sfname) {
			$('#fspefieldsdrop').append("<div id=spef" + index + "></div>");
			$('#spef' + index).html('<span id=sf' + index + '_name>' + sfname + '</span>');
			$('#spef' + index).append('<a id=sf' + index + '_close href="javascript:void(0);" class=closekw></a>');
			$('#sf' + index + "_close").click(function() {
				that.fireRemoveSpeField(sfname);
				$('#spef' + index).remove();
			});
		};

		this.updateCollNatives = function (nname, nnameorg) {
			if( nname == 'Any-Coll-Att') {
				$('#fallclassatt').attr('checked', true); 
			} else {
				$('#fnativedrop').append("<div id=natf" + nname + "></div>");
				$('#natf' + nname).html('<span id=nf' + nname + '_name>' + nnameorg + '</span>');
				$('#natf' + nname).append('<a id=nf' + nname + '_close href="javascript:void(0);" class=closekw></a>');
				$('#nf' + nname + "_close").click(function() {
					that.fireRemoveNative(nname);
					$('#fallcollatt').attr('checked', false);
					$('#natf' +  nname).remove();
				});
			}
		};

		this.updateClassNatives = function (nname, nnameorg) {
			if( nname == 'Any-Class-Att') {
				$('#fallcollatt').attr('checked', true); 
			} else {
				$('#classfnativedrop').append("<div id=classnatf" + nname + "></div>");
				$('#classnatf' + nname).html('<span id=classnf' + nname + '_name>' + nnameorg + '</span>');
				$('#classnatf' + nname).append('<a id=classnf' + nname + '_close href="javascript:void(0);" class=closekw></a>');
				$('#classnf' + nname + "_close").click(function() {
					that.fireRemoveNative(nname);
					$('#fallclassatt').attr('checked', false);
					$('#classnatf' +  nname).remove();
				});
			}
		};

		this.updateRelations = function (index, rname) {
			if( rname == 'Any-Relation') {
				$('#fallrelation').attr('checked', true); 
			} else {
				$('#frelationsdrop').append("<div id=relf" + index + "></div>");
				$('#relf' + index).html('<span id=rf' + index + '_name>' + rname + '</span>');
				$('#relf' + index).append('<a id=rf' + index + '_close href="javascript:void(0);" class=closekw></a>');
				$('#rf' + index + "_close").click(function() {
					that.fireRemoveRelation(rname);
					$('#relf' +  index).remove();
					$('#fallrelation').attr('checked', false);
				});
			}
		};
	}
});

