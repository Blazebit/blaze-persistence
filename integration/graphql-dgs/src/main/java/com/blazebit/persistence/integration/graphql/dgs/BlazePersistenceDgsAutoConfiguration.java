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

package com.blazebit.persistence.integration.graphql.dgs;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodPluralAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry;
import com.netflix.graphql.dgs.internal.DefaultInputObjectMapper;
import com.netflix.graphql.dgs.internal.InputObjectMapper;
import graphql.schema.idl.TypeDefinitionRegistry;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;

/**
 * The following is a bit against the <a href="https://netflix.github.io/dgs/getting-started/#creating-a-schema">schema first DGS principle</a>
 * but we do it anyway since it's a lot easier to work this way than to define types twice and try to make sure they match up.
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@DgsComponent
public class BlazePersistenceDgsAutoConfiguration {

    @Autowired
    EntityViewManager evm;

    private TypeDefinitionRegistry typeRegistry;
    private GraphQLEntityViewSupport graphQLEntityViewSupport;

    @PostConstruct
    public void init() {
        this.typeRegistry = new TypeDefinitionRegistry();
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(true, true);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        graphQLEntityViewSupportFactory.setRegisterScalarTypeDefinitions(true);
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(typeRegistry, evm);
    }

    @DgsTypeDefinitionRegistry
    public TypeDefinitionRegistry registry() {
        return typeRegistry;
    }

    @Bean
    public GraphQLEntityViewSupport getSchema() {
        return graphQLEntityViewSupport;
    }

    @Bean
    public InputObjectMapper getInputObjectMapper() {
        return new EntityViewInputObjectMapper(evm);
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.9
     */
    private static class EntityViewInputObjectMapper implements InputObjectMapper {

        private static final InputObjectMapper DEFAULT = new DefaultInputObjectMapper();
        private final EntityViewManager entityViewManager;

        private EntityViewInputObjectMapper(EntityViewManager entityViewManager) {
            this.entityViewManager = entityViewManager;
        }

        @NotNull
        @Override
        public <T> T mapToKotlinObject(@NotNull Map<String, ?> map, @NotNull KClass<T> kClass) {
            return DEFAULT.mapToKotlinObject(map, kClass);
        }

        @Override
        public <T> T mapToJavaObject(@NotNull Map<String, ?> map, @NotNull Class<T> entityViewClass) {
            ManagedViewType<T> managedViewType = entityViewManager.getMetamodel().managedView(entityViewClass);
            if (managedViewType == null) {
                return DEFAULT.mapToJavaObject(map, entityViewClass);
            }
            boolean updatable = managedViewType.isUpdatable();
            boolean creatable = managedViewType.isCreatable();
            T reference = null;

            // Consume (i.e. remove from the payload json tree) the id if we are going to use getReference
            Object id = retrieveId(map, managedViewType, !creatable || updatable);

            // We create also creatable & updatable views if no id is given
            // If an id is given in such a case, we create a reference for updates
            if (creatable && (!updatable || id == null)) {
                reference = entityViewManager.create(entityViewClass);
            } else if (id != null) {
                reference = entityViewManager.getReference(entityViewClass, id);
            }

            if (reference == null || map.isEmpty()) {
                return reference;
            }

            try {
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    String attributeName = entry.getKey();
                    Object value = entry.getValue();
                    //noinspection unchecked
                    AbstractMethodAttribute<? super T, ?> attribute = (AbstractMethodAttribute<? super T, ?>) managedViewType.getAttribute(attributeName);
                    Object entityViewValue;
                    if (value != null) {
                        if (value instanceof Map<?, ?>) {
                            //noinspection unchecked
                            entityViewValue = mapToJavaObject((Map<String, ?>) value, attribute.getJavaType());
                        } else if (value instanceof Collection<?>) {
                            AbstractMethodPluralAttribute<?, ?, ?> pluralAttribute = (AbstractMethodPluralAttribute<?, ?, ?>) attribute;
                            Class<?> elementJavaType = pluralAttribute.getElementType().getJavaType();
                            //noinspection unchecked
                            Collection<Object> collection = (Collection<Object>) pluralAttribute.getJavaMethod().invoke(reference);
                            if (elementJavaType.isEnum()) {
                                for (Object element : (Collection<?>) value) {
                                    //noinspection unchecked,rawtypes
                                    collection.add(Enum.valueOf((Class) elementJavaType, element.toString()));
                                }
                            } else if (pluralAttribute.isSubview()) {
                                for (Object element : (Collection<?>) value) {
                                    //noinspection unchecked
                                    collection.add(mapToJavaObject((Map<String, ?>) element, elementJavaType));
                                }
                            } else {
                                collection.addAll((Collection<?>) value);
                            }
                            // No need to invoke the setter since collection was retrieved through the getter
                            continue;
                        } else if (attribute.getJavaType().isEnum()) {
                            //noinspection rawtypes,unchecked
                            entityViewValue = Enum.valueOf((Class) attribute.getJavaType(), value.toString());
                        } else {
                            entityViewValue = value;
                        }
                    } else {
                        entityViewValue = null;
                    }
                    attribute.getSetterMethod().invoke(reference, entityViewValue);
                }
            } catch (Exception e) {
                throw new RuntimeException("Couldn't deserialize entity view", e);
            }
            return reference;
        }

        private Object retrieveId(Map<String, ?> map, ManagedViewType<?> managedViewType, boolean consume) {
            MethodAttribute<?, ?> idAttribute;
            Object id;
            if (!(managedViewType instanceof ViewType<?>) || (idAttribute = ((ViewType<?>) managedViewType).getIdAttribute()) == null) {
                id = null;
            } else {
                String idAttributeName = idAttribute.getName();
                Object value = consume ? map.remove(idAttributeName) : map.get(idAttributeName);
                if (value != null) {
                    if (value instanceof Map<?, ?>) {
                        //noinspection unchecked
                        id = mapToJavaObject((Map<String, ?>) value, idAttribute.getJavaType());
                    } else {
                        id = value;
                    }
                } else {
                    id = null;
                }
            }

            return id;
        }
    }
}
