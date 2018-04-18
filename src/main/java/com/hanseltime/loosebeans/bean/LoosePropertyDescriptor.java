/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hanseltime.loosebeans.bean;

/**
 * 
 * SubClass of PropertyDescriptor that corrects for setters and getters with alternative function.
 * <p>
 * This is provided because the current PropertyDescriptor assumes that a setter function must have 
 * a void return type.  However, with certain implementations extraneous data may be returned upon setting 
 * via an integer, etc.
 * <p>
 * This class can only be determined by explicit annotations since it is now the burden of the programmer to specify the explicit
 * getter and setter.
 * 
 * TO BE FINISHED
 * 
 * @author HanseltimeIndustries
 */
/*public class LoosePropertyDescriptor extends PropertyDescriptor {
    
    LoosePropertyDescriptor( ) {
        
    }
    
}*/
