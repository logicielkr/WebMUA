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
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.net.URI;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.net.URLDecoder;


/**
 * 이메일 전달을 위해 필요한 정보를 가져오고, 이메일을 저장할 때 메시지 원본을 첨부파일로 저장한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class ForwardMailProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public ForwardMailProcessorImpl() {
		
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
 * @see kr.graha.post.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory"))) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.90001");
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_fid")) || params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_id"))) {
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_QUERY, "graha_mail.graha_mail_id"))) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.90002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_id is required!!!"); }
			return;
		}
		int graha_mail_fid = params.getInt(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_fid"));
		int graha_mail_id = params.getInt(Record.key(Record.PREFIX_TYPE_QUERY, "graha_mail.graha_mail_id"));
		String mailSaveDirectory = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mail.save.directory"));
		String path = mailSaveDirectory + "attach" + File.separator + graha_mail_fid;
		if(Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path))) {
			Object[] param = new Object[2];
			param[0] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, "graha_mail_fid"));
			param[1] = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "logined_user"));
			String sql = "select graha_mail_id\n";
			sql += "from webmua.graha_mail\n";
			sql += "where graha_mail_id = ?\n";
			sql += "	and insert_id = ?\n";
			List result = null;
			try {
				result = DB.fetch(con, HashMap.class, sql, param);
			} catch (SQLException e) {
				params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.90003");
				if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
			}
			if(result != null && result.size() > 0) {
				String newPath = mailSaveDirectory + "attach" + File.separator + graha_mail_id;
				File newDir = new File(newPath + File.separator);
				if(!newDir.exists()) {
					newDir.mkdirs();
				}
				DirectoryStream<Path> stream = null;
				try {
					stream = Files.newDirectoryStream(Paths.get(path));
					for(Path file : stream) {
						if(Files.isRegularFile(file)) {
							Files.copy(file, Paths.get(ForwardMailProcessorImpl.getUniqueFileURI(newPath, ForwardMailProcessorImpl.decodeFileName(file.toUri()))));
						}
					}
					stream.close();
					stream = null;
				} catch (URISyntaxException | IOException e) {
					params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.90004");
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
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
		}
	}
/**
 * URI 객체로부터 파일이름만 가져온다.
 *
 * 이 메소드는 graha 라이브러리의 GFile 에 있는 것을 그대로 복사한 것이다.
 *
 * @param uri URI 객체(Path.toURI() 메소드의 결과)
 * @return 파일이름
 */
	protected static String decodeFileName(URI uri) {
		return uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
/*
		try {
//			return URLDecoder.decode(uri.toString().substring(uri.toString().lastIndexOf("/") + 1).replace("+", "%2B"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return uri.toString().substring(uri.toString().lastIndexOf("/")+1);
		}
*/
	}
/**
 * 유일한 파일이름을 URI로 가져온다.
 *
 * basePath 에 fileName 과 동일한 파일이 있는 경우,
 * 확장자가 있는 경우 확장자 앞에 확장자가 없는 경우 파일이름의 끝에 "-일련번호" 를 붙인다.
 *
 * 확장자는 fileName 에서 "." 이 있는 경우, 마지막 "." 뒷부분을 확장자로 한다.
 * 확장자가 유효한지 여부를 따지지 않고, 파일이름에서 마지막 "." 뒷부분을 확장자로 취급된다.
 * 또한 ".tar.gz" 혹은 ".tar.bz2" 와 같은 경우에도 "gz", "bz2" 가 확장자가 된다(이 부분은 향후에 개선할 의향이 있고, 만약 그렇게 된다면, kr.graha.helper 아래에 위치하게 될 가능성이 크다).
 *
 * 이 메소드는 kr.graha.post.model.File 을 그대로 복사한 것이다.
 *
 * @param basePath 디렉토리 경로
 * @param fileName 파일이름
 * @return 디렉토리 경로(basePath) 에서 중복되지 않은 파일이름(fileName)
 */
	protected static URI getUniqueFileURI(String basePath, String fileName) throws UnsupportedEncodingException, URISyntaxException {
		URI uri = null;
		int index = 0;
		while(true) {
			if(index == 0) {
				uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20"));
			} else {
				if(fileName.lastIndexOf(".") > 0) {
					uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName.substring(0, fileName.lastIndexOf(".")), "UTF-8").replaceAll("\\+", "%20")  + "-" + index + "." + java.net.URLEncoder.encode(fileName.substring(fileName.lastIndexOf(".") + 1), "UTF-8").replaceAll("\\+", "%20"));
				} else {
					uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "-" + index);
				}
			}
			if(Files.notExists(Paths.get(uri))) {
				break;
			}
			index++;
		}
		return uri;
	}
}
