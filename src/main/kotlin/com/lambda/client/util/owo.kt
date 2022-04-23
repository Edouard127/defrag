package com.lambda.client.util

import com.google.common.base.Predicate
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.EntitySelectors
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.chunk.IChunkProvider
import java.util.*

public object owo {
    var mcObjectMouseOver: RayTraceResult? = null
    private var range: Double = 0.0
    private var requestingEntity: Entity? = null
    private var partialTicks: Float? = null
    var airTargeted = false
    var blockSideHit: EnumFacing? = null
    private val chunkProvider: IChunkProvider? = null// if * AND reach is greater than 3 blocks AND// if the entity found is what the requesting entity is riding// distance between eye position and range location// run when entity is targeted...// if entity is intersected, set entity as intersected// vector between eye position and range location// d1 is either range or distance between ray trace and eye position// add range multiplied by where entity is looking// get vector from angle of look// distance between ray trace and eye position// if range is larger than 3 blocks

    // block reach distance. default 5.0D, max 1024.0D;
    val mouseOver: Unit = Unit
    fun get(partialTicks: Float?, range: Double, mc: Minecraft) {
            if (requestingEntity != null) {
                if (mc.world != null) {
                    mc.profiler.startSection("pick")
                    var pointedEntity: Entity? = null
                    val d0 = range // block reach distance. default 5.0D, max 1024.0D;
                    mcObjectMouseOver = requestingEntity!!.rayTrace(d0, partialTicks!!)
                    val blockPos = mcObjectMouseOver!!.blockPos
                    val state = mc.world.getBlockState(blockPos)
                    blockSideHit = mcObjectMouseOver!!.sideHit
                    val vec3d = requestingEntity!!.getPositionEyes(partialTicks!!)
                    var flag = false
                    val i = 3
                    var d1 = d0
                    if (d0 > 3.0) // if range is larger than 3 blocks
                    {
                        flag = true
                    }
                    if (mcObjectMouseOver != null) {
                        d1 = mcObjectMouseOver!!.hitVec.distanceTo(vec3d) // distance between ray trace and eye position
                    }
                    val vec3d1 = requestingEntity!!.getLook(1.0f) // get vector from angle of look
                    val vec3d2 = vec3d.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0) // add range multiplied by where entity is looking
                    var vec3d3: Vec3d? = null
                    val f = 1.0f
                    val list = mc.world.getEntitiesInAABBexcluding(requestingEntity, requestingEntity!!.collisionBoundingBox!!.expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0, 1.0, 1.0), EntitySelectors.NOT_SPECTATING.and { obj: Entity -> obj.canBeCollidedWith() } as Predicate<in Entity?>)
                    var d2 = d1 // d1 is either range or distance between ray trace and eye position
                    for (entity1 in list) {
                        val axisalignedbb = Objects.requireNonNull(entity1.collisionBoundingBox)?.grow(entity1.collisionBorderSize.toDouble())
                        val raytraceresult = axisalignedbb?.calculateIntercept(vec3d, vec3d2) // vector between eye position and range location
                        if (axisalignedbb != null) {
                            if (axisalignedbb.contains(vec3d)) // if entity is intersected, set entity as intersected
                            {
                                if (d2 >= 0.0) {
                                    pointedEntity = entity1
                                    airTargeted = false
                                    vec3d3 = if (raytraceresult == null) vec3d else raytraceresult.hitVec
                                    d2 = 0.0
                                }
                            } else if (raytraceresult != null) // run when entity is targeted...
                            {
                                val d3 = vec3d.distanceTo(raytraceresult.hitVec) // distance between eye position and range location
                                if (d3 < d2 || d2 == 0.0) {
                                    if (entity1.lowestRidingEntity === requestingEntity!!.lowestRidingEntity && !entity1.canRiderInteract()) // if the entity found is what the requesting entity is riding
                                    {
                                        if (d2 == 0.0) {
                                            pointedEntity = entity1
                                            airTargeted = false
                                            vec3d3 = raytraceresult.hitVec
                                        }
                                    } else {
                                        pointedEntity = entity1
                                        airTargeted = false
                                        vec3d3 = raytraceresult.hitVec
                                        d2 = d3
                                    }
                                }
                            }
                        }
                    }
                    if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0) // if * AND reach is greater than 3 blocks AND
                    {
                        mcObjectMouseOver = RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null as EnumFacing?, BlockPos(vec3d3))
                    }
                    if (pointedEntity != null && (d2 < d1 || mcObjectMouseOver == null)) {
                        mcObjectMouseOver = RayTraceResult(pointedEntity, vec3d3)
                    }
                    mc.profiler.endSection()
                }
            }
        }

}