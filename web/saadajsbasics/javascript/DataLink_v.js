
/**
 * @param params { parentDivId: 'query_div',baseurl}
 * @returns
 */
function DataLink_mVc(params) {
	this.parentDiv = $("#" +params.parentDivId );
	this.baseurl = params.baseurl;
	this.forwardurl = params.forwardurl;
	this.textareaid = params.parentDivId + "_text";

	this.listener = null;
}
DataLink_mVc.prototype = {

		addListener : function(list){
			this.listener = list;
		},
		draw : function(){	
			var that = this;
			var url = (this.forwardurl != null )? (this.forwardurl+ "?target=" + encodeURIComponent( this.baseurl))
					: this.baseurl;
			Processing.show("Fetching Datalink Description at " + url);
			$.ajax({
				type: "GET",
				url: url,
				dataType: "xml",      
				error: function (xhr, ajaxOptions, thrownError) {
					Processing.hide();
					Modalinfo.error(url + "\n" + xhr.responseText + "\n" + thrownError);
				},
				success: function(xml) {
					Processing.hide();
					if( $(xml).find('VOTABLE').length != 0 ){
						that.processVOTABLEResponse(xml);
					} else { 
						that.processXMLResponse(xml);
					}
				}});
		},
		processVOTABLEResponse: function(xml){
			var html = "";
			var hasLink = false;
			$(xml).find('TR').each(function(){
				var i=0;
				var uri ="";
				var url ="";
				var semantic ="";
				var productType ="";
				var contentType ="";
				var size ="";
				
				var type = $(this).find('TD').each(function(){
					hasLink = true;
					switch(i){
					case 0: uri = $(this).text();break;
					case 1: url = $(this).text();break;
					case 2: productType = $(this).text();break;
					case 3: semantic = $(this).text();break;
					case 4: contentType = $(this).text();break;
					case 5: size = $(this).text();break;
					}
					i++;
				});
				html += "<fieldset>";
				html += "  <legend><span>Link <i>" + productType + "</i> <span></legend>";
				html += "    <a class=dldownload href='#' onclick='Location.changeLocation(&quot;" + url + "&quot);' title='Download link target'></a>";
				html += "    <span class=help>" +  semantic + "</span><br>";
				html += "    <span class=help><b>Product URI:</b> " +  uri + "</span><br>";
				html += "    <span class=help><b>Content:</b> " +  contentType + " " +  ((size == 0)? "": size +"b") + "</span>";
				html += "</fieldset>";
			});
			if( hasLink){
			Modalcommand.commandPanel("Link Browser", html);
			$("div.datalinkform").toggle();
			Modalcommand.setDivToggling(function() {that.buildWebserviceForm($(this).attr("id"));});
			} else {
				Modalinfo.info("No link available", "Datalink Info");
			}
		},
		processXMLResponse: function(xml){
			var that = this;
			var html = "";
			$(xml).find('endpoint').each(function(){
				var type = $(this).attr('type');
				var datatype = $(this).attr('datatype');
				html += "<fieldset>";
				html += "  <legend><span>Link <i>" + $(this).attr('name') + "</i> (" + datatype + ")<span></legend>";
				if( type == "download" ) {
					html += that.buildDownloadForm($(this), datatype);
				} else if( type == "webservice" ) {							
					html += that.buildWebserviceEntry($(this));
				} else {
					Modalinfo.error("Unknown link type " + type);
				}
				html += "</fieldset>";
			});
			Modalcommand.commandPanel("Link Browser", html);
			$("div.datalinkform").toggle();
			Modalcommand.setDivToggling(function() {that.buildWebserviceForm($(this).attr("id"));});
		},
		buildDownloadForm : function(xml, datatype) {
			var url = (this.forwardurl != null )? (this.forwardurl+ encodeURIComponent( xml.find('url').text()))
					: xml.find('url').text();
			url =  xml.find('url').text();
			var clk = (datatype == "preview")? "Modalinfo.openIframePanel(&quot;" +  url + "&quot);" 
					: "Location.changeLocation(&quot;" +  url + "&quot);";
			var html = "<div>";
			html += "    <a class=dldownload href='#' onclick='" + clk + "' title='Download link target'></a>";
			html += "    <span class=help>" +  xml.find('description').text() + "</span>";
			html += "</div>";
			return html;	
		},
		buildWebserviceEntry : function(xml) {
			var xmlurl =  xml.find('url').text();
			var url = (this.forwardurl != null )? (this.forwardurl+ encodeURIComponent( xmlurl))
					: xmlurl;
			var webservice = xml.attr("name");
			this.fireStoreWebService(webservice, xmlurl);
			return "<div id=" + webservice + " class=datalinkform >";
			+ "    <ul>";
			+ "      <li>Decription " +  xml.find('description').text() + "</li>";
			+ "      <li>type " +  xml.attr('type')+ "</li>";
			+ "      <li>URL <a href='#' onclick='Modalinfo.openIframePanel(&quot;" +  url + "&quot);'>click</a>"+ "</li>";
			+ "    </ul>";
			+ "</div>";
		},
		buildWebserviceForm : function(webservice) {
			if( webservice != null && !this.fireIsWebServiceLoaded(webservice) ) {
				var url = (this.forwardurl != null )? (this.forwardurl+ "?target=" +  encodeURIComponent(this.fireGetUrl(webservice)))
						: this.fireGetUrl(webservice);
				//var url = "forwardxmlresource?target=" +  encodeURIComponent(this.fireGetUrl(webservice));
				var that = this;
				var sliders = new Array();
				var fields = new Array();
				Processing.show("Build form for webservice " + webservice);
				$.ajax({
					type: "GET",
					url: url,
					dataType: "xml",
					error: function (xhr, ajaxOptions, thrownError) {
						Processing.hide();
						Modalinfo.error(url + "\n" + xhr.responseText + "\n" + thrownError);
					},
					success: function(xml) {
						Processing.hide();
						$('#' + webservice).html(''); 
						var baseurl = $(xml).find('baseurl').text();
						var html = "";
						html += "    <a class=dldownload href='#' id='" + webservice + "_submit' title='Submit the query'></a>";
						html += "    <span class=help>" +   $(xml).find('description').first().text() + "</span><br>";
						$('#' + webservice).append(html); 
						$(xml).find('parameter').each(function(){
							var name = $(this).attr('name');
							var id  = webservice + "_" + $(this).attr('name');
							html  = "    <span><b>" +  name + "</b></span>";
							html += "    <span class=help>" +  $(this).find('description').text() + "</span><br>";
							var range = $(this).find('range');
							var type = range.attr('type');
							if( type == "enum" ) {
								html += "&nbsp;&nbsp;<span>Value</span>&nbsp;";
								html += "<select id=" + id + '_input' + ">";
								$(range).find('value').each(function() {	
									var value = $(this);
									var selected = ( value.attr("type") == "default")? "selected": "";
									html += "<option " + selected + ">" + $(this).text() + "</option>";
								});
								html += "</select><br>";
								fields.push(name);
							} else if( type == "range" ) {
								var min=0, max=0, def=0;
								$(range).find('value').each(function() {	
									var value = $(this);
									var type = value.attr("type");
									if( type == "default") {
										def = $(this).text();
									} else if( type == "min") {
										min = $(this).text();
									} else if( type == "max") {
										max = $(this).text();
									}
								});
								var fmin = parseFloat(min);
								var fmax = parseFloat(max);
								var fdef = parseFloat(def);
								if( fmin >= fmax ||fdef < fmin ||fdef > fmax ) {
									Modalinfo.error("Unconsistant range (min=" + min + " max="+ max + " def=" + def + ") no input for this parameter");
								} else {
									html += '&nbsp;&nbsp;<label for="amount">Value</label>'
										+ ' <input type="text" id="' + id + '_input" style="width: 50px; border: 0;font-weight: bold;" value="' + def + '"/>'
										+ '<div style="display: inline-block;width: 300px" id="' + id + '"></div><br>'    ;
									sliders[id] = {id: id, min: fmin, max: fmax, def: fdef};
									fields.push(name );
								}
							} else {
								Modalinfo.error("Unconsistant range type " + type + " no input for this parameter");
							}
							$('#' + webservice).append(html); 
							for( var p in sliders) {
								var slider = sliders[p];
								$( "#" + slider.id ).slider({
									range: "min",
									value: slider.def,
									min: slider.min,
									max: slider.max,
									step: ((slider.max - slider.min)/20),
									slide: function( event, ui ) {
										$( "#" +  $(this).attr("id")+ '_input').val(ui.value );
									}
								});

							}
							that.fireWebServiceLoaded(webservice);
						});
						$( "#" + webservice + "_submit" ).click(function(){
							var query = baseurl;
							for( var f=0 ; f<fields.length ; f++ ) {
								query += "&" + fields[f] + "=" + $('#' + webservice + "_" + fields[f] + '_input').val();
							}
							//Location.changeLocation(query, query);
							Modalinfo.openIframePanel(query);
						});
					}});
			}
		},
		fireGetQuery: function() {
			return $("#" + this.textareaid ).val();
		},		
		/*
		 *	Params: {type, constraints}
		 *	where supported typed are "column" "orderby" "ucd" "position" "relation" "limit"
		 *  Label is used to identify the form  constraints are coming from
		 */
		fireStoreWebService : function(webservice, url) {
			this.listener.controlStoreWebService(webservice, url);
		},
		fireGetUrl: function(webservice) {
			return this.listener.controlGetUrl(webservice);
		},
		fireWebServiceLoaded: function(webservice) {
			this.listener.controlWebServiceLoaded(webservice);
		},
		fireIsWebServiceLoaded: function(webservice) {
			return this.listener.controlIsWebServiceLoaded(webservice);
		}

};

