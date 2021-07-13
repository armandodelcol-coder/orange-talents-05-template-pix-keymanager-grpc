package br.com.zupedu.armando.httpclients

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.client.url}")
interface BcbClient {
    @Post("/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun criarChavePix(@Body createPixRequest: CreatePixRequest): HttpResponse<CreatePixResponse>

    @Delete("/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun deletarChavePix(@PathVariable key: String, @Body deletePixKeyrequest: DeletePixKeyRequest): HttpResponse<Any>
}

data class CreatePixRequest(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
) {
    data class BankAccountRequest(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: String
    )

    data class OwnerRequest(
        val type: String,
        val name: String,
        val taxIdNumber: String
    )
}

class CreatePixResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: String
) {
    data class BankAccountRequest(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: String
    )

    data class OwnerRequest(
        val type: String,
        val name: String,
        val taxIdNumber: String
    )
}

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)