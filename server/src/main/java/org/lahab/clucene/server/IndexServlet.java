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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lahab.clucene.server.indexer.IndexerNode;

/**
 * A servlet responsible of indexing
 * the different actions are:
 * 	- start: starts the crawling and indexing
 * 	- stop: shutdown the indexing/crawling
 * 	- pause: pause the indexing/crawling
 * 	- resume: resumes the indexing/crawling
 * @author charlymolter
 *
 */
public class IndexServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(IndexServlet.class.getName());
	
	private static final long serialVersionUID = 1L;
	/** the path from where that servlet can joined */
	public static final String PATH = "/_index";
	/** Our indexer thread */
	protected IndexerNode _indexer;
	
	public IndexServlet(IndexerNode indexer) {
		_indexer = indexer;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        if (request.getRequestURI().equals(PATH + "/start")) {
        	LOGGER.finer("start");
			_indexer.start();
			response.getWriter().write("Indexer started");
        } else if (request.getRequestURI().equals(PATH + "/stop")) {
        	LOGGER.finer("stop");
        	_indexer.stop();
			response.getWriter().write("Indexer shutingdown");
        }  else if (request.getRequestURI().equals(PATH + "/pause")) {
        	LOGGER.finer("pause");
        	try {
				_indexer.wait();
				response.getWriter().write("Indexer paused");
        	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else if (request.getRequestURI().equals(PATH + "/resume")) {
        	LOGGER.finer("resume");
        	_indexer.notify();
			response.getWriter().write("Indexer restarted");
        } else if (request.getRequestURI().equals(PATH + "/download")) {
        	LOGGER.finer("download");
        	try {
        		//_indexer.wait();
				_indexer.download();
				//_indexer.notify();
        	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
	}
}
