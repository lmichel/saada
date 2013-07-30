/**
 * This file function is invoked atfer the DOM tree is built.
 * It creates all MVC and bind some DOM node with relevant event handlers
 * 
 * @author michel
 * @version $Id$
 */
var resultPaneView;
var saadaqlView ;
var sapView ;
var webSampView ;
var filterManagerView;
var cartView;

var base_url = '';
var booleansupported = false;
/*
 * JQuery object managing splitter panels
 */
var layoutPane;

$().ready(function() {
	Out.setdebugModeFromUrl();	

	base_url = "http://" + window.location.hostname +  (location.port?":"+location.port:"") + window.location.pathname;
	/*
	 * layout plugin, requires JQuery 1.7 or higher
	 * Split the bottom div in 3 splitters divs.
	 */		
	layoutPane = $('#accesspane').layout();
	/*
	 * Connect the URL passed as parameter
	 */
	var defaultUrl  =  (RegExp('url=' + '(.+?)(&|$)').exec(location.search)||[,null])[1];
	if( defaultUrl != null ) {
		resultPaneView.fireNewNodeEvent(unescape(defaultUrl));
	}

	var resultPaneModel      = new $.ResultPaneModel();
	resultPaneView           = new $.ResultPaneView();
	new $.ResultPaneControler(resultPaneModel, resultPaneView);

	var patternModel      = new $.PatternModel();
	var patternView       = new $.PatternView();
	new $.PatternControler(patternModel, patternView);

	var saadaqlModel      = new $.SaadaQLModel(patternModel);
	saadaqlView           = new $.SaadaQLView();
	new $.SaadaQLControler(saadaqlModel, saadaqlView);

	WebSamp_mVc.init("Saada"
			, (window.location.href + "/images/saadatransp-text-small.gif").replace(/\/#/g, '')
			, "Saada Database");


	var sapModel       = new $.SapModel();
	sapView            = new $.SapView();
	new $.SapControler(sapModel, sapView);

	var filterManagerModel       = new $.FilterManagerModel();
	filterManagerView            = new $.FilterManagerView();
	new $.FilterManagerControler(filterManagerModel, filterManagerView);

	var cartModel       = new $.CartModel();
	cartView            = new $.CartView();
	new $.CartControler(cartModel, cartView);

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
		}
		else {
			Modalinfo.info('The result limit must be a positive integer value' );
			$("#qlimit").val(100);
			return false;
		}

	});
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
	/*
	 * pattern selectors
	 */
	$("#relationselect").change(function() {
		$("#relationselect option:selected").each(function () {
			console.log($(this).text());
			var text = $(this).text();
			if( !text.startsWith("--") ) {
				saadaqlView.fireSelectRelationEvent(text);
			}
			return;
		});
	});
	$("#cpclassselect").change(function() {
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
			$('#saptab').hide();
			$('#saadaqltab').show('slow');
			$("#qhistocount").css("visibility", "visible");
			saadaqlView.fireDisplayHisto();

		}
		else if( mode == 'sap') {
			$('#saadaqltab').hide();
			$('#saptab').show('slow');
			$("#qhistocount").css("visibility", "hidden");
			sapView.fireDisplayHisto();
		}
		else {
			$('#saadaqltab').hide();
			$('#saptab').hide();
			$("#qhistocount").css("visibility", "hidden");

		}
	});   
	/*
	 * Name resolver buton activation
	 */
	$(".sesame").click(function() {
		var inputfield = $(this).parents('div').find(".coordinputvalue");
		Processing.show("Asking Sesame name resolver");
		$.getJSON("sesame", {object: inputfield.val() }, function(data) {
			Processing.hide();
			if( Processing.jsonError(data, "Sesame failure") ) {
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
	Processing.show("Get site description");
	$.getJSON("sitedesc", function(data) {
		Processing.hide();
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
	
	$.getJSON("getversion", function(data) {
		$('#saadaLogo').attr("title", data.dbname + " database build with Saada " + data.version + " on " + data.dbms);
	});
	
	/*
	 * This callback can be changed changed at everytime: do not use the "onclick" HTML  
	 * attribute which is not overriden by JQuery "click" callback
	 */
	$('#showquerymeta').click(function(){Modalinfo.info("No meta data available yet");});

	$("[name=qlang]").filter("[value=\"saadaql\"]").attr("checked","checked");
	
	Processing.show("Loading Data Tree");
	$.getJSON("getmeta", {query: "datatree" }, function(data) {
		Processing.hide();
		if( Processing.jsonError(data, "Cannot make data tree") ) {
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
						Modalinfo.info("Query can only be applied on one data category or one data class");
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
//		Modalinfo.info(data);
//		});
		dataTree.bind("dblclick.jstree", function (e, data) {
			var node = $(e.target).closest("li");
			var id = node[0].id; //id of the selected node					
			var treepath = id.split('.');
			if( treepath.length < 2 ) {
				Modalinfo.info("Query can only be applied on one data category or one data class");
			}
			else {
				Processing.show("Open node " + getTreePathAsKey());
				resultPaneView.fireSetTreePath(treepath);	
				setTitlePath(treepath);
				resultPaneView.fireTreeNodeEvent(treepath);	
				Processing.hide();
			}
		});
	}); // end of ajax

	//Location.confirmBeforeUnlaod();
	//Out.setdebugModeFromUrl();
});
