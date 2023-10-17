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
import kr.graha.helper.DB;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.FetchProfile;
import javax.mail.UIDFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import javax.mail.NoSuchProviderException;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.mail.URLName;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import kr.graha.lib.Encryptor;
import javax.mail.Flags.Flag;
import java.util.Date;
import java.sql.Timestamp;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.PropUtil;
import javax.mail.Header;
import java.util.Enumeration;
import java.util.ArrayList;
import kr.graha.app.encryptor.EncryptorAESGCMImpl;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.pop3.POP3Message;
import java.nio.file.Files;


/**
 * IMAP4 혹은 POP3 에서 이메일을 가져온다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class FetchMailProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public FetchMailProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 * imap 혹은 pop3 서버에서 이메일을 가져온다.
 
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
/**
 * imap 혹은 pop3 서버에서 이메일을 가져온다.
 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void fetch(Record params, Connection con) {
		if(!params.hasKey("prop.mail.save.directory")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put("error.error", "message.30001");
			return;
		}
		if(!params.hasKey("param.graha_mail_account_id")) {
			params.put("error.error", "message.30002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_account_id is required!!!"); }
			return;
		}
		HashMap mailAccount = null;
		try {
			mailAccount = getMailAccount(params, con);
		} catch (SQLException | java.security.NoSuchProviderException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
		if(mailAccount == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info is not exists!!!"); }
			params.put("error.error", "message.30003");
			return;
		}
		if(!mailAccount.containsKey("type") || mailAccount.get("type") == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info(type) is null or empty!!!"); }
			params.put("error.error", "message.30004");
			return;
		}
		if(!mailAccount.containsKey("host") || mailAccount.get("host") == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info(host) is null or empty!!!"); }
			params.put("error.error", "message.30005");
			return;
		}
		if(!mailAccount.containsKey("port") || mailAccount.get("port") == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info(port) is null or empty!!!"); }
			params.put("error.error", "message.30006");
			return;
		}
		if(!mailAccount.containsKey("user_name") || mailAccount.get("user_name") == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info(user_name) is null or empty!!!"); }
			params.put("error.error", "message.30007");
			return;
		}
		if(!mailAccount.containsKey("password") || mailAccount.get("password") == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Account Info(password) is null or empty!!!"); }
			params.put("error.error", "message.30008");
			return;
		}
		Properties props = getProps(mailAccount);
		if(props == null) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("Mail Property is not exists!!!"); }
			params.put("error.error", "message.30009");
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
		boolean isConnect = false;
		try {
			store = session.getStore(urln);
			store.connect();
			isConnect = true;
			if(
				mailAccount.get("type") != null && 
				((String)mailAccount.get("type")).equals("pop3")
			) {
				fetchPOP3(session, store, mailAccount, params, con);
			} else if(
				mailAccount.get("type") != null && 
				((String)mailAccount.get("type")).equals("imap")
			) {
				fetchIMAP(session, store, mailAccount, params, con);
			}
			store.close();
			store = null;
		} catch (MessagingException | SQLException | IOException e) {
			if(isConnect) {
				params.put("error.error", "message.30010");
			} else {
				params.put("error.error", "message.30011");
			}
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		} finally {
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
/**
 * pop3 서버에서 이메일을 가져온다.
 
 * pop3 서버에서 오래된 이메일부터 리턴한다고 가정하고, 반대로 loop를 돌린다.
 * webmua 서버에 메시지가 발견되면, 그 이후의 메시지는 이미 가져왔다고 가정하고, loop 를 종료한다.
 
 * @param store pop3 서버 연결 정보
 * @param mailAccountmailAccount 데이타베이스에서 가져온 이메일 계정에 대한 정보
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void fetchPOP3(
		Session session,
		Store store,
		HashMap mailAccount,
		Record params,
		Connection con
	) throws MessagingException, SQLException, IOException {
		Folder emailFolder = null;
		try {
			emailFolder = store.getFolder("INBOX");
			if(
				mailAccount.get("leave_on_server") != null && 
				((String)mailAccount.get("leave_on_server")).equals("none")
			) {
				emailFolder.open(Folder.READ_ONLY);
			} else {
				emailFolder.open(Folder.READ_WRITE);
			}
			FetchProfile fp = new FetchProfile();
			fp.add(UIDFolder.FetchProfileItem.UID);
			emailFolder.fetch(emailFolder.getMessages(), fp);
			POP3Folder pf = (POP3Folder)emailFolder;
			Message[] messages = emailFolder.getMessages();
			for(int i = messages.length - 1; i >= 0; i--) {
				String uid = pf.getUID(messages[i]);
				ImapMessageMinimalInfo info = new ImapMessageMinimalInfo();
				info.setUid(uid);
				info.setGrahaMailAccountId((int)mailAccount.get("graha_mail_account_id"));
				info.setServerType(ImapMessageMinimalInfo.pop3);
				info.setReceivedDate(messages[i].getReceivedDate());
				info.setMailCheckColumn((String)mailAccount.get("mail_check_column"));
				if(!info.checkByUID()) {
					info.setMessageId(((MimeMessage)messages[i]).getMessageID());
					if(((MimeMessage)messages[i]).getMessageID() == null || ((MimeMessage)messages[i]).getMessageID().equals("")) {
						if(logger.isLoggable(Level.FINEST)) { logger.finest("MessageId is null or empty!!!"); }
						continue;
					}
				}
				HashMap data = save(info, params, con);
				if(data != null && data.containsKey("graha_mail_id")) {
					save((POP3Message)messages[i], data, mailAccount, params, session);
					if(
						mailAccount.get("leave_on_server") != null && 
						((String)mailAccount.get("leave_on_server")).equals("0")
					) {
						messages[i].setFlag(Flag.DELETED, true);
					}
					con.commit();
				} else if(data != null && data.containsKey("insert_date")) {
					if(
						mailAccount.get("leave_on_server") != null && 
						!((String)mailAccount.get("leave_on_server")).equals("none")
					) {
						long diff = (new Date().getTime()) - ((Timestamp)data.get("insert_date")).getTime();
						long leave_on_server = Integer.parseInt((String)mailAccount.get("leave_on_server")) * 1000 * 60 * 60 * 24;
						if(diff > leave_on_server) {
							messages[i].setFlag(Flag.DELETED, true);
						}
					}
				} else {
					if(logger.isLoggable(Level.FINEST)) { logger.finest("data is null or not contains graha_mail_id"); }
					break;
				}
			}
			if(
				mailAccount.get("leave_on_server") != null && 
				((String)mailAccount.get("leave_on_server")).equals("none")
			) {
				emailFolder.close(false);
			} else {
				emailFolder.close(true);
			}
			emailFolder = null;
		} catch (MessagingException | SQLException | IOException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			throw e;
		} finally {
			if(emailFolder != null) {
				try {
					emailFolder.close(false);
					emailFolder = null;
				} catch (MessagingException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		}
	}
/**
 * imap 서버에서 이메일을 가져온다.
 
 * imap 서버에서 오래된 이메일부터 리턴한다고 가정하고, 반대로 loop를 돌린다.
 * webmua 서버에 메시지가 발견되면, 그 이후의 메시지는 이미 가져왔다고 가정하고, 처리하지 않는다.
 * 다만 imap 서버에서는 이미 삭제된 메시지가 있을 수 있으므로, 삭제된 메시지가 있는지만 검사한다.
 
 * @param store imap 서버 연결 정보
 * @param mailAccountmailAccount 데이타베이스에서 가져온 이메일 계정에 대한 정보
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void fetchIMAP(
		Session session,
		Store store,
		HashMap mailAccount,
		Record params,
		Connection con
	) throws MessagingException, SQLException, IOException {
		IMAPFolder imapFolder = null;
		try {
			Folder[] folders = store.getDefaultFolder().list("*");
			boolean isSupportModSeq = false;
			String imap_fetch_type = (String)mailAccount.get("imap_fetch_type");
			if(folders != null) {
				for (int x = 0; x < folders.length; x++) {
					imapFolder = (IMAPFolder)folders[x];
					if(logger.isLoggable(Level.FINEST)) { logger.finest("IMAPFolder name is " + imapFolder.getName()); }
					if((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0) {
						if(logger.isLoggable(Level.FINEST)) { logger.finest("IMAPFolder type is HOLDS_MESSAGES"); }
						continue;
					}
					HashMap folderInfo = saveFolderIfNotExists(imapFolder, (int)mailAccount.get("graha_mail_account_id"), params, con);
					if(imap_fetch_type != null && (imap_fetch_type.equals("F") || imap_fetch_type.equals("M"))) {
						if(
							!params.hasKey("param.graha_mail_folder_id") ||
							!folderInfo.containsKey("graha_mail_folder_id") ||
							!(folderInfo.get("graha_mail_folder_id") instanceof Integer) ||
							params.getInt("param.graha_mail_folder_id") != (int)folderInfo.get("graha_mail_folder_id")
						) {
							if(logger.isLoggable(Level.FINEST)) { logger.finest("imap_fetch_type is F or M but graha_mail_folder_id is not match"); }
							continue;
						}
					}
					if(
						folderInfo != null &&
						folderInfo.containsKey("is_not_fetch") &&
						folderInfo.get("is_not_fetch") != null &&
						folderInfo.get("is_not_fetch") instanceof Boolean &&
						(boolean)folderInfo.get("is_not_fetch")
					) {
						if(logger.isLoggable(Level.FINEST)) { logger.finest("is_not_fetch is true"); }
						continue;
					}
					imapFolder.open(Folder.READ_WRITE);
					if(logger.isLoggable(Level.FINEST)) { logger.finest("IMAPFolder is opened"); }
					long highestModSeq = -1;
					if(x == 0 || isSupportModSeq) {
						highestModSeq = getHighestModSeq(imapFolder);
						if(x == 0 && highestModSeq >= 0) {
							isSupportModSeq = true;
						}
					}
					if(
						isSupportModSeq &&
						highestModSeq > 0 &&
						folderInfo != null &&
						folderInfo.containsKey("modseq") &&
						folderInfo.get("modseq") != null &&
						(long)folderInfo.get("modseq") >= highestModSeq
					) {
						if(imapFolder.isOpen()) {
							imapFolder.close(false);
						}
						imapFolder = null;
						if(logger.isLoggable(Level.FINEST)) { logger.finest("modseq >= highestModSeq"); }
						continue;
					}
					Message[] messages = null;
					messages = imapFolder.getMessages();
					for(int i = messages.length - 1; i >= 0; i--) {
						IMAPMessage msg = (IMAPMessage)messages[i];
						ImapMessageMinimalInfo info = new ImapMessageMinimalInfo();
						info.setServerType(ImapMessageMinimalInfo.imap);
						info.setUid(imapFolder.getUID(messages[i]));
						info.setGrahaMailAccountId((int)mailAccount.get("graha_mail_account_id"));
						info.setMailCheckColumn((String)mailAccount.get("mail_check_column"));
						info.setFolderName(imapFolder.getName());
						info.setSizeLong(msg.getSizeLong());
						info.setMessageId(msg.getMessageID());
						info.setReceivedDate(messages[i].getReceivedDate());
						if(!info.checkByUID()) {
							if(info.getMessageId() == null || info.getMessageId().equals("")) {
								if(logger.isLoggable(Level.FINEST)) { logger.finest("MessageId is null or blank"); }
								continue;
							}
						}
						if(isSupportModSeq) {
							try {
								info.setModseq(msg.getModSeq());
								info.setSupportModSeq(true);
							} catch (MessagingException e) {
								isSupportModSeq = false;
							}
						}
						if(!isSupportModSeq) {
							info.setModseq(-1);
							info.setSupportModSeq(false);
						}
						HashMap data = null;
						data = save(
							info,
							params,
							con
						);
						if(data != null && data.containsKey("graha_mail_id")) {
							save(msg, data, mailAccount, params, session);
							con.commit();
						} else {
							if(logger.isLoggable(Level.FINEST)) { logger.finest("data is null or is not contains graha_mail_id"); }
							break;
						}
					}
					if(isSupportModSeq) {
						updateFolder(imapFolder, (int)mailAccount.get("graha_mail_account_id"), highestModSeq, params, con, false);
					}
					if(imapFolder.isOpen()) {
						imapFolder.close(false);
					}
					imapFolder = null;
				}
			}
		} catch (MessagingException | SQLException | IOException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			throw e;
		} finally {
			if(imapFolder != null) {
				try {
					if(imapFolder.isOpen()) {
						imapFolder.close(false);
					}
					imapFolder = null;
				} catch (MessagingException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		}
	}
/**
 * Imap에서 HighestModSeq 를 가져온다.
 * Imap 서버 구현에 따라 HighestModSeq 혹은 ModSeq를 지원하지 않는 경우도 있는데,
 * 1) MessagingException 을 발생시키거나(Naver), -1 을 리턴(Kakao)하는 경우이고,
 * 이 경우 IMAPMessage 의 getSizeLong 과 getMessageID를 이용해서 처리해야 한다.
 
 * @param imapFolder IMAPFolder 객체
 * @return HighestModSeq 혹은 서버가 이를 지원하지 않는 경우 -1를 돌려준다.
 */
	private long getHighestModSeq(IMAPFolder imapFolder) {
		long modseq = -1;
		try {
			modseq = imapFolder.getHighestModSeq();
		} catch (MessagingException e) {
			modseq = -1;
		}
		return modseq;
	}
