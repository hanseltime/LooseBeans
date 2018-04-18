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
 * Annotation for explicitly identifying Bean Fields that will be coupled to certain Bean Methods.
 * 
 * @author Justin Hanselman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.FIELD )
@Documented
public @interface BeanProperty {
    
    /**
     * Returns the semantic name by which a given Bean Field is annotated. 
     * <p>
     * In this way, the semantic name will be used for lookups of getters and 
     * setters for the property annotated with this name.
     * 
     * @return The semantic name or token for looking up getters and setters
     */
    public String name() default "";
    
}
