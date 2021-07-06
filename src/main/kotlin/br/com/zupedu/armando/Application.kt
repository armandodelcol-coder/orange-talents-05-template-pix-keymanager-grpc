package br.com.zupedu.armando

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupedu.armando")
		.start()
}