/**
 * 이메일 서버 연결에 필요한 Properties 를 가져온다.
 
 * @param mailAccount 데이타베이스에서 가져온 이메일 계정에 대한 정보
 * @return 이메일 서버 연결에 필요한 Properties
 */
	protected Properties getProps(HashMap mailAccount) {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", (String)mailAccount.get("type"));
		if(
			mailAccount.get("type") != null && 
			((String)mailAccount.get("type")).equals("pop3")
		) {
			if(
				mailAccount.get("encryption_type") != null && 
				!((String)mailAccount.get("encryption_type")).equals("plain")
			) {
				props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.pop3.socketFactory.fallback", "false");
				props.setProperty("mail.pop3.socketFactory.port", Integer.toString((int)mailAccount.get("port")));
				props.setProperty("mail.pop3.ssl.trust", "*");
				if(((String)mailAccount.get("encryption_type")).equals("STARTTLS")) {
					props.setProperty("mail.pop3.starttls.enable", "true");
				} else if(((String)mailAccount.get("encryption_type")).equals("SSL/TSL")) {
					props.setProperty("mail.pop3.ssl.enable", "true");
				}
			}
			props.setProperty("mail.pop3.port", Integer.toString((int)mailAccount.get("port")));
		} else if(
			mailAccount.get("type") != null && 
			((String)mailAccount.get("type")).equals("imap")
		) {
			if(
				mailAccount.get("encryption_type") != null && 
				!((String)mailAccount.get("encryption_type")).equals("plain")
			) {
				props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty("mail.imap.socketFactory.port", Integer.toString((int)mailAccount.get("port")));
				props.setProperty("mail.imap.ssl.trust", "*");
				if(((String)mailAccount.get("encryption_type")).equals("STARTTLS")) {
					props.setProperty("mail.imap.starttls.enable", "true");
				} else if(((String)mailAccount.get("encryption_type")).equals("SSL/TSL")) {
					props.setProperty("mail.imap.ssl.enable", "true");
				}
			}
			props.setProperty("mail.imap.fetchsize", Integer.toString(1024 *1024));
			props.setProperty("mail.imap.port", Integer.toString((int)mailAccount.get("port")));
		} else {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("type is not pop3 or imap!!!"); }
			return null;
		}
		return props;
	}
