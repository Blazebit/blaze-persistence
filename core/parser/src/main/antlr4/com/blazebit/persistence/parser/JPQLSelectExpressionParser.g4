/*
 * Copyright 2014 Blazebit.
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
parser grammar JPQLSelectExpressionParser;

options { tokenVocab=JPQLSelectExpressionLexer; }

@parser::members {
private boolean allowOuter = false;
private boolean allowCaseWhen = false;
private boolean allowQuantifiedPredicates = false;
private boolean allowTreatJoinExtension = false;
public JPQLSelectExpressionParser(TokenStream input, boolean allowCaseWhen, boolean allowQuantifiedPredicates, boolean allowTreatJoinExtension){
       this(input);
       this.allowCaseWhen = allowCaseWhen;
       this.allowQuantifiedPredicates = allowQuantifiedPredicates;
       this.allowTreatJoinExtension = allowTreatJoinExtension;
}

}

parseOrderByClause : state_field_path_expression EOF
                   | single_element_path_expression EOF
                   | key_value_expression EOF
                   ;

parsePath : state_field_path_expression EOF
          | single_element_path_expression EOF
          ;

parseJoinPath : join_association_path_expression EOF                                                                #NormalJoinPathExpression
              | Outer_function LP join_association_path_expression RP EOF                                         #OuterJoinPathExpression
              | macroName=Identifier LP (join_association_path_expression (Argument_separator simple_expression)*)?  RP  EOF     #MacroJoinPathExpression
              ;

join_association_path_expression
          : simple_subpath Path_separator general_path_element                                             #SimpleJoinPathExpression
          // TODO: I am not sure this is actually necessary. Maybe we leave it in the grammar but the treated_subpath will be replaced by a join node
          | {allowTreatJoinExtension == true}? treated_subpath Path_separator general_path_element        #ExtendedJoinPathExpression
          | single_element_path_expression                                                      #SingleJoinElementExpression
          | TREAT LP join_path_expression AS subtype RP                                       #TreatJoinPathExpression
          ;

join_path_expression
    : simple_subpath Path_separator general_path_element                                                    #SimplePath
    | {allowTreatJoinExtension == true}? TREAT LP identifier AS subtype RP Path_separator simple_subpath  #TreatedRootPath
    ;

subtype : identifier
        ;

parseSimpleExpression
    : simple_expression EOF 
    ;

parseSimpleOrObjectExpression
    : simple_expression EOF
    | treated_subpath EOF
    ;

parseSimpleSubqueryExpression
@init{
      allowOuter = true;
}
    : simple_expression EOF
    ;

parseScalarExpression
    : scalar_expression;

parseArithmeticExpression
    : arithmetic_expression;

parseStringExpression
    : string_expression;

parseCaseOperandExpression
    : case_operand;
    
parseInItemExpression
    : in_item;

parseInItemOrPathExpression
    : simple_in_item
    | state_field_path_expression
    | single_element_path_expression
    ;

parsePredicateExpression
    : conditional_expression;

simple_expression : single_valued_path_expression
                  | scalar_expression 
                  | aggregate_expression
                  ;
 
key_value_expression : name=KEY LP collection_valued_path_expression RP
                     | name=VALUE LP collection_valued_path_expression RP
                     ;

treated_key_value_expression
    :
    // I think this is an error in the JPQL grammar as it doesn't make sense to do TREAT(ENTRY(...))
    //| TREAT '(' qualified_identification_variable AS subtype ')'
    | TREAT LP key_value_expression AS subtype RP
    ;

qualified_identification_variable : name=ENTRY LP collection_valued_path_expression RP # EntryFunction
                                  | key_value_expression #KeyValueExpression
                                  ;

single_valued_path_expression
    : qualified_identification_variable
    | treated_key_value_expression
    | state_field_path_expression
    | single_element_path_expression
    ;

general_path_start : general_path_element
                   | key_value_expression
                   ;

simple_path_element : identifier
                    ;

general_path_element : simple_path_element
                     | array_expression
                     ;

//TODO: allow only in certain clauses??
//array_expression : simple_path_element '[' arithmetic_expression ']' #ArrayExpressionArithmeticIndex
//                 | simple_path_element '[' string_expression ']' #ArrayExpressionStringIndex
//                 ;

array_expression : simple_path_element LB Input_parameter RB #ArrayExpressionParameterIndex
                | simple_path_element LB state_field_path_expression RB #ArrayExpressionPathIndex
                | simple_path_element LB single_element_path_expression RB #ArrayExpressionSingleElementPathIndex
                | simple_path_element LB Integer_literal RB #ArrayExpressionIntegerLiteralIndex
                | simple_path_element LB string_literal RB #ArrayExpressionStringLiteralIndex
                ;

general_subpath : simple_subpath
                | treated_subpath
                ;

simple_subpath : general_path_start (Path_separator general_path_element)*
               ;

treated_subpath : TREAT LP general_subpath AS subtype RP (Path_separator general_path_element)*
                ;

state_field_path_expression : path
                            | {allowOuter == true}? outer_expression
                            | macro_expression
                            ;

single_valued_object_path_expression : path
                                     | {allowOuter == true}? outer_expression
                                     | macro_expression
                                     ;

path : general_subpath Path_separator general_path_element
     ;

collection_valued_path_expression : single_element_path_expression
                                  | path
                                  | macro_expression
                                  ;

single_element_path_expression : general_path_start
                               ;

aggregate_expression : funcname=( AVG | MAX | MIN | SUM | COUNT) LP (distinct=DISTINCT)? aggregate_argument RP  # AggregateExpression
                     | funcname=COUNT LP Star RP # CountStar
                     ;

aggregate_argument : single_element_path_expression
                   | path // Theoretically we could use state_field_path_expression but not sure if OUTER expression is allowed in subquery
                   | scalar_expression // This is a custom, non JPA compliant extension
                   ;

scalar_expression : null_literal // This is a custom, non JPA compliant extension
                  | arithmetic_expression
                  | string_expression
                  | enum_expression
                  | datetime_expression
                  | boolean_expression
                  | entity_type_expression
                  | case_expression
                  ;

outer_expression : Outer_function LP single_valued_path_expression  RP
                 ;

// Careful before you change something, an empty invocation will produce a child, which is specially handled in JPQLSelectExpressionVisitorImpl
macro_expression : macroName=Identifier LP (simple_expression (Argument_separator simple_expression)*)?  RP
                 ;

arithmetic_expression : arithmetic_term # ArithmeticExpressionTerm
                      | arithmetic_expression op=Signum arithmetic_term # ArithmeticExpressionPlusMinus
                      ;

arithmetic_term : arithmetic_factor # ArithmeticTermFactor
                | term=arithmetic_term op=( Star | Slash ) factor=arithmetic_factor # ArithmeticMultDiv
                ;

arithmetic_factor : signum=Signum? arithmetic_primary;

arithmetic_primary : state_field_path_expression # ArithmeticPrimary
                   | single_element_path_expression # ArithmeticPrimary
                   | numeric_literal # ArithmeticPrimary
                   | LP arithmetic_expression RP # ArithmeticPrimaryParanthesis
                   | Input_parameter # ArithmeticPrimary
                   | functions_returning_numerics # ArithmeticPrimary
                   | aggregate_expression # ArithmeticPrimary
                   | case_expression # ArithmeticPrimary
                   | function_invocation # ArithmeticPrimary
                   ;

string_expression : state_field_path_expression
                  | single_element_path_expression
                  | string_literal
                  | Input_parameter
                  | functions_returning_strings
                  | aggregate_expression
                  | case_expression
                  | function_invocation
                  ;

datetime_expression : state_field_path_expression
                    | single_element_path_expression
                    | Input_parameter
                    | functions_returning_datetime
                    | aggregate_expression
                    | case_expression
                    | function_invocation
                    | temporal_literal
                    ;

boolean_expression : state_field_path_expression
                   | single_element_path_expression
                   | boolean_literal
                   | Input_parameter
                   | case_expression
                   | function_invocation
                   ;

enum_expression : state_field_path_expression
                | single_element_path_expression
                | Input_parameter
                | case_expression
                ;

// is used in comparison for e.g. plain identification variables
entity_expression : single_valued_object_path_expression
                  | simple_entity_expression
                  ;

simple_entity_expression : identifier
                         | Input_parameter
                         ;

entity_type_expression : type_discriminator
                       | Input_parameter
                       ;

entity_type_or_literal_expression : entity_type_expression
                                  | pathElem+=identifier (Path_separator pathElem+=identifier)*
                                  ;

type_discriminator : TYPE LP type_discriminator_arg RP;

type_discriminator_arg : Input_parameter
                       | single_valued_object_path_expression
                       | single_element_path_expression
                       | key_value_expression
                       ;

functions_returning_numerics : LENGTH LP string_expression RP # Functions_returning_numerics_default
                             | LOCATE LP string_expression Argument_separator string_expression (Argument_separator arithmetic_expression)? RP # Functions_returning_numerics_default
                             | ABS LP arithmetic_expression RP # Functions_returning_numerics_default
                             | SQRT LP arithmetic_expression RP # Functions_returning_numerics_default
                             | MOD LP arithmetic_expression Argument_separator arithmetic_expression RP # Functions_returning_numerics_default
                             | SIZE LP collection_valued_path_expression RP # Functions_returning_numerics_size
                             | INDEX LP collection_valued_path_expression RP # IndexFunction
                             ;

functions_returning_datetime : CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP;

functions_returning_strings : CONCAT LP string_expression Argument_separator string_expression (Argument_separator string_expression)* RP # StringFunction
                            | SUBSTRING LP string_expression Argument_separator arithmetic_expression (Argument_separator arithmetic_expression)? RP # StringFunction
                            | TRIM LP ((trim_specification)? (trim_character)? FROM)? string_expression RP # TrimFunction
                            | LOWER LP string_expression RP # StringFunction
                            | UPPER LP string_expression RP # StringFunction
                            ;

trim_specification : LEADING
                   | TRAILING
                   | BOTH
                   ;

function_invocation : FUNCTION LP string_literal (Argument_separator args+=function_arg)* RP;

function_arg :
             state_field_path_expression
             // Entity and Enum literal is handled by state_field_path_expression
             | simple_literal
             | Input_parameter
             | scalar_expression
             ;

case_expression : coalesce_expression
                | nullif_expression
                | {allowCaseWhen == true}? general_case_expression     //for entity view extension only
                | {allowCaseWhen == true}? simple_case_expression     //for entity view extension only
                ;

coalesce_expression : COALESCE LP scalar_expression (Argument_separator scalar_expression)+ RP;

nullif_expression : NULLIF LP scalar_expression Argument_separator scalar_expression RP;

null_literal : NULL;

literal
    : simple_literal
    | pathElem+=identifier (Path_separator pathElem+=identifier)+
    ;

simple_literal
    : boolean_literal
    | numeric_literal
    | string_literal
    | temporal_literal
    ;

numeric_literal
    : Integer_literal
    | Long_literal
    | BigInteger_literal
    | Float_literal
    | Double_literal
    | BigDecimal_literal
    ;

string_literal : String_literal
               | Character_literal
               ;

boolean_literal : Boolean_literal
                ;

temporal_literal
    : Date_literal      #DateLiteral
    | Time_literal      #TimeLiteral
    | Timestamp_literal #TimestampLiteral
    ;

trim_character : string_literal
               | Input_parameter
               ;

/* conditional expression stuff for case when in entity view extension */
conditional_expression : conditional_term # ConditionalExpression
                       | conditional_expression or=OR conditional_term # ConditionalExpression_or
                       ;

