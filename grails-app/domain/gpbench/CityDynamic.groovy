package gpbench

class CityDynamic{
	//transient springSecurityService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static belongsTo = [Region, Country]

	static mapping = {
		cache true
	}

	static constraints = {
		importFrom(City)
	}

	String toString() { name }

	def beforeInsert() {
		dateCreatedUser = SecUtil.userId
	}

	def beforeUpdate() {
		lastUpdatedUser = SecUtil.userId
	}

}
