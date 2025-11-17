@echo off
set default_project_path=%~dp0
set default_jdk_path=C:\Program Files\Java\jdk-17

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
./mvnw -P "blazebit-release,h2,hibernate-6.6,deltaspike-2.0,spring-data-3.3.x" -f %project_path%\pom.xml install deploy:deploy -DskipTests -DskipITs -DaltDeploymentRepository=blazebit::default::https://nexus.blazebit.com/repository/maven-releases/ %*
