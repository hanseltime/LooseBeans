/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestObjects;

import com.hanseltime.loosebeans.annotations.BeanGetter;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bean that implements a getter for a protected method in the super class and therefore influences the
 * lower level property.
 * 
 * @author HanseltimeIndustries
 */
public class TestSubClassLooseBean extends TestLooseBean {
 
    public TestSubClassLooseBean() {
        super();
    }
    
    @BeanGetter( name = "vB" )
    public Object PoorlyNameValueBGetter() {
        return this.readValueB();
    }
    
    @Override
    public PropertyDescriptor[] getAnticipatedPropertyDescriptors() {
        List<PropertyDescriptor> pList = new ArrayList<PropertyDescriptor>();
        try {
            pList.add( new PropertyDescriptor( "valueB_", this.getClass(), "PoorlyNameValueBGetter", null) );
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        for( PropertyDescriptor pd : super.getAnticipatedPropertyDescriptors() ) {
           pList.add(pd);
        }
        return pList.toArray( new PropertyDescriptor[pList.size()]);
    }
    
}
