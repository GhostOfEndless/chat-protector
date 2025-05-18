FROM prom/alertmanager:v0.28.1

COPY ./metrics/alertmanager/config/alertmanager.yml /etc/alertmanager/alertmanager.yml
COPY ./docker/alertmanager/entrypoint.sh /entrypoint.sh

COPY --chmod=0755 ./docker/alertmanager/entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]