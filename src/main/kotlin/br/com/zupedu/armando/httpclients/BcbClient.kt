package br.com.zupedu.armando.httpclients

import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.grpc.dtos.ChavePixDetalhesDto
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

    @Get("/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    fun buscarChavePix(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
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

data class CreatePixResponse(
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

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String
) {
    fun toChavePixDetalhesDto(): ChavePixDetalhesDto {
        return ChavePixDetalhesDto(
            tipoChave = keyType.domainType!!,
            chave = this.key,
            tipoConta = when (this.bankAccount.accountType) {
                "CACC" -> TipoConta.CONTA_CORRENTE
                "SVGS" -> TipoConta.CONTA_POUPANCA
                else -> throw IllegalArgumentException("accountType invalid or not found for ${this.bankAccount.accountType}")
            },
            conta = ChavePixDetalhesDto.ContaAssociadaDto(
                /**
                 * 60701190 ITAÚ UNIBANCO S.A.
                 * https://www.bcb.gov.br/pom/spb/estatistica/port/ASTR003.pdf (line 221)
                 */
                instituicaoNome = "ITAÚ UNIBANCO S.A.",
                instituicaoIspb = bankAccount.participant,
                titularNome = owner.name,
                titularCpf = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber
            )
        )
    }

    data class BankAccountResponse(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: String
    )

    data class OwnerResponse(
        val type: String,
        val name: String,
        val taxIdNumber: String
    )
}

enum class PixKeyType(val domainType: TipoChave?) {
    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.RANDOMICA);

    companion object {
        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)
        fun by(domainType: TipoChave): PixKeyType {
            return  mapping[domainType] ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}