package nsu.titov

import nsu.titov.cli.App
import nsu.titov.utils.UtilsConverters
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(CommandLine(App()).execute(*args))
}
