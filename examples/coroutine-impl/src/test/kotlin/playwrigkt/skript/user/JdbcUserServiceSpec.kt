package playwrigkt.skript.user

import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.amqp.AmqpManager

class JdbcUserServiceSpec: UserServiceSpec() {
    override val sourceConfigFileName: String = "coroutine-application.json"

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()

    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()
    }
}