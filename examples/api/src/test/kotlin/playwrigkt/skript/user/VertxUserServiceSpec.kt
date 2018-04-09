package playwrigkt.skript.user

import io.kotlintest.shouldBe
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.http.HttpMethod
import playwrigkt.skript.performer.VertxHttpRequestPerformer
import playwrigkt.skript.produktion.VertxHttpProduktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.serialize.SerializeSkript
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.VertxPublishStageManager
import playwrigkt.skript.stagemanager.VertxSQLStageManager
import playwrigkt.skript.stagemanager.VertxSerializeStageManager
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.venue.QueueVenue
import playwrigkt.skript.venue.VertxHttpVenue
import playwrigkt.skript.venue.VertxVenue
import playwrigkt.skript.venue.userProduktions
import java.util.*
import kotlin.math.floor

class VertxUserServiceSpec: UserServiceSpec() {
    companion object {
        val vertx by lazy { Vertx.vertx() }

        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 30)
                .put("poolName", "test_pool")
        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }

        val sqlConnectionStageManager by lazy { VertxSQLStageManager(sqlClient) }
        val publishStageManager by lazy { VertxPublishStageManager(vertx)  }
        val serializeStageManager = VertxSerializeStageManager()
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager)
        }

        val vertxVenue = VertxVenue(vertx)

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val httpVenue: VertxHttpVenue = VertxHttpVenue(vertx.createHttpServer(HttpServerOptions().setPort(port)))
        val produktionsFuture = userProduktions(httpVenue, stageManager)

        val produktions by lazy {
            while(!produktionsFuture.isComplete()) {
                Thread.sleep(100)
            }
            produktionsFuture.result()!!
        }
    }

    override fun getUserHttpRequestTest() {
        val userId = UUID.randomUUID().toString()
        val password = "pass10"
        val userName = "sally10"
        val user = UserProfile(userId, userName, false)
        val userAndPassword = UserNameAndPassword(userName, password)

        awaitSucceededFuture(
                userService.createUser(UserProfileAndPassword(user, password)),
                user)

        val session = awaitSucceededFuture(userService.loginUser(userAndPassword))!!

        awaitSucceededFuture(
                userService.getUser(userId, session.sessionKey),
                user)

        val performer = VertxHttpRequestPerformer(vertx.createHttpClient(
                HttpClientOptions().setDefaultPort(port)
        ))
        val getResult = performer.perform(playwrigkt.skript.http.HttpClientRequest(
                "http://localhost/users/{userId}",
                mapOf("userId" to userId),
                emptyMap(),
                mapOf("Authorization" to listOf(session.sessionKey)),
                HttpMethod.Get,
                AsyncResult.succeeded("".toByteArray())
        ))
        awaitSucceededFuture(getResult
                .flatMap { it.responseBody }
                .flatMap { SerializeSkript.Deserialize(UserProfile::class.java).run(it, serializeStageManager.hireTroupe()) }
        ) shouldBe user
    }

    override fun stageManager(): ApplicationStageManager = VertxUserServiceSpec.stageManager
    override fun queueVenue(): QueueVenue = vertxVenue
    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
        produktions.forEach { awaitSucceededFuture(it.stop()) }
    }
}