conditional_term : conditional_factor # ConditionalTerm
                 | conditional_term and=AND conditional_factor # ConditionalTerm_and
                 ;

conditional_factor : (not=NOT)? conditional_primary
                   ;

conditional_primary : simple_cond_expression # ConditionalPrimary_simple
                    | LP conditional_expression RP # ConditionalPrimary
                    ;

simple_cond_expression : comparison_expression
                       | between_expression
                       | like_expression
                       | in_expression
                       | null_comparison_expression
                       | empty_collection_comparison_expression
                       | collection_member_expression
                       | exists_expression
                       ;

between_expression : expr=arithmetic_expression (not=NOT)? BETWEEN bound1=arithmetic_expression AND bound2=arithmetic_expression # BetweenArithmetic
                   | expr=string_expression (not=NOT)? BETWEEN bound1=string_expression AND bound2=string_expression # BetweenString
                   | expr=datetime_expression (not=NOT)? BETWEEN bound1=datetime_expression AND bound2=datetime_expression # BetweenDatetime
                   ;

// TODO: the cases for identifier are actually not JPA compliant and is only required for managing a placeholder that is later replaced by a subquery
// in_expression : (/* Placeholder case */ left=Identifier | state_field_path_expression | type_discriminator) (not=NOT)? IN ( '(' inItems+=in_item (',' inItems+=in_item)* ')' | param=Input_parameter | /* Single expression case */ state_field_path_expression | /* Placeholder case */ single_element_path_expression )
// TODO: allowing scalar expressions on the LHS is not JPA compliant but this is allowed through the API
in_expression : (left=Identifier | scalar_expression) (not=NOT)? IN ( LP inItems+=in_item (Argument_separator inItems+=in_item)* RP | param=Input_parameter | /* Single expression case */ state_field_path_expression | /* Placeholder case */ single_element_path_expression )
              ;

