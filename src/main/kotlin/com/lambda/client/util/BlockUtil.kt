package com.lambda.client.util

import com.google.common.util.concurrent.AtomicDouble
import com.lambda.client.util.graphics.RenderUtils2D.mc
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.*
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.json.XMLTokener.entity
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs


object BlockUtil {
    val blackList = Arrays.asList<Block>(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.ANVIL,
        Blocks.BREWING_STAND,
        Blocks.HOPPER,
        Blocks.DROPPER,
        Blocks.DISPENSER,
        Blocks.TRAPDOOR,
        Blocks.ENCHANTING_TABLE
    )
    val shulkerList = Arrays.asList<Block>(
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.SILVER_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX
    )
    var unSolidBlocks = Arrays.asList<Block>(
        Blocks.FLOWING_LAVA,
        Blocks.FLOWER_POT,
        Blocks.SNOW,
        Blocks.CARPET,
        Blocks.END_ROD,
        Blocks.SKULL,
        Blocks.FLOWER_POT,
        Blocks.TRIPWIRE,
        Blocks.TRIPWIRE_HOOK,
        Blocks.WOODEN_BUTTON,
        Blocks.LEVER,
        Blocks.STONE_BUTTON,
        Blocks.LADDER,
        Blocks.UNPOWERED_COMPARATOR,
        Blocks.POWERED_COMPARATOR,
        Blocks.UNPOWERED_REPEATER,
        Blocks.POWERED_REPEATER,
        Blocks.UNLIT_REDSTONE_TORCH,
        Blocks.REDSTONE_TORCH,
        Blocks.REDSTONE_WIRE,
        Blocks.AIR,
        Blocks.PORTAL,
        Blocks.END_PORTAL,
        Blocks.WATER,
        Blocks.FLOWING_WATER,
        Blocks.LAVA,
        Blocks.FLOWING_LAVA,
        Blocks.SAPLING,
        Blocks.RED_FLOWER,
        Blocks.YELLOW_FLOWER,
        Blocks.BROWN_MUSHROOM,
        Blocks.RED_MUSHROOM,
        Blocks.WHEAT,
        Blocks.CARROTS,
        Blocks.POTATOES,
        Blocks.BEETROOTS,
        Blocks.REEDS,
        Blocks.PUMPKIN_STEM,
        Blocks.MELON_STEM,
        Blocks.WATERLILY,
        Blocks.NETHER_WART,
        Blocks.COCOA,
        Blocks.CHORUS_FLOWER,
        Blocks.CHORUS_PLANT,
        Blocks.TALLGRASS,
        Blocks.DEADBUSH,
        Blocks.VINE,
        Blocks.FIRE,
        Blocks.RAIL,
        Blocks.ACTIVATOR_RAIL,
        Blocks.DETECTOR_RAIL,
        Blocks.GOLDEN_RAIL,
        Blocks.TORCH
    )



    fun getFacing(pos: BlockPos): EnumFacing {
        for (facing in EnumFacing.values()) {
            val rayTraceResult: RayTraceResult? = mc.world.rayTraceBlocks(
                Vec3d(
                    mc.player.posX,
                    mc.player.posY + mc.player.getEyeHeight() as Double,
                    mc.player.posZ
                ),
                Vec3d(
                    pos.getX().toDouble() + 0.5 + facing.getDirectionVec().getX().toDouble() * 1.0 / 2.0,
                    pos.getY().toDouble() + 0.5 + facing.getDirectionVec().getY().toDouble() * 1.0 / 2.0,
                    pos.getZ().toDouble() + 0.5 + facing.getDirectionVec().getZ().toDouble() * 1.0 / 2.0
                ),
                false,
                true,
                false
            )
            if (rayTraceResult != null && (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK || rayTraceResult.getBlockPos() != pos as Any)) continue
            return facing
        }
        return if (pos.getY().toDouble() > mc.player.posY + mc.player.getEyeHeight() as Double) {
            EnumFacing.DOWN
        } else EnumFacing.UP
    }

