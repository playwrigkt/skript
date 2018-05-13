package playwrigkt.skript.user

import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.amqp.AMQPManager

class JDBCUserServiceSpec: UserServiceSpec() {
    override val sourceConfigFileName: String = "coroutine-application.json"

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(AMQPManager.connectionFactory()).close()
    }
    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(AMQPManager.connectionFactory()).close()
    }
}