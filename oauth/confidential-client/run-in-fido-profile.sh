../mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=fido -Djavax.net.ssl.trustStore=../fido-auth-server/keystore.p12 -Djavax.net.ssl.trustStorePassword=changeit"
