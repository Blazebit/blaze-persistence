/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;


/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractUnmappedAttributeCascadeDeleter implements UnmappedAttributeCascadeDeleter {

    protected static final UnmappedAttributeCascadeDeleter[] EMPTY = new UnmappedAttributeCascadeDeleter[0];
    protected final Class<?> elementEntityClass;
    protected final String elementIdAttributeName;
    protected final String attributeName;
    protected final String attributeValuePath;
    protected final boolean cascadeDeleteElement;

    public AbstractUnmappedAttributeCascadeDeleter(EntityViewManagerImpl evm, String attributeName, ExtendedAttribute<?, ?> attribute) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        this.elementEntityClass = attribute.getElementClass();
        this.attributeName = attributeName;
        if (entityMetamodel.getEntity(elementEntityClass) == null) {
            this.elementIdAttributeName = null;
            this.attributeValuePath = attributeName;
            this.cascadeDeleteElement = false;
        } else {
            ExtendedManagedType extendedManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, elementEntityClass);
            this.elementIdAttributeName = extendedManagedType.getIdAttribute().getName();
            this.attributeValuePath = attributeName + "." + elementIdAttributeName;
            this.cascadeDeleteElement = attribute.isDeleteCascaded();
        }
    }

    protected AbstractUnmappedAttributeCascadeDeleter(AbstractUnmappedAttributeCascadeDeleter original) {
        this.elementEntityClass = original.elementEntityClass;
        this.elementIdAttributeName = original.elementIdAttributeName;
        this.attributeName = original.attributeName;
        this.attributeValuePath = original.attributeValuePath;
        this.cascadeDeleteElement = original.cascadeDeleteElement;
    }

    @Override
    public String getAttributeValuePath() {
        return attributeValuePath;

    }
}
