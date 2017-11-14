package gpbench

import grails.compiler.GrailsCompileStatic
import grails.transaction.Transactional

@Transactional
@GrailsCompileStatic
class CityIdGenDao extends CityDaoBase<CityIdGen> {
	//Class domainClass = City

	CityIdGenDao() {
		super(CityIdGen)
	}
}
