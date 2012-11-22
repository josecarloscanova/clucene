package org.lahab.clucene.server.indexer;

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
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


import com.microsoft.windowsazure.services.blob.client.CloudBlob;


/**
 * This a factory Runnable objects to be used with the pool Manager
 * @author charlymolter
 *
 */
public class PoolJobs {
	public final static Logger LOGGER = Logger.getLogger(PoolJobs.class.getName());
	public static Indexer INDEX;
	
	/** 
	 * A job to index a document
	 * @author charlymolter
	 *
	 */
	class DocumentIndexer implements Runnable {
		Document _doc;
		public DocumentIndexer(Document doc) {
			_doc = doc;
		}
		public void run() {
			try {
				INDEX.addDoc(_doc);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * A job to parse and create a document
	 * @author charlymolter
	 *
	 */
	class DocumentParser implements Runnable {
		CloudBlob _blob = null;
		File _file = null;
		
		public DocumentParser(CloudBlob blobItem) {
			_blob = blobItem;
		}
		
		public DocumentParser(File blobItem) {
			_file = blobItem;
		}
		
		public void run() {
			try {
				String name = _file.getName();				
				BufferedReader br = new BufferedReader(new FileReader(_file));
				
				Document doc = new Document();

	        	doc.add(new Field("title", br.readLine(),
	    						  Field.Store.YES, Field.Index.ANALYZED));
	        	String content = "";
	        	String line = null;
	        	while ((line = br.readLine()) != null) {
	        		content += line;
	        	}
	        	doc.add(new Field("content", content, 
				  		  Field.Store.NO, Field.Index.ANALYZED));
	        	br.close();
	    		doc.add(new Field("URI", name, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    		
	    		INDEX.queueDoc(doc);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public Runnable NEW_documentParser(Object blobItem) {
		if (blobItem instanceof File) {
			return new DocumentParser((File) blobItem);
		} else {
			return new DocumentParser((CloudBlob) blobItem);
		}
	}

	public Runnable NEW_documentIndexer(Document doc) {
		// TODO Auto-generated method stub
		return new DocumentIndexer(doc);
	}
}