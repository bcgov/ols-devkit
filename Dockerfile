FROM alpine:3.4
MAINTAINER leo.lou@gov.bc.ca

RUN apk update \
  && apk add nodejs \
  && npm install -g serve

RUN mkdir -p /app
  
WORKDIR /app
ADD . /app

RUN adduser -S app
RUN chown -R appr:0 /app && chmod -R 770 /app
USER app

EXPOSE 5000
CMD serve -C -S /app
