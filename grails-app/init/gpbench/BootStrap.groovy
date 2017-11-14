package gpbench

class BootStrap {

    LoaderSimpleService LoaderSimpleService

    def init = { servletContext ->
        LoaderSimpleService.runBenchMarks()
    }
}
