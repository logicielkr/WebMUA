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
import java.util.Properties;
import javax.mail.Session;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;

import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeUtility;
import javax.mail.Address;
import java.io.UnsupportedEncodingException;
import javax.mail.internet.InternetAddress;
import javax.mail.Message.RecipientType;
import java.nio.file.Files;
import java.util.Date;
import java.sql.Timestamp;
import java.util.Enumeration;
import javax.mail.Header;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.mail.internet.AddressException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;
import java.nio.ByteBuffer;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;


/**
 * IMAP4 혹은 POP3 에서 가져온 이메일을 데이타베이스에 집어 넣는다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class MailParserProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private int BCC = 1;
	private int CC = 2;
	private int TO = 3;
	private int FROM = 4;
	private int ReplyTo = 5;
	private int SENDER = 6;
	private int ALLRCPT = 9;
	public MailParserProcessorImpl() {
		
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
 * @see kr.graha.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		this.execute(params, con);
	}
	public void execute(Record params, Connection con) {
		if(!params.hasKey("prop.mail.save.directory")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put("error.error", "message.30001");
			return;
		}
		if(!params.hasKey("param.graha_mail_account_id")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_account_id is required!!!"); }
			params.put("error.error", "message.30002");
			return;
		}
		int graha_mail_account_id = params.getInt("param.graha_mail_account_id");
		String mailSaveDirectory = params.getString("prop.mail.save.directory");
		String mailBackupDirectory = null;
		if(params.hasKey("prop.mail.backup.directory")) {
			mailBackupDirectory = params.getString("prop.mail.backup.directory");
		}
		
		Object[] param = new Object[2];
		param[0] = params.getString("prop.logined_user");
		param[1] = graha_mail_account_id;
		
		String sql = null;

		sql = "select\n";
		sql += "	graha_mail.graha_mail_id,\n";
		sql += "	graha_mail.default_charset as mail_default_charset,\n";
		sql += "	graha_mail_folder.default_charset as folder_default_charset,\n";
		sql += "	graha_mail_account.default_charset as account_default_charset\n";
		sql += "from webmua.graha_mail\n";
		sql += "	left join webmua.graha_mail_folder\n"; 
		sql += "		on graha_mail_folder.graha_mail_folder_id = graha_mail.graha_mail_folder_id\n";
		sql += "	left join webmua.graha_mail_account \n";
		sql += "		on graha_mail_account.graha_mail_account_id = graha_mail_folder.graha_mail_account_id\n";
		sql += "where graha_mail.insert_id = ?\n";
		sql += "	and graha_mail_account.graha_mail_account_id = ?\n";
		sql += "	and status = 'F'\n";

		try {
			List result = DB.fetch(con, MailCharsetInfo.class, sql, param);
			if(result.size() > 0) {
				int savedMailCount = 0;
				for (Object v : result) {
					MailCharsetInfo mailInfo = (MailCharsetInfo)v;
					if(saveMail(mailInfo, con, params, mailSaveDirectory, mailBackupDirectory)) {
						savedMailCount++;
					}
				}
				if(savedMailCount > 0) {
					params.put("result.savedMailCount", savedMailCount);
				}
			} else {
			}
		} catch(SQLException | MessagingException | IOException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
	}
/**
 * 이메일을 저장한다(데이타베이스 및 첨부파일).
 
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @param mailBackupDirectory 메일을 저장(백업)할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	protected boolean saveMail(
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params,
		String mailSaveDirectory,
		String mailBackupDirectory
	) throws SQLException, MessagingException, IOException {
		File eml = new File(mailSaveDirectory + "eml" + File.separator + mailInfo.getGrahaMailId().intValue() + File.separator + "mail.eml");
		if(eml.exists() && eml.length() > 2) {
			MimeMessage mime = getMessage(eml);
			if(mime != null) {
				return save(mime, mailInfo, con, params, mailSaveDirectory, mailBackupDirectory, false);
			}
		}
		eml = new File(mailSaveDirectory + "eml" + File.separator + mailInfo.getGrahaMailId().intValue() + File.separator + "header.eml");
		if(eml.exists() && eml.length() > 2) {
			MimeMessage mime = getMessage(eml);
			if(mime != null) {
				return save(mime, mailInfo, con, params, mailSaveDirectory, mailBackupDirectory, true);
			}
		}
		String sql = "update webmua.graha_mail set status = 'X' where graha_mail_id = ?";
		Object[] param = new Object[1];
		param[0] = mailInfo.getGrahaMailId();
		DB.execute(con, null, sql, param);
		return false;
	}
/**
 * eml 파일로부터 이메일(MimeMessage 객체)를 가져온다.
 
 * @param eml 이메일 원본파일
 * @return 이메일(MimeMessage 객체)
 */
	private MimeMessage getMessage(File eml) {
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		MimeMessage mime = null;
		Properties props = new Properties();
		props.setProperty("mail.mime.decodefilename", "false");
		try {
			Session session = Session.getInstance(props, null);
			fis = new FileInputStream(eml);
			bis = new BufferedInputStream(fis);
			mime = new MimeMessage(session, bis);
			bis.close();
			bis = null;
			fis.close();
			fis = null;
		} catch (IOException | MessagingException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		} finally {
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
				}
			}
			if(bis != null) {
				try {
					bis.close();
					bis = null;
				} catch (IOException e) {
				}
			}
		}
		return mime;
	}
