keytool -importcert \
  -alias flat-service \
  -file ~/SOA/certs/flat-service.crt \
  -keystore agency-truststore.p12 \
  -storetype PKCS12