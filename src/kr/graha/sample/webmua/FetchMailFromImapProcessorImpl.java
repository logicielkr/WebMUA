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

import kr.graha.lib.Processor;
import kr.graha.lib.Record;
import java.util.logging.Logger;
import java.util.logging.Level;
import kr.graha.helper.LOG;
import java.sql.SQLException;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.Connection;
import com.sun.mail.imap.IMAPFolder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import java.util.Properties;
import javax.mail.Message;
import kr.graha.helper.DB;
import java.util.HashMap;
import java.util.List;
import javax.mail.URLName;
import java.io.File;

/**
 * IMAP 서버에서 이메일을 가져온다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class FetchMailFromImapProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public FetchMailFromImapProcessorImpl() {
		
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
 * @see kr.graha.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		fetch(params, con);
	}
	private void fetch(Record params, Connection con) {
		if(!params.hasKey("prop.mail.save.directory")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put("error.error", "message.40001");
			return;
		}
		if(!params.hasKey("param.graha_mail_id")) {
			params.put("error.error", "message.40002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_id is required!!!"); }
			return;
		}
		
		int graha_mail_id = params.getInt("param.graha_mail_id");
		String mailSaveDirectory = params.getString("prop.mail.save.directory");
		
		String mailBackupDirectory = null;
		if(params.hasKey("prop.mail.backup.directory")) {
			mailBackupDirectory = params.getString("prop.mail.backup.directory");
		}
		
		File f = new File(mailSaveDirectory + "eml" + java.io.File.separator + graha_mail_id + java.io.File.separator + "mail.eml");
		if(f.exists() && f.length() > 0) {
			return;
		}
		
		Object[] param = new Object[2];
		param[0] = graha_mail_id;
		param[1] = params.getString("prop.logined_user");
		
		String sql = null;
		sql = "select graha_mail.imap_uid,\n";
		sql += "	graha_mail_folder.name,\n";
		sql += "	graha_mail_folder.graha_mail_account_id,\n";
		sql += "	graha_mail.default_charset as mail_default_charset,\n";
		sql += "	graha_mail_folder.default_charset as folder_default_charset,\n";
		sql += "	graha_mail_account.default_charset as account_default_charset\n";
		sql += "from webmua.graha_mail,\n";
		sql += "	webmua.graha_mail_folder,\n";
		sql += "	webmua.graha_mail_account\n";
		sql += "where graha_mail_id = ?\n";
		sql += "	and graha_mail.graha_mail_folder_id = graha_mail_folder.graha_mail_folder_id\n";
		sql += "	and graha_mail_account.graha_mail_account_id = graha_mail_folder.graha_mail_account_id\n";
		sql += "	and graha_mail.insert_id = ?\n";
		
		try {
			List result = DB.fetch(con, HashMap.class, sql, param);
			if(result.size() > 0) {
				HashMap data = (HashMap)result.get(0);
				if(
					data.get("imap_uid") != null && 
					((long)data.get("imap_uid")) > 0
				) {
					HashMap mailAccount = null;
					FetchMailProcessorImpl fetcher = new FetchMailProcessorImpl();
					try {
						mailAccount = fetcher.getMailAccount(params, con);
					} catch (SQLException | java.security.NoSuchProviderException e) {
						if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
					}
					if(mailAccount == null) {
						params.put("error.error", "message.40003");
						if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info is not exists!!!"); }
						return;
					}
					mailAccount.put("imap_fetch_type", "A");
					Properties props = fetcher.getProps(mailAccount);
					if(props == null) {
						params.put("error.error", "message.40004");
						return;
					}
					if(!mailAccount.containsKey("type") || mailAccount.get("type") == null) {
						params.put("error.error", "message.40005");
						return;
					}
					if(!mailAccount.containsKey("host") || mailAccount.get("host") == null) {
						params.put("error.error", "message.40006");
						return;
					}
					if(!mailAccount.containsKey("port") || mailAccount.get("port") == null) {
						params.put("error.error", "message.40007");
						return;
					}
					if(!mailAccount.containsKey("user_name") || mailAccount.get("user_name") == null) {
						params.put("error.error", "message.40008");
						return;
					}
					if(!mailAccount.containsKey("password") || mailAccount.get("password") == null) {
						params.put("error.error", "message.40009");
						return;
					}
					URLName urln = new URLName(
						(String)mailAccount.get("type"), 
						(String)mailAccount.get("host"), 
						(int)mailAccount.get("port"), 
						null, 
						(String)mailAccount.get("user_name"), 
						(String)mailAccount.get("password")
					);
					
					Session session = Session.getInstance(props, null);
					Store store = null;
					Folder emailFolder = null;
					boolean isConnect = false;
					try {
						store = session.getStore(urln);
						store.connect();
						isConnect = true;
						emailFolder = store.getFolder((String)data.get("name"));
						emailFolder.open(Folder.READ_ONLY);
						IMAPFolder imapFolder = (IMAPFolder)emailFolder;
						
						Message[] messages = imapFolder.getMessagesByUID(new long[]{(long)data.get("imap_uid")});
						if(messages != null && messages.length > 0) {
							HashMap mailData = new HashMap();
							mailData.put("graha_mail_id", graha_mail_id);
							com.sun.mail.imap.IMAPMessage msg = (com.sun.mail.imap.IMAPMessage)messages[0];
							if(msg != null) {
								fetcher.save(msg, mailData, mailAccount, params, session);
							} else {
								if(logger.isLoggable(Level.SEVERE)) { logger.severe("fail to fetch Mail from imap server using imap_uid!!!(imap_uid=" + data.get("imap_uid") + ")"); }
							}
						} else {
							if(logger.isLoggable(Level.SEVERE)) { logger.severe("fail to fetch Mail from imap server using imap_uid!!!(imap_uid=" + data.get("imap_uid") + ")"); }
						}
						emailFolder.close(false);
						emailFolder = null;
						store.close();
						store = null;
						
						MailCharsetInfo mailInfo = new MailCharsetInfo();
						mailInfo.setGrahaMailId(graha_mail_id);
						mailInfo.setMailDefaultCharset((String)data.get("mail_default_charset"));
						mailInfo.setFolderDefaultCharset((String)data.get("folder_default_charset"));
						mailInfo.setAccountDefaultCharset((String)data.get("account_default_charset"));
						MailParserProcessorImpl parser = new MailParserProcessorImpl();
						parser.saveMail(mailInfo, con, params, mailSaveDirectory, mailBackupDirectory);
						
					} catch (MessagingException | IOException e) {
						if(isConnect) {
							params.put("error.error", "message.40010");
						} else {
							params.put("error.error", "message.40011");
						}
						if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
					} finally {
						if(emailFolder != null && emailFolder.isOpen()) {
							try {
								emailFolder.close(false);
								emailFolder = null;
							} catch (MessagingException e) {
								if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
							}
						}
						if(store != null) {
							try {
								store.close();
								store = null;
							} catch (MessagingException e) {
								if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
	}
}
