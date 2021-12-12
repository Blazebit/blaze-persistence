/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.jsonb;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.bytecode.SignatureAttribute;

import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class EntityViewJsonbDeserializer<T> implements JsonbDeserializer<T> {

    private final Map<Class<?>, EntityViewReferenceDeserializer> deserializers;

    public EntityViewJsonbDeserializer(EntityViewManager entityViewManager, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        this(createDeserializers(entityViewManager, entityViewIdValueAccessor));
    }

    public EntityViewJsonbDeserializer(Map<Class<?>, EntityViewReferenceDeserializer> deserializers) {
        this.deserializers = deserializers;
    }

    public static Map<Class<?>, EntityViewReferenceDeserializer> createDeserializers(EntityViewManager entityViewManager, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        Map<Class<?>, EntityViewReferenceDeserializer> deserializers = new HashMap<>();
        for (ManagedViewType<?> view : entityViewManager.getMetamodel().getManagedViews()) {
            deserializers.put(view.getJavaType(), new EntityViewReferenceDeserializer(entityViewManager, view, entityViewIdValueAccessor));
        }
        return deserializers;
    }

    public static void integrate(JsonbConfig config, EntityViewManager entityViewManager) {
        integrate(config, entityViewManager, EntityViewJsonbDeserializer.createDeserializers(entityViewManager, null));
    }

    public static void integrate(JsonbConfig config, EntityViewManager entityViewManager, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        integrate(config, entityViewManager, EntityViewJsonbDeserializer.createDeserializers(entityViewManager, entityViewIdValueAccessor));
    }

    public static void integrate(JsonbConfig config, EntityViewManager entityViewManager, Map<Class<?>, EntityViewReferenceDeserializer> deserializers) {
        try {
            config.withPropertyVisibilityStrategy(new EntityViewPropertyVisibilityStrategy(entityViewManager));
            // TODO: the deserializer class generation should be done through https://github.com/Blazebit/blaze-persistence/issues/1044
            ClassPool pool = new ClassPool((ClassPool) null);
            pool.appendSystemPath();
            pool.insertClassPath(new ClassClassPath(EntityViewJsonbDeserializer.class));
            CtClass baseDeserializer = pool.get(EntityViewJsonbDeserializer.class.getName());
            for (Map.Entry<Class<?>, EntityViewReferenceDeserializer> entry : deserializers.entrySet()) {
                Class<?> viewClass = entry.getKey();
                pool.insertClassPath(new ClassClassPath(viewClass));
                CtClass deserializerClass = pool.makeClass(viewClass.getName() + "Deserializer", baseDeserializer);
                SignatureAttribute.ClassType classType = new SignatureAttribute.ClassType(EntityViewJsonbDeserializer.class.getName(), new SignatureAttribute.TypeArgument[]{new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(viewClass.getName()))});
                deserializerClass.getClassFile2().addAttribute(new SignatureAttribute(deserializerClass.getClassFile2().getConstPool(), new SignatureAttribute.ClassSignature(null, classType, null).encode()));
                deserializerClass.addConstructor(CtNewConstructor.make(new CtClass[]{pool.get(Map.class.getName())}, new CtClass[0], deserializerClass));
                Class<?> clazz = define(deserializerClass);
                config.withDeserializers((JsonbDeserializer<?>) clazz.getConstructor(Map.class).newInstance(deserializers));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't register deserializers into JsonbConfig!", ex);
        }
    }

    private static Class<?> define(CtClass deserializerClass) throws CannotCompileException {
        try {
            return deserializerClass.toClass();
        } catch (CannotCompileException | LinkageError ex) {
            // If there are multiple proxy factories for the same class loader
            // we could end up in defining a class multiple times, so we check if the classloader
            // actually has something to offer
            LinkageError error;
            if (ex instanceof LinkageError && (error = (LinkageError) ex) != null
                    || ex.getCause() instanceof InvocationTargetException && ex.getCause().getCause() instanceof LinkageError && (error = (LinkageError) ex.getCause().getCause()) != null
                    || ex.getCause() instanceof LinkageError && (error = (LinkageError) ex.getCause()) != null) {
                try {
                    return deserializerClass.getClassPool().getClassLoader().loadClass(deserializerClass.getName());
                } catch (ClassNotFoundException cnfe) {
                    // Something we can't handle happened
                    throw error;
                }
            } else {
                throw ex;
            }
        }
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        EntityViewReferenceDeserializer deserializer = deserializers.get(type);
        if (deserializer == null) {
            return null;
        }
        return deserializer.deserialize(jsonParser, deserializationContext);
    }
}
