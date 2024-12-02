package hu.microsec.cegbir.kocsis.app

import hu.microsec.cegbir.kocsis.gitlab.GitLabProperties
import hu.microsec.cegbir.kocsis.jira.JiraProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties(JiraProperties::class, GitLabProperties::class)
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["hu.microsec.cegbir.kocsis.helper", "hu.microsec.cegbir.kocsis.jira", "hu.microsec.cegbir.kocsis.gitlab", "hu.microsec.cegbir.kocsis.web"])
open class CcBuheratorWebApp

fun main(args: Array<String>) {
    runApplication<CcBuheratorWebApp>(*args)
}

