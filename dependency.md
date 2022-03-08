# 사용한 라이브러리 혹은 프로그램

## 1. 클라이언트 라이브러리

### 1.1. jQuery (https://jquery.com/)

- License : MIT license(https://jquery.org/license/)
- CDN : https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js
- Language : Javascript

## 2. 서버 라이브러리

### 2.1. Graha (http://graha.kr/)

- License : LGPLv3 (https://github.com/logicielkr/graha/blob/master/LICENSE)
- GitHUB : https://github.com/logicielkr/graha
- Language : Java

### 2.2. JavaMail (https://javaee.github.io/javamail/)

- License : CDDL Version 1.1, GPL Version 2 CLASSPATH EXCEPTION (https://javaee.github.io/javamail/LICENSE)
- Language : Java

### 2.3. JAF (JavaBeans Activation Framework)

- License : Oracle Binary Code License Agreement for Java SE (https://www.oracle.com/downloads/licenses/java-se-archive-license.html)
- Download : https://www.oracle.com/java/technologies/downloads.html
- Language : Java

<!--
이 라이브러리는 JDK 9부터 Deprecated 이므로, 머지않은 미래에 걷어내야 한다.

-->

### 2.4. Apache Commons FileUpload (https://commons.apache.org/proper/commons-fileupload/)

- License : Apache License (http://www.apache.org/licenses/)
- Language : Java
- 이 라이브러리는 Graha 가 사용하는 라이브러리이다.  파일 업로드 기능을 사용하지 않는다면, 이 라이브러리는 필요 없다.

### 2.5. Apache Commons IO (http://commons.apache.org/proper/commons-io/)

- License : Apache License (http://www.apache.org/licenses/)
- Language : Java
- 이 라이브러리는 Apache Commons FileUpload 에서 사용하는 라이브러리이다.

### 2.6. Bouncy Castle (https://www.bouncycastle.org/)

- License : MIT (https://www.bouncycastle.org/license.html)
- Language : Java
- AES-GCM 암호화를 위해 JDK 1.7 이하에서만 사용한다.
- 컴파일을 위한 필수 라이브러리이지만, JDK 8 부터는 사용되지 않는다.

### 2.7. PostgreSQL JDBC Driver (https://jdbc.postgresql.org/)
- License : BSD 2-clause "Simplified" License (https://jdbc.postgresql.org/about/license.html)
- Language : Java

### 2.8. xdbc (http://graha.kr/)

- License : LGPLv3 (https://github.com/logicielkr/xdbc/blob/master/LICENSE)
- GitHUB : https://github.com/logicielkr/xdbc
- Language : Java
- 데이타베이스를 프로그램적으로 2중화하거나 디버깅 용도로만 필요하다.

## 3. 서버 프로그램

### 3.1. Apache Tomcat® (http://tomcat.apache.org/)

- License : Apache License (http://www.apache.org/licenses/)
- Language : Java

### 3.2. PostgreSQL (https://www.postgresql.org/)

- License : PostgreSQL License (https://www.postgresql.org/about/licence/)
- Language : C
