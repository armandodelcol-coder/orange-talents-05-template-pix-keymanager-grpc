package br.com.zupedu.armando.pix.model

import br.com.zupedu.armando.core.handler.exceptions.BadRequestErrorException
import br.com.zupedu.armando.core.handler.exceptions.NotFoundDefaultException
import br.com.zupedu.armando.httpclients.BcbClient
import br.com.zupedu.armando.pix.grpc.dtos.ChavePixDetalhesDto
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

sealed class BuscarChavePixFiltro {
    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhesDto

    @Introspected
    data class PorPixId(
        @field:NotBlank val clienteId: String,
        @field:NotBlank val pixId: String,
    ): BuscarChavePixFiltro() {
        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhesDto {
            LOGGER.info("Consultando chave Pix por pixId e clienteId")
            val chavePixDetalhesDto = repository.findByPixId(pixId)
                .filter { it.pertenceAoCliente(clienteId) }
                .map(ChavePixDetalhesDto::from)
                .orElseThrow { NotFoundDefaultException("Chave Pix não encontrada") }
            LOGGER.info("Consultando chave Pix '${chavePixDetalhesDto.chave}' no Banco Central do Brasil (BCB)")
            val response = bcbClient.buscarChavePix(chavePixDetalhesDto.chave)
            when (response.status) {
                HttpStatus.OK -> LOGGER.info("Chave Pix encontrada no BCB")
                else -> throw BadRequestErrorException("Chave Pix não encontrada no BCB")
            }

            return chavePixDetalhesDto
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : BuscarChavePixFiltro() {
        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhesDto {
            LOGGER.info("Consultando chave Pix por chave")
            return repository.findByChave(chave)
                .map(ChavePixDetalhesDto::from)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")
                    val response = bcbClient.buscarChavePix(chave)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toChavePixDetalhesDto()
                        else -> throw NotFoundDefaultException("Chave Pix não encontrada")
                    }
                }
        }
    }

}
