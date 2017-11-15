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

	private static int POOL_SIZE = 9
	private static int BATCH_SIZE = 50 //this should match the hibernate.jdbc.batch_size in datasources

	RegionDao regionDao
	CountryDao countryDao
	GrailsApplication grailsApplication

	CsvReader csvReader
	BenchmarkHelper benchmarkHelper

	@CompileStatic
	void runBenchMarks() {
		//use default poolsize, it can be updated by passing system property -Dgpars.poolsize=xx
		POOL_SIZE = PoolUtils.retrieveDefaultPoolSize()

		println "--- Environment info ---"
		println "Max memory: " + (Runtime.getRuntime().maxMemory() / 1024 )+ " KB"
		println "Total Memory: " + (Runtime.getRuntime().totalMemory() / 1024 )+ " KB"
		println "Free memory: " + (Runtime.getRuntime().freeMemory() / 1024 ) + " KB"
		println "Available processors: " + Runtime.getRuntime().availableProcessors()
		println "Gpars pool size: " + POOL_SIZE
		println "Autowire enabled: " + System.getProperty("autowire.enabled", "true")


		//load base country and city data which is used by all benchmarks
		benchmarkHelper.truncateTables()
		prepareBaseData()

		if(System.getProperty("warmup", "true").toBoolean()){
			//run benchmarks without displaying numbers to warmup jvm so we get consitent results
			//showing that doing this will drop results below on averge about 10%
			println "- Warmming up JVM"
			runBenchmark(new GparsBaselineBenchmark(CityAuditTrail))
			runBenchmark(new GparsBaselineBenchmark(CityBaseline))
			runBenchmark(new GparsDaoBenchmark(CityDynamic))
		}

		//real benchmarks starts here
		println "\n- Running Benchmarks"

		if(System.getProperty("runSingleThreaded", "false").toBoolean()){
			println "-- single threaded - no gpars"
			runBenchmark(new SimpleBatchInsertBenchmark(true))
			//runBenchmark(new CommitEachSaveBenchmark(true))
			//runBenchmark(new OneBigTransactionBenchmark(true))
		}

		runMultiCoreBinding("Pass 1")
		runMultiCoreCopyDomain("Pass 2")
		runMultiThreads("Pass 3")

		//runBenchmark(new DataFlawQueueWithScrollableQueryBenchmark())

		System.exit(0)
	}

	void runMultiCoreBinding(String msg) {
		println "\n********* $msg multi-core binding "
		println "\n- Grails Binding Compare"
		runBenchmark(new GparsBaselineBenchmark(CityBaseline))
		runBenchmark(new GparsBaselineBenchmark(CityModelTrait))
		runBenchmark(new GparsBaselineBenchmark(CityIdGen))
		runBenchmark(new GparsBaselineBenchmark(CityIdGenAssigned))

		runBenchmark(new GparsBaselineBenchmark(CityAuditTrail))

		runBenchmark(new GparsDaoBenchmark(City))
		runBenchmark(new GparsDaoBenchmark(CityDynamic))

		runBenchmark(new BatchInsertWithDataFlowQueueBenchmark())
		runBenchmark(new GparsScriptEngineBenchmark(City))
		runBenchmark(new RxJavaBenchmark(CityBaseline))
		runBenchmark(new RxJavaBenchmark(CityBaseline))

	}

	void runMultiCoreCopyDomain(String msg) {
		println "********* $msg multi-core no grails binding "

		println "\n- GormUtils.copyDomain insead of grails databinding "
		runBenchmark(new GparsBaselineBenchmark(CityBaseline,"bindWithCopy"))
		runBenchmark(new GparsDaoBenchmark(CityDynamic,"bindWithCopyDomain"))
		runBenchmark(new GparsBaselineBenchmark(CityModelTrait,"bindWithCopy"))
		runBenchmark(new BatchInsertWithDataFlowQueueBenchmark("copy"))
		runBenchmark(new GparsDaoBenchmark(City, "copy"))
		runBenchmark(new GparsScriptEngineBenchmark(City, "copy"))
		runBenchmark(new GparsDaoBenchmark(CityIdGen, "copy"))
		runBenchmark(new GparsBaselineBenchmark(CityIdGenAssigned,"copy"))

	}

	void runMultiThreads(String msg){
		println "********* $msg multi-threaded "

		println "\n - not much difference static and dynamic method calls"
		runBenchmark(new GparsDaoBenchmark(City,"bindWithSetters"))
		runBenchmark(new GparsDaoBenchmark(City,"bindWithCopyDomain"))

		println "\n   -dao with static setter method calls"
		runBenchmark(new GparsDaoBenchmark(City,"setter"))
		runBenchmark(new GparsDaoBenchmark(City,"copy"))

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
		autowire(benchmark)
		benchmark.run()

		if(!mute) println "${benchmark.timeTaken}s for $benchmark.description"
	}

	@CompileStatic
	void autowire(def bean) {
		grailsApplication.mainContext.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
	}

}
