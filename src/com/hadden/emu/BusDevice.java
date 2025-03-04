package com.hadden.emu;




public interface BusDevice
{
	String getName();
	BusAddressRange getBusAddressRange();
	void writeAddress(int address, int value, IOSize size);
	int  readAddressSigned(int address, IOSize size);
	int  readAddressUnsigned(int address, IOSize size);
	void reset();
}
