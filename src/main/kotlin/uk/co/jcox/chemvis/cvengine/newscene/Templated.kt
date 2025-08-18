package uk.co.jcox.chemvis.cvengine.newscene

import org.apache.jena.irix.IRIs.reference

class Templated<T>  (
    private var property: T?,
    var reference: Templated<T>? = null
){


    fun set(override: T?) {
        this.property = override
    }

    /**
     * Will return the value of the templated property
     * If the property is null, then the reference object is called
     * If the reference is also null, then null is returned
     */
    fun get() : T? {
        if (property != null) {
            return property
        }
        return reference?.get()
    }
}