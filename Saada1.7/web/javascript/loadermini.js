/**
 * Loads automatically all css scripts used by saadahsbasics
 * Files are loaded one by one keeping that wy the initial order.
 * That make sure not to  break dependencies or style rules overriding
 * 
 * This class is a JS singleton.
 */
resourceMiniLoader = function() {
	
	if( !productionMode )
		return;
	/*
	 * JS directories and files
	 */
	var baseScriptDir = "";
	var jsimportsDir  = baseScriptDir + "/jsimports/";
	var javascriptDir = baseScriptDir + "/javascript/";
	var local_js = ["packed.js"	  
	                ];
	var local_min_js = ["packed.js"];
	var imp_js = [   "ui/minified/jquery-ui.min.js",
	                 "packed.js",
	                 ];
	var js = new Array();  // global list of JS to load

	/*
	 * CSS directories and files
	 */
	var baseCssDir      = "";
	var styleDir        = baseCssDir + "styles/";
	var styleimportsDir = baseCssDir + "styleimports/";
	var baseUrl = "./min";	
	var local_css  = ["basics.css"
	                  , "domain.css"
	                  , "global.css"
	                  , "form.css"
	                  ];
	var import_css = ["themes/base/minified/jquery-ui.min.css"
	                  , "layout-default-latest.css"
	                  , "jquery.dataTables.css"
	                  , "simplemodal.css"
	                  , "aladin.min.css"
	                  , "bootstrap/bootstrap.css"
	                  , "bootstrap/bootstrap.css.map"
	                  , "foundationicon/foundation-icons.css"

	                  ];

	var css = new Array();// global list of CSS to load
	var CssOver = false; // true when all CSS are loaded (can start JS loading)
	
	/*
	 * Check if saadajsbasics resources are installed locally
	 */
	baseUrl = "./min/";

	console.log("jsresources will be taken from " + baseUrl);

	/**
	 * Recursive function loading the first script of the list
	 */
	var loadNextScript = function() {
		var script = document.createElement("script");
		var head = document.getElementsByTagName('HEAD').item(0);
		script.onload = script.onreadystatechange = function() {
			console.log(js[0] + " script loaded " + CssOver);
			js.shift();
			if( js.length > 0 ) loadNextScript();
		};
		script.src = js[0];
		script.type = "text/javascript";
		head.appendChild( script);
	};
	/**
	 * Recursive function loading the first CSS of the list
	 */
	loadNextCss = function() {
		var  href = css[0];
		$.ajax({
			url: href,
			dataType: 'text',
			success: function(){        
				$('<link rel="stylesheet" type="text/css" href="'+href+'" />').appendTo("head");
				console.log(href + " CSS loaded " + CssOver);
				css.shift();
				if( css.length > 0 ) loadNextCss();
				else {
					CssOver = true;
				}
			},
			error : function(jqXHR, textStatus,errorThrown) {
				console.log("Error loading " +  href + " "  + textStatus);

			}
		});

	};
	/**
	 * Start to load JS scripts after the CSS loading is completed
	 */
	var loadScripts = function() {
		if( !CssOver) {
			setTimeout(function() {loadScripts();}, 100);
			return;
		}	else {	
			loadNextScript();
		}
	};

	/***************
	 * externalscripts: array of scripts to be loaded after jsresources 
	 */
	/**
	 * Stores the list of user JS scripts to load
	 * and build the global list of resource to load
	 */
	var setScripts = function(externalscripts) {
		//	console.log("----------- " + that.baseUrl + " " + baseScriptDir);

		for( var i=0 ; i<imp_js.length ; i++ ) {
			js.push(baseUrl + jsimportsDir + imp_js[i]);
		}

		for( var i=0 ; i<local_js.length ; i++ ) {
			var jsf =  baseUrl + javascriptDir + local_js[i];
			if( ! jsf.match(/.*\.js/)  ){
				js.push(jsf + "_m.js");
				js.push(jsf + "_v.js");
				js.push(jsf + "_c.js");
			} else {
				js.push(jsf);

			}
		}
	//	js.push.apply(js, externalscripts);
	};
	/**
	 * Stores the list of user JS scripts to load
	 * and build the global list (with the local short list) of resource to load
	 */
	var setMinScripts = function(externalscripts) {
		for( var i=0 ; i<local_min_js.length ; i++ ) {
			var jsf =  baseUrl + baseScriptDir + local_min_js[i];
			if( ! jsf.match(/.*\.js/)  ){
				js.push(jsf + "_m.js");
				js.push(jsf + "_v.js");
				js.push(jsf + "_c.js");
			} else {
				console.log(jsf);
				js.push(jsf);

			}
		}
		for( var i=0 ; i<imp_js.length ; i++ ) {
			js.push(baseUrl + imp_js[i]);
		}
		js.push.apply(js, externalscripts);
		loadNextScript();
	};
	/**
	 * Stores the list of client CSS files to load
	 * and build the global list of resource to load
	 */
	var setCss = function(externalcss) {
		for( var i=0 ; i<import_css.length ; i++ ) {
			css.push(baseUrl + styleimportsDir+ import_css[i]);
		}
		for( var i=0 ; i<local_css.length ; i++ ) {
			css.push(baseUrl  + styleDir + local_css[i]);
		}
		js.push.apply(css, externalcss);
	};
	/**
	 * Load all resources: must be invoked from the HTML page
	 */
	var loadAll = function() {
		loadNextCss();
		loadScripts();
	};

	var jss = {};
	jss.loadAll = loadAll;
	jss.setScripts = setScripts;
	jss.setMinScripts = setMinScripts;
	jss.setCss = setCss;
	return jss;
}();