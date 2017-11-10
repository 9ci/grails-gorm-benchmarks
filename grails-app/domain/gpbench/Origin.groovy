package gpbench

import gorm.AuditStamp


@AuditStamp
class Origin {
	transient loaderService

	Country country
	Region state
	City city
	
    static constraints = {
		country 	nullable:false
		state 		nullable:true
		city		nullable:true
    }
}
