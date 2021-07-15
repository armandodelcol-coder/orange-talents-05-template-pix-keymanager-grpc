package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.ListarPixRequest
import br.com.zupedu.armando.PixKeyManagerListarServiceGrpc
import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.model.ChavePix
import br.com.zupedu.armando.pix.model.ContaAssociada
import br.com.zupedu.armando.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class ListarChavePixEndpointTest(
    private val service: PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub,
    private val repository: ChavePixRepository
) {
    val dummyChavePix = ChavePix(
        clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
        tipoChave = TipoChave.EMAIL,
        chave = "email@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "ITAU",
            instituicaoIspb = "60701190",
            titularNome = "JOAO TESTADOR",
            titularCpf = "54486070046",
            agencia = "123",
            numero = "123456"
        )
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar um erro de clienteId não informado`() {
        // cenario
        val request = ListarPixRequest.newBuilder().build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            service.listar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("clienteId deve ser informado.", status.description)
        }
    }

    @Test
    fun `deve retornar uma lista de ChavePix`() {
        // cenario
        repository.save(dummyChavePix)
        val request = ListarPixRequest.newBuilder().setClienteId(dummyChavePix.clienteId).build()
        // acao
        val response = service.listar(request)
        // validacao
        with(response) {
            assertEquals(1, response.chavesCount)
        }
    }


    @Factory
    class ClientListarChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub? {
            return PixKeyManagerListarServiceGrpc.newBlockingStub(channel)
        }
    }
}