keytool -genkey -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslclientkeys -storetype PKCS12 -keyalg RSA -storepass "password"
keytool -export -keystore sslclientkeys -file sslclient.cer -storepass "password"
keytool -genkey -alias sslserver -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslserverkeys -storetype PKCS12 -keyalg RSA -storepass "password"
keytool -export -alias sslserver -keystore sslserverkeys -file sslserver.cer -storepass "password"
keytool -import -keystore sslservertrust -file sslclient.cer -storepass "password" -noprompt
keytool -import -alias sslserver -keystore sslclienttrust -file sslserver.cer -storepass "password" -noprompt

move sslclient* Client
move sslserver* Server

javac -cp .:./lib/* Client/*.java
javac -cp .:./lib/* Server/*.java