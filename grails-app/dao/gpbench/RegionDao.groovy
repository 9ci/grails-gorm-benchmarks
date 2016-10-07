package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional
import org.springframework.jdbc.core.JdbcTemplate

class RegionDao extends GormDaoSupport {
	Class domainClass = Region

	def beforeInsertSave(region, params) {
		region.id = params.id as Long
	}

	@GrailsCompileStatic
	public void insertWithSetter(Map row) {
		Country country = Country.load(row['country']['id'] as Long);
		Region r = new Region()
		r.id = row.id as Long
		DomainUtils.copyDomain(r, row)
		r.country = country
		r.save(failOnError: true)
	}


}
