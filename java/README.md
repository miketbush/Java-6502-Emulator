# JUSE - Java Universal System Emulator

** We changed to JUSE! **

This was a great project to find and I really appreciate what Ben and Dylan has done.  
My need was to have flexible device configuration and easily mapped IO ports to model real reto hardware. 
The existing project construction provided an easy way to get what I wanted and allowed me to get there, quickly, in workable stages.
I have started by decoupling things so now you can expand and use it by making your own CPUs, devices and UIs.   
As before, this is still a work in progress and may discover some bugs.  
I would like to use this to do development debugging and gives flexibility to that end.

** NEW SOURCE IS UNDER /java **

FEATURES:
 - Architecture is modularized
 - Flexible Bus Architecture for ease of modeling systems
 - Better cross-platform UI:  Windows, MacOS and Linux should all behave.
 - CPU Assortment, 6502, Z80 and more to come
 - BYOD - Bring your own devices
 - Flexible Bus Architecture for ease of modeling systems
 - Mapped IO
 - System can be defined in configuration
 - Many bus devices out of the box with changeable addresses
     - 40x25 Text Display with variable fonts
	 - 80x25 Text Display with variable fonts
     - 640x480 256 Color Paged Graphics Mode
     - Banked RAM module, up to 256 banks with 64k each
     - Timer Device with IRQ
 - Included ROM and RAM demo binaries
 - New Font Resource Manager
 - New ROM Resource Manager
 - New System Configuration Loader
 - Extensible CPU model, currently 6502 and Z-80, with 68000 in works
 - Address Mapper
 - C64 Testing ability.  Loads the Kernal and BASIC, but not 100% functional
 - CC65/PASM integeration projects
 - System Configuration files 

Some might ask, why write an emulator in Java? And I would respond: "Because no one else would." Sure, Java is terribly slow (more than 1000x slower than the original!), and the fact that Java's ```byte```s and ```short```s are a pain to work with because they're signed, but it's the language I'm best in so I don't care ;)

Feel free to fork it, improve it, whatever, just link back to here. Enjoy!

## New "juse" directory stucture:

- /java - Top level of the JUSE line of code

- /java/dist - result artifact from a complate build

- /java/interfaces - Abstraction layer interfaces that allows the extension of the hardware with a common interface by external authors 

- /java/utility - Common utility functionality for conversions, process, project and configurations, to name a few.

- /java/devices - Default system devices like RAM, ROM, Timer, Displays and other "hardware"

- /java/emulator - Default entry point and UI implementation.   

- /java/ext - Examples of extension devices and functionality

- /java/lib - Support external jars

Each directory has an included build.xml that is callable by [Apache Ant](https://ant.apache.org/).  The root build.xml is contained under /java.  This build.xml will call all others to create a single jar under /dist

Additionally,there is the root /demo directory contains useful bits of sample binaries and configurations.

- /demo/juse - Contains updated JUSE based content.  The newer configurations and devices, as well as, the sample /ext directory container the non-base devices and UIs. 




## Commandline 
- **--demo**:
	- Extracts the demonstration projects, resources, extensions and ROM images to the 'demo' directory.

- **--ext**:
	- Specifies a directory to use for additional extensions to the emulator.
	- The directory, and any sub-directories, are scanned for all jars to be added.<br />
	- Any device in the subdirectories is available for use in the configuration files.

- **--ui**:
	- Specifies an optional UI class to override the default GUI

- **--help**:
	- Provides this informational message.

- **--project**:
	- Specifies a project associated with the emulator to build and run code.

- **--config**:
	- Specifies a configuration for the emulator to use.



** Command Arguments Example: **
  *java -jar JUSE.jar --demo --ext demo/juse/ext --config demo/Z80A.system*
  
  - This extracts the demo artifacts contained by default in the JUSE.jar, sets the extension directory into it,<br /> 
and starts up a Z80 system with the awful green UI (green is for illustrative purposes).


## Configuration File Syntax 

```
####################################################
#             System Configuration File
#
# Describes the devices and address locations 
#
# Extend functionality via class path entries 
#
####################################################
name=Z80A

####################################################
#            Extension Class Path
####################################################
#ext=./ext

####################################################
#            User Interface Handler Class
####################################################
#ui=com.mygui.EmulatorFrameImpl

####################################################
#            Address Map Handler Class
####################################################

addressbus=com.juse.emulator.devices.AddressMap16Impl

####################################################
#             CPU Definition Class
####################################################

#cpu=com.juse.emulator.ext.cpu.Zilog.Z80A
cpu=com.juse.emulator.devices.cpu.Zilog.Z80

####################################################
#         Device Handler Classes and Namings
# device=SYMBOLIC_NAME,CLASSNAME  
####################################################

device=RAM,com.juse.emulator.devices.RAM32Device
device=ROM,com.juse.emulator.devices.ROMDevice
device=DISPLAY,com.juse.emulator.devices.DisplayDevice

####################################################
#         Default Address Bus Device Class
# Any address not mapped to another device 
# is handled by this symbolic device
####################################################

default=RAM

####################################################
#           IRQ Handler Definition Class
####################################################

irqbus=com.juse.emulator.devices.BusIRQImpl

####################################################
#            System Bus Device List
#
# Device arguments are per device, but typically   
# the first argument is the device start address
#
# Format:
# bus=SYMBOLIC_NAME,DEVICE_ARG_1,...,DEVICE_ARG_N
# 
####################################################

bus=RAM,0x00000000,65535
bus=ROM,0x00000000,file://./demo/z80/memtest.bin
bus=DISPLAY,0x000A000,80,25

####################################################
#           Bus Expansion Slots (NOT IMPL)
####################################################

path=c:\downloads\slots
```

## Controls
- C - Toggle Clock
- Space - Pulse Clock
- H/J - Decrement/Increment RAM Page
- K/L - Decrement/Increment ROM Page
- R - Reset
- P - Reset CPU
- S - Toggle Slow Clock
- I - Toggle Interrupt Disable
- Arrows Navigate
- CTL-E Enter/Exit Edit Mode
- CTL-D Enter/Exit Debug Mode
- Enter 
     - Edit Mode, selects and accepts value 
     - Debug Mode, selects break address
- Cursors(Wheel) Instruction Scroll History
- < & > Default Reset Address 
   




#

## Screenshots
![M6502 Emulating](screenshots/M6502.png?raw=true)

![Z80 Emulating](screenshots/z80ui.png?raw=true)

![Screenshot 0](screenshots/screenshot0.png?raw=true)
![Screenshot 1](screenshots/screenshot1.png?raw=true)
![Screenshot 2](screenshots/screenshot2.png?raw=true)
![Text Display](screenshots/display.png?raw=true)
![Text New Font Display](screenshots/font.png?raw=true)
![Graphics Display](screenshots/gfx.png?raw=true)
![Configuration](screenshots/cfg.png?raw=true)
![C64 test](screenshots/c64.png?raw=true)
![Z80 Emulating](screenshots/z80ui.png?raw=true)
![Edit Memory](screenshots/edit.png?raw=true)