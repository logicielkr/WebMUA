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

import java.util.Date;

/**
 * Imap 서버에서 가져온 이메일을 특정하기 위한 최소한의 정보
 * 메시지가 중복되었는지 확인하기 위해 사용된다.

 * @author HeonJik, KIM
 
 * @see MailParserProcessorImpl
 
 * @version 0.9
 * @since 0.9
 */

public class ImapMessageMinimalInfo {
	protected static int pop3 = 1;
	protected static int imap = 2;

	private int serverType;
	public void setServerType(int serverType) {
		this.serverType = serverType;
	}
	public int getServerType() {
		return this.serverType;
	}
	public boolean imap() {
		if(this.serverType == ImapMessageMinimalInfo.imap) {
			return true;
		} else {
			return false;
		}
	}
	public boolean pop3() {
		if(this.serverType == ImapMessageMinimalInfo.pop3) {
			return true;
		} else {
			return false;
		}
	}
	private boolean supportModSeq;
	public void setSupportModSeq(boolean supportModSeq) {
		this.supportModSeq = supportModSeq;
	}
	public boolean getSupportModSeq() {
		return this.supportModSeq;
	}
	private Object uid;
	public void setUid(Object uid) {
		this.uid = uid;
	}
	public Object getUid() {
		return this.uid;
	}
	private int folderId;
	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}
	public int getFolderId() {
		return this.folderId;
	}
	private String folderName;
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getFolderName() {
		return this.folderName;
	}
	private int grahaMailAccountId;
	public void setGrahaMailAccountId(int grahaMailAccountId) {
		this.grahaMailAccountId = grahaMailAccountId;
	}
	public int getGrahaMailAccountId() {
		return this.grahaMailAccountId;
	}
	private long modseq;
	public void setModseq(long modseq) {
		this.modseq = modseq;
	}
	public long getModseq() {
		return this.modseq;
	}
	private long sizeLong;
	public void setSizeLong(long sizeLong) {
		this.sizeLong = sizeLong;
	}
	public long getSizeLong() {
		return this.sizeLong;
	}
	private String messageId;
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getMessageId() {
		return this.messageId;
	}
	private String mailCheckColumn; 
	public void setMailCheckColumn(String mailCheckColumn) {
		this.mailCheckColumn = mailCheckColumn;
	}
	public String getMailCheckColumn() {
		return this.mailCheckColumn;
	}
	public boolean checkByUID() {
		if(
			this.mailCheckColumn == null ||
			this.mailCheckColumn.equals("") ||
			this.mailCheckColumn.equalsIgnoreCase("UID")
		) {
			return true;
		} else {
			return false;
		}
	}
	private Date receivedDate;
	public Date getReceivedDate() {
		return this.receivedDate;
	}
	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}
	public ImapMessageMinimalInfo() {
	}
	public boolean compare(long modseq, long sizeLong, String messageId) {
		if(this.checkByUID()) {
			if(this.getSupportModSeq() && this.getModseq() == modseq) {
				return true;
			} else if(!this.getSupportModSeq() && this.getSizeLong() == sizeLong) {
				String otherMessageId = "";
				if(messageId != null) {
					otherMessageId = messageId;
				}
				String thisMessageId = "";
				if(this.getMessageId() != null) {
					thisMessageId = this.getMessageId();
				}
				return otherMessageId.equals(thisMessageId);
			}
			return false;
		} else {
			if(
				messageId != null && 
				!messageId.equals("") && 
				this.getMessageId() != null && 
				!this.getMessageId().equals("") &&
				messageId.equals(this.getMessageId())
			) {
				return true;
			} else {
				return false;
			}
		}
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("receivedDate = " + this.getReceivedDate());
		sb.append(", mailCheckColumn = " + this.getMailCheckColumn());
		sb.append(", messageId = " + this.getMessageId());
		sb.append(", sizeLong = " + this.getSizeLong());
		sb.append(", modseq = " + this.getModseq());
		sb.append(", grahaMailAccountId = " + this.getGrahaMailAccountId());
		sb.append(", folderName = " + this.getFolderName());
		sb.append(", folderId = " + this.getFolderId());
		sb.append(", uid = " + this.getUid());
		sb.append(", supportModSeq = " + this.getSupportModSeq());
		return sb.toString();
	}
}