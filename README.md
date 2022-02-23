# [Graha 응용프로그램]webmua

## 1. about

Graha를 활용한 Web 기반의 이메일 클라이언트 프로그램이다.

## 2. 기능

webmua 프로그램의 외관은 웹메일 프로그램과 유사하지만, pop3 혹은 imap 서버에서 가져온 이메일을 처리한다.

이 프로그램의 주요 목표는 다음과 같다.

- 이메일의 기본기능을 제공한다.
- pop3 혹은 imap 서버의 메시지의 사본을 보관한다.
- 프로그래머인 사용자가 필요한 나머지 기능을 추가할 수 있도록 한다.

## 3. 실행환경

이 프로그램은 다음과 같은 환경에서 개발되고 테스트 되었다.

- Apache Tomcat 7.x 이상
- PostgreSQL 9.1.4 이상
- JDBC 4.0 이상
- Graha 0.5.0.6 이상
- 최신버전의 JavaMail API

## 4. 테스트한 메일 서버

- Apache James 2.x 의 pop3 계정
- Apache James 3.x 의 imap 계정
- Kakao(Daum) 의 imap 계정 (imap 사용을 설정해야 한다)
- Naver 의 imap 계정 (imap 사용을 설정해야 한다)
- Aol. 의 imap 계정 (imap 사용을 설정하고, Account Security 에서 "App Password" 을 설정해야 한다)
- Gmail 의 imap 게정 (imap 사용을 설정하고, 2단계 인증을 설정하고, "App Password" 을 설정해야 한다)

> Aol. 과 Gmail에서 "App Password" 는 2단계 인증을 사용할수 없는 경우에 임시 패스워드를 발급받아 사용하는 것을 말한다.

## 4. 일반적이지 않은 구현

### 4.1. 이메일을 발송하는 사용자 인터페이스

- 메일쓰기 기능에서는 (이메일을 발송하지 않고) 메시지를 Draft 폴더에만 저장하고,
- Draft 폴더의 메일 상세보기 화면에서 이메일 전송 버튼을 클릭해야만 이메일이 전송된다.

> 미리보기를 한 이후에만 전송할 수 있다고 보면 된다.
> 사용자의 과거 경험과 조화를 이루지 못하지만,
> 메일의 내용이나 첨부파일을 확인한 이후에 이메일을 전송하기 때문에, 잘못된 파일을 첨부하는 등의 사소한 실수를 줄일 수 있다.

### 4.2. HTML 이메일 작성을 지원하지 않는다.

## 5. 비표준 지원

이 프로그램은 표준에서 벗어난 메시지로 부터 발생하는 문제(화면에서 한글이 깨지는 현상)를 해결하기 위해 다음과 같이 처리한다.

### 5.1. 헤더 정보에서 얻어온 charset을 이용하고, 이메일 계정, 폴더에 각각 기본 문자셋을 지정할 수 있다.

   - 메일 헤더의 경우 US-ASCII를 벗어난 경우,
   - 메일 내용은 charset 이 지정되지 않는 경우,
   - 헤더 정보에서 얻어온 charset, 메일의 기본 문자셋, 폴더의 기본 문자셋, 이메일 계정의 기본 문자셋을 우선순위로 처리한다.
   - Thunderbird 와 유사한 방식이지만, 헤더 정보에서 얻어온 charset을 사용한다는 것만 다르다.

### 5.2. 이메일 상세보기 화면에서 charset 을 변경 할 수 있다.

   - 앞으로 이 메일은 여기서 지정된 charset 을 기본 문자셋으로 사용하게 될 것이다.
   - Thunderbird 에서도 이와 같은 방식을 지원한다.

### 5.3. Subject를 가져오기 위한 특별한 처리

   - Subject 헤더가 Quotation mark 로 묶여 있는 경우, 이를 제거하고 처리한다(US-ASCII 범위인 경우에만).
   - Subject 헤더가 base64로 인코딩 되어 있고, 여러 줄인 경우, Message.getSubject() 대신에 MimeUtility.decodeText 를 본 떠 만든 것을 사용한다.
   - JavaMail API 에서는 매 줄마다 decodeWord 메소드를 호출하는데, byte array 를 문자열로 변경 할 때(new String()) 한글이 깨지는 경우가 있다.

