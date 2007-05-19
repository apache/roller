*** Guice Dependency Inject (DI) in Roller, an experiment 

** Goals 

- Get started using DI in Roller so we can simplify Roller bootstrapping
- Don't change the public "Roller API" i.e. RollerFactory and Roller stay
- Maintain same level of back-end pluggability via roller-custom.properties


** Status of the branches/roller_guice

- Roller backend can now starts-up via Guice
- Still need to eliminate RollerFactory from backend
- Still would like to create Manager interface to standardize manager lifecycle


** Advantages

- Easy to define alternative backend modules for testing
- Gives us a way to get rid of the various factories around around
- Less code in RollerFactory


** Implementation notes

- RollerFactory
The factory is now resposible for instantiating the Guice module specified in 
roller.properties like so:
  
   guice.backend.module=org.apache.roller.business.hibernate.HibernateModule

   And here's the new RollerFactory: http://tinyurl.com/ypmeeg

- HibernateModule
A backend module is responsible for binding interface classnames (e.g. Roller)
to interface implementations (e.g. HibernateRollerImpl). The Hibernate module:

   http://tinyurl.com/2bffkt

- RollerImpl
The Roller implementation doesn't create managers anymore, all managers are 
injected by Guice. It's a Guice @Singleton. Here's the new RollerImpl:

   http://tinyurl.com/ynu2me

- HibernateRollerImpl
The HibernateRollerImpl no longer creates managers either, instead it relies
on the fact that it's parent class RollerImpl is injected.

   http://tinyurl.com/yrb4rm
   
- HibernatePersistenceStrategy
The HibernatePersistenceStrategy takes care of it's own initialization using
RollerConfig (someday with a DatabaseProvider). It's a Guice @Singleton too.

- Managers:
The managers all use constructor injection now. The Hibernate managers expect 
to get their strategy and Roller instance (if required) via injection. I kept 
RollerFactory in the "public API" so there is zero impact on front-end code, 
but all references to RollerFactory should be eliminated in the back-end 
(that's work still to be done).

- Added Roller.init() method due to circular dependencies
Some managers need access to a Roller object as part of their implemenation,
so I've added a Roller.init() method. I'd like create a Manager interface as
we have in Planet to standardize manager lifecycle.


Notes on Guice

- Simple small API that is very easy to understand
- Documentation is short and to the point
- Error messages are very good
- No horrible XML files to write

