/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;

/**
 *
 * @author cpbec
 */
public class ProxyFactory {
    
    private static final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private static final AtomicInteger classCounter = new AtomicInteger();
    
    public static <T> Class<? extends T> getProxy(ViewType<T> viewType) {
        Class<T> clazz = viewType.getJavaType();
        Class<? extends T> proxyClass = (Class<? extends T>) proxyClasses.get(clazz);
        
        if (proxyClass == null) {
            proxyClass = createProxyClass(viewType);
            Class<? extends T> oldProxyClass = (Class<? extends T>) proxyClasses.putIfAbsent(clazz, proxyClass);
            
            if (oldProxyClass != null) {
                proxyClass = oldProxyClass;
            }
        }
        
        return proxyClass;
    }

    private static <T> Class<? extends T> createProxyClass(ViewType<T> viewType) {
        Class<?> clazz = viewType.getJavaType();
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass(clazz.getName() + "_$$_javassist_entityview_" + classCounter.getAndIncrement());
        CtClass superCc;
        
        try {
            superCc = pool.get(clazz.getName());
        
            if (clazz.isInterface()) {
                cc.addInterface(superCc);
            } else {
                cc.setSuperclass(superCc);
            }
            
            Set<MethodAttribute<? super T, ?>> attributes = viewType.getAttributes();
            CtField[] attributeFields = new CtField[attributes.size()];
            CtClass[] attributeTypes = new CtClass[attributes.size()];
            int i = 0;
            
            for (MethodAttribute<?, ?> attribute : attributes) {
                Method getter = attribute.getJavaMethod();
                Method setter = ReflectionUtils.getSetter(clazz, attribute.getName());
                
                // Create the field from the attribute
                CtField attributeField = new CtField(getType(pool, attribute), attribute.getName(), cc);
                attributeField.setModifiers(getModifiers(setter != null));
                String genericSignature = getGenericSignature(attribute, attributeField);
                if (genericSignature != null) {
                    attributeField.setGenericSignature(genericSignature);
                }
                cc.addField(attributeField);
                
                boolean createBridges = !attribute.getJavaType().equals(getter.getReturnType());
                
                CtMethod attributeGetter = CtNewMethod.getter(getter.getName(), attributeField);
                
                if (genericSignature != null) {
                    String getterGenericSignature = "()" + genericSignature;
                    attributeGetter.setGenericSignature(getterGenericSignature);
                }
                
                if (createBridges) {
                    CtMethod getterBridge = createGetterBridge(cc, pool, getter, attributeGetter);
                    cc.addMethod(getterBridge);
                }
                cc.addMethod(attributeGetter);
                
                if (setter != null) {
                    CtMethod attributeSetter = CtNewMethod.setter(setter.getName(), attributeField);
                    if (genericSignature != null) {
                        String setterGenericSignature = "(" + genericSignature + ")V";
                        attributeSetter.setGenericSignature(setterGenericSignature);
                    }
                        
                    if (createBridges) {
                        CtMethod setterBridge = createSetterBridge(cc, pool, setter, attributeSetter);
                        cc.addMethod(setterBridge);
                    }
                    cc.addMethod(attributeSetter);
                }
                
                attributeFields[i] = attributeField;
                attributeTypes[i] = attributeField.getType();
                i++;
            }
            
            // Add the default constructor only for interfaces since abstract classes may omit it
            if (clazz.isInterface()) {
                cc.addConstructor(createConstructor(cc, attributeFields, attributeTypes));
            }
            
            Set<MappingConstructor<T>> constructors = viewType.getConstructors();
            
            for (MappingConstructor<?> constructor : constructors) {
                // Copy default constructor parameters
                int constructorParameterCount = attributes.size() + constructor.getParameterAttributes().size();
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(attributeTypes, 0, constructorAttributeTypes, 0, attributes.size());
                
                // Append super constructor parameters to default constructor parameters
                CtConstructor superConstructor = findConstructor(pool, superCc, constructor);
                System.arraycopy(superConstructor.getParameterTypes(), 0, constructorAttributeTypes, attributes.size(), superConstructor.getParameterTypes().length);
                
                cc.addConstructor(createConstructor(cc, attributeFields, constructorAttributeTypes));
            }
            
            return cc.toClass();
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        }
    }

