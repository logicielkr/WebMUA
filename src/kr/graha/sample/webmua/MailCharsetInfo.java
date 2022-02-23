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

/**
 * 이메일 고유번호(grahaMailId, 데이타베이스에서는 graha_mail_id)와 기본 문자셋 정보
 * 기본 문자셋 속성은 graha_mail_account, graha_mail_folder, graha_mail 에서 각각 관리한다.
 * 데이타베이스에서 가져오는 정보에 더해, 파라미터로 넘어올 것으로 예상되는 mailNewCharset과 프로그램에서 메일 헤더를 뒤져서 예상한 guessCharset 가 포함되어 있다.

 * @author HeonJik, KIM
 
 * @see MailParserProcessorImpl
 
 * @version 0.9
 * @since 0.9
 */

public class MailCharsetInfo {
	private Integer grahaMailId;
	public void setGrahaMailId(Integer grahaMailId) {
		this.grahaMailId = grahaMailId;
	}
	public Integer getGrahaMailId() {
		return this.grahaMailId;
	}
	private String mailNewCharset;
	public void setMailNewCharset(String mailNewCharset) {
		this.mailNewCharset = mailNewCharset;
	}
	public String getMailNewCharset() {
		return this.mailNewCharset;
	}
	private String mailDefaultCharset;
	public void setMailDefaultCharset(String mailDefaultCharset) {
		this.mailDefaultCharset = mailDefaultCharset;
	}
	public String getMailDefaultCharset() {
		return this.mailDefaultCharset;
	}
	private String folderDefaultCharset;
	public void setFolderDefaultCharset(String folderDefaultCharset) {
		this.folderDefaultCharset = folderDefaultCharset;
	}
	public String getFolderDefaultCharset() {
		return this.folderDefaultCharset;
	}
	private String accountDefaultCharset;
	public void setAccountDefaultCharset(String accountDefaultCharset) {
		this.accountDefaultCharset = accountDefaultCharset;
	}
	public String getAccountDefaultCharset() {
		return this.accountDefaultCharset;
	}
	private String guessCharset;
	public void setGuessCharset(String guessCharset) {
		this.guessCharset = guessCharset;
	}
	public String getGuessCharset() {
		return this.guessCharset;
	}
	public MailCharsetInfo() {
	}
/**
 * Mail Header 를 처리할 때 사용한 default charset 을 가져온다.
 * 현재 버전에서 getHeaderCharset()과 getContentCharset() 의 구현은 동일하다.
 * Header 와 Mail 내용을 처리할 때,
 * Header 인 경우 아스키코드로만 구성되어 있는지 검사하고,
 * Content 는 contentType 에 charset 이 정의되어 있는지 검사한다.
 * 매우 사소한 차이지만, 나중에 우선 순위를 달리할 수 있으므로 분리한다.
 */
	public String getHeaderCharset() {
		if(getMailNewCharset() != null) {
			return getMailNewCharset();
		}
		if(getGuessCharset() != null) {
			return getGuessCharset();
		}
		if(getMailDefaultCharset() != null) {
			return getMailDefaultCharset();
		}
		if(getFolderDefaultCharset() != null) {
			return getFolderDefaultCharset();
		}
		if(getAccountDefaultCharset() != null) {
			return getAccountDefaultCharset();
		}
		return null;
	}
/**
 * Mail 내용을 처리할 때 사용한 default charset 을 가져온다.
 * @see getHeaderCharset
 */	
	public String getContentCharset() {
		if(getMailNewCharset() != null) {
			return getMailNewCharset();
		}
		if(getGuessCharset() != null) {
			return getGuessCharset();
		}
		if(getMailDefaultCharset() != null) {
			return getMailDefaultCharset();
		}
		if(getFolderDefaultCharset() != null) {
			return getFolderDefaultCharset();
		}
		if(getAccountDefaultCharset() != null) {
			return getAccountDefaultCharset();
		}
		return null;
	}
}