<?xml version="1.0"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
    <channel>
        <title>Blaze-Persistence News</title>
        <link>${config.site_host}</link>
        <atom:link href="${config.site_host}${config.feed_file}" rel="self" type="application/rss+xml" />
        <description>Stories, news and announcements regarding Blaze-Persistence</description>
        <language>en-US</language>
        <pubDate>${published_date?string("EEE, d MMM yyyy HH:mm:ss Z")}</pubDate>
        <lastBuildDate>${published_date?string("EEE, d MMM yyyy HH:mm:ss Z")}</lastBuildDate>

    <#list published_posts as post>
        <item>
            <title><#escape x as x?xml>${post.title}</#escape></title>
            <link>${config.site_host}${post.uri}</link>
            <pubDate>${post.date?string("EEE, d MMM yyyy HH:mm:ss Z")}</pubDate>
            <guid isPermaLink="false">${post.uri}</guid>
            <author>${post.author}</author>
            <description>
              <#if post.body?contains("<!-- PREVIEW-SUFFIX -->")>
                <#assign preview = post.body?keep_before("<!-- PREVIEW-SUFFIX -->")>
                <#assign previewSuffix = post.body?keep_after("<!-- PREVIEW-SUFFIX -->")?trim?keep_after("<!--")?keep_before("-->")>
                <#escape x as x?xml>
                ${preview} ..
                ${previewSuffix}
                </#escape>
              <#else>
                <#escape x as x?xml>
                ${post.body}
                </#escape>
              </#if>
            </description>
        </item>
    </#list>

    </channel>
</rss>
