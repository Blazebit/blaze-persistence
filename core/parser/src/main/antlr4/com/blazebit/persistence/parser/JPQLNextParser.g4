/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
parser grammar JPQLNextParser;

options {
    tokenVocab=JPQLNextLexer;
}

// ############################################################################
// Entry points
// ############################################################################

parseStatement
    : (WITH RECURSIVE? withQuery)? simpleStatement EOF
    ;

parseSelectExpression
    : selectExpression EOF
    ;

parsePathExpression
    : path EOF
    | outerPath EOF
    | macroPath EOF
    ;

parseExpression
    : expression EOF
    ;

parseInItemExpression
    : parameterOrLiteral EOF
    | entityTypeOrEnumLiteral EOF
    ;

parseInItemOrPathExpression
    : parameterOrLiteral EOF
    | parsePathExpression
    ;

parsePredicate
    : predicate EOF
    ;

// ############################################################################
// Statements
// ############################################################################

withQuery
    : entityName LP identifier (COMMA identifier)* RP AS LP simpleStatement RP
    ;

simpleStatement
    : selectStatement
    | updateStatement
    | deleteStatement
    | insertStatement
    ;

selectStatement
    : querySpecification
    | LP selectStatement RP (UNION | INTERSECT | EXCEPT) (ALL | DISTINCT)? selectStatement
    ;

subQuery
    : subQuerySpecification
    | LP subQuery RP (UNION | INTERSECT | EXCEPT) (ALL | DISTINCT)? subQuery
    ;

subQuerySpecification
    : subQuerySelectClause subQueryFromClause keysetClause? whereClause? groupByClause? havingClause? ((UNION | INTERSECT | EXCEPT) (ALL | DISTINCT)? subQuery)? subQueryOrderByClause?
    ;

subQueryOrderByClause
    : orderByClause ((limitClause offsetClause?) | offsetClause)
    ;

deleteStatement
    : DELETE FROM? entityName (LP simpleSubpath RP)? identificationVariable? whereClause? returningClause?
    ;

updateStatement
    : UPDATE FROM? entityName (LP simpleSubpath RP)? identificationVariable? SET dmlAttributeName EQUAL expression (COMMA dmlAttributeName EQUAL expression)* whereClause? returningClause?
    ;

insertStatement
    : INSERT INTO entityName (LP simpleSubpath RP)? targetFieldsSpecification querySpecification returningClause?
    ;

dmlAttributeName
    : simpleSubpath
    | KEY LP path RP
    | INDEX LP path RP
    ;

targetFieldsSpecification
    : LP dmlAttributeName (COMMA dmlAttributeName)* RP
    ;

returningClause
    : RETURNING simpleSubpath (COMMA simpleSubpath)*
    ;

querySpecification
    : selectClause? fromClause keysetClause? whereClause? groupByClause? havingClause? windowClause? ((UNION | INTERSECT | EXCEPT) (ALL | DISTINCT)? querySpecification)? orderByClause? limitClause? offsetClause?
    ;

// ############################################################################
// FROM clause
// ############################################################################

fromClause
    : FROM fromItem (COMMA fromItem)*
    ;

subQueryFromClause
    : FROM subQueryFromItem (COMMA subQueryFromItem)*
    ;

fromItem
    : fromItemElement ( crossJoin | inCollectionJoin | qualifiedJoin )*
    ;

subQueryFromItem
// We simply interpret the fromItemElement differently here to support correlated subqueries
    : fromItemElement ( crossJoin | inCollectionJoin | subQueryQualifiedJoin )*
    ;

fromItemElement
    : entityName (identificationVariable)?                                                              # FromEntity
// The syntax with a value count is the usual way to go
// But the inline value definition is for ad-hoc queries
    | entityName LP INTEGER_LITERAL VALUES RP identificationVariable                                    # FromValues
    | entityName LP VALUES fromItemValuesItem (COMMA fromItemValuesItem)* RP identificationVariable     # FromValuesList
    | identifier LP INTEGER_LITERAL VALUES (LIKE entityName)? RP identificationVariable                 # FromSimpleValuesLikeEntityAttribute
    | identifier LP VALUES fromItemValuesItem (COMMA fromItemValuesItem)* RP identificationVariable     # FromSimpleValuesList
    | (OLD | NEW) LP entityName RP (identificationVariable)?                                            # FromOldOrNew
// TODO: support subquery in the from clause
//    | LP subQuery RP                                                                                    # FromSubQuery
    ;

fromItemValuesItem
    : LP plainLiteral (COMMA plainLiteral)* RP
    ;

entityName
    : simpleSubpath
    ;

identificationVariable
    : (AS identifier)
    | IDENTIFIER
    ;

crossJoin
    : CROSS JOIN fromItemElement
    ;

inCollectionJoin
    : COMMA IN LP path RP (identificationVariable)?
    ;

