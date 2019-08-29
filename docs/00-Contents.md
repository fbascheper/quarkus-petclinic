## Achieving supersonic development speed with Quarkus
> The holy grail of Developer productivity?

***

> Early this year RedHat has entered the arena where Spring Boot and Micronaut were already competing, to become
> the favorite full stack framework for building microservice and serverless applications. What exactly 
> sets Quarkus apart from its competitors? 
>           
> This article will teach you the basics for building applications with Quarkus by converting code from 
> the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) to build a cloud native Quarkus 
> application using 'the best of breed Java libraries and standards', such as Hibernate Panache, RestEasy and GraalVM. 

*** 

1. [What is Quarkus?](#what-is-quarkus)
2. [Testing applications with Quarkus](#testing-applications-with-quarkus)
3. [Getting started with Quarkus](#getting-started-with-quarkus)
4. [Enough of this, let's start coding!](#enough-of-this-lets-start-coding)
   - [Preparation](#preparation)
5. [Writing the PetClinic 'veterinarians' service in Quarkus](#writing-the-petclinic-veterinarians-service-in-quarkus)
   - [Step 1. Create a Quarkus skeleton application](#step-1-create-a-quarkus-skeleton-application)
   - [Step 2. Implement the domain model](#step-2-implement-the-domain-model)
   - [Step 3. Configuring the databases in Quarkus](#step-3-configuring-the-databases-in-quarkus)
   - [Step 4. Implement the VetResource class](#step-4-implement-the-vetresource-class)
   - [Step 5. Reducing clutter with Panache entities](#step-5-reducing-clutter-with-panache-entities)
6. [Testing your application with Quarkus](#testing-your-application-with-quarkus)
7. [Going native with Quarkus](#going-native-with-quarkus)
8. [Wrap up](#wrap-up)

*** 

### What is Quarkus?

*Container-first and cloud-native*

Quarkus is a Kubernetes native Java framework, designed to make Java a leading platform in the new world of 
serverless, microservices, containers and the cloud. By leveraging GraalVM to build native applications, Quarkus can 
achieve incredibly fast startup times (in the order of milliseconds), low memory utilization and small application 
footprint. These characteristics enable automatic scale up and down of microservices in containers, as well as 
Function-as-a-Service (FaaS) on the spot execution. 
  
*Imperative and reactive*

Although Java developers are rapidly adopting a cloud-native, event-driven, asynchronous, and reactive model to address 
business requirements to build highly concurrent and responsive applications, most Java developers are more 
familiar with the imperative programming model and would like to utilize that experience to adopt a new platform. 
Quarkus supports both imperative and reactive programming paradigms for microservices, by fully 
supporting [MicroProfile 2.2](http://download.eclipse.org/microprofile/microprofile-3.0/microprofile-spec-3.0.html#microprofile2.2), 
the [Reactive Streams Operators specification](https://github.com/eclipse/microprofile-reactive-streams-operators)
and even [Reactive Messaging](https://github.com/eclipse/microprofile-reactive-messaging) to interact with Apache Kafka. 

*Optimized for developer joy*

The vision behind Quarkus is to aim for more than productivity: using it should be enjoyable! That's why the 
team behind it has paid so much attention to make live coding, extensions and unified configuration 'just work'!  

* In development mode, which you can launch with `mvn compile quarkus:dev`, Quarkus supports live coding by
  transparently compiling changed files whenever it receives an HTTP request. 
* The extension system is designed to help create a vibrant ecosystem around Quarkus. 
  Extensions, which are basically nothing more than project dependencies, configure, boot and integrate a 
  framework or technology into a Quarkus application. They also provide the right information to GraalVM for 
  your application to compile natively.  
* Unified configuration: a single configuration file (`application.properties`) is all it takes to 
  configure every single extension. To reduce the size of this file, every extension is supposed to provide sensible 
  defaults its configuration properties.  


### Testing applications with Quarkus
Before diving into the code, it's good to take a first look at Quarkus' approach to testing. Using Quarkus you can run 
tests in two different modes:
* JVM mode
* Native mode

By convention, the test classes in 'native mode' extend the tests in JVM mode, and are executed in a Docker
container using the native executable built by GraalVM. The advantage of reusing the same test class for
JVM and native image tests is that we can start writing tests right at the beginning of a project.

I've found it useful to run sanity checks of my REST services using the `http`-command, which is available 
by installing [HTTPie](http://www.httpie.org). This is a powerful command-line HTTP-client with JSON-support, plugins
and more; although you can use `curl` or `wget` as well if you're more comfortable with them.  


### Getting started with Quarkus
At the time of writing, August 2019, the latest version of Quarkus `0.21.1`. From the version numbering
scheme you can deduce that Quarkus is currently considered `beta`-quality. And it's important to realize
that at this stage every new Quarkus version is likely to update its dependencies and libraries to the latest versions. 
So when you start building your own application, don't forget to install the latest versions of the JDK, Maven   
and GraalVM. I'm using `GraalVM community edition version 19.2.0`, `AdoptOpenJDK 8u222` and `Maven 3.6.1`, which
should be compatible with `Quarkus 0.21.1`.  

Using MacOS I've set up the following environment variables to make Quarkus happy: 
```shell script
export JAVA_HOME=/path/to/adopt-openjdk-8.0.222.10-hotspot
export GRAALVM_HOME=/path/to/graalvm-ce-19.2.0
export MAVEN_HOME=/path/to/apache-maven-3.6.1
export PATH=$GRAALVM_HOME/bin:$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
```


### Enough of this, let's start coding!
To get a feeling for Quarkus, we'll convert the code from the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic)
application to build a cloud native Quarkus application. That means we'll start by running the Spring PetClinic.


##### Preparation. 
Clone and run the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic).

```shell script
cd /path/to/workshop
git clone https://github.com/spring-projects/spring-petclinic.git
cd spring-petclinic

mvn clean package
mvn spring-boot:run
```

The last step takes about 16 seconds to run on my machine, of which Spring Boot reports to require about 5.7 seconds to startup. 
Let's conclude by ensuring that the Pet Clinic works fine, either by opening `http://localhost:8080` in your favorite 
browser, or by calling the exposed 'veterinarians' REST service with [HTTPie](http://www.httpie.org).     

```shell script
http localhost:8080/vets
```

As might be expected, this returns a JSON-style list of all the veterinarians in the PetClinic application. 
Let's start by recreating this basic REST service in Quarkus.     


### Writing the PetClinic 'veterinarians' service in Quarkus.
 
##### Step 1. Create a Quarkus skeleton application
Similar to Spring-boot and Micronaut, we can use the Quarkus Maven plugin to build and run our first 'Hello world' 
application with Quarkus.       
       
```shell script
cd /path/to/workshop

mvn io.quarkus:quarkus-maven-plugin:0.21.1:create \
    -DprojectGroupId=com.github.acme \
    -DprojectArtifactId=quarkus-petclinic \
    -DclassName="com.github.acme.quarkus.petclinic.web.resource.VetResource" \
    -Dpath="/vets"

cd quarkus-petclinic
./mvnw compile quarkus:dev
```

If we test the resulting service by calling `http :8080/vets`, this will return `hello` in plain text.
Not really exciting, but what else could you expect from a 'Hello world' application? 
Let's continue by adding the domain model.    

 
##### Step 2. Implement the domain model 
JPA, the de facto standard in Object Relational Mapping, is fully supported in Quarkus using Hibernate ORM. 
To configure Hibernate, we also need a datasource to obtain the connections to a database. In Quarkus, the 
standard datasource and connection pooling implementation is [Agroal](https://agroal.github.io/).
We can add support for Hibernate and Agroal by adding their Quarkus-extensions to our project, like so:

```shell script
./mvnw quarkus:add-extension -Dextensions="agroal, hibernate-orm"
```

Because this action changes the Maven `pom.xml` file, it's a good idea to stop the `quarkus:dev` build first.  
Now we can copy model from the Spring PetClinic to our newly created Quarkus PetClinic: 

```bash
cp -R ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/model src/main/java/com/github/acme/quarkus/petclinic
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/vet/Specialty.java src/main/java/com/github/acme/quarkus/petclinic/model
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/vet/Vet.java src/main/java/com/github/acme/quarkus/petclinic/model
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/Owner.java src/main/java/com/github/acme/quarkus/petclinic/model
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/Pet.java src/main/java/com/github/acme/quarkus/petclinic/model
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/PetType.java src/main/java/com/github/acme/quarkus/petclinic/model
cp ../spring-petclinic/src/main/java/org/springframework/samples/petclinic/visit/Visit.java src/main/java/com/github/acme/quarkus/petclinic/model
```

Because the domain classes use Spring specific code, we have to fix a bit more than the imports and package names.
Fortunately we can benefit from lambda methods in Java 8, which makes these changes almost trivial. 
For example, the `getSpecialties()` method of the `Vets` class can be fixed as follows:   

```java
public class Vet extends Person {

    public List<Specialty> getSpecialties() {
        List<Specialty> sortedSpecs = new ArrayList<>(getSpecialtiesInternal());

        // old code
        // PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
  
        // new code
        sortedSpecs.sort(Comparator.comparing(NamedEntity::getName));

        return Collections.unmodifiableList(sortedSpecs);
    }
}
```

After fixing the compilation errors, running `mvn compile quarkus:dev` will fail with an error
```
 ERROR [io.qua.dev.DevModeMain] Failed to start quarkus: java.lang.RuntimeException: io.quarkus.builder.BuildException: Build failure: Build failed due to errors
       [error]: Build step io.quarkus.hibernate.orm.deployment.HibernateOrmProcessor#build threw an exception: io.quarkus.deployment.configuration.ConfigurationError: Hibernate extension cannot guess the dialect as no JDBC driver is specified by 'quarkus.datasource.driver'
```
Looking at the error message, the reason why Quarkus fails to start is obvious. We haven't configured a database yet. 
        

##### Step 3. Configuring the databases in Quarkus  

Currently, Quarkus provides driver extensions for the following databases.
* H2
* PostgreSQL
* MariaDB (and MySQL)
* Microsoft SQL Server

To retain consistency with the Spring PetClinic, We want H2 in-memory-databases for development and 
MariaDB (MySQL) in production. Similar to Spring and Micronaut, you can use Quarkus 
[configuration profiles](https://quarkus.io/guides/application-configuration-guide#configuration-profiles)
to distinguish between various runtime environments.

We can add both extensions to Quarkus using a single command:
```shell script
./mvnw quarkus:add-extension -Dextensions="jdbc-h2, jdbc-mariadb"
```

For now, we only have to configure H2 in the `dev` profile, by editing the 
unified configuration file `src/main/resources/application.properties` 
```properties
%dev.quarkus.datasource.url=jdbc:h2:mem:default
%dev.quarkus.datasource.driver=org.h2.Driver
%dev.quarkus.datasource.username=username-default
%dev.quarkus.datasource.min-size=3
%dev.quarkus.datasource.max-size=13
%dev.quarkus.hibernate-orm.database.generation=drop-and-create
%dev.quarkus.hibernate-orm.sql-load-script=db/hsqldb/data-and-schema.sql

%prod.quarkus.hibernate-orm.database.generation=none
%prod.quarkus.datasource.driver=org.mariadb.jdbc.Driver
```

This wil configure Hibernate to drop and create an empty database and run a script upon startup.
All we have to do now is combine the sql files for HSQLDB from the Spring Petclinic application
to our new Quarkus PetClinic application. 

```shell script
mkdir -p src/main/resources/db/hsqldb
cat   ../spring-petclinic/src/main/resources/db/hsqldb/schema.sql \
      ../spring-petclinic/src/main/resources/db/hsqldb/data.sql \
    > src/main/resources/db/hsqldb/data-and-schema.sql
```

If we try to run our Quarkus PetClinic application again using `mvn compile quarkus:dev`, it should
startup without any errors. We can test that calling `http :8080/vets` still returns `hello` in plain text.

Let's fix that by implementing the `VetResource` class! 

##### Step 4. Implement the VetResource class

##### Add support for JSON binding and 'Repository' patterns'
Developers tend to have a love/hate relationship with DAOs or Repositories; some even claim that they can't live without them. 
To implement the `VetResource` class in a similar way to the Spring PetClinic, we will use the Repository pattern
and re-implement the `VetRepository` class with the `Hibernate Panache` Quarkus-extension.
 
The vision behind Hibernate Panache is 'to make your entities trivial and fun to write and use'. 
Or put in another way, Hibernate Panache is intended to address complaints such as: 
"Although Hibernate ORM can make complex mappings possible, it does nothing to make simple and common mappings trivial."
In a way, using Hibernate Panache reminds me of Lombok, in the sense that it you can reduce clutter with it. 
You simply have to let your entities extend the `PanacheEntity` class, change the columns from private fields to public fields 
and you can remove all the getters and setters, auto-generated ID's, etc.
                                                                             
Similar to getters and setters, having repositories is optional in Hibernate Panache. If you don't want them, 
you can define convenient finder and list methods inside your entities instead of in a repository-class.

And of course the application must be able to consume and produce JSON. In Quarkus there are two extensions available which 
support 'JSON binding': Jackson and RESTeasy. As RESTeasy appears to be the most mature, we'll use the
RESTeasy JSON binding extension. Once again, it's possible to add both dependencies in a single statement: 
                                                              
```shell script
./mvnw quarkus:add-extension -Dextensions="hibernate-orm-panache, resteasy-jsonb"
```

##### Implement the VetResource
Let's start by implementing the `VetResource`, which will inject the `VetRepository` class defined below. 

```java
@Path("/vets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VetResource {

    @Inject
    VetRepository vetRepository;

    @GET
    public List<Vet> list() {
        return vetRepository.listAll();
    }
}
 
```

##### Implement the VetRepository
By convention, all finder methods are created in the `VetRepository`. Currently we don't need 
to add a specific finder, because the `listAll()` method used by the `VetResource` is already 
implemented by the `PanacheRepository` superclass. 

For demonstration purposes, lets add the 'find by last name' method, 
taking advantage of the `find()` method provided by the `PanacheRepository` superclass.
And we can use this in the `VetResource` class if we so desire.  

```java
@ApplicationScoped
public class VetRepository implements PanacheRepository<Vet> {

    public Vet findByName(String name) {
        return find("lastName", name).firstResult();
    }

}
```

##### Running the application 
```shell script
mvn compile quarkus:dev
http :8080/vets
```

Hooray! This immediately returns the expected result in JSON, similar to the following JSON snippet.

```json
[
    {
        "firstName": "James", "id": 1, "lastName": "Carter", "new": false, "nrOfSpecialties": 0,
        "specialties": []
    },
    {
        "firstName": "Helen", "id": 2, "lastName": "Leary", "new": false, "nrOfSpecialties": 1,
        "specialties": [{ "id": 1, "name": "radiology", "new": false }]
    }
]
``` 


##### Step 5. Reducing clutter with Panache entities 

Optionally, we can decide to reduce some of the clutter by making our Quarkus PetClinic entities extend `PanacheEntity` 
and remove all unused getters and setters. If required, we could even remove the `VetRepository` altogether
and use the listAll method provided by Panache entities, like so:

```java
@Path("/vets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VetResource {

    @GET
    public List<Vet> list() {
        return Vet.listAll();
    }

}
```

Taking advantage of Quarkus's hot-reload functionality, we can verify that the application still works satisfactorily
with HTTPie, and run `http :8080/vets` again. Sweet!
 

### Testing your application with Quarkus
Having verified that our VetResource works, we can modify the skeleton test `VetResourceTest`
which was generated by [Step 1. Create a Quarkus skeleton application](#step-1-create-a-quarkus-skeleton-application).

When unit testing applications, it's good practice to use a Mock Repository, the `MockVetRepository`, as shown below.
We could start by making the `allVets()` method return an empty list.

```java
@Mock
@ApplicationScoped
public class MockVetRepository extends VetRepository implements PanacheRepository<Vet> {

    @Override
    public List<Vet> allVets() {
        return Collections.emptyList();
    }
}
```

When we update the `VetResourceTest` class as follows, we can execute the test.

```java
@QuarkusTest
public class VetResourceTest {

    @Inject
    VetRepository vetRepository;

    public void testFindAllVets() {
        given()
                .when().get("/vets")
                .then()
                .statusCode(200)
                .body(is("[]"));
    }
}
```

Unfortunately there is a [bug in Hibernate and Quarkus](https://github.com/quarkusio/quarkus/issues/3643)
which causes a `java.lang.VerifyError` and prevents startup of our Quarkus test class. 
Fortunately a [simple change in the domain model](https://hibernate.atlassian.net/browse/HHH-13446?oldIssueView=true)
can resolve this and now our test runs fine!


### Going native with Quarkus
The best thing with Quarkus is its great support for building native images.
To set this up, we need to update the `application.properties` file with a 
production (`prod`) profile to support MariaDB, similar to the Spring PetClinic.

```properties
 
%prod.quarkus.datasource.url=jdbc:mariadb://localhost:3306/petclinic
%prod.quarkus.datasource.driver=org.mariadb.jdbc.Driver
%prod.quarkus.datasource.username=root
%prod.quarkus.datasource.password=petclinic
%prod.quarkus.hibernate-orm.database.generation = none
%prod.quarkus.hibernate-orm.sql-load-script = no-file
```

If you don't have a MariaDB instance running, you can start one using Docker and
create the schema and initial data using the scripts from the Spring PetClinic.
For example on my MacBook, I can run the following script:

```shell script             
docker run --name mariadb-pets -p 3306:3306 -e MYSQL_ROOT_PASSWORD=petclinic -d mariadb/server:10.3 --log-bin --binlog-format=MIXED

brew install mariadb
mariadb -P 3306 --protocol=tcp -u root -p < src/main/resources/db/mysql/schema.sql > /tmp/output.tab
mariadb -P 3306 --protocol=tcp -u root -p < src/main/resources/db/mysql/data.sql   > /tmp/output.tab

mariadb -P 3306 --protocol=tcp -u root -p

   select * from petclinic.vets;
   exit
```

Assuming that GraalVM is already installed, we can test and run the application using 

```shell script
gu install native-image
./mvnw clean package -Pnative
./target/quarkus-petclinic-1.0-SNAPSHOT-runner
```

Now we can run the application and do a basic comparison of dev-mode vs a native application.
* Spring boot dev-mode
  - Startup 5.654s
  - `time http :8080/vets`
    - real	0m0.270s
    - user	0m0.215s
    - sys	0m0.045s
* Quarkus dev-mode
  - Quarkus 0.21.1 started in 3.430s. Listening on: http://[::]:8080
  - `time http :8080/vets`
    - real	0m0.278s
    - user	0m0.218s
    - sys	0m0.047s
* Native application
  - Quarkus 0.21.1 started in 0.019s. Listening on: http://[::]:8080
  - `time http :8080/vets`
    - real	0m0.265s
    - user	0m0.210s
    - sys	0m0.045s

Digesting the timings above, it's important to realize that our native application retrieved its data
from a real database, MariaDB, in Docker. Both Spring Boot and Quarkus dev-mode used a fast in-memory database.
And of course, the real difference is found in the startup-time of our native application.
  
### Wrap up
Converting your existing JSON REST services from Spring Boot to Quarkus is a breeze!
What you get is an application with less code, which starts up much faster, with the added bonus 
of a Kubernets native enabled executable. Awesome stuff!
