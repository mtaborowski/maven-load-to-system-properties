mvn install:install-file -Dfile=target/load-to-system-properties-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=org.jboss.qa -DartifactId=load-to-system-properties-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=$HOME/my-testing/datasources-test/lib
rm -rf ~/.m2/repository/org/jboss/qa/load-to-system-properties-maven-plugin/
mvn install:install-file -Dfile=target/load-to-system-properties-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=org.jboss.qa -DartifactId=load-to-system-properties-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=maven-plugin -DlocalRepositoryPath=$HOME/my-testing/datasources-test/lib