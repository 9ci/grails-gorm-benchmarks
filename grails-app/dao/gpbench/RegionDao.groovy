package gpbench

import gorm.tools.GormUtils
import gorm.tools.dao.DefaultGormDao
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

@Transactional
@GrailsCompileStatic
class RegionDao extends DefaultGormDao<Region> {

	void beforeCreate(Region region, Map params) {
		region.id = params.id as Long
	}

	@NotTransactional
	Region bind(Map row) {
		Country country = Country.load(row['country']['id'] as Long);
		Region r = new Region()
		r.id = row.id as Long
		GormUtils.copyDomain(r, row)
		r.country = country
		return r
	}

	@GrailsCompileStatic
	public Region insertWithSetter(Map row) {
		Region r = bind(row)
		r.save(failOnError:true)
		return r
	}

	@GrailsCompileStatic
	public Region insertWithoutValidation(Map row) {
		Region r = bind(row)
		r.save(false)
		return r
	}
}
