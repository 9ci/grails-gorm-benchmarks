package gpbench

import gpbench.benchmarks.*
import gpbench.helpers.BenchmarkHelper
import gpbench.helpers.CsvReader
import grails.core.GrailsApplication
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.GormDaoSupport
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool
import groovyx.gpars.util.PoolUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

class LoaderSimpleService {
	static transactional = false

	static int POOL_SIZE = 9
	static int BATCH_SIZE = 50 //this should match the hibernate.jdbc.batch_size in datasources

	int loadIterations = System.getProperty("load.iterations", "10").toInteger()
	boolean muteConsole = false

	RegionDao regionDao
	CountryDao countryDao
	GrailsApplication grailsApplication

	CsvReader csvReader
	BenchmarkHelper benchmarkHelper

	//@CompileStatic
	void runBenchMarks() {
		//use default poolsize, it can be updated by passing system property -Dgpars.poolsize=xx
		POOL_SIZE = PoolUtils.retrieveDefaultPoolSize()
		loadIterations = System.getProperty("load.iterations", "10").toInteger()

		println "--- Environment info ---"
		println "Max memory: " + (Runtime.getRuntime().maxMemory() / 1024 )+ " KB"
		println "Total Memory: " + (Runtime.getRuntime().totalMemory() / 1024 )+ " KB"
		println "Free memory: " + (Runtime.getRuntime().freeMemory() / 1024 ) + " KB"
		println "Available processors: " + Runtime.getRuntime().availableProcessors()
		println "Gpars pool size: " + POOL_SIZE
		println "Autowire enabled: " + grailsApplication.config.grails.gorm.autowire


		//load base country and city data which is used by all benchmarks
		benchmarkHelper.truncateTables()
		prepareBaseData()

//		if(System.getProperty("warmup", "true").toBoolean()){
//			//run benchmarks without displaying numbers to warmup jvm so we get consitent results
//			//showing that doing this will drop results below on averge about 10%
//			println "- Warming up JVM with initial pass"
//			muteConsole = true
//			loadIterations = 1
//
//			//runMultiCoreGrailsBaseline("")
//			runMultiCore("", 'grails')
//            //runMultiCore("", 'copy')
//
//			loadIterations = System.getProperty("load.iterations", "10").toInteger()
//		}

		muteConsole = false

		//real benchmarks starts here
		println "\n- Running Benchmarks, loading ${ loadIterations * 37230} records each run"

		if(System.getProperty("runSingleThreaded", "false").toBoolean()){
			println "-- single threaded - no gpars"
			runBenchmark(new SimpleBatchInsertBenchmark(true))
		}

		//runMultiCoreGrailsBaseline("## Pass 1 multi-thread - standard grails binding baseline")
//        warmUpAndRun("# Gpars - standard grails binding with baseline",
//            'runMultiCoreGrailsBaseline', 'grails')

        warmUpAndRun("# Gpars - standard grails binding with baseline",
            "runMultiCoreBaselineCompare", 'grails')

        warmUpAndRun("  - Performance problems - standard grails binding with baseline",
            "runMultiCoreSlower", 'grails')

        warmUpAndRun("  - Faster Options - standard grails binding",
            "runMultiCoreFaster", 'grails')

        warmUpAndRun("# Gpars - copy to fields, no grails databinding",
            "runMultiCoreBaselineCompare", 'copy')

        warmUpAndRun("  - Performance problems - standard grails binding with baseline",
            "runMultiCoreSlower", 'copy')

        warmUpAndRun("  - Faster Options - copy to fields, no grails databinding",
            "runMultiCoreFaster", 'copy')

		runMultiThreadsOther("## Misc sanity checks")

		System.exit(0)
	}

    void warmUp(String runMethod, String bindingMethod){
        System.out.print("Warm up cycle ")
        muteConsole = true
        def oldLoadIterations = loadIterations
        loadIterations = 1
        //runMultiCoreGrailsBaseline("")
        "$runMethod"("", bindingMethod)
        //runMultiCore("", 'copy')
        loadIterations = oldLoadIterations
        muteConsole = false
        println ""
    }

    void warmUpAndRun(String msg, String runMethod, String bindingMethod = 'grails'){
        warmUp(runMethod, bindingMethod)
        "$runMethod"(msg, bindingMethod)
    }

