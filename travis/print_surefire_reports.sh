#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== SUREFIRE REPORTS ===\n"

recurse() {
    for i in "$1"/*; do
        if [ -d "$i" ]; then
            print_report "$i"
            recurse "$i"
        fi
    done
}

print_report() {
    if [ -d "$1/target/surefire-reports" ]; then
        for F in $1/target/surefire-reports/*.txt; do
            echo $F
            cat $F
            echo
        done
    fi
}

recurse .
