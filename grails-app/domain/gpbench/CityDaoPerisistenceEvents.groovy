package gpbench

import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import grails.compiler.GrailsCompileStatic

/**
 * Audist stamp fields are set from dao persistence events listener.
 */
@GrailsCompileStatic
class CityDaoPerisistenceEvents implements CityTrait, DateUserStamp {

    static belongsTo = [region:Region, country:Country]

    static mapping = {
        //cache true
    }

    static constraints = {
        importFrom CityTraitConstraints
        importFrom DateUserStampConstraints
    }

	String toString() { name }
}
