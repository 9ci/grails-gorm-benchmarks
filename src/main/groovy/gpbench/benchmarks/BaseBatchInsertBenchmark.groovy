package gpbench.benchmarks

import gpbench.City
import gpbench.GparsLoadService
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileDynamic

//@CompileStatic
abstract class BaseBatchInsertBenchmark<T> extends AbstractBenchmark {
	int poolSize
	int batchSize

	GparsLoadService gparsLoadService

	JsonReader jsonReader

	SpringSecurityService springSecurityService

	Class<T> domainClass = City

	boolean useDatabinding = true //use default grails databinding
	boolean validate = true
	String bindingMethod = 'grails' // can be copy or setter

	List<List<Map>> cities
	int cityListSize = 37230
	int repeatedCityTimes = 10

	BaseBatchInsertBenchmark(boolean useDatabinding) {
		this.useDatabinding = useDatabinding
		if(!useDatabinding) bindingMethod = 'copy'
	}

	BaseBatchInsertBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		if(bindingMethod != 'grails') useDatabinding = false
		this.validate = validate
		this.bindingMethod = bindingMethod
		domainClass = clazz
	}

	void setup() {
		assert springSecurityService.principal.id != null
		assert springSecurityService.principal.id == 1
		assert domainClass.count() == 0

		RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
		List cityfull = recordsLoader.read("City")
		List repeatedCity = []
		(1..repeatedCityTimes).each { i ->
			repeatedCity = repeatedCity + cityfull
		}
		cities = repeatedCity.collate(batchSize)
	}

	@CompileDynamic
	void cleanup() {
		assert domainClass.count() == cityListSize * repeatedCityTimes//345000 //37230
		domainClass.executeUpdate("delete from ${domainClass.getSimpleName()}".toString())
	}

	@Override
	String getDescription() {
		String validateDesc = validate ? "": ", validation: ${validate}"
		return "${this.getClass().simpleName}<${domainClass.simpleName}> [ bindingMethod: ${bindingMethod} ${validateDesc}]"
	}
}
