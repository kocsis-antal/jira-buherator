package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Tester(
    val jiraBuherator: JiraBuherator,
    val branch: Branch,
) {
    fun test2() {
        val key = "HARMASKA-2221"
        val issue = jiraBuherator.getIssue(key)
        logger.debug("issue:\n$issue")

        val issueType = issue.issueType
        logger.info("data loaded for [$key] ${issueType.name}(${issueType.description}): ${issue.summary}")

        val status = issue.status
        logger.info("status: [${status.name}(${status.statusCategory.name})] - ${status.description}")

        val transitions = jiraBuherator.issueClient.getTransitions(issue.transitionsUri).claim()
        logger.info("transitions(${transitions.count()}): [${status.name}] -> ...")
        transitions.forEach {
            logger.info("transition: [${status.name}] -> [${it.name}]")
        }

        jiraBuherator.moveIssue(issue, Statuses.TO_DO, Statuses.IN_PROGRESS)
    }

    fun test3() {
        branch.branchInfo()
    }

    fun test4() {
        jiraBuherator.createMainTaskWithParent("HARMASKA-3324", "HARMASKA", "Teszt cím", "buherátor teszt\npróba")
        jiraBuherator.linkBlock("HARMASKA-3324", "HARMASKA-3330")
    }

    fun test() {
        jiraBuherator.getIssue("CNYOTHERS-484")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Tester::class.java)
    }
}
