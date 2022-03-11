package eu.trade.repo.search.analyzers;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class FullTextAnalyzer extends CustomAnalyzer {

	public FullTextAnalyzer(Version version) {
		super(version);
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(getMatchVersion(), reader);
		src.setMaxTokenLength(getMaxTokenLength());
		TokenStream tok = new StandardFilter(getMatchVersion(), src);
		tok = new ASCIIFoldingFilter(tok);
		tok = new LowerCaseFilter(getMatchVersion(), tok);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				src.setMaxTokenLength(FullTextAnalyzer.this.getMaxTokenLength());
				super.setReader(reader);
			}
		};
	}
}
