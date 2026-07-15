package dev.liander.staking;

import java.math.BigInteger;

/**
 * Time-based staking reward accrual — the Java port of the same protocol I built
 * on Solana (Anchor) and EVM (Solidity): {@code reward = staked * elapsed *
 * rewardRate / 1e12}, with claim resetting the accrual clock.
 *
 * <p>Uses {@link BigInteger} for the intermediate product to avoid overflow, then
 * returns a {@code long} token amount. Pure/deterministic, so it unit-tests cleanly.
 */
public final class StakingPosition {

    /** Fixed-point scale (1e12), matching the Solana/EVM implementations. */
    public static final long SCALE = 1_000_000_000_000L;

    private final long staked;
    private final long rewardRate;
    private long lastTime;
    private long accrued;

    public StakingPosition(long staked, long rewardRate, long startTime) {
        if (staked < 0 || rewardRate < 0) {
            throw new IllegalArgumentException("staked and rewardRate must be non-negative");
        }
        this.staked = staked;
        this.rewardRate = rewardRate;
        this.lastTime = startTime;
    }

    /** Reward accrued by {@code staked} tokens over {@code elapsed} seconds at {@code rate}. */
    public static long rewardFor(long staked, long elapsed, long rate) {
        if (staked < 0 || elapsed < 0 || rate < 0) {
            throw new IllegalArgumentException("inputs must be non-negative");
        }
        return BigInteger.valueOf(staked)
                .multiply(BigInteger.valueOf(elapsed))
                .multiply(BigInteger.valueOf(rate))
                .divide(BigInteger.valueOf(SCALE))
                .longValueExact();
    }

    /** Unclaimed rewards as of {@code now}, without mutating state. */
    public long pending(long now) {
        return accrued + rewardFor(staked, elapsedSince(now), rewardRate);
    }

    /** Settle accrued rewards up to {@code now} and return them, resetting the clock. */
    public long claim(long now) {
        accrued += rewardFor(staked, elapsedSince(now), rewardRate);
        lastTime = now;
        long payout = accrued;
        accrued = 0;
        return payout;
    }

    private long elapsedSince(long now) {
        if (now < lastTime) {
            throw new IllegalArgumentException("time must not go backwards");
        }
        return now - lastTime;
    }
}
