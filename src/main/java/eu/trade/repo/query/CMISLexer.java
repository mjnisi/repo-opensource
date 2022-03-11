// $ANTLR 3.5 C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g 2013-12-17 16:54:52

package eu.trade.repo.query;
import eu.trade.repo.query.ast.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CMISLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__46=46;
	public static final int AND=4;
	public static final int ANY=5;
	public static final int AS=6;
	public static final int ASC=7;
	public static final int BINARY_OPERATOR=8;
	public static final int BOOLEAN=9;
	public static final int COLUMN=10;
	public static final int COMMA=11;
	public static final int CONST=12;
	public static final int CONTAINS=13;
	public static final int DESC=14;
	public static final int DOT=15;
	public static final int EQUALS=16;
	public static final int ESC_SEQ=17;
	public static final int FILTER=18;
	public static final int FROM=19;
	public static final int HEX_DIGIT=20;
	public static final int IN=21;
	public static final int INTEGER=22;
	public static final int IN_FOLDER=23;
	public static final int IN_TREE=24;
	public static final int IS=25;
	public static final int LIKE=26;
	public static final int LPAREN=27;
	public static final int NAME=28;
	public static final int NOT=29;
	public static final int NULL=30;
	public static final int OCTAL_ESC=31;
	public static final int OR=32;
	public static final int ORDER=33;
	public static final int ORDER_BY=34;
	public static final int QUOTE=35;
	public static final int RPAREN=36;
	public static final int SCORE=37;
	public static final int SELECT=38;
	public static final int STAR=39;
	public static final int STRING=40;
	public static final int TABLE=41;
	public static final int TIMESTAMP=42;
	public static final int UNICODE_ESC=43;
	public static final int WHERE=44;
	public static final int WS=45;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public CMISLexer() {} 
	public CMISLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public CMISLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g"; }

	// $ANTLR start "T__46"
	public final void mT__46() throws RecognitionException {
		try {
			int _type = T__46;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:12:7: ( 'SEARCH_SCORE' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:12:9: 'SEARCH_SCORE'
			{
			match("SEARCH_SCORE"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__46"

	// $ANTLR start "SELECT"
	public final void mSELECT() throws RecognitionException {
		try {
			int _type = SELECT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:96:7: ( 'SELECT' | 'select' )
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0=='S') ) {
				alt1=1;
			}
			else if ( (LA1_0=='s') ) {
				alt1=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}

			switch (alt1) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:96:9: 'SELECT'
					{
					match("SELECT"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:96:18: 'select'
					{
					match("select"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SELECT"

	// $ANTLR start "FROM"
	public final void mFROM() throws RecognitionException {
		try {
			int _type = FROM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:97:5: ( 'FROM' | 'from' )
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0=='F') ) {
				alt2=1;
			}
			else if ( (LA2_0=='f') ) {
				alt2=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:97:7: 'FROM'
					{
					match("FROM"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:97:14: 'from'
					{
					match("from"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FROM"

	// $ANTLR start "WHERE"
	public final void mWHERE() throws RecognitionException {
		try {
			int _type = WHERE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:98:6: ( 'WHERE' | 'where' )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0=='W') ) {
				alt3=1;
			}
			else if ( (LA3_0=='w') ) {
				alt3=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:98:8: 'WHERE'
					{
					match("WHERE"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:98:16: 'where'
					{
					match("where"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WHERE"

	// $ANTLR start "ORDER_BY"
	public final void mORDER_BY() throws RecognitionException {
		try {
			int _type = ORDER_BY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:9: ( 'ORDER' ( WS )+ 'BY' | 'order' ( WS )+ 'by' )
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0=='O') ) {
				alt6=1;
			}
			else if ( (LA6_0=='o') ) {
				alt6=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}

			switch (alt6) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:11: 'ORDER' ( WS )+ 'BY'
					{
					match("ORDER"); 

					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:19: ( WS )+
					int cnt4=0;
					loop4:
					while (true) {
						int alt4=2;
						int LA4_0 = input.LA(1);
						if ( ((LA4_0 >= '\t' && LA4_0 <= '\n')||LA4_0=='\r'||LA4_0==' ') ) {
							alt4=1;
						}

						switch (alt4) {
						case 1 :
							// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:19: WS
							{
							mWS(); 

							}
							break;

						default :
							if ( cnt4 >= 1 ) break loop4;
							EarlyExitException eee = new EarlyExitException(4, input);
							throw eee;
						}
						cnt4++;
					}

					match("BY"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:28: 'order' ( WS )+ 'by'
					{
					match("order"); 

					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:36: ( WS )+
					int cnt5=0;
					loop5:
					while (true) {
						int alt5=2;
						int LA5_0 = input.LA(1);
						if ( ((LA5_0 >= '\t' && LA5_0 <= '\n')||LA5_0=='\r'||LA5_0==' ') ) {
							alt5=1;
						}

						switch (alt5) {
						case 1 :
							// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:99:36: WS
							{
							mWS(); 

							}
							break;

						default :
							if ( cnt5 >= 1 ) break loop5;
							EarlyExitException eee = new EarlyExitException(5, input);
							throw eee;
						}
						cnt5++;
					}

					match("by"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ORDER_BY"

	// $ANTLR start "ASC"
	public final void mASC() throws RecognitionException {
		try {
			int _type = ASC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:100:4: ( 'ASC' | 'asc' )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0=='A') ) {
				alt7=1;
			}
			else if ( (LA7_0=='a') ) {
				alt7=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:100:6: 'ASC'
					{
					match("ASC"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:100:12: 'asc'
					{
					match("asc"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ASC"

	// $ANTLR start "DESC"
	public final void mDESC() throws RecognitionException {
		try {
			int _type = DESC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:101:5: ( 'DESC' | 'desc' )
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0=='D') ) {
				alt8=1;
			}
			else if ( (LA8_0=='d') ) {
				alt8=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:101:7: 'DESC'
					{
					match("DESC"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:101:14: 'desc'
					{
					match("desc"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DESC"

	// $ANTLR start "AND"
	public final void mAND() throws RecognitionException {
		try {
			int _type = AND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:102:4: ( 'AND' | 'and' )
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0=='A') ) {
				alt9=1;
			}
			else if ( (LA9_0=='a') ) {
				alt9=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:102:6: 'AND'
					{
					match("AND"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:102:12: 'and'
					{
					match("and"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AND"

	// $ANTLR start "OR"
	public final void mOR() throws RecognitionException {
		try {
			int _type = OR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:103:3: ( 'OR' | 'or' )
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0=='O') ) {
				alt10=1;
			}
			else if ( (LA10_0=='o') ) {
				alt10=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:103:5: 'OR'
					{
					match("OR"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:103:10: 'or'
					{
					match("or"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OR"

	// $ANTLR start "NOT"
	public final void mNOT() throws RecognitionException {
		try {
			int _type = NOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:104:4: ( 'NOT' | 'not' )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0=='N') ) {
				alt11=1;
			}
			else if ( (LA11_0=='n') ) {
				alt11=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:104:6: 'NOT'
					{
					match("NOT"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:104:12: 'not'
					{
					match("not"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NOT"

	// $ANTLR start "IN"
	public final void mIN() throws RecognitionException {
		try {
			int _type = IN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:105:3: ( 'IN' | 'in' )
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0=='I') ) {
				alt12=1;
			}
			else if ( (LA12_0=='i') ) {
				alt12=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}

			switch (alt12) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:105:5: 'IN'
					{
					match("IN"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:105:10: 'in'
					{
					match("in"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IN"

	// $ANTLR start "IS"
	public final void mIS() throws RecognitionException {
		try {
			int _type = IS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:106:3: ( 'IS' | 'is' )
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0=='I') ) {
				alt13=1;
			}
			else if ( (LA13_0=='i') ) {
				alt13=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:106:5: 'IS'
					{
					match("IS"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:106:10: 'is'
					{
					match("is"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IS"

	// $ANTLR start "NULL"
	public final void mNULL() throws RecognitionException {
		try {
			int _type = NULL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:107:5: ( 'NULL' | 'null' )
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0=='N') ) {
				alt14=1;
			}
			else if ( (LA14_0=='n') ) {
				alt14=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}

			switch (alt14) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:107:7: 'NULL'
					{
					match("NULL"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:107:14: 'null'
					{
					match("null"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NULL"

	// $ANTLR start "EQUALS"
	public final void mEQUALS() throws RecognitionException {
		try {
			int _type = EQUALS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:108:7: ( '=' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:108:9: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EQUALS"

	// $ANTLR start "ANY"
	public final void mANY() throws RecognitionException {
		try {
			int _type = ANY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:109:4: ( 'ANY' | 'any' )
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0=='A') ) {
				alt15=1;
			}
			else if ( (LA15_0=='a') ) {
				alt15=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:109:6: 'ANY'
					{
					match("ANY"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:109:12: 'any'
					{
					match("any"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ANY"

	// $ANTLR start "LIKE"
	public final void mLIKE() throws RecognitionException {
		try {
			int _type = LIKE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:110:5: ( 'LIKE' | 'like' )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0=='L') ) {
				alt16=1;
			}
			else if ( (LA16_0=='l') ) {
				alt16=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:110:7: 'LIKE'
					{
					match("LIKE"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:110:14: 'like'
					{
					match("like"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LIKE"

	// $ANTLR start "AS"
	public final void mAS() throws RecognitionException {
		try {
			int _type = AS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:111:3: ( 'AS' | 'as' )
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0=='A') ) {
				alt17=1;
			}
			else if ( (LA17_0=='a') ) {
				alt17=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}

			switch (alt17) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:111:5: 'AS'
					{
					match("AS"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:111:10: 'as'
					{
					match("as"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AS"

	// $ANTLR start "CONTAINS"
	public final void mCONTAINS() throws RecognitionException {
		try {
			int _type = CONTAINS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:112:9: ( 'CONTAINS' | 'contains' )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0=='C') ) {
				alt18=1;
			}
			else if ( (LA18_0=='c') ) {
				alt18=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:112:11: 'CONTAINS'
					{
					match("CONTAINS"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:112:22: 'contains'
					{
					match("contains"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONTAINS"

	// $ANTLR start "IN_FOLDER"
	public final void mIN_FOLDER() throws RecognitionException {
		try {
			int _type = IN_FOLDER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:113:10: ( 'IN_FOLDER' | 'in_folder' )
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0=='I') ) {
				alt19=1;
			}
			else if ( (LA19_0=='i') ) {
				alt19=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}

			switch (alt19) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:113:12: 'IN_FOLDER'
					{
					match("IN_FOLDER"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:113:24: 'in_folder'
					{
					match("in_folder"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IN_FOLDER"

	// $ANTLR start "IN_TREE"
	public final void mIN_TREE() throws RecognitionException {
		try {
			int _type = IN_TREE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:114:8: ( 'IN_TREE' | 'in_tree' )
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0=='I') ) {
				alt20=1;
			}
			else if ( (LA20_0=='i') ) {
				alt20=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}

			switch (alt20) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:114:10: 'IN_TREE'
					{
					match("IN_TREE"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:114:20: 'in_tree'
					{
					match("in_tree"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IN_TREE"

	// $ANTLR start "SCORE"
	public final void mSCORE() throws RecognitionException {
		try {
			int _type = SCORE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:115:6: ( 'SCORE()' | 'score()' )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0=='S') ) {
				alt21=1;
			}
			else if ( (LA21_0=='s') ) {
				alt21=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:115:8: 'SCORE()'
					{
					match("SCORE()"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:115:18: 'score()'
					{
					match("score()"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SCORE"

	// $ANTLR start "TIMESTAMP"
	public final void mTIMESTAMP() throws RecognitionException {
		try {
			int _type = TIMESTAMP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:116:10: ( 'TIMESTAMP' | 'timestamp' )
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0=='T') ) {
				alt22=1;
			}
			else if ( (LA22_0=='t') ) {
				alt22=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 22, 0, input);
				throw nvae;
			}

			switch (alt22) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:116:12: 'TIMESTAMP'
					{
					match("TIMESTAMP"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:116:26: 'timestamp'
					{
					match("timestamp"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TIMESTAMP"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:117:7: ( '(' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:117:9: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:118:7: ( ')' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:118:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "QUOTE"
	public final void mQUOTE() throws RecognitionException {
		try {
			int _type = QUOTE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:119:6: ( '\\'' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:119:8: '\\''
			{
			match('\''); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUOTE"

	// $ANTLR start "STAR"
	public final void mSTAR() throws RecognitionException {
		try {
			int _type = STAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:120:5: ( '*' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:120:7: '*'
			{
			match('*'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STAR"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:121:4: ( '.' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:121:6: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOT"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:122:6: ( ',' )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:122:8: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMA"

	// $ANTLR start "CONST"
	public final void mCONST() throws RecognitionException {
		try {
			int _type = CONST;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:124:6: ( STRING | INTEGER | BOOLEAN | TIMESTAMP WS STRING )
			int alt23=4;
			switch ( input.LA(1) ) {
			case '\'':
				{
				alt23=1;
				}
				break;
			case '+':
			case '-':
			case '.':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				{
				alt23=2;
				}
				break;
			case 't':
				{
				int LA23_3 = input.LA(2);
				if ( (LA23_3=='r') ) {
					alt23=3;
				}
				else if ( (LA23_3=='i') ) {
					alt23=4;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 23, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 'F':
			case 'f':
				{
				alt23=3;
				}
				break;
			case 'T':
				{
				int LA23_5 = input.LA(2);
				if ( (LA23_5=='R') ) {
					alt23=3;
				}
				else if ( (LA23_5=='I') ) {
					alt23=4;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 23, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 23, 0, input);
				throw nvae;
			}
			switch (alt23) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:124:8: STRING
					{
					mSTRING(); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:124:17: INTEGER
					{
					mINTEGER(); 

					}
					break;
				case 3 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:124:27: BOOLEAN
					{
					mBOOLEAN(); 

					}
					break;
				case 4 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:124:37: TIMESTAMP WS STRING
					{
					mTIMESTAMP(); 

					mWS(); 

					mSTRING(); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONST"

	// $ANTLR start "BOOLEAN"
	public final void mBOOLEAN() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:125:17: ( 'true' | 'false' | 'TRUE' | 'FALSE' )
			int alt24=4;
			switch ( input.LA(1) ) {
			case 't':
				{
				alt24=1;
				}
				break;
			case 'f':
				{
				alt24=2;
				}
				break;
			case 'T':
				{
				alt24=3;
				}
				break;
			case 'F':
				{
				alt24=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}
			switch (alt24) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:125:19: 'true'
					{
					match("true"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:125:28: 'false'
					{
					match("false"); 

					}
					break;
				case 3 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:125:38: 'TRUE'
					{
					match("TRUE"); 

					}
					break;
				case 4 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:125:47: 'FALSE'
					{
					match("FALSE"); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOOLEAN"

	// $ANTLR start "NAME"
	public final void mNAME() throws RecognitionException {
		try {
			int _type = NAME;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:126:5: ( ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | ':' )* )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:126:7: ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | ':' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:126:26: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | ':' )*
			loop25:
			while (true) {
				int alt25=2;
				int LA25_0 = input.LA(1);
				if ( ((LA25_0 >= '0' && LA25_0 <= ':')||(LA25_0 >= 'A' && LA25_0 <= 'Z')||(LA25_0 >= 'a' && LA25_0 <= 'z')) ) {
					alt25=1;
				}

				switch (alt25) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= ':')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop25;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAME"

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:129:16: ( QUOTE ( ESC_SEQ |~ ( '\\\\' | QUOTE ) )* QUOTE )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:129:18: QUOTE ( ESC_SEQ |~ ( '\\\\' | QUOTE ) )* QUOTE
			{
			mQUOTE(); 

			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:129:24: ( ESC_SEQ |~ ( '\\\\' | QUOTE ) )*
			loop26:
			while (true) {
				int alt26=3;
				int LA26_0 = input.LA(1);
				if ( (LA26_0=='\\') ) {
					alt26=1;
				}
				else if ( ((LA26_0 >= '\u0000' && LA26_0 <= '&')||(LA26_0 >= '(' && LA26_0 <= '[')||(LA26_0 >= ']' && LA26_0 <= '\uFFFF')) ) {
					alt26=2;
				}

				switch (alt26) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:129:25: ESC_SEQ
					{
					mESC_SEQ(); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:129:35: ~ ( '\\\\' | QUOTE )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop26;
				}
			}

			mQUOTE(); 

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "HEX_DIGIT"
	public final void mHEX_DIGIT() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:133:19: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEX_DIGIT"

	// $ANTLR start "ESC_SEQ"
	public final void mESC_SEQ() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:134:17: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\'' | '\\\\' | '%' | '_' ) | UNICODE_ESC | OCTAL_ESC )
			int alt27=3;
			int LA27_0 = input.LA(1);
			if ( (LA27_0=='\\') ) {
				switch ( input.LA(2) ) {
				case '%':
				case '\'':
				case '\\':
				case '_':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
					{
					alt27=1;
					}
					break;
				case 'u':
					{
					alt27=2;
					}
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					{
					alt27=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 27, 0, input);
				throw nvae;
			}

			switch (alt27) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:134:19: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\'' | '\\\\' | '%' | '_' )
					{
					match('\\'); 
					if ( input.LA(1)=='%'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='_'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:134:66: UNICODE_ESC
					{
					mUNICODE_ESC(); 

					}
					break;
				case 3 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:134:80: OCTAL_ESC
					{
					mOCTAL_ESC(); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ESC_SEQ"

	// $ANTLR start "OCTAL_ESC"
	public final void mOCTAL_ESC() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:135:19: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
			int alt28=3;
			int LA28_0 = input.LA(1);
			if ( (LA28_0=='\\') ) {
				int LA28_1 = input.LA(2);
				if ( ((LA28_1 >= '0' && LA28_1 <= '3')) ) {
					int LA28_2 = input.LA(3);
					if ( ((LA28_2 >= '0' && LA28_2 <= '7')) ) {
						int LA28_4 = input.LA(4);
						if ( ((LA28_4 >= '0' && LA28_4 <= '7')) ) {
							alt28=1;
						}

						else {
							alt28=2;
						}

					}

					else {
						alt28=3;
					}

				}
				else if ( ((LA28_1 >= '4' && LA28_1 <= '7')) ) {
					int LA28_3 = input.LA(3);
					if ( ((LA28_3 >= '0' && LA28_3 <= '7')) ) {
						alt28=2;
					}

					else {
						alt28=3;
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 28, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}

			switch (alt28) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:135:21: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
					{
					match('\\'); 
					if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:135:63: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
					{
					match('\\'); 
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 3 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:135:92: '\\\\' ( '0' .. '7' )
					{
					match('\\'); 
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OCTAL_ESC"

	// $ANTLR start "UNICODE_ESC"
	public final void mUNICODE_ESC() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:136:21: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:136:23: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
			{
			match('\\'); 
			match('u'); 
			mHEX_DIGIT(); 

			mHEX_DIGIT(); 

			mHEX_DIGIT(); 

			mHEX_DIGIT(); 

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNICODE_ESC"

	// $ANTLR start "INTEGER"
	public final void mINTEGER() throws RecognitionException {
		try {
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:17: ( ( '-' | '+' )? ( '0' .. '9' | '.' )+ ( 'E' ( '+' | '-' ) ( '0' .. '9' )+ )? )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:19: ( '-' | '+' )? ( '0' .. '9' | '.' )+ ( 'E' ( '+' | '-' ) ( '0' .. '9' )+ )?
			{
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:19: ( '-' | '+' )?
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0=='+'||LA29_0=='-') ) {
				alt29=1;
			}
			switch (alt29) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
					{
					if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}

			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:29: ( '0' .. '9' | '.' )+
			int cnt30=0;
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0=='.'||(LA30_0 >= '0' && LA30_0 <= '9')) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
					{
					if ( input.LA(1)=='.'||(input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt30 >= 1 ) break loop30;
					EarlyExitException eee = new EarlyExitException(30, input);
					throw eee;
				}
				cnt30++;
			}

			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:45: ( 'E' ( '+' | '-' ) ( '0' .. '9' )+ )?
			int alt32=2;
			int LA32_0 = input.LA(1);
			if ( (LA32_0=='E') ) {
				alt32=1;
			}
			switch (alt32) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:46: 'E' ( '+' | '-' ) ( '0' .. '9' )+
					{
					match('E'); 
					if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:139:58: ( '0' .. '9' )+
					int cnt31=0;
					loop31:
					while (true) {
						int alt31=2;
						int LA31_0 = input.LA(1);
						if ( ((LA31_0 >= '0' && LA31_0 <= '9')) ) {
							alt31=1;
						}

						switch (alt31) {
						case 1 :
							// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt31 >= 1 ) break loop31;
							EarlyExitException eee = new EarlyExitException(31, input);
							throw eee;
						}
						cnt31++;
					}

					}
					break;

			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER"

	// $ANTLR start "BINARY_OPERATOR"
	public final void mBINARY_OPERATOR() throws RecognitionException {
		try {
			int _type = BINARY_OPERATOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:16: ( ( '<>' | '<' | '>' | '<=' | '>=' ) )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:18: ( '<>' | '<' | '>' | '<=' | '>=' )
			{
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:18: ( '<>' | '<' | '>' | '<=' | '>=' )
			int alt33=5;
			int LA33_0 = input.LA(1);
			if ( (LA33_0=='<') ) {
				switch ( input.LA(2) ) {
				case '>':
					{
					alt33=1;
					}
					break;
				case '=':
					{
					alt33=4;
					}
					break;
				default:
					alt33=2;
				}
			}
			else if ( (LA33_0=='>') ) {
				int LA33_2 = input.LA(2);
				if ( (LA33_2=='=') ) {
					alt33=5;
				}

				else {
					alt33=3;
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 33, 0, input);
				throw nvae;
			}

			switch (alt33) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:19: '<>'
					{
					match("<>"); 

					}
					break;
				case 2 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:24: '<'
					{
					match('<'); 
					}
					break;
				case 3 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:28: '>'
					{
					match('>'); 
					}
					break;
				case 4 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:32: '<='
					{
					match("<="); 

					}
					break;
				case 5 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:141:37: '>='
					{
					match(">="); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BINARY_OPERATOR"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:143:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:143:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:143:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt34=0;
			loop34:
			while (true) {
				int alt34=2;
				int LA34_0 = input.LA(1);
				if ( ((LA34_0 >= '\t' && LA34_0 <= '\n')||LA34_0=='\r'||LA34_0==' ') ) {
					alt34=1;
				}

				switch (alt34) {
				case 1 :
					// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt34 >= 1 ) break loop34;
					EarlyExitException eee = new EarlyExitException(34, input);
					throw eee;
				}
				cnt34++;
			}

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:8: ( T__46 | SELECT | FROM | WHERE | ORDER_BY | ASC | DESC | AND | OR | NOT | IN | IS | NULL | EQUALS | ANY | LIKE | AS | CONTAINS | IN_FOLDER | IN_TREE | SCORE | TIMESTAMP | LPAREN | RPAREN | QUOTE | STAR | DOT | COMMA | CONST | NAME | BINARY_OPERATOR | WS )
		int alt35=32;
		alt35 = dfa35.predict(input);
		switch (alt35) {
			case 1 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:10: T__46
				{
				mT__46(); 

				}
				break;
			case 2 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:16: SELECT
				{
				mSELECT(); 

				}
				break;
			case 3 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:23: FROM
				{
				mFROM(); 

				}
				break;
			case 4 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:28: WHERE
				{
				mWHERE(); 

				}
				break;
			case 5 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:34: ORDER_BY
				{
				mORDER_BY(); 

				}
				break;
			case 6 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:43: ASC
				{
				mASC(); 

				}
				break;
			case 7 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:47: DESC
				{
				mDESC(); 

				}
				break;
			case 8 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:52: AND
				{
				mAND(); 

				}
				break;
			case 9 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:56: OR
				{
				mOR(); 

				}
				break;
			case 10 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:59: NOT
				{
				mNOT(); 

				}
				break;
			case 11 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:63: IN
				{
				mIN(); 

				}
				break;
			case 12 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:66: IS
				{
				mIS(); 

				}
				break;
			case 13 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:69: NULL
				{
				mNULL(); 

				}
				break;
			case 14 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:74: EQUALS
				{
				mEQUALS(); 

				}
				break;
			case 15 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:81: ANY
				{
				mANY(); 

				}
				break;
			case 16 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:85: LIKE
				{
				mLIKE(); 

				}
				break;
			case 17 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:90: AS
				{
				mAS(); 

				}
				break;
			case 18 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:93: CONTAINS
				{
				mCONTAINS(); 

				}
				break;
			case 19 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:102: IN_FOLDER
				{
				mIN_FOLDER(); 

				}
				break;
			case 20 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:112: IN_TREE
				{
				mIN_TREE(); 

				}
				break;
			case 21 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:120: SCORE
				{
				mSCORE(); 

				}
				break;
			case 22 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:126: TIMESTAMP
				{
				mTIMESTAMP(); 

				}
				break;
			case 23 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:136: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 24 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:143: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 25 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:150: QUOTE
				{
				mQUOTE(); 

				}
				break;
			case 26 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:156: STAR
				{
				mSTAR(); 

				}
				break;
			case 27 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:161: DOT
				{
				mDOT(); 

				}
				break;
			case 28 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:165: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 29 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:171: CONST
				{
				mCONST(); 

				}
				break;
			case 30 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:177: NAME
				{
				mNAME(); 

				}
				break;
			case 31 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:182: BINARY_OPERATOR
				{
				mBINARY_OPERATOR(); 

				}
				break;
			case 32 :
				// C:\\Dev\\workspace-sts-2.9.0.RELEASE-1\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:1:198: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA35 dfa35 = new DFA35(this);
	static final String DFA35_eotS =
		"\1\uffff\20\37\1\uffff\6\37\2\uffff\1\104\1\uffff\1\105\5\uffff\12\37"+
		"\2\122\1\125\1\37\1\125\7\37\1\142\1\143\1\142\1\143\10\37\2\uffff\14"+
		"\37\1\uffff\1\37\1\172\1\uffff\1\173\1\174\1\172\1\173\1\174\2\37\1\177"+
		"\1\37\1\177\1\37\4\uffff\15\37\1\u0091\1\37\1\u0091\5\37\3\uffff\2\u0098"+
		"\1\uffff\2\u0099\2\uffff\2\u009a\3\37\1\36\1\37\1\36\5\37\1\uffff\2\36"+
		"\2\u00a3\2\37\3\uffff\5\37\1\u00aa\1\uffff\1\u00aa\2\uffff\4\37\2\uffff"+
		"\4\37\2\u00b3\2\37\1\uffff\2\u00b6\1\uffff";
	static final String DFA35_eofS =
		"\u00b7\uffff";
	static final String DFA35_minS =
		"\1\11\1\103\1\143\1\101\1\141\1\110\1\150\1\122\1\162\1\116\1\156\1\105"+
		"\1\145\1\117\1\157\1\116\1\156\1\uffff\1\111\1\151\1\117\1\157\1\111\1"+
		"\151\2\uffff\1\0\1\uffff\1\56\5\uffff\1\101\1\117\1\154\1\157\1\117\1"+
		"\114\1\157\1\154\1\105\1\145\3\60\1\104\1\60\1\144\1\123\1\163\1\124\1"+
		"\114\1\164\1\154\4\60\1\113\1\153\1\116\1\156\1\115\1\125\1\155\1\165"+
		"\2\uffff\1\122\1\105\1\122\1\145\1\162\1\115\1\123\1\155\1\163\1\122\1"+
		"\162\1\105\1\uffff\1\145\1\60\1\uffff\5\60\1\103\1\143\1\60\1\114\1\60"+
		"\1\154\1\106\2\uffff\1\146\1\105\1\145\1\124\1\164\2\105\2\145\2\103\1"+
		"\105\1\143\1\145\1\60\1\105\1\60\1\145\1\105\1\145\1\122\1\162\3\uffff"+
		"\2\60\1\uffff\2\60\2\uffff\2\60\1\101\1\141\1\123\1\60\1\163\1\60\1\110"+
		"\1\124\1\50\1\164\1\50\1\uffff\4\60\2\11\3\uffff\1\111\1\151\1\124\1\164"+
		"\1\137\1\60\1\uffff\1\60\2\uffff\1\116\1\156\1\101\1\141\2\uffff\1\123"+
		"\1\163\1\115\1\155\2\60\1\120\1\160\1\uffff\2\11\1\uffff";
	static final String DFA35_maxS =
		"\1\172\1\105\1\145\1\122\1\162\1\110\1\150\1\122\1\162\1\123\1\163\1\105"+
		"\1\145\1\125\1\165\1\123\1\163\1\uffff\1\111\1\151\1\117\1\157\1\122\1"+
		"\162\2\uffff\1\uffff\1\uffff\1\105\5\uffff\1\114\1\117\1\154\1\157\1\117"+
		"\1\114\1\157\1\154\1\105\1\145\3\172\1\131\1\172\1\171\1\123\1\163\1\124"+
		"\1\114\1\164\1\154\4\172\1\113\1\153\1\116\1\156\1\115\1\125\1\155\1\165"+
		"\2\uffff\1\122\1\105\1\122\1\145\1\162\1\115\1\123\1\155\1\163\1\122\1"+
		"\162\1\105\1\uffff\1\145\1\172\1\uffff\5\172\1\103\1\143\1\172\1\114\1"+
		"\172\1\154\1\124\2\uffff\1\164\1\105\1\145\1\124\1\164\2\105\2\145\2\103"+
		"\1\105\1\143\1\145\1\172\1\105\1\172\1\145\1\105\1\145\1\122\1\162\3\uffff"+
		"\2\172\1\uffff\2\172\2\uffff\2\172\1\101\1\141\1\123\1\172\1\163\1\172"+
		"\1\110\1\124\1\50\1\164\1\50\1\uffff\4\172\2\40\3\uffff\1\111\1\151\1"+
		"\124\1\164\1\137\1\172\1\uffff\1\172\2\uffff\1\116\1\156\1\101\1\141\2"+
		"\uffff\1\123\1\163\1\115\1\155\2\172\1\120\1\160\1\uffff\2\172\1\uffff";
	static final String DFA35_acceptS =
		"\21\uffff\1\16\6\uffff\1\27\1\30\1\uffff\1\32\1\uffff\1\34\1\35\1\36\1"+
		"\37\1\40\42\uffff\1\31\1\33\14\uffff\1\11\2\uffff\1\21\14\uffff\1\13\1"+
		"\14\26\uffff\1\6\1\10\1\17\2\uffff\1\12\2\uffff\1\23\1\24\15\uffff\1\3"+
		"\6\uffff\1\7\1\15\1\20\6\uffff\1\25\1\uffff\1\4\1\5\4\uffff\1\1\1\2\10"+
		"\uffff\1\22\2\uffff\1\26";
	static final String DFA35_specialS =
		"\32\uffff\1\0\u009c\uffff}>";
	static final String[] DFA35_transitionS = {
			"\2\41\2\uffff\1\41\22\uffff\1\41\6\uffff\1\32\1\30\1\31\1\33\1\36\1\35"+
			"\1\36\1\34\1\uffff\12\36\2\uffff\1\40\1\21\1\40\2\uffff\1\11\1\37\1\24"+
			"\1\13\1\37\1\3\2\37\1\17\2\37\1\22\1\37\1\15\1\7\3\37\1\1\1\26\2\37\1"+
			"\5\3\37\6\uffff\1\12\1\37\1\25\1\14\1\37\1\4\2\37\1\20\2\37\1\23\1\37"+
			"\1\16\1\10\3\37\1\2\1\27\2\37\1\6\3\37",
			"\1\43\1\uffff\1\42",
			"\1\45\1\uffff\1\44",
			"\1\47\20\uffff\1\46",
			"\1\51\20\uffff\1\50",
			"\1\52",
			"\1\53",
			"\1\54",
			"\1\55",
			"\1\57\4\uffff\1\56",
			"\1\61\4\uffff\1\60",
			"\1\62",
			"\1\63",
			"\1\64\5\uffff\1\65",
			"\1\66\5\uffff\1\67",
			"\1\70\4\uffff\1\71",
			"\1\72\4\uffff\1\73",
			"",
			"\1\74",
			"\1\75",
			"\1\76",
			"\1\77",
			"\1\100\10\uffff\1\101",
			"\1\102\10\uffff\1\103",
			"",
			"",
			"\0\36",
			"",
			"\1\36\1\uffff\12\36\13\uffff\1\36",
			"",
			"",
			"",
			"",
			"",
			"\1\106\12\uffff\1\107",
			"\1\110",
			"\1\111",
			"\1\112",
			"\1\113",
			"\1\114",
			"\1\115",
			"\1\116",
			"\1\117",
			"\1\120",
			"\13\37\6\uffff\3\37\1\121\26\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\3\37\1\123\26\37",
			"\13\37\6\uffff\2\37\1\124\27\37\6\uffff\32\37",
			"\1\126\24\uffff\1\127",
			"\13\37\6\uffff\32\37\6\uffff\2\37\1\130\27\37",
			"\1\131\24\uffff\1\132",
			"\1\133",
			"\1\134",
			"\1\135",
			"\1\136",
			"\1\137",
			"\1\140",
			"\13\37\6\uffff\32\37\4\uffff\1\141\1\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\4\uffff\1\144\1\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\145",
			"\1\146",
			"\1\147",
			"\1\150",
			"\1\151",
			"\1\152",
			"\1\153",
			"\1\154",
			"",
			"",
			"\1\155",
			"\1\156",
			"\1\157",
			"\1\160",
			"\1\161",
			"\1\162",
			"\1\163",
			"\1\164",
			"\1\165",
			"\1\166",
			"\1\167",
			"\1\170",
			"",
			"\1\171",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\175",
			"\1\176",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u0080",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u0081",
			"\1\u0082\15\uffff\1\u0083",
			"",
			"",
			"\1\u0082\15\uffff\1\u0083",
			"\1\u0084",
			"\1\u0085",
			"\1\u0086",
			"\1\u0087",
			"\1\u0088",
			"\1\u0089",
			"\1\u008a",
			"\1\u008b",
			"\1\u008c",
			"\1\u008d",
			"\1\u008e",
			"\1\u008f",
			"\1\u0090",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u0092",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u0093",
			"\1\u0094",
			"\1\u0095",
			"\1\u0096",
			"\1\u0097",
			"",
			"",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u009b",
			"\1\u009c",
			"\1\u009d",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u009e",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u009f",
			"\1\u00a0",
			"\1\u00a1",
			"\1\u00a2",
			"\1\u00a1",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\2\u00a4\2\uffff\1\u00a4\22\uffff\1\u00a4",
			"\2\u00a4\2\uffff\1\u00a4\22\uffff\1\u00a4",
			"",
			"",
			"",
			"\1\u00a5",
			"\1\u00a6",
			"\1\u00a7",
			"\1\u00a8",
			"\1\u00a9",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"",
			"",
			"\1\u00ab",
			"\1\u00ac",
			"\1\u00ad",
			"\1\u00ae",
			"",
			"",
			"\1\u00af",
			"\1\u00b0",
			"\1\u00b1",
			"\1\u00b2",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\13\37\6\uffff\32\37\6\uffff\32\37",
			"\1\u00b4",
			"\1\u00b5",
			"",
			"\2\36\2\uffff\1\36\22\uffff\1\36\17\uffff\13\37\6\uffff\32\37\6\uffff"+
			"\32\37",
			"\2\36\2\uffff\1\36\22\uffff\1\36\17\uffff\13\37\6\uffff\32\37\6\uffff"+
			"\32\37",
			""
	};

	static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
	static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
	static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
	static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
	static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
	static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
	static final short[][] DFA35_transition;

	static {
		int numStates = DFA35_transitionS.length;
		DFA35_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
		}
	}

	protected class DFA35 extends DFA {

		public DFA35(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 35;
			this.eot = DFA35_eot;
			this.eof = DFA35_eof;
			this.min = DFA35_min;
			this.max = DFA35_max;
			this.accept = DFA35_accept;
			this.special = DFA35_special;
			this.transition = DFA35_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__46 | SELECT | FROM | WHERE | ORDER_BY | ASC | DESC | AND | OR | NOT | IN | IS | NULL | EQUALS | ANY | LIKE | AS | CONTAINS | IN_FOLDER | IN_TREE | SCORE | TIMESTAMP | LPAREN | RPAREN | QUOTE | STAR | DOT | COMMA | CONST | NAME | BINARY_OPERATOR | WS );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA35_26 = input.LA(1);
						s = -1;
						if ( ((LA35_26 >= '\u0000' && LA35_26 <= '\uFFFF')) ) {s = 30;}
						else s = 68;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 35, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
