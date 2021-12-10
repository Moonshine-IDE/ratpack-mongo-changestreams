# ratpack-mongo-changestreams basic demo app
Basic HTTP reactive application that uses Ratpack, MongoStreams and Server-sent events to send notifications to the front-end when a Document is updated in the MongoDB Collection.

# Requirements
- MongoDB  
- Java 8+  
- [Optional] Moonshine-IDE


# 1. Database server setup
## Option 1: VirtualBox Vagrant setup
- Change directory to the base path of this project.
- Open the file `Vagrantfile` and edit the `GUEST_IP` value, use an available local network IP, this is the IP address that will be asigned to the virtual machine.
- Open the file `build.gradle` and edit the `mongo.uri` variable with the local IP address used in the previous step.
- Execute the `vagrant up` command, once the process completes an Ubuntu-MongoDB virtual machine will be running.
- [Continue to step 2](#2-run-the-project).

## Option 2: Mongo DB local installation
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
Import the [restaurants](https://raw.githubusercontent.com/mongodb/docs-assets/drivers/restaurants.json) collection into the test database as shown in this readme file:
https://github.com/mongodb/docs-assets/tree/drivers

# 2. Run the project
Once the mongodb server is running you can continue and run this project
## Using the Gradle wrapper
### Ubuntu  
`./gradlew run`

### Windows  
`gradlew.bat run`

## From Moonshine IDE

Open the project in Moonshine with File > Open/Import Project or by double-clicking on ratpack-push.javaproj.
Project > Run Gradle Command. This will run the default command gradle clean runApp.


# 3. Update the collection using mongosh
Any modification/insertion into the restaurants collection will trigger a server-sent event in the Ratpack application. 

Connect to mongosh \
`mongosh mongodb://<MONGODB_SERVER_IP>:27017/test`

Then within mongosh
To update a single document:  
`db.restaurants.updateOne( { name: "XYZ Coffee Bar" }, { $set: { "stars": 5 } })`

To update multiple documents:  
```
try {
   db.restaurants.updateMany(
      { stars: { $eq: 4 } },
      { $set: { "stars" : 1 } }
   );
} catch (e) {
   print(e);
}
```

# References
- https://www.mongodb.com/blog/post/an-introduction-to-change-streams
