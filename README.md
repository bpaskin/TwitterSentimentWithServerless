### Using IBM Cloud Functions (Serverless) with CLoudant NoSQL DB and Watson Natural Language Understanding to interpret keywords from Twitter all for free!

### Will fix instructions soon - Need to add Prometheus / Grafana 
---

Signup for an IBM Cloud Account: https://cloud.ibm.com </br>
Signup for Twitter Developer account and get necessary Keys and Secrets: https://developer.twitter.com</br>

---

Install IBM Cloud CLI : https://cloud.ibm.com/docs/cli?topic=cli-install-ibmcloud-cli</br>
```
ibmcloud plugin install cloud-functions
ibmcloud plugin install watson
ibmcloud plugin install cloudant
```
---
```
ibmcloud login
ibmcloud target -g default
```
Create Cloudant DB
```
ibmcloud resource service-instance-create cloudant-serverless cloudantnosqldb lite us-south -p '{"legacyCredentials": false}'
ibmcloud resource service-key-create serverless-creds Manager --instance-name cloudant-serverles
ibmcloud resource service-key serverless-creds
```
Create NLU to be used
```
ibmcloud resource service-instance-create NLU natural-language-understanding free us-south -p '{"legacyCredentials": false}'
ibmcloud resource service-key-create nlu-creds Manager --instance-name NLU
ibmcloud resource service-key nlu-creds
```

Using the Cloudant Dashboard create a database called "twitter"

---

Update the configuration file src/main/java/configuration.json</br>
Compile code (mvn package)

---
```
ibmcloud fn namespace create default
ibmcloud fn package create Twitter
ibmcloud fn action create Twitter/ProcessTweets target/ServerlessTwitter-1.0.0-jar-with-dependencies.jar --main com.ibm.example.RetrieveTweets --web false -P src/main/java/configuration.json
ibmcloud fn trigger create Every5Minutes --feed /whisk.system/alarms/alarm --param cron "*/5 * * * *"
ibmcloud fn rule create Triggered Every5Minutes Twitter/ProcessTweets
```
---
Test:
```
ibmcloud fn action invoke Twitter/ProcessTweets --result
```

-+Funeral Winter+-
