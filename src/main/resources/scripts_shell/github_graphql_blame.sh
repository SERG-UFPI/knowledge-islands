#!/bin/bash

user=$1
repository=$2
branch=$3
file=$4
token=$5

script='query {
  repositoryOwner(login: \"'${user}'\") {
    repository(name: \"'${repository}'\") {
      object(expression: \"'${branch}'\") {
        ... on Commit {
          blame(path: \"'${file}'\") {
            ranges {
              startingLine
              endingLine
              age
              commit {
                oid
                author {
                  name
                  email
                }
                authoredDate
                committer{
                  name
                  email
                }
                committedDate
              }
            }
          }
        }
      }
    }
  }
}'

script="$(echo $script)"

curl -H 'Content-Type: application/json' \
   -H "Authorization: bearer ${token}" \
   -X POST -d "{ \"query\": \"$script\"}" https://api.github.com/graphql