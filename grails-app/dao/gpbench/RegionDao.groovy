package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional

@Transactional
class RegionDao extends GormDaoSupport<Region> {
	Class domainClass = Region

	void beforeInsertSave(Region region, Map params) {
		region.id = params.id as Long
	}

	@GrailsCompileStatic
	public Region insertWithSetter(Map row) {
		Country country = Country.load(row['country']['id'] as Long);
		Region r = new Region()
		r.id = row.id as Long
		DomainUtils.copyDomain(r, row)
		r.country = country
		r.save(failOnError: true)
		return r
	}


}
