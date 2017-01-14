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
package org.textmapper.lapg.common;

import org.textmapper.lapg.api.regex.CharacterSet;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Handles set of unicode characters.
 */
public final class CharacterSetImpl implements CharacterSet {

	private final int[] set;
	private final boolean inverted;

	public CharacterSetImpl(int[] set, int size, boolean inverted) {
		assert (size % 2) == 0;
		this.set = new int[size];
		this.inverted = inverted;
		System.arraycopy(set, 0, this.set, 0, size);
		Arrays.sort(this.set);
	}

	public CharacterSetImpl(int[] set, int size) {
		this(set, size, false);
	}

	public CharacterSetImpl(int... set) {
		this(set, set.length, false);
	}

	@Override
	public boolean contains(int c) {
		int sind = binarySearch(set, 0, set.length, c);
		if (sind < 0) {
			sind = -sind - 1;
			boolean inSet = (sind & 1) != 0;
			return inSet ^ inverted;
		} else {
			return !inverted;
		}
	}

	@Override
	public boolean isEmpty() {
		return set.length == 0 && !inverted;
	}

	@Override
	public boolean isInverted() {
		return inverted;
	}

	@Override
	public int[] toArray() {
		if (inverted) {
			throw new UnsupportedOperationException();
		}
		return Arrays.copyOf(set, set.length);
	}

	@Override
	public Iterator<int[]> iterator() {
		return new Iterator<int[]>() {
			int[] token = new int[2];
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < set.length;
			}

			@Override
			public int[] next() {
				if (index < set.length) {
					token[0] = set[index++];
					token[1] = set[index++];
					return token;
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < set.length; i += 2) {
			if (sb.length() > 1) {
				sb.append(',');
			}
			if (set[i] == set[i + 1]) {
				sb.append(set[i]);
			} else {
				sb.append(set[i]);
				sb.append("-");
				sb.append(set[i + 1]);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CharacterSetImpl ints = (CharacterSetImpl) o;

		if (inverted != ints.inverted) return false;
		return Arrays.equals(set, ints.set);

	}

	@Override
	public int hashCode() {
		int result = set != null ? Arrays.hashCode(set) : 0;
		result = 31 * result + (inverted ? 1 : 0);
		return result;
	}

	/**
	 * CharacterSet factory.
	 */
	public static final class Builder {

		private int[] set;
		private int length;

		public Builder() {
			this.set = new int[1024];
			this.length = 0;
		}

		public void clear() {
			this.length = 0;
		}

		private void reallocateSet() {
			int[] newset = new int[set.length * 2];
			System.arraycopy(set, 0, newset, 0, set.length);
			set = newset;
		}

		public void addSymbol(int sym) {
			addRange(sym, sym);
		}

		public void addRange(int start, int end) {
			int sind = binarySearch(set, 0, length, start);
			if (sind < 0) {
				int insert = -sind - 1;
				if (insert >= length) {
					if (length > 0 && start == set[length - 1] + 1) {
						set[length - 1] = end;
					} else {
						if (length + 1 >= set.length) {
							reallocateSet();
						}
						set[length++] = start;
						set[length++] = end;
					}
					return;
				} else if ((insert & 1) == 0) {
					if (insert > 0 && start == set[insert - 1] + 1) {
						start = set[insert - 2];
						sind = insert - 2;
					} else if (end >= set[insert] - 1) {
						set[insert] = start;
						sind = insert;
					} else {
						if (length + 1 >= set.length) {
							reallocateSet();
						}
						for (int i = length - 1; i >= insert; i--) {
							set[i + 2] = set[i];
						}
						set[insert] = start;
						set[insert + 1] = end;
						length += 2;
						return;
					}

				} else {
					start = insert - 1;
					sind = start;
				}
			}
			if (sind >= 0) {
				if ((sind & 1) == 0) {
					if (sind + 2 >= length || end <= set[sind + 1]) {
						if (end > set[sind + 1]) {
							set[sind + 1] = end;
						}
						return;
					}
					sind++;
				} else {
					if (sind + 1 >= length || end < set[sind + 1] - 1) {
						set[sind] = end;
						return;
					}
				}
				int srst = sind + 1;
				int srend = srst;
				while (srend < length && set[srend] <= end + 1) {
					end = Math.max(set[srend + 1], end);
					srend += 2;
				}
				set[sind] = end;
				if (srend > srst) {
					while (srend < length) {
						set[srst++] = set[srend++];
					}
					length = srst;
				}
				return;
			}
			throw new IllegalStateException();
		}

		public void addSet(CharacterSet set) {
			for (int[] range : set) {
				addRange(range[0], range[1]);
			}
		}

		public CharacterSet create(boolean inverted) {
			return new CharacterSetImpl(set, length, inverted);
		}

		public CharacterSet create() {
			return new CharacterSetImpl(set, length);
		}

		public CharacterSet intersect(CharacterSet set1, CharacterSet set2) {
			clear();
			int ind2 = 0;
			for (int[] range1 : set1) {
				ind2 = intersectSegment(range1[0], range1[1], /* FIXME */ ((CharacterSetImpl) set2).set, ind2);
			}
			return create(false);
		}

		public CharacterSet subtract(CharacterSet set1, CharacterSet set2) {
			clear();
			int ind2 = 0;
			for (int[] range1 : set1) {
				ind2 = subtractSegment(range1[0], range1[1], /* FIXME */ ((CharacterSetImpl) set2).set, ind2);
			}
			return create(false);
		}

		private int subtractSegment(int start, int end, int[] subtract, int index) {
			while (index < subtract.length && subtract[index + 1] < start) {
				index += 2;
			}
			while (index < subtract.length && start <= end) {
				if (subtract[index] <= start) {
					start = subtract[index + 1] + 1;
					if (start > end) {
						return index;
					}
				} else if (subtract[index + 1] < end) {
					if (subtract[index] - 1 >= start) {
						addRange(start, subtract[index] - 1);
					}
					start = subtract[index + 1] + 1;
				} else {
					if (subtract[index] - 1 <= end) {
						addRange(start, subtract[index] - 1);
					} else {
						addRange(start, end);
					}
					return index;
				}

				while (index < subtract.length && subtract[index + 1] < start) {
					index += 2;
				}
			}
			if (start <= end) {
				addRange(start, end);
			}
			return index;
		}

		private int intersectSegment(int start, int end, int[] intersect, int index) {
			while (index < intersect.length && intersect[index + 1] < start) {
				index += 2;
			}
			while (index < intersect.length) {
				if (intersect[index] <= start) {
					addRange(start, Math.min(intersect[index + 1], end));
					start = Math.min(intersect[index + 1], end) + 1;
					if (start > end) {
						return index;
					}
				} else if (intersect[index + 1] <= end) {
					addRange(intersect[index], intersect[index + 1]);
					start = intersect[index + 1] + 1;
					if (start > end) {
						return index;
					}
				} else {
					if (intersect[index] <= end) {
						addRange(intersect[index], end);
					}
					return index;
				}

				while (index < intersect.length && intersect[index + 1] < start) {
					index += 2;
				}
			}
			return index;
		}
	}

	private static int binarySearch(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = a[mid];

			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -(low + 1);  // key not found.
	}
}
