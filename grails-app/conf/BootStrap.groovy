import gpbench.LoaderService

class BootStrap {

	LoaderService loaderService

	def init = { servletContext ->

		loaderService.with {
			//truncateTables()
			//runBenchMark()
			insertCity1MRows()
			load_rows_scrollable_resultset()
			load_rows_with_manual_paging()
		}

	}

}
