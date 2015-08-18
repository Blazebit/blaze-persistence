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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
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
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ProxyFactory {

    private final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private final ConcurrentMap<Class<?>, Class<?>> unsafeProxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private final Object proxyLock = new Object();
    private final ClassPool pool;

    public ProxyFactory() {
        this.pool = new ClassPool(ClassPool.getDefault());
    }

    public <T> Class<? extends T> getProxy(ViewType<T> viewType) {
        return getProxy(viewType, false);
    }

    public <T> Class<? extends T> getUnsafeProxy(ViewType<T> viewType) {
    	return getProxy(viewType, true);
    }
    
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> getProxy(ViewType<T> viewType, boolean unsafe) {
        Class<T> clazz = viewType.getJavaType();
        ConcurrentMap<Class<?>, Class<?>> classes = unsafe ? unsafeProxyClasses : proxyClasses;
		Class<? extends T> proxyClass = (Class<? extends T>) classes.get(clazz);

        // Double checked locking since we can only define the class once
        if (proxyClass == null) {
        	synchronized (proxyLock) {
        		proxyClass = (Class<? extends T>) classes.get(clazz);
                if (proxyClass == null) {
		            proxyClass = createProxyClass(viewType, unsafe);
		            proxyClasses.put(clazz, proxyClass);
                }
        	}
        }

        return proxyClass;
    }

    @SuppressWarnings("unchecked")
	private <T> Class<? extends T> createProxyClass(ViewType<T> viewType, boolean unsafe) {
        Class<?> clazz = viewType.getJavaType();
        String suffix = unsafe ? "unsafe_" : "";
        String proxyClassName = clazz.getName() + "_$$_javassist_entityview_" + suffix;
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

            Set<MethodAttribute<? super T, ?>> attributes = viewType.getAttributes();
            CtField[] attributeFields = new CtField[attributes.size()];
            CtClass[] attributeTypes = new CtClass[attributes.size()];
            int i = 1;

            // Create the id field
            MethodAttribute<? super T, ?> idAttribute = viewType.getIdAttribute();
            CtField idField = addMembersForAttribute(idAttribute, clazz, cc);
            attributeFields[0] = idField;
            attributeTypes[0] = idField.getType();
            attributes.remove(idAttribute);

            for (MethodAttribute<?, ?> attribute : attributes) {
                if (attribute == idAttribute) {
                    continue;
                }
                
                CtField attributeField = addMembersForAttribute(attribute, clazz, cc);
                attributeFields[i] = attributeField;
                attributeTypes[i] = attributeField.getType();
                i++;
            }
            
            CtClass equalsDeclaringClass = superCc.getMethod("equals", getEqualsDesc()).getDeclaringClass();
            if (!"java.lang.Object".equals(equalsDeclaringClass.getName())) {
                throw new IllegalArgumentException("The class '" + equalsDeclaringClass.getName() + "' declares 'boolean equals(java.lang.Object)' but is not allowed to!");
            }
            cc.addMethod(createEquals(cc, idField));

            CtClass hashCodeDeclaringClass = superCc.getMethod("hashCode", getHashCodeDesc()).getDeclaringClass();
            if (!"java.lang.Object".equals(hashCodeDeclaringClass.getName())) {
                throw new IllegalArgumentException("The class '" + hashCodeDeclaringClass.getName() + "' declares 'int hashCode()' but is not allowed to!");
            }
            cc.addMethod(createHashCode(cc, idField));

            // Add the default constructor only for interfaces since abstract classes may omit it
            if (clazz.isInterface()) {
                cc.addConstructor(createConstructor(cc, attributeFields, attributeTypes, unsafe));
            }

            Set<MappingConstructor<T>> constructors = viewType.getConstructors();

            for (MappingConstructor<?> constructor : constructors) {
                // Copy default constructor parameters
                int constructorParameterCount = 1 + attributes.size() + constructor.getParameterAttributes().size();
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(attributeTypes, 0, constructorAttributeTypes, 0, 1 + attributes.size());

                // Append super constructor parameters to default constructor parameters
                CtConstructor superConstructor = findConstructor(superCc, constructor);
                System.arraycopy(superConstructor.getParameterTypes(), 0, constructorAttributeTypes, 1 + attributes.size(), superConstructor.getParameterTypes().length);

                cc.addConstructor(createConstructor(cc, attributeFields, constructorAttributeTypes, unsafe));
            }

            try {
                if (unsafe) {
                	return (Class<? extends T>) UnsafeHelper.define(cc.getName(), cc.toBytecode(), clazz);
                } else {
                	return cc.toClass();
                }
            } catch (CannotCompileException ex) {
        		// If there are multiple proxy factories for the same class loader 
            	// we could end up in defining a class multiple times, so we check if the classloader
            	// actually has something to offer
            	if (ex.getCause() instanceof LinkageError) {
            		LinkageError error = (LinkageError) ex.getCause();
	            	try {
	            		return (Class<? extends T>) pool.getClassLoader().loadClass(proxyClassName);
	            	} catch (ClassNotFoundException cnfe) {
	            		// Something we can't handle happened
	            		throw error;
	            	}
            	} else {
            		throw ex;
            	}
            }
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }

	private CtField addMembersForAttribute(MethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc) throws CannotCompileException, NotFoundException {
        Method getter = attribute.getJavaMethod();
        Method setter = ReflectionUtils.getSetter(clazz, attribute.getName());
        
        // Create the field from the attribute
        CtField attributeField = new CtField(getType(attribute), attribute.getName(), cc);
        attributeField.setModifiers(getModifiers(setter != null));
        String genericSignature = getGenericSignature(attribute, attributeField);
        if (genericSignature != null) {
            setGenericSignature(attributeField, genericSignature);
        }
        cc.addField(attributeField);
        
        createGettersAndSetters(attribute, clazz, cc, getter, setter, attributeField);
        
        return attributeField;
    }

	private void createGettersAndSetters(MethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc, Method getter, Method setter, CtField attributeField) throws CannotCompileException, NotFoundException {
		String genericSignature = attributeField.getGenericSignature();
		List<Method> bridgeGetters = getBridgeGetters(clazz, attribute, getter);
        
        CtMethod attributeGetter = CtNewMethod.getter(getter.getName(), attributeField);
        
        if (genericSignature != null) {
            String getterGenericSignature = "()" + genericSignature;
            setGenericSignature(attributeGetter, getterGenericSignature);
        }
        
        for (Method m : bridgeGetters) {
            CtMethod getterBridge = createGetterBridge(cc, m, attributeGetter);
            cc.addMethod(getterBridge);
        }
        cc.addMethod(attributeGetter);
        
        if (setter != null) {
            CtMethod attributeSetter = CtNewMethod.setter(setter.getName(), attributeField);
            List<Method> bridgeSetters = getBridgeSetters(clazz, attribute, setter);
            
            if (genericSignature != null) {
                String setterGenericSignature = "(" + genericSignature + ")V";
                setGenericSignature(attributeSetter, setterGenericSignature);
            }

            for (Method m : bridgeSetters) {
                CtMethod setterBridge = createSetterBridge(cc, m, attributeSetter);
                cc.addMethod(setterBridge);
            }
            cc.addMethod(attributeSetter);
        }
	}
    
    private List<Method> getBridgeGetters(Class<?> clazz, MethodAttribute<?, ?> attribute, Method getter) {
		List<Method> bridges = new ArrayList<Method>();
		String name = getter.getName();
		Class<?> attributeType = attribute.getJavaType();
		
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
		Class<?> attributeType = attribute.getJavaType();
		
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

    private CtMethod createEquals(CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo method = new MethodInfo(cp, "equals", getEqualsDesc());
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tif ($1 == null) { return false; }\n");
        sb.append("\tif ($0.getClass() != $1.getClass()) { return false; }\n");
        sb.append("\tfinal ").append(cc.getName()).append(" other = (").append(cc.getName()).append(") $1;\n");

        for (CtField field : fields) {
            sb.append("\tif ($0.").append(field.getName()).append(" != other.").append(field.getName());
            sb.append(" && ($0.").append(field.getName()).append(" == null");
            sb.append(" || !$0.").append(field.getName()).append(".equals(other.").append(field.getName()).append("))) {\n");
            sb.append("\t\treturn false;\n\t}\n");
        }

        sb.append("\treturn true;\n");
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
            sb.append("\thash = 83 * hash + ($0.").append(field.getName()).append(" != null ? ");
            sb.append("$0.").append(field.getName()).append(".hashCode() : 0);\n");
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

        Bytecode code = new Bytecode(cp, 2, 1);
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

        Bytecode code = new Bytecode(cp, 2, 2);
        code.addAload(0);
        code.addAload(1);
        code.addCheckcast(attributeSetter.getParameterTypes()[0]);
        code.addInvokevirtual(cc, setter.getName(), CtClass.voidType, attributeSetter.getParameterTypes());
        code.addReturn(CtClass.voidType);

        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private CtConstructor createConstructor(CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, boolean unsafe) throws CannotCompileException, NotFoundException {
        CtConstructor ctConstructor = new CtConstructor(attributeTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);
        Bytecode bytecode = new Bytecode(cc.getClassFile().getConstPool(), 3, attributeTypes.length + 1);
        
        if (unsafe) {
	        renderFieldInitialization(attributeFields, bytecode);
	        renderSuperCall(cc, attributeFields, attributeTypes, bytecode);
        } else {
	        renderSuperCall(cc, attributeFields, attributeTypes, bytecode);
	        renderFieldInitialization(attributeFields, bytecode);
        }

        bytecode.add(Bytecode.RETURN);
        ctConstructor.getMethodInfo().setCodeAttribute(bytecode.toCodeAttribute());
        return ctConstructor;
    }

	private void renderFieldInitialization(CtField[] attributeFields, Bytecode bytecode) throws NotFoundException {
		for (int i = 0; i < attributeFields.length; i++) {
			bytecode.addAload(0);
			bytecode.addAload(i + 1);
			bytecode.addPutfield(attributeFields[i].getDeclaringClass(), attributeFields[i].getName(), Descriptor.of(attributeFields[i].getType()));
        }
	}

	private void renderSuperCall(CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes, Bytecode bytecode) throws NotFoundException {
		bytecode.addAload(0);
		
		CtClass[] superArguments = new CtClass[attributeTypes.length - attributeFields.length];
		int size = 0;
        
		for (int i = attributeFields.length; i < attributeTypes.length; i++) {
        	superArguments[size++] = attributeTypes[i];
        	bytecode.addAload(i + 1);
        }
        
		bytecode.addInvokespecial(cc.getSuperclass(), "<init>", Descriptor.ofConstructor(superArguments));
	}
    

    private <T> CtConstructor findConstructor(CtClass superCc, MappingConstructor<T> constructor) throws NotFoundException {
        List<ParameterAttribute<? super T, ?>> parameterAttributes = constructor.getParameterAttributes();
        CtClass[] parameterTypes = new CtClass[parameterAttributes.size()];

        for (int i = 0; i < parameterAttributes.size(); i++) {
            parameterTypes[i] = pool.get(parameterAttributes.get(i).getJavaType().getName());
        }

        return superCc.getDeclaredConstructor(parameterTypes);
    }

    private CtClass getType(MethodAttribute<?, ?> attribute) throws NotFoundException {
        return pool.get(attribute.getJavaType().getName());
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
