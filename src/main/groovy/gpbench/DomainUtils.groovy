package gpbench

import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.util.Holders
import org.grails.core.artefact.DomainClassArtefactHandler

/**
 * Created by sudhir on 29/09/16.
 */
class DomainUtils {

	final static List<String> IGNORED_PROPERTIES = ["id", "version", "createdBy", "createdDate", "editedBy", "editedDate", "num"]


	public static def copyDomain(def instance, def old) {
		if(instance == null) throw new IllegalArgumentException("Copy is null")
		if(old == null) return null

		getDomainClass(instance.class).persistentProperties.each { GrailsDomainClassProperty dp ->
			if(IGNORED_PROPERTIES.contains(dp.name) || dp.identity) return
			if(dp.isAssociation()) return

			String name = dp.name
			instance[name] = old[name]
		}


		return instance
	}


	public static GrailsDomainClass getDomainClass(Class domain) {
		if(!Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, domain)) {
			throw new IllegalArgumentException(domain.name + " is not a domain class")
		} else {
			return Holders.grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domain.name)
		}
	}
}
