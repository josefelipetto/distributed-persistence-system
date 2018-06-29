# Distributed Persistence Server

This a simple implementation of a distributed persistence server built as an experiment to Distributed System's course.
It contain basic mutual exclusion control, using the Ricart-Agrawala Algorithm and a basic replication algorithm  

The systems offer just two services : A write service, to store new messages and a read service, to get stored messages. 
# Architecture 

[![N|Solid](https://i.imgur.com/j7X5wyn.png)]()

# TODO

* Make it work for 1 - N nodes ( currently, we're running with 3 nodes and some validations are considering this number through a control attribute)
* Make Write service works with one or more services are offline. 

 