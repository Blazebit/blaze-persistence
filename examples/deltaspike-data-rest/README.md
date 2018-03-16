Blaze-Persistence Examples DeltaSpike Data REST
==========
This is an AngularJS sample application showcasing how keyset pagination works with Blaze-Persistence
with and without entity views. Also offers insight into how filters could be implemented. 

## How to use it?

Build the WAR file with `mvn -P wildfly package` for an application server like Wildfly that uses Hibernate as JPA provider.
By default, Hibernate 5 is expected. If you use Hibernate 5.2, use `mvn -P wildfly package -Dversion.integration-hibernate=5.2`. 
If you want to deploy to an application server like GlassFish that uses EclipseLink as JPA provider use `mvn -P glassfish package`.

After deployment navigate to http://localhost:8080/index.html where you will find a data table.
You can switch between the *Cats* and *Cat-Views* mode which use the HTTP endpoints `cats` and `cat-views` respectively.

The `cats` endpoint retrieves and returns normal entities whereas the `cat-views` endpoint retrieves and returns entity views.
In the SQL output you can see the difference. When querying the `cat-views` endpoint, the SQL queries don't contain the *age* column in the select list.

You can see keyset pagination in action in both modes. Instead of using an offset, the last retrieved elements are remembered.
When switching to the next or previous page, the last known upper or lower bound of the page are used for querying.
In short, that's what keyset pagination is all about. Using the values of the attributes you sort by of the upper and lower bounds of a result, to query the next or previous page.
You can take a look at the generated queries to further understand what happens or you take a look into the [documentation](https://persistence.blazebit.com/documentation/core/manual/en_US/index.html#anchor-keyset-pagination) for more information on the topic.