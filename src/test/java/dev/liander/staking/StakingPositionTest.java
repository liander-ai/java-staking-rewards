package dev.liander.staking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StakingPositionTest {

    // staked=1000, rate=1e9 => reward = 1000 * elapsed * 1e9 / 1e12 = elapsed
    private static final long STAKED = 1000L;
    private static final long RATE = 1_000_000_000L;

    @Test
    void claimThenClaimAccruesFromReset() {
        StakingPosition pos = new StakingPosition(STAKED, RATE, 0L);
        // after 100s: 1000 * 100 * 1e9 / 1e12 = 100
        assertEquals(100L, pos.claim(100L));
        // 150s more (t=250): another 150, because claim reset the clock at t=100
        assertEquals(150L, pos.claim(250L));
    }

    @Test
    void pendingIsNonMutating() {
        StakingPosition pos = new StakingPosition(STAKED, RATE, 0L);
        assertEquals(100L, pos.pending(100L));
        assertEquals(100L, pos.pending(100L)); // unchanged by the first read
        assertEquals(100L, pos.claim(100L)); // still claimable in full
    }

    @Test
    void rewardIsProportionalToTime() {
        assertEquals(50L, StakingPosition.rewardFor(STAKED, 50L, RATE));
        assertEquals(100L, StakingPosition.rewardFor(STAKED, 100L, RATE));
    }

    @Test
    void zeroStakeOrZeroTimeYieldsNothing() {
        assertEquals(0L, StakingPosition.rewardFor(0L, 100L, RATE));
        assertEquals(0L, StakingPosition.rewardFor(STAKED, 0L, RATE));
    }

    @Test
    void rejectsNegativeInputs() {
        assertThrows(IllegalArgumentException.class, () -> new StakingPosition(-1L, RATE, 0L));
        assertThrows(IllegalArgumentException.class, () -> StakingPosition.rewardFor(STAKED, -1L, RATE));
    }

    @Test
    void rejectsTimeGoingBackwards() {
        StakingPosition pos = new StakingPosition(STAKED, RATE, 100L);
        assertThrows(IllegalArgumentException.class, () -> pos.claim(50L));
    }
}
