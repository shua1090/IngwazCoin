# Mining

A miner is any computer which puts effort into securing the Blockchain. Miners use their computing power to validate
Blocks, essentially cycling through millions of numbers until they reach one that, combined with the rest of the block,
has a certain combined characteristic. Verifying that this number fullfills this characteristic is simple, but if the
block is altered, the entire process must be re-done.

## Proof of Work

A deterministic cryptographic hash function, the Sha-256 Function, is given the block header (A 144 byte header that
contains some metadata about the block). The hash function is designed in such a way that the outputted bytes are
usually random. A Miner must continually change part of the block header, the so-called "Nonce", and hash the header,
checking if the resulting hash is below a specified "Target". Because of the unpredictability of the hash function, the
only way to find a Nonce that satisfies these conditions is by brute-forcing the nonces, incrementing them and rehashing
continually.

## Ingwaz Foreman

Ingwaz offers Miner Client code to communicate with the Node to mine blocks. Currently, it runs two threads that start
at different numbers, and increment/hash concurrently - though this will most likely be changed.
