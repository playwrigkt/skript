package dev.yn.playground.vertx.sql

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.yn.playground.sql.*
import dev.yn.playground.test.*
import io.kotlintest.matchers.fail
import io.kotlintest.matchers.shouldBe
import io.kotlintest.mock.`when`
import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.impl.MessageImpl
import io.vertx.core.impl.VertxImpl
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Instant


/**
 * Created by devyn on 9/23/17.
 */
class SqlTaskSpec: StringSpec() {
    val LOG = LoggerFactory.getLogger(this.javaClass)

//    val connectionMock = mock<SQLConnection>()
//    val connectionContextMock = mock<VertxSQLExecutor>()
//    val resultSetMock = mock<ResultSet>()
//    val updateResultSetMock = mock<UpdateResult>()
//    val vertxMock: Vertx = mock<VertxImpl>()
//    val eventBusMock: EventBus = mock<EventBus>()

//    val provider = TestVertxSQLAndVertxProvider(vertxMock, connectionContextMock )

    init {

//        Json.mapper
//                .registerModule(KotlinModule())
//                .registerModule(JavaTimeModule())
//        "should runIO a non transactional doOptionally with a single query" {
//            val sqlStatement = SQLStatement.Parameterized("sql string", listOf(1, 2, "hello", true, false, "end"))
//            val mapping1 = SQLMapping.query<JsonObject, JsonObject>({ SQLCommand.Query(sqlStatement) }, { input, resultSet -> Try.Success(input) })
//            val unpreparedSqlTransaction = SQLTask.query<JsonObject, JsonObject>(mapping1)
//            val task = TransactionalVertxSQLTask(unpreparedSqlTransaction, provider)
//
//            val input = JsonObject().put("field", 1234L)
//
//            val getConnectionCompleterCaptor = getConnectionCompleterCaptor()
//            val autoCommitFutureCompleterCaptor = voidAsyncResultHandlerCaptor()
//            val resultSetHandlerCaptor = resultSetHandlerCaptor()
//            val closeHandlerCaptor = voidAsyncResultHandlerCaptor()
//
//            `when`(connectionContextMock.query(SQLCommand.Query(sqlStatement))).thenReturn(dev.yn.playground.task.result.AsyncResult.succeeded(SQLResult.Query(emptyList())))
//            `when`(connectionMock.setAutoCommit(eq(true), any())).thenReturn(connectionMock)
////            `when`(connectionMock.queryWithParams(eq(sqlStatement.query), eq(sqlStatement.params), any())).thenReturn(connectionMock)
//
//            val future = task.run(input)
//            verify(clientMock).getConnection(getConnectionCompleterCaptor.capture())
//            getConnectionCompleterCaptor.value.handle(asyncResult(connectionMock))
//            verify(connectionMock).setAutoCommit(eq(true), autoCommitFutureCompleterCaptor.capture())
//            autoCommitFutureCompleterCaptor.value.handle(voidAsyncResult())
//            verify(connectionMock).queryWithParams(eq(sqlStatement.query), eq(sqlStatement.params), resultSetHandlerCaptor.capture())
//            resultSetHandlerCaptor.value.handle(asyncResult(resultSetMock))
//            verify(connectionMock).close(closeHandlerCaptor.capture())
//            closeHandlerCaptor.value.handle(voidAsyncResult())
//
//            awaitSucceededFuture(future, input)
//            verifyNoMoreInteractions(connectionMock, clientMock)
//        }
//
//        "should runIO a non transactional doOptionally with multiple queries" {
//            val task = UserTasks.unpreparedGetTask.prepare(provider)
//            val token = "abc-token-123"
//            val userId = "random-id"
//            val input = TokenAndInput(token, userId)
//            val userSession = UserSession(token, userId, Instant.now().plusSeconds(1000))
//            val userProfile = UserProfile(userId, "name", true)
//
//            val getConnectionCompleterCaptor = getConnectionCompleterCaptor()
//            val autoCommitFutureCompleterCaptor = voidAsyncResultHandlerCaptor()
//            val resultSetHandlerCaptor = resultSetHandlerCaptor()
//            val secondResultSetHandlerCaptor = resultSetHandlerCaptor()
//            val closeHandlerCaptor = voidAsyncResultHandlerCaptor()
//
//            `when`(clientMock.getConnection(any())).thenReturn(clientMock)
//            `when`(connectionMock.setAutoCommit(anyBoolean(), any())).thenReturn(connectionMock)
//            `when`(connectionMock.queryWithParams(any(), any(), any())).thenReturn(connectionMock)
//
//            val future = task.run(input)
//            verify(clientMock).getConnection(getConnectionCompleterCaptor.capture())
//            getConnectionCompleterCaptor.value.handle(asyncResult(connectionMock))
//
//            verify(connectionMock).setAutoCommit(eq(true), autoCommitFutureCompleterCaptor.capture())
//            autoCommitFutureCompleterCaptor.value.handle(voidAsyncResult())
//
//            verify(connectionMock).queryWithParams(eq(UserSQL.selectSessionByKey), eq(JsonArray(listOf(token))), resultSetHandlerCaptor.capture())
//            expectRows(listOf(JsonObject().put("user_id", userSession.userId).put("session_key", userSession.sessionKey).put("expiration", userSession.expiration)))
//            resultSetHandlerCaptor.value.handle(asyncResult(resultSetMock))
//
//            verify(resultSetMock).rows
//            verify(connectionMock).queryWithParams(eq(UserSQL.selectUser), eq(JsonArray(listOf(userId))), secondResultSetHandlerCaptor.capture())
//            expectRows(listOf(JsonObject().put("id", userId).put("user_name", userProfile.name).put("allow_public_message", userProfile.allowPubliMessage)))
//            secondResultSetHandlerCaptor.value.handle(asyncResult(resultSetMock))
//
//            verify(resultSetMock).rows
//            verify(connectionMock).close(closeHandlerCaptor.capture())
//            closeHandlerCaptor.value.handle(voidAsyncResult())
//
//            awaitSucceededFuture(future, userProfile)
//            verifyNoMoreInteractions(connectionMock, clientMock, resultSetMock)
//        }
//
//        "should runIO a transactional update doOptionally with multiple queries" {
//            val task = UserTasks.unpreparedLoginTask.prepare(provider)
//            val userId = "random-id"
//            val username = "ddd_user"
//            val password = "the_passWord"
//            val input = UserNameAndPassword(username, password)
//
//            val getConnectionCompleterCaptor = getConnectionCompleterCaptor()
//            val autoCommitFutureCompleterCaptor = voidAsyncResultHandlerCaptor()
//            val resultSetHandlerCaptor = resultSetHandlerCaptor()
//            val updateResultSetHandlerCaptor = updateResultSetHandlerCaptor()
//            val vertxSendResponseHandlerCaptor = vertxSendResponseHandlerCaptor()
//            val commitHandlerCaptor = voidAsyncResultHandlerCaptor()
//            val closeHandlerCaptor = voidAsyncResultHandlerCaptor()
//
//            val sqlParamCaptor = ArgumentCaptor.forClass(JsonArray::class.java)
//            val vertxSendBodyCaptor = ArgumentCaptor.forClass(String::class.java)
//
//            `when`(clientMock.getConnection(any())).thenReturn(clientMock)
//            `when`(connectionMock.setAutoCommit(anyBoolean(), any())).thenReturn(connectionMock)
//            `when`(connectionMock.queryWithParams(any(), any(), any())).thenReturn(connectionMock)
//            `when`(connectionMock.updateWithParams(any(), any(), any())).thenReturn(connectionMock)
//            `when`(connectionMock.commit(any())).thenReturn(connectionMock)
//            `when`(vertxMock.eventBus()).thenReturn(eventBusMock)
//            `when`(eventBusMock.send(any(), any(), any())).thenReturn(eventBusMock)
//
//            val future = task.run(input)
//            verify(clientMock).getConnection(getConnectionCompleterCaptor.capture())
//            getConnectionCompleterCaptor.value.handle(asyncResult(connectionMock))
//
//            verify(connectionMock).setAutoCommit(eq(false), autoCommitFutureCompleterCaptor.capture())
//            autoCommitFutureCompleterCaptor.value.handle(voidAsyncResult())
//
//            verify(connectionMock).queryWithParams(eq(UserSQL.selectUserId), eq(JsonArray(listOf(username))), resultSetHandlerCaptor.capture())
//            expectRows(listOf(JsonObject().put("id", userId)))
//            resultSetHandlerCaptor.value.handle(asyncResult(resultSetMock))
//
//            verify(resultSetMock).rows
//            verify(connectionMock).queryWithParams(eq(UserSQL.selectUserPassword), eq(JsonArray(listOf(userId, password))), resultSetHandlerCaptor.capture())
//            expectRows(listOf(JsonObject().put("user_id", userId)))
//            resultSetHandlerCaptor.allValues.last().handle(asyncResult(resultSetMock))
//
//            verify(resultSetMock).rows
//            verify(connectionMock).queryWithParams(eq(UserSQL.selectUserSessionExists), eq(JsonArray(listOf(userId))), resultSetHandlerCaptor.capture())
//            expectRows(listOf(JsonObject().put("succeeded", false)))
//            resultSetHandlerCaptor.allValues.last().handle(asyncResult(resultSetMock))
//
//            verify(resultSetMock).rows
//            verify(connectionMock).updateWithParams(eq(UserSQL.insertSession), sqlParamCaptor.capture(), updateResultSetHandlerCaptor.capture())
//            `when`(updateResultSetMock.updated).thenReturn(1)
//            updateResultSetHandlerCaptor.allValues.last().handle(asyncResult(updateResultSetMock))
//
//            verify(updateResultSetMock).updated
//            verify(vertxMock).eventBus()
//            verify(eventBusMock).send(eq(userLoginAddress), vertxSendBodyCaptor.capture(), vertxSendResponseHandlerCaptor.capture())
//            vertxSendResponseHandlerCaptor.allValues.last().handle(asyncResult(message<String>()))
//
//            verify(updateResultSetMock).updated
//            verify(connectionMock).commit(commitHandlerCaptor.capture())
//            commitHandlerCaptor.value.handle(voidAsyncResult())
//
//            verify(connectionMock).close(closeHandlerCaptor.capture())
//            closeHandlerCaptor.value.handle(voidAsyncResult())
//
//            val session = awaitSucceededFuture(future)
//            sqlParamCaptor.value shouldBe JsonArray(listOf(session.sessionKey, session.userId, Timestamp.from(session.expiration)))
//            Json.decodeValue(vertxSendBodyCaptor.value, UserSession::class.java) shouldBe session
//            verifyNoMoreInteractions(connectionMock, clientMock, resultSetMock, updateResultSetMock, vertxMock, eventBusMock)
//        }
    }

