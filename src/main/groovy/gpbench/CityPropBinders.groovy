package gpbench

import gorm.tools.GormUtils
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.DaoDomainTrait
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import gpbench.*
import groovyx.gpars.GParsPool
import org.grails.datastore.gorm.GormEntity

@CompileStatic
class CityPropBinders<T extends CityModel>{
	private Class<T> domainClass

	CityPropBinders(Class<T> clazz) {
		domainClass = clazz
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

	@NotTransactional
	//@CompileDynamic
	T bindWithSetters(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		T c = domainClass.newInstance()
		c.name = row.name
		c.shortCode = row.name

		c.latitude = row.latitude as BigDecimal
		c.longitude = row.longitude as BigDecimal

		c.region = r
		c.country = country
		return c
	}

	T insertWithSetter(Map row) {
		T c = bindWithSetters(row)
		((DaoDomainTrait)c).persist()
		return c
	}

	//See at the bottom why we need this doXX methods
	T insert(Map row, Map args) {
		T entity
		if(args.bindingMethod == 'grails'){
			entity = domainClass.newInstance()
			bindGrails(entity, row)
		}
		else if(args.bindingMethod == 'copy'){
			entity = bindWithCopyDomain(row)
		}
		else if(args.bindingMethod == 'setter'){
			entity = bindWithSetters(row)
		} else {
			entity = (T) callBinderMethod(args.bindingMethod, row)
		}
		return entity
	}

	@CompileDynamic
	def callBinderMethod(methodName, row){
		"${methodName}"(row)
	}

	@CompileDynamic
	void bindGrails(entity, row){
		entity.properties = row
	}

}
