package gpbench

import gorm.AuditStamp


@AuditStamp
class City {
	transient loaderService

	String name
	String shortCode

	Float latitude
	Float longitude

	static belongsTo = [region:Region, country:Country]

	static mapping = {
		if(System.getProperty('idgenerator.enabled', 'false') == "true") {
			id column: 'id', generator:'gorm.tools.idgen.SpringIdGenerator'
		}

		cache true
	}

	static constraints = {
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
	}

	String toString() { name }
}
