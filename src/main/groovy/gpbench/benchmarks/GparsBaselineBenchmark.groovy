package gpbench.benchmarks

import gorm.tools.GormUtils
import gpbench.Country
import gpbench.Region
import groovy.transform.CompileDynamic

/**
 * Baseline benchmark with grails out of the box
 */

//@GrailsCompileStatic
class GparsBaselineBenchmark<T> extends BaseBatchInsertBenchmark<T> {

	GparsBaselineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		super(clazz, bindingMethod,validate)
	}

	@Override
	def execute() {
		def args = [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ]
		gparsLoadService.insertGpars(cities, args){ Map row, Map zargs ->
			insertRow(row)
		}
	}

	void insertRow(Map row) {
		if (bindingMethod == 'grails') {
			T city = domainClass.newInstance()
			city.properties = row
			city.save(failOnError:true, validate:validate)
		}
		else {
			T city = bindWithCopyDomain(row)
			city.save(failOnError:true, validate:validate)
		}
	}

	@CompileDynamic
	void bindGrails(city, row){
		city.properties = row
	}

	T bindWithCopyDomain(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		T c = domainClass.newInstance()
		GormUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

}
