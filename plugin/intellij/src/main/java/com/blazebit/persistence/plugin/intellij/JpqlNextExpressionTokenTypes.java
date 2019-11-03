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
package com.blazebit.persistence.plugin.intellij;

import com.blazebit.persistence.parser.JPQLNextLexer;
import com.blazebit.persistence.parser.JPQLNextParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.intellij.lang.annotations.MagicConstant;

import java.util.List;

public class JpqlNextExpressionTokenTypes {

    public static IElementType BAD_TOKEN_TYPE = new IElementType("BAD_TOKEN", JpqlNextExpressionLanguage.INSTANCE);

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES =
            PSIElementTypeFactory.getTokenIElementTypes(JpqlNextExpressionLanguage.INSTANCE);
    public static final List<RuleIElementType> RULE_ELEMENT_TYPES =
            PSIElementTypeFactory.getRuleIElementTypes(JpqlNextExpressionLanguage.INSTANCE);

    public static final TokenSet COMMENTS =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE
                    // We don't have any comments
//                    ,
//                    JPQLNextLexer.DOC_COMMENT,
//                    JPQLNextLexer.BLOCK_COMMENT,
//                    JPQLNextLexer.LINE_COMMENT
            );

    public static final TokenSet WHITESPACES =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE,
                    JPQLNextLexer.WS);

    public static final TokenSet KEYWORDS =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE,
                    JPQLNextLexer.AFTER,
                    JPQLNextLexer.ALL,
                    JPQLNextLexer.AND,
                    JPQLNextLexer.ANY,
                    JPQLNextLexer.AS,
                    JPQLNextLexer.ASC,
                    JPQLNextLexer.BEFORE,
                    JPQLNextLexer.BETWEEN,
                    JPQLNextLexer.BOTH,
                    JPQLNextLexer.BY,
                    JPQLNextLexer.CASE,
                    JPQLNextLexer.COLLATE,
                    JPQLNextLexer.CONTAINING,
                    JPQLNextLexer.COUNT,
                    JPQLNextLexer.CROSS,
                    JPQLNextLexer.CURRENT,
                    JPQLNextLexer.CURRENT_DATE,
                    JPQLNextLexer.CURRENT_INSTANT,
                    JPQLNextLexer.CURRENT_TIME,
                    JPQLNextLexer.CURRENT_TIMESTAMP,
                    JPQLNextLexer.DELETE,
                    JPQLNextLexer.DESC,
                    JPQLNextLexer.DISTINCT,
                    JPQLNextLexer.ELSE,
                    JPQLNextLexer.EMPTY,
                    JPQLNextLexer.END,
                    JPQLNextLexer.ENTRY,
                    JPQLNextLexer.ESCAPE,
                    JPQLNextLexer.EXCEPT,
                    JPQLNextLexer.EXCLUDE,
                    JPQLNextLexer.EXISTS,
                    JPQLNextLexer.FALSE,
                    JPQLNextLexer.FETCH,
                    JPQLNextLexer.FILTER,
                    JPQLNextLexer.FIRST,
                    JPQLNextLexer.FOLLOWING,
                    JPQLNextLexer.FROM,
                    JPQLNextLexer.FULL,
                    JPQLNextLexer.GROUP,
                    JPQLNextLexer.GROUPS,
                    JPQLNextLexer.HAVING,
                    JPQLNextLexer.IN,
                    JPQLNextLexer.INDEX,
                    JPQLNextLexer.INNER,
                    JPQLNextLexer.INSERT,
                    JPQLNextLexer.INTERSECT,
                    JPQLNextLexer.INTO,
                    JPQLNextLexer.IS,
                    JPQLNextLexer.JOIN,
                    JPQLNextLexer.JUMP,
                    JPQLNextLexer.KEY,
                    JPQLNextLexer.LAST,
                    JPQLNextLexer.LEADING,
                    JPQLNextLexer.LEFT,
                    JPQLNextLexer.LIKE,
                    JPQLNextLexer.LIMIT,
                    JPQLNextLexer.MEMBER,
                    JPQLNextLexer.NEW,
                    JPQLNextLexer.NO,
                    JPQLNextLexer.NOT,
                    JPQLNextLexer.NULL,
                    JPQLNextLexer.NULLS,
                    JPQLNextLexer.OBJECT,
                    JPQLNextLexer.OF,
                    JPQLNextLexer.OFFSET,
                    JPQLNextLexer.OLD,
                    JPQLNextLexer.ON,
                    JPQLNextLexer.OR,
                    JPQLNextLexer.ORDER,
                    JPQLNextLexer.OTHERS,
                    JPQLNextLexer.OUTER,
                    JPQLNextLexer.OVER,
                    JPQLNextLexer.PAGE,
                    JPQLNextLexer.PARTITION,
                    JPQLNextLexer.PRECEDING,
                    JPQLNextLexer.RANGE,
                    JPQLNextLexer.RECURSIVE,
                    JPQLNextLexer.RETURNING,
                    JPQLNextLexer.RIGHT,
                    JPQLNextLexer.ROW,
                    JPQLNextLexer.ROWS,
                    JPQLNextLexer.SELECT,
                    JPQLNextLexer.SET,
                    JPQLNextLexer.SOME,
                    JPQLNextLexer.THEN,
                    JPQLNextLexer.TIES,
                    JPQLNextLexer.TO,
                    JPQLNextLexer.TRAILING,
                    JPQLNextLexer.TREAT,
                    JPQLNextLexer.TRIM,
                    JPQLNextLexer.TRUE,
                    JPQLNextLexer.TYPE,
                    JPQLNextLexer.UNBOUNDED,
                    JPQLNextLexer.UNION,
                    JPQLNextLexer.UPDATE,
                    JPQLNextLexer.VALUE,
                    JPQLNextLexer.VALUES,
                    JPQLNextLexer.WHEN,
                    JPQLNextLexer.WHERE,
                    JPQLNextLexer.WINDOW,
                    JPQLNextLexer.WITH
            );

    public static RuleIElementType getRuleElementType(@MagicConstant(valuesFromClass = JPQLNextParser.class)int ruleIndex){
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }
    public static TokenIElementType getTokenElementType(@MagicConstant(valuesFromClass = JPQLNextLexer.class)int ruleIndex){
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
