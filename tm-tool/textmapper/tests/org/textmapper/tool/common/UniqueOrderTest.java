/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.tool.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class UniqueOrderTest {

	@Test
	public void empty() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		assertArrayEquals(new String[]{}, order.getResult(String[]::new));
	}

	@Test
	public void emptyRows() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A");
		order.flush();
		order.flush();
		order.flush();
		assertArrayEquals(new String[]{"A"}, order.getResult(String[]::new));
	}

	@Test
	public void insertion() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A", "B", "C", "F");
		order.flush();
		order.add("B", "Q", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "Q", "C", "F"}, order.getResult(String[]::new));
	}

	@Test
	public void multipleDeps() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "C");
		order.flush();
		order.add("A", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "C"}, order.getResult(String[]::new));
	}

	@Test
	public void partial() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "C"}, order.getResult(String[]::new));
	}

	@Test
	public void cycle() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "A");
		order.flush();
		assertNull(order.getResult(String[]::new));
	}

	@Test
	public void ambiguous() throws Exception {
		UniqueOrder<String> order = new UniqueOrder<>();
		order.add("A", "B");
		order.flush();
		order.add("A", "C");
		order.flush();
		assertNull(order.getResult(String[]::new));
	}
}