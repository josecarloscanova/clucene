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

import java.io.File;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigurationTest {

	@Rule  
	public TemporaryFolder folder = new TemporaryFolder(); 
	
	@Test
	public void testConfigurationString() throws Exception {
		File file = new File("foo.json");
		file.delete();
		file.createNewFile();
		String json = "{\"key1\": 1, \"key2\": 2}";
		FileUtils.writeStringToFile(file, json);
		JSONObject obj = (JSONObject) JSONSerializer.toJSON(json);
		Configuration conf = new Configuration("foo.json");
        assertNotNull(conf._config);
        assertEquals(obj.get("key1"), conf._config.get("key1"));
        assertEquals(obj.get("key2"), conf._config.get("key2"));
        assertFalse(conf._config.containsKey("key3"));
        file.delete();
	}

	@Test
	public void testConfigurationJSONObject() {
		JSONObject obj = (JSONObject) JSONSerializer.toJSON("{\"key1\": 1, \"key2\": 2}");
        Configuration conf = new Configuration(obj);
        assertNotNull(conf._config);
        assertEquals(obj.get("key1"), conf._config.get("key1"));
        assertEquals(obj.get("key2"), conf._config.get("key2"));
        assertFalse(conf._config.containsKey("key3"));
	}

	@Test
	public void testContainsKey() {
		JSONObject obj = (JSONObject) JSONSerializer.toJSON("{\"key1\": 1, \"key2\": 2, \"compound\": {\"x\": \"hoy\"}}");
        Configuration conf = new Configuration(obj);
        assertTrue(conf.containsKey("key1"));
        assertTrue(conf.containsKey("key2"));
        assertFalse(conf.containsKey("key3"));
        assertTrue(conf.containsKey("compound"));
        assertTrue(conf.containsKey("compound.x"));
        assertFalse(conf.containsKey("compound.y"));
	}

	@Test(expected=Exception.class)
	public void testGet() throws Exception {
		JSONObject obj = (JSONObject) JSONSerializer.toJSON("{\"key1\": 1, \"key2\": 2, \"compound\": {\"x\": \"hoy\"}}");
        Configuration conf = new Configuration(obj);
        assertTrue(conf.get("compound") instanceof Configuration);
        assertTrue(conf.containsKey("compound.x"));
        assertFalse(conf.containsKey("compound.y"));
        Configuration down = conf.get("compound");
        assertTrue(down._config.containsKey("x"));
        assertFalse(down._config.containsKey("y"));
        
        conf.get("key1");
        
	}

	@Test(expected=Exception.class)
	public void testGetObj() throws Exception {
		JSONObject obj = (JSONObject) JSONSerializer.toJSON("{\"key1\": 1, \"key2\": true, \"compound\": {\"x\": \"hoy\"}}");
        Configuration conf = new Configuration(obj);
        assertTrue(conf.get("compound") instanceof Configuration);
        assertTrue((Boolean)conf.getObj("key2"));
        assertTrue("hoy".equals((String)conf.getObj("compound.x")));
        
        conf.getObj("bingou");
	}

	@Test
	public void testIsCompound() throws Exception {
		JSONObject obj = (JSONObject) JSONSerializer.toJSON("{\"key1\": 1, \"key2\": true, \"compound\": {\"x\": \"hoy\", \"more\":{}}, \"key3\": {}}");
		Configuration conf = new Configuration(obj);
		assertTrue(conf.isCompound("compound"));
		assertTrue(conf.isCompound("key3"));
		assertTrue(conf.isCompound("compound.more"));
		
		assertFalse(conf.isCompound("key1"));
		assertFalse(conf.isCompound("key2"));
		assertFalse(conf.isCompound("compound.x"));
	}

}
