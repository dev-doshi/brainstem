#!/bin/bash
# Compiles Brainstem directly against the (unobfuscated) 26.3 server jar - no Loom/remapping needed.
set -euo pipefail
cd "$(dirname "$0")"
SV=../server-263
CP=$(find $SV/libraries $SV/versions -name "*.jar" | tr '\n' ':')
CP="$CP$(ls $SV/.fabric/processedMods/*.jar | tr '\n' ':')../benchprobe/lib/mixinextras-fabric-0.5.5.jar"
VERSION=$(python3 -c 'import json;print(json.load(open("resources/fabric.mod.json"))["version"])')
rm -rf build && mkdir -p build/classes
javac --release 25 -nowarn -proc:none -Xlint:-options -cp "$CP" -d build/classes $(find src -name "*.java")
cp -R resources/. build/classes/
cp COPYING COPYING.LESSER build/classes/
(cd build/classes && jar cf ../brainstem-$VERSION.jar .)
echo "built build/brainstem-$VERSION.jar"
