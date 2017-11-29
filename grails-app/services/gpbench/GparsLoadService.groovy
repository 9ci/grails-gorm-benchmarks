package gpbench

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
import groovyx.gpars.GParsPool

//@CompileStatic
class GparsLoadService {

	void insertGpars(List<List<Map>> batchList, Map args, Closure insertRowCl) {
		//println "batchList size ${batchList.size()}"
		GParsPool.withPool(args.poolSize) {
			batchList.eachParallel { List<Map> batch ->
				//println "eachParallel batch size ${batch.size()}"
				insertBatch(batch, args, insertRowCl)
			}
		}
	}

	@Transactional
	void insertBatch(List<Map> batch,  Map args, Closure insertRowCl) {
		for (Map record : batch) {
			insertRowCl(record, args)
		}
		DaoUtil.flushAndClear()
	}

}