    void runMultiCoreGrailsBaseline(String msg, String bindingMethod = 'grails', boolean validation = true) {
        logMessage "\n$msg"
        logMessage "  - Grails Basic Baseline to measure against"
        runBenchmark(new GparsBaselineBenchmark(CityBaseline,bindingMethod,validation))
    }

    void runMultiCoreBaselineCompare(String msg, String bindingMethod = 'grails', boolean validation = true) {
        logMessage "\n$msg"
        logMessage "  - Grails Basic Baseline to measure against"
        runBenchmark(new GparsBaselineBenchmark(CityBaseline,bindingMethod,validation))

        logMessage "\n  - These should all run within 5% of baseline and each other"
        runBenchmark(new GparsBaselineBenchmark(CityAuditStampManual,bindingMethod,validation))
        //runBenchmark(new GparsBaselineBenchmark(CityAuditStampAutowire,bindingMethod,validation))
        //runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod, validation))
        runBenchmark(new GparsDaoBenchmark(City,bindingMethod, validation))
        runBenchmark(new GparsScriptEngineBenchmark(City,bindingMethod, validation))
        runBenchmark(new GparsDaoBenchmark(CityDynamic,bindingMethod, validation))
    }

	void runMultiCoreFaster(String msg, String bindingMethod = 'grails', boolean validation = true) {
		logMessage "\n$msg"
		logMessage "  - These run faster"
		runBenchmark(new GparsBaselineBenchmark(CityIdGen,bindingMethod, validation))
		runBenchmark(new RxJavaBenchmark(City, bindingMethod, validation))
	}

    void runMultiCoreSlower(String msg, String bindingMethod = 'grails', boolean validation = true) {
        logMessage "\n$msg"
        logMessage "  - These show performance issues"
        runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod, validation))
        runBenchmark(new GparsBaselineBenchmark(CityModelTrait, bindingMethod, validation))
    }

	void runMultiThreadsOther(String msg){
		println "\n$msg"
        runBenchmark(new BatchInsertWithDataFlowQueueBenchmark('copy'))

		logMessage "  - using copy instead of binding and no validation, <10% faster"
		runBenchmark(new GparsBaselineBenchmark(CityBaseline, 'copy', false))

		println "\n - assign id inside domain with beforeValidate"
		runBenchmark(new GparsBaselineBenchmark(CityIdGenAssigned))

		println "\n  - not much difference between static and dynamic method calls"
		runBenchmark(new GparsDaoBenchmark(City,"setter"))
		runBenchmark(new GparsDaoBenchmark(City,"copy"))

		runBenchmark(new GparsDaoBenchmark(City,"bindWithSetters"))
		runBenchmark(new GparsDaoBenchmark(City,"bindWithCopyDomain"))

		new City().attached

	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void logMessage(String msg) {
		if(!muteConsole) {
			println msg
		} else{
			System.out.print("*")
		}
	}

	void prepareBaseData() {
		benchmarkHelper.executeSqlScript("test-tables.sql")
		List<List<Map>> countries = csvReader.read("Country").collate(BATCH_SIZE)
		List<List<Map>> regions = csvReader.read("Region").collate(BATCH_SIZE)
		insert(countries, countryDao)
		insert(regions, regionDao)

		DaoUtil.flushAndClear()

		assert Country.count() == 275
		assert Region.count() == 3953
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void insert(List<List<Map>> batchList, GormDaoSupport dao) {
		GParsPool.withPool(POOL_SIZE) {
			batchList.eachParallel { List<Map> batch ->
				City.withTransaction {
					batch.each { Map row ->
						dao.insert(row)
					}
					DaoUtil.flushAndClear()
				}
			}
		}
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void runBenchmark(AbstractBenchmark benchmark, boolean mute = false) {
		if(benchmark.hasProperty("poolSize")) benchmark.poolSize = POOL_SIZE
		if(benchmark.hasProperty("batchSize")) benchmark.batchSize = BATCH_SIZE
		if(benchmark.hasProperty("repeatedCityTimes")) benchmark.repeatedCityTimes = loadIterations

		autowire(benchmark)
		benchmark.run()
		logMessage "${benchmark.timeTaken}s for $benchmark.description"
		//if(!MUTE_CONSOLE) println "${benchmark.timeTaken}s for $benchmark.description"
	}


	@CompileStatic
	void autowire(def bean) {
		grailsApplication.mainContext.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
	}

}
