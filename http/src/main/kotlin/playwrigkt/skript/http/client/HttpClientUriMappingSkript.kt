package playwrigkt.skript.http.client

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult

data class HttpClientUriMappingSkript<I, Troupe>(val useSsl: Skript<I, Boolean, Troupe>,
                                                 val host: Skript<I, String, Troupe>,
                                                 val port: Skript<I, Int?, Troupe>,
                                                 val pathTemplate: Skript<I, String, Troupe>,
                                                 val pathParameters: Skript<I, Map<String, String>, Troupe>,
                                                 val queryParameters: Skript<I, Map<String, List<String>>, Troupe>): Skript<I, HttpClient.URI, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<HttpClient.URI> {
        val sslResult = useSsl.run(i, troupe)
        val hostResult = host.run(i, troupe)
        val portResult = port.run(i, troupe)
        val pathTemplateResult = pathTemplate.run(i, troupe)
        val pathParametersResult = pathParameters.run(i, troupe)
        val queryParametersResult = queryParameters.run(i, troupe)

        return sslResult.flatMap { useSsl ->
            hostResult.flatMap { host ->
            portResult.flatMap { port ->
            pathTemplateResult.flatMap { pathTemplate ->
            pathParametersResult.flatMap { pathParameters ->
            queryParametersResult.map { queryParameters ->
                HttpClient.URI(useSsl, host, port, pathTemplate, pathParameters, queryParameters)
            } } } } } }
    }
}