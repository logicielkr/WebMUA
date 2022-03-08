CREATE SCHEMA webmua;

CREATE SEQUENCE webmua."graha_mail$graha_mail_id";

create table webmua.graha_mail(
	graha_mail_id integer NOT NULL DEFAULT nextval('webmua.graha_mail$graha_mail_id'::regclass),
	subject character varying,
	message_id character varying,
	pop3_uid character varying,
	status character(1),
	imap_uid bigint,
	graha_mail_folder_id integer,
	sent_date timestamp with time zone,
	received_date timestamp with time zone,
	modseq bigint,
	size_long bigint,
	default_charset character varying,
	graha_mail_rid integer,
	graha_mail_fid integer,
	label character varying,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_pkey PRIMARY KEY (graha_mail_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail is '메일';

COMMENT ON COLUMN webmua.graha_mail.graha_mail_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail.subject IS '제목';
COMMENT ON COLUMN webmua.graha_mail.message_id IS 'Message ID';
COMMENT ON COLUMN webmua.graha_mail.pop3_uid IS 'POP3Folder 의 UID';
COMMENT ON COLUMN webmua.graha_mail.status IS '메시지상태';
COMMENT ON COLUMN webmua.graha_mail.imap_uid IS 'UIDFolder 의 UID';
COMMENT ON COLUMN webmua.graha_mail.graha_mail_folder_id IS '메일폴더 고유번호';
COMMENT ON COLUMN webmua.graha_mail.sent_date IS '보낸날짜';
COMMENT ON COLUMN webmua.graha_mail.received_date IS '받은날짜';
COMMENT ON COLUMN webmua.graha_mail.modseq IS 'MODSEQ';
COMMENT ON COLUMN webmua.graha_mail.size_long IS '메일크기(IMAP에서만)';
COMMENT ON COLUMN webmua.graha_mail.default_charset IS '기본 Charset';
COMMENT ON COLUMN webmua.graha_mail.graha_mail_rid IS '회신원본메일의 고유번호';
COMMENT ON COLUMN webmua.graha_mail.graha_mail_fid IS '전달원본메일의 고유번호';
COMMENT ON COLUMN webmua.graha_mail.label IS 'Label';
COMMENT ON COLUMN webmua.graha_mail.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_part$graha_mail_part_id";

create table webmua.graha_mail_part(
	graha_mail_part_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_part$graha_mail_part_id'::regclass),
	content_type character varying,
	disposition character varying,
	contents text,
	graha_mail_id integer,
	parent_content_type character varying,
	path character varying,
	default_charset character varying,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_part_pkey PRIMARY KEY (graha_mail_part_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_part is '메일내용';

COMMENT ON COLUMN webmua.graha_mail_part.graha_mail_part_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_part.content_type IS 'Content Type';
COMMENT ON COLUMN webmua.graha_mail_part.disposition IS 'Content Disposition';
COMMENT ON COLUMN webmua.graha_mail_part.contents IS '내용';
COMMENT ON COLUMN webmua.graha_mail_part.graha_mail_id IS '메일고유번호';
COMMENT ON COLUMN webmua.graha_mail_part.parent_content_type IS '상위 Content Type';
COMMENT ON COLUMN webmua.graha_mail_part.path IS '경로';
COMMENT ON COLUMN webmua.graha_mail_part.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_part.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_part.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_part.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_part.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_part.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_account$graha_mail_account_id";

create table webmua.graha_mail_account(
	graha_mail_account_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_account$graha_mail_account_id'::regclass),
	encryption_type character varying,
	host character varying,
	port integer,
	user_name character varying,
	password character varying,
	smtp_encryption_type character varying,
	smtp_host character varying,
	smtp_port integer,
	smtp_user_name character varying,
	smtp_password character varying,
	name character varying,
	email character varying,
	type character varying,
	leave_on_server character varying,
	default_charset character varying,
	imap_fetch_type character(1),
	signature text,
	mail_check_column character varying,
	graha_mail_account_template_id integer,
	order_number integer,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_account_pkey PRIMARY KEY (graha_mail_account_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_account is '메일계정';

COMMENT ON COLUMN webmua.graha_mail_account.graha_mail_account_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_account.encryption_type IS '암호화 방식';
COMMENT ON COLUMN webmua.graha_mail_account.host IS 'Host';
COMMENT ON COLUMN webmua.graha_mail_account.port IS 'Port';
COMMENT ON COLUMN webmua.graha_mail_account.user_name IS 'Login 아이디';
COMMENT ON COLUMN webmua.graha_mail_account.password IS '패스워드';
COMMENT ON COLUMN webmua.graha_mail_account.smtp_encryption_type IS '암호화 방식(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account.smtp_host IS 'Host(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account.smtp_port IS 'Port(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account.smtp_user_name IS 'Login 아이디(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account.smtp_password IS '패스워드(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account.name IS '별칭';
COMMENT ON COLUMN webmua.graha_mail_account.email IS '이메일';
COMMENT ON COLUMN webmua.graha_mail_account.type IS '서버의 유형';
COMMENT ON COLUMN webmua.graha_mail_account.leave_on_server IS '서버이메일삭제주기';
COMMENT ON COLUMN webmua.graha_mail_account.default_charset IS '기본 Charset';
COMMENT ON COLUMN webmua.graha_mail_account.imap_fetch_type IS 'IMAP 서버에서 메시지를 가져오는 방식';
COMMENT ON COLUMN webmua.graha_mail_account.signature IS '서명';
COMMENT ON COLUMN webmua.graha_mail_account.mail_check_column IS '이미 받아온 이메일인지 확인하는 방식';
COMMENT ON COLUMN webmua.graha_mail_account.graha_mail_account_template_id IS '메일 계정 템플릿 고유번호';
COMMENT ON COLUMN webmua.graha_mail_account.order_number IS '정렬순서';
COMMENT ON COLUMN webmua.graha_mail_account.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_account.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_account.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_account.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_account.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_account.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_folder$graha_mail_folder_id";

create table webmua.graha_mail_folder(
	graha_mail_folder_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_folder$graha_mail_folder_id'::regclass),
	name character varying,
	type character varying,
	modseq bigint,
	default_charset character varying,
	graha_mail_account_id integer,
	is_not_fetch boolean,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_folder_pkey PRIMARY KEY (graha_mail_folder_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_folder is '메일폴더';

COMMENT ON COLUMN webmua.graha_mail_folder.graha_mail_folder_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_folder.name IS '폴더이름';
COMMENT ON COLUMN webmua.graha_mail_folder.type IS '유형';
COMMENT ON COLUMN webmua.graha_mail_folder.modseq IS 'MODSEQ';
COMMENT ON COLUMN webmua.graha_mail_folder.default_charset IS '기본 Charset';
COMMENT ON COLUMN webmua.graha_mail_folder.graha_mail_account_id IS '메일계정 고유번호';
COMMENT ON COLUMN webmua.graha_mail_folder.is_not_fetch IS '서버에서 이메일 가져올지 여부';
COMMENT ON COLUMN webmua.graha_mail_folder.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_folder.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_folder.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_folder.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_folder.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_folder.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_address$graha_mail_address_id";

create table webmua.graha_mail_address(
	graha_mail_address_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_address$graha_mail_address_id'::regclass),
	type character varying,
	personal_name character varying,
	email_address character varying,
	graha_mail_id integer,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_address_pkey PRIMARY KEY (graha_mail_address_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_address is '보낸사람과 받는사람 등';

COMMENT ON COLUMN webmua.graha_mail_address.graha_mail_address_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_address.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_address.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_address.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_address.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_address.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_address.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_common_code$graha_mail_common_code_id";

create table webmua.graha_mail_common_code(
	graha_mail_common_code_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_common_code$graha_mail_common_code_id'::regclass),
	code character varying,
	value character varying,
	readonly boolean,
	order_number integer,
	upper_id integer,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_common_code_pkey PRIMARY KEY (graha_mail_common_code_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_common_code is '메일 공통 코드';

COMMENT ON COLUMN webmua.graha_mail_common_code.graha_mail_common_code_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_common_code.code IS '코드';
COMMENT ON COLUMN webmua.graha_mail_common_code.value IS '값';
COMMENT ON COLUMN webmua.graha_mail_common_code.readonly IS '읽기전용';
COMMENT ON COLUMN webmua.graha_mail_common_code.order_number IS '정렬순서';
COMMENT ON COLUMN webmua.graha_mail_common_code.upper_id IS '상위코드';
COMMENT ON COLUMN webmua.graha_mail_common_code.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_common_code.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_common_code.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_common_code.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_common_code.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_common_code.update_ip IS '최종수정자IP';

CREATE SEQUENCE webmua."graha_mail_account_template$graha_mail_account_template_id";

create table webmua.graha_mail_account_template(
	graha_mail_account_template_id integer NOT NULL DEFAULT nextval('webmua.graha_mail_account_template$graha_mail_account_template_id'::regclass),
	encryption_type character varying,
	host character varying,
	port integer,
	smtp_encryption_type character varying,
	smtp_host character varying,
	smtp_port character varying,
	type character varying,
	default_charset character varying,
	imap_fetch_type character varying,
	order_number integer,
	source character varying,
	name character varying,
	insert_date timestamp with time zone,
	insert_id character varying(50),
	insert_ip character varying(15),
	update_date timestamp with time zone,
	update_id character varying(50),
	update_ip character varying(15),
	CONSTRAINT graha_mail_account_template_pkey PRIMARY KEY (graha_mail_account_template_id)
) WITH ( OIDS=FALSE );

comment on table webmua.graha_mail_account_template is '메일 계정 템플릿';

COMMENT ON COLUMN webmua.graha_mail_account_template.graha_mail_account_template_id IS '고유번호';
COMMENT ON COLUMN webmua.graha_mail_account_template.encryption_type IS '암호화 방식';
COMMENT ON COLUMN webmua.graha_mail_account_template.host IS 'Host';
COMMENT ON COLUMN webmua.graha_mail_account_template.port IS 'Port';
COMMENT ON COLUMN webmua.graha_mail_account_template.smtp_encryption_type IS '암호화 방식(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account_template.smtp_host IS 'Host(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account_template.smtp_port IS 'Port(SMTP)';
COMMENT ON COLUMN webmua.graha_mail_account_template.type IS '서버의 유형';
COMMENT ON COLUMN webmua.graha_mail_account_template.default_charset IS '기본 Charset';
COMMENT ON COLUMN webmua.graha_mail_account_template.imap_fetch_type IS 'IMAP 서버에서 메시지를 가져오는 방식';
COMMENT ON COLUMN webmua.graha_mail_account_template.order_number IS '정렬순서';
COMMENT ON COLUMN webmua.graha_mail_account_template.source IS '출처';
COMMENT ON COLUMN webmua.graha_mail_account_template.name IS '이름';
COMMENT ON COLUMN webmua.graha_mail_account_template.insert_date IS '작성일시';
COMMENT ON COLUMN webmua.graha_mail_account_template.insert_id IS '작성자ID';
COMMENT ON COLUMN webmua.graha_mail_account_template.insert_ip IS '작성자IP';
COMMENT ON COLUMN webmua.graha_mail_account_template.update_date IS '최종수정일시';
COMMENT ON COLUMN webmua.graha_mail_account_template.update_id IS '최종수정자ID';
COMMENT ON COLUMN webmua.graha_mail_account_template.update_ip IS '최종수정자IP';

COPY webmua.graha_mail_account_template (graha_mail_account_template_id, encryption_type, host, port, smtp_encryption_type, smtp_host, smtp_port, type, default_charset, imap_fetch_type, order_number, source, name, insert_date, insert_id, insert_ip, update_date, update_id, update_ip) FROM stdin;
1	SSL/TSL	imap.gmail.com	993	SSL/TSL	smtp.gmail.com	465	imap	\N	F	\N	https://support.google.com/mail/answer/7126229	Gmail(IMAP)	\N	\N	\N	\N	\N	\N
2	SSL/TSL	pop.gmail.com	995	STARTTLS	smtp.gmail.com	587	pop3	\N	\N	\N	https://support.google.com/mail/answer/7104828	Gmail(POP3)	\N	\N	\N	\N	\N	\N
3	SSL/TSL	imap.naver.com	993	SSL/TSL	smtp.naver.com	465	imap	MS949	F	\N	https://www.naver.com/	Naver(IMAP)	\N	\N	\N	\N	\N	\N
4	SSL/TSL	pop.naver.com	995	SSL/TSL	smtp.naver.com	465	pop3	MS949	\N	\N	https://www.naver.com/	Naver(POP3)	\N	\N	\N	\N	\N	\N
5	SSL/TSL	imap.daum.net	993	SSL/TSL	smtp.daum.net	465	imap	MS949	F	\N	https://www.daum.net/	Daum(IMAP)	\N	\N	\N	\N	\N	\N
6	SSL/TSL	pop.daum.net	995	SSL/TSL	smtp.daum.net	465	pop3	MS949	\N	\N	https://www.daum.net/	Daum(POP3)	\N	\N	\N	\N	\N	\N
\.

COPY webmua.graha_mail_common_code (graha_mail_common_code_id, code, value, readonly, order_number, upper_id, insert_date, insert_id, insert_ip, update_date, update_id, update_ip) FROM stdin;
1	default_charset	기본 Charset	t	\N	0	\N	\N	\N	\N	\N	\N
2	MS949	MS949	f	1	1	\N	\N	\N	\N	\N	\N
3	UTF8	UTF8	t	2	1	\N	\N	\N	\N	\N	\N
4	folder_name_type_map	폴더이름과 type의 매핑정보	t	\N	0	\N	\N	\N	\N	\N	\N
5	Inbox	Inbox	t	1	4	\N	\N	\N	\N	\N	\N
6	INBOX	Inbox	t	2	4	\N	\N	\N	\N	\N	\N
7	임시보관함	Draft	t	3	4	\N	\N	\N	\N	\N	\N
8	Draft	Draft	t	4	4	\N	\N	\N	\N	\N	\N
9	DRAFT	Draft	t	5	4	\N	\N	\N	\N	\N	\N
10	DRAFTS	Draft	t	6	4	\N	\N	\N	\N	\N	\N
11	보낸편지함	Sent	t	7	4	\N	\N	\N	\N	\N	\N
12	Sent	Sent	t	8	4	\N	\N	\N	\N	\N	\N
13	SENT	Sent	t	9	4	\N	\N	\N	\N	\N	\N
14	Sent Messages	Sent	t	10	4	\N	\N	\N	\N	\N	\N
15	휴지통	Trash	t	11	4	\N	\N	\N	\N	\N	\N
16	Trash	Trash	t	12	4	\N	\N	\N	\N	\N	\N
17	TRASH	Trash	t	13	4	\N	\N	\N	\N	\N	\N
18	Deleted Messages	Trash	t	14	4	\N	\N	\N	\N	\N	\N
19	leave_on_server	POP3 서버에서 메시지 삭제주기	t	\N	0	\N	\N	\N	\N	\N	\N
20	none	삭제하지 않음	t	1	19	\N	\N	\N	\N	\N	\N
21	0	즉시삭제	t	2	19	\N	\N	\N	\N	\N	\N
22	3	3일후삭제	f	3	19	\N	\N	\N	\N	\N	\N
23	7	7일후삭제	f	4	19	\N	\N	\N	\N	\N	\N
24	17	14일후삭제	f	5	19	\N	\N	\N	\N	\N	\N
25	imap_fetch_type	IMAP 서버에서 메시지를 가져오는 방식	t	\N	0	\N	\N	\N	\N	\N	\N
26	F	폴더별(모두)	t	1	25	\N	\N	\N	\N	\N	\N
27	H	계정별(헤더만)	t	2	25	\N	\N	\N	\N	\N	\N
28	A	계정별(모두)	t	3	25	\N	\N	\N	\N	\N	\N
29	M	폴더별(헤더만)	t	4	25	\N	\N	\N	\N	\N	\N
30	graha_mail_account_type	메일 계정 유형(pop3 or imap or 보관함)	t	\N	0	\N	\N	\N	\N	\N	\N
31	pop3	POP3	t	1	30	\N	\N	\N	\N	\N	\N
32	imap	IMAP	t	2	30	\N	\N	\N	\N	\N	\N
33	store	보관함	t	3	30	\N	\N	\N	\N	\N	\N
34	encryption_type	암호화유형	t	\N	0	\N	\N	\N	\N	\N	\N
35	plain	plain	t	1	34	\N	\N	\N	\N	\N	\N
36	STARTTLS	STARTTLS	t	2	34	\N	\N	\N	\N	\N	\N
37	SSL/TSL	SSL/TSL	t	3	34	\N	\N	\N	\N	\N	\N
38	graha_mail_address_type	받는사람유형	t	\N	0	\N	\N	\N	\N	\N	\N
39	TO	받는사람	t	1	38	\N	\N	\N	\N	\N	\N
40	CC	참조	t	2	38	\N	\N	\N	\N	\N	\N
41	BCC	숨은참조	t	3	38	\N	\N	\N	\N	\N	\N
42	graha_mail_folder_type	폴더유형	t	\N	0	\N	\N	\N	\N	\N	\N
43	Inbox	Inbox	t	1	42	\N	\N	\N	\N	\N	\N
44	Sent	Sent	t	2	42	\N	\N	\N	\N	\N	\N
45	Draft	Draft	t	3	42	\N	\N	\N	\N	\N	\N
46	Trash	Trash	t	4	42	\N	\N	\N	\N	\N	\N
47	User	User	t	5	42	\N	\N	\N	\N	\N	\N
49	UID	UID	t	1	48	\N	\N	\N	\N	\N	\N
50	MessageID	MessageID	t	2	48	\N	\N	\N	\N	\N	\N
48	mail_check_column	이미 받아온 이메일인지 확인하기 위해 비교하는 칼럼	t	\N	0	\N	\N	\N	\N	\N	\N
51	받은편지함	Inbox	t	2	4	\N	\N	\N	\N	\N	\N
\.

SELECT pg_catalog.setval('webmua."graha_mail_common_code$graha_mail_common_code_id"', 51, false);

SELECT pg_catalog.setval('webmua."graha_mail_account_template$graha_mail_account_template_id"', 6, false);
