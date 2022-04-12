package hu.microsec.cegbir.kocsis.jira

import com.atlassian.httpclient.api.Request
import com.atlassian.jira.rest.client.api.AuthenticationHandler
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

    fun moveIssue(issue: Issue, from: Statuses, to: Statuses): Boolean = getIssue(issue.key).run {
        if (status.name == to.statusName) {
            logger.info("Can't move [${key}] issue: already in [${to.statusName}] status")
            true
        } else if (status.name != from.statusName) {
            logger.warn("Can't move [${issue.key}] issue: it's not in ${from.statusName} status (current: ${status.name})")
            false
        } else moveIssue(this, to)
    }

    fun moveIssue(issue: Issue, to: Statuses): Boolean = if (issue.status.name == to.statusName) {
        logger.info("Can't move [${issue.key}] issue: already in [${to.statusName}] status")
        true
    } else {
        issueClient.getTransitions(issue.transitionsUri).claim().singleOrNull { transition -> transition.name.equals(to.statusName) }.let {
            if (it == null) {
                logger.info("On [${issue.key}] issue no transition from [${issue.status.name}] to [${to.statusName}]")
                false
            } else {
                issueClient.transition(issue, TransitionInput(it.id)).claim()
                logger.info("Moved [${issue.key}] issue from [${issue.status.name}] to [${to.statusName}]")
                true
            }
        }
    }

    fun select(jql: String) = client.searchClient.searchJql(jql).claim()

    companion object {
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
