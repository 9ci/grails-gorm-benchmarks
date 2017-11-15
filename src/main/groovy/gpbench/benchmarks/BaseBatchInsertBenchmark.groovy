package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import gpbench.GparsLoadService
import gpbench.helpers.CsvReader
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.jdbc.core.JdbcTemplate

//@CompileStatic
abstract class BaseBatchInsertBenchmark<T> extends AbstractBenchmark {
	int poolSize
	int batchSize

	JdbcTemplate jdbcTemplate
	GparsLoadService gparsLoadService

	CsvReader csvReader
	JsonReader jsonReader

	Class<T> domainClass = City

	boolean useDatabinding = true //use default grails databinding
	boolean validate = true
	String bindingMethod = 'grails' // can be copy or setter

	List<List<Map>> cities

	BaseBatchInsertBenchmark(boolean useDatabinding) {
		this.useDatabinding = useDatabinding
		if(useDatabinding) bindingMethod = 'copy'
	}

	BaseBatchInsertBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		if(bindingMethod != 'grails') useDatabinding = false
		this.validate = validate
		this.bindingMethod = bindingMethod
		domainClass = clazz
	}

	void setup() {
		assert domainClass.count() == 0
		RecordsLoader recordsLoader = useDatabinding ? csvReader : jsonReader
		cities = recordsLoader.read("City100k").collate(batchSize)
	}

	@CompileDynamic
	void cleanup() {
		assert domainClass.count() == 115000
		domainClass.executeUpdate("delete from ${domainClass.getSimpleName()}".toString())
	}

	@Override
	String getDescription() {
		String validateDesc = validate ? "": ", validation: ${validate}"
		return "${this.getClass().simpleName}<${domainClass.simpleName}> [ bindingMethod: ${bindingMethod} ${validateDesc}]"
	}
}
