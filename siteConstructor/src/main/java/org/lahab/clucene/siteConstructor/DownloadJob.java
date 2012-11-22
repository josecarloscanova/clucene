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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

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
			File f = new File(DIRECTORY + _blob.getName() + ".txt");
        	if (f.exists()) {
        		f.delete();
        	}
        	File ftmp = new File(new Random().nextInt() + "f");
        	FileOutputStream os = new FileOutputStream(ftmp);
        	
        	System.out.println("downloading" + _blob.getName());
			_blob.download(os);
			os.close();
			FileInputStream is = new FileInputStream(ftmp);
			Source source=new Source(is);
			source.fullSequentialParse();

			Element titleContent = source.getFirstElement(HTMLElementName.TITLE);
			Element bodyContent = source.getElementById("content");
			TextExtractor body = new TextExtractor(bodyContent.getContent());
			TextExtractor title = new TextExtractor(titleContent.getContent());
			is.close();
			ftmp.delete(); 
			System.out.println(title.toString());
        	FileWriter writer = new FileWriter(f);
        	writer.write(title + "\n");
        	writer.write(body + "\n");
        	writer.close();
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
