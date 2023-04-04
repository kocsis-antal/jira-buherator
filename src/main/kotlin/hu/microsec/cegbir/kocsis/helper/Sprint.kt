package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.CC_PROJECTS_FILTER
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.DALX_PROJECTS
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Sprint(val buherator: JiraBuherator) {
    fun moveToReady() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = buherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        println("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            buherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            if (buherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)) {
                logger.info("[${it.key}] moved to ${Statuses.READY}")
                println("[${it.key}] moved to ${Statuses.READY}")
            } else logger.warn("Can't move [${it.key}] (${it.status.name})")
        }
    }

    fun moveEpics() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = buherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        println("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            buherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            if (buherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)) {
                logger.info("[${it.key}] moved to ${Statuses.READY}")
                println("[${it.key}] moved to ${Statuses.READY}")
            } else logger.warn("Can't move [${it.key}] (${it.status.name})")
        }
    }

    fun closeRemained() {
        val result = buherator.select("""$CC_PROJECTS_FILTER AND type in standardIssueTypes() AND status = "${Statuses.IN_PROGRESS.statusName}" AND issuefunction in hasSubtasks() AND NOT issuefunction in parentsOf("status != ${Statuses.DONE.statusName}")""")
        println("Found ${result.total} issues.")
        result.issues.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")

            if (DALX_PROJECTS.contains(it.project.key) && (it.fixVersions?.count() ?: 0) == 0) {
                logger.info("[${it.key}] adding fix version development") // TODO
            }

            if (buherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.FEEDBACK) || buherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.RELEASE)) {
                logger.info("[${it.key}] closed")
                println("[${it.key}] closed")
            } else logger.warn("Can't close [${it.key}](${it.assignee?.name}) (${it.status.name})")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Sprint::class.java)
    }
}