in_item : literal
        | Input_parameter
        ;

simple_in_item : simple_literal
        | Input_parameter
        ;

like_expression : string_expression (not=NOT)? LIKE pattern_value (ESCAPE escape_character)?
                ;

pattern_value : string_literal
              | Input_parameter
              ;

escape_character : Character_literal
                 | Input_parameter
                 ;

null_comparison_expression : (single_valued_path_expression | Input_parameter | null_comparison_expression_extension) IS (not=NOT)? NULL
                           ;

// NOTE: This is not standard compliant but not allowing this seems strange..
null_comparison_expression_extension : scalar_expression
                                     ;

empty_collection_comparison_expression : collection_valued_path_expression IS (not=NOT)? EMPTY
                                       ;

collection_member_expression : entity_or_value_expression (not=NOT)? MEMBER OF? collection_valued_path_expression
                             ;

exists_expression : (not=NOT)? EXISTS identifier;

entity_or_value_expression : state_field_path_expression
                           | simple_entity_or_value_expression
                           | single_element_path_expression
                           ;

simple_entity_or_value_expression : identifier
                                  | Input_parameter
                                  // Entity and Enum literal is handled in entity_or_value_expression via state_field_path_expression
                                  | simple_literal
                                  ;

comparison_expression : left=string_expression comparison_operator right=string_expression # ComparisonExpression_string
                      | {allowQuantifiedPredicates == true}? left=string_expression comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_string
                      | left=boolean_expression op=equality_comparison_operator right=boolean_expression # ComparisonExpression_boolean
                      | {allowQuantifiedPredicates == true}? left=boolean_expression op=equality_comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_boolean
                      | left=enum_expression op=equality_comparison_operator right=enum_expression # ComparisonExpression_enum
                      | {allowQuantifiedPredicates == true}? left=datetime_expression comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_datetime
                      | left=datetime_expression comparison_operator right=datetime_expression # ComparisonExpression_datetime
                      | {allowQuantifiedPredicates == true}? left=datetime_expression comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_datetime
                      | left=entity_expression op=equality_comparison_operator right=entity_expression # ComparisonExpression_entity
                      | {allowQuantifiedPredicates == true}? left=entity_expression op=equality_comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_entity
                      | left=arithmetic_expression comparison_operator right=arithmetic_expression # ComparisonExpression_arithmetic
                      | {allowQuantifiedPredicates == true}? left=arithmetic_expression comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_arithmetic
                      | left=entity_type_or_literal_expression op=equality_comparison_operator right=entity_type_or_literal_expression # ComparisonExpression_entitytype
                      | {allowQuantifiedPredicates == true}? left=entity_type_expression op=equality_comparison_operator quantifier=(ALL | ANY | SOME) (LP right=identifier RP | right=identifier) # QuantifiedComparisonExpression_entitytype
                      | left=path op=equality_comparison_operator right=type_discriminator # ComparisonExpression_path_type
                      | left=type_discriminator op=equality_comparison_operator right=path # ComparisonExpression_type_path
                      ;

