jQuery.extend({

	PatternView: function(){
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
		}

		this.initForm= function(relation, collAttributesHandlers, classAttributesHandlers, jsoncpclasses){
			that.classSelected(collAttributesHandlers, classAttributesHandlers);
			/*
			 * Display collection attributes
			 */		
			table  = "<ul class=attlist><li class=\"ui-state-default\"><span class=item>Cardinality</span></li>";
			var q = relation.qualifiers;
			for( i=0 ; i<q.length ; i++) {
				table += "<li class=\"ui-state-default\"><span class=item>Qualifier " +q[i] + "</span></li>";
			}
			table += "</ul>";
			$("#patterncardqual").html(table);

			table = "<option>any</option>";
			for( i=0 ; i<jsoncpclasses.length ; i++) {
				table += "<option>" + jsoncpclasses[i]  + "</option>";
			}
			$("#cpclassselect").html(table);

			$(function() {
				$( "#patterncardqual li" ).draggable({
					connectToSortable: "#patternconst",
					helper: "clone",
					revert: "invalid"
				});
				$( "#patternatt li" ).draggable({
					connectToSortable: "#patternconst",
					helper: "clone",
					revert: "invalid"
				});
				$("#patterncardqual" ).sortable({
					revert: "true"
				});
				$("#patternatt" ).sortable({
					revert: "true"
				});

			});
		}

		this.classSelected = function(collAttributesHandlers, classAttributesHandlers) {
			/*
			 * Display collection attributes
			 */
			var table  = "<ul class=attlist>";
			for( i in collAttributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ collAttributesHandlers[i].nameattr 
					+ " (" + collAttributesHandlers[i].type 
					+ ") " +  collAttributesHandlers[i].unit + "</span></li>";
			}
			for( i in classAttributesHandlers  ) {
				table += "<li class=\"ui-state-default\" style=\"white-space: nowrap;\"><span class=item>" 
					+ classAttributesHandlers[i].nameattr 
					+ " (" + classAttributesHandlers[i].type 
					+ ") " +  classAttributesHandlers[i].unit + "</span></li>";
			}
			table += "</ul>";
			$("#patternatt").html(table);			
			$(function() {
				$( "#patternatt li" ).draggable({
					connectToSortable: "#patternconst",
					helper: "clone",
					revert: "invalid"
				});
			});
		}

		this.formReset = function(){			
			$("#patternatt li").remove();
			$("#patterncardqual li").remove();
			$("#cpclassselect").empty();
			$("#patternconst div").remove();
		}

		this.fireAcceptPattern = function() {
			var query = "";
			$.each(listeners, function(i){
				query = listeners[i].getADQL(false);
				rname = listeners[i]. getRelationName();
			});
			var id_root = query.hashCode();
			$('#patternlist').append("<div class=patternitem id=" + id_root + ">");
			$('#' + id_root).html('<span id=' + id_root + '_name>' + rname + '</span>');
			$('#' + id_root).append("<input type=hidden value='" + escape(query) + "'>");
			$('#' + id_root).append(' <a id=' + id_root + '_show class=preview href="javascript:void(0);"></a>');
			$('#' + id_root).append(' <a id=' + id_root + '_close href="javascript:void(0);" class=closekw></a>');
			$('#patternlist').append("</div>");

			$('#' +  id_root + "_close").click(function() {
				$('#' +  id_root).remove();
				saadaqlView.fireUpdateQueryEvent();
			});
			$('#' +  id_root + "_show").click(function() {
				Modalinfo.info(unescape($('#' +  id_root + ' input').val()));
			});

			$("#patternconst div").remove();
			saadaqlView.fireUpdateQueryEvent();			
			$.each(listeners, function(i){
				listeners[i].removeAllConstRef();
			});

		}

		this.fireEnterEvent = function(operator, operand){
			$.each(listeners, function(i){
				listeners[i].controlEnterEvent(operator, operand);
			});
		}
		this.fireAttributeEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlAttributeEvent(uidraggable);
			});
		}
		this.fireSelectClassEvent = function(classe){
			$.each(listeners, function(i){
				listeners[i].controlSelectClassEvent(classe);
			});
		}

	}
});