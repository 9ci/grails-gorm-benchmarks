package gpbench.listeners

import gorm.tools.dao.events.PreDaoCreateEvent
import gorm.tools.dao.events.PreDaoUpdateEvent
import gpbench.CityDaoPerisistenceEvents
import gpbench.SecUtil
import grails.events.annotation.gorm.Listener
import org.springframework.stereotype.Component

@Component
class CityDaoEventListener {

    @Listener(CityDaoPerisistenceEvents)
    void beforeCreate(PreDaoCreateEvent event) {
        CityDaoPerisistenceEvents entity = (CityDaoPerisistenceEvents)event.entityObject
        entity.dateCreatedUser = SecUtil.userId
        entity.lastUpdatedUser = SecUtil.userId

        entity.dateCreated = new Date()
        entity.lastUpdated = new Date()
    }

    @Listener(CityDaoPerisistenceEvents)
    void beforeUpdate(PreDaoUpdateEvent event) {
        CityDaoPerisistenceEvents entity = (CityDaoPerisistenceEvents)event.entityObject
        entity.lastUpdatedUser = SecUtil.userId
        entity.lastUpdated = new Date()
    }
}
