package com.blazebit.persistence.view.impl.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 13.10.2016.
 */
public class EntityViewNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("entity-views", new EntityViewRegistrar());
    }
}
