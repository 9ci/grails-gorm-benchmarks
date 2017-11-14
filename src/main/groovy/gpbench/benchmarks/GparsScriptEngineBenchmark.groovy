package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.core.GrailsApplication
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class GparsScriptEngineBenchmark extends GparsBaselineBenchmark {
	GroovyScriptEngine scriptEngine
	GrailsApplication grailsApplication

	GparsScriptEngineBenchmark(boolean databinding = true) {
		super(databinding)
	}

	void setup() {
		super.setup()
		scriptEngine = new GroovyScriptEngine("src/main/resources", grailsApplication.classLoader)
	}

	@Override
	def execute() {
		assert City.count() == 0
		insertGpars(cities)
		assert City.count() == 115000
	}

	@Transactional
	@CompileDynamic
	void insertBatch(List<Map> batch) {
		def scriptinsert = scriptEngine.run("insert-city.groovy", new Binding())//new Binding([batch:batch])
		for (Map record : batch) {
			scriptinsert.insertRow(record)
		}
		DaoUtil.flushAndClear()
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city")
	}

	@Override
	String getDescription() {
		return "GparsScriptEngine: databinding=${useDatabinding}, validation:${validate}"
	}
}
