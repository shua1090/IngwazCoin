# Ingwaz Coin #

![Alt text](src/main/resources/IngwazSmall.png?raw=true "Logo")<br/>
Ingwaz is an attempt to create a new, simple-to-understand cryptocurrency, implementing the basic concepts behind a
BlockChain in Java. The cryptocurrency is not designed for large-scale use, and is more suitable for a 'reward' system
within a somewhat small (less than 100,000 people) group. The fundamental concept of a BlockChain is used, but other
security implementations found in famous Currencies may not be utilized (Implementation is more important than security
for this project)

## Status

- A method of organizing Blocks, verifying their validity, and keeping a list of current balances has been developed. A
  Mining (PoW) system has also been completed.
- A networking toolkit for communicating with a Server (or p2p in the future) is currently being designed
- An easily accessible GUI for Wallets and viewing the BlockChain is also in the works

## Current Coin Details

Subject to change until release 1.0<br />
![Alt text](src/main/resources/IngwazCoin.png?raw=true "Logo")<br/>

- 100 Coins as Miner's Reward, along with 1% of every Submitted Transaction
- Network Difficulty Reset every 100 blocks (Methods to verify Target validity)
- 100 Transactions per Block (excluding Mining Reward)
- 1% Non-Negotiable Transaction Fee (Methods available to verify)
- TXID, consisting of Blocknumber and Millisecond time, signed and verified using ECDSA (BouncyCastle as Provider)
- 1 Minute per Block (Estimated, may be subject to change)
- 64 Byte Nonce (144 Byte Block Header)

### Contributors:

- BlockChain and GUI: Shynn Lawrence
- GUI: Gowtham Duggirala
  <br />
  More contributors always welcome! A BlockChain in Java, written and documented by students, should be entertaining in
  the least!
  For more information, Fork and issue a Pull Request or contact shynn.lawrence@gmail.com
  <br/>
  -- Iŋgwaz Team

### License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.