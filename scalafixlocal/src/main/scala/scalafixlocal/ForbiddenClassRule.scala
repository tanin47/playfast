package scalafixlocal

import scalafix.v1._
import scala.meta._

class ForbiddenClassRule extends SemanticRule("ForbiddenClass") {

  case class Deprecation(position: Position) extends Diagnostic {
    override def message = "Use framework.Instant instead of java.time.Instant"
  }

  private val deprecatedFunction = SymbolMatcher.normalized("java.time.Instant.now")

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect { case deprecatedFunction(t: Name) =>
      Patch.lint(Deprecation(t.pos))
    }.asPatch
  }
}