### 5.4. 이메일 주소를 가져오기 위한 특별한 처리

   - JavaMail API는 이메일 주소 형식이 잘못된 것이 명백한 경우, 예외를 발생시키는데, 여러 개의 이메일 주소가 지정되어 있고, 그 중 1개의 이메일 주소 형식이 잘못된 경우 전체가 에러가 난다.
   - 이를 처리하기 위해서 JavaMail API 로 이메일 주소 처리가 실패한 경우, 이메일 주소를 1개씩 읽어, 실패한 것만 skip 하는 방식으로 처리한다.
   - (스팸메일일 가능성이 매우 높지만) 이메일 주소가 매우 긴 경우, 이메일 주소 사이에 줄바꿈이 발생하는 경우도 있고, 이 경우 JavaMail API 에서 처리할 수 없으므로, 프로그램적으로 처리한다.

<!--
이메일과 같이 불특정 다수가 참여하는 시스템이 약속(표준)이 잘 지켜져야만 지속가능하다.

불행한 것은 이메일 시스템은 표준 이전부터 계속되어 왔고, 표준에서 벗어난 이메일 메시지가 발견될 가능성이 매우 높다는 것이다.

표준을 준수하는 것은 매우 중요한 일이고, 기술이나 표준에 대한 이해가 향상된 이후에 만들어진 시스템에서 발송되는 이메일 메시지는 대체로 표준을 준수하는 편이다.

다만, 조금만 과거로 조금만 돌아가면 기술과 표준에 대한 이해가 부족한 상태에서 경험에 의존해서 만들어진 이메일 시스템이 많았고, 그 당시에 발송된 이메일은 물론이고, 최근에 발송된 이메일도 그 당시에 만들어진 메일시스템을 사용하고 있다면, 표준에서 벗어나 있을 가능성이 있다.

> 지금은 Thunderbird 나 GMail 에서 발송한 메시지를 참고하면 되지만, 참고할 만한 표준구현체도 변변하지 못했던 시절에는 개발자의 개인 계정에 메시지가 도착하고, 그 메시지가 잘 보이면 그만인 시절도 있었다.

불행한 것은 비표준 메시지가 에러가 발생시키거나, 명백한 프로그램의 오류로 여겨질 수 있는 결과가 되기도 한다는 것이다.

> 저명한 이메일 호스팅 업체에서 비표준 메시지를 잘 처리하고 있다면 더더욱 그렇다.

대표적인 사례는 한글이 깨져서 화면에 출력되는 것인데, 대한민국과 같이 여러 개의 다른 charset 을 사용하고 있는 나라에서는 비슷한 문제가 있을 수 있다. 

이메일 메시지는 대략 영문 키보드에서 벗어나는 문자가 포함된 경우 특별한 처리를 거쳐야 하고, 특히 헤더에서는 한글 따위의 문자를 직접 사용할 수 없다.

만약 이메일 메시지에 한글이 포함되어야 하는 경우, 약속된 방법으로 적절한 인코딩을 거쳐 US-ASCII 범위로 변경해야 하고,
어떤 방식으로 인코딩 했는지, 사용하는 charset 이 무엇인지도 메시지에 포함되어야,
받는 쪽에서도 이메일을 발송한 쪽에서 알려준 방법으로 메시지를 표시할 수 있다.

가장 빈번하게 발생하는 문제는 한글 따위가 아무런 처리도 없이 포함되어 있는 경우인데,
webmua는 메시지의 다른 부분에서 charset 이 지정되어 있는지 찾아서 처리하고,
그대로 처리가 안되면, 사용자가 charset 을 변경할 수 있도록 하는 방법을 마련하였다.

-->

## 6. 제한

### 6.1. imap 혹은 pop3 서버에서 이메일 가져오는 기능이 느리게 동작하는 경우에도 인내심을 가지고 실행이 끝날때가지 기다려야 한다. 

imap 혹은 pop3 서버에서 받아와야 할 이메일이 많은 경우 매우 느리게 동작할 수 있으며, 다음과 같은 상황에서 심각할 수 있다.

- 계정을 추가하고 처음 이메일을 받아오는데, imap 혹은 pop3 서버에 많은 메시지가 있는 경우
- 계정을 추가한 이후에 오래동안 메시지를 받아오지 않았는데, 그 사이에 imap 혹은 pop3 서버에 많은 메시지가 쌓인 경우

일반적인 상황에서도 (연결을 pooling 할 수 없으므로) imap 혹은 pop3 연결을 맺는데 많은 비용이 발생한다.

### 6.2. imap 혹은 pop3 서버에서 메시지를 가져오는데 실패하면 다음번에도 실패할 가능성이 높다.

webmua 프로그램은 한번 가져온 이메일은 다시 가져오지 않는다.

최근 이메일부터 서버에서 가져오다가, 이미 받아온 이메일을 발견하면, 그 이후의 것들은 가져오지 않는다.

만약 imap 혹은 pop3 서버에서 1개의 메시지만 받아오고, 내부적인 에러가 발생해서 나머지 메시지를 받아오지 못했다면, 나머지 메시지는 앞으로도 가져오지 못할 가능성이 높다.

