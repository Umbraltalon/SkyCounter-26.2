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
import java.util.HashMap;
import java.util.Map;
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


    public record GoldenDragonFinderResult(boolean success, List<String> messages) {
        public static GoldenDragonFinderResult notInWorld() {
            return new GoldenDragonFinderResult(false, List.of("Must be in a world"));
        }
        public static GoldenDragonFinderResult nothingFound() {
            return new GoldenDragonFinderResult(true, List.of("No Golden Dragon room candidate found in the scanned chunks."));
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
     * Finds the Golden Dragon lair by looking for a large connected red statue
     * (redstone blocks, red wool and red terracotta), sandstone details inside
     * its footprint, and nearby gold blocks. Gold is deliberately a supporting
     * signal rather than a fixed platform requirement.
     */
    public static GoldenDragonFinderResult findGoldenDragonRoom(ClientLevel world, BlockPos playerPos) {
        if (world == null || playerPos == null) return GoldenDragonFinderResult.notInWorld();

        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int radius = 10;
        Set<BlockPos> redBlocks = new HashSet<>();

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                LevelChunk chunk = world.getChunk(centerChunkX + cx, centerChunkZ + cz);
                if (chunk == null) continue;
                int startX = chunk.getPos().getMinBlockX();
                int startZ = chunk.getPos().getMinBlockZ();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinY() + 3; y < world.getMaxY() - 2; y++) {
                            BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                            if (isGoldenDragonRedBlock(chunk.getBlockState(pos))) redBlocks.add(pos);
                        }
                    }
                }
            }
        }

        Set<BlockPos> unvisited = new HashSet<>(redBlocks);
        List<BlockPos> candidates = new ArrayList<>();
        while (!unvisited.isEmpty()) {
            BlockPos seed = unvisited.iterator().next();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            queue.add(seed);
            unvisited.remove(seed);
            int count = 0;
            int minX = seed.getX(), maxX = minX, minY = seed.getY(), maxY = minY;
            int minZ = seed.getZ(), maxZ = minZ;
            long sumX = 0, sumY = 0, sumZ = 0;

            while (!queue.isEmpty()) {
                BlockPos pos = queue.removeFirst();
                count++;
                minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
                minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
                minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
                sumX += pos.getX(); sumY += pos.getY(); sumZ += pos.getZ();
                for (DirectionOffset direction : DirectionOffset.values()) {
                    BlockPos neighbour = pos.offset(direction.dx, direction.dy, direction.dz);
                    if (unvisited.remove(neighbour)) queue.addLast(neighbour);
                }
            }

            int width = maxX - minX + 1;
            int height = maxY - minY + 1;
            int depth = maxZ - minZ + 1;
            if (count < 100 || width < 9 || height < 5 || depth < 7) continue;

            // Require the lighter sandstone details to be embedded in/near the statue.
            int sandstone = 0;
            for (int x = minX - 1; x <= maxX + 1; x++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    for (int y = minY - 1; y <= maxY + 1; y++) {
                        String name = BuiltInRegistries.BLOCK.getKey(world.getBlockState(new BlockPos(x, y, z)).getBlock()).getPath();
                        if (name.contains("sandstone")) sandstone++;
                    }
                }
            }
            if (sandstone < 5) continue;

            // Gold may be under, beside, or scattered around the dragon.
            int gold = 0;
            BlockPos goldCenter = new BlockPos((minX + maxX) / 2, minY, (minZ + maxZ) / 2);
            for (int x = minX - 8; x <= maxX + 8; x++) {
                for (int z = minZ - 8; z <= maxZ + 8; z++) {
                    for (int y = minY - 5; y <= minY + 5; y++) {
                        if (world.getBlockState(new BlockPos(x, y, z)).is(Blocks.GOLD_BLOCK)) gold++;
                    }
                }
            }
            if (gold < 8) continue;

            candidates.add(new BlockPos((int)(sumX / count), (int)(sumY / count), (int)(sumZ / count)));
        }

        if (candidates.isEmpty()) return GoldenDragonFinderResult.nothingFound();
        candidates.sort(Comparator.comparingLong(pos -> pos.distSqr(playerPos)));
        BlockPos center = candidates.get(0);
        WaypointManager.setWaypoints(List.of(new WaypointManager.Waypoint(center, "Golden Dragon Room", 0xFFFFAA00)));
        return new GoldenDragonFinderResult(true, List.of("Golden Dragon room candidate found at: " + center.toShortString()));
    }

    private enum DirectionOffset {
        UP(0, 1, 0), DOWN(0, -1, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1), EAST(1, 0, 0), WEST(-1, 0, 0);
        final int dx, dy, dz;
        DirectionOffset(int dx, int dy, int dz) { this.dx = dx; this.dy = dy; this.dz = dz; }
    }

    private static boolean isGoldenDragonRedBlock(net.minecraft.world.level.block.state.BlockState state) {
        String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return name.equals("redstone_block") || name.equals("red_wool") || name.equals("red_terracotta");
    }

    /**
     * Looks for the Key Guardian chamber using the reference screenshots:
     * several tall granite pillars arranged around a broad stone-floored room.
     * Pillar footprints are grouped first, then nearby pillars are grouped into
     * a chamber candidate. This avoids treating a pair of unrelated towers as
     * a valid spawn structure.
     */
    public static KeyGuardianFinderResult findKeyGuardianStructures(ClientLevel world, BlockPos playerPos) {
        if (world == null || playerPos == null) {
            return KeyGuardianFinderResult.notInWorld();
        }

        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int radius = 16;

        // For each horizontal column, record the vertical granite extent and count.
        Map<Long, int[]> columns = new HashMap<>();
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
                        int blockX = startX + x;
                        int blockZ = startZ + z;
                        int count = 0;
                        int minY = Integer.MAX_VALUE;
                        int maxY = Integer.MIN_VALUE;

                        for (int y = world.getMinY() + 3; y < world.getMaxY() - 2; y++) {
                            if (isGraniteStructureBlock(chunk.getBlockState(new BlockPos(blockX, y, blockZ)))) {
                                count++;
                                minY = Math.min(minY, y);
                                maxY = Math.max(maxY, y);
                            }
                        }

                        if (count >= 8 && maxY - minY >= 7) {
                            columns.put(columnKey(blockX, blockZ), new int[]{minY, maxY, count});
                        }
                    }
                }
            }
        }

        // Merge adjacent vertical columns into individual pillar footprints.
        Set<Long> unvisited = new HashSet<>(columns.keySet());
        List<BlockPos> pillars = new ArrayList<>();
        while (!unvisited.isEmpty()) {
            long seed = unvisited.iterator().next();
            ArrayDeque<Long> queue = new ArrayDeque<>();
            queue.add(seed);
            unvisited.remove(seed);

            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            int baseYTotal = 0;
            int members = 0;

            while (!queue.isEmpty()) {
                long key = queue.removeFirst();
                int x = columnX(key);
                int z = columnZ(key);
                int[] info = columns.get(key);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
                baseYTotal += info[0];
                members++;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        long neighbour = columnKey(x + dx, z + dz);
                        if (unvisited.remove(neighbour)) {
                            queue.addLast(neighbour);
                        }
                    }
                }
            }

            // A pillar may be several blocks thick, but should not be a long wall.
            if (maxX - minX <= 4 && maxZ - minZ <= 4) {
                pillars.add(new BlockPos((minX + maxX) / 2, baseYTotal / members, (minZ + maxZ) / 2));
            }
        }

        // Group pillars belonging to the same room.
        List<BlockPos> chamberCenters = new ArrayList<>();
        Set<Integer> remaining = new HashSet<>();
        for (int i = 0; i < pillars.size(); i++) remaining.add(i);

        while (!remaining.isEmpty()) {
            int seed = remaining.iterator().next();
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            List<BlockPos> group = new ArrayList<>();
            queue.add(seed);
            remaining.remove(seed);

            while (!queue.isEmpty()) {
                BlockPos current = pillars.get(queue.removeFirst());
                group.add(current);
                List<Integer> newlyFound = new ArrayList<>();
                for (int index : remaining) {
                    BlockPos other = pillars.get(index);
                    int dx = current.getX() - other.getX();
                    int dz = current.getZ() - other.getZ();
                    if (dx * dx + dz * dz <= 26 * 26
                            && Math.abs(current.getY() - other.getY()) <= 6) {
                        newlyFound.add(index);
                    }
                }
                for (int index : newlyFound) {
                    remaining.remove(index);
                    queue.addLast(index);
                }
            }

            if (group.size() < 4) {
                continue;
            }

            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            long sumX = 0, sumY = 0, sumZ = 0;
            for (BlockPos pillar : group) {
                minX = Math.min(minX, pillar.getX());
                maxX = Math.max(maxX, pillar.getX());
                minZ = Math.min(minZ, pillar.getZ());
                maxZ = Math.max(maxZ, pillar.getZ());
                sumX += pillar.getX();
                sumY += pillar.getY();
                sumZ += pillar.getZ();
            }

            // Require pillars spread in both horizontal directions, not just a pair.
            if (maxX - minX < 7 || maxZ - minZ < 7
                    || maxX - minX > 34 || maxZ - minZ > 34) {
                continue;
            }

            BlockPos center = new BlockPos(
                    (int) (sumX / group.size()),
                    (int) (sumY / group.size()),
                    (int) (sumZ / group.size())
            );

            // The reference chamber has a substantial stone/cobblestone floor.
            int floorBlocks = 0;
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    for (int dy = -2; dy <= 1; dy++) {
                        if (isKeyGuardianFloorBlock(world.getBlockState(center.offset(dx, dy, dz)))) {
                            floorBlocks++;
                        }
                    }
                }
            }

            if (floorBlocks >= 35) {
                chamberCenters.add(center);
            }
        }

        if (chamberCenters.isEmpty()) {
            return KeyGuardianFinderResult.nothingFound();
        }

        chamberCenters.sort(Comparator.comparingInt((BlockPos pos) -> pos.getX())
                .thenComparingInt(pos -> pos.getZ()));
        List<String> messages = new ArrayList<>();
        List<WaypointManager.Waypoint> waypoints = new ArrayList<>();
        int count = Math.min(chamberCenters.size(), 3);
        for (int i = 0; i < count; i++) {
            BlockPos center = chamberCenters.get(i);
            String label = "Key Guardian Chamber " + (i + 1);
            waypoints.add(new WaypointManager.Waypoint(center, label, 0xFF55DDAA));
            messages.add(label + " candidate found at: " + center.toShortString());
        }

        SkyblockCounterService.setKeyGuardianChambers(chamberCenters);
        WaypointManager.setWaypoints(waypoints);
        return new KeyGuardianFinderResult(true, messages);
    }

    private static long columnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static int columnX(long key) {
        return (int) (key >> 32);
    }

    private static int columnZ(long key) {
        return (int) key;
    }

    private static boolean isKeyGuardianFloorBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.CRACKED_STONE_BRICKS)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.STONE)
                || state.is(Blocks.POLISHED_ANDESITE);
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
