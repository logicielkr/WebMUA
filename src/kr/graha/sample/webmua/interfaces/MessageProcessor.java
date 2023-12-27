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


package kr.graha.sample.webmua.interfaces;

import kr.graha.post.lib.Record;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import javax.mail.MessagingException;

/**
 * 메일을 발송하기 전에, MimeMessage 를 가공한다.

 * @author HeonJik, KIM
 * @version 0.9
 * @since 0.9
 */


public interface MessageProcessor {
	MimeMessage execute(MimeMessage msg, Record params) throws MessagingException;
}
