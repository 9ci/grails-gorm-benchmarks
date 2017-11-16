package gpbench

class CityIdGen {

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
		id column: 'id', generator:'gorm.tools.idgen.SpringIdGenerator'
		cache true
	}

	static constraints = {
		importFrom(CityBaseline)
	}

	String toString() { name }


}
