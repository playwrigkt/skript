package playwrigkt.skript.vertx.ex

import io.vertx.core.MultiMap

fun MultiMap.toMap(): Map<String, List<String>> = this.names().map { it to this.getAll(it)}.toMap()