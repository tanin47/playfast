package framework

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport}
import slick.ast.BaseTypedType
import slick.jdbc.SetParameter
import slick.lifted.OptionMapper2

import scala.reflect.ClassTag

trait SlickEnumSupport {
  driver: PostgresProfile =>

  trait SlickEnumApi {
    self: ExtPostgresAPI =>
    implicit def baseEnumMapper[T <: Enum[T]](implicit clazz: ClassTag[T]): BaseColumnType[T] = {
      val method = clazz.runtimeClass.getMethod("valueOf", classOf[String])
      MappedJdbcType.base[T, String](
        tmap = _.name(),
        tcomap = (name) => method.invoke(null, name).asInstanceOf[T]
      )
    }

    // See why we need the below here: https://github.com/slick/slick/issues/1986
    implicit def getOptionMapper2TT[B1, B2 <: Enum[B2]: BaseTypedType, P2 <: B2, BR]
      : OptionMapper2[B1, B2, BR, B1, P2, BR] = OptionMapper2.plain.asInstanceOf[OptionMapper2[B1, B2, BR, B1, P2, BR]]
    implicit def getOptionMapper2TO[B1, B2 <: Enum[B2]: BaseTypedType, P2 <: B2, BR]
      : OptionMapper2[B1, B2, BR, B1, Option[P2], Option[BR]] =
      OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, B1, Option[P2], Option[BR]]]
    implicit def getOptionMapper2OT[B1, B2 <: Enum[B2]: BaseTypedType, P2 <: B2, BR]
      : OptionMapper2[B1, B2, BR, Option[B1], P2, Option[BR]] =
      OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[B1], P2, Option[BR]]]
    implicit def getOptionMapper2OO[B1, B2 <: Enum[B2]: BaseTypedType, P2 <: B2, BR]
      : OptionMapper2[B1, B2, BR, Option[B1], Option[P2], Option[BR]] =
      OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[B1], Option[P2], Option[BR]]]
  }
}

trait PostgresProfile extends ExPostgresProfile with SlickEnumSupport with PgArraySupport {
  def pgjson = "jsonb"

  object MyAPI extends ExtPostgresAPI with ArrayImplicits with SlickEnumApi {
    implicit val strListTypeMapper: BaseColumnType[List[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val setSeqString: SetParameter[Seq[String]] =
      SetParameter[Seq[String]]((inputList, params) => inputList.foreach(params.setString))
    implicit val setSeqInt: SetParameter[Seq[Int]] =
      SetParameter[Seq[Int]]((inputList, params) => inputList.foreach(params.setInt))
    implicit val setSeqLong: SetParameter[Seq[Long]] =
      SetParameter[Seq[Long]]((inputList, params) => inputList.foreach(params.setLong))
  }

  override val api: MyAPI.type = MyAPI
}

object PostgresProfile extends PostgresProfile
