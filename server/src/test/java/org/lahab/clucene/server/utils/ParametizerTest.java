package org.lahab.clucene.server.utils;

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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Test;

public class ParametizerTest {

	private static Map<String, Object> def = new HashMap<String, Object>();
	static {
		def.put("bool", true);
		def.put("bool.true", true);
		def.put("bool.false", false);
		def.put("int", 3);
		def.put("double", 3.1);
		def.put("string", "hello");
	}
	
	@Test
	public void testDefaultParametizer() throws Exception {
		JSONObject json = (JSONObject) JSONSerializer.toJSON("{}");
		Configuration config = new Configuration(json);
		Parametizer p = new Parametizer(def, config);
		assertTrue((Boolean) p._parameters.get("bool"));
		assertTrue((Boolean) p._parameters.get("bool.true"));
		assertFalse((Boolean) p._parameters.get("bool.false"));
		assertEquals((Integer) p._parameters.get("int"), Integer.valueOf(3));
		assertEquals((Double) p._parameters.get("double"), Double.valueOf(3.1));
		assertEquals((String) p._parameters.get("string"), String.valueOf("hello"));
		
		p = new Parametizer(def, null);
		assertTrue((Boolean) p._parameters.get("bool"));
		assertTrue((Boolean) p._parameters.get("bool.true"));
		assertFalse((Boolean) p._parameters.get("bool.false"));
		assertEquals((Integer) p._parameters.get("int"), Integer.valueOf(3));
		assertEquals((Double) p._parameters.get("double"), Double.valueOf(3.1));
		assertEquals((String) p._parameters.get("string"), String.valueOf("hello"));
	}
	
	@Test
	public void testRedefine() throws Exception {
		JSONObject json = (JSONObject) JSONSerializer.toJSON("{\"bool\": {\"true\": false, \"false\": true}, \"int\":2, \"double\": 2.1, \"string\": \"bye\"}");
		Configuration config = new Configuration(json);
		Parametizer p = new Parametizer(def, config);
		assertTrue((Boolean) p._parameters.get("bool"));
		assertFalse((Boolean) p._parameters.get("bool.true"));
		assertTrue((Boolean) p._parameters.get("bool.false"));
		assertEquals((Integer) p._parameters.get("int"), Integer.valueOf(2));
		assertEquals((Double) p._parameters.get("double"), Double.valueOf(2.1));
		assertEquals((String) p._parameters.get("string"), String.valueOf("bye"));
		
		json = (JSONObject) JSONSerializer.toJSON("{\"int\":2}");
		config = new Configuration(json);
		p = new Parametizer(def, config);
		
		assertTrue((Boolean) p._parameters.get("bool"));
		assertTrue((Boolean) p._parameters.get("bool.true"));
		assertFalse((Boolean) p._parameters.get("bool.false"));
		assertEquals((Integer) p._parameters.get("int"), Integer.valueOf(2));
		assertEquals((Double) p._parameters.get("double"), Double.valueOf(3.1));
		assertEquals((String) p._parameters.get("string"), String.valueOf("hello"));
		
	}

	@Test
	public void testGet() throws Exception {
		JSONObject json = (JSONObject) JSONSerializer.toJSON("{\"int\":2, \"double\": 2.1, \"string\": \"bye\"}");
		Configuration config = new Configuration(json);
		Parametizer p = new Parametizer(def, config);
		
		assertTrue(p.getBoolean("bool.true"));
		assertFalse(p.getBoolean("bool.false"));
		assertEquals(p.getInt("int"), 2);
		assertTrue(p.getDouble("double") == 2.1);
		assertEquals(p.getString("string"), "bye");
	}

}
