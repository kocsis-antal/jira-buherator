package hu.microsec.cegbir.kocsis

import hu.microsec.cegbir.kocsis.gitlab.GitLabProperties
import hu.microsec.cegbir.kocsis.helper.ReleaseHtml
import hu.microsec.cegbir.kocsis.helper.ReleaseTxt
import hu.microsec.cegbir.kocsis.helper.Report
import hu.microsec.cegbir.kocsis.helper.Sprint
import hu.microsec.cegbir.kocsis.helper.Tester
import hu.microsec.cegbir.kocsis.jira.JiraProperties
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
private const val TASKS = "t"
private const val RELEASE = "r"
private const val REPORT_TIME_SPENT = "rts"

@EnableConfigurationProperties(JiraProperties::class, GitLabProperties::class)
@SpringBootApplication()
open class CcBuheratorApp(
    val report: Report,
    val tester: Tester,
    val sprintHelper: Sprint, //    val taskTxt: TaskTxt,
    //    val taskHtml: TaskHtml,
    val releaseTxt: ReleaseTxt,
    val releaseHtml: ReleaseHtml,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val optionsFunctions = OptionGroup().apply {
            addOption(Option.builder(MOVE_TO_READY).longOpt("move2ready").desc("in current sprint moves new issues to ready state").build())
            addOption(Option.builder(CLOSE_REMAINED).longOpt("closeRemained").desc("close done issues remaind in progress").build())
            addOption(Option.builder(TASKS).longOpt("tasks").hasArg().argName("format").desc("generates report about tasks (format: txt, html)").build())
            addOption(Option.builder(RELEASE).longOpt("release").hasArg().argName("format").desc("generates report about issues in release status (format: txt, html)").build())
            addOption(Option.builder(REPORT_TIME_SPENT).longOpt("reportTimeSpent").hasArg().argName("period").desc("generates report about time spent on issues in period (format: txt, html)").build())
            addOption(Option.builder("test").longOpt("test").desc("just a test").build())
        }

        val options = Options().addOptionGroup(optionsFunctions)
        DefaultParser().parse(options, args).run {
            when {
                hasOption("test") -> tester.test() // sprint
                hasOption(REPORT_TIME_SPENT) -> report.timeSpent(getOptionValue(REPORT_TIME_SPENT))
                hasOption(MOVE_TO_READY) -> sprintHelper.moveToReady()
                hasOption(CLOSE_REMAINED) -> sprintHelper.closeRemained() // release
                //                hasOption(TASKS) -> getOptionValue(TASKS).run {
                //                    when (this) {
                //                        "html" -> taskHtml
                //                        else -> taskTxt
                //                    }
                //                }.jiraTasks()
                hasOption(RELEASE) -> getOptionValue(RELEASE).run {
                    when (this) {
                        "html" -> releaseHtml
                        else -> releaseTxt
                    }
                }.jiraTasks()

                else -> {
                    HelpFormatter().printHelp(95, this.javaClass.simpleName.substringBefore("$$"), "", options, "", true)
                }
            }
        }
    }

    companion object {
        val projectMap = mapOf<String, String>(
            "BRIS Gateway" to "bris-gateway", //            "C??gb??r??s??g" to "*",
            //            "C??gb??r??s??gi egyablakos rendszer" to "",
            "C??gb??r??s??gi ??s c??gelj??r??s statisztika" to "microsec-occrstat", //            "C??gelj??r??s medi??tor" to "",
            //            "C??gh??rn??k medi??tor" to "",
            //            "C??gk??zl??ny medi??tor" to "",
            //            "C??gnyilv??ntart??si Port??l" to "cnyp-*",
            "Fizet??sk??ptelens??gi Nyilv??ntart??s" to "fk",
            "Hivatali Kapu let??lt?? alkalmaz??s" to "hivatali-kapu-gw",
            "H??rmaska" to "microsec-cegbir",
            "Informatikai Vizsg??lat" to "informatikai-vizsgalat",            //            "Ketteske" to "",
            "Kiadm??ny gener??l?? alkalmaz??s" to "microsec-cegrovat-view-generator",
            "KKSZB Gateway" to "kkszb-gateway",            //            "K??zponti c??gn??v nyilv??ntart??s - DBMS" to "",
            "MQ Series szerver" to "mq7", //            "Online c??ginform??ci?? szolg??ltat??s" to "",
            "Teszt C??g Adatl??trehoz?? Alkalmaz??s" to "teszt-cegadat-karbantarto",            //            "VHKIR" to "*vhkir*",
            //            "??FSZ medi??tor" to "",

            //            "CNY Inbox" to "",
            //            "CNY Others" to "",
            //            "CNY Product Owner Project" to "",
        )
    }
}

//        // get c option value
//        val countryCode = cmd.getOptionValue("c")
//        if (countryCode == null) { // print default date
//        } else { // print date for country specified by countryCode
//        }

fun main(args: Array<String>) {
    runApplication<CcBuheratorApp>(*args)
}
