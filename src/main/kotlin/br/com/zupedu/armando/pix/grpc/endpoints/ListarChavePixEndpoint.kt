package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.*
import br.com.zupedu.armando.core.handler.ErrorAroundHandler
import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
class ListarChavePixEndpoint(
    private val repository: ChavePixRepository
): PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceImplBase() {
    override fun listar(request: ListarPixRequest, responseObserver: StreamObserver<ListarPixResponse>) {
        if (request.clienteId.isNullOrBlank()) throw ArgumentoDeEntradaInvalidoDefaultException("clienteId deve ser informado.")

        val chaves = repository.findByClienteId(request.clienteId)
        responseObserver.onNext(ListarPixResponse.newBuilder().addAllChaves(
            chaves.map { chavePix -> ListarPixResponse.ChavePixListagemResponse.newBuilder()
                .setPixId(chavePix.pixId)
                .setClienteId(chavePix.clienteId)
                .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
                .setChave(chavePix.chave)
                .setTipoConta(TipoConta.valueOf(chavePix.tipoConta.name))
                .setCriadaEm(chavePix.criadaEm.toString())
                .build() }
        ).build())
        responseObserver.onCompleted()
    }
}