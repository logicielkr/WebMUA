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

/**
 * 정렬순서를 적용한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class ApplyGrahaMailAccountOrderNumberProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public ApplyGrahaMailAccountOrderNumberProcessorImpl() {
		
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
		String sql = "update webmua.graha_mail_account set order_number = ? where insert_id = ? and graha_mail_account_id = ?";
		Object[] param = new Object[3];
		param[1] = params.getString("prop.logined_user");
		try {
			int index = 1;
			while(true) {
				if(params.hasKey("param.graha_mail_account_id." + index) && params.hasKey("param.order_number." + index)) {
					param[0] = params.getIntObject("param.order_number." + index);
					param[2] = params.getIntObject("param.graha_mail_account_id." + index);
					if(DB.execute(con, null, sql, param) == 0) {
						params.put("error.error", "message.20001");
						break;
					}
				} else {
					break;
				}
				index++;
			}
		} catch (SQLException e) {
			params.put("error.error", "message.20002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
	}
}
