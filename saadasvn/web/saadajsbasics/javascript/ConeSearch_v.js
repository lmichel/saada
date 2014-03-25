/**
 * COne Search form View
 * 
 * @param params: JS object with the following fields
 * 	parentDivId: ID of the parent div
 *  formName   : Name of the current form
 *  frames     : Arrays of the supported frames
 *  urls   : JS object containing handlers processing events:
 *        sesameURL : name resolver
 *        uploadURL : Handle the upload of position lists
 */
function ConeSearch_mVc(params){
	this.editor = params.editor;
	this.parentDivId = params.parentDivId;
	this.formName = params.formName;
	this.frames = params.frames,
	this.sesameURL = params.urls.sesameURL;
	this.uploadURL = params.urls.uploadURL;
	this.cooFieldId = this.formName + "_CScoofield";
	this.radiusFieldId = this.formName + "_CSradiusfield";
	this.frameSelectId = this.formName + "_CSframeselect";
	this.uploadId = this.formName + "_CSupload";
	this.sesameId = this.formName + "_CSsesame";
	this.stackId = this.formName + "_CSstack";
};
/**
 * Methods prototypes
 */
ConeSearch_mVc.prototype = {
		/**
		 * Draw the field container
		 */
		draw : function() {
			if( this.frames == null || this.frames .length == 0 ) {
				this.frames = ['ICRS'];
			}
			var html = '<fieldset class=fieldiv style="display: inline; float: left;height: 130px;">'
				+ '  <legend> Cone Search Setup </legend>'
				+ '     <div style="background: transparent;">'
				+ '       <span style="text-align: right; display: inline-block; width: 7em;">Coord/Name</span>'
				+ '       <input type=text id="' + this.cooFieldId + '" class=inputvalue  size=18 />'
				+ '       <a href="javascript:void(0);" id="' + this.sesameId + '" title="Invoke the CDS name resolver" class=sesame></a>'
				+ '       <br>'
				+ '       <span style="text-align: right; display: inline-block; width: 7em;">Radius(arcmin)</span>'
				+ '       <input type=text id="' + this.radiusFieldId + '" class=inputvalue style="width: 135px;" value="1" />'
				+ '      <br>'
				+ '      <span style="text-align: right; display: inline-block; width: 7em;">System</span>'
				+ '      <select id="' + this.frameSelectId + '" >'
				+ '      </select>'
				+ '      <br> <br>'
				+ '      <input type=button id="' + this.uploadId + '" value="Upload Position List"/>'
				+ '      <div style="display: inline; float: right;"><span class=help id="uploadresult"></span></div>'
				+ '    </div>'
				+ '</fieldset>';
			$('#' + this.parentDivId).append(html);
			var s = $('#' + this.frameSelectId);
			for( var i=0 ; i<this.frames.length ; i++  ) {
				s.append('<option value=' + this.frames[i] + '>' +this.frames[i] + '</option>');
			}
			this.setUploadForm();
			this.setSesameForm();
		},
		setUploadForm: function() {
			var that = this;
			var handler = ( this.uploadURL != null )
			? function() {Modalinfo.uploadForm("Upload a list of position"
					, that.uploadURL
					, "Upload a CSV position list<br>Error are in arcmin"
					, function(returnedPath){				
						var msg;
						if( returnedPath.retour.name != undefined && returnedPath.retour.size != undefined ) {
							msg = " File " + returnedPath.retour.name  + ' uploaded<br>' + returnedPath.retour.positions + ' positions';
						} else {
							msg = JSON.stringify(returnedPath.retour);
						}
						$('span#uploadresult').html(msg);
						that.editor.firePoslistUpload(returnedPath.path.filename);
						Modalinfo.close();
					}
					, function(){$('span#uploadresult').text('');});
			} 
			: function(){Modalinfo.info("Upload not implemented yet");};
			$('#' + this.uploadId).click(handler) ;

		},
		setSesameForm: function() {
			var inputfield = $('#' + this.cooFieldId);
			var handler = ( this.sesameURL != null )
			? function() {			
				Processing.show("Waiting on SESAME response");
				$.getJSON("sesame", {object: inputfield.val() }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "Sesame failure") ) {
						return;
					} else {
						inputfield.val(data.alpha + ' ' + data.delta);
					}
				});}
			:  function(){Modalinfo.info("name resolver not implemented yet");};

			$('#' + this.sesameId).click(handler) ;
		},
		hasSearchParameters: function() {
			var coo = $('#' + this.cooFieldId).val();
			var radius= $('#' + this.radiusFieldId).val();
			if( coo.trim() == "" || isNaN(radius) ) {
				return false;				
			} else {
				return true;
			}
		},
		getSearchParameters: function() {
			var coo = $('#' + this.cooFieldId).val();
			var radius= $('#' + this.radiusFieldId).val();
			var frame = $('#' + this.frameSelectId + '  option:selected').text();
			if( coo.trim() == "" ) {
				Modalinfo.error("No coordinates given");
				return null;
			}else if( isNaN(radius) ) {
				Modalinfo.error("Radius filed requires a numerical value");
				return null;				
			} else {
				return {position: coo, radius: radius, frame: frame};
			}
		},
		resetPosition: function(){
			$('#' + this.cooFieldId).val("");
		}
};


