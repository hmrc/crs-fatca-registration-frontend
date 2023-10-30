#!/bin/bash

echo ""
echo "Applying migration InformationSent"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /informationSent                       controllers.InformationSentController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "informationSent.title = informationSent" >> ../conf/messages.en
echo "informationSent.heading = informationSent" >> ../conf/messages.en

echo "Migration InformationSent completed"
