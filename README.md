# ratpack-mongo-changestreams basic demo app
Basic HTTP reactive application that uses Ratpack, MongoStreams and Server-sent events to send notifications to the front-end when a Document is updated in the MongoDB Collection.

We use a DevExtreme DataGrid component to provide a simple HTMl page to show and edit the data, this DataGrid uses a custom DataSource that fetches data from a Back-end
endpoint that implements the required logic to query data from a MongoDB collection, parametrized by a DevExtreme [loadOptions](https://js.devexpress.com/Documentation/ApiReference/Data_Layer/CustomStore/LoadOptions/) object.

# Requirements
- MongoDB  
- Java 8+  
- [Optional] Moonshine-IDE


# 1. Database server setup
## Option 1: VirtualBox Vagrant setup
- [Download](https://www.virtualbox.org/wiki/Downloads) and install VirtualBox.
- [Download](https://www.vagrantup.com/downloads) and install Vagrant.
- Open a command line and change directory to the base path of this project.
- Execute the `vagrant up` command, once the process completes an Ubuntu-MongoDB virtual machine will be running.
- [Continue to step 2](#2-run-the-project).

## Option 2: Mongo DB local installation
<details>
<summary>Click to expand!</summary>

### Install MongoDB
#### Ubuntu
https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/.

#### Windows
https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/.

[install mongosh](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/#install-mongosh) as a separate package. \
[Install MongoDB tools](https://docs.mongodb.com/database-tools/installation/installation-windows/) (mongoimport).

Add the path to mongoDB files to windows PATH
In my case it was \
`C:\Program Files\MongoDB\Server\5.0\bin\` \
`C:\Program Files\MongoDB\Tools\100\bin\`

It wasn't required to add mongosh app to the windows PATH variable, but it was installed on this directory: \
`C:\Users\HP\AppData\Local\Programs\mongosh`

### Start mongoDB as a replica
Stop any mongodb server instance.

### Create data directory and Start a replica instance in the default port 27017  
Run the following commands to set up a simple, single-node replica set (for testing purposes).
#### Windows  
```
mkdir C:\mongodb\data
mongod --replSet rs0 --dbpath "C:\mongodb\data"
```

#### Ubuntu  
```
mkdir -p /mongodb/data
mongod --replSet rs0 --dbpath /mongodb/data
```

### Connect (in a different terminal) using mongosh and initialize the replica 
`mongosh mongodb://<MONGODB_SERVER_IP>:27017/test` \
`rs.initiate()`

## Import test collection
Import the grades collection from the file `vagrant/grades.json` into the test database like this:

`mongoimport --db test --collection grades --drop --file grades.json`
</details>

# 2. Run the project
Once the mongodb server is running you can continue and run this project

### Using the Gradle wrapper
#### Ubuntu  
`./gradlew run`

#### Windows
`gradlew.bat run`

### From Moonshine IDE

Open the project in Moonshine with `File > Open/Import Project` or by double-clicking on ratpack-push.javaproj.
`Project > Run Gradle Command`. This will run the default command gradle clean runApp.

### The following endpoints are available for testing:

1. **GET /api/grades**: Receives a DevExtreme [loadOptions](https://js.devexpress.com/Documentation/ApiReference/Data_Layer/CustomStore/LoadOptions/) object as a request parameter. Can be used to test DevExtreme components.

2. **(GET, POST, PUT, DELETE) /api/grades**: this endpoint exposes a basic CRUD functionality.

3. **GET /grades/stream**: This endpoint uses mongo Change Streams to respond with a Server-sent event when any operation (insert, update, delete) is performed on the Grades collection (similar to what we have in the Ratpack back-end demo app).

4. **GET /frontend**: This is an endpoint for testing the back-end endpoints. It responds with an HTML page that displays a list of Grades and dynamically updates the table when a notification (Server-sent event) is received.

# 3. Update the collection using mongosh
Any modification/insertion into the restaurants collection will trigger a server-sent event in the Ratpack application. 

Connect to mongosh \
`mongosh mongodb://<MONGODB_SERVER_IP>:27017/test`

Then within mongosh
To update a single document:  
```
db.grades.updateOne(
   { quizScore: { $gte: 90 } },
   [{ $set: { examScore: { $round: [ { $multiply: [ { $rand: {} }, 100 ] }, 2 ] } } }]
);
```

To update multiple documents:  
```
try {
   db.grades.updateMany(
      { examScore: { $lte: 25 } },
      [{ $set: { examScore: { $round: [ { $multiply: [ { $rand: {} }, 100 ] }, 2 ] } } }],
   );
} catch (e) {
   print(e);
}
```

#### References
- https://www.mongodb.com/blog/post/an-introduction-to-change-streams