/**
 * 이메일을 저장한다(데이타베이스 및 첨부파일).
 
 * @param eml 이메일 원본파일
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @param mailBackupDirectory 메일을 저장(백업)할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @return 성공여부
 */
	private boolean save(
		MimeMessage mime, 
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params,
		String mailSaveDirectory,
		String mailBackupDirectory,
		boolean onlyHeader
	) throws MessagingException, IOException, SQLException
	{
		try {
			mailInfo.setGuessCharset(guessCharset(mime));
			clear(mailInfo, con, mailSaveDirectory, mailBackupDirectory);
			if(!onlyHeader) {
				parse(mime, mailInfo, mime.getContentType(), "0", con, params, mailSaveDirectory, mailBackupDirectory);
			}
			saveAddress(mime, mailInfo, con, params);
			updateMail(mime, mailInfo, con, params);
			return true;
		} catch(MessagingException | IOException | SQLException e) {
			throw e;
		}
	}
/**
 * 이미 저장된 이메일의 캐시(데이타베이스 및 첨부파일)을 삭제한다.
 
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @param mailBackupDirectory 메일을 저장(백업)할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	private void clear(
		MailCharsetInfo mailInfo, 
		Connection con,
		String mailSaveDirectory,
		String mailBackupDirectory
	) throws SQLException {
		Object[] param = new Object[1];
		param[0] = mailInfo.getGrahaMailId();
		String sql = "delete from webmua.graha_mail_part where graha_mail_id = ?";
		DB.execute(con, null, sql, param);
		sql = "delete from webmua.graha_mail_address where graha_mail_id = ?";
		DB.execute(con, null, sql, param);
		if(mailSaveDirectory != null) {
			clearAttach(mailInfo, mailSaveDirectory);
		}
		if(mailBackupDirectory != null) {
			clearAttach(mailInfo, mailBackupDirectory);
		}
	}
/**
 * 이미 저장된 이메일의 캐시 중 첨부파일을 삭제한다.
 
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param target 메일을 저장한 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	private void clearAttach(MailCharsetInfo mailInfo, String target) {
		String path = target + "attach" + File.separator + mailInfo.getGrahaMailId();
		File dir = new File(path + File.separator);
		if(dir.exists()) {
			File[] files = dir.listFiles();
			if(files != null) {
				for(int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}
	}
/**
 * 주소를 데이타베이스에 저장한다.
 
 * @param eml 이메일 원본파일
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	private void saveAddress(
		MimeMessage mime,
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params
	) throws MessagingException, SQLException {
		saveAddress("FROM", getAddress(mime, mailInfo, FROM, true), mailInfo, con, params);
		saveAddress("Reply-To", getAddress(mime, mailInfo, ReplyTo, true), mailInfo, con, params);
		saveAddress("TO", getAddress(mime, mailInfo, TO, true), mailInfo, con, params);
		saveAddress("CC", getAddress(mime, mailInfo, CC, true), mailInfo, con, params);
		saveAddress("BCC", getAddress(mime, mailInfo, BCC, true), mailInfo, con, params);
	}
/**
 * 주소를 데이타베이스에 저장한다.
 * @param type 유형(FROM, Reply-To, TO, CC, BCC)
 * @param address 이메일 주소
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	private void saveAddress(
		String type,
		Address[] address,
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params
	) throws SQLException {
		if(address != null && address.length > 0) {
			for(int i = 0; i < address.length; i++) {
				String personal = ((InternetAddress)address[i]).getPersonal();
				String adr = ((InternetAddress)address[i]).getAddress();
				if(adr != null && adr.equals("null")) {
					continue;
				}
				saveAddress(
					type,
					personal,
					adr,
					mailInfo,
					con,
					params
				);
			}
		}
	}
/**
 * 주소를 데이타베이스에 저장한다.
 * @param type 유형(FROM, Reply-To, TO, CC, BCC)
 * @param personalName 이메일 주소의 이름
 * @param emailAddress 이메일 주소
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	private void saveAddress(
		String type,
		String personalName,
		String emailAddress,
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params
	) throws SQLException {
		String sql = null;
		sql = "select nextval('webmua.graha_mail_address$graha_mail_address_id')::integer as graha_mail_address_id\n"; 
		List result = DB.fetch(con, HashMap.class, sql, null);
		if(result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			if(type != null) {
				data.put("type", type);
			}
			if(personalName != null) {
				data.put("personal_name", personalName);
			}
			if(emailAddress != null) {
				data.put("email_address", emailAddress);
			}
			data.put("graha_mail_id", mailInfo.getGrahaMailId());
			data.put("insert_id", params.getString("prop.logined_user"));
			data.put("update_id", params.getString("prop.logined_user"));
			data.put("insert_date", new Timestamp(new Date().getTime()));
			data.put("update_date", new Timestamp(new Date().getTime()));
			data.put("insert_ip", params.getString("header.remote_addr"));
			data.put("update_ip", params.getString("header.remote_addr"));
			DB.insert(con, data, "webmua.graha_mail_address");
		} else {
			throw new SQLException("fail next value sequence(graha_mail_address_id)");
		}
	}
/**
 * 데이타베이스에 이메일 정보를 갱신한다.
 
 * @param eml 이메일 원본파일
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	private void updateMail(
		MimeMessage mime, 
		MailCharsetInfo mailInfo, 
		Connection con,
		Record params
	) throws MessagingException, UnsupportedEncodingException, SQLException {
		int index = 6;
		String sql = "update webmua.graha_mail\n";
		sql += "set subject = ?,\n";
		sql += "	message_id = ?,\n";
		if(mime.getSentDate() != null) {
			sql += "	sent_date = ?,\n";
			index++;
		} else {
			String[] headers = mime.getHeader("Date");
			if(headers != null && headers.length > 0) {
				java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
				for(int i = 0; i < headers.length; i++) {
					try {
						dateFormat.parse(headers[i]);
						sql += "	sent_date = ?,\n";
						index++;
						break;
					} catch (java.text.ParseException e) {
					}
				}
			}
		}
		if(mailInfo.getMailNewCharset() != null) {
			sql += "	default_charset = ?,\n";
			index++;
		}
		
		String[] labels = mime.getHeader("X-Gmail-Labels");
		if(labels != null && labels.length > 0) {
			sql += "	label = ?,\n";
			index++;
		}
		
		sql += "	status = 'R',\n";
		sql += "	update_date = ?,\n";
		sql += "	update_id = ?,\n";
		sql += "	update_ip = ?\n";
		sql += "where graha_mail_id = ?\n";
		Object[] param = new Object[index];
		String subject = getSubject(mime, mailInfo);
		if(subject != null) {
			param[0] = subject.replaceAll("\u0000", "");
		} else {
			param[0] = null;
		}
		param[1] = mime.getMessageID();
		index = 1;
		if(mime.getSentDate() != null) {
			index++;
			param[index] = new Timestamp(mime.getSentDate().getTime());
		} else {
			String[] headers = mime.getHeader("Date");
			if(headers != null && headers.length > 0) {
				java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
				for(int i = 0; i < headers.length; i++) {
					try {
						Timestamp t = new Timestamp(dateFormat.parse(headers[i]).getTime());
						index++;
						param[index] = t;
						break;
					} catch (java.text.ParseException e) {
					}
				}
			}
		}
		if(mime.getReceivedDate() != null) {
			index++;
			param[index] = new Timestamp(mime.getReceivedDate().getTime());
		}
		if(mailInfo.getMailNewCharset() != null) {
			index++;
			param[index] = mailInfo.getMailNewCharset();
		}
		
		if(labels != null && labels.length > 0) {
			index++;
			param[index] = javax.mail.internet.MimeUtility.decodeText(labels[0]);
		}
		
		param[index + 1] = new Timestamp(new Date().getTime());
		param[index + 2] = params.getString("prop.logined_user");
		param[index + 3] = params.getString("header.remote_addr");
		param[index + 4] = mailInfo.getGrahaMailId();
		DB.execute(con, null, sql, param);
	}
/**
 * ParseException 을 처리한다.
 * content type 이 multipart/alternative 이고, boundary 도 정의되어 있지만,
 * 본문에 boundary 가 없는 메시지를 처리한다.

 * @param part 이메일 본문
 * @param error javax.mail.internet.ParseException
 */
	private Object handleError(
		javax.mail.Part part,
		javax.mail.internet.ParseException error
	) {
		if(part instanceof MimeMessage) {
			try {
				String contentType = part.getContentType();
				if(
					contentType != null &&
					contentType.startsWith("multipart/alternative") &&
					error.getMessage() != null &&
					error.getMessage().equals("Missing start boundary")
				) {
					part.removeHeader("Content-Type");
					Object content = part.getContent();
					return content;
				}
			} catch (MessagingException | IOException e) {
				return null;
			}
		}
		return null;
	}
