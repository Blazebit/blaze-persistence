/*
 * Copyright 2014 - 2019 Blazebit.
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
lexer grammar JPQLNextLexer;

WS : ( ' ' | '\t' | '\f' | EOL ) -> skip;
fragment EOL:               [\r\n]+;

fragment Digit              : '0'..'9';
fragment DigitNotZero       : '1'..'9';
fragment Zero               : '0';
fragment Digits             : Digit+;
fragment DigitsNotZero      : DigitNotZero Digit*;
fragment FloatSuffix        : [fF];
fragment DoubleSuffix       : [dD];
fragment BigDecimalSuffix   : [bB][dD];
fragment LongSuffix         : [lL];
fragment BigIntegerSuffix   : [bB][iI];
fragment SignedInteger      : SignumFragment? Digits;
fragment SignumFragment     : ('+' | '-');
fragment Exponent           : [eE] SignedInteger;
fragment ESCAPE_SEQUENCE    : ('\\' ('b'|'t'|'n'|'f'|'r'|'\\"'|'\''|'\\')) | UNICODE_ESCAPE;
fragment HEX_DIGIT          : ('0'..'9'|'a'..'f'|'A'..'F') ;
fragment UNICODE_ESCAPE     : '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT;

INTEGER_LITERAL
     : DigitsNotZero
     | Zero
     ;

BIG_INTEGER_LITERAL
    : DigitsNotZero BigIntegerSuffix
    | Zero BigIntegerSuffix
    ;

LONG_LITERAL
    : DigitsNotZero LongSuffix
    | Zero LongSuffix
    ;

FLOAT_LITERAL
    : Digits Exponent? FloatSuffix
    | Digits '.' Digits? Exponent? FloatSuffix?
    | '.' Digits Exponent? FloatSuffix?
    ;

DOUBLE_LITERAL
    : Digits Exponent? DoubleSuffix
    | Digits '.' Digits* Exponent? DoubleSuffix
    | '.' Digits Exponent? DoubleSuffix
    ;

BIG_DECIMAL_LITERAL
    : Digits Exponent? BigDecimalSuffix
    | Digits '.' Digits* Exponent? BigDecimalSuffix
    | '.' Digits Exponent? BigDecimalSuffix
    ;

CHARACTER_LITERAL
    : '\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\'' {setText(getText().substring(1, getText().length()-1));}
    ;

STRING_LITERAL
    : '"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"' {setText(getText().substring(1, getText().length() - 1));}
    | ('\'' ( ESCAPE_SEQUENCE | ~('\\'|'\'') )* '\'')+ {setText(getText().substring(1, getText().length() - 1).replace("''", "'"));}
    ;

AFTER               : [aA] [fF] [tT] [eE] [rR];
ALL                 : [aA] [lL] [lL];
AND                 : [aA] [nN] [dD];
ANY                 : [aA] [nN] [yY];
AS                  : [aA] [sS];
ASC                 : [aA] [sS] [cC];
BEFORE              : [bB] [eE] [fF] [oO] [rR] [eE];
BETWEEN             : [bB] [eE] [tT] [wW] [eE] [eE] [nN];
BOTH                : [bB] [oO] [tT] [hH];
BY                  : [bB] [yY];
CASE                : [cC] [aA] [sS] [eE];
COLLATE             : [cC] [oO] [lL] [lL] [aA] [tT] [eE];
CONTAINING          : [cC] [oO] [nN] [tT] [aA] [iI] [nN] [iI] [nN] [gG];
COUNT               : [cC] [oO] [uU] [nN] [tT];
CROSS               : [cC] [rR] [oO] [sS] [sS];
CURRENT             : [cC] [uU] [rR] [rR] [eE] [nN] [tT];
CURRENT_DATE        : [cC] [uU] [rR] [rR] [eE] [nN] [tT] '_' [dD] [aA] [tT] [eE];
CURRENT_INSTANT     : [cC] [uU] [rR] [rR] [eE] [nN] [tT] '_' [iI] [nN] [sS] [tT] [aA] [nN] [tT];
CURRENT_TIME        : [cC] [uU] [rR] [rR] [eE] [nN] [tT] '_' [tT] [iI] [mM] [eE];
CURRENT_TIMESTAMP   : [cC] [uU] [rR] [rR] [eE] [nN] [tT] '_' [tT] [iI] [mM] [eE] [sS] [tT] [aA] [mM] [pP];
DELETE              : [dD] [eE] [lL] [eE] [tT] [eE];
DESC                : [dD] [eE] [sS] [cC];
DISTINCT            : [dD] [iI] [sS] [tT] [iI] [nN] [cC] [tT];
ELSE                : [eE] [lL] [sS] [eE];
EMPTY               : [eE] [mM] [pP] [tT] [yY];
END                 : [eE] [nN] [dD];
ENTRY               : [eE] [nN] [tT] [rR] [yY];
ESCAPE              : [eE] [sS] [cC] [aA] [pP] [eE];
EXCEPT              : [Ee] [Xx] [Cc] [eE] [pP] [Tt];
EXCLUDE             : [Ee] [Xx] [Cc] [Ll] [Uu] [Dd] [Ee];
EXISTS              : [eE] [xX] [iI] [sS] [tT] [sS];
FALSE               : [fF] [aA] [lL] [sS] [eE];
FETCH               : [fF] [eE] [tT] [cC] [hH];
FILTER              : [fF] [iI] [lL] [tT] [eE] [rR];
FIRST               : [fF] [iI] [rR] [sS] [tT];
FOLLOWING           : [Ff] [Oo] [Ll] [Ll] [Oo] [Ww] [Ii] [Nn] [Gg];
FROM                : [fF] [rR] [oO] [mM];
FULL                : [fF] [uU] [lL] [lL];
GROUP               : [gG] [rR] [oO] [uU] [pP];
GROUPS              : [gG] [rR] [oO] [uU] [pP] [sS];
HAVING              : [hH] [aA] [vV] [iI] [nN] [gG];
IN                  : [iI] [nN];
INDEX               : [iI] [nN] [dD] [eE] [xX];
INNER               : [iI] [nN] [nN] [eE] [rR];
INSERT              : [iI] [nN] [sS] [eE] [rR] [tT];
INTERSECT           : [iI] [nN] [tT] [eE] [rR] [sS] [eE] [cC] [tT];
INTO                : [iI] [nN] [tT] [oO];
IS                  : [iI] [sS];
JOIN                : [jJ] [oO] [iI] [nN];
JUMP                : [jJ] [uU] [mM] [pP];
KEY                 : [kK] [eE] [yY];
LAST                : [lL] [aA] [sS] [tT];
LEADING             : [lL] [eE] [aA] [dD] [iI] [nN] [gG];
LEFT                : [lL] [eE] [fF] [tT];
LIKE                : [lL] [iI] [kK] [eE];
LIMIT               : [lL] [iI] [mM] [iI] [tT];
MEMBER              : [mM] [eE] [mM] [bB] [eE] [rR];
NEW                 : [nN] [eE] [wW];
NO                  : [Nn] [Oo];
NOT                 : [nN] [oO] [tT];
NULL                : [nN] [uU] [lL] [lL];
NULLS               : [nN] [uU] [lL] [lL] [sS];
OBJECT              : [oO] [bB] [jJ] [eE] [cC] [tT];
OF                  : [oO] [fF];
OFFSET              : [oO] [fF] [fF] [sS] [eE] [tT];
OLD                 : [oO] [lL] [dD];
ON                  : [oO] [nN];
OR                  : [oO] [rR];
ORDER               : [oO] [rR] [dD] [eE] [rR];
OTHERS              : [Oo] [Tt] [Hh] [Ee] [Rr] [Ss];
OUTER               : [oO] [uU] [tT] [eE] [rR];
OVER                : [oO] [vV] [eE] [rR];
PAGE                : [pP] [aA] [gG] [eE];
PARTITION           : [pP] [aA] [rR] [tT] [iI] [tT] [iI] [oO] [nN];
PRECEDING           : [Pp] [Rr] [Ee] [Cc] [Ee] [Dd] [Ii] [Nn] [Gg];
RANGE               : [rR] [aA] [nN] [gG] [eE];
RECURSIVE           : [rR] [eE] [cC] [uU] [rR] [sS] [iI] [vV] [eE];
RETURNING           : [rR] [eE] [tT] [uU] [rR] [nN] [iI] [nN] [gG];
RIGHT               : [rR] [iI] [gG] [hH] [tT];
ROW                 : [rR] [oO] [wW];
ROWS                : [rR] [oO] [wW] [sS];
SELECT              : [sS] [eE] [lL] [eE] [cC] [tT];
SET                 : [sS] [eE] [tT];
SOME                : [sS] [oO] [mM] [Ee];
THEN                : [tT] [hH] [eE] [nN];
TIES                : [Tt] [Ii] [Ee] [Ss];
TO                  : [Tt] [Oo];
TRAILING            : [tT] [rR] [aA] [iI] [lL] [iI] [nN] [gG];
TREAT               : [tT] [rR] [eE] [aA] [tT];
TRIM                : [tT] [rR] [iI] [mM];
TRUE                : [tT] [rR] [uU] [eE];
TYPE                : [tT] [yY] [pP] [eE];
UNBOUNDED           : [Uu] [Nn] [Bb] [Oo] [Uu] [Nn] [Dd] [Ee] [Dd];
UNION               : [Uu] [Nn] [iI] [Oo] [Nn];
UPDATE              : [uU] [pP] [dD] [aA] [tT] [eE];
VALUE               : [vV] [aA] [lL] [uU] [eE];
VALUES              : [vV] [aA] [lL] [uU] [eE] [sS];
WHEN                : [wW] [hH] [eE] [nN];
WHERE               : [wW] [hH] [eE] [rR] [eE];
WINDOW              : [wW] [iI] [nN] [dD] [oO] [wW];
WITH                : [wW] [iI] [tT] [hH];

TIMESTAMP_ESCAPE_START  : '{ts';
DATE_ESCAPE_START       : '{d';
TIME_ESCAPE_START       : '{t';
TEMPORAL_ESCAPE_END     : '}';

EQUAL                   : '=';
NOT_EQUAL               : '!=' | '<>';
GREATER                 : '>';
GREATER_EQUAL           : '>=';
LESS                    : '<';
LESS_EQUAL              : '<=';

COMMA                   : ',';
DOT                     : '.';
LP                      : '(';
RP                      : ')';
LB                      : '[';
RB                      : ']';
PLUS                    : '+';
MINUS                   : '-';
ASTERISK                : '*';
SLASH                   : '/';
PERCENT                 : '%';
AMPERSAND               : '&';
SEMICOLON               : ';';
COLON                   : ':';
PIPE                    : '|';
DOUBLE_PIPE             : '||';
QUESTION_MARK           : '?';
ARROW                   : '->';

IDENTIFIER
    : ('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
    ;

QUOTED_IDENTIFIER
    : '`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
    ;