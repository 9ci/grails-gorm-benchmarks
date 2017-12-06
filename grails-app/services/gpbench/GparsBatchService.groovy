package gpbench

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
import groovyx.gpars.GParsPool

//@CompileStatic
class GparsBatchService {

    /**
     * Uses collate to break list into batches and then runs with eachParralel
     */
	void eachBatch(List<List<Map>> batchList, Map args, Closure clos) {
		//println "batchList size ${batchList.size()}"
		GParsPool.withPool(args.poolSize) {
			batchList.eachParallel { List<Map> batch ->
				//println "eachParallel batch size ${batch.size()}"
				processBatch(batch, args, clos)
			}
		}
	}

	@Transactional
	void processBatch(List<Map> batch,  Map args, Closure clos) {
		for (Map record : batch) {
			clos(record, args)
		}
		DaoUtil.flushAndClear()
	}

}
