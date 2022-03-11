package eu.trade.repo.index.extractor.tikaext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;


/**
 * Facade class for accessing Tika functionality.
 * 
 * Extends Tika to override the parsers configuration
 * 
 * @see RepoConfig
 * 
 *
 */
public class Repo extends Tika{

	
	 /**
     * Creates a Tika facade using the given detector and parser instances.
     *
     * @param detector type detector
     * @param parser document parser
     */
    public Repo(Detector detector, Parser parser) {
      super(detector, parser);
    }

    /**
     * Creates a Tika facade using the given configuration.
     *
     * @param config Tika configuration
     */
    public Repo(RepoConfig config) {
        this(config.getDetector(), new RepoAutodectectParser(config));
    }

    /**
     * Creates a Tika facade using the default configuration.
     */
    public Repo() {
       this(RepoConfig.getDefaultConfig());
    }

    /**
     * Creates a Tika facade using the given detector instance and the
     * default parser configuration.
     *
     * @param detector type detector
     */
    public Repo(Detector detector) {
        this(detector, new RepoAutodectectParser(RepoConfig.getDefaultConfig()));
    }
	/**
     * Parses the given document and returns the extracted text content.
     * Input metadata like a file name or a content type hint can be passed
     * in the given metadata instance. Metadata information extracted from
     * the document is returned in that same metadata instance.
     * <p>
     * The returned reader will be responsible for closing the given stream.
     * The stream and any associated resources will be closed at or before
     * the time when the {@link Reader#close()} method is called.
     *
     * @param stream the document to be parsed
     * @param metadata document metadata
     * @param context document parsing context
     * @return extracted text content
     * @throws IOException if the document can not be read or parsed
     */
    public Reader parse(InputStream stream, Metadata metadata, ParseContext context)
            throws IOException {
        
    	ParseContext parseContext = completeParseContext(context);
    	return new RepoParsingReader(getParser(), stream, metadata, parseContext);
    }
    
    @Override
    public Reader parse(InputStream stream, Metadata metadata) throws IOException {
    	return parse(stream, metadata, null);
    }
 
    private ParseContext completeParseContext(ParseContext context){
    	ParseContext parseContext = context;
    	
    	if( null == parseContext ){
    		parseContext = getDefaultParseContext();
    		
    	}else if( null == parseContext.get(Parser.class)){
    		parseContext.set(Parser.class, getParser());
    	}
    	if( null == parseContext.get(Locale.class)){
    		parseContext.set(Locale.class, Locale.ENGLISH);
    	}
    	return parseContext;
    }
    
    private ParseContext getDefaultParseContext(){
    	ParseContext context = new ParseContext();
        context.set(Parser.class, getParser());
        context.set(Locale.class, Locale.ENGLISH);
        return context;
    }
}
