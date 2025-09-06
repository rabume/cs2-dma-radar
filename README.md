# CS2 DMA Radar

_Fork of [CS2_DMA_Radar](https://github.com/MoZiHao/CS2_DMA_Radar) with some improvements._

> [!WARNING]
> This project is for educational purposes only. I do not encourage cheating in any way.

![gif](https://github.com/rabume/cs2-dma-radar/assets/19410629/c2d6130c-7d67-49a1-8617-aeef07b148fc)

> If you just want to use the radar, you can download the latest release [here](https://github.com/rabume/cs2-dma-radar/releases)
> Requirements to run latest release: [JDK 21](https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=21)

## 🚀 Usage

After you complied or downloaded the latest release, you can start the radar with the following command:

```bash
# Replace x.x.x with the version you downloaded
java -jar CS2DMA-X.X.X.jar
```

You should be able to access the radar at http://localhost:8080 and your local IP address. If you want to share the radar, check the section [Share radar](#-share-radar).

## ⚡️ Requirements

**Hardware:**

- DMA card -> Only tested with [Screamer PCIe Squirrel](https://shop.lambdaconcept.com/home/50-screamer-pcie-squirrel.html)

**Software:**

- Nodejs (>= 20.3.0) -> Use [nvm](https://github.com/nvm-sh/nvm) or directly install [nodejs](https://nodejs.org/en)
- Java Development Kit (>= 21) -> [Download](https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=21)
- Apache Maven (>= 3.8.7) -> [Download](https://maven.apache.org/download.cgi)
- Make (>= GNU Make 4.4.1) -> Use cygwin or install make directly: [Download](https://www.cygwin.com/)

**Windows users:**

- Download `FTD3XX.dll` from [FTDI D3XX drivers](https://ftdichip.com/drivers/d3xx-drivers/) and place it in the `release/vmm/` folder (required for FPGA hardware communication)

#### 🚨 Note

- If you have issues installing `make` on Windows you can follow this [guide](https://gist.github.com/evanwill/0207876c3243bbb6863e65ec5dc3f058#make)
- Attacker PC can either be running Windows OR Linux. Victim PC is expected to run Windows.

## 📡 Share radar

To share the radar, you have to forward the port 8080 on your router to your local machine.
Then you can share your public IP address with your friends.

Alternatively, you can use a service like [ngrok](https://ngrok.com/) to share your radar.

## 🛠️ Build

1. Install frontend dependencies: `cd client && npm i`
2. Move back to the root directory: `cd ..`
3. Build application: `make build`
4. Move to the release directory: `cd release`
5. Start the application: `java -jar CS2DMA-X.X.X.jar`

## 🗺️ Memmap

For AMD CPU's you need to provide a "memory map" for your victim machine.
To extract this memory map you can use the tool [rammap](https://learn.microsoft.com/en-us/sysinternals/downloads/rammap) from Microsoft. Follow this [guide](https://github.com/ufrisk/LeechCore/wiki/Device_FPGA_AMD_Thunderbolt#known-memory-map--access-to-the-system).
Create an `memmap.txt` in the same folder as the `offsets.json` and enter the values you received.

## 🎯 Offsets

The offsets are stored in a file called `offsets.json` in the root directory of the application.
To get the latest offsets you can use the [cs2-dumper](https://github.com/a2x/cs2-dumper) by a2x.
If you don't want to dump them yourself, you can use the offsets from [offsets.rs](https://github.com/a2x/cs2-dumper/blob/main/output/offsets.rs) and [client.dll.hpp](https://github.com/a2x/cs2-dumper/blob/main/output/client.dll.hpp).

You can also use the github action offset update script to automaticly update the offstes.

## 📄 License

This project includes compiled binaries and assets from third-party open-source projects:

- **MemProcFS** by [@ufrisk](https://github.com/ufrisk/MemProcFS) - Licensed under [AGPL-3.0](https://github.com/ufrisk/MemProcFS/blob/master/LICENSE)
- **LeechCore** by [@ufrisk](https://github.com/ufrisk/LeechCore) - Licensed under [GPL-3.0](https://github.com/ufrisk/LeechCore/blob/master/LICENSE)
- **CS2 React HUD** by [@lexogrine](https://github.com/lexogrine/cs2-react-hud) - Licensed under [MIT](https://github.com/lexogrine/cs2-react-hud/blob/main/LICENSE) - Radar map assets

I'm grateful to Ulf Frisk for these excellent memory acquisition and analysis libraries, and to Lexogrine for the high-quality radar assets that make this project possible.
