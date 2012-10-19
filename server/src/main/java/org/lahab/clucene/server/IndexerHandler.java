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
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.microsoft.windowsazure.services.core.storage.StorageException;

public class IndexerHandler extends AbstractHandler {
	protected IndexerNode _node;
	
	
	public void setNode(IndexerNode node) {
		_node = node;
	}

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int arg3) throws IOException,
			ServletException {
		
		Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        System.out.println(request.getRequestURI());
        Document doc = new Document();
        if (request.getMethod() == "POST") {
	        BufferedReader buf = request.getReader();
	        String value = buf.readLine(); 
	        doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
	        ArrayList<Document> docs = new ArrayList<Document>();
	        docs.add(doc);
	        _node.addDocuments(docs);
	        response.getWriter().println("<h1>" + value + "</h1>");
        } else {
        	try {
				_node.download();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
}
