package com.github.lunatrius.schematica.world.schematic;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.template.Template;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;

public class SchematicStructure extends SchematicFormat {
    @Override
    public Object readFromNBT(final NBTTagCompound tagCompound) {
        final ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

        final Template template = new Template();
        template.read(tagCompound);

        final Schematic schematic = new Schematic(icon,
                template.getSize().getX(), template.getSize().getY(), template.getSize().getZ(), template.getAuthor());


        return null;
    }


        // for (Template.EntityInfo entity : template.entities) {
        //     schematic.addEntity(...);
        //

    @Override
    public boolean writeToNBT(final NBTTagCompound tagCompound, final ISchematic schematic) {
        Template template = new Template();

        template.setAuthor(schematic.getAuthor());

        // NOTE: Can't use MutableBlockPos here because we're keeping a reference to it in BlockInfo
        for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ORIGIN, template.getSize().add(-1, -1, -1))) {
            final TileEntity tileEntity = schematic.getTileEntity(pos);
            final NBTTagCompound compound;
            if (tileEntity != null) {
                compound = NBTHelper.writeTileEntityToCompound(tileEntity);
                // Tile entities in structures don't store these coords
                compound.removeTag("x");
                compound.removeTag("y");
                compound.removeTag("z");
            } else {
                compound = null;
            }

        }

        for (Entity entity : schematic.getEntities()) {
            try {
                // Entity positions are already offset via NBTHelper.reloadEntity
                Vec3d vec3d = new Vec3d(entity.posX, entity.posY, entity.posZ);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                entity.writeToNBTOptional(nbttagcompound);
                BlockPos blockpos;

                // TODO: Vanilla has a check like this, but we don't; this doesn't seem to
                // cause any problems though.
                // if (entity instanceof EntityPainting) {
                //     blockpos = ((EntityPainting)entity).getHangingPosition().subtract(startPos);
                // } else {
                blockpos = new BlockPos(vec3d);
                // }

            } catch (final Throwable t) {
                Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
            }
        }

        template.writeToNBT(tagCompound);
        return true;
    }

    @Override
    public String getName() {
        return Names.Formats.STRUCTURE;
    }

    @Override
    public String getExtension() {
        return Names.Extensions.STRUCTURE;
    }
}
