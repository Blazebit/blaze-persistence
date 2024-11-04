/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
parser grammar SQLParser;

options {
    tokenVocab=SQLLexer;
}

parseSelectStatement
    : select_statement EOF
    ;

parseFrom
    : FROM table_sources (WHERE search_condition)? EOF
    ;

expression
    : primitive_expression
    | function_call
    | expression COLLATE id
    | case_expression
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression (STAR | DIVIDE | MODULE) expression
    | expression (PLUS | MINUS | BIT_AND | BIT_XOR | BIT_OR | DOUBLE_BAR) expression
    | expression comparison_operator expression
    | expression assignment_operator expression
    | over_clause
    ;

primitive_expression
    : NULL | constant | PARAM
    ;

case_expression
    : CASE expression switch_section+ (ELSE expression)? END
    | CASE switch_search_condition_section+ (ELSE expression)? END
    ;

unary_operator_expression
    : BIT_NOT expression
    | (PLUS | MINUS) expression
    ;

bracket_expression
    : LR_BRACKET expression RR_BRACKET | LR_BRACKET subquery RR_BRACKET
    ;

constant_expression
    : NULL
    | constant
    | function_call
    | LR_BRACKET constant_expression RR_BRACKET
    ;

select_statement
    : query_expression order_by_clause?
    ;

subquery
    : select_statement
    ;

search_condition
    : search_condition_and (OR search_condition_and)*
    ;

search_condition_and
    : search_condition_not (AND search_condition_not)*
    ;

search_condition_not
    : NOT? predicate
    ;

predicate
    : EXISTS LR_BRACKET subquery RR_BRACKET
    | expression comparison_operator expression
    | expression comparison_operator (ALL | SOME | ANY) LR_BRACKET subquery RR_BRACKET
    | expression NOT? BETWEEN expression AND expression
    | expression NOT? IN LR_BRACKET (subquery | expression_list) RR_BRACKET
    | expression NOT? LIKE expression (ESCAPE expression)?
    | expression IS null_notnull
    | LR_BRACKET search_condition RR_BRACKET
    ;

query_expression
    : (query_specification | LR_BRACKET query_expression RR_BRACKET) sql_union*
    ;

sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | (LR_BRACKET query_expression RR_BRACKET))
    ;

query_specification
    : SELECT (ALL | DISTINCT)? top_clause?
      select_list
      (FROM table_sources)?
      (WHERE search_condition)?
      (GROUP BY (ALL)? group_by_item (COMMA group_by_item)*)?
      (HAVING search_condition)?
    ;

top_clause
    : TOP (top_percent | top_count) (WITH TIES)?
    ;

top_percent
    : (REAL | FLOAT) PERCENT
    | LR_BRACKET expression RR_BRACKET PERCENT
    ;

top_count
    : DECIMAL
    | LR_BRACKET expression RR_BRACKET
    ;

order_by_clause
    : ORDER BY order_by_expression (COMMA order_by_expression)*
    (
      (OFFSET expression (ROW | ROWS) (FETCH (FIRST | NEXT) expression (ROW | ROWS) ONLY)?)
      | (LIMIT expression (OFFSET expression))
    )?
    ;

order_by_expression
    : expression (ASC | DESC)?
    ;

group_by_item
    : expression
    /*| rollup_spec
    | cube_spec
    | grouping_sets_spec
    | grand_total*/
    ;

select_list
    : select_list_elem (COMMA select_list_elem)*
    ;

column_elem
    : ((table_name DOT)? (id | DOLLAR IDENTITY | DOLLAR ROWGUID) | NULL) as_column_alias?
    ;

expression_elem
    : column_alias EQUAL expression
    | expression as_column_alias?
    ;

select_list_elem
    : column_elem
    | expression_elem
    | STAR
    ;

table_sources
    : table_source (COMMA table_source)*
    ;

table_source
    : table_source_item_joined
    | LR_BRACKET table_source_item_joined RR_BRACKET
    ;

table_source_item_joined
    : table_source_item join_part*
    ;

table_source_item
    : table_name                  as_table_alias?
    | derived_table              (as_table_alias column_alias_list?)?
    | function_call              (as_table_alias column_alias_list?)?
    ;

join_part
    : (INNER? |
       (LEFT | RIGHT | FULL) OUTER?)
       JOIN table_source ON search_condition
    | CROSS JOIN table_source
    | CROSS APPLY table_source
    | OUTER APPLY table_source
    ;

derived_table
    : subquery
    | LR_BRACKET subquery RR_BRACKET
    | table_value_constructor
    | LR_BRACKET table_value_constructor RR_BRACKET
    ;

