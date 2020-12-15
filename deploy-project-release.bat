@echo off
set default_project_path=%~dp0
set default_jdk_path=C:\Program Files\Java\jdk-14

set /p project_path="Path to project [%default_project_path%]: "

if "%project_path%" == "" (
	set project_path=%default_project_path%
)

set /p jdk_path="Path to JDK [%default_jdk_path%]: "

if "%jdk_path%" == "" (
	set jdk_path=%default_jdk_path%
)
set JAVA_HOME=%jdk_path%
echo "Using JAVA_HOME=%JAVA_HOME%"
set MAVEN_OPTS="-Xmx1024m"
./mvnw -P "blazebit-release,deltaspike-1.7" -f %project_path%\pom.xml install deploy:deploy -DskipTests -DskipITs "-Djdk8.home=C:\Program Files\Java\jdk1.8.0_181" -DaltDeploymentRepository=blazebit::default::https://nexus.blazebit.com/repository/maven-releases/ %*