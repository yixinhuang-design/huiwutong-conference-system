@echo off
cd /d G:\huiwutong新版合集\backend\conference-backend
mvn -s maven-settings.xml -f conference-auth\pom.xml spring-boot:run
