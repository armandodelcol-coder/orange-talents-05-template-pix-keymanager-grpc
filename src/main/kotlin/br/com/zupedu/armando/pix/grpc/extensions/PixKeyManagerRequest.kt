package br.com.zupedu.armando.pix.grpc.extensions

import br.com.zupedu.armando.PixKeyManagerRequest
import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import br.com.zupedu.armando.pix.dtos.ChavePixDto
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun PixKeyManagerRequest.toChavePixDto(validator: Validator): ChavePixDto {
    val chavePixDto = ChavePixDto(
        clienteId = clienteId,
        tipoChave = br.com.zupedu.armando.pix.enum.TipoChave.valueOf(tipoChave.name),
        chave = chave,
        tipoConta = br.com.zupedu.armando.pix.enum.TipoConta.valueOf(tipoConta.name)
    )

    val errors = validator.validate(chavePixDto)
    if (!errors.isEmpty()) { // ha erros
        throw ConstraintViolationException(errors)
    }

    return chavePixDto
}