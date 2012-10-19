package org.lahab.clucene.client;

/*
 * #%L
 * client
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.mortbay.jetty.handler.AbstractHandler;

public class SearchHandler extends AbstractHandler {

	protected SearchNode _node;
	
	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int arg) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        //baseRequest.setHandled(true);
        if (request.getMethod() == "GET") {
        	try {
        		System.out.println(request.getParameter("q"));
				ScoreDoc[] hits = _node.search(request.getParameter("q"));
			    // 4. display results
				response.getWriter().println("Found " + hits.length + " hits.");
			    for(int i=0;i<hits.length;++i) {
			      int docId = hits[i].doc;
			      Document d = _node.doc(docId);
			      response.getWriter().println((i + 1) + ". " + d.get("title"));
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
        	response.getWriter().println("<h1>Hello World</h1>");
        }
	}
	
	public void setNode(SearchNode node) {
		_node = node;
	}

}

