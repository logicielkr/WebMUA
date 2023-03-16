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
import java.util.Properties;
import kr.graha.helper.LOG;
import kr.graha.helper.DB;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.io.File;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;


/**
 * ThunderBird 메일 데이타, Mbox, eml 파일을 처리한다.

 * 처리할 수 있는 형태는 다음과 같다.
 * 압축파일 형태도 처리할 수 있다.
 * 1. 압축되지 않은 경우도 ThunderBird 메일 데이타, Mbox, eml 파일을 처리할 수 있는데, eml 파일인 경우 확장자가 .eml 이어야 한다.
 * 2. gzip 으로 압축된 경우는 ThunderBird 메일 데이타, Mbox, eml 파일을 처리할 수 있는데, eml 파일인 경우 확장자가 .eml.gz 이어야 한다.
 * 3. zip 으로 압축된 경우도 ThunderBird 메일 데이타, Mbox, eml 파일을 처리할 수 있는데, eml 파일인 경우 확장자가 .eml 이어야 한다.
 * 4. 확장자가 .mbox 인 경우(GMail 방식) 무조건 Mbox 로 처리한다.
 * 5. .eml 확장자인 경우, 무조건 eml로 처리하고,
 * 6. 그렇지 않은 경우 첫 번째 줄을 읽어 ThunderBird 메일 데이타(From - 로 시작)와  Mbox(From 으로 시작) 포맷을 판별하고,
 * 7. 만약 ThunderBird 메일 데이타(From - 로 시작)와  Mbox(From 으로 시작) 포맷 이 모두 아닌 경우 처리를 생략한다.
 * 8. 확장자가 .html, .jpg, .jpeg, .gif, .vcf, .png인 경우 처리를 생략한다(다른 확장자를 추가하려면 detectFileFormat 메소드의 소스코드를 수정해야 한다).
 * 테스트 한 데이타는 다음과 같다.
 * 1. Daum 에서 다운로드 받은 메일 데이타(여러개의 eml 파일이 1개의 압축파일로 묶여 있는 형태)
 * 2. Naver 에서 다운로드 받은 메일 데이타(여러개의 eml 파일이 1개의 압축파일로 묶여 있는 형태)
 * 3. Gmail 에서 다운로드 받은 메일 데이타(.mbox 확장자를 가진 파일이 Mbox 포맷으로  1개의 압축파일로 묶여 있는 형태)
 * 4. ThunderBird 메일 데이타
 * 5. Linux의 mail 데이타(Mbox)
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class MailMigrationProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private int GZIP = 1;
	private int EML_GZIP = 2;
	private int ZIP = 3;
	private int EML = 4;
	private int MBOX = 5;
	private int THUNDERBIRD_MBOX = 6;
	
	private int SKIP = 8;
	private int UNKNOWN = 9;
	
	public MailMigrationProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 * 이메일을 포워딩(Forwarding) 할 때, 첨부파일을 복사한다.
 
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
		if(!params.hasKey("prop.mail.save.directory")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put("error.error", "message.80006");
			return;
		}
		try {
			if(!params.isEmpty()) {
				Iterator<String> it = params.keySet().iterator();
				while(it.hasNext()) {
					String key = (String)it.next();
					if(params.get(key) instanceof String[]) {
						for(int i = 0; i < ((String[])params.get(key)).length; i++) {
							logger.log(Level.ALL, key + "." + i + "=" + ((String[])params.get(key))[i]);
						}
					} else {
						if(key.startsWith("uploaded.file.path.")) {
							execute(params.getString(key), params, con);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			params.put("error.error", "message.80007");
			return;
		} catch (MessagingException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			params.put("error.error", "message.80008");
			return;
		} catch (IOException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			params.put("error.error", "message.80009");
			return;
		} catch (SQLException e) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			params.put("error.error", "message.80010");
			return;
		}
	}
/**
 * 이메일 파일을 처리한다.
 * detectFileFormat 메소드를 호출하여, 파일 포맷에 따른 메소드를 호출한다.
 
 * @param fileName 처리할 이메일 파일 이름
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @see detectFileFormat
 */
	private void execute(String fileName, Record params, Connection con) throws FileNotFoundException, MessagingException, IOException, SQLException {
		int fileFormat = detectFileFormat(fileName);
		if(fileFormat == EML) {
			parseEml(fileName, params, con);
		} else if(fileFormat == EML_GZIP) {
			parseGzip(fileName, fileFormat, params, con);
		} else if(fileFormat == GZIP) {
			parseGzip(fileName, fileFormat, params, con);
		} else if(fileFormat == ZIP) {
			parseZip(fileName, params, con);
		} else if(fileFormat == UNKNOWN) {
			parseUnknown(fileName, params, con);
		}
	}
