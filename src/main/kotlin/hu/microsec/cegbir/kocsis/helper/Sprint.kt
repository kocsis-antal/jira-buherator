package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.springframework.stereotype.Service

@Service
class Sprint(val buherator: JiraBuherator) {
    fun moveToReady() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = buherator.select("filter = \"CC projects issues in open sprint\" AND status in (${toTranslate.joinToString()})")
        println("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            buherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            buherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)
        }
    }

    fun closeRemained() {
        val result = buherator.select("filter = \"CC projects filter\" AND type in standardIssueTypes() AND status = \"${Statuses.IN_PROGRESS.statusName}\" AND issuefunction in hasSubtasks() AND NOT issuefunction in parentsOf(\"status != ${Statuses.DONE.statusName}\")")
        println("Found ${result.total} issues.")
        result.issues.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            buherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.FEEDBACK)
            buherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.RELEASE)
        }
    }
}
