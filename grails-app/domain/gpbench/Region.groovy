package gpbench

import gorm.AuditStamp

@AuditStamp
class Region {
	transient loaderService

	String name
	String code
	String admCode

	static belongsTo = [country:Country]

	static mapping = {
		cache true
		id generator: "assigned"
		
	}
	
	static constraints = {
		name nullable: false
		code nullable:false
		admCode nullable: true
	}

	String toString() { code }

}
