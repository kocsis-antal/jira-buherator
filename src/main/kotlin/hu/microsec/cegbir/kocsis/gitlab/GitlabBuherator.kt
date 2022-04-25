package hu.microsec.cegbir.kocsis.gitlab

import org.gitlab4j.api.Constants
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.models.MergeRequest
import org.gitlab4j.api.models.MergeRequestFilter
import org.springframework.stereotype.Service

@Service
class GitlabBuherator(properties: GitLabProperties) {
    val gitLabApi = GitLabApi(properties.url, properties.personalAccessToken)

    fun getProject(projectName: String) = gitLabApi.projectApi.getProjects(projectName).filter { it.path.equals(projectName, true) }.single()

    fun getMergeRequests(id: String) = gitLabApi.mergeRequestApi.getMergeRequests(MergeRequestFilter().apply {
        scope = Constants.MergeRequestScope.ALL
        targetBranch = "master"
        search = id
    })

    fun test() { // @formatter:off
        getMergeRequests("release")
            .filter { it.webUrl.contains("cc-team") }
            .filter { it.title.contains("release", true) }
            .filter { releaseVersion.containsMatchIn(it.title) }
            .groupBy { it.projectId }
            .forEach {
                printMergeRequest(it)
            }
        // @formatter:on
    }

    private val releaseVersion = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")

    private fun printMergeRequest(entry: Map.Entry<Int, List<MergeRequest>>) {
        println("------------------------------------------------------------------")
        println(">>> ${gitLabApi.projectApi.getProject(entry.key).name}")

        // @formatter:off
        entry.value
            .sortedWith(
                compareBy<MergeRequest?> {
                    releaseVersion.find(it!!.title)!!.groupValues[1].toInt() }
                    .thenBy { releaseVersion.find(it!!.title)!!.groupValues[2].toInt() }
                    .thenBy { releaseVersion.find(it!!.title)!!.groupValues[3].toInt() }
            )
            .forEach {
                println("----------------------")
                println("${it.title} (${it.state})")

                printCommits(it)

                println()
            }
        // @formatter:on
    }

    private fun printCommits(mergeRequest: MergeRequest) { // @formatter:off
        gitLabApi.mergeRequestApi.getCommits(mergeRequest.projectId, mergeRequest.iid)
            .filter { !it.title.startsWith("Merge ") }
            .distinctBy { it.title }
            .forEach {
                println(" * ${it.title}")
        }
        // @formatter:on
    }

    //
    //    fun test() { // Create a GitLabApi instance to communicate with your GitLab server
    //        // Create a GitLabApi instance to communicate with your GitLab server
    //        val gitLabApi = GitLabApi(properties.url, properties.personalAccessToken)
    //
    //        // Log using the shared logger and default level of FINE
    //        gitLabApi.enableRequestResponseLogging() //        // Log using the shared logger and the INFO level
    //        //        gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO)
    //
    //        gitLabApi.projectApi.getProjects("microsec-cegbir").filter { it.name.equals("microsec-cegbir") }.forEach { println(it) }
    //
    ////        val project = getProject("cc-team/microsec-cegbir/microsec-cegbir")
    ////
    ////        val filter = MergeRequestFilter().apply {
    ////            projectId = project.id
    ////            search = "HARMASKA-256"
    ////        }
    ////
    ////        val projects = gitLabApi.mergeRequestApi.getMergeRequests(filter)
    ////        projects.forEach { println(it) }
    //    }
}
