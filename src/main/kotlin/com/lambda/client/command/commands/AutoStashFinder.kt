package com.lambda.client.command.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.module.modules.movement.AutoStashFinder
import com.lambda.client.module.modules.movement.ElytraBotModule
import com.lambda.client.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos

object AutoStashFinder : ClientCommand(
    name = "autostashfinder",
    description = "Commands for autostashfinder"
) {
    init {
        literal("goto") {

            int("x/y") { xArg ->
                executeSafe("Set goal to a Y level.") {
                    MessageSendHelper.sendChatMessage("You need at specify x and z pos")
                }

                int("y/z") { yArg ->
                    executeSafe("Set goal to X Z.") {
                        AutoStashFinder.zone = BlockPos(xArg.value, 70, yArg.value)
                        AutoStashFinder.enable()

                    }


                }
            }
        }
    }
}