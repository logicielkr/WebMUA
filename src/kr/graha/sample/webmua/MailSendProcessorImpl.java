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
import kr.graha.helper.DB;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;

import java.util.HashMap;
import java.io.File;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import javax.mail.internet.InternetAddress;
import javax.mail.Message.RecipientType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Properties;
import javax.mail.Message;
import java.io.IOException;
import java.io.FileOutputStream;
import java.security.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.Session;
import javax.mail.Transport;
import com.sun.mail.smtp.SMTPTransport;
import kr.graha.post.interfaces.Encryptor;
import java.util.Date;

import kr.graha.app.encryptor.EncryptorAESGCMImpl;

import kr.graha.sample.webmua.interfaces.MessageProcessor;
import java.lang.reflect.InvocationTargetException;

import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;

import java.nio.charset.StandardCharsets;


/**
 * 이메일을 발송한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class MailSendProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public MailSendProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 
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
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60001");
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"))) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_account_id is required!!!"); }
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60002");
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"))) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_account_id is required!!!"); }
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60003");
			return;
		}
		Transport transport = null;
		boolean isConnect = false;
		try {
			HashMap mailAccount = getMailAccount(params, con);
			Properties props = getProps(mailAccount);
			
			if(!mailAccount.containsKey("protocol") || mailAccount.get("protocol") == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60004");
				return;
			}
			if(!mailAccount.containsKey("smtp_host") || mailAccount.get("smtp_host") == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60005");
				return;
			}
			if(!mailAccount.containsKey("smtp_port") || mailAccount.get("smtp_port") == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60006");
				return;
			}
			if(!mailAccount.containsKey("smtp_user_name") || mailAccount.get("smtp_user_name") == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60007");
				return;
			}
			if(!mailAccount.containsKey("smtp_password") || mailAccount.get("smtp_password") == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60008");
				return;
			}
			
			URLName urln = new URLName(
				(String)mailAccount.get("protocol"), 
				(String)mailAccount.get("smtp_host"), 
				(int)mailAccount.get("smtp_port"), 
				null, 
				(String)mailAccount.get("smtp_user_name"), 
				(String)mailAccount.get("smtp_password")
			);
			
			Session session = Session.getInstance(props, null);
			MimeMessage msg = getMessageWithMessageProcessor(mailAccount, params, con, session, request);
			if(msg != null) {
				save(msg, params);
				transport = new SMTPTransport(session, urln);
				transport.connect();
				isConnect = true;
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
				transport = null;
				updateFolderIdAndMessageID(params, msg.getSentDate(), msg.getMessageID(), con);
			}
		} catch (SQLException | NoSuchProviderException | IOException | MessagingException e) {
			if(isConnect || transport == null) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60009");
			} else {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60010");
			}
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		} finally {
			if(transport != null) {
				try {
					transport.close();
					transport = null;
				} catch(MessagingException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		}
	}
/**
 * graha_mail 의 graha_mail_folder_id 를 Sent(graha_mail_folder 의 type 기준) 로 업데이트 한다.
 * sent_date, update_date, update_id, update_ip 도 업데이트 한다.
 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param sentDate 메일 발송 일자(Date 헤더)
 * @param con 데이타베이스 연결(Connection)

 */
	public void updateFolderIdAndMessageID(Record params, Date sentDate, String messageID, Connection con) throws SQLException {
		int index = 5;
		String sql = "update webmua.graha_mail\n";
		sql += "set graha_mail_folder_id = (select graha_mail_folder_id from webmua.graha_mail_folder where graha_mail_account_id = ? and type = 'Sent' limit 1),\n";
		if(sentDate != null) {
			sql += "	sent_date = ?,\n";
			index++;
		}
		if(messageID != null) {
			sql += "	message_id = ?,\n";
			index++;
		}
		sql += "	update_date = ?,\n";
		sql += "	update_id = ?,\n";
		sql += "	update_ip = ?\n";
		sql += "where graha_mail_id = ?\n";
		Object[] param = new Object[index];
		param[0] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"));
		index = 0;
		if(sentDate != null) {
			index++;
			param[index] = new java.sql.Timestamp(sentDate.getTime());
		}
		if(messageID != null) {
			index++;
			param[index] = messageID;
		}
		param[index + 1] = new java.sql.Timestamp(new java.util.Date().getTime());
		param[index + 2] = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "logined_user"));
		param[index + 3] = params.getString(Record.key(Record.PREFIX_TYPE_HEADER, "remote_addr"));
		param[index + 4] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"));
		DB.execute(con, null, sql, param);
	}
