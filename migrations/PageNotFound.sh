#!/bin/bash

echo ""
echo "Applying migration PageNotFound"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /pageNotFound                       controllers.PageNotFoundController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "pageNotFound.title = pageNotFound" >> ../conf/messages.en
echo "pageNotFound.heading = pageNotFound" >> ../conf/messages.en

echo "Migration PageNotFound completed"
