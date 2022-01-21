package hu.microsec.cegbir.kocsis

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "jira")
data class JiraProperties(
    val url: String = "",
    val username: String = "",
    val password: String = "",
) {
    init { // @formatter:off
        log.info("Read JIRA configuration parameters:\n" +
                "\turl: [$url]\n" +
                "\tusername: [$username]\n" +
                "\tpassword: [${(if (password.isNotEmpty()) "***" else "")}]")
        // @formatter:on
    }

    companion object {
        private val log = LoggerFactory.getLogger(JiraProperties::class.java)
    }
}
