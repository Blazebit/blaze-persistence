<#include "header.ftl">
    
    <#include "menu.ftl">
    <#setting locale="en_US">
    
    <section id="page-intro" class="bTop clearfix">
        <h2><#escape x as x?xml>${content.title}</#escape></h2>
        
        <p class="tCenter">
            ${content.description}
        </p>
    </section>

    <section id="main-content" class="clearfix">
        <p>By ${content.author} on <em>${content.date?string("dd MMMM yyyy")}</em></p>

        <p>${content.body}</p>
    </section>
    <section id="tags" class="clearfix">
        <#list content.tags as x >
        <a href="tags/${x}.html" class="background-color bg-hover-color">${x}</a>
        </#list>
    </section>
    
    <section id="comments" class="clearfix">
        <div id="disqus_thread"></div>
        <script type="text/javascript">
            if( window.location.hostname == 'localhost' || window.location.hostname == '127.0.0.1' ) { throw new Error('Skip Disqus on localhost') };
            /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
            var disqus_shortname = 'blaze-persistence'; // required: replace example with your forum shortname
            /* * * DON'T EDIT BELOW THIS LINE * * */
            (function() {
                var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
                dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
                (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
            })();
        </script>
        <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
        <a href="http://disqus.com" class="dsq-brlink">comments powered by <span class="logo-disqus">Disqus</span></a>
    
    </section>
    
<#include "footer.ftl">