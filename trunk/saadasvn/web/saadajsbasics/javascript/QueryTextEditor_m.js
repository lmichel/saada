/**
 * @returns {QueryTextEditor_Mvc}
 */
function QueryTextEditor_Mvc() {
	this.colConst =  new Array();
	this.obConst  = new Array();
	this.ucdConst = new Array();
	this.posConst = new Array();
	this.relConst = new Array();
	this.kwConst  = new Array();
	this.limConst = "";
	this.query = "";
	this.treePath = null;
	this.listener = null;
};

QueryTextEditor_Mvc.prototype = {
		addListener : function(list){
			this.listener = list;
		},
		getQuery: function() {
			return this.query;
		},
		processAddConstraint: function(label, type, constraints) {
			Out.debug("add constraint of type " + type + " from form " + label);
			if( type == "column") {
				this.delConst(label, this.colConst);
				this.addConstraintToArray(label, constraints, this.colConst) ;
			} else if( type == "orderby" && constraints.length > 0) {
				this.obConst = constraints[0];				
			} else if( type == "ucd") {
				this.delConst(label, this.ucdConst);
				this.addConstraintToArray(label, constraints,this. ucdConst) ;
			} else if( type == "position") {
				this.delConst(label,this.posConst);
				this.addConstraintToArray(label, constraints, this.posConst) ;
			} else if( type == "relation") {
				this.delConst(label, this.relConst);
				this.addConstraintToArray(label, constraints, this.relConst) ;
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
				this.addConstraintToArray(label, constraints, this.kwConst) ;
			} else if( type == "limit" && constraints.length > 0) {
				this.limConst = constraints[0];				
			} else {
				Modalinfo.error("QueryTextEditor do not know what to do with a constraint typed as " +type);
			}		
			this.buildQuery();
			this.notifyDisplayQuery();
		},
		addConstraintToArray: function(label, constraints, constArray) {
			if(constraints != null &&  $.trim(constraints) != "" ) {
				constArray.push({label:label, constraints: constraints});
			}
		},
		processDelConstraint: function(label, type) {
			Out.debug("Del constraint of type " + type + " from form " + label);
			if( type == "column") {
				this.delConst(label, this.colConst);
			} else if( type == "orderby") {
				this.obConst = "";				
			} else if( type == "ucd") {
				this.delConst(label, this.ucdConst);
			} else if( type == "position") {
				this.delConst(label, this.posConst);
			} else if( type == "relation") {
				this.delConst(label, this.relConst);
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
			} else if( type == "limit") {
				this.limConst = "";												
			} else {
				Modalinfo.error("QueryTextEditor do not know what to do with a constraint typed as " + type);
			}	
			this.buildQuery();
			this.notifyDisplayQuery();
		},
		delConst: function(label, constArray) {
			for( var i=0 ; i<constArray.length ; i++ ) {
				if( constArray[i].label == label){
					constArray.splice(i,1);
					return;
				}
			}
		},
		buildQuery: function() {
			this.query =  this.buildWhereConstraint("\nWherePosition"         , ","  , this.posConst)
			+ this.buildWhereConstraint("\nWhereAttributeSaada"   , "AND", this.colConst)
			+ this.buildWhereConstraint("\nWhereUCD"              , "AND", this.ucdConst)
			+ this.buildWhereConstraint("\nWhereRelation"         , " "  , this.relConst)
			+ this.buildWhereConstraint("\nHavingCounterpartsWith", ","  , this.kwConst);
			if( this.obConst != "" ){
				this.query += "\nOrder By " + this.obConst + "\n";
			}
			if( this.limConst != "" && !isNaN(this.limConst) ){
				this.query += "\nLimit " + this.limConst + "\n";
			}
		},
		buildWhereConstraint : function(stmt, logical, constArray){
			var constrt ="";		
			var openPar = '';
			var closePar = '';
			if( logical == 'AND' ) {
				openPar = '(';
				closePar = ')';
			}
			for( var i=0 ; i<constArray.length ; i++ ){
				if( i > 0 ) {
					constrt += (logical.trim() == "") ?"\n":  ("\n    " + logical + "\n") ;
				}
				var tc = $.trim(constArray[i].constraints);
				constrt += (constArray.length > 1 )? ("    " +  openPar + tc + closePar):  constArray[i].constraints;
			}
			return (constrt == "")? "" : stmt + " {\n" + constrt + "\n}\n";
		},
		notifyDisplayQuery:  function() {
			this.listener.controlDisplayQuery(this.query);
		},
		setTreePath: function(treePath){
			this.treePath = $.extend({},treePath);
		}
};

/**
 * Subclass of QueryTextEditor_Mvc handling adql queries
 */
