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

package com.blazebit.persistence.integration.view.spring.impl;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class XmlEntityViewConfigurationSource extends AbstractEntityViewConfigurationSource {

    private static final String BASE_PACKAGE = "base-package";

    private final Element element;
    private final ParserContext context;

    private final Collection<TypeFilter> includeFilters;
    private final Collection<TypeFilter> excludeFilters;

    public XmlEntityViewConfigurationSource(Element element, ParserContext context, Environment environment) {
        super(environment);
        Assert.notNull(element);
        Assert.notNull(context);

        this.element = element;
        this.context = context;

        TypeFilterParser parser = new TypeFilterParser(context.getReaderContext());
        this.includeFilters = parser.parseTypeFilters(element, TypeFilterParser.Type.INCLUDE);
        this.excludeFilters = parser.parseTypeFilters(element, TypeFilterParser.Type.EXCLUDE);
    }

    @Override
    public Iterable<String> getBasePackages() {
        String attribute = element.getAttribute(BASE_PACKAGE);
        return Arrays.asList(StringUtils.delimitedListToStringArray(attribute, ",", " "));
    }

    @Override
    protected Iterable<TypeFilter> getExcludeFilters() {
        return excludeFilters;
    }

    @Override
    protected Iterable<TypeFilter> getIncludeFilters() {
        return includeFilters;
    }
}
