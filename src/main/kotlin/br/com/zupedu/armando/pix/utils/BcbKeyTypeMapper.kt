package br.com.zupedu.armando.pix.utils

import br.com.zupedu.armando.pix.enums.TipoChave
import br.com.zupedu.armando.pix.utils.BcbKeyTypeMapper
import java.util.HashMap

object BcbKeyTypeMapper {
    val bcbKeyTypeMaps: MutableMap<TipoChave, String> = HashMap()

    init {
        bcbKeyTypeMaps[TipoChave.CPF] = "CPF"
        bcbKeyTypeMaps[TipoChave.EMAIL] = "EMAIL"
        bcbKeyTypeMaps[TipoChave.CELULAR] = "PHONE"
        bcbKeyTypeMaps[TipoChave.RANDOMICA] = "RANDOM"
    }
}