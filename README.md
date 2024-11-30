# μsManager 

Microservices management on Cloud and Edge

μsManager is a system for managing microservices dynamically, whether in the cloud or at the edge.
It replicates and migrates microservices according to the service load,
through the collection of various metrics, such as device cpu and ram usage, location of requests,
dependencies between microservices, and available cloud and edge devices.
This is a project to obtain a master's degree in Computer Engineering at [FCT-UNL](https://www.fct.unl.pt/).

### Project Structure

- [manager-database](manager-database)

- [manager-hub](manager-hub)

- [manager-master](manager-master)

- [manager-services](manager-services)

- [manager-worker](manager-worker)

- [nginx-basic-auth-proxy](nginx-basic-auth-proxy)

- [nginx-load-balancer](nginx-load-balancer)

- [nginx-load-balancer-api](nginx-load-balancer-api)

- [prometheus](prometheus)

- [registration-client](registration-client)

- [registration-server](registration-server)

- [request-location-monitor](request-location-monitor)

- [registration-client-java](/registration-client-java)

- [registration-client-go](/registration-client-go)

- [registration-client-cpp](/registration-client-cpp)

- [registration-client-csharp](/registration-client-csharp)

- Microserviços
  - Sock shop
    - [carts](../microservices/sock-shop/carts)
    - [rabbitmq](../microservices/sock-shop/rabbitmq)
    - [front-end](../microservices/sock-shop/front-end)
    - [orders](../microservices/sock-shop/orders)
    - [queue-master](../microservices/sock-shop/queue-master)
    - [shipping](../microservices/sock-shop/shipping)
  - Online boutique
    - [adservice](../microservices/online-boutique/src/adservice)
    - [cartservice](../microservices/online-boutique/src/cartservice)
    - [checkoutservice](../microservices/online-boutique/src/checkoutservice)
    - [currencyservice](../microservices/online-boutique/src/currencyservice)
    - [emailservice](../microservices/online-boutique/src/emailservice)
    - [frontend](../microservices/online-boutique/src/frontend)
    - [loadgenerator](../microservices/online-boutique/src/loadgenerator)
    - [paymentservice](../microservices/online-boutique/src/paymentservice)
    - [productcatalogservice](../microservices/online-boutique/src/productcatalogservice)
    - [recommendationservice](../microservices/online-boutique/src/recommendationservice)
    - [shippingservice](../microservices/online-boutique/src/shippingservice)
  - Hotel Reservation
    - [frontend](../microservices/death-star-bench/hotelReservation/services/frontend)
    - [geo](../microservices/death-star-bench/hotelReservation/services/geo)
    - [profile](../microservices/death-star-bench/hotelReservation/services/profile)
    - [rate](../microservices/death-star-bench/hotelReservation/services/rate)
    - [recommendation](../microservices/death-star-bench/hotelReservation/services/recommendation)
    - [reservation](../microservices/death-star-bench/hotelReservation/services/reservation)
    - [search](../microservices/death-star-bench/hotelReservation/services/search)
    - [user](../microservices/death-star-bench/hotelReservation/services/user)
  - Media
    - [CastInfoService](../microservices/death-star-bench/mediaMicroservices/src/CastInfoService)
    - [ComposeReviewService](../microservices/death-star-bench/mediaMicroservices/src/ComposeReviewService)
    - [MovieIdService](../microservices/death-star-bench/mediaMicroservices/src/MovieIdService)
    - [MovieReviewService](../microservices/death-star-bench/mediaMicroservices/src/MovieReviewService)
    - [MovieInfoService](../microservices/death-star-bench/mediaMicroservices/src/MovieInfoService)
    - [PageService](../microservices/death-star-bench/mediaMicroservices/src/PageService)
    - [PlotService](../microservices/death-star-bench/mediaMicroservices/src/PlotService)
    - [RatingService](../microservices/death-star-bench/mediaMicroservices/src/RatingService)
    - [ReviewStorageService](../microservices/death-star-bench/mediaMicroservices/src/ReviewStorageService)
    - [UniqueIdService](../microservices/death-star-bench/mediaMicroservices/src/UniqueIdService)
    - [TextService](../microservices/death-star-bench/mediaMicroservices/src/TextService)
    - [UserReviewService](../microservices/death-star-bench/mediaMicroservices/src/UserReviewService)
    - [UserService](../microservices/death-star-bench/mediaMicroservices/src/UserService)
  - Social Network
    - [UserService](../microservices/death-star-bench/socialNetwork/src/UserService)
    - [TextService](../microservices/death-star-bench/socialNetwork/src/TextService)
    - [UniqueIdService](../microservices/death-star-bench/socialNetwork/src/UniqueIdService)
    - [ComposePostService](../microservices/death-star-bench/socialNetwork/src/ComposePostService)
    - [HomeTimelineService](../microservices/death-star-bench/socialNetwork/src/HomeTimelineService)
    - [PostStorageSerivce](../microservices/death-star-bench/socialNetwork/src/PostStorageSerivce)
    - [SocialGraphService](../microservices/death-star-bench/socialNetwork/src/SocialGraphService)
    - [MediaService](../microservices/death-star-bench/socialNetwork/src/MediaService)
    - [UserMentionService](../microservices/death-star-bench/socialNetwork/src/UrlShortenService)
    - [UserMentionService](../microservices/death-star-bench/socialNetwork/src/UserMentionService)
    - [UserTimelineService](../microservices/death-star-bench/socialNetwork/src/UserTimelineService)
    - [WriteHomeTimelineService](../microservices/death-star-bench/socialNetwork/src/WriteHomeTimelineService)
  - Mixal
    - [movie](../microservices/mixal/movie)
    - [prime](../microservices/mixal/prime)
    - [serve](../microservices/mixal/serve)
    - [webac](../microservices/mixal/webac)
  - Test suite
    - [crash-testing](../microservices/test-suite/crash-testing)
    

