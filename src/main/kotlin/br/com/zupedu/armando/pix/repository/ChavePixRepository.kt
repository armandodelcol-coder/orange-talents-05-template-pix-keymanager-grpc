package br.com.zupedu.armando.pix.repository

import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.pix.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun existsByClienteIdAndChaveAndTipoChave(clienteId: String, chave: String, tipoChave: TipoChave): Boolean
}