qualifiedJoin
    : joinTypeQualifier JOIN FETCH? joinItemElement (qualifiedJoinPredicate)?
    ;

subQueryQualifiedJoin
    : joinTypeQualifier JOIN joinItemElement (qualifiedJoinPredicate)?
    ;

joinItemElement
    : fromItemElement
    | joinExpression
    ;

joinExpression
    : TREAT LP path AS entityName RP (DOT generalSubpath)? (identificationVariable)?
    ;

joinTypeQualifier
    : INNER?
    | (LEFT|RIGHT|FULL)? OUTER?
    ;

qualifiedJoinPredicate
    : ON predicate
    ;


// ############################################################################
// SELECT clause
// ############################################################################

selectClause
    : SELECT DISTINCT? selectItem (COMMA selectItem)*
    ;

subQuerySelectClause
    : SELECT DISTINCT? subQuerySelectItem (COMMA subQuerySelectItem)*
    ;

selectItem
    : selectExpression (resultIdentifier)?
    ;

subQuerySelectItem
    : expression
    ;

selectExpression
    : constructorExpression
    | objectSelectExpression
    | mapEntrySelectExpression
    | expression
    ;

resultIdentifier
    : (AS identifier)
    | IDENTIFIER
    ;

mapEntrySelectExpression
    : ENTRY LP path RP
    ;

constructorExpression
    : NEW simpleSubpath LP constructorItem (COMMA constructorItem)* RP
    ;

constructorItem
    : expression
    ;

objectSelectExpression
    : OBJECT LP identifier RP
    ;


// ############################################################################
// Paths
// ############################################################################

path
    : qualifiedPath (DOT generalSubpath)?
    | generalSubpath
    ;

macroPath
    : identifier LP (expression (COMMA expression)*)? RP
    ;

outerPath
    : OUTER LP (simpleSubpath | macroPath) RP
    ;

qualifiedPath
    : TREAT LP path AS entityName RP   # TreatPath
    | VALUE LP path RP                 # ValuePath
    | KEY LP path RP                   # MapKeyPath
    ;

simpleSubpath
    : identifier (DOT identifierNonStart)*
    ;

generalSubpath
    : simpleSubpath (LB predicateOrExpression RB (DOT generalSubpath)?)?
    ;

// ############################################################################
// GROUP BY clause
// ############################################################################

groupByClause
    : GROUP BY groupingValue (COMMA groupingValue)*
    ;

groupingValue
    : expression
//    | rollupSpecification
//    | cubeSpecification
//    | groupingSetsSpecification
//    | grandTotal
    ;


// ############################################################################
// HAVING clause
// ############################################################################

havingClause
    : HAVING predicate
    ;


// ############################################################################
// ORDER BY clause
// ############################################################################

orderByClause
    : ORDER BY orderByItem (COMMA orderByItem)*
    ;

orderByItem
    : expression (COLLATE STRING_LITERAL)? ((ASC | DESC) (NULLS (FIRST | LAST))?)?
    ;

// ############################################################################
// WINDOW clause
// ############################################################################

windowClause
    : WINDOW identifier AS LP windowDefinition RP (COMMA identifier AS LP windowDefinition RP)*
    ;

windowDefinition
    : identifier? partitionByClause? orderByClause? frameClause?
    ;

partitionByClause
    : PARTITION BY groupingValue (COMMA groupingValue)*
    ;

frameClause
    : frameMode=(RANGE | ROWS | GROUPS) frameStart frameExclusionClause?
    | frameMode=(RANGE | ROWS | GROUPS) BETWEEN frameStart AND frameEnd frameExclusionClause?
    ;

frameStart
    : UNBOUNDED PRECEDING
    | parameterOrNumberLiteral PRECEDING
    | CURRENT ROW
    | parameterOrNumberLiteral FOLLOWING
    ;

frameEnd
    : UNBOUNDED PRECEDING
    | parameterOrNumberLiteral PRECEDING
    | CURRENT ROW
    | parameterOrNumberLiteral FOLLOWING
    | UNBOUNDED FOLLOWING
    ;

frameExclusionClause
    : EXCLUDE CURRENT ROW
    | EXCLUDE GROUP
    | EXCLUDE TIES
    | EXCLUDE NO OTHERS
    ;

// ############################################################################
// LIMIT/OFFSET clause
// ############################################################################

limitClause
    : LIMIT parameterOrNumberLiteral
    ;

offsetClause
    : OFFSET parameterOrNumberLiteral
    | JUMP TO PAGE CONTAINING parameter
    ;

parameterOrNumberLiteral
    : parameter
    | INTEGER_LITERAL
    ;


// ############################################################################
// KEYSET clause
// ############################################################################

keysetClause
    : (BEFORE | AFTER) parameterOrLiteral
    | (BEFORE | AFTER) LP parameterOrLiteral (COMMA parameterOrLiteral)* RP
    ;

