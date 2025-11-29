/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.view;

import com.blazebit.persistence.view.*;
import ${package}.model.*;

@EntityView(Person.class)
public interface PersonSimpleView {
    
    @IdMapping
    Long getId();

    String getName();

}
