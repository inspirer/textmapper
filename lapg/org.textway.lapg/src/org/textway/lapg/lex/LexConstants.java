/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
	public static final int BITS = 32;
	public static final int MAX_LEXEMS = 0x100000;
	public static final int MAX_ENTRIES = 1024;
	public static final int MAX_WORD = 0x7ff0;
	public static final int MAX_DEEP = 128;
	public static final int HASH_SIZE = 1023;

	public static final int UNICODE_SYMBOLS = 0x10000;
	public static final int UNICODE_SET_SIZE = (UNICODE_SYMBOLS + BITS - 1) / BITS;

	public static final int LBR = 0x80010000;
	public static final int RBR = 0x80020000;
	public static final int OR = 0x80030000;
	public static final int SPL = 0x80040000;
	public static final int ANY = 0x80050000;
	public static final int SYM = 0x80060000;
	public static final int MASK = 0x800f0000;

	public static final int HIGH_STORAGE = (1 << 29) + (1 << 30);
	public static final int ALREADYMAXLEXEM = 0x10000000;

}
