<#include "header.ftl">
<#include "menu.ftl">

<section id="page-intro" class="bTop clearfix">
    <h2>Downloads</h2>

    <p class="tCenter">
        <a class="button" href="https://github.com/Blazebit/blaze-persistence/releases/download/${config.stable_version}/blaze-persistence-dist-${config.stable_version}.zip">Download (${config.stable_version})</a>
    </p>
</section>

<section id="main-content" class="clearfix">
    <section id="text-content">
        ${content.body}
    </section>
</section>

<#include "footer.ftl">