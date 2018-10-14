# frdomain-extras
Additional accompaniment to [Functional and Reactive Domain Modeling](https://github.com/debasishg/frdomain) code samples. The idea is to introduce some of the implementations which are currently not covered in the text or the code samples.

## IO

None of the libraries available during the preparation of the book contained a robust implementation of IO - hence the topic was not discussed in the book. However, in recent times, we are looking at quite a few IO implementations like ..

* [cats IO](https://typelevel.org/cats-effect/datatypes/io.html) as part the [cats-effect](https://github.com/typelevel/cats-effect) library
* [monix Task](https://monix.io/docs/3x/eval/task.html) as part of the [monix](https://monix.io/) library
* [zio](https://github.com/scalaz/scalaz-zio) as part of the [scalaz](https://github.com/scalaz/scalaz) ecosystem

## Domain Modeling with Effects
As with any other modeling exercise we can build domain models at various levels of abstractions. In some cases (possibly with the simplest implementations), we specialize implementations too early. This is ok when we have a very simple implementation at hand. But for any non-trivial implementations, generalization is the way to go. As Lars Hupel said once, *premature specialization is the root of all evil*.

In this accompaniment we will consider effect based domain models at various levels of abstractions. The domain model we will start with is the one we discussed in Chapter 6 of [Functional and Reactive Domain Modeling](https://github.com/debasishg/frdomain).

Chapter 6 of the book contains complete implementations of reactive domain models using the various flavors of non blocking paradigms - `scala.concurrent.Future`, `scalaz.concurrent.Task` etc. I take these implementations to the next step by making them more algebraic, more referentially transparent and more compositional.

## Various Levels of Abstractions

The build in this project now contains 3 implementations:

* Implementation of the domain model using [cats-effect IO](https://github.com/typelevel/cats-effect). This is the simplest one specialized on cats-effect IO.
* Same domain model based on the [tagless final](http://okmij.org/ftp/tagless-final/index.html) approach, with the following concrete implementations:
    * cats-effect IO 
    * Monix Task 

This implementation of tagless final closely follows the [approach](http://debasishg.blogspot.com/2017/07/domain-models-late-evaluation-buys-you.html) that I discussed in one of the blog posts quite some time back.

* Same domain implemented with mtl style APIs from [cats-mtl](https://github.com/typelevel/cats-mtl) alongside the algebraic approach of tagless-final. In order to have optimum performance it eschews the monad transformer stack and uses hand rolled instances of the typeclasses. An [awesome presentation](https://youtu.be/y_QHSDOVJM8) on this topic was given by Pawel Szulc (@rabbitonweb on Twitter). Also Luka Jacobowitz wrote a [nice blogpost](https://typelevel.org/blog/2018/10/06/intro-to-mtl.html) on this topic on the typelevel blog.