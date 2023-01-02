### Using IBM Cloud Functions (Serverless) with Cloudant NoSQL DB and Watson Natural Language Understanding to interpret keywords from Twitter and store the sentiment - all for free!

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
ibmcloud fn action create Twitter/metrics target/ServerlessTwitter-1.0.0-jar-with-dependencies.jar --main com.ibm.example.MetricsData --web true -P src/main/java/configuration.json
```

The metrics to be used with Prometheus / Grafana can be found here:
```
ibmcloud fn action get Twitter/metrics --url
```

Different Cloudant [Views](https://cloud.ibm.com/docs/Cloudant?topic=Cloudant-creating-views-mapreduce) can be created to retrieve the specific data, like the counts for a specific term, and the [MetricData.java](https://github.com/bpaskin/TwitterSentimentWithServerless/blob/main/src/main/java/com/ibm/example/MetricsData.java) code can be updated to produce those results for Prometheus ingestion.

---

For Prometheus, the `prometheus.yml` needs to be updated and placed in the configuration path for it to be picked up.  in particular, the `metrics_path` should be the full context root of the command above to get the url.  My sample `prometheus.yml` is included.

Once Prometheus is setup, then Grafana can be pointed to the Prometheus that is running to gather stats and make dashboards.

If either or both are going to be run in contaienrs, I would suggest updating the time in the container to match the current time or the metrics could be off by days, weeks, etc.

![Grafana Image](https://github.com/bpaskin/TwitterSentimentWithServerless/blob/main/images/Grafana.png)

---
Test:
```
ibmcloud fn action invoke Twitter/ProcessTweets --result
```

-+Funeral Winter+-
