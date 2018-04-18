/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestObjects;

import com.hanseltime.loosebeans.annotations.BeanGetter;
import com.hanseltime.loosebeans.annotations.BeanProperty;
import com.hanseltime.loosebeans.annotations.BeanSetter;
import com.hanseltime.loosebeans.bean.LooseBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

/**
 *  Test Implementation of a Class that Inherits its own 
 * 
 * @author HanseltimeIndustries
 */
public class TestBeanWithSuperLooseBeanInfo extends LooseBeanInfo< TestBeanWithSuperLooseBeanInfo > implements BeanInfoAnticipatedReturns {
    
    @BeanProperty( name = "vA")
    private int valueA_;
    @BeanProperty( name = "vB")
    private Object valueB_;
    
    public TestBeanWithSuperLooseBeanInfo() {
        super();
    }
    
    @BeanGetter( name = "vA" )
    public int BadWordedGetValueA() {
        return valueA_;
    }
    
    @BeanSetter( name = "vB")
    public void ValBSetter( Object val ) {
        valueB_ = val;
        //return 12;
    }

    @Override
    public PropertyDescriptor[] getAnticipatedPropertyDescriptors() {
        try {
        return new PropertyDescriptor[]{ new PropertyDescriptor("valueA_", this.getClass(), "BadWordedGetValueA", null),
                                          new PropertyDescriptor("valueB_", this.getClass(), null, "ValBSetter")  };
        } catch ( IntrospectionException ex  ) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public BeanDescriptor getAnticipatedBeanDescriptor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MethodDescriptor[] getAnticipatedMethodDescriptor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
