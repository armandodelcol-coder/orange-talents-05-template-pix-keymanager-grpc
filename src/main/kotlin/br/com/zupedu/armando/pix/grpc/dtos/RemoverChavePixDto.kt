package br.com.zupedu.armando.pix.grpc.dtos

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoverChavePixDto(
    @field:NotBlank
    val pixId: String,

    @field:NotBlank
    val clienteId: String
) {
}
