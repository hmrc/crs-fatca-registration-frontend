#!/bin/bash

echo ""
echo "Applying migration WhatIsYourName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /whatIsYourName                        controllers.WhatIsYourNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /whatIsYourName                        controllers.WhatIsYourNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeWhatIsYourName                  controllers.WhatIsYourNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeWhatIsYourName                  controllers.WhatIsYourNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatIsYourName.title = whatIsYourName" >> ../conf/messages.en
echo "whatIsYourName.heading = whatIsYourName" >> ../conf/messages.en
echo "whatIsYourName.firstName = firstName" >> ../conf/messages.en
echo "whatIsYourName.lastName = lastName" >> ../conf/messages.en
echo "whatIsYourName.checkYourAnswersLabel = WhatIsYourName" >> ../conf/messages.en
echo "whatIsYourName.error.firstName.required = Enter firstName" >> ../conf/messages.en
echo "whatIsYourName.error.lastName.required = Enter lastName" >> ../conf/messages.en
echo "whatIsYourName.error.firstName.length = firstName must be 35 characters or less" >> ../conf/messages.en
echo "whatIsYourName.error.lastName.length = lastName must be 3535 characters or less" >> ../conf/messages.en
echo "whatIsYourName.firstName.change.hidden = firstName" >> ../conf/messages.en
echo "whatIsYourName.lastName.change.hidden = lastName" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhatIsYourName: Arbitrary[WhatIsYourName] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        firstName <- arbitrary[String]";\
    print "        lastName <- arbitrary[String]";\
    print "      } yield WhatIsYourName(firstName, lastName)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Migration WhatIsYourName completed"
