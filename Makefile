default: 
	keytool -genkey -alias sslclient1 -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslclientkeys -storetype PKCS12 -keyalg RSA -storepass "password"
	keytool -export -alias sslclient1 -keystore sslclientkeys -file sslclient.cer -storepass "password"
	keytool -genkey -alias sslserver -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslserverkeys -storetype PKCS12 -keyalg RSA -storepass "password"
	keytool -export -alias sslserver -keystore sslserverkeys -file sslserver.cer -storepass "password"
	keytool -import -alias sslclient1 -keystore sslservertrust -file sslclient.cer -storepass "password" -noprompt
	keytool -import -alias sslserver -keystore sslclienttrust -file sslserver.cer -storepass "password" -noprompt
	mv sslclient* Client
	mv sslserver* Server
	javac -cp .:./lib/* Client/*.java
	javac -cp .:./lib/* Server/*.java

clean:
	rm Client/ssl*
	rm Server/ssl*
	rm Client/*.class
	rm Server/*.class
	
duplicate:
	echo counter is $(ver)
	cp -R Client ClientDup
	javac -cp .:./lib/* ClientDup/*.java
	keytool -genkey -alias sslclient$(ver) -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslclientkeys -storetype PKCS12 -keyalg RSA -storepass "password"
	keytool -export -alias sslclient$(ver) -keystore sslclientkeys -file sslclient.cer -storepass "password"
	keytool -import -alias sslclient$(ver) -keystore Server/sslservertrust -file sslclient.cer -storepass "password" -noprompt
	keytool -import -alias sslserver -keystore sslclienttrust -file Server/sslserver.cer -storepass "password" -noprompt
	mv sslclient* ClientDup