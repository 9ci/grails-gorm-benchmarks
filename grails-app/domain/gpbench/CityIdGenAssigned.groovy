package gpbench

class CityIdGenAssigned {
	//injected bean for ids.
	transient idGenerator

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

    Region region
    Country country
    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    static belongsTo = [region:Region, country:Country]

	static mapping = {
		id generator:'assigned'
		table 'CityIdGenAssigned'
		cache true
	}

	static constraints = {
		importFrom(CityBaseline)
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