parameterOrLiteral
    : parameter
    | plainLiteral
    ;

entityTypeOrEnumLiteral
    : identifier ((DOT | DOLLAR) identifierNonStart)*
    ;

// ############################################################################
// WHERE clause
// ############################################################################

whereClause
    : WHERE predicate
    ;

// ############################################################################
// Expression
// ############################################################################

expression
    : LP expression RP                                                              # GroupedExpression
// TODO: for now, we don't support subqueries directly
//    | LP subQuery RP                                                                # SubQueryExpression
    | CASE operand=expression (simpleCaseWhen)+ (ELSE otherwise=expression)? END    # SimpleCaseExpression
    | CASE (searchedCaseWhen)+ (ELSE expression)? END                               # GeneralCaseExpression
    | literal                                                                       # LiteralExpression
    | parameter                                                                     # ParameterExpression
    | entityType                                                                    # EntityTypeExpression
// The temporal functions are special as they can be interpreted as paths as well, so we neeed to define them here with a highe precedence
    | name=(CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_INSTANT)      # TemporalFunctionExpression
    | path                                                                          # PathExpression
    | function                                                                      # FunctionExpression
    | MINUS expression                                                              # UnaryMinusExpression
    | PLUS expression                                                               # UnaryPlusExpression
    | lhs=expression (ASTERISK|SLASH|PERCENT) rhs=expression                        # MultiplicativeExpression
    | lhs=expression (PLUS|MINUS) rhs=expression                                    # AdditiveExpression
    | lhs=expression DOUBLE_PIPE rhs=expression                                     # ConcatenationExpression
    ;

predicate
    : LP predicate RP                                                                           # GroupedPredicate
    | NOT predicate                                                                             # NegatedPredicate
    | predicate AND predicate                                                                   # AndPredicate
    | predicate OR predicate                                                                    # OrPredicate
// TODO: for now, we don't support subqueries directly
//    | NOT? EXISTS LP subQuery RP                                                                # ExistsPredicate
    | NOT? EXISTS (identifier | LP identifier RP)                                               # ExistsSimplePredicate
    | expression IS NOT? NULL                                                                   # IsNullPredicate
    | expression IS NOT? EMPTY                                                                  # IsEmptyPredicate
// TODO: for now, we don't support subqueries directly
//    | expression EQUAL quantifier=(ALL | ANY | SOME)? LP subQuery RP                            # QuantifiedEqualityPredicate
//    | expression NOT_EQUAL quantifier=(ALL | ANY | SOME)? LP subQuery RP                        # QuantifiedInequalityPredicate
//    | expression GREATER quantifier=(ALL | ANY | SOME)? LP subQuery RP                          # QuantifiedGreaterThanPredicate
//    | expression GREATER_EQUAL quantifier=(ALL | ANY | SOME)? LP subQuery RP                    # QuantifiedGreaterThanOrEqualPredicate
//    | expression LESS quantifier=(ALL | ANY | SOME)? LP subQuery RP                             # QuantifiedLessThanPredicate
//    | expression LESS_EQUAL quantifier=(ALL | ANY | SOME)? LP subQuery RP                       # QuantifiedLessThanOrEqualPredicate
    | expression EQUAL quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP))         # QuantifiedSimpleEqualityPredicate
    | expression NOT_EQUAL quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP))     # QuantifiedSimpleInequalityPredicate
    | expression GREATER quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP))       # QuantifiedSimpleGreaterThanPredicate
    | expression GREATER_EQUAL quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP)) # QuantifiedSimpleGreaterThanOrEqualPredicate
    | expression LESS quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP))          # QuantifiedSimpleLessThanPredicate
    | expression LESS_EQUAL quantifier=(ALL | ANY | SOME)? (identifier | (LP identifier RP))    # QuantifiedSimpleLessThanOrEqualPredicate
    | lhs=expression EQUAL rhs=expression                                                       # EqualityPredicate
    | lhs=expression NOT_EQUAL rhs=expression                                                   # InequalityPredicate
    | lhs=expression GREATER rhs=expression                                                     # GreaterThanPredicate
    | lhs=expression GREATER_EQUAL rhs=expression                                               # GreaterThanOrEqualPredicate
    | lhs=expression LESS rhs=expression                                                        # LessThanPredicate
    | lhs=expression LESS_EQUAL rhs=expression                                                  # LessThanOrEqualPredicate
    | expression NOT? IN inList                                                                 # InPredicate
    | lhs=expression NOT? BETWEEN start=expression AND end=expression                           # BetweenPredicate
    | lhs=expression NOT? LIKE like=expression (ESCAPE escape=expression)?                      # LikePredicate
    | expression NOT? MEMBER OF path                                                            # MemberOfPredicate
    ;