/**
 * 이메일 본문을 데이타베이스에, 혹은 첨부파일을 저장한다.
 * 이메일이 여러 개의 part로 구성된 경우 재귀호출한다.
 
 * @param part 이메일 본문
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param parentContentType 상위 ContentType
 * @param path 경로
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @param mailBackupDirectory 메일을 저장(백업)할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	private void parse(
		javax.mail.Part part,
		MailCharsetInfo mailInfo, 
		String parentContentType,
		String path,
		Connection con,
		Record params,
		String mailSaveDirectory,
		String mailBackupDirectory
	) throws MessagingException, IOException, SQLException
	{
		String parentType = parentContentType;
		if(parentType == null) {
			parentType = "";
		} else if(parentType.indexOf(";") > 0) {
			parentType = parentType.substring(0, parentType.indexOf(";"));
		}
		Object content = null;
		boolean parseException = false;
		String beforeContentType = null;
		try {
			beforeContentType = part.getContentType();
			content = part.getContent();
		} catch(java.io.UnsupportedEncodingException e) {
			content = null;
		} catch(javax.mail.internet.ParseException e) {
			content = handleError(part, e);
			if(content == null) {
				return;
			} else {
				parseException = true;
				part.removeHeader("Content-Type");
			}
		}
		if(content == null) {
			String contentType = part.getContentType();
			if(part.isMimeType("text/html")) {
				contentType = "text/html";
			} else if(part.isMimeType("text/plain") || part.isMimeType("text/text")) {
				contentType = "text/plain";
			}
			InputStream is = part.getInputStream();
			byte[] b = new byte[is.available()];
			is.read(b);
			is.close();
			String charset = guessContentCharset(part, mailInfo);
			if(charset != null) {
				savePart(contentType, null, new String(b, charset), mailInfo, parentType, path, con, params);
			} else {
				savePart(contentType, null, new String(b), mailInfo, parentType, path, con, params);
			}
			b = null;
		} else if(content instanceof Multipart) {
			Multipart multi = (Multipart)content;
			for (int i = 0; i < multi.getCount(); i++) {
				javax.mail.Part p = multi.getBodyPart(i);
				if(parseException) {
					parse(p, mailInfo, beforeContentType, path + "_" + i, con, params, mailSaveDirectory, mailBackupDirectory);
				} else {
					parse(p, mailInfo, part.getContentType(), path + "_" + i, con, params, mailSaveDirectory, mailBackupDirectory);
				}
			}
		} else if(part.isMimeType("message/rfc822")) {
			InputStream is = part.getInputStream();
			MimeMessage mime = new MimeMessage(null, is);
			is.close();
			if(mime.getContent() instanceof Multipart) {
				Multipart multi = (Multipart)mime.getContent();
				for (int i = 0; i < multi.getCount(); i++) {
					javax.mail.Part p = multi.getBodyPart(i);
					if(parseException) {
						parse(p, mailInfo, beforeContentType, path + "_" + i, con, params, mailSaveDirectory, mailBackupDirectory);
					} else {
						parse(p, mailInfo, part.getContentType(), path + "_" + i, con, params, mailSaveDirectory, mailBackupDirectory);
					}
				}
			} else if(mime.getContent() instanceof String) {
				String contentType = mime.getContentType();
				if(mime.isMimeType("text/html")) {
					contentType = "text/html";
				} else if(mime.isMimeType("text/plain")) {
					contentType = "text/plain";
				}
				String contentString = null;
				if(mime.getContent() != null) {
					contentString = mime.getContent().toString();
				}
				if(contentString != null) {
					String charset = guessContentCharset(mime, mailInfo);
					if(charset != null) {
						contentString = new String(contentString.getBytes("iso-8859-1"), charset);
					}
					savePart(contentType, part.getDisposition(), contentString, mailInfo, parentType, path, con, params);
				}
			} else {
				if(parseException) {
					parse((javax.mail.Part)mime.getContent(), mailInfo, beforeContentType, path + "_0", con, params, mailSaveDirectory, mailBackupDirectory);
				} else {
					parse((javax.mail.Part)mime.getContent(), mailInfo, part.getContentType(), path + "_0", con, params, mailSaveDirectory, mailBackupDirectory);
				}
			}
		} else if(part.isMimeType("message/delivery-status")) {
			InputStream is = part.getInputStream();
			byte[] b = new byte[is.available()];
			is.read(b);
			is.close();
			String charset = guessContentCharset(part, mailInfo);
			if(charset != null) {
				savePart("text/plain", part.getDisposition(), new String(b, charset), mailInfo, parentType, path, con, params);
			} else {
				savePart("text/plain", part.getDisposition(), new String(b), mailInfo, parentType, path, con, params);
			}
			b = null;
		} else if((part.getDisposition() != null && part.getDisposition().equalsIgnoreCase("attachment"))) {
			saveFile(part, mailInfo, mailSaveDirectory, mailBackupDirectory);
		} else if(content instanceof InputStream) {
			String contentType = null;
			if(parseException) {
				contentType = beforeContentType;
			} else {
				contentType = part.getContentType();
			}
			if(part.getFileName() != null) {
				saveFile(part, mailInfo, mailSaveDirectory, mailBackupDirectory);
			} else {
				if(part.isMimeType("text/html")) {
					contentType = "text/html";
				} else if(part.isMimeType("text/plain") || part.isMimeType("text/text")) {
					contentType = "text/plain";
				}
				InputStream is = part.getInputStream();
				byte[] b = new byte[is.available()];
				is.read(b);
				is.close();
				String charset = guessContentCharset(part, mailInfo);
				if(charset != null) {
					savePart(contentType, null, new String(b, charset), mailInfo, parentType, path, con, params);
				} else {
					savePart(contentType, null, new String(b), mailInfo, parentType, path, con, params);
				}
				
				b = null;
			}
		} else {
			if(content != null && content.toString() != null && !content.toString().trim().equals("")) {
				String contentType = null;
				if(parseException) {
					contentType = beforeContentType;
				} else {
					contentType = part.getContentType();
				}
				
				if(part.isMimeType("text/html")) {
					contentType = "text/html";
				} else if(part.isMimeType("text/plain")) {
					contentType = "text/plain";
				}

				String contentString = content.toString();
				if(contentString != null) {
					String charset = guessContentCharset(part, mailInfo);
					if(charset != null) {
						contentString = new String(contentString.getBytes("iso-8859-1"), charset);
					}
					savePart(contentType, null, contentString, mailInfo, parentType, path, con, params);
				}
			}
		}
	}
/**
 * 이메일 본문 중 ContentType 이 "text/*" 인 것 들의 charset를 판별한다.
 
 * @param part 이메일 본문
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 */
	private String guessContentCharset(
		javax.mail.Part part,
		MailCharsetInfo mailInfo
	) throws MessagingException {
		if(part instanceof MimeMessage) {
			MimeMessage mime = (MimeMessage)part;
			if(mime.getContentLanguage() == null && mime.getEncoding() == null) {
				String contentType = mime.getContentType();
				if(contentType != null) {
					contentType = contentType.toLowerCase();
					if(contentType.indexOf("charset") == -1) {
						if(mailInfo.getContentCharset() != null) {
							return mailInfo.getContentCharset();
						}
					}
				}
			}
		} else {
			String contentType = part.getContentType();
			if(contentType != null) {
				contentType = contentType.toLowerCase();
				if(contentType.indexOf("charset") == -1) {
					if(mailInfo.getContentCharset() != null) {
						return mailInfo.getContentCharset();
					}
				}
			}
		}
		return null;
	}
