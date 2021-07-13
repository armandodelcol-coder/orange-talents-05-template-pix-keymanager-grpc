package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.PixKeyManagerRemoverServiceGrpc
import br.com.zupedu.armando.RemoverPixRequest
import br.com.zupedu.armando.RemoverPixResponse
import br.com.zupedu.armando.core.handler.ErrorAroundHandler
import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import br.com.zupedu.armando.core.handler.exceptions.BadRequestErrorException
import br.com.zupedu.armando.core.handler.exceptions.NotFoundDefaultException
import br.com.zupedu.armando.httpclients.BcbClient
import br.com.zupedu.armando.httpclients.DeletePixKeyRequest
import br.com.zupedu.armando.httpclients.ItauErpClient
import br.com.zupedu.armando.pix.grpc.extensions.toRemoverChavePixDto
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class RemoverChavePixEndpoint(
    private val validator: Validator,
    private val repository: ChavePixRepository,
    private val itauErpClient: ItauErpClient,
    private val bcbClient: BcbClient
): PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceImplBase() {
    private val logger = LoggerFactory.getLogger(NovaChavePixEndpoint::class.java)

    override fun remover(request: RemoverPixRequest, responseObserver: StreamObserver<RemoverPixResponse>) {
        // Validações de entrada
        logger.info("Validando dados de entrada para remover chave pix")
        val removerChavePixDto = request.toRemoverChavePixDto(validator)
        val possivelChavePix = repository.findByPixId(removerChavePixDto.pixId)
        if (possivelChavePix.isEmpty) throw NotFoundDefaultException("Chave Pix não encontrada")

        // Validação de existência da conta no Itaú
        logger.info("Validando se a chave pix e cliente informado correspondem a uma conta no erp do itau")
        val chavePix = possivelChavePix.get()
        val clienteContaResponse = itauErpClient.buscarContaCliente(removerChavePixDto.clienteId, chavePix.tipoConta.name)
        val conta = clienteContaResponse.body()?.toModel() ?: throw BadRequestErrorException("Conta não encontrada no Itau.")

        // Valida se a chave pix pertence ao cliente informado
        logger.info("Validando se a chave pix pertence ao cliente informado")
        if (conta.titularCpf != chavePix.conta.titularCpf) throw BadRequestErrorException("ChavePix não pertence ao cliente informado.")

        val deletarNoBcbResponse = bcbClient.deletarChavePix(chavePix.chave, DeletePixKeyRequest(chavePix.chave, chavePix.conta.instituicaoIspb))
        if (deletarNoBcbResponse.status.code != 200) throw BadRequestErrorException("Ocorreu um erro ao deletar a chave pix no BCB.")
        repository.deleteById(chavePix.id)
        logger.info("Chave pix deletada com sucesso")
        responseObserver.onNext(RemoverPixResponse.newBuilder().setInfo("Chave Pix excluída com sucesso.").build())
        responseObserver.onCompleted()
    }
}