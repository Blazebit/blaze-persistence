/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class KeysetAwarePagedResourcesAssembler<T> implements RepresentationModelAssembler<Page<T>, PagedModel<EntityModel<T>>> {

    private static final Constructor<EntityModel> ENTITY_MODEL_CONSTRUCTOR;
    private static final Constructor<PagedModel> PAGED_MODEL_CONSTRUCTOR;
    private static final Constructor<Link> LINK_CONSTRUCTOR;

    private static final Method ENTITY_MODEL_FACTORY;
    private static final Method PAGED_MODEL_FACTORY;
    private static final Method LINK_FACTORY;

    static {
        Method entityModelFactory = null;
        Method pagedModelFactory = null;
        Method linkFactory = null;
        Constructor<EntityModel> entityModelConstructor = null;
        Constructor<PagedModel> pagedModelConstructor = null;
        Constructor<Link> linkConstructor = null;
        try {
            entityModelFactory = EntityModel.class.getMethod("of", Object.class);
            pagedModelFactory = PagedModel.class.getMethod("of", Collection.class, PageMetadata.class);
            linkFactory = Link.class.getMethod("of", UriTemplate.class, LinkRelation.class);
        } catch (NoSuchMethodException e) {
            try {
                entityModelConstructor = EntityModel.class.getConstructor(Object.class, Link[].class);
                pagedModelConstructor = PagedModel.class.getConstructor(Collection.class, PageMetadata.class, Link[].class);
                linkConstructor = Link.class.getConstructor(UriTemplate.class, LinkRelation.class);
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new RuntimeException("Could not determine how to construct EntityModel, PagedModel and Link with the given Spring HATEOAS version. Please report this issue!", noSuchMethodException);
            }
        }
        ENTITY_MODEL_CONSTRUCTOR = entityModelConstructor;
        PAGED_MODEL_CONSTRUCTOR = pagedModelConstructor;
        LINK_CONSTRUCTOR = linkConstructor;
        ENTITY_MODEL_FACTORY = entityModelFactory;
        PAGED_MODEL_FACTORY = pagedModelFactory;
        LINK_FACTORY = linkFactory;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final HateoasKeysetPageableHandlerMethodArgumentResolver pageableResolver;
    private final Optional<UriComponents> baseUri;
    private final ObjectMapper objectMapper;
    private final EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

    private boolean forceFirstAndLastRels = false;

    public KeysetAwarePagedResourcesAssembler(HateoasKeysetPageableHandlerMethodArgumentResolver resolver, UriComponents baseUri, ObjectMapper objectMapper) {
        this.pageableResolver = resolver == null ? new HateoasKeysetPageableHandlerMethodArgumentResolver() : resolver;
        this.baseUri = Optional.ofNullable(baseUri);
        this.objectMapper = objectMapper == null ? OBJECT_MAPPER : objectMapper;
    }

    public void setForceFirstAndLastRels(boolean forceFirstAndLastRels) {
        this.forceFirstAndLastRels = forceFirstAndLastRels;
    }

    @Override
    public PagedModel<EntityModel<T>> toModel(Page<T> entity) {
        return toModel(entity, it -> entityModel(it));
    }

    public PagedModel<EntityModel<T>> toModel(Page<T> page, Link selfLink) {
        return toModel(page, it -> entityModel(it), selfLink);
    }

    private EntityModel<T> entityModel(T content) {
        try {
            if (ENTITY_MODEL_FACTORY == null) {
                return ENTITY_MODEL_CONSTRUCTOR.newInstance(content, new Link[0]);
            } else {
                return (EntityModel<T>) ENTITY_MODEL_FACTORY.invoke(null, content);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't instantiate EntityModel", ex);
        }
    }

    public <R extends RepresentationModel<?>> PagedModel<R> toModel(Page<T> page, RepresentationModelAssembler<T, R> assembler) {
        return createModel(page, assembler, Optional.empty());
    }

    public <R extends RepresentationModel<?>> PagedModel<R> toModel(Page<T> page, RepresentationModelAssembler<T, R> assembler, Link link) {
        Assert.notNull(link, "Link must not be null!");
        return createModel(page, assembler, Optional.of(link));
    }

    public PagedModel<?> toEmptyModel(Page<?> page, Class<?> type) {
        return toEmptyModel(page, type, Optional.empty());
    }

    public PagedModel<?> toEmptyModel(Page<?> page, Class<?> type, Link link) {
        return toEmptyModel(page, type, Optional.of(link));
    }

    private PagedModel<?> toEmptyModel(Page<?> page, Class<?> type, Optional<Link> link) {
        Assert.notNull(page, "Page must not be null!");
        Assert.isTrue(!page.hasContent(), "Page must not have any content!");
        Assert.notNull(type, "Type must not be null!");
        Assert.notNull(link, "Link must not be null!");

        PageMetadata metadata = asPageMetadata(page);

        EmbeddedWrapper wrapper = wrappers.emptyCollectionOf(type);
        List<EmbeddedWrapper> embedded = Collections.singletonList(wrapper);

        return addPaginationLinks(pagedModel(embedded, metadata), page, link);
    }

    protected <R extends RepresentationModel<?>, S> PagedModel<R> createPagedModel(List<R> resources, PageMetadata metadata, Page<S> page) {
        Assert.notNull(resources, "Content resources must not be null!");
        Assert.notNull(metadata, "PageMetadata must not be null!");
        Assert.notNull(page, "Page must not be null!");

        return pagedModel(resources, metadata);
    }

    private <R> PagedModel<R> pagedModel(Collection<R> resources, PagedModel.PageMetadata metadata) {
        try {
            if (PAGED_MODEL_FACTORY == null) {
                return PAGED_MODEL_CONSTRUCTOR.newInstance(resources, metadata, new Link[0]);
            } else {
                return (PagedModel<R>) PAGED_MODEL_FACTORY.invoke(null, resources, metadata);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't instantiate PagedModel", ex);
        }
    }

    private <S, R extends RepresentationModel<?>> PagedModel<R> createModel(Page<S> page, RepresentationModelAssembler<S, R> assembler, Optional<Link> link) {
        Assert.notNull(page, "Page must not be null!");
        Assert.notNull(assembler, "ResourceAssembler must not be null!");

        List<R> resources = new ArrayList<>(page.getNumberOfElements());

        for (S element : page) {
            resources.add(assembler.toModel(element));
        }

        PagedModel<R> resource = createPagedModel(resources, asPageMetadata(page), page);

        return addPaginationLinks(resource, page, link);
    }

    private <R> PagedModel<R> addPaginationLinks(PagedModel<R> resources, Page<?> page, Optional<Link> link) {
        UriTemplate base = getUriTemplate(link);

        boolean isNavigable = page.hasPrevious() || page.hasNext();

        if (isNavigable || forceFirstAndLastRels) {
            resources.add(createLink(base, page, PageRequest.of(0, page.getSize(), page.getSort()), IanaLinkRelations.FIRST));
        }

        if (page.hasPrevious()) {
            resources.add(createLink(base, page, page.previousPageable(), IanaLinkRelations.PREV));
        }

        Link selfLink = link.map(it -> it.withSelfRel())//
                .orElseGet(() -> createLink(base, page, page.getPageable(), IanaLinkRelations.SELF));

        resources.add(selfLink);

        if (page.hasNext()) {
            resources.add(createLink(base, page, page.nextPageable(), IanaLinkRelations.NEXT));
        }

        if (isNavigable || forceFirstAndLastRels) {
            int lastIndex = page.getTotalPages() == 0 ? 0 : page.getTotalPages() - 1;
            resources.add(createLink(base, page, PageRequest.of(lastIndex, page.getSize(), page.getSort()), IanaLinkRelations.LAST));
        }

        return resources;
    }

    private UriTemplate getUriTemplate(Optional<Link> baseLink) {
        return UriTemplate.of(baseLink.map(Link::getHref).orElseGet(this::baseUriOrCurrentRequest));
    }

    private Link createLink(UriTemplate base, Page<?> page, Pageable pageable, LinkRelation relation) {
        UriComponentsBuilder builder;
        if (base.getVariables().isEmpty()) {
            builder = fromUriString(base.toString());
        } else {
            builder = fromUri(base.expand());
        }
        pageableResolver.enhance(builder, getMethodParameter(), pageable);

        String previousPagePropertyName = pageableResolver.getParameterNameToUse(pageableResolver.getPreviousPageParameterName(), getMethodParameter());
        String lowestPropertyName = pageableResolver.getParameterNameToUse(pageableResolver.getLowestParameterName(), getMethodParameter());
        String highestPropertyName = pageableResolver.getParameterNameToUse(pageableResolver.getHighestParameterName(), getMethodParameter());

        if ((relation == IanaLinkRelations.NEXT || relation == IanaLinkRelations.PREV) && page instanceof KeysetAwarePage<?>) {
            int pageNumber = page.getNumber();
            builder.replaceQueryParam(previousPagePropertyName, pageableResolver.isOneIndexedParameters() ? pageNumber + 1 : pageNumber);

            Sort sort = page.getSort();
            KeysetPage keysetPage = ((KeysetAwarePage<?>) page).getKeysetPage();
            if (relation == IanaLinkRelations.NEXT) {
                builder.replaceQueryParam(highestPropertyName, serialize(sort, keysetPage.getHighest()));
                builder.replaceQueryParam(lowestPropertyName);
            } else if (pageable.getOffset() != 0) {
                builder.replaceQueryParam(lowestPropertyName, serialize(sort, keysetPage.getLowest()));
                builder.replaceQueryParam(highestPropertyName);
            } else {
                builder.replaceQueryParam(previousPagePropertyName);
                builder.replaceQueryParam(lowestPropertyName);
                builder.replaceQueryParam(highestPropertyName);
            }
        } else {
            builder.replaceQueryParam(previousPagePropertyName);
            builder.replaceQueryParam(lowestPropertyName);
            builder.replaceQueryParam(highestPropertyName);
        }

        return link(UriTemplate.of(builder.build().toString()), relation);
    }

    private Link link(UriTemplate template, LinkRelation rel) {
        try {
            if (LINK_FACTORY == null) {
                return LINK_CONSTRUCTOR.newInstance(template, rel);
            } else {
                return (Link) LINK_FACTORY.invoke(null, template, rel);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't instantiate Link", ex);
        }
    }

    private String serialize(Sort sort, Keyset keyset) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            int index = 0;
            for (Sort.Order order : sort) {
                if (index != 0) {
                    sb.append(',');
                }
                sb.append('"').append(order.getProperty()).append("\":");
                Serializable value = keyset.getTuple()[index];
                sb.append(objectMapper.writeValueAsString(value));
                index++;
            }
            sb.append('}');
            return sb.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected MethodParameter getMethodParameter() {
        return null;
    }

    private PageMetadata asPageMetadata(Page<?> page) {
        Assert.notNull(page, "Page must not be null!");

        int number = pageableResolver.isOneIndexedParameters() ? page.getNumber() + 1 : page.getNumber();

        return new PageMetadata(page.getSize(), number, page.getTotalElements(), page.getTotalPages());
    }

    private String baseUriOrCurrentRequest() {
        return baseUri.map(Object::toString).orElseGet(KeysetAwarePagedResourcesAssembler::currentRequest);
    }

    private static String currentRequest() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toString();
    }
}
