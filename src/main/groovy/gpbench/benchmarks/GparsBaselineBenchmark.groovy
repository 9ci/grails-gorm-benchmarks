package gpbench.benchmarks

import gorm.tools.GormUtils
import gpbench.Country
import gpbench.Region
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

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

	//@CompileDynamic
	void bindGrails(city, row){
		city.properties = row
	}

	T bindWithCopyDomain(Map row) {
		//Region r = Region.load(row['region']['id'] as Long)
		//Country country = Country.load(row['country']['id'] as Long)

		T c = domainClass.newInstance()
		c = GormUtils.bindFast(c, row)
        //c = setPropsFastIterate(c, row)
        //  c.region // = r
        //c.country // = country
		return c
	}

    @CompileStatic
    T setPropsFastIterate(T obj, Map source, boolean ignoreAssociations = false) {
        //if (target == null) throw new IllegalArgumentException("Target is null")
        if (source == null) return

        def sapi = GormEnhancer.findStaticApi(super.domainClass)
        def properties = sapi.gormPersistentEntity.getPersistentProperties()
        for (PersistentProperty prop : properties){
            if(!source.containsKey(prop.name)) {
                continue
            }
            def sval = source[prop.name]
            if (prop instanceof Association && sval['id']) {
                if(ignoreAssociations) return
                def asocProp = (Association)prop
                def asc = GormEnhancer.findStaticApi(asocProp.associatedEntity.javaClass).load(sval['id'] as Long)
                obj[prop.name] = asc
            }
            else{
                obj[prop.name] = sval
            }
            //println prop
            //println "${prop.name}: ${obj[prop.name]} -> region:${obj.region}"
        }
        return obj
    }

}
