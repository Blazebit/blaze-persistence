/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cpbec
 */
//TODO: maybe implement contacts[1] = x1 AND contacts[2] = x2?
public class ArrayExpressionTransformer implements ExpressionTransformer {

    private final Map<TransformationInfo, EqPredicate> transformedPathFilterMap = new HashMap<TransformationInfo, EqPredicate>();
    private final JoinManager joinManager;

    public ArrayExpressionTransformer(JoinManager joinManager) {
        this.joinManager = joinManager;
    }

    @Override
    public Expression transform(Expression original) {
        return transform(original, false);
    }
    
    @Override
    public Expression transform(Expression original, boolean selectClause) {
        if (original instanceof CompositeExpression) {
            CompositeExpression composite = (CompositeExpression) original;
            CompositeExpression transformed = new CompositeExpression(new ArrayList<Expression>());
            for (Expression e : composite.getExpressions()) {
                transformed.getExpressions().add(transform(e, selectClause));
            }
            return transformed;
        }

        if (!(original instanceof PathExpression)) {
            return original;
        }

        PathExpression path = (PathExpression) original;
        ArrayExpression arrayExp = null;
        ArrayExpression farRightArrayExp = null;
        PathExpression farRightValuePath = null;
        EqPredicate farRightValueKeyFilter = null;

        String absBasePath;
        int loopEndIndex = 0;
        if (path.getBaseNode() != null) {
            absBasePath = path.getBaseNode().getAliasInfo().getAbsolutePath();
            
            if (path.getField() != null) {
                absBasePath += "." + path.getField();
            }
            
            if(path.getExpressions().get(0).toString().equals(joinManager.getRootAlias())){
                loopEndIndex = 1;
            }
        } else {
            // this case is for single select and join aliases
            return original;
        }

        //TODO: set baseNodes on created PathExpressions
        
        for (int i = path.getExpressions().size() - 1; i >= loopEndIndex; i--) {

            PathElementExpression expr = path.getExpressions().get(i);

            if (expr instanceof ArrayExpression) {
                arrayExp = (ArrayExpression) expr;

                String currentAbsPath = absBasePath;
                TransformationInfo transInfo = new TransformationInfo(currentAbsPath, arrayExp.getIndex().toString());
                EqPredicate valueKeyFilterPredicate;
                if ((valueKeyFilterPredicate = transformedPathFilterMap.get(transInfo)) == null) {
                    CompositeExpression keyExpression = new CompositeExpression(new ArrayList<Expression>());
                    keyExpression.getExpressions().add(new FooExpression("KEY("));

                    PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>());
                    keyPath.getExpressions().add(arrayExp.getBase());
                    keyExpression.getExpressions().add(keyPath);
                    keyExpression.getExpressions().add(new FooExpression(")"));
                    valueKeyFilterPredicate = new EqPredicate(keyExpression, arrayExp.getIndex());
                    
                    //TODO: change path.getBaseNode() to be the join node for the first path element
                    // so for contacts.partnerDocument.versions return contacts and not versions
                    joinManager.findNode(absBasePath).setWithPredicate(valueKeyFilterPredicate);
                    transformedPathFilterMap.put(transInfo, valueKeyFilterPredicate);

                }

                if (farRightArrayExp == null) {
                    farRightArrayExp = arrayExp;
                    farRightValueKeyFilter = valueKeyFilterPredicate;
                    // this is only necessary for correct map dereferencing output (e.g. VALUE(xy).someproperty) )
                    // however, such dereferencing is not supported by JPQL so we could also remove this
                    List<PathElementExpression> farRightValuePathElements = new ArrayList<PathElementExpression>();
                    for (int j = i + 1; j < path.getExpressions().size(); j++) {
                        farRightValuePathElements.add(path.getExpressions().get(j));
                    }
                    if (farRightValuePathElements.isEmpty() == false) {
                        farRightValuePath = new PathExpression(farRightValuePathElements);
                    }
                }

            }

            if (i == loopEndIndex) {
                absBasePath = "";
            } else {
                absBasePath = absBasePath.substring(0, absBasePath.lastIndexOf('.'));
            }
        }

        // convert occurrence of a.b.c.d[xy] to d and ab.c.d[xy].z to d.z
        if (farRightArrayExp != null) {
            // add value for last array expression
            CompositeExpression valueExpression = new CompositeExpression(new ArrayList<Expression>());
            
            PathExpression valuePath = new PathExpression();
            valuePath.getExpressions().add(farRightArrayExp.getBase());
            
           
            if (farRightValuePath != null) {
                valuePath.getExpressions().addAll(farRightValuePath.getExpressions());
                valueExpression.getExpressions().add(valuePath);
            } else {
                valueExpression.getExpressions().add(valuePath);
            }
            
            if(selectClause == true){
                farRightValueKeyFilter.setRequiredByMapValueSelect(true);
            }

            return valueExpression;
        }

        return original;
    }
    
    private static class TransformationInfo {

        public TransformationInfo(String absoluteFieldPath, String indexedField) {
            this.absoluteFieldPath = absoluteFieldPath;
            this.indexedField = indexedField;
        }

        private final String absoluteFieldPath;
        private final String indexedField;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.absoluteFieldPath != null ? this.absoluteFieldPath.hashCode() : 0);
            hash = 97 * hash + (this.indexedField != null ? this.indexedField.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TransformationInfo other = (TransformationInfo) obj;
            if ((this.absoluteFieldPath == null) ? (other.absoluteFieldPath != null) : !this.absoluteFieldPath.equals(other.absoluteFieldPath)) {
                return false;
            }
            if ((this.indexedField == null) ? (other.indexedField != null) : !this.indexedField.equals(other.indexedField)) {
                return false;
            }
            return true;
        }
    }
}
