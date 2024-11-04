/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.SerializerBase;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPQLOps;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.JPQLTemplates;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Slightly adjusted {@link JPQLSerializer} implementations that has
 * basic support for rendering set operations. Its only used for implementing
 * {@link AbstractBlazeJPAQuery#toString()} and debugging purposes.
 * The actual rendering of the query during execution is done in
 * {@link BlazeCriteriaBuilderRenderer}.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class JPQLNextSerializer extends JPQLSerializer {

    private static final Field BUILDER_FIELD;

    static {
        try {
            Field builderField = SerializerBase.class.getDeclaredField("builder");
            builderField.setAccessible(true);
            BUILDER_FIELD = builderField;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private final StringBuilder builder;

    public JPQLNextSerializer() {
        this(JPQLNextTemplates.DEFAULT);
    }

    public JPQLNextSerializer(JPQLTemplates templates) {
        super(templates);
        this.builder = getStringBuilder(this);
    }

    public JPQLNextSerializer(JPQLTemplates templates, EntityManager em) {
        super(templates, em);
        this.builder = getStringBuilder(this);
    }

    @Override
    public void serialize(QueryMetadata metadata, boolean forCountRow, @Nullable String projection) {
        SetOperationFlag setOperationFlag = SetOperationFlag.getSetOperationFlag(metadata);
        if (setOperationFlag != null) {
            setOperationFlag.getFlag().accept(this, null);
        } else {
            super.serialize(metadata, forCountRow, projection);
        }
    }

    @Override
    public Void visit(SubQueryExpression<?> query, Void context) {
        // Prevent wrapping in parens... However, this creates new exceptions
        // for IN/NOT IN subquery and possibly other operators.
        serialize(query.getMetadata(), false, null);
        return null;
    }

    @Override
    protected void visitOperation(Class<?> type, Operator operator, List<? extends Expression<?>> args) {
        boolean wrapInParens = false;

        if (operator == JPQLOps.CAST) {
            // JPQLSerializer replaces NUMCAST with CAST, which JPQL.Next actually doesn't support
            // JPQL.Next has its own CAST functions however, so use these if they can be found instead.
            try {
                operator = JPQLNextOps.valueOf("CAST_" + type.getSimpleName().toUpperCase());
                args = args.subList(0, 1);
            } catch (IllegalArgumentException e) {
                // no-op
            }
        } else if (operator == Ops.MOD && type != Integer.class) {
            // JPA enforces Integer as result for MOD, but Querydsl may expect any integer type.
            append("TREAT_" + type.getSimpleName().toUpperCase());
            wrapInParens = true;
        }

        if (wrapInParens) {
            append("(");
        }

        super.visitOperation(type, operator, args);

        if (wrapInParens) {
            append(")");
        }
    }

    /**
     * Clear the serialization buffer for serializing expression fragments rather than
     * full queries.
     *
     */
    public void clearBuffer() {
        builder.setLength(0);
    }

    /**
     * Take and clear the buffer.
     *
     * @return The removed buffer contents.
     */
    public String takeBuffer() {
        String res = builder.toString();
        clearBuffer();
        return res;
    }

    private static StringBuilder getStringBuilder(JPQLNextSerializer serializer) {
        try {
            return (StringBuilder) BUILDER_FIELD.get(serializer);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
