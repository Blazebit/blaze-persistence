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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewMetamodelImpl implements ViewMetamodel {

    private final EntityMetamodel metamodel;
    private final Map<Class<?>, ViewTypeImpl<?>> views;
    private final Map<Class<?>, FlatViewTypeImpl<?>> flatViews;
    private final Map<Class<?>, ManagedViewTypeImplementor<?>> managedViews;

    public ViewMetamodelImpl(EntityMetamodel entityMetamodel, MetamodelBuildingContext context, Map<Class<?>, Object> typeTestValues, boolean validateManagedTypes, boolean validateExpressions) {
        this.metamodel = entityMetamodel;

        Collection<ViewMapping> viewMappings = context.getViewMappings();
        Map<Class<?>, ViewTypeImpl<?>> views = new HashMap<>(viewMappings.size());
        Map<Class<?>, FlatViewTypeImpl<?>> flatViews = new HashMap<>(viewMappings.size());
        Map<Class<?>, ManagedViewTypeImplementor<?>> managedViews = new HashMap<>(viewMappings.size());

        // Phase 1: Wire up all view mappings into attributes, inheritance sub- and super types
        for (ViewMapping viewMapping : viewMappings) {
            viewMapping.initializeViewMappings(context, null);
        }

        // Phase 2: Check for circular dependencies
        Set<Class<?>> dependencies = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>(viewMappings.size()));
        for (ViewMapping viewMapping : viewMappings) {
            viewMapping.validateDependencies(context, dependencies, null, null, true);
        }

        // Phase 3: Build the ManagedViewType instances representing the metamodel
        for (ViewMapping viewMapping : viewMappings) {
            ManagedViewTypeImplementor<?> managedView = viewMapping.getManagedViewType(context);

            managedViews.put(viewMapping.getEntityViewClass(), managedView);
            if (managedView instanceof FlatViewType<?>) {
                flatViews.put(viewMapping.getEntityViewClass(), (FlatViewTypeImpl<?>) managedView);
            } else {
                views.put(viewMapping.getEntityViewClass(), (ViewTypeImpl<?>) managedView);
            }
        }

        this.views = Collections.unmodifiableMap(views);
        this.flatViews = Collections.unmodifiableMap(flatViews);
        this.managedViews = Collections.unmodifiableMap(managedViews);

        // Phase 4: Validate expressions against the entity model
        if (!context.hasErrors()) {
            if (validateExpressions) {
                List<AbstractAttribute<?, ?>> parents = new ArrayList<>();
                for (ManagedViewTypeImplementor<?> t : managedViews.values()) {
                    t.checkAttributes(context);
                    t.checkNestedAttributes(parents, context);
                }
            }
        }

        // Phase 5: Validate that JPA types that are used in entity views have sane equals/hashCode implementations
        if (validateManagedTypes) {
            Set<ManagedType<?>> jpaManagedTypes = new HashSet<>();
            for (ManagedViewTypeImplementor<?> managedViewType : managedViews.values()) {
                for (MethodAttribute<?, ?> attribute : managedViewType.getAttributes()) {
                    Type<?> keyType = ((AbstractAttribute<?, ?>) attribute).getKeyType();
                    Type<?> elementType = ((AbstractAttribute<?, ?>) attribute).getElementType();
                    if (keyType instanceof BasicTypeImpl<?>) {
                        jpaManagedTypes.add(((BasicTypeImpl<Object>) keyType).getManagedType());
                    }
                    if (elementType instanceof BasicTypeImpl<?>) {
                        jpaManagedTypes.add(((BasicTypeImpl<Object>) elementType).getManagedType());
                    }
                }
                for (MappingConstructor<?> constructor : managedViewType.getConstructors()) {
                    for (ParameterAttribute<?, ?> parameterAttribute : constructor.getParameterAttributes()) {
                        Type<?> keyType = ((AbstractAttribute<?, ?>) parameterAttribute).getKeyType();
                        Type<?> elementType = ((AbstractAttribute<?, ?>) parameterAttribute).getElementType();
                        if (keyType instanceof BasicTypeImpl<?>) {
                            jpaManagedTypes.add(((BasicTypeImpl<Object>) keyType).getManagedType());
                        }
                        if (elementType instanceof BasicTypeImpl<?>) {
                            jpaManagedTypes.add(((BasicTypeImpl<Object>) elementType).getManagedType());
                        }
                    }
                }
            }

            // A null might end up in here because we don't filter it out before adding, so remove it here again
            jpaManagedTypes.remove(null);
            for (ManagedType<?> jpaManagedType : jpaManagedTypes) {
                Class<?> javaType = jpaManagedType.getJavaType();
                if ((javaType.getModifiers() & Modifier.ABSTRACT) == 0) {
                    try {
                        Constructor<?> declaredConstructor = javaType.getDeclaredConstructor();
                        declaredConstructor.setAccessible(true);
                        Object instance1 = declaredConstructor.newInstance();
                        Object instance2 = declaredConstructor.newInstance();
                        Object instance3 = declaredConstructor.newInstance();

                        // Try to set any value on instance3 so that it would differ from instance1
                        String error = createValue(jpaManagedType, instance2, typeTestValues, true);

                        if (error != null) {
                            context.addError(error);
                        } else {
                            error = createValue(jpaManagedType, instance3, typeTestValues, true);
                            if (error != null) {
                                context.addError(error);
                            } else {
                                String infoText = "Equals/hashCode should be based on the identifier for entities and the full state for embeddables. Consider using a subview instead or add a proper equals/hashCode implementation!";
                                if (!instance2.equals(instance3)) {
                                    context.addError("The use of the JPA managed type '" + javaType.getName() + "' in entity views is problematic because two instances with the same state are not equal. " + infoText);
                                }
                                if (instance2.hashCode() != instance3.hashCode()) {
                                    context.addError("The use of the JPA managed type '" + javaType.getName() + "' in entity views is problematic because two instances with the same state do not have the same hashCode. " + infoText);
                                }
                                if (instance1.equals(instance3)) {
                                    context.addError("The use of the JPA managed type '" + javaType.getName() + "' in entity views is problematic because two instances with different state are equal. " + infoText);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        StringWriter sw = new StringWriter();
                        sw.append("Error during validation of equals/hashCode implementations of managed type [").append(javaType.getName()).append("]. If you think this is due to a bug, please report the problem and temporarily deactivate the type checking by setting the property '").append(ConfigurationProperties.MANAGED_TYPE_VALIDATION_DISABLED).append("' to true.\n");
                        ex.printStackTrace(new PrintWriter(sw));
                        context.addError(sw.toString());
                    }
                }
            }
        }
    }

    private String createValue(ManagedType<?> jpaManagedType, Object instance, Map<Class<?>, Object> typeTestValues, boolean root) throws Exception {
        boolean setAnyValue = false;
        Class<?> javaType = jpaManagedType.getJavaType();
        if ((javaType.getModifiers() & Modifier.ABSTRACT) == 0) {
            Set<SingularAttribute<?, ?>> jpaAttributes;
            if (JpaMetamodelUtils.isIdentifiable(jpaManagedType) && !root) {
                jpaAttributes = JpaMetamodelUtils.getIdAttributes((IdentifiableType<?>) jpaManagedType);
                // Hibernate
                if (jpaAttributes.isEmpty()) {
                    jpaAttributes = (Set<SingularAttribute<?, ?>>) jpaManagedType.getSingularAttributes();
                }
            } else {
                jpaAttributes = (Set<SingularAttribute<?, ?>>) jpaManagedType.getSingularAttributes();
            }

            // Try to set any value on instance so that it would differ from instance1
            for (SingularAttribute<?, ?> jpaAttribute : jpaAttributes) {
                javax.persistence.metamodel.Type<?> type = jpaAttribute.getType();
                Class<?> attributeType = JpaMetamodelUtils.resolveFieldClass(javaType, jpaAttribute);
                Object value = typeTestValues.get(attributeType);
                if (value == null) {
                    if (type.getPersistenceType() == javax.persistence.metamodel.Type.PersistenceType.BASIC) {
                        if (attributeType.isEnum()) {
                            value = attributeType.getEnumConstants()[0];
                        } else {
                            // We just skip basic values we can't handle
                            continue;
                        }
                    } else {
                        Constructor<?> typeConstructor = attributeType.getDeclaredConstructor();
                        typeConstructor.setAccessible(true);
                        value = typeConstructor.newInstance();
                        String error = createValue(metamodel.getManagedType(attributeType), value, typeTestValues, false);
                        if (error != null) {
                            return error;
                        }
                    }
                }
                setAttribute(instance, jpaAttribute, value);
                setAnyValue = true;
            }

            if (!setAnyValue) {
                Set<String> typeNames = new HashSet<>(jpaAttributes.size());
                for (SingularAttribute<?, ?> jpaAttribute : jpaAttributes) {
                    typeNames.add(jpaAttribute.getType().getJavaType().getName());
                }
                return "Can't check if the equals/hashCode implementation of the JPA managed type '" + javaType.getName() + "' which is used in entity views is problematic because there are no type test values registered in the EntityViewConfiguration for any of the types: " + typeNames;
            }
        }
        return null;
    }

    private void setAttribute(Object instance, SingularAttribute<?, ?> jpaAttribute, Object value) throws Exception {
        if (jpaAttribute.getJavaMember() instanceof Method) {
            Method setter = ReflectionUtils.getSetter(instance.getClass(), jpaAttribute.getName());
            setter.setAccessible(true);
            setter.invoke(instance, value);
        } else if (jpaAttribute.getJavaMember() instanceof Field) {
            Field field = (Field) jpaAttribute.getJavaMember();
            field.setAccessible(true);
            field.set(instance, value);
        } else {
            throw new IllegalArgumentException("Unsupported JPA member type: " + jpaAttribute.getJavaMember());
        }
    }

    public EntityMetamodel getEntityMetamodel() {
        return metamodel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ViewTypeImpl<X> view(Class<X> clazz) {
        return (ViewTypeImpl<X>) views.get(clazz);
    }

    @Override
    public Set<ViewType<?>> getViews() {
        return new SetView<ViewType<?>>(views.values());
    }

    public Collection<ViewTypeImpl<?>> views() {
        return views.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ManagedViewTypeImplementor<X> managedView(Class<X> clazz) {
        return (ManagedViewTypeImplementor<X>) managedViews.get(clazz);
    }

    @Override
    public Set<ManagedViewType<?>> getManagedViews() {
        return new SetView<ManagedViewType<?>>(managedViews.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> FlatViewTypeImpl<X> flatView(Class<X> clazz) {
        return (FlatViewTypeImpl<X>) flatViews.get(clazz);
    }

    @Override
    public Set<FlatViewType<?>> getFlatViews() {
        return new SetView<FlatViewType<?>>(flatViews.values());
    }

}
