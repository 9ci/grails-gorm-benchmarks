package gpbench

import gorm.AuditStamp
import groovy.transform.CompileStatic


//@AuditStamp
//@CompileStatic
class City implements BaseCity {

	//transient loaderService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

	static belongsTo = [region:Region, country:Country]

	static mapping = {
		cache true
	}

	static constraints = {
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
		latitude nullable: false, scale: 4, max:90.00
		longitude nullable: false, scale: 4, max:380.00
	}

	String toString() { name }

}
