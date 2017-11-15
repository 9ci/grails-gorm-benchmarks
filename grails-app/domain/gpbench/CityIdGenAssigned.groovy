package gpbench

class CityIdGenAssigned {
	//injected bean for ids.
	transient idGenerator

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	static belongsTo = [region:Region, country:Country]

	static mapping = {
		id generator:'assigned'
		table 'CityIdGenAssigned'
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

	def beforeInsert() {
    	if(!id)  generateId()
	}

	def beforeValidate(){
	    if(!id)  generateId()
	}

	/**
	 * uses the injected idGenerator to get and assign an id to this instance
	 */
	Long generateId(){
	    id = idGenerator.getNextId('CityIdGenAssigned.id')
	}

	String toString() { name }
}
