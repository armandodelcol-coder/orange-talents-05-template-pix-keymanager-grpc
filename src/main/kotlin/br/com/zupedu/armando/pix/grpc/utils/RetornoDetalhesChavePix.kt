package br.com.zupedu.armando.pix.grpc.utils

import br.com.zupedu.armando.BuscarChavePixResponse
import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import br.com.zupedu.armando.pix.grpc.dtos.ChavePixDetalhesDto
import com.google.protobuf.Timestamp
import java.time.ZoneId

object RetornoDetalhesChavePix {
    fun conversor(detalhesDto: ChavePixDetalhesDto): BuscarChavePixResponse {
        return BuscarChavePixResponse.newBuilder()
            .setPixId(detalhesDto.pixId ?: "")
            .setClienteId(detalhesDto.clienteId ?: "")
            .setChavePix(BuscarChavePixResponse.ChavePix.newBuilder()
                .setTipoChave(TipoChave.valueOf(detalhesDto.tipoChave.name))
                .setChave(detalhesDto.chave)
                .setConta(BuscarChavePixResponse.ChavePix.Conta.newBuilder()
                    .setTipoConta(TipoConta.valueOf(detalhesDto.tipoConta.name))
                    .setInstituicaoNome(detalhesDto.conta.instituicaoNome)
                    .setInstituicaoIspb(detalhesDto.conta.instituicaoIspb)
                    .setTitularNome(detalhesDto.conta.titularNome)
                    .setTitularCpf(detalhesDto.conta.titularCpf)
                    .setAgencia(detalhesDto.conta.agencia)
                    .setNumero(detalhesDto.conta.numero)
                )
                .setCriadaEm(detalhesDto.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder().setSeconds(createdAt.epochSecond).setNanos(createdAt.nano)
                })
            ).build()
    }
}
