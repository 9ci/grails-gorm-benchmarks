grails {
    plugin {
        audittrail{
            enabled = true
            //For a field to be added by the annotation at least on config setting needs to be present for that field.
            createdBy.field = "createdBy"  // createdBy is default
            createdBy.constraints = "nullable:false,display:false,editable:false,bindable:false"
            createdDate.field = "createdDate"
            createdDate.constraints = "nullable:false,display:false,editable:false,bindable:false"

            editedBy.field = "editedBy"  // createdBy is default
            editedBy.constraints = "nullable:false,display:false,editable:false,bindable:false"
            editedDate.field = "editedDate"
            editedDate.constraints = "nullable:false,display:false,editable:false,bindable:false"
        }
    }
}

grails.gorm.autowire = Boolean.valueOf(System.getProperty("autowire.enabled", "false"))
hibernate.jdbc.batch_size = System.getProperty("jdbc.batchSize", "100").toInteger()


grails {
    gorm.default.mapping = {
        id generator:'gorm.tools.idgen.SpringIdGenerator'
    }
}
