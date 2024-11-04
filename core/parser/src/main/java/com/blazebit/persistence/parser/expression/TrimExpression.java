/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    public TrimExpression copy(ExpressionCopyContext copyContext) {
        return new TrimExpression(trimspec, trimCharacter == null ? null : trimCharacter.copy(copyContext), trimSource.copy(copyContext));
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
