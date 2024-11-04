/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus;

import com.blazebit.persistence.integration.jpa.JpaMetamodelAccessorImpl;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public class DataNucleus51JpaMetamodelAccessor extends JpaMetamodelAccessorImpl {

    public static final DataNucleus51JpaMetamodelAccessor INSTANCE = new DataNucleus51JpaMetamodelAccessor();

    private DataNucleus51JpaMetamodelAccessor() {
    }


    @Override
    public boolean isJoinable(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            return true;
        }
        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attr;
        // This is a special case for datanucleus... apparently an embedded id is an ONE_TO_ONE association although I think it should be an embedded
        // TODO: create a test case for datanucleus and report the problem
        if (singularAttribute.isId()) {
            return false;
        }
        return attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    @Override
    public boolean isCompositeNode(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attr;
            if (pluralAttribute.getElementType().getPersistenceType() == Type.PersistenceType.BASIC) {
                return false;
            }
            return true;
        }
        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attr;
        // This is a special case for datanucleus... apparently an embedded id is an ONE_TO_ONE association although I think it should be an embedded
        // TODO: create a test case for datanucleus and report the problem
        if (singularAttribute.isId()) {
            return false;
        }
        return attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    @Override
    public boolean isElementCollection(Attribute<?, ?> attribute) {
        if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
            return true;
        }
        // Datanucleus kinda messes up the metamodel for some reason
        if (attribute instanceof PluralAttribute<?, ?, ?>) {
            Type.PersistenceType persistenceType = ((PluralAttribute<?, ?, ?>) attribute).getElementType().getPersistenceType();
            //CHECKSTYLE:OFF: FallThrough
            //CHECKSTYLE:OFF: MissingSwitchDefault
            switch (persistenceType) {
                case BASIC:
                case EMBEDDABLE:
                    return true;
            }
            //CHECKSTYLE:ON: FallThrough
            //CHECKSTYLE:ON: MissingSwitchDefault
        }
        return false;
    }

}
