package hu.microsec.cegbir.kocsis.gitlab

import org.gitlab4j.api.models.Branch
import org.gitlab4j.api.models.MergeRequest

data class GitProjectDTO(
    val key: Long,
    var branches: List<GitBranchDTO> = emptyList(),
    var mergeRequests: List<GitMergeRequestDTO> = emptyList(),
) {
    fun getBranches(key: String) = branches.filter { key.equals(extractKey(it.name)) }
    fun getMergeRequests(key: String) = mergeRequests.filter { key.equals(it.key) }

    companion object {
        fun extractKey(string: String) = """^(?:Resolve )?(?:\w+/)?([A-Z]+-\d+)[: \-_]""".toRegex().find(string).takeIf { it?.groups?.size == 2 }?.groups?.get(1)?.value ?: string
    }
}

data class GitBranchDTO(
    val name: String,
    val merged: Boolean,
) {
    constructor(branch: Branch) : this(branch.name, branch.merged)

    val key: String = GitProjectDTO.extractKey(name)

    override fun toString(): String = """${name}${if (merged) " (merged)" else ""}"""
}

data class GitMergeRequestDTO(
    val name: String,
    val mergeStatus: String,
    val target: String,
) {
    constructor(mergeRequest: MergeRequest) : this(mergeRequest.title, mergeRequest.state, mergeRequest.targetBranch)

    val key: String = GitProjectDTO.extractKey(name)
    val targetKey: String = GitProjectDTO.extractKey(target)

    override fun toString(): String = """${name} (${mergeStatus})"""
}

