package br.com.zupedu.armando.pix.grpc.extensions

import br.com.zupedu.armando.RegistrarPixRequest
import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.grpc.dtos.RegistrarChavePixDto
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RegistrarPixRequest.toChavePixDto(validator: Validator): RegistrarChavePixDto {
    val chavePixDto = RegistrarChavePixDto(
        clienteId = clienteId,
        tipoChave = TipoChave.valueOf(tipoChave.name),
        chave = chave,
        tipoConta = TipoConta.valueOf(tipoConta.name)
    )

    val errors = validator.validate(chavePixDto)
    if (!errors.isEmpty()) { // ha erros
        throw ConstraintViolationException(errors)
    }

    return chavePixDto
}