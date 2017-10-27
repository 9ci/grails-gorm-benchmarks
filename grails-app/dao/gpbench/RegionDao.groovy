package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional

@Transactional
class RegionDao extends GormDaoSupport<Region> {
	Class domainClass = Region

	void beforeInsertSave(Region region, Map params) {
		region.id = params.id as Long
	}

	@NotTransactional
	Region bind(Map row) {
		Country country = Country.load(row['country']['id'] as Long);
		Region r = new Region()
		r.id = row.id as Long
		DomainUtils.copyDomain(r, row)
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
