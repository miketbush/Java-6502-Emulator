package com.juse.emulator.interfaces;
public interface BusExListener extends BusListener
{
	void readListener(long address, IOSize size); 	
	void writeListener(long address, long data, IOSize size);
}
