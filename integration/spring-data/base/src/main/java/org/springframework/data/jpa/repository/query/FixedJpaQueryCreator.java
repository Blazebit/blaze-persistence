/*
 * Copyright 2014 - 2018 Blazebit.
 * Copyright 2010-2014 the original author or authors.
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

package org.springframework.data.jpa.repository.query;

import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.springframework.data.jpa.repository.query.QueryUtils.toExpressionRecursively;
import static org.springframework.data.repository.query.parser.Part.Type.NOT_CONTAINING;
import static org.springframework.data.repository.query.parser.Part.Type.NOT_LIKE;

/**
 * Query creator to create a {@link CriteriaQuery} from a {@link PartTree}.
 *
 * Moritz Becker: Changed inner PredicateBuilder to work around an EclipseLink bug.
 *
 * @author Oliver Gierke
 * @author Moritz Becker
 *
 * @since 1.2.0
 */
public class FixedJpaQueryCreator extends AbstractQueryCreator<CriteriaQuery<Object>, Predicate> {
    private final CriteriaBuilder builder;
    private final Root<?> root;
    private final CriteriaQuery<Object> query;
    private final ParameterMetadataProvider provider;

    public FixedJpaQueryCreator(PartTree tree, Class<?> domainClass, CriteriaBuilder builder,
                           ParameterMetadataProvider provider) {
        super(tree);

        this.builder = builder;
        this.query = builder.createQuery().distinct(tree.isDistinct());
        this.root = query.from(domainClass);
        this.provider = provider;
    }

    /**
     * Returns all {@link javax.persistence.criteria.ParameterExpression} created when creating the query.
     *
     * @return the parameterExpressions
     */
    public List<ParameterMetadataProvider.ParameterMetadata<?>> getParameterExpressions() {
        return provider.getExpressions();
    }

    @Override
    protected Predicate create(Part part, Iterator<Object> iterator) {
        return toPredicate(part, root);
    }

    @Override
    protected Predicate and(Part part, Predicate base, Iterator<Object> iterator) {
        return builder.and(base, toPredicate(part, root));
    }

    @Override
    protected Predicate or(Predicate base, Predicate predicate) {
        return builder.or(base, predicate);
    }

    /**
     * Finalizes the given {@link Predicate} and applies the given sort. Delegates to
     * {@link #complete(Predicate, Sort, CriteriaQuery, CriteriaBuilder, Root)} and hands it the current {@link CriteriaQuery}
     * and {@link CriteriaBuilder}.
     */
    @Override
    protected final CriteriaQuery<Object> complete(Predicate predicate, Sort sort) {
        return complete(predicate, sort, query, builder, root);
    }

    /**
     * Template method to finalize the given {@link Predicate} using the given {@link CriteriaQuery} and
     * {@link CriteriaBuilder}.
     *
     * @param predicate
     * @param sort
     * @param query
     * @param builder
     * @return
     */
    protected CriteriaQuery<Object> complete(Predicate predicate, Sort sort, CriteriaQuery<Object> query,
                                             CriteriaBuilder builder, Root<?> root) {
        CriteriaQuery<Object> select = this.query.select(root).orderBy(QueryUtils.toOrders(sort, root, builder));
        return predicate == null ? select : select.where(predicate);
    }

    /**
     * Creates a {@link Predicate} from the given {@link Part}.
     *
     * @param part
     * @param root
     * @return
     */
    private Predicate toPredicate(Part part, Root<?> root) {
        return new FixedJpaQueryCreator.PredicateBuilder(part, root).build();
    }

    /**
     * Simple builder to contain logic to create JPA {@link Predicate}s from {@link Part}s.
     *
     * Moritz Becker: Rewrote NOT and NOT_IN cases to explicitely cast criteria IN argument to Expression&lt;Collection&lt;?&gt;&gt; which is needed
     * to work around an EclipseLink bug.
     *
     * @author Phil Webb
     * @author Oliver Gierke
     * @author Moritz Becker
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private class PredicateBuilder {

        private final Part part;
        private final Root<?> root;

        /**
         * Creates a new {@link JpaQueryCreator.PredicateBuilder} for the given {@link Part} and {@link Root}.
         *
         * @param part must not be {@literal null}.
         * @param root must not be {@literal null}.
         */
        public PredicateBuilder(Part part, Root<?> root) {

            Assert.notNull(part);
            Assert.notNull(root);
            this.part = part;
            this.root = root;
        }

