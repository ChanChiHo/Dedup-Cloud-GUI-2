Xcopy /E /I Client ClientDup 
javac -cp .:./lib/* ClientDup/*.java
keytool -genkey -alias sslclient%1 -dname "cn=PCL1904, ou=CSE, o=CUHK, c=HK" -keystore sslclientkeys -storetype PKCS12 -keyalg RSA -storepass "password"
keytool -export -alias sslclient%1 -keystore sslclientkeys -file sslclient.cer -storepass "password"
keytool -import -alias sslclient%1 -keystore Server/sslservertrust -file sslclient.cer -storepass "password" -noprompt
keytool -import -alias sslserver -keystore sslclienttrust -file Server/sslserver.cer -storepass "password" -noprompt
move sslclient* ClientDup