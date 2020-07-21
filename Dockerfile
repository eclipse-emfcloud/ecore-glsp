FROM maven:3.6.0-jdk-11-slim AS server-builder

COPY /server/org.eclipse.emfcloud.ecore.glsp/src /usr/src/server/org.eclipse.emfcloud.ecore.glsp/src

COPY /server/org.eclipse.emfcloud.ecore.glsp/pom.xml /usr/src/server/org.eclipse.emfcloud.ecore.glsp/pom.xml

WORKDIR /usr/src/server/org.eclipse.emfcloud.ecore.glsp/

RUN mvn -f pom.xml clean package


FROM node:10.18.0-alpine3.10 AS client-builder

RUN mkdir /usr/src/client -p

WORKDIR /usr/src/client

RUN apk add --update python && \ 
	apk add --update make && \
	apk add --update g++ && \ 
	apk add --update openjdk11-jre

# Have to copy everything because the build statement in theia-ecore starts linting, which requires all files.
# "build": "tsc && yarn run lint"
COPY ./client .

RUN yarn install

RUN yarn rebuild:browser


FROM node:10.18.0-alpine3.10

RUN apk add --update openjdk11-jre

COPY --from=client-builder usr/src/client/browser-app ./browser-app

COPY --from=client-builder usr/src/client/node_modules ./node_modules

COPY --from=client-builder usr/src/client/theia-ecore ./theia-ecore

COPY --from=client-builder usr/src/client/workspace ./workspace

WORKDIR ./browser-app

EXPOSE 3000

CMD yarn start --hostname 0.0.0.0