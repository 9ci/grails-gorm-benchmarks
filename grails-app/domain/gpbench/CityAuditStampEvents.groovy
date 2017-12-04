package gpbench

import grails.compiler.GrailsCompileStatic

/*
 Audit stamp fields are set by a gorm event listener defined in external groovy script.
 */
@GrailsCompileStatic
class CityAuditStampEvents {
	//transient springSecurityService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

    String state
    String countryName

	static belongsTo = [region:Region, country:Country]

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static mapping = {
		cache true
	}

    static constraints = {
        importFrom(CityBaseline)
    }

	String toString() { name }
}
