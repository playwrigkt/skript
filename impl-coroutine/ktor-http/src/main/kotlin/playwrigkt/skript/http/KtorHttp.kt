package playwrigkt.skript.http

import io.ktor.http.HttpMethod

fun method(method: Http.Method): HttpMethod {
    return when(method) {
        Http.Method.Get -> HttpMethod.Get
        Http.Method.Post -> HttpMethod.Post
        Http.Method.Put -> HttpMethod.Put
        Http.Method.Options -> HttpMethod.Options
        Http.Method.Delete -> HttpMethod.Delete
        Http.Method.Patch -> HttpMethod.Patch
        Http.Method.Head -> HttpMethod.Head
        is Http.Method.Other -> HttpMethod(method.name)
        else -> HttpMethod(method.javaClass.simpleName.toUpperCase())
    }
}