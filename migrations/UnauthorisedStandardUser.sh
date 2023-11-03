#!/bin/bash

echo ""
echo "Applying migration UnauthorisedStandardUser"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /unauthorisedStandardUser                       controllers.UnauthorisedStandardUserController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unauthorisedStandardUser.title = unauthorisedStandardUser" >> ../conf/messages.en
echo "unauthorisedStandardUser.heading = unauthorisedStandardUser" >> ../conf/messages.en

echo "Migration UnauthorisedStandardUser completed"
