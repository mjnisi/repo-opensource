package eu.trade.repo.index.extractor.tikaext;

import java.io.IOException;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.tika.config.ServiceLoader;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;



/**
 * 
 * Extends TikaConfig to override the parsers configuration
 * 
 * @see RepoParsersConfig
 * 
 */
public class RepoConfig extends TikaConfig{


	private final CompositeParser parser;
	private final Detector detector;
	private final MimeTypes mimeTypes;



	public RepoConfig() throws TikaException, IOException {
		this( new RepoParsersConfig() );
	}
	/**
	 * Creates a default Tika configuration.
	 * Initializes from the built-in media
	 * type rules and all the {@link Parser} implementations available through
	 * the ServiceRegistry service provider mechanism in the context
	 * class loader of the current thread.</p>
	 *
	 * @throws IOException if the configuration can not be read
	 * @throws TikaException if problem with MimeTypes or parsing XML config
	 */
	public RepoConfig(RepoParsersConfig parsersConfig) throws TikaException, IOException {

		ServiceLoader loader = new ServiceLoader();

		this.mimeTypes = getDefaultMimeTypes();
		this.parser = getDefaultParser(mimeTypes, parsersConfig);
		this.detector = getDefaultDetector(mimeTypes, loader);


	}



	/**
	 * @deprecated Use the {@link #getParser()} method instead
	 */
	public Parser getParser(MediaType mimeType) {
		return parser.getParsers().get(mimeType);
	}

	/**
	 * Returns the configured parser instance.
	 *
	 * @return configured parser
	 */
	public Parser getParser() {
		return parser;
	}

	/**
	 * Returns the configured detector instance.
	 *
	 * @return configured detector
	 */
	public Detector getDetector() {
		return detector;
	}

	public MimeTypes getMimeRepository(){
		return mimeTypes;
	}

	public MediaTypeRegistry getMediaTypeRegistry() {
		return mimeTypes.getMediaTypeRegistry();
	}

	/**
	 * Provides a default configuration (TikaConfig).  Currently creates a
	 * new instance each time it's called; we may be able to have it
	 * return a shared instance once it is completely immutable.
	 *
	 * @return default configuration
	 */
	public static RepoConfig getDefaultConfig() {
		try {
			RepoParsersConfig parsersConfig = new RepoParsersConfig(RepoParsersConfig.class.getName());
			return new RepoConfig(parsersConfig);
		} catch (IOException e) {
			throw new CmisRuntimeException(
					"Unable to read default configuration", e);
		} catch (TikaException e) {
			throw new CmisRuntimeException(
					"Unable to access default configuration", e);
		}
	}


	private static MimeTypes getDefaultMimeTypes() {
		return MimeTypes.getDefaultMimeTypes();
	}

	private static Detector getDefaultDetector( MimeTypes types, ServiceLoader loader) {
		return new DefaultDetector(types, loader);
	}

	private static CompositeParser getDefaultParser(  MimeTypes types, RepoParsersConfig parsersConfig ) {
		return new RepoDefaultParser(types.getMediaTypeRegistry(), parsersConfig);
	}



}
