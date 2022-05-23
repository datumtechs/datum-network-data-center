#!/usr/bin/env bash
#
# Description: Manage jar program
# Author: Clavin <leezj1989@163.com>
#

# Java ENV
export JAVA_HOME=${JAVA_HOME:-"/usr/lib/jvm/java-8-openjdk-amd64"}
export JRE_HOME=${JAVA_HOME}/jre

# Apps Info
APP_ENV=$1
APP_ACT=$2
JAVA_OPTS="-server"
STATUS_FILE="status.hook"

# Colors
RED='\E[1;31m'
GREEN='\E[1;32m'
RES='\E[0m'

# Get the absolute path where manage.sh exists
if [[ -L $0 ]]; then
    HOME_DIR=$(dirname $(readlink -f $0))
else
    HOME_DIR=$(dirname $0)
fi

function _echo_red() {
    echo -e "${RED}$1${RES}"
}

function _echo_green() {
    echo -e "${GREEN}$1${RES}"
}

# Shell Info
function _usage() {
    _echo_green "Usage: sh `basename ${0}` [APP_ENV] [start|stop|restart|status|debug]"
    exit 1
}

function _is_exist() {
    cd ${HOME_DIR} || _echo_red "Failed to change to ${HOME_DIR} directory"

    if [[ ! -f ${JRE_HOME}/bin/java ]]; then
        _echo_red "Java binary not found"
        exit 1
    fi

    APP_NAME=$(find . -maxdepth 1 -type f -regex ".*\.\(jar\)" | sed 's#./##g')
    if [[ -z ${APP_NAME} ]]; then
        _echo_red "Jar file not found"
        exit 1
    fi

    if [[ ! -f application-${APP_ENV}.yml ]]; then
        _echo_red "application-${APP_ENV}.yml not found"
        exit 1
    fi

    PID=$(ps -ef | grep ${APP_NAME} | grep ${APP_ENV} | grep -v $0 | grep -v grep | awk '{print $2}')
    if [[ -z "${PID}" ]]; then
        return 1
    else
        return 0
    fi
}

function start() {
    _is_exist

    if [[ $? -eq "0" ]]; then
        _echo_green "${APP_NAME} is already running, PID=${PID}"
    else
        read -s -p "Please input salt password: " salt
        export JASYPT_ENCRYPTOR_PASSWORD=${salt}

        nohup ${JRE_HOME}/bin/java ${JAVA_OPTS} -jar -Xmx4G -Xms4G -Xmn2G ${APP_NAME} --spring.profiles.active=${APP_ENV} > /dev/null 2>&1 &
        _echo_green "\n${APP_NAME} start success, PID=$!"

        unset JASYPT_ENCRYPTOR_PASSWORD
    fi
}

function stop() {
    _is_exist

    if [[ $? -eq "0" ]]; then
        if [[ ${APP_NAME} =~ browser-agent.*$ ]]; then
            sed -i 's/status=.*/status=SHUTDOWN/g' ${STATUS_FILE}
        else
            kill ${PID}
            _echo_green "${APP_NAME} process stop, PID=${PID}"
        fi
    else
        _echo_red "There is no process of ${APP_NAME}"
    fi
}

function restart() {
    stop
    sleep 2
    start
}

function status() {
    _is_exist

    if [[ $? -eq "0" ]]; then
        _echo_green "${APP_NAME} is running, PID=${PID}"
    else
        _echo_red "There is no process of ${APP_NAME}"
    fi
}

function debug() {
    _is_exist

    if [[ $? -eq "0" ]]; then
        _echo_green "${APP_NAME} is already running, PID=${PID}, Please kill ${APP_NAME} first to debug"
    else
        ${JRE_HOME}/bin/java ${JAVA_OPTS} -jar ${APP_NAME} --spring.profiles.active=${APP_ENV}
    fi
}

case ${APP_ACT} in
    "start")
        start
        ;;
    "stop")
        stop
        ;;
    "restart")
        restart
        ;;
    "status")
        status
        ;;
    "debug")
        debug
        ;;
    *)
        _usage
        ;;
esac
exit 0