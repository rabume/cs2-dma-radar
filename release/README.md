# Third-Party Binaries

This directory contains compiled binaries from third-party open-source projects that are required for CS2 DMA Radar to function.

## Included Libraries

### MemProcFS (AGPL-3.0)

- **Files**: `vmm.dll` (Windows), `libvmm.so` (Linux)
- **Project**: https://github.com/ufrisk/MemProcFS
- **Author**: Ulf Frisk (@ufrisk)
- **License**: GNU Affero General Public License v3.0 (AGPL-3.0)
- **Description**: The Memory Process File System - A easy and convenient way of viewing physical memory as files in a virtual file system.

### LeechCore (GPL-3.0)

- **Files**: `leechcore.dll` (Windows), `leechcore.so` (Linux), `leechcore_ft601_driver_linux.so` (Linux driver)
- **Project**: https://github.com/ufrisk/LeechCore
- **Author**: Ulf Frisk (@ufrisk)
- **License**: GNU General Public License v3.0 (GPL-3.0)
- **Description**: The LeechCore Physical Memory Acquisition Library - Focuses on Physical Memory Acquisition using various hardware and software based methods.

## License Information

The full license texts for these dependencies are available at their respective repositories:

- **MemProcFS (AGPL-3.0)**: https://github.com/ufrisk/MemProcFS/blob/master/LICENSE
- **LeechCore (GPL-3.0)**: https://github.com/ufrisk/LeechCore/blob/master/LICENSE

## Attribution

We are grateful to Ulf Frisk for creating these excellent memory acquisition and analysis libraries that make this CS2 DMA Radar project possible. These libraries are the foundation that enables direct memory access functionality.

## Source Code

The source code for these open-source libraries is available at their respective GitHub repositories:

- **MemProcFS**: https://github.com/ufrisk/MemProcFS
- **LeechCore**: https://github.com/ufrisk/LeechCore
