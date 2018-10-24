#!/bin/bash
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"id":"66921076-ed1d-458b-9d7d-ce9a227d64a9", "title":"JavaScript: Alert", "code":"alert(\"Hello! I am an alert box!\");", "created":"2018-01-01", "modified":"2018-01-01"}' \
  http://localhost:8080/snippets
