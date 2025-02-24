![Project tests](https://github.com/GhostOfEndless/chat-protector/actions/workflows/measure-coverage-workflow.yml/badge.svg)
# Система модерации контента в Telegram чатах

---
## Функционал
### Фильтры для удаления сообщений
Доступна фильтрация следующих типов сообщений:
* Текстовые: любые кликабельные сущности (номера телефонов, упоминания, ссылки и т.д.)

Для текстовых фильтров также можно выбрать режим работы белый/черный список и добавить исключения, 
которые будут обработаны в соответствии с режимом работы фильтра

### Настройка через REST API и Telegram
Настраивать модерацию чатов можно как через REST API, выполнив вход по полученным в Telegram боте логину и паролю, 
а можно через сам Telegram

_* Некоторые функции недоступны для настройки в Telegram боте ввиду невозможности их реализации стандартными 
элементами управления Telegram, например, добавление исключений для текстовых фильтров_

### История удалённых сообщений
Получение истории удалённых сообщений в указанном чате и её фильтрация по пользователю через API

### Модерация нескольких чатов
Можно добавить сколько угодно чатов и настраивать для каждого из них отдельную политику модерации

---
## Ключевые особенности
* __Безопасность.__ Все данные чата хранятся локально (postgres, rabbitmq, redis), доступ к серверу только 
у администратора чата;
* __Масштабируемость.__ Возможно поднять сколько угодно модулей обработки сообщений, тем самым устранив долгую 
обработку или пропуск части сообщений.

---
## Инструкция по запуску
_Для запуска требуется установленный docker compose в системе_
1. Создать бота в [Bot Father](https://t.me/BotFather)
2. Получить ID своего Telegram аккаунта в [Get My ID](https://t.me/getmyid_bot)
3. Клонировать репозиторий
   ```
   git clone https://github.com/GhostOfEndless/chat-protector.git
   ```
4. Перейти в директорию `compose`
   ```
   cd chat-protector/docker/compose
   ```
5. Открыть файл `.env` через любой удобный редактор и присвоить переменным `TELEGRAM_BOT_TOKEN` и 
`TELEGRAM_OWNER_ACCOUNT_ID` значения, полученные в 1 и 2 шагах соответственно
   ```
   ...
   TELEGRAM_BOT_TOKEN=9876543210:AABBCCDDEEFFGGHH1122334455667788990
   TELEGRAM_OWNER_ACCOUNT_ID=123456790
   ...
   ```
6. Запускаем приложение через docker compose
   ```
   docker compose -f compose.yml up -d
   ```
Теперь приложение запущено и готово к работе.
* _Swagger_: http://localhost:8080/swagger-ui/index.html
* _Grafana_: http://localhost:3000/
* _Kibana_: http://localhost:5601/

---
## Инструкция по обновлению
1. Перейти в директорию `compose`:
   ```
   cd chat-protector/docker/compose
   ```
2. Остановить запущенное приложение:
   ```
   docker compose -f compose.yml down
   ```
3. Обновить локальный репозиторий проекта:
   ```
   git pull origin master
   ```
4. Обновить образы сервисов:
   ```
   docker compose -f compose.yml pull
   ```
5. Поднять обновлённое приложение:
   ```
   docker compose -f compose.yml up -d
   ```