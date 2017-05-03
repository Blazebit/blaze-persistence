<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title} | </#escape></#if>Blaze-Persistence</title>
    <base href="${config.site_host}" />
    <meta name="viewport"            content="width=device-width,initial-scale=1" />
    <meta name="description"         content="<#if (content.description)??><#escape x as x?xml>${content.description}</#escape></#if>" />
    <meta name="keywords"            content="<#if (content.keywords)??><#escape x as x?xml>${content.keywords}</#escape></#if>" />
    <meta name="author"              content="<#if (content.author)??><#escape x as x?xml>${content.author}</#escape></#if>" />

    <#if content.type == "post">
    <#if content.author == "Christian Beikov">
    <meta property="article:author"  content="https://facebook.com/blazebit.beikov" />
    <meta name="twitter:creator"     content="@c_beikov" />
    <#elseif content.author == "Moritz Becker">
    <meta property="article:author"  content="https://facebook.com/moritz.becker.1232" />
    <meta name="twitter:creator"     content="@mobecker91" />
    </#if>

    <meta property="og:url"          content="${config.site_host}${content.uri}" />
    <meta name="twitter:url"         content="${config.site_host}${content.uri}" />

    <meta property="og:type"         content="article" />
    <meta name="twitter:card"        content="summary_large_image" />
    <meta name="twitter:site"        content="@Blazebit" />

    <meta property="og:title"        content="<#escape x as x?xml>${content.title}</#escape>" />
    <meta name="twitter:title"       content="<#escape x as x?xml>${content.title}</#escape>" />

    <meta property="og:description"  content="<#escape x as x?xml>${content.description}</#escape>" />
    <meta name="twitter:description" content="<#escape x as x?xml>${content.description}</#escape>" />

    <#if (content.image)??>
    <meta property="og:image"        content="${config.site_host}images/${content.image}" />
    <meta name="twitter:image"       content="${config.site_host}images/${content.image}" />
    <#elseif (content.extimage)??>
    <meta property="og:image"        content="${content.extimage}" />
    <meta name="twitter:image"       content="${content.extimage}" />
    <#else>
    <meta property="og:image"        content="${config.site_host}images/blaze_persistence_square.png" />
    <meta name="twitter:image"       content="${config.site_host}images/blaze_persistence_square.png" />
    </#if>
    </#if>

    <!-- Le styles -->
    <link href="css/bootstrap.min.css" rel="stylesheet" />
    <link href="css/asciidoctor.css" rel="stylesheet" />
    <link href="css/base.css" rel="stylesheet" />
    <link href="css/prettify.min.css" rel="stylesheet" />
    <link rel="alternate" type="application/rss+xml" title="Blaze-Persistence News" href="feed.xml" />

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="js/html5shiv.min.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png" />
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png" />
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png" />
    <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png" />-->
    <link rel="shortcut icon" href="images/favicon.png" />
    
    <link rel="stylesheet" href="css/style.css" />
    <link rel="stylesheet" href="css/minified.css" />
    
    <link href='//fonts.googleapis.com/css?family=Montserrat:400,700' rel='stylesheet' type='text/css' />
    <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet" />

    <!-- Global site tag (gtag.js) - Google Analytics -->
    <script async src="https://www.googletagmanager.com/gtag/js?id=UA-108280807-1"></script>
    <script>
        window.dataLayer = window.dataLayer || [];
        function gtag(){dataLayer.push(arguments);}
        gtag('js', new Date());

        gtag('config', 'UA-108280807-1');
    </script>
  </head>
  <body onload="prettyPrint()">
   