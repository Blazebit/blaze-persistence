/*
 * Copyright 2014 - 2020 Blazebit.
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
lexer grammar SQLLexer;

ALL:                                   [aA][lL][lL];
AND:                                   [aA][nN][dD];
ANY:                                   [aA][nN][yY];
AS:                                    [aA][sS];
ASC:                                   [aA][sS][cC];
BETWEEN:                               [bB][eE][tT][wW][eE][eE][nN];
BY:                                    [bB][yY];
CASE:                                  [cC][aA][sS][eE];
COLLATE:                               [cC][oO][lL][lL][aA][tT][eE];
CONVERT:                               ([tT][rR][yY]'_')? [cC][oO][nN][vV][eE][rR][tT];
CROSS:                                 [cC][rR][oO][sS][sS];
CURRENT:                               [cC][uU][rR][rR][eE][nN][tT];
CURRENT_DATE:                          [cC][uU][rR][rR][eE][nN][tT]'_'[dD][aA][tT][eE];
CURRENT_TIME:                          [cC][uU][rR][rR][eE][nN][tT]'_'[tT][iI][mM][eE];
CURRENT_TIMESTAMP:                     [cC][uU][rR][rR][eE][nN][tT]'_'[tT][iI][mM][eE][sS][tT][aA][mM][pP];
CURRENT_USER:                          [cC][uU][rR][rR][eE][nN][tT]'_'[uU][sS][eE][rR];
DESC:                                  [dD][eE][sS][cC];
DISTINCT:                              [dD][iI][sS][tT][iI][nN][cC][tT];
DOUBLE:                                [dD][oO][uU][bB][lL][eE];
ELSE:                                  [eE][lL][sS][eE];
END:                                   [eE][nN][dD];
ESCAPE:                                [eE][sS][cC][aA][pP][eE];
EXCEPT:                                [eE][xX][cC][eE][pP][tT];
EXISTS:                                [eE][xX][iI][sS][tT][sS];
FETCH:                                 [fF][eE][tT][cC][hH];
FROM:                                  [fF][rR][oO][mM];
FULL:                                  [fF][uU][lL][lL];
GROUP:                                 [gG][rR][oO][uU][pP];
HAVING:                                [hH][aA][vV][iI][nN][gG];
IDENTITY:                              [iI][dD][eE][nN][tT][iI][tT][yY];
IIF:                                   [iI][iI][fF];
IN:                                    [iI][nN];
INNER:                                 [iI][nN][nN][eE][rR];
INTERSECT:                             [iI][nN][tT][eE][rR][sS][eE][cC][tT];
IS:                                    [iI][sS];
JOIN:                                  [jJ][oO][iI][nN];
LEFT:                                  [lL][eE][fF][tT];
LIKE:                                  [lL][iI][kK][eE];
NOT:                                   [nN][oO][tT];
NULL:                                  [nN][uU][lL][lL];
ON:                                    [oO][nN];
OR:                                    [oO][rR];
ORDER:                                 [oO][rR][dD][eE][rR];
OUTER:                                 [oO][uU][tT][eE][rR];
OVER:                                  [oO][vV][eE][rR];
PERCENT:                               [pP][eE][rR][cC][eE][nN][tT];
PRECISION:                             [pP][rR][eE][cC][iI][sS][iI][oO][nN];
RIGHT:                                 [rR][iI][gG][hH][tT];
SELECT:                                [sS][eE][lL][eE][cC][tT];
SESSION_USER:                          [sS][eE][sS][sS][iI][oO][nN]'_'[uU][sS][eE][rR];
SET:                                   [sS][eE][tT];
SOME:                                  [sS][oO][mM][eE];
SYSTEM_USER:                           [sS][yY][sS][tT][eE][mM]'_'[uU][sS][eE][rR];
THEN:                                  [tT][hH][eE][nN];
TOP:                                   [tT][oO][pP];
UNION:                                 [uU][nN][iI][oO][nN];
VALUES:                                [vV][aA][lL][uU][eE][sS];
WHEN:                                  [wW][hH][eE][nN];
WHERE:                                 [wW][hH][eE][rR][eE];
WITH:                                  [wW][iI][tT][hH];
WITHIN:                                [wW][iI][tT][hH][iI][nN];

APPLY:                                 [aA][pP][pP][lL][yY];
CAST:                                  ([tT][rR][yY]'_')? [cC][aA][sS][tT];
COUNT:                                 [cC][oO][uU][nN][tT];
COUNT_BIG:                             [cC][oO][uU][nN][tT]'_'[bB][iI][gG];
DATEADD:                               [dD][aA][tT][eE][aA][dD][dD];
DATEDIFF:                              [dD][aA][tT][eE][dD][iI][fF][fF];
DATENAME:                              [dD][aA][tT][eE][nN][aA][mM][eE];
DATEPART:                              [dD][aA][tT][eE][pP][aA][rR][tT];
FIRST:                                 [fF][iI][rR][sS][tT];
FOLLOWING:                             [fF][oO][lL][lL][oO][wW][iI][nN][gG];
MAX:                                   [mM][aA][xX];
MIN_ACTIVE_ROWVERSION:                 [mM][iI][nN]'_'[aA][cC][tT][iI][vV][eE]'_'[rR][oO][wW][vV][eE][rR][sS][iI][oO][nN];
NEXT:                                  [nN][eE][xX][tT];
OFFSET:                                [oO][fF][fF][sS][eE][tT];
ONLY:                                  [oO][nN][lL][yY];
PARTITION:                             [pP][aA][rR][tT][iI][tT][iI][oO][nN];
PRECEDING:                             [pP][rR][eE][cC][eE][dD][iI][nN][gG];
RANGE:                                 [rR][aA][nN][gG][eE];
ROW:                                   [rR][oO][wW];
ROWGUID:                               [rR][oO][wW][gG][uU][iI][dD];
ROWS:                                  [rR][oO][wW][sS];
TIES:                                  [tT][iI][eE][sS];
UNBOUNDED:                             [uU][nN][bB][oO][uU][nN][dD][eE][dD];

SPACE:              [ \t\r\n]+    -> skip;
COMMENT:            '/*' (COMMENT | .)*? '*/' -> channel(HIDDEN);
LINE_COMMENT:       '--' ~[\r\n]* -> channel(HIDDEN);

