package TestObjects;


import java.beans.BeanDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Interface for TestClasses to implement in order to demonstrate their anticipated 
 * results from a BeanInfo Object.
 * 
 * @author HanseltimeIndustries
 */
public interface BeanInfoAnticipatedReturns {
    
    public PropertyDescriptor[] getAnticipatedPropertyDescriptors();
    
    public BeanDescriptor getAnticipatedBeanDescriptor();
    
    public MethodDescriptor[] getAnticipatedMethodDescriptor();
    
}
