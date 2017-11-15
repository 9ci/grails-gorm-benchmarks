package gpbench

//@AuditStamp
//@CompileStatic
class CityDynamic{
	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

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
	}

	String toString() { name }

}
