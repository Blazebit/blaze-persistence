Doing a release
==========

A release involves various steps which are outlined here and should be kept up-to-date

. Make sure nobody pushes to the master during a release e.g. announce that you do a release
. Make sure you have GPG installed and the executable is available on PATH
. Make sure your Maven settings.xml has credentials for the server `sonatype-nexus-staging` configured
. Make sure your Maven settings.xml has a profile called `blazebit-release` with the property `gpg.passphrase`
. Make sure your `JAVA_HOME` points to a JDK 8 with e.g. `set JAVA_HOME="C:\Program Files\Java\jdk-9.0.4"`
. Make sure your `MAVEN_OPTS` contain a memory configuration with a big heap size with e.g. `set MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"`
. Edit the `README.md` and update the property `blaze-persistence.version` to the latest released version, also update the archetype versions for the quickstarts
. Make sure `CHANGELOG.md` contains all relevant changes and links to the release tags in GitHub, prepare the next changelog entry for the next version
. Check the `roadmap.asciidoc` and add big ticket features that might have been added during development but forgotten to be included there
. Open `website/src/main/jbake/content/downloads.adoc` and update the releases table for the released branch
. Create `website/src/main/jbake/content/news/CURRENT_YEAR/blaze-persistence-VERSION-release.adoc` that contains a list of fixed issues that are interesting i.e. no issues that were created while fixing other issues or documentation issues
. Open `website/src/main/jbake/jbake.properties` and update `stable.version` to the latest released version
. Open `website/pom.xml` and update the property `stable.version` to the latest released version, `snapshot.version` to the latest snapshot version and `series.version` to the current version series
. Open `documentation/pom.xml` and update the property `stable.version` to the latest released version and `series.version` to the current version series
. Prepare a local Maven release via `mvn "-Pblazebit-release" release:clean release:prepare` and skip the tests with `"-Darguments=-DskipTests"` if you know they work
. Actually deploy the release with `mvn "-Pblazebit-release" release:perform "-Darguments=-DskipTests"`
. Goto https://oss.sonatype.org and login. In *Build Promotion* click on *Staging Repositories* then scroll down and find a repository named *comblazebit-...*
. Click on the repository, then click *Close* and *Confirm*. Wait a few seconds, click *Refresh* and finally click *Release* and *Confirm*
. Commit the changes and push the branch `git push origin`, as well as the created tag `git push origin TAG`
. Create a GitHub release from the tag and use the same content as in `website/src/main/jbake/content/news/CURRENT_YEAR/blaze-persistence-VERSION-release.adoc` and add the _tar.gz_ and _zip_ artifacts of `blaze-persistence-distribution` as binaries to the release
. Push the new website changes only if you are working on the lastest version series and first to the staging server by invoking `./build-deploy-website.sh staging` and if everything is alright push to production with `./build-deploy-website.sh prod`
. If you want to push just the documentation changes use `./build-deploy-documentation.sh staging` and if everything is alright push to production with `./build-deploy-documentation.sh prod` instead
. Create tweet about new version