/**
 * 첨부파일을 저장한다.
 
 * @param part 이메일 본문
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 * @param mailBackupDirectory 메일을 저장(백업)할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	private void saveFile(
		javax.mail.Part part,
		MailCharsetInfo mailInfo,
		String mailSaveDirectory,
		String mailBackupDirectory
	) throws IOException, MessagingException
	{
		if(mailSaveDirectory != null) {
			saveFile(part, mailInfo, mailSaveDirectory);
		}
		if(mailBackupDirectory != null) {
			saveFile(part, mailInfo, mailBackupDirectory);
		}
	}
/**
 * 첨부파일을 저장한다.
 
 * @param part 이메일 본문
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param mailSaveDirectory 메일을 저장할 디렉토리 (이 디렉토리 아래에 eml, attach 폴더와 위치하게 되고, "/"로 끝나야 한다)
 */
	private void saveFile(
		javax.mail.Part part,
		MailCharsetInfo mailInfo,
		String dir
	) throws IOException, MessagingException
	{
		if(part.getContent() instanceof InputStream || (part.getDisposition() != null && part.getDisposition().equalsIgnoreCase("attachment"))) {
			String path = dir + "attach" + File.separator + mailInfo.getGrahaMailId();
			File file = new File(path + File.separator);
			if(!file.exists()) {
				file.mkdirs();
			}
			int index = 0;
			String fileName = part.getFileName();
			boolean isPureAscii = true;
			boolean existsFileName = false;
			String[] headers = part.getHeader("Content-Disposition");
			if(headers != null) {
				for(String header: headers) {
					if(header != null && !isPureAscii(header)) {
						isPureAscii = false;
					}
					if(header != null && header.indexOf("filename") >= 0) {
						existsFileName = true;
					}
				}
			}
			if(!existsFileName) {
				headers = part.getHeader("Content-Type");
				if(headers != null) {
					for(String header: headers) {
						if(header != null && !isPureAscii(header)) {
							isPureAscii = false;
						}
					}
				}
			}
			if(!isPureAscii) {
				fileName = new String(fileName.getBytes("iso-8859-1"), mailInfo.getHeaderCharset());
			}
			if(fileName != null && fileName.startsWith("=?")) {
				fileName = MimeUtility.decodeText(fileName);
			}
			if(fileName != null && fileName.lastIndexOf("/") > 0) {
				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			}
			while(true) {
				if(index == 0) {
					file = new File(path + File.separator + fileName);
				} else {
					if(fileName.lastIndexOf(".") > 0) {
						file = new File(path + File.separator + fileName.substring(0, fileName.lastIndexOf("."))  + "-" + index + "." + fileName.substring(fileName.lastIndexOf(".") + 1));
					} else {
						file = new File(path + File.separator + fileName + "-" + index);
					}
				}
				if(!file.exists()) {
					break;
				}
				index++;
			}
			
			InputStream is = part.getInputStream();
			Files.copy(is, file.toPath());
		} else {
			throw new MessagingException("incorrect attach file");
		}
	
	}
