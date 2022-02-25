package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.JiraBuherator
import hu.microsec.cegbir.kocsis.Statuses
import org.springframework.stereotype.Service

@Service
class Release(val buherator: JiraBuherator) {
    fun listTxt() {
        list(false)
    }

    fun listHtml() {
        list(true)
    }

    protected fun list(toHtml: Boolean) {
        val toReleaseBranches = arrayOf("master", "development")

        val result = buherator.select("filter = \"CC projects filter\" AND type in standardIssueTypes() AND status = ${Statuses.RELEASE.statusName}")
        println("Found ${result.total} issues.")
        result.issues.groupBy { it.project }.forEach {
            println(applyTag(toHtml, "Project: ${it.key.name?.replace("CNY - ", "")}", "h1"))

            val (toRelease, toDo) = it.value.filter { it.fixVersions?.none { it.isReleased } != null }.partition { toReleaseBranches.contains(it.fixVersions?.firstOrNull()?.name) }

            if (toRelease.isNotEmpty()) {
                println(applyTag(toHtml, "Kiadásra vár", "h2"))
                if (toHtml) println("<ul>")
                toRelease.forEach {
                    println("\t" + applyTag(toHtml, "${it.key}: ${it.summary}", "li"))
                }
                if (toHtml) println("</ul>")
            } else println(applyTag(toHtml, "Nincs kiadandó.", "p"))

            if (toDo.isNotEmpty()) {
                println(applyTag(toHtml, "Fejlesztőnek van dolga vele", "h2"))
                if (toHtml) println("<ul>")
                toDo.forEach {
                    println("\t" + applyTag(toHtml, "${it.key} ${it.fixVersions?.map { it.name }} - ${it.assignee?.name}", "li"))
                }
                if (toHtml) println("</ul>")
            } else println(applyTag(toHtml, "Nincs külön fejlesztői teendő.", "p"))
            println()
        }
    }

    protected fun applyTag(toHtml: Boolean, text: String, tag: String) = if (toHtml) "<$tag>$text</$tag>" else text
}
