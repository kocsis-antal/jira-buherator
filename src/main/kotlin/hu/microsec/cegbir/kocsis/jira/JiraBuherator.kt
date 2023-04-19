package hu.microsec.cegbir.kocsis.jira

import com.atlassian.httpclient.api.Request
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos
import com.atlassian.jira.rest.client.api.RestClientException
import com.atlassian.jira.rest.client.api.domain.BasicIssue
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput
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

    fun linkParent(parent: String, child: String) {
        logger.info("Issue [$parent] is parent of [$child]")
        issueClient.linkIssue(LinkIssuesInput(child, parent, "Hierarchy [Gantt]")).claim()
    }

    fun linkBlock(blocker: BasicIssue, blocked: BasicIssue) {
        linkBlock(blocker.key, blocked.key)
    }

    fun linkBlock(blocker: String, blocked: String) {
        logger.info("Issue [$blocker] is blocking [$blocked]")
        issueClient.linkIssue(LinkIssuesInput(blocker, blocked, "Blocks")).claim()
    }

    fun createMainTaskWithParent(parent: String, project: String, summary: String, description: String): BasicIssue {
        val newIssue = createMainTask(project, summary, description)
        linkParent(parent, newIssue.key)

        return newIssue
    }

    fun createReleaseTsask(blocker: BasicIssue, project: String): BasicIssue = createReleaseTsask(blocker.key, project)

    fun createReleaseTsask(blocker: String, project: String): BasicIssue {
        val issue = createIssue(project, "Release Task", "$project  csomag élesítés", "")
        linkBlock(blocker, issue.key)

        return issue
    }

    fun createMainTask(project: String, summary: String, description: String): BasicIssue = createIssue(project, "Main Task", summary, description)
    fun createIssue(project: String, issueTypeName: String, summary: String, description: String): BasicIssue {
        val issueType = client.metadataClient.issueTypes.get().first { it.name == issueTypeName } ?: throw RuntimeException()

        val issue = IssueInputBuilder().apply {
            setProjectKey(project)
            setIssueType(issueType)
            setSummary(summary)
            setDescription(description)
        }.build()

        val newIssue = issueClient.createIssue(issue).claim() ?: throw RuntimeException("Issue creation error")

        logger.info("Issue [$newIssue.key] created successfully")
        return newIssue
    }

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
                logger.debug("On [${issue.key} - ${issue.summary}](${issue.assignee?.name}) issue no transition from [${issue.status.name}] to [${to.statusName}]")
                false
            } else {
                try {
                    issueClient.transition(issue, TransitionInput(it.id)).claim()
                    logger.info("Moved [${issue.key} - ${issue.summary}] issue from [${issue.status.name}] to [${to.statusName}]")
                    true
                } catch (e: RestClientException) {
                    logger.warn("Error moving [${issue.key} - ${issue.summary} (${issue.assignee?.name})] issue from [${issue.status.name}] to [${to.statusName}]: ${e.errorCollections.map { it.errors }}")
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
        val DALX_PROJECTS = listOf("KETTESKE")

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
