package com.juse.emulator.devices;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Vector;


import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.BusAccessor;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusEx;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.BusListener;
import com.juse.emulator.interfaces.BusReader;
import com.juse.emulator.interfaces.BusWriter;
import com.juse.emulator.interfaces.Detachable;
import com.juse.emulator.interfaces.HasPorts;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RaisesIRQ;

public class AddressMap16Impl implements BusEx, AddressMap
{
	private NavigableMap<Integer, BusDevice> mappedAddressSpace = new TreeMap<Integer, BusDevice>();

	private BusDevice defaultSpace = null;
	private BusIRQ birq = null;

	private List<BusListener> busListeners = new Vector<BusListener>();

	static protected int asAddress(int addressUknown)
	{
		return (0xFFFFFFFF & addressUknown);
	}
	
	public AddressMap16Impl()
	{
	}	
	
	public void setDefaultDevice(BusDevice bd)
	{
		this.defaultSpace  = bd;		
		mappedAddressSpace.put(defaultSpace.getBusAddressRange().getLowAddress(), defaultSpace);
		mappedAddressSpace.put(defaultSpace.getBusAddressRange().getHighAddress() + 1,defaultSpace);
	}

	public void addBusListener(BusListener bl)
	{
		this.busListeners.add(bl);
	}
	
	public void setIRQHandler(BusIRQ busIRQ)
	{
		this.birq  = busIRQ;		
	}
	
	public AddressMap16Impl(BusDevice bd, BusIRQ birq)
	{
		setDefaultDevice(bd);
		this.birq = birq;
	}

	public AddressMap16Impl addBusDevice(BusDevice bd)
	{
		mappedAddressSpace.put(bd.getBusAddressRange().getLowAddress(), bd);
		mappedAddressSpace.put(bd.getBusAddressRange().getHighAddress() + 1, defaultSpace);
		
		if(bd instanceof RaisesIRQ)
		{
			if(birq!=null)
			{
				((RaisesIRQ)(bd)).attach(this.birq);
			}
		}
		
		if(bd instanceof HasPorts)
		{
			BusDevice[] ports = ((HasPorts)bd).ports(bd.getBusAddressRange().getLowAddress());
			if(ports!=null)
			{
				for(BusDevice port : ports)
				{
					mappedAddressSpace.put(port.getBusAddressRange().getLowAddress(), port);
					mappedAddressSpace.put(port.getBusAddressRange().getHighAddress() + 1, defaultSpace);				
				}
			}
		}
		if(bd instanceof BusAccessor)
		{
			((BusAccessor)bd).setReader(new BusReader() 
			{
				@Override
				public int read(int address)
				{
					return AddressMap16Impl.this.read((short)address);
				}
			} );
			((BusAccessor)bd).setWriter(new BusWriter() 
			{
				@Override
				public void write(int address, int value)
				{
					AddressMap16Impl.this.write((short)(address & 0xFFFF),(byte)(address & 0xFF));
				}				
			});
		}
		
		return this;
	}
	
	public BusDevice getMemoryMappedDevice(int address)
	{
		BusDevice bd = defaultSpace;
		
		if(mappedAddressSpace.floorEntry(address)!=null)
		{
			bd = mappedAddressSpace.floorEntry(address).getValue();
		}
		
		return bd;
	}

	



	@Override
	public byte read(short address)
	{
		int laddr = address;
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			//System.out.println(Integer.toHexString(laddr));
		}
		byte v =  (byte) getMemoryMappedDevice(laddr).readAddressUnsigned(laddr, IOSize.IO8Bit);
		
		if(busListeners!=null && busListeners.size() > 0)
			for(BusListener b : busListeners)
				b.readListener(address);
		
