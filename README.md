# Сервис-ориентированная архитектура: лабораторный проект

TODO: добавить описание проекта

## Деплой `flat-service`

Согласно варианту, `flat-service` необходимо деплоить на сервере приложений Jetty. Для этого необходимо скачать zip-архив с данным сервером на официальном сайте [Jetty](https://jetty.org/download.html), разархивировать его и произвести базовую настройку, согласно инструкции, находящейся внутри zip-архива.
В рамках данного проекта используется сервер Jetty версии `12.1.14`.

Для деплоя приложения на `helios` создана директория `~/SOA/runtime/flat-service/`. В данной директории есть папка `webapps`, внутрь которой необходимо размещать `war` приложения (`flat-service.war`).

Для запуска Jetty сервера необходимо находиться в директории `~/SOA/runtime/flat-service/` и выполнить команду:

```sh
java -jar $JETTY_HOME/start.jar
```

## Подключение `ssl` к `flat-service`

Для создания `HTTPS`-соединения необходимо создать пару ключей с помощью скрипта [gen_keys.sh](./scripts/gen_keys.sh), после чего экспортировать сертификат с помощью скрипта [export_cert.sh](./scripts/export_cert.sh).

В результате генерации пары ключей будет создано keystore-хранилище с расширением `.p12`, его необходимо разместить в директории `$JETTY_BASE/etc`. Далее необходимо подключить модули для `https` и `ssl` с помощью следующей команды:

```sh
java -jar $JETTY_HOME/start.jar --add-modules=https,ssl
```

После исполнения команды в файле `$JETTY_BASE/start.d/ssl-context.ini` необходимо изменить параметры:

```txt
jetty.sslContext.keyStorePath=etc/keystore.p12
jetty.sslContext.keyStorePassword=keystore_password
```

После этого в Jetty будет доступно подключение по https.

Чтобы отключить возможность подключения по http как вариант можно переименовать файл `$JETTY_BASE/start.d/http.ini`:

```sh
cd $JETTY_BASE/start.d
mv http.ini http.ini.disabled
```

Для клиента необходимо при последующих запросах начать использовать сгенерированный сертификат, чтобы подключение прошло успешно.

## Полезные команды

Сборка `flat-service` в `war`:

```sh
cd flat-service
gradle build
```

`flat-service.war` будет находиться в `flat-service/build/libs`.

---

Запуск Jetty сервера:

```sh
java -jar $JETTY_HOME/start.jar jetty.ssl.port=8444
```

Генерация из OpenAPI спецификации для Spring:

```sh
cd flat-service
./gradlew openApiGenerat
```
