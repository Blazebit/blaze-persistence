/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Gavin King      - 3.2

package jakarta.persistence.criteria;

/**
 * Specifies the precedence of null values within query result sets.
 *
 * @see CriteriaBuilder#asc(Expression, Nulls)
 * @see CriteriaBuilder#desc(Expression, Nulls)
 *
 * @since 3.2
 */
public enum Nulls {
    /**
     * Null precedence not specified.
     */
    NONE,
    /**
     * Null values occur at the beginning of the result set.
     */
    FIRST,
    /**
     * Null values occur at the end of the result set.
     */
    LAST
}