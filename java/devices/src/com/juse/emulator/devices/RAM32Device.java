package com.juse.emulator.devices;

import com.juse.emulator.util.loaders.ROMLoader;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RAM;
import com.juse.emulator.interfaces.RAMEx;

public class RAM32Device implements BusDevice, RAMEx
{
	private static final int PAGE_SIZE = 0x0000FFFF;
	
	private byte[][] banks;
	private BusAddressRange bar;

	protected byte[][] createBanks(long allocationSize)
	{
		byte[][] banks;
		
		System.out.println("RAM32Device::" + Long.toHexString(allocationSize));
		
		//System.out.println("RAM32Device::" + Long.toHexString(allocationSize));

		int high = (int)((allocationSize & 0xFFFF0000L) >> 16);
		//System.out.println("createBanks high:" + high);
		
		banks = new byte[(int)high][PAGE_SIZE + 1];
		
		//System.out.println("\tbanks high:" + high + " (" + Integer.toHexString(high) + ")");
		//System.out.println("\tbanks low :" + PAGE_SIZE  + " (" + Integer.toHexString(PAGE_SIZE) + ")");		
		
		return banks;
	}	
	
	@Override
	public int readValue(int address, IOSize size)
	{
		int value = 0;
		
		try
		{
			int high = (int)((address & 0xFFFF0000L) >> 16);
			int low  = (int)((address & 0x0000FFFFL));
			
			if(IOSize.IO8Bit == size)
			{
				value = (int)(banks[high][low] & 0xFF);
			}
			else if(IOSize.IO16Bit == size)
			{
				value  = (int)((banks[high][low] & 0xFF));
				value |= (int)((banks[high][low+1] & 0xFF) << 8);
			}
			else if(IOSize.IOBig16Bit == size)
			{
				value = (int)((banks[high][low+1] & 0xFF));
				value |= (int)((banks[high][low] & 0xFF) << 8);
			}		
			else if(IOSize.IO32Bit == size)
			{
				value =  (int)((banks[high][low]   & 0xFF));
				value |= (int)((banks[high][low+1] & 0xFF) << 8);
				value |= (int)((banks[high][low+2] & 0xFF) << 16);
				value |= (int)((banks[high][low+3] & 0xFF) << 24);   
			}
			else if(IOSize.IOBig32Bit == size)
			{
				value  = (int)((banks[high][low]   & 0xFF) << 24);
				value |= (int)((banks[high][low+1] & 0xFF) << 16);
				value |= (int)((banks[high][low+2] & 0xFF) << 8);
				value |= (int)((banks[high][low+3] & 0xFF));
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
		
		return value;		
	}	

	@Override
	public void writeValue(int address, int value, IOSize size)	
	{
		int high = (int)((address & 0xFFFF0000L) >> 16);
		int low = (int)((address & 0x0000FFFFL));
		
		if(IOSize.IO8Bit == size)
		{
			banks[high][low] = (byte)(value & 0xFF);
		}
		else if(IOSize.IO16Bit == size)
		{
			banks[high][low] = (byte)((value & 0x00FF));
			banks[high][low+1] = (byte)((value & 0xFF00) >> 8);
		}
		else if(IOSize.IOBig16Bit == size)
		{
			banks[high][low] = (byte)((value & 0xFF00) >> 8);
			banks[high][low+1] = (byte)((value & 0x00FF));
		}		
		else if(IOSize.IO32Bit == size)
		{
			banks[high][low]   = (byte)((value & 0x000000FF));
			banks[high][low+1] = (byte)((value & 0x0000FF00) >> 8);
			banks[high][low+2] = (byte)((value & 0x00FF0000) >> 16);
			banks[high][low+3] = (byte)((value & 0xFF000000) >> 24);
		}
		else if(IOSize.IOBig32Bit == size)
		{
			banks[high][low]   = (byte)((value & 0xFF000000) >> 24);
			banks[high][low+1] = (byte)((value & 0x00FF0000) >> 16);
			banks[high][low+2] = (byte)((value & 0x0000FF00) >> 8);
			banks[high][low+3] = (byte)((value & 0x000000FF));
		}						
	}	

	
	
	public RAM32Device(BusAddressRange bar)
	{
		int bankSize = bar.getSize();
		this.banks = createBanks(bankSize);
		this.bar = bar;
	}

	public RAM32Device(int bankAddress, int bankSize)
	{
		this(new BusAddressRangeImpl(bankAddress, bankSize, 1));
	}

	
	@Override
	public String getName()
	{
		return "RAM32";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address < 0)
		{
			address = 0xFFFF + address + 1;
			//System.out.print(BusAddressRange.makeHexAddress(address));
		}
		int effectiveAddress = address - this.bar.getLowAddress();
		writeValue(effectiveAddress,(byte) (value & 0x0FF), IOSize.IO8Bit);
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		if(address < 0)
		{
			address = 0xFFFF + address + 1;
			//System.out.print(BusAddressRange.makeHexAddress(address));
		}		
		int effectiveAddress = address - this.bar.getLowAddress();
		return readValue(effectiveAddress, size);
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		if(address < 0)
		{
			address = 0xFFFF + address + 1;
			//System.out.print(BusAddressRange.makeHexAddress(address));
		}		
		int effectiveAddress = address - this.bar.getLowAddress();
		return readValue(effectiveAddress, size);
	}

	public void dumpContents(int max)
	{
		if (max == -1)
		{
			max = bar.getSize() - 1;
		}
		int bytes = 0;
		for (int i = 0; i < max; i++)
		{
			if (bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");

			System.out.print(BusAddressRange.makeHex((byte)readValue(i,IOSize.IO8Bit)));
			System.out.print(" ");
			bytes++;
			if (bytes > 7)
			{
				System.out.println();
				bytes = 0;
			}
		}
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		if (addresses)
			sb.append("0000: ");

		for (int i = 1; i <= bar.getSize(); i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(ROMLoader.byteToHexString((byte)readValue(i - 1, IOSize.IO8Bit)) + " ");
			}
			else
			{
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString((byte)readValue(i - 1, IOSize.IO8Bit)) + "\n");
				if (addresses)
					sb.append(zeroes.substring(0, Math.max(0, 4 - Integer.toHexString(i).length()))
							+ Integer.toHexString(i) + ": ");
			}
		}

		return sb.toString();
	}

	
	public String toString(int bytesPerLine, int startAddress, int stopAddress, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		if (addresses)
			sb.append("0000: ");

		for (int i = startAddress+1; i <= stopAddress+1; i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(ROMLoader.byteToHexString((byte)readValue(i - 1, IOSize.IO8Bit)) + " ");
			}
			else
			{
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString((byte)readValue(i - 1, IOSize.IO8Bit)) + "\n");
				if (addresses)
				{
					if((i+1) < stopAddress)
						sb.append(zeroes.substring(0, Math.max(0, 4 - Integer.toHexString(i).length()))
								+ Integer.toHexString(i) + ": ");
				}
			}
		}

		return sb.toString();
	}	
	
	public static void main(String[] args)
	{
		RAM32Device rd = new RAM32Device(0x00010000, 64 * 1024);
		System.out.println(rd.getName());
		System.out.println(
				rd.getBusAddressRange().getLowAddressHex() + ":" + rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		rd.dumpContents(32);

		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

	}

	@Override
	public void setRAMArray(byte[] array)
	{
		setRAMArray(0,array);	
	}	
	
	@Override
	public void setRAMArray(int base, byte[] array) 
	{
		//this.array = array;
		reset();
		
		for(int p=0;p<array.length;p++)
			writeValue(base + p,array[p], IOSize.IO8Bit);
		
	}

	@Override
	public byte read(short address)
	{
		//System.out.println(AddressMap.toHexAddress(address, IOSize.IO16Bit));
		return (byte)this.readAddressSigned((int)address, IOSize.IO8Bit);
	}

	@Override
	public void write(short address, byte data)
	{
		this.writeValue((int)address, data, IOSize.IO8Bit);		
	}

	@Override
	public String getRAMString()
	{
		return  this.toString(8, true);
	}


	@Override
	public String getRAMString(int startAddress, int stopAddress)
	{
		return this.toString(8, startAddress, stopAddress, true);
	}	
	
	@Override
	public void reset()
	{
		for(int p=0;p<bar.getSize();p++)
			writeValue(p, 0x00, IOSize.IO8Bit);	
	}

}
