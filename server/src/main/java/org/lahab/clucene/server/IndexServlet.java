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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class IndexServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected IndexerNode _node;
	protected WikipediaParser parser = new WikipediaParser();
	
	public IndexServlet(IndexerNode node) {
		_node = node;
	}
	
	public void setNode(IndexerNode node) {
		_node = node;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("session=" + request.getSession(true).getId());
        
        Document doc;
		try {
			doc = parser.parse(request.getInputStream(), "bingou");
		    ArrayList<Document> docs = new ArrayList<Document>();
		    docs.add(doc);
		    _node.addDocuments(docs);
		    response.getWriter().println("<h1>bingou</h1>");
		} catch (Exception e) {
			e.printStackTrace();
			response.getWriter().println("failed");
		}


	}
}
