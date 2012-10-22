package org.lahab.clucene.indexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
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
	public static final int COMMIT_FREQUENCY = 10;
	/** The index writer */
	protected IndexWriter _index;
	/** The directory that will write */
	private Directory _directory;
	/** The queue of documents to be indexed */
	protected final BlockingQueue<Document> _queue;
	CloudBlobContainer _container;
	/** How many documents have been added since the last commit */
	private int lastCommit;
	private volatile Thread _myThread = null;
	
	/**
	 * Creates a new IndexerNode and create its thread
	 * @param storageAccount the azure storageAccount to access the directory
	 * @param containerName the name of the container in the storageAccount
	 * @param queue the blockingQueue from where we will feed the documents to be indexed
	 */
	public Indexer(CloudStorageAccount storageAccount, String containerName, BlockingQueue<Document> queue) throws Exception {
		_queue = queue;
		CloudBlobClient client = storageAccount.createCloudBlobClient();
		_container = client.getContainerReference(containerName);
	    _directory = new BlobDirectoryFS(storageAccount, containerName, new RAMDirectory());

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
		_myThread = new Thread(this);
	}
	
	/**
	 * Download the entire Index to the locale disc
	 * @param downloadDir the directory where the index will be copied to
	 * @throws Exception
	 */
	public void download(String downloadDir) throws Exception {
		final File[] files = new File(downloadDir).listFiles();
		for (File f: files) f.delete();

		for (ListBlobItem blobItem : _container.listBlobs()) {
		    CloudBlockBlob b = _container.getBlockBlobReference(blobItem.getUri().toString());
		    File f = new File("../index/cloud/"+ b.getName());
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStream outStream = new FileOutputStream(f);
		    b.download(outStream);
		}
	}
	
	/**
	 * Add a document to the index and commits if necessary
	 * @param doc the document to be indexed
	 * @throws IOException 
	 */
	protected void addDoc(Document doc) throws IOException {
	    _index.addDocument(doc);
	    lastCommit++;
	    if (lastCommit == COMMIT_FREQUENCY) {
	    	_index.commit();
	    	lastCommit = 0;
	    }
	}

	@Override
	public void run() {
		LOGGER.info("indexer start");
		Thread thisThread = Thread.currentThread();
		while (_myThread == thisThread) {
			try {
				LOGGER.info("indexer consume");
				Document doc = _queue.take();
				LOGGER.info("indexing: " + doc.get("URI"));
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
		_myThread.start();
	}
	
	public void stop() throws IOException {
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
		_myThread = null;
	}
}
