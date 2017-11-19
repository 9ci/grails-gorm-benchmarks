package gpbench

import gorm.AuditStamp
import grails.compiler.GrailsCompileStatic

@AuditStamp
@GrailsCompileStatic
class CityAuditTrail {
	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

    String state
    String countryName

	static belongsTo = [Region, Country]

	static mapping = {
		//id column: 'id', generator:'gorm.tools.idgen.SpringIdGenerator'
		cache true
	}

	static constraints = {
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
		latitude nullable: false, scale: 4, max:90.00
		longitude nullable: false, scale: 4, max:380.00
		region nullable: false
		country nullable: false
        state nullable: true
        countryName nullable: true
	}

	String toString() { name }

}
