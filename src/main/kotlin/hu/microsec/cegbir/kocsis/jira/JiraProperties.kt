package hu.microsec.cegbir.kocsis.jira

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jira")
data class JiraProperties(
    var url: String = "",
    var personalAccessToken: String = "",
) {
    init { // @formatter:off
        log.info("Read JIRA configuration parameters:\n" +
                "\turl: [$url]\n" +
                "\tpersonalAccessToken: [${(if (personalAccessToken.isNotEmpty()) "***" else "")}]")
        // @formatter:on
    }

    companion object {
        private val log = LoggerFactory.getLogger(JiraProperties::class.java)
    }
}
