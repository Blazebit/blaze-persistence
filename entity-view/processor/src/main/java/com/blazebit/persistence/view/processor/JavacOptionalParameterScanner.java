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

package com.blazebit.persistence.view.processor;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class JavacOptionalParameterScanner {

    private JavacOptionalParameterScanner() {
    }

    public static void scan(Map<String, TypeElement> optionalParameters, ExecutableElement executableElement, Context context) {
        Trees instance = Trees.instance(context.getProcessingEnvironment());
        TreePath path = instance.getPath(executableElement);
        MethodTree methodTree = (MethodTree) path.getLeaf();
        for (StatementTree statement : methodTree.getBody().getStatements()) {
            statement.accept(new TreeScanner<Void, Void>() {
                @Override
                public Void visitLiteral(LiteralTree literalTree, Void aVoid) {
                    if (literalTree.getKind() == Tree.Kind.STRING_LITERAL) {
                        OptionalParameterUtils.addOptionalParameters(optionalParameters, (String) literalTree.getValue(), context);
                    }
                    return super.visitLiteral(literalTree, aVoid);
                }
            }, null);
        }
    }
}
