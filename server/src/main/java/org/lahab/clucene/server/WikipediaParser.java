package org.lahab.clucene.server;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class WikipediaParser {
	public final static Logger LOGGER = Logger.getLogger(WikipediaParser.class.getName());
	// These are the meta we'll analyse
	static Set<String> textMetadataFields = new HashSet<String>();
	
	static {
		textMetadataFields.add(DublinCore.TITLE.getName());
		
		
	}
	
	public Document parse(InputStream is, String url) throws Exception {
		LOGGER.finer("parsing URI:" + url);
		Metadata meta = new Metadata();
		meta.set(Metadata.RESOURCE_NAME_KEY, url);
		Parser parser = new HtmlParser();
		ContentHandler handler = new BodyContentHandler();
		
		try {
			parser.parse(is, handler, meta, new ParseContext());
		} finally {
			is.close();
		}
		
		Document doc = new Document();
		
		String extractedText = handler.toString();
		doc.add(new Field("contents", extractedText, 
						  Field.Store.NO, Field.Index.ANALYZED));
		LOGGER.finest("Text extracted by Tika:" + extractedText);
		
		for (String name: meta.names()) {
			if (textMetadataFields.contains(name)) {
				String value = meta.get(name);
				LOGGER.finer("Adding the meta: " + name + ":" + value);
				doc.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED));
			}
		}
		
		doc.add(new Field("URI", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		
		
		return doc;
		
	}
	
	private void listAvailableMetaDataFields(final Metadata metadata) {
		System.out.println("Existing metadatas:");
		for(int i = 0; i <metadata.names().length; i++) {
			String name = metadata.names()[i];
			System.out.println(name + " : " + metadata.get(name));
		}
	}
}
