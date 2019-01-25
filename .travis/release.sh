#!/usr/bin/env bash

echo "[Release] Cleaning repo"
mvn $MAVEN_CLI_OPTS release:clean

echo "[Release] Preparing release"
mvn $MAVEN_CLI_OPTS release:prepare -DautoVersionSubmodules=true -DtagNameFormat="@{project.version}" -DscmCommentPrefix="[maven-release-plugin] [ci skip] " -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

echo "[Release] Executing release"
mvn $MAVEN_CLI_OPTS release:perform
