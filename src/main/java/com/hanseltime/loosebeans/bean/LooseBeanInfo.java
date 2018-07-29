package com.hanseltime.loosebeans.bean;


import com.hanseltime.loosebeans.annotations.BeanGetter;
import com.hanseltime.loosebeans.annotations.BeanProperty;
import com.hanseltime.loosebeans.annotations.BeanSetter;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.reflect.FieldUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Base Class for defining BeanInfo Based off of the Class of the Generic Variable of the template.
 * <p>
 * Most normal use cases will require a trivial extension of this class and the common BeanInfo naming convention:
 * <code>
 * MyClassBeanInfo extends LooseBeanInfo&lt;MyClass&gt; {
 *     MyClassBeanInfo() {
 *        super();
 *     }
 * }
 * </code>
 * <p>
 * This class takes advantage of the Introspection library's first checking if a ClassBeanInfo class exists
 * and bases its results off the explicit ClassBeanInfo if that is the case.  By Using LooseBeanInfo to define the 
 * BeanInfo for a given class, you can annotate getters and setters without having strict name coupling.
 * In this way, many classes that have been locked into wrongly spelled setters or getters can simply be annotated
 * without causing dependent code to fail, or having to maintain several getters and setters.
 * <p>
 * TODO: Implement Loose Coupled Event Sets and Event Bus Patterns 
 * 
 * @author Justin Hanselman
 */
public class LooseBeanInfo<T> implements BeanInfo {

    //Static HashMap for Storage of Bean Classes that have been called up by Introspection
    //This will prevent re-iterating reflection through the classes
    private static final HashMap< Class, LooseBeanInfo > looseBeanCache_ = new HashMap< Class, LooseBeanInfo >();
    //Due to the ability for upper classes to change this, implement a lock section for acquiring from the cache
    private static final ReentrantLock cacheLock_ = new ReentrantLock();
    //boolean for checking if this instance was derived from the cache or reflection
    private boolean isCacheDerived_ = false;
    
    //The beanClass instance
    Class<T> beanClazz_;
    //All Publically set/get properties
    private PropertyDescriptor[] propDescrips_ = new PropertyDescriptor[0];
    private int defaultPropIdx_;
    //All Public methods that were not synthetically created
    private MethodDescriptor[] methodDescrips_ = new MethodDescriptor[0];
    private BeanDescriptor descriptor_;
    private BeanInfo[] additionalBeanInfo_ = new BeanInfo[0];
    private EventSetDescriptor[] eventSetDescrips_ = new EventSetDescriptor[0];
    private int defaultEvtIdx_;
    private Image icon_;

    /**
     * Base Constructor -
     * <p>
     * Meant to be extended if only with an exact type, in order for reflection to be done on the class
     * and a BeanInfo object created accordingly.
     */
    public LooseBeanInfo() {
        
        //Make beanClazz_ be populated
        populateBeanClassInstance();

        //Lock the cache while we operate on it
        lockCache();
        try {
            populateLooseBeanInfo();
        }finally {
                unlockCache();
        }


    }
    
