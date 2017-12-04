import grails.util.GrailsNameUtils
import groovy.io.FileType

// Place your Spring DSL code here
beans = {
	jdbcTemplate(org.springframework.jdbc.core.JdbcTemplate, ref("dataSource"))

    xmlns lang:"http://www.springframework.org/schema/lang"

    File scriptsDir = new File("scripts")
    assert scriptsDir.exists()

    scriptsDir.eachFileMatch(FileType.FILES, ~/.*\.groovy/) { File plugin ->
        String beanName = GrailsNameUtils.getPropertyName(plugin.name.replace('.groovy', ''))
        lang.groovy(id: beanName, 'script-source': "file:scripts/${plugin.name}", 'refresh-check-delay': 100)
    }
}

