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

package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TrimExpression extends FunctionExpression {

    private final Trimspec trimspec;
    private final Expression trimCharacter;
    private final Expression trimSource;

    public TrimExpression(Trimspec trimspec, Expression trimCharacter, Expression trimSource) {
        super("TRIM", trimArguments(trimspec, trimCharacter, trimSource));
        this.trimspec = trimspec;
        this.trimCharacter = trimCharacter;
        this.trimSource = trimSource;
    }
    
    private static List<Expression> trimArguments(Trimspec trimspec, Expression trimCharacter, Expression trimSource) {
        CompositeExpression compositeExpression = new CompositeExpression(new ArrayList<Expression>());
        compositeExpression.append(trimspec.name() + " ");
        
        if (trimCharacter != null) {
            compositeExpression.append(trimCharacter);
            compositeExpression.append(" ");
        }
        
        compositeExpression.append("FROM ");
        compositeExpression.append(trimSource);
        
        return Arrays.asList((Expression) compositeExpression);
    }

    @Override
    public TrimExpression clone() {
        return new TrimExpression(trimspec, trimCharacter == null ? null : trimCharacter.clone(), trimSource.clone());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
