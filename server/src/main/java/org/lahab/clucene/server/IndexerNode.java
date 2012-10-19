package org.lahab.clucene.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.codehaus.jackson.JsonParser;
import org.lahab.clucene.core.BlobDirectoryFS;
import org.mortbay.jetty.Server;
import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;
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

public class IndexerNode {
	protected IndexWriter _index;
	protected Server _server;
	protected IndexerHandler _handler;
	public static CloudStorageAccount storageAccount;
	/*public static String contName = "lucene1";
	public static String storageConnectionString = "";*/
	public static String configFile = "../config.json";
	public static JSONObject config;
	
	
	public static void main(String[] args) throws Exception {
		// Retrieve storage account from connection-string
		if (config == null) {
			parseConfig();
			JSONObject azureConf = config.getJSONObject("azure");
			storageAccount = CloudStorageAccount.parse("DefaultEndpointsProtocol="+ 
													   azureConf.getString("DefaultEndpointsProtocol") +
													   ";AccountName=" +
													   azureConf.getString("AccountName") +
													   ";AccountKey=" +
													   azureConf.getString("AccountKey") + ";");
		}

	    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
	    Directory index = new BlobDirectoryFS(storageAccount, config.getString("container"), new RAMDirectory());//, null
	    Server server = new Server(9999);
	    IndexerHandler handler = new IndexerHandler();
		NEW_IndexerNode(analyzer, index, server, handler);
		server.start();
		server.join();
		

	}
	
	public void download() throws URISyntaxException, StorageException, IOException {
		final File[] files = new File(config.getString("downloadDir")).listFiles();
		for (File f: files) f.delete();

		CloudBlobClient client = storageAccount.createCloudBlobClient();
		CloudBlobContainer container = client.getContainerReference(config.getString("container"));
		for (ListBlobItem blobItem : container.listBlobs()) {
		    CloudBlockBlob b = container.getBlockBlobReference(blobItem.getUri().toString());
		    File f = new File("../index/cloud/"+ b.getName());
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStream outStream = new FileOutputStream(f);
		    b.download(outStream);
		}	
	}
	
	/**
	 * Creates a new IndexerNode
	 * @param analyzer which analyzer should be used in the indexing phase
	 * @param dir the directory to use
	 * @param com the communication layer to communicate with the rest of the cluster
	 * @return a newly created IndexerNode
	 * @throws Exception 
	 */
	static public IndexerNode NEW_IndexerNode(Analyzer analyzer, Directory dir, 
											  Server server, IndexerHandler handler) throws Exception {		
	    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
	    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		IndexWriter w = null;
		while (w == null) {
			try {
				w = new IndexWriter(dir, config);
			} catch (LockObtainFailedException e) {
				System.out.println("Lock is taken trying again");
				dir.clearLock("write.lock");
			}
		}
		w.setInfoStream(System.out);
	    IndexerNode node = new IndexerNode(w, server, handler);
	    server.addHandler(handler);
	    handler.setNode(node);
	    
		return node;		
	}
	
	/** 
	 * Create a node with IOC to be really flexible
	 * @param index the used IndexWriter 
	 * @param server the existing server
	 * @throws Exception 
	 */
	public IndexerNode(IndexWriter index, Server server, IndexerHandler handler) throws Exception {
		_index = index;
		_server = server;
		_handler = handler;
	}
	
	/**
	 * Insert a list of documents in the index
	 * @param docs a collection of documents
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void addDocuments(Collection<Document> docs) throws CorruptIndexException, IOException {
		for (Document doc: docs) {
			addDoc(doc);
		}
		_index.commit();
	}
	
	
	public static void parseConfig() throws IOException {
        InputStream is = new FileInputStream(new File(configFile));
        String jsonTxt = IOUtils.toString(is);
        config = (JSONObject) JSONSerializer.toJSON(jsonTxt);
	}
	
	protected void addDoc(Document doc) throws IOException {
	    _index.addDocument(doc);
	}
}