/**
 * 이메일 내용을 데이타베이스에 저장한다.
 
 * @param contentType 
 * @param disposition
 * @param contents 이메일 내용
 * @param mailInfo 이메일고유번호와 기본 문자셋 정보
 * @param parentContentType 상위 ContentType
 * @param path 경로
 * @param con 데이타베이스 연결(Connection)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 */
	private void savePart(
		String contentType,
		String disposition,
		String contents,
		MailCharsetInfo mailInfo, 
		String parentContentType,
		String path,
		Connection con,
		Record params
	) throws SQLException
	{
		String sql = null;
		sql = "select nextval('webmua.graha_mail_part$graha_mail_part_id')::integer as graha_mail_part_id\n"; 
		List result = DB.fetch(con, HashMap.class, sql, null);
		if(result.size() > 0) {
			HashMap data = (HashMap)result.get(0);
			if(contentType != null) {
				data.put("content_type", contentType);
			}
			if(disposition != null) {
				data.put("disposition", disposition);
			}
			if(contents != null) {
				data.put("contents", contents.replaceAll("\u0000", ""));
			}
			if(parentContentType != null) {
				data.put("parent_content_type", parentContentType);
			}
			if(path != null) {
				data.put("path", path);
			}
			data.put("graha_mail_id", mailInfo.getGrahaMailId());
			data.put("insert_id", params.getString("prop.logined_user"));
			data.put("update_id", params.getString("prop.logined_user"));
			data.put("insert_date", new Timestamp(new Date().getTime()));
			data.put("update_date", new Timestamp(new Date().getTime()));
			data.put("insert_ip", params.getString("header.remote_addr"));
			data.put("update_ip", params.getString("header.remote_addr"));
			DB.insert(con, data, "webmua.graha_mail_part");
		} else {
			throw new SQLException("fail next value sequence(graha_mail_part_id)");
		}
	}