/**
 * Logger를 가져온다.
 * 이 메소드는 main 메소드를 위한 것이다.
 
 * @return logger
 */
	public Logger getLogger() {
		return this.logger;
	}
/**
 * Properties 에서 key 에 해당하는 값을 가져온다.
 * UTF-8로 변경하여 가져온다.
 * 만약 값이 없는 경우 RuntimeException을 발생시킨다.
 * 이 메소드는 main 메소드를 위한 것이다.
 
 * @param prop Properties
 * @param key 가져올 key
 * @return key 에 해당하는 값
 */
	private String getProperty(Properties prop, String key) throws RuntimeException {
		String value = prop.getProperty(key);
		if(value != null) {
			try {
				return new String(value.getBytes(StandardCharsets.ISO_8859_1), "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			}
		}
		throw new RuntimeException("Missing " + key);
	}
	public static void main(String[] args) throws Exception {
		String propFileName = System.getProperty("migration.prop.file");
		if(propFileName == null) {
			System.err.println("Missing migration.prop.file Property");
			return;
		}
		File propFile = new File(propFileName);
		if(!propFile.exists()) {
			System.err.println("Not Exists " + propFileName);
			return;
		}
		InputStream is = null;
		Properties prop = new Properties();
		try {
			is = new FileInputStream(propFile);
			prop.load(is);
		} catch(IOException e) {
			prop = null;
			throw e;
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(IOException e) {
					throw e;
				}
			}
		}
		if(prop == null) {
			System.err.println("Invalid " + propFileName);
			return;
		}
		Record params = new Record();
		MailMigrationProcessorImpl impl = new MailMigrationProcessorImpl();
		params.put("param.graha_mail_account_id", impl.getProperty(prop, "param.graha_mail_account_id"));
		params.put("prop.mail.save.directory", impl.getProperty(prop, "prop.mail.save.directory"));
		params.put("prop.mail.backup.directory", impl.getProperty(prop, "prop.mail.backup.directory"));
		params.put("prop.logined_user", impl.getProperty(prop, "prop.logined_user"));
		params.put("header.remote_addr", impl.getProperty(prop, "header.remote_addr"));
		
		Connection con = null;
		try {
			Class.forName(impl.getProperty(prop, "jdbc.driver.name"));
			con = java.sql.DriverManager.getConnection(
				impl.getProperty(prop, "jdbc.url"), 
				impl.getProperty(prop, "jdbc.user.name"), 
				impl.getProperty(prop, "jdbc.user.password")
			);
		} catch (ClassNotFoundException | SQLException e) {
			if(impl.getLogger().isLoggable(Level.SEVERE)) { impl.getLogger().severe(LOG.toString(e)); }
		}
		if(con != null) {
			params.put("param.graha_mail_folder_id", impl.getProperty(prop, "param.graha_mail_folder_id"));
			impl.execute(impl.getProperty(prop, "mailbox.file.name"), params, con);
			
			MailParserProcessorImpl parser = new MailParserProcessorImpl();
			parser.execute(params, con);
			try {
				con.close();
			} catch(SQLException e) {
				throw e;
			}
		}
	}
/**
 * 이메일을 처리한다.
 * 다른 메소드들은 돌고 돌아 결국 이 메소드를 호출한다.
 
 * @param is 처리할 이메일을 담은 Stream.  eml 파일의 Stream 과 같다. 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체. 4가지 항목(param.graha_mail_account_id, param.graha_mail_folder_id, prop.logined_user, header.remote_addr)은 반드시 들어 있어야 한다.
 * @param con 데이타베이스 연결(Connection)
 */
	private void parse(InputStream is, Record params, Connection con) throws MessagingException, IOException, SQLException {
		MimeMessage mime = new MimeMessage(null, is);
		FetchMailProcessorImpl fetcher = new FetchMailProcessorImpl();
		ImapMessageMinimalInfo info = new ImapMessageMinimalInfo();
		info.setGrahaMailAccountId(params.getInt("param.graha_mail_account_id"));
		if(params.hasKey("param.graha_mail_folder_id")) {
			info.setFolderId(params.getInt("param.graha_mail_folder_id"));
		} else {
			info.setFolderId(params.getInt("query.graha_mail_folder.graha_mail_folder_id"));
		}
		HashMap data = fetcher.save(info, params, con);
		if(data != null && data.containsKey("graha_mail_id")) {
			fetcher.save(mime, data, params.getInt("param.graha_mail_account_id"), null, null, params, null);
		}
	}
/**
 * 이메일 파일 중 zip으로 압축된 파일을 처리한다.
 * 압축파일을 읽어가면서 detectFileFormat 메소드를 호출하여, 파일 포맷에 따른 메소드를 호출한다.
 
 * @param fileName 처리할 이메일 파일 이름
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 * @see detectFileFormat
 */
	private void parseZip(String fileName, Record params, Connection con) throws IOException, MessagingException, SQLException {
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry entry = null;
		try {
			fis = new FileInputStream(fileName);
			zis = new ZipInputStream(fis, StandardCharsets.ISO_8859_1);
			while((entry = zis.getNextEntry()) != null) {
				int fileFormat = detectFileFormat(entry.getName());
				if(fileFormat == MBOX) {
					parseUnknown(zis, MBOX, params, con, false);
				} else if(fileFormat == EML) {
					parse(zis, params, con);
				} else if(fileFormat == UNKNOWN) {
					parseUnknown(zis, params, con, false);
				}
				zis.closeEntry();
				entry = null;
			}
			zis.close();
			zis = null;
			fis.close();
			fis = null;
		} catch (IOException | MessagingException | SQLException e) {
			throw e;
		} finally {
			if(entry != null) {
				try {
					zis.closeEntry();
				} catch (IOException e) {
				}
			}
			if(zis != null) {
				try {
					zis.close();
					zis = null;
				} catch (IOException e) {
				}
			}
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
				}
			}
		}
	}
