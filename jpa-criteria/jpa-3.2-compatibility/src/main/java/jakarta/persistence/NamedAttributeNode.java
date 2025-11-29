/*
 * Copyright (c) 2011, 2023 Oracle and/or its affiliates. All rights reserved.
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


package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A {@code NamedAttributeNode} is a member element of a
 * {@link NamedEntityGraph}.
 *
 * @see NamedEntityGraph
 * @see NamedSubgraph
 *
 * @since 2.1
 */
@Target({})
@Retention(RUNTIME)
public @interface NamedAttributeNode {

    /**
     * (Required) The name of the attribute that must be included in
     * the graph.
     */
    String value();

    /**
     * (Optional) If the attribute references a managed type that has
     * its own AttributeNodes, this element is used to refer to that
     * {@link NamedSubgraph} definition.
     * If the target type has inheritance, multiple subgraphs can
     * be specified. These additional subgraphs are intended to add
     * subclass-specific attributes. Superclass subgraph entries will
     * be merged into subclass subgraphs.  
     *
     * <p> The value of this element is the name of the subgraph as
     * specified by the {@code name} element of the corresponding
     * {@code NamedSubgraph} element.  If multiple subgraphs are
     * specified due to inheritance, they are referenced by this name.
     */
    String subgraph() default "";

   /**
    * (Optional) If the attribute references a Map type, this element
    * can be used to specify a subgraph for the Key in the case of an
    * Entity key type. A {@code keySubgraph} can not be specified
    * without the {@code Map} attribute also being specified. If the
    * target type has inheritance, multiple subgraphs can be specified.
    * These additional subgraphs are intended to add subclass-specific
    * attributes. Superclass subgraph entries are merged into subclass
    * subgraphs.
    * 
    * <p> The value of this element is the name of the key subgraph as
    * specified by the {@code name} element of the corresponding
    * {@link NamedSubgraph} element. If multiple key subgraphs are
    * specified due to inheritance, they are referenced by this name.
    */
    String keySubgraph() default "";
}


