/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

public class ArrayMergerTest {

	@Test
	public void empty() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		assertArrayEquals(new String[]{}, order.uniqueSort(String[]::new));
	}

	@Test
	public void emptyRows() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A");
		order.flush();
		order.flush();
		order.flush();
		assertArrayEquals(new String[]{"A"}, order.uniqueSort(String[]::new));
	}

	@Test
	public void insertion() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B", "C", "F");
		order.flush();
		order.add("B", "Q", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "Q", "C", "F"}, order.uniqueSort(String[]::new));
	}

	@Test
	public void multipleDeps() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "C");
		order.flush();
		order.add("A", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "C"}, order.uniqueSort(String[]::new));
	}

	@Test
	public void partial() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "C");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "C"}, order.uniqueSort(String[]::new));
	}

	@Test
	public void cycle() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "A");
		order.flush();
		assertNull(order.uniqueSort(String[]::new));
	}

	@Test
	public void ambiguous() throws Exception {
		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B");
		order.flush();
		order.add("A", "C");
		order.flush();
		assertNull(order.uniqueSort(String[]::new));
		assertNotNull(order.topoSort(String[]::new));

		order = new ArrayMerger<>();
		order.add("A", "B");
		order.flush();
		order.add("B", "A");
		order.flush();
		assertNull(order.topoSort(String[]::new));
	}

	@Test
	public void toposort() throws Exception {
		assertArrayEquals(new String[]{}, new ArrayMerger<String>().topoSort(String[]::new));

		ArrayMerger<String> order = new ArrayMerger<>();
		order.add("A", "B", "D");
		order.flush();
		order.add("A", "C", "D");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "C", "D"}, order.topoSort(String[]::new));

		order = new ArrayMerger<>();
		order.add("A", "B", "D");
		order.flush();
		order.add("D", "C", "E");
		order.flush();
		assertArrayEquals(new String[]{"A", "B", "D", "C", "E"}, order.topoSort(String[]::new));

		order = new ArrayMerger<>();
		order.add("A", "Q2", "B");
		order.flush();
		order.add("A", "Q1", "B");
		order.flush();
		order.add("A", "Q3", "B");
		order.flush();
		assertArrayEquals(new String[]{"A", "Q1", "Q2", "Q3", "B"}, order.topoSort(String[]::new));
	}
}
