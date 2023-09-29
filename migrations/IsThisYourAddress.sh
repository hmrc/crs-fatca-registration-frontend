#!/bin/bash

echo ""
echo "Applying migration IsThisYourAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /isThisYourAddress                        controllers.IsThisYourAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /isThisYourAddress                        controllers.IsThisYourAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIsThisYourAddress                  controllers.IsThisYourAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIsThisYourAddress                  controllers.IsThisYourAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isThisYourAddress.title = isThisYourAddress" >> ../conf/messages.en
echo "isThisYourAddress.heading = isThisYourAddress" >> ../conf/messages.en
echo "isThisYourAddress.checkYourAnswersLabel = isThisYourAddress" >> ../conf/messages.en
echo "isThisYourAddress.error.required = Select yes if isThisYourAddress" >> ../conf/messages.en
echo "isThisYourAddress.change.hidden = IsThisYourAddress" >> ../conf/messages.en

echo "Migration IsThisYourAddress completed"