/**
 * 이메일을 eml 파일로 저장한다.
 
 * @param message 저장할 이메일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	public void save(Message message, Record params) throws IOException, MessagingException {
		File dir = new File(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory")) + "eml" + java.io.File.separator + params.getObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id")));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory")) + "eml" + java.io.File.separator + params.getObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id")) + java.io.File.separator + "mail.eml");
		File f_backup = null;
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "mail.backup.directory"))) {
			dir = new File(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.backup.directory")) + "eml" + java.io.File.separator + params.getObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id")));
			if(!dir.exists()) {
				dir.mkdirs();
			}
			f_backup = new File(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.backup.directory")) + "eml" + java.io.File.separator + params.getObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id")) + java.io.File.separator + "mail.eml");
		}
		FileOutputStream fos = null;
		FileOutputStream fos_backup = null;
		try {
			fos = new FileOutputStream(f);
			message.writeTo(fos);
			if(f_backup != null) {
				fos_backup = new FileOutputStream(f_backup);
				message.writeTo(fos_backup);
				fos_backup.close();
				fos_backup = null;
			}
			fos.close();
			fos = null;
		} catch(IOException | MessagingException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			throw e;
		} finally {
			if(fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
			if(fos_backup != null) {
				try {
					fos_backup.close();
					fos_backup = null;
				} catch (IOException e) {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
			}
		}
	}
/**
 * URI 객체로부터 파일이름을 가져온다.
 
 * @param uri URI 객체(Path.toURI() 메소드의 결과)
 * @return 파일이름
 */
	public String decodeFileName(URI uri) {
		try {
			return URLDecoder.decode(uri.toString().substring(uri.toString().lastIndexOf("/") + 1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.warning(LOG.toString(e)); }
			return uri.toString().substring(uri.toString().lastIndexOf("/")+1);
		}
	}
	public MimeMessage getMessageWithMessageProcessor(
		HashMap mailAccount, Record params, Connection con, Session session, HttpServletRequest request
	) throws 
		AddressException, 
		UnsupportedEncodingException, 
		SQLException, 
		MessagingException
	{
		MimeMessage msg = getMessage(mailAccount, params, con, session, request);
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "message.processor"))) {
			if(params.isArray(Record.key(Record.PREFIX_TYPE_PROP, "message.processor"))) {
				List list = params.getArray(Record.key(Record.PREFIX_TYPE_PROP, "message.processor"));
				if(list != null) {
					for(int i = 0; i < list.size(); i++) {
						msg = executeMessageProcessor(msg, params, (String)list.get(i));
					}
				}
			} else {
				msg = executeMessageProcessor(msg, params, params.getString(Record.key(Record.PREFIX_TYPE_PROP, "message.processor")));
			}
		}
		return msg;
	}
	public MimeMessage executeMessageProcessor(MimeMessage msg, Record params, String messageProcessor) throws MessagingException {
		MessageProcessor mp = null;
		try {
			mp = (MessageProcessor)Class.forName(messageProcessor).getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
		if(mp != null) {
			return mp.execute(msg, params);
		}
		return msg;
	}
/**
 * 데이타베이스 및 파일에서 이메일(MimeMessage 객체)을 조합한다.
 
 * @param mailAccount 메일계정정보
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @return 이메일(MimeMessage 객체)
 */
	public MimeMessage getMessage(
		HashMap mailAccount, Record params, Connection con, Session session, HttpServletRequest request
	) throws 
		AddressException, 
		UnsupportedEncodingException, 
		SQLException, 
		MessagingException
	{
		MimeMessage msg = new MimeMessage(session);
		String charset = StandardCharsets.UTF_8.name();
		Object[] param = new Object[2];
		param[0] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, ("graha_mail_id")));
		param[1] = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "logined_user"));
		String sql = "select\n";
		sql += "	graha_mail.subject,\n"; 
		sql += "	re_mail.message_id as re_message_id,\n";
		sql += "	fwd_mail.message_id as fwd_message_id\n";
		sql += "from webmua.graha_mail\n";
		sql += "	left outer join webmua.graha_mail re_mail on re_mail.graha_mail_id = graha_mail.graha_mail_rid\n";
		sql += "	left outer join webmua.graha_mail fwd_mail on fwd_mail.graha_mail_id = graha_mail.graha_mail_fid\n";
		sql += "where graha_mail.graha_mail_id = ?\n";
		sql += "	and exists (\n";
		sql += "		select * from webmua.graha_mail_folder where graha_mail.graha_mail_folder_id = graha_mail_folder.graha_mail_folder_id\n";
		sql += "		and type = 'Draft'\n";
		sql += "	)\n";
		sql += "	and graha_mail.insert_id = ?\n";
		
		List result = DB.fetch(con, HashMap.class, sql, param);
		if(result != null && result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			if(data.get("subject") != null && !((String)data.get("subject")).equals("")) {
				msg.setSubject((String)data.get("subject"), charset);
			} else {
				if(logger.isLoggable(Level.SEVERE)) { logger.severe("subject is null"); }
				return null;
			}
			if(data.containsKey("re_message_id") && data.get("re_message_id") != null && !((String)data.get("re_message_id")).equals("")) {
				msg.addHeader("References", (String)data.get("re_message_id"));
				msg.addHeader("In-Reply-To", (String)data.get("re_message_id"));
			} else if(data.containsKey("fwd_message_id") && data.get("fwd_message_id") != null && !((String)data.get("fwd_message_id")).equals("")) {
				msg.addHeader("References", (String)data.get("fwd_message_id"));
				msg.addHeader("X-Forwarded-Message-Id", (String)data.get("fwd_message_id"));
				msg.addHeader("In-Reply-To", (String)data.get("fwd_message_id"));
			}
		} else {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("mail is null"); }
			return null;
		}
		sql = "select type, personal_name, email_address from webmua.graha_mail_address where graha_mail_id = ? and insert_id = ?";
		result = DB.fetch(con, HashMap.class, sql, param);
		if(result != null && result.size() > 0) {
			InternetAddress address = new InternetAddress((String)mailAccount.get("email"));
			if(mailAccount.get("name") != null && !((String)mailAccount.get("name")).equals("")) {
				address.setPersonal((String)mailAccount.get("name"), charset);
			}
			msg.setFrom(address);
			msg.setSentDate(new java.util.Date());
			String to = null;
			boolean error_email_address = false;
			for(Object v : result) {
				HashMap data = (HashMap)v;
				if(data.get("email_address") != null && !((String)data.get("email_address")).equals("")) {
					String email_address = (String)data.get("email_address");
					if(email_address.indexOf("@") > 0 && email_address.lastIndexOf(".") > 0 && email_address.lastIndexOf(".") > email_address.indexOf("@")) {
						address = new InternetAddress(email_address);
						if(data.get("personal_name") != null && !((String)data.get("personal_name")).equals("")) {
							address.setPersonal((String)data.get("personal_name"), charset);
						}
						if(data.get("type") != null && ((String)data.get("type")).equals("TO")) {
							msg.addRecipients(RecipientType.TO, new InternetAddress[]{address});
							to = email_address;
						} else if(data.get("type") != null && ((String)data.get("type")).equals("CC")) {
							msg.addRecipients(RecipientType.CC, new InternetAddress[]{address});
						} else if(data.get("type") != null && ((String)data.get("type")).equals("BCC")) {
							msg.addRecipients(RecipientType.BCC, new InternetAddress[]{address});
						}
					} else {
						if(logger.isLoggable(Level.SEVERE)) { logger.severe("email_address is null or blank(" + email_address + ")"); }
						params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60014");
						error_email_address = true;
					}
				} else {
					if(logger.isLoggable(Level.SEVERE)) { logger.severe("email_address is null or blank"); }
					params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60013");
					error_email_address = true;
				}
			}
			if(error_email_address) {
				return null;
			} else if(to != null) {
				String contents = null;
				sql = "select contents from webmua.graha_mail_part where graha_mail_id = ? and insert_id = ?";
				result = DB.fetch(con, HashMap.class, sql, param);
				if(result != null && result.size() > 0) {
					HashMap data = (HashMap)result.get(0);
					contents = (String)data.get("contents");
				}
				String path = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory")) + "attach" + File.separator + params.getObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"));
				Multipart multipart = null;
				if(Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path))) {
					DirectoryStream<Path> stream = null;
					try {
						stream = Files.newDirectoryStream(Paths.get(path));
						int index = 0;
						for(Path file : stream) {
							if(file.toFile().isFile()) {
								if(index == 0) {
									multipart = new MimeMultipart("mixed");
								}
								String fileName = decodeFileName(file.toUri());
								MimeBodyPart part = new MimeBodyPart();
								part.attachFile(file.toFile());
								String base64EncodedFileName = MimeUtility.encodeText(fileName, charset, "B");
								base64EncodedFileName = base64EncodedFileName.replace("?= =?" + charset + "?B?", "?=\r\n\t=?" + charset + "?B?");
								
								String contentType = request.getServletContext().getMimeType(fileName) + ";\r\n";
								contentType += "\t";
								contentType += "name=\"" + base64EncodedFileName + "\"";
								part.setHeader("Content-Type", contentType);
								
								String contentDisposition = part.ATTACHMENT + ";\r\n";
								contentDisposition += "\t";
								contentDisposition += "filename=\"" + base64EncodedFileName + "\"";
								part.setHeader("Content-Disposition", contentDisposition);

								multipart.addBodyPart(part);
								index++;
							}
						}
						stream.close();
						stream = null;
					} catch(IOException ex) {
						if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(ex)); }
					} finally {
						if(stream != null) {
							try {
								stream.close();
								stream = null;
							} catch(IOException e) {
								if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
							}
						}
					}
				}
				if(multipart != null) {
					if(contents != null) {
						MimeBodyPart part = new MimeBodyPart();
						part.setText(contents, charset, "plain");
						multipart.addBodyPart(part);
					}
					msg.setContent(multipart);
				} else {
					if(contents != null) {
						msg.setText(contents, charset, "plain");
					} else {
						msg.setText("", charset, "plain");
					}
				}
			} else {
				if(logger.isLoggable(Level.SEVERE)) { logger.severe("to address is null"); }
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60011");
				return null;
			}
		} else {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("address is null"); }
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.60012");
			return null;
		}
		return msg;
	}
