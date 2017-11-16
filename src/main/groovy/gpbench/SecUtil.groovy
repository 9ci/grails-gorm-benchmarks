package gpbench

import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.util.Holders
import groovy.transform.CompileStatic
import grails.plugin.springsecurity.SpringSecurityService

@CompileStatic
class SecUtil {

    static Long getUserId(){
        def secServ = (SpringSecurityService) Holders.applicationContext.getBean("springSecurityService")
        ((GrailsUser)secServ.principal).id as Long
    }
}
