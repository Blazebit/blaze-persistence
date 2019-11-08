/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.spring.data.webmvc.impl;

import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.util.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.Source;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EntityViewAwareJaxb2RootElementHttpMessageConverter extends Jaxb2RootElementHttpMessageConverter {

    private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class<?>, JAXBContext>(64);
    private final EntityViewManager entityViewManager;

    public EntityViewAwareJaxb2RootElementHttpMessageConverter(EntityViewManager entityViewManager) {
        this.entityViewManager = entityViewManager;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (entityViewManager.getMetamodel().view(clazz) == null) {
            return false;
        }
        return super.canRead(clazz, mediaType);
    }

    @Override
    protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source) throws IOException {
        try {
            Class<?> implementationClass = entityViewManager.getReference(clazz, null).getClass();
            source = processSource(source);
            // TODO: Need the serialization class here instead
            Unmarshaller unmarshaller = createUnmarshallerInternal(implementationClass);
            if (clazz.isAnnotationPresent(XmlRootElement.class)) {
                return unmarshaller.unmarshal(source);
            }
            else {
                JAXBElement<?> jaxbElement = unmarshaller.unmarshal(source, implementationClass);
                return jaxbElement.getValue();
            }
        }
        catch (NullPointerException ex) {
            if (!isSupportDtd()) {
                throw new HttpMessageNotReadableException("NPE while unmarshalling. " +
                        "This can happen on JDK 1.6 due to the presence of DTD " +
                        "declarations, which are disabled.", ex);
            }
            throw ex;
        }
        catch (UnmarshalException ex) {
            throw new HttpMessageNotReadableException("Could not unmarshal to [" + clazz + "]: " + ex.getMessage(), ex);

        }
        catch (JAXBException ex) {
            throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
        }
    }

    protected Marshaller createMarshallerInternal(Class<?> clazz) {
        try {
            JAXBContext jaxbContext = getJaxbContextInternal(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            customizeMarshaller(marshaller);
            return marshaller;
        }
        catch (JAXBException ex) {
            throw new HttpMessageConversionException(
                    "Could not create Marshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
        }
    }

    protected Unmarshaller createUnmarshallerInternal(Class<?> clazz) throws JAXBException {
        try {
            JAXBContext jaxbContext = getJaxbContextInternal(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            // Register adapters
            try {
                unmarshaller.setAdapter((XmlAdapter) Class.forName("com.blazebit.persistence.examples.spring.data.webmvc.view.CatAdapter").newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            customizeUnmarshaller(unmarshaller);
            return unmarshaller;
        }
        catch (JAXBException ex) {
            throw new HttpMessageConversionException(
                    "Could not create Unmarshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
        }
    }

    protected JAXBContext getJaxbContextInternal(Class<?> clazz) {
        Assert.notNull(clazz, "'clazz' must not be null");
        JAXBContext jaxbContext = this.jaxbContexts.get(clazz);
        if (jaxbContext == null) {
            try {
//                Map<Class<?>, Class<?>> map = new HashMap<>();
//                map.put(DirtyTracker.class, Object.class);
//                jaxbContext = JAXBContext.newInstance(new Class[]{ clazz }, Collections.singletonMap("com.sun.xml.bind.subclassReplacements", map));
                jaxbContext = JAXBContext.newInstance(new Class[]{ clazz });
                this.jaxbContexts.putIfAbsent(clazz, jaxbContext);
            }
            catch (JAXBException ex) {
                throw new HttpMessageConversionException(
                        "Could not instantiate JAXBContext for class [" + clazz + "]: " + ex.getMessage(), ex);
            }
        }
        return jaxbContext;
    }
}
