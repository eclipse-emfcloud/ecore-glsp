# ecore-glsp
[![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Femfcloud%2Fjob%2Feclipse-emfcloud%2Fjob%2Fecore-glsp%2Fjob%2Fmaster)](https://ci.eclipse.org/emfcloud/job/eclipse-emfcloud/job/ecore-glsp/job/master)

For more information, please visit the [EMF.cloud Website](https://www.eclipse.org/emfcloud/). If you have questions, contact us on our [spectrum chat](https://spectrum.chat/emfcloud/) and have a look at our [communication and support options](https://www.eclipse.org/emfcloud/contact/).

Ecore GLSP provides a web-based editor for Ecore Models (including Diagrams), integrated with Eclipse Theia. It contains two components: one [GLSP](https://github.com/eclipse-glsp/glsp) language server (Server-side, written in Java), and one GLSP client extension to actually present the diagrams (Using [Sprotty](https://github.com/eclipse/sprotty-theia)). 

Ecore GLSP can display an existing Ecore model. The diagram layout will be persisted in an .enotation file next to the .ecore file. The diagram editor also supports creation of new elements (EClasses, EAttributes, EReferences...), as well as partial support for editing existing elements (Renaming, deleting...).

![Ecore GLSP Example](images/diagramanimated.gif)

## Prerequisites

### Java
You need Java 11 to build the ecore-glsp editor.

## Getting started

Clone the ecore-glsp editor:

    git clone https://github.com/eclipse-emfcloud/ecore-glsp.git

Build server and client with the following build-script. This script also copies the needed server artifacts to the client.

    cd ecore-glsp
    ./build.sh


Run
  * Start the client as it is described in the [client README](client/README.md).
  * The copied backend artifacts are automatically launched on startup.
  * If you want to start the backends manually:
    * Execute the Java main classes: `org.eclipse.emfcloud.ecore.glsp.EcoreServerLauncher` and/or `org.eclipse.emfcloud.ecore.modelserver.EcoreModelServerLauncher`
    * For this to work with the client, you have to set the regarding `isRunning` flag of the LaunchOptions to `true`. The LaunchOptions for both server products are located in `client/ecore-server/src/node/backend-extension.ts`.


## Building and deploying via Docker
The client repo contains a [Dockerfile](client/README.md), that builds the entire client application. The image listens on 0.0.0.0:3000 for incoming requests from a browser.

For installing docker locally please consult [docker's installation description](https://docs.docker.com/install/) for your OS.

**The glsp-server needs to be build locally before you build the image**

**Building**
`docker build -t <imagename>:<tagname> .` 

**Running**
`docker run -it -p 3000:3000 --rm <imagename>:<tagname>`

After that you should be able to connect with your browser at localhost:3000.	
