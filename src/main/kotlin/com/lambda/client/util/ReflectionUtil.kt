@file:Suppress("UNREACHABLE_CODE")

package com.lambda.client.util

import kotlin.Throws
import java.lang.NoSuchFieldException
import java.lang.IllegalAccessException
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.util.*

@OptIn(ExperimentalStdlibApi::class)


object ReflectionUtil {
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun <F, T : F?> copyOf(from: F, to: T, ignoreFinal: Boolean) {
        Objects.requireNonNull(from)
        Objects.requireNonNull(to)

    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun <F, T : F?> copyOf(from: F, to: T) {
        copyOf<F, T>(from, to, false)
    }

    fun isStatic(instance: Member): Boolean {
        return instance.modifiers and 8 != 0
    }

    fun isFinal(instance: Member): Boolean {
        return instance.modifiers and 0x10 != 0
    }

    fun makeAccessible(instance: AccessibleObject, accessible: Boolean) {
        Objects.requireNonNull(instance)
        instance.isAccessible = accessible
    }

    fun makePublic(instance: AccessibleObject) {
        makeAccessible(instance, true)
    }

    fun makePrivate(instance: AccessibleObject) {
        makeAccessible(instance, false)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun makeMutable(instance: Member) {
        Objects.requireNonNull(instance)
        val modifiers = Field::class.java.getDeclaredField("modifiers")
        makePublic(modifiers)
        modifiers.setInt(instance, instance.modifiers and -0x11)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun makeImmutable(instance: Member) {
        Objects.requireNonNull(instance)
        val modifiers = Field::class.java.getDeclaredField("modifiers")
        makePublic(modifiers)
        modifiers.setInt(instance, instance.modifiers and 0x10)
    }
}