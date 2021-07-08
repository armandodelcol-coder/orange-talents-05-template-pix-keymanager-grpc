package br.com.zupedu.armando.pix.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class ContaAssociada(
        @field:NotBlank
        @Column(name = "conta_instituicao_nome", nullable = false)
        val instituicaoNome: String,

        @field:NotBlank
        @Column(name = "conta_instituicao_ispb", nullable = false)
        val instituicaoIspb: String,

        @field:NotBlank
        @Column(name = "conta_titular_nome", nullable = false)
        val titularNome: String,

        @field:NotBlank
        @field:Size(max = 11)
        @Column(name = "conta_titular_cpf", length = 11, nullable = false)
        val titularCpf: String,

        @field:NotBlank
        @field:Size(max = 4)
        @Column(name = "conta_agencia", length = 4, nullable = false)
        val agencia: String,

        @field:NotBlank
        @field:Size(max = 6)
        @Column(name = "conta_numero", length = 6, nullable = false)
        val numero: String
) {
}