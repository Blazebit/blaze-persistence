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
grammar JPQL;

 simple_expression : single_valued_path_expression |
                       scalar_expression |
                       aggregate_expression |
                   ;
 
 qualified_identification_variable : composable_qualified_identification_variable |
                                       'ENTRY('Identification_variable')';

 composable_qualified_identification_variable : 'KEY('Identification_variable')' |
                                                  'VALUE('Identification_variable')';

 single_valued_path_expression 
     : qualified_identification_variable
     | state_field_path_expression 
     | single_valued_object_path_expression
     | single_element_path_expression
     ;
 
 general_path_start : general_path_element
                    | composable_qualified_identification_variable
                    ;

 simple_path_element : Identifier
                        | Single_valued_object_field
                        | Collection_valued_field
                        ;
  
 general_path_element : simple_path_element
                      | array_expression
                      ;
 
 //TODO: allow only in certain clauses??
 array_expression : simple_path_element '[' arithmetic_primary ']'
                  ;
 
 array_index : state_field_path_expression
             | ':' input_parameter
             | Numeric_literal
             ;
 
 input_parameter : Identifier
                 ;
      
 general_subpath : general_path_start('.'general_path_element)*;

 state_field_path_expression : general_subpath'.'general_path_element;

 single_valued_object_path_expression : general_subpath'.'general_path_element;

 collection_valued_path_expression : general_subpath'.'general_path_element;
 
 single_element_path_expression : general_path_start
                              ;

 aggregate_expression : ( 'AVG' | 'MAX' | 'MIN' | 'SUM' ) '('('DISTINCT')? state_field_path_expression')' 
                        | 'COUNT' (('DISTINCT')? Identification_variable | state_field_path_expression  | single_valued_object_path_expression) ;

 /*derived_path_expression : simple_derived_path'.'Single_valued_object_field |
                             simple_derived_path'.'Collection_valued_field;

 simple_derived_path : Superquery_identification_variable('.'Single_valued_object_field)*;*/

 scalar_expression : arithmetic_expression |
                       string_expression |
                       enum_expression |
                       datetime_expression |
                       boolean_expression |
                       coalesce_expression |
                       nullif_expression |
                       entity_type_expression;

 arithmetic_expression : arithmetic_term | arithmetic_expression ( '+' | '-' ) arithmetic_term;

 arithmetic_term : arithmetic_factor | arithmetic_term ( '*' | '/' ) arithmetic_factor;

 arithmetic_factor : ( '+' | '-' )? arithmetic_primary;

 arithmetic_primary : state_field_path_expression |
                        Numeric_literal |
                        '('arithmetic_expression')' |
                        ':' input_parameter |
                        functions_returning_numerics |
                        aggregate_expression |
                        case_expression |
                        function_invocation ;

 string_expression : state_field_path_expression |
                       String_literal |
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
                     Enum_literal |
                     Input_parameter |
                     case_expression ;

 entity_expression : single_valued_object_path_expression | simple_entity_expression;

 simple_entity_expression : Identification_variable |
                              Input_parameter;

 entity_type_expression : type_discriminator |
                            Entity_type_literal |
                            Input_parameter;

 type_discriminator : 'TYPE('Identification_variable | single_valued_object_path_expression | Input_parameter ')';

 functions_returning_numerics : 'LENGTH('string_expression')' |
                                  'LOCATE('string_expression',' string_expression (',' arithmetic_expression)? ')' |
                                  'ABS('arithmetic_expression')' |
                                  'SQRT('arithmetic_expression')' |
                                  'MOD('arithmetic_expression',' arithmetic_expression')' |
                                  'SIZE('collection_valued_path_expression')' |
                                  'INDEX('Identification_variable')';

 functions_returning_datetime : 'CURRENT_DATE' | 'CURRENT_TIME' | 'CURRENT_TIMESTAMP';

 functions_returning_strings : 'CONCAT('string_expression',' string_expression (',' string_expression)*')' |
                                 'SUBSTRING('string_expression',' arithmetic_expression (',' arithmetic_expression)?')' |
                                 'TRIM('((trim_specification)? (Trim_character)? 'FROM')? string_expression')' |
                                 'LOWER('string_expression')' |
                                 'UPPER('string_expression')';

 trim_specification : 'LEADING' | 'TRAILING' | 'BOTH';

 function_invocation : 'FUNCTION('String_literal (',' function_arg)*')';

 function_arg : literal |
                  state_field_path_expression |
                  Input_parameter |
                  scalar_expression;

 case_expression : coalesce_expression | nullif_expression;
 
 case_operand : state_field_path_expression | type_discriminator;

 coalesce_expression : 'COALESCE('scalar_expression (',' scalar_expression)+')';

 nullif_expression : 'NULLIF('scalar_expression',' scalar_expression')';

 literal
     : Boolean_literal
     | Enum_literal   
     | Numeric_literal
     | String_literal
     ;
     
 

 literal_temporal 
     : Date_literal 
     | Time_literal 
     | Timestamp_literal
     ;
