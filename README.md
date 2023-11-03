
# What is Fivetran SDK?
A way for partners to create connectors and destinations that run on Fivetran’s platform.

# How does it work?
You build a connector or destination using our SDK in this repo. We’ll deploy it to our platform, and customers can discover it via the Fivetran website, dashboard, and product docs. We’ll tag it with a special designation `partner built` and explain the support process to customers. 

# How do you build?
You write your code in a [gRPC supported language](https://grpc.io/docs/languages/) that can generate a statically linked binary. We recommend java, golang or rust.

# How do we deploy?
Right now we are employing manual processes before getting into automation. You put your code in a public github repo, we pull the code, inspect it and build the executable binary to deploy to our platform. 

# How do we test?
We expect testing to ensure your connector or destination is working as expected, either with test data or with a customer who is eager to adopt. 

## Some recommended tests:
* Handle all data type mappings (between  Fivetran data types and source/destination data types)
* Big data loads
* Big incremental updates
* Narrow event tables
* Wide fact tables

# How do we collaborate on support?
The customer can reach out to either Fivetran or the partner via their support portal. Whoever receives a support ticket will triage it and either fix it and email the customer back or pass it to the other side for fixing, and let the other side notify the customer when the issue is fixed.

Our response times are articulated in our Fivetran Support Policy doc. Bear in mind customers may expect your support response times to be similar. We recommend linking to your own SLAs/Support policies from the docs you create for Fivetran. 

# Create Product Docs
All Fivetran connectors have public docs. For the SDK, we will need you to produce the following:
    * Setup Form ([example](https://fivetran.com/docs/databases/cosmos/setup-guide))
    * Overview Page ([example](https://fivetran.com/docs/databases/cosmos))

We’ll consume these docs and our tech-writing team will edit for grammar and style, sending back to your team (tbd on automation) to keep in sync. 

# How do we go-to-market?
Fivetran will market through multiple methods:
    * Connector release blogs
    * Monthly email updates

## Your connector will appear on:
* Our website in our connector directory
* Our dashboard connector list/search
* Our docs

# I am sold! How do I get started?
Reach out to us via this [intake form](https://forms.gle/KezLXRSn866r7D3J9) and we'll go from there.
The process typically looks like this:
1. Sign NDA
2. Meet with us
3. Sign Partnership Agreement
4. Build the connector/destination
5. Create product docs (overview and setup form)
6. Test it locally
7. Deploy to our platform
8. Test the connector/destination on our platform
9. Support Collaboration
    * Share an email address we can send customer related issues to
    * Share a link on your SLAs
10. Go-to-market with us

# Who can I ask questions to?
Once you sign an NDA, we will create a shared slack channel where we can assist you.