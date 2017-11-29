package gpbench

import gorm.tools.GormUtils
import gorm.tools.dao.DaoEntity
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gpbench.model.CityModel
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import org.grails.datastore.gorm.GormEntity

@Transactional
@CompileStatic
class CityDaoBase<T extends CityModel & GormEntity & WebDataBinding & DaoEntity> extends DefaultGormDao<T> {


	CityDaoBase(Class<T> clazz) {
		super(clazz)
		//thisDomainClass = clazz
	}

	@NotTransactional
	//@CompileDynamic
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
        ((DaoEntity)c).persist()
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
		super.persist((GormEntity)entity, [validate: args.validate?:false ])
		//DaoMessage.created(entity) slows it down by about 15-20%
		return entity //[ok: true, entity: entity, message: DaoMessage.created(entity)]
	}

	@CompileDynamic
	def callBinderMethod(method, row){
		"${method}"(row)
	}

	@CompileDynamic
	void bindGrails(entity, row){
		entity.properties = row
	}

	@NotTransactional
	@CompileDynamic
	void insertGpars(List<List<Map>> batchList, Map args) {
		GParsPool.withPool(args.poolSize) {
			batchList.eachParallel { List<Map> batch ->
				insertBatch(batch, args)
			}
		}
	}

	void insertBatch(List<Map> batch, Map args) {
		for (Map record : batch) {
			insertRow(record, args)
		}
		DaoUtil.flushAndClear()
	}

	void insertRow(Map row, Map args) {
		insert(row, args)
	}


}
