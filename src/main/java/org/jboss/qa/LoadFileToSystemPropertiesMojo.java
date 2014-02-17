package org.jboss.qa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The main purpose of this plugin is ability to load properties
 * from file, being able to handle with them in maven configuration
 * and pass them to maven surefire plugin. Then to have a simple 
 * variable string which could be possible to pass to arquillian 
 * as vm argument (javaVmArguments - used in jboss as container).
 * 
 * Simple plugin which takes property files and converts them
 * to maven project properties which could be used by project later on.
 * 
 * It's done in the way that these properties should be passed as system
 * properties to surefire as well.
 * 
 * The Plugin creates a property which would contains all the loaded properties
 * in the way of -DpropName=propValue
 * 
 * Note: check the configuration option <systemPropertiesFile></systemPropertiesFile>
 * of maven surefire plugin where you can pass system properties from file as well.
 *  
 * @author Ondrej Chaloupka <ochaloup@redhat.com>
 */
@Mojo(name = "load-properties", defaultPhase = LifecyclePhase.INITIALIZE)
public class LoadFileToSystemPropertiesMojo extends AbstractMojo {
  private static final String ALL_PROPERTIES_NO_SPACE_SUFFIX = ".nospace"; 
  
  /**
   * Need for being able to fill data to maven project properties.
   */
  @Component
  private MavenProject project;
  
  /**
   * Maven session - used for passing properties to maven surefire.
   */
  @Component
  private MavenSession session;

  /**
   * Files that will be search for loading properties.
   */
  @Parameter(required = true)
  private File[] files;
  
  /**
   * Name of system parameter where all properties will be put 
   * in as -DpropName=propValue string.
   * There will be createad one more property with name ${allPropertiesName}.nospace
   * which that changes all spaces to underscore character '_'.
   */
  @Parameter(defaultValue = "loaded.properties")
  private String allPropertiesName;
  

  public void execute() throws MojoExecutionException {
    getLog().info("Starting loading properties from files");
    
    StringBuffer allPropertiesString = new StringBuffer();
    // There is a problem of arquillian-container-jboss does parsing the properties
    // by simple splitting by space - this is a workaround
    // @see org.jboss.arquillian.container.jbossas.managed_6.JBossASConfiguration#getJavaVmArguments
    // plus check ALL_PROPERTIES_NO_SPACE_SUFFIX
    StringBuffer allPropertiesWithoutSpacesString = new StringBuffer();
    
    for(File file: files) {
      if(file.exists()) {
        getLog().info("Loading data from file " + file);

        FileInputStream fis = null;
        try {
          Properties loadedProperties = new Properties();
          fis = new FileInputStream(file);
          loadedProperties.load(fis);
          
          for(String loadedPropName: loadedProperties.stringPropertyNames()) {
            String loadedPropValue = loadedProperties.getProperty(loadedPropName);
            getLog().debug("Loading property " + loadedPropName + "=" + loadedPropValue);
            
            // This seems to be for nothing
            System.setProperty(loadedPropName, loadedPropValue);
            // Project would be able to handle with properties
            project.getProperties().setProperty(loadedPropName, loadedPropValue);
            // This is a bit hacky way how to force maven surefire to use these properties
            // see org.apache.maven.plugin.surefire.AbstractSurefireMojo#getUserProperties
            // method available from maven 2.2
            session.getUserProperties().setProperty(loadedPropName, loadedPropValue);
            
            // Filling property string
            allPropertiesString.append(" -D" + loadedPropName + "=" + loadedPropValue);
            allPropertiesWithoutSpacesString.append(" -D" + loadedPropName + "=" + loadedPropValue.replace(" ", "_"));
          }          
          
        } catch (IOException ex) {
          getLog().error("Could not open properties file " + file);
        } finally {
          try {
            fis.close();
          } catch (IOException e) {
            getLog().debug("Can't close input stream for property file " + file);
          }
        }
        
      } else {
        getLog().info("Data from file " + file + " was not loaded as file does not exist.");
      }
    }
    
    // setting string -Dname=value to system properties
    if(allPropertiesString.length() > 0) {
      getLog().info("Setting " + allPropertiesName + "=" + allPropertiesString.toString().trim() + ". There is as well property with name " + 
          allPropertiesName + ALL_PROPERTIES_NO_SPACE_SUFFIX + " created");
      
      // Setting all properties string
      System.setProperty(allPropertiesName, allPropertiesString.toString().trim());
      project.getProperties().setProperty(allPropertiesName, allPropertiesString.toString().trim());
      session.getUserProperties().setProperty(allPropertiesName, allPropertiesString.toString().trim());
      // All properties string without spaces
      System.setProperty(allPropertiesName + ALL_PROPERTIES_NO_SPACE_SUFFIX, allPropertiesWithoutSpacesString.toString().trim());
      project.getProperties().setProperty(allPropertiesName + ALL_PROPERTIES_NO_SPACE_SUFFIX, allPropertiesWithoutSpacesString.toString().trim());
      session.getUserProperties().setProperty(allPropertiesName + ALL_PROPERTIES_NO_SPACE_SUFFIX, allPropertiesWithoutSpacesString.toString().trim());
    }
    
  }
  
}
