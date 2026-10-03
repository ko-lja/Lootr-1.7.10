package lu.kolja.lootr.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class StackUtil {

    public static final int CHEST_SIZE = 27;

    private StackUtil() {}

    public static NBTTagList write(ItemStack[] stacks) {
        var list = new NBTTagList();
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                var tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stacks[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }

    public static ItemStack[] read(NBTTagList list, int size) {
        var stacks = new ItemStack[size];
        for (int i = 0; i < list.tagCount(); i++) {
            var tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < size) {
                stacks[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
        return stacks;
    }

    public static ItemStack[] copy(ItemStack[] stacks) {
        var copy = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copy[i] = ItemStack.copyItemStack(stacks[i]);
        }
        return copy;
    }

    public static boolean isEmpty(ItemStack[] stacks) {
        for (var stack : stacks) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }
}
