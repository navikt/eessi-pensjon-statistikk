FROM ghcr.io/navikt/baseimages/temurin:21

COPY init-scripts/ep-jvm-tuning.sh /init-scripts/
RUN mkdir -p /secure-logs/
COPY build/libs/eessi-pensjon-statistikk.jar /app/app.jar
