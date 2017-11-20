package gpbench.benchmarks

import grails.core.GrailsApplication
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class GparsScriptEngineBenchmark<T> extends GparsBaselineBenchmark<T> {
	GroovyScriptEngine scriptEngine
	GrailsApplication grailsApplication

	GparsScriptEngineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		super(clazz,bindingMethod,validate)
	}

	void setup() {
		super.setup()
		scriptEngine = new GroovyScriptEngine("src/main/resources", grailsApplication.classLoader)
	}

	@Override
	@CompileDynamic
	def execute() {
		def scriptinsert = scriptEngine.run("insert-city.groovy",
			new Binding([dataBinder:dataBinder]))//new Binding([batch:batch])

		def args = [poolSize:poolSize]
		gparsLoadService.insertGpars(cities, args){ row, zargs ->
			scriptinsert.insertRow(row)
		}
	}

}
