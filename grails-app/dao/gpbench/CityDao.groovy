package gpbench

import gorm.tools.GormUtils
import grails.plugin.dao.DaoDomainTrait
import grails.plugin.dao.GormDaoSupport
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@Transactional
@CompileStatic
class CityDao extends GormDaoSupport<City> {
	Class domainClass = City

	@NotTransactional
	//@CompileDynamic
	City bindWithCopyDomain(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)
		City c = new City()
		GormUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

	@NotTransactional
	//@CompileDynamic
	City bindWithSetters(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		City c = new City()
		c.name = row.name
		c.shortCode = row.name

		c.latitude = row.latitude as BigDecimal
		c.longitude = row.longitude as BigDecimal

		c.region = r
		c.country = country
		return c
	}

	City insertWithSetter(Map row) {
		City c = bindWithSetters(row)
		((DaoDomainTrait)c).persist()
		return c
	}

	City insert(Map row, Map args) {
		City entity
		if(args.bindingMethod == 'grails'){
			return (City) super.insert(row).entity
		}
		else if(args.bindingMethod == 'copy'){
			entity = bindWithCopyDomain(row)
		}
		else if(args.bindingMethod == 'setter'){
			entity = bindWithSetters(row)
		} else {
			entity = (City) callBinderMethod(args.bindingMethod, row)
		}
		if (fireEvents) super.beforeInsertSave((GormEntity)entity, row)
		super.save((GormEntity)entity, [validate: args.validate?:false ])
		//DaoMessage.created(entity) slows it down by about 15-20%
		return entity
	}

	@CompileDynamic
	def callBinderMethod(method, row){
		"${method}"(row)
	}

}
