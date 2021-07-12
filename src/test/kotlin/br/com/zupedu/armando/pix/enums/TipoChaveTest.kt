package br.com.zupedu.armando.pix.enums

import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TipoChaveTest {
    // CPF
    @Test
    fun `deve disparar uma exception quando tipo chave for CPF e a chave não for preenchida`() {
        // acao
        val validacaoCpf = assertThrows<RuntimeException> {
            TipoChave.CPF.valida("")
        }
        // validacao
        with (validacaoCpf) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser um CPF válido.", message)
        }
    }

    @Test
    fun `deve disparar uma exception quando tipo chave for CPF e a chave for cpf inválido`() {
        // acao
        val validacaoCpf = assertThrows<RuntimeException> {
            TipoChave.CPF.valida("4220196998?")
        }
        // validacao
        with (validacaoCpf) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser um CPF válido.", message)
        }
    }
    // CELULAR
    @Test
    fun `deve disparar uma exception quando tipo chave for CELULAR e a chave não for preenchida`() {
        // acao
        val validacaoCelular = assertThrows<RuntimeException> {
            TipoChave.CELULAR.valida("")
        }
        // validacao
        with (validacaoCelular) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser +5585988714077", message)
        }
    }

    @Test
    fun `deve disparar uma exception quando tipo chave for CELULAR e a chave for celular inválido`() {
        // acao
        val validacaoCelular = assertThrows<RuntimeException> {
            TipoChave.CELULAR.valida("40337896")
        }
        // validacao
        with (validacaoCelular) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser +5585988714077", message)
        }
    }
    // EMAIL
    @Test
    fun `deve disparar uma exception quando tipo chave for EMAIL e a chave não for preenchida`() {
        // acao
        val validacaoEmail = assertThrows<RuntimeException> {
            TipoChave.EMAIL.valida("")
        }
        // validacao
        with (validacaoEmail) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser seu@email.com", message)
        }
    }

    @Test
    fun `deve disparar uma exception quando tipo chave for EMAIL e a chave for email inválido`() {
        // acao
        val validacaoEmail = assertThrows<RuntimeException> {
            TipoChave.EMAIL.valida("123@2")
        }
        // validacao
        with (validacaoEmail) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("chave é obrigatória e formato esperado deve ser seu@email.com", message)
        }
    }
    // RANDOMICA
    @Test
    fun `deve disparar uma exception quando tipo chave for RANDOMICA e a chave for preenchida`() {
        // acao
        val validacaoRandomica = assertThrows<RuntimeException> {
            TipoChave.RANDOMICA.valida("123")
        }
        // validacao
        with (validacaoRandomica) {
            assertEquals(ArgumentoDeEntradaInvalidoDefaultException::class.java, javaClass)
            assertEquals("Para tipo chave RANDOMICA não deve ser informada uma chave.", message)
        }
    }
}