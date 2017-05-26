jQuery.extend({

	UCDConstraintView: function(key, constlistid){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		var id_root = key;
		var constlist_id = constlistid;

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


		this.initForm= function(ucd, unit, operators, andors, default_value){
			$('#' + constlist_id).append("<div id=" + id_root + " style='float: none;'>");
			$('#' + id_root).html('');
			/*
			 * AND/OR operators
			 */
			if( andors.length > 0 ) {
				var select='<select id=' + id_root + "_andor style=\"font-size: small;font-family: courier;\">";
				for( i=0 ; i<andors.length; i++ ) {
					var op = andors[i];
					select += '<option value=' + op + '>' +op + '</option>';
				}	
				select += '</select>';
				$('#' + id_root).append(select);
			}
			$('#' + id_root).append('<span id=' + id_root + '_name>' + ucd + '</span>');
			/*
			 * Logical operators
			 */
			var select='<select id=' + id_root + "_op style=\"font-size: small;font-family: courier;\">";
			for( i=0 ; i<operators.length; i++ ) {
				var op = operators[i];
				var selected = '';
				if( op == '>' ) {
					op = '&gt;';
				}
				else if( op == '<' ) op = '&lt;';
				select += '<option value=' + op + ' ' + selected + '>' +op + '</option>';
			}	
			select += '</select>';
			$('#' + id_root).append(select);
			$('#' + id_root).append('<input type=text id=' + id_root 
					+ "_val class=inputvalue style=\"font-size: small;font-family: courier;\" value='" 
					+ default_value + "'>");
			$('#' + id_root).append('<input type=text id=' + id_root 
					+ "_unit class=inputvalue style=\"font-size: small;font-family: courier; width: 100\" value='" 
					+ unit + "'>");
			$('input#' + id_root + '_unit').jsonSuggest(
					{data: units
						, minCharacters: 1
						, onSelect: function(data){				
							that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
								, $('#' +  id_root + "_op option:selected").text()
								, $('#' +  id_root + "_val").val()
								, data.text);}
					});

			$('#' + id_root).append('<a id=' + id_root + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' + constlist_id).append("</div>");		

			$('#' +  id_root + "_close").click(function() {
				$('#' +  id_root).remove();
				that.fireRemoveFirstAndOr(id_root);
				that.fireEnterEvent();
			});

			$('#' +  id_root + "_op").change(function() {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, this.value
						, $('#' +  id_root + "_val").val()
						, $('#' +  id_root + "_unit").val());				
			});
			$('#' +  id_root + "_andor").change(function() {
				that.fireEnterEvent(this.value
						, $('#' +  id_root + "_op option:selected").text()
						, $('#' +  id_root + "_val").val()
						, $('#' +  id_root + "_unit").val()
						);				
			});
			$('#' +  id_root + "_val").keyup(function(event) {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, this.value
						, $('#' +  id_root + "_unit").val());
			});
			$('#' +  id_root + "_val").click(function() {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, this.value
						, $('#' +  id_root + "_unit").val());
			});
			$('#' +  id_root + "_unit").keyup(function(event) {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, $('#' +  id_root + "_val").val()
						, this.value);
			});
			$('#' +  id_root + "_unit").click(function() {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, $('#' +  id_root + "_val").val()
						, this.value);
			});

		}

		this.printTypomsg= function(fault, msg){
			$(".ucdtypomsg").each(function() {
				if(fault) {
					$(this).css('color', 'red');
				}
				else {
					$(this).css('color', 'green');					
				}
				$(this).text(msg);
			});
		}

		this.removeAndOr = function() {
			$('#' + id_root + "_andor" ).remove();
		}

		this.fireRemoveFirstAndOr = function(id_root){
			$.each(listeners, function(i){
				listeners[i].controlRemoveFirstAndOr(id_root);
			});
		}
		this.fireEnterEvent = function(andor, operator, operand, unit){
			$.each(listeners, function(i){
				listeners[i].controlEnterEvent(andor, operator, operand, unit);
			});
		}

		this.fireRemoveConstRef = function(ahname){
			$.each(listeners, function(i){
				listeners[i].controlRemoveConstRef(ahname);
			});
		}
	}
});