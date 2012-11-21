

/*
 * Unit array used to setup UCD based queries
 */
var unitMap = new Array();
unitMap['Energy']    = ['erg', 'eV', 'keV', 'MeV', 'GeV', 'TeV', 'J', 'ryd'];
unitMap['Frequency'] = ['Hz', 'KHz', 'MHz', 'GHz', 'THz'];
unitMap['Time']      = ['y', 'd', 'h', 'mn', 'sec', 'msec', 'nsec'];
unitMap['Length']    = ['kpc', 'pc', 'AU', 'km', 'm', 'cm', 'mm', 'um', 'nm', 'Angstroem'];
unitMap['Velocity']  = ['m/s', 'km/s', 'km/h', 'mas/yr'];
unitMap['Angle']     = ['deg', 'arcmin', 'arcsec'];
unitMap['Flux']      = ['erg/s/cm2', 'Jy', 'mJy', 'count/s'];
unitMap['Power']     = ['erg/s', 'W'];

var units =  [
              {id: 'none', text: "none"}

              , {id: 'Power_erg/s', text: "erg/s"}
              , {id: 'Power_W', text: "W"}

              , {id: 'Flux_erg/s/cm2', text: "erg/s/cm2"}
              , {id: 'Flux_Jy', text: "Jy"}
              , {id: 'Flux_mJy', text: "mJy"}
              , {id: 'Flux_mJy', text: "mJy"}

              , {id: 'Angle_deg', text: "deg"}
              , {id: 'Angle_arcmin', text: "arcmin"}
              , {id: 'Angle_arcsec', text: "arcsec"}
              , {id: 'Angle_h:m:s', text: "h:m:s"}

              , {id: 'Velocity_m/s', text: "m/s"}
              , {id: 'Velocity_km/s', text: "km/s"}
              , {id: 'Velocity_km/h', text: "km/h"}
              , {id: 'Velocity_mas/yr', text: "mas/yr"}

              , {id: 'Length_kpc', text: "kpc"}
              , {id: 'Length_pc', text: "pc"}
              , {id: 'Length_AU', text: "AU"}
              , {id: 'Length_km', text: "km"}
              , {id: 'Length_m', text: "m"}
              , {id: 'Length_cm', text: "cm"}
              , {id: 'Length_mm', text: "mm"}
              , {id: 'Length_um', text: "um"}
              , {id: 'Length_nm', text: "nm"}
              , {id: 'Length_Angstroem', text: "Angstroem"}

              , {id: 'Energy_erg', text: "erg"}
              , {id: 'Energy_eV', text: "eV"}
              , {id: 'Energy_keV', text: "keV"}
              , {id: 'Energy_MeV', text: "MeV"}
              , {id: 'Energy_GeV', text: "GeV"}
              , {id: 'Energy_TeV', text: "TeV"}
              , {id: 'Energy_J', text: "J"}
              , {id: 'Energy_ryd', text: "ryd"}

              , {id: 'Frequency_Hz', text: "Hz"}
              , {id: 'Frequency_KHz', text: "KHz"}
              , {id: 'Frequency_MHz', text: "MHz"}
              , {id: 'Frequency_GHz', text: "GHz"}
              , {id: 'Frequency_THz', text: "THz"}

              , {id: 'Time_y', text: "y"}
              , {id: 'Time_d', text: "d"}
              , {id: 'Time_h', text: "h"}
              , {id: 'Time_mn', text: "mn"}
              , {id: 'Time_sec', text: "sec"}
              , {id: 'Time_msec', text: "msec"}
              , {id: 'Time_nsec', text: "nsec"}
              ];

var resultPaneView;
var saadaqlView ;
var sapView ;
var sampView ;
var tapView ;
var filterManagerView;
var cartView;

/*
 * To be set from a JSP 
 */
var base_url = '';
var booleansupported = false;

