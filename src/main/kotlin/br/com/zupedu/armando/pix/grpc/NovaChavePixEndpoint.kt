package br.com.zupedu.armando.pix.grpc

import br.com.zupedu.armando.PixKeyManagerRequest
import br.com.zupedu.armando.PixKeyManagerResponse
import br.com.zupedu.armando.PixKeyManagerServiceGrpc
import br.com.zupedu.armando.core.handler.ErrorAroundHandler
import br.com.zupedu.armando.pix.grpc.extensions.toChavePixDto
import br.com.zupedu.armando.pix.validations.ValidaEnumTipoDesconhecido
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class NovaChavePixEndpoint(
    private val validator: Validator,
    private val service: NovaChavePixService
): PixKeyManagerServiceGrpc.PixKeyManagerServiceImplBase() {
    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    override fun registrar(request: PixKeyManagerRequest, responseObserver: StreamObserver<PixKeyManagerResponse>) {
        // Validações de entrada
        logger.info("Validando dados de entrada")
        ValidaEnumTipoDesconhecido.tipoChave(request.tipoChave)
        ValidaEnumTipoDesconhecido.tipoConta(request.tipoConta)
        val novaChavePixDto = request.toChavePixDto(validator)
        novaChavePixDto.tipoChave?.valida(novaChavePixDto.chave)
        logger.info("dados de entrada OK.")
        // Criar uma nova ChavePix
        val chavePix = service.registra(novaChavePixDto)

        responseObserver.onNext(PixKeyManagerResponse.newBuilder().setPixId(chavePix.pixId).build())
        responseObserver.onCompleted()
    }
}