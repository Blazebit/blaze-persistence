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
package com.blazebit.persistence.plugin.intellij.psi;

import com.blazebit.persistence.plugin.intellij.JpqlNextExpressionLanguage;
import com.blazebit.persistence.plugin.intellij.JpqlNextExpressionParserDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.psi.ANTLRPsiLeafNode;
import org.antlr.intellij.adaptor.psi.Trees;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class IdentifierPSINode extends ANTLRPsiLeafNode implements PsiNamedElement {

    public IdentifierPSINode(IElementType type, CharSequence text) {
        super(type, text);
    }

    @Override
    public String getName() {
        return getText();
    }

    /** Alter this node to have text specified by the argument. Do this by
     *  creating a new node through parsing of an ID and then doing a
     *  replace.
     */
    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if ( getParent()==null ) return this; // weird but it happened once
		/*
		IElementType elType = getParent().getNode().getElementType();
		String kind = "??? ";
		if ( elType instanceof RuleIElementType ) {
			int ruleIndex = ((RuleIElementType) elType).getRuleIndex();
			if ( ruleIndex == RULE_call_expr ) {
				kind = "call ";
			}
			else if ( ruleIndex == RULE_statement ) {
				kind = "assign ";
			}
			else if ( ruleIndex == RULE_function ) {
				kind = "func def ";
			}
		}
		System.out.println("IdentifierPSINode.setName("+name+") on "+
			                   kind+this+" at "+Integer.toHexString(this.hashCode()));
		*/
        PsiElement newID = Trees.createLeafFromText(getProject(),
                JpqlNextExpressionLanguage.INSTANCE,
                getContext(),
                name,
                JpqlNextExpressionParserDefinition.ID);
        if ( newID!=null ) {
            return this.replace(newID); // use replace on leaves but replaceChild on ID nodes that are part of defs/decls.
        }
        return this;
    }

    /** Create and return a PsiReference object associated with this ID
     *  node. The reference object will be asked to resolve this ref
     *  by using the text of this node to identify the appropriate definition
     *  site. The definition site is typically a subtree for a function
     *  or variable definition whereas this reference is just to this ID
     *  leaf node.
     *
     *  As the AST factory has no context and cannot create different kinds
     *  of PsiNamedElement nodes according to context, every ID node
     *  in the tree will be of this type. So, we distinguish references
     *  from definitions or other uses by looking at context in this method
     *  as we have parent (context) information.
     */
    @Override
    public PsiReference getReference() {
        PsiElement parent = getParent();
        IElementType elType = parent.getNode().getElementType();
        // do not return a reference for the ID nodes in a definition
//        if ( elType instanceof RuleIElementType) {
//            switch ( ((RuleIElementType) elType).getRuleIndex() ) {
//                case RULE_statement :
//                case RULE_expr :
//                case RULE_primary :
//                    return new VariableRef(this);
//                case RULE_call_expr :
//                    return new FunctionRef(this);
//            }
//        }
        return null;
    }
}
