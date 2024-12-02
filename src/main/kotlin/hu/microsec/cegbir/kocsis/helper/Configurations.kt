package hu.microsec.cegbir.kocsis.helper

import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
open class Configurations {
    @Bean
    open fun utf8properties(): PropertiesFactoryBean {
        val factoryBean = PropertiesFactoryBean()
        factoryBean.setFileEncoding("UTF-8")
        factoryBean.setLocation(ClassPathResource("application.properties"))
        return factoryBean
    }
}
