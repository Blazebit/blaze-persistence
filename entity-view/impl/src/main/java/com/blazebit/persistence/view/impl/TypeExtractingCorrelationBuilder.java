/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.From;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.Path;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class TypeExtractingCorrelationBuilder implements CorrelationBuilder, FromProvider {

    private static final Logger LOG = Logger.getLogger(TypeExtractingCorrelationBuilder.class.getName());

    private final String viewRootName;
    private final MetamodelBuildingContext context;
    private final ScalarTargetResolvingExpressionVisitor resolver;

    private TypeExtractingCorrelationBuilder(String viewRootName, MetamodelBuildingContext context, ScalarTargetResolvingExpressionVisitor resolver) {
        this.viewRootName = viewRootName;
        this.context = context;
        this.resolver = resolver;
    }

    public static Type<?> extractType(CorrelationProviderFactory factory, String viewRootName, MetamodelBuildingContext context, ScalarTargetResolvingExpressionVisitor resolver) {
        TypeExtractingCorrelationBuilder correlationBuilder = new TypeExtractingCorrelationBuilder(viewRootName, context, resolver);
        try {
            factory.create(SimpleParameterHolder.INSTANCE, Collections.<String, Object>emptyMap()).applyCorrelation(correlationBuilder, "alias");
        } catch (TypeExtractingException ex) {
            if (ex.type == null) {
                LOG.log(Level.FINEST, "Couldn't determine type", ex);
            }
            return ex.type;
        } catch (InvalidManagedTypeException ex) {
            throw ex;
        } catch (Throwable ex) {
            LOG.log(Level.FINEST, "Couldn't determine type", ex);
        }
        return null;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        if (ExpressionFactory.class.equals(serviceClass)) {
            return (T) context.getTypeExtractionExpressionFactory();
        }
        return null;
    }

    @Override
    public FromProvider getCorrelationFromProvider() {
        return this;
    }

    @Override
    public String getCorrelationAlias() {
        return viewRootName;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        ManagedType<?> managedType;
        try {
            managedType = context.getEntityMetamodel().managedType(entityClass);
        } catch (Exception ex) {
            throw new InvalidManagedTypeException();
        }
        throw new TypeExtractingException(managedType);
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
        throw new TypeExtractingException(entityType);
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(String correlationPath) {
        Expression joinPathExpression = context.getTypeValidationExpressionFactory().createJoinPathExpression(correlationPath);
        resolver.clear();
        joinPathExpression.accept(resolver);
        List<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTargetTypes = resolver.getPossibleTargetTypes();
        if (possibleTargetTypes.size() == 1) {
            Type<?> type;
            try {
                type = context.getEntityMetamodel().type(possibleTargetTypes.get(0).getLeafBaseValueClass());
            } catch (Exception ex) {
                throw new InvalidManagedTypeException();
            }
            throw new TypeExtractingException(type);
        }
        throw new TypeExtractingException(null);
    }

    @Override
    public Set<From> getRoots() {
        return Collections.emptySet();
    }

    @Override
    public From getFrom(String alias) {
        return null;
    }

    @Override
    public From getFromByPath(String path) {
        return null;
    }

    @Override
    public Path getPath(String path) {
        return null;
    }

    /**
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static class TypeExtractingException extends RuntimeException {

        private final Type<?> type;

        public TypeExtractingException(Type<?> type) {
            this.type = type;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static class InvalidManagedTypeException extends RuntimeException {
    }
}
