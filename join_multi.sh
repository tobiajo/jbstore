#!/usr/bin/env bash
# TODO: update for new nodes

a="127.0.0.1"
c="127.0.0.1"
d="34567"

for b in `seq 45678 45683`; do
    source join.sh $a $b $c $d &
done
