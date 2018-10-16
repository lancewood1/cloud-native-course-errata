# [Cloud Native References and Errata]

# Before we begin

- Instructor introduction
- Agenda overview
- Students introduction
   - Your Java, Spring, Spring Boot experience
   - Your PCF experience
     -   Have you taken PCF primer course?,
     -   Have you done the 5 labs?
   - What you want to get out of this training
- General structure of this training
   - 80% lab (and discussion), 20% lecture
   - Collaboration (even pair programming) strongly encouraged
   - Team introduction
- Course material review
   - GAIA course material
   - Supplemental document (please bookmark this page)
- Logistics
   - 2 breaks, 1-hour lunch at noon?

# Introduction to The Cloud

## Push to the Cloud

### Known issues

  - `python` app does not have correct dependency - you have to add the
    `flask` to the `requirements.txt`
  - `ruby` application needs to change the Ruby version in the `Gemfile`. 
    
    ```
    ruby '>2.1.8'
    ```
    
  - The URL of the `console` (App manager) should start with `console...` but
    lab document (or email) says `login...`
    
### Lab extras

-   Try the following commands

    ```
    cf help -a
    cf push -h
    cf orgs
    cf spaces
    cf env <app-name>
    cf domains
    cf delete-orphaned-routes
    ```
    
### Challenge questions

- What is the "org"/"space" structure in your PCF installation?
- What makes up a route? 
- Why do you have to use "--random-route"? 
- What is the "cf push" option
  that lets you specify the hostname part of a route? ("cf push -h")
- What are the environment variables that are automatically set
  by PCF? (Google `Cloud Foundry Environment Variables`)

## Logging, Scale, HA

### Trouble-shooting

- If you see the following error in the log, it means you will
  need to bump up the memory for your Java app to `768M`
  
  ```
  2018-10-14T10:08:27.14-0400 [APP/PROC/WEB/0] ERR Cannot calculate JVM memory configuration: There is insufficient memory remaining for heap. 
  Memory available for allocation 512M is less than allocated memory 623337K (-XX:ReservedCodeCacheSize=240M, -XX:MaxDirectMemorySize=10M, -XX:MaxMetaspaceSize=111337K, -Xss1M * 250 threads)
  ``` 

### Challenge questions on pushing Java app

