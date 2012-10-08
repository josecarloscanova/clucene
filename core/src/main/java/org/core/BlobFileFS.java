package org.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.core.cache.Cache;


public class BlobFileFS extends BlobFile {
	private static final long serialVersionUID = 1L;
	RandomAccessFile raf;
	File file;
	
	public BlobFileFS(BlobDirectory blob, String name, Cache cache) throws IOException {
		super(blob, name, cache);
		file = new File(blob.path, name);
		if (!file.exists()) {
			file.createNewFile();
		}
		raf = new RandomAccessFile(file, "rw");
		loadInfos();
		
	}

	@Override
	protected void loadInfos() throws IOException {
		length = raf.length();
	}

	@Override
	protected int readFile(byte[] b, int offset, int size) throws IOException {
		return raf.read(b, offset, size);
	}

	@Override
	protected void write(byte[] b, int offset, int size) throws IOException {
		raf.write(b, offset, size);
		this.length += size;
	}

	@Override
	protected void close() throws IOException {
		super.close();
		raf.close();
	}

	@Override
	public void delete() throws IOException {
		if (!file.delete()) {
			throw new IOException("can't delete the file");
		}	
	}

	@Override
	public void setLength(long newLength) {
		try {
			raf.setLength(newLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		length = newLength;
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}
}
