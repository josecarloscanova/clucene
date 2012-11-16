#!/bin/bash
BASEDIR=$(dirname $0)
CONF_DIR="configs"
DATA_DIR="dataset"
RESULT_DIR="results"
JAR_FILE="../target/queryStresser-1.0-jar-with-dependencies.jar"
NBQUERY=10

function createJSON {
    rm -f "$CONF_DIR"/*.json
    local MEANS=("0" "50000")
    local STDDEV=("10" "1000" "10000")

    LENMEANS=${#MEANS[@]}
    LENSTD=${#STDDEV[@]}
    for (( i=0; i<${LENMEANS}; i++ )); do
        for (( j=0; j<${LENSTD}; j++ )); do
            echo "{
    \"filename\": \"$DATA_DIR/normal${MEANS[$i]}_${STDDEV[$j]}\",
    \"nbOut\": $NBQUERY,
    \"queryFile\": \"querySample.txt\",
    \"nbQueries\": 100000,
    \"uniform\": false,
    \"gaussian\": {
        \"mean\": ${MEANS[$i]},
        \"stddev\": ${STDDEV[$j]}
    }
}" > "$CONF_DIR"/normal"${MEANS[$i]}"_"${STDDEV[$j]}".json
        done
    done

    # We do for uniform
    echo "{
    \"filename\": \"$DATA_DIR/uniform\",
    \"nbOut\": $NBQUERY,
    \"queryFile\": \"querySample.txt\",
    \"nbQueries\": 100000,
    \"uniform\": false,
    \"gaussian\": {
        \"mean\": 0,
        \"stddev\": 0
    }
}" > "$CONF_DIR"/uniform.json
}

function generateData {
    rm -f "$DATA_DIR"/*.csv
    for i in "$CONF_DIR"/*.json; do
        java -jar "$JAR_FILE" "$i" 
    done
}

function analyze {
    rm -f "$RESULT_DIR"/*
    for file in "$DATA_DIR"/*.csv; do 
        echo $file
        jmeter/bin/jmeter -n -t loadTest.jmx -Jfile.name=$(basename "$file") -Jpath.data=$(pwd)/"$DATA_DIR" -Jpath.result=$(pwd)/"$RESULT_DIR"
    done
}

createJSON
generateData
analyze
