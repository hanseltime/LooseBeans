/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hanseltime.loosebeans.bean;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Due to some releases not having the com.sun.beans.finder package available,
 * this class will act as a utility class so that the same functionality can be 
 * exposed.
 * 
 * @author Justin Hanselman
 */
public class ClassFinderUtil {

    /**
     * Attempts to find a class based off of the ClassLoader provided and the name of the class.
     * <p>
     * If this fails, the ClassLoader will default to the context and then to the current to
     * see if the name can be loaded through any of those.
     * 
     * @param name name of the class, equivalent to what would be used with {@link Class#forName(java.lang.String) }
     * @param loadContext The ClassLoader for which this name occurs
     * @return The Class that was loaded
     * @throws ClassNotFoundException If the name does not result in a valid class being loaded
     */
    public static Class findClass(String name, ClassLoader loadContext) throws ClassNotFoundException {
        if (loadContext != null) {
            try {
                return Class.forName(name, false, loadContext );
            } catch (ClassNotFoundException exception) {
                //PassThrough and try default classloader
            } catch (SecurityException exception) {
                //PassThrough and try default classloader
            }
        }
        
        //Look in current contexts
        return findClass(name);

    }
    
    
    /**
     * Attempts to load the class from a string according to the current context ClassLoader
     * and current ClassLoader if that fails.
     * 
     * @param name
     * @return 
     */
    public static Class findClass( String name ) throws ClassNotFoundException {
        try {
            ClassLoader curLoader = Thread.currentThread().getContextClassLoader();
            if (curLoader == null) {
                //IE in 1.5 had an issue (issue 6204697)
                curLoader = ClassLoader.getSystemClassLoader();
            }
            if (curLoader != null) {
                return Class.forName(name, false, curLoader);
            }
        } catch (ClassNotFoundException ex) {
            //PassThrough and try current classloader
        } catch (SecurityException exception) {
            //PassThrough and try current classloader
        }
        
        return Class.forName(name);
    }

}