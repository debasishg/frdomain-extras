# frdomain-extras
Additional accompaniment to [Functional and Reactive Domain Modeling](https://github.com/debasishg/frdomain) code samples. The idea is to introduce some of the implementations which are currently not covered in the text or the code samples.

## Domain Modeling with Effects
As with any other modeling exercise we can build domain models at various levels of abstractions. In some cases (possibly with the simplest implementations), we specialize implementations too early. This is ok when we have a very simple implementation at hand. But for any non-trivial implementations, generalization is the way to go. As Lars Hupel said once, *premature specialization is the root of all evil*.

In functional programming we like to program with _values_ and values can be reasoned about. Instead of directly writing a piece of code that executes when run, we would like to design our program as a _value_. This is as much a value as an `Int` or a `List[Int]`. This value abstracts the _what_ of a program - it does not have any idea about how it will be executed. This value may be submitted to the runtime that executes it immediately or it can be submitted for later execution in a completely asynchronous manner. But irrespective of how it is executed, the _program as a value_ remains the same in all the cases. Hence it is also not surprising that programs as values can be composed with other compatible values - much like we can combine two lists by appending one to the other. This gives better modularity and reusability.

In this accompaniment we will consider _effects as values_. Note an effect is something expressed in the form of a type constructor `F[A]`, where `A` is the result that the effect computes and `F` is the additional stuff modeling the computation. Some examples of effects are `IO[A]`, `Option[A]`, `Either[A, B]` etc. Instead of directly executing a repository action within a domain service, we will model them as effects and construct a value out of our service. This value can then be composed with other services (which are also modeled as values) to yield larger services. This way we build up abstractions of higher level services incrementally from smaller ones and build our domain model. Finally when we have the complete service definition built we will submit it to the appropriate execution engine by assembling appropriate runtimes for execution. 

The domain model we will start with is the one we discussed in Chapter 6 of [Functional and Reactive Domain Modeling](https://github.com/debasishg/frdomain).

Chapter 6 of the book contains complete implementations of reactive domain models using the various flavors of non blocking paradigms - `scala.concurrent.Future`, `scalaz.concurrent.Task` etc. I take these implementations to the next step by making them more algebraic, more referentially transparent and more compositional.

## Effect Implementations

The build in this project now contains 3 implementations:

* Implementation of the domain model using [cats-effect IO](https://github.com/typelevel/cats-effect). This is the simplest one specialized on cats-effect IO.

* Same domain model based on the [tagless final](http://okmij.org/ftp/tagless-final/index.html) approach, with the following concrete implementations:
    * cats-effect IO 
    * Monix Task 

	This implementation of tagless final closely follows the [approach](http://debasishg.blogspot.com/2017/07/domain-models-late-evaluation-buys-you.html) that I discussed in one of the blog posts quite some time back.

* Same domain implemented with mtl style APIs from [cats-mtl](https://github.com/typelevel/cats-mtl) alongside the algebraic approach of tagless-final. In order to have optimum performance it eschews the monad transformer stack and uses hand rolled instances of the typeclasses. An [awesome presentation](https://youtu.be/y_QHSDOVJM8) on this topic was given by Pawel Szulc (@rabbitonweb on Twitter). Also Luka Jacobowitz wrote a [nice blogpost](https://typelevel.org/blog/2018/10/06/intro-to-mtl.html) on this topic on the typelevel blog.

* The same domain model implemented with [ZIO](https://zio.dev/) using the bifunctor based abstraction `ZIO[R, E, A]`, where `R` is the environment, `E` is the exception that can arise out of the execution and `A` is the result that the effect generates.