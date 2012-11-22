jQuery.extend({

	PatternModel: function(){
		var that = this;
		var listeners = new Array();
		var relation;
		var collAttributesHandlers = new Array();
		var classAttributesHandlers = new Array();
		var jsoncpclasses = new Array();
		var kweditors = new Array();
		var cardqualeditors = new Array();
		var const_key = 1;


		this.addListener = function(list){
			listeners.push(list);
		}

		var relations ;

		this.getRelationName = function() {
			return relation.name;
		}
		this.initRelation = function(jsonrelation) {
			collAttributesHandlers = new Array();
			classAttributesHandlers = new Array();
			jsoncpclasses = new Array();
			kweditors = new Array();
			cardqualeditors = new Array();
			const_key = 1;
			that.notifyFormReset();

			/*
			 * Store the current relation
			 */
			relation = jsonrelation;
			/*
			 * Get the description of the ending collection
			 */
			params = {query: "ah", name:  relation.ending_collection + '.' +relation.ending_category };
			showProcessingDialog("Fetching meta data");
			$.getJSON("getmeta", params, function(jsondata) {
				hideProcessingDialog();
				if( processJsonError(jsondata, "Can not get data tree node description") ) {
					return;
				}
				for( i=0 ; i<jsondata.attributes.length ; i++ ) {
					collAttributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
				}

				jsoncpclasses = jsondata.classes;
				that.notifyInitDone();

			});
		}

		this.processSelectClassEvent = function(classe) {
			var jsondata =  {"class"     : "nom de classe",
					"attributes": [ 
					               {"name": "CP_" + classe + "ATT1", "type": "String", "ucd": "a.b.c"},
					               {"name": "CP_" + classe + "ATT2", "type": "int"   , "ucd": "a.b.c"},
					               {"name": "CP_" + classe + "ATT3", "type": "double", "ucd": "a.b.c"} ]
			};
			var params;
			if( classe != 'any') {
				params = {query: "ah", name:  classe };

				showProcessingDialog("Fetching meta data");
				$.getJSON("getmeta", params, function(jsondata) {
					hideProcessingDialog();
					if( processJsonError(jsondata, "Can not get data tree node description") ) {
						return;
					}
					for( i in classAttributesHandlers) {
						$("#patternconst span").each(function () {
							if( $(this).text() == i) {
								delete kweditors[$(this).parent().attr("id")];
								$(this).parent().remove();
							}
						});
						$("#patternatt span").each(function () {
							var attname = $(this).text().split(' ')[0];
							if( attname == i) {
								delete classAttributesHandlers[attname];
								$(this).parent().remove();
							}
						});
					}
					classAttributesHandlers = new Array();
					for( i=0 ; i<jsondata.attributes.length ; i++ ) {
						classAttributesHandlers[jsondata.attributes[i].nameattr] = jsondata.attributes[i];
					}
					that.notifyClassSelected();
				});
			}
			else {
				classAttributesHandlers = new Array();				
				that.notifyClassSelected();
			}
		}

		this.processAttributeEvent = function(uidraggable) {
			var kwname  = uidraggable.find(".item").text().split(' ')[0];
			/*
			 * carqual constraint must be unique: name as map key
			 */
			if( kwname.startsWith('Qualifier') || kwname.startsWith('Cardinality') ) {
				var dv = (kwname.startsWith('Cardinality') )? '0': '';
				/*
				 * Const on qual are referenced as 'Qualifier qual'
				 */
				var kwname  = uidraggable.find(".item").text();
				if( cardqualeditors[kwname] ) {
					alert("Constraint '" + kwname + "'can only be applied once");
					return;
				}
				var ah      = {"nameattr": kwname, "type": "double"};
				var m       = new $.KWConstraintModel(true, ah, this, dv);
				var div_key = "pkw" +  const_key;
				var v       = new $.KWConstraintView(div_key, 'patternconst');
				cardqualeditors[kwname]  =  new $.KWConstraintControler(m, v);
			}
			/*
			 * not true for kw constaints
			 */
			else {
				var ah      = collAttributesHandlers[kwname];
				if( ah == null ) {
					ah      = classAttributesHandlers[kwname];
				}
				var first = true;
				for( k in kweditors ) {
					first = false;
					break;
				}
				var m       = new $.KWConstraintModel(first, ah, this, '');
				var div_key = "pkw" +  const_key;
				var v       = new $.KWConstraintView(div_key, 'patternconst');
				kweditors[div_key] =  new $.KWConstraintControler(m, v);
			}			
			m.notifyInitDone();
			const_key++;
		}

		/*
		 * Used to delete and put again a cardqual const which must remain unique
		 */
		this.processRemoveConstRef= function(ahname) {
			delete cardqualeditors[ahname];
		}
		this.processRemoveAllConstRef= function() {
			kweditors = new Array();
			cardqualeditors = new Array();
		}

		this.updateQuery = function() {
			var pattern = "    matchPattern { " + relation.name ;
			$("#patternconst span").each(function() {
				var kwe = cardqualeditors[$(this).text() ];
				if( kwe != null ) {
					pattern += ",\n        " + kwe.getADQL(false);
				}
			});		           

			var cl = $("#cpclassselect option:selected").text();
			if( cl != 'any' ) {
				pattern += ",\n        AssObjClass{" + cl + "}";				
			}
			var cq="";
			$("#patternconst div").each(function() {
				var kwe = kweditors[$(this).attr('id')];
				if( kwe != null ) {
					cq +=  kwe.getADQL(false);
					if( cq.length > 50 ) cq += '\n        ';
				}
			}); 
			if( cq.length > 0 ) {
				pattern += ",\n        AssObjAttSaada{" + cq + "}";
			}

			pattern += "}";
			return pattern;
		}
		
		this.processRemoveFirstAndOr = function(key) {
			delete kweditors[key];

			for( k in kweditors ) {
				kweditors[k].controlRemoveAndOr();
				break;
			}
		}

		this.notifyInitDone = function(){
			$.each(listeners, function(i){
				listeners[i].isInit(relation, collAttributesHandlers, classAttributesHandlers, jsoncpclasses);
			});
		}

		this.notifyFormReset = function(){
			$.each(listeners, function(i){
				listeners[i].isFormReset();
			});
		}
		this.notifyClassSelected = function(){
			$.each(listeners, function(i){
				listeners[i].isClassSelected(collAttributesHandlers, classAttributesHandlers);
			});
		}

	}
});