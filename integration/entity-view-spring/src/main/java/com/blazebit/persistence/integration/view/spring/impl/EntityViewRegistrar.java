/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.w3c.dom.Element;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRegistrar implements ImportBeanDefinitionRegistrar, BeanDefinitionParser, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        try {
            Environment environment = parserContext.getReaderContext().getEnvironment();
            BeanDefinitionRegistry registry = parserContext.getRegistry();
            XmlEntityViewConfigurationSource configurationSource = new XmlEntityViewConfigurationSource(element, parserContext, environment);

            if (!configurationSource.getBasePackages().iterator().hasNext()) {
                parserContext.getReaderContext().error("You have to specify at least one base package for entity views!", element);
            }

            EntityViewConfigurationDelegate delegate = new EntityViewConfigurationDelegate(configurationSource, resourceLoader, environment);
            delegate.registerEntityViews(registry);
        } catch (RuntimeException e) {
            handleError(e, element, parserContext.getReaderContext());
        }

        return null;
    }

    private void handleError(Exception e, Element source, ReaderContext reader) {
        reader.error(e.getMessage(), reader.extractSource(source), e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationEntityViewConfigurationSource configurationSource = new AnnotationEntityViewConfigurationSource(
                importingClassMetadata, EnableEntityViews.class, resourceLoader, environment);

        EntityViewConfigurationDelegate delegate = new EntityViewConfigurationDelegate(configurationSource, resourceLoader, environment);
        delegate.registerEntityViews(registry);
    }
}
