package dev.yn.playground.user

import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.task.VertxProvider
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient

class SQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient {
        return sqlClient
    }
}
