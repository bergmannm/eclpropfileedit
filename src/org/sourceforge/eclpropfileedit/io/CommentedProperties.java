package org.sourceforge.eclpropfileedit.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Properties, které zachovávají komentáøe a poøadí položek ... 
 * 
 * @author mibecz 22.8.2006
 * 
 * private static final Logger logger =
 * Logger.getLogger(CommentedProperties.class);
 */
public class CommentedProperties extends Properties {
	@Override
    public synchronized Object remove(Object key) {
        Object remove = super.remove(key);
        if (itemNames.contains(key)){
            itemNames.remove(key);
            items.remove(new ItemKeyValue((String) key));
        }
        return remove;
    }

    /**
	 * 
	 */
	private static final long serialVersionUID = 5877717212570413098L;

	static class Item {

	}

	static class ItemKeyValue extends Item {
		String key;

		public ItemKeyValue(String key) {
			this.key = key;
		}

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ItemKeyValue other = (ItemKeyValue) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
	}

	static class ItemComment extends Item {
		String text;

		public ItemComment(String text) {
			this.text = text;
		}
	}

	class LineReader {
		public LineReader(InputStream inStream) {
			this.inStream = inStream;
		}

		byte[] inBuf = new byte[1<<18];

		char[] lineBuf = new char[1<<10];

		int inLimit = 0;

		int inOff = 0;

		InputStream inStream;

		boolean skipNextNewLine = false;

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = inStream.read(inBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						skipNextNewLine = true;
						return len;
					}
				}
				// The line below is equivalent to calling a
				// ISO8859-1 decoder.
				c = (char) (0xff & inBuf[inOff++]);
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						if (c == '\n') {
							if (skipNextNewLine)
								skipNextNewLine = false;
							else
								items.add(new ItemComment(""));
						}
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
					    if (inOff - len - 2>=0)
					        items.add(new ItemComment(new String(inBuf, inOff - len - 2, len + 1)));
						skipNextNewLine = true;
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = inStream.read(inBuf);
						inOff = 0;
						if (inLimit <= 0) {
							skipNextNewLine = true;
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						skipNextNewLine = true;
						return len;
					}
				}
			}
		}
	}

	List<Item> items = new ArrayList<Item>();
	Set<String> itemNames=new HashSet<String>();

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		char[] convtBuf = new char[1024];
		LineReader lr = new LineReader(inStream);

		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;

	    items = new ArrayList<Item>();
	    itemNames=new HashSet<String>();
		
		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;

			// System.out.println("line=<" + new String(lineBuf, 0, limit) +
			// ">");
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				// need check if escaped.
				if ((c == '=' || c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' && c != '\f') {
					if (!hasSep && (c == '=' || c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			if (!itemNames.contains(key)){
				itemNames.add(key);
				items.add(new ItemKeyValue(key));
			}
			put(key, value);
		}
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved
	 * chars to their original forms
	 */
	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}

	/**
	 * argument comments je ignorován
	 */
	@Override
    public synchronized void store(OutputStream out, String comments) throws IOException {
		BufferedWriter awriter;
		awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
		// ko
		// if (comments != null)
		// writeln(awriter, "#" + comments);
		// writeln(awriter, "#" + new Date().toString());
		Set allKeys = new HashSet();
		allKeys.addAll(keySet());
		for (Item item : items) {
			if (item instanceof ItemComment) {
				ItemComment comment = (ItemComment) item;
				writeln(awriter, comment.text);
			} else if (item instanceof ItemKeyValue) {
				ItemKeyValue kv = (ItemKeyValue) item;
				String key = kv.key;
				if (containsKey(key)) {
					String val = (String) get(key);
					key = saveConvert(key, true);

					/*
					 * No need to escape embedded and trailing spaces for value,
					 * hence pass false to flag.
					 */
					val = saveConvert(val, false);
					writeln(awriter, key + "=" + val);
					allKeys.remove(key);
				}
			}
		}
		for (Object keyObject : allKeys) {
			String key = (String) keyObject;
			String val = (String) get(key);
			key = saveConvert(key, true);

			/*
			 * No need to escape embedded and trailing spaces for value, hence
			 * pass false to flag.
			 */
			val = saveConvert(val, false);
			writeln(awriter, key + "=" + val);
		}

		awriter.flush();
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes special characters
	 * with a preceding slash
	 */
	private String saveConvert(String theString, boolean escapeSpace) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' };

	private static void writeln(BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	// static boolean compareProperties(Properties p1, Properties p2) {
	// Map m1 = new TreeMap(p1), m2 = new TreeMap(p2);
	// boolean ret = m1.equals(m2);
	// return ret;
	// }
	//
	// public static void main(String[] args) throws IOException {
	// Properties p = new CommentedProperties();
	// InputStream is = CommentedProperties.class
	// .getResourceAsStream("/cz/elanor/eman/sgui/forms/rtf01/Rtf01.properties");
	// p.load(is);
	// is.close();
	// Properties p2 = new CommentedProperties();
	// is = CommentedProperties.class
	// .getResourceAsStream("/cz/elanor/eman/sgui/forms/rtf01/Rtf01.properties");
	// p2.load(is);
	// System.out.println(compareProperties(p, p2));
	//
	// p.store(System.out, null);
	// }

	@Override
	public synchronized void clear() {
		items.clear();
		super.clear();
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		if (key instanceof String && !itemNames.contains(key)){
			itemNames.add((String) key);
			items.add(new ItemKeyValue((String) key));
		}
		return super.put(key, value);
	}

}
