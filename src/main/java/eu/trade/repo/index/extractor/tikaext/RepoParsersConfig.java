package eu.trade.repo.index.extractor.tikaext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tika.config.LoadErrorHandler;
import org.apache.tika.parser.Parser;


/**
 * Reads the available parsers from a file named eu.trade.repo.index.extractor.tikaext.RepoParsersConfig
 *
 */
public class RepoParsersConfig {


	private static final Pattern COMMENT = Pattern.compile("#.*");
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private final ClassLoader loader;
	private final LoadErrorHandler handler;
	private Parser[] parsers;
	private final String parsersFileName;

	public RepoParsersConfig(){
		loader = getContextClassLoader();
		handler = LoadErrorHandler.IGNORE;
		parsersFileName = RepoParsersConfig.class.getName();
	}

	public RepoParsersConfig(String parsersFileName){
		loader = getContextClassLoader();
		handler = LoadErrorHandler.IGNORE;
		this.parsersFileName = parsersFileName;
	}


	public Parser[] obtainParsers(){
		if( null == parsers ){
			List<Parser> parserList = getDefaultParsers();

			Parser[] parsersTmp = new Parser[parserList.size()];
			parsers = parserList.toArray(parsersTmp);
		}
		return parsers;
	}



	private List<Parser> getDefaultParsers() {
		List<Parser> parserList = loadRepoParsers();
		Collections.sort(parserList, new Comparator<Parser>() {
			public int compare(Parser p1, Parser p2) {
				String n1 = p1.getClass().getName();
				String n2 = p2.getClass().getName();
				boolean t1 = n1.startsWith("org.apache.tika.");
				boolean t2 = n2.startsWith("org.apache.tika.");
				if (t1 == t2) {
					return n1.compareTo(n2);
				} else if (t1) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		return parserList;
	}

	public List<Parser> loadRepoParsers() {
		List<Parser> providers = new ArrayList<Parser>();

		if (loader != null) {
			List<String> names = new ArrayList<String>();

			//service name = package.class = file with the allowed parsers list

			Enumeration<URL> resources = findServiceResources(parsersFileName);
			for (URL resource : Collections.list(resources)) {
				try {
					collectServiceClassNames(resource, names);
				} catch (IOException e) {
					handler.handleLoadError(parsersFileName, e);
				}
			}

			for (String name : names) {
				try {
					Class<?> klass = loader.loadClass(name);
					if (Parser.class.isAssignableFrom(klass)) {
						providers.add((Parser) klass.newInstance());
					}
				} catch (Exception t) {
					handler.handleLoadError(name, t);
				}
			}
		}

		return providers;
	}

	/**
	 * Returns all the available service resources matching the
	 *  given pattern, such as all instances of tika-mimetypes.xml 
	 *  on the classpath, or all org.apache.tika.parser.Parser 
	 *  service files.
	 */
	public Enumeration<URL> findServiceResources(String filePattern) {
		Enumeration<URL> resources = null;
		try {
			resources = loader.getResources(filePattern);
		} catch (IOException ignore) {
			// We couldn't get the list of service resource files
			List<URL> empty = Collections.emptyList();
			resources = Collections.enumeration( empty );
		}
		return resources;
	}

	private void collectServiceClassNames(URL resource, Collection<String> names) throws IOException {
		BufferedReader reader = null;
		InputStream stream = null;
		try {
			stream = resource.openStream();
			reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			String line = reader.readLine();
			while (line != null) {
				line = COMMENT.matcher(line).replaceFirst("");
				line = WHITESPACE.matcher(line).replaceAll("");
				if (line.length() > 0) {
					names.add(line);
				}
				line = reader.readLine();
			}
		} finally {
			if( null != reader ){
				reader.close();
			}else if( null != stream ){
				stream.close();
			}
		}
	}

	private static ClassLoader getContextClassLoader() {
		ClassLoader loader = RepoParsersConfig.class.getClassLoader();
		if (null == loader) {
			loader = ClassLoader.getSystemClassLoader();
		}
		return loader;
	}
}
