#!/bin/sh

# Set this to the directory where you installed the user-service-executable.jar.
# You should have write access to this directory.

PREFIX=$HOME/abc4trust

# Set to 1 to enable logging.

LOGGING=0

# You should not modify anything below this line.

JAR=${PREFIX}/user-service-executable.jar
JETTY_ARGS="-Dconfig=${CONFIG}"
GRACE=10 # 10 secs
LOGS=${PREFIX}/log
PIDFILE=${PREFIX}/user-service.pid

CMD=$1

if [[ -z ${CMD} ]]; then
  java ${JETTY_ARGS} -jar ${JAR} usage
  exit 1
fi

if [[ ! -d ${PREFIX} ]]; then
  echo "Directory ${PREFIX} does not exist."
  exit 1
fi

if [[ ! -f ${JAR} ]]; then
  echo "${JAR} does not exist."
  exit 1
fi

if [[ ${LOGGING} != '0' ]]; then
  mkdir -p ${LOGS}
  STDOUT=${LOGS}/stdout.log
  STDERR=${LOGS}/stderr.log
else
  STDOUT=/dev/null
  STDERR=/dev/null
fi

cd ${PREFIX}

if [[ ${CMD} == 'start' ]]; then

    if [[ -f ${PIDFILE} ]]; then
        echo "ABC4Trust User Service is already running."
        exit 1
    fi

    echo "Starting ABC4Trust User Service..."
    java ${JETTY_ARGS} -jar ${JAR} 1>${STDOUT} 2>${STDERR} &
    PID=$!
    echo "$PID" > ${PIDFILE}
    echo "Started ABC4Trust User Service with pid: ${PID}"

elif [[ ${CMD} == 'stop' ]]; then
    echo "Stopping ABC4Trust User Service..."
    # Try gracefully first
    java ${JETTY_ARGS} -jar ${JAR} stop
    sleep ${GRACE}
    if [[ -f ${PIDFILE} ]]; then
        PID=`cat ${PIDFILE}`
        test -z $PID || kill $PID
        rm ${PIDFILE}
        sleep 1
        echo "Stopped ABC4Trust User Service with pid: ${PID}"
    fi
else # Just let the other cmds through...
    java ${JETTY_ARGS} -jar ${JAR} ${CMD}
fi

exit 0