    /**
     * Calls the nullary constructor but will determine whether or not the constructor unlocks the 
     * cache of previously reflected LooseBeanInfo or expects an external unlock from {@link #unlockCache()}.
     * <p>
     * Note:  This is only provided for use in non-trivial extension classes, i.e., there is a new member that must be populated
     * and returned via other methods due to some other interface requirements for a particular application.  This must call {@link #unlockCache()} in a finally
     * block in order to ensure that the cache is not deadlocked.  Additionally, when these differences
     * are non-trivial, they must be stored in the cache that will shallow copy an instance from the cache rather than
     * performing reflection on all the beanClass members.  Due to the restriction of only using the nullary constructor,
     * the extending class must set the cache instance to its instance for future use, otherwise a LooseBeanInfo object
     * with only that class-levels members will be produced.
     * <p>
     * It is the responsibility of such non-trivial extending classes to: retrieve the instance of itself if it exists,
     * apply the instance members to the subclass members as necessary and return, or
     * perform more reflection or other operations to fill the members of the subclass
     * and set the instance in the cache to be returned during the next constructor call.
     * Finally, unlock the cache.
     * <p> 
     * This is provided for the programmer who wants to take responsibility for the nightmare of
     * extending this wrong and deadlocking code.
     * <p>
     * The prototypical extension should be:
     * <code>
     * public ExtendingLooseBeanInfo() {
     *       super(true);  //Trust that this will only unlock the cache itself if there was an exception
     *       try {
     *         try {
     *              //The cache will be full with a super class instance of LooseBeanInfo, must check
     *              (ExtendingLooseBeanInfo) subClass = getLooseBeanInfoCacheInstance();
     *              populateThisInstance(subClass);
     *              return;
     *         } catch (ClassCastException  ex ) {
     *           //Simply proceed to populate like it is the first time and push a copy into the cache
     *         }
     *         //SubClass Code
     *         setLooseBeanInfoCacheInstance( new ExtendingLooseBeanInfo( this.castMembersToCopy ) );
     *       } finally {
     *          unlockCache();
     *       }
     * }
     * </code>
     * 
     * @param externCacheUnlock Whether of not the calling context will unlock the cache Lock.  If false, this is simply the nullary constructor.
     * @throws Exception If class loader fails in an unmanageable way (catch in a calling context, or at least with debugger)
     */
    protected LooseBeanInfo( boolean externCacheUnlock ) throws Exception {
        //Make beanClazz_ be populated
        populateBeanClassInstance();

        //Lock the cache while we operate on it
        lockCache();
        try {
            populateLooseBeanInfo();
        } catch (Exception ex) {
          //if there is an exception, we will unlock anyway due to failure
          externCacheUnlock = false;
          throw ex;
        } finally {
            //Trust the extening programmer to not royally screw this up
            if( !externCacheUnlock ) {   
                unlockCache();
            }
        }
    }
    