이미 받아온 이메일인지 판정하기 위해 다음과 같은 방식을 사용한다.

- pop3 서버의 경우 : UID로만 비교한다(MessageID 로만 비교하도록 설정할 수 있다).
- imap 서버의 경우 : UID 와 modseq 혹은 UID 와 메시지 크기, MessageID 로 비교한다(MessageID 로만 비교하도록 설정할 수 있다).
- imap 서버가 modseq를 지원하는 경우 imap 서버의 이메일 폴더의 가장 큰 modseq 와 최근에 받아온 이메일의 가장 큰 modseq을 비교한다.

> modseq는 지원하지 않는 경우가 더 많다.  modseq를 지원하는 메일서버는 Apache James 3.x 가 대표적이고, 이메일 서비스 업체중에는 Google 가 대표적이다.
> 이 글을 작성하는 시점에서 Aol, Daum(Kakao), Naver 는 modseq을 지원하지 않는 것이 확인되었다.

### 6.3. imap 서버와 통신하는 방식이 다르다.

webmua 프로그램은 imap 서버와 통신할 때 Thunderbird나 Outlook 과 같이 전통적인 이메일 클라이언트들과 달리 pop3 서버에 접근하는 것과 거의 유사한 방식을 사용한다.

전통적인 이메일 클라이언트들은 imap 서버와 통신하면서 클라이언트의 역활만 담당하고, imap 서버를 이메일 저장소로 사용하면서, 클라이언트에는 이메일 목록을 표시하기 위한 정도의 cache 정도만을 보관한다.

모든 변경사항은 imap 서버에 저장하고, 클라이언트와 서버가 서로 다른 데이타를 가지고 있는 경우 언제나 서버가 우선이 된다. 

반면 webmua 프로그램은 pop3 와 통신하는 것과 매우 유사한 방식으로 통신하며, 구체적인 사항은 다음과 같다.

- 서버에서 이메일을 가져올 때, 메시지 전체를 다운로드 받아서, eml 파일로 저장하고, 데이타베이스와 파일시스템에 서비스용 cache 데이타를 생성한다.
- 먼저 메시지 헤더만 가져오고, 이메일 내용을 확인할 때, 전체 메시지를 가져오도록 설정할 수 있지만, 일부 imap 서버에서 오류가 발견되었으므로 추천하지 않는다.
- 이메일을 발송하거나 메시지를 다른 폴더로 이동하거나 삭제한다고 하더라도 imap 서버에 반영하지 않는다.
- imap 서버에서 삭제된 메시지를 확인하지 않는다(다만, imap 서버의 최신 메일 중 uid 가 동일하지만, MessageID 나 size 가 다른 경우 삭제된 메일로 취급한다).

### 6.4. 가급적 pop3 서버를 이용한다.

pop3 서버를 이용하면 불편한 점도 있다.

- 받은편지함(Inbox)의 이메일만 받아올 수 있다.

반면, imap 서버를 이용할 경우 다음과 같이 성능저하와 기능상의 제약을 감수해야 한다.

- webmua 프로그램은 pop3 나 imap 서버에서 메시지 전체를 다운로드해서, webmua 서버에 저장한다. 
이런 방식은 소규모의 트렌젝션을 빈번하게 발생시키는 imap 통신규약과 조화롭지 못하기 때문에, 
성능에 미치는 부정적인 영향이 있다.
- webmua는 서버용 프로그램이기 때문에,
다른 imap 클라이언트와 같은 방식으로 구현하는 것은 한계가 있고,
imap 클라이언트라고 할수 없을 정도로 대부분의 기능이 누락되었다.

먼저 imap 서버로 계정을 설정하고 모든 편지함의 이메일을 받아온 이후에, pop3 로 변경하는 방법이 있을 수 있고, 이 경우 다음과 같은 절차에 따른다.

> 다만, imap 서버를 이용하는 경우라도 하더라도 서버에서 받아와야 하는 이메일의 갯수가 적은 경우 성능저하가 심각한 수준은 아니므로 매우 유용한 방법은 아니다.

- 받은편지함(Inbox)에 있는 이메일을 다른 편지함으로 이동한다.
- imap 서버로 계정을 설정하고 이메일을 받아온다.
- pop3 서버로 변경하고 사용한다.

> imap 서버와 pop3 서버는 같은 서버라고 하더라도 메시지의 고유번호가 다르기 때문에, imap 서버에서 pop3 서버로 변경하면, 받은편지함(Inbox)에 있는 이메일을 다시 다운로드 한다.

또 다른 방법은 다음과 같다.

