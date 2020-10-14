#!/bin/bash

echo "$(date +"[%T.%3N]") Evaluate Options... "
buildBackend='false'
copyBackend='false'
buildFrontend='false'
forceFrontend='false'

if [[ $1 == "-h" ]]; then
  printf "Usage: build.sh [-h] [-b] [-c] [-ff] [-f]\n\n"
  echo "Options:"
  echo "  -b   Build Backend"
  echo "  -c   Copy Backend"
  echo "  -ff  Remove yarn.lock"
  echo "  -f   Build Frontend"
  exit 0
fi

if [[ "$1" == "" ]]; then
  buildBackend='true'
  copyBackend='true'
  buildFrontend='true'
fi

if [[ ${#1} -gt 2 ]]; then
  if [[ "$1" == -*"b"* ]]; then
    buildBackend='true'
  fi
  if [[ "$1" == -*"c"* ]]; then
    copyBackend='true'
  fi
  if [[ "$1" == -*"f"* ]]; then
    buildFrontend='true'
  fi
  if [[ "$1" == -*"ff"* ]]; then
    forceFrontend='true'
  fi
fi

while [ "$1" != "" ]; do
  case $1 in
    -b | --backend )  buildBackend='true'
                      ;;
    -c | --copy )     copyBackend='true'      
                      ;;
    -f | --frontend ) buildFrontend='true'
                      ;;
    -ff | --forcefrontend ) forceFrontend='true'
                      ;;
  esac
  shift
done

[[ "$buildBackend" == "true" ]] && echo "  Build Backend (-b)"
[[ "$copyBackend" == "true" ]] && echo "  Copy Backend (-c)"
[[ "$forceFrontend" == "true" ]] && echo "  Remove yarn.lock (-ff)"
[[ "$buildFrontend" == "true" ]] && echo "  Build Frontend (-f)"

if [ "$buildBackend" == "true" ]; then
  echo "$(date +"[%T.%3N]") Build backend products"
  cd server/
  mvn clean install -Pfatjar -U
  cd ../
fi

if [ "$copyBackend" == "true" ]; then
  productPath=''
  if [[ "$OSTYPE" == "linux-gnu" ]]; then
	productPath='linux/gtk'
	echo "Running on Linux"
  elif [[ "$OSTYPE" == "darwin"* ]]; then
        # Mac OSX
	productPath='macosx\cocoa'
	echo "Running on Mac"
  elif [[ "$OSTYPE" == "cygwin" ]]; then
        # POSIX compatibility layer and Linux environment emulation for Windows
	productPath='win32\win32'
	echo "Running on Windows with Cygwin"
  elif [[ "$OSTYPE" == "msys" ]]; then
        # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
	productPath='win32\win32'
	echo "Running on Windows with Msys"
  fi
  echo "$productPath"
  echo "$(date +"[%T.%3N]") Copy built products..."

  inputGLSPServer=server/org.eclipse.emfcloud.ecore.glsp/target/org.eclipse.emfcloud.ecore.glsp-0.0.2-SNAPSHOT-glsp.jar
  outputGLSPServer=client/ecore-server/build
  echo "  $(date +"[%T.%3N]") Copy EcoreGLSPServer to '$outputGLSPServer'."
  rm -rf $outputGLSPServer && mkdir -p $outputGLSPServer && cp -rf $inputGLSPServer $outputGLSPServer

  inputEcoreMS=server/org.eclipse.emfcloud.ecore.modelserver/target/org.eclipse.emfcloud.ecore.modelserver-0.0.1-SNAPSHOT-standalone.jar
  outputEcoreMS=client/ecore-server/build
  echo "  $(date +"[%T.%3N]") Copy EcoreModelServer to '$outputEcoreMS'."
  cp -rf $inputEcoreMS $outputEcoreMS


  echo "$(date +"[%T.%3N]") Copy finished."
fi

if [ "$forceFrontend" == "true" ]; then
  cd client/
  rm -f ./yarn.lock
  cd ..
fi

if [ "$buildFrontend" == "true" ]; then
  cd client/
  yarn
  cd ..
fi


