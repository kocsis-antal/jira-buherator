package hu.microsec.cegbir.kocsis.helper

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup
import com.atlassian.jira.rest.client.api.domain.Issue
import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.joda.time.DateTime
import org.joda.time.Hours
import org.springframework.stereotype.Service
import java.io.File
import java.io.PrintStream

@Service
class Report(val buherator: JiraBuherator) {
    fun timeSpent(period: String) {
        val o = PrintStream(File("${period.replace(Regex(".+\"(.+)\""), "$1")}.html"))
        val console = System.out
        System.setOut(o)

        println(
            """
            <head>
                <meta charset="UTF-8">
                <style>
                table, th, td {
                  border: 1px solid black;
                  border-collapse: collapse;
                }
                th, td {
                  padding: 5px;
                }
                </style>
            </head>
            <body>
            <h1>$period</h1>
            <p>Összegző kimutatás a periódusban végzett feladatokról, az <i>Epic-Issue-SubTask</i> vonalat bejárva, és a <i>SubTask</i> szinten történő jira mozgást figyelembe véve:
                <ul>
                    <li>Munka elkezdése: <b>New/ToDo</b> státuszból való kilépés</li>
                    <li>Munka befejezése: <b>Done</b> státuszba való belépés</li>
                </ul>
            </p>
            <h2>Feladatok részletezve</h2>
        """.trimIndent()
        )

        val epics = buherator.select("""type = Epic AND project = "CNY Product Owner Project" AND $period""")
        val epicsWork = mutableMapOf<Issue, Int>()

        var sumEpic = 0
        epics.issues.forEach {

            println("<h3>${it.summary} (${showLink(it.key)})</h3>")
            when (it.status.name) {
                Statuses.NEW.statusName, Statuses.APPROVED.statusName -> println("Nem kezdődött el.")
                else -> {
                    val epicIssues = buherator.select(""""Epic Link" = ${it.key}""")
                    println("Státusz: <i>${it.status.name}</i>, feladat: ${epicIssues.total} db (ebből befejezve: ${epicIssues.issues.filter { it.status.name == Statuses.DONE.statusName }.size} db)")

                    if (epicIssues.issues.count() > 0) {
                        println("<details> <summary>Feladatok</summary>")
                        println("<ul>")

                        var sumIssue = 0
                        epicIssues.issues.forEach {
                            val doneSubtask = it.subtasks?.filter { it.status.name == Statuses.DONE.statusName }.orEmpty() //                        val time = doneSubtask.map { buherator.getIssue(it.issueKey) }.map { it to it.fields.firstOrNull { it.id == "resolutiondate" } }

                            print("<li>")
                            if (it.status.name != Statuses.DONE.statusName) print("<i>")
                            print(it.summary)
                            if (it.status.name != Statuses.DONE.statusName) print("</i>")
                            print(" (${showLink(it.key)}, státusz: <i>${it.status.name}</i>, sub-task: ${it.subtasks?.count()} db - ebből befejezve: ${doneSubtask.size} db)")
                            if (doneSubtask.size > 0) {
                                println("<details> <summary>SubTaskok</summary>")

                                println("<table >")
                                println("<tr><th>SubTask</th><th>assignee</th><th>óra</th><th>nap (óra/8)</th>")

                                var sumSubtask = 0
                                doneSubtask.map { buherator.getIssueWithChangelog(it.issueKey) }.forEach {
                                    val work = calculateDuration(it.changelog)
                                    sumSubtask += work

                                    println("<tr><td>${it.summary} (${showLink(it.key)})</td><td>${it.assignee?.name}</td><td style='text-align: center;'>${work}</td><td style='text-align: center;'>${work / 8}</td>")
                                }
                                println("</table>")
                                println("</details>")
                                println("<b>összesen: ${sumSubtask / 8} nap</b> ($sumSubtask óra)")
                                sumIssue += sumSubtask
                            }
                            println("</li>")
                        }
                        println("</ul>")
                        println("</details>")
                        println("<b>összesen: ${sumIssue / 8} nap</b> ($sumIssue óra)")

                        sumEpic += sumIssue
                        epicsWork.put(it, sumIssue)
                    }
                    println()
                }
            }
        }

        println("<hr>")

        println("<h2>Összegzés</h2>")
        println("<p>A $period alatt megjelölt feladatok teljes összegzése.</p>")

        println("<h3>Feladatok</h3>")
        println("<b>Összes feladat száma: ${epics.total} db</b><br>")
        println("Nem elkezdett feladat: ${epics.issues.filter { it.status.name != Statuses.NEW.statusName && it.status.name != Statuses.APPROVED.statusName }.size} db<br>")
        println("Teljesen lezárt feladat: ${epics.issues.filter { it.status.name == Statuses.DONE.statusName }.size} db<br>")

        println("<h3>Ráfordított munka</h3>")
        println("<b>összes idő: ${sumEpic / 8} embernap</b> ($sumEpic emberóra)<br>")
        println("csapatra vetítve (4 ember esetén): ${sumEpic / 8 / 4} embernap/ember (ez alapján ${sumEpic / 8 / 4 / 5} hét munka)<br>")
        println("csapatra vetítve (5 ember esetén): ${sumEpic / 8 / 5} embernap/ember (ez alapján ${sumEpic / 8 / 5 / 5} hét munka)<br>")
        println("csapatra vetítve (6 ember esetén): ${sumEpic / 8 / 6} embernap/ember (ez alapján ${sumEpic / 8 / 6 / 5} hét munka)<br>")

        println("<br><br><table>")
        println("<tr><th>Link</th><th>Feladat</th><th>Embernap</th></tr>")
        epicsWork.toList().sortedByDescending { it.second }.toMap().forEach {
            println("<tr><td>${showLink(it.key.key)}</td><td>${it.key.summary}</td><td style='text-align: center;'>${it.value / 8}</td></tr>")
        }
        println("</table>")

        println("</body>")
        System.setOut(console)
    }

    private fun showLink(key: String) = """<a href="https://jira.intra.microsec.hu/browse/$key" target="_blank">$key</a>"""
    private fun calculateDuration(changelog: MutableIterable<ChangelogGroup>?): Int {
        var work = 0

        var startDate: DateTime? = null
        var endDate: DateTime? = null
        changelog?.forEach {
            it.items.forEach { item ->
                if (item.field == "status") {
                    if (item.fromString == "New") startDate = it.created
                    if (item.fromString == "ToDo") startDate = it.created //                    if (item.toString == "Code review") endDate = it.created
                    if (item.toString == "Done") endDate = it.created
                }
            }
        }

        if (startDate == null || endDate == null) return 0

        var days = 0
        var hours = Hours.hoursBetween(startDate, endDate).hours // joda nélkül: //        var hours = Long.max(ChronoUnit.HOURS.between(startDate, endDate), 1)

        while (hours > 8) {
            days++
            hours -= if (hours > 24) 24 else 8
        }
        work += (days * 8) + hours + 1

        //        val days = Days.daysBetween(startDate, endDate)
        //        var hours = max(Hours.hoursBetween(startDate, endDate).hours, 1)
        //        if (days == 0) {
        //            work += hours
        //        } else
        //            work += (days * 8) + (hours - days * 24)

        return work
    }
}
