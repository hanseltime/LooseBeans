# LooseBeans
A library for allowing loose annotation of Bean-like properties and their getter/setter methods without explicit method-to-field conventions

#Example

    //BeanLikeClass.java
    public class BeanLikeClass {

        @BeanProperty( name = "vA" )
        private Object internalValueName_;

        ...

        @BeanGetter( name = "vA" )
        public void getMeaningfulNameToContext() {

        }

    }

    //BeanLikeClassBeanInfo.java
    //This is in the event that the programmer chooses not to extend directly
    public class BeanLikeClassBeanInfo extends LooseBeanInfo< BeanLikeClass > {
        //Trivial extension that will aggregate all getters and setters that obey LooseBean or Bean rules
        BeanLikeClassBeanInfo() {
            super();
        }
    }