function_call
    : CAST LR_BRACKET expression AS data_type RR_BRACKET              #CAST
    | CONVERT LR_BRACKET data_type COMMA expression (COMMA expression)? RR_BRACKET                              #CONVERT
    | CURRENT_TIMESTAMP                                 #CURRENT_TIMESTAMP
    | CURRENT_USER                                      #CURRENT_USER
    | DATEADD LR_BRACKET ID COMMA expression COMMA expression RR_BRACKET  #DATEADD
    | DATEDIFF LR_BRACKET ID COMMA expression COMMA expression RR_BRACKET #DATEDIFF
    | DATENAME LR_BRACKET ID COMMA expression RR_BRACKET                #DATENAME
    | DATEPART LR_BRACKET ID COMMA expression RR_BRACKET                #DATEPART
    | MIN_ACTIVE_ROWVERSION                             #MIN_ACTIVE_ROWVERSION
    | SESSION_USER                                      #SESSION_USER
    | SYSTEM_USER                                       #SYSTEM_USER
    | IIF LR_BRACKET search_condition COMMA expression COMMA expression RR_BRACKET   #IFF
    | (COUNT | COUNT_BIG) LR_BRACKET STAR RR_BRACKET over_clause? #COUNT
    | ID LR_BRACKET (ALL | DISTINCT)? expression_list? RR_BRACKET ((WITHIN GROUP  LR_BRACKET order_by_clause RR_BRACKET) | over_clause)?                 #ANY_FUNC
    ;


switch_section
    : WHEN expression THEN expression
    ;

switch_search_condition_section
    : WHEN search_condition THEN expression
    ;

as_column_alias
    : AS? column_alias
    ;

as_table_alias
    : AS? table_alias
    ;

table_alias
    : id
    ;

column_alias_list
    : LR_BRACKET column_alias (COMMA column_alias)* RR_BRACKET
    ;

column_alias
    : id
    | STRING
    ;

table_value_constructor
    : VALUES LR_BRACKET expression_list RR_BRACKET (COMMA LR_BRACKET expression_list RR_BRACKET)*
    ;

expression_list
    : expression (COMMA expression)*
    ;

over_clause
    : OVER LR_BRACKET (PARTITION BY expression_list)? order_by_clause? row_or_range_clause? RR_BRACKET
    ;

row_or_range_clause
    : (ROWS | RANGE) window_frame_extent
    ;

window_frame_extent
    : window_frame_preceding
    | BETWEEN window_frame_bound AND window_frame_bound
    ;

window_frame_bound
    : window_frame_preceding
    | window_frame_following
    ;

window_frame_preceding
    : UNBOUNDED PRECEDING
    | DECIMAL PRECEDING
    | CURRENT ROW
    ;

window_frame_following
    : UNBOUNDED FOLLOWING
    | DECIMAL FOLLOWING
    ;

table_name
    : id (DOT id)*
    ;

full_column_name
    : (table_name DOT)? id
    ;

null_notnull
    : NOT? NULL
    ;

data_type
    : id IDENTITY? (LR_BRACKET (DECIMAL | MAX) (COMMA DECIMAL)? RR_BRACKET)?
    | DOUBLE PRECISION?
    ;

constant
    : STRING
    | BINARY
    | sign? DECIMAL
    | sign? (REAL | FLOAT)
    | sign? DOLLAR (DECIMAL | FLOAT)
    ;

sign
    : PLUS
    | MINUS
    ;

id
    : simple_id
    | DOUBLE_QUOTE_ID
    | SQUARE_BRACKET_ID
    ;

simple_id
    : ID
    | APPLY
    | CAST
    | COUNT
    | COUNT_BIG
    | DATEADD
    | DATEDIFF
    | DATENAME
    | DATEPART
    | FIRST
    | FOLLOWING
    | MIN_ACTIVE_ROWVERSION
    | NEXT
    | OFFSET
    | ONLY
    | PARTITION
    | PRECEDING
    | RANGE
    | ROW
    | ROWGUID
    | ROWS
    | TIES
    | UNBOUNDED
    ;

comparison_operator
    : EQUAL | GREATER | LESS | LESS EQUAL | GREATER EQUAL | LESS GREATER | EXCLAMATION EQUAL | EXCLAMATION GREATER | EXCLAMATION LESS
    ;

assignment_operator
    : PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN | MOD_ASSIGN | AND_ASSIGN | XOR_ASSIGN | OR_ASSIGN
    ;