$().ready(function() {
	var resultPaneModel      = new $.ResultPaneModel();
	resultPaneView           = new $.ResultPaneView();
	new $.ResultPaneControler(resultPaneModel, resultPaneView);

	var patternModel      = new $.PatternModel();
	var patternView       = new $.PatternView();
	new $.PatternControler(patternModel, patternView);

	var saadaqlModel      = new $.SaadaQLModel(patternModel);
	saadaqlView           = new $.SaadaQLView();
	new $.SaadaQLControler(saadaqlModel, saadaqlView);

	var sapModel      = new $.SapModel();
	sapView           = new $.SapView();
	new $.SapControler(sapModel, sapView);

	var sampModel       = new $.SampModel();
	sampView            = new $.SampView();
	new $.SampControler(sampModel, sampView);

	var tapModel       = new $.TapModel();
	tapView            = new $.TapView();
	new $.TapControler(tapModel, tapView);

	var filterManagerModel       = new $.FilterManagerModel();
	filterManagerView            = new $.FilterManagerView();
	new $.FilterManagerControler(filterManagerModel, filterManagerView);

	var cartModel       = new $.CartModel();
	cartView            = new $.CartView();
	new $.CartControler(cartModel, cartView);

	/*
	 * Splitter functions of accesspane, the container of the db tree, 
	 * the data panel and the query form.
	 * see http://methvin.com/splitter
	 */
	$("div#accesspane").splitter({
		splitHorizontal: true,			
		outline: true,
		resizeToWidth: true,
		minTop: 100, 
		//sizeTop: ($(window).height() - 70 - 50), 
		sizeBottom: 250, 
		minBottom: 100,
		sizeTop: true,	
		accessKey: 'I'
	});
	$("div#datapane").splitter({
		splitVertical: true,
		sizeLeft: true,
		outline: true,
		resizeTo: window,
		minLeft: 100, sizeLeft: 150, minRight: 100,
		accessKey: 'I'
	});
	showProcessingDialog();
	$.getJSON("getmeta", {query: "datatree" }, function(data) {
		hideProcessingDialog();
		if( processJsonError(data, "Cannot make data tree") ) {
			return;
		}
		dataTree = $("div#treedisp").jstree({
			"json_data"   : data , 
			"plugins"     : [ "themes", "json_data", "dnd", "crrm", "ui"],
			"dnd"         : {"drop_target" : "#resultpane,#saadaqltab,#saptab,#taptab,#showquerymeta",

				"drop_finish" : function (data) {
					var parent = data.r;
					var treepath = data.o.attr("id").split('.');
					if( treepath.length < 2 ) {
						logged_alert("Query can only be applied on one data category or one data class");
					}
					else {
						while(parent.length != 0  ) {
							resultPaneView.fireSetTreePath(treepath);	
							if(parent.attr('id') == "resultpane" ) {
								setTitlePath(treepath);
								resultPaneView.fireTreeNodeEvent(treepath);	
								return;
							}
							else if(parent.attr('id') == "showquerymeta" ) {
								setTitlePath(treepath);
								resultPaneView.fireShowMetaNode(treepath);	
								return;
							}

//							else if(parent.attr('id') == "displayfilter" ) {
//							setTitlePath(treepath);
//							resultPaneView.fireTreeNodeEvent(treepath);	
//							filterManagerView.fireShowFilterManager(treepath);	
//							return;
//							}

							else if( parent.attr('id') == "saadaqltab" || parent.attr('id') == "saptab" || parent.attr('id') == "taptab") {
								saadaqlView.fireTreeNodeEvent(treepath);	
								sapView.fireTreeNodeEvent(treepath);	
								tapView.fireTreeNodeEvent(treepath);	
								return;
							}
							parent = parent.parent();
						}
					}
				}
			},
			// Node sorting by DnD blocked
			"crrm" : {"move" : {"check_move" : function (m) {return false; }}
			}
		}); // end of jstree
//		dataTree.bind("select_node.jstree", function (e, data) {
//		alert(data);
//		});
		dataTree.bind("dblclick.jstree", function (e, data) {
			var node = $(e.target).closest("li");
			var id = node[0].id; //id of the selected node					
			var treepath = id.split('.');
			if( treepath.length < 2 ) {
				alert("Query can only be applied on one data category or one data class");
			}
			else {
				showProcessingDialog();
				resultPaneView.fireSetTreePath(treepath);	
				setTitlePath(treepath);
				resultPaneView.fireTreeNodeEvent(treepath);	
			}
		});
	}); // end of ajax


	/*
	 * Activate submit buttons
	 */
	$('#submitdirect').click(function() {
		saadaqlView.fireInputCoordEvent();
		resultPaneView.fireSaadaQLQueryEvent($('#saadaqltext').val());
	});
	$('#submitquery').click(function() {
		resultPaneView.fireSubmitQueryEvent();
	});
	$('.sapcapability').click(function() {
		sapView.fireSubmitCapabilityEvent();
	});
	$('.sapregistry').click(function() {
		sapView.fireSubmitRegistryEvent();
	});
	$('.sapglu').click(function() {
		sapView.fireSubmitGluEvent();
	});
	$("#qlimit").keyup(function(event) {
		if( $("#qlimit").val() == '' || $("#qlimit").val().match(/^[0-9]+$/) ) {
			saadaqlView.fireUpdateQueryEvent();
			tapView.fireUpdateQueryEvent();			
		}
		else {
			alert('The result limit must be a positive integer value' );
			$("#qlimit").val(100);
			return false;
		}

	});

	/*********************************************************************************************
	 * Query form setup
	 * Create tabs to switch between SAADAQL and TAP forms
	 */
	$("#saadaqltab").tabs();
	$("#saadaqltab").tabs({
		selected: 4,
		disabled: [0,1,2,3]
	});

	$("#saptab").tabs();
	$("#saptab").tabs({
		unselect : true,
		selected: 0,
		disabled: [1,2]
	});

	$("#saptab").hide();

	$("#taptab").tabs();
	$("#taptab").tabs({
		unselect : true
	});
	$("#taptab").hide();
	/*
	 * Drop area for individual constraints on KWs
	 */
	$("#ConstraintsList").droppable({
		drop: function(event, ui){
			saadaqlView.fireAttributeEvent(ui.draggable);		
		}
	});
	$("#orderby").droppable({
		drop: function(event, ui){
			saadaqlView.fireOrderByEvent(ui.draggable);		
		}
	});
	$("#UCDConstraintsList").droppable({
		drop: function(event, ui){
			saadaqlView.fireUCDEvent(ui.draggable);		
		}
	});
	$("#patternconst").droppable({
		drop: function(event, ui){
			patternView.fireAttributeEvent(ui.draggable);		
		}
	});
	$("#patterncardqual" ).sortable({
		revert: "true"
	});
	$("#patternatt" ).sortable({
		revert: "true"
	});
	$("#tapconstraintlist").droppable({
		drop: function(event, ui){
			tapView.fireAttributeEvent(ui.draggable);		
		}
	});
	$("#tapselectlist").droppable({
		drop: function(event, ui){
			tapView.fireSelectEvent(ui.draggable);		
		}
	});
	$("#tapalpha").droppable({
		drop: function(event, ui){
			tapView.fireAlphaEvent(ui.draggable);		
		}
	});
	$("#tapdelta").droppable({
		drop: function(event, ui){
			tapView.fireDeltaEvent(ui.draggable);		
		}
	});
	$("#fspefieldsdrop").droppable({
		drop: function(event, ui){
			filterManagerView.fireSpeFieldEvent(ui.draggable);		
		}
	});
	$("#fnativedrop").droppable({
		drop: function(event, ui){
			filterManagerView.fireNativeEvent(ui.draggable);		
		}
	});
	$("#frelationsdrop").droppable({
		drop: function(event, ui){
			filterManagerView.fireRelationsEvent(ui.draggable);		
		}
	});

	/*
	 * Coordinates input
	 */
	$("#coordform input").keypress(function(event) {
		if (event.which == '13') {
			saadaqlView.fireInputCoordEvent();
		}
	});
	$("#tapwhere input").keypress(function(event) {
		if (event.which == '13') {
			tapView.fireInputCoordEvent();
		}
	});
	/*
	 * pattern selectors
	 */
	$("#relationselect").change(function() {
		$("#relationselect option:selected").each(function () {
			var text = $(this).text();
			if( !text.startsWith("--") ) {
				saadaqlView.fireSelectRelationEvent(text);
			}
			return;
		});
	});
	$("#cpclassselect").change(function() {
		var retour = new Array();
		$("#cpclassselect option:selected").each(function () {
			var text = $(this).text();
			if( !text.startsWith("--") ) {
				patternView.fireSelectClassEvent(text);
				return;
			}
		});
	});


	$("#acceptpattern").click(function() {
		patternView.fireAcceptPattern();
	});
	/*
	 * Order by
	 */
	$("input[name=sens]").click(function(){
		saadaqlView.fireUpdateQueryEvent();		
	});
	/*
	 * Query language selector
	 */
	$(".langswitch").click(function() {
		var mode = $(this).val();
		if( mode == 'saadaql') {
			$('#taptab').hide();
			$('#saptab').hide();
			$('#saadaqltab').show('slow');
			$("#qhistocount").css("visibility", "visible");
			saadaqlView.fireDisplayHisto();

		}
		else if( mode == 'sap') {
			$('#taptab').hide();
			$('#saadaqltab').hide();
			$('#saptab').show('slow');
			$("#qhistocount").css("visibility", "hidden");
			sapView.fireDisplayHisto();
		}
		else {
			$('#saadaqltab').hide();
			$('#saptab').hide();
			$('#taptab').show('slow');
			$("#qhistocount").css("visibility", "hidden");
			tapView.fireDisplayHisto();							

		}
	});   
	/*
	 * Name resolver buton activation
	 */
	$(".sesame").click(function() {
		var inputfield = $(this).parents('div').find(".coordinputvalue");
		showProcessingDialog();
		$.getJSON("sesame", {object: inputfield.val() }, function(data) {
			hideProcessingDialog();
			if( processJsonError(data, "Sesame failure") ) {
				return;
			}
			else {
				inputfield.val(data.alpha + ' ' + data.delta);
			}
		});
	});



	/*
	 * Get the base URL of the site. 
	 * Importatnt to avoid cross access issues
	 */
	showProcessingDialog();
	$.getJSON("sitedesc", function(data) {
		hideProcessingDialog();
		base_url = data.rooturl;
		if( !base_url.match("/$") ) {
			base_url = base_url +"/";
		}
		$('#pagetitle').html("<span class=pagetitle>" + data.dbname + "</span>&nbsp<span class=pagetitlepath id=titlepath></span>");
		$('title').html(data.dbname);
		if( data.booleansupported == "true") {
			booleansupported = true;
		} 
	});
	/*
	 * This callback can be changed changed at everytime: do not use the "onclick" HTML  
	 * attribute which is not overriden by JQuery "click" callback
	 */
	$('#showquerymeta').click(function(){logged_alert("No meta data available yet");});

	sampView.fireSampInit();
	//tapView.fireRefreshJobList();
	$("[name=qlang]").filter("[value=\"saadaql\"]").attr("checked","checked");
});
