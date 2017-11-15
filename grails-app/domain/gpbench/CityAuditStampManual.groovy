package gpbench


class CityAuditStampManual {

	transient springSecurityService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	static belongsTo = [region:Region, country:Country]

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static mapping = {
		cache true
	}

	static constraints = {
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
		latitude nullable: false, scale: 4, max:90.00
		longitude nullable: false, scale: 4, max:380.00
		region nullable: false

		country nullable: false

		dateCreated nullable:true,display:false,editable:false,bindable:false
		lastUpdated nullable:true,display:false,editable:false,bindable:false
		dateCreatedUser nullable:false,display:false,editable:false,bindable:false
		lastUpdatedUser nullable:false,display:false,editable:false,bindable:false
	}

	String toString() { name }

	def beforeValidate(){
		lastUpdatedUser = springSecurityService.principal.id
		dateCreatedUser = springSecurityService.principal.id
	}

	def beforeInsert() {
		dateCreatedUser = springSecurityService.principal.id
	}

	def beforeUpdate() {
		lastUpdatedUser = springSecurityService.principal.id
	}

}
