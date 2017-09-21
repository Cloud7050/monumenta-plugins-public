#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/Monumenta"
ant pmd build jar
retcode=$?
if [[ $retcode -ne 0 ]]; then
	exit $retcode
fi

cd "$SCRIPT_DIR/BungeeCord"
ant pmd build jar
retcode=$?
if [[ $retcode -ne 0 ]]; then
	exit $retcode
fi
