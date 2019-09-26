package io.cirrocumulus.registry.api

import io.r2dbc.client.R2dbc
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option

data class Configuration(
    val db: Database
) {
    data class Database(
        val host: String = "localhost",
        val port: Int = 5432,
        val name: String = "cirrocumulus_registry",
        val username: String,
        val password: String
    ) {
        fun createClient(): R2dbc {
            val dbConnectionFactory = ConnectionFactories.get(
                ConnectionFactoryOptions
                    .builder()
                    .option(Option.valueOf("driver"), "pool")
                    .option(Option.valueOf("protocol"), "postgresql")
                    .option(Option.valueOf("host"), host)
                    .option(Option.valueOf("port"), port)
                    .option(Option.valueOf("database"), name)
                    .option(Option.valueOf("user"), username)
                    .option(Option.valueOf("password"), password)
                    .build()
            )
            val dbConnectionPool = ConnectionPool(
                ConnectionPoolConfiguration
                    .builder(dbConnectionFactory)
                    .build()
            )
            return R2dbc(dbConnectionPool)
        }
    }
}
