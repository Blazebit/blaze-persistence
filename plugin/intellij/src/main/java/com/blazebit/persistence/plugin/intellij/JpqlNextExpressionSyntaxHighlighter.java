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
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class JpqlNextExpressionSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public static final TextAttributesKey ID =
            createTextAttributesKey("JPQL_NEXT_EXPRESSION_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("JPQL_NEXT_EXPRESSION_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("JPQL_NEXT_EXPRESSION_STRING_LITERAL", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("JPQL_NEXT_EXPRESSION_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    static {
        PSIElementTypeFactory.defineLanguageIElementTypes(JpqlNextExpressionLanguage.INSTANCE,
                JPQLNextParser.tokenNames,
                JPQLNextParser.ruleNames);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        JPQLNextLexer lexer = new JPQLNextLexer(null);
        return new ANTLRLexerAdaptor(JpqlNextExpressionLanguage.INSTANCE, lexer);
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if ( !(tokenType instanceof TokenIElementType) ) return EMPTY_KEYS;
        TokenIElementType myType = (TokenIElementType)tokenType;
        int ttype = myType.getANTLRTokenType();
        TextAttributesKey attrKey;
        switch ( ttype ) {
            case JPQLNextLexer.IDENTIFIER :
                attrKey = ID;
                break;

            case JPQLNextLexer.AFTER:
            case JPQLNextLexer.ALL:
            case JPQLNextLexer.AND:
            case JPQLNextLexer.ANY:
            case JPQLNextLexer.AS:
            case JPQLNextLexer.ASC:
            case JPQLNextLexer.BEFORE:
            case JPQLNextLexer.BETWEEN:
            case JPQLNextLexer.BOTH:
            case JPQLNextLexer.BY:
            case JPQLNextLexer.CASE:
            case JPQLNextLexer.COLLATE:
            case JPQLNextLexer.CONTAINING:
            case JPQLNextLexer.COUNT:
            case JPQLNextLexer.CROSS:
            case JPQLNextLexer.CURRENT:
            case JPQLNextLexer.CURRENT_DATE:
            case JPQLNextLexer.CURRENT_INSTANT:
            case JPQLNextLexer.CURRENT_TIME:
            case JPQLNextLexer.CURRENT_TIMESTAMP:
            case JPQLNextLexer.DELETE:
            case JPQLNextLexer.DESC:
            case JPQLNextLexer.DISTINCT:
            case JPQLNextLexer.ELSE:
            case JPQLNextLexer.EMPTY:
            case JPQLNextLexer.END:
            case JPQLNextLexer.ENTRY:
            case JPQLNextLexer.ESCAPE:
            case JPQLNextLexer.EXCEPT:
            case JPQLNextLexer.EXCLUDE:
            case JPQLNextLexer.EXISTS:
            case JPQLNextLexer.FALSE:
            case JPQLNextLexer.FETCH:
            case JPQLNextLexer.FILTER:
            case JPQLNextLexer.FIRST:
            case JPQLNextLexer.FOLLOWING:
            case JPQLNextLexer.FROM:
            case JPQLNextLexer.FULL:
            case JPQLNextLexer.GROUP:
            case JPQLNextLexer.GROUPS:
            case JPQLNextLexer.HAVING:
            case JPQLNextLexer.IN:
            case JPQLNextLexer.INDEX:
            case JPQLNextLexer.INNER:
            case JPQLNextLexer.INSERT:
            case JPQLNextLexer.INTERSECT:
            case JPQLNextLexer.INTO:
            case JPQLNextLexer.IS:
            case JPQLNextLexer.JOIN:
            case JPQLNextLexer.JUMP:
            case JPQLNextLexer.KEY:
            case JPQLNextLexer.LAST:
            case JPQLNextLexer.LEADING:
            case JPQLNextLexer.LEFT:
            case JPQLNextLexer.LIKE:
            case JPQLNextLexer.LIMIT:
            case JPQLNextLexer.MEMBER:
            case JPQLNextLexer.NEW:
            case JPQLNextLexer.NO:
            case JPQLNextLexer.NOT:
            case JPQLNextLexer.NULL:
            case JPQLNextLexer.NULLS:
            case JPQLNextLexer.OBJECT:
            case JPQLNextLexer.OF:
            case JPQLNextLexer.OFFSET:
            case JPQLNextLexer.OLD:
            case JPQLNextLexer.ON:
            case JPQLNextLexer.OR:
            case JPQLNextLexer.ORDER:
            case JPQLNextLexer.OTHERS:
            case JPQLNextLexer.OUTER:
            case JPQLNextLexer.OVER:
            case JPQLNextLexer.PAGE:
            case JPQLNextLexer.PARTITION:
            case JPQLNextLexer.PRECEDING:
            case JPQLNextLexer.RANGE:
            case JPQLNextLexer.RECURSIVE:
            case JPQLNextLexer.RETURNING:
            case JPQLNextLexer.RIGHT:
            case JPQLNextLexer.ROW:
            case JPQLNextLexer.ROWS:
            case JPQLNextLexer.SELECT:
            case JPQLNextLexer.SET:
            case JPQLNextLexer.SOME:
            case JPQLNextLexer.THEN:
            case JPQLNextLexer.TIES:
            case JPQLNextLexer.TO:
            case JPQLNextLexer.TRAILING:
            case JPQLNextLexer.TREAT:
            case JPQLNextLexer.TRIM:
            case JPQLNextLexer.TRUE:
            case JPQLNextLexer.TYPE:
            case JPQLNextLexer.UNBOUNDED:
            case JPQLNextLexer.UNION:
            case JPQLNextLexer.UPDATE:
            case JPQLNextLexer.VALUE:
            case JPQLNextLexer.VALUES:
            case JPQLNextLexer.WHEN:
            case JPQLNextLexer.WHERE:
            case JPQLNextLexer.WINDOW:
            case JPQLNextLexer.WITH:
                attrKey = KEYWORD;
                break;
            case JPQLNextLexer.STRING_LITERAL :
            case JPQLNextLexer.CHARACTER_LITERAL:
                attrKey = STRING;
                break;
            case JPQLNextLexer.BIG_DECIMAL_LITERAL:
            case JPQLNextLexer.LONG_LITERAL:
            case JPQLNextLexer.BIG_INTEGER_LITERAL:
            case JPQLNextLexer.DOUBLE_LITERAL:
            case JPQLNextLexer.FLOAT_LITERAL:
            case JPQLNextLexer.INTEGER_LITERAL:
                attrKey = NUMBER;
                break;
            default :
                return EMPTY_KEYS;
        }
        return new TextAttributesKey[] {attrKey};
    }
}
