package com.baraddur.qwirkle.utils

import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import spark.ModelAndView
import spark.kotlin.RouteHandler
import spark.template.thymeleaf.ThymeleafTemplateEngine
import spark.utils.IOUtils
import java.io.File
import javax.servlet.MultipartConfigElement
import javax.servlet.http.Part


fun RouteHandler.respondJson(statusCode: Int = 200, body: () -> Any): Any {
    response.status(statusCode)
    response.type(ContentType.APPLICATION_JSON.toString())
    return objectMapper().writeValueAsString(body())
}

fun RouteHandler.respondText(statusCode: Int = 200, body: () -> String): String {
    response.status(statusCode)
    response.type(ContentType.TEXT_PLAIN.toString())
    return body()
}

fun RouteHandler.queryParamsOpt(key: String): String? {
    return request.queryParams(key)
}

fun RouteHandler.respond(statusCode: Int, message: String) {
    response.status(statusCode)
    response.body(message)
}

fun RouteHandler.respondFile(file: File, filename: String = file.name) {
    response.header("Content-Disposition", "attachment; filename=\"$filename\"")
    response.type(ContentType.APPLICATION_OCTET_STREAM.toString())
    response.raw().setContentLength(file.length().toInt())
    response.status(HttpStatus.SC_OK)
    response.raw().outputStream.use { out ->
        file.inputStream().use { ins ->
            IOUtils.copy(ins, out)
        }
    }
}

fun RouteHandler.render(templatePath: String, model: Map<String, Any?> = emptyMap()): String {
    return ThymeleafTemplateEngine().render(ModelAndView(model, templatePath))
}

fun RouteHandler.multipart(): Collection<Part> {
    request.raw()
        .setAttribute(org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT, MultipartConfigElement("/tmp"))
    return request.raw().parts
}
