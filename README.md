# websub-to-eventsource

> WebSub to EventSource - so you can subscribe without hosting a server. Safer, simpler.

... cause you don't always want to go through setting up a web server just to receive notifications.


- https://websub-to-eventsource.herokuapp.com/
- https://devcenter.heroku.com/articles/heroku-postgresql
- https://switchboard.p3k.io/
- https://www.w3.org/TR/websub/
- https://indieweb.org/how-to-push
- https://www.playframework.com/documentation/2.6.x/WSMigration26
- https://www.playframework.com/documentation/2.6.x/StreamsMigration25#Migrating-Enumerators-to-Sources
- https://en.wikipedia.org/wiki/WebSub
- https://websub.rocks/


## Tutorial
To test a subscription, do:
1. Go to https://websub.rocks/subscriber/100
2. Get the URL (eg `https://websub.rocks/blog/100/abcdef`)
3. Execute `curl -i 'https://websub-to-eventsource.herokuapp.com/events?from=https://websub.rocks/blog/100/abcdef'`.
4. Notice a subscription on websub page
5. Press Continue (blue button)
6. Press Create New Post (blue button)
7. Notice that it outputs in your command line :-)


## Clients
You may use other clients, such as [NodeJS EventSource client](https://github.com/EventSource/eventsource)
or [Scala Akka Alpakka Server-Sent-Events Connector](http://developer.lightbend.com/docs/alpakka/current/sse.html).

## Licence

Apache Licence 2.0

## Contributions

There're many holes there, this is only a proof-of-concept.

Contributions to make this much better are very welcome.
