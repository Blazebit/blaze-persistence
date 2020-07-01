/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.SerializableEntityViewManager;
import com.blazebit.persistence.view.StaticImplementation;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.collection.RecordingNavigableMap;
import com.blazebit.persistence.view.impl.collection.RecordingNavigableSet;
import com.blazebit.persistence.view.impl.collection.RecordingSet;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.reflection.ReflectionUtils;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.StackMap;
import javassist.bytecode.StackMapTable;
import javassist.compiler.CompileError;
import javassist.compiler.JvstCodeGen;
import javassist.compiler.Lex;
import javassist.compiler.Parser;
import javassist.compiler.SymbolTable;
import javassist.compiler.ast.Stmnt;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamField;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ProxyFactory {

    private static final String IMPL_CLASS_NAME_SUFFIX = "Impl";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final Logger LOG = Logger.getLogger(ProxyFactory.class.getName());
    private static final Path DEBUG_DUMP_DIRECTORY;
    private final ConcurrentMap<Class<?>, Class<?>> baseClasses = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> unsafeProxyClasses = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> proxyClassesToViewClasses = new ConcurrentHashMap<>();
    private final Object proxyLock = new Object();
    private final ClassPool pool;
    private final boolean unsafeDisabled;
    private final boolean strictCascadingCheck;
    private final PackageOpener packageOpener;

    static {
        String property = System.getProperty("entityview.debugDumpDirectory");
        if (property == null) {
            DEBUG_DUMP_DIRECTORY = null;
        } else {
            Path p = Paths.get(property);
            if (Files.exists(p)) {
                DEBUG_DUMP_DIRECTORY = p.toAbsolutePath();
            } else {
                DEBUG_DUMP_DIRECTORY = null;
                Logger.getLogger(ProxyFactory.class.getName()).severe("The given debug dump directory does not exist: " + p.toAbsolutePath());
            }
        }
    }

    public ProxyFactory(boolean unsafeDisabled, boolean strictCascadingCheck, PackageOpener packageOpener) {
        this.pool = new ClassPool(ClassPool.getDefault());
        this.unsafeDisabled = unsafeDisabled;
        this.strictCascadingCheck = strictCascadingCheck;
        this.packageOpener = packageOpener;
    }

    public <T> Class<? extends T> getProxy(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> viewType) {
        if (viewType.getConstructors().isEmpty() || unsafeDisabled) {
            return getProxy(entityViewManager, viewType, false);
        } else {
            return getProxy(entityViewManager, viewType, true);
        }
    }

    public <T> Class<T> getEntityViewClass(Class<? extends T> implementationClass) {
        return (Class<T>) proxyClassesToViewClasses.get(implementationClass);
    }

    private static String getImplementationClassName(Class<?> javaType, Class<?> baseJavaType) {
        String fqcn = javaType.getName();
        StringBuilder sb = new StringBuilder(fqcn.length() + IMPL_CLASS_NAME_SUFFIX.length() + baseJavaType.getSimpleName().length());
        int i;
        if (javaType.getPackage() == null) {
            i = 0;
        } else {
            String packageName = javaType.getPackage().getName();
            sb.append(packageName).append('.');
            i = packageName.length() + 1;
        }
        for (; i < fqcn.length(); i++) {
            final char c = fqcn.charAt(i);
            if (c != '$') {
                sb.append(c);
            }
        }
        sb.append(IMPL_CLASS_NAME_SUFFIX);
        if (baseJavaType != javaType) {
            sb.append("_").append(baseJavaType.getSimpleName());
        }
        return sb.toString();
    }

    public void loadImplementation(Set<String> errors, ManagedViewType<?> managedView, EntityViewManager entityViewManager) {
        Class<?> javaType = managedView.getJavaType();
        Class<?> entityViewImplementationClass;
        try {
            entityViewImplementationClass = javaType.getClassLoader().loadClass(getImplementationClassName(javaType, managedView.getJavaType()));
            StaticImplementation annotation = entityViewImplementationClass.getAnnotation(StaticImplementation.class);
            if (annotation != null) {
                if (annotation.value() != javaType) {
                    errors.add("The static implementation class '" + entityViewImplementationClass.getName() + "' was expected to be defined for the entity view type '" + javaType.getName() + "' but was defined for: " + annotation.value().getName());
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            // Ignore
            return;
        }
        try {
            entityViewImplementationClass.getDeclaredField(SerializableEntityViewManager.EVM_FIELD_NAME).set(null, entityViewManager);
            // Sanity check
            for (MethodAttribute<?, ?> attribute : managedView.getAttributes()) {
                entityViewImplementationClass.getDeclaredField(attribute.getName());
            }
            proxyClasses.put(javaType, entityViewImplementationClass);
            proxyClassesToViewClasses.put(entityViewImplementationClass, javaType);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errors.add("The initialization of the static metamodel class '" + entityViewImplementationClass.getName() + "' failed: " + sw.toString());
        }
    }

    public void setImplementation(Class<?> entityViewImplementationClass) {
        proxyClasses.put(entityViewImplementationClass, entityViewImplementationClass);
        proxyClassesToViewClasses.put(entityViewImplementationClass, entityViewImplementationClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> getProxy(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> viewType, boolean unsafe) {
        Class<T> clazz = viewType.getJavaType();
        final ConcurrentMap<Class<?>, Class<?>> classes = unsafe ? unsafeProxyClasses : proxyClasses;
        Class<? extends T> proxyClass = (Class<? extends T>) classes.get(clazz);

        // Double checked locking since we can only define the class once
        if (proxyClass == null) {
            synchronized (proxyLock) {
                proxyClass = (Class<? extends T>) classes.get(clazz);
                if (proxyClass == null) {
                    proxyClass = createProxyClass(entityViewManager, viewType, unsafe);
                    classes.put(clazz, proxyClass);
                    proxyClassesToViewClasses.put(proxyClass, clazz);
                }
            }
        }

        return proxyClass;
    }

    private Class<?> getProxyBase(Class<?> baseClass) {
        if (baseClass.isInterface() || !java.lang.reflect.Modifier.isAbstract(baseClass.getSuperclass().getModifiers())) {
            return baseClass;
        }
        Class<?> proxyBaseClass = baseClasses.get(baseClass);

        // No need for locking as we are in a locked context in here anyway
        if (proxyBaseClass == null) {
            proxyBaseClass = createProxyBaseClass(baseClass);
            baseClasses.put(baseClass, proxyBaseClass);
        }

        return proxyBaseClass;
    }

    private Class<?> createProxyBaseClass(Class<?> baseClass) {
        String packageName = baseClass.getPackage() == null ? "" : baseClass.getPackage().getName();
        Map<String, Class<?>> classesToBaseProxy = new LinkedHashMap<>();

        // Traverse the class hierarchy up and collect abstract classes of packages different than the original package
        for (Class<?> c = baseClass.getSuperclass(); c != Object.class; c = c.getSuperclass()) {
            if (java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !packageName.equals(c.getPackage() == null ? "" : c.getPackage().getName())) {
                classesToBaseProxy.put(c.getName(), c);
            }
        }

        if (classesToBaseProxy.isEmpty()) {
            return baseClass;
        }

        ClassPath classPath = new ClassClassPath(baseClass);
        pool.insertClassPath(classPath);
        try {
            Class<?> proxyClass = baseClass;
            for (Class<?> classOfPackage : classesToBaseProxy.values()) {
                CtClass superCc = pool.get(proxyClass.getName());
                CtClass cc = pool.makeClass(classOfPackage.getName() + "_$$_javassist_proxybase_" + baseClass.getName().replace('.',  '_'));
                cc.setSuperclass(superCc);
                String genericSignature = pool.get(classOfPackage.getName()).getGenericSignature();
                if (genericSignature != null) {
                    cc.setGenericSignature(genericSignature);
                }

                // Collect the protected and default visibility methods
                Map<String, MethodInfo> methods = new TreeMap<>();
                Map<String, String> classNameMapping = new HashMap<>();
                CtClass ctClass = pool.get(classOfPackage.getName());
                classNameMapping.put(classOfPackage.getName(), cc.getName());
                for (CtMethod method : ctClass.getDeclaredMethods()) {
                    if (java.lang.reflect.Modifier.isAbstract(method.getModifiers()) && !java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !java.lang.reflect.Modifier.isPrivate(method.getModifiers())) {
                        methods.put(method.getName() + " " + method.getMethodInfo().getDescriptor(), method.getMethodInfo());
                    }
                }

                // Redefine methods in proxy base class as public
                for (MethodInfo value : methods.values()) {
                    MethodInfo methodInfo = new MethodInfo(cc.getClassFile().getConstPool(), value.getName(), value.getDescriptor());
                    if (value.getExceptionsAttribute() != null) {
                        methodInfo.setExceptionsAttribute((ExceptionsAttribute) value.getExceptionsAttribute().copy(cc.getClassFile().getConstPool(), classNameMapping));
                    }
                    for (AttributeInfo attribute : value.getAttributes()) {
                        methodInfo.addAttribute(attribute.copy(cc.getClassFile().getConstPool(), classNameMapping));
                    }

                    methodInfo.setAccessFlags(Modifier.setPublic(value.getAccessFlags()));
                    cc.addMethod(CtMethod.make(methodInfo, cc));
                }

                proxyClass = defineOrGetClass(proxyClass, cc);
            }

            return proxyClass;
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> createProxyClass(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> managedViewType, boolean unsafe) {
        ViewType<T> viewType = managedViewType instanceof ViewType<?> ? (ViewType<T>) managedViewType : null;
        Class<?> clazz = managedViewType.getJavaType();
        String suffix = unsafe ? "unsafe_" : "";
        String baseName = clazz.getName();
        String proxyClassName = baseName + "_$$_javassist_entityview_" + suffix;
        CtClass cc = pool.makeClass(proxyClassName);
        CtClass superCc;

        ClassPath classPath = new ClassClassPath(clazz);
        pool.insertClassPath(classPath);

        try {
            superCc = pool.get(getProxyBase(clazz).getName());

            if (clazz.isInterface()) {
                cc.addInterface(superCc);
            } else {
                cc.setSuperclass(superCc);
            }

            boolean dirtyChecking = false;
            CtField dirtyField = null;
            CtField readOnlyParentsField = null;
            CtField parentField = null;
            CtField parentIndexField = null;
            CtField initialStateField = null;
            CtField mutableStateField = null;
            CtMethod markDirtyStub = null;
            long alwaysDirtyMask = 0L;
            cc.addInterface(pool.get(EntityViewProxy.class.getName()));
            addGetJpaManagedClass(cc, managedViewType.getEntityClass());
            addGetJpaManagedBaseClass(cc, getJpaManagedBaseClass(managedViewType));
            addGetEntityViewClass(cc, clazz);
            addIsNewAndReferenceMembers(managedViewType, cc, clazz);

            CtField evmField = new CtField(pool.get(EntityViewManager.class.getName()), SerializableEntityViewManager.EVM_FIELD_NAME, cc);
            evmField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.VOLATILE);
            cc.addField(evmField);

            cc.addField(CtField.make("public static final " + EntityViewManager.class.getName() + " " + SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME + " = new " + SerializableEntityViewManager.class.getName() + "(" + cc.getName() + ".class, " + cc.getName() + "#" + SerializableEntityViewManager.EVM_FIELD_NAME + ");", cc));

            if (managedViewType.isUpdatable() || managedViewType.isCreatable()) {
                if (true || managedViewType.getFlushMode() == FlushMode.LAZY || managedViewType.getFlushMode() == FlushMode.PARTIAL) {
                    cc.addInterface(pool.get(DirtyStateTrackable.class.getName()));
                    initialStateField = new CtField(pool.get(Object[].class.getName()), "$$_initialState", cc);
                    initialStateField.setModifiers(getModifiers(false));
                    cc.addField(initialStateField);

                    addGetter(cc, initialStateField, "$$_getInitialState");
                }

                cc.addInterface(pool.get(MutableStateTrackable.class.getName()));
                cc.addInterface(pool.get(DirtyTracker.class.getName()));
                mutableStateField = new CtField(pool.get(Object[].class.getName()), "$$_mutableState", cc);
                mutableStateField.setModifiers(getModifiers(false));
                cc.addField(mutableStateField);

                CtField initializedField = new CtField(CtClass.booleanType, "$$_initialized", cc);
                initializedField.setModifiers(getModifiers(false));
                cc.addField(initializedField);

                readOnlyParentsField = new CtField(pool.get(List.class.getName()), "$$_readOnlyParents", cc);
                readOnlyParentsField.setModifiers(getModifiers(true));
                readOnlyParentsField.setGenericSignature(Descriptor.of(List.class.getName()) + "<" + Descriptor.of(Object.class.getName()) + ">;");
                cc.addField(readOnlyParentsField);
                parentField = new CtField(pool.get(DirtyTracker.class.getName()), "$$_parent", cc);
                parentField.setModifiers(getModifiers(true));
                cc.addField(parentField);
                parentIndexField = new CtField(CtClass.intType, "$$_parentIndex", cc);
                parentIndexField.setModifiers(getModifiers(true));
                cc.addField(parentIndexField);

                dirtyChecking = true;

                addGetter(cc, mutableStateField, "$$_getMutableState");
                addGetter(cc, readOnlyParentsField, "$$_getReadOnlyParents");
                addGetter(cc, parentField, "$$_getParent");
                addGetter(cc, parentIndexField, "$$_getParentIndex");
                addAddReadOnlyParent(cc, readOnlyParentsField, parentField);
                addRemoveReadOnlyParent(cc, readOnlyParentsField);
                addSetParent(cc, parentField, parentIndexField);
                addHasParent(cc, parentField);
                addUnsetParent(cc, parentField, parentIndexField, readOnlyParentsField);
                markDirtyStub = addMarkDirtyStub(cc);
            }

            Set<MethodAttribute<? super T, ?>> attributes = new LinkedHashSet<>(managedViewType.getAttributes());
            CtField[] attributeFields = new CtField[attributes.size()];
            CtClass[] attributeTypes = new CtClass[attributes.size()];
            Map<String, CtField> fieldMap = new HashMap<>(attributes.size());
            int i = 0;

            // Create the id field
            AbstractMethodAttribute<? super T, ?> idAttribute = null;
            CtField idField = null;
            AbstractMethodAttribute<? super T, ?> versionAttribute = null;
            final AbstractMethodAttribute<?, ?>[] methodAttributes = new AbstractMethodAttribute[attributeFields.length];
            
            if (viewType != null) {
                idAttribute = (AbstractMethodAttribute<? super T, ?>) viewType.getIdAttribute();
                versionAttribute = (AbstractMethodAttribute<? super T, ?>) viewType.getVersionAttribute();
                idField = addMembersForAttribute(idAttribute, clazz, cc, null, false, true, mutableStateField != null);
                fieldMap.put(idAttribute.getName(), idField);
                attributeFields[0] = idField;
                attributeTypes[0] = idField.getType();
                methodAttributes[0] = idAttribute;
                attributes.remove(idAttribute);
                i = 1;

                if (mutableStateField != null) {
                    addSetId(cc, idField);
                }
            } else if (mutableStateField != null) {
                addSetId(cc, null);
            }

            addGetter(cc, idField, "$$_getId", Object.class);

            int mutableAttributeCount = 0;
            for (MethodAttribute<?, ?> attribute : attributes) {
                AbstractMethodAttribute<?, ?> methodAttribute = (AbstractMethodAttribute<?, ?>) attribute;
                boolean forceMutable = mutableStateField != null && methodAttribute == versionAttribute;
                CtField attributeField = addMembersForAttribute(methodAttribute, clazz, cc, mutableStateField, dirtyChecking, false, forceMutable);
                fieldMap.put(attribute.getName(), attributeField);
                attributeFields[i] = attributeField;
                attributeTypes[i] = attributeField.getType();
                methodAttributes[i] = methodAttribute;

                if (methodAttribute.hasDirtyStateIndex()) {
                    mutableAttributeCount++;
                }

                i++;
            }

            if (mutableStateField != null) {
                if (versionAttribute != null) {
                    CtField versionField = fieldMap.get(versionAttribute.getName());
                    addGetter(cc, versionField, "$$_getVersion", Object.class);
                    addSetVersion(cc, versionField);
                } else {
                    addGetter(cc, null, "$$_getVersion", Object.class);
                    addSetVersion(cc, null);
                }
            } else {
                addGetter(cc, null, "$$_getVersion", Object.class);
            }

            if (dirtyChecking) {
                addReplaceAttribute(cc, methodAttributes);
                cc.removeMethod(markDirtyStub);
                if (mutableAttributeCount > 64) {
                    throw new IllegalArgumentException("Support for more than 64 mutable attributes per view is not yet implemented! " + viewType.getJavaType().getName() + " has " + mutableAttributeCount);
                } else {
                    dirtyField = new CtField(CtClass.longType, "$$_dirty", cc);
                    dirtyField.setModifiers(getModifiers(true));
                    cc.addField(dirtyField);

                    boolean allSupportDirtyTracking = true;
                    boolean[] supportsDirtyTracking = new boolean[mutableAttributeCount];
                    int mutableAttributeIndex = 0;
                    for (int j = 0; j < methodAttributes.length; j++) {
                        if (methodAttributes[j] != null && methodAttributes[j].hasDirtyStateIndex()) {
                            if (supportsDirtyTracking(methodAttributes[j])) {
                                supportsDirtyTracking[mutableAttributeIndex++] = true;
                            } else {
                                allSupportDirtyTracking = false;
                                alwaysDirtyMask |= 1 << mutableAttributeIndex;
                                supportsDirtyTracking[mutableAttributeIndex++] = false;
                            }
                        }
                    }

                    addIsDirty(cc, dirtyField, allSupportDirtyTracking);
                    addIsDirtyAttribute(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                    addMarkDirty(cc, dirtyField);
                    addUnmarkDirty(cc, dirtyField, alwaysDirtyMask);
                    addSetDirty(cc, dirtyField, alwaysDirtyMask);
                    addResetDirty(cc, dirtyField, alwaysDirtyMask);
                    addGetDirty(cc, dirtyField);
                    addGetSimpleDirty(cc, dirtyField);
                    addCopyDirty(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                }
            }

            createEqualsHashCodeMethods(viewType, managedViewType, cc, superCc, attributeFields, idField);
            cc.addMethod(createToString(managedViewType, cc, viewType != null, attributeFields));
            createSpecialMethods(managedViewType, cc, cc);
            createSerializationSubclass(managedViewType, cc);

            Set<MappingConstructorImpl<T>> constructors = (Set<MappingConstructorImpl<T>>) (Set<?>) managedViewType.getConstructors();
            boolean hasEmptyConstructor = managedViewType.hasEmptyConstructor();

            if (hasEmptyConstructor) {
                // Create constructor for create models
                cc.addConstructor(createCreateConstructor(entityViewManager, managedViewType, cc, attributeFields, attributeTypes, idField, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, alwaysDirtyMask, unsafe));
            }

            boolean addedReferenceConstructor = false;
            if (idField != null && hasEmptyConstructor) {
                // Id only constructor for reference models
                cc.addConstructor(createReferenceConstructor(entityViewManager, managedViewType, cc, attributeFields, idField, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, alwaysDirtyMask, unsafe));
                addedReferenceConstructor = true;
            }

            if (shouldAddDefaultConstructor(hasEmptyConstructor, addedReferenceConstructor, attributeFields)) {
                cc.addConstructor(createNormalConstructor(entityViewManager, managedViewType, null, cc, attributeFields, attributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, alwaysDirtyMask, unsafe));
                cc.addConstructor(createTupleConstructor(managedViewType, null, cc, attributeFields.length, attributeFields.length, attributeFields, attributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, false, alwaysDirtyMask, unsafe));
                cc.addConstructor(createTupleConstructor(managedViewType, null, cc, attributeFields.length, attributeFields.length, attributeFields, attributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, true, alwaysDirtyMask, unsafe));
            } else if (hasEmptyConstructor) {
                cc.addConstructor(createTupleConstructor(managedViewType, null, cc, attributeFields.length, attributeFields.length, attributeFields, attributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, false, alwaysDirtyMask, unsafe));
                cc.addConstructor(createTupleConstructor(managedViewType, null, cc, attributeFields.length, attributeFields.length, attributeFields, attributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, true, alwaysDirtyMask, unsafe));
            }

            for (MappingConstructorImpl<?> constructor : constructors) {
                int constructorParameterCount = attributeFields.length + constructor.getParameterAttributes().size();
                // Skip the empty constructor which was handled before
                if (constructor.getParameterAttributes().size() == 0) {
                    continue;
                }
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(attributeTypes, 0, constructorAttributeTypes, 0, attributeFields.length);

                // Append super constructor parameters to default constructor parameters
                CtConstructor superConstructor = findConstructor(superCc, constructor);
                System.arraycopy(superConstructor.getParameterTypes(), 0, constructorAttributeTypes, attributeFields.length, superConstructor.getParameterTypes().length);

                cc.addConstructor(createNormalConstructor(entityViewManager, managedViewType, constructor, cc, attributeFields, constructorAttributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, alwaysDirtyMask, unsafe));
                cc.addConstructor(createTupleConstructor(managedViewType, constructor, cc, attributeFields.length, constructorAttributeTypes.length, attributeFields, constructorAttributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, false, alwaysDirtyMask, unsafe));
                cc.addConstructor(createTupleConstructor(managedViewType, constructor, cc, attributeFields.length, constructorAttributeTypes.length, attributeFields, constructorAttributeTypes, initialStateField, mutableStateField, methodAttributes, mutableAttributeCount, true, alwaysDirtyMask, unsafe));
            }

            return defineOrGetClass(entityViewManager, unsafe, clazz, cc);
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }

    private void createSerializationSubclass(ManagedViewTypeImplementor<?> managedViewType, CtClass cc) throws Exception {
        boolean hasSelfConstructor = false;
        OUTER: for (MappingConstructor<?> constructor : managedViewType.getConstructors()) {
            for (ParameterAttribute<?, ?> parameterAttribute : constructor.getParameterAttributes()) {
                if (parameterAttribute.isSelfParameter()) {
                    hasSelfConstructor = true;
                    break OUTER;
                }
            }
        }
        if (hasSelfConstructor) {
            String serializableClassName = cc.getName() + "Ser";
            Set<AbstractMethodAttribute<?, ?>> attributes = (Set<AbstractMethodAttribute<?, ?>>) (Set<?>) managedViewType.getAttributes();
            CtClass[] attributeTypes = new CtClass[attributes.size()];
            createSerializableClass(managedViewType, cc, serializableClassName, attributes, attributeTypes);

            ConstPool cp = cc.getClassFile().getConstPool();
            MethodInfo minfo = new MethodInfo(cp, "createSelf", Descriptor.ofMethod(pool.get(managedViewType.getJavaType().getName()), attributeTypes));
            CtMethod method = CtMethod.make(minfo, cc);
            minfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\t").append(serializableClassName).append(" self = (").append(serializableClassName).append(") new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(");
            sb.append(serializableClassName).append(".EMPTY_INSTANCE_BYTES)).readObject();\n");
            int index = 1;
            for (MethodAttribute<?, ?> attribute : attributes) {
                sb.append("\tself.").append(attribute.getName()).append(" = $").append(index).append(";\n");
                index++;
            }
            sb.append("\treturn self;\n");
            sb.append("}");
            method.setBody(sb.toString());
            cc.addMethod(method);
        }
    }

    private void createSerializableClass(ManagedViewTypeImplementor<?> managedViewType, CtClass cc, String serializableClassName, Set<AbstractMethodAttribute<?, ?>> attributes, CtClass[] attributeTypes) throws Exception {
        CtClass serializableClass = pool.makeClass(serializableClassName);
        Class<?> clazz = managedViewType.getJavaType();
        if (clazz.isInterface()) {
            serializableClass.addInterface(cc.getSuperclass());
        } else {
            serializableClass.setSuperclass(cc.getSuperclass());
        }
        serializableClass.addInterface(pool.get(Serializable.class.getName()));
        int index = 0;
        for (AbstractMethodAttribute<?, ?> attribute : attributes) {
            CtClass attributeType = pool.get(attribute.getJavaType().getName());
            attributeTypes[index] = attributeType;
            CtField field = addMembersForAttribute(attribute, clazz, serializableClass, null, false, false, true);
            field.setModifiers((field.getModifiers() & ~Modifier.PRIVATE) | Modifier.PUBLIC);
            index++;
        }
        createSpecialMethods(managedViewType, cc, serializableClass);

        CtField serialVersionUidField = new CtField(CtPrimitiveType.longType, "serialVersionUID", serializableClass);
        serialVersionUidField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        serializableClass.addField(serialVersionUidField, CtField.Initializer.constant(1L));

        if (!clazz.isInterface()) {
            StringBuilder sb = new StringBuilder();
            for (CtConstructor superConstructor : cc.getDeclaredConstructors()) {
                CtClass[] parameterTypes = superConstructor.getParameterTypes();
                CtConstructor constructor = new CtConstructor(parameterTypes, serializableClass);
                constructor.setModifiers(Modifier.PRIVATE);
                sb.setLength(0);
                sb.append("super(");
                if (parameterTypes.length != 0) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        sb.append('$').append(i + 1).append(',');
                    }
                    sb.setLength(sb.length() - 1);
                }

                sb.append(");");
                constructor.setBody(sb.toString());
            }
        }

        byte[] emptyInstanceBytes = generateEmptyInstanceBytes(serializableClassName, managedViewType);
        StringBuilder emptyInstanceByteBuilder = new StringBuilder();
        emptyInstanceByteBuilder.append("new byte[]{ ");
        appendBytesAsHex(emptyInstanceByteBuilder, emptyInstanceBytes);
        emptyInstanceByteBuilder.setCharAt(emptyInstanceByteBuilder.length() - 1, '}');
        CtField emptyBytesField = new CtField(pool.get("byte[]"), "EMPTY_INSTANCE_BYTES", serializableClass);
        emptyBytesField.setModifiers(Modifier.STATIC | Modifier.FINAL);
        serializableClass.addField(emptyBytesField, CtField.Initializer.byExpr(emptyInstanceByteBuilder.toString()));
        defineOrGetClass(clazz, serializableClass);
    }

    private static void appendBytesAsHex(StringBuilder sb, byte[] bytes) {
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            sb.append(" (byte) 0x");
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(',');
        }
    }

    private static byte[] generateEmptyInstanceBytes(String serializableClassName, ManagedViewTypeImplementor<?> managedViewTypeImplementor) {
        // Generate empty object serialization bytes according to https://www.javaworld.com/article/2072752/the-java-serialization-algorithm-revealed.html
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DataOutputStream oos = new DataOutputStream(baos);
            oos.writeShort(ObjectStreamConstants.STREAM_MAGIC);
            oos.writeShort(ObjectStreamConstants.STREAM_VERSION);

            // Start object
            oos.writeByte(ObjectStreamConstants.TC_OBJECT);
            // Class descriptor
            oos.writeByte(ObjectStreamConstants.TC_CLASSDESC);
            // Class name
            oos.writeUTF(serializableClassName);
            // Serial version UID of the class
            oos.writeLong(1L);
            // Supported flags
            oos.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);

            List<List<SerializationField>> serializationFieldHierarchy = new ArrayList<>();
            List<SerializationField> serializationFields = new ArrayList<>();
            for (MethodAttribute<?, ?> attribute : managedViewTypeImplementor.getAttributes()) {
                serializationFields.add(new MetaSerializationField((AbstractMethodAttribute<?, ?>) attribute));
            }
            serializationFieldHierarchy.add(serializationFields);
            writeFields(serializationFields, oos);
            oos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);

            // TODO: foreign package supertypes?
            Class<?> superclass = managedViewTypeImplementor.getJavaType();
            if (!superclass.isInterface()) {
                while (superclass != Object.class) {
                    // Class descriptor
                    oos.writeByte(ObjectStreamConstants.TC_CLASSDESC);
                    // Class name
                    oos.writeUTF(superclass.getName());
                    List<SerializationField> fields = new ArrayList<>();
                    ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(superclass);
                    for (ObjectStreamField field : objectStreamClass.getFields()) {
                        fields.add(new ObjectStreamFieldSerializationField(field));
                    }

                    if (Serializable.class.isAssignableFrom(superclass)) {
                        // Serial version UID of the class
                        oos.writeLong(objectStreamClass.getSerialVersionUID());
                        // Supported flags
                        oos.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
                    } else {
                        // Serial version UID of the class
                        oos.writeLong(0L);
                        // Supported flags
                        oos.writeByte(0);
                    }

                    serializationFieldHierarchy.add(fields);
                    writeFields(fields, oos);
                    oos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);

                    superclass = superclass.getSuperclass();
                }
            }
            oos.writeByte(ObjectStreamConstants.TC_NULL);
            for (List<SerializationField> fields : serializationFieldHierarchy) {
                for (SerializationField serializationField : fields) {
                    if (serializationField.isPrimitive()) {
                        switch (serializationField.getType().getName()) {
                            case "int":
                                oos.writeInt(0);
                                break;
                            case "byte":
                                oos.writeByte(0);
                                break;
                            case "long":
                                oos.writeLong(0);
                                break;
                            case "float":
                                oos.writeFloat(0);
                                break;
                            case "double":
                                oos.writeDouble(0);
                                break;
                            case "short":
                                oos.writeShort(0);
                                break;
                            case "char":
                                oos.writeChar(0);
                                break;
                            case "boolean":
                                oos.writeBoolean(false);
                                break;
                            default:
                                throw new UnsupportedOperationException("Unsupported primitive type: " + serializationField.getType().getName());
                        }
                    } else {
                        oos.writeByte(ObjectStreamConstants.TC_NULL);
                    }
                }
            }
            oos.flush();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeFields(List<SerializationField> serializationFields, DataOutputStream oos) throws Exception {
        Collections.sort(serializationFields);
        oos.writeShort(serializationFields.size());
        for (SerializationField serializationField : serializationFields) {
            oos.writeByte(serializationField.getTypeCode());
            oos.writeUTF(serializationField.getName());
            if (!serializationField.isPrimitive()) {
                oos.writeByte(ObjectStreamConstants.TC_STRING);
                oos.writeUTF(serializationField.getTypeString());
//                oos.writeTypeString(serializationField.getTypeString());
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private abstract static class SerializationField implements Comparable<SerializationField> {

        public abstract String getName();

        public abstract Class<?> getType();

        public abstract char getTypeCode();

        public abstract String getTypeString();

        public abstract boolean isPrimitive();

        @Override
        public int compareTo(SerializationField other) {
            boolean isPrim = isPrimitive();
            if (isPrim != other.isPrimitive()) {
                return isPrim ? -1 : 1;
            }
            return getName().compareTo(other.getName());
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class MetaSerializationField extends SerializationField {

        private final AbstractMethodAttribute<?, ?> attribute;
        private final String signature;

        public MetaSerializationField(AbstractMethodAttribute<?, ?> attribute) {
            this.attribute = attribute;
            this.signature = getClassSignature(attribute.getJavaType());
        }

        @Override
        public String getName() {
            return attribute.getName();
        }

        @Override
        public Class<?> getType() {
            return attribute.getJavaType();
        }

        @Override
        public char getTypeCode() {
            return signature.charAt(0);
        }

        @Override
        public String getTypeString() {
            return isPrimitive() ? null : signature;
        }

        @Override
        public boolean isPrimitive() {
            return attribute.getJavaType().isPrimitive();
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class ObjectStreamFieldSerializationField extends SerializationField {

        private final ObjectStreamField field;

        public ObjectStreamFieldSerializationField(ObjectStreamField field) {
            this.field = field;
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public char getTypeCode() {
            return field.getTypeCode();
        }

        @Override
        public String getTypeString() {
            return field.getTypeString();
        }

        @Override
        public boolean isPrimitive() {
            return field.isPrimitive();
        }
    }

    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sbuf.append('I');
            } else if (cl == Byte.TYPE) {
                sbuf.append('B');
            } else if (cl == Long.TYPE) {
                sbuf.append('J');
            } else if (cl == Float.TYPE) {
                sbuf.append('F');
            } else if (cl == Double.TYPE) {
                sbuf.append('D');
            } else if (cl == Short.TYPE) {
                sbuf.append('S');
            } else if (cl == Character.TYPE) {
                sbuf.append('C');
            } else if (cl == Boolean.TYPE) {
                sbuf.append('Z');
            } else if (cl == Void.TYPE) {
                sbuf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            sbuf.append('L' + cl.getName().replace('.', '/') + ';');
        }
        return sbuf.toString();
    }

    private Class<?> getJpaManagedBaseClass(ManagedViewTypeImplementor<?> managedViewType) {
        ManagedType<?> jpaManagedType = managedViewType.getJpaManagedType();
        if (jpaManagedType instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) jpaManagedType;

            do {
                jpaManagedType = entityType.getSupertype();
                if (jpaManagedType instanceof EntityType<?>) {
                    entityType = (EntityType<?>) jpaManagedType;
                } else {
                    break;
                }
            } while (true);

            return entityType.getJavaType();
        }
        return managedViewType.getEntityClass();
    }

    private <T> Class<? extends T> defineOrGetClass(Class<?> clazz, CtClass cc) throws IOException, IllegalAccessException, NoSuchFieldException, CannotCompileException {
        return defineOrGetClass(null, false, clazz, cc);
    }

    private <T> Class<? extends T> defineOrGetClass(EntityViewManager entityViewManager, boolean unsafe, Class<?> clazz, CtClass cc) throws IOException, IllegalAccessException, NoSuchFieldException, CannotCompileException {
        try {
            // Ask the package opener to allow deep access, otherwise defining the class will fail
            if (clazz.getPackage() != null) {
                packageOpener.openPackageIfNeeded(clazz, clazz.getPackage().getName(), ProxyFactory.class);
            }

            if (DEBUG_DUMP_DIRECTORY != null) {
                cc.writeFile(DEBUG_DUMP_DIRECTORY.toString());
            }

            Class<? extends T> c;
            if (unsafe) {
                c = (Class<? extends T>) UnsafeHelper.define(cc.getName(), cc.toBytecode(), clazz);
            } else {
                c = (Class<? extends T>) cc.toClass(clazz.getClassLoader(), null);
            }

            if (entityViewManager != null) {
                c.getField(SerializableEntityViewManager.EVM_FIELD_NAME).set(null, entityViewManager);
            }

            return c;
        } catch (CannotCompileException | LinkageError ex) {
            // If there are multiple proxy factories for the same class loader
            // we could end up in defining a class multiple times, so we check if the classloader
            // actually has something to offer
            LinkageError error;
            if (ex instanceof LinkageError && (error = (LinkageError) ex) != null
                    || ex.getCause() instanceof InvocationTargetException && ex.getCause().getCause() instanceof LinkageError && (error = (LinkageError) ex.getCause().getCause()) != null
                    || ex.getCause() instanceof LinkageError && (error = (LinkageError) ex.getCause()) != null) {
                try {
                    return (Class<? extends T>) pool.getClassLoader().loadClass(cc.getName());
                } catch (ClassNotFoundException cnfe) {
                    // Something we can't handle happened
                    throw error;
                }
            } else {
                throw ex;
            }
        } catch (NullPointerException ex) {
            // With Java 9 it's actually the case that Javassist doesn't throw the LinkageError but instead tries to define the class differently
            // Too bad that this different path lead to a NullPointerException
            try {
                return (Class<? extends T>) pool.getClassLoader().loadClass(cc.getName());
            } catch (ClassNotFoundException cnfe) {
                // Something we can't handle happened
                throw ex;
            }
        }
    }

    private boolean shouldAddDefaultConstructor(boolean hasEmptyConstructor, boolean addedReferenceConstructor, CtField[] attributeFields) {
        // Add the default constructor only for interfaces since abstract classes may omit it
        // Only add the "normal" constructor if there are attributes other than the id attribute available, otherwise we get a duplicate member exception
        return hasEmptyConstructor && (!addedReferenceConstructor && attributeFields.length > 0 || addedReferenceConstructor && attributeFields.length > 1);
    }

    private <T> void createEqualsHashCodeMethods(ViewType<T> viewType, ManagedViewTypeImplementor<T> managedViewType, CtClass cc, CtClass superCc, CtField[] attributeFields, CtField idField) throws NotFoundException, CannotCompileException {
        CtClass equalsDeclaringClass = superCc.getMethod("equals", getEqualsDesc()).getDeclaringClass();
        CtClass hashCodeDeclaringClass = superCc.getMethod("hashCode", getHashCodeDesc()).getDeclaringClass();
        boolean hasCustomEqualsHashCode = false;

        if (!"java.lang.Object".equals(equalsDeclaringClass.getName())) {
            hasCustomEqualsHashCode = true;
            LOG.warning("The class '" + equalsDeclaringClass.getName() + "' declares 'boolean equals(java.lang.Object)'! Hopefully you implemented it based on a unique key!");
        }
        if (!"java.lang.Object".equals(hashCodeDeclaringClass.getName())) {
            hasCustomEqualsHashCode = true;
            LOG.warning("The class '" + hashCodeDeclaringClass.getName() + "' declares 'int hashCode()'! Hopefully you implemented it based on a unique key!");
        }

        if (!hasCustomEqualsHashCode) {
            if (viewType != null) {
                cc.addMethod(createIdEquals(managedViewType, cc));
                cc.addMethod(createHashCode(cc, idField));
            } else {
                cc.addMethod(createEquals(managedViewType, cc, attributeFields));
                cc.addMethod(createHashCode(cc, attributeFields));
            }
        }
    }

    private void createSpecialMethods(ManagedViewTypeImplementor<?> viewType, CtClass cc, CtClass target) throws CannotCompileException {
        for (Method method : viewType.getSpecialMethods()) {
            if (method.getReturnType() == EntityViewManager.class) {
                addEntityViewManagerGetter(cc, method, target);
            } else {
                throw new IllegalArgumentException("Unsupported special method: " + method);
            }
        }
    }

    private void addEntityViewManagerGetter(CtClass cc, Method method, CtClass target) throws CannotCompileException {
        ConstPool cp = target.getClassFile2().getConstPool();
        String desc = Descriptor.of(method.getReturnType().getName());
        MethodInfo minfo = new MethodInfo(cp, method.getName(), "()" + desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, 1, 1);
        code.addGetstatic(cc, SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME, desc);
        code.addOpcode(Bytecode.ARETURN);

        minfo.setCodeAttribute(code.toCodeAttribute());
        target.addMethod(CtMethod.make(minfo, target));
    }

    private void addGetJpaManagedClass(CtClass cc, Class<?> entityClass) throws CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getJpaManagedClass", "()Ljava/lang/Class;");
        minfo.addAttribute(new SignatureAttribute(cp, "()Ljava/lang/Class<*>;"));
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, 1, 1);
        code.addLdc(cp.addClassInfo(entityClass.getName()));
        code.addOpcode(Bytecode.ARETURN);

        minfo.setCodeAttribute(code.toCodeAttribute());
        cc.addMethod(CtMethod.make(minfo, cc));
    }

    private void addGetJpaManagedBaseClass(CtClass cc, Class<?> entityClass) throws CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getJpaManagedBaseClass", "()Ljava/lang/Class;");
        minfo.addAttribute(new SignatureAttribute(cp, "()Ljava/lang/Class<*>;"));
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, 1, 1);
        code.addLdc(cp.addClassInfo(entityClass.getName()));
        code.addOpcode(Bytecode.ARETURN);

        minfo.setCodeAttribute(code.toCodeAttribute());
        cc.addMethod(CtMethod.make(minfo, cc));
    }

    private void addGetEntityViewClass(CtClass cc, Class<?> entityViewClass) throws CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getEntityViewClass", "()Ljava/lang/Class;");
        minfo.addAttribute(new SignatureAttribute(cp, "()Ljava/lang/Class<*>;"));
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, 1, 1);
        code.addLdc(cp.addClassInfo(entityViewClass.getName()));
        code.addOpcode(Bytecode.ARETURN);

        minfo.setCodeAttribute(code.toCodeAttribute());
        cc.addMethod(CtMethod.make(minfo, cc));
    }

    private void addIsNewAndReferenceMembers(ManagedViewType<?> managedViewType, CtClass cc, Class<?> clazz) throws CannotCompileException, NotFoundException {
        CtField kindField = new CtField(CtClass.byteType, "$$_kind", cc);
        kindField.setModifiers(getModifiers(true));
        cc.addField(kindField);
        cc.addMethod(CtMethod.make("public boolean $$_isReference() { return $0.$$_kind == (byte) 1; }", cc));
        if (managedViewType.isCreatable()) {
            cc.addMethod(CtMethod.make("public boolean $$_isNew() { return $0.$$_kind == (byte) 2; }", cc));
            cc.addMethod(CtMethod.make("public void $$_setIsNew(boolean isNew) { if (isNew) { $0.$$_kind = (byte) 2; } else { $0.$$_kind = (byte) 0; } }", cc));
        } else {
            ClassPool classPool = cc.getClassPool();
            try {
                addEmptyIsNew(cc, classPool.get("boolean"));
                addEmptySetIsNew(cc, classPool.get("boolean"));
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }
    }
    
    private CtMethod addGetter(CtClass cc, CtField field, String methodName) throws CannotCompileException {
        return addGetter(cc, field, methodName, field.getFieldInfo().getDescriptor(), false);
    }
    
    private CtMethod addGetter(CtClass cc, CtField field, String methodName, Class<?> returnType) throws CannotCompileException {
        if (returnType.isArray()) {
            return addGetter(cc, field, methodName, Descriptor.toJvmName(returnType.getName()), false);
        } else {
            try {

                return addGetter(cc, field, methodName, Descriptor.of(returnType.getName()), !returnType.isPrimitive() && field != null && field.getType().isPrimitive());
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }
    }
    
    private CtMethod addGetter(CtClass cc, CtField field, String methodName, String returnTypeDescriptor, boolean autoBox) throws CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, methodName, "()" + returnTypeDescriptor);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, needsTwoStackSlots(Descriptor.toClassName(returnTypeDescriptor)) ? 2 : 1, 1);
        
        if (field != null) {
            code.addAload(0);
            code.addGetfield(cc, field.getName(), field.getFieldInfo().getDescriptor());

            if (autoBox) {
                try {
                    autoBox(code, cc.getClassPool(), field.getType());
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Unsupported primitive type: " + Descriptor.toClassName(field.getFieldInfo().getDescriptor()), ex);
                }
                code.addOpcode(Opcode.ARETURN);
            } else {
                try {
                    code.addReturn(field.getType());
                } catch (NotFoundException e) {
                    throw new CannotCompileException(e);
                }
            }
        } else {
            code.addOpcode(Bytecode.ACONST_NULL);
            code.add(Bytecode.ARETURN);
        }

        minfo.setCodeAttribute(code.toCodeAttribute());
        CtMethod method = CtMethod.make(minfo, cc);
        cc.addMethod(method);
        return method;
    }

    private CtField addMembersForAttribute(AbstractMethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc, CtField mutableStateField, boolean dirtyChecking, boolean isId, boolean forceMutable) throws CannotCompileException, NotFoundException {
        Method getter = attribute.getJavaMethod();
        Method setter = ReflectionUtils.getSetter(clazz, attribute.getName());
        
        // Create the field from the attribute
        CtField attributeField = new CtField(getType(attribute), attribute.getName(), cc);
        attributeField.setModifiers(getModifiers(forceMutable || setter != null));
        String genericSignature = getGenericSignature(attribute, attributeField);
        if (genericSignature != null) {
            setGenericSignature(attributeField, genericSignature);
        }
        cc.addField(attributeField);
        
        createGettersAndSetters(attribute, clazz, cc, getter, setter, mutableStateField, attributeField, dirtyChecking, isId);
        
        return attributeField;
    }

    private void createGettersAndSetters(AbstractMethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc, Method getter, Method setter, CtField mutableStateField, CtField attributeField, boolean dirtyChecking, boolean isId) throws CannotCompileException, NotFoundException {
        SignatureAttribute sa = (SignatureAttribute)attributeField.getFieldInfo2().getAttribute(SignatureAttribute.tag);
        String genericSignature = sa == null ? null : sa.getSignature();
        List<Method> bridgeGetters = getBridgeGetters(clazz, attribute, getter);
        
        CtMethod attributeGetter = addGetter(cc, attributeField, getter.getName());
        
        if (genericSignature != null) {
            String getterGenericSignature = "()" + genericSignature;
            setGenericSignature(attributeGetter, getterGenericSignature);
        }
        
        for (Method m : bridgeGetters) {
            CtMethod getterBridge = createGetterBridge(cc, m, attributeGetter);
            cc.addMethod(getterBridge);
        }
        
        if (setter != null) {
            CtMethod attributeSetter = addSetter(attribute, cc, attributeField, setter.getName(), mutableStateField, dirtyChecking, isId);
            List<Method> bridgeSetters = getBridgeSetters(clazz, attribute, setter);

            if (genericSignature != null) {
                String setterGenericSignature = "(" + genericSignature + ")V";
                setGenericSignature(attributeSetter, setterGenericSignature);
            }

            for (Method m : bridgeSetters) {
                CtMethod setterBridge = createSetterBridge(cc, m, attributeSetter);
                cc.addMethod(setterBridge);
            }
        }
    }

    private CtMethod addEmptyIsNew(CtClass cc, CtClass returnType) throws CannotCompileException {
        String desc = "()" + Descriptor.of(returnType);
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_isNew", desc);
        CtMethod method = CtMethod.make(minfo, cc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        method.setBody("{ return false; }");
        cc.addMethod(method);
        return method;
    }

    private CtMethod addEmptySetIsNew(CtClass cc, CtClass argumentType) throws CannotCompileException {
        String desc = "(" + Descriptor.of(argumentType) + ")V";
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setIsNew", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody("{}");
        cc.addMethod(method);
        return method;
    }

    private CtMethod addIsDirty(CtClass cc, CtField dirtyField, boolean allSupportDirtyTracking) throws CannotCompileException {
        String desc = "()" + Descriptor.of("boolean");
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_isDirty", desc);
        CtMethod method = CtMethod.make(minfo, cc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        if (allSupportDirtyTracking) {
            method.setBody("{ return $0." + dirtyField.getName() + " != 0; }");
        } else {
            method.setBody("{ return true; }");
        }

        cc.addMethod(method);
        return method;
    }

    private CtMethod addIsDirtyAttribute(CtClass cc, CtField dirtyField, boolean[] supportsDirtyTracking, boolean allSupportDirtyTracking) throws CannotCompileException {
        String desc = "(" + Descriptor.of("int") + ")" + Descriptor.of("boolean");
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_isDirty", desc);
        CtMethod method = CtMethod.make(minfo, cc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (!allSupportDirtyTracking) {
            sb.append("\tswitch ($1) {\n");
            for (int i = 0; i < supportsDirtyTracking.length; i++) {
                if (!supportsDirtyTracking[i]) {
                    sb.append("\t\tcase ").append(i).append(": return true;\n");
                }
            }
            sb.append("\t\tdefault : break;\n");
            sb.append("\t}\n");
        }

        sb.append("\treturn ($0.").append(dirtyField.getName()).append(" & (1L << $1)) != 0;\n");
        sb.append("}");
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addMarkDirtyStub(CtClass cc) throws CannotCompileException {
        String desc = "(" + Descriptor.of("int") + ")V";
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_markDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        StringBuilder sb = new StringBuilder();

        sb.append("{}");

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addMarkDirty(CtClass cc, CtField dirtyField) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "(" + Descriptor.of("int") + ")V";
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_markDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\t$0.").append(dirtyFieldName).append(" |= (1 << $1);\n");

        sb.append("\tif ($0.$$_parent != null) {\n");
        sb.append("\t$0.$$_parent.$$_markDirty($0.$$_parentIndex);\n");
        sb.append("\t}\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addSetDirty(CtClass cc, CtField dirtyField, long alwaysDirtyMask) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "([" + Descriptor.of("long") + ")V";
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        if (alwaysDirtyMask == 0L) {
            sb.append("\t$0.").append(dirtyFieldName).append(" = $1[0];\n");
        } else {
            sb.append("\t$0.").append(dirtyFieldName).append(" = $1[0] | ").append(alwaysDirtyMask).append("L;\n");
        }

        sb.append("\tif ($0.").append(dirtyFieldName).append(" != 0 && $0.$$_parent != null) {\n");
        sb.append("\t\t$0.$$_parent.$$_markDirty($0.$$_parentIndex);\n");
        sb.append("\t}\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addUnmarkDirty(CtClass cc, CtField dirtyField, long alwaysDirtyMask) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()" + Descriptor.of("void");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_unmarkDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\t$0.").append(dirtyFieldName).append(" = ").append(alwaysDirtyMask).append("L;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addResetDirty(CtClass cc, CtField dirtyField, long alwaysDirtyMask) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()[" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_resetDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tlong[] dirty = new long[1];\n");
        sb.append("\tdirty[0] = $0.").append(dirtyFieldName).append(";\n");
        sb.append("\t$0.").append(dirtyFieldName).append(" = ").append(alwaysDirtyMask).append("L;\n");
        sb.append("\treturn dirty;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addGetDirty(CtClass cc, CtField dirtyField) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()[" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tlong[] dirty = new long[1];\n");
        sb.append("\tdirty[0] = $0.").append(dirtyFieldName).append(";\n");
        sb.append("\treturn dirty;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addGetSimpleDirty(CtClass cc, CtField dirtyField) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getSimpleDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\treturn $0.").append(dirtyFieldName).append(";\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addCopyDirty(CtClass cc, CtField dirtyField, boolean[] supportsDirtyTracking, boolean allSupportDirtyTracking) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "([" + Descriptor.of("java.lang.Object") + "[" + Descriptor.of("java.lang.Object") + ")" + Descriptor.of("boolean");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_copyDirty", desc);
        minfo.addAttribute(new SignatureAttribute(minfo.getConstPool(), "<T:" + Descriptor.of("java.lang.Object") + ">([TT;[TT;)" + Descriptor.of("boolean")));
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        sb.append("\tlong dirty = $0.").append(dirtyFieldName).append(";\n");

        if (allSupportDirtyTracking) {
            sb.append("\tif (dirty == 0) {\n");
            sb.append("\t\treturn false;\n");
            sb.append("\t} else {\n");
        }

        for (int i = 0; i < supportsDirtyTracking.length; i++) {
            long mask = 1 << i;

            if (supportsDirtyTracking[i]) {
                sb.append("\t\t$2[").append(i).append("] = (dirty & ").append(mask).append(") == 0 ? null : $1[").append(i).append("];\n");
            } else {
                sb.append("\t\t$2[").append(i).append("] = $1[").append(i).append("];\n");
            }
        }

        sb.append("\t\treturn true;\n");

        if (allSupportDirtyTracking) {
            sb.append("\t}\n");
        }

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);

        return method;
    }

    private boolean supportsDirtyTracking(AbstractMethodAttribute<?, ?> mutableAttribute) {
        // Non-mutable types always support dirty tracking as there is nothing to track
        // Subview types have dirty tracking implemented
        if (!mutableAttribute.isMutable() || mutableAttribute.isSubview()) {
            return true;
        }

        if (mutableAttribute instanceof SingularAttribute<?, ?>) {
            BasicType<?> type = (BasicType<?>) ((SingularAttribute<?, ?>) mutableAttribute).getType();
            if (!type.getUserType().isMutable()) {
                return true;
            }
            return type.getUserType().supportsDirtyTracking();
        } else {
            BasicType<?> type = (BasicType<?>) ((PluralAttribute<?, ?, ?>) mutableAttribute).getElementType();
            if (!type.getUserType().isMutable()) {
                return true;
            }
            return type.getUserType().supportsDirtyTracking();
        }
    }

    private CtMethod addAddReadOnlyParent(CtClass cc, CtField readOnlyParentField, CtField parentField) throws CannotCompileException {
        FieldInfo parentFieldInfo = readOnlyParentField.getFieldInfo2();
        String desc = "(" + Descriptor.of(DirtyTracker.class.getName()) + "I)V";
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_addReadOnlyParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String readOnlyParentFieldName = parentFieldInfo.getName();
        String parentFieldName = parentField.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        // Strict check for write-parent
        if (strictCascadingCheck) {
            sb.append("\tif ($0 != $1 && $0.").append(parentFieldName).append(" == null) {\n");
            sb.append("\t\tthrow new IllegalStateException(\"Can't set read only parent for object \" + $0.toString() + \" util it doesn't have a writable parent! First add the object to an attribute with proper cascading. If you just want to reference it convert the object with EntityViewManager.getReference() or EntityViewManager.convert()!\");\n");
            sb.append("\t}\n");
        }

        sb.append("\tif ($0.").append(readOnlyParentFieldName).append(" == null) {\n");
        sb.append("\t\t$0.").append(readOnlyParentFieldName).append(" = new java.util.ArrayList();\n");
        sb.append("\t}\n");

        sb.append("\t$0.").append(readOnlyParentFieldName).append(".add($1);\n");
        sb.append("\t$0.").append(readOnlyParentFieldName).append(".add(Integer#valueOf($2));\n");

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addRemoveReadOnlyParent(CtClass cc, CtField readOnlyParentField) throws CannotCompileException {
        FieldInfo parentFieldInfo = readOnlyParentField.getFieldInfo2();
        String desc = "(" + Descriptor.of(DirtyTracker.class.getName()) + "I)V";
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_removeReadOnlyParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String readOnlyParentFieldName = parentFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        sb.append("\tif ($0.").append(readOnlyParentFieldName).append(" != null) {\n");
        sb.append("\t\tint size = $0.").append(readOnlyParentFieldName).append(".size();\n");
        sb.append("\t\tfor (int i = 0; i < size; i += 2) {\n");
        sb.append("\t\t\tif ($0.").append(readOnlyParentFieldName).append(".get(i) == $1 && ((Integer) $0.").append(readOnlyParentFieldName).append(".get(i + 1)).intValue() == $2) {\n");
        sb.append("\t\t\t\t$0.").append(readOnlyParentFieldName).append(".remove(i + 1);\n");
        sb.append("\t\t\t\t$0.").append(readOnlyParentFieldName).append(".remove(i);\n");
        sb.append("\t\t\t\tbreak;\n");
        sb.append("\t\t\t}\n");
        sb.append("\t\t}\n");
        sb.append("\t}\n");

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addReplaceAttribute(CtClass cc, AbstractMethodAttribute[] attributes) throws CannotCompileException {
        String desc = "(" + Descriptor.of(Object.class.getName()) + "I" + Descriptor.of(Object.class.getName()) + ")V";
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_replaceAttribute", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        sb.append("\tswitch ($2) {");
        for (int i = 0; i < attributes.length; i++) {
            AbstractMethodAttribute attribute = attributes[i];
            if (attribute != null && attribute.hasDirtyStateIndex()) {
                sb.append("\t\tcase ").append(attribute.getDirtyStateIndex()).append(": ");
                // If there is no setter, we simply ignore the object rather than throwing an exception
                if (ReflectionUtils.getSetter(attribute.getDeclaringType().getJavaType(), attribute.getName()) != null) {
                    sb.append("$0.set").append(Character.toUpperCase(attribute.getName().charAt(0))).append(attribute.getName(), 1, attribute.getName().length());
                    sb.append('(');
                    if (attribute.getConvertedJavaType().isPrimitive()) {
                        appendUnwrap(sb, attribute.getConvertedJavaType(), "$3");
                    } else {
                        sb.append("(").append(attribute.getConvertedJavaType().getName()).append(") $3");
                    }
                    sb.append("); ");
                }
                sb.append("break;\n");
            }
        }

        sb.append("\t\tdefault: throw new IllegalArgumentException(\"Invalid non-mutable attribute index: \" + $2);\n");
        sb.append("\t}\n");

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addSetParent(CtClass cc, CtField parentField, CtField parentIndexField) throws CannotCompileException {
        FieldInfo parentFieldInfo = parentField.getFieldInfo2();
        FieldInfo parentIndexFieldInfo = parentIndexField.getFieldInfo2();
        String desc = "(" + Descriptor.of(BasicDirtyTracker.class.getName()) + parentIndexFieldInfo.getDescriptor() + ")V";
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String parentFieldName = parentFieldInfo.getName();
        String parentIndexFieldName = parentIndexFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tif ($0.").append(parentFieldName).append(" != null) {\n");
        sb.append("\t\tthrow new IllegalStateException(\"Parent object for \" + $0.toString() + \" is already set to \" + $0.").append(parentFieldName).append(".toString() + \" and can't be set to: \" + $1.toString());\n");
        sb.append("\t}\n");
        sb.append("\t$0.").append(parentFieldName).append(" = $1;\n");
        sb.append("\t$0.").append(parentIndexFieldName).append(" = $2;\n");

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addHasParent(CtClass cc, CtField parentField) throws CannotCompileException {
        FieldInfo parentFieldInfo = parentField.getFieldInfo2();
        String desc = "()" + Descriptor.of("boolean");
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_hasParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String parentFieldName = parentFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\treturn $0.").append(parentFieldName).append(" != null;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addUnsetParent(CtClass cc, CtField parentField, CtField parentIndexField, CtField readOnlyParentsField) throws CannotCompileException {
        FieldInfo parentFieldInfo = parentField.getFieldInfo2();
        FieldInfo parentIndexFieldInfo = parentIndexField.getFieldInfo2();
        String desc = "()V";
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_unsetParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String parentFieldName = parentFieldInfo.getName();
        String parentIndexFieldName = parentIndexFieldInfo.getName();
        String readOnlyParentsFieldName = readOnlyParentsField.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tif ($0.").append(parentFieldName).append(" != null && $0.").append(readOnlyParentsFieldName).append(" != null && !$0.").append(readOnlyParentsFieldName).append(".isEmpty()) {\n");
        sb.append("\t\tthrow new IllegalStateException(\"Can't unset writable parent \" + $0.").append(parentFieldName).append(" + \" on object \" + $0.toString() + \" because it is still connected to read only parents: \" + $0.").append(readOnlyParentsFieldName).append(");\n");
        sb.append("\t}\n");
        sb.append("\t$0.").append(parentFieldName).append(" = null;\n");
        sb.append("\t$0.").append(parentIndexFieldName).append(" = 0;\n");

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addSetId(CtClass cc, CtField field) throws CannotCompileException,NotFoundException {
        String desc = "(" + Descriptor.of(Object.class.getName()) + ")V";
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setId", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod method = CtMethod.make(minfo, cc);
        if (field == null) {
            method.setBody("{\n\tthrow new UnsupportedOperationException(\"No id attribute available!\");\n}");
        } else {
            StringBuilder sb = new StringBuilder();
            appendObjectSetter(sb, field);
            method.setBody(sb.toString());
        }
        cc.addMethod(method);
        return method;
    }

    private CtMethod addSetVersion(CtClass cc, CtField field) throws CannotCompileException, NotFoundException {
        String desc = "(" + Descriptor.of(Object.class.getName()) + ")V";
        ConstPool cp = cc.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setVersion", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod method = CtMethod.make(minfo, cc);
        if (field == null) {
            method.setBody("{\n\tthrow new UnsupportedOperationException(\"No version attribute available!\");\n}");
        } else {
            StringBuilder sb = new StringBuilder();
            appendObjectSetter(sb, field);
            method.setBody(sb.toString());
        }
        cc.addMethod(method);
        return method;
    }

    private void appendObjectSetter(StringBuilder sb, CtField field) throws NotFoundException {
        sb.append("{\n");
        CtClass type = field.getType();
        if (type.isPrimitive()) {
            sb.append("\tif ($1 == null) {\n");
            String defaultValue = getDefaultValue(type);
            sb.append("\t\t$0.").append(field.getName()).append(" = ").append(defaultValue).append(";\n");
            sb.append("\t} else {\n");
            sb.append("\t\t$0.").append(field.getName()).append(" = ");
            appendUnwrap(sb, type, "$1");
            sb.append(";\n");
            sb.append("\t}\n");
        } else {
            sb.append("\t$0.").append(field.getName()).append(" = (").append(Descriptor.toClassName(field.getFieldInfo2().getDescriptor())).append(") $1;\n");
        }
        sb.append("}");
    }

    private void appendUnwrap(StringBuilder sb, CtClass type, String input) {
        if (type == CtClass.longType) {
            sb.append("((Long) ").append(input).append(").longValue()");
        } else if (type == CtClass.floatType) {
            sb.append("((Float) ").append(input).append(").floatValue()");
        } else if (type == CtClass.doubleType) {
            sb.append("((Double) ").append(input).append(").doubleValue()");
        } else if (type == CtClass.intType) {
            sb.append("((Integer) ").append(input).append(").intValue()");
        } else if (type == CtClass.shortType) {
            sb.append("((Short) ").append(input).append(").shortValue()");
        } else if (type == CtClass.byteType) {
            sb.append("((Byte) ").append(input).append(").byteValue()");
        } else if (type == CtClass.booleanType) {
            sb.append("((Boolean) ").append(input).append(").booleanValue()");
        } else if (type == CtClass.charType) {
            sb.append("((Character) ").append(input).append(").charValue()");
        } else {
            throw new UnsupportedOperationException("Unwrap not possible for type: " + type);
        }
    }

    private void appendUnwrap(StringBuilder sb, Class<?> type, String input) {
        if (type == long.class) {
            sb.append("((Long) ").append(input).append(").longValue()");
        } else if (type == float.class) {
            sb.append("((Float) ").append(input).append(").floatValue()");
        } else if (type == double.class) {
            sb.append("((Double) ").append(input).append(").doubleValue()");
        } else if (type == int.class) {
            sb.append("((Integer) ").append(input).append(").intValue()");
        } else if (type == short.class) {
            sb.append("((Short) ").append(input).append(").shortValue()");
        } else if (type == byte.class) {
            sb.append("((Byte) ").append(input).append(").byteValue()");
        } else if (type == boolean.class) {
            sb.append("((Boolean) ").append(input).append(").booleanValue()");
        } else if (type == char.class) {
            sb.append("((Character) ").append(input).append(").charValue()");
        } else {
            throw new UnsupportedOperationException("Unwrap not possible for type: " + type);
        }
    }

    private CtMethod addSetter(AbstractMethodAttribute<?, ?> attribute, CtClass cc, CtField attributeField, String methodName, CtField mutableStateField, boolean dirtyChecking, boolean isId) throws CannotCompileException, NotFoundException {
        FieldInfo finfo = attributeField.getFieldInfo2();
        String fieldType = finfo.getDescriptor();
        String desc = "(" + fieldType + ")V";
        ConstPool cp = finfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, methodName, desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String fieldName = finfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        boolean invalidSetter = false;
        // When the declaring type is updatable/creatable we only allow setting the id value on new objects
        if (isId) {
            if (attribute != null && attribute.getDeclaringType().isCreatable()) {
                sb.append("\tif ($0.$$_kind != (byte) 2) {\n");
                sb.append("\t\tthrow new IllegalArgumentException(\"Updating the id attribute '").append(attribute.getName()).append("' is only allowed for new entity view objects created via EntityViewManager.create()!\");\n");
                sb.append("\t}\n");
            } else if (attribute != null && attribute.getDeclaringType().isUpdatable()) {
                sb.append("\tthrow new IllegalArgumentException(\"Updating the id attribute '").append(attribute.getName()).append("' is only allowed for new entity view objects created via EntityViewManager.create()!\");\n");
                invalidSetter = true;
            }
        }

        // Disallow calling the setter on a mutable only relation with objects of a different identity as that might indicate a programming error
        if (attribute != null && attribute.getDirtyStateIndex() != -1
                && !attribute.isUpdatable() && (attribute.getDeclaringType().isCreatable() || attribute.getDeclaringType().isUpdatable())) {
            sb.append("\tObject tmp;\n");
            sb.append("\tif ($1 != $0.").append(fieldName);

            if (attribute.isCollection()) {
                // TODO: We could theoretically support collections too by looking into them and asserting equality element-wise
            } else {
                SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                Type<?> type = singularAttribute.getType();
                if (attribute.isSubview()) {
                    if (!(type instanceof FlatViewType<?>)) {
                        String idMethodName = ((ViewType<?>) type).getIdAttribute().getJavaMethod().getName();
                        sb.append(" && ");
                        sb.append("($1 == null || (tmp = $1.");
                        sb.append(idMethodName);
                        sb.append("()) == null || !java.util.Objects.equals(tmp, $0.");
                        sb.append(fieldName);
                        sb.append('.').append(idMethodName);
                        sb.append("()))");
                    }
                } else {
                    BasicTypeImpl<?> basicType = (BasicTypeImpl<?>) type;
                    boolean jpaEntity = basicType.isJpaEntity();
                    if (jpaEntity) {
                        IdentifiableType<?> identifiableType = (IdentifiableType<?>) basicType.getManagedType();

                        for (javax.persistence.metamodel.SingularAttribute<?, ?> idAttribute : JpaMetamodelUtils.getIdAttributes(identifiableType)) {
                            Class<?> idClass = JpaMetamodelUtils.resolveFieldClass(basicType.getJavaType(), idAttribute);
                            String idAccessor = addIdAccessor(cc, identifiableType, idAttribute, pool.get(idClass.getName()));
                            sb.append(" && ");
                            sb.append("($1 == null || (tmp = ");
                            sb.append(idAccessor);
                            sb.append("($1)) == null || !java.util.Objects.equals(tmp, ");
                            sb.append(idAccessor);
                            sb.append("($0.");
                            sb.append(fieldName);
                            sb.append(")))");
                        }
                    }
                }
            }

            sb.append(") {\n");
            sb.append("\t\tthrow new IllegalArgumentException(\"Updating the mutable-only attribute '").append(attribute.getName()).append("' with a value that has not the same identity is not allowed! Consider making the attribute updatable or update the value directly instead of replacing it!\");\n");
            sb.append("\t}\n");
        }

        if (attribute != null && attribute.isUpdatable() && dirtyChecking) {
            if (attribute.isCollection()) {
                if (strictCascadingCheck) {
                    // With strict cascading checks enabled, we don't allow setting collections of mutable subviews
                    boolean mutableElement = !attribute.getUpdateCascadeAllowedSubtypes().isEmpty() || !attribute.getPersistCascadeAllowedSubtypes().isEmpty();
                    if (mutableElement && ((AbstractMethodPluralAttribute<?, ?, ?>) attribute).getElementType().getMappingType() != Type.MappingType.BASIC) {
                        sb.append("\t\tthrow new IllegalArgumentException(\"Replacing a collection that PERSIST or UPDATE cascades is prohibited by default! Instead, replace the contents by doing clear() and addAll()!\");\n");
                    }
                }
            } else {
                // Only consider subviews here for now
                if (attribute.isSubview()) {
                    String subtypeArray = addAllowedSubtypeField(cc, attribute);
                    addParentRequiringUpdateSubtypesField(cc, attribute);
                    addParentRequiringCreateSubtypesField(cc, attribute);
                    sb.append("\tif ($1 != null) {\n");
                    sb.append("\t\tClass c;\n");
                    sb.append("\t\tboolean isNew;\n");
                    sb.append("\t\tif ($1 instanceof ").append(EntityViewProxy.class.getName()).append(") {\n");
                    sb.append("\t\t\tc = ((").append(EntityViewProxy.class.getName()).append(") $1).$$_getEntityViewClass();\n");
                    sb.append("\t\t\tisNew = ((").append(EntityViewProxy.class.getName()).append(") $1).$$_isNew();\n");
                    sb.append("\t\t} else {\n");
                    sb.append("\t\t\tc = $1.getClass();\n");
                    sb.append("\t\t\tisNew = false;\n");
                    sb.append("\t\t}\n");

                    sb.append("\t\tif (!").append(attributeField.getDeclaringClass().getName()).append('#').append(attribute.getName()).append("_$$_subtypes.contains(c)) {\n");
                    sb.append("\t\t\tthrow new IllegalArgumentException(");
                    sb.append("\"Allowed subtypes for attribute '").append(attribute.getName()).append("' are [").append(subtypeArray).append("] but got an instance of: \"");
                    sb.append(".concat(c.getName())");
                    sb.append(");\n");
                    sb.append("\t\t}\n");

                    if (strictCascadingCheck) {
                        sb.append("\t\tif ($0 != $1 && !isNew && ").append(attributeField.getDeclaringClass().getName()).append('#').append(attribute.getName()).append("_$$_parentRequiringUpdateSubtypes.contains(c) && !((").append(DirtyTracker.class.getName()).append(") $1).$$_hasParent()) {\n");
                        sb.append("\t\t\tthrow new IllegalArgumentException(");
                        sb.append("\"Setting instances of type [\" + c.getName() + \"] on attribute '").append(attribute.getName()).append("' is not allowed until they are assigned to an attribute that cascades the type! ");
                        sb.append("If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { UPDATE }). You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false\"");
                        sb.append(");\n");
                        sb.append("\t\t}\n");

                        sb.append("\t\tif ($0 != $1 && isNew && ").append(attributeField.getDeclaringClass().getName()).append('#').append(attribute.getName()).append("_$$_parentRequiringCreateSubtypes.contains(c) && !((").append(DirtyTracker.class.getName()).append(") $1).$$_hasParent()) {\n");
                        sb.append("\t\t\tthrow new IllegalArgumentException(");
                        sb.append("\"Setting instances of type [\" + c.getName() + \"] on attribute '").append(attribute.getName()).append("' is not allowed until they are assigned to an attribute that cascades the type! ");
                        sb.append("If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { PERSIST }). You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false\"");
                        sb.append(");\n");
                        sb.append("\t\t}\n");
                    }

                    sb.append("\t}\n");
                }
            }
        }

        if (attribute != null && attribute.getDirtyStateIndex() != -1) {
            int mutableStateIndex = attribute.getDirtyStateIndex();
            // Unset previous object parent
            if (attribute.isCollection()) {
                sb.append("\tif ($0.").append(fieldName).append(" != null && $0.").append(fieldName).append(" != $1) {\n");
                if (attribute instanceof MapAttribute<?, ?, ?>) {
                    sb.append("\t\tif ($0.").append(fieldName).append(" instanceof ").append(RecordingMap.class.getName()).append(") {\n");
                    sb.append("\t\t\t((").append(RecordingMap.class.getName()).append(") $0.").append(fieldName).append(").$$_unsetParent();\n");
                    sb.append("\t\t}\n");
                } else {
                    sb.append("\t\tif ($0.").append(fieldName).append(" instanceof ").append(RecordingCollection.class.getName()).append(") {\n");
                    sb.append("\t\t\t((").append(RecordingCollection.class.getName()).append(") $0.").append(fieldName).append(").$$_unsetParent();\n");
                    sb.append("\t\t}\n");
                }
                sb.append("\t}\n");
            } else if (attribute.isSubview()) {
                if (attribute.isUpdatableOnly() && !attribute.isCorrelated()) {
                    sb.append("\tif ($0.").append(fieldName).append(" != $1 && $0.").append(fieldName).append(" instanceof ").append(MutableStateTrackable.class.getName()).append(") {\n");
                    sb.append("\t\t\t((").append(MutableStateTrackable.class.getName()).append(") $0.").append(fieldName).append(").$$_removeReadOnlyParent($0, ").append(mutableStateIndex).append(");\n");
                    sb.append("\t} else if ($0.").append(fieldName).append(" != $1 && $0.").append(fieldName).append(" instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                    sb.append("\t\t\t((").append(BasicDirtyTracker.class.getName()).append(") $0.").append(fieldName).append(").$$_unsetParent();\n");
                } else {
                    sb.append("\tif ($0.").append(fieldName).append(" != $1 && $0.").append(fieldName).append(" instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                    sb.append("\t\t((").append(BasicDirtyTracker.class.getName()).append(") $0.").append(fieldName).append(").$$_unsetParent();\n");
                }
                sb.append("\t}\n");
            }

            if (mutableStateField != null) {
                // this.mutableState[mutableStateIndex] = $1
                sb.append("\t$0.").append(mutableStateField.getName()).append("[").append(mutableStateIndex).append("] = ");
                renderValueForArray(sb, attributeField.getType(), "$1");
            }
            if (dirtyChecking) {
                // this.dirty = true
                sb.append("\t$0.$$_markDirty(").append(mutableStateIndex).append(");\n");

                // Set new objects parent
                if (attribute.isCollection() || attribute.isSubview()) {
                    sb.append("\tif ($0.$$_initialized && $1 != null && $0.").append(fieldName).append(" != $1) {\n");
                    if (attribute.isCollection()) {
                        if (attribute instanceof MapAttribute<?, ?, ?>) {
                            sb.append("\t\tif ($1 instanceof ").append(RecordingMap.class.getName()).append(") {\n");
                            sb.append("\t\t\t((").append(RecordingMap.class.getName()).append(") $1).$$_setParent($0, ").append(mutableStateIndex).append(");\n");
                            sb.append("\t\t}\n");
                        } else {
                            sb.append("\t\tif ($1 instanceof ").append(RecordingCollection.class.getName()).append(") {\n");
                            sb.append("\t\t\t((").append(RecordingCollection.class.getName()).append(") $1).$$_setParent($0, ").append(mutableStateIndex).append(");\n");
                            sb.append("\t\t}\n");
                        }
                    } else if (attribute.isSubview()) {
                        // Correlated attributes are treated special, we don't consider correlated attributes read-only parents
                        if (attribute.isUpdatableOnly() && !attribute.isCorrelated()) {
                            sb.append("\t\tif ($1 instanceof ").append(MutableStateTrackable.class.getName()).append(") {\n");
                            sb.append("\t\t\t((").append(MutableStateTrackable.class.getName()).append(") $1).$$_addReadOnlyParent($0, ").append(mutableStateIndex).append(");\n");
                            sb.append("\t\t} else if ($1 instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                            sb.append("\t\t\t((").append(BasicDirtyTracker.class.getName()).append(") $1).$$_setParent($0, ").append(mutableStateIndex).append(");\n");
                        } else {
                            sb.append("\t\tif ($1 instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                            sb.append("\t\t\t((").append(BasicDirtyTracker.class.getName()).append(") $1).$$_setParent($0, ").append(mutableStateIndex).append(");\n");
                        }
                        sb.append("\t\t}\n");
                    }
                    sb.append("\t}\n");
                }
            }
        }

        if (!invalidSetter) {
            sb.append("\t$0.").append(fieldName).append(" = $1;\n");
        }

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }
    
    private List<Method> getBridgeGetters(Class<?> clazz, MethodAttribute<?, ?> attribute, Method getter) {
        List<Method> bridges = new ArrayList<Method>();
        String name = getter.getName();
        Class<?> attributeType = attribute.getConvertedJavaType();

        for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
            METHOD: for (Method m : c.getDeclaredMethods()) {
                if (name.equals(m.getName()) && m.getReturnType().isAssignableFrom(attributeType) && !attributeType.equals(m.getReturnType())) {
                    for (Method b : bridges) {
                        if (b.getReturnType().equals(m.getReturnType())) {
                            continue METHOD;
                        }
                    }

                    bridges.add(m);
                }
            }
        }

        return bridges;
    }

    private List<Method> getBridgeSetters(Class<?> clazz, MethodAttribute<?, ?> attribute, Method setter) {
        List<Method> bridges = new ArrayList<Method>();
        String name = setter.getName();
        Class<?> attributeType = attribute.getConvertedJavaType();

        for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
            METHOD: for (Method m : c.getDeclaredMethods()) {
                if (name.equals(m.getName()) && m.getParameterTypes()[0].isAssignableFrom(attributeType) && !attributeType.equals(m.getParameterTypes()[0])) {
                    for (Method b : bridges) {
                        if (b.getParameterTypes()[0].equals(m.getParameterTypes()[0])) {
                            continue METHOD;
                        }
                    }
                    bridges.add(m);
                }
            }
        }

        return bridges;
    }

    private void setGenericSignature(CtField field, String signature) {
        FieldInfo fieldInfo = field.getFieldInfo();
        fieldInfo.addAttribute(new SignatureAttribute(fieldInfo.getConstPool(), signature));
    }
    
    private void setGenericSignature(CtMethod method, String signature) {
        MethodInfo methodInfo = method.getMethodInfo();
        methodInfo.addAttribute(new SignatureAttribute(methodInfo.getConstPool(), signature));
    }

    private String getEqualsDesc() throws NotFoundException {
        CtClass returnType = CtClass.booleanType;
        return "(" + Descriptor.of("java.lang.Object") + ")" + Descriptor.of(returnType);
    }

    private CtMethod createEquals(ManagedViewTypeImplementor<?> managedViewType, CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        return createEquals(managedViewType, cc, false, fields);
    }

    private CtMethod createIdEquals(ManagedViewTypeImplementor<?> managedViewType, CtClass cc) throws NotFoundException, CannotCompileException {
        return createEquals(managedViewType, cc, true, null);
    }

    private CtMethod createEquals(ManagedViewTypeImplementor<?> managedViewType, CtClass cc, boolean idBased, CtField[] fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo method = new MethodInfo(cp, "equals", getEqualsDesc());
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tif ($0 == $1) { return true; }\n");

        Class<?> viewClass = managedViewType.getJavaType();
        if (idBased) {
            sb.append("\tif ($1 == null || $0.$$_getId() == null) { return false; }\n");
            sb.append("\tif ($1 instanceof ").append(EntityViewProxy.class.getName()).append(") {\n");
            sb.append("\t\tif ($0.$$_getJpaManagedBaseClass() == ((").append(EntityViewProxy.class.getName()).append(") $1).$$_getJpaManagedBaseClass() && ");
            sb.append("$0.$$_getId().equals(((").append(EntityViewProxy.class.getName()).append(") $1).$$_getId())) {\n");
            sb.append("\t\t\treturn true;\n");
            sb.append("\t\t} else {\n");
            sb.append("\t\t\treturn false;\n");
            sb.append("\t\t}\n");
            sb.append("\t}\n");
            ViewTypeImplementor<?> viewType = (ViewTypeImplementor<?>) managedViewType;
            MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
            if (viewType.supportsUserTypeEquals()) {
                String wrap;
                Class<?> javaType = idAttribute.getJavaType();
                if (javaType.isPrimitive()) {
                    if (javaType == long.class) {
                        wrap = "Long.valueOf";
                    } else if (javaType == float.class) {
                        wrap = "Float.valueOf";
                    } else if (javaType == double.class) {
                        wrap = "Double.valueOf";
                    } else if (javaType == short.class) {
                        wrap = "Short.valueOf";
                    } else if (javaType == byte.class) {
                        wrap = "Byte.valueOf";
                    } else if (javaType == boolean.class) {
                        wrap = "Boolean.valueOf";
                    } else if (javaType == char.class) {
                        wrap = "Character.valueOf";
                    } else {
                        wrap = "Integer.valueOf";
                    }
                } else {
                    wrap = "";
                }
                sb.append("\tif ($1 instanceof ").append(viewClass.getName()).append(" && $0.$$_getId().equals(").append(wrap).append("(((").append(viewClass.getName()).append(") $1).get");
                StringUtils.addFirstToUpper(sb, idAttribute.getName()).append("()))) {\n");
                sb.append("\t\t\treturn true;\n");
                sb.append("\t\t} else {\n");
                sb.append("\t\t\treturn false;\n");
                sb.append("\t\t}\n");
            } else if (viewType.supportsInterfaceEquals()) {
                sb.append("\t\tthrow new IllegalArgumentException(\"The view class ").append(viewClass.getName()).append(" is defined for an abstract or non-entity type which is why id-based equality can't be checked on the user provided instance: \" + $1);\n");
            } else {
                sb.append("\t\tthrow new IllegalArgumentException(\"A superclass of ").append(viewClass.getName()).append(" declares a protected or default attribute that is relevant for checking for state equality which can't be accessed on the user provided instance: \" + $1);\n");
            }
        } else {
            String name;
            if (managedViewType.supportsInterfaceEquals()) {
                name = viewClass.getName();
            } else {
                name = cc.getName();
            }

            sb.append("\tif ($1 instanceof ").append(name).append(") {\n");
            sb.append("\t\tfinal ").append(name).append(" other = (").append(name).append(") $1;\n");

            for (CtField field : fields) {
                if (field.getType().isPrimitive()) {
                    if (CtClass.booleanType == field.getType() && managedViewType.getAttribute(field.getName()).getJavaMethod().getName().startsWith("is")) {
                        sb.append("\t\tif ($0.").append(field.getName()).append(" != other.is");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(") {\n");
                    } else {
                        sb.append("\t\tif ($0.").append(field.getName()).append(" != other.get");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(") {\n");
                    }
                } else {
                    if (Boolean.class.getName().equals(field.getType().getName()) && managedViewType.getAttribute(field.getName()).getJavaMethod().getName().startsWith("is")) {
                        sb.append("\t\tif ($0.").append(field.getName()).append(" != other.is");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(" && ($0.").append(field.getName()).append(" == null");
                        sb.append(" || !$0.").append(field.getName()).append(".equals(other.is");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append("))) {\n");
                    } else {
                        sb.append("\t\tif ($0.").append(field.getName()).append(" != other.get");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(" && ($0.").append(field.getName()).append(" == null");
                        sb.append(" || !$0.").append(field.getName()).append(".equals(other.get");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append("))) {\n");
                    }
                }
                sb.append("\t\t\treturn false;\n\t\t}\n");
            }

            sb.append("\t} else {\n");
            if (managedViewType.supportsInterfaceEquals()) {
                sb.append("\t\treturn false;\n");
            } else {
                sb.append("\t\tif ($1 == null) { return false; }\n");
                sb.append("\t\tthrow new IllegalArgumentException(\"A superclass of ").append(viewClass.getName()).append(" declares a protected or default attribute that is relevant for checking for state equality which can't be accessed on the user provided instance: \" + $1);\n");
            }
            sb.append("\t}\n");
            sb.append("\treturn true;\n");
        }

        sb.append('}');
        m.setBody(sb.toString());
        return m;
    }

    private String getHashCodeDesc() throws NotFoundException {
        CtClass returnType = CtClass.intType;
        return "()" + Descriptor.of(returnType);
    }

    private CtMethod createHashCode(CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass returnType = CtClass.intType;
        String desc = "()" + Descriptor.of(returnType);
        MethodInfo method = new MethodInfo(cp, "hashCode", desc);
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tlong bits;\n");
        sb.append("\tint hash = 3;\n");

        for (CtField field : fields) {
            if (field.getType().isPrimitive()) {
                CtClass type = field.getType();
                if (CtClass.doubleType == type) {
                    sb.append("bits = java.lang.Double.doubleToLongBits($0.").append(field.getName()).append(");");
                }
                sb.append("\thash = 83 * hash + ");
                if (CtClass.booleanType == type) {
                    sb.append("($0.").append(field.getName()).append(" ? 1231 : 1237").append(");\n");
                } else if (CtClass.byteType == type || CtClass.shortType == type || CtClass.charType == type) {
                    sb.append("(int) $0.").append(field.getName()).append(";\n");
                } else if (CtClass.intType == type) {
                    sb.append("$0.").append(field.getName()).append(";\n");
                } else if (CtClass.longType == type) {
                    sb.append("(int)(");
                    sb.append("$0.").append(field.getName());
                    sb.append(" ^ (");
                    sb.append("$0.").append(field.getName());
                    sb.append(" >>> 32));\n");
                } else if (CtClass.floatType == type) {
                    sb.append("java.lang.Float.floatToIntBits(");
                    sb.append("$0.").append(field.getName());
                    sb.append(");\n");
                } else if (CtClass.doubleType == type) {
                    sb.append("(int)(bits ^ (bits >>> 32));\n");
                } else {
                    throw new IllegalArgumentException("Unsupported primitive type: " + type.getName());
                }
            } else {
                sb.append("\thash = 83 * hash + ($0.").append(field.getName()).append(" != null ? ");
                sb.append("$0.").append(field.getName()).append(".hashCode() : 0);\n");
            }
        }

        sb.append("\treturn hash;\n");
        sb.append('}');
        m.setBody(sb.toString());
        return m;
    }

    private CtMethod createToString(ManagedViewTypeImplementor<?> managedViewType, CtClass cc, boolean idBased, CtField[] fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo method = new MethodInfo(cp, "toString", "()" + Descriptor.of("java.lang.String"));
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');

        if (idBased) {
            ViewTypeImplementor<?> viewType = (ViewTypeImplementor<?>) managedViewType;
            MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
            sb.append("\treturn \"").append(managedViewType.getJavaType().getSimpleName()).append("(").append(idAttribute.getName()).append(" = \" + $0.").append(idAttribute.getName()).append(" + \")\";\n");
        } else {
            int sizeEstimate = managedViewType.getJavaType().getSimpleName().length() + 2;
            for (int i = 0; i < fields.length; i++) {
                // 5 is the amount of chars for the format
                // 10 is the amount of chars that we assume is needed to represent a value on average
                sizeEstimate += fields[i].getName().length() + 5 + 10;
            }
            sb.append("\tStringBuilder sb = new StringBuilder(").append(sizeEstimate).append(");\n");
            sb.append("\tsb.append(\"").append(managedViewType.getJavaType().getSimpleName()).append("(\");\n");

            if (fields.length != 0) {
                sb.append("\tsb.append(\"").append(fields[0].getName()).append(" = \").append($0.").append(fields[0].getName()).append(");\n");

                for (int i = 1; i < fields.length; i++) {
                    sb.append("\tsb.append(\", \");\n");
                    sb.append("\tsb.append(\"").append(fields[i].getName()).append(" = \").append($0.").append(fields[i].getName()).append(");\n");
                }
            }

            sb.append("\tsb.append(\")\");\n");
            sb.append("\treturn sb.toString();\n");
        }

        sb.append('}');
        m.setBody(sb.toString());
        return m;
    }

    private CtMethod createGetterBridge(CtClass cc, Method getter, CtMethod attributeGetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeReturnType = pool.get(getter.getReturnType().getName());
        String desc = "()" + Descriptor.of(bridgeReturnType);
        MethodInfo bridge = new MethodInfo(cp, getter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);

        Bytecode code = new Bytecode(cp, needsTwoStackSlots(bridgeReturnType) ? 2 : 1, 1);
        code.addAload(0);
        code.addInvokevirtual(cc, getter.getName(), attributeGetter.getReturnType(), null);
        code.addReturn(bridgeReturnType);

        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private CtMethod createSetterBridge(CtClass cc, Method setter, CtMethod attributeSetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeParameterType = pool.get(setter.getParameterTypes()[0].getName());
        String desc = "(" + Descriptor.of(bridgeParameterType) + ")V";
        MethodInfo bridge = new MethodInfo(cp, setter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);

        Bytecode code = new Bytecode(cp, needsTwoStackSlots(bridgeParameterType) ? 4 : 2, 2);
        code.addAload(0);
        code.addAload(1);
        code.addCheckcast(attributeSetter.getParameterTypes()[0]);
        code.addInvokevirtual(cc, setter.getName(), CtClass.voidType, attributeSetter.getParameterTypes());
        code.addReturn(CtClass.voidType);

        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private CtConstructor createNormalConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, MappingConstructor<?> constructor, CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, CtField initialStateField, CtField mutableStateField,
                                                  AbstractMethodAttribute<?, ?>[] attributes, int mutableAttributeCount, long alwaysDirtyMask, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        int superConstructorStart = attributeFields.length;
        int superConstructorEnd = attributeTypes.length;
        return createConstructor(evm, managedViewType, constructor, cc, superConstructorStart, superConstructorEnd, attributeFields, attributeTypes, initialStateField, mutableStateField, attributes, mutableAttributeCount, ConstructorKind.NORMAL, null, alwaysDirtyMask, unsafe);
    }

    private CtConstructor createCreateConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, CtField idField, CtField initialStateField, CtField mutableStateField,
                                                  AbstractMethodAttribute<?, ?>[] attributes, int mutableAttributeCount, long alwaysDirtyMask, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        return createConstructor(evm, managedViewType, null, cc, 0, 0, attributeFields, attributeTypes, initialStateField, mutableStateField, attributes, mutableAttributeCount, ConstructorKind.CREATE, idField, alwaysDirtyMask, unsafe);
    }

    private CtConstructor createReferenceConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, CtField[] attributeFields, CtField idField, CtField initialStateField, CtField mutableStateField,
                                                     AbstractMethodAttribute<?, ?>[] attributes, int mutableAttributeCount, long alwaysDirtyMask, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        CtClass[] attributeTypes = new CtClass[]{ idField.getType() };
        return createConstructor(evm, managedViewType, null, cc, 0, 0, attributeFields, attributeTypes, initialStateField, mutableStateField, attributes, mutableAttributeCount, ConstructorKind.REFERENCE, idField, alwaysDirtyMask, unsafe);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private enum ConstructorKind {
        CREATE,
        NORMAL,
        REFERENCE;
    }

    private CtConstructor createConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, MappingConstructor<?> constructor, CtClass cc, int superConstructorStart, int superConstructorEnd, CtField[] attributeFields, CtClass[] attributeTypes, CtField initialStateField, CtField mutableStateField,
                                            AbstractMethodAttribute<?, ?>[] attributes, int mutableAttributeCount, ConstructorKind kind, CtField idField, long alwaysDirtyMask, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        CtClass[] parameterTypes;
        if (kind == ConstructorKind.CREATE) {
            parameterTypes = new CtClass[]{ cc, pool.get(Map.class.getName()) };
        } else {
            parameterTypes = attributeTypes;
        }
        CtConstructor ctConstructor = new CtConstructor(parameterTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (unsafe) {
            renderFieldInitialization(evm, managedViewType, constructor, attributeFields, initialStateField, mutableStateField, attributes, mutableAttributeCount, kind, alwaysDirtyMask, sb, unsafe, idField);
            renderSuperCall(constructor, cc, superConstructorStart, superConstructorEnd, sb);
        } else {
            renderSuperCall(constructor, cc, superConstructorStart, superConstructorEnd, sb);
            renderFieldInitialization(evm, managedViewType, constructor, attributeFields, initialStateField, mutableStateField, attributes, mutableAttributeCount, kind, alwaysDirtyMask, sb, unsafe, idField);
        }

        // Always register dirty tracker after super call
        renderDirtyTrackerRegistration(attributeFields, mutableStateField, attributes, kind, sb);
        finishConstructorWithPostConstruct(managedViewType, cc, kind, unsafe, ctConstructor, sb);

        return ctConstructor;
    }

    private CtConstructor createTupleConstructor(ManagedViewType<?> managedViewType, MappingConstructor<?> constructor, CtClass cc, int superConstructorStart, int superConstructorEnd, CtField[] attributeFields, CtClass[] attributeTypes, CtField initialStateField, CtField mutableStateField,
                                                 AbstractMethodAttribute<?, ?>[] attributes, int mutableAttributeCount, boolean assignment, long alwaysDirtyMask, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        CtClass[] parameterTypes;
        if (assignment) {
            parameterTypes = new CtClass[(superConstructorEnd - superConstructorStart) + 4];
            parameterTypes[0] = cc;
            parameterTypes[1] = CtClass.intType;
            parameterTypes[2] = pool.get("int[]");
            parameterTypes[3] = pool.get("java.lang.Object[]");
            System.arraycopy(attributeTypes, superConstructorStart, parameterTypes, 4, superConstructorEnd - superConstructorStart);
        } else {
            parameterTypes = new CtClass[(superConstructorEnd - superConstructorStart) + 3];
            parameterTypes[0] = cc;
            parameterTypes[1] = CtClass.intType;
            parameterTypes[2] = pool.get("java.lang.Object[]");
            System.arraycopy(attributeTypes, superConstructorStart, parameterTypes, 3, superConstructorEnd - superConstructorStart);
        }
        CtConstructor ctConstructor = new CtConstructor(parameterTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (unsafe) {
            renderFieldInitialization(constructor, attributeFields, initialStateField, mutableStateField, attributes, mutableAttributeCount, assignment, alwaysDirtyMask, unsafe, sb);
            renderTupleSuperCall(constructor, cc, superConstructorStart, superConstructorEnd, assignment, attributeTypes, sb);
        } else {
            renderTupleSuperCall(constructor, cc, superConstructorStart, superConstructorEnd, assignment, attributeTypes, sb);
            renderFieldInitialization(constructor, attributeFields, initialStateField, mutableStateField, attributes, mutableAttributeCount, assignment, alwaysDirtyMask, unsafe, sb);
        }

        // Always register dirty tracker after super call
        renderDirtyTrackerRegistration(attributeFields, mutableStateField, attributes, ConstructorKind.NORMAL, sb);
        finishConstructorWithPostConstruct(managedViewType, cc, ConstructorKind.NORMAL, unsafe, ctConstructor, sb);

        return ctConstructor;
    }

    private void finishConstructorWithPostConstruct(ManagedViewType<?> managedViewType, CtClass cc, ConstructorKind kind, boolean unsafe, CtConstructor ctConstructor, StringBuilder sb) throws CannotCompileException, NotFoundException, BadBytecode {
        Method postConstructMethod = null;
        String postConstructField = null;
        String postConstructMethodName = null;
        if (kind == ConstructorKind.CREATE && managedViewType.getPostCreateMethod() != null) {
            postConstructMethod = managedViewType.getPostCreateMethod();
            postConstructField = "POST_CREATE";
            postConstructMethodName = "$$_getPostCreate";
        } else if (kind == ConstructorKind.NORMAL && managedViewType.getPostLoadMethod() != null) {
            postConstructMethod = managedViewType.getPostLoadMethod();
            postConstructField = "POST_LOAD";
            postConstructMethodName = "$$_getPostLoad";
        }
        if (postConstructMethod != null) {
            // Skip invocation of post-construct method if the type is an interface
            // Note that if you change the invocation here, the invocation below has to be changed as well
            if (!managedViewType.getJavaType().isInterface()) {
                if (Modifier.isPublic(postConstructMethod.getModifiers()) || Modifier.isProtected(postConstructMethod.getModifiers()) || !Modifier.isPrivate(postConstructMethod.getModifiers()) && Objects.equals(postConstructMethod.getDeclaringClass().getPackage() == null ? null : postConstructMethod.getDeclaringClass().getPackage().getName(), cc.getPackageName())) {
                    if (postConstructMethod.getParameterTypes().length == 1) {
                        sb.append("\t$0.").append(postConstructMethod.getName()).append("(").append(cc.getName()).append("#").append(SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME).append(");\n");
                    } else {
                        sb.append("\t$0.").append(postConstructMethod.getName()).append("();\n");
                    }
                } else {
                    String args;
                    if (postConstructMethod.getParameterTypes().length == 1) {
                        args = "new Class[]{ " + EntityViewManager.class.getName() + ".class }";
                    } else {
                        args = "new Class[0]";
                    }
                    try {
                        cc.getField(postConstructField);
                    } catch (NotFoundException ex) {
                        cc.addMethod(CtMethod.make("private static java.lang.reflect.Method " + postConstructMethodName + "() { java.lang.reflect.Method m = " + postConstructMethod.getDeclaringClass().getName() + ".class.getDeclaredMethod(\"" + postConstructMethod.getName() + "\", " + args + "); m.setAccessible(true); return m; }", cc));
                        CtField postConstruct = new CtField(pool.get(Method.class.getName()), postConstructField, cc);
                        postConstruct.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                        cc.addField(postConstruct, CtField.Initializer.byCall(cc, postConstructMethodName));
                    }
                    sb.append("\t").append(cc.getName()).append("#").append(postConstructField).append(".invoke($0");
                    if (postConstructMethod.getParameterTypes().length == 1) {
                        sb.append(", new Object[]{ ").append(cc.getName()).append("#").append(SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME).append(" }");
                    } else {
                        sb.append(", new Object[0]");
                    }
                    sb.append(");");
                }
                postConstructMethod = null;
            }
        }
        sb.append("}");
        if (unsafe) {
            compileUnsafe(ctConstructor, sb.toString());
        } else {
            ctConstructor.setBody(sb.toString());
        }

        // If the method invocation was generated for the abstract class, we set the method to null in the above code
        if (postConstructMethod != null) {
            // Invoke default method on interface
            // The javac included in the currently used Javassist version does not support the required Java syntax
            // so we add the invocation directly via bytecode.

            CodeAttribute codeAttribute = ctConstructor.getMethodInfo().getCodeAttribute();
            Bytecode bc = new Bytecode(codeAttribute.getConstPool(), codeAttribute.getMaxStack(), codeAttribute.getMaxLocals());

            byte[] instructions = codeAttribute.getCode();
            // Add all existing instructions except for return
            for (int i = 0; i < codeAttribute.getCodeLength() - 1; i++) {
                bc.add(instructions[i]);
            }

            String postConstructMethodDescriptor;
            bc.addAload(0);
            if (postConstructMethod.getParameterTypes().length == 1) {
                bc.addGetstatic(cc, SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME, Descriptor.of(EntityViewManager.class.getName()));
                postConstructMethodDescriptor = "(L" + Descriptor.toJvmName(EntityViewManager.class.getName()) + ";)V";
            } else {
                postConstructMethodDescriptor = "()V";
            }
            // Since we implement this default method, we must use invokevirtual
            bc.addInvokevirtual(cc, postConstructMethod.getName(), postConstructMethodDescriptor);
            bc.addReturn(null);

            CodeAttribute newCodeAttribute = bc.toCodeAttribute();
            StackMap stackMap = (StackMap) codeAttribute.getAttribute(StackMap.tag);
            newCodeAttribute.setAttribute(stackMap);
            newCodeAttribute.setAttribute((StackMapTable) codeAttribute.getAttribute(StackMapTable.tag));
            ctConstructor.getMethodInfo().setCodeAttribute(bc.toCodeAttribute());
            // Apparently, the stack map is sometimes not properly built, so we instruct it explicitly to build it
            if (stackMap == null) {
                ctConstructor.getMethodInfo().rebuildStackMap(cc.getClassPool());
            }
        }
    }

    private void compileUnsafe(CtConstructor ctConstructor, String src) throws CannotCompileException {
        // This is essentially what javassist.compiler.Javac.compileBody does, except for an unsafe part
        CtClass cc = ctConstructor.getDeclaringClass();
        MethodInfo methodInfo = ctConstructor.getMethodInfo();
        try {
            Bytecode b = new Bytecode(cc.getClassFile2().getConstPool(), 0, 0);
            JvstCodeGen gen = new JvstCodeGen(b, cc, cc.getClassPool());
            SymbolTable stable = new SymbolTable();

            int mod = ctConstructor.getModifiers();
            gen.recordParams(ctConstructor.getParameterTypes(), Modifier.isStatic(mod), "$", "$args", "$$", stable);

            gen.recordType(CtClass.voidType);
            gen.recordReturnType(CtClass.voidType, "$r", null, stable);

            Parser p = new Parser(new Lex(src));
            SymbolTable stb = new SymbolTable(stable);
            Stmnt s = p.parseStatement(stb);
            if (p.hasMore()) {
                throw new CompileError(
                        "the method/constructor body must be surrounded by {}");
            }
            // setting callSuper to false is the unsafe part...
            boolean callSuper = false;
            gen.atMethodBody(s, callSuper, true);

            methodInfo.setCodeAttribute(b.toCodeAttribute());
            methodInfo.setAccessFlags(methodInfo.getAccessFlags()
                    & ~AccessFlag.ABSTRACT);
            methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            cc.rebuildClassFile();
        } catch (CompileError e) {
            throw new CannotCompileException(e);
        } catch (BadBytecode e) {
            throw new CannotCompileException(e);
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }

    private void renderFieldInitialization(EntityViewManager entityViewManager, ManagedViewType<?> managedViewType, MappingConstructor<?> constructor, CtField[] attributeFields, CtField initialStateField, CtField mutableStateField,
                                           AbstractMethodAttribute<?, ?>[] methodAttributes, int mutableAttributeCount, ConstructorKind kind, long alwaysDirtyMask, StringBuilder sb, boolean unsafe, CtField idField) throws NotFoundException, CannotCompileException {
        if (initialStateField != null) {
            sb.append("\tObject[] initialStateArr = new Object[").append(mutableAttributeCount).append("];\n");
        }

        if (mutableStateField != null) {
            sb.append("\tObject[] mutableStateArr = new Object[").append(mutableAttributeCount).append("];\n");
            if (unsafe) {
                sb.append("\t$0.$$_dirty = ").append(alwaysDirtyMask).append("L;\n");
            } else {
                sb.append("\t$0.$$_dirty |= ").append(alwaysDirtyMask).append("L;\n");
            }
        }

        if (kind == ConstructorKind.CREATE && managedViewType.isCreatable()) {
            sb.append("\t$0.$$_kind = (byte) 2;\n");
        } else if (kind == ConstructorKind.REFERENCE) {
            sb.append("\t$0.$$_kind = (byte) 1;\n");
        }

        for (int i = 0; i < attributeFields.length; i++) {
            if (attributeFields[i] == null) {
                continue;
            }

            AbstractMethodAttribute<?, ?> methodAttribute = methodAttributes[i];

            boolean possiblyInitialized = !unsafe && appendPossiblyInitialized(sb, constructor, methodAttribute, attributeFields[i]);
            if (possiblyInitialized) {
                sb.append('\t');
            }
            sb.append("\t$0.").append(attributeFields[i].getName()).append(" = ");
            if (kind != ConstructorKind.CREATE && attributeFields[i] == idField) {
                // The id field for the reference and normal constructor are never empty
                sb.append('$').append(i + 1).append(";\n");
                if (possiblyInitialized) {
                    sb.append("\t}\n");
                }
            } else if (kind != ConstructorKind.NORMAL) {
                CtClass type = attributeFields[i].getType();
                if (type.isPrimitive()) {
                    if (kind == ConstructorKind.CREATE && methodAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                        String value = "$2.get(\"" + ((MappingAttribute<?, ?>) methodAttribute).getMapping() + "\")";
                        sb.append(value).append(" != null ? ");
                        appendUnwrap(sb, type, value);
                        sb.append(" : ");
                    }
                    sb.append(getDefaultValue(type)).append(";\n");
                    if (mutableStateField != null && methodAttribute != null && methodAttribute.hasDirtyStateIndex()) {
                        if (possiblyInitialized) {
                            sb.append("\t");
                        }
                        sb.append("\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        if (initialStateField != null && kind != ConstructorKind.REFERENCE) {
                            sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }
                        sb.append(getDefaultValueForObject(type)).append(";\n");

                        if (possiblyInitialized) {
                            sb.append("\t} else {\n");
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = $");
                            renderValueForArray(sb, type, "$0." + attributeFields[i].getName());
                            sb.append(";\n");
                            sb.append("\t}\n");
                        }

                        if (initialStateField != null && kind != ConstructorKind.REFERENCE) {
                            sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                            sb.append(getDefaultValueForObject(type)).append(";\n");
                        }
                    } else if (possiblyInitialized) {
                        sb.append("\t}\n");
                    }
                } else if (methodAttribute != null && methodAttribute.hasDirtyStateIndex()) {
                    if (mutableStateField != null) {
                        sb.append("mutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");

                        if (initialStateField != null && (kind != ConstructorKind.REFERENCE || methodAttribute instanceof SingularAttribute<?, ?> && ((SingularAttribute) methodAttribute).isCreateEmptyFlatView())) {
                            sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }
                    }
                    // init embeddables and collections
                    if (methodAttribute instanceof PluralAttribute<?, ?, ?>) {
                        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) methodAttribute;
                        addAllowedSubtypeField(attributeFields[i].getDeclaringClass(), methodAttribute);
                        addParentRequiringUpdateSubtypesField(attributeFields[i].getDeclaringClass(), methodAttribute);
                        addParentRequiringCreateSubtypesField(attributeFields[i].getDeclaringClass(), methodAttribute);

                        switch (pluralAttribute.getCollectionType()) {
                            case MAP:
                                if (pluralAttribute.isSorted()) {
                                    sb.append("new ").append(RecordingNavigableMap.class.getName()).append('(');
                                    sb.append("(java.util.NavigableMap) new java.util.TreeMap(");
                                    if (pluralAttribute.getComparatorClass() != null) {
                                        sb.append("new ").append(pluralAttribute.getComparatorClass().getName()).append("()");
                                    }
                                    sb.append("),");
                                } else if (pluralAttribute.isOrdered()) {
                                    sb.append("new ").append(RecordingMap.class.getName()).append('(');
                                    sb.append("(java.util.Map) new java.util.LinkedHashMap(),true,");
                                } else {
                                    sb.append("new ").append(RecordingMap.class.getName()).append('(');
                                    sb.append("(java.util.Map) new java.util.HashMap(),false,");
                                }
                                break;
                            case SET:
                                if (pluralAttribute.isSorted()) {
                                    sb.append("new ").append(RecordingNavigableSet.class.getName()).append('(');
                                    sb.append("(java.util.NavigableSet) new java.util.TreeSet(");
                                    if (pluralAttribute.getComparatorClass() != null) {
                                        sb.append("new ").append(pluralAttribute.getComparatorClass().getName()).append("()");
                                    }
                                    sb.append("),");
                                } else if (pluralAttribute.isOrdered()) {
                                    sb.append("new ").append(RecordingSet.class.getName()).append('(');
                                    sb.append("(java.util.Set) new java.util.LinkedHashSet(),true,");
                                } else {
                                    sb.append("new ").append(RecordingSet.class.getName()).append('(');
                                    sb.append("(java.util.Set) new java.util.HashSet(),false,");
                                }
                                break;
                            case LIST:
                                sb.append("new ").append(RecordingList.class.getName()).append('(');
                                sb.append("(java.util.List) new java.util.ArrayList(),");
                                sb.append(pluralAttribute.isIndexed()).append(',');
                                break;
                            default:
                                sb.append("new ").append(RecordingCollection.class.getName()).append('(');
                                sb.append("(java.util.Collection) new java.util.ArrayList(),false,false,");
                                break;
                        }
                        sb.append(attributeFields[i].getDeclaringClass().getName()).append('#');
                        sb.append(methodAttribute.getName()).append("_$$_subtypes").append(',');
                        sb.append(attributeFields[i].getDeclaringClass().getName()).append('#');
                        sb.append(methodAttribute.getName()).append("_$$_parentRequiringUpdateSubtypes").append(',');
                        sb.append(attributeFields[i].getDeclaringClass().getName()).append('#');
                        sb.append(methodAttribute.getName()).append("_$$_parentRequiringCreateSubtypes").append(',');
                        sb.append(methodAttribute.isUpdatable()).append(',');
                        sb.append(methodAttribute.isOptimizeCollectionActionsEnabled()).append(',');
                        sb.append(strictCascadingCheck);
                        sb.append(");\n");
                    } else {
                        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) methodAttribute;
                        if (singularAttribute.isCreateEmptyFlatView()) {
                            ManagedViewTypeImplementor<Object> attributeManagedViewType = (ManagedViewTypeImplementor<Object>) singularAttribute.getType();
                            String proxyClassName = getProxy(entityViewManager, attributeManagedViewType).getName();
                            sb.append("new ");
                            sb.append(proxyClassName);
                            sb.append("((").append(proxyClassName).append(") null, ");
                            if (kind == ConstructorKind.CREATE) {
                                sb.append("$2);\n");
                            } else {
                                sb.append("java.util.Collections.emptyMap());\n");
                            }
                        } else if (kind == ConstructorKind.CREATE && methodAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                            sb.append("(").append(methodAttribute.getJavaType().getName());
                            sb.append(") $2.get(\"").append(((MappingAttribute<?, ?>) methodAttribute).getMapping()).append("\");\n");
                        } else {
                            sb.append("null;\n");
                        }
                    }
                    if (possiblyInitialized) {
                        if (mutableStateField != null) {
                            sb.append("\t} else {\n");
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                            renderValueForArray(sb, type, "$0." + attributeFields[i].getName());
                            sb.append(";\n");

                            if (initialStateField != null && kind != ConstructorKind.REFERENCE) {
                                sb.append("\t\tinitialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                                sb.append(getDefaultValueForObject(type)).append(";\n");
                            }
                        }
                        sb.append("\t}\n");
                    }
                } else {
                    // For create constructors we initialize embedded ids
                    if (kind == ConstructorKind.CREATE) {
                        if (methodAttribute instanceof PluralAttribute<?, ?, ?>) {
                            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) methodAttribute;

                            switch (pluralAttribute.getCollectionType()) {
                                case MAP:
                                    if (pluralAttribute.isSorted()) {
                                        sb.append("new java.util.TreeMap(");
                                        if (pluralAttribute.getComparatorClass() != null) {
                                            sb.append("new ").append(pluralAttribute.getComparatorClass().getName()).append("()");
                                        }
                                        sb.append(")");
                                    } else if (pluralAttribute.isOrdered()) {
                                        sb.append("new java.util.LinkedHashMap()");
                                    } else {
                                        sb.append("new java.util.HashMap()");
                                    }
                                    break;
                                case SET:
                                    if (pluralAttribute.isSorted()) {
                                        sb.append("new java.util.TreeSet(");
                                        if (pluralAttribute.getComparatorClass() != null) {
                                            sb.append("new ").append(pluralAttribute.getComparatorClass().getName()).append("()");
                                        }
                                        sb.append(")");
                                    } else if (pluralAttribute.isOrdered()) {
                                        sb.append("new java.util.LinkedHashSet()");
                                    } else {
                                        sb.append("new java.util.HashSet()");
                                    }
                                    break;
                                case LIST:
                                    sb.append("new java.util.ArrayList()");
                                    break;
                                default:
                                    sb.append("new java.util.ArrayList()");
                                    break;
                            }
                            sb.append(";\n");
                        } else {
                            SingularAttribute<?, ?> singularAttribute;
                            MethodAttribute<?, ?> idAttribute;
                            if (attributeFields[i] == idField && (idAttribute = ((ViewType<?>) managedViewType).getIdAttribute()).isSubview()) {
                                singularAttribute = (SingularAttribute<?, ?>) idAttribute;
                            } else {
                                singularAttribute = (SingularAttribute<?, ?>) methodAttribute;
                            }
                            if (singularAttribute != null && singularAttribute.isCreateEmptyFlatView()) {
                                ManagedViewTypeImplementor<Object> attributeManagedViewType = (ManagedViewTypeImplementor<Object>) singularAttribute.getType();
                                String proxyClassName = getProxy(entityViewManager, attributeManagedViewType).getName();
                                sb.append("new ");
                                sb.append(proxyClassName);
                                sb.append("((").append(proxyClassName).append(") null, $2);\n");
                            } else if (methodAttribute != null && methodAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                                sb.append("(").append(methodAttribute.getJavaType().getName());
                                sb.append(") $2.get(\"").append(((MappingAttribute<?, ?>) methodAttribute).getMapping()).append("\");\n");
                            } else {
                                sb.append("null;\n");
                            }
                        }
                    } else {
                        sb.append("null;\n");
                    }
                    if (possiblyInitialized) {
                        sb.append("\t}\n");
                    }
                }
            } else {
                sb.append('$').append(i + 1).append(";\n");
                if (methodAttribute != null && methodAttribute.hasDirtyStateIndex()) {
                    CtClass type = attributeFields[i].getType();
                    if (mutableStateField != null) {
                        if (possiblyInitialized) {
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");

                            if (initialStateField != null) {
                                sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                            }

                            renderValueForArray(sb, type, "$" + (i + 1));
                            sb.append("\t} else {\n");
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                            renderValueForArray(sb, type, "$0." + attributeFields[i].getName());
                            sb.append(";\n");
                            sb.append("\t}\n\t");
                        } else {
                            sb.append("\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }
                        if (initialStateField != null) {
                            sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }

                        renderValueForArray(sb, type, "$" + (i + 1));
                    }
                } else if (possiblyInitialized) {
                    sb.append("\t}\n");
                }
            }

        }

        if (initialStateField != null) {
            sb.append("\t$0.").append(initialStateField.getName()).append(" = initialStateArr;\n");
        }
        if (mutableStateField != null) {
            sb.append("\t$0.").append(mutableStateField.getName()).append(" = mutableStateArr;\n");
        }
    }

    private void renderFieldInitialization(MappingConstructor<?> constructor, CtField[] attributeFields, CtField initialStateField, CtField mutableStateField, AbstractMethodAttribute<?, ?>[] methodAttributes, int mutableAttributeCount, boolean assignment, long alwaysDirtyMask, boolean unsafe, StringBuilder sb) throws NotFoundException {
        if (initialStateField != null) {
            sb.append("\tObject[] initialStateArr = new Object[").append(mutableAttributeCount).append("];\n");
        }

        if (mutableStateField != null) {
            sb.append("\tObject[] mutableStateArr = new Object[").append(mutableAttributeCount).append("];\n");
            if (unsafe) {
                sb.append("\t$0.$$_dirty = ").append(alwaysDirtyMask).append("L;\n");
            } else {
                sb.append("\t$0.$$_dirty |= ").append(alwaysDirtyMask).append("L;\n");
            }
        }

        for (int i = 0; i < attributeFields.length; i++) {
            if (attributeFields[i] == null) {
                continue;
            }

            AbstractMethodAttribute<?, ?> methodAttribute = methodAttributes[i];

            if (methodAttribute != null) {
                boolean possiblyInitialized = !unsafe && appendPossiblyInitialized(sb, constructor, methodAttribute, attributeFields[i]);
                if (possiblyInitialized) {
                    sb.append('\t');
                }
                if (methodAttribute.getConvertedJavaType().isPrimitive()) {
                    sb.append("\t$0.").append(attributeFields[i].getName()).append(" = ");
                    if (assignment) {
                        appendUnwrap(sb, methodAttribute.getConvertedJavaType(),"$4[$2 + $3[" + methodAttribute.getAttributeIndex() + "]]");
                    } else {
                        appendUnwrap(sb, methodAttribute.getConvertedJavaType(),"$3[$2 + " + methodAttribute.getAttributeIndex() + "]");
                    }
                    sb.append(";\n");
                } else {
                    sb.append("\t$0.").append(attributeFields[i].getName()).append(" = (").append(attributeFields[i].getType().getName());
                    if (assignment) {
                        sb.append(") $4[$2 + $3[").append(methodAttribute.getAttributeIndex()).append("]");
                    } else {
                        sb.append(") $3[$2 + ").append(methodAttribute.getAttributeIndex());
                    }
                    sb.append("];\n");
                }

                if (methodAttribute.hasDirtyStateIndex()) {
                    if (possiblyInitialized) {
                        if (mutableStateField != null) {
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");

                            if (assignment) {
                                sb.append("$4[$2 + $3[").append(methodAttribute.getAttributeIndex()).append("]");
                            } else {
                                sb.append("$3[$2 + ").append(methodAttribute.getAttributeIndex());
                            }
                            sb.append("];\n");
                            sb.append("\t} else {\n");
                            sb.append("\t\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = $0.").append(attributeFields[i].getName()).append(";\n");
                            sb.append("\t}\n");
                        }

                        if (initialStateField != null) {
                            sb.append("\tinitialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }

                        if (assignment) {
                            sb.append("$4[$2 + $3[").append(methodAttribute.getAttributeIndex()).append("]");
                        } else {
                            sb.append("$3[$2 + ").append(methodAttribute.getAttributeIndex());
                        }
                        sb.append("];\n");
                    } else {
                        if (mutableStateField != null) {
                            sb.append("\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }

                        if (initialStateField != null) {
                            sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        }

                        if (assignment) {
                            sb.append("$4[$2 + $3[").append(methodAttribute.getAttributeIndex()).append("]");
                        } else {
                            sb.append("$3[$2 + ").append(methodAttribute.getAttributeIndex());
                        }
                        sb.append("];\n");
                    }
                } else if (possiblyInitialized) {
                    sb.append("\t}\n");
                }
            }
        }

        if (initialStateField != null) {
            sb.append("\t$0.").append(initialStateField.getName()).append(" = initialStateArr;\n");
        }
        if (mutableStateField != null) {
            sb.append("\t$0.").append(mutableStateField.getName()).append(" = mutableStateArr;\n");
        }
    }

    private boolean appendPossiblyInitialized(StringBuilder sb, MappingConstructor<?> constructor, AbstractMethodAttribute<?, ?> methodAttribute, CtField attributeField) throws NotFoundException {
        if (constructor == null) {
            for (MappingConstructor<?> viewConstructor : methodAttribute.getDeclaringType().getConstructors()) {
                if (viewConstructor.getParameterAttributes().isEmpty()) {
                    constructor = viewConstructor;
                    break;
                }
            }
        }
        boolean hasRealConstructor = constructor != null || !methodAttribute.getDeclaringType().getJavaType().isInterface();
        boolean possiblyInitialized = hasRealConstructor && !Modifier.isFinal(attributeField.getModifiers());
        if (possiblyInitialized) {
            sb.append("\tif ($0.").append(attributeField.getName()).append(" == ");
            if (methodAttribute.getConvertedJavaType().isPrimitive()) {
                sb.append(getDefaultValue(attributeField.getType()));
            } else {
                sb.append("null");
            }
            sb.append(") {\n");
        }
        return possiblyInitialized;
    }

    private String getDefaultValue(CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                return "0L";
            } else if (type == CtClass.floatType) {
                return "0F";
            } else if (type == CtClass.doubleType) {
                return "0D";
            } else if (type == CtClass.charType) {
                return "'\\u0000'";
            } else if (type == CtClass.booleanType) {
                return "false";
            } else {
                return "0";
            }
        } else {
            return "(" + type.getName() + ") null";
        }
    }

    private String getDefaultValueForObject(CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                return "Long.valueOf(0L)";
            } else if (type == CtClass.floatType) {
                return "Float.valueOf(0F)";
            } else if (type == CtClass.doubleType) {
                return "Double.valueOf(0D)";
            } else if (type == CtClass.shortType) {
                return "Short.valueOf((short) 0)";
            } else if (type == CtClass.byteType) {
                return "Byte.valueOf((byte) 0)";
            } else if (type == CtClass.charType) {
                return "Character.valueOf('\\u0000')";
            } else if (type == CtClass.booleanType) {
                return "Boolean#FALSE";
            } else {
                return "Integer.valueOf(0)";
            }
        } else {
            return "(" + type.getName() + ") null";
        }
    }

    private void renderValueForArray(StringBuilder sb, CtClass type, String argument) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                sb.append("Long.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.floatType) {
                sb.append("Float.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.doubleType) {
                sb.append("Double.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.shortType) {
                sb.append("Short.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.byteType) {
                sb.append("Byte.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.booleanType) {
                sb.append("Boolean.valueOf(").append(argument).append(");\n");
            } else if (type == CtClass.charType) {
                sb.append("Character.valueOf(").append(argument).append(");\n");
            } else {
                sb.append("Integer.valueOf(").append(argument).append(");\n");
            }
        } else {
            sb.append(argument).append(";\n");
        }
    }

    private void renderDirtyTrackerRegistration(CtField[] attributeFields, CtField mutableStateField, AbstractMethodAttribute<?, ?>[] attributes, ConstructorKind kind, StringBuilder sb) throws NotFoundException, CannotCompileException {
        if (mutableStateField != null) {
            sb.append("\t$0.$$_initialized = true;\n");
        }
        for (int i = 0; i < attributeFields.length; i++) {
            if (attributeFields[i] == null) {
                continue;
            }

            AbstractMethodAttribute<?, ?> methodAttribute = attributes[i];
            if (methodAttribute != null && methodAttribute.hasDirtyStateIndex()) {
                if (!methodAttribute.getConvertedJavaType().isPrimitive() && mutableStateField != null && (methodAttribute.isCollection() || methodAttribute.isSubview())) {
                    sb.append("\tif ($0.").append(attributeFields[i].getName()).append(" != null) {\n");

                    // $(i + 1).setParent(this, attributeIndex)
                    if (methodAttribute.isCollection()) {
                        // Collections must be "mutable" for a recording implementation to be used
                        if (methodAttribute.getDirtyStateIndex() != -1) {
                            if (methodAttribute instanceof MapAttribute<?, ?, ?>) {
                                sb.append("\t\t((").append(RecordingMap.class.getName()).append(") $0.").append(attributeFields[i].getName()).append(").$$_setParent($0, ").append(methodAttribute.getDirtyStateIndex()).append(");\n");
                            } else {
                                sb.append("\t\t((").append(RecordingCollection.class.getName()).append(") $0.").append(attributeFields[i].getName()).append(").$$_setParent($0, ").append(methodAttribute.getDirtyStateIndex()).append(");\n");
                            }
                        }
                    } else if (methodAttribute.isSubview()) {
                        sb.append("\t\tif ($0.").append(attributeFields[i].getName()).append(" instanceof ").append(DirtyTracker.class.getName()).append(") {\n");
                        sb.append("\t\t\t((").append(DirtyTracker.class.getName()).append(") $0.").append(attributeFields[i].getName()).append(").$$_setParent($0, ").append(methodAttribute.getDirtyStateIndex()).append(");\n");
                        sb.append("\t\t}\n");
                    }

                    sb.append("\t}\n");
                }
            }
        }
    }

    private String addAllowedSubtypeField(CtClass declaringClass, AbstractMethodAttribute<?, ?> attribute) throws CannotCompileException {
        return addClassSetField(declaringClass, "subtypes", attribute, attribute.getAllowedSubtypes());
    }

    private String addParentRequiringUpdateSubtypesField(CtClass declaringClass, AbstractMethodAttribute<?, ?> attribute) throws CannotCompileException {
        return addClassSetField(declaringClass, "parentRequiringUpdateSubtypes", attribute, attribute.getParentRequiringUpdateSubtypes());
    }

    private String addParentRequiringCreateSubtypesField(CtClass declaringClass, AbstractMethodAttribute<?, ?> attribute) throws CannotCompileException {
        return addClassSetField(declaringClass, "parentRequiringCreateSubtypes", attribute, attribute.getParentRequiringUpdateSubtypes());
    }

    private String addClassSetField(CtClass declaringClass, String fieldSuffix, AbstractMethodAttribute<?, ?> attribute, Set<Class<?>> classes) throws CannotCompileException {
        String subtypeArray;
        if (!classes.isEmpty()) {
            StringBuilder subtypeArrayBuilder = new StringBuilder();
            for (Class<?> c : classes) {
                subtypeArrayBuilder.append(c.getName());
                subtypeArrayBuilder.append(", ");
            }

            subtypeArrayBuilder.setLength(subtypeArrayBuilder.length() - 2);
            subtypeArray = subtypeArrayBuilder.toString();
        } else {
            subtypeArray = "";
        }

        try {
            declaringClass.getDeclaredField(attribute.getName() + "_$$_" + fieldSuffix);
            return subtypeArray;
        } catch (NotFoundException ex) {
        }

        StringBuilder fieldSb = new StringBuilder();
        fieldSb.append("private static final java.util.Set ");
        fieldSb.append(attribute.getName());
        fieldSb.append("_$$_").append(fieldSuffix).append(" = ");

        if (!classes.isEmpty()) {
            fieldSb.append("new java.util.HashSet(java.util.Arrays.asList(new java.lang.Class[]{ ");
            for (Class<?> c : classes) {
                fieldSb.append(c.getName());
                fieldSb.append(".class, ");
            }

            fieldSb.setLength(fieldSb.length() - 2);
            fieldSb.append(" }));");
        } else {
            fieldSb.append("java.util.Collections.emptySet();");
        }
        CtField allowedSubtypesField = CtField.make(fieldSb.toString(), declaringClass);
        declaringClass.addField(allowedSubtypesField);
        return subtypeArray;
    }

    private String addEmptyObjectArray(CtClass declaringClass) throws NotFoundException, CannotCompileException {
        String name = "$$_empty_object_array";
        CtField[] fields = declaringClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (name.equals(fields[i].getName())) {
                return declaringClass.getName() + "#" + name;
            }
        }

        CtField f = CtField.make("private static final Object[] " + name + " = new Object[0];", declaringClass);
        declaringClass.addField(f);
        return declaringClass.getName() + "#" + name;
    }

    private String addEmptyClassArray(CtClass declaringClass) throws NotFoundException, CannotCompileException {
        String name = "$$_empty_class_array";
        CtField[] fields = declaringClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (name.equals(fields[i].getName())) {
                return declaringClass.getName() + "#" + name;
            }
        }

        CtField f = CtField.make("private static final Class[] " + name + " = new Class[0];", declaringClass);
        declaringClass.addField(f);
        return declaringClass.getName() + "#" + name;
    }

    private String addMakeFieldAccessible(CtClass declaringClass) throws NotFoundException, CannotCompileException {
        String name = "$$_make_field_accessible";
        CtMethod[] methods = declaringClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (name.equals(methods[i].getName())) {
                return declaringClass.getName() + "#" + name;
            }
        }

        String desc = "(" + Descriptor.of(Field.class.getName()) + ")" + Descriptor.of(Field.class.getName());

        ConstPool cp = declaringClass.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, name, desc);
        minfo.setAccessFlags(AccessFlag.PRIVATE | AccessFlag.STATIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t$1.setAccessible(true);\n");
        sb.append("\treturn $1;\n");
        sb.append("}");

        CtMethod method = CtMethod.make(minfo, declaringClass);
        method.setBody(sb.toString());
        declaringClass.addMethod(method);

        return declaringClass.getName() + "#" + name;
    }

    private String addMakeMethodAccessible(CtClass declaringClass) throws NotFoundException, CannotCompileException {
        String name = "$$_make_method_accessible";
        CtMethod[] methods = declaringClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (name.equals(methods[i].getName())) {
                return declaringClass.getName() + "#" + name;
            }
        }

        String desc = "(" + Descriptor.of(Method.class.getName()) + ")" + Descriptor.of(Method.class.getName());

        ConstPool cp = declaringClass.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, name, desc);
        minfo.setAccessFlags(AccessFlag.PRIVATE | AccessFlag.STATIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t$1.setAccessible(true);\n");
        sb.append("\treturn $1;\n");
        sb.append("}");

        CtMethod method = CtMethod.make(minfo, declaringClass);
        method.setBody(sb.toString());
        declaringClass.addMethod(method);

        return declaringClass.getName() + "#" + name;
    }

    private String addIdAccessor(CtClass declaringClass, IdentifiableType<?> type, javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute, CtClass idType) throws NotFoundException, CannotCompileException {
        String name = "$$_" + ((EntityType<?>) type).getName() + "_" + jpaIdAttribute.getName();
        CtMethod[] methods = declaringClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (name.equals(methods[i].getName())) {
                return declaringClass.getName() + "#" + name;
            }
        }

        ClassPool classPool = declaringClass.getClassPool();
        CtClass boxedType = autoBox(classPool, idType);

        String desc = "(" + Descriptor.of(type.getJavaType().getName()) + ")" + Descriptor.of(boxedType);

        ConstPool cp = declaringClass.getClassFile().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, name, desc);
        minfo.setAccessFlags(AccessFlag.PRIVATE | AccessFlag.STATIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        String fieldName = name + "_accessor";
        if (jpaIdAttribute.getJavaMember() instanceof Field) {
            StringBuilder fieldSb = new StringBuilder();
            fieldSb.append(addMakeFieldAccessible(declaringClass));
            fieldSb.append('(');
            fieldSb.append(jpaIdAttribute.getJavaMember().getDeclaringClass());
            fieldSb.append(".class.getDeclaredField(\"");
            fieldSb.append(jpaIdAttribute.getJavaMember().getName());
            fieldSb.append("\"));");

            CtField field = new CtField(classPool.get("java.lang.reflect.Field"), fieldName, declaringClass);
            field.getFieldInfo().setAccessFlags(AccessFlag.PRIVATE | AccessFlag.STATIC | AccessFlag.FINAL);
            declaringClass.addField(field, CtField.Initializer.byExpr(fieldSb.toString()));

            sb.append("\treturn ");
            sb.append('(');
            sb.append(boxedType.getName());
            sb.append(") ");
            sb.append(declaringClass.getName());
            sb.append('#');
            sb.append(fieldName).append(".get((Object) $1);\n");
        } else {
            StringBuilder fieldSb = new StringBuilder();
            fieldSb.append(addMakeMethodAccessible(declaringClass));
            fieldSb.append('(');
            fieldSb.append(jpaIdAttribute.getJavaMember().getDeclaringClass().getName());
            fieldSb.append(".class.getDeclaredMethod(\"");
            fieldSb.append(jpaIdAttribute.getJavaMember().getName());
            fieldSb.append("\", ");
            fieldSb.append(addEmptyClassArray(declaringClass));
            fieldSb.append("));");

            CtField field = new CtField(classPool.get("java.lang.reflect.Method"), fieldName, declaringClass);
            field.getFieldInfo().setAccessFlags(AccessFlag.PRIVATE | AccessFlag.STATIC | AccessFlag.FINAL);
            declaringClass.addField(field, CtField.Initializer.byExpr(fieldSb.toString()));

            sb.append("\treturn ");
            sb.append('(');
            sb.append(boxedType.getName());
            sb.append(") ");
            sb.append(declaringClass.getName());
            sb.append('#');
            String emptyObjectArrayField = addEmptyObjectArray(declaringClass);
            sb.append(fieldName).append(".invoke((Object) $1, ").append(emptyObjectArrayField);
            sb.append(");\n");
        }

        sb.append('}');

        CtMethod method = CtMethod.make(minfo, declaringClass);
        method.setBody(sb.toString());
        declaringClass.addMethod(method);
        return declaringClass.getName() + "#" + name;
    }

    private CtClass autoBox(ClassPool classPool, CtClass fieldType) {
        if (fieldType.isPrimitive()) {
            String typeName = fieldType.getName();
            Class<?> type;
            try {
                type = ReflectionUtils.getWrapperClassOfPrimitve(ReflectionUtils.getClass(typeName));
                return classPool.get(type.getName());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unsupported primitive type: " + typeName, ex);
            }
        }
        return fieldType;
    }

    private void autoBox(Bytecode code, ClassPool classPool, CtClass fieldType) {
        if (fieldType.isPrimitive()) {
            String typeName = fieldType.getName();
            Class<?> type;
            try {
                type = ReflectionUtils.getWrapperClassOfPrimitve(ReflectionUtils.getClass(typeName));
                CtClass wrapperType = classPool.get(type.getName());
                code.addInvokestatic(type.getName(), "valueOf", Descriptor.ofMethod(wrapperType, new CtClass[]{ fieldType }));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unsupported primitive type: " + typeName, ex);
            }
        }
    }

    private boolean needsTwoStackSlots(CtClass c) {
        return needsTwoStackSlots(c.getName());
    }

    private boolean needsTwoStackSlots(String name) {
        // Primitive long and double need two stack slots
        return "long".equals(name) || "double".equals(name);
    }

    private void renderSuperCall(MappingConstructor<?> constructor, CtClass cc, int superStart, int superEnd, StringBuilder sb) throws NotFoundException {
        sb.append("\tsuper(");
        if (superStart < superEnd) {
            List<ParameterAttribute<?, ?>> parameterAttributes = (List<ParameterAttribute<?, ?>>) constructor.getParameterAttributes();
            for (int i = superStart; i < superEnd; i++) {
                if (parameterAttributes.get(i - superStart).isSelfParameter()) {
                    sb.append("createSelf(");
                    for (int j = 0; j < superStart; j++) {
                        sb.append('$').append(j + 1).append(',');
                    }
                    sb.setCharAt(sb.length() - 1, ')');
                    sb.append(",");
                } else {
                    sb.append('$').append(i + 1).append(',');
                }
            }
            sb.setCharAt(sb.length() - 1, ')');
        } else {
            sb.append(')');
        }
        sb.append(";\n");
    }

    private void renderTupleSuperCall(MappingConstructor<?> constructor, CtClass cc, int superStart, int superEnd, boolean assignment, CtClass[] attributeTypes, StringBuilder sb) throws NotFoundException {
        sb.append("\tsuper(");
        if (superStart < superEnd) {
            List<ParameterAttribute<?, ?>> parameterAttributes = (List<ParameterAttribute<?, ?>>) constructor.getParameterAttributes();
            for (int i = superStart; i < superEnd; i++) {
                if (parameterAttributes.get(i - superStart).isSelfParameter()) {
                    sb.append("createSelf(");
                    for (int j = 0; j < superStart; j++) {
                        if (attributeTypes[j].isPrimitive()) {
                            if (assignment) {
                                appendUnwrap(sb, attributeTypes[j], "$4[$2 + $3[" + j + "]]");
                            } else {
                                appendUnwrap(sb, attributeTypes[j], "$3[$2 + " + j + "]");
                            }

                            sb.append(",");
                        } else {
                            sb.append("(").append(attributeTypes[j].getName());
                            if (assignment) {
                                sb.append(") $4[$2 + $3[").append(j).append("]");
                            } else {
                                sb.append(") $3[$2 + ").append(j);
                            }
                            sb.append("],");
                        }
                    }
                    sb.setCharAt(sb.length() - 1, ')');
                    sb.append(",");
                } else {
                    if (attributeTypes[i].isPrimitive()) {
                        if (assignment) {
                            appendUnwrap(sb, attributeTypes[i], "$4[$2 + $3[" + i + "]]");
                        } else {
                            appendUnwrap(sb, attributeTypes[i], "$3[$2 + " + i + "]");
                        }

                        sb.append(",");
                    } else {
                        sb.append("(").append(attributeTypes[i].getName());
                        if (assignment) {
                            sb.append(") $4[$2 + $3[").append(i).append("]");
                        } else {
                            sb.append(") $3[$2 + ").append(i);
                        }
                        sb.append("],");
                    }
                }
            }
            sb.setCharAt(sb.length() - 1, ')');
        } else {
            sb.append(')');
        }
        sb.append(";\n");
    }
    
    private <T> CtConstructor findConstructor(CtClass superCc, MappingConstructor<T> constructor) throws NotFoundException {
        List<ParameterAttribute<? super T, ?>> parameterAttributes = constructor.getParameterAttributes();
        CtClass[] parameterTypes = new CtClass[parameterAttributes.size()];

        for (int i = 0; i < parameterAttributes.size(); i++) {
            parameterTypes[i] = getType(parameterAttributes.get(i));
        }

        return superCc.getDeclaredConstructor(parameterTypes);
    }

    private CtClass getType(Attribute<?, ?> attribute) throws NotFoundException {
        return pool.get(attribute.getConvertedJavaType().getName());
    }

    private int getModifiers(boolean hasSetter) {
        if (hasSetter) {
            return Modifier.PRIVATE;
        } else {
            return Modifier.PRIVATE | Modifier.FINAL;
        }
    }

    private String getGenericSignature(MethodAttribute<?, ?> attribute, CtField attributeField) throws NotFoundException {
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(attribute.getDeclaringType().getJavaType(), attribute.getJavaMethod());
        if (typeArguments.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(typeArguments.length * 10);

        String simpleType = Descriptor.of(attributeField.getType());
        sb.append(simpleType, 0, simpleType.length() - 1);
        sb.append('<');

        for (int i = 0; i < typeArguments.length; i++) {
            if (typeArguments[i] == null) {
                throw new IllegalArgumentException("The type argument can not be resolved at index '" + i + "' for the attribute '" + attribute.getName() + "' of the class '" + attribute.getDeclaringType().getJavaType().getName() + "'!");
            }
            
            sb.append(Descriptor.of(typeArguments[i].getName()));
        }

        sb.append('>');
        sb.append(';');

        return sb.toString();
    }
}
