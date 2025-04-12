#!/bin/bash

for dir in *; do
  if [ -d "$dir" ]; then
    command="git worktree add "$dir" origin/feat/"$dir""
    echo ">> $command"
    $command
  fi
done
