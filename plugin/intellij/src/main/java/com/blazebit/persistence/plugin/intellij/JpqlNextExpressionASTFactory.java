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
import com.blazebit.persistence.plugin.intellij.psi.IdentifierPSINode;
import com.intellij.core.CoreASTFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;

public class JpqlNextExpressionASTFactory extends CoreASTFactory {

    /** Create a FileElement for root or a parse tree CompositeElement (not
     *  PSI) for the token. This impl is more or less the default.
     */
    @Override
    public CompositeElement createComposite(IElementType type) {
        if (type instanceof IFileElementType) {
            return new FileElement(type, null);
        }
        return new CompositeElement(type);
    }

    /** Create PSI nodes out of tokens so even parse tree sees them as such.
     *  Does not see whitespace tokens.
     */
    @Override
    public LeafElement createLeaf(IElementType type, CharSequence text) {
        LeafElement t;
        if ( type == JpqlNextExpressionTokenTypes.TOKEN_ELEMENT_TYPES.get(JPQLNextLexer.IDENTIFIER) ) {
            t = new IdentifierPSINode(type, text);
        }
        else {
            t = super.createLeaf(type, text);
        }
        //		System.out.println("createLeaf "+t+" from "+type+" "+text);
        return t;
    }

}
