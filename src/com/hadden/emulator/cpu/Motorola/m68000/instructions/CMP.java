package com.hadden.emulator.cpu.Motorola.m68000.instructions;

import com.hadden.emulator.cpu.Motorola.m68000.*;
/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008-2010, Tony Headford
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//  following conditions are met:
//
//    o  Redistributions of source code must retain the above copyright notice, this list of conditions and the
//       following disclaimer.
//    o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
//       following disclaimer in the documentation and/or other materials provided with the distribution.
//    o  Neither the name of the M68k Project nor the names of its contributors may be used to endorse or promote
//       products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
*/

public class CMP implements InstructionHandler
{
	protected final Cpu cpu;

	public CMP(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base;
		Instruction i;

		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				base = 0xb000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmp_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				base = 0xb040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmp_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				base = 0xb080;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmp_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}

			for(int ea_mode = 0; ea_mode < 8; ea_mode++)
			{
				if(ea_mode == 1 && sz == 0)
					continue;

				for(int ea_reg = 0; ea_reg < 8; ea_reg++)
				{
					if(ea_mode == 7 && ea_reg > 4)
						break;
					for(int r = 0; r < 8; r++)
					{
						is.addInstruction(base + (r << 9) + (ea_mode << 3) + ea_reg, i);
					}
				}
			}
		}
	}

	protected final int cmp_byte(int opcode)
	{
		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);

		int s = op.getByte();
		int d = cpu.getDataRegisterByte((opcode >> 9) & 0x07);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Byte);

		return 4 + op.getTiming();
	}

	protected final int cmp_word(int opcode)
	{
		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);

		int s = op.getWord();
		int d = cpu.getDataRegisterWord((opcode >> 9) & 0x07);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Word);

		return 4 + op.getTiming();
	}

	protected final int cmp_long(int opcode)
	{
		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);

		int s = op.getLong();
		int d = cpu.getDataRegisterLong((opcode >> 9) & 0x07);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Long);

		return 6 + op.getTiming();
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "cmp" + sz.ext(), src, dst);
	}
}
