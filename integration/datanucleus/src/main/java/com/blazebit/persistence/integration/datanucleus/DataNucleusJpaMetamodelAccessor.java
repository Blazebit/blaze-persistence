/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class DataNucleusJpaMetamodelAccessor extends JpaMetamodelAccessorImpl {

    public static final DataNucleusJpaMetamodelAccessor INSTANCE = new DataNucleusJpaMetamodelAccessor();

    private DataNucleusJpaMetamodelAccessor() {
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

}
