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

import javax.activation.DataSource;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.activation.MimetypesFileTypeMap;

/**
 * Path 를 파라미터로 받는 생성자를 가진 javax.activation.DataSource 의 구현체
 * 
 * WAS 의 file.encoding 이 US-ASCII 계열(예를 들어 ANSI_X3.4-1968) 인 경우
 * 파일이름에 한글이 포함된 첨부파일을 처리하기 위해 만들어졌다. 

 * @author HeonJik, KIM
 
 * @version 0.9
 * @since 0.9
 */

public class PathDataSource implements DataSource {
	private Path path;
	private MimetypesFileTypeMap map = null;
	public PathDataSource(Path path) {
		this.path = path;
	}
	public String getContentType() {
		if(this.map == null) {
			this.map = new MimetypesFileTypeMap();
		}
		return this.map.getContentType(this.path.toUri().getPath());
	}
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(this.path);
	}
	public String getName() {
		return this.path.toUri().getPath().substring(this.path.toUri().getPath().lastIndexOf("/") + 1);
	}
	public OutputStream getOutputStream() throws IOException {
		return Files.newOutputStream(this.path); 
	}
}