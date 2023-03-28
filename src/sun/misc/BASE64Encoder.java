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

package sun.misc;

import java.util.Base64;

/**
 * Java 8 이상에서 DKIM-for-JavaMail(https://github.com/usrflo/DKIM-for-JavaMail) 라이브러리를 사용하기 위해서 작성했다.
 * sun.misc.BASE64Encoder 를 사용할 수 있는 버전에서는 삭제해야 한다.
 */

public final class BASE64Encoder {
	public String encode(byte[] src) {
		return Base64.getEncoder().encodeToString(src);
	}
}
