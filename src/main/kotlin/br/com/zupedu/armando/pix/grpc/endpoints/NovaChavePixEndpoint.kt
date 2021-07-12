package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.RegistrarPixRequest
import br.com.zupedu.armando.RegistrarPixResponse
import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import br.com.zupedu.armando.core.handler.ErrorAroundHandler
import br.com.zupedu.armando.pix.grpc.services.NovaChavePixService
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
): PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceImplBase() {
    private val logger = LoggerFactory.getLogger(NovaChavePixEndpoint::class.java)

    override fun registrar(request: RegistrarPixRequest, responseObserver: StreamObserver<RegistrarPixResponse>) {
        // Validações de entrada
        logger.info("Validando dados de entrada")
        ValidaEnumTipoDesconhecido.tipoChave(request.tipoChave)
        ValidaEnumTipoDesconhecido.tipoConta(request.tipoConta)
        val novaChavePixDto = request.toChavePixDto(validator)
        novaChavePixDto.tipoChave?.valida(novaChavePixDto.chave)
        logger.info("dados de entrada OK.")
        // Criar uma nova ChavePix
        val chavePix = service.registra(novaChavePixDto)

        responseObserver.onNext(RegistrarPixResponse.newBuilder().setPixId(chavePix.pixId).build())
        responseObserver.onCompleted()
    }
}