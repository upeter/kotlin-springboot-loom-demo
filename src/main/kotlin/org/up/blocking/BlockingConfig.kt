package org.up.blocking

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.hibernate5.HibernateExceptionTranslator
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.sql.DataSource


@Configuration
class BlockingConfig {

    private val PROPERTY_NAME_DATABASE_DRIVER = "spring.datasource.driverClassName"
    private val PROPERTY_NAME_DATABASE_URL = "spring.datasource.url"
    private val PROPERTY_NAME_DATABASE_USERNAME = "spring.datasource.username"
    private val PROPERTY_NAME_DATABASE_PASSWORD = "spring.datasource.password"
    private val PROPERTY_NAME_DATABASE_PLATFORM = "spring.jpa.database-platform"

    @Bean
    fun entityManagerFactory(dataSource: DataSource, additionalProperties: Properties): LocalContainerEntityManagerFactoryBean =
            LocalContainerEntityManagerFactoryBean().apply {
                this.dataSource = dataSource
                setPackagesToScan("org.up.blocking")
                jpaVendorAdapter = HibernateJpaVendorAdapter()
                setJpaProperties(additionalProperties)
            }

    @Bean
    fun additionalProperties(environment: Environment): Properties = Properties().apply {
        setProperty("hibernate.dialect", environment.getRequiredProperty(PROPERTY_NAME_DATABASE_PLATFORM))
    }


    @Bean
    fun dataSource(environment: Environment): DataSource =
            DriverManagerDataSource().apply {
                setDriverClassName(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER))
                url = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_URL)
                username = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_USERNAME)
                password = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_PASSWORD)
            }

    @Bean
    @Primary
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}