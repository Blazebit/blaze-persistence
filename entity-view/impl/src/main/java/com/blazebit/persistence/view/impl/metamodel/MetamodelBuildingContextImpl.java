/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.MacroFunction;
import com.blazebit.persistence.view.impl.JpqlMacroAdapter;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.macro.DefaultViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MetamodelBuildingContextImpl implements MetamodelBuildingContext {

    private final EntityMetamodel entityMetamodel;
    private final ExpressionFactory expressionFactory;
    private final ProxyFactory proxyFactory;
    private final Set<Class<?>> entityViewClasses;
    private final Set<String> errors;

    public MetamodelBuildingContextImpl(EntityMetamodel entityMetamodel, ExpressionFactory expressionFactory, ProxyFactory proxyFactory, Set<Class<?>> entityViewClasses, Set<String> errors) {
        this.entityMetamodel = entityMetamodel;
        this.expressionFactory = expressionFactory;
        this.proxyFactory = proxyFactory;
        this.entityViewClasses = entityViewClasses;
        this.errors = errors;
    }

    @Override
    public EntityMetamodel getEntityMetamodel() {
        return entityMetamodel;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    @Override
    public ExpressionFactory createMacroAwareExpressionFactory() {
        return createMacroAwareExpressionFactory("syntax_checking_placeholder");
    }

    @Override
    public ExpressionFactory createMacroAwareExpressionFactory(String viewRoot) {
        MacroConfiguration originalMacroConfiguration = expressionFactory.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = expressionFactory.unwrap(AbstractCachingExpressionFactory.class);
        MacroFunction macro = new JpqlMacroAdapter(new DefaultViewRootJpqlMacro(viewRoot), cachingExpressionFactory);
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(Collections.singletonMap("view_root", macro));
        return new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
    }

    @Override
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @Override
    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public boolean isEntityView(Class<?> clazz) {
        return entityViewClasses.contains(clazz);
    }

}