/**
 * 이메일 파일 중 gzip으로 압축된 파일(확장자 .gz)을 처리한다.
 
 * @param fileName 처리할 이메일 파일 이름
 * @param fileFormat 파일형태(EML_GZIP 이거나 GZIP 만 처리된다.)
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseGzip(String fileName, int fileFormat, Record params, Connection con) throws MessagingException, IOException, SQLException {
		FileInputStream fis = null;
		GZIPInputStream gis = null;
		try {
			fis = new FileInputStream(fileName);
			gis = new GZIPInputStream(fis);
			if(fileFormat == EML_GZIP) {
				parse(gis, params, con);
			} else if(fileFormat == GZIP) {
				parseUnknown(gis, 0, params, con, true);
			}
			fis.close();
			fis = null;
			gis.close();
			gis = null;
		} catch (FileNotFoundException | MessagingException | SQLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if(gis != null) {
				try {
					gis.close();
					gis = null;
				} catch(IOException e) {
				}
			}
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch(IOException e) {
				}
			}
		}
	}
/**
 * 이메일 파일 중 형식을 알수 없는 파일을 처리한다.
 * 하지만 ThunderBird 메일 데이타(From - 로 시작)나  Mbox(From 으로 시작) 포맷만 처리된다. 
 
 * @param fileName 처리할 이메일 파일 이름
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseUnknown(String fileName, Record params, Connection con) throws MessagingException, IOException, SQLException {
		parseUnknown(new File(fileName), params, con);
	}
/**
 * 이메일 파일 중 형식을 알수 없는 파일을 처리한다.
 * 하지만 ThunderBird 메일 데이타(From - 로 시작)나  Mbox(From 으로 시작) 포맷만 처리된다.
 * 파일의 첫 번째 줄을 읽어 파일 형식을 감지한다.
 
 * @param file 처리할 이메일 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseUnknown(File file, Record params, Connection con) throws MessagingException, IOException, SQLException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			parseUnknown(fis, 0, params, con, true);
			fis.close();
			fis = null;
		} catch (FileNotFoundException | MessagingException | SQLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch(IOException e) {
				}
			}
		}
	}
/**
 * 이메일 파일 중 형식을 알수 없는 파일을 처리한다.
 * 하지만 ThunderBird 메일 데이타(From - 로 시작)나  Mbox(From 으로 시작) 포맷만 처리된다.
 * 파일의 첫 번째 줄을 읽어 파일 형식을 감지한다.
 
 * @param is 처리할 이메일 파일의 Stream
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseUnknown(InputStream is, Record params, Connection con, boolean streamClose) throws MessagingException, IOException, SQLException {
		parseUnknown(is, 0, params, con, streamClose);
	}
/**
 * 이메일 파일 중 형식을 알수 없는 파일을 처리한다.
 * 하지만 ThunderBird 메일 데이타(From - 로 시작)나  Mbox(From 으로 시작) 포맷만 처리된다.
 * fileFormat 파라미터가 넘어온 경우에는 그것을 사용하고, 그렇지 않은 경우(0이 넘오는 경우) 파일의 첫 번째 줄을 읽어 파일 형식을 감지한다.
 
 * @param is 처리할 이메일 파일의 Stream
 * @param fileFormat 파일 형식. 0, THUNDERBIRD_MBOX, MBOX 중 하나의 값을 넘겨야 한다. 
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseUnknown(InputStream is, int fileFormat, Record params, Connection con, boolean streamClose) throws MessagingException, IOException, SQLException {
		BufferedReader in = null;
		InputStreamReader isr = null;
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		try {
			isr = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
			in = new BufferedReader(isr);
			baos = new ByteArrayOutputStream();
			String s;
			int index = 0;
			while ((s = in.readLine()) != null) {
				if(fileFormat <= 0 && index == 0) {
					if(s.startsWith("From - ")) {
						fileFormat = THUNDERBIRD_MBOX;
					} else if(s.startsWith("From ")) {
						fileFormat = MBOX;
					} else {
						baos.close();
						baos = null;
						break;
					}
				}
				if(index > 0 && 
					(
						(fileFormat == THUNDERBIRD_MBOX && s.startsWith("From - ")) ||
						(fileFormat == MBOX && s.startsWith("From "))
					)
				) {
					if(index > 0) {
						byte[] b = baos.toByteArray();
						bais = new ByteArrayInputStream(b);
						parse(bais, params, con);
						bais.close();
						bais = null;
						b = null;
					}
					baos.reset();
				}
				byte[] buffer = s.getBytes(StandardCharsets.ISO_8859_1);
				baos.write(buffer, 0, buffer.length);
				buffer = "\n".getBytes(StandardCharsets.ISO_8859_1);
				baos.write(buffer, 0, buffer.length);
				buffer = null;
				index++;
			}
			if(streamClose) {
				in.close();
				in = null;
				isr.close();
				isr = null;
			}
			if(baos != null) {
				byte[] b = baos.toByteArray();
				bais = new ByteArrayInputStream(b);
				parse(bais, params, con);
				b = null;
				baos.close();
				baos = null;
				bais.close();
				bais = null;
			}
		} catch (IOException | MessagingException | SQLException e) {
			throw e;
		} finally {
			if(streamClose) {
				if(in !=  null) {
					try {
						in.close();
						in = null;
					} catch (IOException e) {
					}
				}
				if(isr !=  null) {
					try {
						isr.close();
						isr = null;
					} catch (IOException e) {
					}
				}
			}
			if(baos !=  null) {
				try {
					baos.close();
					baos = null;
				} catch (IOException e) {
				}
			}
			if(bais !=  null) {
				try {
					bais.close();
					bais = null;
				} catch (IOException e) {
				}
			}
		}
	}
/**
 * 이메일 파일 중 eml 파일(확장자 .eml)을 처리한다.
 
 * @param file 처리할 이메일 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseEml(File file, Record params, Connection con) throws FileNotFoundException, MessagingException, IOException, SQLException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			parse(fis, params, con);
			fis.close();
			fis = null;
		} catch (FileNotFoundException | MessagingException e) {
			throw e;
		} catch (IOException | SQLException e) {
			throw e;
		} finally {
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch(IOException e) {
				}
			}
		}
	}
/**
 * 이메일 파일 중 eml 파일(확장자 .eml)을 처리한다.
 
 * @param fileName 처리할 이메일 파일 이름
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private void parseEml(String fileName, Record params, Connection con) throws FileNotFoundException, MessagingException, IOException, SQLException {
		parseEml(new File(fileName), params, con);
	}
/**
 * 파일이름으로 부터 파일형식을 판정한다.
 * 확장자에 따라(endsWith로 검사) 판별하고, 대소문자를 구분하지 않는다.
 * .gz : GZIP
 * .eml.gz : EML_GZIP
 * .zip : ZIP
 * .eml : EML
 * .mbox : MBOX (Gmail 을 위해...)
 * .html, jpg, jpeg, git, png, vcf : SKIP (처리하지 않는다)
 * 기타 확장자가 없거나 다른 확장자인 경우 : UNKNOWN (이 경우 파일의 첫 번째 줄에서 ThunderBird 메일 데이타(From - 로 시작)와  Mbox(From 으로 시작) 포맷을 판별한다)
 * 처리하지 않는 파일 확장자를 추가하기 위해서는 이 메소드를 수정해야 한다.
 
 * @param fileName 처리할 이메일 파일 이름
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)
 */
	private int detectFileFormat(String fileName) {
		if(fileName != null) {
			if(fileName.toLowerCase().endsWith(".gz")) {
				return GZIP;
			} else if(fileName.toLowerCase().endsWith(".eml.gz")) {
				return EML_GZIP;
			} else if(fileName.toLowerCase().endsWith(".zip")) {
				return ZIP;
			} else if(fileName.toLowerCase().endsWith(".eml")) {
				return EML;
			} else if(fileName.toLowerCase().endsWith(".mbox")) {
				return MBOX;
			} else if(
				fileName.toLowerCase().endsWith(".html") ||
				fileName.toLowerCase().endsWith(".jpg") ||
				fileName.toLowerCase().endsWith(".jpeg") ||
				fileName.toLowerCase().endsWith(".gif") ||
				fileName.toLowerCase().endsWith(".png") ||
				fileName.toLowerCase().endsWith(".vcf")
			) {
				return SKIP;
			}
		}
		return UNKNOWN;
	}
}
