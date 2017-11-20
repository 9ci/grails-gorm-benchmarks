package gpbench

import bugwork.*
import gorm.tools.GormUtils
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.springframework.util.StopWatch

class BenchmarkDatabindingService {
    JsonReader jsonReader

    Long count = 111690
    Map props = [
        'name': 'test', 'shortCode':'test', state:"Gujarat", "countryName": "india", 'latitude':"10.10", 'longitude': "10.10"]
//    ,
//        'name2': 'test', 'shortCode2':'test', state2:"Gujarat", "country2": "india", 'latitude2':"10.10", 'longitude2': "10.10",
//        'name3': 'test', 'shortCode4':'test', state3:"Gujarat", "country3": "india", 'latitude3':"10.10", 'longitude3': "10.10",]

    boolean mute = false
    int warmupCityTimes = 3
    int loadCityTimes = 3
    List cities

    def runFat(){
        println "run load json file 3x number of fields"

        jsonReader._cache = [:]
        loadCities3xProps(loadCityTimes)
        println "Warm up logan  "
        mute = true
        (1..2).each {
            if(!mute) println "\n - setters or property copy on associations with 20 fields"
            useStaticSettersInDomain(CityFatAssoc)
            gormUtilsBindFast(CityFatAssoc)
            daoCreateNewFast(CityFatAssoc)
            useSetPropsFastIterate(CityFatAssoc)
            useDynamicSettersFat(CityFatAssoc)
            if(!mute) println " - Slower dynamic without @GrailsCompileStatic on domain"
            gormUtilsBindFast(CityFatAssocDynamic)
            mute = false
        }

        mute = true
        (1..2).each {
            if(!mute) println "\n - setters, copy and fast bind on simple no associations"
            gormUtilsBindFast(CityFatStatic)
            useDynamicSettersFat(CityFatStatic)
            if(!mute) println " - without CompileStatic on domains it slows down"
            gormUtilsBindFast(CityFat)
            useDynamicSettersFat(CityFat)
            if(!mute) println "\n Using Traits with CompileStatic is fast using setters or copy (fast bind)"
            gormUtilsBindFast(CityFatWithTraitStatic)
            useDynamicSettersFat(CityFatWithTraitStatic)
            mute = false
        }


        println "\n - Binding is slow -> Simple without associations"
        benchmarkDatabindingFile(CityFat)
        benchmarkDatabindingFile(CityFatStatic)
        println "\n - Binding with Traits are very slow"
        benchmarkDatabindingFile(CityFatWithTraitStatic)

        println "\n - Gets worse when Binding with associations with 20 fields"
        benchmarkDatabindingFile(CityFatAssoc)
        println " - And even slower when not using @CompileStatic"
        benchmarkDatabindingFile(CityFatAssocDynamic)

    }

    def runFileLoad(){
        mute = false
        jsonReader._cache = [:]
        loadCities(loadCityTimes)
        //count = 100_000
        println "\nbenchmark run load json file"
        benchmarkDatabindingFile(CitySimple)
        benchmarkDatabindingFile(CityBaselineDynamic)
        benchmarkDatabindingFile(CityBaseline)
        benchmarkDatabindingFile(CitySimpleWithTrait)
        benchmarkDatabindingFile(CitySimpleWithTraitStatic)

        copyPropsToDomainFromJson(CitySimple)
        copyPropsToDomainFromJson(CityBaselineDynamic)
        copyPropsToDomainFromJson(CityBaseline)
        copyPropsToDomainFromJson(CitySimpleWithTrait)
        copyPropsToDomainFromJson(CitySimpleWithTraitStatic)

        staticSettersFromJson(CitySimple)
        staticSettersFromJson(CityBaselineDynamic)
        staticSettersFromJson(CityBaseline)
        staticSettersFromJson(CitySimpleWithTrait)
        staticSettersFromJson(CitySimpleWithTraitStatic)

    }

