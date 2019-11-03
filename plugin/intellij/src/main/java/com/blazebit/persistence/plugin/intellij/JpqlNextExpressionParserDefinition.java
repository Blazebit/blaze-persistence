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
import com.blazebit.persistence.plugin.intellij.psi.JpqlNextExpressionPSIFileRoot;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import com.blazebit.persistence.parser.antlr.Parser;
import com.blazebit.persistence.parser.antlr.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JpqlNextExpressionParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE =
            new IFileElementType(JpqlNextExpressionLanguage.INSTANCE);

    public static TokenIElementType ID;

    static {
        PSIElementTypeFactory.defineLanguageIElementTypes(JpqlNextExpressionLanguage.INSTANCE,
                JPQLNextParser.tokenNames,
                JPQLNextParser.ruleNames);
        List<TokenIElementType> tokenIElementTypes =
                PSIElementTypeFactory.getTokenIElementTypes(JpqlNextExpressionLanguage.INSTANCE);
        ID = tokenIElementTypes.get(JPQLNextLexer.IDENTIFIER);
    }

    public static final TokenSet COMMENTS =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE
//                    ,
//                    JPQLNextLexer.COMMENT,
//                    JPQLNextLexer.LINE_COMMENT
            );

    public static final TokenSet WHITESPACE =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE,
                    JPQLNextLexer.WS);

    public static final TokenSet STRING =
            PSIElementTypeFactory.createTokenSet(
                    JpqlNextExpressionLanguage.INSTANCE,
                    JPQLNextLexer.STRING_LITERAL);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        JPQLNextLexer lexer = new JPQLNextLexer(null);
        return new ANTLRLexerAdaptor(JpqlNextExpressionLanguage.INSTANCE, lexer);
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        final JPQLNextParser parser = new JPQLNextParser(null);
        return new ANTLRParserAdaptor(JpqlNextExpressionLanguage.INSTANCE, parser) {
            @Override
            protected ParseTree parse(Parser parser, IElementType root) {
                // start rule depends on root passed in; sometimes we want to create an ID node etc...
//                if ( root instanceof IFileElementType ) {
//                    return ((JPQLNextParser) parser).script();
//                }
                // let's hope it's an ID as needed by "rename function"
                return ((JPQLNextParser) parser).parseExpression();
            }
        };
    }

    /** "Tokens of those types are automatically skipped by PsiBuilder." */
    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRING;
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    /** What is the IFileElementType of the root parse tree node? It
     *  is called from {@link #createFile(FileViewProvider)} at least.
     */
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    /** Create the root of your PSI tree (a PsiFile).
     *
     *  From IntelliJ IDEA Architectural Overview:
     *  "A PSI (Program Structure Interface) file is the root of a structure
     *  representing the contents of a file as a hierarchy of elements
     *  in a particular programming language."
     *
     *  PsiFile is to be distinguished from a FileASTNode, which is a parse
     *  tree node that eventually becomes a PsiFile. From PsiFile, we can get
     *  it back via: {@link PsiFile#getNode}.
     */
    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new JpqlNextExpressionPSIFileRoot(viewProvider);
    }

    /** Convert from *NON-LEAF* parse node (AST they call it)
     *  to PSI node. Leaves are created in the AST factory.
     *  Rename re-factoring can cause this to be
     *  called on a TokenIElementType since we want to rename ID nodes.
     *  In that case, this method is called to create the root node
     *  but with ID type. Kind of strange, but we can simply create a
     *  ASTWrapperPsiElement to make everything work correctly.
     *
     *  RuleIElementType.  Ah! It's that ID is the root
     *  IElementType requested to parse, which means that the root
     *  node returned from parsetree to PSI conversion.  But, it
     *  must be a CompositeElement! The adaptor calls
     *  rootMarker.done(root) to finish off the PSI conversion.
     *  See {@link ANTLRParserAdaptor#parse(IElementType root, com.intellij.lang.PsiBuilder)}
     *
     *  If you don't care to distinguish PSI nodes by type, it is
     *  sufficient to create a {@link ANTLRPsiNode} around
     *  the parse tree node
     */
    @NotNull
    public PsiElement createElement(ASTNode node) {
        IElementType elType = node.getElementType();
        if ( elType instanceof TokenIElementType) {
            return new ANTLRPsiNode(node);
        }
        if ( !(elType instanceof RuleIElementType) ) {
            return new ANTLRPsiNode(node);
        }
        RuleIElementType ruleElType = (RuleIElementType) elType;
        switch ( ruleElType.getRuleIndex() ) {
//            case SampleLanguageParser.RULE_function :
//                return new FunctionSubtree(node, elType);
//            case SampleLanguageParser.RULE_vardef :
//                return new VardefSubtree(node, elType);
//            case SampleLanguageParser.RULE_formal_arg :
//                return new ArgdefSubtree(node, elType);
//            case SampleLanguageParser.RULE_block :
//                return new BlockSubtree(node);
//            case SampleLanguageParser.RULE_call_expr :
//                return new CallSubtree(node);
            default :
                return new ANTLRPsiNode(node);
        }
    }
}
