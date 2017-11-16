package gpbench
/**
 * Dao Baseline. This has a DAO and has been touched by the gorm-tools AST
 */
class City {
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
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
		latitude nullable: false, scale: 4, max:90.00
		longitude nullable: false, scale: 4, max:380.00
		region nullable: false
		country nullable: false

		dateCreated nullable:true,display:false,editable:false,bindable:false
		lastUpdated nullable:true,display:false,editable:false,bindable:false
		dateCreatedUser nullable:true,display:false,editable:false,bindable:false
		lastUpdatedUser nullable:true,display:false,editable:false,bindable:false
	}

	String toString() { name }

	def beforeInsert() {
		dateCreatedUser = SecUtil.userId
	}

	def beforeUpdate() {
		lastUpdatedUser = SecUtil.userId
	}
}
