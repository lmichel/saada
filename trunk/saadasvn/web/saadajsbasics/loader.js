/**
 * Loads automatically all scripts used by saadahsbasics
 */
resourceLoader = function() {
	var css = ["basics.css", "domain.css"];
	var that = this;
	var baseBasicUrl = "saadajsbasics/javascript/";
	var baseUrl = "";
	var local_js = ["basics.js"
	                , "WebSamp"
	                , "KWConstraint"
	                , "AttachedData_v.js", "VizierKeywords_v.js","OrderBy_v.js", "ConeSearch_v.js", "ConstList_v.js", "FieldList_v.js", "Sorter_v.js"
	                , "DataLink"
	                , "ConstQEditor"
	                , "QueryTextEditor"
	                , "domain.js"];
	var local_min_js = ["basics.js"
	                , "WebSamp"
	                , "DataLink"
	                , "domain.js"];
	var imp_js = [   "jsimports/ui/jquery-ui.js",
	                 "jsimports/jquery.simplemodal.js",
	                 "jsimports/jquery.alerts.js",
	                 "jsimports/jquery.dataTables.js",
	                 "jsimports/jquery.prints.js",
	                 "jsimports/jquery.tooltip.js",
	                 "jsimports/jquery.form.js",
	                 ];
	var js = new Array();
	/*
	 * Check if saadajsbasics resources are installed locally
	 */

	baseUrl = "http://localhost:8888/jsresources/";
	$.ajax({
		url: baseUrl + 'saadajsbasics/loader.js',
		async: false, 
		dataType: "text",
		error: function(data) {
			baseUrl = "http://obs-he-lm:8888/jsStuff/";
			console.log("Try " + baseUrl + " as jsresource base URL");
			$.ajax({
				url: baseUrl + 'saadajsbasics/loader.js',
				async: false, 
				dataType: "text",
				error: function(data) {
					baseUrl = "";
					console.log("Try " + baseUrl + " as jsresource base URL");					
					$.ajax({
						url: 'saadajsbasics/loader.js',
						async: false, 
						dataType: "text",
						error: function(data) {					
							baseUrl = "http://saada.unistra.fr/jsresources/";
							console.log("Try " + baseUrl + " as jsresource base URL");
						},
						success: function() {
							console.log("Take " + baseUrl + " as jsresource base URL");
						}   
					});						
				} ,
				success: function() {
					console.log("Take " + baseUrl + " as jsresource base URL");
				}                  
			});
		}   ,
		success: function() {
			console.log("Take ./ as jsresource base URL");
		}
	});
	console.log(baseUrl);

	var loadNextScript = function() {
		var script = document.createElement("script");
		var head = document.getElementsByTagName('HEAD').item(0);
		script.onload = script.onreadystatechange = function() {
			console.log(baseUrl + js[0] + " loaded" );
			js.shift();
			if( js.length > 0 ) loadNextScript();
		};
		script.src = js[0];
		script.type = "text/javascript";
		head.appendChild( script);
	};

	/*
	 * Usng jquery make the log traces pointing on jsquery code instaed on my js code
	 */
	var loadNextScriptxxx = function() {
		console.log(this.baseUrl);
		$.ajax({
			url: js[0], 
			async: false, 
			dataType: "script",
			success: function(data) {
				console.log(baseUrl + js[0] + " loaded" );
				js.shift();
				if( js.length > 0 ) loadNextScript();
			} ,                
			error: function(data) {
				console.log("Cannot load " + js[0] );
				alert("Cannot load " +  js[0]);
			}                  
		});
	};


	/**
	 * externalscripts: array of scripts to be loaded after jsresources 
	 */
	var loadScripts = function(externalscripts) {
		//	console.log("----------- " + that.baseUrl + " " + baseBasicUrl);
		for( var i=0 ; i<local_js.length ; i++ ) {
			var jsf =  baseUrl + baseBasicUrl + local_js[i];
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
		console.log(baseUrl);
		loadNextScript();
	};
	var loadMinScripts = function(externalscripts) {
		//	console.log("----------- " + that.baseUrl + " " + baseBasicUrl);
		for( var i=0 ; i<local_min_js.length ; i++ ) {
			var jsf =  baseUrl + baseBasicUrl + local_min_js[i];
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
		console.log(baseUrl);
		loadNextScript();
	};

	var jss = {};
	jss.loadScripts = loadScripts;
	jss.loadMinScripts = loadMinScripts;
	return jss;


}();