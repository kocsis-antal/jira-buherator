package hu.microsec.cegbir.kocsis.app

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
private const val ONLY_DEVELOPERS = "d"
private const val REPORT_TIME_SPENT = "rts"
private const val ROVAT = "rovat"

@EnableConfigurationProperties(JiraProperties::class, GitLabProperties::class)
@SpringBootApplication(scanBasePackages = ["hu.microsec.cegbir.kocsis.helper", "hu.microsec.cegbir.kocsis.jira", "hu.microsec.cegbir.kocsis.gitlab"])
open class CcBuheratorApp(
    val report: Report,
    val tester: Tester,
    val sprintHelper: Sprint, //    val taskTxt: TaskTxt,
    //    val taskHtml: TaskHtml,
    val releaseTxt: ReleaseTxt,
    val releaseHtml: ReleaseHtml,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val options = Options().apply {
            addOptionGroup(OptionGroup().apply {
                addOption(Option.builder(MOVE_TO_READY).longOpt("move2ready").desc("in current sprint moves new issues to ready state").build())
                addOption(Option.builder(CLOSE_REMAINED).longOpt("closeRemained").desc("close done issues remaind in progress").build())
                addOption(
                    Option.builder(TASKS).longOpt("tasks").hasArg().argName("format").desc("generates report about tasks (format: txt, html)").build()
                )
                addOption(
                    Option.builder(RELEASE).longOpt("release").hasArg().argName("format").desc("generates report about issues in release status (format: txt, html)").build()
                )
                addOption(
                    Option.builder(REPORT_TIME_SPENT).longOpt("reportTimeSpent").hasArg().argName("period").desc("generates report about time spent on issues in period (format: txt, html)").build()
                )
                addOption(Option.builder(ROVAT).hasArg().argName("parent id").desc("create rovat task hierarchy").build())
                addOption(Option.builder("test").longOpt("test").desc("just a test").build())
            })
            addOption(Option.builder(ONLY_DEVELOPERS).longOpt("developers").desc("show information only about developers").build())
        }

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
                }.jiraTasks(hasOption(ONLY_DEVELOPERS))

                hasOption(ROVAT) -> sprintHelper.createRovatIssues(getOptionValue(ROVAT))

                else -> {
                    HelpFormatter().printHelp(95, this.javaClass.simpleName.substringBefore("$$"), "", options, "", true)
                }
            }
        }
    }

    companion object {
        val projectMap = mapOf<String, String>(
            "BRIS Gateway" to "bris-gateway", //            "Cégbíróság" to "*",
            //            "Cégbírósági egyablakos rendszer" to "",
            "Cégbírósági és cégeljárás statisztika" to "microsec-occrstat", //            "Cégeljárás mediátor" to "",
            //            "Céghírnök mediátor" to "",
            //            "Cégközlöny mediátor" to "",
            //            "Cégnyilvántartási Portál" to "cnyp-*",
            "Fizetésképtelenségi Nyilvántartás" to "fk",
            "Hivatali Kapu letöltő alkalmazás" to "hivatali-kapu-gw",
            "Hármaska" to "microsec-cegbir",
            "Informatikai Vizsgálat" to "informatikai-vizsgalat",            //            "Ketteske" to "",
            "Kiadmány generáló alkalmazás" to "microsec-cegrovat-view-generator",
            "KKSZB Gateway" to "kkszb-gateway",            //            "Központi cégnév nyilvántartás - DBMS" to "",
            "MQ Series szerver" to "mq7", //            "Online céginformáció szolgáltatás" to "",
            "Teszt Cég Adatlétrehozó Alkalmazás" to "teszt-cegadat-karbantarto",            //            "VHKIR" to "*vhkir*",
            //            "ÁFSZ mediátor" to "",

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
    runApplication<CcBuheratorApp>(*args) {
        webApplicationType = org.springframework.boot.WebApplicationType.NONE
    }
}
