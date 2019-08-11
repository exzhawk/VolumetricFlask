package me.exz.volumetricflask.server;

import me.exz.volumetricflask.common.items.FluidCapabilityProvider;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class Command extends CommandBase {
    @Override
    public String getName() {
        return "vf";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.volumetricflask.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(sender);
            ItemStack stack = entityPlayerMP.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemVolumetricFlask) {
                NBTTagCompound ret = stack.serializeNBT();
                System.out.println(ret.toString());
                FluidCapabilityProvider fluidCapabilityProvider = (FluidCapabilityProvider) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidCapabilityProvider != null) {
                    FluidStack fluidStack = fluidCapabilityProvider.getFluid();
                    if (fluidStack != null && fluidStack.amount != 0) {
                        throw new WrongUsageException("commands.volumetricflask.usage");
                    }
                    int targetCapacity = Integer.parseInt(args[0]);
                    if (targetCapacity > 0 && targetCapacity <= 1000) {
                        fluidCapabilityProvider.setCapacity(targetCapacity);
                    }

                }
            } else {
                throw new WrongUsageException("commands.volumetricflask.usage");
            }
        } else {
            throw new WrongUsageException("commands.volumetricflask.usage");
        }
    }
}
