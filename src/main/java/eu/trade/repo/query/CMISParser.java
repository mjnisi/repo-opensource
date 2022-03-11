package eu.trade.repo.query;
import eu.trade.repo.query.ast.*;
import eu.trade.repo.query.ast.TreeFilter;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class CMISParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANY", "AS", "ASC", "BINARY_OPERATOR", 
		"BOOLEAN", "COLUMN", "COMMA", "CONST", "CONTAINS", "DESC", "DOT", "EQUALS", 
		"ESC_SEQ", "FILTER", "FROM", "HEX_DIGIT", "IN", "INTEGER", "IN_FOLDER", 
		"IN_TREE", "IS", "LIKE", "LPAREN", "NAME", "NOT", "NULL", "OCTAL_ESC", 
		"OR", "ORDER", "ORDER_BY", "QUOTE", "RPAREN", "SCORE", "SELECT", "STAR", 
		"STRING", "TABLE", "TIMESTAMP", "UNICODE_ESC", "WHERE", "WS", "'SEARCH_SCORE'"
	};
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public CMISParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public CMISParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return CMISParser.tokenNames; }
	@Override public String getGrammarFileName() { return "C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g"; }


	public static class cmis_query_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "cmis_query"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:1: cmis_query : SELECT ! columns FROM ! tables ( WHERE ! where_clause )? ( ORDER_BY ! order_by_clause )? EOF !;
	public final CMISParser.cmis_query_return cmis_query() throws RecognitionException {
		CMISParser.cmis_query_return retval = new CMISParser.cmis_query_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token SELECT1=null;
		Token FROM3=null;
		Token WHERE5=null;
		Token ORDER_BY7=null;
		Token EOF9=null;
		ParserRuleReturnScope columns2 =null;
		ParserRuleReturnScope tables4 =null;
		ParserRuleReturnScope where_clause6 =null;
		ParserRuleReturnScope order_by_clause8 =null;

		CMISCommonTree SELECT1_tree=null;
		CMISCommonTree FROM3_tree=null;
		CMISCommonTree WHERE5_tree=null;
		CMISCommonTree ORDER_BY7_tree=null;
		CMISCommonTree EOF9_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:11: ( SELECT ! columns FROM ! tables ( WHERE ! where_clause )? ( ORDER_BY ! order_by_clause )? EOF !)
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:13: SELECT ! columns FROM ! tables ( WHERE ! where_clause )? ( ORDER_BY ! order_by_clause )? EOF !
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			SELECT1=(Token)match(input,SELECT,FOLLOW_SELECT_in_cmis_query87); if (state.failed) return retval;
			pushFollow(FOLLOW_columns_in_cmis_query90);
			columns2=columns();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, columns2.getTree());

			FROM3=(Token)match(input,FROM,FOLLOW_FROM_in_cmis_query92); if (state.failed) return retval;
			pushFollow(FOLLOW_tables_in_cmis_query95);
			tables4=tables();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, tables4.getTree());

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:42: ( WHERE ! where_clause )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0==WHERE) ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:43: WHERE ! where_clause
					{
					WHERE5=(Token)match(input,WHERE,FOLLOW_WHERE_in_cmis_query98); if (state.failed) return retval;
					pushFollow(FOLLOW_where_clause_in_cmis_query101);
					where_clause6=where_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, where_clause6.getTree());

					}
					break;

			}

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:65: ( ORDER_BY ! order_by_clause )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==ORDER_BY) ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:28:66: ORDER_BY ! order_by_clause
					{
					ORDER_BY7=(Token)match(input,ORDER_BY,FOLLOW_ORDER_BY_in_cmis_query106); if (state.failed) return retval;
					pushFollow(FOLLOW_order_by_clause_in_cmis_query109);
					order_by_clause8=order_by_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, order_by_clause8.getTree());

					}
					break;

			}

			EOF9=(Token)match(input,EOF,FOLLOW_EOF_in_cmis_query113); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmis_query"


	public static class columns_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "columns"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:1: columns : ( all_columns -> COLUMN[\"*\", null, null, null] | column_expr ( COMMA ! column_expr )* );
	public final CMISParser.columns_return columns() throws RecognitionException {
		CMISParser.columns_return retval = new CMISParser.columns_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token COMMA12=null;
		ParserRuleReturnScope all_columns10 =null;
		ParserRuleReturnScope column_expr11 =null;
		ParserRuleReturnScope column_expr13 =null;

		CMISCommonTree COMMA12_tree=null;
		RewriteRuleSubtreeStream stream_all_columns=new RewriteRuleSubtreeStream(adaptor,"rule all_columns");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:8: ( all_columns -> COLUMN[\"*\", null, null, null] | column_expr ( COMMA ! column_expr )* )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==STAR) ) {
				alt4=1;
			}
			else if ( (LA4_0==NAME||LA4_0==SCORE) ) {
				alt4=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:10: all_columns
					{
					pushFollow(FOLLOW_all_columns_in_columns121);
					all_columns10=all_columns();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_all_columns.add(all_columns10.getTree());
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 30:22: -> COLUMN[\"*\", null, null, null]
					{
						adaptor.addChild(root_0, new Column(COLUMN, "*", null, null, null));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:65: column_expr ( COMMA ! column_expr )*
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_column_expr_in_columns133);
					column_expr11=column_expr();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, column_expr11.getTree());

					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:77: ( COMMA ! column_expr )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( (LA3_0==COMMA) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:30:78: COMMA ! column_expr
							{
							COMMA12=(Token)match(input,COMMA,FOLLOW_COMMA_in_columns136); if (state.failed) return retval;
							pushFollow(FOLLOW_column_expr_in_columns139);
							column_expr13=column_expr();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, column_expr13.getTree());

							}
							break;

						default :
							break loop3;
						}
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "columns"


	public static class column_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "column_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:31:1: column_expr : ( SCORE ( ( AS )? alias )? -> COLUMN[null, null, $alias.text, \"score\"] | ( qualifier DOT )? column ( ( AS )? alias )? -> COLUMN[$column.text, $qualifier.text, $alias.text, null] | qualifier DOT all_columns -> COLUMN[\"*\", $qualifier.text, null, null] );
	public final CMISParser.column_expr_return column_expr() throws RecognitionException {
		CMISParser.column_expr_return retval = new CMISParser.column_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token SCORE14=null;
		Token AS15=null;
		Token DOT18=null;
		Token AS20=null;
		Token DOT23=null;
		ParserRuleReturnScope alias16 =null;
		ParserRuleReturnScope qualifier17 =null;
		ParserRuleReturnScope column19 =null;
		ParserRuleReturnScope alias21 =null;
		ParserRuleReturnScope qualifier22 =null;
		ParserRuleReturnScope all_columns24 =null;

		CMISCommonTree SCORE14_tree=null;
		CMISCommonTree AS15_tree=null;
		CMISCommonTree DOT18_tree=null;
		CMISCommonTree AS20_tree=null;
		CMISCommonTree DOT23_tree=null;
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleTokenStream stream_SCORE=new RewriteRuleTokenStream(adaptor,"token SCORE");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_alias=new RewriteRuleSubtreeStream(adaptor,"rule alias");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_all_columns=new RewriteRuleSubtreeStream(adaptor,"rule all_columns");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:31:12: ( SCORE ( ( AS )? alias )? -> COLUMN[null, null, $alias.text, \"score\"] | ( qualifier DOT )? column ( ( AS )? alias )? -> COLUMN[$column.text, $qualifier.text, $alias.text, null] | qualifier DOT all_columns -> COLUMN[\"*\", $qualifier.text, null, null] )
			int alt10=3;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==SCORE) ) {
				alt10=1;
			}
			else if ( (LA10_0==NAME) ) {
				int LA10_2 = input.LA(2);
				if ( (LA10_2==DOT) ) {
					int LA10_3 = input.LA(3);
					if ( (LA10_3==NAME) ) {
						alt10=2;
					}
					else if ( (LA10_3==STAR) ) {
						alt10=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 10, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA10_2==AS||LA10_2==COMMA||LA10_2==FROM||LA10_2==NAME) ) {
					alt10=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 10, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:32:3: SCORE ( ( AS )? alias )?
					{
					SCORE14=(Token)match(input,SCORE,FOLLOW_SCORE_in_column_expr151); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SCORE.add(SCORE14);

					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:32:9: ( ( AS )? alias )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0==AS||LA6_0==NAME) ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:32:10: ( AS )? alias
							{
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:32:10: ( AS )?
							int alt5=2;
							int LA5_0 = input.LA(1);
							if ( (LA5_0==AS) ) {
								alt5=1;
							}
							switch (alt5) {
								case 1 :
									// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:32:10: AS
									{
									AS15=(Token)match(input,AS,FOLLOW_AS_in_column_expr154); if (state.failed) return retval; 
									if ( state.backtracking==0 ) stream_AS.add(AS15);

									}
									break;

							}

							pushFollow(FOLLOW_alias_in_column_expr157);
							alias16=alias();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alias.add(alias16.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 32:22: -> COLUMN[null, null, $alias.text, \"score\"]
					{
						adaptor.addChild(root_0, new Column(COLUMN, null, null, (alias16!=null?input.toString(alias16.start,alias16.stop):null), "score"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:3: ( qualifier DOT )? column ( ( AS )? alias )?
					{
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:3: ( qualifier DOT )?
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0==NAME) ) {
						int LA7_1 = input.LA(2);
						if ( (LA7_1==DOT) ) {
							alt7=1;
						}
					}
					switch (alt7) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:4: qualifier DOT
							{
							pushFollow(FOLLOW_qualifier_in_column_expr175);
							qualifier17=qualifier();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_qualifier.add(qualifier17.getTree());
							DOT18=(Token)match(input,DOT,FOLLOW_DOT_in_column_expr177); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DOT.add(DOT18);

							}
							break;

					}

					pushFollow(FOLLOW_column_in_column_expr181);
					column19=column();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_column.add(column19.getTree());
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:27: ( ( AS )? alias )?
					int alt9=2;
					int LA9_0 = input.LA(1);
					if ( (LA9_0==AS||LA9_0==NAME) ) {
						alt9=1;
					}
					switch (alt9) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:28: ( AS )? alias
							{
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:28: ( AS )?
							int alt8=2;
							int LA8_0 = input.LA(1);
							if ( (LA8_0==AS) ) {
								alt8=1;
							}
							switch (alt8) {
								case 1 :
									// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:33:28: AS
									{
									AS20=(Token)match(input,AS,FOLLOW_AS_in_column_expr184); if (state.failed) return retval; 
									if ( state.backtracking==0 ) stream_AS.add(AS20);

									}
									break;

							}

							pushFollow(FOLLOW_alias_in_column_expr187);
							alias21=alias();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alias.add(alias21.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 33:40: -> COLUMN[$column.text, $qualifier.text, $alias.text, null]
					{
						adaptor.addChild(root_0, new Column(COLUMN, (column19!=null?input.toString(column19.start,column19.stop):null), (qualifier17!=null?input.toString(qualifier17.start,qualifier17.stop):null), (alias21!=null?input.toString(alias21.start,alias21.stop):null), null));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:34:3: qualifier DOT all_columns
					{
					pushFollow(FOLLOW_qualifier_in_column_expr204);
					qualifier22=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier22.getTree());
					DOT23=(Token)match(input,DOT,FOLLOW_DOT_in_column_expr206); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT23);

					pushFollow(FOLLOW_all_columns_in_column_expr208);
					all_columns24=all_columns();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_all_columns.add(all_columns24.getTree());
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 34:29: -> COLUMN[\"*\", $qualifier.text, null, null]
					{
						adaptor.addChild(root_0, new Column(COLUMN, "*", (qualifier22!=null?input.toString(qualifier22.start,qualifier22.stop):null), null, null));
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "column_expr"


	public static class qualifier_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "qualifier"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:36:1: qualifier : NAME ;
	public final CMISParser.qualifier_return qualifier() throws RecognitionException {
		CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token NAME25=null;

		CMISCommonTree NAME25_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:36:10: ( NAME )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:36:12: NAME
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			NAME25=(Token)match(input,NAME,FOLLOW_NAME_in_qualifier225); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			NAME25_tree = (CMISCommonTree)adaptor.create(NAME25);
			adaptor.addChild(root_0, NAME25_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "qualifier"


	public static class column_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "column"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:37:1: column : NAME ;
	public final CMISParser.column_return column() throws RecognitionException {
		CMISParser.column_return retval = new CMISParser.column_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token NAME26=null;

		CMISCommonTree NAME26_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:37:7: ( NAME )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:37:9: NAME
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			NAME26=(Token)match(input,NAME,FOLLOW_NAME_in_column231); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			NAME26_tree = (CMISCommonTree)adaptor.create(NAME26);
			adaptor.addChild(root_0, NAME26_tree);
			}

			if ( state.backtracking==0 ) {columns.add(input.toString(retval.start,input.LT(-1)));}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "column"


	public static class alias_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "alias"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:38:1: alias : NAME ;
	public final CMISParser.alias_return alias() throws RecognitionException {
		CMISParser.alias_return retval = new CMISParser.alias_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token NAME27=null;

		CMISCommonTree NAME27_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:38:6: ( NAME )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:38:8: NAME
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			NAME27=(Token)match(input,NAME,FOLLOW_NAME_in_alias240); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			NAME27_tree = (CMISCommonTree)adaptor.create(NAME27);
			adaptor.addChild(root_0, NAME27_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "alias"


	public static class all_columns_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "all_columns"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:39:1: all_columns : STAR ;
	public final CMISParser.all_columns_return all_columns() throws RecognitionException {
		CMISParser.all_columns_return retval = new CMISParser.all_columns_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token STAR28=null;

		CMISCommonTree STAR28_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:39:12: ( STAR )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:39:14: STAR
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			STAR28=(Token)match(input,STAR,FOLLOW_STAR_in_all_columns246); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			STAR28_tree = (CMISCommonTree)adaptor.create(STAR28);
			adaptor.addChild(root_0, STAR28_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "all_columns"


	public static class tables_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "tables"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:41:1: tables : table_expr ;
	public final CMISParser.tables_return tables() throws RecognitionException {
		CMISParser.tables_return retval = new CMISParser.tables_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		ParserRuleReturnScope table_expr29 =null;


		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:41:7: ( table_expr )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:41:9: table_expr
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_table_expr_in_tables253);
			table_expr29=table_expr();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, table_expr29.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "tables"


	public static class table_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "table_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:1: table_expr : table ( ( AS )? alias )? -> TABLE[$table.text, $alias.text] ;
	public final CMISParser.table_expr_return table_expr() throws RecognitionException {
		CMISParser.table_expr_return retval = new CMISParser.table_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token AS31=null;
		ParserRuleReturnScope table30 =null;
		ParserRuleReturnScope alias32 =null;

		CMISCommonTree AS31_tree=null;
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleSubtreeStream stream_alias=new RewriteRuleSubtreeStream(adaptor,"rule alias");
		RewriteRuleSubtreeStream stream_table=new RewriteRuleSubtreeStream(adaptor,"rule table");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:11: ( table ( ( AS )? alias )? -> TABLE[$table.text, $alias.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:13: table ( ( AS )? alias )?
			{
			pushFollow(FOLLOW_table_in_table_expr260);
			table30=table();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_table.add(table30.getTree());
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:19: ( ( AS )? alias )?
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==AS||LA12_0==NAME) ) {
				alt12=1;
			}
			switch (alt12) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:20: ( AS )? alias
					{
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:20: ( AS )?
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0==AS) ) {
						alt11=1;
					}
					switch (alt11) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:43:20: AS
							{
							AS31=(Token)match(input,AS,FOLLOW_AS_in_table_expr263); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_AS.add(AS31);

							}
							break;

					}

					pushFollow(FOLLOW_alias_in_table_expr266);
					alias32=alias();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_alias.add(alias32.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 43:32: -> TABLE[$table.text, $alias.text]
			{
				adaptor.addChild(root_0, new Table(TABLE, (table30!=null?input.toString(table30.start,table30.stop):null), (alias32!=null?input.toString(alias32.start,alias32.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "table_expr"


	public static class table_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "table"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:44:1: table : NAME ;
	public final CMISParser.table_return table() throws RecognitionException {
		CMISParser.table_return retval = new CMISParser.table_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token NAME33=null;

		CMISCommonTree NAME33_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:44:6: ( NAME )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:44:8: NAME
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			NAME33=(Token)match(input,NAME,FOLLOW_NAME_in_table282); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			NAME33_tree = (CMISCommonTree)adaptor.create(NAME33);
			adaptor.addChild(root_0, NAME33_tree);
			}

			if ( state.backtracking==0 ) {tables.add(input.toString(retval.start,input.LT(-1)));}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "table"


	public static class where_clause_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "where_clause"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:47:1: where_clause : expression ;
	public final CMISParser.where_clause_return where_clause() throws RecognitionException {
		CMISParser.where_clause_return retval = new CMISParser.where_clause_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		ParserRuleReturnScope expression34 =null;


		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:47:13: ( expression )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:47:15: expression
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_expression_in_where_clause292);
			expression34=expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, expression34.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "where_clause"


	public static class expression_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "expression"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:49:1: expression : andexpression ;
	public final CMISParser.expression_return expression() throws RecognitionException {
		CMISParser.expression_return retval = new CMISParser.expression_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		ParserRuleReturnScope andexpression35 =null;


		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:49:12: ( andexpression )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:49:14: andexpression
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_andexpression_in_expression300);
			andexpression35=andexpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, andexpression35.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expression"


	public static class andexpression_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "andexpression"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:50:1: andexpression : orexpression ( AND ^ orexpression )* ;
	public final CMISParser.andexpression_return andexpression() throws RecognitionException {
		CMISParser.andexpression_return retval = new CMISParser.andexpression_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token AND37=null;
		ParserRuleReturnScope orexpression36 =null;
		ParserRuleReturnScope orexpression38 =null;

		CMISCommonTree AND37_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:50:15: ( orexpression ( AND ^ orexpression )* )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:50:17: orexpression ( AND ^ orexpression )*
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_orexpression_in_andexpression307);
			orexpression36=orexpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, orexpression36.getTree());

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:50:30: ( AND ^ orexpression )*
			loop13:
			while (true) {
				int alt13=2;
				int LA13_0 = input.LA(1);
				if ( (LA13_0==AND) ) {
					alt13=1;
				}

				switch (alt13) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:50:31: AND ^ orexpression
					{
					AND37=(Token)match(input,AND,FOLLOW_AND_in_andexpression310); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					AND37_tree = (CMISCommonTree)adaptor.create(AND37);
					root_0 = (CMISCommonTree)adaptor.becomeRoot(AND37_tree, root_0);
					}

					pushFollow(FOLLOW_orexpression_in_andexpression313);
					orexpression38=orexpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, orexpression38.getTree());

					}
					break;

				default :
					break loop13;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "andexpression"


	public static class orexpression_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "orexpression"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:51:1: orexpression : notexpression ( OR ^ notexpression )* ;
	public final CMISParser.orexpression_return orexpression() throws RecognitionException {
		CMISParser.orexpression_return retval = new CMISParser.orexpression_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token OR40=null;
		ParserRuleReturnScope notexpression39 =null;
		ParserRuleReturnScope notexpression41 =null;

		CMISCommonTree OR40_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:51:14: ( notexpression ( OR ^ notexpression )* )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:51:16: notexpression ( OR ^ notexpression )*
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_notexpression_in_orexpression322);
			notexpression39=notexpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, notexpression39.getTree());

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:51:30: ( OR ^ notexpression )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==OR) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:51:31: OR ^ notexpression
					{
					OR40=(Token)match(input,OR,FOLLOW_OR_in_orexpression325); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					OR40_tree = (CMISCommonTree)adaptor.create(OR40);
					root_0 = (CMISCommonTree)adaptor.becomeRoot(OR40_tree, root_0);
					}

					pushFollow(FOLLOW_notexpression_in_orexpression328);
					notexpression41=notexpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, notexpression41.getTree());

					}
					break;

				default :
					break loop14;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "orexpression"


	public static class notexpression_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "notexpression"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:52:1: notexpression : ( NOT ^ atom | atom );
	public final CMISParser.notexpression_return notexpression() throws RecognitionException {
		CMISParser.notexpression_return retval = new CMISParser.notexpression_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token NOT42=null;
		ParserRuleReturnScope atom43 =null;
		ParserRuleReturnScope atom44 =null;

		CMISCommonTree NOT42_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:52:15: ( NOT ^ atom | atom )
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==NOT) ) {
				alt15=1;
			}
			else if ( (LA15_0==ANY||(LA15_0 >= CONST && LA15_0 <= CONTAINS)||(LA15_0 >= IN_FOLDER && LA15_0 <= IN_TREE)||(LA15_0 >= LPAREN && LA15_0 <= NAME)) ) {
				alt15=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:52:17: NOT ^ atom
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					NOT42=(Token)match(input,NOT,FOLLOW_NOT_in_notexpression337); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					NOT42_tree = (CMISCommonTree)adaptor.create(NOT42);
					root_0 = (CMISCommonTree)adaptor.becomeRoot(NOT42_tree, root_0);
					}

					pushFollow(FOLLOW_atom_in_notexpression340);
					atom43=atom();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, atom43.getTree());

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:52:29: atom
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_atom_in_notexpression344);
					atom44=atom();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, atom44.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "notexpression"


	public static class atom_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "atom"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:1: atom : ( boolean_expr | ( LPAREN ! andexpression RPAREN !) );
	public final CMISParser.atom_return atom() throws RecognitionException {
		CMISParser.atom_return retval = new CMISParser.atom_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token LPAREN46=null;
		Token RPAREN48=null;
		ParserRuleReturnScope boolean_expr45 =null;
		ParserRuleReturnScope andexpression47 =null;

		CMISCommonTree LPAREN46_tree=null;
		CMISCommonTree RPAREN48_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:6: ( boolean_expr | ( LPAREN ! andexpression RPAREN !) )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==ANY||(LA16_0 >= CONST && LA16_0 <= CONTAINS)||(LA16_0 >= IN_FOLDER && LA16_0 <= IN_TREE)||LA16_0==NAME) ) {
				alt16=1;
			}
			else if ( (LA16_0==LPAREN) ) {
				alt16=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:8: boolean_expr
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_boolean_expr_in_atom351);
					boolean_expr45=boolean_expr();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, boolean_expr45.getTree());

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:23: ( LPAREN ! andexpression RPAREN !)
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:23: ( LPAREN ! andexpression RPAREN !)
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:53:24: LPAREN ! andexpression RPAREN !
					{
					LPAREN46=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom356); if (state.failed) return retval;
					pushFollow(FOLLOW_andexpression_in_atom359);
					andexpression47=andexpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, andexpression47.getTree());

					RPAREN48=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom361); if (state.failed) return retval;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "atom"


	public static class boolean_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "boolean_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:54:1: boolean_expr : ( comparison_or_quantified_predicate | quantified_in_predicate | in_predicate | like_predicate | null_predicate | text_search_predicate | folder_predicate | tree_predicate );
	public final CMISParser.boolean_expr_return boolean_expr() throws RecognitionException {
		CMISParser.boolean_expr_return retval = new CMISParser.boolean_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		ParserRuleReturnScope comparison_or_quantified_predicate49 =null;
		ParserRuleReturnScope quantified_in_predicate50 =null;
		ParserRuleReturnScope in_predicate51 =null;
		ParserRuleReturnScope like_predicate52 =null;
		ParserRuleReturnScope null_predicate53 =null;
		ParserRuleReturnScope text_search_predicate54 =null;
		ParserRuleReturnScope folder_predicate55 =null;
		ParserRuleReturnScope tree_predicate56 =null;


		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:54:13: ( comparison_or_quantified_predicate | quantified_in_predicate | in_predicate | like_predicate | null_predicate | text_search_predicate | folder_predicate | tree_predicate )
			int alt17=8;
			switch ( input.LA(1) ) {
			case CONST:
				{
				alt17=1;
				}
				break;
			case NAME:
				{
				switch ( input.LA(2) ) {
				case DOT:
					{
					int LA17_7 = input.LA(3);
					if ( (LA17_7==NAME) ) {
						switch ( input.LA(4) ) {
						case BINARY_OPERATOR:
						case EQUALS:
							{
							alt17=1;
							}
							break;
						case NOT:
							{
							int LA17_8 = input.LA(5);
							if ( (LA17_8==IN) ) {
								alt17=3;
							}
							else if ( (LA17_8==LIKE) ) {
								alt17=4;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 17, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case IN:
							{
							alt17=3;
							}
							break;
						case LIKE:
							{
							alt17=4;
							}
							break;
						case IS:
							{
							alt17=5;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 17, 12, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 17, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case BINARY_OPERATOR:
				case EQUALS:
					{
					alt17=1;
					}
					break;
				case NOT:
					{
					int LA17_8 = input.LA(3);
					if ( (LA17_8==IN) ) {
						alt17=3;
					}
					else if ( (LA17_8==LIKE) ) {
						alt17=4;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 17, 8, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case IN:
					{
					alt17=3;
					}
					break;
				case LIKE:
					{
					alt17=4;
					}
					break;
				case IS:
					{
					alt17=5;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 17, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case ANY:
				{
				alt17=2;
				}
				break;
			case CONTAINS:
				{
				alt17=6;
				}
				break;
			case IN_FOLDER:
				{
				alt17=7;
				}
				break;
			case IN_TREE:
				{
				alt17=8;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}
			switch (alt17) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:55:3: comparison_or_quantified_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_comparison_or_quantified_predicate_in_boolean_expr372);
					comparison_or_quantified_predicate49=comparison_or_quantified_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_or_quantified_predicate49.getTree());

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:56:3: quantified_in_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_quantified_in_predicate_in_boolean_expr378);
					quantified_in_predicate50=quantified_in_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, quantified_in_predicate50.getTree());

					}
					break;
				case 3 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:57:3: in_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_in_predicate_in_boolean_expr384);
					in_predicate51=in_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, in_predicate51.getTree());

					}
					break;
				case 4 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:58:3: like_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_like_predicate_in_boolean_expr390);
					like_predicate52=like_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, like_predicate52.getTree());

					}
					break;
				case 5 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:59:3: null_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_null_predicate_in_boolean_expr396);
					null_predicate53=null_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, null_predicate53.getTree());

					}
					break;
				case 6 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:60:3: text_search_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_text_search_predicate_in_boolean_expr402);
					text_search_predicate54=text_search_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, text_search_predicate54.getTree());

					}
					break;
				case 7 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:61:3: folder_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_folder_predicate_in_boolean_expr408);
					folder_predicate55=folder_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, folder_predicate55.getTree());

					}
					break;
				case 8 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:62:3: tree_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_tree_predicate_in_boolean_expr414);
					tree_predicate56=tree_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, tree_predicate56.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "boolean_expr"


	public static class comparison_or_quantified_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "comparison_or_quantified_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:1: comparison_or_quantified_predicate : ( ( value EQUALS ANY )=> quantified_predicate | comparison_predicate );
	public final CMISParser.comparison_or_quantified_predicate_return comparison_or_quantified_predicate() throws RecognitionException {
		CMISParser.comparison_or_quantified_predicate_return retval = new CMISParser.comparison_or_quantified_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		ParserRuleReturnScope quantified_predicate57 =null;
		ParserRuleReturnScope comparison_predicate58 =null;


		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:35: ( ( value EQUALS ANY )=> quantified_predicate | comparison_predicate )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==CONST) ) {
				int LA18_1 = input.LA(2);
				if ( (LA18_1==EQUALS) ) {
					int LA18_3 = input.LA(3);
					if ( (LA18_3==ANY) && (synpred1_CMIS())) {
						alt18=1;
					}
					else if ( (LA18_3==NAME) ) {
						alt18=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 18, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA18_1==BINARY_OPERATOR) ) {
					alt18=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA18_0==NAME) ) {
				alt18=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:37: ( value EQUALS ANY )=> quantified_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_quantified_predicate_in_comparison_or_quantified_predicate433);
					quantified_predicate57=quantified_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, quantified_predicate57.getTree());

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:81: comparison_predicate
					{
					root_0 = (CMISCommonTree)adaptor.nil();


					pushFollow(FOLLOW_comparison_predicate_in_comparison_or_quantified_predicate437);
					comparison_predicate58=comparison_predicate();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_predicate58.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "comparison_or_quantified_predicate"


	public static class comparison_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "comparison_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:67:1: comparison_predicate : ( ( qualifier DOT )? column op value -> FILTER[$qualifier.text,$column.text,$op.text,$value.text] | value op ( qualifier DOT )? column -> FILTER[$qualifier.text,$column.text,$op.text,$value.text] );
	public final CMISParser.comparison_predicate_return comparison_predicate() throws RecognitionException {
		CMISParser.comparison_predicate_return retval = new CMISParser.comparison_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token DOT60=null;
		Token DOT67=null;
		ParserRuleReturnScope qualifier59 =null;
		ParserRuleReturnScope column61 =null;
		ParserRuleReturnScope op62 =null;
		ParserRuleReturnScope value63 =null;
		ParserRuleReturnScope value64 =null;
		ParserRuleReturnScope op65 =null;
		ParserRuleReturnScope qualifier66 =null;
		ParserRuleReturnScope column68 =null;

		CMISCommonTree DOT60_tree=null;
		CMISCommonTree DOT67_tree=null;
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_op=new RewriteRuleSubtreeStream(adaptor,"rule op");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:67:21: ( ( qualifier DOT )? column op value -> FILTER[$qualifier.text,$column.text,$op.text,$value.text] | value op ( qualifier DOT )? column -> FILTER[$qualifier.text,$column.text,$op.text,$value.text] )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==NAME) ) {
				alt21=1;
			}
			else if ( (LA21_0==CONST) ) {
				alt21=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:68:3: ( qualifier DOT )? column op value
					{
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:68:3: ( qualifier DOT )?
					int alt19=2;
					int LA19_0 = input.LA(1);
					if ( (LA19_0==NAME) ) {
						int LA19_1 = input.LA(2);
						if ( (LA19_1==DOT) ) {
							alt19=1;
						}
					}
					switch (alt19) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:68:4: qualifier DOT
							{
							pushFollow(FOLLOW_qualifier_in_comparison_predicate448);
							qualifier59=qualifier();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_qualifier.add(qualifier59.getTree());
							DOT60=(Token)match(input,DOT,FOLLOW_DOT_in_comparison_predicate450); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DOT.add(DOT60);

							}
							break;

					}

					pushFollow(FOLLOW_column_in_comparison_predicate454);
					column61=column();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_column.add(column61.getTree());
					pushFollow(FOLLOW_op_in_comparison_predicate456);
					op62=op();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_op.add(op62.getTree());
					pushFollow(FOLLOW_value_in_comparison_predicate458);
					value63=value();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_value.add(value63.getTree());
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 68:36: -> FILTER[$qualifier.text,$column.text,$op.text,$value.text]
					{
						adaptor.addChild(root_0, new ComparisonFilter(FILTER, (qualifier59!=null?input.toString(qualifier59.start,qualifier59.stop):null), (column61!=null?input.toString(column61.start,column61.stop):null), (op62!=null?input.toString(op62.start,op62.stop):null), (value63!=null?input.toString(value63.start,value63.stop):null)));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:69:3: value op ( qualifier DOT )? column
					{
					pushFollow(FOLLOW_value_in_comparison_predicate472);
					value64=value();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_value.add(value64.getTree());
					pushFollow(FOLLOW_op_in_comparison_predicate474);
					op65=op();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_op.add(op65.getTree());
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:69:12: ( qualifier DOT )?
					int alt20=2;
					int LA20_0 = input.LA(1);
					if ( (LA20_0==NAME) ) {
						int LA20_1 = input.LA(2);
						if ( (LA20_1==DOT) ) {
							alt20=1;
						}
					}
					switch (alt20) {
						case 1 :
							// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:69:13: qualifier DOT
							{
							pushFollow(FOLLOW_qualifier_in_comparison_predicate477);
							qualifier66=qualifier();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_qualifier.add(qualifier66.getTree());
							DOT67=(Token)match(input,DOT,FOLLOW_DOT_in_comparison_predicate479); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DOT.add(DOT67);

							}
							break;

					}

					pushFollow(FOLLOW_column_in_comparison_predicate483);
					column68=column();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_column.add(column68.getTree());
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CMISCommonTree)adaptor.nil();
					// 69:36: -> FILTER[$qualifier.text,$column.text,$op.text,$value.text]
					{
						adaptor.addChild(root_0, new ComparisonFilter(FILTER, (qualifier66!=null?input.toString(qualifier66.start,qualifier66.stop):null), (column68!=null?input.toString(column68.start,column68.stop):null), (op65!=null?input.toString(op65.start,op65.stop):null), (value64!=null?input.toString(value64.start,value64.stop):null)));
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "comparison_predicate"


	public static class in_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "in_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:1: in_predicate : ( qualifier DOT )? column ( NOT )? IN in_expr -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree] ;
	public final CMISParser.in_predicate_return in_predicate() throws RecognitionException {
		CMISParser.in_predicate_return retval = new CMISParser.in_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token DOT70=null;
		Token NOT72=null;
		Token IN73=null;
		ParserRuleReturnScope qualifier69 =null;
		ParserRuleReturnScope column71 =null;
		ParserRuleReturnScope in_expr74 =null;

		CMISCommonTree DOT70_tree=null;
		CMISCommonTree NOT72_tree=null;
		CMISCommonTree IN73_tree=null;
		RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
		RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
		RewriteRuleSubtreeStream stream_in_expr=new RewriteRuleSubtreeStream(adaptor,"rule in_expr");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:13: ( ( qualifier DOT )? column ( NOT )? IN in_expr -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:15: ( qualifier DOT )? column ( NOT )? IN in_expr
			{
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:15: ( qualifier DOT )?
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==NAME) ) {
				int LA22_1 = input.LA(2);
				if ( (LA22_1==DOT) ) {
					alt22=1;
				}
			}
			switch (alt22) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:16: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_in_predicate499);
					qualifier69=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier69.getTree());
					DOT70=(Token)match(input,DOT,FOLLOW_DOT_in_in_predicate501); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT70);

					}
					break;

			}

			pushFollow(FOLLOW_column_in_in_predicate505);
			column71=column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_column.add(column71.getTree());
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:39: ( NOT )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==NOT) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:71:39: NOT
					{
					NOT72=(Token)match(input,NOT,FOLLOW_NOT_in_in_predicate507); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NOT.add(NOT72);

					}
					break;

			}

			IN73=(Token)match(input,IN,FOLLOW_IN_in_in_predicate510); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IN.add(IN73);

			pushFollow(FOLLOW_in_expr_in_in_predicate512);
			in_expr74=in_expr();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_in_expr.add(in_expr74.getTree());
			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 71:55: -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree]
			{
				adaptor.addChild(root_0, new InFilter(FILTER, (qualifier69!=null?input.toString(qualifier69.start,qualifier69.stop):null), (column71!=null?input.toString(column71.start,column71.stop):null), (NOT72!=null?NOT72.getText():null), (in_expr74!=null?((CMISCommonTree)in_expr74.getTree()):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_predicate"


	public static class like_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "like_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:1: like_predicate : ( qualifier DOT )? column ( NOT )? LIKE value -> FILTER[$qualifier.text, $column.text, $NOT.text, $value.text] ;
	public final CMISParser.like_predicate_return like_predicate() throws RecognitionException {
		CMISParser.like_predicate_return retval = new CMISParser.like_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token DOT76=null;
		Token NOT78=null;
		Token LIKE79=null;
		ParserRuleReturnScope qualifier75 =null;
		ParserRuleReturnScope column77 =null;
		ParserRuleReturnScope value80 =null;

		CMISCommonTree DOT76_tree=null;
		CMISCommonTree NOT78_tree=null;
		CMISCommonTree LIKE79_tree=null;
		RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_LIKE=new RewriteRuleTokenStream(adaptor,"token LIKE");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:15: ( ( qualifier DOT )? column ( NOT )? LIKE value -> FILTER[$qualifier.text, $column.text, $NOT.text, $value.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:17: ( qualifier DOT )? column ( NOT )? LIKE value
			{
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:17: ( qualifier DOT )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==NAME) ) {
				int LA24_1 = input.LA(2);
				if ( (LA24_1==DOT) ) {
					alt24=1;
				}
			}
			switch (alt24) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:18: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_like_predicate527);
					qualifier75=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier75.getTree());
					DOT76=(Token)match(input,DOT,FOLLOW_DOT_in_like_predicate529); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT76);

					}
					break;

			}

			pushFollow(FOLLOW_column_in_like_predicate533);
			column77=column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_column.add(column77.getTree());
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:41: ( NOT )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==NOT) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:72:41: NOT
					{
					NOT78=(Token)match(input,NOT,FOLLOW_NOT_in_like_predicate535); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NOT.add(NOT78);

					}
					break;

			}

			LIKE79=(Token)match(input,LIKE,FOLLOW_LIKE_in_like_predicate538); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LIKE.add(LIKE79);

			pushFollow(FOLLOW_value_in_like_predicate540);
			value80=value();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_value.add(value80.getTree());
			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 72:57: -> FILTER[$qualifier.text, $column.text, $NOT.text, $value.text]
			{
				adaptor.addChild(root_0, new LikeFilter(FILTER, (qualifier75!=null?input.toString(qualifier75.start,qualifier75.stop):null), (column77!=null?input.toString(column77.start,column77.stop):null), (NOT78!=null?NOT78.getText():null), (value80!=null?input.toString(value80.start,value80.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "like_predicate"


	public static class null_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "null_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:1: null_predicate : ( qualifier DOT )? column IS ( NOT )? NULL -> FILTER[$qualifier.text, $column.text, $NOT.text] ;
	public final CMISParser.null_predicate_return null_predicate() throws RecognitionException {
		CMISParser.null_predicate_return retval = new CMISParser.null_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token DOT82=null;
		Token IS84=null;
		Token NOT85=null;
		Token NULL86=null;
		ParserRuleReturnScope qualifier81 =null;
		ParserRuleReturnScope column83 =null;

		CMISCommonTree DOT82_tree=null;
		CMISCommonTree IS84_tree=null;
		CMISCommonTree NOT85_tree=null;
		CMISCommonTree NULL86_tree=null;
		RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_IS=new RewriteRuleTokenStream(adaptor,"token IS");
		RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:15: ( ( qualifier DOT )? column IS ( NOT )? NULL -> FILTER[$qualifier.text, $column.text, $NOT.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:17: ( qualifier DOT )? column IS ( NOT )? NULL
			{
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:17: ( qualifier DOT )?
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==NAME) ) {
				int LA26_1 = input.LA(2);
				if ( (LA26_1==DOT) ) {
					alt26=1;
				}
			}
			switch (alt26) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:18: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_null_predicate555);
					qualifier81=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier81.getTree());
					DOT82=(Token)match(input,DOT,FOLLOW_DOT_in_null_predicate557); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT82);

					}
					break;

			}

			pushFollow(FOLLOW_column_in_null_predicate561);
			column83=column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_column.add(column83.getTree());
			IS84=(Token)match(input,IS,FOLLOW_IS_in_null_predicate563); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IS.add(IS84);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:44: ( NOT )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==NOT) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:73:44: NOT
					{
					NOT85=(Token)match(input,NOT,FOLLOW_NOT_in_null_predicate565); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NOT.add(NOT85);

					}
					break;

			}

			NULL86=(Token)match(input,NULL,FOLLOW_NULL_in_null_predicate568); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_NULL.add(NULL86);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 73:54: -> FILTER[$qualifier.text, $column.text, $NOT.text]
			{
				adaptor.addChild(root_0, new NullFilter(FILTER, (qualifier81!=null?input.toString(qualifier81.start,qualifier81.stop):null), (column83!=null?input.toString(column83.start,column83.stop):null), (NOT85!=null?NOT85.getText():null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "null_predicate"


	public static class quantified_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "quantified_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:75:1: quantified_predicate : value EQUALS ANY ( qualifier DOT )? column -> FILTER[$qualifier.text, $column.text, $value.text] ;
	public final CMISParser.quantified_predicate_return quantified_predicate() throws RecognitionException {
		CMISParser.quantified_predicate_return retval = new CMISParser.quantified_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token EQUALS88=null;
		Token ANY89=null;
		Token DOT91=null;
		ParserRuleReturnScope value87 =null;
		ParserRuleReturnScope qualifier90 =null;
		ParserRuleReturnScope column92 =null;

		CMISCommonTree EQUALS88_tree=null;
		CMISCommonTree ANY89_tree=null;
		CMISCommonTree DOT91_tree=null;
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:75:21: ( value EQUALS ANY ( qualifier DOT )? column -> FILTER[$qualifier.text, $column.text, $value.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:75:23: value EQUALS ANY ( qualifier DOT )? column
			{
			pushFollow(FOLLOW_value_in_quantified_predicate583);
			value87=value();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_value.add(value87.getTree());
			EQUALS88=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_quantified_predicate585); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS88);

			ANY89=(Token)match(input,ANY,FOLLOW_ANY_in_quantified_predicate587); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ANY.add(ANY89);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:75:40: ( qualifier DOT )?
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==NAME) ) {
				int LA28_1 = input.LA(2);
				if ( (LA28_1==DOT) ) {
					alt28=1;
				}
			}
			switch (alt28) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:75:41: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_quantified_predicate590);
					qualifier90=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier90.getTree());
					DOT91=(Token)match(input,DOT,FOLLOW_DOT_in_quantified_predicate592); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT91);

					}
					break;

			}

			pushFollow(FOLLOW_column_in_quantified_predicate596);
			column92=column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_column.add(column92.getTree());
			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 75:64: -> FILTER[$qualifier.text, $column.text, $value.text]
			{
				adaptor.addChild(root_0, new QuantifiedComparisonFilter(FILTER, (qualifier90!=null?input.toString(qualifier90.start,qualifier90.stop):null), (column92!=null?input.toString(column92.start,column92.stop):null), (value87!=null?input.toString(value87.start,value87.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "quantified_predicate"


	public static class quantified_in_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "quantified_in_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:1: quantified_in_predicate : ANY ( qualifier DOT )? column ( NOT )? IN in_expr -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree] ;
	public final CMISParser.quantified_in_predicate_return quantified_in_predicate() throws RecognitionException {
		CMISParser.quantified_in_predicate_return retval = new CMISParser.quantified_in_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token ANY93=null;
		Token DOT95=null;
		Token NOT97=null;
		Token IN98=null;
		ParserRuleReturnScope qualifier94 =null;
		ParserRuleReturnScope column96 =null;
		ParserRuleReturnScope in_expr99 =null;

		CMISCommonTree ANY93_tree=null;
		CMISCommonTree DOT95_tree=null;
		CMISCommonTree NOT97_tree=null;
		CMISCommonTree IN98_tree=null;
		RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
		RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
		RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
		RewriteRuleSubtreeStream stream_in_expr=new RewriteRuleSubtreeStream(adaptor,"rule in_expr");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:24: ( ANY ( qualifier DOT )? column ( NOT )? IN in_expr -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:26: ANY ( qualifier DOT )? column ( NOT )? IN in_expr
			{
			ANY93=(Token)match(input,ANY,FOLLOW_ANY_in_quantified_in_predicate613); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ANY.add(ANY93);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:30: ( qualifier DOT )?
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0==NAME) ) {
				int LA29_1 = input.LA(2);
				if ( (LA29_1==DOT) ) {
					alt29=1;
				}
			}
			switch (alt29) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:31: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_quantified_in_predicate616);
					qualifier94=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier94.getTree());
					DOT95=(Token)match(input,DOT,FOLLOW_DOT_in_quantified_in_predicate618); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT95);

					}
					break;

			}

			pushFollow(FOLLOW_column_in_quantified_in_predicate622);
			column96=column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_column.add(column96.getTree());
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:54: ( NOT )?
			int alt30=2;
			int LA30_0 = input.LA(1);
			if ( (LA30_0==NOT) ) {
				alt30=1;
			}
			switch (alt30) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:77:54: NOT
					{
					NOT97=(Token)match(input,NOT,FOLLOW_NOT_in_quantified_in_predicate624); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NOT.add(NOT97);

					}
					break;

			}

			IN98=(Token)match(input,IN,FOLLOW_IN_in_quantified_in_predicate627); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IN.add(IN98);

			pushFollow(FOLLOW_in_expr_in_quantified_in_predicate629);
			in_expr99=in_expr();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_in_expr.add(in_expr99.getTree());
			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 77:70: -> FILTER[$qualifier.text, $column.text, $NOT.text, $in_expr.tree]
			{
				adaptor.addChild(root_0, new QuantifiedInFilter(FILTER, (qualifier94!=null?input.toString(qualifier94.start,qualifier94.stop):null), (column96!=null?input.toString(column96.start,column96.stop):null), (NOT97!=null?NOT97.getText():null), (in_expr99!=null?((CMISCommonTree)in_expr99.getTree()):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "quantified_in_predicate"


	public static class text_search_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "text_search_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:79:1: text_search_predicate : CONTAINS LPAREN ( qualifier COMMA )? search_expr RPAREN -> FILTER[$qualifier.text, $search_expr.text] ;
	public final CMISParser.text_search_predicate_return text_search_predicate() throws RecognitionException {
		CMISParser.text_search_predicate_return retval = new CMISParser.text_search_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token CONTAINS100=null;
		Token LPAREN101=null;
		Token COMMA103=null;
		Token RPAREN105=null;
		ParserRuleReturnScope qualifier102 =null;
		ParserRuleReturnScope search_expr104 =null;

		CMISCommonTree CONTAINS100_tree=null;
		CMISCommonTree LPAREN101_tree=null;
		CMISCommonTree COMMA103_tree=null;
		CMISCommonTree RPAREN105_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_CONTAINS=new RewriteRuleTokenStream(adaptor,"token CONTAINS");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_search_expr=new RewriteRuleSubtreeStream(adaptor,"rule search_expr");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:79:22: ( CONTAINS LPAREN ( qualifier COMMA )? search_expr RPAREN -> FILTER[$qualifier.text, $search_expr.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:79:24: CONTAINS LPAREN ( qualifier COMMA )? search_expr RPAREN
			{
			CONTAINS100=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_text_search_predicate644); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_CONTAINS.add(CONTAINS100);

			LPAREN101=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_text_search_predicate646); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN101);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:79:40: ( qualifier COMMA )?
			int alt31=2;
			int LA31_0 = input.LA(1);
			if ( (LA31_0==NAME) ) {
				alt31=1;
			}
			switch (alt31) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:79:41: qualifier COMMA
					{
					pushFollow(FOLLOW_qualifier_in_text_search_predicate649);
					qualifier102=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier102.getTree());
					COMMA103=(Token)match(input,COMMA,FOLLOW_COMMA_in_text_search_predicate651); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COMMA.add(COMMA103);

					}
					break;

			}

			pushFollow(FOLLOW_search_expr_in_text_search_predicate655);
			search_expr104=search_expr();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_search_expr.add(search_expr104.getTree());
			RPAREN105=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_text_search_predicate657); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN105);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 79:78: -> FILTER[$qualifier.text, $search_expr.text]
			{
				adaptor.addChild(root_0, new TextSearchFilter(FILTER, (qualifier102!=null?input.toString(qualifier102.start,qualifier102.stop):null), (search_expr104!=null?input.toString(search_expr104.start,search_expr104.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "text_search_predicate"


	public static class folder_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "folder_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:80:1: folder_predicate : in_folder LPAREN ( qualifier COMMA )? folder_id RPAREN -> FILTER[$in_folder.text, $qualifier.text,$folder_id.text] ;
	public final CMISParser.folder_predicate_return folder_predicate() throws RecognitionException {
		CMISParser.folder_predicate_return retval = new CMISParser.folder_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token LPAREN107=null;
		Token COMMA109=null;
		Token RPAREN111=null;
		ParserRuleReturnScope in_folder106 =null;
		ParserRuleReturnScope qualifier108 =null;
		ParserRuleReturnScope folder_id110 =null;

		CMISCommonTree LPAREN107_tree=null;
		CMISCommonTree COMMA109_tree=null;
		CMISCommonTree RPAREN111_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_folder_id=new RewriteRuleSubtreeStream(adaptor,"rule folder_id");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
		RewriteRuleSubtreeStream stream_in_folder=new RewriteRuleSubtreeStream(adaptor,"rule in_folder");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:80:17: ( in_folder LPAREN ( qualifier COMMA )? folder_id RPAREN -> FILTER[$in_folder.text, $qualifier.text,$folder_id.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:80:19: in_folder LPAREN ( qualifier COMMA )? folder_id RPAREN
			{
			pushFollow(FOLLOW_in_folder_in_folder_predicate671);
			in_folder106=in_folder();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_in_folder.add(in_folder106.getTree());
			LPAREN107=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_folder_predicate673); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN107);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:80:36: ( qualifier COMMA )?
			int alt32=2;
			int LA32_0 = input.LA(1);
			if ( (LA32_0==NAME) ) {
				alt32=1;
			}
			switch (alt32) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:80:37: qualifier COMMA
					{
					pushFollow(FOLLOW_qualifier_in_folder_predicate676);
					qualifier108=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier108.getTree());
					COMMA109=(Token)match(input,COMMA,FOLLOW_COMMA_in_folder_predicate678); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COMMA.add(COMMA109);

					}
					break;

			}

			pushFollow(FOLLOW_folder_id_in_folder_predicate682);
			folder_id110=folder_id();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_folder_id.add(folder_id110.getTree());
			RPAREN111=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_folder_predicate684); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN111);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 80:72: -> FILTER[$in_folder.text, $qualifier.text,$folder_id.text]
			{
				adaptor.addChild(root_0, new FolderFilter(FILTER, (in_folder106!=null?input.toString(in_folder106.start,in_folder106.stop):null), (qualifier108!=null?input.toString(qualifier108.start,qualifier108.stop):null), (folder_id110!=null?input.toString(folder_id110.start,folder_id110.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "folder_predicate"


	public static class tree_predicate_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "tree_predicate"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:81:1: tree_predicate : in_tree LPAREN ( qualifier COMMA )? folder_id RPAREN -> FILTER[$in_tree.text, $qualifier.text,$folder_id.text] ;
	public final CMISParser.tree_predicate_return tree_predicate() throws RecognitionException {
		CMISParser.tree_predicate_return retval = new CMISParser.tree_predicate_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token LPAREN113=null;
		Token COMMA115=null;
		Token RPAREN117=null;
		ParserRuleReturnScope in_tree112 =null;
		ParserRuleReturnScope qualifier114 =null;
		ParserRuleReturnScope folder_id116 =null;

		CMISCommonTree LPAREN113_tree=null;
		CMISCommonTree COMMA115_tree=null;
		CMISCommonTree RPAREN117_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_folder_id=new RewriteRuleSubtreeStream(adaptor,"rule folder_id");
		RewriteRuleSubtreeStream stream_in_tree=new RewriteRuleSubtreeStream(adaptor,"rule in_tree");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:81:15: ( in_tree LPAREN ( qualifier COMMA )? folder_id RPAREN -> FILTER[$in_tree.text, $qualifier.text,$folder_id.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:81:17: in_tree LPAREN ( qualifier COMMA )? folder_id RPAREN
			{
			pushFollow(FOLLOW_in_tree_in_tree_predicate698);
			in_tree112=in_tree();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_in_tree.add(in_tree112.getTree());
			LPAREN113=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tree_predicate700); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN113);

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:81:32: ( qualifier COMMA )?
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==NAME) ) {
				alt33=1;
			}
			switch (alt33) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:81:33: qualifier COMMA
					{
					pushFollow(FOLLOW_qualifier_in_tree_predicate703);
					qualifier114=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier114.getTree());
					COMMA115=(Token)match(input,COMMA,FOLLOW_COMMA_in_tree_predicate705); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COMMA.add(COMMA115);

					}
					break;

			}

			pushFollow(FOLLOW_folder_id_in_tree_predicate709);
			folder_id116=folder_id();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_folder_id.add(folder_id116.getTree());
			RPAREN117=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tree_predicate711); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN117);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 81:68: -> FILTER[$in_tree.text, $qualifier.text,$folder_id.text]
			{
				adaptor.addChild(root_0, new TreeFilter(FILTER, (in_tree112!=null?input.toString(in_tree112.start,in_tree112.stop):null), (qualifier114!=null?input.toString(qualifier114.start,qualifier114.stop):null), (folder_id116!=null?input.toString(folder_id116.start,folder_id116.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "tree_predicate"


	public static class in_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "in_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:1: in_expr : LPAREN ! ( CONST ( COMMA ! CONST )* ) RPAREN !;
	public final CMISParser.in_expr_return in_expr() throws RecognitionException {
		CMISParser.in_expr_return retval = new CMISParser.in_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token LPAREN118=null;
		Token CONST119=null;
		Token COMMA120=null;
		Token CONST121=null;
		Token RPAREN122=null;

		CMISCommonTree LPAREN118_tree=null;
		CMISCommonTree CONST119_tree=null;
		CMISCommonTree COMMA120_tree=null;
		CMISCommonTree CONST121_tree=null;
		CMISCommonTree RPAREN122_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:8: ( LPAREN ! ( CONST ( COMMA ! CONST )* ) RPAREN !)
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:10: LPAREN ! ( CONST ( COMMA ! CONST )* ) RPAREN !
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			LPAREN118=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_in_expr726); if (state.failed) return retval;
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:18: ( CONST ( COMMA ! CONST )* )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:19: CONST ( COMMA ! CONST )*
			{
			CONST119=(Token)match(input,CONST,FOLLOW_CONST_in_in_expr730); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONST119_tree = (CMISCommonTree)adaptor.create(CONST119);
			adaptor.addChild(root_0, CONST119_tree);
			}

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:25: ( COMMA ! CONST )*
			loop34:
			while (true) {
				int alt34=2;
				int LA34_0 = input.LA(1);
				if ( (LA34_0==COMMA) ) {
					alt34=1;
				}

				switch (alt34) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:83:26: COMMA ! CONST
					{
					COMMA120=(Token)match(input,COMMA,FOLLOW_COMMA_in_in_expr733); if (state.failed) return retval;
					CONST121=(Token)match(input,CONST,FOLLOW_CONST_in_in_expr736); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CONST121_tree = (CMISCommonTree)adaptor.create(CONST121);
					adaptor.addChild(root_0, CONST121_tree);
					}

					}
					break;

				default :
					break loop34;
				}
			}

			}

			RPAREN122=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_in_expr741); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_expr"


	public static class op_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "op"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:84:1: op : ( EQUALS | BINARY_OPERATOR ) ;
	public final CMISParser.op_return op() throws RecognitionException {
		CMISParser.op_return retval = new CMISParser.op_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token set123=null;

		CMISCommonTree set123_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:84:3: ( ( EQUALS | BINARY_OPERATOR ) )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			set123=input.LT(1);
			if ( input.LA(1)==BINARY_OPERATOR||input.LA(1)==EQUALS ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CMISCommonTree)adaptor.create(set123));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "op"


	public static class value_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "value"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:85:1: value : CONST ;
	public final CMISParser.value_return value() throws RecognitionException {
		CMISParser.value_return retval = new CMISParser.value_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token CONST124=null;

		CMISCommonTree CONST124_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:85:6: ( CONST )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:85:8: CONST
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			CONST124=(Token)match(input,CONST,FOLLOW_CONST_in_value758); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONST124_tree = (CMISCommonTree)adaptor.create(CONST124);
			adaptor.addChild(root_0, CONST124_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "value"


	public static class in_folder_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "in_folder"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:86:1: in_folder : IN_FOLDER ;
	public final CMISParser.in_folder_return in_folder() throws RecognitionException {
		CMISParser.in_folder_return retval = new CMISParser.in_folder_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token IN_FOLDER125=null;

		CMISCommonTree IN_FOLDER125_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:86:10: ( IN_FOLDER )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:86:12: IN_FOLDER
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			IN_FOLDER125=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_in_folder764); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IN_FOLDER125_tree = (CMISCommonTree)adaptor.create(IN_FOLDER125);
			adaptor.addChild(root_0, IN_FOLDER125_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_folder"


	public static class in_tree_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "in_tree"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:87:1: in_tree : IN_TREE ;
	public final CMISParser.in_tree_return in_tree() throws RecognitionException {
		CMISParser.in_tree_return retval = new CMISParser.in_tree_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token IN_TREE126=null;

		CMISCommonTree IN_TREE126_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:87:8: ( IN_TREE )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:87:10: IN_TREE
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			IN_TREE126=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_in_tree770); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IN_TREE126_tree = (CMISCommonTree)adaptor.create(IN_TREE126);
			adaptor.addChild(root_0, IN_TREE126_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_tree"


	public static class folder_id_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "folder_id"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:88:1: folder_id : CONST ;
	public final CMISParser.folder_id_return folder_id() throws RecognitionException {
		CMISParser.folder_id_return retval = new CMISParser.folder_id_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token CONST127=null;

		CMISCommonTree CONST127_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:88:10: ( CONST )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:88:12: CONST
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			CONST127=(Token)match(input,CONST,FOLLOW_CONST_in_folder_id776); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONST127_tree = (CMISCommonTree)adaptor.create(CONST127);
			adaptor.addChild(root_0, CONST127_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "folder_id"


	public static class search_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "search_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:89:1: search_expr : CONST ;
	public final CMISParser.search_expr_return search_expr() throws RecognitionException {
		CMISParser.search_expr_return retval = new CMISParser.search_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token CONST128=null;

		CMISCommonTree CONST128_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:89:12: ( CONST )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:89:14: CONST
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			CONST128=(Token)match(input,CONST,FOLLOW_CONST_in_search_expr782); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONST128_tree = (CMISCommonTree)adaptor.create(CONST128);
			adaptor.addChild(root_0, CONST128_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "search_expr"


	public static class asc_desc_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "asc_desc"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:90:1: asc_desc : ( ASC | DESC ) ;
	public final CMISParser.asc_desc_return asc_desc() throws RecognitionException {
		CMISParser.asc_desc_return retval = new CMISParser.asc_desc_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token set129=null;

		CMISCommonTree set129_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:90:9: ( ( ASC | DESC ) )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			set129=input.LT(1);
			if ( input.LA(1)==ASC||input.LA(1)==DESC ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CMISCommonTree)adaptor.create(set129));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "asc_desc"


	public static class order_by_column_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "order_by_column"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:1: order_by_column : ( column | 'SEARCH_SCORE' ) ;
	public final CMISParser.order_by_column_return order_by_column() throws RecognitionException {
		CMISParser.order_by_column_return retval = new CMISParser.order_by_column_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token string_literal131=null;
		ParserRuleReturnScope column130 =null;

		CMISCommonTree string_literal131_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:16: ( ( column | 'SEARCH_SCORE' ) )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:18: ( column | 'SEARCH_SCORE' )
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:18: ( column | 'SEARCH_SCORE' )
			int alt35=2;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==NAME) ) {
				alt35=1;
			}
			else if ( (LA35_0==46) ) {
				alt35=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 35, 0, input);
				throw nvae;
			}

			switch (alt35) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:19: column
					{
					pushFollow(FOLLOW_column_in_order_by_column799);
					column130=column();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, column130.getTree());

					}
					break;
				case 2 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:91:26: 'SEARCH_SCORE'
					{
					string_literal131=(Token)match(input,46,FOLLOW_46_in_order_by_column801); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal131_tree = (CMISCommonTree)adaptor.create(string_literal131);
					adaptor.addChild(root_0, string_literal131_tree);
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "order_by_column"


	public static class order_expr_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "order_expr"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:1: order_expr : ( qualifier DOT )? order_by_column ( asc_desc )? -> ORDER[$qualifier.text, $order_by_column.text,$asc_desc.text] ;
	public final CMISParser.order_expr_return order_expr() throws RecognitionException {
		CMISParser.order_expr_return retval = new CMISParser.order_expr_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token DOT133=null;
		ParserRuleReturnScope qualifier132 =null;
		ParserRuleReturnScope order_by_column134 =null;
		ParserRuleReturnScope asc_desc135 =null;

		CMISCommonTree DOT133_tree=null;
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_order_by_column=new RewriteRuleSubtreeStream(adaptor,"rule order_by_column");
		RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
		RewriteRuleSubtreeStream stream_asc_desc=new RewriteRuleSubtreeStream(adaptor,"rule asc_desc");

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:11: ( ( qualifier DOT )? order_by_column ( asc_desc )? -> ORDER[$qualifier.text, $order_by_column.text,$asc_desc.text] )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:13: ( qualifier DOT )? order_by_column ( asc_desc )?
			{
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:13: ( qualifier DOT )?
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0==NAME) ) {
				int LA36_1 = input.LA(2);
				if ( (LA36_1==DOT) ) {
					alt36=1;
				}
			}
			switch (alt36) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:14: qualifier DOT
					{
					pushFollow(FOLLOW_qualifier_in_order_expr810);
					qualifier132=qualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_qualifier.add(qualifier132.getTree());
					DOT133=(Token)match(input,DOT,FOLLOW_DOT_in_order_expr812); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT133);

					}
					break;

			}

			pushFollow(FOLLOW_order_by_column_in_order_expr816);
			order_by_column134=order_by_column();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_order_by_column.add(order_by_column134.getTree());
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:46: ( asc_desc )?
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==ASC||LA37_0==DESC) ) {
				alt37=1;
			}
			switch (alt37) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:93:46: asc_desc
					{
					pushFollow(FOLLOW_asc_desc_in_order_expr818);
					asc_desc135=asc_desc();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_asc_desc.add(asc_desc135.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CMISCommonTree)adaptor.nil();
			// 93:56: -> ORDER[$qualifier.text, $order_by_column.text,$asc_desc.text]
			{
				adaptor.addChild(root_0, new Order(ORDER, (qualifier132!=null?input.toString(qualifier132.start,qualifier132.stop):null), (order_by_column134!=null?input.toString(order_by_column134.start,order_by_column134.stop):null), (asc_desc135!=null?input.toString(asc_desc135.start,asc_desc135.stop):null)));
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "order_expr"


	public static class order_by_clause_return extends ParserRuleReturnScope {
		CMISCommonTree tree;
		@Override
		public CMISCommonTree getTree() { return tree; }
	};


	// $ANTLR start "order_by_clause"
	// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:94:1: order_by_clause : order_expr ( COMMA ! order_expr )* ;
	public final CMISParser.order_by_clause_return order_by_clause() throws RecognitionException {
		CMISParser.order_by_clause_return retval = new CMISParser.order_by_clause_return();
		retval.start = input.LT(1);

		CMISCommonTree root_0 = null;

		Token COMMA137=null;
		ParserRuleReturnScope order_expr136 =null;
		ParserRuleReturnScope order_expr138 =null;

		CMISCommonTree COMMA137_tree=null;

		try {
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:94:16: ( order_expr ( COMMA ! order_expr )* )
			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:94:18: order_expr ( COMMA ! order_expr )*
			{
			root_0 = (CMISCommonTree)adaptor.nil();


			pushFollow(FOLLOW_order_expr_in_order_by_clause833);
			order_expr136=order_expr();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, order_expr136.getTree());

			// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:94:29: ( COMMA ! order_expr )*
			loop38:
			while (true) {
				int alt38=2;
				int LA38_0 = input.LA(1);
				if ( (LA38_0==COMMA) ) {
					alt38=1;
				}

				switch (alt38) {
				case 1 :
					// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:94:30: COMMA ! order_expr
					{
					COMMA137=(Token)match(input,COMMA,FOLLOW_COMMA_in_order_by_clause836); if (state.failed) return retval;
					pushFollow(FOLLOW_order_expr_in_order_by_clause839);
					order_expr138=order_expr();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, order_expr138.getTree());

					}
					break;

				default :
					break loop38;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CMISCommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CMISCommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "order_by_clause"

	// $ANTLR start synpred1_CMIS
	public final void synpred1_CMIS_fragment() throws RecognitionException {
		// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:37: ( value EQUALS ANY )
		// C:\\dev\\git\\repo\\src\\main\\java\\eu\\trade\\repo\\query\\CMIS.g:65:38: value EQUALS ANY
		{
		pushFollow(FOLLOW_value_in_synpred1_CMIS425);
		value();
		state._fsp--;
		if (state.failed) return;

		match(input,EQUALS,FOLLOW_EQUALS_in_synpred1_CMIS427); if (state.failed) return;

		match(input,ANY,FOLLOW_ANY_in_synpred1_CMIS429); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_CMIS

	// Delegated rules

	public final boolean synpred1_CMIS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_CMIS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



	public static final BitSet FOLLOW_SELECT_in_cmis_query87 = new BitSet(new long[]{0x000000A010000000L});
	public static final BitSet FOLLOW_columns_in_cmis_query90 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_FROM_in_cmis_query92 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_tables_in_cmis_query95 = new BitSet(new long[]{0x0000100400000000L});
	public static final BitSet FOLLOW_WHERE_in_cmis_query98 = new BitSet(new long[]{0x0000000039803020L});
	public static final BitSet FOLLOW_where_clause_in_cmis_query101 = new BitSet(new long[]{0x0000000400000000L});
	public static final BitSet FOLLOW_ORDER_BY_in_cmis_query106 = new BitSet(new long[]{0x0000400010000000L});
	public static final BitSet FOLLOW_order_by_clause_in_cmis_query109 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_cmis_query113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_columns_in_columns121 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_expr_in_columns133 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_COMMA_in_columns136 = new BitSet(new long[]{0x0000002010000000L});
	public static final BitSet FOLLOW_column_expr_in_columns139 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_SCORE_in_column_expr151 = new BitSet(new long[]{0x0000000010000042L});
	public static final BitSet FOLLOW_AS_in_column_expr154 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_alias_in_column_expr157 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_column_expr175 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_column_expr177 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_column_expr181 = new BitSet(new long[]{0x0000000010000042L});
	public static final BitSet FOLLOW_AS_in_column_expr184 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_alias_in_column_expr187 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_column_expr204 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_column_expr206 = new BitSet(new long[]{0x0000008000000000L});
	public static final BitSet FOLLOW_all_columns_in_column_expr208 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAME_in_qualifier225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAME_in_column231 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAME_in_alias240 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAR_in_all_columns246 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_expr_in_tables253 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_in_table_expr260 = new BitSet(new long[]{0x0000000010000042L});
	public static final BitSet FOLLOW_AS_in_table_expr263 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_alias_in_table_expr266 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAME_in_table282 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_where_clause292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_andexpression_in_expression300 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_orexpression_in_andexpression307 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_AND_in_andexpression310 = new BitSet(new long[]{0x0000000039803020L});
	public static final BitSet FOLLOW_orexpression_in_andexpression313 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_notexpression_in_orexpression322 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_OR_in_orexpression325 = new BitSet(new long[]{0x0000000039803020L});
	public static final BitSet FOLLOW_notexpression_in_orexpression328 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_NOT_in_notexpression337 = new BitSet(new long[]{0x0000000019803020L});
	public static final BitSet FOLLOW_atom_in_notexpression340 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_notexpression344 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_expr_in_atom351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_atom356 = new BitSet(new long[]{0x0000000039803020L});
	public static final BitSet FOLLOW_andexpression_in_atom359 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_RPAREN_in_atom361 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_comparison_or_quantified_predicate_in_boolean_expr372 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_quantified_in_predicate_in_boolean_expr378 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_in_predicate_in_boolean_expr384 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_like_predicate_in_boolean_expr390 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_null_predicate_in_boolean_expr396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_text_search_predicate_in_boolean_expr402 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_folder_predicate_in_boolean_expr408 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tree_predicate_in_boolean_expr414 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_quantified_predicate_in_comparison_or_quantified_predicate433 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_comparison_predicate_in_comparison_or_quantified_predicate437 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_comparison_predicate448 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_comparison_predicate450 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_comparison_predicate454 = new BitSet(new long[]{0x0000000000010100L});
	public static final BitSet FOLLOW_op_in_comparison_predicate456 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_value_in_comparison_predicate458 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_value_in_comparison_predicate472 = new BitSet(new long[]{0x0000000000010100L});
	public static final BitSet FOLLOW_op_in_comparison_predicate474 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_qualifier_in_comparison_predicate477 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_comparison_predicate479 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_comparison_predicate483 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_in_predicate499 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_in_predicate501 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_in_predicate505 = new BitSet(new long[]{0x0000000020200000L});
	public static final BitSet FOLLOW_NOT_in_in_predicate507 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_IN_in_in_predicate510 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_in_expr_in_in_predicate512 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_like_predicate527 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_like_predicate529 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_like_predicate533 = new BitSet(new long[]{0x0000000024000000L});
	public static final BitSet FOLLOW_NOT_in_like_predicate535 = new BitSet(new long[]{0x0000000004000000L});
	public static final BitSet FOLLOW_LIKE_in_like_predicate538 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_value_in_like_predicate540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_null_predicate555 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_null_predicate557 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_null_predicate561 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_IS_in_null_predicate563 = new BitSet(new long[]{0x0000000060000000L});
	public static final BitSet FOLLOW_NOT_in_null_predicate565 = new BitSet(new long[]{0x0000000040000000L});
	public static final BitSet FOLLOW_NULL_in_null_predicate568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_value_in_quantified_predicate583 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_EQUALS_in_quantified_predicate585 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ANY_in_quantified_predicate587 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_qualifier_in_quantified_predicate590 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_quantified_predicate592 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_quantified_predicate596 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ANY_in_quantified_in_predicate613 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_qualifier_in_quantified_in_predicate616 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_quantified_in_predicate618 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_column_in_quantified_in_predicate622 = new BitSet(new long[]{0x0000000020200000L});
	public static final BitSet FOLLOW_NOT_in_quantified_in_predicate624 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_IN_in_quantified_in_predicate627 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_in_expr_in_quantified_in_predicate629 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONTAINS_in_text_search_predicate644 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_text_search_predicate646 = new BitSet(new long[]{0x0000000010001000L});
	public static final BitSet FOLLOW_qualifier_in_text_search_predicate649 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_COMMA_in_text_search_predicate651 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_search_expr_in_text_search_predicate655 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_RPAREN_in_text_search_predicate657 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_in_folder_in_folder_predicate671 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_folder_predicate673 = new BitSet(new long[]{0x0000000010001000L});
	public static final BitSet FOLLOW_qualifier_in_folder_predicate676 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_COMMA_in_folder_predicate678 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_folder_id_in_folder_predicate682 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_RPAREN_in_folder_predicate684 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_in_tree_in_tree_predicate698 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_tree_predicate700 = new BitSet(new long[]{0x0000000010001000L});
	public static final BitSet FOLLOW_qualifier_in_tree_predicate703 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_COMMA_in_tree_predicate705 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_folder_id_in_tree_predicate709 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_RPAREN_in_tree_predicate711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_in_expr726 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_CONST_in_in_expr730 = new BitSet(new long[]{0x0000001000000800L});
	public static final BitSet FOLLOW_COMMA_in_in_expr733 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_CONST_in_in_expr736 = new BitSet(new long[]{0x0000001000000800L});
	public static final BitSet FOLLOW_RPAREN_in_in_expr741 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONST_in_value758 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IN_FOLDER_in_in_folder764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IN_TREE_in_in_tree770 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONST_in_folder_id776 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONST_in_search_expr782 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_in_order_by_column799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_46_in_order_by_column801 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_order_expr810 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DOT_in_order_expr812 = new BitSet(new long[]{0x0000400010000000L});
	public static final BitSet FOLLOW_order_by_column_in_order_expr816 = new BitSet(new long[]{0x0000000000004082L});
	public static final BitSet FOLLOW_asc_desc_in_order_expr818 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_order_expr_in_order_by_clause833 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_COMMA_in_order_by_clause836 = new BitSet(new long[]{0x0000400010000000L});
	public static final BitSet FOLLOW_order_expr_in_order_by_clause839 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_value_in_synpred1_CMIS425 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_EQUALS_in_synpred1_CMIS427 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ANY_in_synpred1_CMIS429 = new BitSet(new long[]{0x0000000000000002L});
}