/**
 * Used for merged catalogues 3XMM
 * @param params
 * @returns {SimplePos_mVc}
 */
function SimplePos_mVc(params){
	ConeSearch_mVc.call(this, params);
	this.queryView = params.queryView;
};

/**
 * Method overloading
 */
SimplePos_mVc.prototype = Object.create(ConeSearch_mVc.prototype, {
	/**
	 * Draw the field container
	 */
	draw : {
		value: function() {
			var that = this;
			if( this.frames == null || this.frames .length == 0 ) {
				this.frames = ['ICRS'];
			}
			var html = '<div style="background: transparent;  float: left;">'
				+ '       <span style="text-align: right; display: inline-block; width: 7em;">Coord/Name</span>'
				+ '       <input type=text id="' + this.cooFieldId + '" class=inputvalue  size=18 />'
				+ '       <a href="javascript:void(0);" id="' + this.sesameId + '" title="Invoke the CDS name resolver" class=sesame></a>'
				+ '       <span>Radius(arcmin)</span>'
				+ '       <input type=text id="' + this.radiusFieldId + '" class=inputvalue style="width: 40px;" value="1" />'
				+ '      <span>System</span>'
				+ ((this.frames != null)? '      <select id="' + this.frameSelectId + '" >':'')
				+ '      </select>'
				+ '      <input type=button id="' + this.uploadId + '" value="Upload Position List"   />'
				+ '      <div style="width: 110px; height: 30px; float: right; display: inline; overflow: hidden;">'
				+ '         <span class=help id="uploadresult"></span>'
				+ '      </div>'
				+ '    </div>';
			$('#' + this.parentDivId).append(html);
			var s = $('#' + this.frameSelectId);
			for( var i=0 ; i<this.frames.length ; i++  ) {
				s.append('<option value=' + this.frames[i] + '>' +this.frames[i] + '</option>');
			}
			this.setUploadForm();
			this.setSesameForm();

			$('#' + this.cooFieldId ).keyup(function(event) {
				if ( event.which == 13 ) {
					event.preventDefault();
				} else{
					that.readAndUpdate();
				}
			});
			$('#' + this.cooFieldId ).click(function(event) {
				that.readAndUpdate();
			});
			$('#' + this.radiusFieldId ).keyup(function(event) {
				if ( event.which == 13 ) {
					event.preventDefault();
				} else{
					that.readAndUpdate();
				}
			});
			$('#' + this.radiusFieldId ).click(function(event) {
				that.readAndUpdate();
			});
			$('#' + this.frameSelectId ).change(function(event) {
				that.readAndUpdate();
			});
		}
	},	
	readAndUpdate:{
		value: function() {
			var coo = $('#' + this.cooFieldId).val();
			var radius= $('#' + this.radiusFieldId).val();
			var frame = $('#' + this.frameSelectId + '  option:selected').text();
			this.updateQuery(coo, radius, frame);
		}
	},
	setUploadForm : {
		value : function() {
			var that = this;
			var handler = ( this.uploadURL != null )
			? function() {Modalinfo.uploadForm("Upload a list of position"
					, that.uploadURL
					, "Upload a CSV position list<br>Error are in arcmin"
					, function(returnedPath){				
						var msg;
						if( returnedPath.retour.name != undefined && returnedPath.retour.size != undefined ) {
							msg = returnedPath.retour.name  + ' uploaded, ' + returnedPath.retour.positions + ' positions';
						} else {
							msg = JSON.stringify(returnedPath.retour);
						}
						$('#' + that.parentDivId + ' span#uploadresult').html(msg);
						$('#' + that.cooFieldId).val("poslist:" + returnedPath.retour.name);
						$('#' + that.radiusFieldId).val("0");
						that.readAndUpdate();
					}
					, function(){$('span#uploadresult').text('');});
			} 
			: function(){Modalinfo.info("Upload not implemented yet");};
			$('#' + this.uploadId).click(handler) ;

		}
	},
	setSesameForm : {
		value : function() {
			var that = this;
			var inputfield = $('#' + this.cooFieldId);
			var handler = ( this.sesameURL != null )
			? function() {			
				Processing.show("Waiting on SESAME response");
				$.getJSON("sesame", {object: inputfield.val() }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "Sesame failure") ) {
						that.updateQuery('', '', null);
						return;
					} else {
						inputfield.val(data.alpha + ' ' + data.delta);
						that.readAndUpdate();
					}
				});}
			:  function(){Modalinfo.info("name resolver not implemented yet");};

			$('#' + this.sesameId).click(handler) ;
		}
	},
	updateQuery : {
		value: function(coord, radius, frame) {
			if( this.queryView != null ) {
				this.queryView.fireDelConstraint(this.formName, "position");
				if( coord != '' && radius != '' ){
					this.queryView.fireAddConstraint(this.formName
							, "position"
							, '    isInCircle("' + coord + '", ' + radius + ', ' 
							+ ((frame == "FK5")? "J2000":
								(frame == "FK4")? "J1950": '-')
								+ ', ' + frame + ')');
				}
			} else {
				Out.info("No query view");
			}
		}
	},
	fireClearAllConst: {
		value: function() {
			var that = this;
			$('#' + this.cooFieldId).val('');
			that.readAndUpdate();
		}
	}
});


