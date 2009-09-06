package net.sf.lapg.lex;

import java.util.Arrays;

public class TablesArchiver {

	private final int nstates, nchars;
	private final int[][] change;

	public TablesArchiver(int nstates, int nchars, int[][] change) {
		this.nstates = nstates;
		this.nchars = nchars;
		this.change = change;
	}

	public int[] pDefault, pBase, pNext, pCheck;

	public void buildPacked() {
		pDefault = new int[nstates];
		pBase = new int[nstates];

		int[][] next = new int[nstates][];
		int[][] check = new int[nstates][];
		int pstates = 0;

		for (int i = 0; i < nstates; i++) {
			int bestDefault = -1, minOverrides = Integer.MAX_VALUE;
			for (int e = 0; e < i; e++) {
				int toOverride = 0;
				for (int c = 0; c < nchars && toOverride < minOverrides; c++) {
					if (change[e][c] != change[i][c]) {
						toOverride++;
					}
				}
				if (toOverride < minOverrides) {
					bestDefault = e;
					minOverrides = toOverride;
				}
			}

			pDefault[i] = bestDefault;

			int bestBase = -1;
			if (bestDefault >= 0) {
				int[] target = new int[minOverrides];
				int targetIndex = 0;
				for (int c = 0; c < nchars; c++) {
					if (change[bestDefault][c] != change[i][c]) {
						target[targetIndex++] = c;
					}
				}

				int y;
				targetIndex = pstates;
				for (int e = 0; e < pstates; e++) {
					for (y = 0; y < minOverrides; y++) {
						if (check[e][target[y]] != -1) {
							break;
						}
					}
					if (y == minOverrides) {
						targetIndex = e;
						break;
					}
				}

				if (targetIndex == pstates) {
					next[pstates] = new int[nchars];
					check[pstates] = new int[nchars];
					Arrays.fill(check[pstates], -1);
					pstates++;
				}

				bestBase = targetIndex * nchars;
				for (y = 0; y < minOverrides; y++) {
					check[targetIndex][target[y]] = i;
					next[targetIndex][target[y]] = change[i][target[y]];
				}
			} else {
				bestBase = pstates * nchars;
				next[pstates] = new int[nchars];
				check[pstates] = new int[nchars];
				Arrays.fill(check[pstates], -1);

				for (int c = 0; c < nchars; c++) {
					check[pstates][c] = i;
					next[pstates][c] = change[i][c];
				}
				pstates++;
			}
			pBase[i] = bestBase;
		}

		pNext = new int[pstates * nchars];
		pCheck = new int[pstates * nchars];

		for (int i = 0; i < pstates; i++) {
			System.arraycopy(next[i], 0, pNext, i * nchars, nchars);
			System.arraycopy(check[i], 0, pCheck, i * nchars, nchars);
		}

		System.out
				.println("compressed - was: " + (nchars * nstates) + ", now: " + (pstates * nchars * 2 + 2 * nstates));
		checkIntegrity();
	}

	private int nextState(int s2, int ch) {
		int curr = s2;
		while (curr != -1) {
			if (pCheck[pBase[curr] + ch] == curr) {
				return pNext[pBase[curr] + ch];
			}
			curr = pDefault[curr];
		}
		return -1;
	}

	private void checkIntegrity() {
		for (int i = 0; i < nstates; i++) {
			for (int e = 0; e < nchars; e++) {
				if (nextState(i, e) != change[i][e]) {
					System.out.println("integrity problem at " + i + ", " + e);
					return;
				}
			}
		}
	}
}
