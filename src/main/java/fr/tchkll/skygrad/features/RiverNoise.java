package fr.tchkll.skygrad.features;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Domain-warped ridge noise for procedural rivers.
 * Java port of {@code river_noise} from {@code side/river_poc.py}.
 *
 * <p>Each river system is built from three Perlin samples — two warps that
 * displace the input coordinates, plus the base sample at the warped location —
 * and is then biased by a directional tilt so rivers tend to flow along an
 * axis.  Multiple systems can be overlaid: even-indexed systems use Y-axis
 * tilt, odd-indexed use X-axis tilt, allowing rivers to cross.</p>
 *
 * <p>Each instance owns one {@link ImprovedNoise} (Minecraft's gradient noise),
 * seeded once at construction.  The single permutation is shared across all
 * three samples per system, mirroring the POC which uses one global perm
 * table.  System decorrelation is handled with input-space offsets, just like
 * the POC.</p>
 *
 * <p>{@link #sample} returns the river field in [0, 1]: 0 far from any river,
 * 1 at the centerline.  {@link #riverValue} thresholds against the configured
 * river width; {@link #depth} returns how far inside the band you are.</p>
 */
public final class RiverNoise {

    // ── defaults that mirror the POC ─────────────────────────────────────────

    public static final int    DEFAULT_RIVER_QUANTITY = 1;
    public static final double DEFAULT_SCALE          = 0.008;
    public static final double DEFAULT_RIVER_WIDTH    = 0.08;
    public static final double DEFAULT_TILT_SIZE      = 500.0;

    // ── magic numbers from the POC, named for clarity ────────────────────────

    private static final double WARP_AMOUNT     = 1.5;   // base = perlin(x + 1.5·wx, ...)
    private static final double BASE_WEIGHT     = 0.6;   // mix of base sample
    private static final double TILT_WEIGHT     = 0.4;   // mix of directional tilt
    private static final double WARP_OFFSET_X   = 5.2;   // decorrelate wx from wy
    private static final double WARP_OFFSET_Y   = 1.3;
    private static final double SYSTEM_OFFSET_X = 17.3;  // decorrelate river systems
    private static final double SYSTEM_OFFSET_Y = 23.7;

    // ── instance state ───────────────────────────────────────────────────────

    private final ImprovedNoise perlin;
    private final int    riverQuantity;
    private final double scale;
    private final double riverWidth;
    private final double tiltWidth;
    private final double tiltHeight;

    // ── construction ─────────────────────────────────────────────────────────

    /**
     * @param seed          permutation seed for the underlying gradient noise
     * @param riverQuantity number of overlaid river systems (POC: 1)
     * @param scale         noise frequency — smaller = wider, lazier rivers (POC: 0.003)
     * @param riverWidth    bright-band thickness in [0, 1] (POC: 0.08)
     * @param tiltWidth     X-axis normalisation for the tilt term (POC: image width)
     * @param tiltHeight    Y-axis normalisation for the tilt term (POC: image height)
     */
    public RiverNoise(long seed, int riverQuantity, double scale,
                      double riverWidth, double tiltWidth, double tiltHeight) {
        this.perlin        = new ImprovedNoise(new LegacyRandomSource(seed));
        this.riverQuantity = riverQuantity;
        this.scale         = scale;
        this.riverWidth    = riverWidth;
        this.tiltWidth     = tiltWidth;
        this.tiltHeight    = tiltHeight;
    }

    /** Convenience constructor: defaults match {@code river_poc.py}. */
    public RiverNoise(long seed) {
        this(seed, DEFAULT_RIVER_QUANTITY, DEFAULT_SCALE,
             DEFAULT_RIVER_WIDTH, DEFAULT_TILT_SIZE, DEFAULT_TILT_SIZE);
    }

    // ── public API ───────────────────────────────────────────────────────────

    /**
     * Returns the river field at (x, y) in [0, 1].
     * 0 = far from any river centerline; 1 = right on a centerline.
     * Direct port of {@code river_noise(x, y)} in the POC.
     */
    public double sample(double x, double y) {
        double s = scale;
        double best = 0.0;

        for (int i = 0; i < riverQuantity; i++) {
            double ox = i * SYSTEM_OFFSET_X;
            double oy = i * SYSTEM_OFFSET_Y;

            // Two warp samples — domain-warp the base coordinates.
            double wx = perlin01(x * s + ox,                  y * s + oy);
            double wy = perlin01(x * s + ox + WARP_OFFSET_X,  y * s + oy + WARP_OFFSET_Y);

            // Base sample at warped position.
            double base = perlin01(x * s + WARP_AMOUNT * wx + ox,
                                   y * s + WARP_AMOUNT * wy + oy);

            // Alternating tilt — even systems flow ~along Y, odd ~along X.
            double tilt = (i % 2 == 0) ? (y / tiltHeight) : (x / tiltWidth);
            double directed = base * BASE_WEIGHT + tilt * TILT_WEIGHT;

            // Ridge transform: 0 at extremes, 1 at the 0.5 contour.
            double ridge = 1.0 - Math.abs(2.0 * directed - 1.0);
            if (ridge > best) best = ridge;
        }

        return best;
    }

    /** Yes */
    public double riverValue(double x, double y) {
        var sample = sample(x, y);

        var threshold = (1.0 - riverWidth);
        if(sample < threshold) return 0;

        return (sample - threshold) / riverWidth;
    }

    /**
     * River-depth in [0, 1]: 0 at the band edge, 1 at the centerline,
     * exactly 0 outside the band.  Useful for carving deeper channels
     * along the centerline.
     */
    public double depth(double x, double y) {
        double s = sample(x, y);
        double threshold = 1.0 - riverWidth;
        if (s <= threshold) return 0.0;
        return (s - threshold) / riverWidth;
    }

    // ── internals ────────────────────────────────────────────────────────────

    /**
     * {@link ImprovedNoise#noise} is roughly in [-1, 1]; remap to [0, 1] like
     * the POC's {@code _perlin}.  We sample on the y = 0 plane so the noise is
     * effectively 2-D.
     */
    private double perlin01(double x, double y) {
        return (perlin.noise(x, 0.0, y) + 1.0) / 2.0;
    }
}
