package br.com.zupedu.armando.repository

import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun existsByClienteIdAndChaveAndTipoChave(clienteId: String, chave: String, tipoChave: TipoChave): Boolean
}