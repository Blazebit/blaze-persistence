/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class JavacOptionalParameterScanner {

    private JavacOptionalParameterScanner() {
    }

    public static void scan(Map<String, String> optionalParameters, ExecutableElement executableElement, Context context) {
        Trees instance = Trees.instance(context.getProcessingEnvironment());
        TreePath path = instance.getPath(executableElement);
        ClassTree classTree = (ClassTree) path.getParentPath().getLeaf();
        Map<Name, String> constants = new HashMap<>();
        for (Tree classTreeMember : classTree.getMembers()) {
            if (classTreeMember instanceof VariableTree) {
                VariableTree variableTree = (VariableTree) classTreeMember;
                Set<Modifier> flags = variableTree.getModifiers().getFlags();
                if (flags.contains(Modifier.STATIC) && flags.contains(Modifier.FINAL) && variableTree.getInitializer() instanceof LiteralTree) {
                    LiteralTree literalTree = (LiteralTree) variableTree.getInitializer();
                    constants.put(variableTree.getName(), (String) literalTree.getValue());
                }
            }
        }

        MethodTree methodTree = (MethodTree) path.getLeaf();
        for (StatementTree statement : methodTree.getBody().getStatements()) {
            OptionalParameterCollectingTreeScanner scanner = new OptionalParameterCollectingTreeScanner(optionalParameters, context, constants);
            statement.accept(scanner, null);
            if (scanner.last != null) {
                OptionalParameterUtils.addOptionalParameters(optionalParameters, scanner.last, context);
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static class OptionalParameterCollectingTreeScanner extends TreeScanner<String, String> {

        private final Map<String, String> optionalParameters;
        private final Context context;
        private final Map<Name, String> constants;
        private String last;

        public OptionalParameterCollectingTreeScanner(
                Map<String, String> optionalParameters,
                Context context,
                Map<Name, String> constants) {
            this.optionalParameters = optionalParameters;
            this.context = context;
            this.constants = constants;
        }

        @Override
        public String visitLiteral(LiteralTree literalTree, String aVoid) {
            if (literalTree.getKind() == Tree.Kind.STRING_LITERAL) {
                if (last != null) {
                    OptionalParameterUtils.addOptionalParameters( optionalParameters, last, context );
                }
                return last = (String) literalTree.getValue();
            }
            return super.visitLiteral(literalTree, aVoid);
        }

        @Override
        public String visitIdentifier(IdentifierTree identifierTree, String s) {
            String value = constants.get( identifierTree.getName());
            if (last != null) {
                OptionalParameterUtils.addOptionalParameters(optionalParameters, last, context);
            }
            return last = value;
        }

        @Override
        public String visitBinary(BinaryTree binaryTree, String s) {
            if (binaryTree.getKind() == Tree.Kind.PLUS) {
                if (last != null) {
                    OptionalParameterUtils.addOptionalParameters(optionalParameters, last, context);
                }
                last = null;
                String lhs = scan(binaryTree.getLeftOperand(), null);
                last = null;
                String rhs = scan(binaryTree.getRightOperand(), null);
                if (lhs == null) {
                    return last = rhs;
                } else if (rhs == null) {
                    return last = lhs;
                } else {
                    return last = lhs + rhs;
                }
            }

            return super.visitBinary(binaryTree, s);
        }
    }
}
