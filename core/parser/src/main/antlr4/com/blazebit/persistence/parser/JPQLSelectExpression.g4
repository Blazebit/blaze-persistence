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
grammar JPQLSelectExpression;

//TODO: add antlr issue to illustrate that import of grammar containing left-recursive rules leads to error in antlr 4.3 (arithmetic_term)
import JPQL_lexer;

@parser::members {
private boolean allowCaseWhen = false;
public JPQLSelectExpressionParser(TokenStream input, boolean allowCaseWhen){
       this(input);
       this.allowCaseWhen = allowCaseWhen;
}      
}

parseOrderByClause : state_field_path_expression EOF
                   | single_element_path_expression EOF
                   | key_value_expression EOF
                   ;

parseSimpleExpression
    : simple_expression EOF 
    ;

parseSimpleSubqueryExpression
    : simple_subquery_expression EOF
    ;

parseScalarExpression
    : scalar_expression;
parseCaseOperandExpression
    : case_operand;

simple_expression : single_valued_path_expression |
                       scalar_expression |
                       aggregate_expression
                   ;

simple_subquery_expression : single_valued_path_expression # Simple_subquery_expression_default
                           | scalar_expression # Simple_subquery_expression_default
                           | aggregate_expression # Simple_subquery_expression_default
                           | Outer_function '(' single_valued_path_expression  ')' # Simple_subquery_expression_outerFunction
                           ;
 
key_value_expression : name='KEY' '('collection_valued_path_expression')'
                     | name='VALUE' '('collection_valued_path_expression')'
                     ;