/**
 * 제목(Subject 헤더)를 가져온다.
 * 제목(Subject 헤더)는 2가지 이슈가 있다.
 * 1. 표준에서 벗어나서 한글이 그대로 사용된 경우
 * 2. Base64 인코딩 했으나, JavaMail API 에서 부정확하게 처리되는 경우(비표준으로 생각됨)
 *    - 여러 줄로 구성된 경우 JavaMail API는 각 줄마다 문자열로 만드는데, 1개의 글자를 구성하는 여러 개의 byte 사이에 줄바꿈이 있는 경우, 한글이 깨진다.
 *    - ThunderBird 에서는 정상적으로 처리됨. 

 * @param mime 이메일 메시지
 * @param mailInfo 이메일 고유번호와 기본 문자셋 정보
 * @return 제목(Subject 헤더)
 */
	private String getSubject(MimeMessage mime, MailCharsetInfo mailInfo) throws MessagingException, UnsupportedEncodingException {
		String[] subjects = mime.getHeader("Subject");
		if(subjects == null || subjects.length == 0) {
			return null;
		}
		boolean quotationMark = false;
		String subject = subjects[0];
		if(subject.startsWith("\"") && subject.endsWith("\"")) {
			subject = subject.substring(1, subject.length() - 1);
			quotationMark = true;
		}
		if(isPureAscii(subject)) {
			if(quotationMark) {
				return MimeUtility.decodeText(subject);
			} else {
				ByteArrayOutputStream baos = null;
				String charset = null;
				if(subject.indexOf("\r\n") > 0) {
					String[] ss = subject.split("\r\n");
					if(ss != null && ss.length > 0) {
						String prefix = null;
						if(ss[0] != null && ss[0].trim().startsWith("=?") && ss[0].trim().endsWith("?=")) {
							int start = 2;
							int pos = ss[0].trim().indexOf('?', start);
							try {
								Charset.forName(ss[0].trim().substring(start, pos));
								charset = MimeUtility.javaCharset(ss[0].trim().substring(start, pos));
							} catch (UnsupportedCharsetException ex) {
								charset = null;
							}
							if(charset != null && pos > start) {
								start = pos;
								pos = ss[0].trim().indexOf('?', start + 1);
								if(pos > start) {
									String encoding = ss[0].trim().substring(start + 1, pos);
									if(encoding.equals("B")) {
										prefix = ss[0].trim().substring(0, pos + 1);
									}
								}
							}
						}
						if(prefix != null) {
							baos = new java.io.ByteArrayOutputStream();
							try {
								for(int i = 0; i < ss.length; i++) {
									if(ss[i] != null && ss[i].trim().startsWith(prefix) && ss[i].trim().endsWith("?=")) {
										write(ss[i].trim().substring(prefix.length(), ss[i].length() - 2), baos);
									} else {
										baos.close();
										baos = null;
										break;
									}
								}
							} catch (IOException e) {
								baos = null;
							}
						}
					}
				}
				if(charset != null && baos != null) {
					return new String(baos.toByteArray(), charset);
				} else {
					return mime.getSubject();
				}
			}
		} else {
			if(subject != null && mailInfo.getHeaderCharset() != null) {
				return new String(subject.getBytes("iso-8859-1"), mailInfo.getHeaderCharset());
			} else {
				return subject;
			}
		}
	}
