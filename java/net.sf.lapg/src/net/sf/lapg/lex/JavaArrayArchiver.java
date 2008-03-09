package net.sf.lapg.lex;

public class JavaArrayArchiver {

	/***************************************************************
	Function: emit_driver
	Description: Output an integer table as a string.  Written by
	Raimondas Lencevicius 6/24/98; reorganized by CSA 9-Aug-1999.
	From his original comments:
	 yy_nxt[][] values are coded into a string
	 by printing integers and representing
	 integer sequences as "value:length" pairs.
	 **************************************************************/
	public static String emit_table_as_string(int[][] ia, int leftpadding) {

		StringBuffer sb = new StringBuffer("");

		int sequenceLength = 0; // RL - length of the number sequence
		boolean sequenceStarted = false; // RL - has number sequence started?
		int previousInt = ia.length > 0 ? ~ia[0][0] : -100; // != first element

		// RL - Output matrix size
		sb.append(ia.length);
		sb.append(",");
		sb.append(ia.length > 0 ? ia[0].length : 0);
		sb.append(",\n");

		StringBuffer outstr = new StringBuffer();

		//  RL - Output matrix
		for (int elem = 0; elem < ia.length; ++elem) {
			for (int i = 0; i < ia[elem].length; ++i) {
				int writeInt = ia[elem][i];
				if (writeInt == previousInt) // RL - sequence?
				{
					if (sequenceStarted) {
						sequenceLength++;
					} else {
						outstr.append(writeInt);
						outstr.append(":");
						sequenceLength = 2;
						sequenceStarted = true;
					}
				} else // RL - no sequence or end sequence
				{
					if (sequenceStarted) {
						outstr.append(sequenceLength);
						outstr.append(",");
						sequenceLength = 0;
						sequenceStarted = false;
					} else {
						if (elem != 0 || i != 0) {
							outstr.append(previousInt);
							outstr.append(",");
						}
					}
				}
				previousInt = writeInt;
				// CSA: output in 75 character chunks.
				if (outstr.length() > 75) {
					String s = outstr.toString();
					for (int e = 0; e < leftpadding; e++) {
						sb.append("\t");
					}
					sb.append("\"" + s.substring(0, 75) + "\" +\n");
					outstr = new StringBuffer(s.substring(75));
				}
			}
		}
		if (sequenceStarted) {
			outstr.append(sequenceLength);
		} else {
			outstr.append(previousInt);
		}
		// CSA: output in 75 character chunks.
		if (outstr.length() > 75) {
			String s = outstr.toString();
			for (int e = 0; e < leftpadding; e++) {
				sb.append("\t");
			}
			sb.append("\"" + s.substring(0, 75) + "\" +\n");
			outstr = new StringBuffer(s.substring(75));
		}
		for (int e = 0; e < leftpadding; e++) {
			sb.append("\t");
		}
		sb.append("\"" + outstr + "\"");
		return sb.toString();
	}

	public int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex == -1) ? st : st.substring(0, commaIndex);
				st = st.substring(commaIndex + 1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j] = Integer.parseInt(workString);
					continue;
				}
				lengthString = workString.substring(colonIndex + 1);
				sequenceLength = Integer.parseInt(lengthString);
				workString = workString.substring(0, colonIndex);
				sequenceInteger = Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}

}
