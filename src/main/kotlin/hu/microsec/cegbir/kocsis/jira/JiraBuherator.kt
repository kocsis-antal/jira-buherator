package hu.microsec.cegbir.kocsis.jira

import com.atlassian.httpclient.api.Request
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos
import com.atlassian.jira.rest.client.api.RestClientException
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class JiraBuherator(
    properties: JiraProperties
) {
    var client = AsynchronousJiraRestClientFactory().createWithAuthenticationHandler(URI(properties.url), BearerHttpAuthenticationHandler(properties.personalAccessToken))
    var issueClient = client.issueClient

    fun getIssue(key: String) = issueClient.getIssue(key).claim()
    fun getIssueWithChangelog(key: String) = issueClient.getIssue(key, listOf(Expandos.CHANGELOG)).claim()

    fun moveIssue(issue: Issue, from: Statuses, to: Statuses): Boolean = getIssue(issue.key).run {
        if (status.name == to.statusName) {
            logger.info("Can't move [${key} - ${issue.summary}] issue: already in [${to.statusName}] status")
            true
        } else if (status.name != from.statusName) {
            logger.info("Can't move [${issue.key} - ${issue.summary} (${issue.assignee?.name})] issue: it's not in ${from.statusName} status (current: ${status.name})")
            false
        } else moveIssue(this, to)
    }

    fun moveIssue(issue: Issue, to: Statuses): Boolean = if (issue.status.name == to.statusName) {
        logger.debug("Can't move [${issue.key} - ${issue.summary}] issue: already in [${to.statusName}] status")
        true
    } else {
        issueClient.getTransitions(issue.transitionsUri).claim().singleOrNull { transition -> transition.name.equals(to.statusName) }.let {
            if (it == null) {
                logger.debug("On [${issue.key} - ${issue.summary}] issue no transition from [${issue.status.name}] to [${to.statusName}]")
                false
            } else {
                try {
                    issueClient.transition(issue, TransitionInput(it.id)).claim()
                    logger.info("Moved [${issue.key} - ${issue.summary}] issue from [${issue.status.name}] to [${to.statusName}]")
                    true
                } catch (e: RestClientException) {
                    logger.warn("Error moving [${issue.key} - ${issue.summary} (${issue.assignee?.name})] issue from [${issue.status.name}] to [${to.statusName}]: ${e.errorCollections.map { it.errorMessages }}")
                    false
                } catch (e: Exception) {
                    logger.warn("Error moving [${issue.key} - ${issue.summary} (${issue.assignee?.name})] issue from [${issue.status.name}] to [${to.statusName}]: ${e.localizedMessage}")
                    false
                }
            }
        }
    }

    fun select(jql: String) = client.searchClient.searchJql(jql).claim()

    companion object {
        const val CC_PROJECTS_FILTER = """filter = "CC projects filter""""
        const val CNY_PREFIX = "CNY - "

        private val logger = LoggerFactory.getLogger(JiraBuherator::class.java)
    }
}

class BearerHttpAuthenticationHandler(private val token: String) : AuthenticationHandler {
    override fun configure(builder: Request.Builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "Bearer $token")
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
    }
}
