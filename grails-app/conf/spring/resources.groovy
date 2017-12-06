import grails.util.GrailsNameUtils
import groovy.io.FileType

// Place your Spring DSL code here
beans = {
	jdbcTemplate(org.springframework.jdbc.core.JdbcTemplate, ref("dataSource"))

    //see see https://docs.spring.io/spring/docs/5.0.2.RELEASE/spring-framework-reference/languages.html#dynamic-language-refreshable-beans

    xmlns lang:"http://www.springframework.org/schema/lang"

    File scriptsDir = new File("scripts")
    assert scriptsDir.exists()

    scriptsDir.eachFileMatch(FileType.FILES, ~/.*\.groovy/) { File plugin ->
        String beanName = GrailsNameUtils.getPropertyName(plugin.name.replace('.groovy', ''))
        lang.groovy(id: beanName, 'script-source': "file:scripts/${plugin.name}", 'refresh-check-delay': 100)
    }
}