    /** 
     * Populates the BeanClass instance of the Generic Type to the LooseBeanInfo Object
     */
    private void populateBeanClassInstance() {
        //If this is subclassed we run through any generic subclasses
        Type supType = this.getClass().getGenericSuperclass();
        while( supType!= null ) {
            try {
                //Continue until casting to the correct super level (this level)
                ParameterizedType genType = (ParameterizedType) supType;
                Class<LooseBeanInfo<T>> curSuper = (Class<LooseBeanInfo<T>>) genType.getRawType();
                beanClazz_ = (Class<T>) (genType.getActualTypeArguments()[0]);
                break;
            } catch (ClassCastException ex) {
                //This is not the anticipated type, keep iterating
            } /*catch (InstantiationException ex) {
                //Unsure how to handle these currently
                Logger.getLogger(LooseBeanInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(LooseBeanInfo.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            Class temp = supType.getClass();
            supType = temp.getGenericSuperclass();
        } 
        
        if( beanClazz_ == null ) {
            //We should throw a meaningful exception
        }
    }
    
    /**
     * Populates the object instance, either by using the cached instance from the cache
     * or by using reflection.
     */
    private void  populateLooseBeanInfo() {
                
        final Class clazz = beanClazz_;
        //Check the cache For if it has already had its BeanInfo populated
        LooseBeanInfo<T> cacheBean = (LooseBeanInfo<T>) looseBeanCache_.get(clazz);
        //Assign all the same values to this instance, since callers will be calling newInstance()
        if( cacheBean != null ) {
            populateFromCache(cacheBean);
            return;
        }
        
        //Generate the Property and Method Descriptors for this instance
        populatePropertiesAndMethodsViaReflection();
        
        //Store an instance of the same bean to the cache
        //This operation avoids leaking constructor while producing an instance for future reference
        setLooseBeanInfoCacheInstance(new LooseBeanInfo( descriptor_, propDescrips_, defaultPropIdx_,
                               methodDescrips_, eventSetDescrips_, defaultEvtIdx_, additionalBeanInfo_, icon_ ));
    }
    

    /**
     * Standardized Method for exposing the cache lock for the LooseBeanInfo instances.
     * <p>
     * This is in an effort to standardize the very niche use of unlockCache() function for extending classes
     * that will implement their own nullary constructor and will invoke {@link #LooseBeanInfo(boolean)} with the parameter
     * set to true.
     * <p>
     * Warning: Deadlocks can occur if the programmer does not read through the description of {@link #LooseBeanInfo(boolean) }
     * carefully.  Additionally, the programmer should make sure they cannot simply create a trivial specific instance
     * for their BeanInfo Object, meaning: 
     * <code>
     * SpecificClassBeanInfo extends LooseBeanInfo&lt;SpecificClass$gt; {
     *      SpecificClassBeanInfo() {
     *          super();
     *      }
     * }
     * </code>
     */
    private void lockCache() {
        cacheLock_.lock();
    }
    
    /**
     *  Method for exposing the .lock() function for the static cache of LooseBeanInfo instances that have already been reflected.
     * <p>
     * Note:  This is only provided for use in non-trivial extension classes, i.e., there is a new member that must be populated
     * and returned due to some other interface requirements for a particular application.  This must call {@link #unlockCache()} in a finally
     * block in order to ensure that the cache is not deadlocked.  This is provided to the programmer who wants to take responsibility for this only.
     * <p>
     * Warning: Deadlocks can occur if the programmer does not read through the description of {@link #LooseBeanInfo(boolean) }
     * carefully.  Additionally, the programmer should make sure they cannot simply create a trivial specific instance
     * for their BeanInfo Object (i.e. 
     * <code>
     * SpecificClassBeanInfo extends LooseBeanInfo&lt;SpecificClass$gt; {
     *      SpecificClassBeanInfo() {
     *          super();
     *      }
     * }
     * </code>
     */
    protected void unlockCache() {
        cacheLock_.unlock();
    }
    
    
    /**
     * Populates this LooseBeanInfo instance from a cached LooseBeanInfo reference.
     * <p>
     * This is meant to emulate factory behavior by copying over the reference values into the instance
     * produced by newInstance();
     * 
     * @param cacheBeanInfo The LooseBeanInfo instance (non-null) returned by the current {@link #beanClazz_} of the LooseBeanInfo.
     */
    private final void populateFromCache( LooseBeanInfo<T> cacheBeanInfo ) {
        propDescrips_ = cacheBeanInfo.propDescrips_;
        defaultPropIdx_ = cacheBeanInfo.defaultPropIdx_;
        //All Public methods that were not synthetically created
        methodDescrips_ = cacheBeanInfo.methodDescrips_;
        descriptor_ = cacheBeanInfo.descriptor_;
        additionalBeanInfo_ = cacheBeanInfo.additionalBeanInfo_;
        eventSetDescrips_ = cacheBeanInfo.eventSetDescrips_;
        defaultEvtIdx_ = cacheBeanInfo.defaultEvtIdx_;
        icon_ = cacheBeanInfo.icon_;

        //set Cache boolean for testing really
        setIsCacheDerived( true );
    }
    
    /**
     * Sets if this instance is derived from a cache_. 
     * <p>
     * This method is protected for the sake of any non-trivial extending classes that
     * need to indicate when an instance was populated from the cache in the
     * subclass constructor.
     * <p>
     * See the {@link #LooseBeanInfo(boolean) ) for implementation examples of non-trivial 
     * subclasses.
     * 
     * @param cacheDerived - true is this instance was populated from the cache
     * @return 
     */
    protected final void setIsCacheDerived( boolean cacheDerived ) {
        isCacheDerived_ = cacheDerived;
    }
    
    /**
     * Get whether or not this instance was derived from a cache or was the result of reflection.
     * <p>
     * Mainly intended for testing purposes.
     * 
     * @return <code>true</code> if this instance was populated by the cache
     */
    public boolean isCacheDerived( ) {
        return isCacheDerived_; 
    }
    
    /**
     * Protected Complete Constructor
     * <p>
     * Used to create the hashed instance for future use by newInstance calls, in order
     * to avoid reflecting everytime.
     * 
     * @param beanDescrip The {@link BeanDescriptor} for the {@link #beanClazz_}
     * @param pDescrips The array of {@link PropertyDescriptor}s that correspond to the {@link #beanClazz_}
     * @param mDescrips The array of {@link MethodDescriptor}s that correspond to the {@link #beanClazz_}
     * @param defPropIdx The default Property Index
     * @param evtDescrips The array of {@link EventDescriptor}s that correspond to the {@link #beanClazz_}
     * @param defEventIdx The default Event Index
     * @param additionalBeanInfo The array of BeanInfo objects that are passed as additional for the {@link #beanClazz_}
     * @param icon The icon used for graphical display of the {@link #beanClazz_}
     */
    protected LooseBeanInfo( BeanDescriptor beanDescrip, PropertyDescriptor[] pDescrips, int defPropIdx, 
                            MethodDescriptor[] mDescrips, EventSetDescriptor[] evtDescrips, int defEventIdx, 
                            BeanInfo[] additionalBeanInfo, Image icon ) {
        propDescrips_ = pDescrips;
        defaultPropIdx_ = defPropIdx;
        //All Public methods that were not synthetically created
        methodDescrips_ = mDescrips;
        descriptor_ = beanDescrip;
        additionalBeanInfo_ = additionalBeanInfo;
        eventSetDescrips_ = evtDescrips;
        defaultEvtIdx_ = defEventIdx;
        icon_ = icon;
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return descriptor_;
    }

    //In the future, we can customize this for EventBus and other models with annotations
    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return null;
    }

    @Override
    public int getDefaultEventIndex() {
        return -1;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return propDescrips_;
    }

    @Override
    public int getDefaultPropertyIndex() {
        return -1;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return methodDescrips_;
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    @Override
    public Image getIcon(int i) {
        return null;
    }
    
    /**
     * Stores an instance of the looseBean to the current beanClass Class object.
     * <p>
     * Note:  This method exists for the use of non-trivial extensions of the LooseBeanInfo<T> class.
     * In order to avoid constructor leaking, this method MUST be appended at the end of an instance's first creation
     * so that the beanInstance will be stored according to the Class Object and it should take a separately created
     * instance that copies the members of the LooseBean.
     * <p>
     * Normally, this behavior will be completely automatic, as there should be no need to
     * extend a complex LooseBeanInfo, but in the event that such a circumstance arises, this method may be used.
     * 
     * @param beanInstance The instance of an extended BeanInstance.  This will overwrite the LooseBeanInfo instance
     * that as previously associated with the beanClass.
     */
    protected final void setLooseBeanInfoCacheInstance( LooseBeanInfo<T> beanInstance ) {
        //Since it's a reentrant lock, let's just protect against someone stupid trying to abuse this and lock the cache
        lockCache();
        try {
            looseBeanCache_.put(beanClazz_, beanInstance );
        } finally {
            unlockCache();
        }
    }
    
    /**
     * Gets the LooseBeanInfo Instance that is associate with the beanClass of this LooseBeanInfo Class.
     * <p>
     * Note: This is intended for use with non-trivial extending classes of LooseBeanInfo.  Since the 
     * design pattern of this constructor is to store a cache of {@link #beanClazz_} objects and their LooseBeanInfo
     * instances, in order to avoid reflection, this class must be called by the constructor of a non-trivial subclass
     * in order to either 1. determine if there is no cached instance or 2. use the cached instance to populate its 
     * instance values quickly.  It should be noted that since the cache is at the LooseBeanInfo level, 
     * there will be a returned object after the super() is finished, but that object will not be able to be cast to the
     * extending class yet.
     * <p>
     * Example code (Note this should be wrapped in a constructor) {@link #LooseBeanInfo(boolean) }:
     * <code>
     *      try {
     *          ExtendingLooseBeanInfo subClass = (ExtendingLooseBeanInfo) getLooseBeanInfoCacheInstance()
     *          //Use subClass to populate members of this subclass
     *          return;
     *      } catch (ClassCastException ex ) {
     *          //There hasn't been a saved instance yet from this subClass
     *      }
     * 
     *      //Operations to populate non-trivial fields of this subclass
     *      ...
     *      setLooseBeanInfoCacheInstance( new ExtendingLooseBeanInfo( members to use ) );
     * </code>
     * 
     * @return The LooseBeanInfo instance that was stored against the beanClazz_ of the generic type.
         This may need to be checked if implementing classes have additional members, as the 
         instance is a separate instance of LooseBeanInfo in order to avoid constructor leaking.
     */
    protected final LooseBeanInfo<T> getLooseBeanInfoCacheInstance( ) {
        lockCache();
        try {
            return looseBeanCache_.get(beanClazz_ );
        } finally {
            unlockCache();
        }
    }
    
    /**
     * Takes the class object stored with this LooseBeen {@link #beanClazz_} and
     * populated the method descriptors and PropertyDescriptors via reflection.
     * 
     */
    private final void populatePropertiesAndMethodsViaReflection( ) {
        
        final Class clazz = beanClazz_;
        
        descriptor_ = new BeanDescriptor(clazz);

        // In order to get all methods, do this in a privileged context
        //We will move trhough all supers in the event of a looseBean structure with a subclass getter/setter
        Method[] allMethods = (Method[]) AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                Class curLvl = clazz;
                List<Method> mList = new ArrayList<Method>();
                while( curLvl != null ) {
                    for( Method m : curLvl.getDeclaredMethods() ) {
                        mList.add(m);
                    }
                    curLvl = curLvl.getSuperclass();
                }
                
                return mList.toArray( new Method[ mList.size() ]);
            }
        });

