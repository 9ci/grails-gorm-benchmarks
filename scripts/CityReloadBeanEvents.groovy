import gpbench.CityAuditStampEvents
import gpbench.SecUtil
import grails.events.annotation.gorm.Listener
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

@CompileStatic
class CityReloadBeanEvents {

    @Listener(CityAuditStampEvents)
    void beforeInsert(PreInsertEvent event) {
        //println "BeofreInsert: " + event.entityObject.class.simpleName
        EntityAccess ea = event.entityAccess
        ea.setProperty("dateCreatedUser", SecUtil.userId)
        ea.setProperty("dateCreated", new Date())
    }

    @Listener(CityAuditStampEvents)
    void beforeUpdate(PreUpdateEvent event) {
        //println "beforeUpdate"
        EntityAccess ea = event.entityAccess
        ea.setProperty("lastUpdatedUser", SecUtil.userId)
        ea.setProperty("lastUpdated", new Date())
    }

}
