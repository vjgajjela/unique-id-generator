# unique-id-generator
Spring boot library for generating custom unique id's using twitter's snowflake algorithm


# How to configure
The following properties need to be added to the micro-service application.properties /yml to use unique ID service.
<br/>

## The condition used to initialize beans required for unique-id service
unique_id.init=true
## The character set provided is used for id generation, this optional by default it will use set 0-9A-Za-z
unique_id.charset = 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
## The length of the id to be, this can vary from 15 or more based on the node and sequence bits selection
unique_id.length = 16
## Custom epoch, optional and default set to 1420070400000
unique_id.custom_epoch = 1420070400000
## Node bits, 7 will give 0 to 127 nodes (2^7)
unique_id.node_bits = 7
## Sequence bits, local counter for the id, 5 will give 0 to 32 sequences per millisecond
unique_id.sequence_bits = 5


# How to use
Add below code in your Spring boot application bean
<br/><br/>

@Autowired
<br/>
	UniqueIdGeneratorService idGeneratorService;

Numeric id : 	   idGeneratorService.generateNumericId()	
<br/>
Alpha-numeric id : idGeneratorService.generateAlphaNumericId()
<br/><br/>
Do not forget to add the component scan either.

