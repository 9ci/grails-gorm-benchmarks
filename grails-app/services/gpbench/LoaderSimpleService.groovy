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

	int loadIterations
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

		if(System.getProperty("warmup", "true").toBoolean()){
			//run benchmarks without displaying numbers to warmup jvm so we get consitent results
			//showing that doing this will drop results below on averge about 10%
			println "- Warming up JVM with initial pass"
			muteConsole = true
			loadIterations = 1

			runMultiCoreGrailsBaseline("")
			runMultiCore("", 'grails')

			loadIterations = System.getProperty("load.iterations", "10").toInteger()
		}
		muteConsole = false

		//real benchmarks starts here
		println "\n- Running Benchmarks"

		if(System.getProperty("runSingleThreaded", "false").toBoolean()){
			println "-- single threaded - no gpars"
			runBenchmark(new SimpleBatchInsertBenchmark(true))
			//runBenchmark(new CommitEachSaveBenchmark(true))
			//runBenchmark(new OneBigTransactionBenchmark(true))
		}

		runMultiCoreGrailsBaseline("## Pass 1 multi-thread - standard grails binding baseline")
		runMultiCore("## Pass 2 multi-thread", 'grails')
		runMultiCore("## Pass 3 multi-thread - copy props", 'copy')
		//runMultiCore("## Pass 3 multi-thread - copy props no validation", 'copy', false)
		//runMultiCore("Pass 3 multi-thread - standard grails binding with GrailsParameterMap", 'grails')
		runMultiThreadsOther("## Pass 4 sanity checks")

		//runBenchmark(new DataFlawQueueWithScrollableQueryBenchmark())

		System.exit(0)
	}

	void runMultiCoreGrailsBaseline(String msg) {
		logMessage "\n$msg"
		logMessage "  - Baseline to measure against"
		runBenchmark(new GparsBaselineBenchmark(CityBaseline))
		logMessage "  - using copy instead of binding, >20% faster"
		runBenchmark(new GparsBaselineBenchmark(CityBaseline,'copy'))
	}

	void runMultiCore(String msg, String bindingMethod = 'grails', boolean validation = true) {
		logMessage "\n$msg"
		logMessage "\n  - These should all run within 5% of baseline and each other"
		runBenchmark(new GparsBaselineBenchmark(CityAuditStampManual,bindingMethod,validation))
		runBenchmark(new GparsScriptEngineBenchmark(City,bindingMethod, validation))
		runBenchmark(new GparsDaoBenchmark(CityDynamic,bindingMethod, validation))
		runBenchmark(new BatchInsertWithDataFlowQueueBenchmark(bindingMethod, validation))

		logMessage "\n  - These run faster"
		runBenchmark(new GparsBaselineBenchmark(CityIdGen,bindingMethod, validation))
		runBenchmark(new RxJavaBenchmark(City, bindingMethod, validation))

		logMessage "\n  - These show performance issues"
		runBenchmark(new GparsDaoBenchmark(City,bindingMethod, validation))
		runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod, validation))
		runBenchmark(new GparsBaselineBenchmark(CityModelTrait, bindingMethod, validation))
	}

	void runMultiThreadsOther(String msg){
		println "\n$msg"

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
