/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public class ImplicitNamingStrategyComponentPathJoinTable
        extends ImplicitNamingStrategyComponentPathImpl {

    @Override
    public Identifier determineJoinTableName(
            ImplicitJoinTableNameSource source) {
        String name = source.getOwningPhysicalTableName() + "_"
                + source.getAssociationOwningAttributePath().getProperty();
        return this.toIdentifier(name, source.getBuildingContext());
    }

}
