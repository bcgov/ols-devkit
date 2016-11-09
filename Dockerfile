FROM alpine:3.4
MAINTAINER leo.lou@gov.bc.ca

RUN apk update \
  && apk add alpine-sdk nodejs \
  && git config --global url.https://github.com/.insteadOf git://github.com/ \
  && npm install -g serve

RUN mkdir -p /app
  
WORKDIR /app
ADD . /app

RUN adduser -S appuser
RUN chown -R appuser:0 /app && chmod -R 770 /app
RUN apk del --purge alpine-sdk

USER appuser

EXPOSE 3000
CMD serve -C -D -J -S --compress .
