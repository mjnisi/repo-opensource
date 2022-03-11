grammar CMIS;

options {
  language = Java;
  output = AST;
  ASTLabelType = CMISCommonTree;
  //backtrack=true;
}

tokens {
  COLUMN;
  TABLE;
  FILTER;
  ORDER;
}

@parser::header {
package eu.trade.repo.query;
import eu.trade.repo.query.ast.*;
import eu.trade.repo.query.ast.TreeFilter;
}

@lexer::header {
package eu.trade.repo.query;
import eu.trade.repo.query.ast.*;
}

cmis_query: SELECT! columns FROM! tables (WHERE! where_clause)? (ORDER_BY! order_by_clause)? EOF!;

columns: all_columns -> COLUMN<Column>["*", null, null, null] | column_expr (COMMA! column_expr)* ;
column_expr: 
  SCORE (AS? alias)? -> COLUMN<Column>[null, null, $alias.text, "score"] | 
  (qualifier DOT)? column (AS? alias)? -> COLUMN<Column>[$column.text, $qualifier.text, $alias.text, null] | 
  qualifier DOT all_columns -> COLUMN<Column>["*", $qualifier.text, null, null];
  
qualifier: NAME;
column: NAME {columns.add($column.text);}; 
alias: NAME;
all_columns: STAR;

tables: table_expr;
//tables: table_expr (COMMA table_expr)*;
table_expr: table (AS? alias)? -> TABLE<Table>[$table.text, $alias.text];
table: NAME {tables.add($table.text);};


where_clause: expression;

expression : andexpression;
andexpression : orexpression (AND^ orexpression)*;
orexpression : notexpression (OR^ notexpression)*;
notexpression : NOT^ atom | atom;
atom : boolean_expr | (LPAREN! andexpression RPAREN!);
boolean_expr: 
  comparison_or_quantified_predicate |
  quantified_in_predicate |
  in_predicate |
  like_predicate |
  null_predicate |
  text_search_predicate |
  folder_predicate |
  tree_predicate
  ;

comparison_or_quantified_predicate: (value EQUALS ANY)=> quantified_predicate | comparison_predicate;

comparison_predicate: 
  (qualifier DOT)? column op value -> FILTER<ComparisonFilter>[$qualifier.text,$column.text,$op.text,$value.text] |
  value op (qualifier DOT)? column -> FILTER<ComparisonFilter>[$qualifier.text,$column.text,$op.text,$value.text];

in_predicate: (qualifier DOT)? column NOT? IN in_expr -> FILTER<InFilter>[$qualifier.text, $column.text, $NOT.text, $in_expr.tree];
like_predicate: (qualifier DOT)? column NOT? LIKE value -> FILTER<LikeFilter>[$qualifier.text, $column.text, $NOT.text, $value.text];
null_predicate: (qualifier DOT)? column IS NOT? NULL -> FILTER<NullFilter>[$qualifier.text, $column.text, $NOT.text];

quantified_predicate: value EQUALS ANY (qualifier DOT)? column -> FILTER<QuantifiedComparisonFilter>[$qualifier.text, $column.text, $value.text];
  
quantified_in_predicate: ANY (qualifier DOT)? column NOT? IN in_expr -> FILTER<QuantifiedInFilter>[$qualifier.text, $column.text, $NOT.text, $in_expr.tree];

text_search_predicate: CONTAINS LPAREN (qualifier COMMA)? search_expr RPAREN -> FILTER<TextSearchFilter>[$qualifier.text, $search_expr.text];
folder_predicate: in_folder LPAREN (qualifier COMMA)? folder_id RPAREN -> FILTER<FolderFilter>[$in_folder.text, $qualifier.text,$folder_id.text];
tree_predicate: in_tree LPAREN (qualifier COMMA)? folder_id RPAREN -> FILTER<TreeFilter>[$in_tree.text, $qualifier.text,$folder_id.text];

in_expr: LPAREN! (CONST (COMMA! CONST)*) RPAREN!;
op: (EQUALS|BINARY_OPERATOR);
value: CONST;
in_folder: IN_FOLDER;
in_tree: IN_TREE;
folder_id: CONST;//TODO to change by ID
search_expr: CONST;//TODO text search
asc_desc: (ASC|DESC);
order_by_column: (column|'SEARCH_SCORE');

order_expr: (qualifier DOT)? order_by_column asc_desc? -> ORDER<Order>[$qualifier.text, $order_by_column.text,$asc_desc.text];
order_by_clause: order_expr (COMMA! order_expr)*;
  
SELECT: 'SELECT'|'select';
FROM: 'FROM'|'from';
WHERE: 'WHERE'|'where';
ORDER_BY: 'ORDER' WS+ 'BY'|'order' WS+ 'by';
ASC: 'ASC'|'asc';
DESC: 'DESC'|'desc';
AND: 'AND'|'and';
OR: 'OR'|'or'; 
NOT: 'NOT'|'not';
IN: 'IN'|'in';
IS: 'IS'|'is';
NULL: 'NULL'|'null';
EQUALS: '=';
ANY: 'ANY'|'any';
LIKE: 'LIKE'|'like';
AS: 'AS'|'as';
CONTAINS: 'CONTAINS'|'contains';
IN_FOLDER: 'IN_FOLDER'|'in_folder';
IN_TREE: 'IN_TREE'|'in_tree';
SCORE: 'SCORE()'|'score()';
TIMESTAMP: 'TIMESTAMP' | 'timestamp';
LPAREN: '(';
RPAREN: ')';
QUOTE: '\'';
STAR: '*';
DOT: '.';
COMMA: ',';
//ID: HEX_DIGIT+;
CONST: STRING | INTEGER | BOOLEAN | TIMESTAMP WS STRING;
fragment BOOLEAN: 'true' | 'false' | 'TRUE' | 'FALSE';
NAME: ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|':')*;

//CHAR:'\''(ESC_SEQ | ~('\''|'\\') ) '\'';
fragment STRING: QUOTE (ESC_SEQ | ~('\\'|QUOTE) )* QUOTE;

//fragment TIME: TIMESTAMP STRING;

fragment HEX_DIGIT: ('0'..'9'|'a'..'f'|'A'..'F') ;
fragment ESC_SEQ: '\\' ('b'|'t'|'n'|'f'|'r'|'\''|'\\'|'%'|'_') | UNICODE_ESC | OCTAL_ESC;
fragment OCTAL_ESC: '\\' ('0'..'3') ('0'..'7') ('0'..'7') |   '\\' ('0'..'7') ('0'..'7') | '\\' ('0'..'7');
fragment UNICODE_ESC: '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT;

//fragment ALPHA: ('a'..'z'|'A'..'Z'|'0'..'9')+;
fragment INTEGER: ('-'|'+')?('0'..'9'|'.')+ ('E'('+'|'-')('0'..'9')+ )?;

BINARY_OPERATOR: ('<>'|'<'|'>'|'<='|'>=');

WS: (' '|'\t'|'\r'|'\n')+ { $channel = HIDDEN; };