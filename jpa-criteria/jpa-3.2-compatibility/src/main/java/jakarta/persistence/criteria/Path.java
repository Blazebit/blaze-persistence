/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence.criteria;

import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.MapAttribute;

/**
 * Represents a simple or compound attribute path from a 
 * bound type or collection, and is a "primitive" expression.
 *
 * @param <X>  the type referenced by the path
 *
 * @since 2.0
 */
public interface Path<X> extends Expression<X> {

    /** 
     * Return the bindable object that corresponds to the path
     * expression.
     * @return bindable object corresponding to the path
     */
    Bindable<X> getModel(); 
    
    /**
     * Return the parent "node" in the path or null if no parent.
     * @return parent
     */
    Path<?> getParentPath();
	
    /**
     * Create a path corresponding to the referenced
     * single-valued attribute.
     * @param attribute single-valued attribute
     * @return path corresponding to the referenced attribute
     */
    <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute);

    /**
     * Create a path corresponding to the referenced 
     * collection-valued attribute.
     * @param collection collection-valued attribute
     * @return expression corresponding to the referenced attribute
     */
    <E, C extends java.util.Collection<E>> Expression<C> get(PluralAttribute<? super X, C, E> collection);

    /**
     * Create a path corresponding to the referenced 
     * map-valued attribute.
     * @param map map-valued attribute
     * @return expression corresponding to the referenced attribute
     */
    <K, V, M extends java.util.Map<K, V>> Expression<M> get(MapAttribute<? super X, K, V> map);

    /**
     * Create an expression corresponding to the type of the path.
     * @return expression corresponding to the type of the path
     */
    Expression<Class<? extends X>> type();


    //String-based:
	
    /**
     * Create a path corresponding to the referenced attribute.
     * 
     * <p> Note: Applications using the string-based API may need to 
     * specify the type resulting from the {@link #get} operation in
     * order to avoid the use of {@code Path} variables.
     *
     * <p>For example:
     * {@snippet :
     * CriteriaQuery<Person> q = cb.createQuery(Person.class);
     * Root<Person> p = q.from(Person.class);
     * q.select(p)
     *  .where(cb.isMember("joe",
     *                     p.<Set<String>>get("nicknames")));
     * }
     * <p>rather than:
     * {@snippet :
     * CriteriaQuery<Person> q = cb.createQuery(Person.class);
     * Root<Person> p = q.from(Person.class);
     * Path<Set<String>> nicknames = p.get("nicknames");
     * q.select(p)
     *  .where(cb.isMember("joe", nicknames));
     * }
     *
     * @param attributeName  name of the attribute
     * @return path corresponding to the referenced attribute
     * @throws IllegalStateException if invoked on a path that
     *         corresponds to a basic type
     * @throws IllegalArgumentException if attribute of the given
     *         name does not otherwise exist
     */
    <Y> Path<Y> get(String attributeName);
}
