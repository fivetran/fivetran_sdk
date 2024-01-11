# Fivetran Partner SDK
A way for partners to create connectors and destinations that run on [Fivetran’s platform](https://www.fivetran.com/). Read more in our [product docs](https://fivetran.com/docs/partner-built-program). 

## Repo Structure
This repo consists of example connectors and destinations along with a local testing environment. Connectors and destinations are written in a [gRPC supported language](https://grpc.io/docs/languages/) that can generate a statically linked binary. We recommend java, golang or rust. 
* [Examples](examples/)
* [Local Testing Tools](tools/)

## Development
You can follow the [SDK Development Guide](development-guide.md) for guidance on how to develop your code. 

Once you have your code ready to run:
1. Start up your connector running on port 50051 (for destination code, use port 50052)
2. Run the local test environment

Our Readme's for [connectors](tools/connector-tester/) and [destinations](tools/destination-tester/) will take you through setting up the local test environment. 

> To run an example connector or destination from `examples` directory, follow the Readme specific to the example. 

### Other Examples
You can look at the following public repos to see how other partner's have built their connectors:
#### Connectors
* [PlanetScale](https://github.com/planetscale/fivetran-source)
* [Convex](https://github.com/get-convex/convex-fivetran-source)
* [Materialize](https://github.com/MaterializeInc/materialize/tree/main/src/fivetran-destination)
#### Destinations
* [Motherduck](https://github.com/MotherDuck-Open-Source/motherduck-fivetran-connector)

## Partner Process

### How does it work?
You build a connector or destination using our SDK in this repo. We’ll deploy it to our platform, and customers can discover it via the Fivetran website, dashboard, and product docs. We’ll tag it with a `partner-built`  designation and explain the support process to customers. 

### How do we deploy?
Right now we are employing manual processes before getting into automation. You put your code in a public github repo, we pull the code, inspect it, and build the executable binary to deploy to our platform. 

### How do we test?
We expect testing to ensure your connector or destination is working as expected, either with test data or with a customer who is eager to adopt. We provide [testers](tools/) in this repo you can use locally without connecting to Fivetran platform. We also recommend you think through typical scenarios for testing:

* Handle all data type mappings (between Fivetran data types and source/destination data types)
* Big data loads
* Big incremental updates
* Narrow event tables
* Wide fact tables

### How do we collaborate on support?
Once we get customers using your connector or destination, they can reach out to either Fivetran or you via support portals. Whoever receives a support ticket triages it and either fixes the issue and emails the customer back or passes it to the other side for handling.

Our response times are articulated in our [Fivetran Support Policy](https://support.fivetran.com/hc/en-us/articles/5893119459223-Fivetran-Support-Policy) doc. Bear in mind customers may expect your support response times to be similar. We recommend linking to your own SLAs/Support policies from the docs you create for Fivetran. 

### How do we create product docs?
All Fivetran connectors have public docs. For the SDK, we will need you to produce the following:
* Setup Form [example](https://fivetran.com/docs/databases/cosmos/setup-guide)
* Overview Page [example](https://fivetran.com/docs/databases/cosmos)

We’ll consume these docs and our tech-writing team will edit for grammar and style, sending back to your team (tbd on automation) to keep in sync. 

### How do we go-to-market?
Fivetran will market through multiple methods:
* Connector release blogs
* Monthly email updates

Your connector will appear on:
* Our website in our connector directory
* Our dashboard connector list/search
* Our docs

### I am sold! How do I get started?
Reach out to us via this [intake form](https://forms.gle/KezLXRSn866r7D3J9) and we'll go from there.
The process typically looks like this:
1. Explore the [github repo](https://github.com/fivetran/fivetran_sdk)
2. Meet with us to go over the Partnership Agreement
3. Build the connector/destination
4. Create product docs (overview and setup form)
5. Test it locally
6. Deploy to our platform
7. Test the connector/destination on our platform
8. Support Collaboration
    * Share an email address we can send customer related issues to
    * Share a link on your SLAs
9. Go-to-market with us

### How do I get help?
After you meet with us, we will create a shared slack channel where we can assist you.
