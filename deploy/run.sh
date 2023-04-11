#!/usr/bin/env bash

#generate memory options for JVM according to the memory size of current system
#-------------Using G1 GC
#1 G Xms=512m MaxMetaspaceSize=64m ReservedCodeCacheSize=32m InitialCodeCacheSize=32m
#2 G Xms=1g MaxMetaspaceSize=128m ReservedCodeCacheSize=64m InitialCodeCacheSize=64m
#4 G Xms=2g MaxMetaspaceSize=256m ReservedCodeCacheSize=64m InitialCodeCacheSize=64m
#8 G Xms=4g MaxMetaspaceSize=256m ReservedCodeCacheSize=128m InitialCodeCacheSize=128m
#16G Xms=12g MaxMetaspaceSize=512m ReservedCodeCacheSize=256m InitialCodeCacheSize=256m
#22-28G Xms=18g MaxMetaspaceSize=512m ReservedCodeCacheSize=256m InitialCodeCacheSize=256m
#32-44G Xms=24g MaxMetaspaceSize=1g ReservedCodeCacheSize=512m InitialCodeCacheSize=512m
#64-88G Xms=32g MaxMetaspaceSize=2g ReservedCodeCacheSize=1g InitialCodeCacheSize=1g


function init() {
    APP_KEY="com.sankuai.avatar.web"
    DEPLOY_ARTIFACT_ID="avatar-web"

    if [ -z "$LOG_PATH" ]; then
        LOG_PATH="/opt/logs/$APP_KEY"
    fi
    mkdir -p $LOG_PATH

    if [ -z "$WORK_PATH" ]; then
        WORK_PATH="/opt/meituan/$APP_KEY"
    fi

    if [ -z "$DEBUG_PORT" ]; then
        DEBUG_PORT=44399
    fi

    REMOTE_DEBUG=`remoteDebug`

    JAVA_CMD="java8"
    if ! command -v $JAVA_CMD >/dev/null 2>&1; then
        JAVA_CMD="/usr/local/$JAVA_CMD/bin/java"
    fi

    JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"

    if [ -z "$JVM_GC" ]; then
        JVM_GC="-XX:+UseG1GC -XX:G1HeapRegionSize=4M -XX:InitiatingHeapOccupancyPercent=40 -XX:MaxGCPauseMillis=100 -XX:+TieredCompilation -XX:CICompilerCount=4 -XX:-UseBiasedLocking -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintStringTableStatistics -XX:+PrintAdaptiveSizePolicy -XX:+PrintGCApplicationStoppedTime -XX:+PrintFlagsFinal -XX:-UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M"
    fi

    if [ -z "$JVM_HEAP" ]; then
        JVM_HEAP=`getJVMMemSizeOpt`
    fi
}

function run() {
    EXEC="exec"
    CONTEXT=/
    EXEC_JAVA="$EXEC $JAVA_CMD $JVM_ARGS $JVM_HEAP $JVM_GC $REMOTE_DEBUG"
	EXEC_JAVA=$EXEC_JAVA" -Xloggc:$LOG_PATH/gc.log -XX:ErrorFile=$LOG_PATH/vmerr.log -XX:HeapDumpPath=$LOG_PATH"

    if [ "$UID" = "0" ]; then
        ulimit -n 1024000
        umask 000
    else
        echo $EXEC_JAVA
    fi
    cd $WORK_PATH
    pwd

    #QA扫描需求begin
    UNZIP_ENVS="dev test"
    ENV=`getEnv`
    for UNZIP_ENV in $UNZIP_ENVS
    do
        if [ "$ENV" == "$UNZIP_ENV" ]; then
            unzip -oq  $DEPLOY_ARTIFACT_ID.jar    2>&1
            break
        fi
    done
    #QA扫描需求end

    $EXEC_JAVA -jar $DEPLOY_ARTIFACT_ID.jar 2>&1
}

function getEnv(){
    FILE_NAME="/data/webapps/appenv"
    PROP_KEY="env"
    PROP_VALUE=""
    if [[ -f "$FILE_NAME" ]]; then
        PROP_VALUE=`cat ${FILE_NAME} | grep -w ${PROP_KEY} | cut -d'=' -f2`
    fi
    echo $PROP_VALUE
}

