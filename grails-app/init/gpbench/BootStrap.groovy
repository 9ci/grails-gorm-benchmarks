package gpbench

class BootStrap {
    LoaderService loaderService

    def init = { servletContext ->
        loaderService.runBenchMarks()
    }
}
