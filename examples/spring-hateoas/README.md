Blaze-Persistence Examples Spring HATEOAS WebMvc
==========
This is a Spring HATEOAS sample application showcasing how keyset pagination works with Blaze-Persistence
with and without entity views. Also offers insight into how filters could be implemented. 

## How to use it?

Just run `mvn spring-boot:run` and use the endpoints http://localhost:8080/cats?sort=id or http://localhost:8080/cat-views?sort=id to see the integration in action.
When using the content type `application/hal+json`, you can see the pagination links in the JSON payload, otherwise you will see it in the `Link` HTTP response header.

You can take a look at the generated queries to further understand what happens or you take a look into the [documentation](https://persistence.blazebit.com/documentation/core/manual/en_US/index.html#anchor-keyset-pagination) for more information on the topic.