    def runSimple(){
        println "warmup run"
        mute = true
        count = 10000
        benchmarkDatabinding(CitySimple)
        benchmarkDatabinding(CityBaselineDynamic)
        benchmarkDatabinding(CityBaseline)
        benchmarkDatabinding(CitySimpleStatic)
        benchmarkDatabinding(GrailsCompileStaticCity)
        benchmarkDatabinding(CitySimpleWithTrait)
        benchmarkDatabinding(CitySimpleWithTraitStatic)
        benchmarkManualAssignment(CitySimple)
        benchmarkManualAssignment(CityBaselineDynamic)
        benchmarkManualAssignment(CityBaseline)
        benchmarkManualAssignment(CitySimpleStatic)
        benchmarkManualAssignment(GrailsCompileStaticCity)
        benchmarkManualAssignment(CitySimpleWithTrait)
        benchmarkManualAssignment(CitySimpleWithTraitStatic)

        mute = false
        count = 111690
        println "benchmark run"
        benchmarkDatabinding(CitySimple)
        benchmarkDatabinding(CityBaselineDynamic)
        benchmarkDatabinding(CityBaseline)
        benchmarkDatabinding(CitySimpleStatic)
        benchmarkDatabinding(GrailsCompileStaticCity)
        benchmarkDatabinding(CitySimpleWithTrait)
        benchmarkDatabinding(CitySimpleWithTraitStatic)
        benchmarkManualAssignment(CitySimple)
        benchmarkManualAssignment(CityBaselineDynamic)
        benchmarkManualAssignment(CityBaseline)
        benchmarkManualAssignment(CitySimpleStatic)
        benchmarkManualAssignment(GrailsCompileStaticCity)
        benchmarkManualAssignment(CitySimpleWithTrait)
        benchmarkManualAssignment(CitySimpleWithTraitStatic)

    }

    void benchmarkDatabinding(Class domain) {
        StopWatch watch = new StopWatch()
        def instance = domain.newInstance()
        watch.start()
        for (int i in (1..count)) {
            instance.properties = props
        }
        watch.stop()
        println "Took ${watch.totalTimeSeconds} seconds to databind domain $domain.simpleName $count times"
    }

    void benchmarkManualAssignment(Class domain) {
        StopWatch watch = new StopWatch()
        def instance = domain.newInstance()
        watch.start()
        for (int i in (1..count)) {
            instance.name = props['name']
            instance.shortCode = props['shortCode']
            instance.state = props['state']
            instance.countryName = props['countryName']
            instance.latitude = props['latitude'] as BigDecimal
            instance.longitude = props['longitude'] as BigDecimal
        }
        watch.stop()
        if(!mute) println "Took ${watch.totalTimeSeconds} seconds to manually set props on domain $domain.simpleName $count times"
    }

    void benchmarkDatabindingFile(Class domain) {
        eachCity("benchmarkDatabindingFile", domain){ instance , Map row ->
            instance.properties = row
        }
    }

    void useStaticSettersInDomain(Class domain) {
        eachCity("useStaticSettersInDomain", domain){ instance , Map row ->
            instance.setPropsFast(row)
        }
    }

    void useSetPropsFastIterate(Class domain) {
        eachCity("setPropsFastIterate", domain){ instance , Map row ->
            setPropsFastIterate(instance, row)
        }
    }

    void gormUtilsBindFast(Class domain) {
        eachCity("gormUtilsBindFast", domain){ instance , Map row ->
            GormUtils.bindFast(instance, row)
            //setPropsFastIterate(instance, row)
        }
    }

    void daoCreateNewFast(Class domain) {
        eachCity("daoCreateNewFast", domain){ instance , Map row ->
            domain.dao.bindCreate(instance, row, [dataBinder:'fast'])
        }
    }

    void useSettersDynamicSimple(Class domain) {
        eachCity("useDynamicSettersFat", domain){ instance , Map row ->
            instance.name = row['name']
            instance.shortCode = row['shortCode']
            instance.state = row['state']
            instance.countryName = row['countryName']
            instance.latitude = row['latitude'] as BigDecimal
            instance.longitude = row['longitude'] as BigDecimal
        }
    }

