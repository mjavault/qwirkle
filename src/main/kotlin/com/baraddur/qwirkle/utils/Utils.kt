package com.baraddur.qwirkle.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import kotlin.math.pow
import kotlin.random.Random

fun objectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerModule(ParameterNamesModule())
    .registerModule(Jdk8Module())
    .registerModule(KotlinModule())
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

fun Int.pow(n: Int): Double = toDouble().pow(n.toDouble())

fun <T> MutableList<T>.pollRandom(): T {
    val i = Random.nextInt(size)
    val item = get(i)
    removeAt(i)
    return item
}
