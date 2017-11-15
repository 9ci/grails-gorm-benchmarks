package gpbench.benchmarks
/**
 * Runs batch inserts in parallel using gparse.
 */
//@CompileStatic
class GparsDaoBenchmark<T> extends BaseBatchInsertBenchmark<T>  {

	GparsDaoBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		super(clazz, bindingMethod,validate)
	}

	@Override
	def execute() {
		def args = [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ]
		gparsLoadService.insertGpars(cities, args){ row, zargs ->
			domainClass.dao.insert(row, zargs)
		}
	}

}
