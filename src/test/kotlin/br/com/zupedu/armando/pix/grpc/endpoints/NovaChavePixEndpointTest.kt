package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import br.com.zupedu.armando.RegistrarPixRequest
import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import br.com.zupedu.armando.httpclients.ItauErpClient
import br.com.zupedu.armando.httpclients.dtos.ContaClienteResponse
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
internal class NovaChavePixEndpointTest(
    private val serviceGrpc: PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub,
    private val itauErpClient: ItauErpClient,
    private val repository: ChavePixRepository
) {
    @MockBean(ItauErpClient::class)
    fun itauErpMock(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    val dummyRequest = RegistrarPixRequest.newBuilder()
        .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
        .setTipoChave(TipoChave.EMAIL)
        .setChave("email@mail.com")
        .setTipoConta(TipoConta.CONTA_CORRENTE)

    val dummyChavePix = ChavePix(
        clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
        tipoChave = br.com.zupedu.armando.pix.enums.TipoChave.EMAIL,
        chave = "email@mail.com",
        tipoConta = br.com.zupedu.armando.pix.enums.TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "ITAU",
            instituicaoIspb = "123",
            titularNome = "JOAO TESTADOR",
            titularCpf = "54486070046",
            agencia = "123",
            numero = "123456"
        )
    )

    val dummyContaClienteResponse = ContaClienteResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = ContaClienteResponse.InstituicaoResponse("ITAU", "123"),
        agencia = "123",
        numero = "123456",
        titular = ContaClienteResponse.TitularResponse("JOAO TESTADOR", "54486070046")
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar um erro quando tipo de chave for desconhecido`() {
        // cenario
        val request = dummyRequest.setTipoChave(TipoChave.CHAVE_DESCONHECIDA).build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `deve retornar um erro quando tipo de conta for desconhecida`() {
        // cenario
        val request = dummyRequest.setTipoConta(TipoConta.CONTA_DESCONHECIDA).build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `deve validar a chave quando o tipo for CPF`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.CPF)
            .setChave("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave é obrigatória e formato esperado deve ser um CPF válido.", status.description)
        }
    }

    @Test
    fun `deve validar a chave quando o tipo for CELULAR`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.CELULAR)
            .setChave("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave é obrigatória e formato esperado deve ser +5585988714077", status.description)
        }
    }

    @Test
    fun `deve validar a chave quando o tipo for EMAIL`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.EMAIL)
            .setChave("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave é obrigatória e formato esperado deve ser seu@email.com", status.description)
        }
    }

    @Test
    fun `deve validar se informar a chave quando o tipo for RANDOMICA`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.RANDOMICA)
            .setChave("123")
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Para tipo chave RANDOMICA não deve ser informada uma chave.", status.description)
        }
    }

    @Test
    fun `deve retornar um erro quando cliente não encontrado`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.RANDOMICA)
            .setChave("")
            .build()
        Mockito.`when`(itauErpClient.buscarCliente(request.clienteId)).thenReturn(HttpResponse.notFound())
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("Cliente não encontrado.", status.description)
        }
    }

    @Test
    fun `deve retornar um erro quando cliente não possui tipo conta informado`() {
        // cenario
        val request = dummyRequest
            .setTipoChave(TipoChave.RANDOMICA)
            .setChave("")
            .build()
        Mockito.`when`(itauErpClient.buscarCliente(request.clienteId)).thenReturn(HttpResponse.ok())
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, request.tipoConta.name)).thenReturn(HttpResponse.notFound())
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("Tipo de conta não encontrada para o cliente no Itau.", status.description)
        }
    }

    @Test
    fun `não deve registrar uma chave já existente`() {
        // cenario
        repository.save(dummyChavePix)
        val request = dummyRequest.build()
        Mockito.`when`(itauErpClient.buscarCliente(request.clienteId)).thenReturn(HttpResponse.ok())
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, request.tipoConta.name)).thenReturn(HttpResponse.ok(dummyContaClienteResponse))
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave ${dummyChavePix.chave} já existe.", status.description)
        }
    }

    @Test
    fun `deve retornar um erro quando clienteId não informado`() {
        // cenario
        val request = dummyRequest.setClienteId("").build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.registrar(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    // HAPPY PATH
    @Test
    fun `deve registrar uma chave pix`() {
        // cenario
        val request = dummyRequest.build()
        Mockito.`when`(itauErpClient.buscarCliente(request.clienteId)).thenReturn(HttpResponse.ok())
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, request.tipoConta.name)).thenReturn(HttpResponse.ok(dummyContaClienteResponse))
        // acao
        val response = serviceGrpc.registrar(request)
        // validacao
        val chavePix = repository.findAll().first()
        with(response) {
            assertEquals(chavePix.pixId, pixId)
        }
    }

    @Factory
    class ClientRegistrarChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub? {
            return PixKeyManagerRegistrarServiceGrpc.newBlockingStub(channel)
        }
    }
}