        //For Now we will only populate public getters and setters, however, the C# Property design style of protected
        // setters and getters may be worked towards if other overrides are taken for use with introspection classes
        HashMap< String, Method> getterCache = new HashMap();
        HashMap< String, Method> setterCache = new HashMap();
        HashMap< String, Method> iGetterCache = new HashMap();
        HashMap< String, Method> iSetterCache = new HashMap();
        List<MethodDescriptor> mDescrips = new ArrayList<MethodDescriptor>();
        for (Method m : allMethods) {
            int mods = m.getModifiers();
            if (Modifier.isPublic(mods) && !m.isSynthetic()) {
                //Go Ahead and add this method
                //Introspector assumes name collision, but this is not anticipated since the list will be returned unordered
                mDescrips.add(new MethodDescriptor(m));

                BeanGetter getter = m.getAnnotation(BeanGetter.class);
                BeanSetter setter = m.getAnnotation(BeanSetter.class);
                Class[] argTypes = m.getParameterTypes();
                Class resType = m.getReturnType();
                int numArgs = argTypes.length;

                if (getter != null && !getter.name().isEmpty()) {
                    //Simple Getter Function
                    if (numArgs == 0) {
                        getterCache.put(getter.name(), m);
                    } else if (numArgs == 1 && argTypes[0] == int.class) {
                        //Indexed Getter function
                        iGetterCache.put(getter.name(), m);
                    }

                } else if (setter != null && !setter.name().isEmpty()) {
                    //Simple setter, does not check for returns because a setter may yield its own 
                    //return value
                    if (numArgs == 1) {
                        setterCache.put(setter.name(), m);
                    } else if (numArgs == 2 && argTypes[0] == int.class) {
                        //Indexed Setter Function
                        iSetterCache.put(setter.name(), m);
                    }
                } else {
                    //Default Bean getter-setter search behavior
                    String mName = m.getName();
                    switch (numArgs) {
                        case 0:
                            //Simple getters
                            if (mName.startsWith("get")) {
                                getterCache.put(mName.substring(3), m);
                            } else if (mName.startsWith("is")) {
                                getterCache.put(mName.substring(2), m);
                            }
                            break;
                        case 1:
                            if (argTypes[0] == int.class && mName.startsWith("get")) {
                                //Indexed Getter
                                iGetterCache.put(mName.substring(3), m);
                            } else if (resType == void.class && mName.startsWith("set")) {
                                setterCache.put(mName.substring(3), m);
                            }
                            break;
                        case 2:
                            if (argTypes[0] == int.class && mName.startsWith("set")) {
                                iSetterCache.put(mName.substring(3), m);
                            }
                            break;
                        default:
                            break;
                    }

                }

            }
        }

