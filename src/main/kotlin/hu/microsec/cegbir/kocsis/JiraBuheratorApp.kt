package hu.microsec.cegbir.kocsis

import hu.microsec.cegbir.kocsis.helper.Release
import hu.microsec.cegbir.kocsis.helper.Sprint
import hu.microsec.cegbir.kocsis.helper.Tester
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionGroup
import org.apache.commons.cli.Options
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

private const val MOVE_TO_READY = "m"
private const val CLOSE_REMAINED = "c"

@EnableConfigurationProperties(JiraProperties::class)
@SpringBootApplication()
open class JiraBuheratorApp(
    val tester: Tester,
    val sprintHelper: Sprint,
    val release: Release,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val optionsFunctions = OptionGroup().apply {
            addOption(Option.builder(MOVE_TO_READY).longOpt("move2ready").desc("move in current sprint to ready").build())
            addOption(Option.builder(CLOSE_REMAINED).longOpt("closeRemained").desc("close done issues remaind in progress").build())
            addOption(Option.builder("t").longOpt("test").desc("move in current sprint to ready").build())
        }

        val options = Options().addOptionGroup(optionsFunctions)
        DefaultParser().parse(options, args).run {
            when {
                hasOption("t") -> tester.test()
                hasOption(MOVE_TO_READY) -> sprintHelper.moveToReady()
                hasOption(CLOSE_REMAINED) -> sprintHelper.closeRemained()

                else -> {
                    HelpFormatter().printHelp(this.javaClass.simpleName.substringBefore("$$"), options, true)
                }
            }
        }
    }
}

//        // get c option value
//        val countryCode = cmd.getOptionValue("c")
//        if (countryCode == null) { // print default date
//        } else { // print date for country specified by countryCode
//        }

fun main(args: Array<String>) {
    runApplication<JiraBuheratorApp>(*args)
}
