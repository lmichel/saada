package ajaxservlet.json;

import ajaxservlet.SaadaServlet;
import ajaxservlet.formator.AttributeHandlerDisplayFilter;
import ajaxservlet.formator.DisplayFilter;
import ajaxservlet.formator.EntryDisplayFilter;
import ajaxservlet.formator.FlatfileDisplayFilter;
import ajaxservlet.formator.ImageDisplayFilter;
import ajaxservlet.formator.InstanceDisplayFilter;
import ajaxservlet.formator.MiscDisplayFilter;
import ajaxservlet.formator.SpectrumDisplayFilter;
import ajaxservlet.formator.TableDisplayFilter;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
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

/**
 * Servlet implementation class JSAttributeHandlers
 */
public class JSAttributeHandlers extends SaadaServlet implements Servlet {
	private static final long serialVersionUID = 1L;
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
		String name = request.getParameter("name");
		printAccess(request, false);
		try {
			String[] nodes = name.split("\\.");
			int category;
			String collection;
			MetaClass mc= null;
			DisplayFilter colfmtor;
			/*
			 * data node like collection.category
			 */
			if( nodes.length == 2 ) {
				category = Category.getCategory(nodes[1]);
				collection = nodes[0];
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
			}
			/*
			 * data node like classname
			 */				
			else if( nodes.length == 1 ) {
				mc = Database.getCachemeta().getClass(nodes[0]);
				category = mc.getCategory();
				collection = mc.getCollection_name();
				colfmtor = new InstanceDisplayFilter(collection);
				colfmtor.setMetaClass(mc);
			}
			else {				
				reportJsonError(request, response, "Query badly formed (" + nodes.length + " nodes)");
				return;
			}


			JSONObject retour = new JSONObject();
			/*
			 * Reply format
			 * { collection: {name:name, category: category, attributes: { aoColumns: [], aaData: [[]]}, starting_relations[], ending_relations[]}, 
			 *  class: {name:name, attributes: { aoColumns: [], aaData: [[]]}}
			 *  }
			 */

			JSONObject jscoll = new JSONObject();
			jscoll.put("name", collection);
			jscoll.put("category", Category.explain(category));

			JSONObject jsattributes = new JSONObject();			
			JSONArray ahcols = new JSONArray();
			AttributeHandlerDisplayFilter ahdf = new AttributeHandlerDisplayFilter();
			for( AttributeHandler ah: colfmtor.getQueriableColumns()) {
				for( String ahc: ahdf.getDisplayedColumns()) {
					JSONObject jc = new JSONObject();
					jc.put("sTitle", ahc);
					ahcols.add(jc);
				}
				break;
			}			
			jsattributes.put("aoColumns", ahcols);

			JSONArray ahvals = new JSONArray();
			for( AttributeHandler ah: colfmtor.getQueriableColumns()) {
				if( !ah.getNameattr().startsWith("_")) {
					JSONArray ahval = new JSONArray();
					for( String ahv: ahdf.getRow(ah, -1)) {
						ahval.add(ahv);
					}
					ahvals.add(ahval);
				}
			}
			jsattributes.put("aaData", ahvals);
			jscoll.put("attributes", jsattributes);


			JSONObject jstartrelations = new JSONObject();			
			JSONArray relcols = new JSONArray();
			JSONObject jo;
			jo = new JSONObject(); jo.put("sTitle", "name")       ; relcols.add(jo);
			jo = new JSONObject(); jo.put("sTitle", "target")     ; relcols.add(jo);
			jo = new JSONObject(); jo.put("sTitle", "qualifiers") ; relcols.add(jo);
			jo = new JSONObject(); jo.put("sTitle", "correlator") ; relcols.add(jo);
			jo = new JSONObject(); jo.put("sTitle", "description"); relcols.add(jo);
			jstartrelations.put("aoColumns", relcols);

			JSONArray relvals = new JSONArray();
			String[] rns = Database.getCachemeta().getRelationNamesStartingFromColl(collection, category);
			for( String rn: rns) {
				MetaRelation mr = Database.getCachemeta().getRelation(rn);
				ArrayList<String> qls = mr.getQualifier_names();				
				JSONArray relval = new JSONArray();
				relval.add(rn);
				relval.add(mr.getSecondary_coll() + "." + Category.explain(mr.getSecondary_category()));
				String sq = "";;
				for( String q: qls) {
					sq += q + " " ;
				}
				relval.add(sq);
				relval.add(mr.getCorrelator());
				relval.add(mr.getDescription());
				relvals.add(relval);
			}
			jstartrelations.put("aaData", relvals);
			jscoll.put("startingRelations", jstartrelations);


			JSONObject jendingrelations = new JSONObject();			
			jendingrelations.put("aoColumns", relcols);

			relvals = new JSONArray();
			rns = Database.getCachemeta().getRelationNamesEndingOnColl(collection, category);
			for( String rn: rns) {
				MetaRelation mr = Database.getCachemeta().getRelation(rn);
				ArrayList<String> qls = mr.getQualifier_names();				
				JSONArray relval = new JSONArray();
				relval.add(rn);
				relval.add(mr.getSecondary_coll() + "." + Category.explain(mr.getSecondary_category()));
				String sq = "";;
				for( String q: qls) {
					sq += q + " " ;
				}
				relval.add(sq);
				relval.add(mr.getCorrelator());
				relval.add(mr.getDescription());
				relvals.add(relval);
			}
			jendingrelations.put("aaData", relvals);
			jscoll.put("endingRelations", jendingrelations);

			retour.put("collectionLevel", jscoll);

			if( mc != null ) {
				JSONObject jsclass = new JSONObject();
				jsclass.put("name", mc.getName());

				jsattributes = new JSONObject();
				jsattributes.put("aoColumns", ahcols);
				ahvals = new JSONArray();
				for( AttributeHandler ah: colfmtor.getQueriableColumns()) {
					if( ah.getNameattr().startsWith("_")) {
						JSONArray ahval = new JSONArray();
						for( String ahv: ahdf.getRow(ah, -1)) {
							ahval.add(ahv);
						}
						ahvals.add(ahval);
					}
				}

				jsattributes.put("aaData", ahvals);
				jsclass.put("attributes", jsattributes);
				retour.put("classLevel", jsclass);

			}
			JsonUtils.teePrint(response.getOutputStream(), retour.toJSONString());

		}catch( Exception e ) {
			reportJsonError(request, response, e);
		}

	}
}
