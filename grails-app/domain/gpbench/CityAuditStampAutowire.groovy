package gpbench

class CityAuditStampAutowire {
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
        autowire true
		cache true
	}

	static constraints = {
		importFrom(CityBaseline)
	}

	String toString() { name }


	def beforeInsert() {
		dateCreatedUser = springSecurityService.principal.id
	}

	def beforeUpdate() {
		lastUpdatedUser = springSecurityService.principal.id
	}

}
