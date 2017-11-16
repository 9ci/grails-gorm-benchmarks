package gpbench

class CityDateStamp {
	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static belongsTo = [region:Region, country:Country]

	static mapping = {
		cache true
	}

	static constraints = {
        importFrom(CityBaseline)
	}

	String toString() { name }

}
