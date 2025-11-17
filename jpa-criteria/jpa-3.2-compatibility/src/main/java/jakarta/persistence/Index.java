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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used in schema generation to specify creation of an index.
 *
 * <p>The syntax of the {@code columnList} element is given by
 * the following simple BNF:
 * 
 * {@snippet :
 *     column_list ::= index_column [, index_column]*
 *     index_column ::= column_name [ASC | DESC]
 * }
 * 
 * <p>If neither {@code ASC} nor {@code DESC} is not specified,
 * {@code ASC}, that is, ascending order, is assumed.
 *
 * <p>Note that it is not necessary to specify an index for a
 * primary key, as the primary key has a unique constraint with
 * an index created automatically.
 *
 * @see Table
 * @see SecondaryTable
 * @see CollectionTable
 * @see JoinTable
 * @see TableGenerator
 *
 * @since 2.1
 *
 */
@Target({})
@Retention(RUNTIME)
public @interface Index {

    /**
     * (Optional) The name of the index.
     * <p> Defaults to a provider-generated name.
     */
    String name() default "";

    /**
     * (Required) The columns included in the index, in order,
     * following the BNF rule {@code column_list} given above.
     */
    String columnList();

    /**
     * (Optional) Whether the index is unique.
     */
    boolean unique() default false;

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * which creates this index.
     *
     * @since 3.2
     */
    String options() default "";
}
