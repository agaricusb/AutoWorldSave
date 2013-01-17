#!/bin/sh
javac -cp ../craftbukkit-1.4.6-R0.3.jar:PermissionsEx.jar:bpermissions.jar `find . -name '*.java'`
jar cfM AutoSaveWorld-nobypass.jar autosave plugin.yml
