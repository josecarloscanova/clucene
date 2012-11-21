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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.lahab.clucene.core.BlobDirectoryFS;
import org.lahab.clucene.utils.CloudStorage;
import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.Parametizer;
import org.lahab.clucene.utils.ParametizerException;
import org.lahab.clucene.utils.Statable;

import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * A wrapper to encapsulate all communication with the index writer
 * It also keeps some stats and schedule jobs to the pool manager
 * @author charlymolter
 *
 */
public class Indexer implements Statable {
	public final static Logger LOGGER = Logger.getLogger(Indexer.class.getName());
	
	/** The index writer */
	protected IndexWriter _index;
	/** The directory that will write to */
	private Directory _directory;
	protected PoolManager _pool;
	/** How many documents have been added since the indexer is opened */
	private volatile int _nbCommit = 0;
	private int _nbLastCommit = 0;
	
	public Parametizer _params;

	private CloudStorage _cloudStorage;



	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		// Default parameters
		DEFAULTS.put("regular", false);
		DEFAULTS.put("container", "clucene");
		DEFAULTS.put("folder", "indexCache");
	}

	/**
	 * Creates an new indexer
	 * @param storage
	 * @param pool
	 * @param config
	 * @throws Exception
	 */
	public Indexer(CloudStorage storage, PoolManager pool, Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		_pool = pool;
		_cloudStorage = storage;
		_cloudStorage.addContainer("directory", _params.getString("container"));
		if (_params.getBoolean("regular")) {
			_directory = FSDirectory.open(new File(_params.getString("folder")));
		} else {
			_directory = new BlobDirectoryFS(_cloudStorage.getAccount(), _params.getString("container"), 
											 FSDirectory.open(new File(_params.getString("folder"))));
		}
	}

	/**
	 * Download the entire Index to the locale disc
	 * @param downloadDir the directory where the index will be copied to
	 * @throws Exception
	 */
	public void download(String downloadDir) throws Exception {
		final File[] files = new File(downloadDir).listFiles();
		for (File f: files) f.delete();

		for (ListBlobItem blobItem : _cloudStorage.getContainer("directory").listBlobs()) {
		    CloudBlockBlob b = _cloudStorage.getContainer("directory").getBlockBlobReference(blobItem.getUri().toString());
		    File f = new File(downloadDir + "/" + b.getName());
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStream outStream = new FileOutputStream(f);
		    b.download(outStream);
		}
	}

	/**
	 * Adds the document to the index and commits if necessary
	 * @param _doc
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParametizerException
	 */
	public void addDoc(Document _doc) throws CorruptIndexException, IOException, ParametizerException {
		_index.addDocument(_doc);
	}
	
	public void commit() throws CorruptIndexException, IOException {
		LOGGER.info("Commit Start");
		int nbDocs = _index.maxDoc();
		if (_nbLastCommit < nbDocs) {
			_nbLastCommit = nbDocs;
			_index.commit();
			_nbCommit++;
			LOGGER.info("Commit done");
			
		} else {
			LOGGER.info("Commit unecessary");
		}
	}
	
	/**
	 * Opens an index writer on the current directory
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void open() throws CorruptIndexException, IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
	    IndexWriterConfig configWriter = new IndexWriterConfig(Version.LUCENE_36, analyzer);
	    configWriter.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	    
		// Sometimes azure refuses to give us a lock the first time so we try again
		while (_index == null) {
			try {
				_index = new IndexWriter(_directory, configWriter);
				_nbLastCommit = _index.maxDoc();
			} catch (LockObtainFailedException e) {
				System.out.println("Lock is taken trying again");
				_directory.clearLock("write.lock");
			}
		}
	}

	/**
	 * Closes the current index writer
	 */
	public void close() {
		if (_index != null) {
			try {
				_index.close();
				_directory.close();
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_index = null;
		}
	}
	
	/**
	 * Tells the pool to add a job to index this document
	 * @param doc Document to be indexed
	 */
	public void queueDoc(Document doc) {
		_pool.addIndexJob(doc);
	}

	@Override
	public String[] record() {
		try {
			int nbDoc = _index == null ? 0 : _index.numDocs();
			String[] stats={String.valueOf(nbDoc), String.valueOf(_nbCommit), 
							String.valueOf(_index == null ? 0 : _index.ramSizeInBytes())};
			LOGGER.info("Doc added:" + nbDoc);
			return stats;
		} catch (IOException e){
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public String[] header() {
		String[] stats = {"nbIndex", "nbCommits", "sizeBuffer"};
		return stats;
	}

	public void delete() throws StorageException {
		_cloudStorage.getContainer("directory").delete();
	}
}
