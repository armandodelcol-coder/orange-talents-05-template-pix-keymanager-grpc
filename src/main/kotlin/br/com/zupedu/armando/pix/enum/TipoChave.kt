package br.com.zupedu.armando.pix.enum

import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?) {
            if (chave.isNullOrBlank() || !CPFValidator().run {
                    initialize(null)
                    isValid(chave, null)
                }) {
                throw ArgumentoDeEntradaInvalidoDefaultException("chave é obrigatória e formato esperado deve ser um CPF válido.")
            }
        }
    },
    CELULAR {
        override fun valida(chave: String?) {
            if (chave.isNullOrBlank() || !chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                throw ArgumentoDeEntradaInvalidoDefaultException("chave é obrigatória e formato esperado deve ser +5585988714077")
            }
        }
    },
    EMAIL {
        override fun valida(chave: String?) {
            if (chave.isNullOrBlank() || !chave.matches("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".toRegex())) {
                throw ArgumentoDeEntradaInvalidoDefaultException("chave é obrigatória e formato esperado deve ser seu@email.com")
            }
        }
    },
    RANDOMICA {
        override fun valida(chave: String?) {
            if (!chave.isNullOrBlank()) throw ArgumentoDeEntradaInvalidoDefaultException("Para tipo chave RANDOMICA não deve ser informada uma chave.")
        }
    };

    abstract fun valida(chave: String?)
}
