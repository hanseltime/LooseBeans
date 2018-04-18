/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hanseltime.loosebeans.bean;

import TestObjects.BeanInfoAnticipatedReturns;
import TestObjects.TestBeanWithSuperLooseBeanInfo;
import TestObjects.TestLooseBean;
import TestObjects.TestLooseBeanBeanInfo;
import TestObjects.TestSubClassLooseBean;
import TestObjects.TestSubClassLooseBeanBeanInfo;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import junit.framework.Assert;
import org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author HanseltimeIndustries
 */
public class LooseBeanInfoNGTest {
    
    
    public LooseBeanInfoNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @DataProvider(name = "beanToBeanInfo")
    public Object[][] createData1() {
        return new Object[][]{
            //Single Layer LooseBean and BeanInfo
            {new TestLooseBean() , TestLooseBeanBeanInfo.class},
            {new TestSubClassLooseBean(), TestSubClassLooseBeanBeanInfo.class},
            {new TestBeanWithSuperLooseBeanInfo(), TestBeanWithSuperLooseBeanInfo.class},
        };
    }
    
    @DataProvider( name = "beansForIntrospection")
    public Object[] createData2() {
        return new Object[]{
            new TestLooseBean(),
            new TestSubClassLooseBean(),
            //new TestBeanWithSuperLooseBeanInfo(),
        };
    }

   /* @DataProvider( name = "LooseBeanInfoClazz") 
    public Object[] createData3() {
        return new Object[] {
            TestLooseBeanBeanInfo.class,
            TestSubClassLooseBeanBeanInfo.class,
            TestBeanWithSuperLooseBeanInfo.class,
        };
    }*/
    
    /**
     * Should test Nullary constructor of the loose bean against the LooseBeanInfo object that should be created from it.
     * <p>
     * Constructs the LooseBeanInfo Object and compares it against the beanTester anticipated values
     * 
     * 
     * @param beanTester - The Beantester that the beanInfo Object corresponds too
     * @param lBeanInfo - The BeanInfo Class that will be invoked via newInstanct()
     */
    @Test( dataProvider = "beanToBeanInfo")
    public void TestLooseBeanInfoNullaryConstructor( BeanInfoAnticipatedReturns beanTester, Class<LooseBeanInfo> lBeanInfo ) {
        BeanInfo bInfo = null;
        try {
            bInfo = lBeanInfo.newInstance();
        } catch( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }

        if( bInfo != null ) {
            //Generate a cache for comparison
            HashMap< String, PropertyDescriptor > resMap = new HashMap<String, PropertyDescriptor>();
            PropertyDescriptor[] pds = bInfo.getPropertyDescriptors();
            for( PropertyDescriptor pd : pds ) {
                resMap.put( pd.getName(), pd);
            }
            //Comparison Feature
            for( PropertyDescriptor pd : beanTester.getAnticipatedPropertyDescriptors() ) {
                PropertyDescriptor resPd = resMap.get( pd.getName() );
                if( resMap.get( pd.getName() ) == null ) {
                    Assert.fail("Property of name " + pd.getName() + " was expected by not returned by LooseBeanInfo" );
                } 
                Method readM = pd.getReadMethod();
                if (readM != null) {
                    if (!readM.equals(resPd.getReadMethod())) {
                        Assert.fail("Read Method did not match for value " + pd.getName());
                    }
                } else if (resPd.getReadMethod() != null) {
                    Assert.fail("Read Method did not match for value " + pd.getName());
                }
                Method writeM = pd.getWriteMethod();
                if (writeM != null) {
                    if (! writeM.equals(resPd.getWriteMethod())) {
                        Assert.fail("Write Method did not match for value " + pd.getName());
                    }
                } else if (resPd.getWriteMethod() != null) {
                    Assert.fail("Read Method did not match for value " + pd.getName());
                }
            }
        } else {
           Assert.fail( "No BeanInfo Object found");
        }
              
        Assert.assertTrue(true);
                
    }
    
    /**
     * Test to make sure Introspection Behaves as such on the given beanTest
     * Implied that there is a ClassNameBeanInfo Object available for the Introspector to find.
     * 
     * @param beanTester 
     */
    @Test( dataProvider = "beansForIntrospection" )
    public void testIntrospection( BeanInfoAnticipatedReturns beanTester ) {
        
        //Verify that the there is a BeanInfo Object is this class doesn't already instantiate it
        String beanInfoName = beanTester.getClass().getName()+ "BeanInfo";
        if (!(beanTester instanceof LooseBeanInfo)) {
            try {
                ClassLoader cl = beanTester.getClass().getClassLoader();
                Class cls = ClassFinderUtil.findClass(beanInfoName, cl);
                BeanInfo test = (java.beans.BeanInfo) cls.newInstance();
            } catch (Exception ex) {
                Assert.fail("There is no BeanInfo Object: Error " + ex.getMessage());
            }
        }
        
        BeanInfo bInfo = null;
        try {
            bInfo = Introspector.getBeanInfo( beanTester.getClass() );
        } catch( IntrospectionException ex ) {
            Assert.fail( "Instrospection Failed " + ex.getMessage() );
        }

        if( bInfo != null ) {
            //Generate a cache for comparison
            HashMap< String, PropertyDescriptor > resMap = new HashMap<String, PropertyDescriptor>();
            PropertyDescriptor[] pds = bInfo.getPropertyDescriptors();
            for( PropertyDescriptor pd : pds ) {
                resMap.put( pd.getName(), pd);
            }
            //Comparison Feature
            for( PropertyDescriptor pd : beanTester.getAnticipatedPropertyDescriptors() ) {
                PropertyDescriptor resPd = resMap.get( pd.getName() );
                if( resMap.get( pd.getName() ) == null ) {
                    Assert.fail("Property of name " + pd.getName() + " was expected by not returned by LooseBeanInfo" );
                } 
                Method readM = pd.getReadMethod();
                if (readM != null) {
                    if (!readM.equals(resPd.getReadMethod())) {
                        Assert.fail("Read Method did not match for value " + pd.getName());
                    }
                } else if (resPd.getReadMethod() != null) {
                    Assert.fail("Read Method did not match for value " + pd.getName());
                }
                Method writeM = pd.getWriteMethod();
                if (writeM != null) {
                    if (! writeM.equals(resPd.getWriteMethod())) {
                        Assert.fail("Write Method did not match for value " + pd.getName());
                    }
                } else if (resPd.getWriteMethod() != null) {
                    Assert.fail("Read Method did not match for value " + pd.getName());
                }
            }
        } else {
           Assert.fail( "No BeanInfo Object found");
        }
              
        Assert.assertTrue(true);
    }
    
    /**
     * Will instantiate one looseBeanInfo Instance and then another of the same type
     * in order to determine if {@link LooseBeanInfo#isCacheDerived_} returns true 
     * from the second instance.
     * 
     * @param lBeanInfo the LooseBeanInfo Class that will be instantiated
     */
    @Test( dataProvider = "LooseBeanInfoClazz")
    public void cacheBehaviorTest( Class<LooseBeanInfo> lBeanInfo ) {

        for (int i = 0; i < 2; i++) {
            LooseBeanInfo bInfo = null;
            try {
                bInfo = lBeanInfo.newInstance();
            } catch (Exception ex) {
                Assert.fail(ex.getMessage());
            }

            //Since programmatically, the first beanInfo may already be cached, we can't say much about this one
            if (bInfo == null) {
                Assert.fail("Failed to get instance from Class " + lBeanInfo.getName());
            } else if( i > 0 ) {
                Assert.assertTrue( bInfo.isCacheDerived());
            }
        }
        
        
    }
    
}