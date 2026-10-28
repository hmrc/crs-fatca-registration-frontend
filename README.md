
# crs-fatca-registration-frontend

This service allows a user to register for the CRS/FATCA service

### Running the service

Service manager: CRS_FATCA_ALL

Port: 10030

Link: http://localhost:10030/register-for-crs-and-fatca

---
### Dependencies

| Service                                              | Link                                                                   |
|------------------------------------------------------|------------------------------------------------------------------------|
| Auth                                                 | https://github.com/hmrc/auth                                           |
| CRS & FATCA                                          | https://github.com/hmrc/crs-fatca-registration                         |
| Tax enrolments                                       | https://github.com/hmrc/tax-enrolments                                 |
| Enrolment Store Proxy                                | https://github.com/hmrc/enrolment-store-proxy                          |
| Email                                                | https://github.com/hmrc/email                                          |
| Address Lookup                                       | https://github.com/hmrc/address-looup                                  |
| User details                                         | https://github.com/hmrc/user-details                                   |
| Identity verification                                | https://github.com/hmrc/identity-verification                          |                               
---


## Running Tests
Run unit tests:
```
sbt test
```

Run integration tests:
```
sbt it/test
```

---
### Prototype and tests

The prototype for this service can be viewed [here](https://crs-fatca-prototype-019ff249bf18.herokuapp.com)

The journey tests can be found [here](https://github.com/hmrc/crs-fatca-registration-ui-tests)


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").