#!/bin/bash

echo ""
echo "Applying migration InformationMissing"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /informationMissing                       controllers.InformationMissingController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "informationMissing.title = informationMissing" >> ../conf/messages.en
echo "informationMissing.heading = informationMissing" >> ../conf/messages.en

echo "Migration InformationMissing completed"
