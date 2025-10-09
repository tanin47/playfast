package framework

import scala.deriving.Mirror
import scala.util.Try
import scala.compiletime.erasedValue

object Tuples:
  /**
    * Produces a value suitable for case-class unapply:
    * - For single-parameter case classes, returns Option[Param]
    * - For multi-parameter case classes, returns Option[(P1, P2, ...)]
    */
  type UnapplyResult[T] = T match
    case h *: EmptyTuple => h
    case _ => T

  inline def to[A <: Product](value: A)(using mirror: Mirror.ProductOf[A]): Option[UnapplyResult[mirror.MirroredElemTypes]] =
    Try {
      val tuple = Tuple.fromProductTyped(value)
      inline erasedValue[mirror.MirroredElemTypes] match
        case _: (h *: EmptyTuple) =>
          // Convert Tuple1[h] to h for single-field case classes
          tuple.asInstanceOf[Tuple1[Any]]._1
        case _ =>
          // Return the whole tuple for multi-field case classes
          tuple
    }.toOption.asInstanceOf[Option[UnapplyResult[mirror.MirroredElemTypes]]]

  def from[A](value: Product)(using mirror: Mirror.ProductOf[A], ev: value.type <:< mirror.MirroredElemTypes): A =
    mirror.fromProduct(value)
