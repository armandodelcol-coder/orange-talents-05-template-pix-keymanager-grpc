package br.com.zupedu.armando.pix.validations

import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException

class ValidaEnumTipoDesconhecido {
    companion object {
        fun tipoChave(tipoChave: TipoChave) {
            if (tipoChave.equals(TipoChave.CHAVE_DESCONHECIDA)) {
                val validos = TipoChave.values()
                    .filter { it.ordinal != 0 && it.ordinal != TipoChave.values().size - 1 }
                    .map { "${it.ordinal} - ${it.name}" }
                throw ArgumentoDeEntradaInvalidoDefaultException("TipoChave é obrigatório e valores validos são: $validos")
            }
        }

        fun tipoConta(tipoConta: TipoConta) {
            if (tipoConta.equals(TipoConta.CONTA_DESCONHECIDA)) {
                val validos = TipoConta.values()
                    .filter { it.ordinal != 0 && it.ordinal != TipoConta.values().size - 1 }
                    .map { "${it.ordinal} - ${it.name}" }
                throw ArgumentoDeEntradaInvalidoDefaultException("TipoConta é obrigatório e valores validos são: $validos")
            }
        }
    }
}
