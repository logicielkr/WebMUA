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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchProviderException;
import kr.graha.lib.Encryptor;

/**
 * AES-GCM 암호화(그라하(Graha) Encryptor 구현체)
 * 암호화 혹은 복호화한다.
 
 * JDK 1.7 이하에서는 BouncyCastle(https://www.bouncycastle.org/) 라이브러리가 설치되어 있어야 한다.
 * 이 프로그램은 JDK 1.8 부터는 JDK 에 내장된 AES-GCM 라이브러리를 이용하고, 
 * JDK 1.7 이하에서는 BouncyCastle(https://www.bouncycastle.org/) 을 사용하여 암호화 혹은 복호화한다.

 * 이 프로그램을 사용하기 위해서는 전역변수 pwd 와 iv 를 변경하여 컴파일한 후에 사용해야 한다.
 * pwd 와 iv 는 사람이 읽을 수 있는 문자열이면 되고, 같은 값이거나 다른 값이어도 상관없지만, 다른 값을 권장한다.
 * 내부적으로 sha-256 으로 변환하여 공급된다(iv 값은 앞의 12자리만 잘라서).

 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Encryptor
 
 * @version 0.9
 * @since 0.9
 */

public class EncryptorAESGCMImpl implements Encryptor {
	protected final String pwd = "change it!";
	protected final String iv = "Change It!!!";
/**
 * 암호화한다.
 * @param plain 암호화할 평문 문자열
 * @return 암호화 후에 Base64로 인코딩된 문자열
 */
	@Override
	public String encrypt(String plain) throws NoSuchProviderException {
		if(Double.valueOf(System.getProperty("java.specification.version")) > 1.7) {
			AESGCMCipher aes = AESGCMCipher.getInstance();
			try {
				return aes.encrypt(plain, this.pwd, this.iv);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
		} else {
			AESGCMCipherUsingBouncyCastle aes = AESGCMCipherUsingBouncyCastle.getInstance();
			try {
				return aes.encrypt(plain, this.pwd, this.iv);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
/**
 * 복호화한다.
 * @param encrypted 암호화 후에 Base64로 인코딩된 문자열
 * @return 복호화한 문자열
 */
	@Override
	public String decrypt(String encrypted) throws NoSuchProviderException {
		if(Double.valueOf(System.getProperty("java.specification.version")) > 1.7) {
			AESGCMCipher aes = AESGCMCipher.getInstance();
			try {
				return aes.decrypt(encrypted, this.pwd, this.iv);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
		} else {
			AESGCMCipherUsingBouncyCastle aes = AESGCMCipherUsingBouncyCastle.getInstance();
			try {
				return aes.decrypt(encrypted, this.pwd, this.iv);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
