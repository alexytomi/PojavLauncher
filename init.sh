#!/bin/bash

if [ "$1" = "force" ]; then
  for dir in *; do
    if [ -d "$dir" ]; then
      command="git worktree add -f "$dir" origin/feat/"$dir""
      echo ">> $command"
      $command
    fi
  done
else
  for dir in *; do
    if [ -d "$dir" ]; then
      command="git worktree add "$dir" origin/feat/"$dir""
      echo ">> $command"
      $command
    fi
  done
fi
