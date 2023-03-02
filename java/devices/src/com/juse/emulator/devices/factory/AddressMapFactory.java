package com.juse.emulator.devices.factory;

import com.juse.emulator.devices.AddressMap16Impl;
import com.juse.emulator.devices.AddressMapImpl;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.IOSize;

public class AddressMapFactory
{
	private AddressMapFactory()
	{}
	
	static public AddressMap createAddressMap(IOSize size)
	{
		if(IOSize.IO16Bit == size)
		{
			return new AddressMap16Impl();
		}
		return new AddressMapImpl();
	}
}
