package com.lambda.client.commons.extension

import java.util.*

fun <E : Any> MutableCollection<E>.add(e: E?) {
    if (e != null) this.add(e)
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0.0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <E> MutableCollection<E>.synchronized(): MutableCollection<E> =
    Collections.synchronizedCollection(this)

fun <E> MutableList<E>.synchronized(): MutableList<E> =
    Collections.synchronizedList(this)

fun <E> MutableSet<E>.synchronized(): MutableSet<E> =
    Collections.synchronizedSet(this)

fun <E> SortedSet<E>.synchronized(): SortedSet<E> =
    Collections.synchronizedSortedSet(this)

fun <E> NavigableSet<E>.synchronized(): NavigableSet<E> =
    Collections.synchronizedNavigableSet(this)