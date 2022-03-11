package eu.trade.repo.search.analyzers;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

public class PropertiesAnalyzer extends CustomAnalyzer{

	public PropertiesAnalyzer(Version version) {
		super(version);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final KeywordTokenizer src = new KeywordTokenizer(reader);
		TokenStream tok = new StandardFilter(getMatchVersion(), src);
		tok = new ASCIIFoldingFilter(tok);
		tok = new LowerCaseFilter(getMatchVersion(), tok);
		return new TokenStreamComponents(src, tok);
	}
	

}