/**
 * 데이타베이스에서 이메일 계정에 대한 정보를 가져온다.
 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @return 데이타베이스에서 가져온 이메일 계정에 대한 정보
 */
	protected HashMap getMailAccount(Record params, Connection con)
		throws SQLException, java.security.NoSuchProviderException
	{
		String sql = null;
		Object[] param = new Object[2];
		if(params.hasKey("param.graha_mail_account_id")) {
			param[0] = params.getIntObject("param.graha_mail_account_id");
		} else {
			return null;
		}
		param[1] = params.getString("prop.logined_user");
		sql = "select\n";
		sql += "	graha_mail_account_id,\n";
		sql += "	COALESCE(graha_mail_account_template.type, graha_mail_account.type) as type,\n";
		sql += "	COALESCE(graha_mail_account_template.encryption_type, graha_mail_account.encryption_type) as encryption_type,\n";
		sql += "	leave_on_server,\n";
		sql += "	COALESCE(graha_mail_account.imap_fetch_type, graha_mail_account_template.imap_fetch_type) as imap_fetch_type,\n";
		sql += "	REGEXP_REPLACE(COALESCE(graha_mail_account_template.host, graha_mail_account.host), '^\\s+|\\s+$', '', 'g') as host,\n";
		sql += "	COALESCE(graha_mail_account_template.port, graha_mail_account.port) as port,\n";
		sql += "	user_name,\n";
		sql += "	password,\n";
		sql += "	mail_check_column\n";
		sql += "from webmua.graha_mail_account\n";
		sql += "left outer join webmua.graha_mail_account_template on graha_mail_account.graha_mail_account_template_id = graha_mail_account_template.graha_mail_account_template_id\n";
		sql += "where graha_mail_account_id = ?\n";
		sql += "	and graha_mail_account.insert_id = ?\n";
		List result = DB.fetch(con, HashMap.class, sql, param);
		if(result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			Encryptor encryptor = new EncryptorAESGCMImpl();
			data.put("password", encryptor.decrypt((String)data.get("password")));
			return data;
		} else {
			return null;
		}
	}
