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

class LoaderService {
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
		println "IdGenerator enabled: " + System.getProperty("idgenerator.enabled", "false")


		//load base country and city data which is used by all benchmarks
		benchmarkHelper.truncateTables()
		prepareBaseData()

		//run two benchmarks without displaying numbers just to warmup jvm
		println "--- Warmming up JVM ---"
		runBenchmark(new GparsDaoBenchmark(false), true)
		runBenchmark(new GparsDaoBenchmark(true), true)


		//real benchmarks starts here
		println "--- Running Benchmarks ---"

		runBenchmark(new GparsDaoBenchmark(false))
		runBenchmark(new GparsDaoBenchmark(true))
		runBenchmark(new GparsBaselineBenchmark(true, true))
		runBenchmark(new GparsBaselineBenchmark(false, true))

		runBenchmark(new GparsBaselineBenchmark(true, false)) //with databinding, no validation
		runBenchmark(new GparsBaselineBenchmark(false, false)) //no data binding, no validation

		runBenchmark(new GparsThreadPerTransactionBenchmark(false))
		runBenchmark(new GparsThreadPerTransactionBenchmark(true))

		runBenchmark(new BatchInsertWithDataFlowQueueBenchmark(true))
		runBenchmark(new BatchInsertWithDataFlowQueueBenchmark(false))

		runBenchmark(new SimpleBatchInsertBenchmark(false))
		runBenchmark(new SimpleBatchInsertBenchmark(true))

		runBenchmark(new CommitEachSaveBenchmark(false))
		runBenchmark(new CommitEachSaveBenchmark(true))

		runBenchmark(new OneBigTransactionBenchmark(false))
		runBenchmark(new OneBigTransactionBenchmark(true))

		runBenchmark(new DataFlowQueueWithScrollableQueryBenchmark())

		System.exit(0)
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

		if(!mute) println "Took $benchmark.timeTaken seconds to run $benchmark.description"
	}

	@CompileStatic
	void autowire(def bean) {
		grailsApplication.mainContext.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
	}

}
