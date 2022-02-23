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
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.nio.file.Files;


/**
 * 이메일 전달을 위해 필요한 정보를 가져오고, 이메일을 저장할 때 메시지 원본을 첨부파일로 저장한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
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
 * @see kr.graha.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		if(!params.hasKey("prop.mail.save.directory")) {
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("prop.mail.save.directory is required!!!"); }
			params.put("error.error", "message.90001");
			return;
		}
		if(!params.hasKey("param.graha_mail_fid") || params.hasKey("param.graha_mail_id")) {
			return;
		}
		if(!params.hasKey("query.graha_mail.graha_mail_id")) {
			params.put("error.error", "message.90002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe("param.graha_mail_id is required!!!"); }
			return;
		}
		int graha_mail_fid = params.getInt("param.graha_mail_fid");
		int graha_mail_id = params.getInt("query.graha_mail.graha_mail_id");
		String mailSaveDirectory = params.getString("prop.mail.save.directory");
		String path = mailSaveDirectory + "attach" + File.separator + graha_mail_fid;
		File dir = new File(path + File.separator);
		if(dir.exists()) {
			File[] files = dir.listFiles();
			if(files != null) {
				Object[] param = new Object[2];
				param[0] = params.getIntObject("param.graha_mail_fid");
				param[1] = params.getString("prop.logined_user");
				String sql = "select graha_mail_id\n";
				sql += "from webmua.graha_mail\n";
				sql += "where graha_mail_id = ?\n";
				sql += "	and insert_id = ?\n";
				List result = null;
				try {
					result = DB.fetch(con, HashMap.class, sql, param);
				} catch (SQLException e) {
					params.put("error.error", "message.90003");
					if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
				}
				if(result != null && result.size() > 0) {
					String newPath = mailSaveDirectory + "attach" + File.separator + graha_mail_id;
					File file = new File(newPath + File.separator);
					if(!file.exists()) {
						file.mkdirs();
					}
					for(int i = 0; i < files.length; i++) {
						try {
							Files.copy(files[i].toPath(), file.toPath().resolve(files[i].toPath().getFileName()));
						} catch (IOException e) {
							params.put("error.error", "message.90004");
							if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
							break;
						}
					}
				}
			}
		}
	}
}