### Tools

[<img src="https://i.imgur.com/c6X4nsq.png" alt="" width="48" height="48"> IntelliJ IDEA](https://www.jetbrains.com/idea/) - IntelliJ IDEA is an integrated development environment written in Java for developing computer software

[<img src="https://i.imgur.com/LxlB6ty.png" alt="" width="48" height="48"> CLion](https://www.jetbrains.com/clion/) - A cross-platform IDE for C and C++

The specific tools used in each of the modules can be seen in the respective README.md files:

> [Manager services](manager-services/README.md#ferramentas)  

> [Manager master](manager-master/README.md#ferramentas)  

> [Manager worker](manager-worker/README.md#ferramentas)  

> [Manager hub](manager-hub/README.md#ferramentas)  

> [Request location monitor](request-location-monitor/README.md#ferramentas)  

> [Registration client](registration-client/README.md#ferramentas)  

> [Registration server](registration-server/README.md#ferramentas)  

> [Nginx-load-balancer-api](registration-client/README.md#ferramentas)  

### Configuration

##### Aws

The system uses AWS EC2 instances to host microservices. To configure, follow:

- [Create an account](https://signin.aws.amazon.com/signin?redirect_uri=https%3A%2F%2Fconsole.aws.amazon.com%2Fconsole%2Fhome%3Fstate%3DhashArgs%2523%26isauthcode%3Dtrue&client_id=arn%3Aaws%3Aiam%3A%3A015428540659%3Auser%2Fhomepage&forceMobileApp=0&code_challenge=Gzp7ZBgZKf6PFunBuy7d8chpcB2c9KDZzViYgdhBy1Q&code_challenge_method=SHA-256) in AWS, if you don't already have it. The free version should be enough.

- The dashboard is available [here](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#Home:).

- [Create](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#SecurityGroups:) a Security Group, 
with name `us-manager-security-group`, e an Inbound rule `Custom TCP 22-80 Anywhere`

- [Start an instance](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#LaunchInstanceWizard:) 
t2-micro, based on, for example, Ubuntu Server 20.04 LTS. Save the .pem file in the /manager-master/src/main/resources/aws folder.
Run `chmod 400 file.pem` on the .pem file that was transferred.

- Create an image (ami) from the previously launched instance, in the instance menu [here](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us- east-2#Instances:https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#Instances:),
Image -> Create image. Once created, add the us-manager=true tag.
Replace the ami id in application.yaml, aws.instance.ami property.

- [Create](https://console.aws.amazon.com/iam/home#/users) an *ia* user to access aws resources through,
with Programmatic access access type, and AdministratorAccess policy. Replace the access key and secret access key values ​​in application.yaml, aws.access.key and aws.access.secret-key properties, respectively.

- [Configure keys](https://aws.amazon.com/en/premiumsupport/knowledge-center/ec2-ssh-key-pair-regions/) in all available regions.

### Bugs
- When there are many users accessing the managers: https://stackoverflow.com/questions/32968530/hikaricp-connection-is-not-available
- Error when deleting simulated rules and metrics, and their entities: see associations in database tables.

### License

μsManager is licensed under [MIT license](LICENSE). See the license in the header of the respective file to confirm.
