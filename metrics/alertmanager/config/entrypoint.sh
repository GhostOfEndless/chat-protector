#!/bin/sh
set -e

sed -i "s|\${TELEGRAM_ALERT_BOT_TOKEN}|${TELEGRAM_ALERT_BOT_TOKEN}|g" /etc/alertmanager/alertmanager.yml
sed -i "s|\${TELEGRAM_OWNER_ACCOUNT_ID}|${TELEGRAM_OWNER_ACCOUNT_ID}|g" /etc/alertmanager/alertmanager.yml

exec alertmanager --config.file=/etc/alertmanager/alertmanager.yml