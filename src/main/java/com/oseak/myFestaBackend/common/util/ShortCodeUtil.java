package com.oseak.myFestaBackend.common.util;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import com.oseak.myFestaBackend.common.exception.OsaekException;

/**
 * BASE64기반으로 id(long) -> code(string)으로 변환하는 유틸
 * 코드를 클라이언트에 응답으로 넘겨주어 url에 pk와 같은 값이 직접 노출되지 않도록 함
 */
public final class ShortCodeUtil {
	private ShortCodeUtil() {
	}

	private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
	private static final int BASE = ALPHABET.length;

	public static String encode(long num) {
		if (num == 0)
			return String.valueOf(ALPHABET[0]);
		StringBuilder sb = new StringBuilder();
		while (num > 0) {
			sb.append(ALPHABET[(int)(num % BASE)]);
			num /= BASE;
		}
		return sb.reverse().toString();
	}

	public static long decode(String code) {
		long num = 0;
		for (char c : code.toCharArray()) {
			int idx = indexOf(c);
			if (idx < 0)
				throw new OsaekException(SHORT_CODE_INVALID);
			num = num * BASE + idx;
		}
		return num;
	}

	private static int indexOf(char c) {
		for (int i = 0; i < BASE; i++)
			if (ALPHABET[i] == c)
				return i;
		return -1;
	}
}
