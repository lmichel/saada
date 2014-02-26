jQuery.extend({

	UCDConstraintModel: function(first, ucd, units, model, def_value){
		var val1;
		var val2;
		var that = this;

		var ucd = ucd;
		var units = units;
		var listeners = new Array();
		var operators = ["=", "!=", ">", "<", "][", "]=[", "[]", "[=]"];
		var andors = new Array();

		this.addListener = function(list){
			listeners.push(list);
		}

		var saadaqlmodel = model;
		var default_value = def_value;
		var operator;
		var operand ;
		var andor ;
		var unit;


		if( first == true ) {
			andors = [];
		}			
		else {
			andors = ["AND", "OR"];
		}


		this.processEnterEvent = function(ao, op, opd, unt) {
			andor = ao;
			if( unt == undefined || unt == '' ) {
				unit = "none";
			}
			else {
				unit = unt;

			}
			if( !that.checkAndFormatNum(op, opd) ) {
				return;

			}
			that.notifyTypomsg(0, operator + ' ' + operand + ' [' + unt + ']');				
			if( andors.lengthlength == 0 ) {
				that.removeAndOr();
			}
			saadaqlmodel.updateQuery();
		}

		this.checkAndFormatNum = function(op, opd) {
			operator = op;
			if( /^\s*$/.test(opd)  ) {
				that.notifyTypomsg(1, 'Operand is required');
				return 0 ;
			}
			else if(  op == '][' || op == ']=[' || op == '[]' || op == '[=]') {

				var regexp = /('.*')\s+(?:and|,)\s+('.*')/i;
				var match = regexp.exec(opd);
				if(  match != null && match.length == 3 ) {
					operand = '(' + match[1] + ' , ' + match[2] + ')';	
					return 1 ;
				}
				else {
					regexp = /([^']+)\s+(?:and|,)\s+([^']+)/i;
					match = regexp.exec(opd);
					if( match != null && match.length == 3 ) {
						if( isNaN(match[1]) ||  isNaN(match[2])) {
							operand = "('" + match[1] + "' , '" + match[2] + "')";	
							return 1 ;
						}
						else {
							operand = '(' + match[1] + ' , ' + match[2] + ')';						
							return 1 ;
						}
					}
				}
				that.notifyTypomsg(1, "Operand like \"'string' and 'string'\" or \"num and num\"");				
				return 0 ;
			}
			else {
				if( ! isNaN(opd) ) {
					operand = opd ;						
					return 1 ;			
				}
				else if(/^'.*'$/.test(opd) ) {
					operand = opd;							
					return 1 ;			
				}
				else if( /^[^']*$/.test(opd) ) {
					operand = "'" + opd + "'";						
					return 1 ;									
				}
				else {
					that.notifyTypomsg(1, "Simple quotes are not supported in operands");									
					return 0 ;
				}
			}
			return 1 ;			
		}

		this.processRemoveConstRef = function(ahname) {
			saadaqlmodel.processRemoveConstRef(ahname);
		}

		this.processRemoveFirstAndOr = function(key) {
			saadaqlmodel.processRemoveFirstAndOr(key);
		}

		this.removeAndOr = function() {
			andor = "";
		}

		this.getADQL = function() {		
			return andor + ' [' + ucd + '] ' + operator + ' ' + operand + ' [' + unit + ']';
		}
		this.notifyInitDone = function(){
			var unit;
			if( units.length == 0 ) {
				unit = "none";
			}
			else {
				unit = units[0];

			}
			$.each(listeners, function(i){
				listeners[i].isInit(ucd, unit, operators, andors, default_value);
			});
		}
		this.notifyTypomsg = function(fault, msg) {
			$.each(listeners, function(i){
				listeners[i].printTypomsg(fault,msg);
			});			
		}

	}
});