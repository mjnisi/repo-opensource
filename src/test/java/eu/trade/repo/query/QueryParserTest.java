package eu.trade.repo.query;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.trade.repo.query.ast.CMISCommonErrorNode;
import eu.trade.repo.query.ast.CMISCommonTree;

public class QueryParserTest {
    
	@Test
	public void testQuery01() throws RecognitionException {
		QueryResult r = runQuery(
			"select * from cmis:document where (cmis:id = 554 and c = '5') or (e=3 and g='33')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        Assert.assertEquals(r.parser.getColumns().size(), 4);
     }
    
	
	@Test
	public void testQuery02() throws RecognitionException {
		QueryResult r = runQuery(
			"select cmis:name, cmis:test from cmis:document " +
			"where ('1' = cmis:id and cmis:id = 7) " +
			"or (cmis:date <> 'd' and cmis:author='z') " +
			"order by cmis:date");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        Assert.assertEquals(r.parser.getColumns().size(), 5);
    
	}
    
	
	@Test
	public void testQuery03() throws RecognitionException {
		QueryResult r = runQuery(
			"select cmis:name a, cmis:test b " +
			"from cmis:document d " +
			"where ('1' = d.cmis:id and d.cmis:id = 7) " +
			"or (x.cmis:date <> 'd' and x.cmis:author='z') " +
			"order by cmis:date");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
	}
	
	@Test
	public void testQueryColumns01() throws RecognitionException {
		QueryResult r = runQuery("select " +
				"column1," +
				"column2 as alias2," +
				"column3 alias3, " +
				"table1.column4, " +
				"table1.*, " +
				"table1.column5 as alias5, " +
				"score()," +
				"score() as scoreAsAlias," +
				"score() scoreAlias  " +
				"from cmis:document");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        Assert.assertEquals(r.parser.getColumns().size(), 5);
    
	}
	
	@Test
	public void testQueryColumns02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryIn01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document a " +
				"where a.cmis:name in ('a','b','c') " +
				"or cmis:name not in (2,3,4)");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryLike01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:author like 'martjoe' " +
				"or cmis:author not like '0928727' ");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryLike02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:author like 'MARTJOE' " +
				"or cmis:document.cmis:author like 'martjoe'");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryNull01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:id is null " +
				"or cmis:author is not null");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryAny01() throws RecognitionException {
		
		QueryResult r = runQuery("select * from cmis:document where 'CA' = any cmis:country");
		
		r = runQuery("select * from cmis:document where any cmis:name in ('Allen')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
		
		r = runQuery("select * from cmis:document where cmis:country = 'a string'");
		r = runQuery("select * from cmis:document where cmis:country = 5");
		
		r = runQuery("select * from cmis:document where 5 = cmis:country");
		
		r = runQuery("select * from cmis:document where 'a string' = cmis:country");
	}
	
	@Test
	public void testQueryAny02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where any cmis:country in ('CA','US')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryAny03() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where any cmis:country not in ('CA','US')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
    
	}
	
	@Test
	public void testQueryContains01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where contains('china')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}

	@Test
	public void testQueryContains02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where contains(cmis:document, 'china')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}

	
	@Test
	public void testQueryFolder01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where in_tree('1234567890') or in_folder('0987654321')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}

	@Test
	public void testQueryFolder02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where in_tree(cmis:document, '1234567')");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}

	@Test
	public void testQueryTimestamp02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:creationDate > TIMESTAMP '2013-12-31T23:45:22.123' ");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}
	
	@Test
	public void testQueryDecimal01() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:n = 1.2 and cmis:n = 15");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}

	@Test
	public void testQueryDecimal02() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:n = 1.2 and cmis:n = 15E-1");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}
	
	@Test
	public void testQueryDecimal03() throws RecognitionException {
		QueryResult r = runQuery("select * from cmis:document " +
				"where cmis:n = 1.2 and cmis:n = 15E+12");
				
        Assert.assertEquals(r.parser.getErrors().size(), 0);
        Assert.assertEquals(r.lexer.getErrors().size(), 0);
        Assert.assertEquals(r.parser.getTables().size(), 1);
        
	}
	
	/*--------------------------------------------------------------------------
	 * query methods
	 -------------------------------------------------------------------------*/

	private QueryResult runQuery(String query) throws RecognitionException {
		CMISLexer lex = new CMISLexer(new ANTLRStringStream(query));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        CMISParser parser = new CMISParser(tokens);
        parser.setTreeAdaptor(new CommonTreeAdaptor() {
    		@Override
    		public Object create(Token payload) {
    			return new CMISCommonTree(payload);
    		}

    		@Override
    		public Object dupNode(Object old) {
    			return (old == null) ? null : ((CMISCommonTree) old).dupNode();
    		}

    		@Override
    		public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
    			return new CMISCommonErrorNode(input, start, stop, e);
    		}
    	});
        
        CMISParser.cmis_query_return r = parser.cmis_query();
        
        CommonTree ast = r.getTree();
        
        log.debug(printTree(ast));
        log.debug(printTreeInline(ast));
        log.info("syntax errors: " + parser.getNumberOfSyntaxErrors());
        
        log.info("query: " + query);
        log.info("parser errors: " + parser.getErrors());
        log.info("lexer errors: " + lex.getErrors());
        log.info("tables: " + parser.getTables());
        log.info("columns: " + parser.getColumns());
        
        
        
        QueryResult result = new  QueryResult();
        result.lexer = lex;
        result.parser = parser;
        result.ast = ast;
        
        return result;
	}
	
	private class QueryResult {
		public CMISLexer lexer;
		public CMISParser parser;
		public CommonTree ast;
	}
	
	/*--------------------------------------------------------------------------
	 * helper methods
	 -------------------------------------------------------------------------*/
	
	private static final Log log = LogFactory.getLog(QueryParserTest.class);
	
    private static String printTreeInline(CommonTree tree) {
		StringBuffer sb = new StringBuffer();
    	
    	if(tree.getChildCount() > 1) {
			sb.append("("); 
		}
		
    	sb.append(tree.getText() + " ");

		//print all children
		if (tree.getChildren() != null)
			for (java.lang.Object ie : tree.getChildren()) {
				sb.append(printTreeInline((CommonTree) ie));
			}
 	
		if(tree.getChildCount() > 1) {
			sb.append(") ");
		}
		
		return sb.toString();
    }
    
    private static String printTree(CommonTree ast) {
		return print(ast, 0);
	}

	private static String print(CommonTree tree, int level) {
		
		String t = (tree.getType() != -1) ? 
				CMISParser.tokenNames[tree.getType()] : "";
				
				
		//indent level
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append("-");
		}

		//print node description: type code followed by token text
		sb.append(sb + " " + t + " " + tree.getText() + "\n");

		//print all children
		if (tree.getChildren() != null) {
			for (java.lang.Object ie : tree.getChildren()) {
				sb.append(print((CommonTree) ie, level + 1));
			}
			
		}
		
		return sb.toString();
	}

}