    private static CtMethod createGetterBridge(CtClass cc, ClassPool pool, Method getter, CtMethod attributeGetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeReturnType = pool.get(getter.getReturnType().getName());
        String desc = "()" + Descriptor.of(bridgeReturnType);
        MethodInfo bridge = new MethodInfo(cp, getter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);
        
        Bytecode code = new Bytecode(cp, 2, 1);
        code.addAload(0);
        code.addInvokevirtual(cc, getter.getName(), attributeGetter.getReturnType(), null);
        code.addReturn(bridgeReturnType);
        
        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private static CtMethod createSetterBridge(CtClass cc, ClassPool pool, Method setter, CtMethod attributeSetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeParameterType = pool.get(setter.getParameterTypes()[0].getName());
        String desc = "(" + Descriptor.of(bridgeParameterType) + ")V";
        MethodInfo bridge = new MethodInfo(cp, setter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);
        
        Bytecode code = new Bytecode(cp, 2, 2);
        code.addAload(0);
        code.addAload(1);
        code.addCheckcast(attributeSetter.getParameterTypes()[0]);
        code.addInvokevirtual(cc, setter.getName(), CtClass.voidType, attributeSetter.getParameterTypes());
        code.addReturn(CtClass.voidType);
        
        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }
    
    private static CtConstructor createConstructor(CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes) throws CannotCompileException {
        CtConstructor ctConstructor = new CtConstructor(attributeTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\tsuper(");
        for (int i = attributeFields.length; i < attributeTypes.length; i++) {
            if (i != attributeFields.length) {
                sb.append(',');
            }
            
            sb.append('$').append(i + 1);
        }
        sb.append(");\n");

        for (int i = 0; i < attributeFields.length; i++) {
            sb.append("\tthis.").append(attributeFields[i].getName()).append(" = ").append('$').append(i + 1).append(";\n");
        }
        
        sb.append('}');
        ctConstructor.setBody(sb.toString());
        return ctConstructor;
    }

    private static CtConstructor findConstructor(ClassPool pool, CtClass superCc, MappingConstructor<?> constructor) throws NotFoundException {
        List<ParameterAttribute<?, ?>> parameterAttributes = (List<ParameterAttribute<?, ?>>) constructor.getParameterAttributes();
        CtClass[] parameterTypes = new CtClass[parameterAttributes.size()];
        
        for (int i = 0; i < parameterAttributes.size(); i++) {
            parameterTypes[i] = pool.get(parameterAttributes.get(i).getJavaType().getName());
        }
        
        return superCc.getDeclaredConstructor(parameterTypes);
    }
    
    private static CtClass getType(ClassPool pool, MethodAttribute<?, ?> attribute) throws NotFoundException {
        return pool.get(attribute.getJavaType().getName());
    }

    private static int getModifiers(boolean hasSetter) {
        if (hasSetter) {
            return Modifier.PRIVATE;
        } else {
            return Modifier.PRIVATE | Modifier.FINAL;
        }
    }

    private static String getGenericSignature(MethodAttribute<?, ?> attribute, CtField attributeField) throws NotFoundException {
        Class[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(attribute.getDeclaringType().getJavaType(), attribute.getJavaMethod());
        if (typeArguments.length == 0) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(typeArguments.length * 10);
        
        String simpleType = Descriptor.of(attributeField.getType());
        sb.append(simpleType, 0, simpleType.length() - 1);
        sb.append('<');
        
        for (int i = 0; i < typeArguments.length; i++) {
            sb.append(Descriptor.of(typeArguments[i].getName()));
        }
        
        sb.append('>');
        sb.append(';');
        
        return sb.toString();
    }
}
