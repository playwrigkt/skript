## Example User API

This is an example application implemented via skript.  This project
serves as a technical demo for what skript can do.  Currently, it
implements some rest endpoints for modifying sql resources and
publishing to queues.

More functionality is added to this api as it is implemented.

Currently showcased features:
* [SQL Mapping](https://github.com/dgoetsch/skript/blob/master/examples/api/src/main/kotlin/playwrigkt/skript/user/sql/UserQueries.kt)
* [Service Transactions](https://github.com/dgoetsch/skript/blob/master/examples/api/src/main/kotlin/playwrigkt/skript/user/UserSkripts.kt)
* Http
  * [Http Server](https://github.com/dgoetsch/skript/blob/master/examples/api/src/main/kotlin/playwrigkt/skript/venue/ExampleHttpVenue.kt)
  * [Mappings](https://github.com/dgoetsch/skript/blob/master/examples/api/src/main/kotlin/playwrigkt/skript/user/http/UserHttpSkripts.kt)
* [Queue publisher](https://github.com/dgoetsch/skript/blob/master/examples/api/src/main/kotlin/playwrigkt/skript/user/UserSkripts.kt#L17)
* [Queue Consumer](https://github.com/dgoetsch/skript/blob/master/examples/api/src/test/kotlin/playwrigkt/skript/user/UserServiceSpec.kt#L56)
* [Vertx](https://github.com/dgoetsch/skript/blob/master/examples/api/src/test/kotlin/playwrigkt/skript/user/VertxUserServiceSpec.kt) or [Library](https://github.com/dgoetsch/skript/blob/master/examples/api/src/test/kotlin/playwrigkt/skript/user/JdbcUserServiceSpec.kt) based implementation