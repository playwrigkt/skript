package playwrigkt.skript.user

import playwrigkt.skript.application.VertxVenueLoader


class VertxUserServiceSpec: UserServiceSpec() {
    override val sourceConfigFileName: String = "vertx-application.json"
}