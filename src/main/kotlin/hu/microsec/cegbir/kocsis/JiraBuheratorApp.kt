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
private const val RELEASE = "r"

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
            addOption(Option.builder(RELEASE).longOpt("release").hasArg().argName("format").desc("report about issues in release status (format: txt, html)").build())
            addOption(Option.builder("t").longOpt("test").desc("just test").build())
        }

        val options = Options().addOptionGroup(optionsFunctions)
        DefaultParser().parse(options, args).run {
            when {
                hasOption("t") -> tester.test() // sprint
                hasOption(MOVE_TO_READY) -> sprintHelper.moveToReady()
                hasOption(CLOSE_REMAINED) -> sprintHelper.closeRemained() // release
                hasOption(RELEASE) -> getOptionValue(RELEASE).run {
                    when (this) {
                        "html" -> release.listHtml()
                        else -> release.listTxt()
                    }
                }

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