qualified_identification_variable : name='ENTRY' '('collection_valued_path_expression')' # EntryFunction
                                  | key_value_expression #KeyValueExpression
                                  ;

 single_valued_path_expression 
     : qualified_identification_variable
     | state_field_path_expression
     | single_element_path_expression
     ;
 
 general_path_start : general_path_element
                    ;

 simple_path_element : Identifier
                     ;
  
 general_path_element : simple_path_element
                      | array_expression
                      ;
 
 //TODO: allow only in certain clauses??
 array_expression : simple_path_element '[' arithmetic_expression ']'
                  ;
      
 general_subpath : general_path_start('.'general_path_element)*;

 state_field_path_expression : general_subpath'.'general_path_element;

 single_valued_object_path_expression : general_subpath'.'general_path_element;

 collection_valued_path_expression : single_element_path_expression | state_field_path_expression;
 
 single_element_path_expression : general_path_start
                              ;

 aggregate_expression : funcname=( 'AVG' | 'MAX' | 'MIN' | 'SUM' | 'COUNT') '('(distinct='DISTINCT')? aggregate_argument ')'  # AggregateExpression
                      | funcname='COUNT' '(' Star_operator ')' # CountStar
                      ;
 
 aggregate_argument : single_element_path_expression
                    | state_field_path_expression
                    ;

 scalar_expression : arithmetic_expression |
                       string_expression |
                       enum_expression |
                       datetime_expression |
                       boolean_expression |
                       coalesce_expression |
                       nullif_expression |
                       entity_type_expression |
                       case_expression
                   ;

 arithmetic_expression : arithmetic_term # ArithmeticExpressionTerm
                       | arithmetic_expression op=( '+' | '-' ) arithmetic_term # ArithmeticExpressionPlusMinus
                       ;

 arithmetic_term : arithmetic_factor # ArithmeticTermFactor
                 | term=arithmetic_term op=( '*' | '/' ) factor=arithmetic_factor # ArithmeticMultDiv
                 ;

 arithmetic_factor : signum=( '+' | '-' )? arithmetic_primary;

 arithmetic_primary : state_field_path_expression # ArithmeticPrimary
                    | single_element_path_expression # ArithmeticPrimary
                    | Numeric_literal # ArithmeticPrimary
                    | '('arithmetic_expression')' # ArithmeticPrimaryParanthesis
                    | Input_parameter # ArithmeticPrimary
                    | functions_returning_numerics # ArithmeticPrimary
                    | aggregate_expression # ArithmeticPrimary
                    | case_expression # ArithmeticPrimary
                    | function_invocation # ArithmeticPrimary
                    ;

 string_expression : state_field_path_expression |
                     single_element_path_expression |
                       string_literal |
                       Input_parameter |
                       functions_returning_strings |
                       aggregate_expression |
                       case_expression |
                       function_invocation ;

 datetime_expression : state_field_path_expression |
                         Input_parameter |
                         functions_returning_datetime |
                         aggregate_expression |
                         case_expression |
                         function_invocation |
                         literal_temporal ;

 boolean_expression : state_field_path_expression |
                        Boolean_literal |
                        Input_parameter |
                        case_expression |
                        function_invocation ;

 enum_expression : state_field_path_expression |
                     //Enum_literal |
                     Input_parameter |
                     case_expression 
                 ;

 entity_expression : single_valued_object_path_expression | simple_entity_expression;

 simple_entity_expression : Identifier |
                              Input_parameter;

 entity_type_expression : type_discriminator |
                            Identifier |
                            Input_parameter;

 type_discriminator : 'TYPE' '(' type_discriminator_arg ')';
 
 type_discriminator_arg : Input_parameter 
                        | single_valued_object_path_expression 
                        | single_element_path_expression
                        ;

 functions_returning_numerics : 'LENGTH' '('string_expression')' # Functions_returning_numerics_default
                              | 'LOCATE' '('string_expression',' string_expression (',' arithmetic_expression)? ')' # Functions_returning_numerics_default
                              | 'ABS' '('arithmetic_expression')' # Functions_returning_numerics_default
                              | 'SQRT' '('arithmetic_expression')' # Functions_returning_numerics_default
                              | 'MOD' '('arithmetic_expression',' arithmetic_expression')' # Functions_returning_numerics_default
                              | Size_function '('collection_valued_path_expression')' # Functions_returning_numerics_size 
                              | 'INDEX' '('collection_valued_path_expression')' # Functions_returning_numerics_default
                              ;

 functions_returning_datetime : 'CURRENT_DATE' | 'CURRENT_TIME' | 'CURRENT_TIMESTAMP';

 functions_returning_strings : 'CONCAT' '('string_expression',' string_expression (',' string_expression)*')' # StringFunction
                             | 'SUBSTRING' '('string_expression',' arithmetic_expression (',' arithmetic_expression)?')' # StringFunction
                             | 'TRIM' '('((trim_specification)? (trim_character)? 'FROM')? string_expression')' # TrimFunction
                             | 'LOWER' '('string_expression')' # StringFunction
                             | 'UPPER' '('string_expression')' # StringFunction
                             ;

 trim_specification : 'LEADING' | 'TRAILING' | 'BOTH';

 function_invocation : 'FUNCTION' '(' string_literal (',' args+=function_arg)*')';

 function_arg : literal |
                  state_field_path_expression |
                  Input_parameter |
                  scalar_expression;

 case_expression : {allowCaseWhen == true}? general_case_expression |    //for entity view extension only
                    {allowCaseWhen == true}? simple_case_expression |   //for entity view extension only
                     coalesce_expression |
                     nullif_expression
                 ;
 
 case_operand : state_field_path_expression | type_discriminator;

 coalesce_expression : 'COALESCE' '('scalar_expression (',' scalar_expression)+')';

 nullif_expression : 'NULLIF' '('scalar_expression',' scalar_expression')';

 literal
     : Boolean_literal
     //| Enum_literal   
     | Numeric_literal
     | string_literal
     ;
 
 string_literal : String_literal
                | Character_literal
                ;

 literal_temporal 
     : Date_literal 
     | Time_literal 
     | Timestamp_literal
     ;

 trim_character : string_literal
                | Input_parameter
                ; 
 /* conditional expression stuff for case when in entity view extension */
 conditional_expression : conditional_term # ConditionalExpression
                        | conditional_expression or='OR' conditional_term # ConditionalExpression_Or
                        ;

 conditional_term : conditional_factor # ConditionalTerm
                  | conditional_term and='AND' conditional_factor # ConditionalTerm_And
                  ;

 conditional_factor : ('NOT')? conditional_primary
                    ;

 conditional_primary : simple_cond_expression | '('conditional_expression')'
                     ;

 simple_cond_expression : comparison_expression |
                            between_expression |
                            like_expression |
                            in_expression |
                            null_comparison_expression |
                            empty_collection_comparison_expression |
                            collection_member_expression |
                        ;

 between_expression : expr=arithmetic_expression ('NOT')? 'BETWEEN' bound1=arithmetic_expression 'AND' bound2=arithmetic_expression # BetweenArithmetic
                    | expr=string_expression ('NOT')? 'BETWEEN' bound1=string_expression 'AND' bound2=string_expression # BetweenString
                    | expr=datetime_expression ('NOT')? 'BETWEEN' bound1=datetime_expression 'AND' bound2=datetime_expression # BetweenDatetime
                    ;

 in_expression : (state_field_path_expression | type_discriminator) ('NOT')? 'IN' ( paranth='(' in_item (',' in_item)* ')' | param=Input_parameter )
               ;

 in_item : literal | Input_parameter
         ;

 like_expression : string_expression ('NOT')? 'LIKE' pattern_value (escapeToken='ESCAPE' escape_character)?
                 ;
 
 pattern_value : string_literal
               | Input_parameter
               ;
 
 escape_character : Character_literal
                  | Input_parameter
                  ;

 null_comparison_expression : (single_valued_path_expression | Input_parameter) isToken='IS' ('NOT')? nullToken='NULL'
                            ;

 empty_collection_comparison_expression : collection_valued_path_expression Empty_function
                                        ;

 collection_member_expression : entity_or_value_expression Member_of_function collection_valued_path_expression
                              ;

 entity_or_value_expression : state_field_path_expression |
                              simple_entity_or_value_expression |
                              single_element_path_expression
                            ;

 simple_entity_or_value_expression : Identifier |
                                       Input_parameter |
                                       literal
                                   ;
 
 comparison_expression : string_expression comparison_operator string_expression |
                           boolean_expression ( '=' | Not_equal_operator ) boolean_expression |
                           enum_expression ( '=' | Not_equal_operator ) enum_expression |
                           datetime_expression comparison_operator datetime_expression |
                           entity_expression ( '=' | Not_equal_operator ) entity_expression |
                           arithmetic_expression comparison_operator arithmetic_expression |
                           entity_type_expression ( '=' | Not_equal_operator ) entity_type_expression
                       ;
 
 comparison_operator : '=' | '>' | '>=' | '<' | '<=' | Not_equal_operator
                     ;
 
 general_case_expression : caseTerminal='CASE' when_clause (when_clause)* elseTerminal='ELSE' scalar_expression endTerminal='END'
                         ;

 when_clause : whenTerminal='WHEN' conditional_expression thenTerminal='THEN' scalar_expression
             ;

 simple_case_expression : caseTerminal='CASE' case_operand simple_when_clause (simple_when_clause)* elseTerminal='ELSE' scalar_expression endTerminal='END'
                        ;

 simple_when_clause : whenTerminal='WHEN' scalar_expression thenTerminal='THEN' scalar_expression
                    ;