package gpbench

import gorm.tools.dao.GormDao
import groovy.transform.CompileStatic

@CompileStatic
class CityMethodEventsDao implements GormDao<CityMethodEvents> {

    CityMethodEventsDao() {
        domainClass = CityMethodEvents
    }

    void beforeCreate(CityMethodEvents entity, Map params) {
        entity.dateCreatedUser = SecUtil.userId
        entity.lastUpdatedUser = SecUtil.userId

        entity.dateCreated = new Date()
        entity.lastUpdated = new Date()
    }

    void beforeUpdate(CityMethodEvents entity, Map params) {
        entity.lastUpdatedUser = SecUtil.userId
        entity.lastUpdated = new Date()
    }

}
