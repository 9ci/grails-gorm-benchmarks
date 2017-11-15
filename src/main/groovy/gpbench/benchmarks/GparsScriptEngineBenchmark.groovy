package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import gpbench.GparsLoadService
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
	GparsLoadService gparsLoadService

	GparsScriptEngineBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}

	void setup() {
		super.setup()
		scriptEngine = new GroovyScriptEngine("src/main/resources", grailsApplication.classLoader)
	}

	@Override
	@CompileDynamic
	def execute() {
		assert City.count() == 0

		def scriptinsert = scriptEngine.run("insert-city.groovy",
			new Binding([bindingMethod:bindingMethod, dao:cityDao]))//new Binding([batch:batch])

		def args = [poolSize:poolSize]
		gparsLoadService.insertGpars(cities, args){ row, zargs ->
			scriptinsert.insertRow(row)
		}
		assert City.count() == 115000
	}


	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city")
	}

}
