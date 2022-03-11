package eu.trade.repo.query;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

public abstract class Parser extends org.antlr.runtime.Parser {

	protected List<String> errors = new LinkedList<String>();
	protected Set<String> tables = new HashSet<String>();
	protected Set<String> columns = new HashSet<String>();

	public Parser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	public Parser(TokenStream input) {
		super(input);
	}

	public void displayRecognitionError(String[] tokenNames,
			RecognitionException e) {
		String hdr = getErrorHeader(e);
		String msg = getErrorMessage(e, tokenNames);
		errors.add(hdr + " " + msg);
	}

	public List<String> getErrors() {
		return errors;
	}

	public Set<String> getTables() {
		return tables;
	}

	public Set<String> getColumns() {
		return columns;
	}
}
