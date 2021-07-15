package br.com.zupedu.armando.pix.repository

import br.com.zupedu.armando.pix.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun existsByChave(chave: String): Boolean
    fun findByPixId(pixId: String): Optional<ChavePix>
    fun findByChave(chave: String): Optional<ChavePix>
    fun findByClienteId(clienteId: String): List<ChavePix>
}