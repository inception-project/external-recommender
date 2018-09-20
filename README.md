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

Please make sure to use the file ending `.properties` for the configuration file. If you run into RAM issues, assign a suited amount of RAM by providing additionally the `-Xmx=4g` flag right after the `-jar ` command for assinging more RAM to the Java Virtual Machine.

## Requests for training new models and requests for prediction
Once the server runs, requests are servered under `/train` for training and `/predict` for prediction, i.e.
```
# Train requests
http://yourIp:serverPort/train
# Prediction requests
http://yourIp:serverPort/predict
```

# Data format of train/predict requests
## Train request
The format of a train request is expected to be sent as json. The content consists of one or more UIMA CAS `documents` encoded as json array with a base 64 encoding. The `typeSystem` used by these CAS is provided as separate parameter. The `layer` parameter provides the full qualified name of the annotation in the CAS that provides the label information. The `feature` values carries the name of the field within this annotation that shall be used during the training process as label information.

```
{
	"layer":"name.of.annotation",
	"feature":"name.of.feature.in.annotation",
	"typeSystem":"WwgdmVyc", #Base 64 encoded
	"documents":["PD9...X]   #Base 64 encoded
}
```

# Prediction request
The prediction request uses an identical json file format as during the training process. The server sends a json array of one or more annotated CAS in XMI data format. The returned values are `not` base64 encoded anymore, i.e.

```
[ "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmi:XMI xmlns.....>",
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmi:XMI xmlns.....> ]
```
