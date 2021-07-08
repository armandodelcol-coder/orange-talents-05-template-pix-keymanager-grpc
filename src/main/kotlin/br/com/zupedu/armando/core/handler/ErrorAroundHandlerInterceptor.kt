package br.com.zupedu.armando.core.handler

import br.com.zupedu.armando.core.handler.exceptions.ArgumentoDeEntradaInvalidoDefaultException
import br.com.zupedu.armando.core.handler.exceptions.BadRequestErrorException
import br.com.zupedu.armando.core.handler.exceptions.ChavePixJaExisteException
import br.com.zupedu.armando.pix.grpc.NovaChavePixService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {
    private val logger = LoggerFactory.getLogger(ErrorAroundHandlerInterceptor::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (ex) {
                is ConstraintViolationException,
                is ArgumentoDeEntradaInvalidoDefaultException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)

                is ChavePixJaExisteException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription(ex.message)

                is BadRequestErrorException -> Status.INTERNAL
                    .withCause(ex)
                    .withDescription(ex.message)

                else -> {
                    logger.error(ex.stackTraceToString())
                    logger.error(ex.message)
                    Status.UNKNOWN
                        .withCause(ex)
                        .withDescription("Ops, um erro inesperado ocorreu")
                }
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}