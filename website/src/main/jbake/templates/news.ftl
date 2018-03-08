<#include "header.ftl">
    
    <#assign content = { "rootpath" : content.rootpath , "page": "news" }>
    <#include "menu.ftl">
    <#setting locale="en_US">
    
    <section class="bTop clearfix">
        <h2>Blaze-Persistence News</h2>
        
        <p class="tCenter">
            Stories, news and announcements regarding Blaze-Persistence
        </p>
    </section>

    <section id="main-content" class="clearfix">
    <#list posts as post>
          <#if (post.status == "published")>
            <div class="news-post">
                <#if (post.icon)??>
                <div class="head-image">
                    <img height="95" width="95" src="images/${post.icon}" alt="Picture of ${post.author}" />
                </div>
                </#if>
                  <a href="${post.uri}" class="post-title"><h3><#escape x as x?xml>${post.title}</#escape></h3></a>
                  <p class="post-date">${post.date?string("dd MMMM yyyy")}</p>
                  <div class="post-content">
                        <#if post.body?contains("<!-- PREVIEW-SUFFIX -->")>
                            <#assign preview = post.body?keep_before("<!-- PREVIEW-SUFFIX -->")>
                            <#assign previewSuffix = post.body?keep_after("<!-- PREVIEW-SUFFIX -->")?trim?keep_after("<!--")?keep_before("-->")>
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
                </div>
            </div>
          </#if>
      </#list>
    </section>
    
    <hr />
    
    <p>Older posts are available in the <a href="${content.rootpath}${config.archive_file}">archive</a>.</p>

<#include "footer.ftl">