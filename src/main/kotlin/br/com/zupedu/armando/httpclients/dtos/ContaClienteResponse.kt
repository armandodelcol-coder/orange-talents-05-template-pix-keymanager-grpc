package br.com.zupedu.armando.httpclients.dtos

import br.com.zupedu.armando.pix.model.ContaAssociada

data class ContaClienteResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicaoNome = this.instituicao.nome,
            instituicaoIspb = this.instituicao.ispb,
            titularNome = this.titular.nome,
            titularCpf = this.titular.cpf,
            agencia = this.agencia,
            numero = this.numero
        )
    }

    data class TitularResponse(val nome: String, val cpf: String)
    data class InstituicaoResponse(val nome: String, val ispb: String)
}
