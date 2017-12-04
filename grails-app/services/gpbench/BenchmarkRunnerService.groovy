package gpbench

import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.GormDao
import gpbench.benchmarks.*
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.helpers.BenchmarkHelper
import gpbench.helpers.CsvReader
import grails.core.GrailsApplication
import gorm.tools.dao.DaoUtil
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.util.PoolUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

class BenchmarkRunnerService {

    GparsLoadService gparsLoadService
	static transactional = false

	static int POOL_SIZE = PoolUtils.retrieveDefaultPoolSize()

    //this should match the hibernate.jdbc.batch_size in datasources
	static int BATCH_SIZE = System.getProperty("jdbc.batchSize", "50").toInteger()

	int loadIterations = System.getProperty("load.iterations", "3").toInteger()
    int warmupCycles = 1
	boolean muteConsole = false

	RegionDao regionDao
	CountryDao countryDao
	GrailsApplication grailsApplication

	CsvReader csvReader
	BenchmarkHelper benchmarkHelper

	//@CompileStatic
	void runBenchMarks() {
		println "--- Environment info ---"
		//println "Max memory: " + (Runtime.getRuntime().maxMemory() / 1024 )+ " KB"
		//println "Total Memory: " + (Runtime.getRuntime().totalMemory() / 1024 )+ " KB"
		//println "Free memory: " + (Runtime.getRuntime().freeMemory() / 1024 ) + " KB"
		println "Available processors: " + Runtime.getRuntime().availableProcessors()
		println "Gpars pool size: " + POOL_SIZE
        println "batch size: " + grailsApplication.config.hibernate.jdbc.batch_size
		println "Autowire enabled: " + grailsApplication.config.grails.gorm.autowire


		//load base country and city data which is used by all benchmarks
		benchmarkHelper.truncateTables()
		prepareBaseData()

		muteConsole = false

		//real benchmarks starts here
		println "\n- Running Benchmarks, loading ${ loadIterations * 37230} records each run"

		if(System.getProperty("runSingleThreaded", "false").toBoolean()){
			println "-- single threaded - no gpars"
			runBenchmark(new SimpleBatchInsertBenchmark(true))
		}

		//runMultiCoreGrailsBaseline("## Pass 1 multi-thread - standard grails binding baseline")
        //warmUpAndRun("# Gpars - standard grails binding with baseline",
        //    'runMultiCoreGrailsBaseline', 'grails')

        warmUpAndRun("### Gpars - Assign Properties, no grails databinding",
            "runMultiCoreBaselineCompare", 'fast')

        warmUpAndRun("### Gpars - standard grails binding with baseline Slower",
            "runMultiCoreBaselineCompare", 'grails')

        warmUpAndRun("  - Performance problems go away without databinding on traits",
            "runMultiCoreSlower", 'fast')

        warmUpAndRun("  - Performance problems - standard grails binding with baseline",
            "runMultiCoreSlower", 'grails')

		runMultiThreadsOther("## Misc sanity checks")

		System.exit(0)
	}

    void warmUp(String runMethod, String bindingMethod){
        System.out.print("Warm up cycle ")
        muteConsole = true
        def oldLoadIterations = loadIterations
        loadIterations = 1
        //runMultiCoreGrailsBaseline("")
        (1..warmupCycles).each{
            "$runMethod"("", bindingMethod)
        }
        loadIterations = oldLoadIterations
        muteConsole = false
        println ""
    }

    void warmUpAndRun(String msg, String runMethod, String bindingMethod = 'grails'){
        warmUp(runMethod, bindingMethod)
        //warmUp(runMethod, bindingMethod)
        "$runMethod"(msg, bindingMethod)
    }


    void runMultiCoreBaselineCompare(String msg, String bindingMethod = 'grails') {
        runBenchmark(new GparsFatBenchmark(CityFat,bindingMethod))
        logMessage "\n$msg"
        logMessage "  - Grails Basic Baseline to measure against"
        runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic,bindingMethod))
        runBenchmark(new GparsBaselineBenchmark(CityBaseline,bindingMethod))
        runBenchmark(new GparsBaselineBenchmark(City, bindingMethod))

        runBenchmark(new GparsFatBenchmark(CityFatDynamic,bindingMethod))
        logMessage "  - benefits of CompileStatic are more obvious with more fields"
        runBenchmark(new GparsFatBenchmark(CityFat,bindingMethod))

        logMessage "\n  - These should all run within about 5% of baseline and each other"
        runBenchmark(new GparsDaoBenchmark(City, bindingMethod))
        runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod))
        runBenchmark(new GparsBaselineBenchmark(CityAuditStampEvents, bindingMethod))
        runBenchmark(new RxJavaBenchmark(City, bindingMethod))
        runBenchmark(new GparsScriptEngineBenchmark(City,bindingMethod))
    }

    void runMultiCoreSlower(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        logMessage "  - Traits have big performance issues with databinding but may be fine with assignment"
        runBenchmark(new GparsBaselineBenchmark(CityModelTrait, bindingMethod))
        logMessage "  - Fully Dynamic should be slower than statically compiled counter parts"
        runBenchmark(new GparsDaoBenchmark(CityDynamic,bindingMethod))
    }

	void runMultiThreadsOther(String msg){
		println "\n$msg"
        runBenchmark(new BatchInsertWithDataFlowQueueBenchmark('fast'))

		logMessage "  - using copy instead of binding and no validation, <10% faster"
		runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic, 'fast', false))

		println "\n - assign id inside domain with beforeValidate"
		//runBenchmark(new GparsBaselineBenchmark(CityIdGenAssigned))

		println "\n  - not much difference between static and dynamic method calls"
//		runBenchmark(new GparsDaoBenchmark(City,"setter"))
//		runBenchmark(new GparsDaoBenchmark(City,"fast"))
//
//		runBenchmark(new GparsDaoBenchmark(City,"bindWithSetters"))
//		runBenchmark(new GparsDaoBenchmark(City,"bindWithCopyDomain"))

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
	void insert(List<List<Map>> batchList, GormDao dao) {
        gparsLoadService.insertGpars(batchList, [poolSize:POOL_SIZE]){ Map row, args ->
            dao.create(row)
        }
//		GParsPool.withPool(POOL_SIZE) {
//			batchList.eachParallel { List<Map> batch ->
//				City.withTransaction {
//					batch.each { Map row ->
//						dao.insert(row)
//					}
//					DaoUtil.flushAndClear()
//				}
//			}
//		}
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void runBenchmark(AbstractBenchmark benchmark, boolean mute = false) {
		if(benchmark.hasProperty("poolSize")) benchmark.poolSize = POOL_SIZE
		if(benchmark.hasProperty("batchSize")) benchmark.batchSize = BATCH_SIZE
		if(benchmark.hasProperty("repeatedCityTimes")) benchmark.repeatedCityTimes = loadIterations
        if(benchmark.hasProperty("disableSave")) benchmark.disableSave = disableSave

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
