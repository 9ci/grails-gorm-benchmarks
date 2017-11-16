package gpbench

/**
 * Dao Baseline. This has a DAO and has been touched by the gorm-tools AST
 */
class City {

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
