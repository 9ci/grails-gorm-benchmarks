package gpbench

//import grails.plugin.springsecurity.SpringSecurityService

//import javax.persistence.Transient

class CityModelTrait implements CityModel{
	//@Transient
	//transient SpringSecurityService springSecurityService

	static belongsTo = [Region, Country]

	static mapping = {
		cache true
	}

	static constraints = {
		importFrom(City)
	}

	String toString() { name }

}
