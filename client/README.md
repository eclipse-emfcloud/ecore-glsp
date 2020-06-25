# theia-ecore
The example of how to build the Theia-based applications with the theia-ecore.

## Getting started

Install [nvm](https://github.com/creationix/nvm#install-script).

    curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.5/install.sh | bash

Install npm and node.

    nvm install 10
    nvm use 10

Install yarn.

    npm install -g yarn

## Remarks regarding glsp-server start up
You will need openjdk >= 11 on your path, as the client starts the ecore-glsp-server.

Before you start the browser or electron application you have to run the `mvn clean install` on the server application. This copies a build artifact into the build folder of theia-glsp-server.

## Running the browser example

    yarn rebuild:browser
    cd browser-app
    yarn start

Open http://localhost:3000 in the browser. You may use the example workspace in client/workspace to quickly get started.

## Running the Electron example

    yarn rebuild:electron
    cd electron-app
    yarn start

## Developing with the browser example

Start watching of theia-ecore.

    cd theia-ecore
    yarn watch

Start watching of the browser example.

    yarn rebuild:browser
    cd browser-app
    yarn watch

Launch `Start Browser Backend` configuration from VS code.

Open http://localhost:3000 in the browser.

## Developing with the Electron example

Start watching of theia-ecore.

    cd theia-ecore
    yarn watch

Start watching of the electron example.

    yarn rebuild:electron
    cd electron-app
    yarn watch

Launch `Start Electron Backend` configuration from VS code.

## Publishing theia-ecore

Create a npm user and login to the npm registry, [more on npm publishing](https://docs.npmjs.com/getting-started/publishing-npm-packages).

    npm login

Publish packages with lerna to update versions properly across local packages, [more on publishing with lerna](https://github.com/lerna/lerna#publish).

    npx lerna publish

## Running the testcafe tests

Build the Server

    mvn install -U

Start client, server and the tests. (Client and Server will shutdown after the execution of the tests).

    yarn e2etest

Start the tests if client and server are already running.

    yarn testcafe:start

Please keep the mouse focus on the Testcafe window during the tests. Losing focus will cause tests to fail.
