package de.maexle.skyblockcounter.gui;

import de.maexle.skyblockcounter.SkyblockCounterConfig;
import de.maexle.skyblockcounter.SkyblockCounterService;
import de.maexle.skyblockcounter.waypoint.WaypointManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class SkyblockCounterActions {

    private SkyblockCounterActions() {
    }

    public record CorleoneFinderResult(boolean success, List<String> messages) {
        public static CorleoneFinderResult notInWorld() {
            return new CorleoneFinderResult(false, List.of("Must be in a world"));
        }

        public static CorleoneFinderResult nothingFound() {
            return new CorleoneFinderResult(true, List.of("Neither structure was found in the loaded chunks."));
        }
    }

    public record KeyGuardianFinderResult(boolean success, List<String> messages) {
        public static KeyGuardianFinderResult notInWorld() {
            return new KeyGuardianFinderResult(false, List.of("Must be in a world"));
        }

        public static KeyGuardianFinderResult nothingFound() {
            return new KeyGuardianFinderResult(true, List.of("No Key Guardian towers were found in the scanned chunks."));
        }
    }


    public record OdawaFinderResult(boolean success, List<String> messages) {
        public static OdawaFinderResult notInWorld() {
            return new OdawaFinderResult(false, List.of("Must be in a world"));
        }

        public static OdawaFinderResult nothingFound() {
            return new OdawaFinderResult(true, List.of("No Odawa huts were found in the scanned chunks."));
        }
    }

    public record MobSwitchResult(boolean success, String message) {
    }

    public record MobAddResult(String message) {
    }

    public static void switchToTreasureHoarder() {
        SkyblockCounterService.switchMob("treasure_hoarder_70", "Treasure Hoarder");
    }

    public static void switchToCorleone() {
        SkyblockCounterService.switchMob("team_treasurite_corleone_200", "Corleone");
    }

    public static void switchToZealot() {
        SkyblockCounterService.switchMob("zealot_enderman_55", "Zealot");
    }

    public static MobSwitchResult switchToMob(String id) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        Optional<SkyblockCounterConfig.MobEntry> entry = config.getMobEntries().stream()
                .filter(e -> e.id.equals(id))
                .findFirst();

        if (entry.isEmpty()) {
            return new MobSwitchResult(false, "Mob not found: " + id);
        }

        SkyblockCounterConfig.MobEntry mob = entry.get();
        SkyblockCounterService.switchMob(mob.id, mob.name);
        return new MobSwitchResult(true, "Switched to " + mob.name);
    }

    public static List<SkyblockCounterConfig.MobEntry> listMobs() {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        if (config.getMobEntries().isEmpty()) {
            config.mobEntries.add(new SkyblockCounterConfig.MobEntry("treasure_hoarder_70", "Treasure Hoarder", "textures/gui/sprites/treasure_hoarder_head.png"));
            config.mobEntries.add(new SkyblockCounterConfig.MobEntry("team_treasurite_corleone_200", "Corleone", "textures/gui/sprites/boss_corleone_head.png"));
            config.mobEntries.add(new SkyblockCounterConfig.MobEntry("zealot_enderman_55", "Zealot", "textures/gui/sprites/zealot_enderman_head.png"));
            config.save();
            SkyblockCounterService.reloadMobTextures();
        }
        return config.getMobEntries();
    }

    public static MobAddResult addMob(String id, String name, String texture) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        String textureValue = texture != null && !texture.isBlank() ? texture : null;
        config.addMobEntry(id, name, textureValue);
        config.save();
        SkyblockCounterService.reloadMobTextures();
        String textureInfo = textureValue != null ? " (Texture: " + textureValue + ")" : "";
        return new MobAddResult("Added mob: " + name + " (" + id + ")" + textureInfo);
    }

    public static String removeMob(String id) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        config.removeMobEntry(id);
        config.save();
        SkyblockCounterService.reloadMobTextures();
        return "Removed mob: " + id;
    }

    public static void setHudPosition(int x, int y) {
        SkyblockCounterService.setHudPosition(x, y);
    }

    public static void setApiKey(String apiKey) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        config.setAPI_KEY(apiKey);
        SkyblockCounterService.setApiKey(apiKey);
        config.save();
    }

    public static void setUuid(String uuid) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        String undashedUuid = uuid.replace("-", "");
        config.setundashedUuid(undashedUuid);
        SkyblockCounterService.setUndashedUuid(undashedUuid);
        config.save();
    }

    public static boolean toggleSession() {
        boolean currentMode = SkyblockCounterService.isShowSessionKills();
        SkyblockCounterService.setShowSessionKills(!currentMode);
        return !currentMode;
    }

    public static void addSessionKills(int amount) {
        SkyblockCounterService.addSessionKills(amount);
    }

    public static void removeSessionKills(int amount) {
        SkyblockCounterService.removeSessionKills(amount);
    }

    public static void resetSession() {
        SkyblockCounterService.setStartSessionKills(0);
    }

    public static Optional<String> formatTimeFarmed() {
        if (!SkyblockCounterService.isShowSessionKills()) {
            return Optional.empty();
        }

        long totalSeconds = (System.currentTimeMillis() - SkyblockCounterService.sessionStartTime) / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return Optional.of(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    public static Optional<String> formatCorleonitePerHour() {
        if (!SkyblockCounterService.isShowSessionKills()) {
            return Optional.empty();
        }

        long timePassed = (System.currentTimeMillis() - SkyblockCounterService.sessionStartTime) / 1000;
        if (timePassed <= 0) {
            return Optional.of("0.00");
        }

        long corleonite = SkyblockCounterService.getLocalCorleoniteCount();
        double cph = ((double) corleonite / timePassed) * 3600.0;
        return Optional.of(String.format("%.2f", cph));
    }

    public static Optional<String> getCorleoniteStats() {
        if (!SkyblockCounterService.isShowSessionKills()) {
            return Optional.empty();
        }

        int corleoniteCount = SkyblockCounterService.getLocalCorleoniteCount();
        int sessionKills = SkyblockCounterService.getSessionKills();
        if (sessionKills <= 0) {
            return Optional.of("Corleonite: " + corleoniteCount + " | No session kills yet");
        }

        double percentage = (corleoniteCount * 100.0) / sessionKills;
        return Optional.of(String.format("Corleonite: %d | Drop: %.2f%%", corleoniteCount, percentage));
    }

    public static CorleoneFinderResult findCorleoneStructures(ClientLevel world, BlockPos playerPos) {
        System.out.println("Finding Corleone structures");
        if (world == null || playerPos == null) {
            return CorleoneFinderResult.notInWorld();
        }

        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int radius = 16;

        List<BlockPos> andesitePositions = new ArrayList<>();
        List<BlockPos> granitePositions = new ArrayList<>();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                int chunkX = centerChunkX + xOffset;
                int chunkZ = centerChunkZ + zOffset;

                LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                int startX = chunk.getPos().getMinBlockX();
                int startZ = chunk.getPos().getMinBlockZ();
                int bottomY = world.getMinY();
                int topY = world.getMaxY();

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = bottomY + 3; y < topY - 2; y++) {
                            BlockPos basePos = new BlockPos(startX + x, y, startZ + z);

                            if (chunk.getBlockState(basePos).is(Blocks.POLISHED_ANDESITE)
                                    && chunk.getBlockState(basePos.above()).is(Blocks.STONE_BRICK_STAIRS)
                                    && chunk.getBlockState(basePos.above(2)).is(Blocks.SMOOTH_STONE_SLAB)) {
                                andesitePositions.add(basePos);
                            }

                            if (chunk.getBlockState(basePos).is(Blocks.POLISHED_GRANITE)
                                    && world.getBlockState(basePos.below(1)).is(Blocks.POLISHED_GRANITE)
                                    && world.getBlockState(basePos.below(2)).is(Blocks.POLISHED_GRANITE)
                                    && world.getBlockState(basePos.below(3)).is(Blocks.POLISHED_GRANITE)) {
                                BlockPos[] step1Dirs = {basePos.north(), basePos.south(), basePos.east(), basePos.west()};
                                BlockPos[] step2Dirs = {basePos.north(2), basePos.south(2), basePos.east(2), basePos.west(2)};

                                for (int i = 0; i < 4; i++) {
                                    if (world.getBlockState(step1Dirs[i].above()).is(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("light_gray_terracotta")))
                                            && world.getBlockState(step2Dirs[i].above(2)).is(Blocks.STONE_BRICKS)) {
                                        granitePositions.add(basePos);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<String> messages = new ArrayList<>();
        List<WaypointManager.Waypoint> foundWaypoints = new ArrayList<>();

        if (andesitePositions.size() > 1) {
            BlockPos centerPos = averagePosition(andesitePositions);
            foundWaypoints.add(new WaypointManager.Waypoint(centerPos, "Corleone Structure 1"));
            messages.add("Corleone structure 1 found at: " + centerPos.toShortString());
        }

        if (!granitePositions.isEmpty()) {
            BlockPos centerPos = averagePosition(granitePositions);
            // 513, 85, 541 is a false positive, the crystal nucleus is not a corleone structure
            if(!(centerPos.getX() == 513 && centerPos.getY() == 85 && centerPos.getZ() == 541)) {
                foundWaypoints.add(new WaypointManager.Waypoint(centerPos, "Corleone Structure 2"));
                messages.add("Corleone structure 2 found at: " + centerPos.toShortString());
            }
        }

        if (messages.isEmpty()) {
            return CorleoneFinderResult.nothingFound();
        }

        WaypointManager.setWaypoints(foundWaypoints);

        return new CorleoneFinderResult(true, messages);
    }


    /**
     * Searches the currently accessible Crystal Hollows chunks for the two granite
     * temple variants in which Key Guardians spawn. The structure's granite shell
     * is used as the marker; small natural granite patches are filtered out by
     * component size and vertical extent.
     */
    public static KeyGuardianFinderResult findKeyGuardianStructures(ClientLevel world, BlockPos playerPos) {
        if (world == null || playerPos == null) {
            return KeyGuardianFinderResult.notInWorld();
        }

        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int radius = 16;
        Set<BlockPos> graniteBlocks = new HashSet<>();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                int chunkX = centerChunkX + xOffset;
                int chunkZ = centerChunkZ + zOffset;
                LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                int startX = chunk.getPos().getMinBlockX();
                int startZ = chunk.getPos().getMinBlockZ();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinY() + 3; y < world.getMaxY() - 2; y++) {
                            BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                            if (isGraniteStructureBlock(chunk.getBlockState(pos))) {
                                graniteBlocks.add(pos);
                            }
                        }
                    }
                }
            }
        }

        Set<BlockPos> remaining = new HashSet<>(graniteBlocks);
        List<BlockPos> towerCenters = new ArrayList<>();
        while (!remaining.isEmpty()) {
            BlockPos seed = remaining.iterator().next();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            List<BlockPos> component = new ArrayList<>();
            queue.add(seed);
            remaining.remove(seed);

            int minX = seed.getX(), maxX = seed.getX();
            int minY = seed.getY(), maxY = seed.getY();
            int minZ = seed.getZ(), maxZ = seed.getZ();
            long sumX = 0, sumY = 0, sumZ = 0;

            while (!queue.isEmpty()) {
                BlockPos current = queue.removeFirst();
                component.add(current);
                int x = current.getX(), y = current.getY(), z = current.getZ();
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
                sumX += x; sumY += y; sumZ += z;

                BlockPos[] neighbours = {
                        new BlockPos(x + 1, y, z), new BlockPos(x - 1, y, z),
                        new BlockPos(x, y + 1, z), new BlockPos(x, y - 1, z),
                        new BlockPos(x, y, z + 1), new BlockPos(x, y, z - 1)
                };
                for (BlockPos neighbour : neighbours) {
                    if (remaining.remove(neighbour)) {
                        queue.addLast(neighbour);
                    }
                }
            }

            int widthX = maxX - minX + 1;
            int height = maxY - minY + 1;
            int widthZ = maxZ - minZ + 1;
            if (component.size() >= 45 && height >= 8 && widthX <= 22 && widthZ <= 22) {
                towerCenters.add(new BlockPos(
                        (int) (sumX / component.size()),
                        (int) (sumY / component.size()),
                        (int) (sumZ / component.size())
                ));
            }
        }

        if (towerCenters.isEmpty()) {
            return KeyGuardianFinderResult.nothingFound();
        }

        towerCenters.sort(Comparator.comparingInt((BlockPos pos) -> pos.getX()).thenComparingInt(pos -> pos.getZ()));
        List<String> messages = new ArrayList<>();
        List<WaypointManager.Waypoint> waypoints = new ArrayList<>();
        int count = Math.min(towerCenters.size(), 2);
        for (int i = 0; i < count; i++) {
            BlockPos center = towerCenters.get(i);
            String label = "Key Guardian Tower " + (i + 1);
            waypoints.add(new WaypointManager.Waypoint(center, label, 0xFF55DDAA));
            messages.add(label + " found at: " + center.toShortString());
        }

        WaypointManager.setWaypoints(waypoints);
        return new KeyGuardianFinderResult(true, messages);
    }


    /**
     * Finds Odawa's wooden hut using the distinctive brewing-stand, flower-pot,
     * and lever arrangement, with a surrounding wooden-structure check.
     */
    public static OdawaFinderResult findOdawaStructures(ClientLevel world, BlockPos playerPos) {
        if (world == null || playerPos == null) {
            return OdawaFinderResult.notInWorld();
        }

        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int radius = 16;
        List<BlockPos> hutPositions = new ArrayList<>();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                LevelChunk chunk = world.getChunk(centerChunkX + xOffset, centerChunkZ + zOffset);
                if (chunk == null) {
                    continue;
                }

                int startX = chunk.getPos().getMinBlockX();
                int startZ = chunk.getPos().getMinBlockZ();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinY(); y < world.getMaxY(); y++) {
                            BlockPos brewingStand = new BlockPos(startX + x, y, startZ + z);
                            if (!chunk.getBlockState(brewingStand).is(Blocks.BREWING_STAND)) {
                                continue;
                            }

                            BlockPos flowerPot = null;
                            BlockPos lever = null;
                            for (int dx = -10; dx <= 10; dx++) {
                                for (int dz = -10; dz <= 10; dz++) {
                                    for (int dy = -4; dy <= 4; dy++) {
                                        BlockPos nearby = brewingStand.offset(dx, dy, dz);
                                        var state = world.getBlockState(nearby);
                                        if (state.is(Blocks.FLOWER_POT)) {
                                            flowerPot = nearby;
                                        } else if (state.is(Blocks.LEVER)) {
                                            lever = nearby;
                                        }
                                    }
                                }
                            }

                            if (flowerPot == null || lever == null) {
                                continue;
                            }

                            int potDistance = Math.abs(flowerPot.getX() - brewingStand.getX())
                                    + Math.abs(flowerPot.getZ() - brewingStand.getZ());
                            int leverDistance = Math.abs(lever.getX() - brewingStand.getX())
                                    + Math.abs(lever.getZ() - brewingStand.getZ());
                            if (potDistance < 2 || potDistance > 12 || leverDistance < 2 || leverDistance > 14
                                    || Math.abs(flowerPot.getY() - brewingStand.getY()) > 4
                                    || Math.abs(lever.getY() - brewingStand.getY()) > 4) {
                                continue;
                            }

                            int woodenBlocks = 0;
                            for (int dx = -7; dx <= 7; dx++) {
                                for (int dz = -7; dz <= 7; dz++) {
                                    for (int dy = -4; dy <= 5; dy++) {
                                        var nearbyState = world.getBlockState(brewingStand.offset(dx, dy, dz));
                                        String blockName = BuiltInRegistries.BLOCK.getKey(nearbyState.getBlock()).getPath();
                                        if (blockName.contains("planks") || blockName.contains("log")
                                                || blockName.contains("wood") || blockName.contains("stairs")
                                                || blockName.contains("slab") || blockName.contains("fence")
                                                || blockName.contains("trapdoor")) {
                                            woodenBlocks++;
                                        }
                                    }
                                }
                            }

                            if (woodenBlocks >= 25) {
                                hutPositions.add(brewingStand);
                            }
                        }
                    }
                }
            }
        }

        if (hutPositions.isEmpty()) {
            return OdawaFinderResult.nothingFound();
        }

        List<WaypointManager.Waypoint> waypoints = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        int count = 0;
        for (BlockPos hut : hutPositions) {
            boolean duplicate = hutPositions.stream().anyMatch(other ->
                    other != hut && Math.abs(other.getX() - hut.getX()) <= 8
                            && Math.abs(other.getY() - hut.getY()) <= 5
                            && Math.abs(other.getZ() - hut.getZ()) <= 8
                            && other.asLong() < hut.asLong());
            if (duplicate) {
                continue;
            }
            count++;
            String label = "Odawa Hut " + count;
            waypoints.add(new WaypointManager.Waypoint(hut, label, 0xFFFFCC55));
            messages.add(label + " found at: " + hut.toShortString());
        }

        if (waypoints.isEmpty()) {
            return OdawaFinderResult.nothingFound();
        }

        WaypointManager.setWaypoints(waypoints);
        return new OdawaFinderResult(true, messages);
    }

    private static boolean isGraniteStructureBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.GRANITE) || state.is(Blocks.POLISHED_GRANITE);
    }

    private static BlockPos averagePosition(List<BlockPos> positions) {
        int sumX = 0;
        int sumY = 0;
        int sumZ = 0;
        for (BlockPos pos : positions) {
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }
        int size = positions.size();
        return new BlockPos(sumX / size, sumY / size, sumZ / size);
    }

    public static String addWaypoint(String name, int x, int y, int z, int color) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        config.addWaypoint(name, x, y, z, color);
        config.save();
        return "Added waypoint: " + name + " (" + x + ", " + y + ", " + z + ")";
    }

    public static String removeWaypoint(String name) {
        SkyblockCounterConfig config = SkyblockCounterService.getConfig();
        config.removeWaypoint(name);
        config.save();
        return "Removed waypoint: " + name;
    }

}
