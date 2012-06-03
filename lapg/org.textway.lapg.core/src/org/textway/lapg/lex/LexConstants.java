/**
 * Copyright 2002-2012 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.lex;

public interface LexConstants {
	public static final int MAX_LEXEMS = 0x100000;
	public static final int MAX_ENTRIES = 1024;
	public static final int MAX_WORD = 0x7ff0;
	public static final int MAX_DEEP = 128;

	public static final int LBR = 0x11000000;
	public static final int RBR = 0x12000000;
	public static final int OR = 0x13000000;
	public static final int PLUS = 0x14000000;
	public static final int STAR = 0x15000000;
	public static final int QMARK = 0x16000000;
	public static final int ANY = 0x17000000;
	public static final int SYM = 0x18000000;
	public static final int SET = 0x19000000;
	public static final int DONE = 0x1a000000;

	public static final int MASK = 0x1f000000;
}
