package com.defrag.commons.extension

import com.defrag.commons.interfaces.DisplayEnum

fun <E : Enum<E>> E.next(): E = declaringClass.enumConstants.run {
    get((ordinal + 1) % size)
}

fun Enum<*>.readableName() = (this as? DisplayEnum)?.displayName
    ?: name.mapEach('_') { low -> low.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }.joinToString(" ")