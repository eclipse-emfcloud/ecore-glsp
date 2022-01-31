FROM node:12.22.9-alpine3.15

RUN mkdir /usr/src/client -p

WORKDIR /usr/src/client

RUN apk add --update python3 && \
	apk add --update make && \
	apk add --update g++ && \
	apk add --update libsecret-dev && \
	apk add --update openjdk11-jre

# Have to copy everything because the build statement in theia-ecore starts linting, which requires all files.
# "build": "tsc && yarn run lint"
COPY . .

RUN yarn install

RUN yarn rebuild:browser

WORKDIR ./browser-app

EXPOSE 3000

CMD yarn start --hostname 0.0.0.0