package hu.microsec.cegbir.kocsis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(JiraProperties::class)
@SpringBootApplication
open class JiraBuherator(val properties: JiraProperties)

fun main(args: Array<String>) {
    runApplication<JiraBuherator>(*args)
}
