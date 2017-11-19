package gpbench

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic  //FIXME not sure whats up here.
class CityIdGen {

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country
    String state
    String countryName

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

	static belongsTo = [Region, Country]

	static mapping = {
		id generator:'gorm.tools.idgen.SpringIdGenerator'
		cache true
	}

	static constraints = {
		importFrom(CityBaselineDynamic)
	}

	String toString() { name }


}
