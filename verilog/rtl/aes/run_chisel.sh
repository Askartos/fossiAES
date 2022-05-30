#!/bin/bash
if [ ! -d generated ]; then
  mkdir generated
fi

sbt 'runMain fossiAES.aesMain'

rm ./generated/*
mv *.v ./generated/
rm *.json
rm *.fir
