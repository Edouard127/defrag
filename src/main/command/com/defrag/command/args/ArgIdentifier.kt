package com.defrag.command.args

import com.defrag.commons.interfaces.Nameable

/**
 * The ID for an argument
 */
@Suppress("UNUSED")
data class ArgIdentifier<T : Any>(override val name: String) : Nameable
