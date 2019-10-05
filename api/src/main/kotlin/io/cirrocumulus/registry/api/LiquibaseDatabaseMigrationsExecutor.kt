package io.cirrocumulus.registry.api

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager

class LiquibaseDatabaseMigrationsExecutor(
    private val config: Configuration.Database
) : DatabaseMigrationsExecutor {
    override fun update() {
        DriverManager.getConnection(config.jdbcUrl, config.username, config.password).use { connection ->
            val db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
            val liquibase = Liquibase("db/changelog.xml", ClassLoaderResourceAccessor(), db)
            liquibase.update("")
        }
    }
}
