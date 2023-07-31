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


package kr.graha.sample.webmua.impl;

import java.util.HashMap;
import kr.graha.lib.Record;
import java.sql.Connection;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import javax.mail.MessagingException;
import java.util.logging.Logger;
import java.util.logging.Level;
import kr.graha.helper.LOG;
import javax.mail.internet.InternetAddress;

import de.agitos.dkim.DKIMSigner;
import de.agitos.dkim.SMTPDKIMMessage;

import kr.graha.sample.webmua.interfaces.MessageProcessor;

import javax.mail.Address;

/**
 * 메일을 발송하기 전에 DKIM 서명한다.
 * DKIM-for-JavaMail(https://github.com/usrflo/DKIM-for-JavaMail) 라이브러리를 사용하였다.
 * DKIM-for-JavaMail 라이브러리를 Java 8 이상에서 사용하기 위해서는 sun.misc;BASE64Encoder(DKIMforJavaMail-java8-compatible.jar) 를 추가해야 한다.
 
 * mail.xml 에 query[@id="send"]/header 부분에 다음의 내용을 추가하여 적용한다.
 * <prop name="message.processor" value="kr.graha.sample.webmua.impl.DKIMSignMessageProcessor" />
 
 * DKIM 위해서 selector 와 개인키 파일(der 파일)의 경로를 mail.xml 에 query[@id="send"]/header 부분에 추가한다.
 * <prop name="dkim.graha.kr.selector" value="javamail" />
 * <prop name="dkim.graha.kr.private.key.der" value="/var/lib/tomcat9/conf/private.key.der/graha.kr.private.der" />
 
 * "graha.kr" 는 사용하는 도메인으로 변경한다.
 * 도메인에 해당하는 prop 가 정의되어 있는 경우에만 실행한다.
 * 도메인은 MimeMessage 에서 getFrom 에서 얻어온다.

 * @author HeonJik, KIM
 * @version 0.9
 * @since 0.9
 */


public class DKIMSignMessageProcessor implements MessageProcessor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public MimeMessage execute(MimeMessage msg, Record params) throws MessagingException {
		String signingDomain = null;
		Address[] address = msg.getFrom();
		if(address != null && address.length > 0) {
			for(int i = 0; i < address.length; i++) {
				String adr = ((InternetAddress)address[i]).getAddress();
				if(adr != null && adr.indexOf("@") > 0) {
					signingDomain = adr.substring(adr.indexOf("@") + 1);
					break;
				}
			}
		}
		
		if(signingDomain != null) {
			if(
				params.hasKey("prop.dkim." + signingDomain + ".selector") &&
				params.hasKey("prop.dkim." + signingDomain + ".private.key.der")
			) {
				String selector = params.getString("prop.dkim." + signingDomain + ".selector");
				String derFileName = params.getString("prop.dkim." + signingDomain + ".private.key.der");
				try {
					DKIMSigner signer = new DKIMSigner(signingDomain, selector, derFileName);
					return new SMTPDKIMMessage(msg, signer);
				} catch (Exception e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		} else {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("signingDomain is null"); }
		}
		return msg;
	}
}
