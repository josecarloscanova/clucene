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
 * This creates a new thread that will sleep for _frequency milliseconds and wake 
 * to call the record method on a list of Statable objects
 * @author charlymolter
 *
 */
public class StatRecorder implements Runnable {
	public int _frequency;
	protected Thread _thread;
	protected Statable[] _statable;
	protected Writer _out;
	protected String _fileName;
	
	public StatRecorder(String path, int freq, Statable[] statable) {
		_frequency = freq;
		_statable = statable;
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
		_thread = new Thread(this);
	}
	
	@Override
	public void run() {
		write("time");
		for (Statable stat: _statable) {
			write(stat.header());
		}
		endRecord();
		Thread thisThread = Thread.currentThread();
		while (thisThread == _thread) {	
			try {
				write(String.valueOf(System.currentTimeMillis()));
				for (Statable stat: _statable) {
					write(stat.record());
				}
				endRecord();
				Thread.sleep(_frequency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Does what you have to do at the end of one list of stats
	 * by default it writes a end of line
	 */
	protected void endRecord() {
		try {
			_out.write("\n");
		} catch (IOException e) {
			System.err.println("Can't write to stat file");
		}
	}

	/**
	 * Write an element with its separator
	 * by default ";" for csv
	 * @param elt
	 */
	protected void write(String elt) {
		try {
			_out.write(elt + ";");
		} catch (IOException e) {
			System.err.println("Can't write to stat file");
		}
	}

	/**
	 * Write to the file the new data
	 * @param record
	 * @throws IOException
	 */
	protected void write(String[] record) {
		if (record == null) {
			return;
		}
		for (String elt : record) {
			write(elt);
		}	
	}

	public void start() {
		_thread.start();
	}
	
	public void stop() {
		try {
			_out.close();
		} catch (IOException e) {
			System.err.println("Can't close the file");
		}
		_thread = null;
	}
}
