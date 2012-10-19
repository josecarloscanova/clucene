package org.lahab.clucene.core;

/*
 * #%L
 * core
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.store.IndexInput;

public class StreamInput extends InputStream {
	public IndexInput input;
	
	public StreamInput(IndexInput openInput) {
		input = openInput;
	}

	@Override
	public int read() throws IOException {
		if (input.getFilePointer() >= input.length()) {
			return -1;
		}
		int b = (int) input.readByte() & 0xff;
		return b;
	}

}