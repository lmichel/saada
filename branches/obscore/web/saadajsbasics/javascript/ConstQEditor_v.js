/**
 * ConstQEditor_mVc: View of the constraint query editor
 * 
 * @param parentDivId : ID of the div containing the query editor
 * @param formName    : Name of the form. Although internal use must be 
 *                      set from outside to avoi conflict by JQuery selectors 
 * @param queryview   : JS object supposed to take the constraint locally edited.
 *                      It supposed to have a public method names fireConstQuery(const)
 *                      where const is an object like {columnConst:"SQL stmt", orderConst:"att name"}
 *                     
 * @returns {ConstQEditor_mVc}
 */
function ConstQEditor_mVc(params /*{parentDivId,formName,queryView}*/){
	var that = this;
	this.fieldsLoaded = false;
	this.dataTreePath = null; // instance of DataTreePath
	/**
	 * who is listening to us?
	 */
	this.listeners = new Array();
	this.queryView = params.queryView;
	/**
	 * DOM references
	 */
	this.parentDiv = $("#" + params.parentDivId );
	this.constContainerId   = params.parentDivId + "_constcont";
	this.constListId   = '';
	this.formName = params.formName;
	this.orderById     = params.parentDivId + "_orderby";
	this.fieldListView = new FieldList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	         , orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	         }
	);
	this.constListView = new ConstList_mVc(params.parentDivId
			, this.formName
			, this.constContainerId
			, function(ahName){ that.fireClearAllConst();}
	);
	this.orderByView = new 	OrderBy_mVc(params.parentDivId
			, this.formName
			, this.constContainerId
			, function(ahName){ that.fireOrderBy(ahName);}
	);
};

ConstQEditor_mVc.prototype = {
		/**
		 * Instanciate components
		 */
		/**
		 * add a listener to this view
		 */
		addListener : function(list){
			this.listeners.push(list);
		},
		draw : function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="width: 450px;float: left;background: transparent; display: inline;"></div>');
			this.constListId = this.constListView.draw();
			this.orderByView.draw();
		},	
		fireSetTreepath: function(dataTreePath){
			var that = this;
			this.dataTreePath = dataTreePath;	
			this.fieldListView.setDataTreePath(this.dataTreePath);
			$.each(this.listeners, function(i){
				that.listeners[i].controlLoadFields(that.dataTreePath);
			});
		},
		fireOrderBy : function(nameattr){
			if( nameattr != 'OrderBy' ) {
				this.orderByView.setOrderBy(nameattr);
				var stmt = nameattr ;
				if( this.orderByView.isDesc()) stmt += " desc";
				if( this.queryView != null ) {				
					this.queryView.fireAddConstraint(this.formName, "orderby", [stmt]);
				} else {
					Out.info("No Query View OB:" + stmt);
				}
			} else {
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}
		},
		fireAttributeEvent : function(ahname){
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlAttributeEvent(ahname, that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		},
		fireClearAllConst : function() {
			this.constListView.fireClearAllConst();
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlClearAllConst();
			});		
		},
		fireGetNumberOfEditor : function() {
			var that = this;
			var retour = 0;
			$.each(this.listeners, function(i){
				retour =  that.listeners[i].controlGetNumberOfEditor();
			});		
			return retour;
		},
		printTypoMsg : function(fault, msg){
			this.constListView.printTypoMsg(fault, msg);
		},
		isTypoGreen : function(){
			return constListView.printTypoMsg(fault, msg);
		},
		updateQuery : function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "column", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		},
		runQuery : function() {	
			if( this.queryView != null ) {
				this.queryView.runQuery();
			} else {
				Out.info("No Query View");
			}
		}, 
		getAttributeHandlers: function(){
			var that = this;
			var retour = 0;
			$.each(this.listeners, function(i){
				retour =  that.listeners[i].controlGetAttributeHandlers();
			});		
			return retour;
		},
		fieldsStored: function(ahs){
			this.fieldListView.displayFields(ahs);
		}
};

/**
 * Subclass of ConstQEditor_mVc handling the edition of UCD bases constraints
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function UcdQEditor_mVc(params /*{parentDivId,formName,queryView, help}*/){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.fieldListView = new UcdFieldList_mVc(params.parentDivId
			, this.formName
			, { stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	          , orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	          }
	);
};

/**
 * Method overloading
 */
UcdQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="background: transparent; width:450px;float: left;"></div>');
			this.constListId = this.constListView.draw();
			if( this.help != undefined)
				$('#' + this.constContainerId).append('<div style="width: 100%;"><span class=spanhelp>' + this.help + '</span></div>');
		}
	},		
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "ucd", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});

/**
 * Subclass of UcdQEditor_mVc handling the edition of UCD bases constraints in matchPattern
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function UcdPatternEditor_mVc(params /*{parentDivId, formName, queryView, help}*/){
	UcdQEditor_mVc.call(this, params);
};
/**
 * Method overloading
 */