function remoteDebug(){
    ENV=`getEnv`
    DEBUG_CMD=""
    DEBUG_ENVS="dev test staging"
    STG_ENV="staging"
    #QA要求在线下环境提供覆盖率扫描功能参数
    for DEBUG_ENV in $DEBUG_ENVS
    do
        if [ "$ENV" == "$DEBUG_ENV" ]; then
            if [ "$ENV" == "$STG_ENV" ]; then
                DEBUG_CMD="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$DEBUG_PORT"
            else
                DEBUG_CMD="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$DEBUG_PORT -javaagent:/opt/meituan/qa_test/jacocoagent.jar=output=tcpserver,port=6300,address=*,excludes=com.dianping.*"
            fi
            break
        fi
    done
    echo $DEBUG_CMD
}


function getTotalMemSizeMb() {
	memsizeKb=`cat /proc/meminfo|grep MemTotal|awk '{print $2}'`
    if [ -z "$memsizeKb" ]; then
        memsizeKb=8*1000*1000
    fi
	memsizeMb=$(( $memsizeKb/1024 ))
	echo $memsizeMb
}

function outputJvmArgs() {
	jvmSize=$1
	MaxMetaspaceSize=$2
	ReservedCodeCacheSize=$3
	InitialCodeCacheSize=$4
	echo "-Xss512k -Xmx"$jvmSize" -Xms"$jvmSize" -XX:MetaspaceSize="$MaxMetaspaceSize" -XX:MaxMetaspaceSize="$MaxMetaspaceSize" -XX:+AlwaysPreTouch -XX:ReservedCodeCacheSize="$ReservedCodeCacheSize" -XX:InitialCodeCacheSize="$InitialCodeCacheSize" -XX:+HeapDumpOnOutOfMemoryError "
}


function getJVMMemSizeOpt() {
	memsizeMb=`getTotalMemSizeMb`

	#公司的机器内存比实际标的数字要小，比如8G实际是7900M左右，一般误差小于1G
	let maxSize_lvl1=63*1024
	let maxSize_lvl2=31*1024
	let maxSize_lvl3=21*1024
	let maxSize_lvl4=15*1024
	let maxSize_lvl5=7*1024
	let maxSize_lvl6=3*1024
	let maxSize_lvl7=1*1024

	if [[ $memsizeMb -gt $maxSize_lvl1 ]]
	then
		jvmSize="32g"
		MaxMetaspaceSize="2g"
		ReservedCodeCacheSize="1g"
		InitialCodeCacheSize="1g"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl2 && $memsizeMb -lt $maxSize_lvl1 ]]
	then
		jvmSize="24g"
		MaxMetaspaceSize="1g"
		ReservedCodeCacheSize="512m"
		InitialCodeCacheSize="512m"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl3 && $memsizeMb -lt $maxSize_lvl2 ]]
	then
		jvmSize="18g"
		MaxMetaspaceSize="512m"
		ReservedCodeCacheSize="256m"
		InitialCodeCacheSize="256m"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl4 && $memsizeMb -lt $maxSize_lvl3 ]]
	then
		jvmSize="12g"
		MaxMetaspaceSize="512m"
		ReservedCodeCacheSize="256m"
		InitialCodeCacheSize="256m"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl5 && $memsizeMb -lt $maxSize_lvl4 ]]
	then
		jvmSize="4g"
		MaxMetaspaceSize="256m"
		ReservedCodeCacheSize="128m"
		InitialCodeCacheSize="128m"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl6 && $memsizeMb -lt $maxSize_lvl5 ]]
	then
		jvmSize="2g"
		MaxMetaspaceSize="256m"
		ReservedCodeCacheSize="64m"
		InitialCodeCacheSize="64m"
	fi

	if [[ $memsizeMb -gt $maxSize_lvl7 && $memsizeMb -lt $maxSize_lvl6 ]]
	then
		jvmSize="1g"
		MaxMetaspaceSize="128m"
		ReservedCodeCacheSize="64m"
		InitialCodeCacheSize="64m"
	fi

	if [[ $memsizeMb -lt $maxSize_lvl7 && $memsizeMb > 512 ]]
	then
		jvmSize="512m"
		MaxMetaspaceSize="64m"
		ReservedCodeCacheSize="32m"
		InitialCodeCacheSize="32m"
	fi

	if [ $memsizeMb -lt 512 ]; then
		exit 1
	fi

	outputJvmArgs $jvmSize $MaxMetaspaceSize $ReservedCodeCacheSize $InitialCodeCacheSize
	exit 0
}

# ------------------------------------
# actually work
# ------------------------------------
init
run