    fun <T> asyncResult(result: T? = null, failure: Throwable? = null): AsyncResult<T> = object : AsyncResult<T> {
        override fun succeeded(): Boolean = result != null

        override fun failed(): Boolean = failure != null

        override fun cause(): Throwable? = failure

        override fun result(): T? = result
    }

    fun voidAsyncResult(failure: Throwable? = null): AsyncResult<Void> = object : AsyncResult<Void> {
        override fun succeeded(): Boolean = failure == null

        override fun cause(): Throwable? = failure

        override fun failed(): Boolean = failure != null
        override fun result(): Void? = null
    }

    fun <T> message(): Message<T> {
        return MessageImpl<T, T>()
    }
//    fun expectRows(rows: List<JsonObject>) {
//        verifyNoMoreInteractions(resultSetMock)
//        reset(resultSetMock)
//        `when`(resultSetMock.rows).thenReturn(rows)
//    }
    fun <T> awaitSucceededFuture(future: dev.yn.playground.task.result.AsyncResult<T>, result: T? = null, maxDuration: Long = 1000L): T {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        if(!future.isComplete()) fail("Timeout")
        if(future.isFailure()) LOG.error("Expected Success", future.error())
        future.isSuccess() shouldBe true
        result?.let { future.result() shouldBe it }
        return future.result()!!
    }

