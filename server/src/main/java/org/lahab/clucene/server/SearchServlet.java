package org.lahab.clucene.server;

/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 NTNU
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.lahab.clucene.server.searcher.SearcherNode;


public class SearchServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PATH = "/_search";
	
	private SearcherNode _searcher;
	
	public SearchServlet(SearcherNode searcher) throws IOException {
		_searcher = searcher;
		_searcher.start();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = URLDecoder.decode(request.getParameter("q"), "UTF-8");
		if (query == null) {
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
		response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject results = new JSONObject();
        try {
        	LOGGER.info("Searching:" + query);
			Document[] docs = _searcher.search(query);
			for (int i = 0; i < docs.length; i++) {
				results.accumulate(docs[i].get("URI"), docs[i].get("title"));
			}
			response.getWriter().write(results.toString());
        } catch (ParseException e) {
			response.getWriter().write("{\"message\": \"Your query couldn't be parsed\"}");
		}
	}

}
