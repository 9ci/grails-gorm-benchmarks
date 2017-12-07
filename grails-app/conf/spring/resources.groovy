import grails.util.GrailsNameUtils
import groovy.io.FileType

// Place your Spring DSL code here
beans = {
	jdbcTemplate(org.springframework.jdbc.core.JdbcTemplate, ref("dataSource"))

    //see see https://docs.spring.io/spring/docs/5.0.2.RELEASE/spring-framework-reference/languages.html#dynamic-language-refreshable-beans

    xmlns lang:"http://www.springframework.org/schema/lang"

    File scriptsDir = new File("scripts")
    assert scriptsDir.exists()

    int listenerCount = application.config.benchmark.eventListenerCount

    if(listenerCount > 0){
        scriptsDir.eachFileMatch(FileType.FILES, ~/.*Listener\.groovy/) { File plugin ->
            String beanName = GrailsNameUtils.getPropertyName(plugin.name.replace('.groovy', ''))
            lang.groovy(id: beanName, 'script-source': "file:scripts/${plugin.name}", 'refresh-check-delay': 1000)
            println "refreshable bean $beanName created"
        }

        if(listenerCount > 1){
            println "adding extra listeners to benchmark load"
            (2..listenerCount).each { eid ->
                lang.groovy(id: "dummyListenerBean$eid", 'script-source': "file:scripts/DummyListenerBean.groovy", 'refresh-check-delay': 1000)
            }
        }
    } else {
        println "refreshableBeans for Listeners disabled"
    }

    int subCount = application.config.benchmark.eventSubscriberCount
    if(subCount > 0){
        println "Subscriber refreshableBeans enabled for $subCount, loading event beans from scripts"
        (1..subCount).each { eid ->
            scriptsDir.eachFileMatch(FileType.FILES, ~/.*Subscriber\.groovy/) { File plugin ->
                String beanName = GrailsNameUtils.getPropertyName(plugin.name.replace('.groovy', '')) + "$eid"
                lang.groovy(id: beanName, 'script-source': "file:scripts/${plugin.name}", 'refresh-check-delay': 1000){
                    lang.property( name:'subnum', value:eid)
                }
                println "refreshable bean $beanName created"
            }
        }
    }else{
        println "refreshableBeans for Listeners disabled"
    }

}

