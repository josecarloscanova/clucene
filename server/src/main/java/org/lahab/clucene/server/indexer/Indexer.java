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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.lahab.clucene.core.BlobDirectoryFS;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

/**
 * A thread that will index the documents given in the queue to a AzureBlobStorage
 * @author charlymolter
 *
 */
public class Indexer implements Runnable {
	public final static Logger LOGGER = Logger.getLogger(Indexer.class.getName());
	
	/** How often the indexWriter will commit the new documents.*/
	public static int COMMIT_FREQUENCY = 10;
	
	public static int NB_THREAD = 3;
	/** The index writer */
	protected IndexWriter _index;
	/** The directory that will write */
	private Directory _directory;
	/** The queue of documents to be indexed */
	protected BlockingQueue<Document> _queue;
	protected CloudBlobContainer _container;
	/** How many documents have been added since the last commit */
	private volatile static int numberAdded = 0;
	private volatile static long lastCommit = System.currentTimeMillis();
	
	protected ExecutorService _pool;

	private Thread _thread;
	
	protected class AddDocumentJob implements Runnable {
		Document _doc;
		public AddDocumentJob(Document doc) {
			_doc = doc;
		}
		public void run() {
			try {
				_index.addDocument(_doc);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean IS_REGULAR = false;

	public void init(CloudStorageAccount storageAccount, String containerName, BlockingQueue<Document> queue, String dirFolder) throws Exception {
		_queue = queue;
		CloudBlobClient client = storageAccount.createCloudBlobClient();
		_container = client.getContainerReference(containerName);
		if (IS_REGULAR) {
			_directory = FSDirectory.open(new File(dirFolder));
		} else {
			_directory = new BlobDirectoryFS(storageAccount, containerName, FSDirectory.open(new File(dirFolder)));
		}
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
	    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
	    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	    
		_index = null;
		// Sometimes azure refuses to give us a lock the first time so we try again
		while (_index == null) {
			try {
				_index = new IndexWriter(_directory, config);
			} catch (LockObtainFailedException e) {
				System.out.println("Lock is taken trying again");
				_directory.clearLock("write.lock");
			}
		}
		startPool();
		_thread = new Thread(this);
	}
	
	/**
	 * Download the entire Index to the locale disc
	 * @param downloadDir the directory where the index will be copied to
	 * @throws Exception
	 */
	public void download(String downloadDir) throws Exception {
		finish();
		final File[] files = new File(downloadDir).listFiles();
		for (File f: files) f.delete();

		for (ListBlobItem blobItem : _container.listBlobs()) {
		    CloudBlockBlob b = _container.getBlockBlobReference(blobItem.getUri().toString());
		    File f = new File(downloadDir + "/" + b.getName());
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStream outStream = new FileOutputStream(f);
		    b.download(outStream);
		}
		startPool();
	}
	
	protected void startPool() {
		_pool = new ThreadPoolExecutor(NB_THREAD, NB_THREAD, 0, TimeUnit.SECONDS,
										new ArrayBlockingQueue<Runnable>(NB_THREAD * 4 + 1, false),
										new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * Add a document to the index and commits if necessary
	 * @param doc the document to be indexed
	 * @throws IOException 
	 */
	protected void addDoc(Document doc) throws IOException {
	    _pool.execute(new AddDocumentJob(doc));
	    numberAdded++;
	    if (numberAdded % COMMIT_FREQUENCY == 0) {
	    	finish();
	    	_index.commit();
	    	long commitTime = System.currentTimeMillis();
	    	System.out.println(commitTime - lastCommit);
	    	lastCommit = commitTime;
	    	startPool();
	    }
	    if (numberAdded % 100 == 0) {
	    	LOGGER.info(numberAdded + " Document indexed");
	    }
	}

	@Override
	public void run() {
		LOGGER.info("indexer start");
		Thread thisThread = Thread.currentThread();
		while (thisThread == _thread) {
			try {
				System.out.println("taking" + _queue.size());
				Document doc = _queue.take();
				LOGGER.fine("indexing: " + doc.get("URI"));
				this.addDoc(doc);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void start() {
		_thread.start();
	}
	
	public void stop() throws IOException {
		_thread = null;
		//TODO make the indexWriter shutdown nicely
		try {
			_index.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (IndexWriter.isLocked(_directory)) {
				IndexWriter.unlock(_directory);
			}
		}

	}
	
	protected void finish() {
		_pool.shutdown();
		while (true) {
			try {
				if (_pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
	}
}
