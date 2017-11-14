package gpbench

import gorm.tools.GormUtils
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@Transactional
@GrailsCompileStatic
class CityDao extends CityDaoBase<City> {
	//Class domainClass = City

	CityDao() {
		super(City)
	}
}
