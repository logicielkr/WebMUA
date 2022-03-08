# 시스템 환경 및 설치요령

WebMUA 프로그램은 Graha 응용프로그램이므로, [메모장 프로그램의 시스템 환경 및 설치요령](https://github.com/logicielkr/memo/blob/master/install-guide.md)과 거의 유사한 방법으로 설치할 수 있다.

WebMUA 프로그램은 이메일 계정의 패스워드를 Java 에서 지원하는 AES 방식으로 암호화/복호화 한다.

> JDK 1.7 이하에서는 [BouncyCastle](https://www.bouncycastle.org/) 라이브러리를 사용하며, Runtime 에서 결정한다.

WebMUA 프로그램은 필자가 배포하는 여타의 Graha 응용프로그램과는 다르게 AES 암호화/복호화에 공급되는 패스워드와 iv 를 위한 값들을 변경하고 다시 컴파일 해야 한다.

### WebMUA 컴파일 가이드

Apache Ant 가 설치되어 있는 경우, build.xml 파일을 이용해서 컴파일/패키징 할 수 있지만, 다음 사항의 주의해야 한다.

- build.xml 파일의 "/opt/java/lib/" 이하의 jar 파일들을 적절히 추가해야 한다.
- 컴파일을 위해서 JDK 8 이상이 요구된다.  부득이 JDK 7 이하를 사용하는 경우 AESGCMCipher.java 를 제거하고, 이를 참조하는 EncryptorAESGCMImpl.java 파일을 손봐서 사용해야 한다.
- 패키징 할때는 EncryptorAESGCMImpl.java 파일이 포함되지 않도록 주의한다(build.xml 파일에서 주석으로 막힌 부분을 참고한다).