        //Set the methodDescriptors
        methodDescrips_ = mDescrips.toArray(methodDescrips_);

        //Having Cached Getters and setters, we now produce Property Descriptors relevant to the fields                        
        List<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>();
        Field[] fields = FieldUtils.getAllFields(clazz);
        for (Field f : fields) {
            BeanProperty bProp = f.getAnnotation(BeanProperty.class);
            PropertyDescriptor pd = null;
            String prop;
            //We need to name the Property after its underlying variable name for
            //reflection lookups based off of field.
            String propName = f.getName();
            if (bProp != null && !bProp.name().isEmpty()) {
                prop = bProp.name();
            } else {
                prop = propName;
            }
            try {
                //To preseve Type safety
                if (iGetterCache.containsKey(prop) || iSetterCache.containsKey(prop)) {
                    pd = new IndexedPropertyDescriptor(propName, getterCache.get(prop), setterCache.get(prop),
                            iGetterCache.get(prop), iSetterCache.get(prop));
                    //SOME TYPE OF CONTINUE
                } else {
                    if (getterCache.containsKey(prop) || setterCache.containsKey(prop)) {
                        //Ensure that we are not just registering properties with no getters or setters due to loose wording in spec 
                        pd = new PropertyDescriptor(propName, getterCache.get(prop), setterCache.get(prop));
                    }
                }
            } catch (IntrospectionException ex) {
                //The property descriptor failed, so we are just going to continue through the other fields
                pd = null;
            }

            if (pd != null) {
                pds.add(pd);
            }

        }

        //Cache these property descriptors
        propDescrips_ = pds.toArray(propDescrips_);
    }

}