        /**
         * Builds a JPA {@link Predicate} from the underlying {@link Part}.
         *
         * @return
         */
        public Predicate build() {

            PropertyPath property = part.getProperty();
            Part.Type type = part.getType();

            switch (type) {
                case BETWEEN:
                    ParameterMetadataProvider.ParameterMetadata<Comparable> first = provider.next(part);
                    ParameterMetadataProvider.ParameterMetadata<Comparable> second = provider.next(part);
                    return builder.between(getComparablePath(root, part), first.getExpression(), second.getExpression());
                case AFTER:
                case GREATER_THAN:
                    return builder.greaterThan(getComparablePath(root, part),
                            provider.next(part, Comparable.class).getExpression());
                case GREATER_THAN_EQUAL:
                    return builder.greaterThanOrEqualTo(getComparablePath(root, part),
                            provider.next(part, Comparable.class).getExpression());
                case BEFORE:
                case LESS_THAN:
                    return builder.lessThan(getComparablePath(root, part), provider.next(part, Comparable.class).getExpression());
                case LESS_THAN_EQUAL:
                    return builder.lessThanOrEqualTo(getComparablePath(root, part),
                            provider.next(part, Comparable.class).getExpression());
                case IS_NULL:
                    return getTypedPath(root, part).isNull();
                case IS_NOT_NULL:
                    return getTypedPath(root, part).isNotNull();
                /************************************************
                 * Moritz Becker:
                 * Added cast to Expression<Collection<?>> to work around an EclipseLink bug.
                 ************************************************/
                case NOT_IN:
                    return getTypedPath(root, part).in((Expression<Collection<?>>) provider.next(part, Collection.class).getExpression()).not();
                case IN:
                    return getTypedPath(root, part).in((Expression<Collection<?>>) provider.next(part, Collection.class).getExpression());
                /************************************************
                 * end of changes
                 ************************************************/
                //CHECKSTYLE:OFF: checkstyle:FallThrough
                case STARTING_WITH:
                case ENDING_WITH:
                case CONTAINING:
                case NOT_CONTAINING:

                    if (property.getLeafProperty().isCollection()) {

                        Expression<Collection<Object>> propertyExpression = traversePath(root, property);
                        Expression<Object> parameterExpression = provider.next(part).getExpression();

                        // Can't just call .not() in case of negation as EclipseLink chokes on that.
                        return type.equals(NOT_CONTAINING) ? builder.isNotMember(parameterExpression, propertyExpression)
                                : builder.isMember(parameterExpression, propertyExpression);
                    }
                //CHECKSTYLE:OFF: checkstyle:FallThrough
                case LIKE:
                case NOT_LIKE:
                    Expression<String> stringPath = getTypedPath(root, part);
                    Expression<String> propertyExpression = upperIfIgnoreCase(stringPath);
                    Expression<String> parameterExpression = upperIfIgnoreCase(provider.next(part, String.class).getExpression());
                    Predicate like = builder.like(propertyExpression, parameterExpression);
                    return type.equals(NOT_LIKE) || type.equals(NOT_CONTAINING) ? like.not() : like;
                case TRUE:
                    Expression<Boolean> truePath = getTypedPath(root, part);
                    return builder.isTrue(truePath);
                case FALSE:
                    Expression<Boolean> falsePath = getTypedPath(root, part);
                    return builder.isFalse(falsePath);
                case SIMPLE_PROPERTY:
                    ParameterMetadataProvider.ParameterMetadata<Object> expression = provider.next(part);
                    Expression<Object> path = getTypedPath(root, part);
                    return expression.isIsNullParameter() ? path.isNull()
                            : builder.equal(upperIfIgnoreCase(path), upperIfIgnoreCase(expression.getExpression()));
                case NEGATING_SIMPLE_PROPERTY:
                    return builder.notEqual(upperIfIgnoreCase(getTypedPath(root, part)),
                            upperIfIgnoreCase(provider.next(part).getExpression()));
                default:
                    throw new IllegalArgumentException("Unsupported keyword " + type);
            }
        }

        /**
         * Applies an {@code UPPERCASE} conversion to the given {@link Expression} in case the underlying {@link Part}
         * requires ignoring case.
         *
         * @param expression must not be {@literal null}.
         * @return
         */
        private <T> Expression<T> upperIfIgnoreCase(Expression<? extends T> expression) {

            switch (part.shouldIgnoreCase()) {

                case ALWAYS:

                    Assert.state(canUpperCase(expression), "Unable to ignore case of " + expression.getJavaType().getName()
                            + " types, the property '" + part.getProperty().getSegment() + "' must reference a String");
                    return (Expression<T>) builder.upper((Expression<String>) expression);

                case WHEN_POSSIBLE:

                    if (canUpperCase(expression)) {
                        return (Expression<T>) builder.upper((Expression<String>) expression);
                    }

                case NEVER:
                default:

                    return (Expression<T>) expression;
            }
        }

        private boolean canUpperCase(Expression<?> expression) {
            return String.class.equals(expression.getJavaType());
        }

        /**
         * Returns a path to a {@link Comparable}.
         *
         * @param root
         * @param part
         * @return
         */
        private Expression<? extends Comparable> getComparablePath(Root<?> root, Part part) {
            return getTypedPath(root, part);
        }

        private <T> Expression<T> getTypedPath(Root<?> root, Part part) {
            return toExpressionRecursively(root, part.getProperty());
        }

        private <T> Expression<T> traversePath(Path<?> root, PropertyPath path) {

            Path<Object> result = root.get(path.getSegment());
            return (Expression<T>) (path.hasNext() ? traversePath(result, path.next()) : result);
        }
    }
}
