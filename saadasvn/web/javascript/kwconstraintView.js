jQuery.extend({

	KWConstraintView: function(key, constlistid){
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
		};


		this.initForm= function(ah, operators, andors, default_value){
			$('#' + constlist_id).append("<div id=" + id_root + " style='float: none;'>");
			$('#' + id_root).html('');
			/*
			 * AND/OR operators
			 */
			if( andors.length > 0 ) {
				var select='<select id=' + id_root + "_andor style=\"font-size: small;font-family: courier;\">";
				for( var i=0 ; i<andors.length; i++ ) {
					var op = andors[i];
					select += '<option value=' + op + '>' +op + '</option>';
				}	
				select += '</select>';
				$('#' + id_root).append(select);
			}
			$('#' + id_root).append('<span id=' + id_root + '_name>' + ah.nameattr + '</span>');
			/*
			 * Logical operators
			 */
			if( operators.length > 0 ) {
				var select='<select id=' + id_root + "_op style=\"font-size: small;font-family: courier;\">";
				for( i=0 ; i<operators.length; i++ ) {
					var op = operators[i];
					var selected = '';
					if( op == '>' ) {
						op = '&gt;';
						if( ah.nameattr == 'Cardinality' ) {
							selected = 'selected';
						} 
					}
					else if( op == '<' ) {
						op = '&lt;';
					}
					select += '<option value="' + op + '" ' + selected + '>' +op + '</option>';
				}	

				select += '</select>';
				$('#' + id_root).append(select);
			}

			if( operators.length > 0 ) {
				$('#' + id_root).append('<input type=text id=' + id_root 
						+ "_val class=inputvalue style=\"font-size: small;font-family: courier;\" value='" 
						+ default_value + "'>");
			}
			$('#' + id_root).append('&nbsp;<a id=' + id_root + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' + constlist_id).append("</div>");		

			$('#' +  id_root + "_close").click(function() {
				$('#' +  id_root).remove();
				that.fireRemoveFirstAndOr(id_root);
				if( ah.nameattr.startsWith('Qualifier') || ah.nameattr.startsWith('Cardinality')) {
					that.fireRemoveConstRef(ah.nameattr);
				}
				that.fireEnterEvent();
			});

			$('#' +  id_root + "_op").change(function() {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, this.value
						, $('#' +  id_root + "_val").val());				
			});
			$('#' +  id_root + "_andor").change(function() {
				that.fireEnterEvent(this.value
						, $('#' +  id_root + "_op option:selected").text()
						, $('#' +  id_root + "_val").val());				
			});
			$('#' +  id_root + "_val").keyup(function(event) {
				/*
				 * Run the query is CR is typed in a KW editor
				 */
				if (event.which == '13') {
					if(  $('span.typomsg').css('color') == 'green') {
						resultPaneView.fireSubmitQueryEvent();
					}
					else {
						alert("Current contraint is not valid: can not run the query");
					}
				}
				else {
					that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
							, $('#' +  id_root + "_op option:selected").text()
							, this.value);
				}
			});
			$('#' +  id_root + "_val").click(function(event) {
				that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
						, $('#' +  id_root + "_op option:selected").text()
						, this.value);

			});


			that.fireEnterEvent($('#' +  id_root + "_andor option:selected").text()
					,$('#' +  id_root + "_op option:selected").text()
					,$('#' +  id_root + "_val").val());
		};

		this.printTypomsg= function(fault, msg){
			$(".typomsg").each(function() {
				if(fault) {
					$(this).css('color', 'red');
				}
				else {
					$(this).css('color', 'green');					
				}
				$(this).text(msg);
			});
		};

		this.removeAndOr = function() {
			$('#' + id_root + "_andor" ).remove();
		};

		this.fireRemoveFirstAndOr = function(id_root){
			$.each(listeners, function(i){
				listeners[i].controlRemoveFirstAndOr(id_root);
			});
		};
		this.fireEnterEvent = function(andor, operator, operand){
			$.each(listeners, function(i){
				listeners[i].controlEnterEvent(andor, operator, operand);
			});
		};

		this.fireRemoveConstRef = function(ahname){
			$.each(listeners, function(i){
				listeners[i].controlRemoveConstRef(ahname);
			});
		};
	}
});