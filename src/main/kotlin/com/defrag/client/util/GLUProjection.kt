package com.defrag.client.util

import java.nio.IntBuffer
import java.nio.FloatBuffer
import org.lwjgl.BufferUtils
import com.defrag.client.util.GLUProjection.ClampMode
import org.lwjgl.util.glu.GLU
import com.defrag.client.util.GLUProjection
import org.lwjgl.util.vector.Matrix4f

class GLUProjection private constructor() {
    private var viewport: IntBuffer? = null
    private var modelview: FloatBuffer? = null
    private var projection: FloatBuffer? = null
    private val coords = BufferUtils.createFloatBuffer(3)
    private var frustumPos: Vector3D? = null
    lateinit var frustum: Array<Vector3D>
        private set
    private lateinit var invFrustum: Array<Vector3D>
    private var viewVec: Vector3D? = null
    private var displayWidth = 0.0
    private var displayHeight = 0.0
    private var widthScale = 0.0
    private var heightScale = 0.0
    private var bra = 0.0
    private var bla = 0.0
    private var tra = 0.0
    private var tla = 0.0
    private var tb: Line? = null
    private var bb: Line? = null
    private var lb: Line? = null
    private var rb: Line? = null
    var fovY = 0f
        private set
    var fovX = 0f
        private set
    var lookVector: Vector3D? = null
        private set

