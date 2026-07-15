# java-staking-rewards

**Time-based staking reward accrual in Java** — the same protocol I built on
Solana (Anchor/Rust) and EVM (Solidity), now on the JVM: `reward = staked ·
elapsed · rewardRate / 1e12`, with `claim` resetting the accrual clock.

`StakingPosition` (`src/main/java/dev/liander/staking/`) uses `BigInteger` for the
intermediate product to avoid overflow, then returns a `long` token amount. It's a
pure, deterministic calculation, so it unit-tests cleanly with **JUnit 5**.

## Tests (`src/test/java/...`)

- `claim` then `claim` accrues from the reset point (100, then 150),
- `pending` is read-only (doesn't consume the reward),
- reward is proportional to elapsed time,
- zero stake / zero time yields nothing,
- negative inputs and backwards time are rejected.

## Run it

```bash
mvn -B test
```

Requires a JDK 17+ and Maven. CI (`.github/workflows/ci.yml`) provisions Temurin 17
via `actions/setup-java` and runs `mvn -B test` on every push.

## License

MIT — see [LICENSE](LICENSE).
