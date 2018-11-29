# inception-recommender
External recommender implemented with DKPro Text Classification.

# Setup & Run
## How to build the external recommender project
Change directory into the `recommender-server` subproject. The project is build via Maven3 by using the following command:
```
mvn clean install
```
You should find in the target folder a .jar file named `recommender-server.jar`.

## Running the recommender-server.jar
The server requires three parameters to run, which should be placed in a file that is provided at start up.
```
logging.file=logfile.txt
repositoryRoot=modelRoot
server.port=30500
```

`repositoryRoot` is the path to the folder in which the models will be stored. The folder will be created if it does not exist yet.
`server.port` is the port on which the server listens for requests.

This file is provided as parameter when the sever is started:

```
java 
    -jar target/recommender-server.jar 
    --spring.config.location=/path/to/file/with/parameters.properties
```

Please make sure to use the *file ending* `.properties` for the configuration file. If you run into RAM issues, assign a suited amount of RAM by providing additionally the `-Xmx=4g` flag right after the `-jar ` command for assinging more RAM to the Java Virtual Machine.

## Requests for training new models and requests for prediction
Once the server runs, requests are served under `/train` for training and `/predict` for prediction, i.e.
```
# Train requests
http://yourIp:serverPort/train
# Prediction requests
http://yourIp:serverPort/predict
```

# Data format of train/predict requests

The data format for training and prediction requests is described in the [INCEpTION developer documentation](https://zoidberg.ukp.informatik.tu-darmstadt.de/jenkins/job/INCEpTION%20(GitHub)%20(master)/de.tudarmstadt.ukp.inception.app$inception-app-webapp/doclinks/3/#_external_recommender_api_overview) .