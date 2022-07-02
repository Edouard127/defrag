package com.lambda.client.mixin.client.entity;

import com.lambda.client.mixin.client.accessor.AccessorEntityFireworkRocket;
import com.lambda.client.module.modules.movement.ElytraFlight;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    @Unique
    private Vec3d modifiedVec = null;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }


    @ModifyVariable(
        method = "travel(FFF)V",
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 3
    )
    private float f(float original) {
        if (shouldWork()) {
            return ElytraFlight.INSTANCE.getPacketPitch() * 0.017453292f;
        }
        return original;
    }

    @Inject(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionZ:D", ordinal = 3),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void getVec(
        float strafe,
        float vertical,
        float forward,
        CallbackInfo ci,
        // Local capture
        Vec3d vec3d
    ) {
        modifiedVec = vec3d;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionX:D", ordinal = 7)
    )
    public double motionX(EntityLivingBase it) {
        if (shouldModify()) {
            it.motionX += modifiedVec.x * 0.1 + (modifiedVec.x * 1.5 - this.motionX) * 0.5;
        }
        return it.motionX;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionY:D", ordinal = 7)
    )
    public double motionY(EntityLivingBase it) {
        if (shouldModify()) {
            it.motionY += modifiedVec.y * 0.1 + (modifiedVec.y * 1.5 - this.motionY) * 0.5;
        }
        return it.motionY;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionZ:D", ordinal = 7)
    )
    public double motionZ(EntityLivingBase it) {
        if (shouldModify()) {
            it.motionZ += modifiedVec.z * 0.1 + (modifiedVec.z * 1.5 - this.motionZ) * 0.5;
        }
        return it.motionZ;
    }

    @Unique
    private boolean shouldWork() {
        return EntityPlayerSP.class.isAssignableFrom(getClass())
            && ElytraFlight.INSTANCE.isEnabled()
            && ElytraFlight.INSTANCE.getMode().getValue() == ElytraFlight.ElytraFlightMode.VANILLA;
    }

    @Unique
    private boolean shouldModify() {
        return shouldWork() && world.loadedEntityList.stream().anyMatch(entity -> {
                if (entity instanceof EntityFireworkRocket) {
                    EntityLivingBase boosted = ((AccessorEntityFireworkRocket) entity).getBoostedEntity();
                    return boosted != null && boosted.equals(this);
                }
                return false;
            }
        );
    }
}