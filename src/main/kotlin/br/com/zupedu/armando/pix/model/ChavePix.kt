package br.com.zupedu.armando.pix.model

import br.com.zupedu.armando.httpclients.CreatePixRequest
import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.utils.BcbAccountTypeMapper
import br.com.zupedu.armando.pix.utils.BcbKeyTypeMapper
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotBlank
    @Column(nullable = false)
    val clienteId: String,
    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,
    @Column(nullable = false, length = 77, unique = true)
    var chave: String,
    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,
    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    val pixId: String = UUID.randomUUID().toString()

    fun toCriarPixRequest(): CreatePixRequest {
        return CreatePixRequest(
            keyType = BcbKeyTypeMapper.bcbKeyTypeMaps[tipoChave].toString(),
            key = chave,
            bankAccount = CreatePixRequest.BankAccountRequest(
                conta.instituicaoIspb,
                conta.agencia,
                conta.numero,
                BcbAccountTypeMapper.bcbAccountTypeMaps[tipoConta].toString()
            ),
            owner = CreatePixRequest.OwnerRequest(
                type = "NATURAL_PERSON",
                name = conta.titularNome,
                taxIdNumber = conta.titularCpf
            )
        )
    }
}