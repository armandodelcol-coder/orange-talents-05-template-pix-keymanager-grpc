package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.*
import br.com.zupedu.armando.core.handler.ErrorAroundHandler
import br.com.zupedu.armando.httpclients.BcbClient
import br.com.zupedu.armando.pix.grpc.extensions.toFiltro
import br.com.zupedu.armando.pix.grpc.utils.RetornoDetalhesChavePix
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import br.com.zupedu.armando.pix.utils.BcbAccountTypeMapper
import br.com.zupedu.armando.pix.utils.BcbKeyTypeMapper
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class BuscarChaveEndpoint(
    private val validator: Validator,
    private val repository: ChavePixRepository,
    private val bcbClient: BcbClient
): PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceImplBase() {
    override fun buscar(
        request: BuscarChavePixRequest,
        responseObserver: StreamObserver<BuscarChavePixResponse>
    ) {
        val filtro = request.toFiltro(validator)
        val chavePixDetalhesDto = filtro.filtra(repository, bcbClient)
        responseObserver.onNext(RetornoDetalhesChavePix.conversor(chavePixDetalhesDto))
        responseObserver.onCompleted()
    }
}