equality_comparison_operator : EQ # EqPredicate
                             | NEQ # NeqPredicate
                             ;

comparison_operator : equality_comparison_operator # EqOrNeqPredicate
                    | GT # GtPredicate
                    | GE # GePredicate
                    | LT # LtPredicate
                    | LE # LePredicate
                    ;

// NOTE: it is not standard compliant to have an optional ELSE branch, but we make up for that in our renderer by rendering a NULL result in the ELSE branch
general_case_expression : caseTerminal=CASE when_clause (when_clause)* (elseTerminal=ELSE scalar_expression)? endTerminal=END
                        ;

when_clause : whenTerminal=WHEN conditional_expression thenTerminal=THEN scalar_expression
            ;

// NOTE: it is not standard compliant to have an optional ELSE branch, but we make up for that in our renderer by rendering a NULL result in the ELSE branch
simple_case_expression : caseTerminal=CASE case_operand simple_when_clause (simple_when_clause)* (elseTerminal=ELSE scalar_expression)? endTerminal=END
                       ;

simple_when_clause : whenTerminal=WHEN scalar_expression thenTerminal=THEN scalar_expression
                   ;

case_operand : state_field_path_expression
             | type_discriminator
             ;

keyword :KEY
       | VALUE
       | ENTRY
       | AVG
       | SUM
       | MAX
       | MIN
       | COUNT
       | DISTINCT
       | ENUM
       | ENTITY
       | TYPE
       | LENGTH
       | LOCATE
       | ABS
       | SQRT
       | MOD
       | INDEX

/* We have to exclude date time functions from the "keyword as identifier" part because without brackets we don't know for sure if it's an identifier or function. So we assume it's never an identifier */

       /*
       | CURRENT_DATE
       | CURRENT_TIME
       | CURRENT_TIMESTAMP
       */

       /*
       | NULL
       */

       | CONCAT
       | SUBSTRING
       | TRIM
       | LOWER
       | UPPER
       | FROM
       | LEADING
       | TRAILING
       | BOTH
       | FUNCTION
       | COALESCE
       | NULLIF
       | NOT
       | OR
       | AND
       | BETWEEN
       | IN
       | LIKE
       | ESCAPE
       | IS
       | CASE
       | ELSE
       | END
       | WHEN
       | THEN
       | SIZE
       | ALL
       | ANY
       | SOME
       | EXISTS
       | EMPTY
       | MEMBER
       | OF
       | TREAT
       | AS
       | Outer_function
       | MACRO
       ;

identifier : Identifier
           | keyword
           ;
