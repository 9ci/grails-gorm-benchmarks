package gpbench.helpers

import gorm.tools.GormUtils
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import gpbench.*

@Transactional
@GrailsCompileStatic
class CityDaoBase<T> extends GormDaoSupport<T> {
	private Class<T> thisDomainClass

	CityDaoBase(Class<T> clazz) {
		super(clazz)
		thisDomainClass = clazz
	}

	@NotTransactional
	//@CompileStatic
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
	//@CompileStatic
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
		c.persist()
		return c
	}

	//See at the bottom why we need this doXX methods
	T insert(Map row, Map args) {
		T entity
		if(args.bindingMethod == 'grails'){
			entity = T.newInstance()
			entity.properties = row
		}
		else if(args.bindingMethod == 'copy'){
			entity = bindWithCopyDomain(row)
		}
		else if(args.bindingMethod == 'setter'){
			entity = bindWithSetters(row)
		} else {
			entity = (T) callBinderMethod(args.bindingMethod, row)
		}
		if (fireEvents) super.beforeInsertSave(entity, row)
		super.save(entity, [validate: args.validate?:false ])
		//DaoMessage.created(entity) slows it down by about 15-20%
		return entity //[ok: true, entity: entity, message: DaoMessage.created(entity)]
	}

	@CompileDynamic
	def callBinderMethod(method, row){
		"${method}"(row)
	}


}
