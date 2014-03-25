/**
 * 
 * @param parentDivId: ID of the div containing thge filed list. It must exist before.
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors  
 * @param  constContainerId  : Id of the div containing all the list: must existe before             
 * @param  removeAllHandler   : Handler processing the click on the remove all button          
 */
ConstList_mVc = function(parentDivId, formName, constContainerId, removeAllHandler){
	/**
	 * keep a reference to ourselves
	 */
	var that = this;

	var constListId   = parentDivId + "_constlist";
	var delConstListId   = parentDivId + "_delconstlist";
	var typoMsgId     = parentDivId + "_typoMsgId";
	/**
	 *  parentDiv: JQuery DOM node of the container
	 */
	this.draw = function() {
//		$("#" + constContainerId).append('<div class=constdiv ><fieldset class="constraintlist" id="' + constListId	+  '">'
//				+ '<legend class=help>List of Active Constraints <a href="javascript:void(0);" id=' + delConstListId + ' class=closekw title="Remove all constraints"></a></legend>'
//				+ '<span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
//				+ '</fieldset>'
//				+ '<span style="font-style: italic;color: lightgray;">QL stmt </span><span style="height: 18;" class=typomsg_ok id=' + typoMsgId + '></span>'
//				+ '</div>');
		$("#" + constContainerId).append(
				
				
				  '<div class=constdiv class="constraintlist" style="width: 100%">'
				+ '    <fieldset style="height: 100px; background-color: whitesmoke;">'
				+ '        <legend class=help>List of Active Constraints <a href="javascript:void(0);" id=' + delConstListId + ' class=closekw style="float: none;" title="Remove all constraints"></a></legend>'
				+ '        <div  style="overflow: auto;height: 90px; background-color: transparent; width: 100%" id="' + constListId	+  '"><span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span></div>'

				//+ '        <span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
				+ '    </fieldset>'
				+ '    <div>'
				+ '      <span style="font-style: italic;color: lightgray;">QL stmt </span><span style="height: 18;" class=typomsg_ok id=' + typoMsgId + '></span>'
				+ '    </div>'
				+ '</div>');

		
		$('#' + delConstListId).click(function() {
			removeAllHandler();
		});		
		return constListId;
	};
	
	this.printTypoMsg= function(fault, msg){
		$("#"+ typoMsgId).each(function() {
			if(fault) {
				$(this).attr('class', 'typomsg_ko');
			} else {
				$(this).attr('class', 'typomsg_ok');					
			}
			$(this).text(msg);
		});
	};
	this.isTypoGreen= function(){
		return ( $("#"+ typoMsgId).first().attr('class') ==  'typomsg_ok');					
	};
	this.fireClearAllConst= function() {
		$("#" + constListId + " div.kwConstraint").each(function() {
			$(this).remove();
		});
	};
	this.fireRemoveAllHandler= function() {
		removeAllHandler();
	};
};
