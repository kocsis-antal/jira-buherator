package hu.microsec.cegbir.kocsis.jira

import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.Subtask
import com.atlassian.jira.rest.client.api.domain.Version

data class JiraIssueDTO(
    val key: String,
    val status: String,
    val assignee: String?,
    val fixVersions: Set<JiraFixVersionDTO>,
    val subtasks: Set<JiraSubtaskDTO>,
) {
    constructor(issue: Issue) : this(issue.key, issue.status.name, issue.assignee?.name, issue.fixVersions?.map { JiraFixVersionDTO(it) }?.toSet().orEmpty(), issue.subtasks?.map { JiraSubtaskDTO(it) }?.toSet().orEmpty())

    val notReleasedFixVersions = fixVersions.filter { !it.isReleased }

    override fun toString(): String = "${key}${if (status == Statuses.RELEASE.statusName) "(R)" else ""} (${assignee})"
}

data class JiraFixVersionDTO(
    val name: String,
    val isReleased: Boolean,
) {
    constructor(version: Version) : this(version.name, version.isReleased)
}

data class JiraSubtaskDTO(
    val key: String,
) {
    constructor(subtask: Subtask) : this(subtask.issueKey)
}

