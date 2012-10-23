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

import javax.servlet.http.HttpServlet;

import org.lahab.clucene.server.searcher.SearcherNode;


public class SearchServlet extends HttpServlet {
	private SearcherNode _searcher;
	
	public SearchServlet(SearcherNode searcher) {
		_searcher = searcher;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PATH = "_search";

}
