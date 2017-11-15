package gpbench

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class BootStrap {

    LoaderSimpleService LoaderSimpleService

    def init = { servletContext ->
        mockAuthentication()
        LoaderSimpleService.runBenchMarks()
    }

    void mockAuthentication() {
        GrailsUser grailsUser = new GrailsUser("test", "test", true,
                true, false, true, AuthorityUtils.createAuthorityList('ROLE_ADMIN'), 1)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, "test", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
    }
}
