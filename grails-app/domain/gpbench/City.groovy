package gpbench

import grails.compiler.GrailsCompileStatic

/**
 * Dao Baseline. This has a DAO and has been touched by the gorm-tools AST
 */
@GrailsCompileStatic
class City {
    String name
    String shortCode

    BigDecimal latitude
    BigDecimal longitude

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    Region region
    Country country
    String state
    String countryName

    static belongsTo = [region:Region, country:Country]

    static mapping = {
        //cache true
    }

	static constraints = {
        importFrom(CityBaseline)
	}

	String toString() { name }

    def beforeInsert() {
        dateCreatedUser = SecUtil.userId
    }

    def beforeUpdate() {
        lastUpdatedUser = SecUtil.userId
    }

}
