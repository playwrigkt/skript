package playwrigkt.skript.http.client

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult

data class HttpClientUriMappingSkript<I, Troupe>(val useSsl: Skript<I, Boolean, Troupe>,
                                                 val host: Skript<I, String, Troupe>,
                                                 val port: Skript<I, Int?, Troupe>,
                                                 val pathTemplate: Skript<I, String, Troupe>): Skript<I, String, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<String> {
        val sslResult = useSsl.run(i, troupe)
        val hostResult = host.run(i, troupe)
        val portResult = port.run(i, troupe)
        val pathTemplateResult = pathTemplate.run(i, troupe)

        return sslResult.flatMap { useSsl ->
            hostResult.flatMap { host ->
                portResult.flatMap { port ->
                    pathTemplateResult.map { pathTemplate ->
                        "http${if(useSsl) "s" else "" }://$host:$port/${pathTemplate.removePrefix("/").removeSuffix("/")}"
                    } } }
        }
    }
}