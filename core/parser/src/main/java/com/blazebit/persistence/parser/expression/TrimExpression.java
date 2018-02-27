/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.parser.expression;


/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TrimExpression extends AbstractExpression {

    private final Trimspec trimspec;
    private Expression trimCharacter;
    private Expression trimSource;

    public TrimExpression(Trimspec trimspec, Expression trimCharacter, Expression trimSource) {
        this.trimspec = trimspec;
        this.trimCharacter = trimCharacter;
        this.trimSource = trimSource;
    }

    public Trimspec getTrimspec() {
        return trimspec;
    }

    public Expression getTrimCharacter() {
        return trimCharacter;
    }

    public void setTrimCharacter(Expression trimCharacter) {
        this.trimCharacter = trimCharacter;
    }

    public Expression getTrimSource() {
        return trimSource;
    }

    public void setTrimSource(Expression trimSource) {
        this.trimSource = trimSource;
    }

    @Override
    public TrimExpression clone(boolean resolved) {
        return new TrimExpression(trimspec, trimCharacter == null ? null : trimCharacter.clone(resolved), trimSource.clone(resolved));
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
