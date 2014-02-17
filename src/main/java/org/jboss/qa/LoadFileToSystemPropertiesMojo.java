package org.jboss.qa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Simple plugin which takes property files and converts them
 * to system properties which could be used by project later on.
 *  
 * @author Ondrej Chaloupka <ochaloup@redhat.com>
 */
@Mojo(name = "loadProperties", defaultPhase = LifecyclePhase.INITIALIZE)
public class LoadFileToSystemPropertiesMojo extends AbstractMojo {
   
  /**
   * Need for being able to fill data to maven project properties.
   */
  @Parameter(required = true)
  private MavenProject project;

  /**
   * Files that will be search for loading properties.
   */
  @Parameter(required = true)
  private File[] files;
  
  /**
   * Name of system parameter where all properties will be put 
   * in as -DpropName=propValue string.
   */
  @Parameter(defaultValue = "loaded.properties")
  private String allPropertiesName;
  
  /**
   * Saying whether the properties should be pass to project properties as well.
   * Otherwise just system properties will be filled.
   */
  @Parameter
  private Boolean fillProjectProperty;

  public void execute() throws MojoExecutionException {
    getLog().info("Starting loading properties from files");
    
    StringBuffer allPropertiesString = new StringBuffer();
    
    for(File file: files) {
      if(file.exists()) {
        getLog().info("Loading data from file " + file);

        FileInputStream fis = null;
        try {
          Properties loadedProperties = new Properties();
          fis = new FileInputStream(file);
          loadedProperties.load(fis);
          
          // System.setProperties(loadedProperties);
          for(String loadedPropName: loadedProperties.stringPropertyNames()) {
            System.setProperty(loadedPropName, loadedProperties.getProperty(loadedPropName));
            allPropertiesString.append(" -D" + loadedPropName + "=" + loadedProperties.getProperty(loadedPropName));
            
            if(fillProjectProperty) {
              project.getProperties().setProperty(loadedPropName, loadedProperties.getProperty(loadedPropName));
            }
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
      System.setProperty(allPropertiesName, allPropertiesString.toString());
      if(fillProjectProperty) {
        project.getProperties().setProperty(allPropertiesName, allPropertiesString.toString());
      }
    }
    
  }
  
}
