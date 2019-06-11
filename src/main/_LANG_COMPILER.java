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

package main;

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
	private static final int OPT_MAX = 32;
	public static int strCode = 0;
	private static final List<VarManager.VAR_> vars = new ArrayList<>();
	private static final List<VarManager.VAR_> dataVars = new ArrayList<>();
	private static int tg = 1;
	private static int cond_code = 0;
	private static String program_file_name;
	private static String target_binary_file;
	private static StringBuilder parsed_src;
	private static Statement[] statements;
	private static StringBuilder assembly;
	public static int fileCode = 0;
	private static String asm_source_file;
	private static final Pattern register = Pattern.compile("^%rax|%eax|%ax|%al|%rcx|%ecx|%cx|%cl|%rdx|%edx|%dx|%dl|%rbx|%ebx|%bx|%bl|%rsi|%esi|%si|%sil|%rdi|%edi|%di|%dil|%rsp|%esp|%sp|%spl|%rbp|%ebp|%bp|%bpl|%r8|%r8d|%r8w|%r8b|%r9|%r9d|%r9w|%r9b|%r10|%r10d|%r10w|%r10b|%r11|%r11d|%r11w|%r11b|%r12|%r12d|%r12w|%r12b|%r13|%r13d|%r13w|%r13b|%r14|%r14d|%r14w|%r14b|%r15|%r15d|%r15w|%r15b$");
	private static int internal_cache_index = 0;
	private static int cache_ptr = 0;
	public static int rec_ind = 0;
	public static Map<String, Integer> var_indices = new HashMap<>();
	private static Map<String, Boolean> registers_constant = new HashMap<>();
	private static Map<String, Integer> registers_values = new HashMap<>();
	private static Map<String, REGISTER> registerMap = new HashMap<>();
	private static String regs = "rax%eax%ax%al%rcx%ecx%cx%cl%rdx%edx%dx%dl%rbx%ebx%bx%bl%rsi%esi%si%sil%rdi%edi%di%dil%rsp%esp%sp%spl%rbp%ebp%bp%bpl%r8%r8d%r8w%r8b%r9%r9d%r9w%r9b%r10%r10d%r10w%r10b%r11%r11d%r11w%r11b%r12%r12d%r12w%r12b%r13%r13d%r13w%r13b%r14%r14d%r14w%r14b%r15%r15d%r15w%r15b".replaceAll("%", " ");
	private static Map<String, Boolean> memory_constant = new HashMap<>();
	private static Map<String, Integer> memory_values = new HashMap<>();
	private static List<OptimizationStrategy> optimizationStrategies = new ArrayList<>();
	private static Map<String, Boolean> register_required = new HashMap<>();
	private static Map<String, Boolean> memory_required = new HashMap<>();
	private static int var_ind = 0;
	private static final String INDEX_REGISTER = "%rdi";
	public static String preparations; // NEEDED FOR WORKING WITH ARRAYS
	private static int alignment = 1;

	public static void addNewVar(String name, String value) {
		dataVars.add(new VarManager.VAR_(name, DATA_TYPE.STRING, value));
	}

	private static final String[] keywords = {
			"while", "for", "do", "if", "then", "else", "begin", "end",
			"repeat", "until", "==", "=", "<-", "<>", "<=", "<<", "<", "{", "}", ",", "float",
			"file_stream", "file", "pointer", "(", ")", "[", "]",
			"-", "+", "*", "%", "/", "&&", "&", "and", "si", "||", "or", "sau", "|",
			"^", ">=", ">>", "!=", "!", ">",
			"daca", "cat timp", "pentru", "inceput", "sfarsit", "execita",
			"atunci", "real", "intreg", "repeta", "pana cand", "int", "true", "false"
	};

	private static String jumpFalseLabel;
	private static String jumpTrueLabel;
	private static String nl = "\n";
	private static String nlr = "\\n";
	private static Map<String, Boolean> method_required = new HashMap<>();

	private static void tokenizeProgram() {
		statements = Parser.getStatements(parsed_src.toString());
	}

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
		new REGISTER_ADDRESSING_SET(reg("rax"), reg("eax"), reg("ax"), reg("al"));
		new REGISTER_ADDRESSING_SET(reg("rbx"), reg("ebx"), reg("bx"), reg("bl"));
		new REGISTER_ADDRESSING_SET(reg("rcx"), reg("ecx"), reg("cx"), reg("cl"));
		new REGISTER_ADDRESSING_SET(reg("rdx"), reg("edx"), reg("dx"), reg("dl"));
		new REGISTER_ADDRESSING_SET(reg("rsi"), reg("esi"), reg("si"), reg("sil"));
		new REGISTER_ADDRESSING_SET(reg("rdi"), reg("edi"), reg("di"), reg("dil"));
		new REGISTER_ADDRESSING_SET(reg("rsp"), reg("esp"), reg("sp"), reg("spl"));
		new REGISTER_ADDRESSING_SET(reg("rbp"), reg("ebp"), reg("bp"), reg("bpl"));
		new REGISTER_ADDRESSING_SET(reg("r8"), reg("r8d"), reg("r8w"), reg("r8b"));
		new REGISTER_ADDRESSING_SET(reg("r9"), reg("r9d"), reg("r9w"), reg("r9b"));
		new REGISTER_ADDRESSING_SET(reg("r10"), reg("r10d"), reg("r10w"), reg("r10b"));
		new REGISTER_ADDRESSING_SET(reg("r11"), reg("r11d"), reg("r11w"), reg("r11b"));
		new REGISTER_ADDRESSING_SET(reg("r12"), reg("r12d"), reg("r12w"), reg("r12b"));
		new REGISTER_ADDRESSING_SET(reg("r13"), reg("r13d"), reg("r13w"), reg("r13b"));
		new REGISTER_ADDRESSING_SET(reg("r14"), reg("r14d"), reg("r14w"), reg("r14b"));
		new REGISTER_ADDRESSING_SET(reg("r15"), reg("r15d"), reg("r15w"), reg("r15b"));
	}

	static {
		optimizationStrategies.add(new OptimizationStrategy("mov(b|w|l|q)\\s+(.*),\\s+(.*)" + nlr + "push\\1\\s+\\3", "push$1 $2"));//REPLACE MOV b,a PUSH a with PUSH b
		optimizationStrategies.add(new OptimizationStrategy("mov(b|w|l|q)\\s+(.*),\\s+(.*)" + nlr + "mov\\1\\s+\\3,\\s+\\2", "mov$1 $2, $3"));//REPLACE MOV a,b MOV b,a with MOV a,b
		optimizationStrategies.add(new OptimizationStrategy("movq\\s+(.*),\\s+%r10" + nlr + "movq\\s+(.*),\\s+%r11" + nlr + "cmp.?\\s+%r11, %r10", "movq $1, %r10" + nl + "cmp $2, %r10"));
		optimizationStrategies.add(new OptimizationStrategy("movq\\s+(\\$\\d+),\\s*%r11" + nlr + "(add|sub)(.?)\\s+\\1,\\s*%r10", "$2$3 $1, %r10"));
		optimizationStrategies.add(new OptimizationStrategy("movq\\s+(.*),\\s+%r10" + nlr + "(add|sub|shl|shr)q\\s+$(\\d+),\\s+%r10" + nlr + "mov.?\\s+%r10,\\s*\\1", "$2 \\$$3, $1" + nl + "movq $1, %r10"));
		optimizationStrategies.add(new OptimizationStrategy("push(.?)\\s+(.*)" + nlr + "pop\\1?\\s+\\2", ""));
		optimizationStrategies.add(new OptimizationStrategy("movq\\s+\\$(\\d+),\\s+%r11" + nlr + "cmpq\\s+\\$\\1,\\s+%r10", "cmpq \\$$1, %r10"));
		optimizationStrategies.add(new OptimizationStrategy("movq\\s+\\$(\\d+),\\s+%r10" + nlr + "movq\\s+(\\$\\1|%r10),\\s+var_(.*)", "movq \\$$1, var_$3"));
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
			if (spl[i].contains("###"))
				spl[i] = spl[i].split("###")[0];
			parsed_src.append(spl[i]).append("\n");
		}
		if (!spl[spl.length - 1].startsWith("exit"))
			parsed_src.append("exit 0\n");
	}

	public static VarManager.VAR_ lookupStringVar(String s) {
		for (VarManager.VAR_ v : dataVars)
			if (v.value.equals(s)) {
				return v;
			}
		return null;
	}

	public static void main(String[] args) {
		program_file_name = "pseudo.psl";
		asm_source_file = "pseudo.asm";
		target_binary_file = "pseudo";
		for (int i = 0; i < args.length; i++)
			switch (args[i]) {
				case "-src":
					i++;
					program_file_name = args[i];
					break;
				case "-o":
					i++;
					target_binary_file = args[i];
					break;
				case "-asm":
					i++;
					asm_source_file = args[i];
					break;
			}
		readProgram();
		tokenizeProgram();
		AssemblyMake.makeAssembly();
		Optimizer.optimizeAssembly();
		AssemblyMake.compileAssembly();
	}

	public static class AssemblyMake {

		private static void appendAssembly(Statement statement, StringBuilder asm) {
			switch (statement.type) {
				case VAR_DECLARE: {
					VarManager.VAR_ var = new VarManager.VAR_(((VarDeclare_Statement) statement).name, ((VarDeclare_Statement) statement).type);
					vars.add(var);
					IdentifierToken.identifiers.forEach((IdentifierToken id) -> {
						if (id.var == null && var.name.equals(id.identifier)) {
							id.var = var;
							id.data_type = var.type;
						}
					});
					String trueName = "var_" + var_ind;
					memory_values.put(trueName, 0);
					memory_constant.put(trueName, false);
					memory_required.put(trueName, true);
					var_indices.put(var.name, var_ind++);
					break;
				}
				case VAR_UPDATE: {
					rec_ind = 0;
					asm.append("CLEAR_CACHE\n");
					asm.append(valueInstructions(((VarUpdate_Statement) statement).value));
					if (((VarUpdate_Statement) statement).name instanceof IdentifierToken) {
						asm.append("movq %r10, var_").append(var_indices.get(((IdentifierToken) ((VarUpdate_Statement) statement).name).identifier)).append('\n');
					} else {
						String v = value(((VarUpdate_Statement) statement).name);
						asm.append("\tmovq %r10, %rax\n").append(preparations).append("\tmovq %rax, ").append(v).append("\n");
					}
					asm.append("CLEAR_CACHE\n");
					break;
				}
				case WHILE_LOOP: {
					int a = tg++;
					jumpTrueLabel = "WHILE_" + a;
					jumpFalseLabel = "WHILE_" + a + "_END";
					asm.append("WHILE_").append(a).append(":\n");
					asm.append("CLEAR_CACHE\n");
					asm.append(conditional(((WhileLoop) statement).conditionTokens));
					asm.append("\tCMPQ $0, %r10\n\tJE WHILE_").append(a).append("_END\n");
					asm.append("CLEAR_CACHE\n");
					asm.append(assemblyInstructions(new Statements(((WhileLoop) statement).statements.statements)));
					asm.append("CLEAR_CACHE\n");
					asm.append("\tJMP WHILE_").append(a).append("\n");
					asm.append("CLEAR_CACHE\n");
					asm.append("WHILE_").append(a).append("_END:\n");
					asm.append("CLEAR_CACHE\n");
					break;
				}
				case FOR_LOOP: {
					int a = tg++;
					jumpTrueLabel = "WHILE_" + a;
					jumpFalseLabel = "WHILE_" + a + "_END";
					asm.append(valueInstructions(((ForLoop) statement).forboundtokens[0])).append("\tmovq %r10, var_").append(var_indices.get(((ForLoop) statement).var.identifier)).append("\n");
					asm.append("WHILE_").append(a).append(":\n");
					asm.append("CLEAR_CACHE\n");
					asm.append(forConditional(((ForLoop) statement).var, ((ForLoop) statement).forboundtokens[1]));
					asm.append("\tCMPQ $0, %r10\n\tJE WHILE_").append(a).append("_END\n");
					asm.append("CLEAR_CACHE\n");
					asm.append(assemblyInstructions(new Statements(((ForLoop) statement).repeat.statements)));
					asm.append("CLEAR_CACHE\n");
					asm.append(valueInstructions(((ForLoop) statement).forboundtokens[2]));
					asm.append("\taddq %r10, var_").append(var_indices.get(((ForLoop) statement).var.identifier)).append("\n");
					asm.append("\tJMP WHILE_").append(a).append("\n");
					asm.append("CLEAR_CACHE\n");
					asm.append("WHILE_").append(a).append("_END:\n");
					asm.append("CLEAR_CACHE\n");
					break;
				}
				case METHOD_CALL:
					asm.append("CLEAR_CACHE\n");
					asm.append(((MethodCallStatement) statement).assembly());
					asm.append("CLEAR_CACHE\n");
					break;
				case CONDITIONAL:
					cond_code++;
					int cnd = cond_code;
					if (((Conditional) statement).onFalse != null) {
						jumpFalseLabel = "COND_" + cnd + "_FALSE";
					} else jumpFalseLabel = "COND_" + cnd + "_FINAL_END";
					jumpTrueLabel = "COND_" + cnd + "_TRUE";
					asm.append("CLEAR_CACHE\n");
					asm.append(conditional(((Conditional) statement).condition)).append("\nCMPQ $0, %r10\n\tJE COND_").append(cond_code).append(((Conditional) statement).onFalse != null ? "_FALSE" : "_FINAL_END").append("\n");
					asm.append("CLEAR_CACHE\n");
					asm.append("COND_").append(cnd).append("_TRUE:\n").append(assemblyInstructions(((Conditional) statement).onTrue));
					if (((Conditional) statement).onFalse != null) {
						asm.append("\n\tJMP COND_").append(cnd).append("_FINAL_END\n");
						asm.append("COND_").append(cnd).append("_FALSE:\n");
						asm.append("CLEAR_CACHE\n");
						asm.append(assemblyInstructions(((Conditional) statement).onFalse));
					}
					asm.append("COND_").append(cnd).append("_FINAL_END:\n");
					asm.append("CLEAR_CACHE\n");
					break;

				case DO_WHILE: {
					int a = tg++;
					jumpTrueLabel = "WHILE_" + a;
					jumpFalseLabel = "WHILE_" + a + "_END";
					asm.append("WHILE_").append(a).append(":\n");
					asm.append("CLEAR_CACHE\n");
					asm.append(assemblyInstructions(new Statements(((DoWhile) statement).repeat.statements)));
					asm.append("CLEAR_CACHE\n");
					rec_ind = 0;
					asm.append(conditional(((DoWhile) statement).condtokens));
					asm.append("\tCMPQ $0, %r10\n\tJNE WHILE_").append(a).append("\n");
					break;
				}
			}
			rec_ind = 0;
		}

		private static void makeAssembly() {
			assembly = new StringBuilder("#DO NOT EDIT\n#THIS FILE IS COMPUTER GENERATED\n#AS A RESULT OF THE COMPILATION OF \"" + program_file_name + "\"\n.section .text\n\t.globl main\nmain:\n\tcall _pseudo_stdlib_init@PLT\n");
			for (Statement statement : statements) {
				appendAssembly(statement, assembly);
			}
			StringBuilder asm_vars = new StringBuilder(".section .bss\n\t.lcomm INTERNAL____CACHE, 524288\n");
			for (VarManager.VAR_ var : vars) {
				asm_vars.append("\t.lcomm var_").append(var_indices.get(var.name)).append("\t, ").append(var.type.bytesize * var.size).append("\n");
			}
			assembly = new StringBuilder(asm_vars + "\n\n" + assembly.toString());
			asm_vars = new StringBuilder("\n\n.section .rodata\n");
			for (VarManager.VAR_ var : dataVars) {
				asm_vars.append("\t").append(var.name).append(":\n\t\t").append(var.value).append("\n");
			}
			assembly.append(asm_vars);
			assembly = new StringBuilder(assembly.toString().replaceAll("\\t", ""));
		}

		private static String requiredFunctions() {
			StringBuilder ext = new StringBuilder();
			for (String s : method_required.keySet())
				if (method_required.get(s)) {
					ext.append("\t\t\t.extern ").append(s).append("\n");
				}
			return ext.toString();
		}

		private static String conditional(Token[] conditionTokens) {
			return valueInstructions(conditionTokens);
		}

		private static String assemblyInstructions(Statements statements) {
			StringBuilder asm = new StringBuilder();
			for (Statement statement : statements) {
				appendAssembly(statement, asm);
			}
			return asm.toString();
		}

		private static String forConditional(IdentifierToken nameToken, Token[] forboundtoken) {
			Token[] tk = new Token[forboundtoken.length + 2];
			tk[0] = new IdentifierToken(nameToken.identifier, nameToken.data_type, nameToken.var);
			tk[1] = new OperatorToken(OperatorToken.Math_Operator.LOGIC_SE);
			System.arraycopy(forboundtoken, 0, tk, 2, forboundtoken.length);
			return valueInstructions(tk);
		}

		public static String valueInstructions(Token[] valueTokens) {
			if (rec_ind == 0) {
				internal_cache_index = 0;
				cache_ptr = 0;
				alignment = 1;
			}
			int depth = rec_ind++;
			boolean constant = isConstant(valueTokens);
			if (constant) {
				if (depth == 0)
					return "\tmovq $" + Objects.requireNonNull(evaluate(valueTokens)).vi + ", %r10\n";
				else
					return "\tmovq $" + Objects.requireNonNull(evaluate(valueTokens)).vi + ", INTERNAL____CACHE+" + (8 * (cache_ptr = internal_cache_index++)) + "\n";
			}

			if (valueTokens.length == 1) {
				preparations = "";
				String v = value(valueTokens[0]);
				return preparations + "\tmovq " + v + ", %r10\n" + (depth != 0 ? "\tmovq %r10, INTERNAL____CACHE+" + (8 * (cache_ptr = internal_cache_index++)) + "\n" : "");
			} else if (valueTokens.length == 3) {
				preparations = "";
				String val = value(valueTokens[0]);
				String asm_ = preparations + "\tmovq " + val + ", %r10\n\tpushq %r10\n";
				preparations = "";
				val = value(valueTokens[2]);
				asm_ += preparations + "\tpopq %r10\n\tmovq " + val + ", %r11\n";
				asm_ += "\t" + ((OperatorToken) valueTokens[1]).asm_code("r10", "r11") + "\n";
				if (depth != 0)
					asm_ += "\tmovq %r10, INTERNAL____CACHE+" + (8 * (cache_ptr = internal_cache_index++)) + "\n";
				return asm_;
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
				StringBuilder asm = new StringBuilder();
				boolean parenthesis = false;
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
						asm.append("\t").append(valueInstructions(t)).append("\n\t");
						t = new Token[valueTokens.length - i + j];
						System.arraycopy(valueTokens, 0, t, 0, j);
						System.arraycopy(valueTokens, i + 1, t, j + 1, valueTokens.length - i - 1);
						t[j] = new INTERNAL____CACHE_TOKEN(cache_ptr);
						valueTokens = t;
						i = j - 1;
						parenthesis = true;
					}
				}
				if (parenthesis)
					if (valueTokens.length == 1) {
						preparations = "";
						String v = value(valueTokens[0]);
						return asm.toString() + preparations + "\tmovq " + v + ", %r10\n" + (depth != 0 ? "\tmovq %r10, INTERNAL____CACHE+" + (8 * (cache_ptr = internal_cache_index++)) + "\n" : "");
					} else if (valueTokens.length == 3) {
						preparations = "";
						String val = value(valueTokens[0]);
						String asm_ = preparations + "\tmovq " + val + ", %r10\n\tpushq %r10\n";
						preparations = "";
						val = value(valueTokens[2]);
						asm_ += preparations + "\tpopq %r10\n\tmovq " + val + ", %r11\n";
						asm_ += "\t" + ((OperatorToken) valueTokens[1]).asm_code("r10", "r11") + "\n";
						if (depth != 0)
							asm_ += "\tmovq %r10, INTERNAL____CACHE+" + (8 * (cache_ptr = internal_cache_index++)) + "\n";
						return asm.toString() + asm_;
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
								asm.append(valueInstructions(tokens1));
								a = 8 * cache_ptr;
								if (depth == 0) {
									if (((OperatorToken) valueTokens[i]).mop == OperatorToken.Math_Operator.LOGIC_AND) {
										asm.append("\tCMPQ $0, INTERNAL____CACHE+a\n\tJE ").append(jumpFalseLabel).append("\n");
									} else if (((OperatorToken) valueTokens[i]).mop == OperatorToken.Math_Operator.LOGIC_OR) {
										asm.append("\tCMPQ $0, INTERNAL____CACHE+a\n\tJNE ").append(jumpTrueLabel).append("\n");
									}
								}
								asm.append(valueInstructions(tokens2));
								asm.append("\tmovq INTERNAL____CACHE+").append(a).append(", %r10\n");
								asm.append("\tmovq INTERNAL____CACHE+").append(8 * cache_ptr).append(", %r11\n");
								asm.append(((OperatorToken) valueTokens[i]).asm_code("r10", "r11"));
								if (depth != 0)
									asm.append("movq %r10, INTERNAL____CACHE+").append(8 * (cache_ptr = a / 8)).append("\n");
								return asm.toString();
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
								asm.append(valueInstructions(tokens1));
								a = 8 * cache_ptr;
								asm.append(valueInstructions(tokens2));
								asm.append("\tmovq INTERNAL____CACHE+").append(a).append(", %r10\n");
								asm.append("\tmovq INTERNAL____CACHE+").append(8 * cache_ptr).append(", %r11\n");
								asm.append(((OperatorToken) valueTokens[i]).asm_code("r10", "r11"));
								if (depth != 0)
									asm.append("movq %r10, INTERNAL____CACHE+").append(8 * (cache_ptr = a / 8)).append("\n");
								return asm.toString();
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
								asm.append(valueInstructions(tokens1));
								a = 8 * cache_ptr;
								asm.append(valueInstructions(tokens2));
								asm.append("\tmovq INTERNAL____CACHE+").append(a).append(", %r10\n");
								asm.append("\tmovq INTERNAL____CACHE+").append(8 * cache_ptr).append(", %r11\n");
								asm.append(((OperatorToken) valueTokens[i]).asm_code("r10", "r11"));
								if (depth != 0)
									asm.append("movq %r10, INTERNAL____CACHE+").append(8 * (cache_ptr = a / 8)).append("\n");
								return asm.toString();
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
								asm.append(valueInstructions(tokens1));
								a = 8 * cache_ptr;
								asm.append(valueInstructions(tokens2));
								asm.append("\tmovq INTERNAL____CACHE+").append(a).append(", %r10\n");
								asm.append("\tmovq INTERNAL____CACHE+").append(8 * cache_ptr).append(", %r11\n");
								asm.append(((OperatorToken) valueTokens[i]).asm_code("r10", "r11"));
								if (depth != 0)
									asm.append("movq %r10, INTERNAL____CACHE+").append(8 * (cache_ptr = a / 8)).append("\n");
								return asm.toString();
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
								asm.append(valueInstructions(tokens1));
								a = 8 * cache_ptr;
								asm.append(valueInstructions(tokens2));
								asm.append("\tmovq INTERNAL____CACHE+").append(a).append(", %r10\n");
								asm.append("\tmovq INTERNAL____CACHE+").append(8 * cache_ptr).append(", %r11\n");
								asm.append(((OperatorToken) valueTokens[i]).asm_code("r10", "r11"));
								if (depth != 0)
									asm.append("movq %r10, INTERNAL____CACHE+").append(8 * (cache_ptr = a / 8)).append("\n");
								return asm.toString();
						}
				}
				for (i = valueTokens.length - 1; i >= 0; i--) {
					if (valueTokens[i] instanceof UnaryOperatorToken) {
						Token[] tokens2 = new Token[valueTokens.length - i - 1];
						System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
						asm.append(valueInstructions(tokens2));
						asm.append(((UnaryOperatorToken) valueTokens[i]).asm_code("r10"));
						if (depth != 0)
							asm.append("\n\tmovq %r10, INTERNAL____CACHE+").append(8 * cache_ptr).append("\n");
						return asm.toString();
					}
				}
				return "\n";
			}
		}

		private static void compileAssembly() {
			try (FileOutputStream fout = new FileOutputStream(asm_source_file)) {
				fout.write((requiredFunctions() + assembly.toString()).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Process p = Runtime.getRuntime().exec(new String[]{"as", asm_source_file, "-o", "pseudo.o"});
				p.waitFor();
				if (p.exitValue() != 0) {
					InputStream inr = p.getErrorStream();
					String msg = new String(inr.readAllBytes());
					System.err.println("GAS:\n" + msg);
					System.exit(p.exitValue());
				}
				p = Runtime.getRuntime().exec(new String[]{"ld", "-o", target_binary_file, "-dynamic-linker", "/lib/ld-linux-x86-64.so.2", "/usr/lib/crt1.o", "/usr/lib/crti.o", "-lc", "pseudo.o", "/usr/lib/crtn.o", "-lpseudo-std"});
				p.waitFor();
				if (p.exitValue() != 0) {
					InputStream inr = p.getErrorStream();
					String msg = new String(inr.readAllBytes());
					System.err.println("LD:\n" + msg);
					System.exit(p.exitValue());
				}
				p = Runtime.getRuntime().exec(new String[]{"rm", "pseudo.o"});
				p.waitFor();
				if (p.exitValue() != 0) {
					InputStream inr = p.getErrorStream();
					String msg = new String(inr.readAllBytes());
					System.err.println("RM:\n" + msg);
					System.exit(p.exitValue());
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		public static String value(Token token) {
			preparations = "";
			if (token instanceof NumberToken)
				return "$" + (((NumberToken) token).v * alignment);
			else if (token instanceof IdentifierToken) {
				IdentifierToken idf = ((IdentifierToken) token);
				if (idf.data_type == DATA_TYPE.POINTER)
					alignment = idf.var.type.bytesize;
				return (idf.data_type == DATA_TYPE.POINTER ? "$" : "") + "var_" + var_indices.get(idf.identifier);
			} else if (token instanceof INTERNAL____CACHE_TOKEN) {
				return "INTERNAL____CACHE+" + (8 * ((INTERNAL____CACHE_TOKEN) token).qwordoffset);
			} else if (token instanceof LogicConstantValueToken)
				return ((LogicConstantValueToken) token).v ? "$1" : "$0";
			else if (token instanceof ArrayIdentifier) {
				if (isConstant(((ArrayIdentifier) token).indexTokens))
					return "var_" + var_indices.get(((ArrayIdentifier) token).array.name) + "+" + (8 * Objects.requireNonNull(evaluate(((ArrayIdentifier) token).indexTokens)).vi);
				else {
					int temprecind = rec_ind, inci = internal_cache_index;
					rec_ind = 0;
					preparations = valueInstructions(((ArrayIdentifier) token).indexTokens) + "\tmovq %r10, " + INDEX_REGISTER + "\n";
					rec_ind = temprecind;
					internal_cache_index = inci;
					cache_ptr = internal_cache_index - 1;
					return "var_" + var_indices.get(((ArrayIdentifier) token).array.name) + "(," + INDEX_REGISTER + "," + ((ArrayIdentifier) token).array.type.bytesize + ")";
				}
			} else return "";
		}

		private static boolean isConstant(Token[] t) {
			boolean constant = true;
			for (int i = 0; i < t.length && constant; i++)
				constant = !(t[i] instanceof IdentifierToken || t[i] instanceof INTERNAL____CACHE_TOKEN || t[i] instanceof ArrayIdentifier);
			return constant;
		}

		public static Value evaluate(Token[] valueTokens) {
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
					t[j] = new NumberToken(pv != null ? pv.vi : 0);
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
							Value va = Objects.requireNonNull(evaluate(tokens1));
							Value vb = Objects.requireNonNull(evaluate(tokens2));
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
							Value va = Objects.requireNonNull(evaluate(tokens1));
							Value vb = Objects.requireNonNull(evaluate(tokens2));
							return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
					}
			}
			for (i = valueTokens.length - 1; i >= 0; i--) {
				if (valueTokens[i] instanceof OperatorToken)
					switch (((OperatorToken) valueTokens[i]).mop) {
						case SUBTRACT:
						case ADD:
							if (i == 0) {
								Token[] tokens2 = new Token[valueTokens.length - 1];
								System.arraycopy(valueTokens, 1, tokens2, 0, valueTokens.length - 1);
								Value v = Objects.requireNonNull(evaluate(tokens2));
								return new Value(((OperatorToken) valueTokens[0]).result(0, v.vi));
							} else {
								Token[] tokens1 = new Token[i];
								Token[] tokens2 = new Token[valueTokens.length - i - 1];
								System.arraycopy(valueTokens, 0, tokens1, 0, i);
								System.arraycopy(valueTokens, i + 1, tokens2, 0, valueTokens.length - i - 1);
								Value va = Objects.requireNonNull(evaluate(tokens1));
								Value vb = Objects.requireNonNull(evaluate(tokens2));
								return new Value(((OperatorToken) valueTokens[i]).result(va.vi, vb.vi));
							}
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
							Value va = Objects.requireNonNull(evaluate(tokens1));
							Value vb = Objects.requireNonNull(evaluate(tokens2));
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
							Value va = Objects.requireNonNull(evaluate(tokens1));
							Value vb = Objects.requireNonNull(evaluate(tokens2));
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
	}

	private static class Optimizer {
		private static boolean isConstant(String value) {
			if (register.matcher(value).matches())
				return registers_constant.get(value.substring(1));
			if (value.matches("^(INTERNAL____CACHE|var_\\d+).*$")) {
				return !value.matches("^.*(" + regs.replaceAll(" ", "|") + ").*$") &&
						memory_constant.containsKey(value) && memory_constant.get(value);
			}
			return value.matches("^\\$\\d+$");
		}

		private static String cvalue(String name) {
			if (name.matches("^\\$\\d+$"))
				return name.substring(1) /*remove leading $*/;
			if (register.matcher(name).matches())
				return Integer.toString(registers_values.get(name.substring(1)/*remove leading %*/));
			return Integer.toString(memory_values.get(name));
		}

		private static void optimizeAssembly() {
			StringBuilder optimized = new StringBuilder();
			List<ASMOP> OPERATIONS = new ArrayList<>();
			boolean isCode = false;
			for (String LINE : assembly.toString().split("\n")) {
				while (LINE.startsWith(" ") || LINE.startsWith("\t"))
					LINE = LINE.substring(1);
				if (isCode) {
					if (LINE.startsWith(".section ")) {
						isCode = false;
						optimized.append(LINE).append("\n");
						continue;
					}
					if (!LINE.isEmpty())
						OPERATIONS.add(operation(LINE));
				} else if (!LINE.equals(".section .text"))
					optimized.append(LINE).append("\n");
				if (!isCode)
					isCode = LINE.equals(".section .text");
			}
			optimized.append(".section .text\n");
			for (int i = 0; i < OPERATIONS.size(); i++) {
				ASMOP asmop = OPERATIONS.get(i);
				if (asmop.isLabel) {
					for (String reg : regs.split(" ")) {
						registers_constant.replace(reg, false);
					}
					for (String mem : memory_constant.keySet()) {
						memory_constant.replace(mem, false);
					}
				} else if (asmop.OP.equals("CLEAR_CACHE")) {
					for (String mem : memory_constant.keySet()) {
						if (mem.startsWith("INTERNAL____CACHE"))
							memory_constant.replace(mem, false);
					}
				} else if (asmop.arg2 != null) {
					OPERAND operand = asmop.arg1;
					String check = asmop.arg1.value;
					String check2 = asmop.arg2.value;
					ASMOP prevop = OPERATIONS.get(i - 1);
					if (check.contains(INDEX_REGISTER) && !check.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check = check.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
					}
					if (check2.contains(INDEX_REGISTER) && !check2.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check2 = check2.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
					}
					if (asmop.OP.matches("^mov(zx)?(.?)$")) {
						operand.value_is_constant = isConstant(check);
						if (operand.value_is_constant) {
							operand.value = "$" + cvalue(check);
							setConstant(check2, true, Integer.parseInt(operand.value.replace("$", "")));
						} else setConstant(check2, false, 0);
					} else if (asmop.OP.matches("^lea.?$")) {
						setConstant(check2, false, 0);
						setConstant(check, false, 0); // ASSUME IT IS GOING TO BE MODIFIED
					} else if (asmop.OP.matches("^(add|sub)(.?)$")) {
						operand.value_is_constant = isConstant(check);
						if (operand.value_is_constant) {
							operand.value = "$" + cvalue(check);
						}
						setConstant(check2, false, 0);
					} else if (asmop.OP.matches("^(or|xor|and|shl|shr)(.?)$")) {
						operand.value_is_constant = isConstant(check);
						if (operand.value_is_constant) {
							operand.value = "$" + cvalue(check);
						}
						setConstant(check2, false, 0);
					} else if (asmop.OP.matches("cmp(.?)")) {
						operand.value_is_constant = isConstant(check);
						if (operand.value_is_constant) {
							operand.value = "$" + cvalue(check);
						}
					}
				} else { // 1 or 0 args
					if (asmop.OP.equals("int") && asmop.arg1.value.equals("$0x80") || asmop.OP.equals("syscall")) {
						setConstant("%rax", false, 0);
					} else if (asmop.OP.matches("^(mul|div)(.?)$")) {
						setConstant("%rax", false, 0);
						setConstant("%rdx", false, 0);
					} else if (asmop.OP.equals("call")) {
						for (String reg : registers_constant.keySet())
							registers_constant.replace(reg, false);
						method_required.put(asmop.arg1.value.endsWith("@PLT") ? asmop.arg1.value.substring(0, asmop.arg1.value.indexOf('@')) : asmop.arg1.value, true);
					}
				}
			}

			for (ASMOP op : OPERATIONS) op.print();
			for (int i = OPERATIONS.size() - 1; i >= 0; i--) {
				ASMOP op = OPERATIONS.get(i);
				if (op.isLabel || op.isJump) {
					for (String reg : register_required.keySet())
						register_required.put(reg, true);
					for (String memloc : memory_required.keySet())
						memory_required.put(memloc, true);
					continue;
				} else if (op.OP.equals("CLEAR_CACHE")) {
					for (String memloc : memory_required.keySet())
						if (memloc.startsWith("INTERNAL____CACHE"))
							memory_required.put(memloc, false);
					OPERATIONS.remove(i);
					continue;
				}
				if (op.OP.matches("^mov(zx)?(.?)$")) {
					String check = op.arg1.value;
					String check2 = op.arg2.value;
					ASMOP prevop = OPERATIONS.get(i - 1);
					boolean requiresindex = false;
					if (check.contains(INDEX_REGISTER) && !check.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check = check.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
						requiresindex = true;
					}
					if (check2.contains(INDEX_REGISTER) && !check2.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check2 = check2.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
						requiresindex = true;
					}
					if ((op.comment == null || !(op.comment.equals("POINTER") || op.comment.equals("NO_DELETE"))) && isNotRequired(check2)) {
						OPERATIONS.remove(i);
						continue;
					}
					setrequired(check2, false);
					setrequired(check, true);
					if (requiresindex)
						setrequiredreg(INDEX_REGISTER, true);
				} else if (op.OP.startsWith("lea")) {
					String check = op.arg1.value;
					String check2 = op.arg2.value;
					ASMOP prevop = OPERATIONS.get(i - 1);
					boolean requiresindex = false;
					if (check.contains(INDEX_REGISTER) && !check.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check = check.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
						requiresindex = true;
					}
					if (check2.contains(INDEX_REGISTER) && !check2.equals(INDEX_REGISTER) && prevop.arg2.value.equals(INDEX_REGISTER)) {
						check2 = check2.replaceAll(INDEX_REGISTER, prevop.arg1.value.replace("$", ""));
						requiresindex = true;
					}
					if ((op.comment == null || !(op.comment.equals("POINTER") || op.comment.equals("NO_DELETE"))) && isNotRequired(check2)) {
						OPERATIONS.remove(i);
						continue;
					}
					setrequired(check2, false);
					setrequired(check, true);
					if (requiresindex)
						setrequiredreg(INDEX_REGISTER, true);
				} else if ((op.OP.equals("int") && op.arg1.value.equals("0x80"))) {
					setrequiredreg("%rax", true);
					setrequiredreg("%rbx", true);
					setrequiredreg("%rcx", true);
					setrequiredreg("%rdx", true);
				} else if (op.OP.equals("syscall")) {
					setrequiredreg("%rax", true);
					setrequiredreg("%rdi", true);
					setrequiredreg("%rsi", true);
					setrequiredreg("%rdx", true);
				} else if (op.OP.startsWith("div")) {
					setrequiredreg("%rax", true);
					setrequiredreg("%rdx", true);
					setrequired(op.arg1.value, true);
				} else if (op.OP.startsWith("mul")) {
					setrequiredreg("%rax", true);
					setrequired(op.arg1.value, true);
				} else if (op.OP.startsWith("test")) {
					setrequiredreg(op.arg1.value, true);
					setrequiredreg(op.arg2.value, true);
				} else if (op.OP.equals("call")) {
					switch (op.arg1.value) {
						case "_print_char@PLT":
						case "_print_number@PLT":
							setrequiredreg("%rax", true);
							setrequiredreg("%r8", true);
							break;
						case "_read_value@PLT":
							setrequiredreg("%r8", true);
							break;
						case "_exit@PLT":
						case "_f_close@PLT":
						case "_f_ro_open@PLT":
						case "_prime@PLT":
						case "_perfect@PLT":
							setrequiredreg("%rax", true);
							break;
						case "_f_wo_open@PLT":
						case "_sort@PLT":
							setrequiredreg("%rax", true);
							setrequiredreg("%rbx", true);
							break;
						case "_swap@PLT":
							setrequiredreg("%rcx", true);
							setrequiredreg("%rdx", true);
							break;
						case "_merge_sort@PLT":
							setrequiredreg("%rsi", true);
							setrequiredreg("%rdi", true);
							break;
						case "_div_sum@PLT":
							setrequiredreg("%rbx", true);
							break;
						case "_print_string@PLT":
							setrequiredreg("%r8", true);
							setrequiredreg("%rax", true);
							setrequiredreg("%rbx", true);
							break;
					}
				} else if (op.OP.startsWith("push")) {
					setrequired(op.arg1.value, true);
				} else if (op.OP.startsWith("pop")) {
					setrequired(op.arg1.value, false);
				} else if (op.OP.startsWith("cmp")) {
					setrequired(op.arg1.value, true);
					setrequired(op.arg2.value, true);
				} else if (op.OP.matches("^(add|sub|and|or|xor|shl|shr)(.?)$")) {
					setrequired(op.arg1.value, true);
					setrequired(op.arg2.value, true);
				} else if (op.OP.startsWith("add") || op.OP.startsWith("sub")) {
					setrequired(op.arg1.value, true);
					setrequired(op.arg2.value, true);
				}
			}
			for (int opt = 0; opt < OPT_MAX; opt++)
				for (int i = OPERATIONS.size() - 1; i >= 1; i--) {
					ASMOP op = OPERATIONS.get(i);
					ASMOP op1 = OPERATIONS.get(i - 1);
					if (op.isLabel || op1.isLabel)
						continue;
					if (op.OP.startsWith("pop") && op1.OP.startsWith("push")) {
						if (op.arg1.value.equals(op1.arg1.value)) {
							OPERATIONS.remove(i);
							OPERATIONS.remove(i - 1);
						}
					}
					if (op.OP.startsWith("mov") && op1.OP.startsWith("mov")) {
						if (op1.arg2.value.equals(op.arg1.value) && op1.arg2.value.startsWith("%") && ((op.arg2.value.startsWith("%")) || (op1.arg1.value.startsWith("%") || op1.arg1.value.startsWith("$")))) {
							op.arg1.value = op1.arg1.value;
							OPERATIONS.remove(i - 1);
						}
					}
				}

			for (ASMOP op : OPERATIONS) {
				optimized.append(op.OP);
				if (op.arg1 != null)
					optimized.append(" ").append(op.arg1.value);
				if (op.arg2 != null) optimized.append(", ").append(op.arg2.value);
				if (op.comment != null && op.comment.startsWith("COMMENT:"))
					optimized.append(" #").append(op.comment.substring(8));
				optimized.append(nl);
			}
			String optstr = optimized.toString();
			for (OptimizationStrategy strategy : optimizationStrategies) {
				optstr = optstr.replaceAll(strategy.instructions, strategy.optimized);
			}

			optstr = optstr.replaceAll(nlr, "\n");

			optimized = new StringBuilder(optstr);

			assembly = optimized;

			OPERATIONS = new ArrayList<>();
			optimized = new StringBuilder(); // Reformat
			isCode = false;
			for (String LINE : assembly.toString().split("\n")) {
				while (LINE.startsWith(" ") || LINE.startsWith("\t"))
					LINE = LINE.substring(1);
				if (isCode) {
					if (LINE.startsWith(".section ")) {
						isCode = false;
						optimized.append(LINE).append("\n");
						continue;
					}
					if (!LINE.isEmpty()) {
						OPERATIONS.add(operation(LINE));
					}
				} else if (!LINE.equals(".section .text"))
					optimized.append(LINE).append("\n");
				if (!isCode)
					isCode = LINE.equals(".section .text");
			}
			optimized.append(".section .text\n");
			for (ASMOP op : OPERATIONS) {
				optimized.append(op.OP);
				if (op.arg1 != null) {
					optimized.append(op.OP.length() < 4 ? "\t" : "").append("\t\t").append(op.arg1.value);
					if (op.arg2 != null)
						optimized.append(",").append("\t".repeat(6 - (op.arg1.value.length() + 1) / 4)).append(op.arg2.value);
				}
				optimized.append(nl);
			}
			assembly = optimized;

			{
				StringBuilder asm = new StringBuilder();
				for (String line : assembly.toString().split("\n")) {
					if (!(line.startsWith("\t") || line.contains(":") || line.startsWith(".section .")))
						asm.append("\t\t\t").append(line).append("\n");
					else if (!line.matches("^\\s*$")) asm.append(line).append("\n");
				}
				assembly = asm;
			}
		}

		private static void setrequiredreg(String name, boolean required) {
			if (name.startsWith("%"))
				name = name.substring(1);
			REGISTER_ADDRESSING_SET ras = reg(name).addressing;
			register_required.replace(ras.x64.name, required);
			register_required.replace(ras.x32.name, required);
			register_required.replace(ras.x16.name, required);
			register_required.replace(ras.x8.name, required);
		}

		private static boolean isNotRequired(String name) {
			if (register.matcher(name).matches()) {
				return !register_required.get(name.substring(1));
			} else if (name.matches("^var_\\d+$") || name.startsWith("INTERNAL____CACHE+")) {
				return (!memory_required.containsKey(name) || !memory_required.get(name));
			} else return !name.matches("^var_\\d+\\(.*(" + regs.replaceAll(" ", "|") + ").*\\)");
		}

		private static void setrequired(String name, boolean required) {
			if (NumberToken.isAsmImmediate(name))
				return;
			if (register.matcher(name).matches()) {
				setrequiredreg(name.substring(1), required);
			} else {
				for (String reg : regs.split(" "))
					if (name.contains("%" + reg))
						setrequiredreg(reg, true);
				memory_required.put(name, required);
			}
		}

		private static void setConstant(String name, boolean constant, int val) {
			if (register.matcher(name).matches()) {
				REGISTER_ADDRESSING_SET ras = reg(name.substring(1)).addressing;
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
				if (constant) {
					memory_constant.put(name, true);
					memory_values.put(name, val);
				}
			}
		}

		private static ASMOP operation(String line) {
			if (line.endsWith(":") || !line.contains(" "))
				return new ASMOP(line, null, null);
			if (line.equals("CLEAR_CACHE"))
				return new ASMOP("CLEAR_CACHE", null, null);
			String opcode = line.substring(0, line.indexOf(' ')).toLowerCase();
			String argsfull = line.substring(line.indexOf(' ') + 1);
			String comment = null;
			if (argsfull.contains("//")) {
				String[] parts = argsfull.split("//");
				comment = parts[1];
				argsfull = parts[0];
			}
			String[] args = argsfull.split(",\\s*(?=[^)]*(?:\\(|$))");
			if (args.length == 2)
				return new ASMOP(opcode, new OPERAND(args[0]), new OPERAND(args[1])).withComment(comment);
			else if (args.length == 1)
				return new ASMOP(opcode, new OPERAND(args[0]), null).withComment(comment);
			else return new ASMOP(opcode, null, null).withComment(comment);
		}
	}

	private static class Parser {
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
				if (tokens[i] instanceof IdentifierToken && tokens[i + 1] instanceof ArrayAccessBeginToken) {
					int b = i + 2;
					int j;
					int d = 1;
					for (j = b + 1; j < tokens.length; j++) {
						if (tokens[j] instanceof ArrayAccessBeginToken) {
							d++;
						} else if (tokens[j] instanceof ArrayAccessEndToken) {
							d--;
							if (d == 0)
								break;
						}
					}
					if (d != 0)
						throw new ParsingError("Invalid expression");
					Token[] indexTokens = new Token[j - b];
					System.arraycopy(tokens, b, indexTokens, 0, indexTokens.length);
					for (int k = i + 1; k <= j; k++)
						tokens[k] = null;
					tokens[i] = new ArrayIdentifier(new Array(((IdentifierToken) tokens[i]).identifier), indexTokens);
				}
			}
			for (int i = tokens.length - 2; i >= 0; i--) {
				if (tokens[i] instanceof NewLineToken && tokens[i + 1] instanceof NewLineToken)
					tokens[i + 1] = null;
			}
			int l = 0;
			for (Token token : tokens)
				if (token != null)
					l++;
			Token[] tk = new Token[l];
			int ptr = 0;
			for (Token token : tokens)
				if (token != null)
					tk[ptr++] = token;
			tokens = tk;
			return parse(tokens);
		}

		private static Token[] tokenize(String lines) {
			List<Token> tokens = new ArrayList<>();
			while (!lines.isBlank()) {
				String t = Objects.requireNonNull(nextToken(lines));
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
						int i = ind.ind + 1, j;
						while (!(t[i] instanceof AssignmentToken))
							i++;
						i++;
						for (j = i + 1; j < t.length && !(t[j] instanceof NewLineToken); j++) ;
						Token[] valtkns = new Token[j - i];
						System.arraycopy(t, i, valtkns, 0, valtkns.length);
						Statement[] arr = new Statement[]{vardecl, new VarUpdate_Statement(t[ind.ind + 1], valtkns)};
						ind.ind = j + 1;
						return arr;
					} else {
						ind.ind += 3;
						return new Statement[]{vardecl};
					}
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
					if (indObj.ind < t.length) {
						boolean lookForElse = true;
						while (t[indObj.ind] instanceof NewLineToken) {
							indObj.ind++;
							if (indObj.ind == t.length) {
								lookForElse = false;
								break;
							}
						}
						if (lookForElse && t[indObj.ind] instanceof ElseToken) {
							indObj.ind++;
							onFalse = nextInstruction(t, indObj);
						}
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
					arr = new Statement[]{new VarUpdate_Statement(t[ind.ind], valtkns)};
					ind.ind = j + 1;
					return arr;
				}
				case WHILE_LOOP: {
					int i = ind.ind + 2, j, d = 1;
					for (j = i; j < t.length; j++) {
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
					indObj.ind = j;
					while (!(t[indObj.ind] instanceof DoToken))
						indObj.ind++;
					indObj.ind++;
					Statements repeat = nextInstruction(t, indObj);
					ind.ind = indObj.ind;
					return new Statement[]{new WhileLoop(condition, repeat)};
				}
				case METHOD_CALL: {
					String methodName = ((IdentifierToken) t[ind.ind]).identifier.toUpperCase();
					METHOD m = METHOD.valueOf(methodName);
					Token[][] params = callParameterTokens(t, ind);
					MethodCallStatement mcs = new MethodCallStatement(m, params);
					return new Statement[]{mcs};
				}
				case FOR_LOOP: {
					IdentifierToken var = (IdentifierToken) t[ind.ind + 1];
					Token[][] forboundtokens = forBounds(t, ind);
					if (forboundtokens[2] == null)
						forboundtokens[2] = new Token[]{new NumberToken(1)};
					Statements repeat = nextInstruction(t, ind);
					return new Statement[]{new ForLoop(var, forboundtokens, repeat)};
				}
				case DO_WHILE: {
					ind.ind++;
					Statements repeat = nextInstruction(t, ind);
					while (t[ind.ind] instanceof NewLineToken)
						ind.ind++;
					boolean negate;
					if (t[ind.ind] instanceof WhileToken) {
						negate = false;
					} else if (t[ind.ind] instanceof UntilToken) {
						negate = true;
					} else throw new ParsingError("Token in do while is neither a while token nor an until token");
					ind.ind++;
					int i = ind.ind, j, d = 1;
					for (j = i + 1; j < t.length; j++) {
						if (t[j] instanceof ParenthesisOpenedToken)
							d++;
						else if (t[j] instanceof ParenthesisClosedToken) {
							d--;
							if (d == 0)
								break;
						} else if (t[j] instanceof NewLineToken)
							break;
					}
					if (d != 0)
						throw new ParsingError("Parenthesis not closed");
					Token[] condtkns = new Token[j - i + 1 + (negate ? 1 : 0)];
					System.arraycopy(t, i, condtkns, negate ? 1 : 0, condtkns.length - (negate ? 1 : 0));
					if (negate)
						condtkns[0] = new UnaryOperatorToken(UnaryOperatorToken.OP.LOGIC_NOT);
					ind.ind = j + 1;
					return new Statement[]{new DoWhile(repeat, condtkns)};
				}
			}
			return null;
		}

		private static Statements nextInstruction(Token[] t, IndObj indObj) {
			while (t[indObj.ind] instanceof NewLineToken)
				indObj.ind++;
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
			if (j == i) {
				ind.ind = j + 1;
				return null;
			}
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

		private static Token[][] forBounds(Token[] t, IndObj ind) {
			int i = ind.ind + 3, j;
			for (j = i + 1; j < t.length; j++) {
				if (t[j] instanceof CommaToken) {
					break;
				}
			}
			Token[][] tokens = new Token[3][];
			tokens[0] = new Token[j - i];
			System.arraycopy(t, i, tokens[0], 0, tokens[0].length);
			i = j + 1;
			for (j = i + 1; j < t.length; j++) {
				if (t[j] instanceof CommaToken || t[j] instanceof DoToken) {
					break;
				}
			}
			tokens[1] = new Token[j - i];
			System.arraycopy(t, i, tokens[1], 0, tokens[1].length);
			if (t[j] instanceof CommaToken) {
				i = j + 1;
				for (j = i + 1; j < t.length; j++) {
					if (t[j] instanceof DoToken) {
						break;
					}
				}
				tokens[2] = new Token[j - i];
				System.arraycopy(t, i, tokens[2], 0, tokens[2].length);
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
			else if (value.equals("/") || value.equals("div"))
				return new OperatorToken(OperatorToken.Math_Operator.DIVIDE);
			else if (value.equals("%") || value.equals("mod"))
				return new OperatorToken(OperatorToken.Math_Operator.MODULO);
			else if (value.equals("==")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_E);
			else if (value.equals(">=")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_GE);
			else if (value.equals(">")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_G);
			else if (value.equals("<=")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_SE);
			else if (value.equals("<")) return new OperatorToken(OperatorToken.Math_Operator.LOGIC_S);
			else if (value.equals("<<")) return new OperatorToken(OperatorToken.Math_Operator.SHIFT_LEFT);
			else if (value.equals(">>")) return new OperatorToken(OperatorToken.Math_Operator.SHIFT_RIGHT);
			else if (value.equals("!=") || value.equals("<>"))
				return new OperatorToken(OperatorToken.Math_Operator.LOGIC_NE);
			else if (value.equals("|"))
				return new OperatorToken(OperatorToken.Math_Operator.BITWISE_OR);
			else if (value.equals("&"))
				return new OperatorToken(OperatorToken.Math_Operator.BITWISE_AND);
			else if (value.equals("(")) return new ParenthesisOpenedToken();
			else if (value.equals(")")) return new ParenthesisClosedToken();
			else if (value.matches("^(int|float|intreg|real|pointer)$")) return new TypeToken(value);
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
			else if (value.matches("^true|false$")) return new LogicConstantValueToken(value.equals("true"));
			else if (value.startsWith("\"") && value.endsWith("\"")) return new StringToken(value);
			else if (value.equals(",")) return new CommaToken();
			else if (value.equals("else") || value.equals("altfel")) return new ElseToken();
			else if (value.equals("do") || value.equals("executa")) return new DoToken();
			else if (FILE_ACCESS.access(value) != null) return new FILE_ACCESS_TOKEN(FILE_ACCESS.last);
			else if (value.equals("file_stream")) return new TypeToken(value);
			else if (value.equals("file")) return new FILE_TOKEN();
			else if (value.equals("repeat") || value.equals("repeta")) return new RepeatToken();
			else if (value.equals("until") || value.equals("pana cand")) return new UntilToken();
			else if (value.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) return new IdentifierToken(value, null, null);
			else if (value.equals("\n")) return new NewLineToken();
			else if (value.equals("!")) return new UnaryOperatorToken(UnaryOperatorToken.OP.LOGIC_NOT);
			else if (value.equals("[")) return new ArrayAccessBeginToken();
			else if (value.equals("]")) return new ArrayAccessEndToken();
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
			if (s.matches("^\\d+(.|\\n)*$")) {
				int i;
				char c = s.charAt(0);
				for (i = 1; i < s.length() && (isDigit(c) || c == '.'); i++) {
					c = s.charAt(i);
				}
				return s.substring(0, i - 1);
			}

			for (String keyword : keywords)
				if (s.startsWith(keyword))
					return keyword;

			if (s.startsWith("\"")) {
				int i;
				for (i = 1; i < s.length() && s.charAt(i) != '"'; i++) ;
				return s.substring(0, i + 1);
			}

			for (FILE_ACCESS fa : FILE_ACCESS.values())
				for (String td : fa.type_declarators)
					if (s.startsWith(td)) {
						return td;
					}

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
	}

	public static class VarManager {
		public static void setVarSize(String identifier, int size) {
			for (VAR_ v : vars)
				if (v.name.equals(identifier)) {
					v.size = size;
					return;
				}
		}

		public static class VAR_ {
			public final String name;
			private final DATA_TYPE type;
			int size = 1;
			private String value;

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
}