UcdPatternEditor_mVc.prototype = Object.create(UcdQEditor_mVc.prototype, {	
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "relation", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});

/**
 * Subclass of ConstQEditor_mVc handling the edition of position based constraints
 * @param parentDivId
 * @param formName
 * @param queryview
 * @returns {UcdQEditor_mVc}
 */
function PosQEditor_mVc(params /*{parentDivId, formName, queryView, frames, urls}*/){
	ConstQEditor_mVc.call(this, params);
	params.editor = this;
	this.fieldListView = new ConeSearch_mVc(params);
};
/**
 * Method overloading
 */
PosQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			var that = this;
			this.fieldListView.draw();
			this.parentDiv.append('<div  style="width: 25px; height: 80px;float: left;background: transparent; padding-left: 10px;padding-top: 30px; display: inline;"><input  title="Click to add a search cone" class="stackconstbutton" type="button"></div>');
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="float: left;background: transparent; display: inline;"></div>');
			this.constListId = this.constListView.draw();
			this.parentDiv.find("input.stackconstbutton").click(function() {that.fireAttributeEvent();});
		}
	},
	displayFields: { 
		value: function() {}
	},
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "position", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	fireAttributeEvent: { 
		value: function() {			
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlAttributeEvent(that.fieldListView.getSearchParameters(), that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
			this.fieldListView.resetPosition();
		}
	},
	fireAttributeAutoEvent: { 
		value: function() {			
			var that = this;
			/*
			 * More than One editor mean that he users have stacked multiple position constraints
			 */
			if( this.fireGetNumberOfEditor() <= 1 && this.fieldListView.hasSearchParameters() ) {
				this.fireClearAllConst();
				$.each(this.listeners, function(i){
					that.listeners[i].controlAttributeEvent(that.fieldListView.getSearchParameters(), that.constListId);
				});
				$("#" + this.constListId + " span.help").attr("style","display:none;");
			}
		}
	},
	firePoslistUpload: { 
		value: function(filename) {			
			var that = this;
			this.constListView.fireRemoveAllHandler();
			$.each(this.listeners, function(i){
				that.listeners[i].controlAttributeEvent({position: "poslist:" + filename, radius: 0, frame: 'ICRS'}, that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	}
});

/**
 * Subclass of CatalogueQEditor_mVc handling the edition of catalogue based constraints in matchPattern
 */
function CatalogueQEditor_mVc(params /*{parentDivId, formName, getMetaUrl, queryView, relationName, distanceQualifer, help}*/){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.fieldListView = new CatalogueList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	}
	);
};
/**
 * Method overloading
 */
CatalogueQEditor_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			var that = this;
			this.fieldListView.draw();
			this.parentDiv.append('<div id=' + this.constContainerId + ' style="width:450px;float: left;background: transparent; display: inline;"></div>');
			this.constListId = this.constListView.draw();
			$('#' + this.constContainerId).append('<div style="width: 100%;"><span class=spanhelp>' + this.help + '</span></div>');
			if( !this.fieldsLoaded ) {
				$.each(this.listeners, function(i){
					that.listeners[i].controlLoadFields();
					this.fieldsLoaded = true;
				});
			}
		}
	},		
	updateQuery : { 
		value:  function(consts) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "relation", consts);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	}
});


/**
 * Subclass of tapQEditor_mVc handling the edition of tap queries
 */
function tapColSelector_mVc(params){
	ConstQEditor_mVc.call(this, params);
	var that = this;
	this.help = params.help;
	this.constPosContainer   = params.parentDivId + "_constposcont";
	this.fieldListView = new TapColList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	         , orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	}	        
	, params.sessionID
	);
};
/**
 * Method overloading
 */
tapColSelector_mVc.prototype = Object.create(ConstQEditor_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div  class="editorrightpart">'
					//	+ '<div id=' + this.constPosContainer + '></div>'
					+ '<div id=' + this.constContainerId + ' style="position: absolute;top: 0px"></div>'
					+ '</div>');


			this.constListId = this.constListView.draw();
			this.orderByView.draw();
		}
	},
	// handler is called after the form is uptdated: query submission e.g.
	fireSetTreepath : { 
		value: function(treePath, handler){
			var that = this;
			if( this.dataTreePath != null ){
				this.fireClearAllConst();
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}

			this.dataTreePath = treePath;	
			// table name can include the schema
			//this.dataTreePath.table = this.dataTreePath.table.split('.').pop();
			this.queryView.fireSetTreePath(this.dataTreePath);
			this.queryView.fireAddConstraint(this.formName, "select", ["*"]);
			this.orderByView.fireClearAllConst();
			$.each(this.listeners, function(i){
				that.listeners[i].controlLoadFields(that.dataTreePath, handler);
			});
			this.fieldListView.setDataTreePath(this.dataTreePath);
		}
	},
	addTableOption: { 
		value: function(treePath) {
			this.fieldListView.addTableOption(treePath);
		}
	},
	fireAttributeEvent :  { 
		value: function(ahname){
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlAttributeHandlerEvent(that.fieldListView.getAttributeHandler(ahname), that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},		
	fireOrderBy :  { 
		value : function(nameattr){
			var tpn ="";
			if( nameattr != 'OrderBy' ) {
				tpn = this.fieldListView.dataTreePath.schema + "." + this.fieldListView.dataTreePath.table + "." + nameattr;
				var qtpn = tpn.quotedTableName();
				this.orderByView.setOrderBy(tpn);
				var stmt = nameattr ;
				if( this.orderByView.isDesc()) qtpn += " desc";
				if( this.queryView != null ) {				
					this.queryView.fireAddConstraint(this.formName, "orderby" , [qtpn]);
				} else {
					Out.info("No Query View OB:" + stmt);
				}
			} else {
				this.queryView.fireDelConstraint(this.formName, "orderby");
			}
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlOrderBy(tpn);
			});

		}
	},
	updateQuery  :  { 
		value:function(consts, joins) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "select", consts, joins);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	getCurrentTreePath: {
		value: function(){
			return this.fieldListView.dataTreePath;
		}
	}

});


