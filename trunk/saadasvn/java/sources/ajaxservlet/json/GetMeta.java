package ajaxservlet.json;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaRelation;
import ajaxservlet.SaadaServlet;
import ajaxservlet.formator.DisplayFilter;
import ajaxservlet.formator.EntryDisplayFilter;
import ajaxservlet.formator.FlatfileDisplayFilter;
import ajaxservlet.formator.ImageDisplayFilter;
import ajaxservlet.formator.MiscDisplayFilter;
import ajaxservlet.formator.SpectrumDisplayFilter;
import ajaxservlet.formator.TableDisplayFilter;


/**
 * Servlet implementation class GetMeta
 * Protocol
 * --------------------------------------------------------
 * query		name			return
 * --------------------------------------------------------
 * datatree		--				global datatree of the DB
 * ah			category		attribute hanldler coll level
 * ah			tree_path		attribute hanldler coll level + relation of that collection + class level ucds
 * ah			classname		attribute hanldler class level
 * relation		relationname	description of the relation
 * aharray		tree_path		attribute hanldler array coll level + relation of that collection
 * aharray		classname		attribute hanldler array class level + relation of that collection
 */
public class GetMeta extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	@SuppressWarnings("unchecked")
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter("query");
		String name = request.getParameter("name");
		RequestDispatcher rd = null;
		printAccess(request, true);
		try {
			if( JSON_FILE_MODE ) {
				if( "datatree".equals(query)) {
					request.setAttribute("file", "datatree");
				}
				else if( "ah".equals(query)) {
					request.setAttribute("file", name);
				}


				rd = request.getRequestDispatcher("getjsonarray");
				rd.forward(request, response);
			}
			else {
				ServletOutputStream out = response.getOutputStream();
				if( "datatree".equals(query)) {
					rd = request.getRequestDispatcher("datatree");		
					rd.forward(request, response);			
				}
				else if( "aharray".equals(query)) {
					rd = request.getRequestDispatcher("aharray");		
					rd.forward(request, response);			
				}
				else if( "ah".equals(query)) {
					String[] nodes = name.split("\\.");
					int category;
					String collection;
					MetaClass mc= null;
					/*
					 * data node like collection.category
					 */
					if( nodes.length == 2 ) {
						category = Category.getCategory(nodes[1]);
						collection = nodes[0];
					}
					/*
					 * data node like classname
					 */				
					else if( nodes.length == 1 ) {
						mc = Database.getCachemeta().getClass(nodes[0]);
						category = mc.getCategory();
						collection = mc.getCollection_name();
					}
					else {				
						reportJsonError(request, response, "Query badly formed (" + nodes.length + " nodes)");
						return;
					}
					DisplayFilter colfmtor;
					switch(category) {
					case Category.TABLE :			
						colfmtor = new TableDisplayFilter(collection);
						colfmtor.setMetaClass(mc);
						break;
					case Category.ENTRY :		
						colfmtor = new EntryDisplayFilter(collection);
						colfmtor.setMetaClass(mc);
						break;
					case Category.IMAGE :		
						colfmtor = new ImageDisplayFilter(collection);
						colfmtor.setMetaClass(mc);
						break;
					case Category.SPECTRUM :		
						colfmtor = new SpectrumDisplayFilter(collection);
						colfmtor.setMetaClass(mc);
						break;
					case Category.MISC :		
						colfmtor = new MiscDisplayFilter(collection);
						colfmtor.setMetaClass(mc);
						break;
					case Category.FLATFILE :		
						colfmtor = new FlatfileDisplayFilter(collection);
						colfmtor.setMetaClass(null);
						break;
					default :
						reportJsonError(request, response, "category \"" + Category.explain(category) + "\" not supported");
						return ;
					}
					JSONObject retour = new JSONObject();
					retour.put("collection", collection);
					retour.put("category", Category.explain(category));

					String[] cls = Database.getCachemeta().getClassesOfCollection(collection, category);
					JSONArray array = new JSONArray();
					for( String cl: cls) {
						array.add(cl);
					}
					retour.put("classes", array);

					array = new JSONArray();
					for( AttributeHandler col: colfmtor.getQueriableColumns()) {
						array.add(JsonUtils.JsonSerialize(col));						
					}
					retour.put("attributes", array);

					array = new JSONArray();
					String[] rns = Database.getCachemeta().getRelationNamesStartingFromColl(collection, category);
					for( String rn: rns ){
						MetaRelation mr = Database.getCachemeta().getRelation(rn);
						ArrayList<String> qls = mr.getQualifier_names();
						JSONObject jsrelation = new JSONObject();
						jsrelation.put("name", rn);
						jsrelation.put("ending_collection", mr.getSecondary_coll());
						jsrelation.put("ending_category", Category.explain(mr.getSecondary_category()));
						JSONArray jsq  = new JSONArray();
						for( String q: qls) {
							jsq.add(q);						
						}
						jsrelation.put("qualifiers", jsq);
						array.add(jsrelation);
					}					
					retour.put("relations", array);
					/*
					 * Append the list of queriable UCDS at both class and collection level
					 */
					if( mc == null ) {
						AttributeHandler[] qahs = Database.getCachemeta().getUCDs(collection, category, true);
						array = new JSONArray();
						for( AttributeHandler col: qahs) {
							array.add(JsonUtils.JsonSerialize(col));						
						}
					}
					// collection level UCD not suported by the query engine
					else {
//						for( AttributeHandler col: Database.getCachemeta().getCollection(collection).getUCDs(category, true) ) {
//							array.add(JsonUtils.JsonSerialize(col));						
//						}
						for( AttributeHandler col: mc.getUCDFields(true) ) {
							array.add(JsonUtils.JsonSerialize(col));						
						}


					}
					retour.put("queriableucds", array);
					/*
					 * Push the JSon object into the stream
					 */
					JsonUtils.teePrint(out,retour.toJSONString());
				}
				else {
					reportJsonError(request, response, "request " + query  + " unsupported");
					return;
				}
			}

		}catch( Exception e ) {
			reportJsonError(request, response, e);
		}

	}
}
