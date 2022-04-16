package io.github.springroll.test.annotation

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.TYPE, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@interface NoTx {

}