/**
 * Base64 인코딩된 문자열을 디코딩해서 메모리 기반의 출력스트림에 쓴다.

 * @param encoded Base64 인코딩된 문자열
 * @param baos 디코딩한 한 것을 쓸 메모리 기반의 출력스트림
 */
	private void write(String encoded, ByteArrayOutputStream baos) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(encoded));
		InputStream is = new BASE64DecoderStream(bis);
		byte[] bytes = new byte[bis.available()];
		int count = is.read(bytes, 0, bytes.length);
		if(count > 0) {
			baos.write(bytes, 0, count);
		}
	}
/**
 * 이메일 주소를 파싱한다.
 * JavaMail 표준 API에서 실패한 경우만 처리한다
 * 이 메소드는 현재 구현에서 미흡한 부분이 있는데, ","를 구분자로 사용해서 여러개의 이메일 주소를 분리하는데, Personal 이 ""로 감싸져 있고, 그 안에 ","가 있는 경우 오동작한다.

 * @param args 문자열 형식의 이메일 주소의 배열
 
 * @return 이메일 주소
 */
	private Address[] parseAddress(String[]... args) {
		if(args != null) {
			ArrayList address = new ArrayList();
			for(int i = 0; i < args.length; i++) {
				for (String[] headers : args) {
					if(headers != null) {
						for(String adr : headers) {
							StringTokenizer st = new StringTokenizer(adr, ",");
							while (st.hasMoreTokens()) {
								String token = st.nextToken();
								if(token != null) {
									try {
										if(token.trim().indexOf("\r\n") > 0) {
											String line = "";
											String[] lines = token.trim().split("\r\n");
											if(lines != null) {
												for(int x = 0; x < lines.length; x++) {
													if(lines[x] != null) {
														line += lines[x].trim();
													}
												}
											}
										} else {
											InternetAddress inetAddr = new InternetAddress(token.trim());
											address.add(inetAddr);
										}
									} catch(MessagingException e) {
										if(logger.isLoggable(Level.WARNING)) { logger.warning(LOG.toString(e)); }
									}
								}
							}
						}
					}
				}
			}
			if(address.size() > 0) {
				return (Address[])address.toArray(new InternetAddress[address.size()]);
			} else {
				return null;
			}
		}
		return null;
	}
/**
 * 표준에서 벗어나 이름(Personal) 에 한글 따위가 있는 것들을 교정한다.
 
 * @param address 이메일 주소
 * @param mailInfo 이메일 고유번호와 기본 문자셋 정보
 
 * @return 이메일 주소
 */
	private Address[] correctAddress(Address[] address, MailCharsetInfo mailInfo)  {
		if(address == null) {
			return null;
		}
		for(int i = 0; i < address.length; i++) {
			try {
				InternetAddress inetAddr = (InternetAddress)address[i];
				if(inetAddr.getPersonal() != null && !isPureAscii(inetAddr.toString())) {
					if(mailInfo.getHeaderCharset() != null) {
						inetAddr.setPersonal(new String(inetAddr.getPersonal().getBytes("iso-8859-1"), mailInfo.getHeaderCharset()), mailInfo.getHeaderCharset());
					}
				} else if(inetAddr.getPersonal() != null) {
					String personal = inetAddr.getPersonal();
					if(personal.startsWith("\"") && personal.endsWith("\"")) {
						personal = personal.substring(1, personal.length() - 1);
						inetAddr.setPersonal(MimeUtility.decodeText(personal));
					}
				}
				String addr = inetAddr.getAddress();
				if(addr.startsWith("\"=?") && addr.endsWith("?=\"")) {
					addr = addr.substring(1, addr.length() - 1);
					inetAddr.setAddress(MimeUtility.decodeText(addr));
				}
			} catch (UnsupportedEncodingException e) {
				if(logger.isLoggable(Level.WARNING)) { logger.warning(LOG.toString(e)); }
			}
		}
		return address;
	}
