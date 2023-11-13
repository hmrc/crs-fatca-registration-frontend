#!/bin/bash

echo ""
echo "Applying migration SessionExpired"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /sessionExpired                       controllers.SessionExpiredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sessionExpired.title = sessionExpired" >> ../conf/messages.en
echo "sessionExpired.heading = sessionExpired" >> ../conf/messages.en

echo "Migration SessionExpired completed"
