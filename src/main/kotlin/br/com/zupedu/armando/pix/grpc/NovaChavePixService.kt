package br.com.zupedu.armando.pix.grpc

import br.com.zupedu.armando.*
import br.com.zupedu.armando.httpclients.ItauErpClient
import br.com.zupedu.armando.pix.model.ChavePix
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class NovaChavePixService(
    private val itauErpClient: ItauErpClient,
    private val chavePixRepository: ChavePixRepository
): PixKeyManagerServiceGrpc.PixKeyManagerServiceImplBase() {
    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    override fun registrar(
        request: PixKeyManagerRequest,
        responseObserver: StreamObserver<PixKeyManagerResponse>
    ) {
        // Verificar existencia do cliente
        val clienteResponse = itauErpClient.buscarCliente(request.clienteId)
        when (clienteResponse.status.code) {
            404 -> {
                val e = Status.INVALID_ARGUMENT
                    .withDescription("ClienteId informado não foi encontrado.")
                    .augmentDescription("ClienteId é obrigatório e deve ser correspondente a um cliente válido.")
                    .asRuntimeException()
                responseObserver.onError(e)
                return
            }
            200 -> logger.info("Cliente encontrado.")
            else -> {
                responseObserver.onError(Status.INTERNAL.asRuntimeException())
                return
            }
        }

        // Validar o tipo de chave
        if (request.tipoChave.equals(TipoChave.TIPO_CHAVE_DESCONHECIDO)) {
            val tiposValidos = TipoChave.values()
                .filter { it.ordinal != 0 && it.ordinal != TipoChave.values().size - 1 }
                .map { "${it.ordinal} - ${it.name}" }
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("TipoChave inválido, verifique e tente novamente.")
                    .augmentDescription("TipoChave é obrigatório e valores validos são: $tiposValidos")
                    .asRuntimeException()
            )
            return
        }

        // Validar chave
        var chave = request.chave
        when (request.tipoChave) {
            TipoChave.CPF -> {
                if (!request.chave.matches("^[0-9]{11}\$".toRegex())) {
                    responseObserver.onError(
                        Status.INVALID_ARGUMENT
                            .withDescription("chave não informada ou formato inválido.")
                            .augmentDescription("chave é obrigatória e formato esperado deve ser 12345678901")
                            .asRuntimeException()
                    )
                    return
                }
            }
            TipoChave.CELULAR -> {
                if (!request.chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                    responseObserver.onError(
                        Status.INVALID_ARGUMENT
                            .withDescription("chave não informada ou formato inválido.")
                            .augmentDescription("chave é obrigatória e formato esperado deve ser +5585988714077")
                            .asRuntimeException()
                    )
                    return
                }
            }
            TipoChave.EMAIL -> {
                if (!request.chave.matches("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".toRegex())) {
                    responseObserver.onError(
                        Status.INVALID_ARGUMENT
                            .withDescription("chave não informada ou formato inválido.")
                            .augmentDescription("chave é obrigatória e formato esperado deve ser seu@email.com")
                            .asRuntimeException()
                    )
                    return
                }
            }
            TipoChave.RANDOMICA -> {
                if (!request.chave.isNullOrBlank()) {
                    responseObserver.onError(
                        Status.INVALID_ARGUMENT
                            .withDescription("Para tipo chave RANDOMICA não deve ser informada uma chave.")
                            .asRuntimeException()
                    )
                    return
                }
                chave = UUID.randomUUID().toString()
            }
        }

        // validar tipo conta
        if (request.tipoConta.ordinal == 0 || request.tipoConta.ordinal == TipoConta.values().size - 1) {
            val tiposValidos = TipoConta.values()
                .filter { it.ordinal != 0 && it.ordinal != TipoConta.values().size - 1 }
                .map { "${it.ordinal} - ${it.name}" }
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("TipoConta não informado ou inválido.")
                    .augmentDescription("TipoConta válidos: ${tiposValidos}")
                    .asRuntimeException()
            )
            return
        }
        val tipoConta = "CONTA_${request.tipoConta.name}"
        val clienteContaResponse = itauErpClient.buscarContaCliente(request.clienteId, tipoConta)
        when (clienteContaResponse.status.code) {
            404 -> {
                val e = Status.INVALID_ARGUMENT
                    .withDescription("TipoConta não foi encontrada.")
                    .augmentDescription("O cliente não possuí uma conta do tipo informado.")
                    .asRuntimeException()
                responseObserver.onError(e)
                return
            }
            200 -> logger.info("TipoConta encontrada.")
            else -> {
                responseObserver.onError(Status.INTERNAL.asRuntimeException())
                return
            }
        }

        // salvar a chave pix
        val chaveExiste = chavePixRepository.existsByClienteIdAndChaveAndTipoChave(
            request.clienteId,
            chave,
            request.tipoChave
        )
        if (chaveExiste) {
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Essa ChavePix já existe para o cliente informado")
                    .asRuntimeException()
            )
            return
        }

        val chavePix = ChavePix(
            request.clienteId,
            request.tipoChave,
            chave,
            request.tipoConta
        )
        try {
            chavePixRepository.save(chavePix)
            logger.info("Chave Pix Criada com Sucesso")
        } catch (e: ConstraintViolationException) {
            logger.warn("Problemas ao criar a chave Pix")
            logger.error(e.message)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("dados de entrada inválidos.")
                    .asRuntimeException()
            )
            return
        }

        responseObserver.onNext(PixKeyManagerResponse.newBuilder().setPixId(chavePix.pixId).build())
        responseObserver.onCompleted()
    }
}