package br.com.zupedu.armando.model

import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import java.util.*
import javax.persistence.*
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
    @field:NotBlank
    @Column(nullable = false, length = 77)
    val chave: String,
    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    val pixId: String = UUID.randomUUID().toString()
}