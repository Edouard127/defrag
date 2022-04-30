import com.lambda.client.module.modules.movement.ElytraBotModule
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import kotlin.math.abs
import kotlin.math.sqrt

object AStar {
    private var check = false

    /**
     * Generates a path to the given goal.
     * @goal The goal it will path to
     * @positions The nearby positions that can possibly by added to the open list
     * @checkPositions The positions it will check not to be solid when iterating the above list
     */
    fun generatePath(mc: Minecraft, start: BlockPos, goal: BlockPos, positions: Array<BlockPos>, checkPositions: ArrayList<BlockPos>, loopAmount: Int): ArrayList<BlockPos> {
        AStarNode.nodes.clear()
        var current = start
        var closest = current
        val open = ArrayList<BlockPos>()
        val closed = ArrayList<BlockPos>()
        var noClosest = 0
        for (i in 0 until loopAmount) {
            //Check if were in the goal
            if (current == goal) {
                check = false
                return getPath(current)
            }

            //Get the pos with lowest f cost from open list and put it to closed list
            var lowestFCost = Int.MAX_VALUE.toDouble()
            open.forEach {
                val fCost = fCost(it, goal, start)
                if (fCost < lowestFCost) {
                    lowestFCost = fCost
                    current = it
                }
            }

            //Update the lists
            closed.add(current)
            open.remove(current)
            getOpen(mc, positions, checkPositions, current, start, open, closed)?.let {
                open.addAll(it)
            }

            //Set the closest pos.
            if (lowestFCost < fCost(closest, goal, start)) {
                closest = current
                noClosest = 0
            } else {
                noClosest++

                //If there hasent been a closer pos found in x times then break
                if (noClosest > 200) {
                    break
                }
            }
        }

        //If there was no path found to the goal then return path to the closest pos.
        //As the goal is probably out of render distance.
        return if (!check) {
            check = true
            generatePath(mc, start, closest, positions, checkPositions, loopAmount)
        } else {
            check = false
            ArrayList()
        }
    }

    /**
     * Adds the nearby positions to the open list. And updates the best parent for the AStarNodes
     */
    private fun getOpen(mc: Minecraft, positions: Array<BlockPos>, checkPositions: ArrayList<BlockPos>, current: BlockPos, start: BlockPos, open: ArrayList<BlockPos>, closed: ArrayList<BlockPos>): ArrayList<BlockPos>? {
        val list = ArrayList<BlockPos>()
        val positions2 = ArrayList<BlockPos>()
        positions.forEach {
            positions2.add(current.add(it.x, it.y, it.z))
        }
        outer@ for (pos in positions2) {
            if (!mc.world.getBlockState(pos).material.isSolid && !closed.contains(pos)) {
                val checkPositions2 = ArrayList<BlockPos>()
                checkPositions.forEach {
                    checkPositions2.add(pos.add(it.x, it.y, it.z))
                }
                for (check in checkPositions2) {
                    if (ElytraBotModule.travelMode == ElytraBotModule.ElytraBotMode.Highway && !mc.world.getChunk(check).isLoaded) {
                        return null
                    }
                    if (mc.world.getBlockState(check).material.isSolid || !mc.world.getChunk(check).isLoaded) {
                        continue@outer
                    }
                    if (mc.world.getBlockState(check).block == Blocks.LAVA && ElytraBotModule.avoidLava) {
                        continue@outer
                    }

                }
                var n = AStarNode.getNodeFromBlockpos(pos)
                if (n == null) {
                    n = AStarNode(pos)
                }
                if (!open.contains(pos)) {
                    list.add(pos)
                }
                if (n.parent == null || gCost(current, start) < gCost(n.parent, start)) {
                    n.parent = current
                }
            }
        }
        return list
    }

    /**
     * Calculates the f cost between pos and goal
     */
    private fun fCost(pos: BlockPos, goal: BlockPos, start: BlockPos): Double {
        // H cost
        val dx = (goal.x - pos.x).toDouble()
        val dz = (goal.z - pos.z).toDouble()
        val h = sqrt(dx * dx + dz * dz)
        return gCost(pos, start) + h
    }

    /**
     * Calculates the G Cost
     */
    private fun gCost(pos: BlockPos?, start: BlockPos): Double {
        val dx = (start.x - pos!!.x).toDouble()
        val dy = (start.y - pos.y).toDouble()
        val dz = (start.z - pos.z).toDouble()
        return sqrt(abs(dx) + abs(dy) + abs(dz))
    }

    /**
     * Gets the path by backtracing the closed list with the AStarNode things
     */
    private fun getPath(current: BlockPos): ArrayList<BlockPos> {
        val path = ArrayList<BlockPos>()
        try {
            var n = AStarNode.getNodeFromBlockpos(current)
            if (n == null) {
                n = AStarNode.nodes[AStarNode.nodes.size - 1]
            }
            path.add(n.pos)
            while (n?.parent != null) {
                path.add(n.parent!!)
                n = AStarNode.getNodeFromBlockpos(n.parent)
            }
        } catch (e: IndexOutOfBoundsException) {
            //Ingored. The path is zero in lenght
        }
        return path
    }

    /**
     * Used for backtracing the closed list to get the actual path
     */
    class AStarNode(var pos: BlockPos) {
        var parent: BlockPos? = null

        companion object {
            var nodes = ArrayList<AStarNode>()
            fun getNodeFromBlockpos(pos: BlockPos?): AStarNode? {
                for (node in nodes) {
                    if (node.pos == pos) {
                        return node
                    }
                }
                return null
            }
        }

        init {
            nodes.add(this)
        }
    }
}