function ADQLTextEditor_Mvc(){
	QueryTextEditor_Mvc.call(this);
	this.selectConst = new Array();
	this.joinedTables = new Array();
	this.flatJoined = new Array();
	/*
	 * 
	 * {tablename -> {joinKey [select][orderby][constrainst]}
	 * joinKey {source_table, source_column, target_table, target_column}
	 */
	this.tableConst = new Array();

};
/**
 * Method overloading
 */
ADQLTextEditor_Mvc.prototype = Object.create(QueryTextEditor_Mvc.prototype, {	
	processAddConstraint: {
		value: function(label, type, constraints, tableJoin) {
			Out.debug("add constraint of type " + type + " from form " + label);
			if( type == "select") {
				this.selectConst = [{label:label, constraints: constraints}];
			} else if( type == "column") {
				this.delConst(label, this.colConst);
				this.addConstraintToArray(label, constraints, this.colConst) ;
			} else if( type == "orderby" && constraints.length > 0) {
				this.obConst = [{label:label, constraints: constraints[0]}];
			} else if( type == "kwconst") {
				this.delConst(label, this.kwConst);
				this.addConstraintToArray(label, constraints, this.kwConst) ;
			} else if( type == "limit" && constraints.length > 0) {
				this.limConst = {label:label, constraints: constraints[0]};;				
			} else {
				Modalinfo.error("QueryTextEditor do not know what to do with a constraint typed as " +type);
				return;
			}	
			this.joinedTables = new Array();
			this.flatJoined = new Array();
			if( tableJoin != null ) {
				for( var jt in tableJoin ) {
					var joinKey = tableJoin[jt];
					if( joinKey.target_column == "" ) {
						this.flatJoined.push(joinKey.target_table);
					} else {
						this.joinedTables[jt] = joinKey;
					}
				}
			}
			this.buildQuery();
			this.notifyDisplayQuery();
		}
	},

	getJoin : { 
		value: function() {
			var retour = "";
			for( var jt in this.joinedTables ) {
				var joinKey = this.joinedTables[jt];
				var tt = (joinKey.target_datatreepath.schema + "." + joinKey.target_datatreepath.table).quotedTableName();
				retour += "JOIN " + tt + " ON " 
				+ this.getCurrentTableName().quotedTableName() + "." +  joinKey.source_column.quotedTableName()
				+ " = " 
				+  tt + "." + joinKey.target_column.quotedTableName()  + "\n";		
			}
			return retour;
		}
	},

	buildQuery :{
		value: function() {
			var tableName = this.getCurrentTableName().quotedTableName();
			var li = this.limConst.constraints;
			var topLimit = ( li != undefined) ? ' TOP ' + li + ' ': '';
			this.query = "";
			this.query += this.buildWhereConstraint("SELECT " + topLimit , ",", this.selectConst);
			this.query += "FROM " + tableName ;
			if( this.flatJoined != null ){
				for( var i in this.flatJoined) {
					this.query += ", " + this.flatJoined[i];
				}
			}
			this.query += "\n";
			this.query += this.getJoin();			
			this.query += this.buildWhereConstraint("WHERE"   , "AND", this.colConst);
			this.query += this.buildWhereConstraint("ORDER BY", ","  , this.obConst);
		}
	},

	buildWhereConstraint : {
		value: function(stmt, logical, constArray){
			var constrt ="";		
			var openPar = '';
			var closePar = '';
			if( logical == 'AND' ) {
				openPar = '(';
				closePar = ')';
			}
			for( var i=0 ; i<constArray.length ; i++ ){
				if( i > 0 ) {
					constrt += (logical.trim() == "") ?"\n":  ("\n    " + logical + "\n") ;
				}
				var tc = $.trim(constArray[i].constraints) ;
				constrt += (constArray.length > 1 )? "    " +  openPar + tc + closePar:  constArray[i].constraints;
			}
			return (constrt == "")? "" : stmt + " " + constrt + "\n";
		}
	},

	getCurrentTableName: {
		value: function(){
			return this.treePath.schema + "." + this.treePath.table;
		}
	},
//	quoteTableName : {
//		value: function(tableName){
//			var regex = /([^.]*)\.(.*)/;
//			var results = regex.exec(tableName);
//			var table, schema;
//			if(!results){
//				table = tableName;
//				schema = "";
//			} else if( results.length == 2 ) {
//				table = results[1]; 
//				schema = "";
//			} else  {
//				table =  results[2];  
//				schema = results[1] + ".";
//			}
//			if( table.match(/^[a-zA-Z0-9][a-zA-Z0-9_]*$/ ) ){
//				return schema + table;
//			} else {
//				return schema + '"' + table +'"';
//			}
//		}
//	}
});

