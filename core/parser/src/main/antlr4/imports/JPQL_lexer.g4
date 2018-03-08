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

KEY: [Kk][Ee][Yy];

VALUE: [Vv][Aa][Ll][Uu][Ee];

ENTRY: [Ee][Nn][Tt][Rr][Yy];

AVG : [Aa][Vv][Gg];

SUM : [Ss][Uu][Mm];

MAX : [Mm][Aa][Xx];

MIN : [Mm][Ii][Nn];

COUNT : [Cc][Oo][Uu][Nn][Tt];

DISTINCT : [Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt];

ENUM : [Ee][Nn][Uu][Mm];

ENTITY : [Ee][Nn][Tt][Ii][Tt][Yy];

TYPE : [Tt][Yy][Pp][Ee];

LENGTH : [Ll][Ee][Nn][Gg][Tt][Hh];

LOCATE : [Ll][Oo][Cc][Aa][Tt][Ee];

ABS : [Aa][Bb][Ss];

SQRT : [Ss][Qq][Rr][Tt];

MOD : [Mm][Oo][Dd];

INDEX : [Ii][Nn][Dd][Ee][Xx];

CURRENT_DATE : [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Dd][Aa][Tt][Ee];

CURRENT_TIME : [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Tt][Ii][Mm][Ee];

CURRENT_TIMESTAMP : [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp];

CONCAT : [Cc][Oo][Nn][Cc][Aa][Tt];

SUBSTRING : [Ss][Uu][Bb][Ss][Tt][Rr][Ii][Nn][Gg];

TRIM : [Tt][Rr][Ii][Mm];

LOWER : [Ll][Oo][Ww][Ee][Rr];

UPPER : [Uu][Pp][Pp][Ee][Rr];

FROM  : [Ff][Rr][Oo][Mm];

LEADING : [Ll][Ee][Aa][Dd][Ii][Nn][Gg];

TRAILING : [Tt][Rr][Aa][Ii][Ll][Ii][Nn][Gg];

BOTH : [Bb][Oo][Tt][Hh];

FUNCTION : [Ff][Uu][Nn][Cc][Tt][Ii][Oo][Nn];

COALESCE : [Cc][Oo][Aa][Ll][Ee][Ss][Cc][Ee];

NULLIF : [Nn][Uu][Ll][Ll][Ii][Ff];

NOT : NOT_FRAG;

OR : [Oo][Rr];

AND : [Aa][Nn][Dd];

BETWEEN : [Bb][Ee][Tt][Ww][Ee][Ee][Nn];

IN : [Ii][Nn];

LIKE : [Ll][Ii][Kk][Ee];

ESCAPE : [Ee][Ss][Cc][Aa][Pp][Ee];

IS : [Ii][Ss];

NULL : [Nn][Uu][Ll][Ll];

CASE : [Cc][Aa][Ss][Ee];

ELSE : [Ee][Ll][Ss][Ee];

END : [Ee][Nn][Dd];

WHEN : [Ww][Hh][Ee][Nn];

THEN : [Tt][Hh][Ee][Nn];
    
SIZE : [Ss][Ii][Zz][Ee];

ALL : [Aa] [Ll] [Ll];

ANY: [Aa] [Nn] [Yy];

SOME: [Ss] [Oo] [Mm] [Ee];

EXISTS: [Ee] [Xx] [Ii] [Ss] [Tt] [Ss];

EMPTY: [Ee][Mm][Pp][Tt][Yy];

MEMBER: [Mm][Ee][Mm][Bb][Ee][Rr];

OF: [Oo][Ff];

TREAT: [Tt] [Rr] [Ee] [Aa] [Tt];

AS: [Aa] [Ss];

Outer_function : [Oo][Uu][Tt][Ee][Rr];
 
Star_operator : '*';

Character_literal : '\''JavaLetter'\'';

String_literal : '\'' ~[\']* '\'';

Input_parameter : ':'Identifier | '?'Digits;
 
//Enum_literal : (Identifier'.')+Identifier;
 
//Entity_type_literal : Identifier;
 
Date_literal : '{' 'd' (' ' | '\t')+ '\'' Date_string '\'' (' ' | '\t')* '}';

Time_literal : '{' 't' (' ' | '\t')+ '\'' Time_string '\'' (' ' | '\t')* '}';

Timestamp_literal : '{' 'ts' (' ' | '\t')+ '\'' Date_string ' ' Time_string ('.' DIGIT*)? '\'' (' ' | '\t')* '}';
     
Boolean_literal
     : [Tt][Rr][Uu][Ee]
     | [Ff][Aa][Ll][Ss][Ee]
     ;
 
Not_equal_operator
     : '<>'
     | '!='
     ;

Signum : SignumFragment
       ;

BigInteger_literal
    : DigitsNotZero BigIntegerSuffix
    | ZERO BigIntegerSuffix
    ;

Long_literal
    : DigitsNotZero LongSuffix
    | ZERO LongSuffix
    ;

Float_literal
    : Digits Exponent? FloatSuffix
    | Digits '.' Digits? Exponent? FloatSuffix?
    | '.' Digits Exponent? FloatSuffix?
    ;

Double_literal
    : Digits Exponent? DoubleSuffix
    | Digits '.' Digits* Exponent? DoubleSuffix
    | '.' Digits Exponent? DoubleSuffix
    ;

BigDecimal_literal
    : Digits Exponent? BigDecimalSuffix
    | Digits '.' Digits* Exponent? BigDecimalSuffix
    | '.' Digits Exponent? BigDecimalSuffix
    ;

Integer_literal
     : DigitsNotZero
     | ZERO
     ;
 
Path_separator
     : '.'
     ;
 
WS: [ \n\t\r]+ -> channel(HIDDEN);
 
Identifier
     : JavaLetter JavaLetterOrDigit*
     ;

fragment NOT_FRAG : [Nn][Oo][Tt];

fragment Date_string : DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;

fragment Time_string : DIGIT DIGIT? ':' DIGIT DIGIT ':' DIGIT DIGIT;

fragment DIGIT: '0'..'9';
fragment DIGIT_NOT_ZERO: '1'..'9';
fragment ZERO: '0';
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

fragment Digits
    :   DIGIT+
    ;

fragment DigitsNotZero
    : DIGIT_NOT_ZERO DIGIT*
    ;

fragment FloatSuffix : [fF];

fragment DoubleSuffix : [dD];

fragment BigDecimalSuffix : [bB][dD];

fragment LongSuffix : [lL];

fragment BigIntegerSuffix : [bB][iI];

fragment Exponent
    : [eE] SignedInteger
    ;

fragment SignedInteger
    : SignumFragment? Digits
    ;

fragment SignumFragment
    : ('+' | '-')
    ;