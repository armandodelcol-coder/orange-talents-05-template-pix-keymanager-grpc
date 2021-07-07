package br.com.zupedu.armando.httpclients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itauerp.client.url}")
interface ItauErpClient {
    @Get("/v1/clientes/{clienteId}")
    fun buscarCliente(@PathVariable clienteId: String): HttpResponse<Any>

    @Get("/v1/clientes/{clienteId}/contas")
    fun buscarContaCliente(@PathVariable clienteId: String, @QueryValue("tipo") tipo: String): HttpResponse<Any>
}