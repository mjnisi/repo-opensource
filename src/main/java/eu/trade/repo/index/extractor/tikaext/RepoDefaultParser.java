package eu.trade.repo.index.extractor.tikaext;

import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.CompositeParser;

public class RepoDefaultParser extends CompositeParser{

	private static final long serialVersionUID = 1L;

	public RepoDefaultParser(MediaTypeRegistry registry, RepoParsersConfig parsersConfig){
		super(registry, parsersConfig.loadRepoParsers());
	}

}
