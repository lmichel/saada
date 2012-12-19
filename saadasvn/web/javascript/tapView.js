jQuery.extend({

	TapView: function(){
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
		/*
		 * Fire external events
		 */
		this.fireTreeNodeEvent = function(treepath, andsubmit){
			$.each(listeners, function(i){
				listeners[i].controlTreeNodeEvent(treepath, andsubmit, null);
			});
		}
		this.fireAttributeEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlAttributeEvent(uidraggable);
			});
		}
		this.fireSelectEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlSelectEvent(uidraggable);
			});
		}
		this.fireInputCoordEvent = function(){
			if( $("#tapcoordval").val() == '' || $("#tapradiusval").val() == '' ) {
				Modalinfo.info("Both position and radius must be given");
				return;
			}

			$.each(listeners, function(i){
				listeners[i].controlInputCoord($("#tapcoordval").val()
						, $("#tapradiusval").val(), $('#tapboxcircle').val());
			});
		}
		this.fireAlphaEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlAlphaEvent(uidraggable);
			});
		}
		this.fireDeltaEvent = function(uidraggable){
			$.each(listeners, function(i){
				listeners[i].controlDeltaEvent(uidraggable);
			});
		}
		this.fireUpdateQueryEvent = function(){
			$.each(listeners, function(i){
				listeners[i].controlUpdateQueryEvent();
			});
		}
		this.fireSubmitQueryEvent = function(){
			$.each(listeners, function(i){
				listeners[i].controlSubmitQueryEvent();
			});
		}
		this.fireRefreshJobList = function(){
			$('#tapjobs').html('');
			$.each(listeners, function(i){
				listeners[i].controlRefreshJobList();
			});
		}
		this.fireJobAction = function(jid){
			$.each(listeners, function(i){
				listeners[i].controlJobAction(jid);
			});
		}
		this.fireDownloadVotable = function(jid){
			$.each(listeners, function(i){
				listeners[i].controlDownloadVotable(jid);
			});
		}
		this.fireCheckJobCompleted= function(jid){
			$.each(listeners, function(i){
				listeners[i].controlCheckJobCompleted(jid);
			});
		}
		this.fireSampBroadcast= function(jid){
			$.each(listeners, function(i){
				listeners[i].controlSampBroadcast(jid);
			});
		}
		/*
		 * Local processing
		 */

		this.showProgressStatus = function(){
			Modalinfo.info("Job in progress");
		}
		this.showFailure = function(textStatus){
			Modalinfo.info("view: " + textStatus);
		}		
		this.displayResult= function(dataJSONObject){
		}
		this.initForm= function(attributesHandlers, selectAttributesHandlers){
			/*
			 * Reset form
			 */
			//$('#adqltext').val('');
			$('#tapconstraintlist').html('');
			$('#kwalpha').html('');
			$('#kwdelta').html('');
			$('#attlist').html('');
			$('#tapselectlist').html('');
			$('#tapselectmeta').html('');
			/*
			 * Get table columns for where clause
			 */
			var table  = "<ul class=attlist>";
			for( i in attributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ attributesHandlers[i].nameattr 
					+ " (" + attributesHandlers[i].type 
					+ ") " + attributesHandlers[i].unit 
					+ "</span></li>";
			}
			table += "</select>";
			$("#tapmeta").html(table);
			$(function() {
				$("#tapmeta" ).sortable({
					revert: "true"
				});
				$( "div#tapmeta li" ).draggable({
					connectToSortable: "#tapconstraintslist",
					helper: "clone",
					revert: "invalid"
				});

			});
			/*
			 * Get table columns for select clause
			 */
			var table  = "<ul class=attlist>";
			for( i in selectAttributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ selectAttributesHandlers[i].nameattr 
					+ " (" + selectAttributesHandlers[i].type 
					+ ") " + selectAttributesHandlers[i].unit 
					+ "</span></li>";
			}
			table += "</select>";
			$("#tapselectmeta").html(table);
			$(function() {
				$("#tapselectmeta" ).sortable({
					revert: "true"
				});
				$( "div#tapselectmeta li" ).draggable({
					connectToSortable: "#tapselectlist",
					helper: "clone",
					revert: "invalid"
				});

			});
			$("#taptab").tabs({
				selected: 2
			});
		}

		this.coordDone= function(key, constr){
			$('#CoordList').append("<div id=" + key + "></div>");

			$('#' + key).html('<span id=' + key + '_name>' + constr + '</span>');
			$('#' + key).append('<a id=' + key + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' +  key + "_close").click(function() {
				$('#' +  key).remove();
				that.fireUpdateQueryEvent();
			});
		}

		this.queryUpdated= function(query){
			$('#adqltext').val(query);
		}

		this.jobView= function(jobview){
			jobview.fireInitForm('tapjobs');
		}

		this.fireDisplayHisto = function(){
			var result = '';
			result += '<img src="images/histoleft-grey.png">';
			result += '<img src="images/historight-grey.png">';	
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		}
	}
});