/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.plantPack;

import com.google.common.collect.Maps;
import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.gf.tree.lsystem.AdvanceAxionElementGeneration;
import org.terasology.gf.tree.lsystem.AdvancedLSystemTreeDefinition;
import org.terasology.gf.tree.lsystem.AxionElementGeneration;
import org.terasology.gf.tree.lsystem.AxionElementReplacement;
import org.terasology.gf.tree.lsystem.DefaultAxionElementGeneration;
import org.terasology.gf.tree.lsystem.SimpleAxionElementReplacement;
import org.terasology.gf.tree.lsystem.SurroundAxionElementGeneration;
import org.terasology.gf.tree.lsystem.TreeBlockDefinition;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Arrays;
import java.util.Map;

@RegisterPlugin
public class MapleGrowthDefinition implements PlantGrowthDefinition {
    public static final String ID = "PlantPack:maple";
    public static final String GENERATED_BLOCK = "PlantPack:MapleSaplingGenerated";

    private AdvancedLSystemTreeDefinition treeDefinition;

    public MapleGrowthDefinition() {
        Map<Character, AxionElementReplacement> replacementMap = Maps.newHashMap();

        SimpleAxionElementReplacement sapling = new SimpleAxionElementReplacement("s");
        sapling.addReplacement(1f, "Tt");

        final FastRandom rnd = new FastRandom();

        SimpleAxionElementReplacement trunkTop = new SimpleAxionElementReplacement("t");
        trunkTop.addReplacement(0.6f,
                new SimpleAxionElementReplacement.ReplacementGenerator() {
                    @Override
                    public String generateReplacement(String currentAxion) {
                        // 137.5 degrees is a golden ratio
                        int deg = rnd.nextInt(125, 152);
                        return "+(" + deg + ")[&Mb]Wt";
                    }
                });
        trunkTop.addReplacement(0.4f,
                new SimpleAxionElementReplacement.ReplacementGenerator() {
                    @Override
                    public String generateReplacement(String currentAxion) {
                        // Always generate at least 2 branches
                        if (currentAxion.split("b").length < 2) {
                            // 137.5 degrees is a golden ratio
                            int deg = rnd.nextInt(130, 147);
                            return "+(" + deg + ")[&Mb]Wt";
                        }
                        return "Wt";
                    }
                });

        SimpleAxionElementReplacement smallBranch = new SimpleAxionElementReplacement("b");
        smallBranch.addReplacement(0.8f, "Bb");

        SimpleAxionElementReplacement trunk = new SimpleAxionElementReplacement("T");
        trunk.addReplacement(0.7f, "TN");

        replacementMap.put('s', sapling);
        replacementMap.put('g', sapling);
        replacementMap.put('t', trunkTop);
        replacementMap.put('T', trunk);
        replacementMap.put('b', smallBranch);

        TreeBlockDefinition mapleSapling = new TreeBlockDefinition("PlantPack:MapleSapling");
        TreeBlockDefinition mapleSaplingGenerated = new TreeBlockDefinition(GENERATED_BLOCK);
        TreeBlockDefinition greenLeaf = new TreeBlockDefinition("PlantPack:MapleLeaf");
        TreeBlockDefinition mapleTrunk = new TreeBlockDefinition("PlantPack:MapleTrunk");
        TreeBlockDefinition mapleBranch = new TreeBlockDefinition("PlantPack:MapleBranch", true);

        float trunkAdvance = 0.3f;
        float branchAdvance = 0.2f;

        Map<Character, AxionElementGeneration> blockMap = Maps.newHashMap();
        blockMap.put('s', new DefaultAxionElementGeneration(mapleSapling, trunkAdvance));
        blockMap.put('g', new DefaultAxionElementGeneration(mapleSaplingGenerated, trunkAdvance));

        // Trunk building blocks
        blockMap.put('t', new SurroundAxionElementGeneration(greenLeaf, greenLeaf, trunkAdvance, 2f));
        blockMap.put('T', new DefaultAxionElementGeneration(mapleTrunk, trunkAdvance));
        blockMap.put('N', new DefaultAxionElementGeneration(mapleTrunk, trunkAdvance));
        blockMap.put('W', new SurroundAxionElementGeneration(mapleBranch, greenLeaf, trunkAdvance, 2f));

        // Branch building blocks
        SurroundAxionElementGeneration smallBranchGeneration = new SurroundAxionElementGeneration(greenLeaf, greenLeaf, branchAdvance, 2.6f);
        smallBranchGeneration.setMaxZ(0);
        SurroundAxionElementGeneration largeBranchGeneration = new SurroundAxionElementGeneration(mapleBranch, greenLeaf, branchAdvance, 1.1f, 3.5f);
        largeBranchGeneration.setMaxZ(0);
        blockMap.put('b', smallBranchGeneration);
        blockMap.put('B', largeBranchGeneration);
        blockMap.put('M', new AdvanceAxionElementGeneration(branchAdvance));

        treeDefinition = new AdvancedLSystemTreeDefinition(ID, "g", replacementMap, blockMap, Arrays.asList(mapleTrunk, mapleBranch, greenLeaf), 1.5f);
    }

    @Override
    public String getPlantId() {
        return ID;
    }

    @Override
    public void generatePlant(String seed, Vector3i chunkPos, ChunkView chunkView, int x, int y, int z, GenerationParameters generationParameters) {
        treeDefinition.generateTree(seed, GENERATED_BLOCK, chunkPos, chunkView, x, y, z);
    }

    @Override
    public boolean initializePlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return treeDefinition.setupTreeBaseBlock(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public void updatePlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef treeRef) {
        treeDefinition.updateTree(worldProvider, blockEntityRegistry, treeRef);
    }
}