		return v;
	}


	@Override
	public void write(short address, byte data)
	{
		int laddr = address;
		
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			//System.out.println(Integer.toHexString(laddr));
		}		
		
		getMemoryMappedDevice(laddr).writeAddress(laddr, data,IOSize.IO8Bit);		

		if(busListeners!=null && busListeners.size() > 0)
			for(BusListener b : busListeners)
				b.writeListener(address, data);
	}

	@Override
	public int read(int address, IOSize size)
	{
		int laddr = address;
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			//System.out.println(Integer.toHexString(laddr));
		}
		byte v =  (byte) getMemoryMappedDevice(laddr).readAddressUnsigned(laddr, IOSize.IO8Bit);
		
		//if(busListeners!=null && busListeners.size() > 0)
		//	for(BusListener b : busListeners)
		//		b.readListener(address, IOSize.IO8Bit);
		
		return v;
	}

	@Override
	public void write(int address, int data, IOSize size)
	{
		// TODO Auto-generated method stub		
	}	
	
	public void printAddressMap()
	{
		for(Integer adr : this.mappedAddressSpace.keySet())
		{
			String hex = Long.toHexString(adr).toUpperCase();
			
			int hlen = hex.length();
			int nlen = (8 - hlen);
			
			if(nlen > 0)
			{
				for(int i=0;i<nlen;i++)
				{
					hex = "0" + hex;
				}
			}	
			
			hex = hex.substring(0,4) + ":" + hex.substring(4); 
			
			System.out.println("[" + hex + "] " + mappedAddressSpace.get(adr).getName());
			
		}
	}

	public String dumpBytesAsString()
	{
		return toString(8, true);
	}
	
	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		//boolean mode = SystemEmulator.enableDebug(false);
		
		int lineSize = bytesPerLine;
		//System.out.println();
		if(this.defaultSpace!=null)
		{
			BusAddressRange bar = defaultSpace.getBusAddressRange();
			
			for(int bk = bar.getLowAddress(); bk <= bar.getHighAddress(); bk++)
			{
				String hex = Integer.toHexString(this.read((short)bk) & 0xFF).toUpperCase();
				
				int hlen = hex.length();
				int nlen = (2 - hlen);
				
				if(nlen > 0)
				{
					for(int i=0;i<nlen;i++)
					{
						hex = "0" + hex;
					}
				}	
				
				if(addresses && (lineSize == bytesPerLine))
				{
					String hexAddress = Integer.toHexString(bk).toUpperCase(); 
							
					int halen = hexAddress.length();
					int nalen = (4 - halen);
					
					if(nalen > 0)
					{
						for(int i=0;i<nalen;i++)
						{
							hexAddress = "0" + hexAddress;
						}
					}
					
					//System.out.print(hexAddress + ": ");
					sb.append(hexAddress + ": ");
				}				
				
				//System.out.print(hex + " ");
				sb.append(hex);
				
				lineSize--;
				if(lineSize < 1)
				{
					lineSize = bytesPerLine;
					//System.out.println();
					sb.append("\n");
				}
				else
				{
					sb.append(" ");
				}
					
			}
			

			
			//System.out.println("[" + hex + "] " + mappedAddressSpace.get(adr).getName());
			
		}
		
		//SystemEmulator.enableDebug(mode);
		
		return sb.toString();
	}

	@Override
	public Collection<BusDevice> getDevices()
	{
		return this.mappedAddressSpace.values();
	}

	@Override
	public void removeDevices() 
	{
		System.out.println("removeDevices");
		for(BusDevice bd : mappedAddressSpace.values())
			if(bd instanceof Detachable)
				((Detachable)bd).detach();
		
		mappedAddressSpace.clear();		
	}

	@Override
	public void reset()
	{
		if(busListeners!=null && busListeners.size() > 0)
			for(BusListener b : busListeners)
				b.busReset();
	}

	protected static byte[][] createBanks(long allocationSize)
	{
		byte[][] banks;
		long bankCount = 0;
		
		System.out.println("createBanks:" + Long.toHexString(allocationSize));

		int high = (int)((allocationSize & 0xFFFF0000L) >> 16);
		System.out.println("createBanks high:" + high);
		
		if(allocationSize < 0xFFFFL)
		{
			bankCount = 1;
		}
		else
		{
		    int mod = (int) (allocationSize % 0xFFFFL);
			
			bankCount = (allocationSize/0xFFFFL);
			if(mod > 0)
				bankCount++;
		}

		System.out.println("banks:" + bankCount);
		
		banks = new byte[(int)high][0x0000FFFF + 1];
		
		System.out.println("\tbanks high:" + high + " (" + Integer.toHexString(high) + ")");
		System.out.println("\tbanks low :" + 0x0000FFFF  + " (" + Integer.toHexString(0x0000FFFF) + ")");		
		
		
		return banks;
	}
	
	public static void printPaging(long offset)
	{
		int high = (int)((offset & 0xFFFF0000L) >> 16);
		int low = (int)((offset & 0x0000FFFFL));
		
		System.out.println("linear address:" + Long.toHexString(offset));
		System.out.println("\thigh:" + high + " (" + Integer.toHexString(high) + ")");
		System.out.println("\tlow :" + low  + " (" + Integer.toHexString(low) + ")");
	}

	public static void setAddress(byte[][] map, long offset)
	{
		int high = (int)((offset & 0xFFFF0000L) >> 16);
		int low = (int)((offset & 0x0000FFFFL));
		
		System.out.println("linear address:" + Long.toHexString(offset));
		System.out.println("\thigh:" + high + " (" + Integer.toHexString(high) + ")");
		System.out.println("\tlow :" + low  + " (" + Integer.toHexString(low) + ")");
		
		
		map[high][low] = 1;		
	}
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		System.out.println("Max int:" + Integer.MAX_VALUE);
		System.out.println("Max int:" + Long.toHexString(Integer.MAX_VALUE));
		System.out.println("Max Long:" + Long.MAX_VALUE);
		System.out.println("Max Long:" + Long.toHexString(Long.MAX_VALUE));
		
		
		
		long linearAddress = 0x0L;
		printPaging(linearAddress);
		
		linearAddress = 0xFFFFL;
		printPaging(linearAddress);
		
		linearAddress = 0x10000L;
		printPaging(linearAddress);

		linearAddress = 0x10001L;
		printPaging(linearAddress);
		
		linearAddress = 0xFFFFFFL;
		printPaging(linearAddress);
		
		linearAddress = 0x1000000L;
		printPaging(linearAddress);
		
		
		byte[][] addressMap; 
		
		//addressMap = createBanks(0xFFFFFFFFL);
		//addressMap = null;
		//System.gc();