DOUBLE_QUOTE_ID:    '"' ~'"'+ '"';
SINGLE_QUOTE:       '\'';
SQUARE_BRACKET_ID:  '[' ~']'+ ']';
DECIMAL:             DEC_DIGIT+;
ID:                  ( [a-zA-Z_#] | FullWidthLetter) ( [a-zA-Z_#$@0-9] | FullWidthLetter )*;
STRING:              'N'? '\'' (~'\'' | '\'\'')* '\'';
BINARY:              '0' 'X' HEX_DIGIT*;
FLOAT:               DEC_DOT_DEC;
REAL:                (DECIMAL | DEC_DOT_DEC) ('E' [+-]? DEC_DIGIT+);

EQUAL:               '=';

GREATER:             '>';
LESS:                '<';
EXCLAMATION:         '!';

PLUS_ASSIGN:         '+=';
MINUS_ASSIGN:        '-=';
MULT_ASSIGN:         '*=';
DIV_ASSIGN:          '/=';
MOD_ASSIGN:          '%=';
AND_ASSIGN:          '&=';
XOR_ASSIGN:          '^=';
OR_ASSIGN:           '|=';

DOUBLE_BAR:          '||';
DOT:                 '.';
DOLLAR:              '$';
LR_BRACKET:          '(';
RR_BRACKET:          ')';
COMMA:               ',';
STAR:                '*';
DIVIDE:              '/';
MODULE:              '%';
PLUS:                '+';
MINUS:               '-';
BIT_NOT:             '~';
BIT_OR:              '|';
BIT_AND:             '&';
BIT_XOR:             '^';
PARAM:               '?';

fragment LETTER:       [A-Z_];
fragment IPV6_OCTECT:  [0-9A-F][0-9A-F][0-9A-F][0-9A-F];
IPV4_OCTECT:           [0-9]?[0-9]?[0-9];
fragment DEC_DOT_DEC:  (DEC_DIGIT+ '.' DEC_DIGIT+ |  DEC_DIGIT+ '.' | '.' DEC_DIGIT+);
fragment HEX_DIGIT:    [0-9A-F];
fragment DEC_DIGIT:    [0-9];

fragment FullWidthLetter
    : '\u00c0'..'\u00d6'
    | '\u00d8'..'\u00f6'
    | '\u00f8'..'\u00ff'
    | '\u0100'..'\u1fff'
    | '\u2c00'..'\u2fff'
    | '\u3040'..'\u318f'
    | '\u3300'..'\u337f'
    | '\u3400'..'\u3fff'
    | '\u4e00'..'\u9fff'
    | '\ua000'..'\ud7ff'
    | '\uf900'..'\ufaff'
    | '\uff00'..'\ufff0'
    // | '\u10000'..'\u1F9FF'  //not support four bytes chars
    // | '\u20000'..'\u2FA1F'
    ;