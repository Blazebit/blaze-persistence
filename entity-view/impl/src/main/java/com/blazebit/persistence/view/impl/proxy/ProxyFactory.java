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

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.impl.CorrelationProviderProxyBase;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.collection.RecordingNavigableMap;
import com.blazebit.persistence.view.impl.collection.RecordingNavigableSet;
import com.blazebit.persistence.view.impl.collection.RecordingSet;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractParameterAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ConstrainedAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.reflection.ReflectionUtils;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ProxyFactory {

    private static final Logger LOG = Logger.getLogger(ProxyFactory.class.getName());
    // This has to be static since runtime generated correlation providers can't be matched in a later run, so we always create a new one with a unique name
    private static final ConcurrentMap<Class<?>, AtomicInteger> CORRELATION_PROVIDER_CLASS_COUNT = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProxyClassKey, Class<?>> proxyClasses = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProxyClassKey, Class<?>> unsafeProxyClasses = new ConcurrentHashMap<>();
    private final Object proxyLock = new Object();
    private final ClassPool pool;
    private final boolean unsafeDisabled;
    private final PackageOpener packageOpener;

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static final class ProxyClassKey {
        private final Class<?> viewTypeClass;
        private final Class<?> inheritanceBaseClass;

        public ProxyClassKey(Class<?> viewTypeClass, Class<?> inheritanceBaseClass) {
            this.viewTypeClass = viewTypeClass;
            this.inheritanceBaseClass = inheritanceBaseClass;
        }

        @Override
        public boolean equals(Object o) {
            ProxyClassKey that = (ProxyClassKey) o;

            if (!viewTypeClass.equals(that.viewTypeClass)) {
                return false;
            }
            return inheritanceBaseClass != null ? inheritanceBaseClass.equals(that.inheritanceBaseClass) : that.inheritanceBaseClass == null;
        }

        @Override
        public int hashCode() {
            int result = viewTypeClass.hashCode();
            result = 31 * result + (inheritanceBaseClass != null ? inheritanceBaseClass.hashCode() : 0);
            return result;
        }
    }

    public ProxyFactory(boolean unsafeDisabled, PackageOpener packageOpener) {
        this.pool = new ClassPool(ClassPool.getDefault());
        this.unsafeDisabled = unsafeDisabled;
        this.packageOpener = packageOpener;
    }

    public <T> Class<? extends T> getProxy(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> viewType, ManagedViewTypeImplementor<? super T> inheritanceBase) {
        if (viewType.getConstructors().isEmpty() || unsafeDisabled) {
            return getProxy(entityViewManager, viewType, inheritanceBase, false);
        } else {
            return getProxy(entityViewManager, viewType, inheritanceBase, true);
        }
    }

    public Class<? extends CorrelationProvider> getCorrelationProviderProxy(Class<?> correlated, String correlationKeyAlias, String correlationExpression) {
        AtomicInteger counter = CORRELATION_PROVIDER_CLASS_COUNT.get(correlated);
        if (counter == null) {
            counter = new AtomicInteger(0);
            AtomicInteger oldCounter = CORRELATION_PROVIDER_CLASS_COUNT.putIfAbsent(correlated, counter);
            if (oldCounter != null) {
                counter = oldCounter;
            }
        }
        int value = counter.getAndIncrement();
        String proxyClassName = correlated.getName() + "CorrelationProvider_$$_javassist_entityview_" + value;

        ClassPath classPath = new ClassClassPath(CorrelationProviderProxyBase.class);
        pool.insertClassPath(classPath);

        try {
            CtClass cc = pool.getAndRename(CorrelationProviderProxyBase.class.getName(), proxyClassName);

            // We only have one other constructor
            CtConstructor otherConstructor = cc.getDeclaredConstructors()[0];

            // Create a new constructor
            CtClass[] emptyParamTypes = new CtClass[0];
            CtConstructor newConstructor = new CtConstructor(emptyParamTypes, cc);
            newConstructor.setModifiers(Modifier.PUBLIC);
            ConstPool constPool = cc.getClassFile().getConstPool();
            Bytecode bytecode = new Bytecode(constPool, 4, 1);

            // Add the correlation params as constants to the constant pool
            int correlatedIndex = constPool.addClassInfo(correlated.getName());
            int correlationKeyAliasIndex = constPool.addStringInfo(correlationKeyAlias);
            int correlationExpressionIndex = constPool.addStringInfo(correlationExpression);

            // Invocation target
            bytecode.addAload(0);

            // In the byte code load the constants
            bytecode.addLdc(correlatedIndex);
            bytecode.addLdc(correlationKeyAliasIndex);
            bytecode.addLdc(correlationExpressionIndex);

            // Invoke the parameterized constructor
            bytecode.addInvokespecial(cc, "<init>", Descriptor.ofConstructor(otherConstructor.getParameterTypes()));

            bytecode.add(Bytecode.RETURN);
            newConstructor.getMethodInfo().setCodeAttribute(bytecode.toCodeAttribute());
            cc.addConstructor(newConstructor);

            return (Class<? extends CorrelationProvider>) cc.toClass(correlated.getClassLoader(), null);
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> getProxy(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> viewType, ManagedViewTypeImplementor<? super T> inheritanceBase, boolean unsafe) {
        Class<T> clazz = viewType.getJavaType();
        Class<? super T> baseClazz;

        if (inheritanceBase == null) {
            baseClazz = null;
        } else {
            baseClazz = inheritanceBase.getJavaType();
        }

        final ConcurrentMap<ProxyClassKey, Class<?>> classes = unsafe ? unsafeProxyClasses : proxyClasses;
        final ProxyClassKey key = new ProxyClassKey(clazz, baseClazz);
        Class<? extends T> proxyClass = (Class<? extends T>) classes.get(key);

        // Double checked locking since we can only define the class once
        if (proxyClass == null) {
            synchronized (proxyLock) {
                proxyClass = (Class<? extends T>) classes.get(key);
                if (proxyClass == null) {
                    proxyClass = createProxyClass(entityViewManager, viewType, inheritanceBase, unsafe);
                    classes.put(key, proxyClass);
                }
            }
        }

        return proxyClass;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> createProxyClass(EntityViewManager entityViewManager, ManagedViewTypeImplementor<T> managedViewType, ManagedViewTypeImplementor<? super T> inheritanceBase, boolean unsafe) {
        ViewType<T> viewType = managedViewType instanceof ViewType<?> ? (ViewType<T>) managedViewType : null;
        Class<?> clazz = managedViewType.getJavaType();
        String suffix = unsafe ? "unsafe_" : "";
        String baseName;
        int subtypeIndex = 0;

        if (inheritanceBase == null) {
            baseName = clazz.getName();
        } else {
            subtypeIndex = managedViewType.getSubtypeIndex(inheritanceBase);
            baseName = inheritanceBase.getJavaType().getName();
            baseName += "_" + managedViewType.getJavaType().getSimpleName();
        }

        String proxyClassName = baseName + "_$$_javassist_entityview_" + suffix;
        CtClass cc = pool.makeClass(proxyClassName);
        CtClass superCc;

        ClassPath classPath = new ClassClassPath(clazz);
        pool.insertClassPath(classPath);

        try {
            superCc = pool.get(clazz.getName());

            if (clazz.isInterface()) {
                cc.addInterface(superCc);
            } else {
                cc.setSuperclass(superCc);
            }

            boolean dirtyChecking = false;
            CtField dirtyField = null;
            CtField parentField = null;
            CtField parentIndexField = null;
            CtField initialStateField = null;
            CtField mutableStateField = null;
            CtMethod markDirtyStub = null;
            cc.addInterface(pool.get(EntityViewProxy.class.getName()));
            addGetJpaManagedClass(cc, managedViewType.getEntityClass());
            addGetEntityViewClass(cc, clazz);
            addIsNewMembers(managedViewType, cc, clazz);

            CtField evmField = new CtField(pool.get(EntityViewManager.class.getName()), "$$_evm", cc);
            evmField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.VOLATILE);
            cc.addField(evmField);

            if (managedViewType.isUpdatable() || managedViewType.isCreatable()) {
                if (managedViewType.getFlushMode() == FlushMode.LAZY || managedViewType.getFlushMode() == FlushMode.PARTIAL) {
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

                parentField = new CtField(pool.get(DirtyTracker.class.getName()), "$$_parent", cc);
                parentField.setModifiers(getModifiers(true));
                cc.addField(parentField);
                parentIndexField = new CtField(CtClass.intType, "$$_parentIndex", cc);
                parentIndexField.setModifiers(getModifiers(true));
                cc.addField(parentIndexField);

                dirtyChecking = true;

                addGetter(cc, mutableStateField, "$$_getMutableState");
                addGetter(cc, parentField, "$$_getParent");
                addGetter(cc, parentIndexField, "$$_getParentIndex");
                addSetParent(cc, parentField, parentIndexField);
                addHasParent(cc, parentField);
                addUnsetParent(cc, parentField, parentIndexField);
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
            
            if (viewType != null) {
                idAttribute = (AbstractMethodAttribute<? super T, ?>) viewType.getIdAttribute();
                versionAttribute = (AbstractMethodAttribute<? super T, ?>) viewType.getVersionAttribute();
                idField = addMembersForAttribute(idAttribute, clazz, cc, null, false, true, mutableStateField != null);
                fieldMap.put(idAttribute.getName(), idField);
                attributeFields[0] = idField;
                attributeTypes[0] = idField.getType();
                attributes.remove(idAttribute);
                i = 1;

                if (mutableStateField != null) {
                    addSetId(cc, idField);
                }
            } else if (mutableStateField != null) {
                addSetId(cc, null);
            }

            addGetter(cc, idField, "$$_getId", Object.class);
            
            final AbstractMethodAttribute<?, ?>[] mutableAttributes = new AbstractMethodAttribute[attributeFields.length];
            int mutableAttributeCount = 0;
            for (MethodAttribute<?, ?> attribute : attributes) {
                AbstractMethodAttribute<?, ?> methodAttribute = (AbstractMethodAttribute<?, ?>) attribute;
                boolean forceMutable = mutableStateField != null && methodAttribute == versionAttribute;
                CtField attributeField = addMembersForAttribute(methodAttribute, clazz, cc, mutableStateField, dirtyChecking, false, forceMutable);
                fieldMap.put(attribute.getName(), attributeField);
                attributeFields[i] = attributeField;
                attributeTypes[i] = attributeField.getType();

                if (methodAttribute.getDirtyStateIndex() != -1) {
                    mutableAttributes[i] = methodAttribute;
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
                    for (int j = 0; j < mutableAttributes.length; j++) {
                        if (mutableAttributes[j] != null) {
                            if (supportsDirtyTracking(mutableAttributes[j])) {
                                supportsDirtyTracking[mutableAttributeIndex++] = true;
                            } else {
                                allSupportDirtyTracking = false;
                                supportsDirtyTracking[mutableAttributeIndex++] = false;
                            }
                        }
                    }

                    addIsDirty(cc, dirtyField, allSupportDirtyTracking);
                    addIsDirtyAttribute(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                    addMarkDirty(cc, dirtyField);
                    addUnmarkDirty(cc, dirtyField);
                    addSetDirty(cc, dirtyField);
                    addResetDirty(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                    addGetDirty(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                    addGetSimpleDirty(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                    addCopyDirty(cc, dirtyField, supportsDirtyTracking, allSupportDirtyTracking);
                }
            }

            createEqualsHashCodeMethods(viewType, clazz, cc, superCc, attributeFields, idField);
            createSpecialMethods(managedViewType, cc);

            Set<MappingConstructorImpl<T>> constructors = (Set<MappingConstructorImpl<T>>) (Set<?>) managedViewType.getConstructors();
            boolean hasEmptyConstructor = clazz.isInterface() || hasEmptyConstructor(constructors);

            if (hasEmptyConstructor) {
                // Create constructor for create models
                cc.addConstructor(createCreateConstructor(entityViewManager, managedViewType, cc, attributeFields, attributeTypes, idField, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, unsafe));
            }

            boolean addedReferenceConstructor = false;
            if (idField != null && hasEmptyConstructor) {
                // Id only constructor for reference models
                cc.addConstructor(createReferenceConstructor(entityViewManager, managedViewType, cc, attributeFields, idField, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, unsafe));
                addedReferenceConstructor = true;
            }

            if (inheritanceBase == null) {
                if (shouldAddDefaultConstructor(hasEmptyConstructor, addedReferenceConstructor, attributeFields)) {
                    cc.addConstructor(createNormalConstructor(entityViewManager, managedViewType, cc, attributeFields, attributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, unsafe));
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

                    cc.addConstructor(createNormalConstructor(entityViewManager, managedViewType, cc, attributeFields, constructorAttributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, unsafe));
                }
            } else {
                createInheritanceConstructors(entityViewManager, constructors, inheritanceBase, managedViewType, subtypeIndex, addedReferenceConstructor, unsafe, cc, initialStateField, mutableStateField, fieldMap, mutableAttributes, mutableAttributeCount);
            }

            return defineOrGetClass(entityViewManager, unsafe, clazz, proxyClassName, cc);
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }

    private <T> Class<? extends T> defineOrGetClass(EntityViewManager entityViewManager, boolean unsafe, Class<?> clazz, String proxyClassName, CtClass cc) throws IOException, IllegalAccessException, NoSuchFieldException, CannotCompileException {
        try {
            // Ask the package opener to allow deep access, otherwise defining the class will fail
            packageOpener.openPackageIfNeeded(clazz, clazz.getPackage().getName(), ProxyFactory.class);

            Class<? extends T> c;
            if (unsafe) {
                c = (Class<? extends T>) UnsafeHelper.define(cc.getName(), cc.toBytecode(), clazz);
            } else {
                c = (Class<? extends T>) cc.toClass(clazz.getClassLoader(), null);
            }

            c.getField("$$_evm").set(null, entityViewManager);

            return c;
        } catch (CannotCompileException | LinkageError ex) {
            // If there are multiple proxy factories for the same class loader
            // we could end up in defining a class multiple times, so we check if the classloader
            // actually has something to offer
            LinkageError error;
            if (ex instanceof LinkageError && (error = (LinkageError) ex) != null
                    || ex.getCause() instanceof LinkageError && (error = (LinkageError) ex.getCause()) != null) {
                try {
                    return (Class<? extends T>) pool.getClassLoader().loadClass(proxyClassName);
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
                return (Class<? extends T>) pool.getClassLoader().loadClass(proxyClassName);
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

    private <T> boolean hasEmptyConstructor(Set<MappingConstructorImpl<T>> constructors) {
        if (constructors.isEmpty()) {
            return true;
        }
        for (MappingConstructorImpl<?> c : constructors) {
            if (c.getParameterAttributes().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> void createInheritanceConstructors(EntityViewManager entityViewManager, Set<MappingConstructorImpl<T>> constructors, ManagedViewTypeImplementor<? super T> inheritanceBase, ManagedViewTypeImplementor<T> managedView, int subtypeIndex, boolean addedReferenceConstructor,
                                                   boolean unsafe, CtClass cc, CtField initialStateField, CtField mutableStateField, Map<String, CtField> fieldMap, AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount) throws NotFoundException, CannotCompileException, BadBytecode {
        Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>> overallAttributesClosure = (Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>>) (Map<?, ?>) inheritanceBase.getOverallInheritanceSubtypeConfiguration().getAttributesClosure();
        CtClass[] overallParameterTypes = new CtClass[overallAttributesClosure.size()];
        Map<String, CtClass[]> overallConstructorParameterTypes = new HashMap<>();
        boolean addedDefaultConstructor;
        {
            CtField[] fields = new CtField[overallAttributesClosure.size()];
            AbstractMethodAttribute<?, ?>[] subtypeMutableAttributes = new AbstractMethodAttribute<?, ?>[overallAttributesClosure.size()];
            int subtypeMutableAttributeCount = 0;

            // The id attribute always comes first
            String idName = null;
            int j = 0;
            if (inheritanceBase instanceof ViewType<?>) {
                idName = ((ViewType<?>) inheritanceBase).getIdAttribute().getName();
                CtField field = fieldMap.get(idName);
                fields[j] = field;
                overallParameterTypes[j] = field.getType();
                j++;
            }

            for (Map.Entry<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>> entry : overallAttributesClosure.entrySet()) {
                String attributeName = entry.getKey().getAttributeName();
                // Skip the id attribute that we handled before
                if (attributeName.equals(idName)) {
                    continue;
                }
                CtField field = fieldMap.get(attributeName);
                CtClass type;
                if (field == null) {
                    AbstractMethodAttribute<?, ?> attribute = entry.getValue().getAttribute();
                    type = getType(attribute);
                    if (attribute.getDirtyStateIndex() != -1) {
                        subtypeMutableAttributes[j] = attribute;
                        subtypeMutableAttributeCount++;
                    }
                } else {
                    type = field.getType();
                }
                fields[j] = field;
                overallParameterTypes[j] = type;
                j++;
            }

            boolean hasEmptyConstructor = managedView.getJavaType().isInterface() || hasEmptyConstructor(constructors);
            if (addedDefaultConstructor = shouldAddDefaultConstructor(hasEmptyConstructor, addedReferenceConstructor, fields)) {
                cc.addConstructor(createNormalConstructor(entityViewManager, managedView, cc, fields, overallParameterTypes, initialStateField, mutableStateField, subtypeMutableAttributes, subtypeMutableAttributeCount, unsafe));
            }

            for (MappingConstructorImpl<T> constructor : constructors) {
                // Skip the default constructor that is handled before
                if (constructor.getParameterAttributes().isEmpty()) {
                    continue;
                }
                MappingConstructorImpl<T> baseConstructor = (MappingConstructorImpl<T>) inheritanceBase.getConstructor(constructor.getName());
                MappingConstructorImpl.InheritanceSubtypeConstructorConfiguration<T> overallParameterAttributesClosureConfig = baseConstructor.getOverallInheritanceParametersAttributesClosureConfiguration();

                @SuppressWarnings("unchecked")
                List<AbstractParameterAttribute<? super T, ?>> parameterAttributes = overallParameterAttributesClosureConfig.getParameterAttributesClosure();
                // Copy default constructor parameters
                int constructorParameterCount = fields.length + parameterAttributes.size();
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(overallParameterTypes, 0, constructorAttributeTypes, 0, overallParameterTypes.length);

                // Find the start position in the parameters for passing through to this constructor
                int constructorParameterStartPosition = fields.length;
                int i = 0;
                for (ManagedViewType<?> subtype : inheritanceBase.getOverallInheritanceSubtypeConfiguration().getInheritanceSubtypes()) {
                    if (i == subtypeIndex) {
                        break;
                    }

                    constructorParameterStartPosition += subtype.getConstructor(constructor.getName()).getParameterAttributes().size();
                    i++;
                }

                // Copy constructor parameter types
                i = fields.length;
                for (AbstractParameterAttribute<?, ?> paramAttr : parameterAttributes) {
                    constructorAttributeTypes[i++] = getType(paramAttr);
                }

                overallConstructorParameterTypes.put(constructor.getName(), constructorAttributeTypes);
                int superConstructorParameterEndPosition = constructorParameterStartPosition + constructor.getParameterAttributes().size();
                cc.addConstructor(createConstructor(entityViewManager, managedView, cc, constructorParameterStartPosition, superConstructorParameterEndPosition, fields, constructorAttributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, ConstructorKind.NORMAL, null, unsafe));
            }
        }

        // Go through all configurations that contain this entity view and create a static inheritance factory for it
        Map<Map<ManagedViewTypeImplementor<?>, String>, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<? super T>> inheritanceSubtypeConfigurationMap = (Map<Map<ManagedViewTypeImplementor<?>, String>, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<? super T>>) (Map<?, ?>) inheritanceBase.getInheritanceSubtypeConfigurations();
        for (Map.Entry<Map<ManagedViewTypeImplementor<?>, String>, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<? super T>> configurationEntry : inheritanceSubtypeConfigurationMap.entrySet()) {
            if (!configurationEntry.getKey().containsKey(managedView)) {
                continue;
            }

            ManagedViewTypeImpl.InheritanceSubtypeConfiguration<? super T> inheritanceSubtypeConfiguration = configurationEntry.getValue();
            Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>> subtypeAttributesClosure = (Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>>) (Map<?, ?>) inheritanceSubtypeConfiguration.getAttributesClosure();
            CtClass[] parameterTypes = new CtClass[subtypeAttributesClosure.size()];

            // The id attribute always comes first
            String idName = null;
            int j = 0;
            if (inheritanceBase instanceof ViewType<?>) {
                idName = ((ViewType<?>) inheritanceBase).getIdAttribute().getName();
                CtField field = fieldMap.get(idName);
                parameterTypes[j] = field.getType();
                j++;
            }

            for (Map.Entry<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<?, ?>>> entry : subtypeAttributesClosure.entrySet()) {
                String attributeName = entry.getKey().getAttributeName();
                // Skip the id attribute that we handled before
                if (attributeName.equals(idName)) {
                    continue;
                }
                CtField field = fieldMap.get(attributeName);
                CtClass type;
                if (field != null) {
                    type = field.getType();
                } else {
                    AbstractMethodAttribute<?, ?> attribute = entry.getValue().getAttribute();
                    type = getType(attribute);
                }
                parameterTypes[j] = type;
                j++;
            }

            if (addedDefaultConstructor) {
                cc.addMethod(createStaticFactory(cc, inheritanceSubtypeConfiguration.getConfigurationIndex(), "", 0, inheritanceSubtypeConfiguration.getOverallPositionAssignment(managedView), parameterTypes, overallParameterTypes));
            }

            for (MappingConstructorImpl<T> constructor : constructors) {
                MappingConstructorImpl<T> baseConstructor = (MappingConstructorImpl<T>) inheritanceBase.getConstructor(constructor.getName());
                MappingConstructorImpl.InheritanceSubtypeConstructorConfiguration<T> subtypeConstructorConfiguration = baseConstructor.getSubtypeConstructorConfiguration((Map<ManagedViewTypeImplementor<? extends T>, String>) (Map<?, ?>) configurationEntry.getKey());
                @SuppressWarnings("unchecked")
                List<AbstractParameterAttribute<?, ?>> parameterAttributes = (List<AbstractParameterAttribute<?, ?>>) (List<?>) subtypeConstructorConfiguration.getParameterAttributesClosure();
                // Copy default constructor parameters
                int constructorParameterCount = parameterTypes.length + parameterAttributes.size();
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(parameterTypes, 0, constructorAttributeTypes, 0, parameterTypes.length);

                // Copy constructor parameter types
                int i = parameterTypes.length;
                for (AbstractParameterAttribute<?, ?> paramAttr : parameterAttributes) {
                    constructorAttributeTypes[i++] = getType(paramAttr);
                }

                cc.addMethod(createStaticFactory(cc, inheritanceSubtypeConfiguration.getConfigurationIndex(), "_" + constructor.getName(), parameterTypes.length, subtypeConstructorConfiguration.getOverallPositionAssignment(), constructorAttributeTypes, overallConstructorParameterTypes.get(constructor.getName())));
            }
        }
    }

    private CtMethod createStaticFactory(CtClass cc, int inheritanceConfigurationIndex, String suffix, int passThroughCount, int[] positionAssignments, CtClass[] parameterTypes, CtClass[] overallParameterTypes) throws CannotCompileException {
        String descriptor = Descriptor.ofMethod(cc, parameterTypes);
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "create" + inheritanceConfigurationIndex + suffix, descriptor);
        minfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
        CtMethod method = CtMethod.make(minfo, cc);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\treturn new ");
        sb.append(cc.getName()).append("(\n");
        for (int i = 0; i < passThroughCount; i++) {
            sb.append("\t\t");
            sb.append('$').append(positionAssignments[i] + 1);
            sb.append(",\n");
        }
        for (int i = 0; i < positionAssignments.length; i++) {
            sb.append("\t\t");
            if (positionAssignments[i] == -1) {
                sb.append(getDefaultValue(overallParameterTypes[i]));
            } else {
                sb.append('$').append(passThroughCount + positionAssignments[i] + 1);
            }
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\n\t);\n");
        sb.append("}");
        method.setBody(sb.toString());

        return method;
    }

    private <T> void createEqualsHashCodeMethods(ViewType<T> viewType, Class<?> entityViewClass, CtClass cc, CtClass superCc, CtField[] attributeFields, CtField idField) throws NotFoundException, CannotCompileException {
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
                cc.addMethod(createIdEquals(entityViewClass, cc));
                cc.addMethod(createHashCode(cc, idField));
            } else {
                cc.addMethod(createEquals(entityViewClass, cc, attributeFields));
                cc.addMethod(createHashCode(cc, attributeFields));
            }
        }
    }

    private void createSpecialMethods(ManagedViewTypeImplementor<?> viewType, CtClass cc) throws CannotCompileException {
        for (Method method : viewType.getSpecialMethods()) {
            if (method.getReturnType() == EntityViewManager.class) {
                addEntityViewManagerGetter(cc, method);
            } else {
                throw new IllegalArgumentException("Unsupported special method: " + method);
            }
        }
    }

    private void addEntityViewManagerGetter(CtClass cc, Method method) throws CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        String desc = Descriptor.of(method.getReturnType().getName());
        MethodInfo minfo = new MethodInfo(cp, method.getName(), "()" + desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);

        Bytecode code = new Bytecode(cp, 1, 1);
        code.addGetstatic(cc, "$$_evm", desc);
        code.addOpcode(Bytecode.ARETURN);

        minfo.setCodeAttribute(code.toCodeAttribute());
        cc.addMethod(CtMethod.make(minfo, cc));
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

    private void addIsNewMembers(ManagedViewType<?> managedViewType, CtClass cc, Class<?> clazz) throws CannotCompileException, NotFoundException {
        if (managedViewType.isCreatable()) {
            CtField isNewField = new CtField(CtClass.booleanType, "$$_isNew", cc);
            isNewField.setModifiers(getModifiers(true));
            cc.addField(isNewField);

            addGetter(cc, isNewField, "$$_isNew");
            addSetter(null, cc, isNewField, "$$_setIsNew", null, false, false);
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

    private CtMethod addSetDirty(CtClass cc, CtField dirtyField) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "([" + Descriptor.of("long") + ")V";
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_setDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\t$0.").append(dirtyFieldName).append(" = $1[0];\n");

        sb.append("\tif ($0.").append(dirtyFieldName).append(" != 0 && $0.$$_parent != null) {\n");
        sb.append("\t\t$0.$$_parent.$$_markDirty($0.$$_parentIndex);\n");
        sb.append("\t}\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addUnmarkDirty(CtClass cc, CtField dirtyField) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()" + Descriptor.of("void");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_unmarkDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\t$0.").append(dirtyFieldName).append(" = 0;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addResetDirty(CtClass cc, CtField dirtyField, boolean[] supportsDirtyTracking, boolean allSupportDirtyTracking) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()[" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_resetDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tlong[] dirty = new long[1];\n");
        sb.append("\tdirty[0] = $0.").append(dirtyFieldName);

        if (!allSupportDirtyTracking) {
            long mask = 0;
            for (int i = 0; i < supportsDirtyTracking.length; i++) {
                if (!supportsDirtyTracking[i]) {
                    mask |= 1 << i;
                }
            }

            sb.append(" | ").append(mask);
        }

        sb.append(";\n");

        sb.append("\t$0.").append(dirtyFieldName).append(" = 0;\n");

        sb.append("\treturn dirty;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addGetDirty(CtClass cc, CtField dirtyField, boolean[] supportsDirtyTracking, boolean allSupportDirtyTracking) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()[" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tlong[] dirty = new long[1];\n");
        sb.append("\tdirty[0] = $0.").append(dirtyFieldName);

        if (!allSupportDirtyTracking) {
            long mask = 0;
            for (int i = 0; i < supportsDirtyTracking.length; i++) {
                if (!supportsDirtyTracking[i]) {
                    mask |= 1 << i;
                }
            }

            sb.append(" | ").append(mask);
        }

        sb.append(";\n");
        sb.append("\treturn dirty;\n");
        sb.append('}');

        CtMethod method = CtMethod.make(minfo, cc);
        method.setBody(sb.toString());
        cc.addMethod(method);
        return method;
    }

    private CtMethod addGetSimpleDirty(CtClass cc, CtField dirtyField, boolean[] supportsDirtyTracking, boolean allSupportDirtyTracking) throws CannotCompileException {
        FieldInfo dirtyFieldInfo = dirtyField.getFieldInfo2();
        String desc = "()" + Descriptor.of("long");
        ConstPool cp = dirtyFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_getSimpleDirty", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String dirtyFieldName = dirtyFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\treturn $0.").append(dirtyFieldName);

        if (!allSupportDirtyTracking) {
            long mask = 0;
            for (int i = 0; i < supportsDirtyTracking.length; i++) {
                if (!supportsDirtyTracking[i]) {
                    mask |= 1 << i;
                }
            }

            sb.append(" | ").append(mask);
        }

        sb.append(";\n");
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
        sb.append("\t\tthrow new IllegalStateException(\"Parent object for \" + $0.toString() + \" is already set to \" + $0.").append(parentFieldName).append(".toString() + \" and can't be set to:\" + $1.toString());\n");
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

    private CtMethod addUnsetParent(CtClass cc, CtField parentField, CtField parentIndexField) throws CannotCompileException {
        FieldInfo parentFieldInfo = parentField.getFieldInfo2();
        FieldInfo parentIndexFieldInfo = parentIndexField.getFieldInfo2();
        String desc = "()V";
        ConstPool cp = parentFieldInfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, "$$_unsetParent", desc);
        minfo.setAccessFlags(AccessFlag.PUBLIC);
        String parentFieldName = parentFieldInfo.getName();
        String parentIndexFieldName = parentIndexFieldInfo.getName();

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
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
                sb.append("\tif (!$0.$$_isNew) {\n");
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

        if (attribute != null && attribute.getDirtyStateIndex() != -1) {
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
                sb.append("\tif ($0.").append(fieldName).append(" != $1 && $0.").append(fieldName).append(" instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                sb.append("\t\t((").append(BasicDirtyTracker.class.getName()).append(") $0.").append(fieldName).append(").$$_unsetParent();\n");
                sb.append("\t}\n");
            }
        }

        if (attribute != null && attribute.isUpdatable()) {
            // Collections do type checking in their recording collection implementations
            if (!attribute.isCollection() && (attribute.isPersistCascaded() || attribute.isUpdateCascaded())) {
                // Only consider subviews here for now
                if (attribute.isSubview()) {
                    String subtypeArray = addAllowedSubtypeField(cc, attribute);
                    sb.append("\tif ($1 != null) {\n");
                    sb.append("\t\tClass c;\n");
                    sb.append("\t\tif ($1 instanceof ").append(EntityViewProxy.class.getName()).append(") {\n");
                    sb.append("\t\t\tc = ((").append(EntityViewProxy.class.getName()).append(") $1).$$_getEntityViewClass();\n");
                    sb.append("\t\t} else {\n");
                    sb.append("\t\t\tc = $1.getClass();\n");
                    sb.append("\t\t}\n");

                    sb.append("\t\tif (!").append(attributeField.getDeclaringClass().getName()).append('#').append(attribute.getName()).append("_$$_subtypes.contains(c)) {\n");
                    sb.append("\t\t\tthrow new IllegalArgumentException(");
                    sb.append("\"Allowed subtypes for attribute '").append(attribute.getName()).append("' are [").append(subtypeArray).append("] but got an instance of: \"");
                    sb.append(".concat(c.getName())");
                    sb.append(");\n");
                    sb.append("\t\t}\n");
                    sb.append("\t}\n");
                }
            }
        }

        if (attribute != null && attribute.getDirtyStateIndex() != -1) {
            int mutableStateIndex = attribute.getDirtyStateIndex();
            if (mutableStateField != null) {
                // this.mutableState[mutableStateIndex] = $1
                sb.append("\t$0.").append(mutableStateField.getName()).append("[").append(mutableStateIndex).append("] = ");
                renderValueForArray(sb, attributeField.getType(), 1);
            }
            if (dirtyChecking) {
                // this.dirty = true
                sb.append("\t$0.$$_markDirty(").append(mutableStateIndex).append(");\n");

                // Set new objects parent
                if (attribute.isCollection() || attribute.isSubview()) {
                    sb.append("\tif ($1 != null && $0.").append(fieldName).append(" != $1) {\n");
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
                        sb.append("\t\tif ($1 instanceof ").append(BasicDirtyTracker.class.getName()).append(") {\n");
                        sb.append("\t\t\t((").append(BasicDirtyTracker.class.getName()).append(") $1).$$_setParent($0, ").append(mutableStateIndex).append(");\n");
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
            METHOD: for (Method m : c.getMethods()) {
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
            METHOD: for (Method m : c.getMethods()) {
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

    private CtMethod createEquals(Class<?> viewClass, CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        return createEquals(viewClass, cc, false, fields);
    }

    private CtMethod createIdEquals(Class<?> viewClass, CtClass cc) throws NotFoundException, CannotCompileException {
        return createEquals(viewClass, cc, true, null);
    }

    private CtMethod createEquals(Class<?> viewClass, CtClass cc, boolean idBased, CtField[] fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo method = new MethodInfo(cp, "equals", getEqualsDesc());
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tif ($0 == $1) { return true; }\n");
        sb.append("\tif ($1 == null || !($1 instanceof ").append(EntityViewProxy.class.getName()).append(")) { return false; }\n");
        sb.append("\tif ($0.$$_getJpaManagedClass() != ((").append(EntityViewProxy.class.getName()).append(") $1).$$_getJpaManagedClass()) { return false; }\n");

        if (idBased) {
            sb.append("\treturn $0.$$_getId() != null && $0.$$_getId().equals(((").append(EntityViewProxy.class.getName()).append(") $1).$$_getId());\n");
        } else {
            sb.append("\tfinal ").append(viewClass.getName()).append(" other = (").append(viewClass.getName()).append(") $1;\n");

            for (CtField field : fields) {
                if (field.getType().isPrimitive()) {
                    if (CtClass.booleanType == field.getType()) {
                        sb.append("\tif ($0.").append(field.getName()).append(" != other.is");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(") {\n");
                    } else {
                        sb.append("\tif ($0.").append(field.getName()).append(" != other.get");
                        StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                        sb.append(") {\n");
                    }
                } else {
                    sb.append("\tif ($0.").append(field.getName()).append(" != other.get");
                    StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                    sb.append(" && ($0.").append(field.getName()).append(" == null");
                    sb.append(" || !$0.").append(field.getName()).append(".equals(other.get");
                    StringUtils.addFirstToUpper(sb, field.getName()).append("()");
                    sb.append("))) {\n");
                }
                sb.append("\t\treturn false;\n\t}\n");
            }
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
        sb.append("\tint hash = 3;\n");

        for (CtField field : fields) {
            if (field.getType().isPrimitive()) {
                CtClass type = field.getType();
                if (CtClass.doubleType == type) {
                    sb.append("long bits = java.lang.Double.doubleToLongBits($0.").append(field.getName()).append(");");
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

    private CtConstructor createNormalConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, CtField initialStateField, CtField mutableStateField,
                                                  AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        int superConstructorStart = attributeFields.length;
        int superConstructorEnd = attributeTypes.length;
        return createConstructor(evm, managedViewType, cc, superConstructorStart, superConstructorEnd, attributeFields, attributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, ConstructorKind.NORMAL, null, unsafe);
    }

    private CtConstructor createCreateConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, CtField idField, CtField initialStateField, CtField mutableStateField,
                                                  AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        return createConstructor(evm, managedViewType, cc, 0, 0, attributeFields, attributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, ConstructorKind.CREATE, idField, unsafe);
    }

    private CtConstructor createReferenceConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, CtField[] attributeFields, CtField idField, CtField initialStateField, CtField mutableStateField,
                                                     AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        CtClass[] attributeTypes = new CtClass[]{ idField.getType() };
        return createConstructor(evm, managedViewType, cc, 0, 0, attributeFields, attributeTypes, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, ConstructorKind.REFERENCE, idField, unsafe);
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

    private CtConstructor createConstructor(EntityViewManager evm, ManagedViewType<?> managedViewType, CtClass cc, int superConstructorStart, int superConstructorEnd, CtField[] attributeFields, CtClass[] attributeTypes, CtField initialStateField, CtField mutableStateField,
                                            AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount, ConstructorKind kind, CtField idField, boolean unsafe) throws CannotCompileException, NotFoundException, BadBytecode {
        CtClass[] parameterTypes;
        if (kind == ConstructorKind.CREATE) {
            parameterTypes = new CtClass[]{ };
        } else {
            parameterTypes = attributeTypes;
        }
        CtConstructor ctConstructor = new CtConstructor(parameterTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (unsafe) {
            renderFieldInitialization(evm, managedViewType, attributeFields, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, kind, sb, idField);
            renderSuperCall(cc, superConstructorStart, superConstructorEnd, sb);
        } else {
            renderSuperCall(cc, superConstructorStart, superConstructorEnd, sb);
            renderFieldInitialization(evm, managedViewType, attributeFields, initialStateField, mutableStateField, mutableAttributes, mutableAttributeCount, kind, sb, idField);
        }

        // Always register dirty tracker after super call
        renderDirtyTrackerRegistration(attributeFields, mutableStateField, mutableAttributes, kind, sb);
        Method postCreateMethod = null;
        if (kind == ConstructorKind.CREATE && managedViewType.getPostCreateMethod() != null) {
            postCreateMethod = managedViewType.getPostCreateMethod();
            // Skip invocation of postCreate method if the type is an interface
            // Note that if you change the invocation here, the invocation below has to be changed as well
            if (!managedViewType.getJavaType().isInterface()) {
                if (postCreateMethod.getParameterTypes().length == 1) {
                    sb.append("\t$0.").append(postCreateMethod.getName()).append("(").append(cc.getName()).append("#$$_evm);\n");
                } else {
                    sb.append("\t$0.").append(postCreateMethod.getName()).append("();\n");
                }
                postCreateMethod = null;
            }
        }
        sb.append("}");
        if (unsafe) {
            compileUnsafe(ctConstructor, sb.toString());
        } else {
            ctConstructor.setBody(sb.toString());
        }

        // If the method invocation was generated for the abstract class, we set the method to null in the above code
        if (postCreateMethod != null) {
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

            String postCreateMethodDescriptor;
            bc.addAload(0);
            if (postCreateMethod.getParameterTypes().length == 1) {
                bc.addGetstatic(cc, "$$_evm", Descriptor.of(EntityViewManager.class.getName()));
                postCreateMethodDescriptor = "(L" + Descriptor.toJvmName(EntityViewManager.class.getName()) + ";)V";
            } else {
                postCreateMethodDescriptor = "()V";
            }
            bc.addInvokespecial(managedViewType.getJavaType().getName(), postCreateMethod.getName(), postCreateMethodDescriptor);
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

        return ctConstructor;
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

    private void renderFieldInitialization(EntityViewManager entityViewManager, ManagedViewType<?> managedViewType, CtField[] attributeFields, CtField initialStateField, CtField mutableStateField, AbstractMethodAttribute<?, ?>[] mutableAttributes, int mutableAttributeCount, ConstructorKind kind, StringBuilder sb, CtField idField) throws NotFoundException, CannotCompileException {
        if (initialStateField != null) {
            sb.append("\tObject[] initialStateArr = new Object[").append(mutableAttributeCount).append("];\n");
        }

        if (mutableStateField != null) {
            sb.append("\tObject[] mutableStateArr = new Object[").append(mutableAttributeCount).append("];\n");
        }

        if (kind == ConstructorKind.CREATE && managedViewType.isCreatable()) {
            sb.append("\t$0.$$_isNew = true;\n");
        }

        for (int i = 0; i < attributeFields.length; i++) {
            if (attributeFields[i] == null) {
                continue;
            }

            AbstractMethodAttribute<?, ?> methodAttribute = mutableAttributes[i];

            // this.$(attributeField[i]) = $(fieldSlot)
            sb.append("\t$0.").append(attributeFields[i].getName()).append(" = ");
            if (kind != ConstructorKind.CREATE && attributeFields[i] == idField) {
                // The id field for the reference and normal constructor are never empty
                sb.append('$').append(i + 1).append(";\n");
            } else if (kind != ConstructorKind.NORMAL) {
                CtClass type = attributeFields[i].getType();
                if (type.isPrimitive()) {
                    sb.append(getDefaultValue(type)).append(";\n");
                    if (mutableStateField != null && methodAttribute != null) {
                        sb.append("\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                        if (type == CtClass.longType) {
                            sb.append("Long.valueOf(0L);\n");
                        } else if (type == CtClass.floatType) {
                            sb.append("Float.valueOf(0F);\n");
                        } else if (type == CtClass.doubleType) {
                            sb.append("Double.valueOf(0D);\n");
                        } else if (type == CtClass.shortType) {
                            sb.append("Short.valueOf((short) 0);\n");
                        } else if (type == CtClass.byteType) {
                            sb.append("Byte.valueOf((byte) 0);\n");
                        } else if (type == CtClass.booleanType) {
                            sb.append("Boolean.FALSE;\n");
                        } else if (type == CtClass.charType) {
                            sb.append("Character.valueOf('\\u0000');\n");
                        } else {
                            sb.append("Integer.valueOf(0);\n");
                        }
                    }
                } else if (methodAttribute != null) {
                    if (mutableStateField != null) {
                        sb.append("mutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                    }
                    if (kind == ConstructorKind.CREATE) {
                        // init embeddables and collections
                        if (methodAttribute instanceof PluralAttribute<?, ?, ?>) {
                            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) methodAttribute;
                            addAllowedSubtypeField(attributeFields[i].getDeclaringClass(), methodAttribute);

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
                            sb.append(methodAttribute.isUpdatable()).append(',');
                            sb.append(methodAttribute.isOptimizeCollectionActionsEnabled());
                            sb.append(");\n");
                        } else {
                            SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) methodAttribute;
                            if (singularAttribute.getType().getMappingType() == Type.MappingType.FLAT_VIEW) {
                                ManagedViewTypeImplementor<Object> attributeManagedViewType = (ManagedViewTypeImplementor<Object>) singularAttribute.getType();
                                sb.append("new ");
                                sb.append(getProxy(entityViewManager, attributeManagedViewType, null).getName());
                                sb.append("();\n");
                            } else {
                                sb.append("null;\n");
                            }
                        }

                    } else {
                        // Attributes for reference constructors don't initialize objects
                        sb.append("null;\n");
                    }
                } else {
                    // For create constructors we initialize embedded ids
                    MethodAttribute<?, ?> idAttribute;
                    if (kind == ConstructorKind.CREATE && attributeFields[i] == idField
                            && (idAttribute = ((ViewType<?>) managedViewType).getIdAttribute()).isSubview()) {
                        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) idAttribute;
                        sb.append("new ");
                        sb.append(getProxy(entityViewManager, (ManagedViewTypeImplementor<Object>) singularAttribute.getType(), null).getName());
                        sb.append("();\n");
                    } else {
                        sb.append("null;\n");
                    }
                }
            } else {
                sb.append('$').append(i + 1).append(";\n");
            }

            if (kind == ConstructorKind.NORMAL && methodAttribute != null) {
                CtClass type = attributeFields[i].getType();
                if (mutableStateField != null) {
                    // locvar2[j] = $(i + 1)
                    sb.append("\tmutableStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                }

                if (initialStateField != null) {
                    // locvar1[j] = $(i + 1)
                    sb.append("initialStateArr[").append(methodAttribute.getDirtyStateIndex()).append("] = ");
                }

                if (mutableStateField != null) {
                    renderValueForArray(sb, type, i + 1);
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

    private String getDefaultValue(CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                return "0L";
            } else if (type == CtClass.floatType) {
                return "0F";
            } else if (type == CtClass.doubleType) {
                return "0D";
            } else {
                return "0";
            }
        } else {
            return "(" + type.getName() + ") null";
        }
    }

    private void renderValueForArray(StringBuilder sb, CtClass type, int index) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                sb.append("Long.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.floatType) {
                sb.append("Float.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.doubleType) {
                sb.append("Double.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.shortType) {
                sb.append("Short.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.byteType) {
                sb.append("Byte.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.booleanType) {
                sb.append("Boolean.valueOf($").append(index).append(");\n");
            } else if (type == CtClass.charType) {
                sb.append("Character.valueOf($").append(index).append(");\n");
            } else {
                sb.append("Integer.valueOf($").append(index).append(");\n");
            }
        } else {
            sb.append("$").append(index).append(";\n");
        }
    }

    private void renderDirtyTrackerRegistration(CtField[] attributeFields, CtField mutableStateField, AbstractMethodAttribute<?, ?>[] mutableAttributes, ConstructorKind kind, StringBuilder sb) throws NotFoundException, CannotCompileException {
        for (int i = 0; i < attributeFields.length; i++) {
            if (attributeFields[i] == null) {
                continue;
            }

            AbstractMethodAttribute<?, ?> methodAttribute = mutableAttributes[i];
            if (kind != ConstructorKind.REFERENCE && methodAttribute != null) {
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

    private String addAllowedSubtypeField(CtClass declaringClass, AbstractMethodAttribute<?, ?> attribute) throws NotFoundException, CannotCompileException {
        StringBuilder subtypeArrayBuilder = new StringBuilder();

        Set<Class<?>> allowedSubtypes = new HashSet<>();
        if (attribute.isCollection()) {
            allowedSubtypes.add(((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType());
        } else {
            allowedSubtypes.add(attribute.getJavaType());
        }
        for (Type<?> t : attribute.getPersistCascadeAllowedSubtypes()) {
            allowedSubtypes.add(t.getJavaType());
        }
        for (Type<?> t : attribute.getUpdateCascadeAllowedSubtypes()) {
            allowedSubtypes.add(t.getJavaType());
        }

        for (Class<?> c : allowedSubtypes) {
            subtypeArrayBuilder.append(c.getName());
            subtypeArrayBuilder.append(", ");
        }

        subtypeArrayBuilder.setLength(subtypeArrayBuilder.length() - 2);
        String subtypeArray = subtypeArrayBuilder.toString();

        try {
            declaringClass.getDeclaredField(attribute.getName() + "_$$_subtypes");
            return subtypeArray;
        } catch (NotFoundException ex) {
        }

        StringBuilder fieldSb = new StringBuilder();
        fieldSb.append("private static final java.util.Set ");
        fieldSb.append(attribute.getName());
        fieldSb.append("_$$_subtypes = new java.util.HashSet(java.util.Arrays.asList(new java.lang.Class[]{ ");

        for (Class<?> c : allowedSubtypes) {
            fieldSb.append(c.getName());
            fieldSb.append(".class, ");
        }

        fieldSb.setLength(fieldSb.length() - 2);
        fieldSb.append(" }));");
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

    private void renderSuperCall(CtClass cc, int superStart, int superEnd, StringBuilder sb) throws NotFoundException {
        sb.append("\tsuper(");
        if (superStart < superEnd) {
            for (int i = superStart; i < superEnd; i++) {
                sb.append('$').append(i + 1).append(',');
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
