package gpbench

import gorm.tools.GormUtils
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.DaoDomainTrait
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.GormDaoSupport
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool
import org.grails.datastore.gorm.GormEntity

/**
 *vFuly dynamic compile with liberal use of defs and no typing
 */
@Transactional
class CityDynamicDao extends GormDaoSupport<CityDynamic> {

	GparsLoadService gparsLoadService

	Class domainClass = CityDynamic

	def bindWithCopyDomain(Map row) {
		def r = Region.load(row['region']['id'] as Long)
		def country = Country.load(row['country']['id'] as Long)
		def c = domainClass.newInstance()
		GormUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

	//See at the bottom why we need this doXX methods
	def insert( row, args) {
		def entity
		if(args.bindingMethod == 'grails'){
			entity = domainClass.newInstance()
			entity.properties = row
		}
		else if(args.bindingMethod == 'copy'){
			entity = bindWithCopyDomain(row)
		}
		else {
			entity = "${args.bindingMethod}"(row)
		}
		if (fireEvents) super.beforeInsertSave(entity, row)
		super.save(entity, [validate: args.validate?:false ])
		//DaoMessage.created(entity) slows it down by about 15-20%
		return entity //[ok: true, entity: entity, message: DaoMessage.created(entity)]
	}

}
