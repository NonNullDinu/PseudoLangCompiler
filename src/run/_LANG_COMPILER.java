/*
 * Copyright (C) 2018-2019  Dinu Blanovschi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package run;

import lang.*;
import lang.exceptions.InvalidExpressionException;
import lang.exceptions.ParsingError;
import lang.exceptions.TokenException;
import statements.*;
import tokens.*;
import tree.Statements;
import variables.DATA_TYPE;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class _LANG_COMPILER {
	private static final int OPTMAX = 32;
	public static int strCode = 0;
	private static final String functions_code = "print_char:\n" +
			"\tpush rax\n" +
			"\tmov ecx, eax\n" +
			"\tmov eax, 4\n" +
			"\tmov ebx, r8d\n" +
			"\tmov edx, 1\n" +
			"\tint 0x80\n" +
			"\tpop rax\n" +
			"\tret\n" +
			"printNumber:\n" +
			"\tpush rax\n" +
			"\tpush rdx\n" +
			"\txor edx,edx\n" +
			"\tdiv dword[const10]\n" +
			"\ttest eax,eax\n" +
			"\tje .l1\n" +
			"\tcall printNumber\n" +
			".l1:\n" +
			"\tlea eax, [digits+edx]\n" +
			"\tcall print_char\n" +
			"\tpop rdx\n" +
			"\tpop rax\n" +
			"\tret\n" +
			"printNewLine:\n" +
			"\tmov eax, 4\n" +
			"\tmov ebx, 1\n" +
			"\tmov ecx, new_line\n" +
			"\tmov edx, 1\n" +
			"\tint 0x80\n" +
			"\tret\n" +
			"readValue:\n" +
			"\tmov r11, QWORD[INTERNAL____READ_PTR]\n" +
			"\tmov r12, QWORD[INTERNAL____READ_PTR+8]\n" +
			"\tcmp r11, r12\n" +
			"\tjl .l4\n" +
			"\tmov eax, 3\n" +
			"\tmov ebx, 0\n" +
			"\tmov ecx, INTERNAL____READ\n" +
			"\tmov edx, 65536\n" +
			"\tint 0x80\n" +
			"\tmov ebx, eax\n" +
			"\tsub ebx, 1\n" +
			"\tmov QWORD[INTERNAL____READ_PTR+8], rbx//NO_DELETE\n" +
			"\tmov r10, 0\n" +
			"\tjmp .l5\n" +
			".l4:\n" +
			"\tmov r10, QWORD[INTERNAL____READ_PTR]\n" +
			"\tmov rbx, QWORD[INTERNAL____READ_PTR+8]\n" +
			".l5:\n" +
			"\tmov rax, 0\n" +
			".l2:\n" +
			"\tmovzx rcx, BYTE [INTERNAL____READ + r10]\n" +
			"\tcmp rcx, 32\n" +
			"\tje .l3\n" +
			"\tcmp rcx, 10\n" +
			"\tje .l3\n" +
			"\tsub rcx, '0'\n" +
			"\tINC r10\n" +
			"\tmul DWORD [const10]\n" +
			"\tadd rax, rcx\n" +
			"\tCMP r10d, ebx\n" +
			"\tJL .l2\n" +
			".l3:\n" +
			"\tadd r10, 1\n" +
			"\tmov QWORD[INTERNAL____READ_PTR], r10//NO_DELETE\n" +
			"\tret\n" +
			"\n";
	private static final List<VAR_> vars = new ArrayList<>();
	private static final List<VAR_> dataVars = new ArrayList<>();
	private static int tg = 1;
	private static int cond_code = 0;
	private static String program_file_name;
	private static String target_binary_file;
	private static StringBuilder parsed_src;
	private static Statement[] statements;
	private static StringBuilder assembly;
	public static int fileCode = 0;
	private static String asm_source_file;
	private static final Pattern register = Pattern.compile("rax%eax%ax%al%rcx%ecx%cx%cl%rdx%edx%dx%dl%rbx%ebx%bx%bl%rsi%esi%si%sil%rdi%edi%di%dil%rsp%esp%sp%spl%rbp%ebp%bp%bpl%r8%r8d%r8w%r8b%r9%r9d%r9w%r9b%r10%r10d%r10w%r10b%r11%r11d%r11w%r11b%r12%r12d%r12w%r12b%r13%r13d%r13w%r13b%r14%r14d%r14w%r14b%r15%r15d%r15w%r15b".replaceAll("%", "|"));
	private static int internal_cache_index = 0;
	private static int cache_ptr = 0;
	public static int rec_ind = 0;
	private static Map<String, Method> methods = new HashMap<>();
	private static Map<String, String> localvars = new HashMap<>();
	private static Map<String, Boolean> registers_constant = new HashMap<>();
	private static Map<String, Integer> registers_values = new HashMap<>();
	private static Map<String, REGISTER> registerMap = new HashMap<>();
	private static String regs = "rax%eax%ax%al%rcx%ecx%cx%cl%rdx%edx%dx%dl%rbx%ebx%bx%bl%rsi%esi%si%sil%rdi%edi%di%dil%rsp%esp%sp%spl%rbp%ebp%bp%bpl%r8%r8d%r8w%r8b%r9%r9d%r9w%r9b%r10%r10d%r10w%r10b%r11%r11d%r11w%r11b%r12%r12d%r12w%r12b%r13%r13d%r13w%r13b%r14%r14d%r14w%r14b%r15%r15d%r15w%r15b".replaceAll("%", " ");
	private static List<REGISTER_ADDRESSING_SET> registerList = new ArrayList<>();
	private static Map<String, Boolean> memory_constant = new HashMap<>();
	private static Map<String, Integer> memory_values = new HashMap<>();
	private static List<OptimizationStrategy> optimizationStrategies = new ArrayList<>();
	private static Map<String, Boolean> register_required = new HashMap<>();
	private static Map<String, Boolean> memory_required = new HashMap<>();

	public static void addNewVar(String name, String value) {
		dataVars.add(new VAR_(name, DATA_TYPE.STRING, value));
	}

	private static void tokenizeProgram() {
		statements = getStatements(parsed_src.toString());
	}

	public static void addNewVar(String name, byte[] value) {
		StringBuilder content = new StringBuilder();
		for (byte b : value)
			content.append(Byte.toUnsignedInt(b)).append(", ");
		dataVars.add(new VAR_(name, DATA_TYPE.STRING, content.substring(0, content.length() - 2)));
	}

	private static String jumpFalseLabel;
	private static String jumpTrueLabel;
	private static String nl = "\n";
	private static String nlr = "\\n";

	static {
		for (String reg : regs.split(" ")) {
			int sz;
			switch (reg) {
				case "rax":
				case "rbx":
				case "rcx":
				case "rdx":
				case "r8":
				case "r9":
				case "r10":
				case "r11":
				case "r12":
				case "r13":
				case "r14":
				case "r15":
				case "rsi":
				case "rdi":
				case "rsp":
				case "rbp":
					sz = 8;
					break;
				case "eax":
				case "ebx":
				case "ecx":
				case "edx":
				case "r8d":
				case "r9d":
				case "r10d":
				case "r11d":
				case "r12d":
				case "r13d":
				case "r14d":
				case "r15d":
				case "esi":
				case "edi":
				case "esp":
				case "ebp":
					sz = 4;
					break;
				case "ax":
				case "bx":
				case "cx":
				case "dx":
				case "r8w":
				case "r9w":
				case "r10w":
				case "r11w":
				case "r12w":
				case "r13w":
				case "r14w":
				case "r15w":
				case "si":
				case "di":
				case "sp":
				case "bp":
					sz = 2;
					break;
				case "al":
				case "bl":
				case "cl":
				case "dl":
				case "r8b":
				case "r9b":
				case "r10b":
				case "r11b":
				case "r12b":
				case "r13b":
				case "r14b":
				case "r15b":
				case "sil":
				case "dil":
				case "spl":
				case "bpl":
					sz = 1;
					break;
				default:
					sz = 0;
					break;
			}
			registerMap.put(reg, new REGISTER(sz, reg));
			registers_values.put(reg, 0);
			registers_constant.put(reg, false);
			register_required.put(reg, false);
		}
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rax"), reg("eax"), reg("ax"), reg("al")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rbx"), reg("ebx"), reg("bx"), reg("bl")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rcx"), reg("ecx"), reg("cx"), reg("cl")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rdx"), reg("edx"), reg("dx"), reg("dl")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rsi"), reg("esi"), reg("si"), reg("sil")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rdi"), reg("edi"), reg("di"), reg("dil")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rsp"), reg("esp"), reg("sp"), reg("spl")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("rbp"), reg("ebp"), reg("bp"), reg("bpl")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r8"), reg("r8d"), reg("r8w"), reg("r8b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r9"), reg("r9d"), reg("r9w"), reg("r9b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r10"), reg("r10d"), reg("r10w"), reg("r10b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r11"), reg("r11d"), reg("r11w"), reg("r11b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r12"), reg("r12d"), reg("r12w"), reg("r12b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r13"), reg("r13d"), reg("r13w"), reg("r13b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r14"), reg("r14d"), reg("r14w"), reg("r14b")));
		registerList.add(new REGISTER_ADDRESSING_SET(reg("r15"), reg("r15d"), reg("r15w"), reg("r15b")));

		//PREPARE FOR OPTIMIZATIONS
		optimizationStrategies.add(new OptimizationStrategy("(BYTE|WORD|DWORD|QWORD)\\[(.*)]", "$1 [$2]"));

		optimizationStrategies.add(new OptimizationStrategy("mov (.*), (.*)" + nlr + "push \\1", "push $2"));//REPLACE MOV a,b PUSH a with PUSH b
		optimizationStrategies.add(new OptimizationStrategy("mov (.*), (.*)" + nlr + "mov \\2, \\1", "mov $1, $2"));//REPLACE MOV a,b MOV b,a with MOV a,b
		optimizationStrategies.add(new OptimizationStrategy("mov r10, (.*)" + nlr + "mov r11, (.*)" + nlr + "cmp r10, r11", "mov r10, $1" + nl + "cmp r10, $2"));
		optimizationStrategies.add(new OptimizationStrategy("mov r10, (BYTE|WORD|DWORD|QWORD) \\[(.*)]" + nlr + "(add|sub|shl|shr) r10, (\\d+)" + nlr + "mov \\1 \\[\\2], r10", "$3 $1 [$2], $4" + nl + "mov r10, $1 [$2]"));
	}

	public static REGISTER reg(String name) {
		return registerMap.get(name);
	}

	private static void readProgram() {
		String program;
		try (FileInputStream fin = new FileInputStream(program_file_name)) {
			program = new String(fin.readAllBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String[] spl = program.split("\n");
		parsed_src = new StringBuilder();
		for (int i = 0; i < spl.length; i++) {
			while (spl[i].startsWith(" ") || spl[i].startsWith("\t"))
				spl[i] = spl[i].substring(1);
			if (spl[i].startsWith("###"))
				continue;
			if (spl[i].contains("###"))
				spl[i] = spl[i].split("###")[0];
			parsed_src.append(spl[i]).append("\n");
		}
		if (!spl[spl.length - 1].startsWith("exit"))
			parsed_src.append("exit 0\n");
	}

	private static void makeAssembly() {
		assembly = new StringBuilder(";DO NOT EDIT\n;THIS FILE IS COMPUTER GENERATED\n;AS A RESULT OF THE COMPILATION OF \"" + program_file_name + "\"\nsection .text\n" + functions_code + "\n\tglobal _start\n_start:\n\tmov QWORD[INTERNAL____READ_PTR], 0\n\tmov QWORD[INTERNAL____READ_PTR+8], 0\n");
		boolean prevdec = true;
		for (Statement statement : statements) {
			switch (statement.type) {
				case VAR_DECLARE:
					if (!prevdec)
						throw new ParsingError("Variable \"" + ((VarDeclare_Statement) statement).name + "\" can only be declared at the beginning");
					VAR_ var = new VAR_(((VarDeclare_Statement) statement).name, ((VarDeclare_Statement) statement).type);
					vars.add(var);
					IdentifierToken.identifiers.forEach((IdentifierToken id) -> {
						if (id.var == null && var.name.equals(id.identifier)) {
							id.var = var;
							id.data_type = var.type;
						}
					});
					memory_values.put(var.name, 0);
					memory_constant.put(var.name, false);
					memory_required.put(var.name, true);
					break;
				case VAR_UPDATE:
					rec_ind = 0;
					assembly.append(valueInstructions(((VarUpdate_Statement) statement).value));
					assembly.append("\tmov QWORD [").append(((VarUpdate_Statement) statement).name).append("], r10\n");
					break;
				case WHILE_LOOP:
					int a = tg++;
					jumpTrueLabel = ".WHILE_" + a;
					jumpFalseLabel = ".WHILE_" + a + "_END";
					assembly.append(conditional(((WhileLoop) statement).conditionTokens));
					assembly.append("\tCMP r10, 0\n\tJE .WHILE_").append(a).append("_END\n");
					assembly.append(".WHILE_").append(a).append(":\n").append(assemblyInstructions(new Statements(((WhileLoop) statement).statements.statements), new HashMap<>()));
					assembly.append(conditional(((WhileLoop) statement).conditionTokens));
					assembly.append("\tCMP r10, 0\n\tJNE .WHILE_").append(a).append("\n");
					assembly.append(".WHILE_").append(a).append("_END:\n");
					prevdec = false;
					break;
				case INCREMENT: {
					if (((Increment_Statement) statement).dt == null) {
						DATA_TYPE trg = null;
						for (VAR_ v : vars)
							if (v.name.equals(((Increment_Statement) statement).name)) {
								trg = v.type;
								break;
							}
						if (trg == null)
							throw new ParsingError("Variable \"" + ((Increment_Statement) statement).name + "\" does not exist");
						((Increment_Statement) statement).dt = trg;
					}
					assembly.append("\tINC ").append(((Increment_Statement) statement).dt.wrdtype).append("[").append(((Increment_Statement) statement).name).append("]\n");
					prevdec = false;
					break;
				}
				case METHOD_CALL:
					assembly.append(((MethodCallStatement) statement).assembly());
					break;
				case CONDITIONAL:
					cond_code++;
					int cnd = cond_code;
					if (((Conditional) statement).onFalse != null) {
						jumpFalseLabel = ".COND_" + cnd + "_FALSE";
					} else jumpFalseLabel = ".COND_" + cnd + "_FINAL_END";
					jumpTrueLabel = ".COND_" + cnd + "_TRUE";
					assembly.append(conditional(((Conditional) statement).condition)).append("\nCMP r10, 0\n\tJE .COND_").append(cond_code).append(((Conditional) statement).onFalse != null ? "_FALSE" : "_FINAL_END").append("\n");
					assembly.append(";.COND_").append(cnd).append("_TRUE:\n").append(assemblyInstructions(((Conditional) statement).onTrue, new HashMap<>()));
					if (((Conditional) statement).onFalse != null) {
						assembly.append("\n\tJMP .COND_").append(cnd).append("_FINAL_END\n").append(".COND_").append(cnd).append("_FALSE:\n").append(assemblyInstructions(((Conditional) statement).onFalse, new HashMap<>()));
					}
					assembly.append(".COND_").append(cnd).append("_FINAL_END:\n");
					prevdec = false;
					break;
			}
			rec_ind = 0;
		}
		StringBuilder asm_vars = new StringBuilder("section .bss\n\tINTERNAL____READ RESB 65536\n");
		asm_vars.append("\tINTERNAL____CACHE RESQ 65536\n");//INTERNAL____CACHE
		asm_vars.append("\tINTERNAL____READ_PTR RESQ 2\n");//INTERNAL____CACHE
		for (VAR_ var : vars) {
			asm_vars.append("\t").append(var.name).append(" ").append(var.type.asm_type).append(" 1\n");
		}
		assembly = new StringBuilder(asm_vars + "\n\n" + assembly.toString());
		asm_vars = new StringBuilder("\n\nsection .rodata\n\tconst10 dd 10\n\tdigits db 48,49,50,51,52,53,54,55,56,57\n\tnew_line DB 10\n\t___end DB \"Process finished with exit code \"\n\t___end_len equ $-___end\n");
		for (VAR_ var : dataVars) {
			asm_vars.append("\t").append(var.name).append(" DB ").append(var.value).append("\n");
		}
		assembly.append(asm_vars);
		assembly = new StringBuilder(assembly.toString().replaceAll("\\t", ""));
	}

	private static String conditional(Token[] conditionTokens) {
		return valueInstructions(conditionTokens);
	}

	private static String assemblyInstructions(Statements statements, Map<String, VAR_> localvars) {
		StringBuilder asm = new StringBuilder();
		boolean prevdec = true;
		Map<String, VAR_> localvars_ = new HashMap<>(localvars);
		for (Statement statement : statements) {
			switch (statement.type) {
				case VAR_DECLARE: {
					if (!prevdec)
						throw new ParsingError("Variable \"" + ((VarDeclare_Statement) statement).name + "\" can only be declared at the beginning");
					VAR_ var = new VAR_(((VarDeclare_Statement) statement).name, ((VarDeclare_Statement) statement).type);
					vars.add(var);
					IdentifierToken.identifiers.forEach((IdentifierToken id) -> {
						if (id.var == null && var.name.equals(id.identifier)) {
							id.var = var;
							id.data_type = var.type;
						}
					});
					break;
				}
				case VAR_UPDATE:
					rec_ind = 0;
					asm.append(valueInstructions(((VarUpdate_Statement) statement).value));
					String name = ((VarUpdate_Statement) statement).name;
					boolean ptr = false;
					if (name.startsWith("*")) {
						name = name.substring(1);
						ptr = true;
					}
					if (((VarUpdate_Statement) statement).dt == null) {
						DATA_TYPE trg = null;
						for (VAR_ v : vars) {
							if (v.name.equals(name)) {
								trg = v.type;
								break;
							}
						}
						if (trg == null)
							throw new ParsingError("Variable " + name + " does not exist");
						((VarUpdate_Statement) statement).dt = trg;
					}
					if (localvars_.containsKey(name))
						name = _LANG_COMPILER.localvars.get(name);
					if (ptr) {
						asm.append("\tmov r11, ").append(((VarUpdate_Statement) statement).dt.wrdtype).append(" [").append(name).append("]\n\tmov [r11], r10//POINTER\n");
					} else
						asm.append("\tmov ").append(((VarUpdate_Statement) statement).dt.wrdtype).append(" [").append(name).append("], r10\n");
					break;
				case WHILE_LOOP:
					int a = tg++;
					jumpTrueLabel = ".WHILE_" + a;
					jumpFalseLabel = ".WHILE_" + a + "_END";
					asm.append(conditional(((WhileLoop) statement).conditionTokens));
					asm.append("\tCMP r10, 0\n\tJE .WHILE_").append(a).append("_END\n");
					asm.append(".WHILE_").append(a).append(":\n").append(assemblyInstructions(new Statements(((WhileLoop) statement).statements.statements), new HashMap<>()));
					asm.append(conditional(((WhileLoop) statement).conditionTokens));
					asm.append("\tCMP r10, 0\n\tJNE .WHILE_").append(a).append("\n");
					asm.append(".WHILE_").append(a).append("_END:\n");
					prevdec = false;
					break;
				case INCREMENT: {
					if (((Increment_Statement) statement).dt == null) {
						DATA_TYPE trg = null;
						for (VAR_ v : vars)
							if (v.name.equals(((Increment_Statement) statement).name)) {
								trg = v.type;
								break;
							}
						if (trg == null)
							throw new ParsingError("Variable \"" + ((Increment_Statement) statement).name + "\" does not exist");
						((Increment_Statement) statement).dt = trg;
					}
					asm.append("\tINC ").append(((Increment_Statement) statement).dt.wrdtype).append(" [").append(((Increment_Statement) statement).name).append("]\n");
					prevdec = false;
					break;
				}
				case METHOD_CALL:
					asm.append(((MethodCallStatement) statement).assembly());
					break;
				case CONDITIONAL:
					cond_code++;
					int cnd = cond_code;
					if (((Conditional) statement).onFalse != null) {
						jumpFalseLabel = ".COND_" + cnd + "_FALSE";
					} else jumpFalseLabel = ".COND_" + cnd + "_FINAL_END";
					jumpTrueLabel = ".COND_" + cnd + "_TRUE";
					asm.append(conditional(((Conditional) statement).condition)).append("\nCMP r10, 0\n\tJE .COND_").append(cond_code).append(((Conditional) statement).onFalse != null ? "_FALSE" : "_FINAL_END").append("\n");
					asm.append(";.COND_").append(cnd).append("_TRUE:\n").append(assemblyInstructions(((Conditional) statement).onTrue, localvars_));
					if (((Conditional) statement).onFalse != null) {
						asm.append("\n\tJMP .COND_").append(cnd).append("_FINAL_END\n").append(".COND_").append(cnd).append("_FALSE:\n").append(assemblyInstructions(((Conditional) statement).onFalse, localvars_));
					}
					asm.append(".COND_").append(cnd).append("_FINAL_END:\n");
					prevdec = false;
					break;
			}
			rec_ind = 0;
		}
		return asm.toString();
	}

	public static String valueInstructions(Token[] valueTokens) {
		if (rec_ind == 0) {
			internal_cache_index = 0;
			cache_ptr = 0;
		}
		int depth = rec_ind++;
		boolean constant = true;
		for (int i = 0; i < valueTokens.length && constant; i++)
			constant = !(valueTokens[i] instanceof IdentifierToken || valueTokens[i] instanceof INTERNAL____CACHE_TOKEN);
		if (constant) {
			return "mov " + (depth == 0 ? "r10" : "QWORD [INTERNAL____CACHE + " + (8 * (cache_ptr = internal_cache_index++)) + "]") + ", " + Objects.requireNonNull(evaluate(valueTokens)).vi + "\n";
		}

		if (valueTokens.length == 1) {
			return "\tmov r10, " + value(valueTokens[0]) + "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * (cache_ptr = internal_cache_index++)) + "], r10\n";
		} else if (valueTokens.length == 3) {
			return "\tmov r10, " + value(valueTokens[0]) + "\n\tmov r11, " + value(valueTokens[2]) + "\n\t" + ((OperatorToken) valueTokens[1]).asm_code("r10", "r11") + "\n" + (depth == 0 ? "" : "\tmov QWORD [INTERNAL____CACHE + " + (8 * (cache_ptr = internal_cache_index++)) + "], r10\n");
		} else {
			int i;
			int cnt = 0;
			for (i = 0; i < valueTokens.length; i++) {
				if (valueTokens[i] instanceof ParenthesisOpenedToken)
					cnt++;
				else if (valueTokens[i] instanceof ParenthesisClosedToken) {
					cnt--;
					if (cnt < 0)
						break;
				}
			}
			if (cnt != 0)
				throw new InvalidExpressionException(Arrays.deepToString(valueTokens));
			String asm = "";

			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof ParenthesisClosedToken) {
					int d_ = 1, j;
					for (j = i - 1; j >= 0; j--) {
						if (valueTokens[j] instanceof ParenthesisOpenedToken) {
							--d_;
							if (d_ == 0)
								break;
						} else if (valueTokens[j] instanceof ParenthesisClosedToken)
							d_++;
					}
					if (d_ != 0) throw new InvalidExpressionException(Arrays.toString(valueTokens));
					Token[] t = new Token[i - j - 1];
					System.arraycopy(valueTokens, j + 1, t, 0, i - j - 1);
					asm += "\t" + valueInstructions(t) + "\n\t";
					t = new Token[valueTokens.length - i + j];
					System.arraycopy(valueTokens, 0, t, 0, j);
					System.arraycopy(valueTokens, i + 1, t, j + 1, valueTokens.length - i - 1);
					t[j] = new INTERNAL____CACHE_TOKEN(cache_ptr);
					valueTokens = t;
					i = j - 1;
				}
			}

			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case LOGIC_AND:
						case LOGIC_OR:
						case LOGIC_XOR:
							Token[] tokens1 = new Token[i];
							Token[] tokens2 = new Token[valueTokens.length - i - 1];
							System.arraycopy(valueTokens, 0, tokens1, 0, i);
							System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
							int a;
							asm += valueInstructions(tokens1);
							a = 8 * cache_ptr;
							if (depth == 0) {
								if (((OperatorToken) valueTokens[i]).mop == OperatorToken.Math_Operator.LOGIC_AND) {
									asm += "\tCMP QWORD [INTERNAL____CACHE + " + a + "], 0\n\tJE " + jumpFalseLabel + "\n";
								} else if (((OperatorToken) valueTokens[i]).mop == OperatorToken.Math_Operator.LOGIC_OR) {
									asm += "\tCMP QWORD [INTERNAL____CACHE + " + a + "], 0\n\tJNE " + jumpTrueLabel + "\n";
								}
							}
							asm += valueInstructions(tokens2);
							asm += "\tmov r10, QWORD [INTERNAL____CACHE + " + a + "]\n";
							asm += "\tmov r11, QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "]\n";
							asm += ((OperatorToken) valueTokens[i]).asm_code("r10", "r11");
							if (depth != 0)
								asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
							return asm;
					}
			}

			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case LOGIC_E:
						case LOGIC_G:
						case LOGIC_GE:
						case LOGIC_NE:
						case LOGIC_S:
						case LOGIC_SE:
							Token[] tokens1 = new Token[i];
							Token[] tokens2 = new Token[valueTokens.length - i - 1];
							System.arraycopy(valueTokens, 0, tokens1, 0, i);
							System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
							int a;
							asm += valueInstructions(tokens1);
							a = 8 * cache_ptr;
							asm += valueInstructions(tokens2);
							asm += "\tmov r10, QWORD [INTERNAL____CACHE + " + a + "]\n";
							asm += "\tmov r11, QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "]\n";
							asm += ((OperatorToken) valueTokens[i]).asm_code("r10", "r11");
							if (depth != 0)
								asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
							return asm;
					}
			}
			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case SUBTRACT:
						case ADD:
							Token[] tokens1 = new Token[i];
							Token[] tokens2 = new Token[valueTokens.length - i - 1];
							System.arraycopy(valueTokens, 0, tokens1, 0, i);
							System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
							int a;
							asm += valueInstructions(tokens1);
							a = 8 * cache_ptr;
							asm += valueInstructions(tokens2);
							asm += "\tmov r10, QWORD [INTERNAL____CACHE + " + a + "]\n";
							asm += "\tmov r11, QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "]\n";
							asm += ((OperatorToken) valueTokens[i]).asm_code("r10", "r11");
							if (depth != 0)
								asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
							return asm;
					}
			}
			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case DIVIDE:
						case MULTIPLY:
							Token[] tokens1 = new Token[i];
							Token[] tokens2 = new Token[valueTokens.length - i - 1];
							System.arraycopy(valueTokens, 0, tokens1, 0, i);
							System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
							int a;
							asm += valueInstructions(tokens1);
							a = 8 * cache_ptr;
							asm += valueInstructions(tokens2);
							asm += "\tmov r10, QWORD [INTERNAL____CACHE + " + a + "]\n";
							asm += "\tmov r11, QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "]\n";
							asm += ((OperatorToken) valueTokens[i]).asm_code("r10", "r11");
							if (depth != 0)
								asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
							return asm;
					}
			}
			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case SHIFT_LEFT:
						case SHIFT_RIGHT:
						case BITWISE_AND:
						case BITWISE_OR:
						case BITWISE_XOR:
							Token[] tokens1 = new Token[i];
							Token[] tokens2 = new Token[valueTokens.length - i - 1];
							System.arraycopy(valueTokens, 0, tokens1, 0, i);
							System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
							int a;
							asm += valueInstructions(tokens1);
							a = 8 * cache_ptr;
							asm += valueInstructions(tokens2);
							asm += "\tmov r10, QWORD [INTERNAL____CACHE + " + a + "]\n";
							asm += "\tmov r11, QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "]\n";
							asm += ((OperatorToken) valueTokens[i]).asm_code("r10", "r11");
							if (depth != 0)
								asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
							return asm;
					}
			}
			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof UnaryOperatorToken) {
					Token[] tokens2 = new Token[valueTokens.length - i - 1];
					System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
					int a;
					asm += valueInstructions(tokens2);
					asm += ((UnaryOperatorToken) valueTokens[i]).asm_code("r10");
					if (depth != 0)
						asm += "\n\tmov QWORD [INTERNAL____CACHE + " + (8 * cache_ptr) + "], r10\n";
					return asm;
				}
			}
			return "\n";
		}
	}

	private static void compileAssembly() {
		try (FileOutputStream fout = new FileOutputStream(asm_source_file)) {
			fout.write(assembly.toString().getBytes());
			fout.close();
			Process p = Runtime.getRuntime().exec(new String[]{"nasm", "-f", "elf64", asm_source_file, "-o", "pseudo.o"});
			p.waitFor();
			if (p.exitValue() != 0) {
				InputStream inr = p.getErrorStream();
				String msg = new String(inr.readAllBytes());
				System.err.println(msg);
				System.exit(p.exitValue());
			}
			p = Runtime.getRuntime().exec(new String[]{"ld", "-o", target_binary_file, "pseudo.o"});
			p.waitFor();
			if (p.exitValue() != 0) {
				InputStream inr = p.getErrorStream();
				String msg = new String(inr.readAllBytes());
				System.err.println(msg);
				System.exit(p.exitValue());
			}
			p = Runtime.getRuntime().exec(new String[]{"rm", "pseudo.o"});
			p.waitFor();
			if (p.exitValue() != 0) {
				InputStream inr = p.getErrorStream();
				String msg = new String(inr.readAllBytes());
				System.err.println(msg);
				System.exit(p.exitValue());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static String value(Token token) {
		if (token instanceof NumberToken)
			return Integer.toString(((NumberToken) token).v);
		else if (token instanceof IdentifierToken) {
			IdentifierToken idf = ((IdentifierToken) token);
			return idf.data_type.wrdtype + "[" + idf.identifier + "]";
		} else if (token instanceof INTERNAL____CACHE_TOKEN)
			return "QWORD [INTERNAL____CACHE + " + (((INTERNAL____CACHE_TOKEN) token).qwordoffset * 8) + "]";
		else if (token instanceof LogicConstantValueToken)
			return ((LogicConstantValueToken) token).v ? "1" : "0";
		else return "";
	}

	public static void main(String[] args) {
		program_file_name = "pseudo.psl";
		asm_source_file = "pseudo.asm";
		target_binary_file = "pseudo";
		readProgram();
		tokenizeProgram();
		makeAssembly();
		optimizeAssembly();
		compileAssembly();
	}

	private static void optimizeAssembly() {
		System.out.println(assembly.toString());
		StringBuilder optimized = new StringBuilder();
		List<ASMOP> OPERATIONS = new ArrayList<>();
		boolean isCode = false;
		for (String LINE : assembly.toString().split("\n")) {
			while (LINE.startsWith(" ") || LINE.startsWith("\t"))
				LINE = LINE.substring(1);
			if (isCode) {
				if (LINE.startsWith("section ")) {
					isCode = false;
					optimized.append(LINE).append("\n");
					continue;
				}
				if (!LINE.isEmpty())
					OPERATIONS.add(operation(LINE));
			} else if (!LINE.equals("section .text"))
				optimized.append(LINE).append("\n");
			if (!isCode)
				isCode = LINE.equals("section .text");
		}
		optimized.append("section .text\n");
		for (int i = 0; i < OPERATIONS.size(); i++) {
			ASMOP asmop = OPERATIONS.get(i);
			if (asmop.isLabel) {
				for (String reg : regs.split(" ")) {
					registers_constant.replace(reg, false);
				}
				for (String mem : memory_constant.keySet()) {
					memory_constant.replace(mem, false);
				}
			} else if (asmop.arg2 != null) {
				OPERAND operand = asmop.arg2;
				if (asmop.OP.matches("^mov(zx|sb)?$")) {
					operand.value_is_constant = isConstant(operand.value);
					if (operand.value_is_constant) {
						operand.value = cvalue(operand.value);
						setConstant(asmop.arg1.value, true, Integer.parseInt(operand.value));
					} else setConstant(asmop.arg1.value, false, 0);
				} else if (asmop.OP.matches("^lea$")) {
					setConstant(asmop.arg1.value, false, 0);
					setConstant(asmop.arg2.value, false, 0); // ASSUME IT IS GOING TO BE MODIFIED
				} else if (asmop.OP.matches("^(add|sub)$")) {
					operand.value_is_constant = isConstant(operand.value);
					if (operand.value_is_constant) {
						operand.value = cvalue(asmop.arg2.value);
						int a = Integer.parseInt(operand.value);
						boolean firstIsConstant = isConstant(asmop.arg1.value);
						setConstant(asmop.arg1.value, firstIsConstant, firstIsConstant ? (Integer.parseInt(cvalue(asmop.arg1.value)) + (asmop.OP.equals("add") ? a : (-a))) : 0);
					} else setConstant(asmop.arg1.value, false, 0);
				} else if (asmop.OP.matches("^(or|xor|and|shl|shr)$")) {
					operand.value_is_constant = isConstant(operand.value);
					if (operand.value_is_constant) {
						operand.value = cvalue(asmop.arg2.value);
					}
					setConstant(asmop.arg1.value, false, 0);
				} else if (asmop.OP.equals("cmp")) {
					operand.value_is_constant = isConstant(operand.value);
					if (operand.value_is_constant) {
						operand.value = cvalue(asmop.arg2.value);
					}
				}
			} else { // 1 or 0 args
				if (asmop.OP.equals("int") && asmop.arg1.value.equals("0x80")) {
					setConstant("rax", false, 0);
				} else if (asmop.OP.matches("^(mul|div)$")) {
					setConstant("rax", false, 0);
					setConstant("rdx", false, 0);
				} else if (asmop.OP.equals("call")) {
					switch (asmop.arg1.value) {
						case "readValue":
							setConstant("rax", false, 0);
							break;
					}
				}
			}
		}
		for (int i = OPERATIONS.size() - 1; i >= 0; i--) {
			ASMOP op = OPERATIONS.get(i);
			if (op.isLabel || op.isJump) {
				for (String reg : register_required.keySet())
					register_required.replace(reg, true);
				for (String memloc : memory_required.keySet())
					memory_required.replace(memloc, true);
				continue;
			}
			if (op.OP.matches("^mov(zx|sb)?$")) {
				if ((op.comment == null || !(op.comment.equals("POINTER") || op.comment.equals("NO_DELETE"))) && !isRequired(op.arg1.value)) {
					OPERATIONS.remove(i);
					continue;
				}
				setrequired(op.arg1.value, false);
				setrequired(op.arg2.value, true);
			} else if (op.OP.equals("lea")) {
				if ((op.comment == null || !(op.comment.equals("POINTER") || op.comment.equals("NO_DELETE"))) && !isRequired(op.arg1.value)) {
					OPERATIONS.remove(i);
					continue;
				}
				setrequired(op.arg1.value, false);
				setrequired(op.arg2.value, true);
			} else if ((op.OP.equals("int") && op.arg1.value.equals("0x80")) || op.OP.equals("syscall")) {
				setrequiredreg("rax", true);
				setrequiredreg("rbx", true);
				setrequiredreg("rcx", true);
				setrequiredreg("rdx", true);
			} else if (op.OP.equals("div")) {
				setrequiredreg("rax", true);
				setrequiredreg("rdx", true);
				setrequired(op.arg1.value, true);
			} else if (op.OP.equals("mul")) {
				setrequiredreg("rax", true);
				setrequired(op.arg1.value, true);
			} else if (op.OP.equals("test")) {
				setrequiredreg(op.arg1.value, true);
				setrequiredreg(op.arg2.value, true);
			} else if (op.OP.equals("call")) {
				switch (op.arg1.value) {
					case "print_char":
					case "printNumber":
						setrequiredreg("rax", true);
						setrequiredreg("r8", true);
						break;
					case "readValue":
						break;
				}
			} else if (op.OP.equals("push")) {
				setrequired(op.arg1.value, true);
			} else if (op.OP.equals("pop")) {
				setrequired(op.arg1.value, false);
			} else if (op.OP.equals("cmp")) {
				setrequired(op.arg1.value, true);
				setrequired(op.arg2.value, true);
			} else if (op.OP.matches("^(add|sub|and|or|xor|shl|shr)$")) {
				setrequired(op.arg1.value, true);
				setrequired(op.arg2.value, true);
			}
		}
		for (int opt = 0; opt < OPTMAX; opt++)
			for (int i = OPERATIONS.size() - 1; i >= 2; i--) {
				ASMOP op = OPERATIONS.get(i);
				ASMOP op1 = OPERATIONS.get(i - 1);
				ASMOP op2 = OPERATIONS.get(i - 2);
				if (op.isLabel || op1.isLabel || op2.isLabel)
					continue;
				if (op.OP.equals("pop") && op1.OP.equals("push")) {
					if (op.arg1.value.equals(op1.arg1.value)) {
						OPERATIONS.remove(i);
						OPERATIONS.remove(i - 1);
					}
				}
				if (op.OP.equals("mov") && op1.OP.equals("mov") && op2.OP.equals("cmp")) {
					boolean rem_a = false, rem_b = false;
					if (op.arg1.value.equals(op2.arg1.value)) {
						op2.arg1.value = op.arg2.value;
						rem_a = true;
					}
					if (op1.arg1.value.equals(op2.arg2.value)) {
						op2.arg2.value = op1.arg2.value;
						rem_b = true;
					}
					if (rem_b) OPERATIONS.remove(i - 1);
					if (rem_a) OPERATIONS.remove(i - 2);
				}
			}

		for (ASMOP op : OPERATIONS) {
			optimized.append(op.OP);
			if (op.arg1 != null) optimized.append(' ').append(op.arg1.value);
			if (op.arg2 != null) optimized.append(", ").append(op.arg2.value);
			optimized.append(nl);
		}
		String optstr = optimized.toString();
		for (OptimizationStrategy strategy : optimizationStrategies) {
			optstr = optstr.replaceAll(strategy.instructions, strategy.optimized);
		}

		optstr = optstr.replaceAll(nlr, "\n");

		optimized = new StringBuilder(optstr);

		assembly = optimized;

		{
			StringBuilder asm = new StringBuilder();
			for (String line : assembly.toString().split("\n")) {
				if (!(line.startsWith("\t") || line.contains(":") || line.startsWith("section .")))
					asm.append("\t").append(line).append("\n");
				else if (!line.matches("^\\s*$")) asm.append(line).append("\n");
			}
			assembly = asm;
		}
	}

	private static void setrequiredreg(String name, boolean required) {
		REGISTER_ADDRESSING_SET ras = reg(name).addressing;
		register_required.replace(ras.x64.name, required);
		register_required.replace(ras.x32.name, required);
		register_required.replace(ras.x16.name, required);
		register_required.replace(ras.x8.name, required);
	}

	private static boolean isRequired(String name) {
		if (register.matcher(name).matches()) {
			return register_required.get(name);
		} else if (name.matches("^.*\\[.*].*$")) {
			String n = name.substring(name.indexOf('[') + 1, name.indexOf(']'));
			return (memory_required.containsKey(n) && memory_required.get(n));
		}
		return false;
	}

	private static void setrequired(String name, boolean required) {
		if (register.matcher(name).matches()) {
			setrequiredreg(name, required);
		} else if (name.matches("^.*\\[.*].*$")) {
			String n = name.substring(name.indexOf('[') + 1, name.indexOf(']'));
			for (String reg : regs.split(" "))
				if (n.matches("^.*" + reg + ".*$"))
					setrequiredreg(reg, true);
			memory_required.put(n, required);
		}
	}

	private static void setConstant(String name, boolean constant, int val) {
		if (register.matcher(name).matches()) {
			REGISTER_ADDRESSING_SET ras = reg(name).addressing;
			registers_constant.put(ras.x64.name, constant);
			registers_constant.put(ras.x32.name, constant);
			registers_constant.put(ras.x16.name, constant);
			registers_constant.put(ras.x8.name, constant);
			registers_values.put(ras.x64.name, val);
			registers_values.put(ras.x32.name, val);
			registers_values.put(ras.x16.name, val);
			registers_values.put(ras.x8.name, val);
		} else {
			//MEMORY
			if (name.matches(".*\\[.*]"))
				name = name.substring(name.indexOf('[') + 1, name.indexOf(']'));
			memory_constant.put(name, constant);
			memory_values.put(name, val);
		}
	}

	private static ASMOP operation(String line) {
		if (line.contains(":") || !line.contains(" "))
			return new ASMOP(line, null, null);
		String opcode = line.substring(0, line.indexOf(' ')).toLowerCase();
		String argsfull = line.substring(line.indexOf(' ') + 1);
		String comment = null;
		if (argsfull.contains("//")) {
			String[] parts = argsfull.split("//");
			comment = parts[1];
			argsfull = parts[0];
		}
		String[] args = argsfull.split("\\s*,\\s*");
		if (args.length == 2)
			return new ASMOP(opcode, new OPERAND(args[0]), new OPERAND(args[1])).withComment(comment);
		else if (args.length == 1)
			return new ASMOP(opcode, new OPERAND(args[0]), null).withComment(comment);
		else return new ASMOP(opcode, null, null).withComment(comment);
	}

	private static Statement[] getStatements(String lines) throws TokenException, ParsingError {
		Token[] tokens = tokenize(lines);
		int first_type_token_ind = -2, last_type_token_ind = -2;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] == null)
				continue;
			if (tokens[i] instanceof TypeToken) {
				if (last_type_token_ind == i - 1) {
					if (tokens[first_type_token_ind] instanceof CompositeTypeToken) {
						((CompositeTypeToken) tokens[first_type_token_ind]).tkns.add(((TypeToken) tokens[i]));
						tokens[i] = null;
					} else {
						tokens[first_type_token_ind] = new CompositeTypeToken(((TypeToken) tokens[first_type_token_ind]), ((TypeToken) tokens[i]));
						tokens[i] = null;
					}
				} else first_type_token_ind = i;
				last_type_token_ind = i;
			}
		}
		for (int i = tokens.length - 2; i >= 0; i--) {
			if (tokens[i] instanceof NewLineToken && tokens[i + 1] instanceof NewLineToken)
				tokens[i + 1] = null;
		}
		int l = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i] != null)
				l++;
		Token[] tk = new Token[l];
		int ptr = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i] != null)
				tk[ptr++] = tokens[i];
		tokens = tk;
		System.out.println(Arrays.deepToString(tokens));
		Statement[] statements = parse(tokens);
		return statements;
	}

	private static Token[] tokenize(String lines) {
		List<Token> tokens = new ArrayList<>();
		while (!lines.isBlank()) {
			String t = nextToken(lines);
			Token tk = getToken(t);
			if (tk == null)
				System.out.println(lines);
			lines = lines.substring(lines.indexOf(t) + t.length());
			tokens.add(tk);
		}
		return tokens.toArray(new Token[0]);
	}

	private static Statement[] parse(Token[] tokens) {
		List<Statement> statements = new ArrayList<>();
		IndObj ind = new IndObj();
		while (ind.ind < tokens.length) {
			Statement[] s = getStatement(tokens, ind);
			if (s != null)
				statements.addAll(Arrays.asList(s));
		}
		return statements.toArray(new Statement[0]);
	}

	private static Statement[] getStatement(Token[] t, IndObj ind) {
		while (t[ind.ind] instanceof NewLineToken) {
			ind.ind++;
			if (ind.ind == t.length)
				return new Statement[]{};
		}
		Statement_TYPE st = getFirstStatementType(t, ind.ind);
		if (st == null && t[ind.ind] instanceof CompositeInstructionBeginToken)
			return nextInstruction(t, ind).statements;
		if (st == null)
			throw new ParsingError("INVALID STATEMENT:" + t[ind.ind].toString() + " " + t[ind.ind + 1].toString());
		switch (st) {
			case VAR_DECLARE: {
				VarDeclare_Statement vardecl = new VarDeclare_Statement(((IdentifierToken) t[ind.ind + 1]).identifier, t[ind.ind] instanceof CompositeTypeToken ? ((CompositeTypeToken) t[ind.ind]).data_type() : ((TypeToken) t[ind.ind]).data_type());
				if (t[ind.ind + 2] instanceof AssignmentToken) {
					System.out.println("DECLARATION AND ASSIGNMENT");
					int i = ind.ind + 1, j;
					while (!(t[i] instanceof AssignmentToken))
						i++;
					i++;
					for (j = i + 1; j < t.length && !(t[j] instanceof NewLineToken); j++) ;
					Token[] valtkns = new Token[j - i];
					System.out.println("i:" + i + " j:" + j);
					System.arraycopy(t, i, valtkns, 0, valtkns.length);
					Statement[] arr = new Statement[]{vardecl, new VarUpdate_Statement(((IdentifierToken) t[ind.ind + 1]).identifier, valtkns)};
					ind.ind = j + 1;
					return arr;
				} else {
					ind.ind += 3;
					return new Statement[]{vardecl};
				}
			}
			case INCREMENT: {
				ind.ind += 3;
				return new Statement[]{new Increment_Statement(((IdentifierToken) t[ind.ind]).identifier)};
			}
			case CONDITIONAL: {
				int i = ind.ind + 2, j, d = 1;
				for (j = i + 1; j < t.length; j++) {
					if (t[j] instanceof ParenthesisOpenedToken)
						d++;
					else if (t[j] instanceof ParenthesisClosedToken) {
						d--;
						if (d == 0)
							break;
					}
				}
				if (d != 0)
					throw new ParsingError("Invalid parenthesis");
				Token[] condition = new Token[j - i];
				System.arraycopy(t, i, condition, 0, condition.length);
				IndObj indObj = new IndObj();
				while (!(t[j] instanceof ThenToken))
					j++;
				indObj.ind = j + 1;
				Statements onTrue = nextInstruction(t, indObj), onFalse = null;
				while (t[indObj.ind] instanceof NewLineToken)
					indObj.ind++;
				if (t[indObj.ind] instanceof ElseToken) {
					indObj.ind++;
					onFalse = nextInstruction(t, indObj);
				}
				ind.ind = indObj.ind;
				return new Statement[]{new Conditional(condition, onTrue, onFalse)};
			}
			case VAR_UPDATE: {
				int i = ind.ind, j;
				while (!(t[i] instanceof AssignmentToken))
					i++;
				i++;
				for (j = i; j < t.length && !(t[j] instanceof NewLineToken); j++) ;
				Token[] valtkns = new Token[j - i];
				System.arraycopy(t, i, valtkns, 0, valtkns.length);
				Statement[] arr;
				arr = new Statement[]{new VarUpdate_Statement(((IdentifierToken) t[ind.ind]).identifier, valtkns)};
				ind.ind = j + 1;
				return arr;
			}
			case WHILE_LOOP: {
				int i = ind.ind + 2, j, d = 1;
				for (j = i + 1; j < t.length; j++) {
					if (t[j] instanceof ParenthesisOpenedToken)
						d++;
					else if (t[j] instanceof ParenthesisClosedToken) {
						d--;
						if (d == 0)
							break;
					}
				}
				if (d != 0)
					throw new ParsingError("Invalid parenthesis");
				Token[] condition = new Token[j - i];
				System.arraycopy(t, i, condition, 0, condition.length);
				IndObj indObj = new IndObj();
				indObj.ind = j + 2;
				Statements repeat = nextInstruction(t, indObj);
				ind.ind = indObj.ind;
				return new Statement[]{new WhileLoop(condition, repeat)};
			}
			case METHOD_CALL: {
				String methodName = ((IdentifierToken) t[ind.ind]).identifier.toUpperCase();
				METHOD m = methods.containsKey(methodName) ? METHOD.DEFINED_METHOD : METHOD.valueOf(methodName);
				Method def_m = methods.get(methodName);
				Token[][] params = callParameterTokens(t, ind);
				MethodCallStatement mcs = new MethodCallStatement(m, params);
				mcs.def_m = def_m;
				return new Statement[]{mcs};
			}
		}
		return null;
	}

	private static Statements nextInstruction(Token[] t, IndObj indObj) {
		if (t[indObj.ind] instanceof CompositeInstructionBeginToken) {
			int i = indObj.ind + 1, j, d = 1;
			for (j = i; j < t.length; j++) {
				if (t[j] instanceof CompositeInstructionBeginToken) {
					d++;
				} else if (t[j] instanceof CompositeInstructionEndToken) {
					d--;
					if (d == 0)
						break;
				}
			}
			if (d != 0)
				throw new ParsingError("Expected code block to end");
			Token[] actions = new Token[j - i];
			System.arraycopy(t, i, actions, 0, actions.length);
			indObj.ind = j + 1;
			return new Statements(parse(actions));
		} else {
			int i = indObj.ind, j;
			for (j = i; j < t.length && !(t[j] instanceof NewLineToken); j++) ;
			Token[] actions = new Token[j - i];
			System.arraycopy(t, i, actions, 0, actions.length);
			indObj.ind = j + 1;
			return new Statements(parse(actions));
		}
	}

	private static Token[][] callParameterTokens(Token[] t, IndObj ind) {
		int i = ind.ind + 1, j;
		int args = 1;
		for (j = i; j < t.length; j++) {
			if (t[j] instanceof CommaToken)
				args++;
			else if (t[j] instanceof NewLineToken)
				break;
		}
		if (j == i)
			return null;
		Token[][] tokens = new Token[args][];
		int ptr = i;
		for (int k = 0; k < args; k++) {
			int ptr_b = ptr;
			for (; ptr < j; ptr++) {
				if (t[ptr] instanceof CommaToken) {
					break;
				}
			}
			Token[] tkn = new Token[ptr - ptr_b];
			ptr++;
			System.arraycopy(t, ptr_b, tkn, 0, tkn.length);
			tokens[k] = tkn;
		}
		ind.ind = j + 1;
		return tokens;
	}

	private static Statement_TYPE getFirstStatementType(Token[] t, int ind) {
		for (Statement_TYPE st : Statement_TYPE.values())
			if (st.fits(t, ind))
				return st;
		return null;
	}

	private static Token getToken(String value) {
		if (value.equals("&&")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_AND);
		else if (value.equals("||")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_OR);
		else if (value.equals("^")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_XOR);
		else if (value.equals("+")) return new OperatorToken(OperatorToken.Math_Operator.ADD);
		else if (value.equals("-")) return new OperatorToken(OperatorToken.Math_Operator.SUBTRACT);
		else if (value.equals("*")) return new OperatorToken(OperatorToken.Math_Operator.MULTIPLY);
		else if (value.equals("/") || value.equals("div")) return new OperatorToken(OperatorToken.Math_Operator.DIVIDE);
		else if (value.equals("%") || value.equals("mod")) return new OperatorToken(OperatorToken.Math_Operator.MODULO);
		else if (value.equals("==")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_E);
		else if (value.equals(">=")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_GE);
		else if (value.equals(">")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_G);
		else if (value.equals("<=")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_SE);
		else if (value.equals("<")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_S);
		else if (value.equals("<<")) return new OperatorToken(OperatorToken.Math_Operator.SHIFT_LEFT);
		else if (value.equals(">>")) return new OperatorToken(OperatorToken.Math_Operator.SHIFT_RIGHT);
		else if (value.equals("!=") || value.equals("<>"))
			return new OperatorToken(OperatorToken.Math_Operator.LOGIC_NE);
		else if (value.equals("(")) return new ParenthesisOpenedToken();
		else if (value.equals(")")) return new ParenthesisClosedToken();
		else if (value.matches("^(int|float|intreg|real)$")) return new TypeToken(value);
		else if (value.equals("if") || value.equals("daca")) return new IfToken();
		else if (value.equals("then") || value.equals("atunci")) return new ThenToken();
		else if (value.equals("while") || value.equals("cat timp")) return new WhileToken();
		else if (value.equals("for") || value.equals("pentru")) return new ForToken();
		else if (value.equals("=") || value.equals("<-")) return new AssignmentToken();
		else if (value.equals("{") || value.equals("begin") || value.equals("inceput"))
			return new CompositeInstructionBeginToken();
		else if (value.equals("}") || value.equals("end") || value.equals("sfarsit"))
			return new CompositeInstructionEndToken();
		else if (value.matches("^\\d+$")) return new NumberToken(Integer.parseInt(value));
		else if (value.startsWith("\"") && value.endsWith("\"")) return new StringToken(value);
		else if (value.equals(",")) return new CommaToken();
		else if (value.equals("else") || value.equals("altfel")) return new ElseToken();
		else if (value.equals("do") || value.equals("executa")) return new DoToken();
		else if (value.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) return new IdentifierToken(value, null, null);
		else if (value.equals("\n")) return new NewLineToken();
		else if (value.equals("!")) return new UnaryOperatorToken(UnaryOperatorToken.OP.LOGIC_NOT);
		return null;
	}

	private static boolean isDigit(char c) {
		return c >= 48 && c <= 57;
	}

	private static String nextToken(String s) {
		while (s.startsWith(" ") || s.startsWith("\t"))
			s = s.substring(1);
		if (s.startsWith("\n"))
			return "\n";
		if (s.matches("^([(+\\-*%/)]|&&|and|si|\\|\\||or|sau|\\^|==|>=|>|<=|!=|!|<>|<<|>>)(.|\\n)*$")) {
			if (s.matches("^(&&|\\|\\||==|>=|<=|!=|<>|<<|>>)(.|\\n)*$"))
				return s.substring(0, 2);
			else if (s.startsWith("and"))
				return "and";
			else if (s.startsWith("si"))
				return "si";
			else if (s.startsWith("or"))
				return "or";
			else if (s.startsWith("sau"))
				return "sau";
			else
				return s.substring(0, 1);
		}
		if (s.matches("^\\d+(.|\\n)*$")) {
			int i;
			char c = s.charAt(0);
			for (i = 1; i < s.length() && isDigit(c); i++) {
				c = s.charAt(i);
			}
			return s.substring(0, i - 1);
		}
		if (s.startsWith("("))
			return "(";
		if (s.startsWith(")"))
			return ")";
		if (s.startsWith(","))
			return ",";
		if (s.startsWith("float"))
			return "float";
		if (s.startsWith("real"))
			return "real";
		if (s.startsWith("intreg"))
			return "intreg";
		if (s.startsWith("int"))
			return "int";
		if (s.startsWith("if"))
			return "if";
		if (s.startsWith("daca"))
			return "daca";
		if (s.startsWith("while"))
			return "while";
		if (s.startsWith("cat timp"))
			return "cat timp";
		if (s.startsWith("for"))
			return "for";
		if (s.startsWith("pentru"))
			return "pentru";
		if (s.startsWith("do"))
			return "do";
		if (s.startsWith("executa"))
			return "executa";
		if (s.startsWith("="))
			return "=";
		if (s.startsWith("<-"))
			return "<-";
		if (s.startsWith("<"))
			return "<";
		if (s.startsWith("{"))
			return "{";
		if (s.startsWith("inceput"))
			return "inceput";
		if (s.startsWith("begin"))
			return "begin";
		if (s.startsWith("}"))
			return "}";
		if (s.startsWith("sfarsit"))
			return "sfarsit";
		if (s.startsWith("end"))
			return "end";
		if (s.startsWith("\"")) {
			int i;
			for (i = 1; i < s.length() && s.charAt(i) != '"'; i++) ;
			return s.substring(0, i + 1);
		}
		if (s.startsWith("else"))
			return "else";
		if (s.startsWith("altfel"))
			return "altfel";
		if (s.matches("^[a-zA-Z_][a-zA-Z0-9_]*(.|\\n)*$")) {
			int i;
			char c = s.charAt(0);
			for (i = 1; i < s.length() && (Character.isLetterOrDigit(c) || c == '_'); i++) {
				c = s.charAt(i);
			}
			return s.substring(0, i - 1);
		}
		return null;
	}

	private static class IndObj {
		int ind = 0;
	}

	public static String printIdentifier(Token token) {
		if (token instanceof IdentifierToken) {
			return "\tmov rax, [" + ((IdentifierToken) token).identifier + "]\n\tcall printNumber\n\tcall printNewLine\n";
		} else return "\n";
	}

	public static void addNewRESWVar(String name) {
		vars.add(new VAR_(name, DATA_TYPE.SHORT_INT));
	}

	public static boolean isConstant(String value) {
		if (register.matcher(value).matches())
			return registers_constant.get(value);
		if (value.matches("^.*\\[.*].*$")) {
			String key = value.substring(value.indexOf('[') + 1, value.indexOf(']'));
			return !value.matches("^.*(" + regs.replaceAll(" ", "|") + ").*$") &&
					memory_constant.containsKey(key) && memory_constant.get(key);
		}
		return value.matches("^\\d+$");
	}

	public static String cvalue(String name) {
		if (name.matches("^\\d+$"))
			return name;
		if (register.matcher(name).matches())
			return Integer.toString(registers_values.get(name));
		return Integer.toString(memory_values.get(name.matches(".*\\[.*]") ? name.substring(name.indexOf('[') + 1, name.indexOf(']')) : name));
	}

	private static Value evaluate(Token[] valueTokens) {
		int i;
		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof ParenthesisClosedToken) {
				int d_ = 1, j;
				for (j = i - 1; j >= 0; j--) {
					if (valueTokens[j] instanceof ParenthesisOpenedToken) {
						--d_;
						if (d_ == 0)
							break;
					} else if (valueTokens[j] instanceof ParenthesisClosedToken)
						d_++;
				}
				Token[] t = new Token[i - j - 1];
				System.arraycopy(valueTokens, j + 1, t, 0, i - j - 1);
				Value pv = evaluate(t);
				t = new Token[valueTokens.length - i + j];
				System.arraycopy(valueTokens, 0, t, 0, j);
				System.arraycopy(valueTokens, i + 1, t, j + 1, valueTokens.length - i - 1);
				t[j] = new NumberToken(pv.vi);
				valueTokens = t;
				i = j - 1;
			}
		}

		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof OperatorToken)
				switch (((OperatorToken) valueTokens[i]).mop) {
					case LOGIC_AND:
					case LOGIC_OR:
					case LOGIC_XOR:
						Token[] tokens1 = new Token[i];
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, 0, tokens1, 0, i);
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						Value va = evaluate(tokens1);
						Value vb = evaluate(tokens2);
						return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
				}
		}

		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof OperatorToken)
				switch (((OperatorToken) valueTokens[i]).mop) {
					case LOGIC_E:
					case LOGIC_G:
					case LOGIC_GE:
					case LOGIC_NE:
					case LOGIC_S:
					case LOGIC_SE:
						Token[] tokens1 = new Token[i];
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, 0, tokens1, 0, i);
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						Value va = evaluate(tokens1);
						Value vb = evaluate(tokens2);
						return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
				}
		}
		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof OperatorToken)
				switch (((OperatorToken) valueTokens[i]).mop) {
					case SUBTRACT:
					case ADD:
						Token[] tokens1 = new Token[i];
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, 0, tokens1, 0, i);
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						Value va = evaluate(tokens1);
						Value vb = evaluate(tokens2);
						return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
				}
		}
		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof OperatorToken)
				switch (((OperatorToken) valueTokens[i]).mop) {
					case DIVIDE:
					case MULTIPLY:
						Token[] tokens1 = new Token[i];
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, 0, tokens1, 0, i);
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						Value va = evaluate(tokens1);
						Value vb = evaluate(tokens2);
						return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
				}
		}
		for (i = valueTokens.length - 1; i >= 0; i--) {
			if (valueTokens[i] instanceof OperatorToken)
				switch (((OperatorToken) valueTokens[i]).mop) {
					case SHIFT_LEFT:
					case SHIFT_RIGHT:
					case BITWISE_AND:
					case BITWISE_OR:
					case BITWISE_XOR:
						Token[] tokens1 = new Token[i];
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, 0, tokens1, 0, i);
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						Value va = evaluate(tokens1);
						Value vb = evaluate(tokens2);
						return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
				}
		}
		if (valueTokens.length == 0)
			return null;
		if (valueTokens[0] instanceof StringToken)
			return new Value(((StringToken) valueTokens[0]).str);
		else
			return new Value(valueTokens[0] instanceof NumberToken ? ((NumberToken) valueTokens[0]).v : (((LogicConstantValueToken) valueTokens[0]).v ? 1 : 0));
	}

	private static class INTERNAL____CACHE_TOKEN extends Token {
		int qwordoffset;

		INTERNAL____CACHE_TOKEN(int qwordoffset) {
			this.qwordoffset = qwordoffset;
		}

		@Override
		public String toString() {
			return "ICT(" + qwordoffset + ")";
		}
	}

	public static class VAR_ {
		private String value;
		private final String name;
		private final DATA_TYPE type;

		VAR_(String name, DATA_TYPE type) {
			this.name = name;
			this.type = type;
		}

		VAR_(String name, DATA_TYPE type, String value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}
	}
}