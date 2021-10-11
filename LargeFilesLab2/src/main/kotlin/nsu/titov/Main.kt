package nsu.titov

import nsu.titov.cli.App
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(CommandLine(App()).execute(*args))
}