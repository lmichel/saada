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

/*
 * instance of DataTreePath: {nodekey:table, schema: collection, table: table, tableorg: table, category}
 */
var globalTreePath ;

var queryView;
var nativeConstraintEditor;
//var patternConstraintEditor;
var posConstraintEditor;

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

	var saadaqlModel      = new $.SaadaQLModel(null);
	saadaqlView           = new $.SaadaQLView();
	new $.SaadaQLControler(saadaqlModel, saadaqlView);

	WebSamp_mVc.init("Saada"
			, (window.location.href + "/images/saadatransp-text-small.gif").replace(/\/#/g, '')
			, "Saada Database");


	var sapModel       = new $.SapModel();
	sapView            = new $.SapView();
	new $.SapControler(sapModel, sapView);

	var cartModel       = new $.CartModel();
	cartView            = new $.CartView();
	new $.CartControler(cartModel, cartView);

	/*********************************************************************************************
	 * Query form setup
	 * Create tabs to switch between SAADAQL and TAP forms
	 */
	//$("#saadaqltab").tabs();
	$("#saadaqltab").tabs({
		selected: 4,
		disabled: [0,1,2,3]
	});
	//$("#saptab").tabs();
	$("#saptab").tabs({
		unselect : true,
		//selected: 0,
		disabled: [0, 1,2]
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
	
	MetadataSource.init({getMetaTable: "getmeta"})
	queryView = QueryConstraintEditor.queryTextEditor({ parentDivId: 'texttab', defaultQuery: ''});	   		    
	posConstraintEditor = QueryConstraintEditor.posConstraintEditor({ parentDivId: 'postab', formName:'simpleposcolumns',  queryView: queryView
			, frames: ['ICRS', 'GALACTIC', 'FK5'], urls: {sesameURL: "sesame", uploadURL: "uploadposlist"}} );
	
	nativeConstraintEditor = QueryConstraintEditor.nativeConstraintEditor({parentDivId: 'kwtab', getMetaUrl: "getmeta", queryView: queryView});
	
	patternConstraintEditor = QueryConstraintEditor.matchPatternEditor({parentDivId: 'patterntab',formName: 'matchPattern',queryView: queryView});
	
		//qce.fireSetTreepath(new DataTreePath({nodekey:'node', schema: 'schema', table: 'table', tableorg: 'schema.table'}));
	$("#qlimit").keyup(function(event) {
		var v =  $("#qlimit").val();
		if( v == '' || v.match(/^[0-9]+$/) ) {
			queryView.fireAddConstraint("Merged", "limit", [v]);
		}
		else {
			Modalinfo.info('The result limit must be a positive integer value' );
			$("#qlimit").val(100);
			return false;
		}

	});
	
	DataTree.init();

	//PageLocation.confirmBeforeUnlaod();		
	Out.setdebugModeFromUrl();
});
