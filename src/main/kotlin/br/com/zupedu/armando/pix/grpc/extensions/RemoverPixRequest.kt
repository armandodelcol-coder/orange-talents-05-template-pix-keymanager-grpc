package br.com.zupedu.armando.pix.grpc.extensions

import br.com.zupedu.armando.RemoverPixRequest
import br.com.zupedu.armando.pix.grpc.dtos.RemoverChavePixDto
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RemoverPixRequest.toRemoverChavePixDto(validator: Validator): RemoverChavePixDto {
    val removerChavePixDto = RemoverChavePixDto(
        pixId = pixId,
        clienteId = clienteId
    )

    val errors = validator.validate(removerChavePixDto)
    if (!errors.isEmpty()) { // ha erros
        throw ConstraintViolationException(errors)
    }

    return removerChavePixDto
}