    fun <T> awaitFailedFuture(future: dev.yn.playground.task.result.AsyncResult<T>, cause: Throwable? = null, maxDuration: Long = 1000L): Throwable {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.isFailure() shouldBe true
        cause?.let { future.error() shouldBe it}
        return future.error()!!
    }

    fun getConnectionCompleterCaptor(): ArgumentCaptor<Handler<AsyncResult<SQLConnection>>> {
        val handler: Handler<AsyncResult<SQLConnection>> = object : Handler<AsyncResult<SQLConnection>> {
            override fun handle(event: AsyncResult<SQLConnection>?) =TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        return ArgumentCaptor.forClass(handler.javaClass)
    }

    fun voidAsyncResultHandlerCaptor(): ArgumentCaptor<Handler<AsyncResult<Void>>> {
        val voidHandler: Handler<AsyncResult<Void>> = object : Handler<AsyncResult<Void>> {
            override fun handle(event: AsyncResult<Void>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        return ArgumentCaptor.forClass(voidHandler.javaClass)
    }

    fun resultSetHandlerCaptor(): ArgumentCaptor<Handler<AsyncResult<ResultSet>>> {
        val resultSetHandler: Handler<AsyncResult<ResultSet>> = object : Handler<AsyncResult<ResultSet>> {
            override fun handle(event: AsyncResult<ResultSet>) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        return ArgumentCaptor.forClass(resultSetHandler.javaClass)
    }

    fun updateResultSetHandlerCaptor(): ArgumentCaptor<Handler<AsyncResult<UpdateResult>>> {
        val handler = object: Handler<AsyncResult<UpdateResult>> {
            override fun handle(event: AsyncResult<UpdateResult>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        return ArgumentCaptor.forClass(handler.javaClass)
    }

    fun vertxSendResponseHandlerCaptor(): ArgumentCaptor<Handler<AsyncResult<Message<String>>>> {
        val handler = object: Handler<AsyncResult<Message<String>>> {
            override fun handle(event: AsyncResult<Message<String>>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        return ArgumentCaptor.forClass(handler.javaClass)
    }


}