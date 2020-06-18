package frdomain.ch6.domain.ext

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import skunk.Codec

object skunkx {

  implicit class CodecOps[B](codec: Codec[B]) {
    def cimap[A: Coercible[B, *]](implicit ev: Coercible[A, B]): Codec[A] =
      codec.imap(_.coerce[A])((ev(_)))
  }

}