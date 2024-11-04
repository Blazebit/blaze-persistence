/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.impl;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("entity-views", new EntityViewRegistrar());
    }
}
