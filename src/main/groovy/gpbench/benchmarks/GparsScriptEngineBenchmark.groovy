package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.core.GrailsApplication
import grails.transaction.Transactional

/**
 * Calls external script for each row. The script does the insert.
 */

//@CompileStatic
class GparsScriptEngineBenchmark extends GparsDaoBenchmark {
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
		insertGpars(cities, cityDao)
		assert City.count() == 115000
	}

	@Transactional
	void insertRow(Map row, CityDao dao) {
		Binding binding = new Binding([record:row])
		scriptEngine.run("insert-city.groovy", binding)
	}

	@Override
	String getDescription() {
		return "GparsScriptEngine: databinding=${useDatabinding}, validation:${validate}"
	}
}
