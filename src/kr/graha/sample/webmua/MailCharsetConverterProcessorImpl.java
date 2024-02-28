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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.graha.post.interfaces.Processor;
import kr.graha.post.lib.Record;
import java.util.logging.Logger;
import java.util.logging.Level;
import kr.graha.helper.LOG;
import java.sql.SQLException;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.Connection;
import java.net.URISyntaxException;

/**
 * 이메일의 Charset 을 변경한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class MailCharsetConverterProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public MailCharsetConverterProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 * 이메일의 charset 을 변경한다.
 
 * @param request HttpServlet 에 전달된 HttpServletRequest
 * @param response HttpServlet 에 전달된 HttpServletResponse
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @see javax.servlet.http.HttpServletRequest (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletRequest (Apache Tomcat 10 이상)
 * @see javax.servlet.http.HttpServletResponse (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletResponse (Apache Tomcat 10 이상)
 * @see kr.graha.post.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory"))) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.50001");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"))) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.50002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_account_id is required!!!"); }
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"))) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.50003");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_id is required!!!"); }
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "charset"))) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.50004");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.charset is required!!!"); }
			return;
		}
		int graha_mail_account_id = params.getInt(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"));
		int graha_mail_id = params.getInt(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"));
		String mailSaveDirectory = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory"));
		String charset = params.getString(Record.key(Record.PREFIX_TYPE_PARAM, "charset"));
		String mailBackupDirectory = null;
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "mail.backup.directory"))) {
			mailBackupDirectory = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.backup.directory"));
		}
		try {
			MailCharsetInfo mailInfo = new MailCharsetInfo();
			mailInfo.setMailNewCharset(charset);
			mailInfo.setGrahaMailId(graha_mail_id);
			MailParserProcessorImpl parser = new MailParserProcessorImpl();
			parser.saveMail(mailInfo, con, params, mailSaveDirectory, mailBackupDirectory);
		} catch(SQLException | MessagingException | IOException | URISyntaxException e) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.50005");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
	}
}
