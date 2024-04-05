package hu.microsec.cegbir.kocsis.gitlab

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gitlab")
data class GitLabProperties(
    var url: String = "",
    var personalAccessToken: String = "",
) {
    init { // @formatter:off
        log.info("Read GitLab configuration parameters:\n" +
                "\turl: [$url]\n" +
                "\tpersonalAccessToken: [${(if (personalAccessToken.isNotEmpty()) "***" else "")}]")
        // @formatter:on
    }

    companion object {
        private val log = LoggerFactory.getLogger(GitLabProperties::class.java)
    }
}
