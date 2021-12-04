package com.lambda.command.execute

import com.lambda.command.AbstractCommandManager
import com.lambda.command.Command
import com.lambda.command.args.AbstractArg
import com.lambda.command.args.ArgIdentifier

/**
 * Event being used for executing the [Command]
 */
interface IExecuteEvent {

    val commandManager: AbstractCommandManager<*>

    /**
     * Parsed arguments
     */
    val args: Array<String>

    /**
     * Maps argument for the [argTree]
     */
    suspend fun mapArgs(argTree: List<AbstractArg<*>>)

    /**
     * Gets mapped value for an [ArgIdentifier]
     *
     * @throws NullPointerException If this [ArgIdentifier] isn't mapped
     */
    val <T : Any> ArgIdentifier<T>.value: T

}
