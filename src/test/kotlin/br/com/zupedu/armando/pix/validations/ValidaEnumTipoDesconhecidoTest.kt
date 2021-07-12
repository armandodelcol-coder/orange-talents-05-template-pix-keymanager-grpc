package br.com.zupedu.armando.pix.validations

import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ValidaEnumTipoDesconhecidoTest {
    @Test
    fun `deve disparar uma exception quando TipoChave for desconhecida`() {
        // cenario
        val tiposValidos = TipoChave.values()
            .filter { it.ordinal != 0 && it.ordinal != TipoChave.values().size - 1 }
            .map { "${it.ordinal} - ${it.name}" }

        // acao
        val validaTipoChave = assertThrows<RuntimeException> {
            ValidaEnumTipoDesconhecido.tipoChave(TipoChave.CHAVE_DESCONHECIDA)
        }

        // validacao
        with (validaTipoChave) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("TipoChave é obrigatório e valores validos são: $tiposValidos", message)
        }
    }

    @Test
    fun `não deve disparar exception quando TipoChave for diferente de desconhecida`() {
        val acao = ValidaEnumTipoDesconhecido.tipoChave(TipoChave.EMAIL)

        assertEquals(Unit, acao)
    }

    @Test
    fun `deve disparar uma exception quando TipoConta for desconhecida`() {
        // cenario
        val tiposValidos = TipoConta.values()
            .filter { it.ordinal != 0 && it.ordinal != TipoConta.values().size - 1 }
            .map { "${it.ordinal} - ${it.name}" }

        // acao
        val validaTipoChave = assertThrows<RuntimeException> {
            ValidaEnumTipoDesconhecido.tipoConta(TipoConta.CONTA_DESCONHECIDA)
        }

        // validacao
        with (validaTipoChave) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("TipoConta é obrigatório e valores validos são: $tiposValidos", message)
        }
    }

    @Test
    fun `não deve disparar exception quando TipoConta for diferente de desconhecida`() {
        val acao = ValidaEnumTipoDesconhecido.tipoConta(TipoConta.CONTA_CORRENTE)

        assertEquals(Unit, acao)
    }
}