<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title} | </#escape></#if>Blaze-Persistence</title>
    <base href="${config.site_host}">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <meta name="description" content="<#if (content.description)??><#escape x as x?xml>${content.description}</#escape></#if>">
    <meta name="keywords" content="<#if (content.keywords)??><#escape x as x?xml>${content.keywords}</#escape></#if>">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/asciidoctor.css" rel="stylesheet">
    <link href="css/base.css" rel="stylesheet">
    <link href="css/prettify.min.css" rel="stylesheet">
    <link rel="alternate" type="application/rss+xml" title="Blaze-Persistence News" href="feed.xml" />

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="js/html5shiv.min.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png">-->
    <link rel="shortcut icon" href="images/favicon.png">
    
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/minified.css">
    
    <link href='//fonts.googleapis.com/css?family=Montserrat:400,700' rel='stylesheet' type='text/css'>
    <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">
    <!--
    <meta name="google-site-verification" content="_Xlwf-Ro-n7nGJpq-_2VrRV8NGrLv-_pYKUSk_sZVIE" />
    -->
  </head>
  <body onload="prettyPrint()">
   