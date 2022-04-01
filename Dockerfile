FROM navikt/java:17-appdynamics

COPY build/libs/eessi-pensjon-statistikk.jar /app/app.jar

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER statistikk
ENV APPD_ENABLED true
