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

import com.blazebit.persistence.plugin.intellij.JpqlNextExpressionFileType;
import com.blazebit.persistence.plugin.intellij.JpqlNextExpressionLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.intellij.adaptor.SymtabUtils;
import org.antlr.intellij.adaptor.psi.ScopeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JpqlNextExpressionPSIFileRoot extends PsiFileBase implements ScopeNode {

    public JpqlNextExpressionPSIFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, JpqlNextExpressionLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return JpqlNextExpressionFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Sample Language file";
    }

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Javaee.PersistenceEntity;
    }

    /** Return null since a file scope has no enclosing scope. It is
     *  not itself in a scope.
     */
    @Override
    public ScopeNode getContext() {
        return null;
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
        //		System.out.println(getClass().getSimpleName()+
        //		                   ".resolve("+element.getName()+
        //		                   " at "+Integer.toHexString(element.hashCode())+")");
//        if ( element.getParent() instanceof CallSubtree ) {
//            return SymtabUtils.resolve(this, SampleLanguage.INSTANCE,
//                    element, "/script/function/ID");
//        }
//        return SymtabUtils.resolve(this, SampleLanguage.INSTANCE,
//                element, "/script/vardef/ID");
        return null;
    }
}
