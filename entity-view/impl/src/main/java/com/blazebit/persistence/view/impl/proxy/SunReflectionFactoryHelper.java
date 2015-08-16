package com.blazebit.persistence.view.impl.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

class SunReflectionFactoryHelper {

   @SuppressWarnings("unchecked")
   public static <T> Constructor<T> newConstructorForSerialization(Class<T> type, Constructor<?> constructor) {

      try {
    	  Class<?> reflectionFactoryClass = Class.forName("sun.reflect.ReflectionFactory");
    	  
          Method getReflectionFactory = reflectionFactoryClass.getDeclaredMethod("getReflectionFactory");
    	  Object reflectionFactory = getReflectionFactory.invoke(null);

          Method newConstructorForSerializationMethod = reflectionFactoryClass.getDeclaredMethod("newConstructorForSerialization", Class.class, Constructor.class);
          
          return (Constructor<T>) newConstructorForSerializationMethod.invoke(reflectionFactory, type, constructor);
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }
}