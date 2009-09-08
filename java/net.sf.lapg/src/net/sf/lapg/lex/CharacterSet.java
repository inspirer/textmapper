package net.sf.lapg.lex;

import java.util.Arrays;

/**
 * Handles set of unicode characters.
 */
public class CharacterSet {
	
	private final int[] set;

	public CharacterSet(int[] set, int size) {
		assert (size%2) == 0;
		this.set = new int[size];
		System.arraycopy(set, 0, this.set, 0, size);
		Arrays.sort(this.set);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = 0; i < set.length; i+=2) {
			if(sb.length() > 1) {
				sb.append(',');
			}
			if(set[i] == set[i+1]) {
				sb.append(set[i]);
			} else {
				sb.append(set[i]);
				sb.append("-");
				sb.append(set[i+1]);
			}
		}		
		sb.append("]");
		return sb.toString();
	}

	/**
	 * CharacterSet factory.
	 */
	public static class Builder {
		
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
			int[] newset = new int[set.length*2];
			System.arraycopy(set, 0, newset, 0, set.length);
			set = newset;
		}
	
		public void addSymbol(int sym) {
			if(length+1 >= set.length) {
				reallocateSet();
			}
			set[length++] = sym;
			set[length++] = sym;
		}
		
		public void addRange(int start, int end) {
			if(length+2 >= set.length) {
				reallocateSet();
			}
			set[length++] = start;
			set[length++] = end;
		}

		public CharacterSet create() {
			return new CharacterSet(set, length);
		}

       	public CharacterSet subtract(CharacterSet set1, CharacterSet set2) {
       		clear();
       		int start, end;
       		for(int ind1 = 0, ind2 = 0; ind1 < set1.set.length;) {
       			start = set1.set[ind1++];
       			end = set1.set[ind1++];
       			ind2 = subtractSegment(start,end,set2.set,ind2);
       		}
			return create();
		}
       	
       	private int subtractSegment(int start, int end, int[] subtract, int index) {
       		while(index < subtract.length && subtract[index+1] < start) {
       			index += 2;
       		}
       		while(index < subtract.length && start <= end) {
       			if(subtract[index] <= start) {
       				start = subtract[index+1] + 1;
       				if(start > end) {
       					return index;
       				}
       			} else if(subtract[index+1] < end) {
       				if(subtract[index]-1 >= start) {
       					addRange(start, subtract[index]-1);
       				}
       				start = subtract[index+1] + 1;
       			} else {
       				if(subtract[index]-1 <= end) {
       					addRange(start, subtract[index]-1);
       				} else {
       					addRange(start,end);
       				}
       				return index;
       			}
       			
           		while(index < subtract.length && subtract[index+1] < start) {
           			index += 2;
           		}
       		}
       		if(start <= end) {
       			addRange(start, end);
       		}
       		return index;
       	}
	}
}