/**
 * 이메일 서버 연결에 필요한 Properties 를 가져온다.
 
 * @param mailAccount 데이타베이스에서 가져온 이메일 계정에 대한 정보
 * @return 이메일 서버 연결에 필요한 Properties
 */
	public Properties getProps(HashMap mailAccount) {
		Properties props = new Properties();
		if(
			mailAccount.get("smtp_encryption_type") != null && 
			!((String)mailAccount.get("smtp_encryption_type")).equals("plain")
		) {
			props.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtps.socketFactory.fallback", "false");
			props.setProperty("mail.smtps.socketFactory.port", Integer.toString((int)mailAccount.get("smtp_port")));
			props.setProperty("mail.smtps.ssl.trust", "*");
			if(((String)mailAccount.get("smtp_encryption_type")).equals("STARTTLS")) {
				props.setProperty("mail.smtps.starttls.enable", "true");
			} else if(((String)mailAccount.get("smtp_encryption_type")).equals("SSL/TSL")) {
				props.setProperty("mail.smtps.ssl.enable", "true");
			}
			props.setProperty("mail.smtps.port", Integer.toString((int)mailAccount.get("smtp_port")));
		}
		return props;
	}
/**
 * 데이타베이스에서 이메일 계정에 대한 정보를 가져온다.
 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @return 데이타베이스에서 가져온 이메일 계정에 대한 정보
 */
	public HashMap getMailAccount(Record params, Connection con)
		throws SQLException, java.security.NoSuchProviderException
	{
		String sql = null;
		Object[] param = new Object[2];
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"))) {
			param[0] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_account_id"));
		} else {
			return null;
		}
		param[1] = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "logined_user"));
		sql = "select\n";
		sql += "	graha_mail_account_id,\n";
		sql += "	COALESCE(graha_mail_account_template.smtp_encryption_type, graha_mail_account.smtp_encryption_type) as smtp_encryption_type,\n";
		sql += "	REGEXP_REPLACE(COALESCE(graha_mail_account_template.smtp_host, graha_mail_account.smtp_host), '^\\s+|\\s+$', '', 'g') as smtp_host,\n";
		sql += "	COALESCE(graha_mail_account_template.smtp_port, graha_mail_account.smtp_port) as smtp_port,\n";
		sql += "	COALESCE(smtp_user_name, user_name) as smtp_user_name,\n";
		sql += "	COALESCE(smtp_password, password) as smtp_password,\n";
		sql += "	email,\n";
		sql += "	graha_mail_account.name,\n";
		sql += "	case when COALESCE(graha_mail_account_template.smtp_encryption_type, graha_mail_account.smtp_encryption_type) = 'plain' then 'smtp' else 'smtps' end as protocol\n";
		sql += "from webmua.graha_mail_account\n";
		sql += "	left outer join webmua.graha_mail_account_template on graha_mail_account.graha_mail_account_template_id = graha_mail_account_template.graha_mail_account_template_id\n";
		sql += "where graha_mail_account_id = ?\n";
		sql += "	and graha_mail_account.insert_id = ?\n";
		List result = DB.fetch(con, HashMap.class, sql, param);
		if(result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			Encryptor encryptor = new EncryptorAESGCMImpl();
			data.put("smtp_password", encryptor.decrypt((String)data.get("smtp_password")));
			return data;
		} else {
			return null;
		}
	}
}

