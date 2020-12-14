Blaze-Persistence Examples Spring Data GraphQL
==========
This is an GraphQL sample application showcasing how the Blaze-Persistence GraphQL
integration can be used to develop GraphQL interfaces with ease. 

## How to use it?

Just run `mvn spring-boot:run` and navigate to http://localhost:8080/graphiql where you can run your GraphQL queries.
You will see that the queries exposed there use the GraphQL Relay spec to implement pagination through cursors.
You can take a look at the generated queries to further understand what happens or you take a look into the [documentation](https://persistence.blazebit.com/documentation/core/manual/en_US/index.html#anchor-keyset-pagination) for more information on the topic.

Another thing that you will see from the generated queries is that the GraphQL selection list actually alters what is selected in the SQL query!