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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A simple class to save stats in a file
 * @author charlymolter
 *
 */
public class Stats {
	protected long _start;
	protected int _indice = 0;
	protected int _nb = 1;
	protected String _fileName;
	protected Writer _out;
	
	public Stats (String path, int sensibility){
		_fileName = path;
		File f = null;
	    try {
	      f = new File(_fileName);
	      if (f.exists()) {
	    	  f.delete();
	      } 
	      f.createNewFile();
		  _out = new BufferedWriter(new FileWriter(f));
	    } catch (IOException ex) {
	      System.out.println("Error creating file");
	    }
	}
	
	public void startTimer() {
		_start = System.currentTimeMillis();
	}
	
	public void record(String value) throws IOException {
		_out.write(_indice + ";" + value + "\n");
	}
	
	public void close() throws IOException {
		_out.close();
	}
}
