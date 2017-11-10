package gpbench

import gorm.AuditStamp

@AuditStamp
class Country {
	transient loaderService

	String name
	String capital
	String fips104
	String iso2
	String iso3

	static mapping = {
		id generator: "assigned"
		cache true
	}

	static constraints = {
		name unique: true
		capital nullable: true
	}

	String toString() { name }

}