    fun updateMatrices(
        viewport: IntBuffer?,
        modelview: FloatBuffer?,
        projection: FloatBuffer?,
        widthScale: Double,
        heightScale: Double
    ) {
        val fov: Float
        this.viewport = viewport
        this.modelview = modelview
        this.projection = projection
        this.widthScale = widthScale
        this.heightScale = heightScale
        fov = Math.toDegrees(Math.atan(1.0 / this.projection!![5].toDouble()) * 2.0).toFloat()
        fovY = fov
        displayWidth = this.viewport!![2].toDouble()
        displayHeight = this.viewport!![3].toDouble()
        fovX = Math.toDegrees(
            2.0 * Math.atan(
                displayWidth / displayHeight * Math.tan(
                    Math.toRadians(
                        fovY.toDouble()
                    ) / 2.0
                )
            )
        ).toFloat()
        val lv = Vector3D(
            this.modelview!![0].toDouble(), this.modelview!![1].toDouble(), this.modelview!![2].toDouble()
        )
        val uv = Vector3D(
            this.modelview!![4].toDouble(), this.modelview!![5].toDouble(), this.modelview!![6].toDouble()
        )
        val fv = Vector3D(
            this.modelview!![8].toDouble(), this.modelview!![9].toDouble(), this.modelview!![10].toDouble()
        )
        val nuv = Vector3D(0.0, 1.0, 0.0)
        val nlv = Vector3D(1.0, 0.0, 0.0)
        var yaw = Math.toDegrees(Math.atan2(nlv.cross(lv).length(), nlv.dot(lv))) + 180.0
        if (fv.x < 0.0) {
            yaw = 360.0 - yaw
        }
        var pitch = 0.0
        pitch =
            if (-fv.y > 0.0 && yaw >= 90.0 && yaw < 270.0 || fv.y > 0.0 && (yaw < 90.0 || yaw >= 270.0)) Math.toDegrees(
                Math.atan2(nuv.cross(uv).length(), nuv.dot(uv))
            ) else -Math.toDegrees(Math.atan2(nuv.cross(uv).length(), nuv.dot(uv)))
        lookVector = getRotationVector(yaw, pitch)
        val modelviewMatrix = Matrix4f()
        modelviewMatrix.load(this.modelview!!.asReadOnlyBuffer())
        modelviewMatrix.invert()
        frustumPos =
            Vector3D(modelviewMatrix.m30.toDouble(), modelviewMatrix.m31.toDouble(), modelviewMatrix.m32.toDouble())
        frustum = getFrustum(
            frustumPos!!.x,
            frustumPos!!.y,
            frustumPos!!.z,
            yaw,
            pitch,
            fov.toDouble(),
            1.0,
            displayWidth / displayHeight
        )
        invFrustum = getFrustum(
            frustumPos!!.x,
            frustumPos!!.y,
            frustumPos!!.z,
            yaw - 180.0,
            -pitch,
            fov.toDouble(),
            1.0,
            displayWidth / displayHeight
        )
        viewVec = getRotationVector(yaw, pitch).normalized()
        bra = Math.toDegrees(
            Math.acos(
                displayHeight * heightScale / Math.sqrt(
                    displayWidth * widthScale * displayWidth * widthScale + displayHeight * heightScale * displayHeight * heightScale
                )
            )
        )
        bla = 360.0 - bra
        tra = bla - 180.0
        tla = bra + 180.0
        rb = Line(displayWidth * this.widthScale, 0.0, 0.0, 0.0, 1.0, 0.0)
        tb = Line(0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
        lb = Line(0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
        bb = Line(0.0, displayHeight * this.heightScale, 0.0, 1.0, 0.0, 0.0)
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    fun project(x: Double, y: Double, z: Double, clampModeOutside: ClampMode, extrudeInverted: Boolean): Projection {
        val outsideFrustum: Boolean
        if (viewport == null || modelview == null || projection == null) return Projection(
            0.0,
            0.0,
            Projection.Type.FAIL
        )
        val posVec = Vector3D(x, y, z)
        val frustum = doFrustumCheck(frustum, frustumPos, x, y, z)
        outsideFrustum = frustum[0] || frustum[1] || frustum[2] || frustum[3]
        val bl = outsideFrustum
        if (outsideFrustum) {
            val outsideInvertedFrustum: Boolean
            val opposite = posVec.sub(frustumPos).dot(viewVec) <= 0.0
            val invFrustum = doFrustumCheck(invFrustum, frustumPos, x, y, z)
            outsideInvertedFrustum = invFrustum[0] || invFrustum[1] || invFrustum[2] || invFrustum[3]
            val bl2 = outsideInvertedFrustum
            if (extrudeInverted && !outsideInvertedFrustum || outsideInvertedFrustum && clampModeOutside != ClampMode.NONE) {
                if (extrudeInverted && !outsideInvertedFrustum || clampModeOutside == ClampMode.DIRECT && outsideInvertedFrustum) {
                    var vecX = 0.0
                    var vecY = 0.0
                    if (!GLU.gluProject(
                            x.toFloat(),
                            y.toFloat(),
                            z.toFloat(),
                            modelview,
                            projection,
                            viewport,
                            coords as FloatBuffer
                        )
                    ) return Projection(0.0, 0.0, Projection.Type.FAIL)
                    if (opposite) {
                        vecX =
                            displayWidth * widthScale - coords[0].toDouble() * widthScale - displayWidth * widthScale / 2.0
                        vecY =
                            displayHeight * heightScale - (displayHeight - coords[1].toDouble()) * heightScale - displayHeight * heightScale / 2.0
                    } else {
                        vecX = coords[0].toDouble() * widthScale - displayWidth * widthScale / 2.0
                        vecY = (displayHeight - coords[1].toDouble()) * heightScale - displayHeight * heightScale / 2.0
                    }
                    val vec = Vector3D(vecX, vecY, 0.0).snormalize()
                    vecX = vec.x
                    vecY = vec.y
                    val vectorLine =
                        Line(displayWidth * widthScale / 2.0, displayHeight * heightScale / 2.0, 0.0, vecX, vecY, 0.0)
                    var angle = Math.toDegrees(Math.acos(vec.y / Math.sqrt(vec.x * vec.x + vec.y * vec.y)))
                    if (vecX < 0.0) {
                        angle = 360.0 - angle
                    }
                    var intersect: Vector3D? = Vector3D(0.0, 0.0, 0.0)
                    intersect =
                        if (angle >= bra && angle < tra) rb!!.intersect(vectorLine) else if (angle >= tra && angle < tla) tb!!.intersect(
                            vectorLine
                        ) else if (angle >= tla && angle < bla) lb!!.intersect(vectorLine) else bb!!.intersect(
                            vectorLine
                        )
                    return Projection(
                        intersect!!.x,
                        intersect.y,
                        if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
                    )
                }
                if (clampModeOutside != ClampMode.ORTHOGONAL || !outsideInvertedFrustum) return Projection(
                    0.0,
                    0.0,
                    Projection.Type.FAIL
                )
                if (!GLU.gluProject(
                        x.toFloat(),
                        y.toFloat(),
                        z.toFloat(),
                        modelview,
                        projection,
                        viewport,
                        coords as FloatBuffer
                    )
                ) return Projection(0.0, 0.0, Projection.Type.FAIL)
                var guiX = coords[0].toDouble() * widthScale
                var guiY = (displayHeight - coords[1].toDouble()) * heightScale
                if (opposite) {
                    guiX = displayWidth * widthScale - guiX
                    guiY = displayHeight * heightScale - guiY
                }
                if (guiX < 0.0) {
                    guiX = 0.0
                } else if (guiX > displayWidth * widthScale) {
                    guiX = displayWidth * widthScale
                }
                if (guiY < 0.0) {
                    guiY = 0.0
                    return Projection(
                        guiX,
                        guiY,
                        if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
                    )
                } else {
                    if (guiY <= displayHeight * heightScale) return Projection(
                        guiX,
                        guiY,
                        if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
                    )
                    guiY = displayHeight * heightScale
                }
                return Projection(
                    guiX,
                    guiY,
                    if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
                )
            }
            if (!GLU.gluProject(
                    x.toFloat(),
                    y.toFloat(),
                    z.toFloat(),
                    modelview,
                    projection,
                    viewport,
                    coords as FloatBuffer
                )
            ) return Projection(0.0, 0.0, Projection.Type.FAIL)
            var guiX = coords[0].toDouble() * widthScale
            var guiY = (displayHeight - coords[1].toDouble()) * heightScale
            if (!opposite) return Projection(
                guiX,
                guiY,
                if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
            )
            guiX = displayWidth * widthScale - guiX
            guiY = displayHeight * heightScale - guiY
            return Projection(
                guiX,
                guiY,
                if (outsideInvertedFrustum) Projection.Type.OUTSIDE else Projection.Type.INVERTED
            )
        }
        if (!GLU.gluProject(
                x.toFloat(),
                y.toFloat(),
                z.toFloat(),
                modelview,
                projection,
                viewport,
                coords as FloatBuffer
            )
        ) return Projection(0.0, 0.0, Projection.Type.FAIL)
        val guiX = coords[0].toDouble() * widthScale
        val guiY = (displayHeight - coords[1].toDouble()) * heightScale
        return Projection(guiX, guiY, Projection.Type.INSIDE)
    }

    fun doFrustumCheck(
        frustumCorners: Array<Vector3D>,
        frustumPos: Vector3D?,
        x: Double,
        y: Double,
        z: Double
    ): BooleanArray {
        val point = Vector3D(x, y, z)
        val c1 = crossPlane(arrayOf(frustumPos, frustumCorners[3], frustumCorners[0]), point)
        val c2 = crossPlane(arrayOf(frustumPos, frustumCorners[0], frustumCorners[1]), point)
        val c3 = crossPlane(arrayOf(frustumPos, frustumCorners[1], frustumCorners[2]), point)
        val c4 = crossPlane(arrayOf(frustumPos, frustumCorners[2], frustumCorners[3]), point)
        return booleanArrayOf(c1, c2, c3, c4)
    }

    fun crossPlane(plane: Array<Vector3D?>, point: Vector3D?): Boolean {
        val z = Vector3D(0.0, 0.0, 0.0)
        val e0 = plane[1]!!.sub(plane[0])
        val e1 = plane[2]!!.sub(plane[0])
        val normal = e0.cross(e1).snormalize()
        val D = z.sub(normal).dot(plane[2])
        val dist = normal.dot(point) + D
        return dist >= 0.0
    }

    fun getFrustum(
        x: Double,
        y: Double,
        z: Double,
        rotationYaw: Double,
        rotationPitch: Double,
        fov: Double,
        farDistance: Double,
        aspectRatio: Double
    ): Array<Vector3D> {
        val hFar = 2.0 * Math.tan(Math.toRadians(fov / 2.0)) * farDistance
        val wFar = hFar * aspectRatio
        val view = getRotationVector(rotationYaw, rotationPitch).snormalize()
        val up = getRotationVector(rotationYaw, rotationPitch - 90.0).snormalize()
        val right = getRotationVector(rotationYaw + 90.0, 0.0).snormalize()
        val camPos = Vector3D(x, y, z)
        val view_camPos_product = view.add(camPos)
        val fc = Vector3D(
            view_camPos_product.x * farDistance,
            view_camPos_product.y * farDistance,
            view_camPos_product.z * farDistance
        )
        val topLeftfrustum = Vector3D(
            fc.x + up.x * hFar / 2.0 - right.x * wFar / 2.0,
            fc.y + up.y * hFar / 2.0 - right.y * wFar / 2.0,
            fc.z + up.z * hFar / 2.0 - right.z * wFar / 2.0
        )
        val downLeftfrustum = Vector3D(
            fc.x - up.x * hFar / 2.0 - right.x * wFar / 2.0,
            fc.y - up.y * hFar / 2.0 - right.y * wFar / 2.0,
            fc.z - up.z * hFar / 2.0 - right.z * wFar / 2.0
        )
        val topRightfrustum = Vector3D(
            fc.x + up.x * hFar / 2.0 + right.x * wFar / 2.0,
            fc.y + up.y * hFar / 2.0 + right.y * wFar / 2.0,
            fc.z + up.z * hFar / 2.0 + right.z * wFar / 2.0
        )
        val downRightfrustum = Vector3D(
            fc.x - up.x * hFar / 2.0 + right.x * wFar / 2.0,
            fc.y - up.y * hFar / 2.0 + right.y * wFar / 2.0,
            fc.z - up.z * hFar / 2.0 + right.z * wFar / 2.0
        )
        return arrayOf(topLeftfrustum, downLeftfrustum, downRightfrustum, topRightfrustum)
    }

    fun getRotationVector(rotYaw: Double, rotPitch: Double): Vector3D {
        val c = Math.cos(-rotYaw * 0.01745329238474369 - Math.PI)
        val s = Math.sin(-rotYaw * 0.01745329238474369 - Math.PI)
        val nc = -Math.cos(-rotPitch * 0.01745329238474369)
        val ns = Math.sin(-rotPitch * 0.01745329238474369)
        return Vector3D(s * nc, ns, c * nc)
    }

    enum class ClampMode {
        ORTHOGONAL, DIRECT, NONE
    }

    class Projection(val x: Double, val y: Double, val type: Type) {
        fun isType(type: Type): Boolean {
            return this.type == type
        }

        enum class Type {
            INSIDE, OUTSIDE, INVERTED, FAIL
        }
    }

    class Vector3D(var x: Double, var y: Double, var z: Double) {
        fun add(v: Vector3D): Vector3D {
            return Vector3D(x + v.x, y + v.y, z + v.z)
        }

        fun add(x: Double, y: Double, z: Double): Vector3D {
            return Vector3D(this.x + x, this.y + y, this.z + z)
        }

        fun sub(v: Vector3D?): Vector3D {
            return Vector3D(x - v!!.x, y - v.y, z - v.z)
        }

        fun sub(x: Double, y: Double, z: Double): Vector3D {
            return Vector3D(this.x - x, this.y - y, this.z - z)
        }

        fun normalized(): Vector3D {
            val len = Math.sqrt(x * x + y * y + z * z)
            return Vector3D(x / len, y / len, z / len)
        }

        fun dot(v: Vector3D?): Double {
            return x * v!!.x + y * v.y + z * v.z
        }

        fun cross(v: Vector3D): Vector3D {
            return Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
        }

        fun mul(m: Double): Vector3D {
            return Vector3D(x * m, y * m, z * m)
        }

        operator fun div(d: Double): Vector3D {
            return Vector3D(x / d, y / d, z / d)
        }

        fun length(): Double {
            return Math.sqrt(x * x + y * y + z * z)
        }

        fun sadd(v: Vector3D): Vector3D {
            x += v.x
            y += v.y
            z += v.z
            return this
        }

        fun sadd(x: Double, y: Double, z: Double): Vector3D {
            this.x += x
            this.y += y
            this.z += z
            return this
        }

        fun ssub(v: Vector3D): Vector3D {
            x -= v.x
            y -= v.y
            z -= v.z
            return this
        }

        fun ssub(x: Double, y: Double, z: Double): Vector3D {
            this.x -= x
            this.y -= y
            this.z -= z
            return this
        }

        fun snormalize(): Vector3D {
            val len = Math.sqrt(x * x + y * y + z * z)
            x /= len
            y /= len
            z /= len
            return this
        }

        fun scross(v: Vector3D): Vector3D {
            x = y * v.z - z * v.y
            y = z * v.x - x * v.z
            z = x * v.y - y * v.x
            return this
        }

        fun smul(m: Double): Vector3D {
            x *= m
            y *= m
            z *= m
            return this
        }

        fun sdiv(d: Double): Vector3D {
            x /= d
            y /= d
            z /= d
            return this
        }

        override fun toString(): String {
            return "(X: " + x + " Y: " + y + " Z: " + z + ")"
        }
    }

    class Line(sx: Double, sy: Double, sz: Double, dx: Double, dy: Double, dz: Double) {
        var sourcePoint = Vector3D(0.0, 0.0, 0.0)
        var direction = Vector3D(0.0, 0.0, 0.0)
        fun intersect(line: Line): Vector3D? {
            val a = sourcePoint.x
            val b = direction.x
            val c = line.sourcePoint.x
            val d = line.direction.x
            val e = sourcePoint.y
            val f = direction.y
            val g = line.sourcePoint.y
            val h = line.direction.y
            val te = -(a * h - c * h - d * (e - g))
            val be = b * h - d * f
            if (be == 0.0) {
                return intersectXZ(line)
            }
            val t = te / be
            val result = Vector3D(0.0, 0.0, 0.0)
            result.x = sourcePoint.x + direction.x * t
            result.y = sourcePoint.y + direction.y * t
            result.z = sourcePoint.z + direction.z * t
            return result
        }

        private fun intersectXZ(line: Line): Vector3D? {
            val a = sourcePoint.x
            val b = direction.x
            val c = line.sourcePoint.x
            val d = line.direction.x
            val e = sourcePoint.z
            val f = direction.z
            val g = line.sourcePoint.z
            val h = line.direction.z
            val te = -(a * h - c * h - d * (e - g))
            val be = b * h - d * f
            if (be == 0.0) {
                return intersectYZ(line)
            }
            val t = te / be
            val result = Vector3D(0.0, 0.0, 0.0)
            result.x = sourcePoint.x + direction.x * t
            result.y = sourcePoint.y + direction.y * t
            result.z = sourcePoint.z + direction.z * t
            return result
        }

        private fun intersectYZ(line: Line): Vector3D? {
            val a = sourcePoint.y
            val b = direction.y
            val c = line.sourcePoint.y
            val d = line.direction.y
            val e = sourcePoint.z
            val f = direction.z
            val g = line.sourcePoint.z
            val h = line.direction.z
            val te = -(a * h - c * h - d * (e - g))
            val be = b * h - d * f
            if (be == 0.0) {
                return null
            }
            val t = te / be
            val result = Vector3D(0.0, 0.0, 0.0)
            result.x = sourcePoint.x + direction.x * t
            result.y = sourcePoint.y + direction.y * t
            result.z = sourcePoint.z + direction.z * t
            return result
        }

        fun intersectPlane(pointOnPlane: Vector3D, planeNormal: Vector3D?): Vector3D? {
            val result = Vector3D(sourcePoint.x, sourcePoint.y, sourcePoint.z)
            val d = pointOnPlane.sub(sourcePoint).dot(planeNormal) / direction.dot(planeNormal)
            result.sadd(direction.mul(d))
            return if (direction.dot(planeNormal) == 0.0) {
                null
            } else result
        }

        init {
            sourcePoint.x = sx
            sourcePoint.y = sy
            sourcePoint.z = sz
            direction.x = dx
            direction.y = dy
            direction.z = dz
        }
    }

    companion object {
        @JvmStatic
        var instance: GLUProjection? = null
            get() {
                if (field == null) {
                    field = GLUProjection()
                }
                return field
            }
            private set
    }
}