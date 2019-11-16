package com.pugz.bloomful.common.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import com.pugz.bloomful.core.registry.BlockRegistry;
import com.pugz.bloomful.core.util.BiomeFeatures;
import com.pugz.bloomful.core.util.WisteriaColor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static com.pugz.bloomful.common.block.WisteriaVineBlock.HALF;
import static com.pugz.bloomful.core.util.WisteriaTreeUtils.*;

public class WisteriaTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
    private final BlockState LOG = BlockRegistry.WISTERIA_LOG.getDefaultState();
    private BlockState LEAF;
    private BlockState VINE_UPPER;
    private BlockState VINE_LOWER;

    public WisteriaTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configIn, boolean doBlockNotifyIn, WisteriaColor color) {
        super(configIn, doBlockNotifyIn);
        setBlocksByColor(color);
    }

    private void setBlocksByColor(WisteriaColor color) {
        switch (color) {
            case PURPLE:
                LEAF = BlockRegistry.PURPLE_WISTERIA_LEAVES.getDefaultState();
                VINE_UPPER = BlockRegistry.PURPLE_WISTERIA_VINE.getDefaultState();
                VINE_LOWER = BlockRegistry.PURPLE_WISTERIA_VINE.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
                break;
            case WHITE:
                LEAF = BlockRegistry.WHITE_WISTERIA_LEAVES.getDefaultState();
                VINE_UPPER = BlockRegistry.WHITE_WISTERIA_VINE.getDefaultState();
                VINE_LOWER = BlockRegistry.WHITE_WISTERIA_VINE.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
                break;
            case PINK:
                LEAF = BlockRegistry.PINK_WISTERIA_LEAVES.getDefaultState();
                VINE_UPPER = BlockRegistry.PINK_WISTERIA_VINE.getDefaultState();
                VINE_LOWER = BlockRegistry.PINK_WISTERIA_VINE.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
                break;
            case BLUE:
                LEAF = BlockRegistry.BLUE_WISTERIA_LEAVES.getDefaultState();
                VINE_UPPER = BlockRegistry.BLUE_WISTERIA_VINE.getDefaultState();
                VINE_LOWER = BlockRegistry.BLUE_WISTERIA_VINE.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
                break;
        }
    }

    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader world, Random random, BlockPos pos, MutableBoundingBox boundingBox) {
        if (isDirtOrGrassBlock(world, pos.down())) {
            int height = random.nextInt(7) + 5;
            for (int i = 0; i < height; ++i) {
                setLogState(changedBlocks, world, pos.add(0, i, 0), LOG, boundingBox);
            }
            placeBranch(world, random, pos.down(), pos.up(height).getY());
            if (random.nextInt(4) == 3) placeBranch(world, random, pos.down(), pos.up(height).getY());
            ArrayList<BlockPos> trunkBlacklist = new ArrayList<BlockPos>() {};
            for (int y = pos.getY(); y <= height + pos.getY() - 1; ++y) {
                trunkBlacklist.add(new BlockPos(pos.getX(), y, pos.getZ()));
            }
            for (int y = 4; y > -4; --y)
                for (int x = 4; x > -4; --x)
                    for (int z = 4; z > -4; --z)
                        if (Math.sqrt((x * x) + (y > 0 ? (y * y) : 0) + (z * z)) <= 4) {
                            BlockPos leafPos = pos.up(height).add(x, y, z);
                            boolean place = true;
                            if (y < 0) {
                                place = world.hasBlockState(leafPos.add(0, 1, 0), (state) -> {
                                    return state.isIn(BlockTags.LEAVES);
                                });
                                if (place && random.nextInt(Math.abs(y) + 1) != 0) {
                                    place = false;
                                    if (random.nextInt(2) == 0) {
                                        placeVines(world, random, leafPos, LEAF, VINE_LOWER, VINE_UPPER);
                                    }
                                }
                            }
                            if (place && isAirOrLeaves(world, leafPos)) {
                                BlockPos placePos = new BlockPos(x, y, z);
                                for (BlockPos blacklistPos : trunkBlacklist) {
                                    if (placePos != blacklistPos) {
                                        setLogState(changedBlocks, world, leafPos, LEAF, boundingBox);
                                    }
                                }
                            }
                        }
            return true;
        }
        return false;
    }

    private void placeBranch(IWorldGenerationReader world, Random random, BlockPos pos, int treeHeight) {
        int heightOffset = random.nextInt(3);
        BlockPos[] startPositions = new BlockPos[] {
                new BlockPos(pos.getX() - 1, treeHeight - heightOffset, pos.getZ()),
                new BlockPos(pos.getX() + 1, treeHeight - heightOffset, pos.getZ()),
                new BlockPos(pos.getX(), treeHeight - heightOffset, pos.getZ() - 1),
                new BlockPos(pos.getX(), treeHeight - heightOffset, pos.getZ() + 1),
                new BlockPos(pos.getX() - 1, treeHeight - heightOffset, pos.getZ() - 1),
                new BlockPos(pos.getX() + 1, treeHeight - heightOffset, pos.getZ() - 1),
                new BlockPos(pos.getX() - 1, treeHeight - heightOffset, pos.getZ() + 1),
                new BlockPos(pos.getX() + 1, treeHeight - heightOffset, pos.getZ() + 1)
        };
        BlockPos startPos = startPositions[random.nextInt(8)];
        if (isAirOrLeaves(world, startPos)) {
            boolean vines = random.nextInt(6) != 5;
            BlockPos placePos = startPos;
            for (int y = (treeHeight - heightOffset); y <= treeHeight; ++y) {
                placePos = new BlockPos(startPos.getX(), y, startPos.getZ());
                world.setBlockState(placePos, LOG, 18);
            }
            world.setBlockState(placePos.up(), LEAF, 18);
            if (vines) {
                placeVines(world, random, startPos.down(), LEAF, VINE_LOWER, VINE_UPPER);
            }
        }
    }

    public static void addFeature() {
        ForgeRegistries.BIOMES.getValues().forEach(WisteriaTreeFeature::generate);
    }

    public static void generate(Biome biome) {
        if (biome.getCategory() == Biome.Category.JUNGLE) {
            BiomeFeatures.addWisteriaTree(biome, WisteriaColor.PINK, 0, 0.05F);
        }
        else if (biome.getCategory() == Biome.Category.SWAMP) {
            BiomeFeatures.addWisteriaTree(biome, WisteriaColor.BLUE, 0, 0.025F);
        }
        else if (biome.getCategory() == Biome.Category.PLAINS) {
            BiomeFeatures.addWisteriaTrees(biome,0, 0.01F);
        }
        else if (biome.getCategory() == Biome.Category.FOREST) {
            if (biome == Biomes.FLOWER_FOREST) BiomeFeatures.addWisteriaTrees(biome,0, 0.05F);
            else BiomeFeatures.addWisteriaTrees(biome,0, 0.025F);
        }
    }
}