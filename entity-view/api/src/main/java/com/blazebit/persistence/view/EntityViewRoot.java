/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.JoinType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers an entity view root for the annotated entity view.
 * <br><br>
 * A view root can be either defined through an entity class with a condition:
 * <pre>
 * {@code
 * @EntityViewRoot(
 *     name = "root1",
 *     entity = Document.class,
 *     condition = "id = VIEW(documentId)"
 * )
 * }
 * </pre>
 * an expression with an optional condition:
 * <pre>
 * {@code
 * @EntityViewRoot(
 *     name = "root2",
 *     expression = "Document[id = VIEW(documentId)]",
 *     condition = "root2.age > 10"
 * )
 * }
 * </pre>
 * or through a correlator:
 * <pre>
 * {@code
 * @EntityViewRoot(
 *     name = "root3",
 *     correlator = MyCorrelationProvider.class
 * )
 * }
 * </pre>
 *
 * Paths that are not fully qualified i.e. relative paths that use no root alias, are prefixed with the entity view root alias.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EntityViewRoots.class)
public @interface EntityViewRoot {

    /**
     * The name of the entity view root through which it can be accessed in the entity view mappings.
     *
     * @return The entity view root name
     */
    String name();

    /**
     * The entity class for which to create this entity view root.
     * Can be omitted if a {@link #expression()} or {@link #correlator()} is specified.
     *
     * @return The entity class
     */
    Class<?> entity() default void.class;

    /**
     * The expression to use to create this entity view root.
     * Can be omitted if a {@link #entity()} or {@link #correlator()} is specified.
     *
     * Paths that are not fully qualified i.e. relative paths that use no root alias, are prefixed with the entity view root alias.
     *
     * @return The expression
     */
    String expression() default "";

    /**
     * The class which provides the correlation provider for this entity view root.
     * Can be omitted if a {@link #entity()} or {@link #expression()} is specified.
     *
     * @return The correlation provider
     */
    Class<? extends CorrelationProvider> correlator() default CorrelationProvider.class;

    /**
     * The condition expression to use for joining the entity view root.
     * It is illegal to use the condition when a {@link #correlator()} was specified.
     *
     * Paths that are not fully qualified i.e. relative paths that use no root alias, are prefixed with the entity view root alias.
     *
     * @return The condition expression
     */
    String condition() default "";

    /**
     * The join type to use for the entity view root.
     *
     * @return The join type
     */
    JoinType joinType() default JoinType.LEFT;

    /**
     * The associations of the entity that should be fetched.
     *
     * @return The associations of the entity that should be fetched
     */
    String[] fetches() default {};

    /**
     * The maximum amount of elements to fetch for the annotated attribute.
     * Can be an integer literal e.g. <code>5</code> or a parameter expression <code>:myParam</code>.
     *
     * @return The limit
     */
    String limit() default "";

    /**
     * The amount of elements to skip for the annotated attribute.
     * Can be an integer literal e.g. <code>5</code> or a parameter expression <code>:myParam</code>.
     *
     * @return The offset
     */
    String offset() default "";

    /**
     * The order to use for the elements for the limit. This will not necessarily order the elements in a collection!
     * The syntax is like for a JPQL.next order by item i.e. something like <code>age DESC NULLS LAST</code>.
     *
     * Paths that are not fully qualified i.e. relative paths that use no root alias, are prefixed with the entity view root alias.
     *
     * @return order to use for the limit
     */
    String[] order() default {};
}
