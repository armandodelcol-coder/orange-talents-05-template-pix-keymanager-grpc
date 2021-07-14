package br.com.zupedu.armando.pix.grpc.extensions

import br.com.zupedu.armando.BuscarChavePixRequest
import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import br.com.zupedu.armando.pix.model.BuscarChavePixFiltro
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun BuscarChavePixRequest.toFiltro(validator: Validator): BuscarChavePixFiltro {
    val filtro = if (chave.isNullOrBlank() && pixId.isNotBlank() && clienteId.isNotBlank()) {
        BuscarChavePixFiltro.PorPixId(clienteId = clienteId, pixId = pixId)
    } else if (pixId.isNullOrBlank() && clienteId.isNullOrBlank() && chave.isNotBlank()) {
        BuscarChavePixFiltro.PorChave(chave)
    } else {
        throw ArgumentoDeEntradaInvalidoDefaultException(
            "Deve informar apenas uma chave válida OU a apenas a combinação clienteId e pixId"
        )
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}