predicateOrExpression
    : predicate
    | expression
    ;

inList
    : LP expression (COMMA expression)* RP
    | expression
    ;

entityType
    : TYPE LP (path | parameter) RP
    ;

simpleCaseWhen
    : WHEN when=expression THEN then=expression
    ;

searchedCaseWhen
    : WHEN predicate THEN expression
    ;

literal
    : STRING_LITERAL
    | CHARACTER_LITERAL
    | INTEGER_LITERAL
    | LONG_LITERAL
    | BIG_INTEGER_LITERAL
    | FLOAT_LITERAL
    | DOUBLE_LITERAL
    | BIG_DECIMAL_LITERAL
    | NULL
    | TRUE
    | FALSE
    | timestampLiteral
    | dateLiteral
    | timeLiteral
    ;

plainLiteral
    : STRING_LITERAL
    | CHARACTER_LITERAL
    | plainNumericLiteral
    | NULL
    | TRUE
    | FALSE
    | timestampLiteral
    | dateLiteral
    | timeLiteral
    ;

plainNumericLiteral
    : (PLUS|MINUS)? INTEGER_LITERAL
    | (PLUS|MINUS)? LONG_LITERAL
    | (PLUS|MINUS)? BIG_INTEGER_LITERAL
    | (PLUS|MINUS)? FLOAT_LITERAL
    | (PLUS|MINUS)? DOUBLE_LITERAL
    | (PLUS|MINUS)? BIG_DECIMAL_LITERAL
    ;

timestampLiteral
    : TIMESTAMP_ESCAPE_START dateTimeLiteralText TEMPORAL_ESCAPE_END
    ;

dateLiteral
    : DATE_ESCAPE_START dateTimeLiteralText TEMPORAL_ESCAPE_END
    ;

timeLiteral
    : TIME_ESCAPE_START dateTimeLiteralText TEMPORAL_ESCAPE_END
    ;

dateTimeLiteralText
    : STRING_LITERAL | CHARACTER_LITERAL
    ;

parameter
    : COLON identifier                  # NamedParameter
    | QUESTION_MARK INTEGER_LITERAL     # PositionalParameter
    ;

// Mandatory only
function
    : TRIM LP trimSpecification? trimCharacter? FROM? expression RP                                                                                                 # TrimFunction
    | name=(CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_INSTANT) (LP RP)?                                                                             # TemporalFunction
    | COUNT LP ((DISTINCT? expression) | ASTERISK) RP (FILTER LP whereClause RP)? (OVER (windowName=identifier | (LP windowDefinition RP)))?                        # CountFunction
    | name=identifier LP DISTINCT? (expression (COMMA expression)*)? RP
        (WITHIN GROUP LP orderByClause RP)?
        (FILTER LP whereClause RP)?
        (OVER (windowName=identifier | (LP windowDefinition RP)))?                                                                                                  # GenericFunctionInvocation
    ;

trimSpecification
    : LEADING
    | TRAILING
    | BOTH
    ;

trimCharacter
    : CHARACTER_LITERAL
    | STRING_LITERAL
    | parameter
    ;

identifier
    : IDENTIFIER
    | AFTER
    | ALL
    | AND
    | ANY
    | AS
    | ASC
    | BEFORE
    | BETWEEN
    | BOTH
    | BY
    | CASE
    | COLLATE
    | CONTAINING
    | COUNT
    | CROSS
    | CURRENT
    | DELETE
    | DESC
    | DISTINCT
    | ELSE
    | EMPTY
    | END
    | ENTRY
    | ESCAPE
    | EXCEPT
    | EXCLUDE
    | EXISTS
    | FETCH
    | FILTER
    | FIRST
    | FOLLOWING
    | FROM
    | FULL
    | GROUP
    | GROUPS
    | HAVING
    | IN
    | INDEX
    | INNER
    | INSERT
    | INTERSECT
    | INTO
    | IS
    | JOIN
    | JUMP
    | KEY
    | LAST
    | LEADING
    | LEFT
    | LIKE
    | LIMIT
    | MEMBER
    | NEW
    | NO
    | NOT
    | NULLS
    | OBJECT
    | OF
    | OFFSET
    | ON
    | OR
    | ORDER
    | OTHERS
    | OUTER
    | OVER
    | PAGE
    | PARTITION
    | PRECEDING
    | RANGE
    | RECURSIVE
    | RETURNING
    | RIGHT
    | ROW
    | ROWS
    | SELECT
    | SET
    | SOME
    | THEN
    | TIES
    | TO
    | TRAILING
    | TREAT
    | TRIM
    | TYPE
    | UNBOUNDED
    | UNION
    | UPDATE
    | VALUE
    | VALUES
    | WHEN
    | WHERE
    | WINDOW
    | WITH
    ;

identifierNonStart
    : identifier
    | TRUE
    | FALSE
    ;
