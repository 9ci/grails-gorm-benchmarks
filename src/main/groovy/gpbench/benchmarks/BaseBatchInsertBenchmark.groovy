package gpbench.benchmarks

import gpbench.CityDao
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

	CsvReader csvReader
	JsonReader jsonReader

	CityDao cityDao

	boolean useDatabinding = false

	List<List<Map>> cities

	BaseBatchInsertBenchmark(boolean databinding) {
		this.useDatabinding = databinding
	}

	void setup() {
		RecordsLoader recordsLoader = useDatabinding ? csvReader : jsonReader
		cities = recordsLoader.read("City100k").collate(batchSize)
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city")
	}

}
