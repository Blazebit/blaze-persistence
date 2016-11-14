    <div id="header-wrapper" class="clearfix <#if content.page?? && content.page == 'index'>index<#else>small</#if>">
        <header>
            <div id="top-wrapper">
                <div class="inner">
                    <h1 id="logo">
                        <a href="index.html">
                            <img width="90" src="images/blaze_persistence_logo_white_render.png" alt="Blaze-Persistence Logo" />
                        </a>
                    </h1>
                    <nav>
                        <ul>
                            <li<#if content.page?? && content.page == 'news'> class="current"</#if>>
                                <a href="news.html">News</a>
                            </li>
                            <li<#if content.page?? && content.page == 'downloads'> class="current"</#if>>
                                <a href="downloads.html">Downloads</a>
                            </li>
                            <li<#if content.page?? && content.page == 'documentation'> class="current"</#if>>
                                <a href="documentation.html">Documentation</a>
                            </li>
                            <li>
                                <a href="https://blazebit.com/blog.html" target="_blank">Blog<span class="fa fa-external-link" style="width: 16px; margin-left: 2px"></span></a>
                            </li>
                            <li>
                                <a href="https://github.com/Blazebit/blaze-persistence" target="_blank">Source Code<span class="fa fa-external-link" style="width: 16px; margin-left: 2px"></span></a>
                            </li>
                            <li<#if content.page?? && content.page == 'community'> class="current"</#if>>
                                <a href="community.html">Community</a>
                            </li>
                            <li<#if content.page?? && content.page == 'support'> class="current"</#if>>
                                <a href="support.html">Support</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </div>
            <!--<img id="teaser" height="155" width="1080" src="/images/index_teaser.png" alt="Wir entwickeln Websites" />-->
            <!--<canvas style="margin-top: 80px;" id="flipHeadline" width="1080" height="155"></canvas>-->

            <#if (content.bigheader)??>
            <h1 id="headline">
                <span class="first-word">Blaze-Persistence</span>
                <span class="last-word">
                    Tomorrow's JPA, today
                    <!-- Tomorrows JPA, today -->
                    <!-- JPA Criteria done right -->
                </span>
                <span class="links">
                    <a class="button" href="https://github.com/Blazebit/blaze-persistence#core-quick-start">Get started</a>
                    <a class="button" href="downloads.html">Download (${config.stable_version})</a>
                </span>
            </h1>
            </#if>
        </header>
    </div>
    <div id="content-wrapper" class="clearfix">