- Why, everytime you push an app, are the list of buildpacks downloaded again?
- How many containers are needed for pushing an app?
- Why do you see "container gets created and destroyed" as part of pushing app?
- Can you find diagram that shows the sequence of internal operations
    that occur when pushing an application? (Google "PCF how applications 
    are staged".)
- Suppose you deployed an application with "cf push <app-name> -m 768M",
    what would be memory allocated when you re-deployed the same application
    with "cf push <app-name>"?
- What is the auto-configuration is being performed when
  you push an application?  See [here](https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration) for an anwser.

### Challenge questions on Logging

- What is the difference between `cf logs articulate` vs 
  `cf logs articulate --recent`?
- Where should your application write logs? Is it a good practice
  write logs to a file? How does this change how you access logs 
  today? At scale?
- What are some of the different origin codes (log types) seen in the log?
  (Google "PCF log types"!)
- Can you find `PCF architecture diagram`? (Google it!)
- What is the PCF component that handles logging? 

### Challenge questions on Scaling

- What is the preferred, "scaling out" or "scaling up"?  Why?
- When inceasing number of instances, do existing instances get restarted?
  How do you find that out? What about the case of decreasing number of
  instances?
- How do you recover failing application instances today?
- What effect does this have on your application design?
- Is there "auto-scaling" service that automatically scales up
  or scale down depending on CPU utilization, latency, etc?

### Challenge questions on HA

- How could you determine if your application has been crashing?
- What are 4 HA features in PCF?

### Lab extras (for pushing Java app)

-   Use Java option to reduce the memory requirement
    of your Spring app as following
        
    ```
    cf push spring-music -p ./build/libs/spring-music-1.0.jar --random-route -m 512M --no-start
    cf set-env spring-music JAVA_OPTS -Xss228K
    cf start spring-music
    ```
    
    or set it in the manifest file
    
    ```
    applications:
    - name: spring-music
      disk_quota: 1G
      env:
        JAVA_OPTS: -Xss228K
      instances: 1
      memory: 512M
      path: ./build/libs/spring-music-1.0.jar
      routes:
      - route: spring-music-unemigrant-nontransportation.cfapps.io
    ```
    
-   Push docker image `tutum/hello-world` (Try `cf push -a | grep docker`)
    
-   [This is disabled in GAIA] Do `cf ssh articulate` 
    and display the values of the
    environment variables using 
    
    ```
    $echo $<environment-variable-name>
    ```
    
-   [This is disabled in GAIA] Install and use `cf cli open` 
    plugin as described in
    [Cloud Foundry community](https://plugins.cloudfoundry.org/)
    
    ```
    cf install-plugin -r CF-Community "open"
    ```

### Lab Extras (for Scaling)

-   [This is disabled in GAIA]
    Try auto-scaling available in the App Manager as following

    - Start with 3 instances of `articulate`
    - Use `CPU utlization` so that it scales down to 1 when
      it is less than 50%
    - Observe that the number of instances scales down to 1
    - You will also receive email notifications everytime
      scaling up and down event occurs
    - Turn off the auto-scaling

## Services

### Trouble-shooting tips

-   In order to verify that `attendee-service` is bound to 
    `attendee-mysql-<sid>`, you can also add a new `attendee` using 
    curl/HTTPie/PostMan
    
    ```
    curl -i -XPOST -H"Content-Type: application/json" attendee-service-<sid>.cfapps.io/attendees -d'{"firstName": "Pete", "lastName": "Jones", "emailAddress": "man@man.com"}' 
    curl attendee-service-<sid>.cfapps.io/attendees
    ```
    
    ```
    http post attendee-service-<sid>.cfapps.io/attendees firstName=sang1 lastName=shin2 emailAddress=yo@yo.com
    http attendee-service-<sid>.cfapps.io/attendees
    ```
    
-   If you experience the following error when performing 
    `cf bind-service articulate attendee-service-upsi`, it is because
    the `attendee-service-upsi` is created with incorrect 
    `url` value, possibly without `attendees` at the end.
    
    ```
    2018-10-15T09:44:55.50-0400 [APP/PROC/WEB/0] ERR Caused by: java.lang.RuntimeException: 
    The attendee-service uri must have '/attendees' in the path.  For example: http://localhost:8181/attendees
    ```
    
-   If you experience the following error when you try 
    `cf delete-service attendee-service-upsi`, it is because it
    is still bound to `articulate` application. Unbind the service
    first from the application before deleting.

    ```
    < gaia/attendee-service > cf delete-service attendee-service-upsi
    Really delete the service attendee-service-upsi?> yes
    Deleting service attendee-service-upsi in org sashin-org / space jpmc as ..
    FAILED
    Cannot delete service instance, service keys and bindings must first be deleted
    ```

### Challenge questions

- What does "create service" do? What about "bind service"?
- What is the difference between "restart" and "restaging"? What
  could be use cases you will have to do "restaging"?
- What is the difference between "managed service" (service found
  in the "cf marketplace") and "non-managed service" 
  ("user provided service")?
- What are the 3 use cases of "user provided service"? 
  ("cf cups -h")
- From an application perspective, are managed services
  instances different from user provided service instances?
- Is there a way to make your "non-managed service" "managed service"?
- What `cf` command(s) can I use to find out what applications
  are bound to a particular service instance?

### Lab extras

-   Use `Papertrail` as a user provided service following 
    the steps below

    -   Sign up for an account at 
    [Papertrail](https://papertrailapp.com/)
    -   Follow the 
    [instruction](http://help.papertrailapp.com/kb/hosting-services/cloud-foundry/)

-   Use `New Relic` as a user provided service following
    the steps below
  
    -    Go to [newrelic](https://newrelic.com/signup) and 
         create a free trial account
    -    Once logged in, select `APM` and select `Java`
    -    Scroll down until you see “Get your license key”. 
    -    Click “Reveal your license key” and copy the value
    -    Create UPSI (User Provided Service Instance) 
         using `-p licenseKey`
    -    Bind your application to the newly created UPSI

## Buildpacks

### Trouble-shooting

-   If you experience the following error, it is because your PCF installation
    does not have the version you specified in offline mode. 
     
    
    ```
    -----> Downloading Jvmkill Agent 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/jvmkill/trusty/x86_64/jvmkill-1.16.0_RELEASE.so (found in cache)
    ERROR Finalize failed with exception #<RuntimeError: Unable to find cached file for https://java-buildpack.cloudfoundry.org/openjdk/trusty/x86_64/openjdk-1.8.0_45.tar.gz>
    Unable to find cached file for https://java-buildpack.cloudfoundry.org/openjdk/trusty/x86_64/openjdk-1.8.0_45.tar.gz
    Failed to compile droplet: Failed to run finalize script: exit status 1
    ```
    
    You can try online version of `java_buildpack` as shown below.
    
        
    ```
    cf push articulate -p articulate-0.0.1-SNAPSHOT.jar -b https://github.com/cloudfoundry/java-buildpack
    ```

### Challenge questions

- What are the buildpack scripts that get executed when 
  creating a droplet?
  (Google "pcf understanding buildpacks")
- What is the buildpack script that gets omitted when 
  you deploy a docker image?
- What is the pros and cons of using buildpack (to create 
  an container image) as opposed to using docker image?
  
### Lab extras

-   [This is disabled in GAIA] Do `cf ssh articulate` 
    and see which command 
    is executed by the `release` script.  
-   [This is disabled in GAIA] Do the same for `Node` 
    and `Ruby` applications

## Blue-Green deployment

### Known issues

- Internet Explorer breaks on Blue Green - use Chrome
- The lab document uses the term `subdomain`, 
  which is actually `hostname`, which can be specified
  by either `--hostname` or `-n`
  
### Steps of Blue-Green Deployment

1.  V1 - R1 is currently running
1.  Create V2 with R2 and make sure it is working
1.  Add R1 to V2 - now V2 handles both R1 and R2
1.  Remove R1 from V1 - now V2 handles 100% of R1
1.  Remove R2 from V2 - now V2 handles only R1
1.  Rename V2 to V1

### Challenge questions

- Can an application have multiple routes?
- Can a route be applied to multiple applications?
- Can a route exist without an application associated with it?
  (See "cf routes" and "cf create-route" commands.)
- How can I delete all orphaned routes?
  (See if there is "cf" command for this.)
- Is a route unique per PCF installation, per org, or per space?
- What are the constraints of Blue-Green deployment?
- What types of versions is the PCF Blue-Green deployment 
  good for?  See about [Semantic Versioning](https://semver.org/)

### Lab extras

- [This is disabled in GAIA] Use 
  [blue-green-deployment](https://github.com/bluemixgaragelondon/cf-blue-green-deploy) 
  plugin for 
  automating the process of blue-green-deployment
  
# Spring Boot

## Spring vs. Spring Boot

### Before you do the lab

  - **Skip the first part of the lab 
    `Creating a Spring Web Application`** given that this is just 
    to show how much more work you have to do if you are not using
    Spring Boot - Just do the second part of the lab 
    `Creating a Spring Boot Web Application`
  - **Make sure to select "Spring Boot 1.5.x" (not "Spring Boot 2.0.x")**
    if you are creating a Spring Boot app from `start.spring.io`.
  - If you are creating an Spring Boot app from internal 
    `go/moneta`,
    it is already using "Spring Boot 1.5.x"
  - Make sure you are using Java 8 - using Java 9 might result in
    an error as described [here](https://blog.codefx.org/java/java-9-migration-guide/#Dependencies-On-Java-EE-Modules)
  - Feel free to create the project within the IDE (IntelliJ or STS)
  - If you experience Maven build problem, make sure you set the
    correct Maven proxy in your `$HOME/.m2/settings.xml` file
    
### Trouble-shooting tips

  - If you experience 404 error when accessing your controller,
    think about the rules of component scanning
    
### Lab extras

#### Use properties

  - Add `message` property to `application.yml` (or `application.properties`)
    and use it as a string that gets returned (instead of "Hello world")
  - Use different port (instead of default port of 8080)
    by setting `server.port` property
  - Use customer banner: create one from 
    [patorjk.com](http://patorjk.com/software/taag/#p=display&f=Graffiti&t=Type%20Something%20) 
    by creating `banner.txt`
    under `/src/main/resources` directory 
   
#### Use profiles

  - Review [Spring Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
  - Create spring profile: `dev` and `production` 
    from which different
    message value can be extracted
  - Run the application with `SPRING_PROFILES_ACTIVE` 
    environment variable set to either `dev` or `production`
    
    For Windows,
    
    ```
    set SPRING_PROFILES_ACTIVE=production
    mvn spring-boot:run
    ```
    
  - Create external property file(s) as opposed 
    to the property files
    inside the jar file
  - Set the `message` property as environment variable 
    and run the application
    
#### Write integration test code

  - In general, you should write test code first in the
    sprit of TDD.   
    (See [example code](https://github.com/sashinpivotal/spring-boot-tdd) as a reference) 
    
#### Deploying it to PCF

  - Deploy the application to the cloud and run with `dev` profile
    setting `SPRING_PROLFILES_ACTIVE` environment variable 
    to `dev`
  - Create manifest file using `cf create-app-manifest` command
  - Deploy the app using the newly created manifest file - note
    that manifest file that gets created does not have `path`
    attribute

### Challenge questions

  - What are three ways to config Spring bean? 
    What are the use cases
    for each scheme?
  - What are the 4 major features Spring Boot provide?

## Spring Boot Internals

### Challenge questions

- Which auto-configuration class is used for auto-configuring 
  data source object?
- What file tells Spring Boot which configuration 
  to load?

## Data Access

### Challenge questions

- Name some of the Enterprise Application Patterns implemented by the Snippet Manager.
- Why are there different Snippet objects (i.e. SnippetRecord vs. SnippetInfo)?
- How might you reuse or refactor the solution to support either synchronous over HTTP, or an asynchronous solution adding or updating Snippets through AMQP or Kafka?

### Lab extras

- Take a look `DataSourceConfiguration` class, which auto-configures
  `DataSource` bean
- Review the `SnippetController.snippets` GET method.
	- What is the Java programming style/API used?
	- How might you refactor the solution to account for 
	  hosting active or inactive Snippets, and filtering 
	  only active records?

## Actuator

### Before you do the lab

  - When creating Spring Boot project 
    from [Spring Initializr](https://start.spring.io/),
    make sure you choose Spring Boot 1.5.x (not Spring Boot 2.0.x)
    given that the document is written with Spring Boot 1.5.x 
    in mind
  - Feel free to use the Spring Boot project that was created
    in the previous lab - just add `spring-boot-starter-actuator`
    dependency
  - **Skip the `Challenges` part of the lab** in the middle of
    the document
  - Lab document assumes you are using Gradle. If you are 
    using Maven, just add Maven dependency.

### Lab extras

  - Add `MemoryHealthIndicator` (The solution project is 
    available from 
    [here](https://github.com/sashinpivotal/helloworld1))
    - If the ratio of freeMemory/totalMemory
      is less than 0.9, return `DOWN` status. 
    - Otherwise, return `UP` status.
      
  - Add build information as described 
    [here](https://docs.spring.io/spring-boot/docs/current/maven-plugin/examples/build-info.html)
    
  - Deploy the application to the PCF

### Challenge questions

  - How to use Actuator health checks in Cloud Foundry 
    to discard unhealthy AI's?
  - Is adding "instrumentation" code such as counting
    "number of times a call is made" as described in the 
    lab document
    a good practice?  Is there an alternative?

# Spring Cloud Services

## Setup for Bitbucket/GitHub

You will authenticate with Bitbucket/GitHub using `ssh` key.

-   Follow
  [the instruction](https://help.github.com/articles/connecting-to-github-with-ssh/) 
    to create an ssh key pair and add the public key to 
    your GitHub account.
-   When you create `ssh` key, do not add a `passphrase` 
    to this key when you are 
    creating ssh key, as it might make things difficult 
    for some of the labs.
-   The `sping cloud services labs` projects can be cloned 
    in the following 3 ways: use one that works for you.

    -   Option #1: choose this if you set up your ssh key
        (This is the way official lab document suggests)
        

        ```
        git clone ssh://git@bitbucketdc-ssh.<company>.net:7999/cloudtraining/apps-spring-cloud-services-labs.git spring-cloud-services-labs 
        ```
        
    -   Option #2: choose this if you're having a problem 
 	     using option #1


 	     ```
 	     git clone https://<sid>@bitbucketdc.<company>.net/scm/cloudtraining/apps-spring-cloud-services-labs.git spring-cloud-services-labs
 	     ``` 
 	
    -   Option #3: choose this if you're having a problem 
 	     using #1 and #2 above
 	     

 	     ```
 	     git clone https://github.com/pivotal-bill-kable/apps-spring-cloud-services-labs spring-cloud-services-labs
 	     ```


## Config Server

### Before you get started

- Make sure `app-config` and `spring-cloud-services-labs` 
  directories are two
  separate directories: in other words, do not make one to be under
  the other.  The following shows a correct directory structure.
 
 ```
 drwxr-xr-x   4 sang  staff   128B Jun 22 16:22 app-config/
 drwxr-xr-x   3 sang  staff    96B Mar  5  2018 articulate/
 drwxr-xr-x   3 sang  staff    96B Mar  5  2018 attendee-service/
 drwxr-xr-x   9 sang  staff   288B Mar 19  2018 demo-apps/
 drwxr-xr-x  17 sang  staff   544B Mar  6  2018 spring-cloud-services-labs/
 ```
 
- If you have not set up `ssh` key (or somehow it is not working),
  use local `app-config` directory as your repository 
  as a temporary means as shown below
  
   ```yml
   server:
     port: 8888

   spring:
     cloud:
       config:
         server:
           git:
             uri: file:///C:\tmp\app-config

   ```

- Please note that the quote server mentioned in the 
  lab document as shown below is not working

	```yml
	quoteServiceURL: http://quote-service-dev.apps.dev.na-1.<company>.net/quote
	```
	
  Build and deploy your own quote server from 
  [here](https://github.com/pivotal-bill-kable/apps-spring-cloud-services-labs/tree/master/quote-server)	as following:
  
  -   Deploy one with a hostname `quote-service-<sid>-dev`
  -   Deploy another one with a hostname `quite-service-<sid>-qa`

  Or use the one provided by the instructor
  
  ```
  quoteServiceURL: http://quote-server-sang-shin-dev.cfapps.io/quote
  ```
 
### Tool Recommendations 
  
- If you are using IntelliJ Ultimate Edition or STS, 
  please feel free to use `Spring Boot Dashboard`
- If you are using `Windows`, please feel free to use
  [ConEMU shell](https://conemu.github.io/) 

### Challenge questions

- What happens to your application by default if config 
  server is down during its startup?
- What happens to your application by default if config 
  server is down during actuator refresh?
- How might you handle configuration changes of database 
  passwords with zero downtime?

### Lab extras

- Configure your application to fail-fast if config 
  server is not available during start up.
- Configure your application to use a retry/backoff 
  policy on a config server fail-fast.
  
## Service Disovery

### Before you get started

- Creating service instance of `p-service-registry` in PCF
  could take a few minutes.  
  You might want to create it first before starting the lab.

## Client Load Balancing

### Challenge quetions

- Would you want to use Client load balancing for 
  public-facing applications?
- How would "client-side load balancing" works under PCF given
  that addresses of application instances are in the form of
  a `route`?
  
### Labs extras

- Under PCF, enable `container-to-container networking` 
 `greeting-ribbon-rest` and `fortune` service - and observe that
  services are now registered with IP-address/Port combincation
  in the service registry dashboard

## Circuit Breakers

### Challenge questions

- Distinguish between Transient and Persistent Resource failure.
- Does Hystrix help protect against either/or/both Transient 
  and Persistent failures? How?
- Classify the use of the following downstream integration APIs 
  in your Hystrix protected methods as either "Trusted" or 
  "Untrusted", and explain why:
	 - Use of SolrCloud API
	 - Use of Zookeeper API
	 - EJB Client Kit
	 - A Rest Template managed by the hosting Spring Boot application
- Which Hystrix isolation strategy is more appropriate for 
  Trusted clients?  Which is more appropriate for Untrusted clients?
- Should you use Hystrix to protect against transactionally 
  sensitive operations?  Why or why not?

### Lab extras

- Configure Hystrix in your application to use Semaphore isolation
  strategy, restart your applications and hystrix dashboard, and 
  execute some requests.  Do you see thread pools in your dashboard?   
  Why or why not?

### Hystrix demo code

- For a demo of Hystrix fault tolerance characteristics, see [here](https://github.com/pivotal-bill-kable/spring-cloud-netflix-oss-ft-demos)


# References

## Pivotal Cloud Foundry References

- [Pivotal Cloud Foundry Architecture](https://docs.pivotal.io/pivotalcf/2-1/concepts/diego/diego-architecture.html)
- [Cloud Foundry Services](https://docs.cloudfoundry.org/services/overview.html)
- [Healthchecks](https://docs.cloudfoundry.org/devguide/deploy-apps/healthchecks.html)

## Spring Cloud - Cloud Native References

Following are some suggested references - books, courses, blogs, articles and videos to give a deeper dive into Spring Cloud
and Cloud Native Developer courses:

### Some good overview Pluralsight courses

- [Cloud Foundry for Developers](https://app.pluralsight.com/library/courses/cloud-foundry-developers/table-of-contents)
- [Pivotal Cloud Foundry for Developers](https://app.pluralsight.com/library/courses/cloud-foundry-developer-1dot7-pivotal/table-of-contents)
- [Java Microservices with Spring Cloud: Coordinating Services:](https://app.pluralsight.com/library/courses/java-microservices-spring-cloud-coordinating-services/table-of-contents)
- [Java Microservices with Spring Cloud: Developing Services](https://app.pluralsight.com/library/courses/java-microservices-spring-cloud-developing-services/table-of-contents)
- [Git Intro](https://app.pluralsight.com/library/courses/how-git-works/table-of-contents)
- [Git Advanced](https://app.pluralsight.com/library/courses/mastering-git/table-of-contents)

### App Performance Test Tools

Following are useful open tools in order of simplicity vs. flexibility

- [Siege](https://github.com/JoeDog/siege)
- Apache Benchmark (AB)
	- [website](https://httpd.apache.org/docs/2.4/programs/ab.html)
	- [tutorial](https://www.tutorialspoint.com/apache_bench/index.htm)
	- [blogs](https://qafoo.com/blog/109_why_apache_benchmark_is_not_enough.html)
- [Apache Jmeter](https://jmeter.apache.org/)
- [Blazemeter](https://www.blazemeter.com/)
- [Gatling](https://gatling.io/)

### Development, Design, and Cloud Native Migration Strategies

#### App continuum

- [This site covers strategy of evolving applications from MVP (Minimum Viable Product, unstructured apps, through modularized monoliths, to distributed systems](http://www.appcontinuum.io/)
- [A great Spring One 2017 Talk about practical use of SOLID principles](https://springoneplatform.io/sessions/solid-in-the-wild-life-when-your-software-is-actually-soft)

#### 12 factor

- [Original 12 Factors](https://12factor.net)
- ["Beyond 12 Factors" (Pivotal evolution of original 12 Factors)](https://content.pivotal.io/blog/beyond-the-twelve-factor-app)
- [Breaking the Monolith](https://content.pivotal.io/slides/breaking-the-monolith)
- [12 Factor presentation](https://content.pivotal.io/slides/the-12-factors-for-building-cloud-native-software)

#### Monolith vs Microservices

- [Monolith First](https://martinfowler.com/bliki/MonolithFirst.html)
- [Microservices Prerequisites](https://martinfowler.com/bliki/MicroservicePrerequisites.html)
- [Low risk monolith to micro services by Christian Posta](http://blog.christianposta.com/microservices/low-risk-monolith-to-microservice-evolution/)
- [Application Strangulation](https://paulhammant.com/2013/07/14/legacy-application-strangulation-case-studies/)
- [Online migration at scale blog](https://stripe.com/blog/online-migrations)


### Spring Boot Plugins (building and packaging Springboot Apps):
- [Gradle Springboot Plugin](https://docs.spring.io/spring-boot/docs/2.0.1.BUILD-SNAPSHOT/gradle-plugin/reference/html/)
- [Maven Springboot Plugin](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#build-tool-plugins-maven-plugin)
- [Enterprise Application Patterns](https://martinfowler.com/eaaCatalog/index.html)
- [Enterprise Integration Patterns](http://www.enterpriseintegrationpatterns.com/)

### Spring Cloud on .NET

- [Steeltoe (Spring Cloud Config Server and Spring Cloud Netflix on .NET)](https://steeltoe.io/)

### Netflix OSS Docs

- [Ribbon Wiki](https://github.com/Netflix/Ribbon/wiki)
- [Hystrix Wiki](https://github.com/Netflix/Hystrix/wiki)
- [Hystrix Algorithm](https://github.com/Netflix/Hystrix/wiki/How-it-Works)
- [Hystrix Metrics and Monitoring](https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring)
- [Performance Implications of using Hystrix](https://github.com/Netflix/Hystrix/wiki/FAQ%20:%20General#what-is-the-processing-overhead-of-using-hystrix)
- [Hystrix Operations and Tuning Guide](https://github.com/Netflix/Hystrix/wiki/Operations)
- [Eureka Wiki](https://github.com/Netflix/Eureka/wiki)
- [Nebula build plugins](https://nebula-plugins.github.io/)

### Spring Cloud Netflix - Eureka, Ribbon, Hystrix

- [Project Home](https://cloud.spring.io/spring-cloud-netflix/)
- [Product Documentation](http://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/1.4.3.RELEASE/single/spring-cloud-netflix.html)

### Spring Cloud Config

- [Project Home](https://cloud.spring.io/spring-cloud-config/)
- [Project Documentation](http://cloud.spring.io/spring-cloud-static/spring-cloud-config/1.4.2.RELEASE/single/spring-cloud-config.html)
- [Spring External Configuration Orders of Precedence](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
- [How to push config via git webhook](https://spencergibb.netlify.com/blog/2015/09/24/spring-cloud-config-push-notifications/)
- [Feature Toggles](https://martinfowler.com/articles/feature-toggles.html)

### Spring Cloud Bus

- [Home](http://cloud.spring.io/spring-cloud-static/spring-cloud-bus/1.3.3.RELEASE/single/spring-cloud-bus.html)
- [Targeting application for config bus refresh](http://cloud.spring.io/spring-cloud-static/spring-cloud-bus/1.3.3.RELEASE/single/spring-cloud-bus.html#_addressing_all_instances_of_a_service)

### Spring Cloud Commons - Useful for seeing what's under the hood

- [Project Home](https://cloud.spring.io/spring-cloud-commons/)
- [Projection Documention](http://cloud.spring.io/spring-cloud-static/spring-cloud-commons/1.3.2.RELEASE/single/spring-cloud-commons.html)
- [Ribbon Retry Policy](http://cloud.spring.io/spring-cloud-static/spring-cloud-commons/1.3.2.RELEASE/single/spring-cloud-commons.html#_retrying_failed_requests)
- [Service Registry - Ignoring Network Interfaces](http://cloud.spring.io/spring-cloud-static/spring-cloud-commons/1.3.2.RELEASE/single/spring-cloud-commons.html#ignore-network-interfaces)
- [Configuring Http Clients](http://cloud.spring.io/spring-cloud-static/spring-cloud-commons/1.3.2.RELEASE/single/spring-cloud-commons.html#http-clients)
- [Abstracting different Spring Cloud Service Registries](http://cloud.spring.io/spring-cloud-static/spring-cloud-commons/1.3.2.RELEASE/single/spring-cloud-commons.html#__enablediscoveryclient)

### Pivotal Cloud Foundry - Spring Cloud Services

- [Home](http://docs.pivotal.io/spring-cloud-services/1-4/common/index.html)
- [Dependencies Matrix](http://docs.pivotal.io/spring-cloud-services/1-4/common/client-dependencies.html)
- [Configuring Cross Cloud Foundry Service Registy (route mode)](http://docs.pivotal.io/spring-cloud-services/1-4/common/service-registry/enabling-peer-replication.html)
- [GoRouter does honor Ribbon load balancing algorithm](http://docs.pivotal.io/spring-cloud-services/1-4/common/service-registry/connectors.html#instance-specific-routing-in-ribbon)
- [Configuring PCF Container-to-Container Networking, Service Registry and Client Load Balancing (SpringOne 2017)](https://www.youtube.com/watch?v=1WJhFhBr-0Q)

### Pivotal Cloud Foundry - Security

- [PCF Security](https://www.slideshare.net/WillTran1/enabling-cloud-native-security-with-oauth2-and-multitenant-uaa)

### Blogs

- [General blog of Cloud Native, Spring Cloud subjects from a Spring Cloud thought leader](https://spencergibb.netlify.com/)
- [Dipping into spring cloud topics from a Spring Cloud contributor](http://ryanjbaxter.com/)
- [The Spring Cloud blog](https://spring.io/blog)

### Spring Cloud Dataflow documentation (handling streaming and data centric applications using cloud native patterns and tooling)

- [Project Home](https://cloud.spring.io/spring-cloud-dataflow/)
- [Project Reference](https://docs.spring.io/spring-cloud-dataflow-samples/docs/current/reference/htmlsingle/)
- [Task Batch](https://docs.spring.io/spring-cloud-dataflow-samples/docs/current/reference/htmlsingle/#_task_batch)
- [Using PCF Job Scheduler](http://docs.pivotal.io/pcf-scheduler/1-1/using.html)
- [Project Home for Dataflow on PCF](https://cloud.spring.io/spring-cloud-dataflow-server-cloudfoundry/)
- [Project Reference for Dataflow on PCF](https://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/current-SNAPSHOT/reference/htmlsingle/)

### Spring Cloud Supplement

- [Service Registry Pattern](./supplement/ServiceRegistry.pdf)
- [Service Discovery with Netflix Eureka](./supplement/ServiceDiscoverywithNetflixEureka.pdf)
- [Load Balancing Patterns](./supplement/LoadBalancingPatterns.pdf)
- [Fault Tolerance Patterns](./supplement/FaultTolerancePatterns.pdf)
- [Circuit Breaker Pattern](./supplement/CircuitBreakerPattern.pdf)
- [Hystrix Load Shedding](./supplement/HystrixLoadShedding.pdf)
- [Hystrix Isolation Strategy](./supplement/HystrixIsolationStrategy.pdf)

### Spring Cloud App Code

- [Legacy lab code repo](https://github.com/pivotal-education/apps-spring-cloud-services-labs)

### Books

- [Release It! 2nd Edition](https://www.amazon.com/Release-Design-Deploy-Production-Ready-Software-ebook/dp/B079YWMY2V/ref=mt_kindle?_encoding=UTF8&me=&dpID=419zAwJJH4L&preST=_SX342_QL70_&dpSrc=detail)
- [Continuous Integration](https://www.amazon.com/Continuous-Delivery-Deployment-Automation-Addison-Wesley-ebook/dp/B003YMNVC0/ref=mt_kindle?_encoding=UTF8&me=&dpID=51yF2SYUi7L&preST=_SY445_QL70_&dpSrc=detail)
- [Building Evolutionary Architectures](https://www.amazon.com/Building-Evolutionary-Architectures-Support-Constant-ebook/dp/B075RR1XVG/ref=mt_kindle?_encoding=UTF8&me=&dpID=61kAEC%252BouJL&preST=_SY445_QL70_&dpSrc=detail)

# Before the Training

- The following message can be sent out to students
  a few days before the training:
  
```
Folks,

My name is <Instructor-Name>. I will be the instructor of the 2-day
"GAIA Cloud Native Application with Spring Cloud" training,
which starts on <Date> at JPMC campus in <City>.

A few items I would like to communicate before the training:

- In addition to the official lab document you have received,
  we are going to use the following supplemental document
  (We will call it "supplemental document" from now on.)
 
  https://github.com/pivotal-bill-kable/cloud-native-course-errata/blob/master/README.md
  (Please bookmark this document along with the official one.)
 
  This goal of this document is to supplement the official
  training material in the following aspects.
     
  - Known issues (in the lab document and code)
  - Read this (before you do the lab)
  - Trouble-shooting tips
  - Challenge questions
  - Lab extras (in case you finish earlier than others)
  - References

  Please also note that I, as non-JPMC employee, do not have
  access to the official lab document - I am using pseudo
  lab document that is supposed to be close to what you
  have.
 
- Even though the prerequisites email says that you are
  supposed to finish the following labs on your own, we
  will go over these topics in the morning of Day 1.
 
  1. Push to Cloud
  2. Logging, Scale, HA
  3. Services
  4. Buildpacks
  5. Blue-Green Deployments
 
- If you bring your laptop, please make sure your laptop
  is configured as following
 
  - Being able to connect your GAIA API endpoint
  - Being able to connect internal JPMC Maven repository: See
    if you can build a simple Spring Boot app
   
I will be in the classroom from 8:15AM in case you need any
help.

If you have any questions/issues, please feel free to contact
me (sashin@pivotal.io) before/during/after the training.

Looking forward to seeing you all.

-<Instructor Name>
```

- The following message can be sent out to students at
  the end of Day 1 before
  `Spring Cloud Services` part gets started on Day 2

```
-Setting up "ssh" key with your BitBucket

 If you have not set up "ssh" key with your BitBucket, 
 please do so before we start the `Spring Cloud Services` lab
 on Day 2.  (Instructors will be in the classroom around
 8:30AM in case you need help.)

 Please read the "Setup for BitBucket/GitHub" section under  
 "Spring Cloud Services" of the reference document below

 https://github.com/pivotal-bill-kable/cloud-native-course-errata/ blob/master/README.md

-Lunchtime tech-talk

We might have "lunchtime tech-talk" on "OAuth2" between  
12:30PM-1:00PM on Day 2 if there is enough interest.
The presentation slides are available from the following links

  -https://www.slideshare.net/SangShin1/spring4-security-oauth2
  -https://www.slideshare.net/WillTran1/enabling-cloud-native-security-with-oauth2-and-multitenant-uaa (only slides 7-37)

```
