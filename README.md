# breskul_bibernate

Object-Relational Mapping framework

## How to build project
run in console 
- mvn clean
- mvn install


## How to use in project
- You need add JDBC driver for your DB like dependency
- Add jar file in External Libraries

## Example how to use in class :

```java
@Table(value = "person")
public class Person {
    @Id
    private Long id;
    @Column(value = "name")
    private String name;
}
```

## Annotations description:
### @Table annotation
This annotation set on class and have property value. In this property must set table name in you DB for mapping.
```java
@Table(value = "person")
public class Person {
}
```
### @Id annotation
This annotation set on field and have property value. In this property must set field name in you DB for mapping.
Only for id field. This field shod be long and DB must auto increment him. 
```java
    @Id(value = "id")
    private Long id;
```

### @Column annotation
This annotation set on field and have property value. In this property must set field name in you DB for mapping.
```java
    @Column(value = "name")
    private String name;
```

## How to use:
The main class for api
```java 
Session.java
```
we can get Session from SessionFactory. Firstly we need create PooledDataSource.
```java 
PooledDataSource pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
```
This object set poll of connections with DB.

Then create SessionFactory with poledDataSource 

```java 
SessionFactory sessionFactory = new SessionFactory(pooledDataSource);
```

and SessionFactory can creat Session.
```java 
PooledDataSource pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
SessionFactory sessionFactory = new SessionFactory(pooledDataSource);
Session session = sessionFactory.createSession();
```

```java 
SettingsForSession.class
```
Session can be with SettingsForSession or default.
SettingsForSession have 2 param in constructor.
We can set showSql like boolean if true generated sql will print in console.
Second parameter set enableDirtyChecker default true.

## The main methods of Session or how its work:
```java 
find(final Class<T> classType, final Object id)
```
This method return Object from DB by id. First parameter class with annotation @Entity @Id and @Column.
Second parameter id what about will find entity in DB.

```java
persist(Object entity)
```
Create new record in DB from entity.

```java 
 remove(Object entity)
```
Remove record form DB.

```java
flush()
```
Do operation with BD, by Queue on moment when call method 

```java
close()
```
return connection to poll and do flush, if enableDirtyChecker = true will update all entities witch have changes