package gpbench

import gorm.tools.dao.DefaultGormDao
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CityDao extends DefaultGormDao<City> {

    CityDao(){
        super(City)
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
		c.persist()
		return c
	}

}
