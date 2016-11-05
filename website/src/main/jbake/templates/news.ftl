<#include "header.ftl">
    
    <#assign content = { "rootpath" : content.rootpath , "page": "news" }>
    <#include "menu.ftl">
    <#setting locale="en_US">
    
    <section id="page-intro" class="bTop clearfix">
        <h2>Blaze-Persistence News</h2>
        
        <p class="tCenter">
            Stories, news and announcements regarding Blaze-Persistence
        </p>
    </section>

    <section id="main-content" class="clearfix">
    <#list posts as post>
          <#if (post.status == "published")>
            <#if (post.icon)??>
            <div class="head-image">
                <img height="95" width="95" src="images/${post.icon}" alt="Picture of ${post.author}" />
            </div>
            </#if>
              <a href="${post.uri}"><h3><#escape x as x?xml>${post.title}</#escape></h3></a>
              <p>${post.date?string("dd MMMM yyyy")}</p>
              <p>
                <#if post.body?contains("<!-- PREVIEW-SUFFIX -->")>
                    <#assign preview = post.body?keep_before("<!-- PREVIEW-SUFFIX -->")>
                    <#assign previewSuffix = post.body?keep_after("<!-- PREVIEW-SUFFIX --><!-- ")?keep_before(" --><!-- PREVIEW-END -->")>
                    ${preview} ..
                    ${previewSuffix}
                    (<a href="${post.uri}">click here to read more</a>)
                <#else>
                    <#assign words = post.body?word_list>
                    <#if words?size gt 150 >
                        <#assign body = words[0..149]?join(' ') >
                        ${body} .. (<a href="${post.uri}">click here to read more</a>)
                    <#else>
                        <#assign body = words?join(' ') >
                        ${body}
                    </#if>
                </#if>
            </p>
          </#if>
      </#list>
    </section>
    
    <hr />
    
    <p>Older posts are available in the <a href="${content.rootpath}${config.archive_file}">archive</a>.</p>

<#include "footer.ftl">