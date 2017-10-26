package gpbench

class BootStrap {
    LoaderService loaderService

    def init = { servletContext ->
        loaderService.with {
            truncateTables()
            runBenchMark()
            //load_rows_scrollable_resultset_two()
            //load_rows_with_manual_paging()
        }
    }
}
