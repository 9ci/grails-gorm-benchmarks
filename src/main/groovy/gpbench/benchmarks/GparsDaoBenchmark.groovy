package gpbench.benchmarks
/**
 * Runs batch inserts in parallel using gparse.
 */
//@CompileStatic
class GparsDaoBenchmark<T> extends BaseBatchInsertBenchmark<T>  {

	GparsDaoBenchmark(Class<T> clazz, String bindingMethod = 'grails') {
		super(clazz, bindingMethod)
	}

	@Override
	def execute() {
		def args = [poolSize:poolSize]
		gparsLoadService.insertGpars(cities, args){ Map row, zargs ->
            //domainClass.dao.create( row, [validate:validate, dataBinder:dataBinder ])
            domainClass.dao.create( row)
		}
	}

}
