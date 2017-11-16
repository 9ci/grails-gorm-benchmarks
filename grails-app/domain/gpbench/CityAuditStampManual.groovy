package gpbench

class CityAuditStampManual {
	//transient springSecurityService

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
