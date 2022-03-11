package eu.trade.repo.search.codecs.impl;

import java.io.IOException;
import java.io.StringReader;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public abstract class CMISStringPropertyTypeNormalizer {


	public String normalizeString(String value){
		StringBuilder stb = new StringBuilder();
		try {
			TokenStream stream = createPropertiesAnalyzer().tokenStream("content", new StringReader(value));
			stream.reset();
			CharTermAttribute charTermAtt = stream.addAttribute(CharTermAttribute.class);

			while(stream.incrementToken()) {
				stb.append(charTermAtt.toString());
			}
		} catch (IOException e) {
			//LOG THIS
			throw new CmisRuntimeException("Error occurred while normalizing property value: " + value, e);
		}
		return stb.toString();
	}

	protected abstract Analyzer createPropertiesAnalyzer();

}
