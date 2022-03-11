package eu.trade.repo.search.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

public abstract class CustomAnalyzer extends Analyzer{
	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private final Version matchVersion;
	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	protected CustomAnalyzer(Version version) {
		super();
		matchVersion = version;
	}

	/**
	 * Set maximum allowed token length.  If a token is seen
	 * that exceeds this length then it is discarded.  This
	 * setting only takes effect the next time tokenStream or
	 * tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public Version getMatchVersion(){
		return matchVersion;
	}
}
