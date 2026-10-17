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
java -jar $JETTY_HOME/start.jar jetty.http.host=127.0.0.0 jetty.http.port=8080
```
