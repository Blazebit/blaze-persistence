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

import jakarta.persistence.TupleElement;
import java.util.List;

/**
 * The {@code Selection} interface defines an item that is to be
 * returned in a query result.
 *
 * @param <X> the type of the selection item
 *
 * @since 2.0
 */
public interface Selection<X> extends TupleElement<X> {

    /**
     * Assigns an alias to the selection item.
     * Once assigned, an alias cannot be changed or reassigned.
     * Returns the same selection item.
     * @param name  alias
     * @return selection item 
     */
    Selection<X> alias(String name);

    /**
     * Whether the selection item is a compound selection.
     * @return boolean indicating whether the selection is a compound
     *         selection
     */
    boolean isCompoundSelection();

    /**
     * Return the selection items composing a compound selection.
     * Modifications to the list do not affect the query.
     * @return list of selection items
     * @throws IllegalStateException if selection is not a 
     *         compound selection
     */
    List<Selection<?>> getCompoundSelectionItems();
}
