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
lexer grammar JPQL_lexer;
 
Star_operator : '*';
    
Size_function : 'SIZE';

Outer_function : 'OUTER';
 
String_literal : '\'' ~[\']* '\'';
 
 Character_literal : '\''JavaLetter'\'';
 
 Input_parameter : ':'Identifier;
 
 //Enum_literal : (Identifier'.')+Identifier;
 
 Date_literal : '(' 'd' (' ' | '\t')+ '\'' Date_string '\'' (' ' | '\t')* ')';

 Time_literal : '(' 't' (' ' | '\t')+ '\'' Time_string '\'' (' ' | '\t')* ')';

 Timestamp_literal : '(' 'ts' (' ' | '\t')+ '\'' Date_string ' ' Time_string '\'' (' ' | '\t')* ')';

 Date_string : DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;

 Time_string : DIGIT DIGIT? ':' DIGIT DIGIT ':' DIGIT DIGIT '.' DIGIT*;
 
 Identifier
     : JavaLetter JavaLetterOrDigit*
     ;
 
 Identification_variable
     : Identifier
     ;
 
 Superquery_identification_variable
     : Identifier
     ;
 
 Entity_name
     : Identifier
     ;
 
 Result_variable
     : Identifier
     ;
 
 Constructor_name
     : Identifier
     ;
 
 Entity_type_literal
     : Identifier
     ;
 
 Subtype
     : Identifier
     ;
 
 Collection_valued_field
     : Identifier
     ;
 
 Single_valued_object_field
     : Identifier
     ;
 
 Single_valued_embeddable_object_field
     : Identifier
     ;
 
 State_field
     : Identifier
     ;
                
 Boolean_literal
     : 'TRUE'
     | 'FALSE'
     ;
 
 Numeric_literal
     : DIGIT+
     ;
 
 Path_separator
     : '.'
     ;
 
 WS: [ \n\t\r]+ -> skip;
 
fragment DIGIT: '0'..'9';
fragment DIGIT_NOT_ZERO: '1'..'9';
fragment
JavaLetter
: [a-zA-Z$_] // these are the "java letters" below 0xFF
| // covers all characters above 0xFF which are not a surrogate
~[\u0000-\u00FF\uD800-\uDBFF]
{Character.isJavaIdentifierStart(_input.LA(-1))}?
| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
[\uD800-\uDBFF] [\uDC00-\uDFFF]
{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
;

fragment
JavaLetterOrDigit
: [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
| // covers all characters above 0xFF which are not a surrogate
~[\u0000-\u00FF\uD800-\uDBFF]
{Character.isJavaIdentifierPart(_input.LA(-1))}?
| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
[\uD800-\uDBFF] [\uDC00-\uDFFF]
{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
;
