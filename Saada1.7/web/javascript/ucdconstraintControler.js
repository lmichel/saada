jQuery.extend({

	UCDConstraintControler: function(model, view){
		/**
		 * listen to the view
		 */
		var vlist = {
			controlEnterEvent : function(andor, operator, operand, unit){
				model.processEnterEvent(andor, operator, operand, unit);
			},
			controlRemoveConstRef : function(operator, operand){
				model.processRemoveConstRef(operator, operand);
			},
			controlRemoveFirstAndOr: function(key){
				model.processRemoveFirstAndOr(key);
			}
		}
		view.addListener(vlist);

		var mlist = {
			isInit : function(ucd, unit, operators, andors, default_value){
				view.initForm(ucd, unit, operators, andors, default_value);
			},
			printTypomsg: function(fault, msg){
				view.printTypomsg(fault,  msg);
			}
		}
		model.addListener(mlist);
		
		this.controlRemoveAndOr= function() {
			model.removeAndOr();
			view.removeAndOr();
		}
		this.getADQL = function() {
			return model.getADQL();
		}
	}
});
