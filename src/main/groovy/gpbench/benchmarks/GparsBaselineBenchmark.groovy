package gpbench.benchmarks

import gorm.tools.GormUtils
import gorm.tools.databinding.FastBinder
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

/**
 * Baseline benchmark with grails out of the box
 */

//@GrailsCompileStatic
class GparsBaselineBenchmark<T> extends BaseBatchInsertBenchmark<T> {

    FastBinder fastBinder

	GparsBaselineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		super(clazz, bindingMethod,validate)
	}

	@Override
	def execute() {
		def args = [poolSize:poolSize]
		gparsLoadService.insertGpars(cities, args){ Map row, Map zargs ->
			insertRow(row)
		}
	}

	void insertRow(Map row) {

		if (dataBinder == 'grails') {
			T city = domainClass.newInstance()
			city.properties = row
			city.save(failOnError:true, validate:validate)
		}
		else {
			T city = bindWithCopyDomain(row)
			city.save(failOnError:true, validate:validate)
		}
	}

	//@CompileDynamic
	void bindGrails(city, row){
		city.properties = row
	}

	T bindWithCopyDomain(Map row) {
		//Region r = Region.load(row['region']['id'] as Long)
		//Country country = Country.load(row['country']['id'] as Long)

		T c = domainClass.newInstance()
		c = fastBinder.bind(c, row)
        //c = setPropsFastIterate(c, row)
        //  c.region // = r
        //c.country // = country
		return c
	}


}