//		addressMap = createBanks(0xFFL);		
//		addressMap = null;
//		System.gc();
//		addressMap = createBanks(0xFFFFL);
//		addressMap = null;
//		System.gc();
//		addressMap = createBanks(0xFFFEL);
//		addressMap = null;
//		System.gc();
		addressMap = createBanks(0xFFFFFFL);
		addressMap = null;
		System.gc();
		addressMap = createBanks(16L * 1024L * 1024L);

		long size = 16L * 1024L * 1024L;
		
		long offset = 0;
		setAddress(addressMap, offset);

		offset = 0xFFFFL;
		setAddress(addressMap, offset);
		
		offset = 0x10000L;
		setAddress(addressMap, offset);
		
		offset = 0xFFFFFFL;
		setAddress(addressMap, offset);

		
		int high = (int)((offset & 0xFFFF0000L) >> 16);
		int low = (int)((offset & 0x0000FFFFL));
		
		
		/*
		for(long i=0;i<=size;i++)
		{
			long offset = i - 1;
			
			int high = (int)((offset & 0xFFFF0000L) >> 16);
			int low = (int)((offset & 0x0000FFFFL));
			
			System.out.println("high:" + high + " (" + Integer.toHexString(high) + ")");
			System.out.println("low :" + low  + " (" + Integer.toHexString(low) + ")");
			
			addressMap[high - 1][low - 1] = (int)i;
		}			
		*/
		
		String addr1 = "FFFFFFFF";
		int intVal = (int)Long.parseLong(addr1, 16);
		System.out.println("intVal:" + intVal);
		System.out.println("intVal:" + Integer.toHexString(intVal));

		
		for(long addr=0;addr<0xffffffff;addr++)
		{
			int currentInt = 0;
			
			
			
		}
		
		
		intVal = 0xffffffff;
		System.out.println("intVal:" + intVal);
		System.out.println("intVal:" + Integer.toHexString(intVal));		
		
		System.out.println((long)intVal);
		System.out.println(Long.toHexString(intVal & 0xFFFFFFFF));
		
		String addr2 = "-1";
		System.out.println(Integer.toHexString(Integer.parseInt(addr2, 10)));
		
		
		System.out.println(AddressMap16Impl.asAddress(0));
		
		
		System.out.println(AddressMap.toHexAddress(15, IOSize.IO8Bit));
		System.out.println(AddressMap.toHexAddress(15, IOSize.IO16Bit));
		System.out.println(AddressMap.toHexAddress(15, IOSize.IO32Bit));
		
		System.out.println(AddressMap.toHexAddress(0xABCD,  IOSize.IO16Bit));
		
		System.out.println(AddressMap.toHexAddress(0xABCDEF,IOSize.IOBig32Bit));
		
		AddressMap16Impl map =  new AddressMap16Impl(new RAMDevice(0x00000000,1024*1024),
				                         new BusIRQ() 
										{
											@Override
											public void raise(int source)
											{
												System.out.println("CPU IRQ");
												
											}
										});


		map.addBusDevice(new ROMDevice(0x00008000))
		   .addBusDevice(new DisplayDevice(0x0000A000,40,10));

		   
		int t = 0;
		for(int i=0;i<1024;i++)
		{
			map.write((short)i, (byte)(t & 0xFF));
			t++;
		}
		map.printAddressMap();
		System.out.println(map.toString(8,true));
		
		BusDevice bd = map.getMemoryMappedDevice(0x00001000);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000A000);
		if(bd!=null)
			System.out.println(bd.getName());
		bd = map.getMemoryMappedDevice(0x0000a18f);
		if(bd!=null)
			System.out.println(bd.getName());

		bd = map.getMemoryMappedDevice(0x0000a190);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000B000);
		if(bd!=null)
			System.out.println(bd.getName());		

		bd = map.getMemoryMappedDevice(0x0000B001);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B003);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B00A);
		if(bd!=null)
			System.out.println(bd.getName());		
		
		int base = 0x0000A000;
		String msg = "Hello World!";
		for(int c=0;c<msg.length();c++)
			map.getMemoryMappedDevice(base).writeAddress(base++, (byte)(int)msg.charAt(c), IOSize.IO8Bit);
		
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x0, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 0xF, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x1, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 'A', IOSize.IO8Bit);
		
	}
	
}
