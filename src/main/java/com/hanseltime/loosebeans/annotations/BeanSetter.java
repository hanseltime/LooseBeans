/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hanseltime.loosebeans.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to indicate a certain method is associated as the setter
 * for a {@link BeanProperty} with the same semantic name for the given class and its super classes.
 * <p>
 * This Annotation can be applied to both IndexSetter Methods and normal setter methods.
 * <p>
 * Note: It remains to be determined how to handle BeanGetters that override.
 * 
 * @author Justin Hanselman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.METHOD )
@Documented
public @interface BeanSetter {
    
     /**
     * Returns the semantic name that this method is associated with.
     * <p>
     * In this way, the semantic name will be used for lookups of the field that 
     * this method annotates.
     * 
     * @return The semantic name or token for looking up getters and setters
     */
    public String name() default "";
    
}
