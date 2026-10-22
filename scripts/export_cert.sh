keytool -exportcert \
    -keystore flat-service.p12 \
    -alias flat-service \
    -rfc \
    -file flat-service.crt \
    -v