<div align="center"> 
  <img src="https://raw.githubusercontent.com/haveno-dex/haveno-meta/721e52919b28b44d12b6e1e5dac57265f1c05cda/logo/haveno_logo_landscape.svg" alt="Haveno logo">

  [![Codacy Badge](https://app.codacy.com/project/badge/Grade/1a4ddf140d634f2ca1fd120a7cff4574)](https://www.codacy.com/gh/haveno-dex/haveno/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=haveno-dex/haveno&amp;utm_campaign=Badge_Grade)
  ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/haveno-dex/haveno/CI)
  ![GitHub issues with bounty](https://img.shields.io/github/issues-search/haveno-dex/haveno?color=%23fef2c0&label=Issues%20with%20bounties&query=project%3Ahaveno-dex%2F2) | 
  ![Twitter Follow](https://img.shields.io/twitter/follow/HavenoDEX?style=social)
  ![Matrix Room](https://img.shields.io/badge/Matrix%20room-%23haveno-blue)
</div>

## What is Haveno?

Haveno (pronounced ha‧ve‧no) is a private and decentralized way to exchange Monero for national currencies or other cryptocurrencies. Haveno uses peer-to-peer networking and multi-signature escrow to facilitate trading without a trusted third party custodian. Disputes can be resolved using non-custodial arbitration. Everything is built around Monero and Tor.

Haveno is the Esperanto word for "Harbor". The project is stewarded by a core Team, currently formed by 2 people: ErCiccione and Woodser. See the [FAQ in the wiki](https://github.com/haveno-dex/haveno/wiki/FAQ) for more information about the project.

Haveno is still work in progress. There are no binaries available at this stage. If you want to test Haveno you have to compile it first (see [Running a local Haveno test network](#running-a-local-haveno-test-network)).

## Status of the project

At the moment Haveno is only a Proof of Concept. It's already possible to initiate crypto <-> XMR and fiat <-> XMR trades, but the platform still needs a lot of work before being available for public use.

The project is divided between multiple repositories:

- **[haveno](https://github.com/haveno-dex/haveno)** - This repository. Contains the Proof of Concept of what will be the heart of Haveno.
- **[haveno-ui-poc](https://github.com/haveno-dex/haveno-ui-poc)** - The PoC of the future user interface. Uses gRPC APIs to serve the UI in React.
- **[haveno-design](https://github.com/haveno-dex/haveno-design)** - Temporary (for now empty) repository for design discussions.
- **[haveno-meta](https://github.com/haveno-dex/haveno-meta)** - For project-wide discussions and proposals.
- **[haveno-site](https://github.com/haveno-dex/haveno-site)** - The repository of the future website.

Currently, efforts are focused in developing the core repository ('haveno'). If you wish to help, take a look at the [issue tracker](https://github.com/haveno-dex/haveno/issues) and the [Kanban boards (projects)](https://github.com/haveno-dex/haveno/projects). We run a bounty program to incentivize development, the issues elegible for a reward in XMR [are labelled '💰bounty'](https://github.com/haveno-dex/haveno/labels/%F0%9F%92%B0bounty).

## Trade protocol

See [docs/trade-protocol.md](docs/trade-protocol.md)

## Keep in touch and help out!

Haveno is a community-driven project. For it to be succesful it's fundamental to have the support and help of the Monero community. We have our own Matrix server. Registrations are not open at the moment, but the rooms are public and can be joined from any matrix client (like Element). We look forward to hearing from you!

- General discussions: **Haveno** (`#haveno:haveno.network`) relayed on Freenode (`#haveno`)
- Development discussions: **Haveno Development** (`#haveno-dev:haveno.network`) relayed on Freenode (`#haveno-dev`)

Temporary email: havenodex@protonmail.com

### Styling guidelines

See [CONTRIBUTING.md](docs/CONTRIBUTING.md)

## Bounties

To incentivize development and reward contributors we adopt a simple bounty system. Contributors may be awarded bounties after completing a task (resolving an issue). [More details in the docs](docs/bounties.md).

## Running a local Haveno test network

1. Download [Monero CLI](https://www.getmonero.org/downloads/) for your system and sync Monero stagenet: `./monerod --stagenet --rpc-login superuser:abctesting123`, or alternatively, [set up a local Monero stagenet network](#running-a-local-monero-stagenet-network) (recommended)
2. Download and install [Bitcoin-Qt](https://bitcoin.org/en/download)
3. Run Bitcoin-Qt in regtest mode, e.g.: `./Bitcoin-Qt -regtest -peerbloomfilters=1`
4. In Bitcoin-Qt console, mine BTC regtest blocks: `generatetoaddress 101 bcrt1q6j90vywv8x7eyevcnn2tn2wrlg3vsjlsvt46qz`
5. Install [git lfs](https://git-lfs.github.com) for your system (Ubuntu: `sudo apt install git-lfs`)
6. `git clone https://github.com/Haveno-Dex/haveno`
7. Copy monero-wallet-rpc from step 1 to the haveno project root
8. Apply permission to run monero-wallet-rpc, e.g. `chmod 777 monero-wallet-rpc`
9. Optionally modify [WalletConfig.java](core/src/main/java/bisq/core/btc/setup/WalletConfig.java) with custom settings
10. `cd haveno`
11. `./gradlew build`
12. Start seed node, arbitrator, Alice, and Bob:
    1. `./haveno-seednode --baseCurrencyNetwork=XMR_STAGENET --useLocalhostForP2P=true --useDevPrivilegeKeys=true --nodePort=2002 --appName=haveno-XMR_STAGENET_Seed_2002`
    2. `./haveno-desktop --baseCurrencyNetwork=XMR_STAGENET --useLocalhostForP2P=true --useDevPrivilegeKeys=true --nodePort=4444 --appName=haveno-XMR_STAGENET_arbitrator --apiPassword=apitest --apiPort=9998`
    3. `./haveno-desktop --baseCurrencyNetwork=XMR_STAGENET --useLocalhostForP2P=true --useDevPrivilegeKeys=true --nodePort=5555 --appName=haveno-XMR_STAGENET_Alice --apiPassword=apitest --apiPort=9999`
    4. `./haveno-desktop --baseCurrencyNetwork=XMR_STAGENET --useLocalhostForP2P=true --useDevPrivilegeKeys=true --nodePort=6666 --appName=haveno-XMR_STAGENET_Bob --apiPassword=apitest --apiPort=10000`
13. Arbitrator window > Account > cmd+n to register a new arbitrator
14. Arbitrator window > Account > cmd+d to register a new mediator
15. Deposit stagenet XMR to Alice and Bob's Haveno wallets (wallet address printed to terminal)
16. When deposited XMR is available, proceed to post offers, etc

### Running a local Monero stagenet network

1. Build [monero-project](https://github.com/monero-project/monero) with the following modification to the bottom of hardforks.cpp:
    ```c++
    const hardfork_t stagenet_hard_forks[] = {
      // version 1 from the start of the blockchain
      { 1, 1, 0, 1341378000 },
      
      // versions 2-7 in rapid succession from March 13th, 2018
      { 2, 10, 0, 1521000000 },
      { 3, 20, 0, 1521120000 },
      { 4, 30, 0, 1521240000 },
      { 5, 40, 0, 1521360000 },
      { 6, 50, 0, 1521480000 },
      { 7, 60, 0, 1521600000 },
      { 8, 70, 0, 1537821770 },
      { 9, 80, 0, 1537821771 },
      { 10, 90, 0, 1550153694 },
      { 11, 100, 0, 1550225678 },
      { 12, 110, 0, 1571419280 },
      { 13, 120, 0, 1598180817 },
      { 14, 130, 0, 1598180818 }
    };
    ```
2. Using the executables built in step 1:
    * `./monerod --stagenet --no-igd --hide-my-port --data-dir node1 --p2p-bind-ip 127.0.0.1 --p2p-bind-port 48080 --rpc-bind-port 48081 --zmq-rpc-bind-port 48082 --add-exclusive-node 127.0.0.1:38080 --rpc-login superuser:abctesting123 --rpc-access-control-origins http://localhost:8080`
    * `./monerod --stagenet --no-igd --hide-my-port --data-dir node2 --p2p-bind-ip 127.0.0.1 --rpc-bind-ip 0.0.0.0 --confirm-external-bind --add-exclusive-node 127.0.0.1:48080 --rpc-login superuser:abctesting123 --rpc-access-control-origins http://localhost:8080`
4. Mine the first 130 blocks to a random address before using so wallets only use the latest output type.  For example, in a daemon: `start_mining 56k9Yra1pxwcTYzqKcnLip8mymSQdEfA6V7476W9XhSiHPp1hAboo1F6na7kxTxwvXU6JjDQtu8VJdGj9FEcjkxGJfsyyah 1`

## Sponsors

Would you like to help us build Haveno? Become a sponsor! We will show your logo here. Contact us at havenodex@protonmail.com.

<a href="https://getmonero.org"><img src="/media/sponsors/monero-community.png" title="Monero community" alt="Monero community logo" width="100px"></a>
<a href="https://samouraiwallet.com/"><img src="/media/sponsors/samourai.png" title="Samourai wallet" alt="Samourai wallet logo" width="100px"></a>
<a href="https://cakewallet.com/"><img src="/media/sponsors/cake-logo-blue.jpg" title="Cake wallet" alt="Cake wallet logo" width="100px"></a>
<a href="https://twitter.com/DonYakka"><img src="/media/sponsors/donyakka.jpg" title="Don Yakka" alt="Don Yakka logo" width="100px"></a>

## Support

To bring Haveno to life, we need resources. If you have the possibility, please consider donating to the project. At this stage, donations are fundamental:

`42sjokkT9FmiWPqVzrWPFE5NCJXwt96bkBozHf4vgLR9hXyJDqKHEHKVscAARuD7in5wV1meEcSTJTanCTDzidTe2cFXS1F`

![Qr code](https://raw.githubusercontent.com/haveno-dex/haveno/master/media/qrhaveno.png)

If you are using a wallet that supports Openalias (like the 'official' CLI and GUI wallets), you can simply put `donations@haveno.network` as the "receiver" address.
