package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.CcBuheratorApp
import hu.microsec.cegbir.kocsis.gitlab.GitBranchDTO
import hu.microsec.cegbir.kocsis.gitlab.GitMergeRequestDTO
import hu.microsec.cegbir.kocsis.gitlab.GitProjectDTO
import hu.microsec.cegbir.kocsis.gitlab.GitlabBuherator
import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.JiraFixVersionDTO
import hu.microsec.cegbir.kocsis.jira.JiraIssueDTO
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Branch(
    val jiraBuherator: JiraBuherator,
    val gitlabBuherator: GitlabBuherator,
) {
    fun getIssuesActive() = jiraBuherator.select("""filter = "CC projects filter" AND type in standardIssueTypes() AND status in ("${Statuses.IN_PROGRESS.statusName}", "${Statuses.RELEASE.statusName}") """).also { logger.info("Found ${it.total} active issues.") }.issues.groupBy { it.project }.toSortedMap({ o1, o2 -> o1.key.compareTo(o2.key) })

    fun bla() {
        getIssuesActive().forEach {
            val projectName = it.key.name?.replace(JiraBuherator.CNY_PREFIX, "").orEmpty()
            println("***********")
            println(">${projectName}<")
            if (!projectName.isEmpty() && CcBuheratorApp.projectMap.contains(projectName)) {
                val gitProject = gitlabBuherator.getProject(CcBuheratorApp.projectMap.get(projectName).orEmpty())

                val gitProjectDTO = GitProjectDTO(gitProject.id).apply {
                    branches = gitlabBuherator.gitLabApi.repositoryApi.getBranches(gitProject.id).map { GitBranchDTO(it) }
                    mergeRequests = gitlabBuherator.gitLabApi.mergeRequestApi.getMergeRequests(gitProject.id).map { GitMergeRequestDTO(it) }
                }

                val (okList, nemOkList) = it.value.map { JiraIssueDTO(it) }.map { BranchIssueDTO(it, gitProjectDTO) }.partition { it.status.startsWith("OK") }
                if (okList.isNotEmpty()) {
                    println("-Rendben")
                    okList.forEach {
                        println(""" - ${it.jiraIssueDTO}""")
                    }
                }
                if (nemOkList.isNotEmpty()) {
                    println("-Nincs rendben")
                    nemOkList.forEach {
                        println(""" - ${it.jiraIssueDTO}: ${it.status}""")
                    }
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Branch::class.java)
    }
}

data class BranchIssueDTO(
    val jiraIssueDTO: JiraIssueDTO,
    val gitProjectDTO: GitProjectDTO,
) {
    val jiraBranches = jiraIssueDTO.notReleasedFixVersions
    val gitBranches = mutableListOf<GitBranchDTO>().apply {
        addAll(gitProjectDTO.getBranches(jiraIssueDTO.key))
        addAll(jiraIssueDTO.subtasks.map { gitProjectDTO.getBranches(it.key) }.flatten())
    }
    val gitMergeRequests = mutableListOf<GitMergeRequestDTO>().apply {
        addAll(gitProjectDTO.getMergeRequests(jiraIssueDTO.key))
        addAll(jiraIssueDTO.subtasks.map { gitProjectDTO.getMergeRequests(it.key) }.flatten())
    }
    val commonBranches = gitBranches.map { it.key }.filter { jiraBranches.map { it.name }.contains(it) }

    val gitBranchesStatus = gitBranches.map { it.merged }.distinct().let {
        when {
            it.isEmpty() -> "ismeretlen (nincs)"
            it.size == 1 -> it.single()
            else -> "ismeretlen (többszörös)"
        }
    }
    val gitMergeRequestsStatus = gitMergeRequests.map { it.mergeStatus }.distinct().let {
        when {
            it.isEmpty() -> "ismeretlen (nincs)"
            it.size == 1 -> it.single()
            else -> "ismeretlen (többszörös)"
        }
    }

    private val subPrefix = "      "
    private val sublistPrefix = "$subPrefix - "
    val status = when { // JIRA - dev OK
        jiraBranches.any { "development".equals(it.name) } -> "OK (development)"

        // sehol sincs
        jiraBranches.isEmpty() && gitBranches.isEmpty() && gitMergeRequests.isEmpty() -> if (jiraIssueDTO.status == Statuses.RELEASE.statusName) "???? (sehol nem található)" else "OK (folyamatban)"

        // JIRA nincs, de GIT van
        jiraBranches.isEmpty() && gitMergeRequests.isNotEmpty() -> "JIRA verzió hiányzik (GIT MR ${if (gitMergeRequestsStatus == "merged") "merged => ${gitMergeRequests.map { it.targetKey }.distinct().joinToString()}" else gitMergeRequests.joinToString { it.key }})"
        jiraBranches.isEmpty() && gitBranches.isNotEmpty() -> "JIRA verzió hiányzik (GIT branch $gitBranchesStatus)"

        gitBranches.isEmpty() && gitMergeRequests.isEmpty() -> "nem található GIT-ben, JIRA-ban:\n${jiraBranches.joinToString(separator = "\n", prefix = sublistPrefix, transform = JiraFixVersionDTO::name)}"

        // van közös pont
        commonBranches.isNotEmpty() -> "OK - egyezés (${commonBranches.joinToString()})"

        else -> "ismeretlen" + //
                "\n${subPrefix}JIRA v: ${jiraBranches.joinToString { it.name }}" + //
                "\n${subPrefix}GIT br: ${gitBranches.joinToString()}" + //
                "\n${subPrefix}GIT MR: ${gitMergeRequests.joinToString()}" + //
                "\n${subPrefix}common: ${commonBranches.joinToString()}"
    }

    override fun toString(): String {
        return "BranchIssueDTO(jiraIssueDTO=$jiraIssueDTO, gitProjectDTO=$gitProjectDTO, jiraBranches=$jiraBranches, gitBranches=$gitBranches, gitMergeREquests=$gitMergeRequests, commonBranches=$commonBranches)"
    }
}
