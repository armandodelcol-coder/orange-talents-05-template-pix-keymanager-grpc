package br.com.zupedu.armando.pix.utils

import br.com.zupedu.armando.pix.enums.TipoConta
import br.com.zupedu.armando.pix.utils.BcbAccountTypeMapper
import java.util.HashMap

object BcbAccountTypeMapper {
    val bcbAccountTypeMaps: MutableMap<TipoConta, String> = hashMapOf(
        Pair(TipoConta.CONTA_CORRENTE, "CACC"),
        Pair(TipoConta.CONTA_POUPANCA, "SVGS")
    )
}