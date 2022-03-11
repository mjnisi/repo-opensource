package eu.trade.repo.query;

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public abstract class Lexer extends org.antlr.runtime.Lexer {

	protected List<String> errors = new LinkedList<String>();

	public Lexer() {
		super();
	}

	public Lexer(CharStream input, RecognizerSharedState state) {
		super(input, state);
	}

	public Lexer(CharStream input) {
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
}