/**
 * 이메일을 eml 파일로 저장한다.
 
 * @param message 저장할 이메일
 * @param data 데이타베이스에서 가져온 이메일에 대한 정보(graha_mail_id 만 사용한다)
 * @param mailAccount 메일계정정보
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param session 
 */
	protected void save(MimeMessage message, HashMap data, HashMap mailAccount, Record params, Session session) throws IOException, MessagingException {
		int graha_mail_account_id = (int)mailAccount.get("graha_mail_account_id");
		String type = (String)mailAccount.get("type");
		String imap_fetch_type = (String)mailAccount.get("imap_fetch_type");
		save(message, data, graha_mail_account_id, type, imap_fetch_type, params, session);
	}
/**
 * 이메일을 eml 파일로 저장한다.
 
 * @param message 저장할 이메일
 * @param data 데이타베이스에서 가져온 이메일에 대한 정보(graha_mail_id 만 사용한다)
 * @param graha_mail_account_id 메일계정의 고유번호
 * @param type 메일계정의 유형(pop3, imap)
 * @param imap_fetch_type IMAP 서버에서 메시지를 가져오는 방식
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param session 
 */
	protected void save(MimeMessage message, HashMap data, int graha_mail_account_id, String type, String imap_fetch_type, Record params, Session session) throws IOException, MessagingException {
		File dir = new File(params.getString("prop.mail.save.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id"));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		if(params.hasKey("prop.mail.backup.directory")) {
			dir = new File(params.getString("prop.mail.backup.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id"));
			if(!dir.exists()) {
				dir.mkdirs();
			}
		}
		File f = null;
		File f_backup = null;
		if(type != null && type.equals("imap") && imap_fetch_type != null && (imap_fetch_type.equals("H") || imap_fetch_type.equals("M"))) {
			f = new File(params.getString("prop.mail.save.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id") + java.io.File.separator + "header.eml");
			if(params.hasKey("prop.mail.backup.directory")) {
				f_backup = new File(params.getString("prop.mail.backup.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id") + java.io.File.separator + "header.eml");
			}
		} else {
			f = new File(params.getString("prop.mail.save.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id") + java.io.File.separator + "mail.eml");
			if(params.hasKey("prop.mail.backup.directory")) {
				f_backup = new File(params.getString("prop.mail.backup.directory") + "eml" + java.io.File.separator + data.get("graha_mail_id") + java.io.File.separator + "mail.eml");
			}
		}
		FileOutputStream fos = null;
		LineOutputStream los = null;
		try {
			fos = new FileOutputStream(f);
			if(type != null && type.equals("imap") && imap_fetch_type != null && (imap_fetch_type.equals("H") || imap_fetch_type.equals("M"))) {
				if(session != null && session.getProperties() != null) {
					los = new LineOutputStream(fos, PropUtil.getBooleanProperty(session.getProperties(), "mail.mime.allowutf8", false));
				} else {
					los = new LineOutputStream(fos, false);
				}
				Enumeration<String> headerLines = message.getNonMatchingHeaderLines(null);
				while(headerLines.hasMoreElements()) {
					String line = (String)headerLines.nextElement();
					los.writeln(line);
				}
				los.writeln();
				los.close();
				los = null;
			} else {
				message.writeTo(fos);
			}
			fos.close();
			fos = null;
			if(f_backup != null) {
				Files.copy(f.toPath(), f_backup.toPath());
			}
		} catch(IOException | MessagingException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			throw e;
		} finally {
			if(los != null) {
				try {
					los.close();
					los = null;
				} catch (IOException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
			if(fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		}
	}
/**
 * graha_mail_folder 테이블에 modseq 컬럼을 update 하거나(before 파라미터가 false 인 경우),
 * graha_mail 테이블에 status 컬럼을 update 한다(before 파라미터가 true 인 경우).
 * modseq 를 지원하지 않는 경우 이 메소드를 호출해서는 안된다.
 
 * @param folder Folder 객체.  폴더이름을 가져오기 위해 입력받는다.
 * @param graha_mail_account_id 메일계정고유번호
 * @param modseq 폴더의 HighestModSeq 값
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @param before 메세지를 처리하기 이전에는 true, 이후에는 false
 */
	private void updateFolder(Folder folder, int graha_mail_account_id, long modseq, Record params, Connection con, boolean before) throws SQLException {
		if(before) {
		} else {
			String sql = "update webmua.graha_mail_folder set modseq = ?\n";
			sql += "where graha_mail_account_id = ?\n";
			sql += "	and insert_id = ?\n";
			sql += "	and name = ?\n";
			Object[] param = new Object[4];
			param[0] = modseq;
			param[1] = graha_mail_account_id;
			param[2] = params.getString("prop.logined_user");
			param[3] = folder.getName();
			DB.execute(con, null, sql, param);
		}
	}
/**
 * 메일 폴더의 type (graha_mail_folder 테이블의 type 컬럼에 들어갈 값)을 가져온다.
 * graha_mail_folder 테이블의 type 컬럼은 1) 정렬 2) Sent, Draft 메일 폴더를 가져오기 위해 사용된다.
 * 이 메소드는 Imap 서버에서 폴더를 가져올 때 사용된다.
  
 * @param folderName Folder 객체.  폴더이름을 가져오기 위해 입력받는다.
 * @param con 데이타베이스 연결(Connection)
 
 * @return 메일 폴더의 Type (Inbox, Sent, Draft, Trash 중 하나)
 
 */
	private String getFolderType(String folderName, Connection con) throws SQLException {
		Object[] param = new Object[1];
		param[0] = folderName;
		String sql = "select value from webmua.graha_mail_common_code\n";
		sql += "where upper_id in (\n";
		sql += "	select graha_mail_common_code_id from webmua.graha_mail_common_code\n";
		sql += "	where upper_id = 0\n";
		sql += "		and code = 'folder_name_type_map'\n";
		sql += ")\n";
		sql += "and code = ?\n";
		sql += "order by order_number, code\n";
		List result = DB.fetch(con, HashMap.class, sql, param);
		if(result != null && result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			return (String)data.get("value");
		}
		return "User";
	}
/**
 * (메일폴더의 이름으로) 메일 폴더의 정보를 가져온다.
 * 만약 메일 폴더가 없는 경우, 메일 폴더를 새로 만든다.
 * 이 메소드는 Imap 서버에서 폴더를 가져올 때 사용된다.

 * @param folder 메일폴더
 * @param graha_mail_account_id 이메일 계정의 고유번호
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @return 메일 폴더 정보 (graha_mail_folder_id, modseq
 *         메일폴더를 새로 만든 경우 추가 되는 항목 : name, type, insert_id, update_id, insert_date, update_date, insert_ip, update_ip

 */
	private HashMap saveFolderIfNotExists(Folder folder, int graha_mail_account_id, Record params, Connection con) throws SQLException {
		return saveFolderIfNotExists(folder.getName(), graha_mail_account_id, params, con);
	}
/**
 * (메일폴더의 이름으로) 메일 폴더의 정보를 가져온다.
 * 만약 메일 폴더가 없는 경우, 메일 폴더를 새로 만든다.
 * 이 메소드는 Imap 서버에서 폴더를 가져올 때 사용된다.

 * @param folderName 폴더이름
 * @param graha_mail_account_id 이메일 계정의 고유번호
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @return 메일 폴더 정보 (graha_mail_folder_id, modseq
 *         메일폴더를 새로 만든 경우 추가 되는 항목 : name, type, insert_id, update_id, insert_date, update_date, insert_ip, update_ip

 */
	protected HashMap saveFolderIfNotExists(String folderName, int graha_mail_account_id, Record params, Connection con) throws SQLException {
		Object[] param = new Object[3];
		param[0] = graha_mail_account_id;
		param[1] = params.getString("prop.logined_user");
		param[2] = folderName;
		String sql = "select graha_mail_folder_id\n";
		sql += "	, modseq\n";
		sql += "	, is_not_fetch\n";
		sql += "from webmua.graha_mail_folder\n"; 
		sql += "where graha_mail_account_id = ?\n";
		sql += "	and insert_id = ?\n";
		sql += "	and name = ?\n";
		List result = DB.fetch(con, HashMap.class, sql, param);
		if(result == null || result.size() == 0) {
			sql = "select nextval('webmua.graha_mail_folder$graha_mail_folder_id')::integer as graha_mail_folder_id\n";
			result = DB.fetch(con, HashMap.class, sql, null);
			if(result.size() > 0) {
				HashMap data = (HashMap)result.get(0);
				data.put("graha_mail_account_id", graha_mail_account_id);
				data.put("name", folderName);
				data.put("type", getFolderType(folderName, con));
				data.put("insert_id", params.getString("prop.logined_user"));
				data.put("update_id", params.getString("prop.logined_user"));
				data.put("insert_date", new java.sql.Timestamp(new java.util.Date().getTime()));
				data.put("update_date", new java.sql.Timestamp(new java.util.Date().getTime()));
				data.put("insert_ip", params.getString("header.remote_addr"));
				data.put("update_ip", params.getString("header.remote_addr"));
				DB.insert(con, data, "webmua.graha_mail_folder");
				return data;
			}
			return null;
		} else {
			return (HashMap)result.get(0);
		}
	}
/**
 * imap 혹은 pop3에서 가져온 이메일을 저장한다.

 * @param info Imap 서버에서 가져온 이메일을 특정하기 위한 최소한이 정보
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @return 메일 정보.  이미 존재하는 이메일이어서 데이타베이스에 이메일을 저장하지 않은 경우, graha_mail_id 가 없다.

 */
	protected HashMap save(ImapMessageMinimalInfo info, Record params, Connection con) throws SQLException {
		String sql = null;
		Object[] param = null;
		List result = null;
		if(info.checkByUID() || (info.getMessageId() != null && !info.getMessageId().equals(""))) {
			if(info.imap()) {
				param = new Object[5];
			} else {
				param = new Object[4];
			}
			param[0] = info.getUid();
			param[1] = params.getString("prop.logined_user");
			param[2] = info.getGrahaMailAccountId();
			param[3] = params.getString("prop.logined_user");
			if(info.imap()) {
				if(info.getFolderId() > 0) {
					param[4] = info.getFolderId();
				} else {
					param[4] = info.getFolderName();
				}
			}
			sql = "	select insert_date\n";
			if(info.checkByUID()) {
				if(info.imap()) {
					if(info.getSupportModSeq()) {
						sql += ", modseq\n";
					} else {
						sql += ", size_long, message_id\n";
					}
				}
			} else {
				sql += ", message_id\n";
			}
			sql += ", graha_mail_id from webmua.graha_mail\n";
			if(info.checkByUID()) {
				if(info.pop3()) {
					sql += "where pop3_uid = ?\n";
				} else {
					sql += "where imap_uid = ?\n";
				}
			} else {
				sql += "where message_id = ?\n";
			}
			sql += "	and insert_id = ?\n";
			sql += "	and graha_mail_folder_id in (\n";
			sql += "		select graha_mail_folder_id from webmua.graha_mail_folder where graha_mail_account_id = ? and insert_id = ?\n";
			if(info.imap()) {
				if(info.getFolderId() > 0) {
					sql += "			and graha_mail_folder_id = ?\n";
				} else {
					sql += "			and name = ?\n";
				}
			}
			sql += "	)\n";
			sql += "	and status in ('F', 'R')\n";
			result = DB.fetch(con, HashMap.class, sql, param);
			if(result != null && result.size() > 0) {
				HashMap data = (HashMap)result.get(0);
				if(info.pop3()) {
					data.remove("graha_mail_id");
					return data;
				}
				long modseq = -1;
				if(data.containsKey("modseq") && data.get("modseq") != null) {
					modseq = (long)data.get("modseq");
				}
				long sizeLong = -1;
				if(data.containsKey("size_long") && data.get("size_long") != null) {
					sizeLong = (long)data.get("size_long");
				}
				String messageId = null;
				if(data.containsKey("message_id") && data.get("message_id") != null) {
					messageId = (String)data.get("message_id");
				}
				if(info.compare(modseq, sizeLong, messageId)) {
					data.remove("graha_mail_id");
					return data;
				} else if(info.checkByUID()) {
					sql = "update webmua.graha_mail set status = 'B' where graha_mail_id = ?";
					param = new Object[1];
					param[0] = (int)data.get("graha_mail_id");
					DB.execute(con, null, sql, param);
				}
			}
			param = null;
		}
		if(info.getFolderId() > 0) {
			param = null;
		} else if(info.getFolderName() != null) {
			param = new Object[3];
		} else {
			param = new Object[2];
		}
		if(param != null) {
			int index = 0;
			param[index++] = info.getGrahaMailAccountId();
			param[index++] = params.getString("prop.logined_user");
			if(info.getFolderName() != null) {
				param[index++] = info.getFolderName();
			}
		}
		sql = "select nextval('webmua.graha_mail$graha_mail_id')::integer as graha_mail_id\n";
		if(info.getFolderId() > 0) {
		} else {
			sql += "	, graha_mail_folder_id \n";
			sql += "from webmua.graha_mail_folder\n"; 
			sql += "where graha_mail_account_id = ?\n";
			sql += "	and insert_id = ?\n";
			if(info.getFolderName() != null) {
				sql += "	and name = ?\n";
			} else {
				sql += "	and name = 'Inbox'\n";
			}
			sql += "limit 1\n";
		}

		result = DB.fetch(con, HashMap.class, sql, param);
		if(result != null && result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			if(info.pop3()) {
				data.put("pop3_uid", info.getUid());
			} else if(info.imap()) {
				data.put("imap_uid", info.getUid());
				if(info.getSupportModSeq()) {
					data.put("modseq", info.getModseq());
				}
				data.put("size_long", info.getSizeLong());
				if(info.getMessageId() != null) {
					data.put("message_id", info.getMessageId());
				}
			}
			if(info.getFolderId() > 0) {
				data.put("graha_mail_folder_id", info.getFolderId());
			}
			if(info.getReceivedDate() != null) {
				data.put("received_date", new java.sql.Timestamp(info.getReceivedDate().getTime()));
			}
			data.put("status", "F");
			data.put("insert_id", params.getString("prop.logined_user"));
			data.put("update_id", params.getString("prop.logined_user"));
			data.put("insert_date", new java.sql.Timestamp(new java.util.Date().getTime()));
			data.put("update_date", new java.sql.Timestamp(new java.util.Date().getTime()));
			data.put("insert_ip", params.getString("header.remote_addr"));
			data.put("update_ip", params.getString("header.remote_addr"));

			DB.insert(con, data, "webmua.graha_mail");
			return data;
		} else {
			return null;
		}
	}
}
