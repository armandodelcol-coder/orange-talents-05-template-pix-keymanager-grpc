package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.BuscarChavePixRequest
import br.com.zupedu.armando.PixKeyManagerBuscarServiceGrpc
import br.com.zupedu.armando.httpclients.BcbClient
import br.com.zupedu.armando.httpclients.PixKeyDetailsResponse
import br.com.zupedu.armando.httpclients.PixKeyType
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
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class BuscarChaveEndpointTest(
    private val serviceGrpc: PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val bcbClient: BcbClient
) {
    val mensagemErroDeComoConsultar = "Deve informar apenas uma chave válida OU a apenas a combinação clienteId e pixId"

    val dummyChavePix = ChavePix(
        clienteId = "e7eb62c7-20a0-4d6f-90b1-dc4043ab7eb8",
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

    val dummyPixKeyDetailsResponseResponseBcb = PixKeyDetailsResponse(
        keyType = PixKeyType.EMAIL,
        key = dummyChavePix.chave,
        bankAccount = PixKeyDetailsResponse.BankAccountResponse(
            dummyChavePix.conta.instituicaoIspb,
            dummyChavePix.conta.agencia,
            dummyChavePix.conta.numero,
            "CACC"
        ),
        owner = PixKeyDetailsResponse.OwnerResponse(
            "",
            dummyChavePix.conta.titularNome,
            dummyChavePix.conta.titularCpf
        ),
        createdAt = LocalDateTime.now().toString()
    )

    val dummyRequestPorChave = BuscarChavePixRequest.newBuilder()
        .setChave(dummyChavePix.chave)

    val dummyRequestPorPixId = BuscarChavePixRequest.newBuilder()
        .setPixId(dummyChavePix.pixId)
        .setClienteId(dummyChavePix.clienteId)

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar um erro caso não tenha dados de entrada`() {
        // cenario
        val request = BuscarChavePixRequest.newBuilder().build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        Assertions.assertEquals(mensagemErroDeComoConsultar, response.status.description)
    }

    @Test
    fun `deve retornar um erro caso informe dados de chave e de pixId`() {
        // cenario
        val request = BuscarChavePixRequest.newBuilder()
            .setChave("123")
            .setPixId("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        Assertions.assertEquals(mensagemErroDeComoConsultar, response.status.description)
    }

    @Test
    fun `deve retornar um erro caso informe chave vazia`() {
        // cenario
        val request = BuscarChavePixRequest.newBuilder()
            .setChave("")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        Assertions.assertEquals(mensagemErroDeComoConsultar, response.status.description)
    }

    @Test
    fun `deve retornar um erro caso informe pixId e não informa clienteId`() {
        // cenario
        val request = BuscarChavePixRequest.newBuilder()
            .setPixId("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        Assertions.assertEquals(mensagemErroDeComoConsultar, response.status.description)
    }

    @Test
    fun `deve retornar um erro caso informe clienteId e não informa pixId`() {
        // cenario
        val request = BuscarChavePixRequest.newBuilder()
            .setClienteId("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        Assertions.assertEquals(mensagemErroDeComoConsultar, response.status.description)
    }

    @Test
    fun `deve retornar um erro de chave não encontrada quando clienteId não pertence a chave Pix`() {
        // cenario
        repository.save(dummyChavePix)
        val request = dummyRequestPorPixId.setClienteId("123").build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.NOT_FOUND.code, response.status.code)
        Assertions.assertEquals("Chave Pix não encontrada", response.status.description)
    }

    @Test
    fun `deve retornar um erro de chave não encontrada quando chave Pix existe no sistema mas não foi encontrada no BCB`() {
        // cenario
        repository.save(dummyChavePix)
        val request = dummyRequestPorPixId.build()
        Mockito.`when`(bcbClient.buscarChavePix(dummyChavePix.chave)).thenReturn(HttpResponse.notFound())
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.INTERNAL.code, response.status.code)
        Assertions.assertEquals("Chave Pix não encontrada no BCB", response.status.description)
    }

    @Test
    fun `deve retornar a chave pix consultada com sucesso quando informado pixId e clienteId válidos`() {
        // cenario
        repository.save(dummyChavePix)
        val request = dummyRequestPorPixId.build()
        Mockito.`when`(bcbClient.buscarChavePix(dummyChavePix.chave)).thenReturn(HttpResponse.ok())
        // acao
        val response = serviceGrpc.buscar(request)
        // validacao
        Assertions.assertEquals(dummyChavePix.chave, response.chavePix.chave)
        Assertions.assertEquals(dummyChavePix.tipoConta.name, response.chavePix.conta.tipoConta.name)
    }

    @Test
    fun `deve retornar um erro de chave não encontrada quando chave não existe no sistema e não foi encontrada no BCB`() {
        // cenario
        val request = dummyRequestPorChave.build()
        Mockito.`when`(bcbClient.buscarChavePix(dummyChavePix.chave)).thenReturn(HttpResponse.notFound())
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.buscar(request)
        }
        // validacao
        Assertions.assertEquals(Status.NOT_FOUND.code, response.status.code)
        Assertions.assertEquals("Chave Pix não encontrada", response.status.description)
    }

    @Test
    fun `deve retornar a chave pix consultada com sucesso quando a chave existir no sistema`() {
        // cenario
        repository.save(dummyChavePix)
        val request = dummyRequestPorChave.build()
        // acao
        val response = serviceGrpc.buscar(request)
        // validacao
        Assertions.assertEquals(dummyChavePix.chave, response.chavePix.chave)
        Assertions.assertEquals(dummyChavePix.tipoConta.name, response.chavePix.conta.tipoConta.name)
    }

    @Test
    fun `deve retornar a chave pix consultada com sucesso quando a chave não existir no sistema mas existir no BCB`() {
        // cenario
        val request = dummyRequestPorChave.build()
        Mockito.`when`(bcbClient.buscarChavePix(dummyChavePix.chave)).thenReturn(HttpResponse.ok(dummyPixKeyDetailsResponseResponseBcb))
        // acao
        val response = serviceGrpc.buscar(request)
        // validacao
        Assertions.assertEquals(dummyChavePix.chave, response.chavePix.chave)
        Assertions.assertEquals(dummyChavePix.tipoConta.name, response.chavePix.conta.tipoConta.name)
    }

    @Factory
    class ClientBuscarChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub? {
            return PixKeyManagerBuscarServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}