/**
 * Used for Taphandle
 * params.postUploadHandler is invoked after the upload succeed
 * It recieved the an object as parameter {name: filename, size: filesize, positions: num of valid positions}
 * @param params
 * @returns {SimplePos_mVc}
 */
function TapSimplePos_mVc(params){
	SimplePos_mVc.call(this, params);
	this.handler = this.editor.fireInputCoordEvent;		
	this.postUploadHandler= params.postUploadHandler;
	this.uploadedFile = ""; 
};

/**
 * Method overloading
 */
TapSimplePos_mVc.prototype = Object.create(SimplePos_mVc.prototype, {
	/**
	 * Draw the field container
	 */
	draw : {
		value: function() {
			var that = this;
			if( this.frames == null || this.frames .length == 0 ) {
				this.frames = ['ICRS'];
			}
			var html = '<div style="background: transparent; position: absolute; top: 0px; left: 0px; width: 460px; height: 55px">'
				+ '       <span class=help style="text-align: right; display: inline-block; width: 7em;">Coord/Name</span>'
				+ '       <input type=text id="' + this.cooFieldId + '" class=inputvalue  size=18 />'
				+ '       <a href="javascript:void(0);" id="' + this.sesameId + '" title="Invoke the CDS name resolver" class=sesame></a>'
				+ '       <span class=help >System</span>'
				+ ((this.frames != null)? '      <select id="' + this.frameSelectId + '" >':'')
				+ '      </select>'
				+ '       <br><span class=help >Radius(arcmin)</span>'
				+ '       <input type=text id="' + this.radiusFieldId + '" class=inputvalue style="width: 40px;" value="1" />'
				+ '       <input class=stackconstbutton id="' + this.stackId + '" type="button"/>'
				+ '       <input type=button id="' + this.uploadId + '" value="Upload Position List" disabled title="Not implemented yet (wait for next release)"/>'
				+ '       <div style="width: 150px; height: 30px; position: absolute; top: 25px; left: 295px; overflow: hidden; background-color: whitesmoke;">'
				+ '            <span class=help id="uploadresult"></span>'
				+ '       </div>'
				+ ' </div>';
			$('#' + this.parentDivId).append(html);
			var s = $('#' + this.frameSelectId);
			for( var i=0 ; i<this.frames.length ; i++  ) {
				s.append('<option value=' + this.frames[i] + '>' +this.frames[i] + '</option>');
			}
			this.setUploadForm();
			this.setSesameForm();
			$('#' + this.stackId ).click(function() {
				that.readAndUpdate();				
			});
			$('#' + this.cooFieldId ).keyup(function(event) {
				if ( event.which == 13 ) {
					that.readAndUpdate();
				}
			});
//			$('#' + this.cooFieldId ).click(function(eventObject) {
//				that.readAndUpdate();
//			});
			$('#' + this.radiusFieldId ).keyup(function(event) {
				if ( event.which == 13 ) {
					that.readAndUpdate();
				}
			});
//			$('#' + this.radiusFieldId ).click(function(eventObject) {
//				that.readAndUpdate();
//			});
			$('#' + this.frameSelectId ).change(function(event) {
				that.readAndUpdate();
			});
		}
	},	
	readAndUpdate:{
		value: function() {
			this.uploadedFile = "";
			var coo = $('#' + this.cooFieldId).val();
			var radius= $('#' + this.radiusFieldId).val();
			var frame = $('#' + this.frameSelectId + '  option:selected').text();
			if( coo.length == 0 ) {
				Modalinfo.error("No coordinates given", "input error");
			} else if( radius.length == 0 ) {
				Modalinfo.error("No radius given", "input error");
			} else if( !$.isNumeric(radius)  ) {
				Modalinfo.error("Radius must be numeric", "input error");
			} else {
				var rd = coo.split(/\s+/);
				if( coo.startsWith('poslist:')  ) {
					this.uploadedFile = coo.replace('poslist:','');
						this.editor.fireInputCoordEvent(coo, null, radius, frame);				
				} else if( rd.length != 2 ) {
					Modalinfo.error("Coordinates must be separataed with a blank", "input error");					
				} else if( !$.isNumeric(rd[0]) ||!$.isNumeric(rd[1])  ) {
					Modalinfo.error("Radius must be numeric", "input error");
				} else {					
					this.editor.fireInputCoordEvent(rd[0], rd[1], radius, frame);
				}
			}
		}
	},
	setUploadForm : {
		value : function() {
			var that = this;
			var handler = ( this.uploadURL != null )
			? function() {
				var radius= $('#' + that.radiusFieldId).val();
				if( radius == "" ){
					Modalinfo.error("Radius must be set");
				} else	if( that.editor.isReadyToUpload() ) {
					Modalinfo.uploadForm("Upload a list of position"
					, that.uploadURL
					, "Upload a CSV position list (ra dec or object name)"
					, function(returnedPath){				
						var msg;
						if( returnedPath.retour.name != undefined && returnedPath.retour.positions != undefined ) {
							msg = returnedPath.retour.name  + ' uploaded, ' + returnedPath.retour.positions + ' positions';
							$('#' + that.cooFieldId).val("poslist:" + returnedPath.retour.name);
							$('#' + that.parentDivId + ' span#uploadresult').html(msg);
							that.readAndUpdate();
							if( that.postUploadHandler !=null ) {
								that.postUploadHandler( returnedPath.retour);
							}
							Modalinfo.close();
						} else {
							Modalinfo.error(JSON.stringify(returnedPath), "Upload Failure");
						}
					}
					, function(){$('span#uploadresult').text('');}
					, [{name: 'radius', value: radius}]);
				}
			} 
			: function(){Modalinfo.info("Upload not implemented yet");};
			$('#' + this.uploadId).click(handler) ;

		}
	},
	setSesameForm : {
		value : function() {
			var that = this;
			var inputfield = $('#' + this.cooFieldId);
			var handler = ( this.sesameURL != null )
			? function() {			
				Processing.show("Waiting on SESAME response");
				$.getJSON("sesame", {object: inputfield.val() }, function(data) {
					Processing.hide();
					if( Processing.jsonError(data, "Sesame failure") ) {
						that.updateQuery('', '', null);
						return;
					} else {
						inputfield.val(data.alpha + ' ' + data.delta);
						that.readAndUpdate();
					}
				});}
			:  function(){Modalinfo.info("name resolver not implemented yet");};

			$('#' + this.sesameId).click(handler) ;
		}
	},
	updateQuery : {
		value: function(coord, radius, frame) {
			if( this.queryView != null ) {
				this.queryView.fireDelConstraint(this.formName, "position");
				if( coord != '' && radius != '' ){
					this.queryView.fireAddConstraint(this.formName
							, "position"
							, '    isInCircle("' + coord + '", ' + radius + ', ' 
							+ ((frame == "FK5")? "J2000":
								(frame == "FK4")? "J1950": '-')
								+ ', ' + frame + ')');
				}
			} else {
				Out.info("No query view");
			}
		}
	},
	fireClearAllConst: {
		value: function() {
			$('#' + this.cooFieldId).val('');
			that.readAndUpdate();
		}
	}
});
