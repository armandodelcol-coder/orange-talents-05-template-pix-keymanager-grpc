package br.com.zupedu.armando.pix.grpc.dtos

import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.model.ChavePix
import br.com.zupedu.armando.pix.model.ContaAssociada
import java.time.LocalDateTime

data class ChavePixDetalhesDto(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipoChave: TipoChave,
    val chave: String,
    val tipoConta: TipoConta,
    val conta: ContaAssociadaDto,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {
    data class ContaAssociadaDto(
        val instituicaoNome: String,
        val instituicaoIspb: String,
        val titularNome: String,
        val titularCpf: String,
        val agencia: String,
        val numero: String
    ) {
        constructor(contaAssociada: ContaAssociada) : this(
            instituicaoNome = contaAssociada.instituicaoNome,
            instituicaoIspb = contaAssociada.instituicaoIspb,
            titularNome = contaAssociada.titularNome,
            titularCpf = contaAssociada.titularCpf,
            agencia = contaAssociada.agencia,
            numero = contaAssociada.numero
        )
    }

    companion object {
        fun from(chave: ChavePix): ChavePixDetalhesDto {
            return ChavePixDetalhesDto(
                pixId = chave.pixId,
                clienteId = chave.clienteId,
                tipoChave = chave.tipoChave,
                chave = chave.chave,
                tipoConta = chave.tipoConta,
                conta = ContaAssociadaDto(chave.conta),
                registradaEm = chave.criadaEm
            )
        }
    }
}
