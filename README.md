# Maven Properties Loader Plugin #

This is a plugin which aims to load properties from file and let you use it during configuration (bound to phase INITIALIZE by default).

Loaded properties are passed to maven surefire as well. Test class could use them by the System.getProperty() call.

As a bonus a string packing properties to one string is created. It looks in a way -Dname=value. This string property is supposed to be used in arquillian-jboss-container property javaVmArguments.

## How to use it ##

Currently there is no plugin repo with this plugin so you can do it by hacky way like (inspired by http://stackoverflow.com/questions/2065928/maven-2-assembly-with-dependencies-jar-under-scope-system-not-included)

    rm -rf ~/.m2/repository/org/jboss/qa/load-to-system-properties-maven-plugin/
    mvn clean package
    mvn install:install-file -Dfile=target/load-to-system-properties-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=org.jboss.qa -DartifactId=load-to-system-properties-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=maven-plugin -DlocalRepositoryPath=<extern_path>

Then in you project you would need to specify local maven repo:

    <pluginRepositories>
      <pluginRepository>
        <id>project-internal-lib-repo</id>
        <url>file://<extern_path></url>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
      </pluginRepository>
    </pluginRepositories>

And then use it like:

     <plugin>
       <groupId>org.jboss.qa</groupId>
       <artifactId>load-to-system-properties-maven-plugin</artifactId>
       <version>1.0-SNAPSHOT</version>
       <executions>
         <execution>
           <configuration>
             <files>
               <file>prop.properties</file>
             </files>
           </configuration>
           <goals>
             <goal>load-properties</goal>
           </goals>
         </execution>
       </executions>
     </plugin>

And for arquillian.xml you can add something like:

    <container qualifier="JBOSS_AS_MANAGED_7.X">
      <protocol type="jmx-as7" />
      <configuration>
        <property name="javaVmArguments">${loaded.properties:}</property>
      </configuration>
    </container>
