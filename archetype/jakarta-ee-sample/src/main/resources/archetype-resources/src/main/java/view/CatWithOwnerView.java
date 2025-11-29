/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.view;

import com.blazebit.persistence.view.*;
import ${package}.model.*;

@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {
    
    PersonSimpleView getOwner();

}