- pop3 서버로 설정하고 이메일을 받아온다.
- 웹메일 서비스를 통해서 imap 서버에 받은편지함(Inbox)에 있는 이메일을 임시로 만든 폴더로 옮긴다.
- webmua 서버에서 받은편지함(Inbox)에 있는 이메일 임시로 만든 폴더로 옮긴다.
- 웹메일 서비스를 통해서 imap 서버에서 받아와야 하는 다른 폴더에 있는 이메일을 받은편지함(Inbox)으로 옮긴다.
- webmua 에서 이메일을 받아온다.
- 웹메일 서비스를 통해서 받은편지함(Inbox)에 있는 이메일을 원래 폴더로 옮긴다.

<!--
imap은 서버가 중심이 되는데 반해, 조금 더 오래전에 고안된 pop3 방식은 메시지를 클라이언트로 다운로드 하는 것을 전제로 하고,
인터넷 전송속도가 느리거나 서버가 서버 스토리지를 제공할 수 없는 경우에 최적화되어 있다.

imap은 휴대전화와 같이 저장공간이 충분하지 않을 가능성이 높거나, 전송량에 따라 추가적인 통신요금이 발생하는 클라이언트에 적합하다.

imap은 작은 요청을 계속해서 처리하는 것을 전제로 하기 때문에, imap을 pop3 처럼 사용하면 속도가 훨씬 더 느려지게 된다.
-->
<!--
pop3 서버의 경우 메시지를 파일시스템에 보관(Mbox 나 Maildir)하는 것이 유리하지만,
imap 서버의 경우 파일시스템에 보관할 것이면 Maildir에 보관해야 하고, 데이타베이스에 메시지를 보관해도 성능에 큰 영향이 없다.
-->

## 7. 알려진 버그 및 이슈

이 프로그램의 알려진 버그는 다음과 같다.

- UTF-7은 처리하지 못한다.

## 8. Apache James를 위한 tip

이 프로그램에 메일서버가 더해지면, 웹메일 서비스를 구축할 수 있는데, 
메일서버로 Apache James 를 검토하고 있다면, 필수적인 tip 을 소개하기로 한다.

Apache James 에서 비표준 메시지를 정상적으로 수신하기 위해서는(한글이 깨지지 않기 위해서는), 
Apache James 를 기동할 때 다음과 같은 파라미터가 추가되어야 한다.

```
-Dfile.encoding=ISO-8859-1
```

> US-ASCII 가 아니다.  US-ASCII와 ISO-8859-1 는 바이트 수가 다르다.

이를 위해서, Apache James 2 에서는 다음과 같은 환경변수를 설정한 이후에, Apache James 를 기동하면 된다.

```
export PHOENIX_JVM_OPTS="-Dfile.encoding=ISO-8859-1"
```

Apache James 3 에서는 conf/wrapper.conf 파일에 다음과 같은 내용을 추가해야 한다.

> Apache James 2 에서도 Apache James 3 와 같은 방법을 사용할 수 있다.

```
wrapper.java.additional.15=-Dfile.encoding=ISO-8859-1
```

> 15는 일련번호 이므로 적당히 변경한다.

Apache James 를 기동하고, 다음의 명령어로 프로세스를 검사했을 때, Apache James 프로세스가 보여야 한다.

```bash
ps -ef | grep james | grep ISO-8859-1
```
<!--
다만, 메일서버를 직접 운영하는 것은 신중할 필요는 있다.

이메일을 발송할 때 상대방 이메일 서버에서 스팸함으로 보내거나 방지하기 위해서 spf 설정과 white domain 등록은 필수이고,
해외로 이메일을 발송할 일이 있을 것 같으면 다른 설정(예를 들어 Reverse DNS, DKIM, DMARC) 을 해야하는 경우도 있다.

들어오는 이메일 중에 스팸을 걸러내는 것도 중요한 일 중에 하나이다.

스펨이 단순히 광고성 이메일을 일컫는 것이라면,
그걸 굳이 시스템에서 원천적으로 차단할 필요가 있을까 싶지만(사용자가 차단하면 될 문제이므로),
피싱(phishing)이나 위험한 파일이 첨부될 가능성까지 고려한다면, 사용자가 입을 불측의 피해를 예방하는 활동은 아무리 강조해도 지나치지 않을 것이다.

이런 일들은 경험에 의존해야 하는 경우가 많은데,
오랜 노하우를 가진 전문 이메일 서비스 업체들이 우위에 있을 수 밖에 없다.
-->
## 9. 배포하는 곳

* 소스코드 : GitHub JavascriptExecutor 프로젝트 (https://github.com/logicielkr/webmua)
* 웹사이트 : Graha 홈페이지 (https://graha.kr)
