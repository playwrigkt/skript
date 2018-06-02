package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import playwrigkt.skript.Async
import playwrigkt.skript.Async.awaitSucceededFuture
import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.application.createApplication
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.chatroom.models.ChatRoom
import playwrigkt.skript.chatroom.models.ChatRoomUser
import playwrigkt.skript.ex.createFile
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.readFile
import playwrigkt.skript.ex.writeFile
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.user.UserFixture
import playwrigkt.skript.user.UserHttpClient
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.floor

abstract class ChatRoomApiSpec: StringSpec() {

    val userClient: UserHttpClient = UserHttpClient()
    val chatRoomClient: ChatRoomHttpClient = ChatRoomHttpClient()

    val log = LoggerFactory.getLogger(this.javaClass)

    abstract val sourceConfigFileName: String
    val port: Int = floor((Math.random() * 8000)).toInt() + 2000
    fun configFile() = sourceConfigFileName.split(".").joinToString("-$port.")

    val skriptApplication by lazy { Async.awaitSucceededFuture(createApplication(configFile()))!! }

    val stageManager by lazy  {
        skriptApplication.applicationResources
                .filter { it.value is ApplicationStageManager }
                .map { it.value as ApplicationStageManager }
                .first()
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())
        awaitSucceededFuture(Skript.identity<Unit, SkriptApplicationLoader>()
                .map { FileReference.Relative(sourceConfigFileName) }
                .readFile()
                .map { it.readText() }
                .map { it.replace("\"port\":8080", "\"port\":$port")}
                .split(Skript.identity<String, SkriptApplicationLoader>()
                        .map { FileReference.Relative(configFile()) }
                        .createFile()
                        .writeFile())
                .join { json, writer ->
                    writer.write(json)
                    writer.flush()
                    writer.close()
                }
                .run(Unit, loader))!!
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager.hireTroupe().initUserSchema())
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.initAction),
                Unit))
    }

    override fun afterSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(skriptApplication.tearDown())
        Files.delete(Paths.get(configFile()))
    }

    private  fun createUser(userProfileAndPassword: UserProfileAndPassword): UserProfile =
        awaitSucceededFuture(userClient.createUserRequestSkript
                .run(userProfileAndPassword, stageManager.hireTroupe()))!!

    private fun loginUser(userNameAndPassword: UserNameAndPassword): UserSession =
            awaitSucceededFuture(userClient.loginRequestSkript
                    .run(userNameAndPassword, stageManager.hireTroupe()))!!

    private fun getUser(userId: String, authToken: String): UserProfile =
        awaitSucceededFuture(userClient.getUserRequestSkript
                .run(TokenAndInput(authToken, userId), stageManager.hireTroupe()))!!

    private fun createChatRoom(chatRoom: ChatRoom, authToken: String): ChatRoom =
            awaitSucceededFuture(chatRoomClient.createChatRoom
                    .run(TokenAndInput(authToken, chatRoom), stageManager.hireTroupe()))!!

    private fun updateChatRoom(chatRoom: ChatRoom, authToken: String): ChatRoom =
            awaitSucceededFuture(chatRoomClient.updateChatRoom
                    .run(TokenAndInput(authToken, chatRoom), stageManager.hireTroupe()))!!

    private fun getChatRoom(chatRoomId: String, authToken: String): ChatRoom =
            awaitSucceededFuture(chatRoomClient.getChatRoom
                    .run(TokenAndInput(authToken, chatRoomId), stageManager.hireTroupe()))!!

    private fun addUserPermissions(chatRoomUser: ChatRoomUser, authToken: String): ChatRoom =
            awaitSucceededFuture(chatRoomClient.addUserPermissions
                    .run(TokenAndInput(authToken, chatRoomUser), stageManager.hireTroupe()))!!

    private fun removeUserPermissions(chatRoomUser: ChatRoomUser, authToken: String): ChatRoom =
            awaitSucceededFuture(chatRoomClient.removeUserPermissions
                    .run(TokenAndInput(authToken, chatRoomUser), stageManager.hireTroupe()))!!

    init {


        "Create a chatroom" {
            val (user1, user1Password) = UserFixture.generateUser(1)
            createUser(UserProfileAndPassword(user1, user1Password))
            val user1Session = loginUser(UserNameAndPassword(user1.name, user1Password))
            val chatroom = ChatRoomFixture.generateChatroom(UUID.randomUUID().toString(), user1, emptySet())

            val createdChatroom = createChatRoom(chatroom, user1Session.sessionKey)

            createdChatroom shouldBe chatroom
        }
    }


}