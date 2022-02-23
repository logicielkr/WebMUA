/*
 *
 * Copyright (C) HeonJik, KIM
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


package kr.graha.sample.webmua;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * AES-GCM 암호화 유틸리티
 * JDK 1.8 이상에서만 사용할 수 있다.
 * JDK 1.7 이하에서 사용하게 되면, 예외(java.security.NoSuchAlgorithmException: Cannot find any provider supporting AES/GCM/NoPadding)가 발생한다.
 * 만약 Oracle JDK 를 사용한다면, 예외(java.security.InvalidKeyException: Illegal key size) 가 발생할 수 있고, policy.jar 파일을 교체하는 방법으로 해결해야 한다.
 * @author HeonJik, KIM
 * @version 0.5
 * @since 0.5
 * @see AESGCMCipherUsingBouncyCastle
 */

public class AESGCMCipher {
	private static volatile AESGCMCipher INSTANCE;
	public static AESGCMCipher getInstance() {
		if(INSTANCE == null) {
			synchronized(AESGCMCipher.class) {
				if(INSTANCE == null)
					INSTANCE = new AESGCMCipher();
			}
		}
		return INSTANCE;
	}
	private AESGCMCipher() {
	}
/**
 * 암호화한다.
 * @param plain 암호화할 평문 문자열
 * @param pwd 패스워드 (입력된 값은 SHA-256 으로 암호화한 후에 사용한다)
 * @param iv IV 값을 만들 문자열 (입력된 값은 SHA-256 으로 암호화한 후에 앞의 12자리만 사용한다.)
 * @return 암호화 후에 Base64로 인코딩된 문자열
 */
	public String encrypt(String plain, String pwd, String iv) throws 
		java.io.UnsupportedEncodingException, 
		NoSuchAlgorithmException, 
		NoSuchPaddingException, 
		InvalidKeyException, 
		InvalidAlgorithmParameterException, 
		IllegalBlockSizeException, 
		BadPaddingException
	{
		return encryptOrDecrypt(plain, pwd, iv, Cipher.ENCRYPT_MODE);
	}
/**
 * 복호화한다.
 * @param encrypted 암호화 후에 Base64로 인코딩된 문자열
 * @param pwd 패스워드 (입력된 값은 SHA-256 으로 암호화한 후에 사용한다)
 * @param iv IV 값을 만들 문자열 (입력된 값은 SHA-256 으로 암호화한 후에 앞의 12자리만 사용한다.)
 * @return 복호화한 문자열
 */
	public String decrypt(String encrypted, String pwd, String iv) throws 
		java.io.UnsupportedEncodingException, 
		NoSuchAlgorithmException, 
		NoSuchPaddingException, 
		InvalidKeyException, 
		InvalidAlgorithmParameterException, 
		IllegalBlockSizeException, 
		BadPaddingException
	{
		return encryptOrDecrypt(encrypted, pwd, iv, Cipher.DECRYPT_MODE);
	}
/**
 * 암호화하거나 복호화한다.
 * @param str 평문 혹은 암호화된 문자열.  암호화된 문자열이라면 Base64로 인코딩되어 있어야 한다.
 * @param pwd 패스워드 (입력된 값은 SHA-256 으로 암호화한 후에 사용한다)
 * @param iv IV 값을 만들 문자열 (입력된 값은 SHA-256 으로 암호화한 후에 앞의 12자리만 사용한다.)
 * @param mode 이 값(Cipher.ENCRYPT_MODE 혹은 Cipher.DECRYPT_MODE)에 따라 암호화 혹은 복호화가 결정된다.
 * @return 복호화한 혹은 암호화 후에 Base64로 인코딩된 문자열
 */
	private String encryptOrDecrypt(String str, String pwd, String iv, int mode) throws 
		java.io.UnsupportedEncodingException, 
		NoSuchAlgorithmException, 
		NoSuchPaddingException, 
		InvalidKeyException, 
		InvalidAlgorithmParameterException, 
		IllegalBlockSizeException, 
		BadPaddingException
	{
		Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
		MessageDigest sh = MessageDigest.getInstance("SHA-256");
		sh.update(pwd.getBytes());
		byte[] key = sh.digest();
		sh.reset();
		sh.update(iv.getBytes());
		byte[] shaIV = sh.digest();
		byte[] src = new byte[12];
		System.arraycopy(shaIV, 0, src, 0, src.length);
		SecretKey secureKey = new SecretKeySpec(key, "AES");
		c.init(mode, secureKey, new GCMParameterSpec(128, src));
		if(mode == Cipher.ENCRYPT_MODE) {
			byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
			return new String(Base64.getEncoder().encodeToString(encrypted));
		} else if(mode == Cipher.DECRYPT_MODE) {
			byte[] encrypted = Base64.getDecoder().decode(str.getBytes());
			return new String(c.doFinal(encrypted),"UTF-8");
		} else {
			throw new InvalidAlgorithmParameterException("unknown mode.  mode is Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE");
		}
	}
}
