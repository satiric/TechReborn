package techreborn.tiles.lesu;


import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import reborncore.common.misc.Functions;
import reborncore.common.util.Inventory;
import techreborn.config.ConfigTechReborn;
import techreborn.powerSystem.TilePowerAcceptor;

import java.util.ArrayList;

public class TileLesu extends TilePowerAcceptor {//TODO wrench

    private ArrayList<LesuNetwork> countedNetworks = new ArrayList<LesuNetwork>();
    public int connectedBlocks = 0;

    private double euLastTick = 0;
    private double euChange;
    private int ticks;
    private int output;
    private int maxStorage;

    public Inventory inventory = new Inventory(2, "TileAesu", 64, this);

    public TileLesu() {
        super(5);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) {
            return;
        }
        countedNetworks.clear();
        connectedBlocks = 0;
        for (EnumFacing dir : EnumFacing.values()) {
            if (worldObj.getTileEntity(new BlockPos(getPos().getX() + dir.getFrontOffsetX(), getPos().getY() + dir.getFrontOffsetY(), getPos().getZ() + dir.getFrontOffsetZ())) instanceof TileLesuStorage) {
                if (((TileLesuStorage) worldObj.getTileEntity(new BlockPos(getPos().getX() + dir.getFrontOffsetX(), getPos().getY() + dir.getFrontOffsetY(), getPos().getZ() + dir.getFrontOffsetZ()))).network != null) {
                    LesuNetwork network = ((TileLesuStorage) worldObj.getTileEntity(new BlockPos(getPos().getX() + dir.getFrontOffsetX(), getPos().getY() + dir.getFrontOffsetY(), getPos().getZ() + dir.getFrontOffsetZ()))).network;
                    if (!countedNetworks.contains(network)) {
                        if (network.master == null || network.master == this) {
                            connectedBlocks += network.storages.size();
                            countedNetworks.add(network);
                            network.master = this;
                            break;
                        }
                    }
                }
            }
        }
        maxStorage = ((connectedBlocks + 1) * ConfigTechReborn.lesuStoragePerBlock);
        output = (connectedBlocks * ConfigTechReborn.extraOutputPerLesuBlock) + ConfigTechReborn.baseLesuOutput;

        if (ticks == ConfigTechReborn.aveargeEuOutTickTime) {
            euChange = -1;
            ticks = 0;
        } else {
            ticks++;
            if (euChange == -1) {
                euChange = 0;
            }
            euChange += getEnergy() - euLastTick;
            if (euLastTick == getEnergy()) {
                euChange = 0;
            }
        }

        euLastTick = getEnergy();
    }


    public double getEuChange() {
        if (euChange == -1) {
            return 0;
        }
        return (euChange / ticks);
    }

    @Override
    public double getMaxPower() {
        return maxStorage;
    }

    @Override
    public boolean canAcceptEnergy(EnumFacing direction) {
        return direction != getFacingEnum();
    }

    @Override
    public boolean canProvideEnergy(EnumFacing direction) {
        return direction == getFacingEnum();
    }

    @Override
    public double getMaxOutput() {
        return output;
    }

    @Override
    public double getMaxInput() {
        return 8192;
    }
}
