package framework

import scala.deriving.Mirror
import scala.util.Try

object Tuples:
  def to[A <: Product](value: A)(using mirror: Mirror.ProductOf[A]): Option[mirror.MirroredElemTypes] =
    Try(Tuple.fromProductTyped(value)).toOption

  def from[A](value: Product)(using mirror: Mirror.ProductOf[A], ev: value.type <:< mirror.MirroredElemTypes): A =
    mirror.fromProduct(value)
