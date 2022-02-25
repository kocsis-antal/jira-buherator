package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.JiraBuherator
import hu.microsec.cegbir.kocsis.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Tester(val buherator: JiraBuherator) {
    fun test() {
        val key = "HARMASKA-2221"
        val issue = buherator.getIssue(key)
        logger.debug("issue:\n$issue")

        val issueType = issue.issueType
        logger.info("data loaded for [$key] ${issueType.name}(${issueType.description}): ${issue.summary}")

        val status = issue.status
        logger.info("status: [${status.name}(${status.statusCategory.name})] - ${status.description}")

        val transitions = buherator.issueClient.getTransitions(issue.transitionsUri).claim()
        logger.info("transitions(${transitions.count()}): [${status.name}] -> ...")
        transitions.forEach {
            logger.info("transition: [${status.name}] -> [${it.name}]")
        }

        buherator.moveIssue(issue, Statuses.TO_DO, Statuses.IN_PROGRESS)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Tester::class.java)
    }
}
