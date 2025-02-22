package mypals.ml.features.soundListener3D;

import com.google.common.collect.Lists;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.sound.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.config.LucidityConfig.renderBlockEventsColor;

public class SoundListener implements SoundInstanceListener {
    List<SoundEventEntry> soundEntries = Lists.<SoundEventEntry>newArrayList();
    private boolean enabled;
    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float range) {
        if (soundSet.getSubtitle() != null) {
            Text text = soundSet.getSubtitle();
            this.soundEntries.add(new SoundEventEntry(text, range, new Vec3d(sound.getX(), sound.getY(), sound.getZ())));
        }
    }
    public void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player == null || client.gameRenderer.getCamera() == null) return;
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        SoundManager soundManager = client.getSoundManager();
        if (!this.enabled && LucidityConfig.renderSoundEvents) {
            soundManager.registerListener(this);
            this.enabled = true;
        } else if (this.enabled && !LucidityConfig.renderSoundEvents) {
            soundManager.unregisterListener(this);
            this.enabled = false;
        }
        if (this.enabled) {
            for(SoundEventEntry soundEventEntry : this.soundEntries) {
                SoundEntry soundEntry = soundEventEntry.getNearestSound(cameraPos);
                if(soundEntry!=null) {
                    Vec3d soundPos = soundEntry.location;
                    BlockPos blockPos = new BlockPos(MathHelper.floor(soundPos.x), MathHelper.floor(soundPos.y), MathHelper.floor(soundPos.z));
                    InformationRender.addAreaBox(new AreaBox(blockPos,blockPos, renderSoundEventsColor,
                            renderSoundEventsColor.getAlpha()/255.0f, false));

                    InformationRender.addText(new mypals.ml.rendering.shapes.Text(
                            new ArrayList<String>() {{
                        add(soundEventEntry.getText().getString());
                        add(soundEntry.location.toString());
                    }}, soundPos, 0.01f, new ArrayList<Color>() {{
                        add(new Color(renderSoundEventsColor.getRed(),renderSoundEventsColor.getGreen(),renderSoundEventsColor.getBlue(),1));
                        add(new Color(renderSoundEventsColor.getRed(),renderSoundEventsColor.getGreen(),renderSoundEventsColor.getBlue(),1));
                    }}, 255, true));
                    soundEventEntry.removeExpired(renderSoundEventsExpiredTime);
                }
            }

        }
    }
    static record SoundEntry(Vec3d location, long time) {
    }
    static class SoundEventEntry {
        private final Text text;
        private final float range;
        private final List<SoundEntry> sounds = new ArrayList();

        public SoundEventEntry(Text text, float range, Vec3d pos) {
            this.text = text;
            this.range = range;
            this.sounds.add(new SoundEntry(pos, Util.getMeasuringTimeMs()));
        }

        public Text getText() {
            return this.text;
        }

        public SoundEntry getNearestSound(Vec3d pos) {
            if (this.sounds.isEmpty()) {
                return null;
            } else {
                return this.sounds.size() == 1
                        ? this.sounds.getFirst()
                        : this.sounds.stream().min(Comparator.comparingDouble(soundPos -> soundPos.location().distanceTo(pos))).orElse(null);
            }
        }

        public void reset(Vec3d pos) {
            this.sounds.removeIf(sound -> pos.equals(sound.location()));
            this.sounds.add(new SoundEntry(pos, Util.getMeasuringTimeMs()));
        }

        public boolean canHearFrom(Vec3d pos) {
            if (Float.isInfinite(this.range)) {
                return true;
            } else if (this.sounds.isEmpty()) {
                return false;
            } else {
                SoundEntry soundEntry = this.getNearestSound(pos);
                return soundEntry == null ? false : pos.isInRange(soundEntry.location, this.range);
            }
        }

        public void removeExpired(double expiry) {
            long l = Util.getMeasuringTimeMs();
            this.sounds.removeIf(sound -> (double)(l - sound.time()) > expiry);
        }

        public boolean hasSounds() {
            return !this.sounds.isEmpty();
        }
    }
}
