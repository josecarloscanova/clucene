package org.lahab.clucene.siteConstructor;

/*
 * #%L
 * siteConstructor
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
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public class DownloadJob implements Runnable {

	public static String DIRECTORY;
	protected CloudBlob _blob;

	public DownloadJob(CloudBlob blobItem) {
		_blob = blobItem;
	}

	@Override
	public void run() {
        try {
        	File f = new File(DIRECTORY + _blob.getName() + ".html");
        	if (f.exists()) {
        		f.delete();
        	}
        	OutputStream os = new FileOutputStream(f);
			_blob.download(os);
			os.close();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}

}
