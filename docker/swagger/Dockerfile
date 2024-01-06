FROM swaggerapi/swagger-ui

USER root
RUN apk --no-cache add nodejs npm
RUN npm install -g swagger-combine
COPY ./swagger.json swagger.json

EXPOSE 8080