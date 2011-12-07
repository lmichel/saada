jQuery.extend({

	KWConstraintModel: function(first, ah, model, def_value){
		var that = this;

		var listeners = new Array();
		var operators = new Array();
		var andors = new Array();

		this.addListener = function(list){
			listeners.push(list);
		};

		var attributehandler = ah;
		var saadaqlmodel = model;
		var default_value = def_value;
		var operator= '' ;
		var operand = '';
		var andor = '';

		if( ah.nameattr == 'Cardinality' || ah.nameattr.startsWith('Qualifier ')) {
			operators = ["=", "!=", ">", "<", "][", "]=[", "[]", "[=]"];			
			andors = [];
		}
		else if( ah.type == 'Select' ) {
			operators = [];
			andors = [];
		}
		else if( ah.type == 'ADQLPos' ) {
			operators = ["inCircle", "inBox"];
			andors = ["OR", "AND"];

		}
		else if( ah.type != 'String' ) {
			operators = ["=", "!=", ">", "<", "between", 'IS NULL'];
			andors = ['AND', 'OR'];
		}
		else {
			operators = ["=", "!=", "LIKE", "NOT LIKE", 'IS NULL'];
			andors = ['AND', 'OR'];
		}

		if( first == true ) {
			andors = [];
		}
		if( attributehandler.type == 'orderby' ) {
			operators = [];
			andors = [];
		}

		this.processEnterEvent = function(ao, op, opd) {
			andor = ao;
			if( attributehandler.type == 'orderby') {
				saadaqlmodel.updateQuery();
				return;
			}
			else if( attributehandler.type == 'String') {
				if( !that.checkAndFormatString(op, opd) ) {
					return;
				}
			}
			else {
				if( !that.checkAndFormatNum(op, opd) ) {
					return;
				}			
			}
			that.notifyTypomsg(0, operator + ' ' + operand);				
			if( andors .lengthlength == 0 ) {
				that.removeAndOr();
			}
			saadaqlmodel.updateQuery();
		};

		this.checkAndFormatNum = function(op, opd) {
			/*
			 * Case of select items in ADQL
			 */
			if( op == null || op.length == 0 ) {
				operator = "";
				operand = "";
				return 1 ;			
			}
			if( op == 'IS NULL' ) {
				operator = 'IS NULL';
				operand = '';
				return 1;								
			}
			else if( /^\s*$/.test(opd)  ) {
				if( ah.nameattr == 'Cardinality' || ah.nameattr.startsWith('Qualifier ')) {
					that.notifyTypomsg(1, 'Numerical operand requested');
					return 0 ;
				}
				else {
					operator = 'IS NOT NULL';
					operand = '';
					return 1;
				}
			}
			else if( op == 'between' || op == '][' || op == ']=[' || op == '[]' || op == '[=]') {
				var words = opd.split(' ') ;
				if( words.length != 3 || !/and/i.test(words[1]) ||
						words[0].length == 00 || words[2].length == 00 ||
						isNaN(words[0]) || isNaN(words[2]) ) {
					that.notifyTypomsg(1, 'Operand must have the form "num1 and num2" with operator "' + op + '"');
					return 0 ;
				}
				if( op == 'between' ) {
					operator = op;
					operand = words[0] + ' AND ' + words[2];						
				}
				else {
					operator = op;
					operand = '(' + words[0] + ' , ' + words[2] + ')';												
				}
				return 1 ;
			}
			else if( op == 'inCircle' || op == 'inBox')  {
				var area = opd.split(',');
				if( area.length != 3 || isNaN(area[0]) || isNaN(area[1]) || isNaN(area[2]) ) {
					that.notifyTypomsg(1, 'Search area must be like :alpha,delta,size"');					
					return 0 ;
				}
				if( op == 'inCircle') {
					operator = "CIRCLE('ICRS GEOCENTER', '" + area[0]+ "', '" +area[1] + "', " + area[2]+ ")";
					operand = "";
				}
				else {
					operator = "BOX('ICRS GEOCENTER', '" + area[0]+ "', '" +area[1] + "', " + area[2]+  ", " + area[2]  +")";
					operand = "";					
				}
				return 1 ;

			}
			else if( isNaN(opd) ) {
				that.notifyTypomsg(1, 'Single numeric operand required with operator "' + op + '"');				
				return 0 ;
			}
			else {
				operator = op;
				operand = opd;
				return 1 ;			
			}
		};

		this.checkAndFormatString = function(op, opd) {
			if( op == 'IS NULL' ) {
				operator = 'IS NULL';
				operand = '';
				return 1;								
			}
			else if( /^\s*$/.test(opd)  ) {
				operator = 'IS NOT NULL';
				operand = '';
				return 1;				
			}
			else {
				if ( /^\s*'.*'\s*$/.test(opd)  ) {
					operand = opd;
				}
				else {
					operand = "'" + opd + "'";
				}
				operator = op;
				return 1;			
			}
		};

		this.processRemoveConstRef = function(ahname) {
			saadaqlmodel.processRemoveConstRef(ahname);
		};

		this.processRemoveFirstAndOr = function(key) {			
			if( attributehandler.type != 'orderby') {
				saadaqlmodel.processRemoveFirstAndOr(key);
			}
		};

		this.removeAndOr = function() {
			andor = "";
		};

		this.getADQL = function(attQuoted) {		
			if(  ah.nameattr.startsWith('Qualifier ')) {
				return 'Qualifier{ ' + ah.nameattr.split(' ')[1] + operator + ' ' + operand + '}';
			}
			else if( operator.startsWith('CIRCLE') || operator.startsWith('BOX'))  {
				//				CONTAINS(POINT('ICRS GEOCENTER', "_s_ra", "_s_dec"), BOX('ICRS GEOCENTER', 'dsa', 'dsad', 'dsa', 'dsad')) = 'true';
				var coordkw = attributehandler.nameattr.split(' ');
				var bcomp = ( booleansupported )? "'true'" :  "1";
				return andor + " CONTAINS(POINT('ICRS GEOCENTER', \"" + coordkw[0] + "\", \"" +  coordkw[1] + "\"), "
				+ operator + ") = " + bcomp;
			}
			else if( attQuoted ){
				return andor + ' "' + attributehandler.nameattr + '" ' + operator + ' ' + operand;
			}
			else {
				return andor + ' ' + attributehandler.nameattr + ' ' + operator + ' ' + operand;
			}
		};
		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(attributehandler, operators, andors, default_value);
			});
		};
		this.notifyTypomsg = function(fault, msg) {
			$.each(listeners, function(i){
				listeners[i].printTypomsg(fault,msg);
			});			
		};

	}
});