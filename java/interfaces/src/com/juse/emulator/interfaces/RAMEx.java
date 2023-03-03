package com.juse.emulator.interfaces;

public interface RAMEx extends RAM
{
	int readValue(int address, IOSize size);
	void writeValue(int address, int value, IOSize size);
	public String getRAMString(int startAddress, int stopAddress);
}
