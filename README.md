# Ingwaz Coin #

![Alt text](src/main/resources/IngwazSmall.png?raw=true "Logo")<br/>
Ingwaz is an attempt to create a new, simple-to-understand cryptocurrency, implementing the basic concepts behind a
BlockChain in Java. The cryptocurrency is not designed for large-scale use, and is more suitable for a 'reward' system
within a somewhat small (less than 100,000 people) group. The fundamental concept of a BlockChain is used, but other
security implementations found in famous Currencies may not be utilized (Implementation is more important than security
for this project)

For a high-level overview of this project, see our [website](https://shua1090.github.io/IngwazSite/)!

## Using this Project

See Releases for a .jar files to host your own Node, run a Miner Client, or create/access a Wallet.

## Current Coin Testnet Details

Subject to change until release 1.0<br />
![Alt text](src/main/resources/IngwazCoin.png?raw=true "Logo")<br/>

- 100 Coins as Initial Miner's Reward, along with 1% of every Transaction within the Block
- Halving every 100,000 blocks
- 1 Centralized Node, with the ability for "Nodeship" to be transferred to other Computers easily
- Variable Network Difficulty, with Target Difficulty altered every 100 blocks
- 1000 Transactions per Block (excluding Mining Reward)
- 1% Non-Negotiable Transaction Fee
- TXID, consisting of Blocknumber and Millisecond time, signed and verified using ECDSA (BouncyCastle as Provider)
- 10 Minute per Block (Estimated, may be subject to change)
- 64 Byte Nonce (144 Byte Block Header)

Note, this coin can easily be self-hosted and the hoster can change most of the above parameters through a config file.

### Contribution

  <br />
  More contributors always welcome. 
  Please note: commits and Pull Requests that only reformat code without adding use-able functionality will be rejected.
  For more information contact shynn.lawrence@gmail.com
  <br/>
  -- Shynn Lawrence

### License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