/**
 * @param parentDivId
 * @param formName
 * @param queryview
 * @param getTableAttUrl
 * @param sesameUrl
 * @param upload: {url,  postHandler: called on success}
 * @param sessionID
 * @param help
 * @returns {tapQEditor_mVc}
 */
function tapQEditor_mVc(params /*parentDivId, formName, sesameUrl, upload { url, postHandler}, queryView, currentNode }*/){
	tapColSelector_mVc.call(this, params);
	var that = this;
	this.fieldListView = new TapFieldList_mVc(params.parentDivId
			, this.formName
			, {stackHandler: function(ahName){ that.fireAttributeEvent(ahName);}
	, orderByHandler: function(ahName){ that.fireOrderBy(ahName);}
	, raHandler: null
	, decHandler: null}	        
	, params.sessionID
	);
	this.posEditor = new TapSimplePos_mVc({editor: this
		, parentDivId: this.constPosContainer
		, formName: this.formName
		, frames: null
		, urls: {sesameURL: params.sesameUrl, uploadURL:params.upload.url}
	, postUploadHandler: params.upload.postHandler
	});
};
/**
 * Method overloading
 */
tapQEditor_mVc.prototype = Object.create(tapColSelector_mVc.prototype, {	
	draw : { 
		value: function() {
			this.fieldListView.draw();
			this.parentDiv.append('<div  class="editorrightpart">'
					+ '<div id=' + this.constPosContainer + '></div>'
					+ '<div id=' + this.constContainerId + ' style="position: absolute;top: 55px"></div>'
					+ '</div>');


//			$("#" +  this.constContainerId).append('<div class=constdiv><fieldset class="constraintlist">'
//			+ '<legend>Position</legend>'
//			+ '<span class=help>Click on a <input class="stackconstbutton" type="button"> button to append,<br>the constraint to the list</span>'
//			+ '</fieldset>'
//			+ '</div>');
			this.posEditor.draw();
			this.constListId = this.constListView.draw();
			this.orderByView.draw();
		}
	},
	displayFields: { 
		value: function(attributesHandlers){
			this.fieldListView.displayFields(attributesHandlers);
			this.fieldListView.lookForAlphaKeyword();
			this.fieldListView.lookForDeltaKeyword();
			return;
		}
	},
	fireAttributeEvent :  { 
		value: function(ahname){
			var that = this;
			$.each(this.listeners, function(i){
				that.listeners[i].controlAttributeHandlerEvent(that.fieldListView.getAttributeHandler(ahname), that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},		
	fireInputCoordEvent :  { 
		value : function(ra, dec, radius, frame){
			var that = this;
			var rakw = this.fieldListView.getRaKeyword();
			if( rakw.length == '' ) {
				Modalinfo.error("RA field not set");
				return;
			}
			var deckw = this.fieldListView.getDeltaKeyword();
			if( deckw.length == '' ) {
				Modalinfo.error("DEC field not set");
				return;
			}
			$.each(this.listeners, function(i){
				that.listeners[i].controlInputCoord(ra, dec, radius, frame
						, rakw, deckw, that.constListId);
			});
			$("#" + this.constListId + " span.help").attr("style","display:none;");
		}
	},
	updateQuery  :  { 
		value:function(consts, joins) {	
			if( this.queryView != null ) {
				this.queryView.fireAddConstraint(this.formName, "column", consts, joins);
			} else {
				Out.info("No Query View:" + JSON.stringify(consts));
			}
		}
	},
	isReadyToUpload :  { 
		value:function(consts, joins) {	
			if( this.fieldListView.getRaKeyword().length == '' || this.fieldListView.getDeltaKeyword() == '' ) {
				Modalinfo.error("Both RA and DEC fields must be set");
				return false;
			}
			return true;
		}
	},
	getUploadedFile: {
		value: function(){
			return this.posEditor.uploadedFile;
		}
	}
});

