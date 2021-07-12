package br.com.zupedu.armando.pix.grpc.endpoints

import br.com.zupedu.armando.PixKeyManagerRemoverServiceGrpc
import br.com.zupedu.armando.RemoverPixRequest
import br.com.zupedu.armando.httpclients.ItauErpClient
import br.com.zupedu.armando.httpclients.dtos.ContaClienteResponse
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
internal class RemoverChavePixEndpointTest(
    private val serviceGrpc: PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val itauErpClient: ItauErpClient
) {
    val dummyRequest = RemoverPixRequest.newBuilder()
        .setClienteId("e7eb62c7-20a0-4d6f-90b1-dc4043ab7eb8")
        .setPixId("5141ae89-8cab-41b1-a6ca-fdcb4508f309")

    val dummyChavePix = ChavePix(
        clienteId = "e7eb62c7-20a0-4d6f-90b1-dc4043ab7eb8",
        tipoChave = TipoChave.EMAIL,
        chave = "email@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "ITAU",
            instituicaoIspb = "123",
            titularNome = "JOAO TESTADOR",
            titularCpf = "54486070046",
            agencia = "123",
            numero = "123456"
        )
    )

    val dummyChavePix2 = ChavePix(
        clienteId = "960d43d6-4d58-4a2b-91fb-33c7abb2ac81",
        tipoChave = TipoChave.CELULAR,
        chave = "+5511999789855",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "ITAU",
            instituicaoIspb = "123",
            titularNome = "DIANA TESTADORA",
            titularCpf = "74582106056",
            agencia = "123",
            numero = "125478"
        )
    )

    val dummyContaChavePix2 = ContaClienteResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = ContaClienteResponse.InstituicaoResponse("ITAU", "123"),
        agencia = "123",
        numero = "125478",
        titular = ContaClienteResponse.TitularResponse("DIANA TESTADORA", "74582106056")
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar uma exception caso pixId não informado`() {
        // cenario
        val request = dummyRequest.setPixId("").build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.remover(request)
        }
        // validacao
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
    }

    @Test
    fun `deve retornar uma exception caso clientId não informado`() {
        // cenario
        val request = dummyRequest.setClienteId("").build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.remover(request)
        }
        // validacao
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
    }

    @Test
    fun `deve retornar uma exception de not found quando o pixId não for encontrado`() {
        // cenario
        val request = dummyRequest.build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.remover(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve retornar uma exception quando não encontrar a conta do cliente informado`() {
        // cenario
        repository.save(dummyChavePix)
        val chavePix = repository.findAll().first()
        val request = dummyRequest.setPixId(chavePix.pixId).build()
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, chavePix.tipoConta.name)).thenReturn(
            HttpResponse.notFound()
        )
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.remover(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("Conta não encontrada no Itau.", status.description)
        }
    }

    @Test
    fun `deve retornar uma exception quando a chave pix não pertence ao cliente informado`() {
        // cenario
        repository.save(dummyChavePix)
        val chavePix = repository.findAll().first()
        repository.save(dummyChavePix2)
        val request = dummyRequest
            .setPixId(chavePix.pixId)
            .setClienteId(dummyChavePix2.clienteId)
            .build()
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, dummyContaChavePix2.tipo)).thenReturn(HttpResponse.ok(dummyContaChavePix2))
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.remover(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("ChavePix não pertence ao cliente informado.", status.description)
        }
    }

    @Test
    fun `deve remover a chavePix com sucesso`() {
        // cenario
        repository.save(dummyChavePix2)
        val chavePix = repository.findAll().first()
        val request = dummyRequest
            .setPixId(chavePix.pixId)
            .setClienteId(chavePix.clienteId)
            .build()
        Mockito.`when`(itauErpClient.buscarContaCliente(request.clienteId, chavePix.tipoConta.name)).thenReturn(HttpResponse.ok(dummyContaChavePix2))
        // acao
        val response = serviceGrpc.remover(request)
        // validacao
        assertEquals("Chave Pix excluída com sucesso.", response.info)
        assertTrue(repository.findByPixId(chavePix.pixId).isEmpty)
    }

    @Factory
    class ClientRemoverChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub? {
            return PixKeyManagerRemoverServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauErpClient::class)
    fun itauErpMock(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }
}