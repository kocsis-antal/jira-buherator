package hu.microsec.cegbir.kocsis.helper

import com.atlassian.jira.rest.client.api.domain.Issue
import hu.microsec.cegbir.kocsis.JiraBuherator
import hu.microsec.cegbir.kocsis.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

abstract class Release(val buherator: JiraBuherator) {
    fun generate() {
        val toReleaseBranches = arrayOf("master", "development")

        val result = buherator.select("filter = \"CC projects filter\" AND type in standardIssueTypes() AND status = ${Statuses.RELEASE.statusName}")
        logger.info("Found ${result.total} issues.")
        result.issues.groupBy { it.project }.toSortedMap({ o1, o2 -> o1.key.compareTo(o2.key) }).forEach {
            println(printProject(it.key.name?.replace("CNY - ", "").orEmpty()))

            val (toRelease, toDo) = it.value.filter { it.fixVersions?.none { it.isReleased } != null }.partition { toReleaseBranches.contains(it.fixVersions?.firstOrNull()?.name) }

            if (toRelease.isNotEmpty()) {
                println(printToHeader("Kiadásra vár"))
                toRelease.forEach {
                    println(printToReleaseItem(it))
                }
                print(printToFooter())
            } else println(printToEmpty("Nincs kiadandó."))

            if (toDo.isNotEmpty()) {
                println(printToHeader("Fejlesztőnek van dolga vele"))
                toDo.forEach {
                    println(printToDoItem(it))
                }
                print(printToFooter())
            } else println(printToEmpty("Nincs külön fejlesztői teendő."))
            println()
        }
    }

    abstract fun printProject(name: String): String

    abstract fun printToHeader(string: String): String
    abstract fun printToFooter(): String
    abstract fun printToEmpty(string: String): String

    abstract fun printToReleaseItem(issue: Issue): String
    abstract fun printToDoItem(issue: Issue): String

    companion object {
        private val logger = LoggerFactory.getLogger(Release::class.java)
    }
}

@Service
class ReleaseTxt(buherator: JiraBuherator) : Release(buherator) {
    override fun printProject(name: String) = name

    override fun printToHeader(string: String) = "- $string"
    override fun printToFooter() = ""
    override fun printToEmpty(string: String) = "- $string"

    override fun printToReleaseItem(issue: Issue) = "\t - ${issue.key}: ${issue.summary}"
    override fun printToDoItem(issue: Issue) = "\t - ${issue.key} ${issue.fixVersions?.map { it.name }}: ${issue.assignee?.displayName}"
}

@Service
class ReleaseHtml(buherator: JiraBuherator) : Release(buherator) {
    override fun printProject(name: String) = "<h1>$name</h1>"

    override fun printToHeader(string: String) = "<h2>$string</h2>\n<ul>"
    override fun printToFooter() = "</ul>\n"
    override fun printToEmpty(string: String) = "<p>$string</p>"

    override fun printToReleaseItem(issue: Issue) = "\t<li>${issueLink(issue)}: ${issue.summary}</li>"
    override fun printToDoItem(issue: Issue) = "\t<li>${issueLink(issue)} ${issue.fixVersions?.map { it.name }}: ${issue.assignee?.displayName}</li>"

    protected fun issueLink(issue: Issue) = "<a href=\"https://jira.intra.microsec.hu/browse/${issue.key}\">${issue.key}</a>"
}