    fun getPossibleSides(pos: BlockPos?): List<EnumFacing> {
        val facings: ArrayList<EnumFacing> = ArrayList<EnumFacing>()
        if (mc.world == null || pos == null) {
            return facings
        }
        for (side in EnumFacing.values()) {
            val neighbour: BlockPos = pos.offset(side)
            val blockState: IBlockState = mc.world.getBlockState(neighbour)
            if (blockState == null || !blockState.getBlock()
                    .canCollideCheck(blockState, false) || blockState.getMaterial().isReplaceable()
            ) continue
            facings.add(side)
        }
        return facings
    }

    fun getFirstFacing(pos: BlockPos?): EnumFacing? {
        val iterator: Iterator<EnumFacing> = getPossibleSides(pos).iterator()
        return if (iterator.hasNext()) {
            iterator.next()
        } else null
    }

    fun getRayTraceFacing(pos: BlockPos): EnumFacing {
        val result: RayTraceResult? = mc.world.rayTraceBlocks(
            Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight() as Double,
                mc.player.posZ
            ), Vec3d(pos.getX().toDouble() + 0.5, pos.getX().toDouble() - 0.5, pos.getX().toDouble() + 0.5)
        )
        return if (result == null || result.sideHit == null) {
            EnumFacing.UP
        } else result.sideHit
    }

    fun isPositionPlaceable(pos: BlockPos, rayTrace: Boolean): Int {
        return isPositionPlaceable(pos, rayTrace, true)
    }

    fun isPositionPlaceable(pos: BlockPos, rayTrace: Boolean, entityCheck: Boolean): Int {
        val block: Block = mc.world.getBlockState(pos).getBlock()
        if (!(block is BlockAir || block is BlockLiquid || block is BlockTallGrass || block is BlockFire || block is BlockDeadBush || block is BlockSnow)) {
            return 0
        }
        if (!rayTracePlaceCheck(pos, rayTrace, 0.0f)) {
            return -1
        }
        if (entityCheck) {
            for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(pos))) {
                if (entity is EntityItem || entity is EntityXPOrb) continue
                return 1
            }
        }
        for (side in getPossibleSides(pos)) {
            if (!canBeClicked(pos.offset(side))) continue
            return 3
        }
        return 2
    }

    fun rightClickBlock(pos: BlockPos, vec: Vec3d, hand: EnumHand?, direction: EnumFacing?, packet: Boolean) {
        if (packet) {
            val f: Float = (vec.x - pos.getX().toDouble()).toFloat()
            val f1: Float = (vec.y - pos.getY().toDouble()).toFloat()
            val f2: Float = (vec.z - pos.getZ().toDouble()).toFloat()
            mc.player.connection.sendPacket(
                CPacketPlayerTryUseItemOnBlock(
                    pos,
                    direction,
                    hand,
                    f,
                    f1,
                    f2
                ) as Packet<*>?
            )
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand)
        }
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    fun rightClickBed(
        pos: BlockPos,
        range: Float,
        rotate: Boolean,
        hand: EnumHand?,
        yaw: AtomicDouble,
        pitch: AtomicDouble,
        rotating: AtomicBoolean,
        packet: Boolean
    ) {
        val posVec: Vec3d = Vec3d(pos as Vec3i).add(0.5, 0.5, 0.5)
        val result: RayTraceResult? = mc.world.rayTraceBlocks(
            Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight() as Double,
                mc.player.posZ
            ), posVec
        )
        val face: EnumFacing = if (result == null || result.sideHit == null) EnumFacing.UP else result.sideHit
        val eyesPos: Vec3d = RotationUtil.eyesPos
        if (rotate) {
            val rotations = RotationUtil.getLegitRotations(posVec)
            yaw.set(rotations[0].toDouble())
            pitch.set(rotations[1].toDouble())
            rotating.set(true)
        }
        rightClickBlock(pos, posVec, hand, face, packet)
        mc.player.swingArm(hand)
    }

    fun rightClickBlockLegit(
        pos: BlockPos,
        range: Float,
        rotate: Boolean,
        hand: EnumHand?,
        Yaw2: AtomicDouble,
        Pitch: AtomicDouble,
        rotating: AtomicBoolean,
        packet: Boolean
    ) {
        val eyesPos: Vec3d = RotationUtil.eyesPos
        val posVec: Vec3d = Vec3d(pos as Vec3i).add(0.5, 0.5, 0.5)
        val distanceSqPosVec: Double = eyesPos.squareDistanceTo(posVec)
        for (side in EnumFacing.values()) {
            val hitVec: Vec3d = posVec.add(Vec3d(side.getDirectionVec()).scale(0.5))
            val distanceSqHitVec: Double = eyesPos.squareDistanceTo(hitVec)
            if (distanceSqHitVec > MathUtil.square(range) || distanceSqHitVec >= distanceSqPosVec || mc.world.rayTraceBlocks(
                    eyesPos,
                    hitVec,
                    false,
                    true,
                    false
                ) != null
            ) continue
            if (rotate) {
                val rotations = RotationUtil.getLegitRotations(hitVec)
                Yaw2.set(rotations[0].toDouble())
                Pitch.set(rotations[1].toDouble())
                rotating.set(true)
            }
            rightClickBlock(pos, hitVec, hand, side, packet)
            mc.player.swingArm(hand)
            break
        }
    }

    fun placeBlock(pos: BlockPos, hand: EnumHand?, rotate: Boolean, packet: Boolean, isSneaking: Boolean): Boolean {
        var sneaking = false
        val side: EnumFacing = getFirstFacing(pos) ?: return isSneaking
        val neighbour: BlockPos = pos.offset(side)
        val opposite: EnumFacing = side.getOpposite()
        val hitVec: Vec3d =
            Vec3d(neighbour as Vec3i).add(0.5, 0.5, 0.5).add(Vec3d(opposite.getDirectionVec()).scale(0.5))
        val neighbourBlock: Block = mc.world.getBlockState(neighbour).getBlock()
        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(
                CPacketEntityAction(
                    mc.player as Entity,
                    CPacketEntityAction.Action.START_SNEAKING
                ) as Packet<*>?
            )
            mc.player.setSneaking(true)
            sneaking = true
        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true)
        }
        rightClickBlock(neighbour, hitVec, hand, opposite, packet)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        return sneaking || isSneaking
    }

    fun placeBlockSmartRotate(
        pos: BlockPos,
        hand: EnumHand?,
        rotate: Boolean,
        packet: Boolean,
        isSneaking: Boolean
    ): Boolean {
        var sneaking = false
        val side: EnumFacing = getFirstFacing(pos) ?: return isSneaking
        val neighbour: BlockPos = pos.offset(side)
        val opposite: EnumFacing = side.getOpposite()
        val hitVec: Vec3d =
            Vec3d(neighbour as Vec3i).add(0.5, 0.5, 0.5).add(Vec3d(opposite.getDirectionVec()).scale(0.5))
        val neighbourBlock: Block = mc.world.getBlockState(neighbour).getBlock()
        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(
                CPacketEntityAction(
                    mc.player as Entity,
                    CPacketEntityAction.Action.START_SNEAKING
                ) as Packet<*>?
            )
            sneaking = true
        }

        rightClickBlock(neighbour, hitVec, hand, opposite, packet)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        return sneaking || isSneaking
    }

    fun placeBlockStopSneaking(pos: BlockPos, hand: EnumHand?, rotate: Boolean, packet: Boolean, isSneaking: Boolean) {
        val sneaking = placeBlockSmartRotate(pos, hand, rotate, packet, isSneaking)
        if (!isSneaking && sneaking) {
            mc.player.connection.sendPacket(
                CPacketEntityAction(
                    mc.player as Entity,
                    CPacketEntityAction.Action.STOP_SNEAKING
                ) as Packet<*>?
            )
        }
    }

    fun getHelpingBlocks(vec3d: Vec3d): Array<Vec3d> {
        return arrayOf<Vec3d>(
            Vec3d(vec3d.x, vec3d.y - 1.0, vec3d.z),
            Vec3d(
                if (vec3d.x != 0.0) vec3d.x * 2.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x != 0.0) vec3d.z else vec3d.z * 2.0
            ),
            Vec3d(
                if (vec3d.x == 0.0) vec3d.x + 1.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x == 0.0) vec3d.z else vec3d.z + 1.0
            ),
            Vec3d(
                if (vec3d.x == 0.0) vec3d.x - 1.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x == 0.0) vec3d.z else vec3d.z - 1.0
            ),
            Vec3d(vec3d.x, vec3d.y + 1.0, vec3d.z)
        )
    }



    fun getSphere(pos: BlockPos, r: Float, h: Int, hollow: Boolean, sphere: Boolean, plus_y: Int): List<BlockPos> {
        val circleblocks: ArrayList<BlockPos> = ArrayList<BlockPos>()
        val cx: Int = pos.getX()
        val cy: Int = pos.getY()
        val cz: Int = pos.getZ()
        var x = cx - r.toInt()
        while (x.toFloat() <= cx.toFloat() + r) {
            var z = cz - r.toInt()
            while (z.toFloat() <= cz.toFloat() + r) {
                var y = if (sphere) cy - r.toInt() else cy
                while (true) {
                    val f = y.toFloat()
                    val f2 = if (sphere) cy.toFloat() + r else (cy + h).toFloat()
                    if (f >= f2) break
                    val dist =
                        ((cx - x) * (cx - x) + (cz - z) * (cz - z) + if (sphere) (cy - y) * (cy - y) else 0).toDouble()
                    if (!(dist >= (r * r).toDouble() || hollow && dist < ((r - 1.0f) * (r - 1.0f)).toDouble())) {
                        val l = BlockPos(x, y + plus_y, z)
                        circleblocks.add(l)
                    }
                    ++y
                }
                ++z
            }
            ++x
        }
        return circleblocks
    }

    fun getDisc(pos: BlockPos, r: Float): List<BlockPos> {
        val circleblocks: ArrayList<BlockPos> = ArrayList<BlockPos>()
        val cx: Int = pos.getX()
        val cy: Int = pos.getY()
        val cz: Int = pos.getZ()
        var x = cx - r.toInt()
        while (x.toFloat() <= cx.toFloat() + r) {
            var z = cz - r.toInt()
            while (z.toFloat() <= cz.toFloat() + r) {
                val dist = ((cx - x) * (cx - x) + (cz - z) * (cz - z)).toDouble()
                if (dist < (r * r).toDouble()) {
                    val position = BlockPos(x, cy, z)
                    circleblocks.add(position)
                }
                ++z
            }
            ++x
        }
        return circleblocks
    }

    fun canPlaceCrystal(blockPos: BlockPos): Boolean {
        val boost: BlockPos = blockPos.add(0, 1, 0)
        val boost2: BlockPos = blockPos.add(0, 2, 0)
        return try {
            (mc.world.getBlockState(blockPos).getBlock() === Blocks.BEDROCK || mc.world.getBlockState(blockPos)
                .getBlock() === Blocks.OBSIDIAN) && mc.world.getBlockState(boost)
                .getBlock() === Blocks.AIR && mc.world.getBlockState(boost2)
                .getBlock() === Blocks.AIR && mc.world.getEntitiesWithinAABB(
                Entity::class.java, AxisAlignedBB(boost)
            ).isEmpty() && mc.world.getEntitiesWithinAABB(
                Entity::class.java, AxisAlignedBB(boost2)
            ).isEmpty()
        } catch (e: Exception) {
            false
        }
    }



    fun canPlaceCrystal(blockPos: BlockPos, specialEntityCheck: Boolean, oneDot15: Boolean): Boolean {
        val boost: BlockPos = blockPos.add(0, 1, 0)
        val boost2: BlockPos = blockPos.add(0, 2, 0)
        try {
            if (mc.world.getBlockState(blockPos).getBlock() !== Blocks.BEDROCK && mc.world.getBlockState(blockPos)
                    .getBlock() !== Blocks.OBSIDIAN
            ) {
                return false
            }
            if (!oneDot15 && mc.world.getBlockState(boost2).getBlock() !== Blocks.AIR || mc.world.getBlockState(boost)
                    .getBlock() !== Blocks.AIR
            ) {
                return false
            }
            for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(boost))) {
                if (entity.isDead || specialEntityCheck && entity is EntityEnderCrystal) continue
                return false
            }
            if (!oneDot15) {
                for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(boost2))) {
                    if (entity.isDead || specialEntityCheck && entity is EntityEnderCrystal) continue
                    return false
                }
            }
        } catch (ignored: Exception) {
            return false
        }
        return true
    }

    fun canBeClicked(pos: BlockPos): Boolean {
        return getBlock(pos).canCollideCheck(getState(pos), false)
    }

    fun getBlock(pos: BlockPos): Block {
        return getState(pos).block
    }

    private fun getState(pos: BlockPos): IBlockState {
        return mc.world.getBlockState(pos)
    }

    fun isBlockAboveEntitySolid(entity: Entity?): Boolean {
        if (entity != null) {
            val pos = BlockPos(entity.posX, entity.posY + 2.0, entity.posZ)
            return isBlockSolid(pos)
        }
        return false
    }



    fun placeCrystalOnBlock(pos: BlockPos, hand: EnumHand?, swing: Boolean, exactHand: Boolean) {
        val result: RayTraceResult? = mc.world.rayTraceBlocks(
            Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight() as Double,
                mc.player.posZ
            ), Vec3d(pos.getX().toDouble() + 0.5, pos.getY().toDouble() - 0.5, pos.getZ().toDouble() + 0.5)
        )
        val facing: EnumFacing = if (result == null || result.sideHit == null) EnumFacing.UP else result.sideHit
        mc.player.connection.sendPacket(
            CPacketPlayerTryUseItemOnBlock(
                pos,
                facing,
                hand,
                0.0f,
                0.0f,
                0.0f
            ) as Packet<*>?
        )
        if (swing) {
            mc.player.connection.sendPacket(CPacketAnimation(if (exactHand) hand else EnumHand.MAIN_HAND) as Packet<*>?)
        }
    }

    fun toBlockPos(vec3ds: Array<Vec3d?>): Array<BlockPos?> {
        val list: Array<BlockPos?> = arrayOfNulls<BlockPos>(vec3ds.size)
        for (i in vec3ds.indices) {
            list[i] = BlockPos(vec3ds[i])
        }
        return list
    }

    fun posToVec3d(pos: BlockPos?): Vec3d {
        return Vec3d(pos as Vec3i?)
    }

    fun vec3dToPos(vec3d: Vec3d?): BlockPos {
        return BlockPos(vec3d)
    }

    fun isBlockBelowEntitySolid(entity: Entity?): Boolean {
        if (entity != null) {
            val pos = BlockPos(entity.posX, entity.posY - 1.0, entity.posZ)
            return isBlockSolid(pos)
        }
        return false
    }

    fun isBlockSolid(pos: BlockPos?): Boolean {
        return !isBlockUnSolid(pos)
    }

    fun isBlockUnSolid(pos: BlockPos?): Boolean {
        return isBlockUnSolid(mc.world.getBlockState(pos).getBlock())
    }

    fun isBlockUnSolid(block: Block): Boolean {
        return unSolidBlocks.contains(block)
    }

    fun convertVec3ds(vec3d: Vec3d, input: Array<Vec3d?>): Array<Vec3d?> {
        val output: Array<Vec3d?> = arrayOfNulls<Vec3d>(input.size)
        for (i in input.indices) {
            output[i] = vec3d.add(input[i])
        }
        return output
    }

    fun convertVec3ds(entity: EntityPlayer, input: Array<Vec3d?>?): Array<Vec3d?>? {
        return input?.let { convertVec3ds(entity.getPositionVector(), it) }
    }

    fun canBreak(pos: BlockPos?): Boolean {
        val blockState: IBlockState = mc.world.getBlockState(pos)
        val block: Block = blockState.getBlock()
        return block.getBlockHardness(blockState, mc.world as World, pos) != -1.0f
    }

    fun isValidBlock(pos: BlockPos?): Boolean {
        val block: Block = mc.world.getBlockState(pos).getBlock()
        return block !is BlockLiquid && block.getMaterial(null) !== Material.AIR
    }

    fun isScaffoldPos(pos: BlockPos?): Boolean {
        return mc.world.isAirBlock(pos) || mc.world.getBlockState(pos)
            .getBlock() === Blocks.SNOW_LAYER || mc.world.getBlockState(pos)
            .getBlock() === Blocks.TALLGRASS || mc.world.getBlockState(pos).getBlock() is BlockLiquid
    }

    fun rayTracePlaceCheck(pos: BlockPos, shouldCheck: Boolean, height: Float): Boolean {
        return !shouldCheck || mc.world.rayTraceBlocks(
            Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight() as Double,
                mc.player.posZ
            ),
            Vec3d(pos.getX().toDouble(), (pos.getY().toFloat() + height).toDouble(), pos.getZ().toDouble()),
            false,
            true,
            false
        ) == null
    }

    fun rayTracePlaceCheck(pos: BlockPos, shouldCheck: Boolean): Boolean {
        return rayTracePlaceCheck(pos, shouldCheck, 1.0f)
    }

    fun rayTracePlaceCheck(pos: BlockPos): Boolean {
        return rayTracePlaceCheck(pos, true)
    }

    val isInHole: Boolean
        get() {
            val blockPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
            val blockState: IBlockState = mc.world.getBlockState(blockPos)
            return isBlockValid(blockState, blockPos)
        }
    val nearestBlockBelow: Double
        get() {
            var y: Double = mc.player.posY
            while (y > 0.0) {
                if (mc.world.getBlockState(BlockPos(mc.player.posX, y, mc.player.posZ))
                        .getBlock() is BlockSlab || mc.world.getBlockState(BlockPos(mc.player.posX, y, mc.player.posZ))
                        .getBlock().getDefaultState()
                        .getCollisionBoundingBox(mc.world as IBlockAccess, BlockPos(0, 0, 0)) == null
                ) {
                    y -= 0.001
                    continue
                }
                return y
                y -= 0.001
            }
            return -1.0
        }

    fun isBlockValid(blockState: IBlockState, blockPos: BlockPos): Boolean {
        if (blockState.getBlock() !== Blocks.AIR) {
            return false
        }
        if (mc.player.getDistanceSq(blockPos) < 1.0) {
            return false
        }
        if (mc.world.getBlockState(blockPos.up()).getBlock() !== Blocks.AIR) {
            return false
        }
        return if (mc.world.getBlockState(blockPos.up(2)).getBlock() !== Blocks.AIR) {
            false
        } else isBedrockHole(blockPos) || isObbyHole(blockPos) || isBothHole(
            blockPos
        ) || isElseHole(blockPos)
    }

    fun isObbyHole(blockPos: BlockPos): Boolean {
        for (pos in getTouchingBlocks(blockPos)) {
            val touchingState: IBlockState = mc.world.getBlockState(pos)
            if (touchingState.getBlock() !== Blocks.AIR && touchingState.getBlock() === Blocks.OBSIDIAN) continue
            return false
        }
        return true
    }

    fun isBedrockHole(blockPos: BlockPos): Boolean {
        for (pos in getTouchingBlocks(blockPos)) {
            val touchingState: IBlockState = mc.world.getBlockState(pos)
            if (touchingState.getBlock() !== Blocks.AIR && touchingState.getBlock() === Blocks.BEDROCK) continue
            return false
        }
        return true
    }

    fun isBothHole(blockPos: BlockPos): Boolean {
        for (pos in getTouchingBlocks(blockPos)) {
            val touchingState: IBlockState = mc.world.getBlockState(pos)
            if (touchingState.getBlock() !== Blocks.AIR && (touchingState.getBlock() === Blocks.BEDROCK || touchingState.getBlock() === Blocks.OBSIDIAN)) continue
            return false
        }
        return true
    }

    fun isElseHole(blockPos: BlockPos): Boolean {
        for (pos in getTouchingBlocks(blockPos)) {
            val touchingState: IBlockState = mc.world.getBlockState(pos)
            if (touchingState.getBlock() !== Blocks.AIR && touchingState.isFullBlock()) continue
            return false
        }
        return true
    }

    fun getTouchingBlocks(blockPos: BlockPos): Array<BlockPos> {
        return arrayOf<BlockPos>(blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down())
    }

    fun possiblePlacePositions(any: Any): Any {
        return arrayOf<Any>()

    }
    fun distance(first: BlockPos, second: BlockPos): Double {
        val deltaX: Double = (first.x - second.x).toDouble()
        val deltaY: Double = (first.y - second.y).toDouble()
        val deltaZ: Double = (first.z - second.z).toDouble()
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
    }
    fun isInRenderDistance(pos: BlockPos?): Boolean {
        return mc.world.getChunk(pos!!).isLoaded
    }
    fun isSolid(pos: BlockPos?): Boolean {
        return try {
            mc.world.getBlockState(pos).getMaterial().isSolid()
        } catch (e: NullPointerException) {
            false
        }
    }
}

