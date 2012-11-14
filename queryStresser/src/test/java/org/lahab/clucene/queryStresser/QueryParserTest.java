package org.lahab.clucene.queryStresser;

/*
 * #%L
 * queryStresser
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

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.lahab.clucene.utils.JSONConfiguration;

public class QueryParserTest {

	private static ArrayList<String> queries = new ArrayList<String>();
	
	static {
		queries.add("query1");
		queries.add("query2");
		queries.add("query3");
		queries.add("query4");
	}

	static private QueryParser Nqp;
	static private QueryParser Uqp;

	@Test
	public void testQueryParser() throws Exception {
		JSONObject obj = new JSONObject();
		File file = new File("test.json");
		file.createNewFile();
		FileUtils.writeLines(file, queries);
		obj.accumulate("queryFile", "test.json");
		obj.accumulate("nbQueries", 4);
		obj.accumulate("uniform", true);
		obj.accumulate("gaussian", JSONSerializer.toJSON("{\"mean\": 2, \"stddev\": 0.2}"));
		Uqp = new QueryParser(new JSONConfiguration(obj));
		assertTrue(Uqp._mean == 0.0);
		assertTrue(Uqp._stddev == 0.0);
		assertSame(Uqp._uniform, true);
		for (int i = 0; i < Uqp._queries.length; i++) {
			assertEquals(Uqp._queries[i], queries.get(i));
		}
		
		obj = new JSONObject();
		obj.accumulate("queryFile", "test.json");
		obj.accumulate("nbQueries", 3);
		obj.accumulate("gaussian", JSONSerializer.toJSON("{\"mean\": 1, \"stddev\": 0.2}"));
		Nqp = new QueryParser(new JSONConfiguration(obj));
		assertTrue(Nqp._mean == 1);
		assertTrue(Nqp._stddev == 0.2);
		assertFalse(Nqp._uniform);
		for (int i = 0; i < Nqp._queries.length; i++) {
			assertEquals(Nqp._queries[i], queries.get(i));
		}
		file.delete();
	}

	@Test
	public void testGetQuery() {
		for (int i = 0; i < 100; i++) {
			Uqp.getQuery();
			Nqp.getQuery();
		}
	}

}
