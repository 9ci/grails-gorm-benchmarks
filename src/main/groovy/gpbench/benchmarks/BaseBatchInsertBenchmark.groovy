package gpbench.benchmarks

import gpbench.CityDao
import gpbench.GparsLoadService
import gpbench.helpers.CsvReader
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import org.springframework.jdbc.core.JdbcTemplate

@CompileStatic
abstract class BaseBatchInsertBenchmark extends AbstractBenchmark {
	int poolSize
	int batchSize

	JdbcTemplate jdbcTemplate
	GparsLoadService gparsLoadService

	CsvReader csvReader
	JsonReader jsonReader

	CityDao cityDao

	boolean useDatabinding = true //use default grails databinding
	boolean validate = true
	String bindingMethod = 'grails' // can be copy or setter

	List<List<Map>> cities

	BaseBatchInsertBenchmark(boolean useDatabinding) {
		this.useDatabinding = useDatabinding
		if(useDatabinding) bindingMethod = 'copy'
	}

	BaseBatchInsertBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		if(bindingMethod != 'grails') useDatabinding = false
		this.validate = validate
		this.bindingMethod = bindingMethod
	}

	void setup() {
		RecordsLoader recordsLoader = useDatabinding ? csvReader : jsonReader
		cities = recordsLoader.read("City100k").collate(batchSize)
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city")
	}

	@Override
	String getDescription() {
		String validateDesc = validate ? "": ", validation:${validate}"
		return "${this.getClass().simpleName} [ bindingMethod:${bindingMethod} ${validateDesc}]"
	}

}
