package br.com.zupedu.armando.pix.grpc.dtos

import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.model.ChavePix
import br.com.zupedu.armando.pix.model.ContaAssociada
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class RegistrarChavePixDto(
    @field:NotBlank
    val clienteId: String,

    @field:NotNull
    val tipoChave: TipoChave,

    val chave: String,

    @field:NotNull
    val tipoConta: TipoConta
) {
    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = if (chave.isNullOrBlank()) UUID.randomUUID().toString() else chave,
            tipoConta = tipoConta,
            conta = conta
        )
    }
}