    void useDynamicSettersFat(Class domain) {

        eachCity("useDynamicSettersFat", domain){ instance , Map row ->
            instance.name = row['name']
            instance.shortCode = row['shortCode']
            instance.state = row['state']
            instance.countryName = row['countryName']
            instance.latitude = row['latitude'] as BigDecimal
            instance.longitude = row['longitude'] as BigDecimal

            instance.name2 = row['name2']
            instance.shortCode2 = row['shortCode2']
            instance.state2 = row['state2']
            instance.countryName2 = row['countryName2']
            instance.latitude2 = row['latitude2'] as BigDecimal
            instance.longitude2 = row['longitude2'] as BigDecimal

            instance.name3 = row['name3']
            instance.shortCode3 = row['shortCode3']
            instance.state3 = row['state3']
            instance.countryName3 = row['countryName3']
            instance.latitude3 = row['latitude3'] as BigDecimal
            instance.longitude3 = row['longitude3'] as BigDecimal
            //instance.properties = row

            setAssociations(instance, "region", Region, row)
            setAssociations(instance, "country", Country, row)
            setAssociations(instance, "region2", Region, row)
            setAssociations(instance, "country2", Country, row)
            setAssociations(instance, "region3", Region, row)
            setAssociations(instance, "country3", Country, row)
        }
    }

    void setAssociations(instance, String key, Class assocClass, Map row){
        if(instance.hasProperty(key) && row[key] && row[key].id){
            instance[key] = assocClass.load(row[key].id)
        }
    }

    void eachCity(String msg, Class domain, Closure rowClosure) {
        StopWatch watch = new StopWatch()
        def instance = domain.newInstance()
        watch.start()
        for (Map row in cities) {
            rowClosure.call(instance, row)
        }
        watch.stop()
        if(!mute) println "${watch.totalTimeSeconds}s $msg $domain.simpleName | ${cities.size()} rows"
    }

    void loadCities(int mult) {
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
        for (Map row in cityfull) {
            row.state  = row.region.id
            row.countryName  = row.country.id
            row.remove('region')
            row.remove('country')
            //instance.properties = row
        }
        List repeatedCity = []
        (1..mult).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        cities = repeatedCity
        //cities = repeatedCity.collate(batchSize)
    }

    void loadCities3xProps(int mult) {
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
        for (Map row in cityfull) {
            row.region2 = [id:row.region.id]
            row.region3 = [id:row.region.id]

            row.country2 = [id:row.country.id]
            row.country3 = [id:row.country.id]

            row.state  = row.region.id
            row.countryName  = row.country.id
            row.state2  = row.region.id
            row.countryName2  = row.country.id
            row.state3  = row.region.id
            row.countryName3  = row.country.id

            row.name2 = row.name
            row.shortCode2 = row.shortCode
            row.latitude2 = row.latitude
            row.longitude2 = row.longitude

            row.name3 = row.name
            row.shortCode3 = row.shortCode
            row.latitude3 = row.latitude
            row.longitude3 = row.longitude
            //row.remove('region')
            //row.remove('country')
            //instance.properties = row
        }
        List repeatedCity = []
        (1..mult).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        cities = repeatedCity
        //cities = repeatedCity.collate(batchSize)
    }

    @CompileStatic
    Object setPropsFastIterate(Object obj, Map source, boolean ignoreAssociations = false) {
        //if (target == null) throw new IllegalArgumentException("Target is null")
        if (source == null) return

        def sapi = GormEnhancer.findStaticApi(obj.getClass())
        def properties = sapi.gormPersistentEntity.getPersistentProperties()
        for (PersistentProperty prop : properties){
            if(!source.containsKey(prop.name)) {
                continue
            }
            def sval = source[prop.name]
            if (prop instanceof Association && sval['id']) {
                if(ignoreAssociations) return
                def asocProp = (Association)prop
                def asc = GormEnhancer.findStaticApi(asocProp.associatedEntity.javaClass).load(sval['id'] as Long)
                obj[prop.name] = asc
            }
            else{
                obj[prop.name] = sval
            }
            //println prop
            //println "${prop.name}: ${obj[prop.name]} -> region:${obj.region}"
        }
        return obj
    }


}
