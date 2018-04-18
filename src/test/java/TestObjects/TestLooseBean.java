package TestObjects;

import com.hanseltime.loosebeans.bean.LooseBeanInfo;
import com.hanseltime.loosebeans.annotations.BeanGetter;
import com.hanseltime.loosebeans.annotations.BeanProperty;
import com.hanseltime.loosebeans.annotations.BeanSetter;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *  Test Loose Coupled Bean Using Annotations
 * 
 * @author HanseltimeIndustries
 */
public class TestLooseBean implements BeanInfoAnticipatedReturns {
    
    @BeanProperty( name = "vA" )
    private int valueA_ = 9;
    @BeanProperty( name = "vB" )
    private Object valueB_ = 0;
    
    public TestLooseBean() {
        
    }
    
    @BeanSetter( name = "vA" )
    public void setArbitraryAName( int a ) {
        valueA_ = a;
    }
    
    @BeanGetter( name = "vA")
    public int getArbitratyAName() {
        return valueA_;
    }

    protected Object readValueB() {
        return valueB_;
    }
    
    @Override
    public PropertyDescriptor[] getAnticipatedPropertyDescriptors() {
        try {
        return new PropertyDescriptor[]{ new PropertyDescriptor("valueA_", this.getClass(), "getArbitratyAName", "setArbitraryAName" ) };
        } catch (IntrospectionException ex ) {
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
