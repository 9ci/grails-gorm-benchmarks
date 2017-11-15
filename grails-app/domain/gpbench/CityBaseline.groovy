package gpbench

class CityBaseline {
	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	//these don't do anything and are just here to equalize the number of fields
	Date dateCreatedx
	Date lastUpdatedx
	Long dateCreatedUser
	Long lastUpdatedUser

	static belongsTo = [region:Region, country:Country]

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

		dateCreatedx nullable:true,display:false,editable:false,bindable:false
		lastUpdatedx nullable:true,display:false,editable:false,bindable:false
		dateCreatedUser nullable:true,display:false,editable:false,bindable:false
		lastUpdatedUser nullable:true,display:false,editable:false,bindable:false
	}

	String toString() { name }

}
