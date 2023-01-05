/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AbstractEntityViewTest extends AbstractCoreTest {

    protected static EntityViewManager evm;
    private static Map<ProxyFactoryCacheKey, ProxyFactory> proxyFactoryCache = new HashMap<>();
    private static Map<EntityViewManagerFactoryCacheKey, EntityViewManager> evmCache = new HashMap<>();

    public EntityViewManager build(Class<?>... classes) {
        return build(EntityViews.createDefaultConfiguration(), classes);
    }

    public EntityViewManager build(EntityViewConfiguration cfg, Class<?>... classes) {
        for (Class<?> c : classes) {
            cfg.addEntityView(c);
        }
        EntityViewManagerFactoryCacheKey cacheKey = new EntityViewManagerFactoryCacheKey(cbf, cfg);
        EntityViewManager evm;
        if ((evm = evmCache.get(cacheKey)) == null) {
            evm = build0(cfg, classes);
            evmCache.put(cacheKey, evm);
        }
        AbstractEntityViewTest.evm = evm;

        PackageOpener packageOpener = cbf.getService(PackageOpener.class);
        boolean unsafeDisabled = !Boolean.valueOf(String.valueOf(cfg.getProperty(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        boolean strictCascadingCheck = Boolean.valueOf(String.valueOf(cfg.getProperty(ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK)));
        ProxyFactoryCacheKey proxyFactoryCacheKey = new ProxyFactoryCacheKey(unsafeDisabled, strictCascadingCheck, packageOpener);
        ProxyFactory proxyFactory;
        if ((proxyFactory = proxyFactoryCache.get(proxyFactoryCacheKey)) == null) {
            proxyFactoryCache.put(proxyFactoryCacheKey, ((EntityViewManagerImpl) evm).getProxyFactory());
        } else {
            try {
                Field proxyFactoryField = EntityViewManagerImpl.class.getDeclaredField("proxyFactory");
                proxyFactoryField.setAccessible(true);
                proxyFactoryField.set(evm, proxyFactory);

                boolean scanStaticImplementations = !Boolean.valueOf(String.valueOf(cfg.getProperty(ConfigurationProperties.STATIC_IMPLEMENTATION_SCANNING_DISABLED)));
                for (ManagedViewType<?> managedView : evm.getMetamodel().getManagedViews()) {
                    Class<?> javaType = managedView.getJavaType();

                    if (!javaType.isInterface() && !Modifier.isAbstract(javaType.getModifiers())) {
                        proxyFactory.setImplementation(javaType);
                    } else if (scanStaticImplementations) {
                        proxyFactory.loadImplementation(new HashSet<>(), managedView, evm);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return evm;
    }

    private EntityViewManager build0(EntityViewConfiguration cfg, Class<?>[] classes) {
        return cfg.createEntityViewManager(cbf);
    }

    protected <T> CriteriaBuilder<T> applySetting(EntityViewManager evm, Class<T> entityViewClass, CriteriaBuilder<?> criteriaBuilder) {
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityViewClass);
        return evm.applySetting(setting, criteriaBuilder);
    }

    private static class ProxyFactoryCacheKey {
        private final boolean unsafeDisabled;
        private final boolean strictCascadingCheck;
        private final PackageOpener packageOpener;

        private ProxyFactoryCacheKey(boolean unsafeDisabled, boolean strictCascadingCheck, PackageOpener packageOpener) {
            this.unsafeDisabled = unsafeDisabled;
            this.strictCascadingCheck = strictCascadingCheck;
            this.packageOpener = packageOpener;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProxyFactoryCacheKey that = (ProxyFactoryCacheKey) o;
            return unsafeDisabled == that.unsafeDisabled &&
                    strictCascadingCheck == that.strictCascadingCheck &&
                    packageOpener.equals(that.packageOpener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unsafeDisabled, strictCascadingCheck, packageOpener);
        }
    }

    private static class EntityViewManagerFactoryCacheKey {
        private final CriteriaBuilderFactory cbf;
        private final EntityViewConfigurationEqualityWrapper entityViewConfiguration;

        private EntityViewManagerFactoryCacheKey(CriteriaBuilderFactory cbf, EntityViewConfiguration entityViewConfiguration) {
            this.cbf = cbf;
            this.entityViewConfiguration = new EntityViewConfigurationEqualityWrapper(entityViewConfiguration);
        }

        private static class EntityViewConfigurationEqualityWrapper {
            private final Properties properties;
            private final Map<String, Object> optionalParameters;
            private final Map<Class<?>, Map<Class<?>, Class<?>>> typeConverters;
            private final Map<Class<?>, Class<?>> basicUserTypes;
            private final Set<EntityViewMappingEqualityWrapper> entityViewMappings;

            private EntityViewConfigurationEqualityWrapper(EntityViewConfiguration cfg) {
                this.properties = cfg.getProperties();
                this.optionalParameters = cfg.getOptionalParameters();
                this.typeConverters = cfg.getTypeConverters().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> entry.getValue().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey, entry2 -> entry2.getValue().getClass()
                        ))
                ));
                this.basicUserTypes = cfg.getBasicUserTypes().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> entry.getValue().getClass()
                ));
                this.entityViewMappings = cfg.getEntityViewMappings().stream()
                        .map(mapping -> new EntityViewMappingEqualityWrapper(mapping.getEntityViewClass(), mapping.getFlushMode(), mapping.getFlushStrategy(), mapping.getVersionAttribute() != null))
                        .collect(Collectors.toSet());
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EntityViewConfigurationEqualityWrapper that = (EntityViewConfigurationEqualityWrapper) o;
                return properties.equals(that.properties) &&
                        optionalParameters.equals(that.optionalParameters) &&
                        typeConverters.equals(that.typeConverters) &&
                        basicUserTypes.equals(that.basicUserTypes) &&
                        entityViewMappings.equals(that.entityViewMappings);
            }

            @Override
            public int hashCode() {
                return Objects.hash(properties, optionalParameters, typeConverters, basicUserTypes, entityViewMappings);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityViewManagerFactoryCacheKey that = (EntityViewManagerFactoryCacheKey) o;
            return cbf.equals(that.cbf) &&
                    entityViewConfiguration.equals(that.entityViewConfiguration);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cbf, entityViewConfiguration);
        }

        private static class EntityViewMappingEqualityWrapper {
            private final Class<?> entityViewClass;
            private final FlushMode flushMode;
            private final FlushStrategy flushStrategy;
            private final boolean versionAttributeSet;

            public EntityViewMappingEqualityWrapper(Class<?> entityViewClass, FlushMode flushMode, FlushStrategy flushStrategy, boolean versionAttributeSet) {
                this.entityViewClass = entityViewClass;
                this.flushMode = flushMode;
                this.flushStrategy = flushStrategy;
                this.versionAttributeSet = versionAttributeSet;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EntityViewMappingEqualityWrapper that = (EntityViewMappingEqualityWrapper) o;
                return versionAttributeSet == that.versionAttributeSet &&
                        entityViewClass.equals(that.entityViewClass) &&
                        flushMode == that.flushMode &&
                        flushStrategy == that.flushStrategy;
            }

            @Override
            public int hashCode() {
                return Objects.hash(entityViewClass, flushMode, flushStrategy, versionAttributeSet);
            }
        }
    }
}