/**
 * 이메일 메시지에서 주소를 가져온다.
 * 표준에서 벗어나 이름(Personal) 에 한글 따위가 있는 것들을 처리하고,
 * 이메일 주소 파싱에 실패하는 경우도 처리한다.

 * @param mime 이메일 메시지
 * @param mailInfo 이메일 고유번호와 기본 문자셋 정보
 * @param type 유형 (이 클레스의 BCC, CC, TO, FROM, ReplyTo, SENDER, ALLRCPT)
 * @param guess 표준에서 벗어난 이름 따위가 있는 경우 자동으로 교정할 지 여부
 
 * @return 이메일 주소
 */
	private Address[] getAddress(MimeMessage mime, MailCharsetInfo mailInfo, int type, boolean guess) throws MessagingException {
		Address[] address = null;
		try {
			if(type == BCC) {
				address = mime.getRecipients(RecipientType.BCC);
			} else if(type == CC) {
				address = mime.getRecipients(RecipientType.CC);
			} else if(type == TO) {
				address = mime.getRecipients(RecipientType.TO);
			} else if(type == FROM) {
				address = mime.getFrom();
			} else if(type == ReplyTo) {
				address = mime.getReplyTo();
			} else if(type == SENDER) {
				if(mime.getSender() == null) {
					return null;
				} else {
					address = new Address[]{mime.getSender()};
				}
			} else if(type == ALLRCPT) {
				address = mime.getAllRecipients();
			} else {
				return null;
			}
		} catch (AddressException e) {
			address = null;
		}
		if(address == null) {
			if(type == BCC) {
				address = parseAddress(mime.getHeader("Bcc"));
			} else if(type == CC) {
				address = parseAddress(mime.getHeader("Cc"));
			} else if(type == TO) {
				address = parseAddress(mime.getHeader("To"));
			} else if(type == FROM) {
				address = parseAddress(mime.getHeader("From"));
			} else if(type == ReplyTo) {
				address = parseAddress(mime.getHeader("Reply-To"));
			} else if(type == SENDER) {
				address = parseAddress(mime.getHeader("Sender"));
			} else if(type == ALLRCPT) {
				address = parseAddress(mime.getHeader("To"), mime.getHeader("Cc"), mime.getHeader("Bcc"));
			} else {
				return null;
			}
		}
		if(guess) {
			return correctAddress(address, mailInfo);
		} else {
			return address;
		}
	}
/**
 * 이메일의 charset을 추출한다.
 * 헤더 정보를 돌면서, charset 정보가 포함되어 찾아낸다.
 * 찾을 수 없다면 null을 리턴한다.
 
 * @param mime 이메일
 * @return charset
 */
	private String guessCharset(MimeMessage mime) throws MessagingException {
		Address[] adr = getAddress(mime, null, ALLRCPT, false);
		if(adr != null) {
			for(int i = 0; i < adr.length; i++) {
				if(adr[i] != null && adr[i].toString().startsWith("=?")) {
					return (adr[i].toString().substring(2, adr[i].toString().indexOf("?", 3)));
				}
			}
		}
		adr = getAddress(mime, null, FROM, false);
		if(adr != null) {
			for(int i = 0; i < adr.length; i++) {
				if(adr[i] != null && adr[i].toString().startsWith("=?")) {
					return (adr[i].toString().substring(2, adr[i].toString().indexOf("?", 3)));
				}
			}
		}
		adr = getAddress(mime, null, SENDER, false);
		if(adr != null) {
			for(int i = 0; i < adr.length; i++) {
				if(adr[i] != null && adr[i].toString().startsWith("=?")) {
					return (adr[i].toString().substring(2, adr[i].toString().indexOf("?", 3)));
				}
			}
		}
		
		Enumeration<Header> headers = mime.getAllHeaders();
		while (headers.hasMoreElements()) {
			Header  h = (Header) headers.nextElement();
			if(h.getValue().startsWith("=?")) {
				return (h.getValue().substring(2, h.getValue().indexOf("?", 3)));
			}
		}
		if(mime.getContentType() != null && mime.getContentType().indexOf("charset=") > 0) {
			if(mime.getContentType().indexOf("charset=\"") > 0) {
				return (mime.getContentType().substring(mime.getContentType().indexOf("charset=\"") + 9, mime.getContentType().indexOf("\"", mime.getContentType().indexOf("charset=\"") + 9)));
			} else if(mime.getContentType().indexOf("charset=") > 0) {
				return (mime.getContentType().substring(mime.getContentType().indexOf("charset=") + 8));
			}
		}
		return null;
	}
/**
 * 문자열이 US-ASCII 인지 검사한다.
 
 * @param v 검사할 문자열
 * @return 문자열이 US-ASCII 인지 여부
 */
	private static boolean isPureAscii(String v) {
		byte[] bytearray = v.getBytes();
		java.nio.charset.CharsetDecoder d = java.nio.charset.Charset.forName("US-ASCII").newDecoder();
		try {
			java.nio.CharBuffer r = d.decode(java.nio.ByteBuffer.wrap(bytearray));
		} catch(java.nio.charset.CharacterCodingException e) {
			return false;
		}
		return true;
	}
}
