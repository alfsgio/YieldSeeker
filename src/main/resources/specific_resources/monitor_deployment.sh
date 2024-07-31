#!/bin/bash

JAR_DIR="/home/ec2-user/jar"
JAR_FILE="YieldSeeker-1.0-SNAPSHOT.jar"
PID_FILE="/home/ec2-user/pid/yieldseeker.pid"

# Function to start the application
start_app() {
    echo "Starting application..."
    nohup java -jar "$JAR_DIR/$JAR_FILE" > /dev/null 2>&1 &
    echo $! > "$PID_FILE"
    echo "Application started with PID $(cat $PID_FILE)"
}

# Function to stop the application
stop_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null; then
            echo "Stopping application with PID $PID..."
            kill $PID
            rm -f "$PID_FILE"
            echo "Application stopped"
        fi
    fi
}

# Monitor the JAR directory for changes
inotifywait -m -e create,modify,move,delete "$JAR_DIR" | while read path action file; do
    if [ "$file" == "$JAR_FILE" ]; then
        echo "Detected $action on $JAR_FILE"
        stop_app
        start_app
    fi
done
