# frdomain-extras
Additional accompaniment to [Functional and Reactive Domain Modeling](https://github.com/debasishg/frdomain) code samples. The idea is to introduce some of the implementations which are currently not covered in the text or the code samples.

## IO

None of the libraries available during the preparation of the book contained a robust implementation of IO - hence the topic was not discussed in the book. However, in recent times, we are looking at quite a few IO implementations like ..

* [cats IO](https://typelevel.org/cats-effect/datatypes/io.html) as part the [cats-effect](https://github.com/typelevel/cats-effect) library
* [monix Task](https://monix.io/docs/3x/eval/task.html) as part of the [monix](https://monix.io/) library
* [zio](https://github.com/scalaz/scalaz-zio) as part of the [scalaz](https://github.com/scalaz/scalaz) ecosystem

This accompaniment contains an implementation of the following usecases as part of demonstrating the idea of using the principles of functional programming while implementing domain models with side-effects.

Chapter 6 of the book contains complete implementations of reactive domain models using the various flavors of non blocking paradigms - `scala.concurrent.Future`, `scalaz.concurrent.Task` etc. I take these implementations to the next step by making them more algebraic, more referentially transparent and more compositional.

The build in this project now contains 2 implementations:

* Implementation of the domain model using cats-effect IO
* Same domain model based on the [tagless final](http://okmij.org/ftp/tagless-final/index.html) approach, with cats-effect IO as one of the possible concrete implementations. This implementation closely follows the [approach](http://debasishg.blogspot.com/2017/07/domain-models-late-evaluation-buys-you.html) that I discussed in one of the blog posts quite some time back.