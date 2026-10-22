keytool -genkeypair \
    -alias flat-service \
    -validity 365 \
    -keyalg RSA \
    -keysize 2048 \
    -keystore flat-service.p12 \
    -storetype pkcs12 \
    -dname "CN=localhost" \
    -ext san=dns:localhost,ip:127.0.0.1 \
    -v